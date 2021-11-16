package bitimage.storage.postgres.dao;

import bitimage.storage.dto.ImageDTO;
import bitimage.storage.dto.TagDTO;
import bitimage.storage.postgres.query.QueryExecutor;
import bitimage.storage.postgres.query.SQLQuery;
import bitimage.storage.postgres.query.Transaction;
import bitimage.storage.postgres.resultset.ImageExpandedViewResultSetMapper;
import bitimage.storage.postgres.resultset.ImageResultSetMapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ImageDAO
{
    private final QueryExecutor queryExecutor;

    public ImageDAO(QueryExecutor queryExecutor)
    {
        this.queryExecutor = queryExecutor;
    }

    /**
     * Creates table for storing image metadata, with
     * multiple indexes to optimize for faster read
     * performance, based on column usage in select clauses.
     */
    public void createImagesTable()
            throws Exception
    {
        String sql = """
            CREATE TABLE IF NOT EXISTS public.images (
              id UUID NOT NULL DEFAULT uuid_generate_v4(),
              name VARCHAR(250) NOT NULL,
              user_id UUID NOT NULL,
              hash_md5 UUID NOT NULL,
              size_bytes BIGINT NOT NULL,
              file_format VARCHAR(50) NOT NULL,
              is_private BOOLEAN NOT NULL,
              created_at TIMESTAMP NOT NULL,
              updated_at TIMESTAMP NOT NULL,
              CONSTRAINT pk_images PRIMARY KEY(id),
              CONSTRAINT fk_images_to_users FOREIGN KEY(user_id)
                REFERENCES public.users(id)
                ON DELETE CASCADE
            );

            CREATE INDEX IF NOT EXISTS ix_images_user_id
              ON public.images(user_id);

            CREATE INDEX IF NOT EXISTS ix_images_user_id_id
              ON public.images(user_id, id);

            CREATE INDEX IF NOT EXISTS ix_images_user_id_name
              ON public.images(user_id, name);

            CREATE INDEX IF NOT EXISTS ix_images_updated_at
              ON public.images(updated_at DESC);
        """;

        queryExecutor.write(new SQLQuery(sql));
    }

    public void createTagsTable()
            throws Exception
    {
        String sql = """
            CREATE TABLE IF NOT EXISTS public.tags (
              id UUID NOT NULL,
              name VARCHAR(250) NOT NULL,
              CONSTRAINT pk_tags PRIMARY KEY(id)
            );

            CREATE UNIQUE INDEX IF NOT EXISTS ux_tags_name
              ON public.tags(name);
        """;

        queryExecutor.write(new SQLQuery(sql));
    }

    /**
     * Creates junction table used to map many-to-many
     * relationship between images and tags, storing a
     * single image id associated to a tag id.
     */
    public void createImageTagLinkTable()
            throws Exception
    {
        String sql = """
            CREATE TABLE IF NOT EXISTS public.image_tags (
              image_id UUID NOT NULL,
              tag_id UUID NOT NULL,
              created_at TIMESTAMP NOT NULL,
              updated_at TIMESTAMP NOT NULL,
              CONSTRAINT pk_image_tags PRIMARY KEY(image_id, tag_id),
              CONSTRAINT fk_image_tags_to_images FOREIGN KEY(image_id)
                REFERENCES public.images(id)
                ON DELETE CASCADE,
              CONSTRAINT fk_image_tags_to_tags FOREIGN KEY(tag_id)
                REFERENCES public.tags(id)
                ON DELETE CASCADE
            );
        """;

        queryExecutor.write(new SQLQuery(sql));
    }

    /**
     * Creates a new function that will remove all tags that
     * are not associated with an image (either because the
     * image or user was deleted).
     */
    public void createFunctionToDeleteOrphanedTags()
            throws Exception
    {
        String sql = """
            CREATE OR REPLACE FUNCTION delete_orhpaned_tags()
            RETURNS TRIGGER LANGUAGE PLPGSQL

            AS $$
              BEGIN
                DELETE FROM
                  public.tags USING public.tags t
                  LEFT JOIN public.image_tags it ON t.id = it.tag_id
                WHERE
                  tags.id = t.id
                  AND it.tag_id IS NULL;

                RETURN NULL;
              END;
            $$
        """;

        queryExecutor.write(new SQLQuery(sql));
    }

    /**
     * Creates trigger that executes function to delete
     * all tags that aren't linked to an image after a
     * user record or image record is deleted.
     * <p>
     * This is needed because the tags table has no foreign
     * key reference to the images or users table, so deletes
     * cannot be cascaded.
     *
     * @precondition SQL function "delete_orphaned_tags" must
     * exist, by calling createFunctionToDeleteOrphanedTags().
     */
    public void createTriggerToDeleteOrphanedTags()
            throws Exception
    {
        String sql = """
            DROP TRIGGER IF EXISTS delete_orphaned_tags_after_user_deleted
              ON public.users;

            DROP TRIGGER IF EXISTS delete_orphaned_tags_after_images_deleted
              ON public.images;

            CREATE TRIGGER delete_orphaned_tags_after_user_deleted
            AFTER DELETE
              ON public.users
              EXECUTE PROCEDURE delete_orhpaned_tags();

            CREATE TRIGGER delete_orphaned_tags_after_images_deleted
            AFTER DELETE
              ON public.images
              EXECUTE PROCEDURE delete_orhpaned_tags();
        """;

        queryExecutor.write(new SQLQuery(sql));
    }

    /**
     * Inserts a collection of images, and each
     * image's tags within a transaction.
     */
    public void insertImages(List<ImageDTO> imageDTOs)
            throws Exception
    {
        Transaction transaction = queryExecutor.newTransaction();

        for (ImageDTO imageDTO : imageDTOs) {
            SQLQuery insertImageQuery = new SQLQuery(
                    getInsertImageSQL(),
                    getInsertImageParams(imageDTO));

            transaction.send(insertImageQuery);
            insertImageTags(transaction, imageDTO.id, imageDTO.tag_dtos);
        }

        queryExecutor.commit(transaction);
    }

    /**
     * Inserts an image's tags as part of transaction
     * when inserting an image.
     *
     * @postcondition Transaction object must be committed.
     */
    private void insertImageTags(
            Transaction transaction,
            UUID imageID,
            List<TagDTO> tagDTOs)
    {
        for (TagDTO tagDTO : tagDTOs) {
            SQLQuery insertTagQuery = new SQLQuery(
                    getInsertTagSQL(),
                    List.of(tagDTO.id, tagDTO.name));

            SQLQuery insertImageTagLinkQuery = new SQLQuery(
                    getInsertImageTagLinkSQL(),
                    getInsertImageTagLinkParams(imageID, tagDTO));

            transaction
                    .send(insertTagQuery)
                    .send(insertImageTagLinkQuery);
        }
    }

    private static String getInsertImageSQL()
    {
        return """
            INSERT INTO public.images (
              id,
              name,
              size_bytes,
              user_id,
              hash_md5,
              file_format,
              is_private,
              created_at,
              updated_at
            ) VALUES
              (?, ?, ?, ?, ?::uuid, ?, ?, ?, ?);
        """;
    }

    private static List<Object> getInsertImageParams(ImageDTO imageDTO)
    {
        return List.of(
                imageDTO.id,
                imageDTO.name,
                imageDTO.size_bytes,
                imageDTO.user_id,
                imageDTO.hash_md5,
                imageDTO.file_format,
                imageDTO.is_private,
                imageDTO.created_at,
                imageDTO.updated_at);
    }

    /**
     * Query for inserting a generic tag record.
     *
     * <p>If the tag already exists by name, nothing should
     * happen since this tag can be linked to images owned by
     * different users.
     */
    private static String getInsertTagSQL()
    {
        return """
            INSERT INTO public.tags (
              id,
              name
            ) VALUES
              (?, ?)
            ON CONFLICT DO NOTHING;
        """;
    }

    /**
     * Query for inserting record that links an image
     * to a tag by ids.
     *
     * <p>Because tags table is normalized, the tag id
     * (linked to the image) is found for given the tag
     * name. This reduces redundant tag records in tags
     * table.
     */
    private static String getInsertImageTagLinkSQL()
    {
        return """
            INSERT INTO public.image_tags (
              image_id,
              tag_id,
              created_at,
              updated_at
            ) (
              SELECT
                ?, id, ?, ?
              FROM
                public.tags
              WHERE
              name = ?
            ) ON CONFLICT DO NOTHING;
        """;
    }

    public static List<Object> getInsertImageTagLinkParams(
            UUID imageID,
            TagDTO tagDTO)
    {
        return List.of(
                imageID,
                tagDTO.created_at,
                tagDTO.updated_at,
                tagDTO.name);
    }

    public ImageDTO selectImageByID(UUID userID, UUID imageID)
            throws Exception
    {
        List<ImageDTO> results = queryExecutor.read(
                new SQLQuery(getSelectImageByIDSQL(), List.of(userID, imageID)),
                new ImageExpandedViewResultSetMapper());

        if (results.isEmpty()) {
            return new ImageDTO().asNull();
        }

        return results.get(0);
    }

    /**
     * Selects image row by id, with joined to link table for images to tags. A single image row's
     * tags are projected into a json array to simplify mapping to application objects.
     *
     * <p>If an image has no tags, for the tags column, PostgreSQL returns [null], which is converted
     * to an empty json list.
     */
    private static String getSelectImageByIDSQL()
    {
        return """
            SELECT
              i.id AS id,
              i.name AS name,
              i.user_id AS user_id,
              i.is_private AS is_private,
              i.size_bytes AS size_bytes,
              i.file_format AS file_format,
              REPLACE(i.hash_md5 :: text, '-', '') AS hash_md5,
              COALESCE(
                NULLIF(json_agg(DISTINCT t.*) :: text, '[null]'),
                '[]'
              ) AS tags,
              COALESCE(
                NULLIF(json_agg(DISTINCT cl.*) :: text, '[null]'),
                '[]'
              ) AS content_labels
            FROM
              public.images i
              LEFT JOIN public.image_tags it ON i.id = it.image_id
              LEFT JOIN public.tags t ON it.tag_id = t.id
              LEFT JOIN public.image_content_labels icl ON i.id = icl.image_id
              LEFT JOIN public.content_labels cl ON icl.label_id = cl.id
            WHERE
              (
                i.user_id = ? OR
                i.is_private = FALSE
              ) AND
              i.id = ?
            GROUP BY
              i.id;
        """;
    }

    /**
     * Selects all publicly available images, limiting by the 100 latest updated images.
     */
    public List<ImageDTO> selectAllPublicImages()
            throws Exception
    {
        String sql = """
            SELECT
              id,
              name,
              user_id,
              is_private,
              size_bytes,
              file_format,
              REPLACE(hash_md5 :: text, '-', '') AS hash_md5
            FROM
              public.images
            WHERE
              is_private = FALSE
            ORDER BY
              updated_at DESC
            LIMIT 100;
            """;

        return queryExecutor.read(
                new SQLQuery(sql),
                new ImageResultSetMapper());
    }

    /**
     * Selects all user's images, utilizing descending order index to quickly return all results
     * ordered by the most recently updated (or created) images.
     */
    public List<ImageDTO> selectAllUserImages(UUID userID)
            throws Exception
    {
        String sql = """
            SELECT
              id,
              name,
              user_id,
              is_private,
              size_bytes,
              file_format,
              REPLACE(hash_md5 :: text, '-', '') AS hash_md5
            FROM
              public.images
            WHERE
              user_id = ?
            ORDER BY
              updated_at DESC;
        """;

        return queryExecutor.read(
                new SQLQuery(sql, List.of(userID)),
                new ImageResultSetMapper());
    }

    /**
     * Selects and filters for all images that match by name. Returned images are either be public or
     * owned by user.
     *
     * <p>Image name is compared case-insensitively.
     */
    public List<ImageDTO> selectImagesByName(UUID userID, String imageName)
            throws Exception
    {
        String sql = """
            SELECT
              id,
              name,
              user_id,
              is_private,
              size_bytes,
              file_format,
              REPLACE(hash_md5::text, '-', '') AS hash_md5
            FROM
              public.images
            WHERE
              (
                user_id = ? OR
                is_private = FALSE
              ) AND
              name ILIKE ?;
        """;

        return queryExecutor.read(
                new SQLQuery(sql, List.of(userID, imageName + "%")),
                new ImageResultSetMapper());
    }

    /**
     * Selects and filters for all user's images that have an associate matching tag name. Tag names
     * are compared "fuzzily".
     *
     * <p>Tag names are compared case-insensitively.
     */
    public List<ImageDTO> selectImagesByTag(UUID userID, String tagName)
            throws Exception
    {
        String sql = """
            SELECT
              DISTINCT i.id AS id,
              i.name AS name,
              i.user_id AS user_id,
              i.is_private AS is_private,
              i.size_bytes AS size_bytes,
              i.file_format AS file_format,
              REPLACE(i.hash_md5::text, '-', '') AS hash_md5
            FROM
              public.images i
              INNER JOIN public.image_tags it ON i.id = it.image_id
              INNER JOIN public.tags t ON it.tag_id = t.id
            WHERE
              (
                user_id = ? OR
                is_private = FALSE
              ) AND
              t.name ILIKE ?;
        """;

        return queryExecutor.read(
                new SQLQuery(sql, List.of(userID, tagName + "%")),
                new ImageResultSetMapper());
    }

    public List<ImageDTO> selectImagesByContentLabel(UUID userID, String labelName)
            throws Exception
    {
        String sql = """
            SELECT
              DISTINCT i.id AS id,
              i.name AS name,
              i.user_id AS user_id,
              i.is_private AS is_private,
              i.size_bytes AS size_bytes,
              i.file_format AS file_format,
              REPLACE(i.hash_md5 :: text, '-', '') AS hash_md5
            FROM
              public.images i
              INNER JOIN public.image_content_labels icl ON i.id = icl.image_id
              INNER JOIN public.content_labels cl ON icl.label_id = cl.id
            WHERE
              (
                user_id = ? OR
                is_private = FALSE
              ) AND
              AND cl.name ILIKE ?;
        """;

        return queryExecutor.read(
                new SQLQuery(sql, List.of(userID, labelName + "%")),
                new ImageResultSetMapper());
    }

    /**
     * Deletes a user's images by image ids. Orphaned tags
     * (which are not linked to an image) are also deleted.
     * <p>
     * Only an image's owner can delete it.
     *
     * <p>Images are deleted within a transaction that throws
     * an exception when attempting to delete non existent
     * images. Postgres JDBC driver does not allow prepared
     * query parameters within explicit transactions, so ids
     * (which are pre-validated) are injected.
     *
     * <p>This is done to enforce data integrity between rdbms
     * and AWS S3, by preventing a request to delete a newly
     * uploaded images files whose metadata has not yet been
     * written to rdbms.
     */
    public void deleteImagesByID(UUID userID, List<UUID> imageIDs)
            throws Exception
    {
        String imageIDsToDelete = uuidListToString(imageIDs);
        String sql = String.format("""
            DO $$
            BEGIN
              DELETE FROM
                public.images
              WHERE
                user_id = '%s' AND
                id = ANY (string_to_array('%s', ',')::uuid[]);
              IF NOT FOUND THEN
                raise exception 'No rows affected (Given bad id)';
              END IF;
            END
            $$;
        """, userID.toString(), imageIDsToDelete);

        queryExecutor.write(new SQLQuery(sql));
    }

    /**
     * Converts list of UUIDs to comma delimited string of UUIDs.
     *
     * <p>This method is needed because the JDBC driver does
     * not accept a Java UUID typed array or list as an argument.
     */
    private static String uuidListToString(List<UUID> ids)
    {
        List<String> uuidStrings = ids.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());

        return String.join(",", uuidStrings);
    }

    public void deleteImagesByUserID(UUID userID)
            throws Exception
    {
        String sql = """
            DELETE FROM
              public.images
            WHERE
              user_id = ?;
        """;

        queryExecutor.write(new SQLQuery(sql, List.of(userID)));
    }
}

package bitimage.storage.dao;

import bitimage.storage.adapters.dto.ImageDTO;
import bitimage.storage.adapters.dto.TagDTO;
import bitimage.storage.pg.query.QueryExecutor;
import bitimage.storage.pg.query.SQLQuery;
import bitimage.storage.pg.query.Transaction;
import bitimage.storage.pg.resultset.ImageExpandedViewResultSetMapper;
import bitimage.storage.pg.resultset.ImageResultSetMapper;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ImageDAO {

  private final QueryExecutor queryExecutor;

  public ImageDAO(QueryExecutor queryExecutor) {
    this.queryExecutor = queryExecutor;
  }

  public void createImagesTable() throws Exception {
    final String sql =
        """
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

    this.queryExecutor.write(new SQLQuery(sql));
  }

  public void createTagsTable() throws Exception {
    final String sql =
        """
			CREATE TABLE IF NOT EXISTS public.tags (
					id UUID NOT NULL,
					name VARCHAR(250) NOT NULL,
					CONSTRAINT pk_tags PRIMARY KEY(id)
			);

			CREATE UNIQUE INDEX IF NOT EXISTS ux_tags_name
					ON public.tags(name);
		""";

    this.queryExecutor.write(new SQLQuery(sql));
  }

  /**
   * Creates junction table used to map many-to-many relationship between images and tags, storing a
   * single image id associated to a tag id.
   */
  public void createImageTagLinkTable() throws Exception {
    final String sql =
        """
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

    this.queryExecutor.write(new SQLQuery(sql));
  }

  public void createFunctionToDeleteOrphanedTags() throws Exception {
    final String sql =
        """
          CREATE OR REPLACE FUNCTION delete_orhpaned_tags()
          RETURNS TRIGGER LANGUAGE PLPGSQL
          AS $$
          BEGIN
            DELETE FROM
                public.tags
            USING
					public.tags t
					LEFT JOIN public.image_tags it ON t.id = it.tag_id
                WHERE
					tags.id = t.id AND
					it.tag_id IS NULL;

				RETURN NULL;
            END;
			$$
		""";

    this.queryExecutor.write(new SQLQuery(sql));
  }

  /**
   * Creates trigger that executes function to delete all tags that aren't linked to an image after
   * a user record or image record is deleted.
   *
   * <p>This is needed because the tags table has no foreign key reference to the images or users
   * table, so deletes cannot be cascaded.
   *
   * @precondition SQL function "delete_orhpaned_tags" must exist, by calling
   *     createFunctionToDeleteOrphanedTags()
   */
  public void createTriggerToDeleteOrphanedTags() throws Exception {
    final String sql =
        """
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

    this.queryExecutor.write(new SQLQuery(sql));
  }

  /** Inserts a collection of images, and each image's tags within a transaction. */
  public void insertImages(List<ImageDTO> imageDTOs) throws Exception {
    final Transaction tx = this.queryExecutor.newTransaction();

    for (ImageDTO imageDTO : imageDTOs) {

      final String insertImageSQL = this.getInsertImageSQL();
      final List<Object> insertImageParams = this.getInsertImageParams(imageDTO);

      tx.send(new SQLQuery(insertImageSQL, insertImageParams));
      this.insertImageTags(tx, imageDTO.id, imageDTO.tag_dtos);
    }

    this.queryExecutor.commit(tx);
  }

  /**
   * Inserts an image's tags as part of transaction when inserting an image.
   *
   * @postcondition Transaction object must be committed
   */
  private final void insertImageTags(Transaction tx, UUID imageID, List<TagDTO> tagDTOs)
      throws Exception {

    for (TagDTO tagDTO : tagDTOs) {

      final String insertTagSQL = this.getInsertTagSQL();
      final List<Object> insertTagParams = Arrays.asList(tagDTO.id, tagDTO.name);

      final String insertImageTagLinkSQL = this.getInsertImageTagLinkSQL();
      final List<Object> insertImageTagLinkParams =
          this.getInsertImageTagLinkParams(imageID, tagDTO);

      tx.send(new SQLQuery(insertTagSQL, insertTagParams))
          .send(new SQLQuery(insertImageTagLinkSQL, insertImageTagLinkParams));
    }
  }

  private String getInsertImageSQL() {
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

  private final List<Object> getInsertImageParams(ImageDTO imageDTO) {
    return Arrays.asList(
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
   * <p>If the tag already exists by name, nothing should happen since this tag can be linked to
   * images owned by different users.
   */
  private String getInsertTagSQL() {
    return """
			INSERT INTO public.tags (
				id,
				name
			) VALUES
				(?, ?)
			ON CONFLICT
				DO NOTHING;
		""";
  }

  /**
   * Query for inserting record that links an image to a tag by ids.
   *
   * <p>Because tags table is normalized, the tag id (linked to the image) is found for given the
   * tag name. This reduces redundant tag records in tags table.
   */
  private String getInsertImageTagLinkSQL() {
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
			) ON CONFLICT
					DO NOTHING;
		""";
  }

  public List<Object> getInsertImageTagLinkParams(UUID imageID, TagDTO tagDTO) {
    return Arrays.asList(imageID, tagDTO.created_at, tagDTO.updated_at, tagDTO.name);
  }

  /**
   * Selects image row by id, with joined to link table for images to tags. A single image row's
   * tags are projected into a json array to simplify mapping to application objects.
   *
   * <p>If an image has no tags, for the tags column, PostgreSQL returns [null], which is converted
   * to an empty json list.
   */
  public ImageDTO selectImageByID(UUID userID, UUID imageID) throws Exception {
    final String sql = this.getSelectImageByIDSQL();
    final List<Object> params = Arrays.asList(userID, imageID);

    final List<ImageDTO> results =
        this.queryExecutor.<ImageDTO>read(
            new SQLQuery(sql, params), new ImageExpandedViewResultSetMapper());

    if (results.isEmpty()) {
      return new ImageDTO().asNull();
    }

    return results.get(0);
  }

  private String getSelectImageByIDSQL() {
    return """
		SELECT
				i.id AS id,
				i.name AS name,
				i.user_id AS user_id,
				i.is_private AS is_private,
				i.size_bytes AS size_bytes,
				i.file_format AS file_format,
				REPLACE(i.hash_md5::text, '-', '') AS hash_md5,
				COALESCE( NULLIF(json_agg(DISTINCT t.*)::text, '[null]'), '[]' ) AS tags,
				COALESCE( NULLIF(json_agg(DISTINCT cl.*)::text, '[null]'), '[]' ) AS content_labels
 		FROM
				public.images i
				LEFT JOIN
			 			public.image_tags it
			 			ON i.id = it.image_id
				LEFT JOIN
			 			public.tags t
			 			ON it.tag_id = t.id
				LEFT JOIN
			 			public.image_content_labels icl
			 			ON i.id = icl.image_id
				LEFT JOIN
			 			public.content_labels cl
			 			ON icl.label_id = cl.id
 		WHERE
				i.user_id = ?
				AND i.id = ?
 		GROUP BY
				i.id;
		""";
  }

  /**
   * Selects all user's images, utilizing descending order index to quickly return all results
   * ordered by the most recently updated (or created) images.
   */
  public List<ImageDTO> selectAllUserImages(UUID userID) throws Exception {
    final String sql =
        """
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
						user_id = ?
					ORDER BY
						updated_at DESC;
				""";

    final List<Object> params = Arrays.asList(userID);

    return this.queryExecutor.<ImageDTO>read(new SQLQuery(sql, params), new ImageResultSetMapper());
  }

  public List<ImageDTO> selectImagesByName(UUID userID, String imageName) throws Exception {
    final String sql =
        """
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
						user_id = ? AND
						name ILIKE ?;
				""";

    final List<Object> params = Arrays.asList(userID, imageName + "%");

    return this.queryExecutor.<ImageDTO>read(new SQLQuery(sql, params), new ImageResultSetMapper());
  }

  /**
   * Selects and filters for all user's images that have an associate matching tag name. Tag names
   * are compared "fuzzily".
   *
   * <p>Tag names are compared case-insensitively.
   */
  public List<ImageDTO> selectImagesByTag(UUID userID, String tagName) throws Exception {
    final String sql =
        """
					SELECT DISTINCT
						i.id AS id,
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
						i.user_id = ? AND
						t.name ILIKE ?
					GROUP BY
						i.id;
			  """;

    final List<Object> params = Arrays.asList(userID, tagName + "%");

    return this.queryExecutor.<ImageDTO>read(new SQLQuery(sql, params), new ImageResultSetMapper());
  }

  public List<ImageDTO> selectImagesByContentLabel(UUID userID, String labelName) throws Exception {
    final String sql =
        """
					SELECT DISTINCT
						i.id AS id,
						i.name AS name,
						i.user_id AS user_id,
						i.is_private AS is_private,
						i.size_bytes AS size_bytes,
						i.file_format AS file_format,
						REPLACE(i.hash_md5::text, '-', '') AS hash_md5
					FROM
						public.images i
						INNER JOIN public.image_content_labels icl ON i.id = icl.image_id
						INNER JOIN public.content_labels cl ON icl.label_id = cl.id
					WHERE
						i.user_id = ? AND
						cl.name ILIKE ?;
				""";

    final List<Object> params = Arrays.asList(userID, labelName + "%");

    return this.queryExecutor.<ImageDTO>read(new SQLQuery(sql, params), new ImageResultSetMapper());
  }

  /**
   * Deletes a user's images by image ids. Orphaned tags (which are not linked to an image) are also
   * deleted.
   *
   * <p>Images are deleted within a transaction that throws an exception when attempting to delete
   * non existent images. Postgres JDBC driver does not allow prepared query parameters within
   * explicit transactions, so ids (which are pre-validated) are injected.
   *
   * <p>This is done to enforce data integrity between rdbms and AWS S3, by preventing a request to
   * delete a newly uploaded images files whose metadata has not yet been written to rdbms.
   */
  public void deleteImagesByID(UUID userID, List<UUID> imageIDs) throws Exception {
    final String imageIDsToDelete = this.uuidListToString(imageIDs);
    final String sql =
        """
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
				"""
            .formatted(userID.toString(), imageIDsToDelete);

    this.queryExecutor.write(new SQLQuery(sql));
  }

  /**
   * Converts list of UUIDs to comma delimited string of UUIDs.
   *
   * <p>This method is needed because the JDBC driver does not accept a Java UUID typed array or
   * list as an argument.
   */
  private String uuidListToString(List<UUID> ids) {
    final List<String> uuidStrings = ids.stream().map(UUID::toString).collect(Collectors.toList());

    return String.join(",", uuidStrings);
  }

  public void deleteImagesByUserID(UUID userID) throws Exception {
    final String sql = """
			DELETE FROM
				public.images
			WHERE
				user_id = ?;
		""";

    final List<Object> params = Arrays.asList(userID);

    this.queryExecutor.write(new SQLQuery(sql, params));
  }
}

package bitimage.storage.postgres.dao;

import bitimage.storage.dto.LabelDTO;
import bitimage.storage.postgres.query.QueryExecutor;
import bitimage.storage.postgres.query.SQLQuery;
import bitimage.storage.postgres.query.Transaction;

import java.util.List;

public class LabelDAO
{
    private final QueryExecutor queryExecutor;

    public LabelDAO(QueryExecutor queryExecutor)
    {
        this.queryExecutor = queryExecutor;
    }

    public void createLabelsTable()
            throws Exception
    {
        String sql = """
            CREATE TABLE IF NOT EXISTS public.content_labels (
              id UUID NOT NULL,
              name VARCHAR(250) NOT NULL,
              content_category VARCHAR(250) NOT NULL,
              CONSTRAINT pk_content_labels PRIMARY KEY(id),
              CONSTRAINT uk_content_labels_name_content_category UNIQUE(name, content_category)
            );

            CREATE INDEX IF NOT EXISTS ux_content_labels_name_content_category
              ON public.content_labels(name, content_category);
        """;

        queryExecutor.write(new SQLQuery(sql));
    }

    public void createImageLabelLinkTable()
            throws Exception
    {
        String sql = """
            CREATE TABLE IF NOT EXISTS public.image_content_labels (
              image_id UUID NOT NULL,
              label_id UUID NOT NULL,
              label_confidence_score DOUBLE PRECISION NOT NULL,
              created_at TIMESTAMP WITHOUT TIME ZONE,
              updated_at TIMESTAMP WITHOUT TIME ZONE,
              CONSTRAINT pk_image_content_labels PRIMARY KEY(image_id, label_id),
              CONSTRAINT fk_image_content_labels_to_images FOREIGN KEY(image_id)
                REFERENCES public.images(id)
                ON DELETE CASCADE,
              CONSTRAINT fk_image_content_labels_to_content_labels FOREIGN KEY(label_id)
                REFERENCES public.content_labels(id)
                ON DELETE CASCADE
            );
        """;

        queryExecutor.write(new SQLQuery(sql));
    }

    public void createFunctionToDeleteOrphanedLabels()
            throws Exception
    {
        String sql = """
            CREATE OR REPLACE FUNCTION delete_orhpaned_content_labels()
            RETURNS TRIGGER LANGUAGE PLPGSQL

            AS $$
            BEGIN
              DELETE FROM
                public.content_labels
              USING
                public.content_labels cl
                LEFT JOIN public.image_content_labels icl ON cl.id = icl.label_id
              WHERE
                content_labels.id = cl.id AND
                icl.label_id IS NULL;

              RETURN NULL;
            END;
            $$
        """;

        queryExecutor.write(new SQLQuery(sql));
    }

    /**
     * Creates trigger that executes function to delete all
     * labels that aren't linked to an image after a user
     * record or image record is deleted.
     *
     * <p>This is needed because the labels table has no foreign
     * key reference to the images or users table, so deletes
     * cannot be cascaded.
     *
     * @precondition SQL function "delete_orphaned_content_labels"
     * must exist, by calling createFunctionToDeleteOrphanedTags().
     */
    public void createTriggerToDeleteOrphanedLabels()
            throws Exception
    {
        String sql = """
            DROP TRIGGER IF EXISTS delete_orphaned_content_labels_after_user_deleted
              ON public.users;

            DROP TRIGGER IF EXISTS delete_orphaned_content_labels_after_images_deleted
              ON public.images;

            CREATE TRIGGER delete_orphaned_content_labels_after_user_deleted
              AFTER DELETE ON public.users
              EXECUTE PROCEDURE delete_orhpaned_content_labels();

            CREATE TRIGGER delete_orphaned_content_labels_after_images_deleted
              AFTER DELETE ON public.images
              EXECUTE PROCEDURE delete_orhpaned_content_labels();
        """;

        queryExecutor.write(new SQLQuery(sql));
    }

    public void insertLabels(List<LabelDTO> labelDTOs)
            throws Exception
    {
        Transaction transaction = queryExecutor.newTransaction();

        for (LabelDTO labelDTO : labelDTOs) {
            SQLQuery insertLabelQuery = new SQLQuery(
                    getInsertLabelSQL(),
                    getInsertLabelParams(labelDTO));

            SQLQuery insertImageLabelLinkQuery = new SQLQuery(
                    getInsertImageLabelLinkSQL(),
                    getInsertImageLabelLinkParams(labelDTO));

            transaction
                    .send(insertLabelQuery)
                    .send(insertImageLabelLinkQuery);
        }

        queryExecutor.commit(transaction);
    }

    private static String getInsertLabelSQL()
    {
        return """
            INSERT INTO public.content_labels (
              id,
              name,
              content_category
            ) VALUES
              (?, ?, ?)
            ON CONFLICT DO NOTHING;
        """;
    }

    private static List<Object> getInsertLabelParams(LabelDTO labelDTO)
    {
        return List.of(
                labelDTO.id,
                labelDTO.name,
                labelDTO.content_category);
    }

    private static String getInsertImageLabelLinkSQL()
    {
        return """
            INSERT INTO public.image_content_labels (
              image_id,
              label_id,
              label_confidence_score,
              created_at,
                updated_at
            ) (
              SELECT
                ?, id, ?, ?, ?
              FROM
                public.content_labels
              WHERE
                name = ? AND
                content_category = ?
            ) ON CONFLICT DO NOTHING;
        """;
    }

    private static List<Object> getInsertImageLabelLinkParams(LabelDTO labelDTO)
    {
        return List.of(
                labelDTO.image_id,
                labelDTO.label_confidence_score,
                labelDTO.created_at,
                labelDTO.updated_at,
                labelDTO.name,
                labelDTO.content_category);
    }
}

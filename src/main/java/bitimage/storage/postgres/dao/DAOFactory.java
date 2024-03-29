package bitimage.storage.postgres.dao;

import bitimage.storage.postgres.query.QueryExecutor;

/**
 * Factory that maintains PostgreSQL connection pool
 * allocation, and custom logic for creating new tables
 * in correct order.
 */
public class DAOFactory
{
    private final QueryExecutor queryExecutor;

    private DAOFactory(QueryExecutor queryExecutor)
    {
        this.queryExecutor = queryExecutor;
    }

    /**
     * Creates a new Data Access Object factory, and
     * handles instantiation of DAO PostgreSQL tables in
     * correct order based on table dependencies.
     *
     * TODO: Move into DB migration script
     */
    public static DAOFactory CreateNew(QueryExecutor queryExecutor)
            throws Exception
    {
        DAOFactory daoFactory = new DAOFactory(queryExecutor);

        UserDAO userDAO = daoFactory.getUserDAO();
        ImageDAO imageDAO = daoFactory.getImageDAO();
        LabelDAO labelDAO = daoFactory.getLabelDAO();

        userDAO.createUsersTable();

        imageDAO.createImagesTable();
        imageDAO.createTagsTable();
        imageDAO.createImageTagLinkTable();
        imageDAO.createFunctionToDeleteOrphanedTags();
        imageDAO.createTriggerToDeleteOrphanedTags();

        labelDAO.createLabelsTable();
        labelDAO.createImageLabelLinkTable();
        labelDAO.createFunctionToDeleteOrphanedLabels();
        labelDAO.createTriggerToDeleteOrphanedLabels();

        return daoFactory;
    }

    public ImageDAO getImageDAO()
    {
        return new ImageDAO(queryExecutor);
    }

    public UserDAO getUserDAO()
    {
        return new UserDAO(queryExecutor);
    }

    public LabelDAO getLabelDAO()
    {
        return new LabelDAO(queryExecutor);
    }
}

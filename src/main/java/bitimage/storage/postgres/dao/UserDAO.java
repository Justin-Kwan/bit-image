package bitimage.storage.postgres.dao;

import bitimage.storage.dto.UserDTO;
import bitimage.storage.postgres.query.QueryExecutor;
import bitimage.storage.postgres.query.SQLQuery;
import bitimage.storage.postgres.resultset.ExistsResultSetMapper;

import java.util.List;
import java.util.UUID;

public class UserDAO
{
    private final QueryExecutor queryExecutor;

    public UserDAO(QueryExecutor queryExecutor)
    {
        this.queryExecutor = queryExecutor;
    }

    public void createUsersTable()
            throws Exception
    {
        String sql = """
            CREATE TABLE IF NOT EXISTS public.users (
              id UUID NOT NULL,
              created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
                DEFAULT (current_timestamp AT TIME ZONE 'UTC'),
              updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
                DEFAULT (current_timestamp AT TIME ZONE 'UTC'),
              image_upload_limit INTEGER,
              image_upload_count INTEGER,
              CONSTRAINT pk_users PRIMARY KEY(id)
            );
        """;

        queryExecutor.write(new SQLQuery(sql));
    }

    public void insertUser(UserDTO userDTO)
            throws Exception
    {
        String sql = """
            INSERT INTO public.users (
              id,
              created_at,
              updated_at,
              image_upload_limit,
              image_upload_count
            ) VALUES
              (?, ?, ?, ?, ?);
        """;

        queryExecutor.write(new SQLQuery(
                sql,
                getInsertUserParams(userDTO)));
    }

    private static List<Object> getInsertUserParams(UserDTO userDTO)
    {
        return List.of(
                userDTO.id,
                userDTO.created_at,
                userDTO.updated_at,
                userDTO.image_upload_limit,
                userDTO.image_upload_count);
    }

    public void deleteUserByID(UUID userID)
            throws Exception
    {
        String sql = """
            DELETE FROM
              public.users
            WHERE
              id = ?;
        """;

        queryExecutor.write(new SQLQuery(sql, List.of(userID)));
    }

    public boolean doesUserExist(UUID userID)
            throws Exception
    {
        String sql = """
            SELECT EXISTS(
              SELECT
                id
              FROM
                users
              WHERE
                id = ?
            );
        """;

        return queryExecutor.read(
                new SQLQuery(sql, List.of(userID)),
                new ExistsResultSetMapper())
                .get(0);
    }
}

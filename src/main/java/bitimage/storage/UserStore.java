package bitimage.storage;

import bitimage.domain.common.entities.EntityID;
import bitimage.domain.uploading.entities.User;
import bitimage.domain.uploading.ports.IUserStore;
import bitimage.storage.dto.UserDTO;
import bitimage.storage.mappers.UserStoreMapper;
import bitimage.storage.postgres.dao.DAOFactory;
import bitimage.storage.postgres.dao.UserDAO;

public class UserStore
        implements IUserStore
{
    private final DAOFactory daoFactory;
    private final UserStoreMapper mapper;

    public UserStore(DAOFactory daoFactory, UserStoreMapper mapper)
    {
        this.daoFactory = daoFactory;
        this.mapper = mapper;
    }

    public void addUser(User user)
            throws Exception
    {
        UserDTO userDTO = mapper.mapToUserDTO(user);
        UserDAO userDAO = daoFactory.getUserDAO();
        userDAO.insertUser(userDTO);
    }

    public void deleteUserByID(EntityID userID)
            throws Exception
    {
        UserDAO userDAO = daoFactory.getUserDAO();
        userDAO.deleteUserByID(userID.toUUID());
    }

    public boolean doesUserExist(EntityID userID)
            throws Exception
    {
        UserDAO userDAO = daoFactory.getUserDAO();
        return userDAO.doesUserExist(userID.toUUID());
    }
}

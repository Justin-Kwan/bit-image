package bitimage.transport.controllers;

import bitimage.uploading.entities.User;
import bitimage.uploading.services.UserService;
import bitimage.transport.dto.UserDTO;
import bitimage.transport.mappers.UserControllerMapper;
import bitimage.transport.middleware.TokenChecker;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Post;

import javax.inject.Inject;

@Controller(BaseEndpoints.USERS)
public class UserController
        extends BaseController
{
    private final UserService userService;
    private final UserControllerMapper mapper;

    @Inject
    public UserController(
            UserService userService,
            UserControllerMapper mapper,
            TokenChecker tokenChecker)
    {
        super(tokenChecker);

        this.userService = userService;
        this.mapper = mapper;
    }

    @Post()
    public HttpResponse<Object> createUser(HttpHeaders headers)
    {
        return super.handleRequest(() -> {
            String user_id = tokenChecker.doAuthCheck(headers);

            User user = userService.createUser(user_id);
            UserDTO userDTO = mapper.mapToUserDTO(user);

            return HttpResponse.created(userDTO);
        });
    }

    @Delete()
    public HttpResponse<Object> deleteUser(HttpHeaders headers)
    {
        return super.handleRequest(() -> {
            String user_id = tokenChecker.doAuthCheck(headers);

            userService.deleteUser(user_id);

            return HttpResponse.noContent();
        });
    }
}

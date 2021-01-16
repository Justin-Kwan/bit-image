package bitimage.transport.controllers;

import bitimage.domain.sharedkernel.entities.Image;
import bitimage.domain.uploading.commands.CreateImageCmd;
import bitimage.domain.uploading.entities.FileUrl;
import bitimage.domain.uploading.services.ImageService;
import bitimage.regex.RegexPatterns;
import bitimage.transport.adapters.dto.ImageDTO;
import bitimage.transport.adapters.dto.ImageUploadUrlsDTO;
import bitimage.transport.adapters.dto.ImagesDTO;
import bitimage.transport.adapters.dto.SearchImageDTO;
import bitimage.transport.adapters.mappers.ImageControllerMapper;
import bitimage.transport.middleware.ITokenChecker;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.validation.Validated;
import java.lang.reflect.Field;
import java.util.List;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Validated
@Controller(BaseEndpoints.IMAGES)
public class ImageController extends BaseController {

  private final ImageService imageService;
  private final ImageControllerMapper mapper;

  @Inject
  public ImageController(
      ImageService imageService, ImageControllerMapper mapper, ITokenChecker tokenChecker) {
    super(tokenChecker);

    this.imageService = imageService;
    this.mapper = mapper;
  }

  // all good
  @Get("/upload_urls")
  public HttpResponse<Object> getImageUploadUrls(
      HttpHeaders headers, @QueryValue("count") @Min(1) @Max(1000) int count) {

    return super.handleRequest(
        () -> {
          final String userID = super.tokenChecker.doAuthCheck(headers);

          final List<FileUrl> uploadUrls = this.imageService.generateImageUploadUrls(count, userID);
          final ImageUploadUrlsDTO uploadUrlsDTO = this.mapper.mapToImageUploadUrlsDTO(uploadUrls);

          return HttpResponse.ok(uploadUrlsDTO);
        });
  }

  // all good
  @Post()
  public HttpResponse<Object> confirmImagesUploaded(
      HttpHeaders headers, @Body @Valid ImagesDTO request) {

    return super.handleRequest(
        () -> {
          final String user_id = super.tokenChecker.doAuthCheck(headers);

          final List<CreateImageCmd> cmds = this.mapper.mapToCreateImageCmds(user_id, request);
          final List<Image> uploadedImages = this.imageService.confirmImagesUploaded(cmds);
          final ImagesDTO uploadedImagesDTO = this.mapper.mapToListViewImagesDTO(uploadedImages);

          return HttpResponse.ok(uploadedImagesDTO);
        });
  }

  @Get("/{image_id}/expanded")
  public HttpResponse<Object> getImage(
      HttpHeaders headers,
      @Pattern(regexp = RegexPatterns.UUID, message = "Image id must be a valid UUID") @NotBlank
          String image_id) {

    return super.handleRequest(
        () -> {
          final String user_id = super.tokenChecker.doAuthCheck(headers);

          final Image image = this.imageService.getImage(user_id, image_id);
          final ImageDTO imageDTO = this.mapper.mapToExpandedViewImageDTO(image);

          return HttpResponse.ok(imageDTO);
        });
  }

  // all good
  @Get("/summary{?request*}")
  public HttpResponse<Object> searchImages(HttpHeaders headers, @Valid SearchImageDTO request) {

    return super.handleRequest(
        () -> {
          final String user_id = super.tokenChecker.doAuthCheck(headers);

          List<Image> images = this.searchImagesByProvidedParam(user_id, request);
          final ImagesDTO imagesDTO = this.mapper.mapToListViewImagesDTO(images);

          return HttpResponse.ok(imagesDTO);
        });
  }

  /**
   * Searches for images given search field provided (image name, tag or content label.) Defaults to
   * searching for all user's images if no search field is provided
   */
  private List<Image> searchImagesByProvidedParam(String user_id, SearchImageDTO request)
      throws Exception {
    if (request.name.isPresent()) {
      return this.imageService.searchImagesByName(user_id, request.name.get());
    } else if (request.tag.isPresent()) {
      return this.imageService.searchImagesByTag(user_id, request.tag.get());
    } else if (request.content_label.isPresent()) {
      return this.imageService.searchImagesByContentLabel(user_id, request.content_label.get());
    } else {
      return this.imageService.getAllUserImages(user_id);
    }
  }

  public static <T> String printObject(T t) {
    StringBuilder sb = new StringBuilder();

    for (Field field : t.getClass().getDeclaredFields()) {
      field.setAccessible(true);

      try {
        sb.append(field.getName()).append(": ").append(field.get(t)).append('\n');
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return sb.toString();
  }

  @Delete()
  public HttpResponse<Object> deleteImages(
      HttpHeaders headers,
      @QueryValue("id")
          List<
                  @Valid @NotNull
                  @Size(min = 1, message = "List of image ids to delete must not be empty")
                  @Pattern(regexp = RegexPatterns.UUID, message = "Image id must be a valid UUID")
                  String>
              image_ids) {

    return super.handleRequest(
        () -> {
          final String user_id = super.tokenChecker.doAuthCheck(headers);
          this.imageService.deleteImagesByID(user_id, image_ids);

          return HttpResponse.noContent();
        });
  }
}

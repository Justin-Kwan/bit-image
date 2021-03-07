package bitimage.transport.controllers;

import bitimage.domain.common.entities.Image;
import bitimage.domain.uploading.commands.CreateImageCmd;
import bitimage.domain.uploading.entities.FileUrl;
import bitimage.domain.uploading.services.ImageUploadService;
import bitimage.regexp.RegexPatterns;
import bitimage.transport.dto.ImageDTO;
import bitimage.transport.dto.ImageUploadUrlsDTO;
import bitimage.transport.dto.ImagesDTO;
import bitimage.transport.dto.SearchImageDTO;
import bitimage.transport.mappers.ImageControllerMapper;
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

  private final ImageUploadService imageUploadService;
  private final ImageControllerMapper mapper;

  @Inject
  public ImageController(
      ImageUploadService imageUploadService,
      ImageControllerMapper mapper,
      ITokenChecker tokenChecker) {
    super(tokenChecker);

    this.imageUploadService = imageUploadService;
    this.mapper = mapper;
  }

  @Get("/upload_urls")
  public HttpResponse<Object> getImageUploadUrls(
      HttpHeaders headers, @QueryValue("image_count") @Min(1) @Max(1000) int image_count) {

    return super.handleRequest(() -> {
      final String user_id = super.tokenChecker.doAuthCheck(headers);

      final List<FileUrl> uploadUrls =
          this.imageUploadService.generateImageUploadUrls(image_count, user_id);
      final ImageUploadUrlsDTO uploadUrlsDTO = this.mapper.mapToImageUploadUrlsDTO(uploadUrls);

      return HttpResponse.ok(uploadUrlsDTO);
    });
  }

  @Post()
  public HttpResponse<Object> confirmImagesUploaded(
      HttpHeaders headers, @Body @Valid ImagesDTO request) {

    return super.handleRequest(() -> {
      final String user_id = super.tokenChecker.doAuthCheck(headers);

      final List<CreateImageCmd> cmds = this.mapper.mapToCreateImageCmds(user_id, request);
      final List<Image> uploadedImages = this.imageUploadService.confirmImagesUploaded(cmds);
      final ImagesDTO uploadedImagesDTO = this.mapper.mapToListViewImagesDTO(uploadedImages);

      return HttpResponse.ok(uploadedImagesDTO);
    });
  }

  @Get("/{image_id}/expanded")
  public HttpResponse<Object> getImage(
      HttpHeaders headers,
      @Pattern(regexp = RegexPatterns.UUID, message = "Image id must be a valid UUID")
      @NotBlank
      String image_id) {

    return super.handleRequest(() -> {
      final String user_id = super.tokenChecker.doAuthCheck(headers);

      final Image image = this.imageUploadService.getImage(user_id, image_id);
      final ImageDTO imageDTO = this.mapper.mapToExpandedViewImageDTO(image);

      return HttpResponse.ok(imageDTO);
    });
  }

  @Get("/public/summary")
  public HttpResponse<Object> getAllPublicImages() {

    return super.handleRequest(() -> {
      List<Image> images = this.imageUploadService.getAllPublicImages();
      final ImagesDTO imagesDTO = this.mapper.mapToListViewImagesDTO(images);

      return HttpResponse.ok(imagesDTO);
    });
  }

  @Get("/private/summary{?request*}")
  public HttpResponse<Object> searchImages(HttpHeaders headers, @Valid SearchImageDTO request) {

    return super.handleRequest(() -> {
      final String user_id = super.tokenChecker.doAuthCheck(headers);

      List<Image> images = this.searchImagesByProvidedParam(user_id, request);
      final ImagesDTO imagesDTO = this.mapper.mapToListViewImagesDTO(images);
  
      return HttpResponse.ok(imagesDTO);
    });
  }

  /**
   * Searches for images (owned by a user) given search field provided (image name, tag or content
   * label). Searches for all user's images if no search field is provided
   */
  private List<Image> searchImagesByProvidedParam(String user_id, SearchImageDTO request)
      throws Exception {
    if (request.name.isPresent()) {

      final String imageName = request.name.get();
      return this.imageUploadService.searchImagesByName(user_id, imageName);

    } else if (request.tag.isPresent()) {

      final String tagName = request.tag.get();
      return this.imageUploadService.searchImagesByTag(user_id, tagName);

    } else if (request.content_label.isPresent()) {

      final String contentLabel = request.content_label.get();
      return this.imageUploadService.searchImagesByContentLabel(user_id, contentLabel);

    } else {
      return this.imageUploadService.getAllUserImages(user_id);
    }
  }

  @Delete()
  public HttpResponse<Object> deleteImages(HttpHeaders headers, @QueryValue("id") List<
        @Valid
        @NotNull
        @Size(min = 1, message = "List of image ids to delete must not be empty")
        @Pattern(regexp = RegexPatterns.UUID, message = "Image id must be a valid UUID")
  String> image_ids) {

    return super.handleRequest(() -> {
      final String user_id = super.tokenChecker.doAuthCheck(headers);

      this.imageUploadService.deleteImagesByID(user_id, image_ids);

      return HttpResponse.noContent();
    });
  }
}

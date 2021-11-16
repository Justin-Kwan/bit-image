package bitimage.transport.controllers;

import bitimage.shared.entities.Image;
import bitimage.uploading.commands.CreateImageCmd;
import bitimage.uploading.entities.FileUrl;
import bitimage.uploading.services.ImageUploadService;
import bitimage.regexp.RegexPatterns;
import bitimage.transport.dto.ImageDTO;
import bitimage.transport.dto.ImageUploadUrlsDTO;
import bitimage.transport.dto.ImagesDTO;
import bitimage.transport.dto.SearchImageDTO;
import bitimage.transport.mappers.ImageControllerMapper;
import bitimage.transport.middleware.TokenChecker;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.validation.Validated;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import java.util.List;

@Validated
@Controller(BaseEndpoints.IMAGES)
public class ImageController
        extends BaseController
{
    private final ImageUploadService imageUploadService;
    private final ImageControllerMapper mapper;

    @Inject
    public ImageController(
            ImageUploadService imageUploadService,
            ImageControllerMapper mapper,
            TokenChecker tokenChecker)
    {
        super(tokenChecker);

        this.imageUploadService = imageUploadService;
        this.mapper = mapper;
    }

    @Get("/upload_urls")
    public HttpResponse<Object> getImageUploadUrls(
            HttpHeaders headers,
            @QueryValue("image_count")
            @Min(1)
            @Max(1000) int image_count)
    {
        return super.handleRequest(() -> {
            String user_id = tokenChecker.doAuthCheck(headers);

            List<FileUrl> uploadUrls = imageUploadService.generateImageUploadUrls(image_count, user_id);
            ImageUploadUrlsDTO uploadUrlsDTO = mapper.mapToImageUploadUrlsDTO(uploadUrls);

            return HttpResponse.ok(uploadUrlsDTO);
        });
    }

    @Post()
    public HttpResponse<Object> confirmImagesUploaded(
            HttpHeaders headers,
            @Body @Valid ImagesDTO request)
    {
        return super.handleRequest(() -> {
            String user_id = tokenChecker.doAuthCheck(headers);

            List<CreateImageCmd> cmds = mapper.mapToCreateImageCmds(user_id, request);
            List<Image> uploadedImages = imageUploadService.confirmImagesUploaded(cmds);
            ImagesDTO uploadedImagesDTO = mapper.mapToListViewImagesDTO(uploadedImages);

            return HttpResponse.ok(uploadedImagesDTO);
        });
    }

    @Get("/{image_id}/expanded")
    public HttpResponse<Object> getImage(
            HttpHeaders headers,
            @Pattern(regexp = RegexPatterns.UUID, message = "Image id must be a valid UUID")
            @NotBlank String image_id)
    {
        return super.handleRequest(() -> {
            String user_id = tokenChecker.doAuthCheck(headers);

            Image image = imageUploadService.getImage(user_id, image_id);
            ImageDTO imageDTO = mapper.mapToExpandedViewImageDTO(image);

            return HttpResponse.ok(imageDTO);
        });
    }

    @Get("/public/summary")
    public HttpResponse<Object> getAllPublicImages()
    {
        return super.handleRequest(() -> {
            List<Image> images = imageUploadService.getAllPublicImages();
            ImagesDTO imagesDTO = mapper.mapToListViewImagesDTO(images);

            return HttpResponse.ok(imagesDTO);
        });
    }

    @Get("/private/summary{?request*}")
    public HttpResponse<Object> searchImages(HttpHeaders headers, @Valid SearchImageDTO request)
    {
        return super.handleRequest(() -> {
            String user_id = tokenChecker.doAuthCheck(headers);

            List<Image> images = searchImagesByProvidedParam(user_id, request);
            ImagesDTO imagesDTO = mapper.mapToListViewImagesDTO(images);

            return HttpResponse.ok(imagesDTO);
        });
    }

    /**
     * Searches for images (owned by a user) given search
     * field provided (image name, tag or content label).
     * Searches for all user's images if no search field
     * is provided.
     */
    private List<Image> searchImagesByProvidedParam(String user_id, SearchImageDTO request)
            throws Exception
    {
        if (request.name.isPresent()) {
            String imageName = request.name.get();
            return imageUploadService.searchImagesByName(user_id, imageName);
        }
        else if (request.tag.isPresent()) {
            String tagName = request.tag.get();
            return imageUploadService.searchImagesByTag(user_id, tagName);
        }
        else if (request.content_label.isPresent()) {
            String contentLabel = request.content_label.get();
            return imageUploadService.searchImagesByContentLabel(user_id, contentLabel);
        }
        else {
            return imageUploadService.getAllUserImages(user_id);
        }
    }

    @Delete()
    public HttpResponse<Object> deleteImages(
            HttpHeaders headers,
            @QueryValue("id") List<
                    @Valid
                    @NotNull
                    @Size(min = 1, message = "List of image ids to delete must not be empty")
                    @Pattern(regexp = RegexPatterns.UUID, message = "Image id must be a valid UUID") String> image_ids)
    {
        return super.handleRequest(() -> {
            String user_id = tokenChecker.doAuthCheck(headers);
            imageUploadService.deleteImagesByID(user_id, image_ids);

            return HttpResponse.noContent();
        });
    }
}

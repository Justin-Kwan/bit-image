package bitimage.transport.dto;

import bitimage.regexp.RegexPatterns;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import java.util.List;

public class ImageDTO
{
    @NotBlank(message = "Image id cannot be blank")
    @NotNull(message = "Image id cannot be null")
    @Pattern(regexp = RegexPatterns.UUID, message = "Image id must be a valid UUID")
    public String id;

    @NotBlank(message = "Image name cannot be blank")
    @NotNull(message = "Image name cannot be null")
    @Size(min = 1, max = 200, message = "Image name must be between 1 and 200 characters")
    @Pattern(
            regexp = RegexPatterns.ALPHA_NUMERIC,
            message = "Image name can only contain letters, numbers, periods and underscores")
    public String name;

    @NotBlank(message = "Image hash cannot be blank")
    @NotNull(message = "Image hash cannot be null")
    @Pattern(
            regexp = RegexPatterns.HASH_MD5,
            message = "Image hash must be a valid MD5 hash")
    public String hash;

    @NotNull(message = "Image privacy status cannot be null")
    @Pattern(
            regexp = RegexPatterns.BOOLEAN,
            message = "Image privacy status must be true or false")
    public String is_private;

    public Double size_mb;

    public String file_format;

    public String view_url;

    @NotNull(message = "Image tag list cannot be null")
    @Size(min = 0)
    @Valid
    public List<@Valid @NotNull TagDTO> tags;

    public List<LabelDTO> labels;
}

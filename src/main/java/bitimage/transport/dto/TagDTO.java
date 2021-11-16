package bitimage.transport.dto;

import bitimage.regexp.RegexPatterns;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class TagDTO
{
    @NotBlank(message = "Tag name cannot be blank")
    @NotNull(message = "Tag name cannot be null")
    @Size(min = 1, max = 200, message = "Tag name must be between 1 and 200 characters")
    @Pattern(
            regexp = RegexPatterns.ALPHA_NUMERIC,
            message = "Tag name can only contain letters and numbers")
    public String name;

    public String id;
}

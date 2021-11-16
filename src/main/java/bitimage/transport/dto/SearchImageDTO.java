package bitimage.transport.dto;

import bitimage.regexp.RegexPatterns;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import java.util.Optional;

public class SearchImageDTO
{
    public Optional<
            @Size(min = 1, max = 200, message = "Image name must be between 1 and 200 characters")
            @Pattern(
                    regexp = RegexPatterns.ALPHA_NUMERIC,
                    message = "Image name can only contain letters and numbers")
                    String>
            name;

    public Optional<
            @Size(min = 1, max = 200, message = "Tag name must be between 1 and 200 characters")
            @Pattern(
                    regexp = RegexPatterns.ALPHA_NUMERIC,
                    message = "Tag name can only contain letters and numbers")
                    String>
            tag;

    public Optional<
            @Size(min = 1, max = 200, message = "Content label must be between 1 and 200 characters")
            @Pattern(
                    regexp = RegexPatterns.ALPHA_NUMERIC,
                    message = "Content label can only contain letters and numbers")
                    String>
            content_label;

    public SearchImageDTO()
    {
        this.name = Optional.empty();
        this.tag = Optional.empty();
        this.content_label = Optional.empty();
    }
}

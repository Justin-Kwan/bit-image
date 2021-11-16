package bitimage.domain.uploading.commands;

import java.util.List;

/**
 * Command class that encapsulates arguments which are
 * passed into the domain service layer, when creating new image(s).
 */
final public class CreateImageCmd
{
    public String userID;
    public String imageID;
    public String imageName;
    public String providedImageHash;
    public boolean isImagePrivate;
    public List<String> imageTagNames;
}

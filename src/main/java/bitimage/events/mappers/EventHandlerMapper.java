package bitimage.events.mappers;

import bitimage.domain.analysis.commands.ExtractImageContentsCmd;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

public class EventHandlerMapper {

  private final String ROOT_IMAGES_JSON_KEY = "/images";
  private final String IMAGE_NAME_JSON_KEY = "name";
  private final String USER_ID_JSON_KEY = "userID";
  private final String ID_JSON_KEY = "id";

  public String mapToDeletedUserID(String message) throws Exception {
    final var jsonMapper = new ObjectMapper();

    JsonNode deletedUserJson = jsonMapper.readTree(message);

    return deletedUserJson.get(this.USER_ID_JSON_KEY).get(this.ID_JSON_KEY).textValue();
  }

  public List<ExtractImageContentsCmd> mapToExtractImageContentsCmd(String message)
      throws Exception {
    final var jsonMapper = new ObjectMapper();

    JsonNode imagesJson = jsonMapper.readTree(message);
    final var cmds = new ArrayList<ExtractImageContentsCmd>();

    imagesJson
        .at(this.ROOT_IMAGES_JSON_KEY)
        .forEach(
            imageJson -> {
              final var cmd = this.mapToExtractImageContentCmd(imageJson);
              cmds.add(cmd);
            });

    return cmds;
  }

  public ExtractImageContentsCmd mapToExtractImageContentCmd(JsonNode imageJson) {
    final var extractImageContentsCmd = new ExtractImageContentsCmd();

    extractImageContentsCmd.imageID =
        imageJson.get(this.ID_JSON_KEY).get(this.ID_JSON_KEY).textValue();

    extractImageContentsCmd.imageName = imageJson.get(this.IMAGE_NAME_JSON_KEY).textValue();

    extractImageContentsCmd.userID =
        imageJson.get(this.USER_ID_JSON_KEY).get(this.ID_JSON_KEY).textValue();

    return extractImageContentsCmd;
  }
}

package bitimage.eventhandling.mappers;

import bitimage.analysis.commands.ExtractImageContentsCmd;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class EventHandlerMapper
{
    private final static String ROOT_IMAGES_JSON_KEY = "/images";
    private final static String IMAGE_NAME_JSON_KEY = "name";
    private final static String USER_ID_JSON_KEY = "userID";
    private final static String ID_JSON_KEY = "id";

    private final static ObjectMapper JSON_MAPPER = new ObjectMapper();

    public String mapToDeletedUserID(String message)
            throws Exception
    {
        JsonNode deletedUserJson = JSON_MAPPER.readTree(message);

        return deletedUserJson
                .get(USER_ID_JSON_KEY)
                .get(ID_JSON_KEY)
                .textValue();
    }

    public List<ExtractImageContentsCmd> mapToExtractImageContentsCmd(String message)
            throws Exception
    {
        JsonNode imagesJson = JSON_MAPPER.readTree(message);
        List<ExtractImageContentsCmd> cmds = new ArrayList<>();

        imagesJson
                .at(ROOT_IMAGES_JSON_KEY)
                .forEach(imageJson -> {
                    ExtractImageContentsCmd cmd = mapToExtractImageContentCmd(imageJson);
                    cmds.add(cmd);
                });

        return cmds;
    }

    public ExtractImageContentsCmd mapToExtractImageContentCmd(JsonNode imageJson)
    {
        ExtractImageContentsCmd extractImageContentsCmd = new ExtractImageContentsCmd();

        extractImageContentsCmd.imageID = imageJson
                .get(ID_JSON_KEY)
                .get(ID_JSON_KEY)
                .textValue();
        extractImageContentsCmd.imageName = imageJson
                .get(IMAGE_NAME_JSON_KEY)
                .textValue();
        extractImageContentsCmd.userID = imageJson
                .get(USER_ID_JSON_KEY)
                .get(ID_JSON_KEY)
                .textValue();

        return extractImageContentsCmd;
    }
}

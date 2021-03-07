package bitimage.eventhandling;

import bitimage.domain.analysis.commands.ExtractImageContentsCmd;
import bitimage.domain.analysis.services.ImageAnalysisService;
import bitimage.eventhandling.mappers.EventHandlerMapper;

import java.util.List;

public class ImagesUploadedEventHandler implements IEventHandler {

  private final EventHandlerMapper mapper;
  private final ImageAnalysisService service;

  public ImagesUploadedEventHandler(EventHandlerMapper mapper, ImageAnalysisService service) {
    this.mapper = mapper;
    this.service = service;
  }

  public void handle(String message) throws Exception {
    final List<ExtractImageContentsCmd> cmds = this.mapper.mapToExtractImageContentsCmd(message);

    this.service.extractImageContents(cmds);
  }
}

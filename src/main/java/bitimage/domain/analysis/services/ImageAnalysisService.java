package bitimage.domain.analysis.services;

import bitimage.domain.analysis.commands.ExtractImageContentsCmd;
import bitimage.domain.analysis.entities.CleanImageLabels;
import bitimage.domain.analysis.entities.ExtractImageLabels;
import bitimage.domain.analysis.entities.StoreImageLabels;
import bitimage.domain.analysis.ports.IImageClassifier;
import bitimage.domain.analysis.ports.ILabelStore;
import bitimage.domain.common.entities.EntityID;
import bitimage.domain.sharedkernel.entities.Image;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImageAnalysisService {

  private final ILabelStore labelStore;
  private final IImageClassifier imageClassifier;

  public ImageAnalysisService(ILabelStore labelStore, IImageClassifier imageClassifier) {
    this.labelStore = labelStore;
    this.imageClassifier = imageClassifier;
  }

  public void extractImageContents(List<ExtractImageContentsCmd> cmds) throws Exception {
    final List<Image> images = cmds.stream().map(this::createImage).collect(Collectors.toList());

    images.parallelStream()
        .forEach(
            image ->
                Stream.of(image)
                    .map(new ExtractImageLabels(this.imageClassifier)::process)
                    .map(new CleanImageLabels()::process)
                    .forEach(new StoreImageLabels(this.labelStore)::process));
  }

  public Image createImage(ExtractImageContentsCmd cmd) {
    final var imageID = EntityID.CreateNew(cmd.imageID);
    final var userID = EntityID.CreateNew(cmd.userID);

    return new Image.Builder(imageID, cmd.imageName, userID).build();
  }
}

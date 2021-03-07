package bitimage.domain.analysis.entities;

import bitimage.domain.analysis.ports.IImageClassifier;
import bitimage.domain.common.entities.Image;
import bitimage.domain.common.entities.Label;
import java.util.ArrayList;
import java.util.List;

public class ExtractImageLabels implements IFilter<Image, List<Label>> {

  private final IImageClassifier imageClassifier;

  public ExtractImageLabels(IImageClassifier imageClassifier) {
    this.imageClassifier = imageClassifier;
  }

  public List<Label> process(Image image) {
    final var labels = new ArrayList<Label>();

    labels.addAll(this.imageClassifier.detectObjectsInImage(image));
    labels.addAll(this.imageClassifier.detectFacesInImage(image));
    labels.addAll(this.imageClassifier.detectCelebritiesInImage(image));
    labels.addAll(this.imageClassifier.detectUnsafeContentInImage(image));

    return labels;
  }
}

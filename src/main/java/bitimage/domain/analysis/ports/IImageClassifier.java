package bitimage.domain.analysis.ports;

import bitimage.domain.sharedkernel.entities.Image;
import bitimage.domain.sharedkernel.entities.Label;
import java.util.List;

public interface IImageClassifier {
  public List<Label> detectObjectsInImage(Image image);

  public List<Label> detectFacesInImage(Image image);

  public List<Label> detectTextInImage(Image image);

  public List<Label> detectCelebritiesInImage(Image image);

  public List<Label> detectUnsafeContentInImage(Image image);
}

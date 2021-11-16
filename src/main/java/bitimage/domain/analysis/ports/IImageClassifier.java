package bitimage.domain.analysis.ports;

import bitimage.domain.common.entities.Image;
import bitimage.domain.common.entities.Label;

import java.util.List;

public interface IImageClassifier
{
    List<Label> detectObjectsInImage(Image image);

    List<Label> detectFacesInImage(Image image);

    List<Label> detectTextInImage(Image image);

    List<Label> detectCelebritiesInImage(Image image);

    List<Label> detectUnsafeContentInImage(Image image);
}

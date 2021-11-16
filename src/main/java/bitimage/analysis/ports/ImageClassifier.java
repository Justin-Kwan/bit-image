package bitimage.analysis.ports;

import bitimage.shared.entities.Image;
import bitimage.shared.entities.Label;

import java.util.List;

public interface ImageClassifier
{
    List<Label> detectObjectsInImage(Image image);

    List<Label> detectFacesInImage(Image image);

    List<Label> detectTextInImage(Image image);

    List<Label> detectCelebritiesInImage(Image image);

    List<Label> detectUnsafeContentInImage(Image image);
}

package bitimage.analysis.entities;

import bitimage.analysis.ports.ImageClassifier;
import bitimage.shared.entities.Image;
import bitimage.shared.entities.Label;

import java.util.ArrayList;
import java.util.List;

public class ImageLabelExtractor
        implements ImageFilter<Image, List<Label>>
{
    private final ImageClassifier imageClassifier;

    public ImageLabelExtractor(ImageClassifier imageClassifier)
    {
        this.imageClassifier = imageClassifier;
    }

    public List<Label> process(Image image)
    {
        List<Label> labels = new ArrayList<Label>();

        labels.addAll(imageClassifier.detectObjectsInImage(image));
        labels.addAll(imageClassifier.detectFacesInImage(image));
        labels.addAll(imageClassifier.detectCelebritiesInImage(image));
        labels.addAll(imageClassifier.detectUnsafeContentInImage(image));

        return labels;
    }
}

package bitimage.domain.analysis.entities;

import bitimage.domain.analysis.ports.IImageClassifier;
import bitimage.domain.common.entities.Image;
import bitimage.domain.common.entities.Label;

import java.util.ArrayList;
import java.util.List;

public class ExtractImageLabels
        implements IFilter<Image, List<Label>>
{
    private final IImageClassifier imageClassifier;

    public ExtractImageLabels(IImageClassifier imageClassifier)
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

package bitimage.analysis.services;

import bitimage.analysis.commands.ExtractImageContentsCmd;
import bitimage.analysis.entities.ImageLabelCleaner;
import bitimage.analysis.entities.ImageLabelExtractor;
import bitimage.analysis.entities.ImageLabelSink;
import bitimage.analysis.ports.ImageClassifier;
import bitimage.analysis.ports.LabelStore;
import bitimage.shared.entities.EntityID;
import bitimage.shared.entities.Image;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImageAnalysisService
{
    private final LabelStore labelStore;
    private final ImageClassifier imageClassifier;

    public ImageAnalysisService(LabelStore labelStore, ImageClassifier imageClassifier)
    {
        this.labelStore = labelStore;
        this.imageClassifier = imageClassifier;
    }

    public void extractImageContents(List<ExtractImageContentsCmd> cmds)
    {
        List<Image> images = cmds.stream()
                .map(this::createImage)
                .collect(Collectors.toList());

        images.parallelStream().forEach(
                image -> Stream.of(image)
                        .map(new ImageLabelExtractor(imageClassifier)::process)
                        .map(new ImageLabelCleaner()::process)
                        .forEach(new ImageLabelSink(labelStore)::process));
    }

    public Image createImage(ExtractImageContentsCmd cmd)
    {
        return new Image.Builder(
                EntityID.CreateNew(cmd.imageID),
                cmd.imageName,
                EntityID.CreateNew(cmd.userID)).build();
    }
}

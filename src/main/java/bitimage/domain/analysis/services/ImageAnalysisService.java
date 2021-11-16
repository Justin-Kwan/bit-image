package bitimage.domain.analysis.services;

import bitimage.domain.analysis.commands.ExtractImageContentsCmd;
import bitimage.domain.analysis.entities.CleanImageLabels;
import bitimage.domain.analysis.entities.ExtractImageLabels;
import bitimage.domain.analysis.entities.StoreImageLabels;
import bitimage.domain.analysis.ports.IImageClassifier;
import bitimage.domain.analysis.ports.ILabelStore;
import bitimage.domain.common.entities.EntityID;
import bitimage.domain.common.entities.Image;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImageAnalysisService
{
    private final ILabelStore labelStore;
    private final IImageClassifier imageClassifier;

    public ImageAnalysisService(ILabelStore labelStore, IImageClassifier imageClassifier)
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
                        .map(new ExtractImageLabels(imageClassifier)::process)
                        .map(new CleanImageLabels()::process)
                        .forEach(new StoreImageLabels(labelStore)::process));
    }

    public Image createImage(ExtractImageContentsCmd cmd)
    {
        return new Image.Builder(
                EntityID.CreateNew(cmd.imageID),
                cmd.imageName,
                EntityID.CreateNew(cmd.userID)).build();
    }
}

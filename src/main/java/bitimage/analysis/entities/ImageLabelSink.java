package bitimage.analysis.entities;

import bitimage.analysis.ports.LabelStore;
import bitimage.shared.entities.Label;

import java.util.List;

public class ImageLabelSink
        implements ImageFilter<List<Label>, Void>
{

    private final LabelStore labelStore;

    public ImageLabelSink(LabelStore labelStore)
    {
        this.labelStore = labelStore;
    }

    public Void process(List<Label> labels)
            throws RuntimeException
    {
        try {
            labelStore.addLabels(labels);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}

package bitimage.analysis.ports;

import bitimage.shared.entities.Label;

import java.util.List;

public interface LabelStore
{
    void addLabels(List<Label> labels)
            throws Exception;
}

package bitimage.domain.analysis.ports;

import bitimage.domain.common.entities.Label;
import java.util.List;

public interface ILabelStore {
  public void addLabels(List<Label> labels) throws Exception;
}

package bitimage.domain.analysis.ports;

import bitimage.domain.sharedkernel.entities.Label;
import java.util.List;

public interface ILabelStore {
  public void addLabels(List<Label> labels) throws Exception;
}

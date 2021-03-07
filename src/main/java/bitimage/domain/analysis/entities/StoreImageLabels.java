package bitimage.domain.analysis.entities;

import bitimage.domain.analysis.ports.ILabelStore;
import bitimage.domain.common.entities.Label;
import java.util.List;

public class StoreImageLabels implements IFilter<List<Label>, Void> {

  private final ILabelStore labelStore;

  public StoreImageLabels(ILabelStore labelStore) {
    this.labelStore = labelStore;
  }

  public Void process(List<Label> labels) throws RuntimeException {
    try {
      this.labelStore.addLabels(labels);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return null;
  }
}

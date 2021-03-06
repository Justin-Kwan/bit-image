package bitimage.storage;

import bitimage.domain.analysis.ports.ILabelStore;
import bitimage.domain.common.entities.Label;
import bitimage.storage.dto.LabelDTO;
import bitimage.storage.mappers.LabelStoreMapper;
import bitimage.storage.postgres.dao.DAOFactory;
import bitimage.storage.postgres.dao.LabelDAO;
import java.util.List;

public class LabelStore implements ILabelStore {

  private final DAOFactory daoFactory;
  private final LabelStoreMapper mapper;

  public LabelStore(DAOFactory daoFactory, LabelStoreMapper mapper) {
    this.daoFactory = daoFactory;
    this.mapper = mapper;
  }

  public void addLabels(List<Label> labels) throws Exception {
    final LabelDAO labelDAO = this.daoFactory.getLabelDAO();

    final List<LabelDTO> labelDTOs = this.mapper.mapToLabelDTOs(labels);
    labelDAO.insertLabels(labelDTOs);
  }
}

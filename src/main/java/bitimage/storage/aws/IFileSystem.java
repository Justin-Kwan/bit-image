package bitimage.storage.aws;

import bitimage.storage.adapters.dto.FileDTO;
import bitimage.storage.adapters.dto.FileMetadataDTO;
import java.util.List;

public interface IFileSystem {
  public void createFolder(String folderName);

  public String generateFileViewUrl(String fileID, String folderName);

  public String generateFileUploadUrl(String fileID, String folderName);

  public FileMetadataDTO getFileMetadata(String fileID, String folderName);

  public void deleteFilesFromFolder(List<String> fileIDs, String folderName);

  public List<String> lookupFileIDsByPrefix(String filePrefix, String folderName);

  public void moveFileToFolder(FileDTO fileDTO, String fromFolderName, String toFolderName)
      throws Exception;

  public void moveFilesToFolder(List<FileDTO> fileDTOs, String srcFolderName, String destFolderName)
      throws Exception;
}

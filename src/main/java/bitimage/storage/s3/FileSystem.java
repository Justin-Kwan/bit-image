package bitimage.storage.s3;

import bitimage.storage.dto.FileDTO;
import bitimage.storage.dto.FileMetadataDTO;

import java.util.List;

public interface FileSystem
{
    void createFolder(String folderName);

    String generateFileViewUrl(String fileID, String folderName);

    String generateFileUploadUrl(String fileID, String folderName);

    FileMetadataDTO getFileMetadata(String fileID, String folderName);

    void deleteFilesFromFolder(List<String> fileIDs, String folderName);

    List<String> lookupFileIDsByPrefix(String filePrefix, String folderName);

    void moveFileToFolder(FileDTO fileDTO, String srcFolderName, String destFolderName)
            throws Exception;

    void moveFilesToFolder(List<FileDTO> fileDTOs, String srcFolderName, String destFolderName)
            throws Exception;
}

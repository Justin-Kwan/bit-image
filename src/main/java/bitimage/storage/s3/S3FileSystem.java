package bitimage.storage.s3;

import bitimage.storage.dto.FileDTO;
import bitimage.storage.dto.FileMetadataDTO;
import bitimage.storage.exceptions.ExceptionTranslator;
import bitimage.storage.exceptions.StorageObjectAlreadyExistsException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.Copy;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.model.CopyResult;
import com.google.common.util.concurrent.Striped;

import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

/**
 * Adapter class that interacts with S3 filesystem.
 */
public class S3FileSystem
        implements FileSystem
{
    private final AmazonS3 s3Client;
    private final TransferManager s3TransferManager;
    private final ExceptionTranslator<AmazonServiceException, RuntimeException>
            exceptionTranslator;

    Striped<Lock> resourceLocks = Striped.lazyWeakLock(RESOURCE_LOCK_STRIPE_COUNT);

    private static final Integer URL_TTL_MS = 120000;
    private static final int RESOURCE_LOCK_STRIPE_COUNT = 10;

    private S3FileSystem(
            AmazonS3 s3Client,
            TransferManager s3TransferManager,
            ExceptionTranslator<AmazonServiceException, RuntimeException> exceptionTranslator)
    {
        this.s3Client = s3Client;
        this.s3TransferManager = s3TransferManager;
        this.exceptionTranslator = exceptionTranslator;
    }

    public static S3FileSystem CreateNew(
            AwsEnv env,
            ExceptionTranslator<AmazonServiceException, RuntimeException> exceptionTranslator)
    {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                env.getAwsAccessID(),
                env.getAwsAccessKey());

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(env.getAwsRegion())
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();

        TransferManager s3TransferManager = TransferManagerBuilder.standard()
                .withS3Client(s3Client)
                .build();

        return new S3FileSystem(s3Client, s3TransferManager, exceptionTranslator);
    }

    public void createFolder(String folderName)
    {
        if (!s3Client.doesBucketExistV2(folderName)) {
            s3Client.createBucket(folderName);
        }
    }

    /**
     * @precondition All given object keys must exist
     * within bucket from FileBucketName.
     * @affects Deletes files from original folder after
     * moved to destination folder.
     */
    public void moveFilesToFolder(List<FileDTO> fileDTOs, String srcFolderName, String destFolderName)
    {
        fileDTOs.parallelStream().forEach(fileDTO -> {
            try {
                moveFileToFolder(fileDTO, srcFolderName, destFolderName);
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * If hash of transferred file does not match hash of
     * original file (provided by caller), this means that
     * a race condition has occurred where the file was
     * overwritten in the original folder before we began
     * transferring it.
     *
     * <p>A rollback is then applied, where the transferred
     * object/file is removed from the new folder.
     *
     * <p>Any further operations are then cancelled to preserve
     * data integrity between filesystem and rdbms.
     */
    public void moveFileToFolder(FileDTO fileDTO, String srcFolderName, String destFolderName)
            throws Exception
    {
        String fileID = fileDTO.id;
        String providedFileHash = fileDTO.hash;

        // hold lock on most granular resource (single file)
        Lock resourceLock = resourceLocks.get(fileID);

        try {
            resourceLock.lock();
            assertFileDoesNotExist(fileID, destFolderName);

            CopyObjectRequest request = new CopyObjectRequest(srcFolderName, fileID, destFolderName, fileID);
            Copy transferProcess = s3TransferManager.copy(request);

            CopyResult result = transferProcess.waitForCopyResult();
            String transferredFileHash = result.getETag();

            if (!transferredFileHash.equals(providedFileHash)) {
                deleteFileFromFolder(fileID, destFolderName);
                throw new Exception("Transferred file hash does not match original");
            }
        }
        catch (AmazonS3Exception e) {
            throw exceptionTranslator.translate(e);
        }
        finally {
            resourceLock.unlock();
        }
    }

    private void assertFileDoesNotExist(String fileID, String folderName)
    {
        if (s3Client.doesObjectExist(folderName, fileID)) {
            throw new StorageObjectAlreadyExistsException();
        }
    }

    public String generateFileUploadUrl(String fileID, String folderName)
    {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(folderName, fileID)
                .withMethod(HttpMethod.PUT)
                .withExpiration(getHourExpirationTime());

        return s3Client.generatePresignedUrl(request).toString();
    }

    public String generateFileViewUrl(String fileID, String folderName)
    {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(folderName, fileID)
                .withMethod(HttpMethod.GET)
                .withExpiration(getHourExpirationTime());

        return s3Client.generatePresignedUrl(request).toString();
    }

    private static Date getHourExpirationTime()
    {
        long currTimeMs = System.currentTimeMillis();
        long expTimeMs = currTimeMs + S3FileSystem.URL_TTL_MS;

        Date expirationTime = new java.util.Date();
        expirationTime.setTime(expTimeMs);

        return expirationTime;
    }

    public FileMetadataDTO getFileMetadata(String fileID, String folderName)
    {
        try {
            ObjectMetadata objectMeta = s3Client.getObjectMetadata(folderName, fileID);

            return new FileMetadataDTO(
                    objectMeta.getETag(),
                    objectMeta.getContentType(),
                    objectMeta.getContentLength());
        }
        catch (AmazonS3Exception e) {
            return new FileMetadataDTO().asNull();
        }
    }

    public List<String> lookupFileIDsByPrefix(String filePrefix, String folderName)
    {
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(folderName)
                .withPrefix(filePrefix);

        ListObjectsV2Result fileSummaries = s3Client.listObjectsV2(request);
        List<String> fileIDs = fileSummaries.getObjectSummaries()
                .parallelStream()
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());

        return fileIDs;
    }

    public void deleteFilesFromFolder(List<String> fileIDs, String folderName)
    {
        List<KeyVersion> fileIDsToDelete = fileIDs.stream()
                .map(KeyVersion::new)
                .collect(Collectors.toList());

        DeleteObjectsRequest request = new DeleteObjectsRequest(folderName)
                .withKeys(fileIDsToDelete)
                .withQuiet(false);

        s3Client.deleteObjects(request);
    }

    public void deleteFileFromFolder(String fileID, String folderName)
    {
        s3Client.deleteObject(new DeleteObjectRequest(folderName, fileID));
    }
}

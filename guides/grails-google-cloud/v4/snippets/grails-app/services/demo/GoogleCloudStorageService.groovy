package demo

import com.google.cloud.storage.Acl
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import org.springframework.web.multipart.MultipartFile
import groovy.transform.CompileStatic

@SuppressWarnings('GrailsStatelessService')
@CompileStatic
class GoogleCloudStorageService implements GrailsConfigurationAware {

    Storage storage = StorageOptions.defaultInstance.service

    // Google Cloud Platform project ID.
    String projectId

    // Cloud Storage Bucket
    String bucket

    @Override
    void setConfiguration(Config co) {
        projectId = co.getRequiredProperty('googlecloud.projectid', String)
        bucket = co.getProperty('googlecloud.cloudStorage.bucket', String, projectId)
    }

    String storeMultipartFile(String fileName, MultipartFile multipartFile) {
        storeInputStream(fileName, multipartFile.inputStream)
    }

    String storeInputStream(String fileName, InputStream inputStream) {
        BlobInfo blobInfo = storage.create(readableBlobInfo(bucket, fileName), inputStream)
        blobInfo.mediaLink
    }

    String storeBytes(String fileName, byte[] bytes) {
        BlobInfo blobInfo = storage.create(readableBlobInfo(bucket, fileName), bytes)
        blobInfo.mediaLink
    }

    private static BlobInfo readableBlobInfo(String bucket, String fileName) {
        BlobInfo.newBuilder(bucket, fileName)
                // Modify access list to allow all users with link to read file
                .setAcl([Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)])
                .build()
   }

   boolean deleteFile(String fileName) {
       BlobId blobId = BlobId.of(bucket, fileName)
       storage.delete(blobId)
   }
}

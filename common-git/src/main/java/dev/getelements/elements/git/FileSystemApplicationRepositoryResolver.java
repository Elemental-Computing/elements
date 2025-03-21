package dev.getelements.elements.git;

import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.cluster.id.ApplicationId;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ServiceMayNotContinueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.function.Consumer;

import static dev.getelements.elements.rt.git.FileSystemScriptStorageGitLoaderProvider.ELEMENT_STORAGE_DIRECTORY;
import static dev.getelements.elements.rt.git.FilesystemGitApplicationAssetLoader.getBareStorageDirectory;

/**
 * This loads an instance of {@link Repository} from a place on the filesystem.
 *
 * Created by patricktwohig on 8/2/17.
 */
public class FileSystemApplicationRepositoryResolver implements ApplicationRepositoryResolver {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemApplicationRepositoryResolver.class);

    private File gitStorageDirectory;

    @Override
    public Repository resolve(final Application application, Consumer<Repository> onCreate) throws Exception {

        final File repositoryDirectory = getStorageDirectoryForApplication(application);
        final FileRepository fileRepository = new FileRepository(repositoryDirectory);

        if (!fileRepository.getConfig().getFile().exists()) {
            fileRepository.create(true);
            onCreate.accept(fileRepository);
        }

        fileRepository.incrementOpen();
        return fileRepository;

    }

    private File getStorageDirectoryForApplication(final Application application) throws ServiceMayNotContinueException {

        if (getGitStorageDirectory() == null) {
            throw new IllegalStateException();
        }

        final var applicationId = ApplicationId.forUniqueName(application.getId());
        final var repositoryDirectory = getBareStorageDirectory(getGitStorageDirectory(), applicationId);

        if (!repositoryDirectory.exists() && !repositoryDirectory.mkdirs()) {
            throw new ServiceMayNotContinueException("cannot create " + repositoryDirectory.getAbsolutePath());
        } else if (!repositoryDirectory.isDirectory()) {
            throw new ServiceMayNotContinueException(repositoryDirectory.getAbsolutePath() + " is not a directory.");
        }

        return repositoryDirectory;

    }

    public File getGitStorageDirectory() {
        return gitStorageDirectory;
    }

    @Inject
    public void initDirectory(@Named(ELEMENT_STORAGE_DIRECTORY) final File gitStorageDirectory) {

        if (gitStorageDirectory.mkdirs()) {
            logger.info("Created git storage directory {}", gitStorageDirectory);
        } else {
            logger.debug("Directory already exists {}", gitStorageDirectory);
        }

        this.gitStorageDirectory = gitStorageDirectory;

    }

}

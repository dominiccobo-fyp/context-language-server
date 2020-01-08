package com.dominiccobo.fyp.langserver;

import com.dominiccobo.fyp.context.models.git.GitRemoteIdentifier;
import com.dominiccobo.fyp.context.models.git.GitRemoteURL;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Collection of ultities
 *
 * @author Dominic Cobo (contact@dominiccobo.com)
 */
@Component
public class GitFolderRemoteResolver {

    private GitFolderRemoteResolver() {}

    /**
     * Calls a local installation of Git to retrieve the Git project parent folder as JGIt
     * does not implement this functionality.
     * @param fileUri
     * @return the project root....
     * @throws IOException
     */
    private static String workingDirectory(URI fileUri) throws IOException {
        final ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(fileUri));
        processBuilder.command("git", "rev-parse", "--show-toplevel");
        final Process start = processBuilder.start();
        final InputStream inputStream = start.getInputStream();
        final String s = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        return s.trim();
    }

    /**
     * Obtains the git object model folder (.git) for a given project root.
     * @param projectRootDir the project root.
     * @return
     * @throws URISyntaxException
     */
    private static File getGitObjectDirForProjectRoot(String projectRootDir) throws URISyntaxException {
        projectRootDir = projectRootDir + "/.git";
        final File file = new File(projectRootDir);
        return file;
    }

    private static Map<GitRemoteIdentifier, GitRemoteURL> getRemoteURLs(File projectGitObjectDir) throws IOException {
        final FileRepository fileRepository = new FileRepository(projectGitObjectDir);
        Map<GitRemoteIdentifier, GitRemoteURL> remotes = new HashMap<>();
        for (String remoteName : fileRepository.getRemoteNames()) {
            String url = getUrlForRemoteName(fileRepository, remoteName);
            remotes.put(new GitRemoteIdentifier(remoteName), new GitRemoteURL(url));
        }
        fileRepository.close();
        return remotes;
    }

    private static String getUrlForRemoteName(FileRepository fileRepository, String remoteName) {
        return fileRepository.getConfig().getString("remote", remoteName, "url");
    }

    public Map<GitRemoteIdentifier, GitRemoteURL> getRemotesForDirectory(URI fileUri) throws IOException, URISyntaxException {
        final String workingDirectory = workingDirectory(fileUri);
        final File gitObjectDirForProjectRoot = getGitObjectDirForProjectRoot(workingDirectory);
        return getRemoteURLs(gitObjectDirForProjectRoot);
    }
}

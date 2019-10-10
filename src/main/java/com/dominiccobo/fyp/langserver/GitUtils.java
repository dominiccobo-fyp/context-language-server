package com.dominiccobo.fyp.langserver;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.internal.storage.file.FileRepository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/**
 * Collection of ultities
 *
 * @author Dominic Cobo (contact@dominiccobo.com)
 */
public class GitUtils {

    /**
     * Calls a local installation of Git to retrieve the Git project parent folder as JGIt
     * does not implement this functionality.
     * @param fileUri
     * @return the project root....
     * @throws IOException
     */
    static String workingDirectory(URI fileUri) throws IOException {
        final ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(fileUri).getParentFile());
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
    static File getGitObjectDirForProjectRoot(String projectRootDir) throws URISyntaxException {
        projectRootDir = projectRootDir + "/.git";
        final File file = new File(projectRootDir);
        return file;
    }

    static String getUpstream(File projectGitObjectDir) throws IOException {
        final FileRepository fileRepository = new FileRepository(projectGitObjectDir);
        final String originUrl = fileRepository.getConfig().getString("remote", "origin", "url");
        return originUrl;
    }

    static String getUpstreamForFile(URI fileUri) throws IOException, URISyntaxException {
        final String workingDirectory = workingDirectory(fileUri);
        final File gitObjectDirForProjectRoot = getGitObjectDirForProjectRoot(workingDirectory);
        return getUpstream(gitObjectDirForProjectRoot);
    }
}

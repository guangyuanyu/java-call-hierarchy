package org.joker.java.call.hierarchy.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;

public class GitRepoUtils {

    public static Git gitLocal(String repoUrl, String sourceDir) throws IOException {
        Git git = Git.open(new File(sourceDir));
        return git;
    }

    private static CredentialsProvider createCredential() {
        return new UsernamePasswordCredentialsProvider("yugy1", "111@csc");
    }
}

package fuud.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class ClassPathHelper {
    private static Map<String, List<String>> artifactToClassPath = null;

    public static synchronized List<String> getClasspathForArtifact(String artifactId) {
        if (artifactToClassPath == null) {
            artifactToClassPath = new HashMap<>();
            try {
                File topProjectDir = findTopProjectDir();
                checkPomChanges(topProjectDir);
                searchForClassPathFiles(topProjectDir, artifactToClassPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        List<String> result = artifactToClassPath.get(artifactId);
        if (result == null) {
            throw new IllegalStateException("Classpath for artifactId " + artifactId + " is not found, known artifacts: " + artifactToClassPath.keySet());
        }
        return result;
    }

    private static void checkPomChanges(File topProjectDir) throws IOException {
        File pomXml = new File(topProjectDir, "pom.xml");
        if (pomXml.exists()) {
            File targetPomFile = new File(new File(topProjectDir, "target"), "pom-copy.xml");
            if (!targetPomFile.exists()) {
                throw new IllegalStateException(targetPomFile.getAbsolutePath() + " is not generated, run `mvn process-classes` first");
            }
            if (!Files.readString(pomXml.toPath()).equals(Files.readString(targetPomFile.toPath()))) {
                throw new IllegalStateException(targetPomFile.getAbsolutePath() + " is not equal to " + pomXml.getAbsolutePath() + ", run `mvn process-classes` first");
            }
            File[] probablySubmodules = topProjectDir.listFiles(File::isDirectory);
            if (probablySubmodules != null) {
                for (File probablySubmodule : probablySubmodules) {
                    checkPomChanges(probablySubmodule);
                }
            }
        }
    }

    private static void searchForClassPathFiles(File topProjectDir, Map<String, List<String>> results) throws IOException {
        File pomXml = new File(topProjectDir, "pom.xml");
        if (pomXml.exists()) {
            File targetDir = new File(topProjectDir, "target");
            File[] classPathFiles = targetDir.listFiles(pathname -> pathname.getName().startsWith("classpath_") && pathname.getName().endsWith(".txt"));
            if (classPathFiles != null) {
                if (classPathFiles.length > 1) {
                    throw new IllegalStateException("Found more than one classpath file in dir " + targetDir.getAbsolutePath());
                }
                if (classPathFiles.length == 1) {
                    File classPathFile = classPathFiles[0];
                    List<String> classPath = new ArrayList<>(Arrays.asList(Files.readString(classPathFile.toPath()).split(System.getProperty("path.separator"))));
                    // maven-dependency-plugin build-classpath does not include module classes, let's include them now
                    classPath.add(0, new File(targetDir, "classes").getAbsolutePath());

                    String artifactId = classPathFile.getName().replaceAll("^classpath_", "").replaceAll(".txt$", "");
                    if (results.containsKey(artifactId)) {
                        throw new IllegalStateException("Duplicate artifact id: " + artifactId);
                    }
                    results.put(artifactId, classPath);
                }
            }
            File[] probablySubmodules = topProjectDir.listFiles(File::isDirectory);
            if (probablySubmodules != null) {
                for (File probablySubmodule : probablySubmodules) {
                    searchForClassPathFiles(probablySubmodule, results);
                }
            }
        }
    }

    private static File findTopProjectDir() throws IOException {
        File topProjectDir = new File(".").getCanonicalFile();
        do {
            if (new File(topProjectDir, ".top.project.dir").exists()) {
                return topProjectDir;
            }
            topProjectDir = topProjectDir.getParentFile();
        } while (topProjectDir != null);

        throw new IllegalStateException("Cannot find marker file .top.project.dir starting from " + new File(".").getAbsolutePath());
    }

}

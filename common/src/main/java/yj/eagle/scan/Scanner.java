package yj.eagle.scan;

import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/15 13:10
 */

public class Scanner {
    private final String scanPackages;

    public Scanner(String scanPackages) {
        this.scanPackages = scanPackages;
    }

    public List<Class<?>> scan(List<Class<?>> scanClass) throws Exception {
        Set<String> set = new HashSet<>();
        for (String scanPackage : scanPackages.split(",")) {
            if ((scanPackage = scanPackage.trim()).length() > 0) {
                Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(scanPackage);
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    String protocol = url.getProtocol();
                    if ("file".equals(protocol)) {
                        set.addAll(scanFile(url));
                    } else if ("jar".equals(protocol)) {
                        set.addAll(scanJar(url));
                    }
                }
            }
        }

        List<Class<?>> ret = new ArrayList<>();
        for (String s : set) {
            s = s.replace("/", ".");
            Class<?> clazz = Class.forName(s);
            for (Class<?> c : scanClass) {
                if (c.isAssignableFrom(clazz)) {
                    ret.add(clazz);
                }
            }
        }

        return ret;
    }

    private List<String> scanFile(URL url) throws Exception {

        List<String> classNames = new ArrayList<>();
        Path path = Paths.get(url.toURI());
        Files.walkFileTree(path, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toFile().getName().endsWith(".class")) {
                    InputStream inputStream = Files.newInputStream(file, StandardOpenOption.READ);
                    String className = new ClassReader(inputStream).getClassName();
                    classNames.add(className);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });

        return classNames;
    }

    private List<String> scanJar(URL url) throws Exception {
        List<String> classNames = new ArrayList<>();
        JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
        try (JarFile jarFile = jarURLConnection.getJarFile()) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.getName().endsWith(".class")) {
                    InputStream inputStream = jarFile.getInputStream(jarEntry);
                    String className = new ClassReader(inputStream).getClassName();
                    classNames.add(className);
                }
            }
        }
        return classNames;
    }
}

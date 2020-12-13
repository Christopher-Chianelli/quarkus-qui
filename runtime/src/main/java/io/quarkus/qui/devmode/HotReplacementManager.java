package io.quarkus.qui.devmode;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.quarkus.qui.WindowManager;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.configuration.ProfileManager;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class HotReplacementManager {
    WindowManager windowManager;

    WatchService watchService;

    ExecutorService managedExecutor = Executors.newCachedThreadPool();

    Map<WatchKey, Path> watchKeyPathMap = new HashMap<>();

    public HotReplacementManager(WindowManager windowManager) {
        if (ProfileManager.getLaunchMode() != LaunchMode.DEVELOPMENT) {
            return;
        }
        this.windowManager = windowManager;
        System.out.println("Setting up listeners...");
        try {
            watchService = FileSystems.getDefault().newWatchService();
            for (Path srcDir : QuiHotReplacementService.getSourceDirectories()) {
                registerPath(srcDir);
            }
            managedExecutor.submit(this::listenForChanges);
        } catch (IOException e) {
            System.out.println("Unable to watch files: " + e.getMessage());
            System.out.println("Automatic hot reload will be disabled");
        }
    }

    private void notifyFileChange(Path file) {
        if (file.getFileName().toString().trim().endsWith(".java")) {
            try {
                watchService.close();
            } catch (IOException e) {
                // Do nothing
            } finally {
                windowManager._onFileChange();
            }
        }
    }

    private void registerPath(Path p) {
        if (!p.toFile().exists() || !p.toFile().isDirectory()) {
            throw new RuntimeException("folder " + p + " does not exist or is not a directory");
        }
        try {
            Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    WatchKey watchKey = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                    watchKeyPathMap.put(watchKey, dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Error registering path " + p);
        }
    };

    private void listenForChanges() {
        while (true) {
            final WatchKey key;
            try {
                key = watchService.take(); // wait for a key to be available
            } catch (InterruptedException ex) {
                return;
            }

            final Path dir = watchKeyPathMap.get(key);
            if (dir == null) {
                continue;
            }

            for (WatchEvent watchEvent : key.pollEvents()) {
                if (watchEvent.kind() == ENTRY_CREATE) {
                    Path p = ((WatchEvent<Path>) watchEvent).context();
                    final Path absPath = dir.resolve(p);
                    if (absPath.toFile().isDirectory()) {
                        registerPath(absPath);
                    } else {
                        notifyFileChange(absPath);
                    }
                } else if (watchEvent.kind() == ENTRY_MODIFY) {
                    Path p = ((WatchEvent<Path>) watchEvent).context();
                    notifyFileChange(p);
                }
                else if (watchEvent.kind() == ENTRY_DELETE) {
                    Path p = ((WatchEvent<Path>) watchEvent).context();
                    final Path prefix = dir.resolve(p);
                    Set<Map.Entry<WatchKey, Path>> entrySet = new HashSet<>(watchKeyPathMap.entrySet());
                    for (Map.Entry<WatchKey, Path> watchKeyPathEntry : entrySet) {
                        if (watchKeyPathEntry.getValue().startsWith(prefix)) {
                            watchKeyPathMap.remove(watchKeyPathEntry.getKey());
                            watchKeyPathEntry.getKey().cancel();
                        }
                    }
                    notifyFileChange(p);
                }
            }

            boolean valid = key.reset(); // IMPORTANT: The key must be reset after processed
            if (!valid) {
                break;
            }
        }
    }
}

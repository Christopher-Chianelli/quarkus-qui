package io.quarkus.qui.devmode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.quarkus.dev.spi.HotReplacementContext;
import io.quarkus.dev.spi.HotReplacementSetup;

public class QuiHotReplacementService implements HotReplacementSetup {
    static List<Path> sourceDirectoryList = new ArrayList<>();

    @Override
    public void setupHotDeployment(HotReplacementContext context) {
        sourceDirectoryList.addAll(context.getSourcesDir());
        sourceDirectoryList.addAll(context.getResourcesDir());
    }

    public static List<Path> getSourceDirectories() {
        return sourceDirectoryList;
    }
}

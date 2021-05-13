package ca.ibodrov.concord.plugins.tool;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2021 Concord Authors
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import com.walmartlabs.concord.dependencymanager.DependencyManagerException;
import com.walmartlabs.concord.runtime.v2.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

@Named("tool")
public class ToolTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(ToolTask.class);

    private final Path workDir;
    private final DependencyManager dependencyManager;
    private final FileService fileService;

    @Inject
    public ToolTask(WorkingDirectory workDir,
                    DependencyManager dependencyManager,
                    FileService fileService) {

        this(workDir.getValue(), dependencyManager, fileService);
    }

    public ToolTask(Path workDir,
                    DependencyManager dependencyManager,
                    FileService fileService) {

        this.workDir = workDir;
        this.dependencyManager = dependencyManager;
        this.fileService = fileService;
    }

    @Override
    public TaskResult execute(Variables input) throws Exception {
        String toolUrl = input.assertString("url");

        toolUrl = interpolate(toolUrl);
        log.info("URL: {}", toolUrl);

        Path src;
        try {
            src = dependencyManager.resolve(URI.create(toolUrl));
        } catch (DependencyManagerException e) {
            throw new RuntimeException("File not found: " + toolUrl);
        }

        Path dst = fileService.createTempDirectory("tool")
                .resolve(src.getFileName());
        Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);

        Set<PosixFilePermission> attrs = new HashSet<>();
        attrs.add(PosixFilePermission.OWNER_READ);
        attrs.add(PosixFilePermission.OWNER_WRITE);
        attrs.add(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(dst, attrs);

        dst = workDir.relativize(dst);
        log.info("File copied into {}", dst);

        return TaskResult.success()
                .value("path", dst.toString());
    }

    private static String interpolate(String s) {
        String os = System.getProperty("os.name");
        String arch = System.getProperty("os.arch");

        return s.replace("{os}", os)
                .replace("{os:lower}", os.toLowerCase())
                .replace("{arch}", arch)
                .replace("{arch:lower}", arch.toLowerCase());
    }
}

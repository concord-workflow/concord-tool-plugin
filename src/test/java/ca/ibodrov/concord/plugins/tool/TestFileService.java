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

import com.walmartlabs.concord.runtime.v2.sdk.FileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestFileService implements FileService {

    private final Path workDir;

    public TestFileService(Path workDir) {
        this.workDir = workDir;
    }

    @Override
    public Path createTempFile(String prefix, String suffix) throws IOException {
        return Files.createTempFile(ensureTmpDir(), prefix, suffix);
    }

    @Override
    public Path createTempDirectory(String prefix) throws IOException {
        return Files.createTempDirectory(ensureTmpDir(), prefix);
    }

    private Path ensureTmpDir() throws IOException {
        Path dst = workDir.resolve(".tmp");
        Files.createDirectories(dst);
        return dst;
    }
}

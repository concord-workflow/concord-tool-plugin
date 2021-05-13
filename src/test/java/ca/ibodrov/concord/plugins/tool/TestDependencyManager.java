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

import com.walmartlabs.concord.runtime.v2.sdk.DependencyManager;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestDependencyManager implements DependencyManager {

    private final com.walmartlabs.concord.dependencymanager.DependencyManager delegate;

    public TestDependencyManager() {
        try {
            Path tmpDir = Files.createTempDirectory("deps");
            this.delegate = new com.walmartlabs.concord.dependencymanager.DependencyManager(tmpDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Path resolve(URI uri) throws IOException {
        return delegate.resolveSingle(uri).getPath();
    }
}

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
import com.walmartlabs.concord.runtime.v2.sdk.FileService;
import com.walmartlabs.concord.runtime.v2.sdk.MapBackedVariables;
import com.walmartlabs.concord.runtime.v2.sdk.TaskResult;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

@Ignore
public class TaskTest {

    @Test
    public void test() throws Exception {
        Path workDir = Files.createTempDirectory("test");
        DependencyManager dependencyManager = new TestDependencyManager();
        FileService fileService = new TestFileService(workDir);

        ToolTask toolTask = new ToolTask(workDir, dependencyManager, fileService);
        Map<String, Object> input = Collections.singletonMap("url", "https://dl.k8s.io/release/v1.20.6/bin/{os:lower}/{arch}/kubectl");
        TaskResult result = toolTask.execute(new MapBackedVariables(input));

        String path = (String) ((TaskResult.SimpleResult) result).values().get("path");
        assertNotNull(path);

        ExecTask execTask = new ExecTask(workDir, fileService);
        input = Collections.singletonMap("cmd", path + " version -o yaml");
        execTask.execute(new MapBackedVariables(input));
    }
}

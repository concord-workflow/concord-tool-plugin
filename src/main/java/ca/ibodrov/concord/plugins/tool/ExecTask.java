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

import com.walmartlabs.concord.runtime.v2.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Named("exec")
public class ExecTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(ExecTask.class);

    private final Path workDir;
    private final FileService fileService;

    @Inject
    public ExecTask(WorkingDirectory workDir,
                    FileService fileService) {

        this(workDir.getValue(), fileService);
    }

    public ExecTask(Path workDir, FileService fileService) {
        this.workDir = workDir;
        this.fileService = fileService;
    }

    @Override
    public TaskResult execute(Variables input) throws Exception {
        String cmd = input.assertString("cmd");

        Path scriptFile = fileService.createTempFile("script", ".sh");
        Files.write(scriptFile, cmd.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);

        Process proc = new ProcessBuilder()
                .command("/usr/bin/env", "bash", workDir.relativize(scriptFile).toString())
                .directory(workDir.toFile())
                .start();

        Thread stdoutReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
            } catch (IOException e) {
                log.warn("Error while reading the command's stdout: {}", e.getMessage());
            }
        });
        stdoutReader.start();

        Thread stderrReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.warn(line);
                }
            } catch (IOException e) {
                log.warn("Error while reading the command's stderr: {}", e.getMessage());
            }
        });
        stderrReader.start();

        int code = proc.waitFor();
        stdoutReader.join();
        stderrReader.join();

        if (code != 0) {
            throw new RuntimeException("Non-zero exit code: " + code);
        }

        return TaskResult.success();
    }
}

/*
 * Copyright 2013 the original author or authors.
 *
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
 */

package org.gradle.nativebinaries.toolchain.internal.gcc;

import org.gradle.api.Action;
import org.gradle.api.tasks.WorkResult;
import org.gradle.nativebinaries.language.c.internal.CCompileSpec;
import org.gradle.nativebinaries.toolchain.internal.ArgsTransformer;
import org.gradle.nativebinaries.toolchain.internal.CommandLineTool;

import java.util.List;

public class CHeaderPrecompiler extends CCompiler {
    private final CommandLineTool<CCompileSpec> commandLineTool;

    public CHeaderPrecompiler(CommandLineTool<CCompileSpec> commandLineTool, Action<List<String>> argsAction, boolean useCommandFile) {
        ArgsTransformer<CCompileSpec> argsTransformer = new CHeaderPrecompileArgsTransformer();
        argsTransformer = new UserArgsTransformer<CCompileSpec>(argsTransformer, argsAction);
        if (useCommandFile) {
            argsTransformer = new GccOptionsFileArgTransformer<CCompileSpec>(argsTransformer);
        }
        this.commandLineTool = commandLineTool.withArguments(argsTransformer);
    }

    public WorkResult execute(CCompileSpec spec) {
        return commandLineTool.inWorkDirectory(spec.getObjectFileDir()).execute(spec);
    }

    private static class CHeaderPrecompileArgsTransformer extends GccCompilerArgsTransformer<CCompileSpec> {
        protected String getLanguage() {
            return "c-header";
        }
    }
}

/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.plugins.cpp.gpp
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.internal.tasks.DefaultTaskDependency
import org.gradle.api.internal.tasks.compile.Compiler
import org.gradle.api.tasks.TaskDependency
import org.gradle.plugins.binaries.model.CompileSpec
import org.gradle.plugins.binaries.model.Library
import org.gradle.plugins.binaries.model.NativeComponent
import org.gradle.plugins.binaries.model.internal.CompileTaskAware
import org.gradle.plugins.cpp.CppCompile
import org.gradle.plugins.cpp.CppSourceSet
import org.gradle.plugins.cpp.compiler.capability.StandardCppCompiler
import org.gradle.plugins.cpp.internal.CppCompileSpec

class GppCompileSpec extends DefaultCppCompileSpec implements CompileSpec, StandardCppCompiler, CompileTaskAware, CppCompileSpec {
    NativeComponent nativeComponent

    private CppCompile task
    List<Closure> settings = []

    private final Compiler<? super GppCompileSpec> compiler
    private final ProjectInternal project
    private final ConfigurableFileCollection libs
    private final ConfigurableFileCollection includes
    private final ConfigurableFileCollection source

    GppCompileSpec(NativeComponent nativeComponent, Compiler<? super GppCompileSpec> compiler, ProjectInternal project) {
        this.nativeComponent = nativeComponent
        this.compiler = compiler
        this.project = project
        libs = project.files()
        includes = project.files()
        source = project.files()
    }

    void configure(CppCompile task) {
        this.task = task
        task.spec = this
        task.compiler = compiler

        task.onlyIf { !task.inputs.files.empty }

        // problem: will break if a source set is removed
        nativeComponent.sourceSets.withType(CppSourceSet).all { from(it) }
    }

    String getName() {
        nativeComponent.name
    }

    TaskDependency getBuildDependencies() {
        return new DefaultTaskDependency().add(task)
    }

    File getWorkDir() {
        project.file "$project.buildDir/compileWork/$name"
    }

    Iterable<File> getLibs() {
        return libs
    }

    Iterable<File> getIncludeRoots() {
        return includes
    }

    Iterable<File> getSource() {
        return source
    }

    void setting(Closure closure) {
        settings << closure
    }

    void from(CppSourceSet sourceSet) {
        includes sourceSet.exportedHeaders
        source sourceSet.source
        libs sourceSet.libs

        sourceSet.nativeDependencySets.all { deps ->
            includes deps.includeRoots
            source deps.files
        }
    }

    void includes(SourceDirectorySet dirs) {
        task.inputs.files dirs
        includes.from({dirs.srcDirs})
    }

    // special filecollection version because filecollection may be buildable
    void includes(FileCollection includeRoots) {
        task.inputs.files includeRoots
        includes.from(includeRoots)
    }

    void includes(Iterable<File> includeRoots) {
        for (File includeRoot in includeRoots) {
            task.inputs.dir(includeRoot)
        }
        includes.from(includeRoots)
    }

    void source(Iterable<File> files) {
        task.inputs.files files
        source.from files
    }

    // special filecollection version because filecollection may be buildable
    void source(FileCollection files) {
        task.inputs.source files
        source.from files
    }

    void libs(Iterable<Library> libs) {
        task.dependsOn libs
        this.libs.from({ libs*.outputFile })
        includes(project.files { libs*.headers*.srcDirs })
    }

    void args(Object... args) {
        setting {
            it.args args
        }
    }
}
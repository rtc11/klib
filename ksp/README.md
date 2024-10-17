Generate code compile time e.g. for @JsonSerialize

## using from CLI
```
java -cp \
kotlin-analysis-api-2.0.0-1.0.21.jar:common-deps-2.0.0-1.0.21.jar:symbol-processing-api-2.0.0-1.0.21.jar:kotlin-stdlib-2.0.0.jar \
com.google.devtools.ksp.cmdline.KSPJvmMain \
-jvm-target 11 \
-module-name=main \
-source-roots project_dir/src/kotlin/main \
-project-base-dir project_dir/ \
-output-base-dir=project_dir/build/ \
-caches-dir=project_dir/build/caches/ \
-class-output-dir=project_dir/build/out/main/classes \
-kotlin-output-dir=project_dir/build/out/main/kotlin/ \
-java-output-dir project_dir/build/out/main/java/ \
-resource-output-dir project_dir/build/out/main/res/ \
-language-version=2.0 \
-api-version=2.0 \
path/to/processor.jar
```

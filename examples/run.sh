#!/usr/bin/env bash

set -e

# set directory to where this script is located
cd "$(dirname "$0")"

echo "work dir: $(pwd)"

java \
-Dfile.encoding=UTF-8 \
-Dsun.stdout.encoding=UTF-8 \
-Dsun.stderr.encoding=UTF-8 \
-cp /Users/robin/code/kotlin/k/out \
:/Users/robin/code/kotlin/k/klib/build/resources/main \
:/Users/robin/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib/2.0.20/7388d355f7cceb002cd387ccb7ab3850e4e0a07f/kotlin-stdlib-2.0.20.jar \
:/Users/robin/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm/1.9.0/9beade4c1c1569e4f36cbd2c37e02e3e41502601/kotlinx-coroutines-core-jvm-1.9.0.jar \
:/Users/robin/.gradle/caches/modules-2/files-2.1/org.jetbrains/annotations/23.0.0/8cc20c07506ec18e0834947b84a864bfc094484e/annotations-23.0.0.jar \
LogExampleKt


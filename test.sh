#!/bin/bash

set -e
cd "$(dirname "$0")/.build"

java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 \
-cp \
test/:\
src/:\
../.libs/com/github/luben/zstd-jni/1.5.5-11/zstd-jni-1.5.5-11.jar:\
../.libs/commons-beanutils/commons-beanutils/1.7.0/commons-beanutils-1.7.0.jar:\
../.libs/commons-digester/commons-digester/1.8/commons-digester-1.8.jar:\
../.libs/commons-io/commons-io/2.15.1/commons-io-2.15.1.jar:\
../.libs/commons-logging/commons-logging/1.2/commons-logging-1.2.jar:\
../.libs/dom4j/dom4j/1.1/dom4j-1.1.jar:\
../.libs/oro/oro/2.0.8/oro-2.0.8.jar:\
../.libs/commons-chain/commons-chain/1.1/commons-chain-1.1.jar:\
../.libs/commons-lang/commons-lang/2.4/commons-lang-2.4.jar:\
../.libs/javax/inject/javax.inject/1/javax.inject-1.jar:\
../.libs/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar:\
../.libs/commons-codec/commons-codec/1.16.1/commons-codec-1.16.1.jar:\
../.libs/org/tukaani/xz/1.9/xz-1.9.jar:\
../.libs/org/iq80/snappy/snappy/0.4/snappy-0.4.jar:\
../.libs/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar:\
../.libs/org/jetbrains/annotations/23.0.0/annotations-23.0.0.jar:\
../.libs/org/sonatype/plexus/plexus-build-api/0.0.7/plexus-build-api-0.0.7.jar:\
../.libs/org/ow2/asm/asm/9.7/asm-9.7.jar:\
../.libs/org/codehaus/plexus/plexus-i18n/1.0-beta-10/plexus-i18n-1.0-beta-10.jar:\
../.libs/org/jetbrains/kotlin/kotlin-stdlib/2.0.0/kotlin-stdlib-2.0.0.jar:\
../.libs/org/jetbrains/kotlinx/kotlinx-coroutines-core-jvm/1.9.0/kotlinx-coroutines-core-jvm-1.9.0.jar:\
../.libs/org/codehaus/plexus/plexus-interpolation/1.26/plexus-interpolation-1.26.jar:\
../.libs/org/codehaus/plexus/plexus-archiver/4.9.2/plexus-archiver-4.9.2.jar:\
../.libs/org/codehaus/plexus/plexus-io/3.4.2/plexus-io-3.4.2.jar:\
../.libs/org/codehaus/plexus/plexus-xml/3.0.0/plexus-xml-3.0.0.jar:\
../.libs/org/apache/velocity/velocity-tools/2.0/velocity-tools-2.0.jar:\
../.libs/org/apache/httpcomponents/httpclient/4.5.13/httpclient-4.5.13.jar:\
../.libs/org/codehaus/plexus/plexus-utils/4.0.1/plexus-utils-4.0.1.jar:\
../.libs/org/codehaus/plexus/plexus-component-annotations/2.0.0/plexus-component-annotations-2.0.0.jar:\
../.libs/org/codehaus/plexus/plexus-velocity/1.2/plexus-velocity-1.2.jar:\
../.libs/org/apache/velocity/velocity/1.7/velocity-1.7.jar:\
../.libs/org/apache/commons/commons-text/1.12.0/commons-text-1.12.0.jar:\
../.libs/org/apache/commons/commons-lang3/3.8.1/commons-lang3-3.8.1.jar:\
../.libs/org/apache/httpcomponents/httpcore/4.4.14/httpcore-4.4.14.jar:\
../.libs/org/apache/commons/commons-compress/1.26.1/commons-compress-1.26.1.jar:\
../.libs/org/apache/maven/resolver/maven-resolver-util/1.4.1/maven-resolver-util-1.4.1.jar:\
../.libs/org/apache/maven/shared/maven-dependency-tree/3.3.0/maven-dependency-tree-3.3.0.jar:\
../.libs/org/apache/maven/reporting/maven-reporting-api/3.1.1/maven-reporting-api-3.1.1.jar:\
../.libs/org/apache/maven/plugins/maven-install-plugin/3.1.2/maven-install-plugin-3.1.2.jar:\
../.libs/org/apache/maven/plugins/maven-deploy-plugin/3.1.2/maven-deploy-plugin-3.1.2.jar:\
../.libs/org/apache/maven/doxia/doxia-integration-tools/1.11.1/doxia-integration-tools-1.11.1.jar:\
../.libs/org/apache/maven/resolver/maven-resolver-api/1.4.1/maven-resolver-api-1.4.1.jar:\
../.libs/org/apache/maven/reporting/maven-reporting-impl/3.2.0/maven-reporting-impl-3.2.0.jar:\
../.libs/org/apache/maven/plugins/maven-site-plugin/3.12.1/maven-site-plugin-3.12.1.jar:\
../.libs/org/apache/maven/plugins/maven-assembly-plugin/3.7.1/maven-assembly-plugin-3.7.1.jar:\
../.libs/org/apache/maven/doxia/doxia-logging-api/1.12.0/doxia-logging-api-1.12.0.jar:\
../.libs/org/apache/maven/doxia/doxia-module-xhtml5/1.11.1/doxia-module-xhtml5-1.11.1.jar:\
../.libs/org/apache/maven/doxia/doxia-core/1.11.1/doxia-core-1.11.1.jar:\
../.libs/org/apache/maven/plugins/maven-clean-plugin/3.2.0/maven-clean-plugin-3.2.0.jar:\
../.libs/org/apache/maven/doxia/doxia-decoration-model/1.11.1/doxia-decoration-model-1.11.1.jar:\
../.libs/org/apache/maven/plugins/maven-dependency-plugin/3.7.0/maven-dependency-plugin-3.7.0.jar:\
../.libs/org/apache/maven/plugins/maven-antrun-plugin/3.1.0/maven-antrun-plugin-3.1.0.jar:\
../.libs/org/apache/maven/doxia/doxia-module-xhtml/1.11.1/doxia-module-xhtml-1.11.1.jar:\
../.libs/org/apache/maven/doxia/doxia-skin-model/1.11.1/doxia-skin-model-1.11.1.jar:\
../.libs/org/apache/maven/shared/maven-common-artifact-filters/3.3.2/maven-common-artifact-filters-3.3.2.jar:\
../.libs/org/apache/maven/doxia/doxia-site-renderer/1.11.1/doxia-site-renderer-1.11.1.jar:\
../.libs/org/apache/maven/doxia/doxia-sink-api/1.12.0/doxia-sink-api-1.12.0.jar:\
../.libs/org/apache/maven/shared/maven-dependency-analyzer/1.14.1/maven-dependency-analyzer-1.14.1.jar:\
../.libs/org/apache/maven/shared/maven-artifact-transfer/0.13.1/maven-artifact-transfer-0.13.1.jar:\
../.libs/org/apache/maven/shared/maven-shared-utils/3.4.2/maven-shared-utils-3.4.2.jar:\
/usr/local/Cellar/kotlin/2.0.21/libexec/lib/kotlin-stdlib.jar:\
../.res/ \
test.TesterKt $@


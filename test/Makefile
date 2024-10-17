.PHONY: all libs clean

STDLIB := $(shell echo ${KOTLIN_HOME})/libexec/lib/kotlin-stdlib.jar
LIBS   := $(shell rg --files .build/.libs/ ../src/.build/.libs/ -g '*.jar' | tr '\n' ':')${STDLIB}
SRC    := $(shell rg --files -g '*.kt')
RES    := .res/:../src/.res/
BIN    := $(SRC:%.kt=%Kt)
CLASS  := $(BIN:%=.build/%.class)

MAIN := org.junit.platform.console.ConsoleLauncher 
OPTIONS := --scan-classpath --disable-banner --exclude-engine=junit-vintage --exclude-engine=junit-platform-suite

lsp: 
	@echo "#!/bin/bash\necho ${LIBS}.build/" >kls-classpath
	@chmod +x kls-classpath

test: 
	@echo "#!/bin/bash\n\nset -e\n\njava -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -cp .build/:../src/.build/:${LIBS}${RES} $(MAIN) $(OPTIONS)" >.build/test.sh
	@chmod +x .build/test.sh
	@.build/test.sh

build: libs 
	kotlinc ${SRC} -cp ${LIBS}:../src/.build/ -d .build

libs: .build/.libs/cached.txt

.build/.libs/cached.txt: .build/.libs/download.sh
	@.build/.libs/download.sh && cp libs.txt .build/.libs/cached.txt

.build/.libs/download.sh: libs.txt
	@mkdir -p .build/.libs
	@echo '#!/usr/bin/env bash\nfor i in $$(cat libs.txt | grep -v "^#") ; do mvn dependency:get -Dmaven.repo.local=./.build/.libs -Dartifact=$$i -Dtransitive=true ; done' >.build/.libs/download.sh
	@chmod +x .build/.libs/download.sh

clean:
	@mkdir -p .build
	rm -rf $(shell rg --files .build/ -g '*.class')


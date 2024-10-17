.PHONY: all libs clean

STDLIB := $(shell echo ${KOTLIN_HOME})/libexec/lib/kotlin-stdlib.jar
LOCAL  := $(shell rg --files .libs/ -g '*.jar' | tr '\n' ':')
LIBS   := $(shell rg --files .build/.libs/ -g '*.jar' | tr '\n' ':')${STDLIB}:${LOCAL}
SRC    := $(shell rg --files -g '*.kt')
RES    := .res/
BIN    := $(SRC:%.kt=%Kt)
CLASS  := $(BIN:%=.build/%.class)

lsp: 
	@echo "#!/bin/bash\necho ${LIBS}.build/" >kls-classpath
	@chmod +x kls-classpath

# PROJECT NAME: `make appjar NAME=myapp`
NAME := klib

# main class: `make run MAIN=myapp.AppKt`
MAIN := LogExampleKt

run: 
	@echo "#!/bin/bash\n\nset -e\n\njava -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -cp .build/:${LIBS}${RES} $(MAIN)" >.build/run.sh
	@chmod +x .build/run.sh
	@.build/run.sh

build: libs 
	kotlinc ${SRC} -cp ${LIBS} -d .build

libs: .build/.libs/cached.txt

.build/.libs/cached.txt: .build/.libs/download.sh
	@.build/.libs/download.sh && cp libs.txt .build/.libs/cached.txt

.build/.libs/download.sh: libs.txt
	@mkdir -p .build/.libs
	@echo '#!/usr/bin/env bash\nfor i in $$(cat libs.txt | grep -v "^#") ; do mvn dependency:get -Dmaven.repo.local=./.build/.libs -Dartifact=$$i -Dtransitive=true ; done' >.build/.libs/download.sh
	@chmod +x .build/.libs/download.sh

libjar:
	@kotlinc $(SRC) -cp ${LIBS} -d .build/${NAME}.jar 

appjar:
	@kotlinc $(SRC) -cp ${LIBS}:${RES} -include-runtime -d .build/${NAME}.jar 

clean:
	@mkdir -p .build
	@rm -rf $(shell rg --files .build/ -g '*.class')
	@rm -f .build/${NAME}.jar


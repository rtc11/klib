.PHONY: all libs clean

STDLIB := $(shell echo ${KOTLIN_HOME})/libexec/lib/kotlin-stdlib.jar
LIBS   := $(shell rg --files .libs/ -g '*.jar' | tr '\n' ':')${STDLIB}
SRC    := $(shell rg --files -g '*.kt')
RES    := .res/
BIN    := $(SRC:%.kt=%Kt)
CLASS  := $(BIN:%=.build/%.class)
COMPILE_OPT := -Xuse-fast-jar-file-system -Xenable-incremental-compilation -Xno-optimize

# PROJECT NAME: `make appjar NAME=myapp`
NAME := klib

# main class: `make run MAIN=myapp.AppKt`
MAIN := test.TesterKt

build: libs 
	@mkdir -p .build
	@echo "#!/bin/bash\n\nset -e\n\njava -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -cp .build/:${LIBS}:${RES} \$$@" >.build/run.sh
	@chmod +x .build/run.sh
	kotlinc ${SRC} -cp ${LIBS} ${COMPILE_OPT} -d .build

libs: .libs/cached.txt

.libs/cached.txt: .libs/download.sh
	@.libs/download.sh && cp libs.txt .libs/cached.txt

.libs/download.sh: libs.txt
	@mkdir -p .libs
	@echo '#!/usr/bin/env bash\nfor i in $$(cat libs.txt | grep -v "^#") ; do mvn dependency:get -Dmaven.repo.local=./.libs -Dartifact=$$i -Dtransitive=true ; done' >.libs/download.sh
	@chmod +x .libs/download.sh

lsp: 
	@echo "#!/bin/bash\necho .build/:${LIBS}" >kls-classpath
	@chmod +x kls-classpath

lib:
	@kotlinc $(SRC) -cp ${LIBS} ${COMPILE_OPT} -d .build/${NAME}.jar 

app:
	@kotlinc $(SRC) -cp ${LIBS}:${RES} ${COMPILE_OPT} -include-runtime -d .build/${NAME}.jar 

clean:
	@mkdir -p .build
	rm -rf .build/


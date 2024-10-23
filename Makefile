.PHONY: all libs clean

NAME := klib

STDLIB := $(shell echo ${KOTLIN_HOME})/libexec/lib/kotlin-stdlib.jar
LIBS   := $(shell rg --files .libs/ -g '*.jar' | tr '\n' ':')${STDLIB}
RES    := .res/

ALL    := $(shell rg --files -g '*.kt')
BIN    := $(ALL:%.kt=%Kt)
CLASS  := $(BIN:%=.build/%.class)

SRC    := $(shell rg --files src/ -g '*.kt')
SRC_BIN    := $(SRC:%.kt=%Kt)
SRC_CLASS  := $(SRC_BIN:%=.build/%.class)

TEST    := $(shell rg --files test/ -g '*.kt')
TEST_BIN    := $(TEST:%.kt=%Kt)
TEST_CLASS  := $(TEST_BIN:%=.build/%.class)

COMPILE_OPT := -Xuse-fast-jar-file-system -Xenable-incremental-compilation -Xno-optimize

print:
	@echo $(CLASS)

update: $(CLASS)

.build/src/serde/%.class: %.kt 
	echo $<

%.class: %.kt 
	echo $<
	# @echo "#!/bin/bash\n\n#set -e\n\njava -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -cp .build/:${LIBS}:${RES} \$$@" >.build/run.sh
	# @chmod +x .build/run.sh
	# kotlinc $< -cp ${LIBS} ${COMPILE_OPT} -d .build

build: libs buildsrc buildtest

# DELETE ME, JUST FOR COMPILING ONE FILE
test: .build/src/test/TesterKt.class 
# DELETE ME, JUST FOR COMPILING ONE FILE
.build/src/test/TesterKt.class: src/test/Tester.kt
	@kotlinc src/test/Tester.kt -cp ${LIBS}:.build/src ${COMPILE_OPT} -d .build/test

buildsrc:
	@mkdir -p .build/src
	@echo "#!/bin/bash\n\nset -e\n\njava -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -cp .build/src/:${LIBS}:${RES} \$$@" >.build/src/run.sh
	@chmod +x .build/src/run.sh
	kotlinc $(SRC) -cp ${LIBS} ${COMPILE_OPT} -d .build/src

buildtest:
	@mkdir -p .build/test
	@echo "#!/bin/bash\n\nset -e\n\njava -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -cp .build/test/:.build/src/:${LIBS}:${RES} \$$@" >.build/test/run.sh
	@chmod +x .build/test/run.sh
	kotlinc $(TEST) -cp ${LIBS}:.build/src ${COMPILE_OPT} -d .build/test

libs: .libs/cached.txt

.libs/cached.txt: .libs/download.sh
	@.libs/download.sh && cp libs.txt .libs/cached.txt

.libs/download.sh: libs.txt
	@mkdir -p .libs
	@echo '#!/usr/bin/env bash\nfor i in $$(cat libs.txt | grep -v "^#") ; do mvn dependency:get -Dmaven.repo.local=.libs -Dartifact=$$i -Dtransitive=true ; done' >.libs/download.sh
	@chmod +x .libs/download.sh

lsp: 
	@echo "#!/bin/bash\necho src/:test/:.build/:${LIBS}" >kls-classpath
	@chmod +x kls-classpath

lib:
	@kotlinc $(ALL) -cp ${LIBS} ${COMPILE_OPT} -d .build/${NAME}.jar 

app:
	@kotlinc $(ALL) -cp ${LIBS}:${RES} ${COMPILE_OPT} -include-runtime -d .build/${NAME}.jar 

clean:
	@mkdir -p .build/
	rm -rf .build/


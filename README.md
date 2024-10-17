# klib
A simple kotlin library for building a simple http server and client.

Strives to be simple and easy to use, with minimal dependencies..

## Development
Build with Makefile, no IDE required.

Build sources
`cd src && make build`

Build tests
`cd test && make build`

Run tests
`make test`

Build a library
`make jar`

Build an execuateble
`make app`

Index kotlin LSP
`make lsp`

- Everyting is compiled into .build/ dir.
- Build src and test separately.


```
.
├── examples
│   ├── .libs
│   │   └── klib.jar        # symlink to src/.build/klib.jar
│   ├── .res
│   │   └── log.conf        # configure the logger
│   ├── ClientExample.kt    
│   ├── LogExample.kt       
│   ├── ServerExample.kt    
│   ├── libs.txt            # dependent on kotlin coroutines
│   └── Makefile            # NO-IDE build
├── src
│   ├── client
│   ├── json
│   ├── parser
│   ├── serde
│   ├── server
│   ├── util
│   ├── libs.txt
│   └── Makefile            # build before running tests/examples
└── test
    ├── json
    ├── server
    ├── util
    ├── libs.txt            # dependent on junit
    └── Makefile
```

# Roadmap
- [x] HTTP Client
- [x] HTTP Server
- [x] IDE independent builds
- [ ] JDBC driver (postgres)
- [x] Logging support
    - [x] Console log
    - [ ] JSON log
    - [ ] File log
- [ ] Kafka?
- [ ] OAuth2
- [x] Parser Combinator
- [x] Rust-like Result type
- [x] Serializer and Deserializer
    - [x] JSON serde
    - [ ] XML serde
- [ ] Test enginge


# klib
A simple kotlin library for building a simple http server and client.

Strives to be simple and easy to use, with minimal dependencies..

## Development
Build with Makefile, no IDE required.

### Build
  - `make build`        # build everything
  - `make buildsrc`     # build src
  - `make buildtest`    # build test

### Release
  - `make lib`          # release library
  - `make app`          # release executable

### LSP
  - `make lsp`          # Index classpath and libs for kotlin-language-server

```
.
├── Makefile        # build script
├── libs.txt        # dependencies
├── test.sh         # test script
├── .res            # resources
│   └── log.conf    # log configuration
├── test
│   └── ...
└── src
    ├── client
    ├── examples
    ├── json
    ├── parser
    ├── serde
    ├── server
    ├── test
    └── util
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
- [x] Test enginge
    - [x] Output result to console (hide test print/log/errors in console)
    - [ ] Output result to file (test println/log/errors console)
- [x] neotest adapter


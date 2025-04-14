#!/bin/sh

set -e

# Run the tests, build the binary
# cargo clean
cargo fmt
cargo clippy
cargo build --release
cargo test
cargo test -- --ignored

# Copy the binary to the local bin directory
cp './target/release/sw' "${HOME}/.local/bin/"

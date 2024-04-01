# Sociable Weaver

Build documentation as code.

## Development Guidelines

1. Build and test the application

   This application is developed with the
   [rust](https://www.rust-lang.org/tools/install) programming language, version
   `1.76`.

   ```shell
   $ cargo clean
   $ cargo fmt
   $ cargo clippy
   $ cargo build --release
   $ cargo test
   $ cargo test -- --ignored
   ```

2. Continuous delivery

   This application employs GitHub Actions for automated building and testing.
   Every push to the `main` branch triggers these actions, ensuring that the
   application remains stable and functional with each update.

   You can find all official releases
   [here](https://github.com/albertattard/sociable-weaver/releases). There is no
   need for special tags; simply push your changes to `main`, and GitHub Actions
   will handle the build and testing processes.

3. Install the application

   **This only works on Linux**.

   ```shell
   $ mkdir -p "${HOME}/.local/bin"
   $ curl \
     --silent \
     --location \
     --output "${HOME}/.local/bin/sw" \
     'https://github.com/albertattard/sociable-weaver/releases/download/20240327.060739/sw'
   $ chmod +x "${HOME}/.local/bin/sw"
   $ sudo rm -f '/usr/local/bin/sw'
   $ sudo ln -s "${HOME}/.local/bin/sw" '/usr/local/bin/sw'
   ```

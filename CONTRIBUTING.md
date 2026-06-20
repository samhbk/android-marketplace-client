# Contributing

Thank you for your interest in this project. This is a **portfolio sample**; contributions that improve clarity, tests, or maintainability are welcome.

## Getting started

1. Fork the repository and create a branch from `main`.
2. Open the project in **Android Studio** (or use the CLI with JDK 17 + Android SDK).
3. Point the app at a running [symfony-marketplace-api](https://github.com/sameh-bakleh/symfony-marketplace-api) instance (see [README.md](README.md)).

## Configuration

This Android app does **not** load a `.env` file at runtime. API configuration lives in:

- `app/build.gradle.kts` → default `API_BASE_URL` via `BuildConfig`
- `local.properties` → optional `API_BASE_URL` override + Android SDK path (gitignored; never commit)
- [`.env.example`](.env.example) and [`local.properties.example`](local.properties.example) → documentation templates

```bash
cp local.properties.example local.properties
# Set sdk.dir (and optionally API_BASE_URL)
```

## Code style

- Match existing Kotlin and Compose conventions in the repo.
- Keep changes focused; avoid unrelated refactors in the same PR.
- Prefer `Result` and `StateFlow` patterns already used in repositories and ViewModels.

## Tests

Add or update unit tests when changing repository or ViewModel logic:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
```

CI runs the same commands on every push and pull request.

## Pull requests

- Fill out the PR template completely.
- Ensure `./gradlew testDebugUnitTest` passes locally when possible.
- Include a short note on **what** changed and **why**.

## Issues

Use the GitHub issue templates for bugs and feature requests. For security concerns, see [SECURITY.md](SECURITY.md) — do not open public issues for vulnerabilities.

## License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE).

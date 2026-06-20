# Security Policy

This repository is a **portfolio sample** for local development against a self-hosted API.

## Supported versions

| Version | Supported |
|---------|-----------|
| `main` / latest | Yes |
| Older tags | Best effort |

## Reporting a vulnerability

**Do not** open a public GitHub issue for security problems.

1. Use [GitHub Security Advisories](https://github.com/sameh-bakleh/android-marketplace-client/security/advisories/new) for private disclosure, **or**
2. Contact the maintainer through the channel listed on their GitHub profile.

Include steps to reproduce, impact, and any suggested fix if you have one.

## Secrets

- Do **not** commit API keys, JWT private keys, keystores (`.jks`, `.keystore`), or production credentials.
- `local.properties` (Android SDK path, optional `API_BASE_URL`) is gitignored — never commit it.
- Templates: [`local.properties.example`](local.properties.example), [`.env.example`](.env.example) (documentation only).
- `API_BASE_URL` defaults to `http://10.0.2.2:8080` for the emulator — use local/dev hosts only.

This project does **not** load a `.env` file at runtime. Configuration is via `build.gradle.kts` (`BuildConfig`) and gitignored `local.properties`.

## Network

- Cleartext HTTP is enabled for **dev hosts** (`10.0.2.2`, `localhost`) via `network_security_config.xml`.
- Production builds should use **HTTPS** and disable cleartext traffic.

## Token storage

- Access and refresh tokens are stored in **DataStore Preferences** (not encrypted).
- Acceptable for a demo; production apps should use Encrypted DataStore or the Android Keystore.

## Logging

- OkHttp body logging runs only when `BuildConfig.DEBUG` is true.

## Dependency updates

Keep Gradle dependencies reasonably current. Review release notes before major version bumps.

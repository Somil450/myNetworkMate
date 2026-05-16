# Security Policy

## Supported Versions

Currently, only the latest release on the `main` branch is supported with security updates.

## Reporting a Vulnerability

Security is a top priority for SignalSense AI, especially given the sensitive nature of telecom and location data.

If you discover a security vulnerability within this project, please DO NOT open a public issue. Instead, send an email to the project maintainers directly.

Please include:
*   A description of the vulnerability.
*   Steps to reproduce.
*   Potential impact (e.g., data leak, unauthorized access).

We will acknowledge receipt of your vulnerability report within 48 hours and strive to provide a resolution or mitigation plan promptly.

## Privacy First
SignalSense AI operates on a strict Privacy-First principle. Any PR that attempts to bypass the `SecurityManager` consent flow or extract raw PII (Personally Identifiable Information) without encryption will be immediately rejected.

# Contributing to Network Mate

First off, thank you for considering contributing to Network Mate! It's people like you that make the open-source telecom analytics community thrive.

## Code of Conduct
By participating in this project, you are expected to uphold our [Code of Conduct](CODE_OF_CONDUCT.md).

## How Can I Contribute?

### Reporting Bugs
*   Ensure the bug was not already reported by searching on GitHub under Issues.
*   If you're unable to find an open issue addressing the problem, open a new one. Be sure to include a title and clear description, as much relevant information as possible, and a code sample or an executable test case demonstrating the expected behavior that is not occurring.

### Suggesting Enhancements
*   Open a new issue with a clear title and description.
*   Provide a compelling reason why the enhancement should be included.

### Pull Requests
1.  Fork the repo and create your branch from `main`.
2.  If you've added code that should be tested, add tests.
3.  If you've changed APIs, update the documentation.
4.  Ensure the test suite passes.
5.  Make sure your code lints.
6.  Issue that pull request!

## Architecture Guidelines
Please adhere to the Clean Architecture principles used in this project. Do not mix Presentation logic in the Data or Domain layers. Use Hilt for all dependency injections.

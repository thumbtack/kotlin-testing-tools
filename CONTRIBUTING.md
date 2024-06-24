# Contributing to Kotlin Testing Tools

Contributions to Thumbtack's Kotlin Testing Tools are welcomed. These contributions can
range from small bug reports to requesting new features.

Here are a few ways to get started:

## File a bug or request a feature

Providing feedback is the easiest way to contribute. You can do this by
[creating an issue on GitHub](https://github.com/thumbtack/kotlin-testing-tools/issues).

## Contribute code to Kotlin Testing Tools

There are two ways to contribute code to Kotlin Testing Tools:

1. **Tackle open GitHub issues:** Issues labeled as “[good first issue](https://github.com/thumbtack/kotlin-testing-tools/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22)” or “[help wanted](https://github.com/thumbtack/kotlin-testing-tools/issues?q=is%3Aopen+is%3Aissue+label%3A%22help+wanted%22)” are perfect for contributors that want to tackle small tasks.
2. **Propose an improvement to an existing function or suggest a new function:** Please [create a GitHub issue](https://github.com/thumbtack/kotlin-testing-tools/issues) to propose an enhancement to the repo. Thumbtack will then review the request and will respond on the issue if we decide to move forward with it.

### Submitting a pull request

You can create a pull request using the standard `gh pr create` command. Here are a few things to keep in mind when
creating a pull request:

- **Tests:** Our suite of tests will run automatically on the creation of a pr but you can also run them on your local
branch by running `./gradlew check`.

- **Creating a local maven JAR:** If you want to test your changes locally before creating a PR, you can publish your
JAR locally by running `./gradlew publishToMavenLocal`. Make sure to add the maven local repo to the application that
is importing Kotlin Testing Tools so it is fetched from there and not JitPack.

If you're having issues, try changing the version number to one that isn't currently used by JitPack and importing that
version locally.

## Releasing a new version of Kotlin Testing Tools

This will be done by a member of Thumbtack Engineering when code has been merged and is ready for release.

1. From your local repo, pull the latest `main` branch.
2. Run `scripts/release.sh x.y.z`. This will create a new release tag and bump the version number in Gradle.
3. **Create a new release in GitHub:** On the [Releases](https://github.com/thumbtack/kotlin-testing-tools/releases) page
for the repo, click "Draft a new release". Set "Tag version" to the name of the tag you created in step 3
(e.g., `1.2.3`). Set "Release title" to the same value as the tag version. In the description field, give an overview of the changes going into this release. When all fields have been filled out, click "Publish release."

---

As always, [create an issue](https://github.com/thumbtack/thumbprint-android/issues) if you have questions or feedback. Thank you!

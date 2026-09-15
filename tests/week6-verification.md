# Week 6 verification

Verified on **15 September 2026**, using Java 25. Release code: `985f3a0`.
The scope is the individual project (iP), as described on the
[Week 6 project page](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week6/project.html).
Team-project deliverables belong in the team's separate repository.

## Required increments

The course requires at least two of these optional increments. All four are
implemented, and their exact lightweight tags are present on GitHub.

| Increment | Evidence |
| --- | --- |
| `A-BetterGui` | Asymmetric conversation, readable styling, resizable window, task counts, command suggestions, and history. |
| `A-Personality` | Wangsa's quest theme, character avatars, and friendly task responses. |
| `A-MoreErrorHandling` | Missing values, invalid dates and numbers, duplicate markers, full lists, bad saved data, and failed saves. |
| `A-MoreTesting` | Parser, task list, SQLite, legacy storage, shared service, CLI, formatting, AI fallback, and platform-selection regression tests. |

## Java and release checks

`./gradlew clean check jar javadoc` builds the executable JAR with Java 25.
**80 unit tests pass**, with no failures, errors, or skips. Both Checkstyle tasks
and Javadoc generation pass.

The [release verification run](https://github.com/Manutd1234/ip/actions/runs/34981216513)
built one JAR on Ubuntu and tested that same artifact on:

| Platform | Unit/style checks | Packaged GUI and storage checks |
| --- | --- | --- |
| Linux x64 | Passed | Passed under Xvfb |
| Windows x64 | Passed | Passed |
| macOS Intel | Passed | Passed |
| macOS Apple Silicon | Passed | Passed |

`tests/release_smoke.py` copies the JAR to a temporary folder and compiles its
small test harness against that JAR alone. The test invokes the actual launcher
and checks adding every task type, finding with original task numbers, marking,
unmarking, sorting, deleting, listing, built-in help, command history, resizing,
and `bye`. It restarts in a separate JVM to check saved state, then verifies that
a corrupt database blocks commands and remains unchanged. The packaged CLI reads
the same saved tasks and provides offline AI help without a key.

The four-platform artifact was downloaded and checked again on local macOS
ARM64 with Java 25.0.4. The documented `java -jar Wangsa.jar` command was also
checked in an empty folder. No repository task data was used.

Release artifact: `Wangsa.jar`, 29,682,266 bytes.

```text
SHA-256: 098c1d7f6cb5e7fd2d3c86e30105e33f3361daf530ccb9028dc217ee25c1c823
```

## User guide and desktop

- The [product website](https://manutd1234.github.io/ip/) uses GitHub Pages with
  `master` and `/docs`; `docs/index.md` includes the guide from `docs/README.md`.
- The published guide was inspected in a browser, including its table of contents,
  command summary, and screenshot. Its setup instructions cover Java 25 and the
  supported release platforms. AI remains optional.
- `docs/Ui.png` is a fresh PNG capture of the full Wangsa window, including the
  title and command box. The running desktop was checked with all three task
  types, completion, list output, and header counts using disposable sample data.
- The guide describes all commands, automatic saving, backup and recovery,
  task numbering, the 100-task limit, and unsupported event chronology checks.
- Credits for the starter, OpenJFX packaging guidance, AI integration guidance,
  and user-guide structure are recorded in the README and guide.

## Submission checks and limits

- Keep the latest release's single `Wangsa.jar` asset, the published guide, and
  `master` synchronized. The course PR uses `branch-Level-7` as its source.
- The last five commit subjects were checked against the SE-EDU conventions.
  Published history and milestone tags were retained.
- The public progress dashboard anonymizes student identities. The user should
  confirm their own **Git Standard** cell after its daily refresh; this report
  does not claim that the dashboard is green or verify earlier peer-review work.
- [Artwork credits](../docs/CREDITS.md) identify the reused characters and a
  matching Charizard reference. The original download pages were not recorded;
  the creator and source of the particular Ash illustration remain unverified.
- A teammate's test drive remains unconfirmed. The
  [peer smoke-test checklist](peer-smoke-test.md) contains the steps and a
  result template. Automated cross-platform checks do not replace that result.
- Live Groq answer quality was not evaluated. Automated AI tests use fixtures
  and failure cases; the release works without AI setup.
- GUI checks cover the listed GitHub runner environments, not every OS version
  or display setup. JavaFX 17 can emit compatibility warnings on Java 25.

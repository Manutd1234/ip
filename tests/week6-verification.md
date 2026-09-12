# Week 6 quality and user guide verification

Verified on 12 September 2026 with Java 25.0.4 on macOS ARM64, starting from
`master` at `468668c`. Changes are on `codex/week6-quality-user-guide`.

## Generated feedback

The findings in [issue #3](https://github.com/Manutd1234/ip/issues/3) were addressed:

| Finding | Resolution |
| --- | --- |
| Boolean naming | Rename the migration flag to `isLegacyMigrationPending` and the completion parameter to `shouldMarkAsDone`. |
| Long legacy parser method | Extract status parsing and type-specific construction; retain line-specific validation errors. |
| Long GUI composer method | Extract command field, row, send button, and suggestion builders. |
| Header comment | Rephrase the add-command summary and correct the legacy storage class description. |
| Commit bodies | Wrap every new commit message at 72 characters. Existing published history is retained. |

## Automated checks

`./gradlew clean check jar javadoc` passed: **59 tests**, no failures or skips,
both Checkstyle tasks, Javadoc generation, and executable JAR packaging.

Regression coverage includes malformed legacy records, whitespace and detail
markers, full-list search numbers, the CLI search-to-mark flow, failed startup,
SQLite position shifts, and retrying a corrected legacy import. The position-shift
test was observed failing before the fix and passing afterward.

## Desktop smoke checks

A temporary JavaFX harness loaded the packaged app in isolated folders under
`/tmp/wangsa-week6-smoke`; the repository's runtime data was not used.

- Add all three task types, find, mark, unmark, sort, delete, and list: passed.
- Search numbering, no-match output, duplicate-marker rejection: passed.
- Header counts, Up/Down command history, and suggestion prefix: passed.
- Malformed-database startup and blocked task commands: passed.
- Direct database inspection confirmed the normal saved order and unchanged
  malformed row after rejected commands.
- Scene screenshots were inspected for the normal and blocked-startup states.

## Documentation checks and remaining release work

- The guide renders locally with 20 headings and two tables; the screenshot
  exists, and the Pages index includes the guide from its single source.
- GitHub Pages already uses `master` and `/docs`.
- Browser policy blocked opening the local HTML preview. The published Jekyll
  output has not been visually verified for this branch.
- Commits and the lightweight `A-UserGuide` tag are local. Merge and push them
  before expecting the public website or course dashboard to change.
- This build includes JavaFX libraries for macOS ARM64. Windows, Linux, and Intel
  macOS execution were not tested; a cross-platform release remains separate work.
- JavaFX 17 emitted module/Unsafe deprecation warnings on Java 25, while both
  desktop smoke scenarios completed successfully.

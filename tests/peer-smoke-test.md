# Manual test checklist

Extract the download ZIP into an empty folder and open its start script, or copy
the JAR there and run `java -jar Wangsa.jar`. Check Java 25 with `java -version`.
No API key is needed.

Click the command box at the bottom. Enter these commands in order, one at a time.
Use Enter for the todo and SEND for the deadline to test both ways of submitting.

| Command or action | Expected result |
| --- | --- |
| `todo read a book` | A todo is added. |
| `deadline return book /by 2026-09-20` | A deadline is added. |
| `event lunch /from 12pm /to 1pm` | An event is added with both times. |
| `list` | Three tasks appear, numbered 1–3. |
| `find book` | The todo and deadline appear with their original numbers. |
| `mark 1`, then `unmark 1` | The todo becomes complete, then incomplete. |
| `deadline ST2334 by 2026-09-30` | A missing `/by` error appears; no task is added. |
| `deadline ST2334 /by 2026-09-31` | A date error appears because September has only 30 days; no task is added. |
| `deadline test /by 2026-02-30` | A date error appears; no task is added. |
| `mark 99` | A number error appears; tasks stay unchanged. |
| `sort`, then `list` | The deadline comes first, then the todo and event. |
| `delete 2` | The todo is removed, leaving the deadline and event. |
| `help` | Four point-form command groups with examples appear; the reply opens at its beginning. |
| Press Up, then Down in the input | Up recalls `help`; Down returns to an empty input. |
| `@ai How do I add a task?` without a key | Offline help appears. |
| Resize the window and scroll | Messages wrap; Due/From/To align with their task title. Symbols, header, and input stay aligned. Help cards stack on narrow windows. |
| `bye`, then restart the same JAR from another working folder | The deadline and event are restored from the text file beside the JAR. |

Try a few commands from the [user guide](../docs/README.md) as well.
For any problem, record the exact command, expected result, and actual result.
The valid September example is `deadline ST2334 /by 2026-09-30`.
Do not add it during the sequence above unless you also adjust the expected task counts.

## Test record

- Tester:
- Date:
- OS and processor:
- Java version:
- Release or commit:
- JAR SHA-256:
- Result:
- Problems found:

# Manual test checklist

Extract the download ZIP into an empty folder and open its start script, or copy
the JAR there and run `java -jar Wangsa.jar`. Check Java 25 with `java -version`.
No API key is needed.

Enter these commands in order:

| Command or action | Expected result |
| --- | --- |
| `todo read a book` | A todo is added. |
| `deadline return book /by 2026-09-20` | A deadline is added. |
| `event lunch /from 12pm /to 1pm` | An event is added with both times. |
| `list` | Three tasks appear, numbered 1–3. |
| `find book` | The todo and deadline appear with their original numbers. |
| `mark 1`, then `unmark 1` | The todo becomes complete, then incomplete. |
| `deadline test /by 2026-02-30` | A date error appears; no task is added. |
| `mark 99` | A number error appears; tasks stay unchanged. |
| `sort`, then `list` | The deadline comes first, then the todo and event. |
| `delete 2` | The todo is removed, leaving the deadline and event. |
| `help` | The command reference appears. |
| `@ai How do I add a task?` without a key | Offline help appears. |
| Resize the window and scroll | Messages wrap; avatars, header, and input stay aligned. |
| `bye`, then restart the same JAR from another working folder | The deadline and event are restored from the text file beside the JAR. |

Try a few commands from the [user guide](../docs/README.md) as well.
For any problem, record the exact command, expected result, and actual result.

## Test record

- Tester:
- Date:
- OS and processor:
- Java version:
- Release or commit:
- JAR SHA-256:
- Result:
- Problems found:

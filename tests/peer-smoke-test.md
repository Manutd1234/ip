# Teammate test drive

Status: **Awaiting a teammate's result.** Automated release tests have passed
on four platforms; this checklist records the separate human test requested
by the Week 6 instructions.

Download the single `Wangsa.jar` asset from
[v0.4](https://github.com/Manutd1234/ip/releases/tag/v0.4), put it in a new empty
folder, and open a terminal there. Run `java -version` to confirm Java 25, then
run `java -jar Wangsa.jar`. No API key is needed.

Use this short sequence in the desktop command box:

| Command or action | Expected result |
| --- | --- |
| `todo read a book` | A todo is added. |
| `deadline return book /by 2026-09-20` | A deadline is added with the correct date. |
| `event lunch /from 12pm /to 1pm` | An event is added with both times. |
| `list` | Three tasks appear, numbered 1–3. |
| `find book` | The todo and deadline appear with their original numbers. |
| `mark 1`, then `unmark 1` | The todo changes to completed, then incomplete. |
| `deadline test /by 2026-02-30` | A helpful date error appears; no task is added. |
| `mark 99` | An invalid-number error appears; existing tasks stay intact. |
| `sort`, then `list` | The three tasks remain present; use their current numbers. |
| `delete NUMBER` | The task with that displayed number is removed. |
| `help` | A command reference appears. |
| Resize the window and scroll | Messages and the command box remain usable. |
| `bye`, then run the JAR again from the same folder | The two remaining tasks are restored. |

Also try a few commands of your own using the
[user guide](https://manutd1234.github.io/ip/). Report anything confusing, even
if the app did not crash.

## Result to record

- Tester:
- Date:
- OS and processor architecture:
- Output of `java -version`:
- Release: v0.4
- Result: pending
- Problems found, with the exact command and observed behavior:

Do not mark the test as passed until a teammate has run it and reported back.

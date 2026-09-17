# Wangsa

Wangsa is a task manager with a desktop window and a terminal
interface. It tracks todos, deadlines, and events, and saves changes automatically.

[User guide](https://manutd1234.github.io/ip/) ·
[Download](https://github.com/Manutd1234/ip/releases/latest) ·
[Credits](docs/CREDITS.md)

## Try it

Click the command box at the bottom of the window. Enter one command at a time,
then press **Enter** or click **SEND**:

```text
todo read a book
deadline ST2334 /by 2026-09-30
list
```

Keep the slash in `/by`. Dates must exist: September has 30 days, so
`2026-09-31` is not valid. Type `help` for more examples.

## Run from source

Use **Java 25**. From the project folder:

```shell
java -version
./gradlew run
```

For the terminal interface, use `./gradlew runCli`. On Windows, replace
`./gradlew` with `gradlew.bat`.

In IntelliJ IDEA, open the project folder, import the Gradle project, and set both
the project SDK and Gradle JVM to Java 25. Keep Java source files under
`src/main/java`.

## Build and test

```shell
./gradlew clean check shadowJar portableZip javadoc
python3 tests/release_smoke.py build/libs/Wangsa.jar
```

The first command runs the unit tests and style checks, generates Javadoc, and
builds `build/libs/Wangsa.jar` and `build/distributions/Wangsa.zip`. The ZIP includes
Windows and Mac start scripts and a short quick start. The second checks the packaged app in temporary
folders, without changing your saved tasks. It sets the command field and invokes
SEND programmatically; it does not replace clicking and typing through the
[manual checklist](tests/peer-smoke-test.md). It needs a graphical desktop.
Use `python` on Windows or `xvfb-run -a python3` on headless Linux.

The `shadowJar` task is an alias for Wangsa's custom fat-JAR builder. The JAR
includes JavaFX and supports Windows x64, Linux x64, Intel Mac, and Apple Silicon.
Linux needs GTK 3. To try the JAR, copy it to an empty folder and run:

```shell
java -jar Wangsa.jar
```

See the [test plan](tests/test-plan.md) and [manual checklist](tests/peer-smoke-test.md).

## How the code is organised

The desktop and terminal interfaces share the same parser, task service, and
storage. `Task` subclasses represent todos, deadlines, and events. `Storage` saves
tasks in a plain text file, `data/wangsa.txt`. The packaged app keeps this beside
the JAR; source runs use the project folder. No database or account is needed.

The [architecture guide](docs/ARCHITECTURE.md) explains the main classes.
The user guide lives in `docs/README.md`; GitHub Pages serves it from `master`
and `/docs`.

## Optional AI help

`help` works offline. `@ai QUESTION` can ask Groq to explain a command; it cannot
change tasks. Set `LLM_API_KEY` to a Groq key to enable it. The default in-app
model is `openai/gpt-oss-20b`; `LLM_MODEL` can override it.

See [AI setup and privacy](docs/README.md#optional-ai-command-help-ai).

## AI assistance and credits

Manutd1234 used **OpenAI Codex with [GPT-6 Astra](https://developers.openai.com/api/docs/models/gpt-6-astra)**
to help write and refactor code, improve the GUI and error handling, add tests,
prepare the JAR, and write documentation. AI contributed to substantial parts of
the project, not just wording. The project author is responsible for the final work.
This development assistance is separate from the optional Groq feature in the app.

## Acknowledgements

Wangsa started from the [course iP template](https://github.com/NUS-CS2103-AY2627-S1/ip),
based on [SE-EDU Duke](https://github.com/se-edu/duke).

- [JavaFX](https://openjfx.io/) provides the desktop interface.
- [LangChain4j](https://github.com/langchain4j/langchain4j) connects optional command help to Groq.
- [SLF4J](https://www.slf4j.org/) handles dependency logging.
- [JUnit](https://junit.org/), [Gradle](https://gradle.org/), and
  [Checkstyle](https://checkstyle.org/) support testing, builds, and style checks.

The interface uses simple symbols drawn in JavaFX, with no external artwork or audio.
The [credits page](docs/CREDITS.md) lists the code sources, tutorials, and AI assistance.

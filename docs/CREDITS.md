---
layout: default
title: Wangsa credits
---

# Credits

## AI assistance

Manutd1234 used **OpenAI Codex with [GPT-6 Astra](https://developers.openai.com/api/docs/models/gpt-6-astra)**
for help with implementation, OOP refactoring, GUI layout, error handling,
tests, JAR packaging, and documentation. AI helped write and revise substantial
parts of the project. The project author is responsible for the final work.

The optional `@ai` command is separate: it uses Groq through LangChain4j.
Its default model is `openai/gpt-oss-20b`, not GPT-6 Astra.

## Interface symbols

Wangsa uses simple arrows and a compass drawn in JavaFX code. It does not use
third-party artwork, character images, generated illustrations, or audio.
The user guide shows a screenshot of the app itself.

## Starter code and guides

- [Course iP template](https://github.com/NUS-CS2103-AY2627-S1/ip), based on
  [SE-EDU Duke](https://github.com/se-edu/duke): starting project.
- [OpenJFX fat-JAR guidance](https://openjfx.io/openjfx-docs/#modular):
  cross-platform packaging.
- [SE-EDU AI tutorial](https://se-education.org/guides/tutorials/addingAiToJavaApp.html):
  read-only AI command help.
- [AddressBook Level 3 User Guide](https://se-education.org/addressbook-level3/UserGuide.html):
  inspiration for the user-guide structure.

## Libraries and tools

| Library or tool | Used for |
| --- | --- |
| [JavaFX](https://openjfx.io/) | Desktop interface |
| [LangChain4j](https://github.com/langchain4j/langchain4j) | Connecting to Groq |
| [SLF4J](https://www.slf4j.org/) | Dependency logging |
| [JUnit](https://junit.org/) | Unit tests |
| [Gradle](https://gradle.org/) | Building and running the project |
| [Checkstyle](https://checkstyle.org/) | Checking Java style |

Versions are listed in `build.gradle`. Each dependency has its own licence.
This credits page is also included in the JAR at `META-INF/wangsa/CREDITS.md`.

[Back to the user guide](./)

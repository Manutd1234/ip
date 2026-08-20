package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** Tests command classification, task construction, and argument validation. */
class ParserTest {
    private final Parser parser = new Parser();

    @Test
    void parseCommandType_recognizesSupportedCommands() throws WangsaException {
        assertEquals(Parser.CommandType.BYE, parser.parseCommandType("bye"));
        assertEquals(Parser.CommandType.LIST, parser.parseCommandType("list"));
        assertEquals(Parser.CommandType.MARK, parser.parseCommandType("mark 1"));
        assertEquals(Parser.CommandType.UNMARK, parser.parseCommandType("unmark 1"));
        assertEquals(Parser.CommandType.DELETE, parser.parseCommandType("delete 1"));
        assertEquals(Parser.CommandType.ADD_TASK, parser.parseCommandType("todo read book"));
    }

    @Test
    void parseTask_buildsAllTaskTypesAndParsesDates() throws WangsaException {
        assertInstanceOf(Todo.class, parser.parseTask("todo read book"));

        Deadline deadline = (Deadline) parser.parseTask("deadline return book /by 2019-12-02");
        assertEquals(LocalDate.of(2019, 12, 2), deadline.getBy());
        assertEquals("[D][ ] return book (by: Dec 2 2019)", deadline.toString());

        Event event = (Event) parser.parseTask("event meeting /from 2pm /to 4pm");
        assertEquals("2pm", event.getFrom());
        assertEquals("4pm", event.getTo());
    }

    @Test
    void parseTask_rejectsMalformedOrInvalidDates() {
        assertThrows(WangsaException.class, () -> parser.parseTask("todo"));
        assertThrows(WangsaException.class, () -> parser.parseTask("deadline return book /by 2019-02-29"));
        assertThrows(WangsaException.class, () -> parser.parseTask("event meeting /from 2pm"));
        assertThrows(WangsaException.class, () -> parser.parseTask("unknown task"));
    }

    @Test
    void parseTaskNumber_rejectsMissingAndNonNumericArguments() throws WangsaException {
        assertEquals(12, parser.parseTaskNumber("delete 12"));
        assertThrows(WangsaException.class, () -> parser.parseTaskNumber("mark"));
        assertThrows(WangsaException.class, () -> parser.parseTaskNumber("mark one"));
    }
}

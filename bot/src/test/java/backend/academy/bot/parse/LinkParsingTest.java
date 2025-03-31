package backend.academy.bot.parse;

import static org.junit.Assert.assertEquals;

import backend.academy.bot.session.UserIO;
import java.util.List;
import org.junit.jupiter.api.Test;

public class LinkParsingTest {
    @Test
    public void testLinkParsing() {
        UserIO userIO = new UserIO(null, null);
        List<String> parsedLinks = userIO.parseInput("https://example.com https://another.com");

        assertEquals(List.of("https://example.com", "https://another.com"), parsedLinks);
    }
}

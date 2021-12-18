package ru.curs.celestadoc.generator;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class FromCelestaToAsciidocGeneratorTest {

    @Test
    void test() throws Exception {
        final Path adoc = Files.createTempFile("cdtest", ".adoc");
        final Path pu = Paths.get(adoc.toString().replace(".adoc", ".pu"));
        System.out.println(adoc);
        System.out.println(pu);
        try {
            try (FromCelestaToAsciidocGenerator generator = new FromCelestaToAsciidocGenerator(
                    "src/test/resources",
                    adoc.toString(), "_",
                    true, "")) {
                generator.generate();
            }

            final String files = Stream.of(
                    Files.lines(adoc),
                    Stream.of("--------------"),
                    Files.lines(pu))
                    .reduce(Stream::concat)
                    .orElseGet(Stream::empty).collect(Collectors.joining("\n"));
            Approvals.verify(files);
        } finally {
            Files.delete(adoc);
            Files.delete(pu);
        }
    }

}
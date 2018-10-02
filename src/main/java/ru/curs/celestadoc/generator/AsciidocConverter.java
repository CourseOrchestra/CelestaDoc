package ru.curs.celestadoc.generator;

import org.asciidoctor.Asciidoctor;

import java.nio.file.Paths;
import java.util.Map;

import static org.asciidoctor.OptionsBuilder.options;
import static org.asciidoctor.internal.JRubyAsciidoctor.create;

public class AsciidocConverter {

    private AsciidocConverter() {}

    public static void convert(String fileName) {
        Asciidoctor asciidoctor = create();

        Map<String, Object> options = options()
                .backend("pdf")
                .asMap();

        String pdf = asciidoctor.convertFile(Paths.get(fileName).toFile(), options);

        if (pdf != null && !pdf.isEmpty()) {
            throw new RuntimeException("Conversion is not done!");
        }
    }
}

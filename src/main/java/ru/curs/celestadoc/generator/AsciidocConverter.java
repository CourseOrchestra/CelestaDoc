package ru.curs.celestadoc.generator;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.SafeMode;

import java.nio.file.Paths;
import java.util.Map;

import static org.asciidoctor.OptionsBuilder.options;
import static org.asciidoctor.internal.JRubyAsciidoctor.create;

public class AsciidocConverter {
    private final static Asciidoctor asciidoctor = create();

    private AsciidocConverter() {}

    public static void convertToPdf(String fileName) {
        Map<String, Object> options = options()
                .backend("pdf")
                .asMap();

        String pdf = asciidoctor.convertFile(Paths.get(fileName).toFile(), options);

        if (pdf != null && !pdf.isEmpty()) {
            throw new RuntimeException("Conversion is not done!");
        }
    }

    public static void convertToHtml(String fileName) {

        Map<String, Object> options = options()
                .backend("html")
                .safe(SafeMode.UNSAFE)
                .asMap();

        String html = asciidoctor.convertFile(Paths.get(fileName).toFile(), options);

        if (html != null && !html.isEmpty()) {
            throw new RuntimeException("Conversion is not done!");
        }
    }
}

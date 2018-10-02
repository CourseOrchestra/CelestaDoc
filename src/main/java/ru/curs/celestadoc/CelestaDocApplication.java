package ru.curs.celestadoc;

import ru.curs.celestadoc.generator.AsciidocConverter;
import ru.curs.celestadoc.generator.FromCelestaToAsciidocGenerator;
import ru.curs.celestadoc.helper.LocaleDefinition;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import static ru.curs.celestadoc.validator.CommandLineArgumentValidator.ValidateException;
import static ru.curs.celestadoc.validator.CommandLineArgumentValidator.validate;

public class CelestaDocApplication {
    public static void main(String[] args) {
        if (args.length != 2) {
            help();
            System.exit(1);
        }
        try {
            validate(args);
        } catch (ValidateException exc) {
            System.out.println(exc.getMessage());
            System.exit(1);
        }

        String celestaPath = args[0];

        LocaleDefinition definition = null;
        try {
            definition= LocaleDefinition.getLocaleDefinition(args[1]);
        } catch (IllegalArgumentException exc) {
            System.out.println(exc.getMessage());
            System.out.println("You can use: " + Arrays.stream(LocaleDefinition.values())
                                                        .map(LocaleDefinition::name)
                                                        .collect(Collectors.joining(", ")));
            System.exit(1);
        }

        try (FromCelestaToAsciidocGenerator generator =
                     new FromCelestaToAsciidocGenerator(celestaPath, "report.adoc", definition)) {
            generator.generate();
        } catch (IOException e) {
            System.out.println(e.getMessage() + ": cannot create file for write asciidoc");
        } catch (Exception e) {
            System.out.println("Celesta parser error: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            AsciidocConverter.convert("report.adoc");
        } catch (RuntimeException exc) {
            System.out.println(exc.getMessage());
        }
    }

    private static void help() {
        System.out.println(helpMessage);
    }

    private static final String helpMessage = "Please use the next command to run:\ncelestadoc C:/user/score/ docru\n" +
            "where C:/user/score - path where celesta sql files are\n" +
            "      docru - argument for language of document. You can write comments in your celesta sql in format:\n" +
            "doc(locale): some_comment, for example: doc-ru: Комментарий\n" +
            "                                        doc-en: A comment\n" +
            "                                        doc-fr: Le commentaire\n ";
}

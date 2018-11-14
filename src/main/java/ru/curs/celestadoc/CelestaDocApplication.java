package ru.curs.celestadoc;

import java.io.IOException;
import ru.curs.celestadoc.arg.Argument;
import ru.curs.celestadoc.generator.AsciidocConverter;
import ru.curs.celestadoc.generator.FromCelestaToAsciidocGenerator;
import ru.curs.celestadoc.validator.CommandLineArgumentValidator;

public class CelestaDocApplication {
    public static void main(String[] args) {
        try {
            Argument argument = new Argument(args);

            try (FromCelestaToAsciidocGenerator generator = new FromCelestaToAsciidocGenerator(
                    argument.getCelestaPath(), argument.getFileName(), argument.getPrefix(), argument.is(Argument.PLANTUML),
                    argument.getIncludeFile())) {
                generator.generate();
            } catch (IOException e) {
                System.out.println(e.getMessage() + ": cannot create file for write asciidoc");
                System.exit(1);
            } catch (Exception e) {
                System.out.println("Celesta parser error: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }

            try {
                if (argument.is(Argument.PDF)) {
                    AsciidocConverter.convertToPdf(argument.getFileName());
                }
                if (argument.is(Argument.HTML)) {
                    AsciidocConverter.convertToHtml(argument.getFileName());
                }
            } catch (RuntimeException exc) {
                System.out.println(exc.getMessage());
            }
        } catch (CommandLineArgumentValidator.ValidateException exc) {
            System.out.println(exc.getMessage());
            help();
            System.exit(1);
        }
    }

    private static void help() {
        System.out.println(helpMessage);
    }

    private static final String helpMessage = "Please use the next command to run:\ncelestadoc C:/user/score/ doc-ru " +
            "report.adoc\n" +
            "where C:/user/score - path where celesta sql files are\n" +
            "      docru - argument for language of document. You can write comments in your celesta sql in format:\n" +
            "doc(locale): some_comment, for example: doc-ru: Комментарий\n" +
            "                                        doc-en: A comment\n" +
            "                                        doc-fr: Le commentaire\n " +
            "Also you can use flag -pdf or(and) -html for converting asciidoc result to pdf format.\n" +
            "Flag -plantuml for generating plantuml's report. You can add optional param <filename> after flag -plantuml," +
            "this file will be used as param file in section !include";
}

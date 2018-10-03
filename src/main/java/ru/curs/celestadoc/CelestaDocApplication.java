package ru.curs.celestadoc;

import ru.curs.celestadoc.generator.AsciidocConverter;
import ru.curs.celestadoc.generator.FromCelestaToAsciidocGenerator;

import java.io.IOException;

import static ru.curs.celestadoc.validator.CommandLineArgumentValidator.ValidateException;
import static ru.curs.celestadoc.validator.CommandLineArgumentValidator.validate;

public class CelestaDocApplication {
    public static void main(String[] args) {
        if (args.length < 3) {
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
        String prefix = args[1];
        String fileName = args[2];
        boolean isPdf = args.length == 4 && args[3].equalsIgnoreCase("-pdf");
        boolean isHtml = args.length == 4 && args[3].equalsIgnoreCase("-html");

        if (args.length == 5) {
            String options = (args[3] + args[4]).toLowerCase();
            isPdf = options.contains("-pdf");
            isHtml = options.contains("-html");
        }

        try (FromCelestaToAsciidocGenerator generator =
                     new FromCelestaToAsciidocGenerator(celestaPath, fileName, prefix)) {
            generator.generate();
        } catch (IOException e) {
            System.out.println(e.getMessage() + ": cannot create file for write asciidoc");
        } catch (Exception e) {
            System.out.println("Celesta parser error: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            if (isPdf) {
                AsciidocConverter.convertToPdf(fileName);
            }
            if (isHtml) {
                AsciidocConverter.convertToHtml(fileName);
            }
        } catch (RuntimeException exc) {
            System.out.println(exc.getMessage());
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
            "Also you can use flag -pdf or(and) -html for converting asciidoc result to pdf format.\n";
}

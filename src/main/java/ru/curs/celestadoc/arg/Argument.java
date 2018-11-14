package ru.curs.celestadoc.arg;

import java.util.HashMap;
import java.util.Map;
import ru.curs.celestadoc.validator.CommandLineArgumentValidator;

import static ru.curs.celestadoc.validator.CommandLineArgumentValidator.validateFile;

public class Argument {
    public static final String PDF = "-pdf";
    public static final String HTML = "-html";
    public static final String PLANTUML = "-plantuml";

    private String celestaPath;
    private String prefix;
    private String fileName;
    private String includeFile = "pu_params.pu";

    private Map<String, Boolean> formatsForConversion;

    public Argument(String[] args) {
        validateArgsLength(args);

        celestaPath = args[0];
        validateFile(celestaPath);

        prefix = args[1];
        fileName = args[2];

        formatsForConversion = new HashMap<>();
        parseFormatArgs(args);
    }

    public String getCelestaPath() {
        return celestaPath;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getFileName() {
        return fileName;
    }

    public String getIncludeFile() {
        return includeFile;
    }

    public boolean is(String flag) {
        Boolean result = formatsForConversion.get(flag.toLowerCase());
        return result != null && result;
    }

    private void validateArgsLength(String[] args) {
        if (args.length < 3) {
            throw new CommandLineArgumentValidator.ValidateException("At least should be 3 args");
        }
    }

    private void parseFormatArgs(String[] args) {
        if (args.length < 4) {
            return;
        }

        for (int i = 3; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case PDF:
                    formatsForConversion.put(PDF, Boolean.TRUE);
                    break;
                case HTML:
                    formatsForConversion.put(HTML, Boolean.TRUE);
                    break;
                case PLANTUML:
                    formatsForConversion.put(PLANTUML, Boolean.TRUE);
                    break;
                default:
                    if (args[i - 1].equalsIgnoreCase(PLANTUML)) {
                        validateFile(args[i]);
                        includeFile = args[i];
                    } else {
                        throw new CommandLineArgumentValidator.ValidateException("Incorrect argument " + args[i]);
                    }
            }
        }
    }
}

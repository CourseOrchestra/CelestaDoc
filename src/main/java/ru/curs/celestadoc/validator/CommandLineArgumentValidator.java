package ru.curs.celestadoc.validator;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CommandLineArgumentValidator {
    public static void validate(String[] args) {
        Path path = Paths.get(args[0]);
        File file = path.toFile();
        if (!file.isDirectory() || !file.canRead()) {
            throw new ValidateException(String.format("%s - is not directory or cannot be read", args[0]));
        }
    }

    public static class ValidateException extends RuntimeException {
        ValidateException(String message) {
            super(message);
        }
    }
}

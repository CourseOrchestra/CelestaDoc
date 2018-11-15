package ru.curs.celestadoc.validator;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CommandLineArgumentValidator {
    public static void validateFile(String fileName) {
        Path path = Paths.get(fileName);
        File file = path.toFile();
        if (!file.isDirectory() || !file.canRead()) {
            throw new ValidateException(String.format("%s - is not directory or cannot be read", fileName));
        }
    }

    public static class ValidateException extends RuntimeException {
        public ValidateException(String message) {
            super(message);
        }
    }
}

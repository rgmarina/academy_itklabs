package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TestLogParser {

    private record TestKey(String name) {
    }

    public record ParseReport(
            List<TestResult> results,
            List<String> errors
    ) {
    }

    public ParseReport parse(List<String> lines) {
        Map<TestKey, TestResult> latestResults =
                new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i);
            int lineNumber = i + 1;

            if (line == null || line.isBlank()) {
                errors.add("Строка " + lineNumber
                        + " — пустая строка");
                continue;
            }

            String[] parts = line.split("\\|", -1);

            if (parts.length != 3) {
                errors.add("Строка " + lineNumber
                        + ": " + line
                        + " — ожидалось 3 поля, получено "
                        + parts.length);
                continue;
            }

            String name = parts[0].trim();
            String statusText = parts[1].trim();
            String durationText = parts[2].trim();

            if (name.isEmpty()) {
                errors.add("Строка " + lineNumber
                        + ": " + line
                        + " — пустое имя теста");
                continue;
            }
            Status status;

            try {
                status = Status.valueOf(statusText);
            } catch (IllegalArgumentException e) {
                errors.add("Строка " + lineNumber
                        + ": " + line
                        + " — неизвестный статус: "
                        + statusText);
                continue;
            }
            long duration;

            try {
                duration = Long.parseLong(durationText);
            } catch (NumberFormatException e) {
                errors.add("Строка " + lineNumber
                        + ": " + line
                        + " — не число в длительности");
                continue;
            }

            if (duration < 0) {
                errors.add("Строка " + lineNumber
                        + ": " + line
                        + " — отрицательная длительность");
                continue;
            }

            TestResult result =
                    new TestResult(name, status, duration);
            TestKey key = new TestKey(name);
            latestResults.remove(key);
            latestResults.put(key, result);
        }

        return new ParseReport(
                List.copyOf(latestResults.values()),
                List.copyOf(errors)
        );
    }

    public Optional<TestResult> firstFailed(
            List<TestResult> results
    ) {
        return results.stream()
                .filter(result ->
                        result.status() == Status.FAILED)
                .findFirst();
    }

    public static void main(String[] args)
            throws IOException {

        List<String> lines = new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             Path.of("run.log"),
                             StandardCharsets.UTF_8
                     )) {

            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        TestLogParser parser = new TestLogParser();
        ParseReport report = parser.parse(lines);
        System.out.println("Результаты:");

        for (TestResult result : report.results()) {
            System.out.println("  "
                    + result.name()
                    + " — " + result.status()
                    + " (" + result.durationMs() + " ms)");
        }

        if (report.errors().isEmpty()) {
            System.out.println("Битые строки: нет");
        } else {
            System.out.println("Битые строки:");
            for (String error : report.errors()) {
                System.out.println("  " + error);
            }
        }

        Optional<TestResult> failed =
                parser.firstFailed(report.results());
        System.out.println("Первый упавший: "
                + failed.map(TestResult::name)
                .orElse("Упадений нет"));
    }
}

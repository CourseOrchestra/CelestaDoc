package ru.curs.celestadoc.generator;

import ru.curs.celesta.score.*;
import ru.curs.celestadoc.helper.XMLResourceBundleControl;
import ru.curs.celestadoc.reader.CelestaSqlReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FromCelestaToAsciidocGenerator implements AutoCloseable{
    private final ResourceBundle header = ResourceBundle.getBundle("header", new XMLResourceBundleControl());
    private final ResourceBundle scheme = ResourceBundle.getBundle("scheme", new XMLResourceBundleControl());
    private final ResourceBundle table = ResourceBundle.getBundle("table", new XMLResourceBundleControl());
    private final ResourceBundle fkeyTable = ResourceBundle.getBundle("fkey_table", new XMLResourceBundleControl());
    private final ResourceBundle plantUml = ResourceBundle.getBundle("plantuml", new XMLResourceBundleControl());
    private final String regex = "(%s:)(.+)$";

    private final Pattern pattern;
    private final CelestaSqlReader celestaSqlReader;
    private final BufferedWriter writerAsciiDoc;
    private final BufferedWriter writerPlantUml;
    private final String params;

    public FromCelestaToAsciidocGenerator(String celestaPath, String outputPath, String prefix,
                                          boolean isPlantUml, String params)
            throws ParseException, IOException {
        celestaSqlReader = new CelestaSqlReader(celestaPath);
        writerAsciiDoc = Files.newBufferedWriter(Paths.get(outputPath));
        pattern = Pattern.compile(String.format(regex, prefix));

        if (isPlantUml) {
            writerPlantUml = Files.newBufferedWriter(Paths.get(outputPath.replace(".adoc", ".pu")));
            this.params = params;
        } else {
            writerPlantUml = null;
            this.params = null;
        }
    }

    public void generate() throws IOException {
        writerAsciiDoc.write(header.getString("header"));
        writerAsciiDoc.newLine();

        writePlantUml(plantUml.getString("start"));
        writePlantUml(String.format(plantUml.getString("include"), params));

        Map<String, Grain> grains = celestaSqlReader.getGrains();
        for (Map.Entry<String, Grain> entry : grains.entrySet()) {
            String schemeName = entry.getKey();
            String schemeDoc = getDescription(entry.getValue().getCelestaDoc());

            writerAsciiDoc.write(String.format(scheme.getString("scheme"), schemeName, schemeName, schemeDoc));
            writerAsciiDoc.newLine();

            Map<String, Table> tableMap = entry.getValue().getTables();
            Map<Table, List<ForeignKey>> tableForeignKeyMap = getForeignKeys(tableMap);
            for (Map.Entry<String, Table> tableEntry : tableMap.entrySet()) {
                String tableName = tableEntry.getKey();
                String celestaTableIdentifier = String.format("celestareporter_t_%s_%s", schemeName, tableName);
                String tableDescription = getDescription(tableEntry.getValue().getCelestaDoc());

                writerAsciiDoc.write(String.format(
                        table.getString("tableName"), celestaTableIdentifier, tableName, celestaTableIdentifier,
                        tableDescription));
                writerAsciiDoc.newLine();

                writerAsciiDoc.write(table.getString("table"));
                writerAsciiDoc.newLine();

                writePlantUml(String.format(plantUml.getString("classStart"),
                        tableName, celestaTableIdentifier));

                Map<String, Column> columnMap = tableEntry.getValue().getColumns();
                Set<ForeignKey> foreignKeys = tableEntry.getValue().getForeignKeys();
                for (Map.Entry<String, Column> columnEntry : columnMap.entrySet()) {
                    String field = columnEntry.getKey();
                    String formatUmlField = plantUml.getString("field");

                    if (tableEntry.getValue().getPrimaryKey().containsKey(field)) {
                        field += table.getString("keyIcon");
                        formatUmlField = plantUml.getString("key");
                    }
                    Column column = columnEntry.getValue();

                    String specification = getSpecification(column);
                    String description = getDescription(column.getCelestaDoc());

                    writerAsciiDoc.write(String.format(table.getString("row"), field, specification, description));
                    writerAsciiDoc.newLine();

                    writePlantUml(String.format(formatUmlField, columnEntry.getKey()));
                }
                writerAsciiDoc.write(table.getString("tableEnd"));
                writerAsciiDoc.newLine();

                writePlantUml(plantUml.getString("classEnd"));

                if (tableForeignKeyMap.containsKey(tableEntry.getValue())) {
                    writerAsciiDoc.write(table.getString("isReference"));
                    writerAsciiDoc.newLine();
                    for (ForeignKey fk : tableForeignKeyMap.get(tableEntry.getValue())) {
                        String referencedCelestaIdentifier =
                                String.format("celestareporter_t_%s_%s", schemeName, fk.getParentTable().getName());
                        String schemeTableName = String.format("%s.%s", schemeName, fk.getParentTable().getName());
                        writerAsciiDoc.write(String.format(
                                table.getString("referencedTable"), referencedCelestaIdentifier, schemeTableName));
                        writerAsciiDoc.newLine();
                    }
                    writerAsciiDoc.write(table.getString("isReferenceEnd"));
                    writerAsciiDoc.newLine();
                }

                if (!foreignKeys.isEmpty()) {
                    writerAsciiDoc.write(String.format(
                            fkeyTable.getString("fkeyTable"), celestaTableIdentifier));
                    writerAsciiDoc.newLine();
                    for (ForeignKey fk : foreignKeys) {
                        Set<String> fkeysNames = fk.getColumns().keySet();
                        Set<String> pkeysNames = fk.getReferencedTable().getPrimaryKey().keySet();

                        Iterator<String> fkeysNamesIterator = fkeysNames.iterator();
                        Iterator<String> pkeysNamesIterator = pkeysNames.iterator();

                        StringBuilder keyField = new StringBuilder();
                        while (fkeysNamesIterator.hasNext() && pkeysNamesIterator.hasNext()) {
                            keyField.append('*').append(fkeysNamesIterator.next())
                                    .append('*').append(":")
                                    .append(pkeysNamesIterator.next()).append('\n').append('\n');
                        }
                        String referencedCelestaIdentifier =
                                String.format("celestareporter_t_%s_%s", schemeName, fk.getReferencedTable().getName());
                        String schemeTableName = String.format("%s.%s", schemeName, fk.getReferencedTable().getName());

                        writerAsciiDoc.write(String.format(fkeyTable.getString("table"), keyField.toString(),
                                referencedCelestaIdentifier, schemeTableName));

                        writePlantUml(String.format(plantUml.getString("reference"),
                                tableName, fk.getReferencedTable().getName()));
                    }
                    writerAsciiDoc.write(String.format(
                            fkeyTable.getString("fkeyEnd"), celestaTableIdentifier));
                    writerAsciiDoc.newLine();
                }

                writerAsciiDoc.write(String.format(table.getString("tableSectionEnd"), celestaTableIdentifier));
                writerAsciiDoc.newLine();

            }
            writerAsciiDoc.write(String.format(scheme.getString("schemeEnd"), schemeName));
            writerAsciiDoc.newLine();

            writePlantUml(plantUml.getString("end"));
        }
    }

    private void writePlantUml(String line) throws IOException {
        if (writerPlantUml != null) {
            writerPlantUml.write(line);
            writerPlantUml.newLine();
        }
    }

    @Override
    public void close() throws Exception {
        writerAsciiDoc.close();
        if (writerPlantUml != null) {
            writerPlantUml.close();
        }
    }

    private String getDescription(String celestaDoc) {
        if (celestaDoc == null) {
            return "";
        }

        for (String docLine : celestaDoc.split("\\r?\\n")) {
            Matcher matcher = pattern.matcher(docLine.trim());
            if (matcher.find()) {
                return matcher.group(2).trim();
            }
        }
        return "";
    }

    private Map<Table, List<ForeignKey>> getForeignKeys(Map<String, Table> tableMap) {
        Map<Table, List<ForeignKey>> result = new HashMap<>();
        for (Map.Entry<String, Table> entry : tableMap.entrySet()) {
            Table table = entry.getValue();
            for (ForeignKey fk : table.getForeignKeys()) {
                if (result.containsKey(fk.getReferencedTable())) {
                    result.get(fk.getReferencedTable()).add(fk);
                } else {
                    List<ForeignKey> foreignKeys = new ArrayList<>();
                    foreignKeys.add(fk);
                    result.put(fk.getReferencedTable(), foreignKeys);

                }
            }
        }

        return result;
    }

    private String getSpecification(Column column) {
        String specification = column.getCelestaType();
        if (specification.equalsIgnoreCase("varchar")) {
            specification = String.format("%s(%d)", specification, ((StringColumn) column).getLength());
        } else if (specification.equalsIgnoreCase("decimal")) {
            DecimalColumn decimalColumn = (DecimalColumn) column;
            specification = String.format("%s(%d,%d)", specification, decimalColumn.getScale(),
                    decimalColumn.getPrecision());
        }
        if (!column.isNullable()) {
            specification += " NOT NULL";
        }
        if (column.getCelestaDefault() != null) {
            specification += String.format(" DEFAULT %s", column.getCelestaDefault());
        }
        return specification;
    }
}

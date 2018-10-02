package ru.curs.celestadoc.generator;

import ru.curs.celesta.score.*;
import ru.curs.celestadoc.helper.LocaleDefinition;
import ru.curs.celestadoc.helper.XMLResourceBundleControl;
import ru.curs.celestadoc.reader.CelestaSqlReader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FromCelestaToAsciidocGenerator implements AutoCloseable{
    private final ResourceBundle header = ResourceBundle.getBundle("header", new XMLResourceBundleControl());
    private final ResourceBundle scheme = ResourceBundle.getBundle("scheme", new XMLResourceBundleControl());
    private final ResourceBundle table = ResourceBundle.getBundle("table", new XMLResourceBundleControl());
    private final ResourceBundle fkeyTable = ResourceBundle.getBundle("fkey_table", new XMLResourceBundleControl());
    private final String regex = "(%s:)(.+)$";

    private final Pattern pattern;
    private final CelestaSqlReader celestaSqlReader;
    private final BufferedWriter writer;
    private final ResourceBundle param;

    public FromCelestaToAsciidocGenerator(String celestaPath, String outputPath, LocaleDefinition locale)
            throws ParseException, IOException {
        celestaSqlReader = new CelestaSqlReader(celestaPath);
        writer = new BufferedWriter(new FileWriter(outputPath));
        pattern = Pattern.compile(String.format(regex, locale.getValue()));

        param = ResourceBundle.getBundle("param", locale.getLocale(), new XMLResourceBundleControl());
    }

    public void generate() throws IOException {
        writer.write(header.getString("header"));
        writer.newLine();
        writer.write(param.getString("params"));
        writer.newLine();

        Map<String, Grain> grains = celestaSqlReader.getGrains();
        for (Map.Entry<String, Grain> entry : grains.entrySet()) {
            String schemeName = entry.getKey();
            writer.write(String.format(scheme.getString("scheme"), schemeName));
            writer.newLine();

            Map<String, Table> tableMap = entry.getValue().getTables();
            Map<Table, List<ForeignKey>> tableForeignKeyMap = getForeignKeys(tableMap);
            for (Map.Entry<String, Table> tableEntry : tableMap.entrySet()) {
                String tableName = tableEntry.getKey();
                String celestaTableIdentifier = String.format("celestareporter_t_%s_%s", schemeName, tableName);
                String tableDescription = getDescription(tableEntry.getValue().getCelestaDoc());

                writer.write(String.format(
                        table.getString("tableName"), celestaTableIdentifier, tableName, celestaTableIdentifier,
                        tableDescription));
                writer.newLine();

                writer.write(table.getString("table"));
                writer.newLine();

                Map<String, Column> columnMap = tableEntry.getValue().getColumns();
                Set<ForeignKey> foreignKeys = tableEntry.getValue().getForeignKeys();
                for (Map.Entry<String, Column> columnEntry : columnMap.entrySet()) {
                    String field = columnEntry.getKey();
                    if (tableEntry.getValue().getPrimaryKey().containsKey(field)) {
                        field += table.getString("keyIcon");
                    }
                    Column column = columnEntry.getValue();

                    String specification = getSpecification(column);
                    String description = getDescription(column.getCelestaDoc());

                    writer.write(String.format(table.getString("row"), field, specification, description));
                    writer.newLine();
                }
                writer.write(table.getString("tableEnd"));
                writer.newLine();

                if (tableForeignKeyMap.containsKey(tableEntry.getValue())) {
                    writer.write(table.getString("isReference"));
                    writer.newLine();
                    for (ForeignKey fk : tableForeignKeyMap.get(tableEntry.getValue())) {
                        String referencedCelestaIdentifier =
                                String.format("celestareporter_t_%s_%s", schemeName, fk.getParentTable().getName());
                        writer.write(String.format(
                                table.getString("referencedTable"), referencedCelestaIdentifier));
                        writer.newLine();
                    }
                    writer.write(table.getString("isReferenceEnd"));
                    writer.newLine();
                }

                if (!foreignKeys.isEmpty()) {
                    writer.write(String.format(
                            fkeyTable.getString("fkeyTable"), celestaTableIdentifier));
                    writer.newLine();
                    for (ForeignKey fk : foreignKeys) {
                        Set<String> fkeysNames = fk.getColumns().keySet();
                        Set<String> pkeysNames = fk.getReferencedTable().getPrimaryKey().keySet();

                        Iterator<String> fkeysNamesIterator = fkeysNames.iterator();
                        Iterator<String> pkeysNamesIterator = pkeysNames.iterator();

                        StringBuilder keyField = new StringBuilder();
                        while (fkeysNamesIterator.hasNext() && pkeysNamesIterator.hasNext()) {
                            keyField.append('*').append(fkeysNamesIterator.next())
                                    .append('*').append(":").append(pkeysNamesIterator.next()).append('\n');
                        }
                        String referencedCelestaIdentifier =
                                String.format("celestareporter_t_%s_%s", schemeName, fk.getReferencedTable().getName());

                        writer.write(String.format(fkeyTable.getString("table"), keyField.toString(),
                                referencedCelestaIdentifier, fk.getReferencedTable().getName()));
                    }
                    writer.write(String.format(
                            fkeyTable.getString("fkeyEnd"), celestaTableIdentifier));
                    writer.newLine();
                }

                writer.write(String.format(table.getString("tableSectionEnd"), celestaTableIdentifier));
                writer.newLine();

            }
            writer.write(scheme.getString("schemeEnd"));
            writer.newLine();
        }
    }

    @Override
    public void close() throws Exception {
        writer.close();
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

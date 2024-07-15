package mobiliz.tospringdoc.writer.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.github.javaparser.printer.DefaultPrettyPrinter;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import mobiliz.tospringdoc.core.MigrationUnit;
import mobiliz.tospringdoc.writer.SourceWriter;


public class SourceFileWriter implements SourceWriter {

    private String targetPath;

    public SourceFileWriter(String targetPath) {
        this.targetPath = targetPath;
    }

    @Override
    public void write(MigrationUnit migrationUnit) throws IOException {
        File file = new File(targetPath + migrationUnit.getRelativePath());
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        DefaultPrettyPrinter printer =
                new DefaultPrettyPrinter(new DefaultPrinterConfiguration()
                        .addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.INDENT_PRINT_ARRAYS_OF_ANNOTATIONS)));

        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(printer.print(migrationUnit.getCompilationUnit()));
        fileWriter.close();
    }
}

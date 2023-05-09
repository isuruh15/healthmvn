package org.wso2.healthcare;

import io.ballerina.cli.BLauncherCmd;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;

@CommandLine.Command(name = "healthmvn", description = "Health mvn sub tool")
public class HealthMavenTool implements BLauncherCmd {

    private final PrintStream printStream;
    private final String toolName = "healthmvn";
    @CommandLine.Option(names = {"--help", "-h", "?"}, usageHelp = true)
    private boolean helpFlag;

    @CommandLine.Parameters(description = "User name")
    private List<String> argList;

    public HealthMavenTool(PrintStream printStream) {
        this.printStream = printStream;
    }

    public HealthMavenTool(){
        this.printStream = System.out;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            StringBuilder builder = new StringBuilder();
            builder.append("Sample tool for bal tool testing\n\n");
            builder.append("bal health-mvn [args]\n\n");
            builder.append("--args--\n");
            builder.append("  <name>\n");
            builder.append("    greets with a hello <name>\n\n");
            printStream.println(builder);
            return;
        }
        printStream.println("health-mvn command is executing\n");
        if (argList == null || argList.size() != 1) {
            printStream.println("Invalid number of arguments recieved!\n try bal health-mvn --help for more information.");
            return;
        }
        printStream.println("Hello " + argList.get(0) + "!");

    }

    @Override
    public String getName() {
        return toolName;
    }

    @Override
    public void printLongDesc(StringBuilder stringBuilder) {

    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {

    }

    @Override
    public void setParentCmdParser(CommandLine commandLine) {

    }
}
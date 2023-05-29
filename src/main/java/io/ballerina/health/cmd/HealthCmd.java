
package io.ballerina.health.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.health.cmd.fhir.FhirSubCmd;
import io.ballerina.health.cmd.hl7.Hl7SubCmd;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;

@CommandLine.Command(
        name = "health",
        description = "Health mvn sub tool",
        subcommands = {
                FhirSubCmd.class,
                Hl7SubCmd.class
        }
)
public class HealthCmd implements BLauncherCmd {

    private final PrintStream printStream;
    private final String toolName = "health";
    @CommandLine.Option(names = {"--help", "-h", "?"}, usageHelp = true)
    private boolean helpFlag;

    @CommandLine.Parameters(description = "Options for the sub commands")
    private List<String> argList;

    private String specPath;
    private String subCommand;

    public HealthCmd(PrintStream printStream) {
        this.printStream = printStream;
    }

    public HealthCmd() {
        this.printStream = System.out;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            StringBuilder builder = new StringBuilder();
            builder.append("Sample tool for bal tool testing\n\n");
            builder.append("bal health [args]\n\n");
            builder.append("--args--\n");
            builder.append("  <name>\n");
            builder.append("    greets with a hello <name>\n\n");
            printStream.println(builder);
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            printStream.println(commandUsageInfo);
            return;
        }

        if (argList == null || argList.size() < 2) {
            //at minimum arg count is 2 (protocol and spec path)
            printStream.println("Invalid number of arguments received!\n try bal health --help for more information.");
            return;
        }
        //spec path is the last argument
        specPath = argList.get(argList.size()-1);
        subCommand = argList.get(0);

        printStream.println("health command is executing\n");

        printStream.println("Hello " + argList.get(0) + "! \n Please use sub command to generate artifacts." +
                "$ bal health <protocol: fhir|hl7> [OPTIONS]");

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
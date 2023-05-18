/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.health.cmd.fhir;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.cli.BLauncherCmd;
import io.ballerina.health.cmd.core.config.HealthCmdConfig;
import io.ballerina.health.cmd.core.exception.BallerinaHealthException;
import io.ballerina.health.cmd.core.utils.ErrorMessages;
import io.ballerina.health.cmd.core.utils.HealthCmdUtils;
import org.apache.commons.lang3.StringUtils;
import org.wso2.healthcare.codegen.tooling.common.config.ToolConfig;
import org.wso2.healthcare.codegen.tooling.common.core.TemplateGenerator;
import org.wso2.healthcare.codegen.tooling.common.core.Tool;
import org.wso2.healthcare.codegen.tooling.common.core.ToolContext;
import org.wso2.healthcare.codegen.tooling.common.exception.CodeGenException;
import org.wso2.healthcare.codegen.tooling.common.model.JsonConfigType;
import org.wso2.healthcare.fhir.codegen.tool.lib.config.FHIRToolConfig;
import org.wso2.healthcare.fhir.codegen.tool.lib.core.FHIRTool;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

@CommandLine.Command(name = "fhir", description = "Generates Ballerina service/client for FHIR contract for Ballerina service.")
public class FhirSubCmd implements BLauncherCmd {
    private final PrintStream printStream;
    private boolean exitWhenFinish;
    private final String toolName = "fhir";
    private String configPath;
    private String resourceHome;
    private String target;

    @CommandLine.Option(names = {"--help", "-h", "?"}, usageHelp = true)
    private boolean helpFlag;

    @CommandLine.Parameters(description = "User name")
    private List<String> argList;

    public FhirSubCmd(PrintStream printStream, boolean exitWhenFinish) {
        this.printStream = printStream;
        this.exitWhenFinish = exitWhenFinish;
    }

    public FhirSubCmd() {
        this.printStream = System.out;
        this.exitWhenFinish = true;
        this.configPath = "/home/isurus/open-healthcare/open-healthcare-integration/healthcare-codegen-tool-framework/healthcare-codegen-tool-executors/healthcare-codegen-fhir-to-x-tool-executor/src/main/resources/tool-config.json";
        this.resourceHome = "/home/isurus/open-healthcare/open-healthcare-integration/healthcare-codegen-tool-framework/healthcare-codegen-tool-impl/tools/fhir-integration-project-gen-tool/src/main/resources";
        this.target = "/home/isurus/open-healthcare/Ballerina/Tools/health-tool-v1/gen";
    }

    @Override
    public void execute() {
        printStream.println("Hello from FHIR " + argList.get(0) + "!");

        JsonObject executorConfig = null;
        try {
            //this might not needed
            executorConfig = HealthCmdConfig.getParsedConfig("/home/isurus/open-healthcare/open-healthcare-integration/healthcare-codegen-tool-framework/healthcare-codegen-tool-executors/healthcare-codegen-fhir-to-x-tool-executor/src/main/resources/tool-executor-config.json");
        } catch (BallerinaHealthException e) {
            //todo: verify
//            LOG.error("Error while initializing tool lib configs.", e);
            printStream.println(ErrorMessages.CONFIG_INITIALIZING_FAILED);
            HealthCmdUtils.exitError(this.exitWhenFinish);
        }

        JsonElement toolExecConfigs = executorConfig.get("toolExecConfigs");
        JsonArray toolExecConfigArr = toolExecConfigs.getAsJsonArray();
        FHIRToolConfig fhirToolConfig = new FHIRToolConfig();

        if (StringUtils.isNotBlank(configPath)) {
            JsonConfigType toolConfig = null;
            FHIRTool fhirToolLib = null;
            try {
                JsonObject toolConfigJson = HealthCmdConfig.getParsedConfig(configPath);
                toolConfig = new JsonConfigType(toolConfigJson);
                fhirToolLib = new FHIRTool();
                fhirToolConfig.configure(toolConfig);
                fhirToolLib.initialize(fhirToolConfig);
            } catch (CodeGenException | BallerinaHealthException e) {
                //todo: verify if internal logging is needed
//                LOG.error("Error while initializing tool lib configs.", e);
                printStream.println(ErrorMessages.LIB_INITIALIZING_FAILED);
                HealthCmdUtils.exitError(this.exitWhenFinish);
            }

            for (JsonElement jsonElement : toolExecConfigArr) {
                JsonObject toolExecConfig = jsonElement.getAsJsonObject();
                String configClassName = toolExecConfig.get("configClass").getAsString();
                String toolClassName = toolExecConfig.get("toolClass").getAsString();
                String name = toolExecConfig.get("name").getAsString();
                Tool tool;
                TemplateGenerator mainTemplateGenerator = null;
                try {
                    Class<?> configClazz = Class.forName(configClassName);
                    Class<?> toolClazz = Class.forName(toolClassName);
                    ToolConfig toolConfigInstance = (ToolConfig) configClazz.newInstance();
                    //todo if null set to tool resources directory
                    toolConfigInstance.setConfigHomeDir(resourceHome);
                    toolConfigInstance.setTargetDir(target);
                    toolConfigInstance.setToolName(name);
                    JsonArray tools = toolConfig.getConfigObj().get("tools").getAsJsonArray();
                    for (JsonElement element : tools) {
                        JsonElement toolName = element.getAsJsonObject().get("name");
                        if (toolName.getAsString().equals(name)) {
                            toolConfigInstance.configure(new JsonConfigType(element.getAsJsonObject()));
                        }
                    }

                    tool = (Tool) toolClazz.newInstance();
                    tool.initialize(toolConfigInstance);
                    fhirToolLib.getToolImplementations().putIfAbsent(name, tool);
                    mainTemplateGenerator = tool.execute(fhirToolLib.getToolContext());
                } catch (ClassNotFoundException e) {
//                    LOG.error("Error occurred while loading tool classes.", e);
                    printStream.println(ErrorMessages.CONFIG_INITIALIZING_FAILED);
                    HealthCmdUtils.exitError(this.exitWhenFinish);
//                    return;
                } catch (InstantiationException | IllegalAccessException e) {
//                    LOG.error("Error occurred while instantiating classes", e);
//                    return;
                    printStream.println(ErrorMessages.CONFIG_INITIALIZING_FAILED);
                    HealthCmdUtils.exitError(this.exitWhenFinish);
                } catch (CodeGenException e) {
//                    LOG.error("Error occurred while initializing tool configs for the tool: " + toolClassName);
//                    return;
                    printStream.println(ErrorMessages.CONFIG_INITIALIZING_FAILED);
                    HealthCmdUtils.exitError(this.exitWhenFinish);
                }
                if (mainTemplateGenerator != null) {
                    try {
                        mainTemplateGenerator.generate(fhirToolLib.getToolContext(),
                                mainTemplateGenerator.getGeneratorProperties());
                        TemplateGenerator childTemplateGenerator = mainTemplateGenerator.getChildTemplateGenerator();
                        engageChildTemplateGenerators(childTemplateGenerator, fhirToolLib.getToolContext(),
                                mainTemplateGenerator.getGeneratorProperties());
                    } catch (CodeGenException e) {
//                        LOG.error("Error occurred while template generation for the tool: " + name, e);
//                        return;
                        printStream.println(ErrorMessages.CONFIG_INITIALIZING_FAILED);
                        HealthCmdUtils.exitError(this.exitWhenFinish);
                    }
                } else {
//                    LOG.error("Template generator is not registered for the tool: " + name);
                    printStream.println(ErrorMessages.CONFIG_INITIALIZING_FAILED);
                    HealthCmdUtils.exitError(this.exitWhenFinish);
                }
            }
        }



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

    private void engageChildTemplateGenerators(TemplateGenerator templateGenerator, ToolContext context,
                                               Map<String, Object> properties) throws CodeGenException {
        if (templateGenerator != null) {
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("generating templates for child template generator: " +
//                        templateGenerator.getClass().getName());
//            }
            templateGenerator.generate(context, properties);
            engageChildTemplateGenerators(templateGenerator.getChildTemplateGenerator(), context, properties);
        }
    }
}

/**
 * Copyright (C) 2014-2015 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.pinot.tools.admin;

import java.lang.reflect.Field;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;

import com.linkedin.pinot.tools.Quickstart;
import com.linkedin.pinot.tools.admin.command.AddSchemaCommand;
import com.linkedin.pinot.tools.admin.command.AddTableCommand;
import com.linkedin.pinot.tools.admin.command.AddTenantCommand;
import com.linkedin.pinot.tools.admin.command.Command;
import com.linkedin.pinot.tools.admin.command.CreateSegmentCommand;
import com.linkedin.pinot.tools.admin.command.GenerateDataCommand;
import com.linkedin.pinot.tools.admin.command.PostQueryCommand;
import com.linkedin.pinot.tools.admin.command.StartBrokerCommand;
import com.linkedin.pinot.tools.admin.command.StartControllerCommand;
import com.linkedin.pinot.tools.admin.command.StartServerCommand;
import com.linkedin.pinot.tools.admin.command.StartZookeeperCommand;
import com.linkedin.pinot.tools.admin.command.StopProcessCommand;
import com.linkedin.pinot.tools.admin.command.UploadSegmentCommand;
import com.linkedin.pinot.tools.admin.command.StartKafkaCommand;
import com.linkedin.pinot.tools.admin.command.StreamAvroIntoKafkaCommand;


/**
 * Class to implement Pinot Administrator, that provides the following commands:
 *
 */
public class PinotAdministrator {

  // @formatter:off
  @Argument(handler = SubCommandHandler.class, metaVar = "<subCommand>")
  @SubCommands({
      @SubCommand(name = "GenerateData", impl = GenerateDataCommand.class),
      @SubCommand(name = "CreateSegment", impl = CreateSegmentCommand.class),
      @SubCommand(name = "StartZookeeper", impl = StartZookeeperCommand.class),
      @SubCommand(name = "StartKafka", impl = StartKafkaCommand.class),
      @SubCommand(name = "StreamAvroIntoKafka", impl = StreamAvroIntoKafkaCommand.class),
      @SubCommand(name = "StartController", impl = StartControllerCommand.class),
      @SubCommand(name = "StartBroker", impl = StartBrokerCommand.class),
      @SubCommand(name = "StartServer", impl = StartServerCommand.class),
      @SubCommand(name = "AddTable", impl = AddTableCommand.class),
      @SubCommand(name = "AddTenant", impl = AddTenantCommand.class),
      @SubCommand(name = "AddSchema", impl = AddSchemaCommand.class),
      @SubCommand(name = "UploadSegment", impl = UploadSegmentCommand.class),
      @SubCommand(name = "PostQuery", impl = PostQueryCommand.class),
      @SubCommand(name = "StopProcess", impl = StopProcessCommand.class),
      @SubCommand(name = "Quickstart", impl = Quickstart.class)
  })
  Command _subCommand;
  // @formatter:on

  @Option(name = "-help", required = false, help = true, aliases={"-h", "--h", "--help"}, usage="Print this message.")
  boolean _help = false;

  public void execute(String[] args) throws Exception {
    try {
      CmdLineParser parser = new CmdLineParser(this);
      parser.parseArgument(args);

      if ((_subCommand == null) || _help) {
        printUsage();
      } else if (_subCommand.getHelp()) {
        _subCommand.printUsage();
      } else {
        System.out.println("Executing command: " + _subCommand);
        _subCommand.execute();
      }

    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  public static void main(String[] args) throws Exception {
    new PinotAdministrator().execute(args);
  }

  public void printUsage() {
    System.out.println("Usage: pinot-admin.sh <subCommand>");
    System.out.println("Valid subCommands are:");

    Class<PinotAdministrator> obj = PinotAdministrator.class;

    for (Field f : obj.getDeclaredFields()) {
      if (f.isAnnotationPresent(SubCommands.class)) {
        SubCommands subCommands = f.getAnnotation(SubCommands.class);

        for (SubCommand subCommand : subCommands.value()) {
          Class<?> subCommandClass = subCommand.impl();
          Command command = null;

          try {
            command = (Command) subCommandClass.newInstance();
            System.out.println("\t" + subCommand.name() + "\t<" + command.description() + ">");
          } catch (Exception e) {
            System.out.println("Internal Error: Error instantiating class.");
          }
        }
      }
    }
  }
}

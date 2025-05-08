package edu.kis.powp.jobs2d.command;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kis.powp.jobs2d.Job2dDriver;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of ICompoundCommand for running multiple commands.
 */
public class ComplexCommand implements ICompoundCommand {
    private final List<DriverCommand> commandList;

    private ComplexCommand(List<DriverCommand> commandList) {
        this.commandList = commandList;
    }

    public ComplexCommand(){
        this.commandList = new ArrayList<>();
    }

    public void addCommand(DriverCommand command) {
        this.commandList.add(command);
    }

    public void addCommand(int index, DriverCommand command){
        this.commandList.add(index, command);
    }

    public void removeCommand(int index){
        this.commandList.remove(index);
    }

    public void removeLastCommand(){
        this.commandList.remove(this.commandList.size()-1);
    }

    public void setCommand(int index, DriverCommand command){
        this.commandList.set(index, command);
    }

    public void clear(){
        this.commandList.clear();
    }

    public boolean isEmpty(){
        return this.commandList.isEmpty();
    }

    public int size(){
        return this.commandList.size();
    }

    @Override
    public void execute(Job2dDriver driver){
        for (DriverCommand command : commandList) {
            command.execute(driver);
        }
    }

    @Override
    public Iterator<DriverCommand> iterator() {
        return commandList.iterator();
    }

    @Override
    public DriverCommand copy() {
        ComplexCommand newComplexCommand = new ComplexCommand();
        Iterator<DriverCommand> iterator = this.iterator();
        while (iterator.hasNext()) {
            newComplexCommand.addCommand(iterator.next().copy());
        }
        return newComplexCommand;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ComplexCommand second = (ComplexCommand) obj;

        return commandList.equals(second.commandList);
    }

    public static class Builder {
        private final List<DriverCommand> commands;

        public Builder() {
            this.commands = new ArrayList<>();
        }

        public ComplexCommand build() {
            return new ComplexCommand(commands);
        }

        public void loadCommandsFromJson(String filename) {
            this.commands.clear();
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(filename);
            try {
                List<JsonCommandEntry> commands = mapper.readValue(file, new TypeReference<List<JsonCommandEntry>>() {});

                for (JsonCommandEntry command : commands) {

                    String commandName = command.getCommandName();
                    int x = command.getX();
                    int y = command.getY();

                    DriverCommand newCommand;

                    switch(commandName) {
                        case "operateTo":
                            newCommand = new OperateToCommand(x, y);
                            break;
                        case "setPosition":
                            newCommand = new SetPositionCommand(x, y);
                            break;
                        default:
                            newCommand = null;
                    }

                    // TODO change so user knows that there was bad command name in JSON
                    if(newCommand == null){
                        continue;
                    }

                    this.addCommand(newCommand);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        public void addCommand(DriverCommand command) {
            commands.add(command);
        }

        public void addCommands(List<DriverCommand> commands) {
            this.commands.addAll(commands);
        }
    }
}

    package mars.mips.instructions.customlangs;
    import mars.simulator.*;
    import mars.mips.hardware.*;
    import mars.mips.instructions.syscalls.*;
    import mars.*;
    import mars.util.*;
    import java.util.*;
    import java.io.*;
    import mars.mips.instructions.*;
    import java.util.Random;


public class FootballAssembly extends CustomAssembly{
    @Override
    public String getName(){
        return "Football Assembly";
    }

    @Override
    public String getDescription(){
        return "Assembly language that simulates a football game";
    }

    @Override
    protected void populate(){

        // set (same as li)
        instructionList.add(
            new BasicInstruction("set $t1, -10",
            "Set temp quantity reg ($t1) to immediate amount (dist or score)",
            BasicInstructionFormat.I_FORMAT,
            "100000 fffff 00000 ssssssssssssssss",
            new SimulationCode()
            {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int[] operands = statement.getOperands();
                    int imm = operands[1] << 16 >> 16;
                    int rdVal = imm;
                    RegisterFile.updateRegister(operands[0], rdVal);
                }
        }));

        // catch (similar to add)
        instructionList.add(
            new BasicInstruction("catch $t0, $t1, $t2",
            "Adds the catch distance ($t1) and the yards after catch ($t2) to total yards ($t0)",
            BasicInstructionFormat.R_FORMAT,
            "000000 sssss ttttt fffff 00000 000001",
            new SimulationCode()
            {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int[] operands = statement.getOperands();
                    int rdVal = RegisterFile.getValue(operands[0]);
                    int rsVal = RegisterFile.getValue(operands[1]);
                    int rtVal = RegisterFile.getValue(operands[2]);
                    int result = rdVal + rsVal + rtVal;
                    int catchdist = rsVal + rtVal;

                    // overflow on A+B+C to detect when A,B,C have the same sign but the addition of the three are the opposite
                    if (((rdVal >= 0 && rsVal >= 0 && rtVal >= 0) && result < 0) ||
                        ((rdVal < 0 && rsVal < 0 && rtVal < 0) && result >= 0))
                    {
                        throw new ProcessingException(statement,
                            "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                    }
                    RegisterFile.updateRegister(operands[0], result);
                    SystemIO.printString("Catch for " + catchdist + " yards\n");
                }
        }));

        // run (similar to addi)
        instructionList.add(
            new BasicInstruction("run $t0, -10",
            "Add immediate yards to total yards ($t0)",
            BasicInstructionFormat.I_FORMAT,
            "001000 fffff 00000 ssssssssssssssss",
            new SimulationCode()
            {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int[] operands = statement.getOperands();
                    int rsVal = RegisterFile.getValue(operands[0]);
                    int imm = operands[1] << 16 >> 16;
                    int result = rsVal + imm;
                
                //overflow check of A+B
                if ((rsVal >= 0 && imm >= 0 && result < 0) ||
                    (rsVal < 0 && imm < 0 && result >= 0))
                {
                    throw new ProcessingException(statement,
                        "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                }
                    RegisterFile.updateRegister(operands[0], result); 
                    SystemIO.printString("Run for " + imm + " yards\n");
                }
        }));

        // ran (also similar to addi)
        instructionList.add(
            new BasicInstruction("ran $t0, $t1",
            "Add yards just ran ($t1) to total yards ($t0)",
            BasicInstructionFormat.R_FORMAT,
            "000000 fffff sssss 00000 00000 100011",
            new SimulationCode()
            {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int[] operands = statement.getOperands();
                    int rdVal = RegisterFile.getValue(operands[0]);
                    int rsVal = RegisterFile.getValue(operands[1]);
                    int result = rdVal + rsVal;
                    RegisterFile.updateRegister(operands[0], result);
                    SystemIO.printString("Total yards: " + result + " yards\n");
                }
        }));

        //tackle (similar to sub)
        instructionList.add(
            new BasicInstruction("tackle $t0, $t1, $t2",
            "Subtracts tackle yards ($t2) from ran yards($t1) added to total yards ($t0)",
            BasicInstructionFormat.R_FORMAT,
            "000000 sssss ttttt fffff 00000 000010",
            new SimulationCode()
            {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int[] operands = statement.getOperands();
                    int rdVal = RegisterFile.getValue(operands[0]);
                    int rsVal = RegisterFile.getValue(operands[1]);
                    int rtVal = RegisterFile.getValue(operands[2]);
                    int result = rdVal + rsVal - rtVal;

                    //overflow check of A-B
                    if ((rsVal >= 0 && rtVal < 0 && result < 0) ||
                        (rsVal < 0 && rtVal >= 0 && result >= 0))
                    {
                        throw new ProcessingException(statement,
                            "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                    }

                    RegisterFile.updateRegister(operands[0], result);
                    SystemIO.printString("Tackled after run for loss of " + rtVal + " yards\n");
                }
        }));
        
        // sprint (similar to mul)
        instructionList.add(
            new BasicInstruction("sprint $t1, $t2",
            "Apply a multiplier of ($t2) onto ran yards ($t1)",
            BasicInstructionFormat.R_FORMAT,
            "000000 fffff sssss 00000 00000 000011",
            new SimulationCode()
            {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int[] operands = statement.getOperands();
                    int rdVal = RegisterFile.getValue(operands[0]);
                    int rsVal = RegisterFile.getValue(operands[1]);
                    int result = rdVal * rsVal;
                    RegisterFile.updateRegister(operands[0], result);
                    SystemIO.printString("Sprinted for " + result + " yards\n");
                }
        }));

        // relax playing style (branch similar to bgt)
        instructionList.add(
            new BasicInstruction("relax $t3, $t4, label",
            "Branch if team score ($t3) is at least 14 greater than opponent ($t4)",
            BasicInstructionFormat.I_BRANCH_FORMAT,
            "001100 sssss ttttt ffffffffffffffff",
            new SimulationCode()
            {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int[] operands = statement.getOperands();

                    int rsVal = RegisterFile.getValue(operands[0]);
                    int rtVal = RegisterFile.getValue(operands[1]);
                    int labelAddress = operands[2];

                    SystemIO.printString("End of quarter...\n");

                    // Check if $t3 >= $t4 + 14
                    if (rsVal >= rtVal + 14)
                    {
                        Globals.instructionSet.processBranch(labelAddress);
                        SystemIO.printString("Less energy playing style activated!\n");
                    }
                }
        }));

        // stiffarm
        instructionList.add(
            new BasicInstruction("stiffarm $t1, -10",
            "Adds extra yards after contact (imm) to total yards ($t1)",
            BasicInstructionFormat.I_FORMAT,
            "001000 fffff 00000 ssssssssssssssss",
            new SimulationCode()
            {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int[] operands = statement.getOperands();
                    int rsVal = RegisterFile.getValue(operands[0]);
                    int imm = operands[1] << 16 >> 16;
                    int result = rsVal + imm;
                    RegisterFile.updateRegister(operands[0], result);
                    SystemIO.printString("Stiffarm for extra " + imm + " yards\n");
                }
        }));

        // ad break (printing)
        instructionList.add(
            new BasicInstruction("break label",
            "Print ad message stored at the label",
            BasicInstructionFormat.I_BRANCH_FORMAT,
            "001101 00000 00000 ffffffffffffffff",
            new SimulationCode()
            {
            public void simulate(ProgramStatement statement) throws ProcessingException
            {        
                char ch = 0;
                String label = statement.getOriginalTokenList().get(1).getValue();
                // Searching the label in the program symbol table to get its address
                int byteAddress = Globals.program.getLocalSymbolTable().getAddressLocalOrGlobal(label);
                try
                    {
                    ch = (char) Globals.memory.getByte(byteAddress);
                    // ensures it prints all possible characters
                    while (ch != 0)
                    {
                        SystemIO.printString(new Character(ch).toString());
                        byteAddress++;
                        ch = (char) Globals.memory.getByte(byteAddress);
                    }
                    } 
                    catch (AddressErrorException e)
                    {
                        throw new ProcessingException(statement, e);
                    }
                }                  
        }));
        
        // interception (similar to div)
        instructionList.add(
            new BasicInstruction("pick $t0",
            "Divides total yards ($t0) by 2",
            BasicInstructionFormat.R_FORMAT,
            "00000 fffff 00000 00000 00000 000100",
            new SimulationCode()
            {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int[] operands = statement.getOperands();
                    int rdVal = RegisterFile.getValue(operands[0]);
                    int result = rdVal / 2;
                    RegisterFile.updateRegister(operands[0], result);
                    SystemIO.printString("Intercepted!\nTotal yards: " + result + " yards\n");
                }
        }));

        // fumble (similar to sub)
        instructionList.add(
            new BasicInstruction("fumble $t0, -10",
            "Subtracts yards due to fumble (imm) from total yards ($t0)",
            BasicInstructionFormat.I_FORMAT,
            "001010 fffff 00000 ssssssssssssssss",
            new SimulationCode()
            {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int[] operands = statement.getOperands();
                    int rtVal = RegisterFile.getValue(operands[0]);
                    int imm = operands[1] << 16 >> 16;
                    int result = rtVal - imm;
                    RegisterFile.updateRegister(operands[0], result);
                    SystemIO.printString("Fumble!\nTotal yards: " + result + " yards\n");
                }
        }));

        // touchdown
        instructionList.add(
            new BasicInstruction("td $t3",
            "Adds 6 to total score of team ($t3)",
            BasicInstructionFormat.R_FORMAT,
            "000000 00000 00000 fffff 00000 000110",
            new SimulationCode()
            {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int[] operands = statement.getOperands();
                    int rtVal = RegisterFile.getValue(operands[0]);
                    int result = rtVal + 6;
                    RegisterFile.updateRegister(operands[0], result);
                    int oppScore = RegisterFile.getValue(12);
                    SystemIO.printString("Touchdown!\nScore: " + result + " -- " + oppScore + "\n");
                }
        }));

        // point after touchdown
        instructionList.add(
            new BasicInstruction("pat $t3",
            "Adds 1 to total score of team ($t3)",
            BasicInstructionFormat.R_FORMAT,
            "010100 fffff 00000 ssssssssssssssss",
            new SimulationCode()
            {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int[] operands = statement.getOperands();
                    int rtVal = RegisterFile.getValue(operands[0]);
                    int result = rtVal + 1;
                    RegisterFile.updateRegister(operands[0], result);
                    int oppScore = RegisterFile.getValue(12);
                    SystemIO.printString("Point after Touchdown!\nScore: " + result + " -- " + oppScore + "\n");
                }
        }));

        // safety
        instructionList.add(
            new BasicInstruction("safety $t3",
            "Adds 2 to team score ($t3)",
            BasicInstructionFormat.R_FORMAT,
            "000000 00000 00000 fffff 00000 001000",
            new SimulationCode()
            {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int[] operands = statement.getOperands();
                    int rtVal = RegisterFile.getValue(operands[0]);
                    int result = rtVal + 2;
                    RegisterFile.updateRegister(operands[0], result);
                    int oppScore = RegisterFile.getValue(12);
                    SystemIO.printString("Safety!\nScore: " + result + " -- " + oppScore + "\n");
                }
        }));

        // field goal
        instructionList.add(
            new BasicInstruction("fg $t3",
            "Adds 3 to total score of team ($t3)",
            BasicInstructionFormat.R_FORMAT,
            "000000 00000 00000 fffff 00000 001001",
            new SimulationCode()
            {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int[] operands = statement.getOperands();
                    int rtVal = RegisterFile.getValue(operands[0]);
                    int result = rtVal + 3;
                    RegisterFile.updateRegister(operands[0], result);
                    int oppScore = RegisterFile.getValue(12);
                    SystemIO.printString("Field goal!\nScore: " + result + " -- " + oppScore + "\n");
                }
        }));

        // hailmary attempt
        instructionList.add(
            new BasicInstruction("hailmary $t3",
            "Hailmary attempt: Randomly adds 0, 6, or -6 to team score ($t3)",
            BasicInstructionFormat.R_FORMAT,
            "000000 00000 00000 fffff 00000 001011",
            new SimulationCode() {
                public void simulate(ProgramStatement statement) throws ProcessingException {
                    int[] operands = statement.getOperands();
                    int rdVal = RegisterFile.getValue(operands[0]);
                    int oppScore = RegisterFile.getValue(12);
                    // random int number (0, 1, or 2)
                    int r = (int)(Math.floor(Math.random() * 3));
                    int modifier;
                    switch (r) {
                        case 0:
                            modifier = 6;
                            oppScore += modifier;
                            SystemIO.printString("HORRIBLE hailmary; returned for pick 6!\nScore: " + rdVal + " -- " + oppScore + "\n");
                            break;
                        case 1:
                            modifier = 0;
                            rdVal += modifier;
                            SystemIO.printString("FAILED hailmary attempt\n");
                            break;
                        case 2:
                            modifier = 6;
                            rdVal += modifier;
                            SystemIO.printString("SUCCESSFUL hailmary!\nScore: " + rdVal + " -- " + oppScore + "\n");
                            break;
                        default:
                            modifier = 0;
                            SystemIO.printString("Not working \n");
                    }
                    RegisterFile.updateRegister(operands[0], rdVal);
                    RegisterFile.updateRegister(12, oppScore);

                }
        }));

        // two-point conversion attempt
        instructionList.add(
            new BasicInstruction("twopt $t3",
            "Two-point conversion attempt: Randomly adds 0 or 2 to team score ($t3)",
            BasicInstructionFormat.R_FORMAT,
            "000000 00000 00000 fffff 00000 001010",
            new SimulationCode() {
                public void simulate(ProgramStatement statement) throws ProcessingException {

                    int[] operands = statement.getOperands();
                    int rdVal = RegisterFile.getValue(operands[0]);
                    int oppScore = RegisterFile.getValue(12);
                    // random int number (0 or 1)
                    int r = (int)(Math.floor(Math.random() * 2));
                    int modifier;
                    int result = rdVal;
                    switch (r) {
                        case 0:
                            modifier = 0;
                            result += modifier;
                            SystemIO.printString("FAILED two-point attempt\n");
                            break;

                        case 1:
                            modifier = 2;
                            result += modifier;
                            SystemIO.printString("SUCCESSFUL two-point conversion!\nScore: " + result + " -- " + oppScore + "\n");
                            break;
                        default:
                            modifier = 0;
                            SystemIO.printString("Not working \n");
                    }
                    RegisterFile.updateRegister(operands[0], result);
                }
        }));

        // flag on the play
        instructionList.add(
            new BasicInstruction("flag $t0, $t1",
            "Randomly adds - or + ($t1) flag yards to total yards ($t0)",
            BasicInstructionFormat.R_FORMAT,
            "000000 fffff sssss 00000 00000 000101",
            new SimulationCode() {
                public void simulate(ProgramStatement statement) throws ProcessingException {
                    int[] operands = statement.getOperands();
                    
                    int rtVal = RegisterFile.getValue(operands[0]);
                    int rsVal = RegisterFile.getValue(operands[1]);
                    Random rand = new Random();
                    // randomly choose sign (+ or -)
                    int sign = rand.nextBoolean() ? 1 : -1;
                    int modifier = sign * rsVal;
                    int result = rtVal + modifier;
                    RegisterFile.updateRegister(operands[0], result);

                    if(sign == 1) {
                        SystemIO.printString("Lucky flag! Added " + rsVal + " yards\nTotal yards: " + result + "\n");
                    } else {
                        SystemIO.printString("Penalty flag! Lost " + rsVal + " yards\nTotal yards: " + result + "\n");
                    }
                }
        }));

        // sack
        instructionList.add(
            new BasicInstruction("sack $t0, -10",
            "Subtracts yards due to sack (imm) from total yards ($t0)",
            BasicInstructionFormat.I_FORMAT,
            "001011 fffff 00000 ssssssssssssssss",
            new SimulationCode()
            {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int[] operands = statement.getOperands();
                    int rtVal = RegisterFile.getValue(operands[0]);
                    int imm = operands[1] << 16 >> 16;
                    int result = rtVal - imm;
                    RegisterFile.updateRegister(operands[0], result);
                    SystemIO.printString("Sacked.\nTotal yards: " + result + " yards\n");
                }
        }));

        //random celebration
        instructionList.add(
                new BasicInstruction("cele",
            	 "Prints random celebration noise and store random result in $v0",
                BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 00000 00000 001101", 
                new SimulationCode()
               {

                  public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     // random int number (0 - 3)
                    int r = (int)(Math.floor(Math.random() * 4));

                     RegisterFile.updateRegister(2, r);

                     // prints celebration received depending on random num
                     switch (RegisterFile.getValue(2)){ // 2 = register $v0 
                        case 0:
                           SystemIO.printString("LETSSS GO!\n");
                           break;
                        case 1:
                           SystemIO.printString("WOOOOHHH!\n");
                           break;
                        case 2:
                           SystemIO.printString("AYEEEEEE!\n");
                           break;
                        case 3:
                           SystemIO.printString("TOO EASYYY!\n");
                           break;
                        default:
                           SystemIO.printString("Doesn't work\n");
                    }
                }
            }));

        //random playcall
        instructionList.add(
                new BasicInstruction("shout",
            	 "Prints random playcall and store random result in $v0",
                BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 00000 00000 001110", 
                new SimulationCode()
               {

                  public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     // random int number (0 - 3)
                    int r = (int)(Math.floor(Math.random() * 4));

                     RegisterFile.updateRegister(2, r);

                     // prints playcall received depending on random num
                     switch (RegisterFile.getValue(2)){ // 2 = register $v0 
                        case 0:
                           SystemIO.printString("Blue 80\n");
                           break;
                        case 1:
                           SystemIO.printString("Green 48\n");
                           break;
                        case 2:
                           SystemIO.printString("Turbo\n");
                           break;
                        case 3:
                           SystemIO.printString("White 80\n");
                           break;
                        default:
                           SystemIO.printString("Doesn't work\n");
                    }
                }
            }));
        
        // forfeit
        instructionList.add(
            new BasicInstruction("forfeit $t3",
            "Sets team score ($t3) to 0 and opponent score ($t4) to 7",
            BasicInstructionFormat.R_FORMAT,
            "000000 00000 00000 fffff 00000 001100",
            new SimulationCode() {
                public void simulate(ProgramStatement statement) throws ProcessingException {
                    int[] operands = statement.getOperands();
                    RegisterFile.updateRegister(operands[0], 0);
                    RegisterFile.updateRegister(12, 7);
                    SystemIO.printString("Forfeit! Team score: 0, Opponent score: 7\n");
                }
        }));
    

    }   
}


/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.pdavm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 */
@SuppressWarnings("serial")
public class PDAVirtualMachine {

    static class Stack extends LinkedList<Object> {
        public Object pop() {
            return isEmpty() ? 0 : remove(size() - 1);
        }

        public void push(Object value) {
            add(value);
        }
    }
    
    static class Register {
        Register(String name) { 
            fName = name; 
        }
        String fName;
        String fGroup = "<no_group>";
        boolean fIsWriteable = true;
        Map<String, BitField> fBitFields = new LinkedHashMap<String, BitField>(0);
        int fValue;
    }

    static class BitField {
        BitField(String name) { 
            fName = name; 
        }
        String fName;
        int fBitOffset;
        int fBitCount;
        Map<String, Integer> fMnemonics = new LinkedHashMap<String, Integer>(0);
    }

    Map<String,Register> fRegisters = new LinkedHashMap<String,Register>(0);
    
    class Args {
        final String[] fArgs;

        int next = 0;

        Args(String[] args) {
            fArgs = args;
        }

        String getNextStringArg() {
            if (fArgs.length > next) {
                return fArgs[next++];
            }
            return "";
        }

        int getNextIntArg() {
            String arg = getNextStringArg();
            try {
                return Integer.parseInt(arg);
            } catch (NumberFormatException e) {
            }
            return 0;
        }

        boolean getNextBooleanArg() {
            String arg = getNextStringArg();
            try {
                return Boolean.parseBoolean(arg);
            } catch (NumberFormatException e) {
            }
            return false;
        }

        Object getNextIntOrStringArg() {
            String arg = getNextStringArg();
            try {
                return Integer.parseInt(arg);
            } catch (NumberFormatException e) {
            }
            return arg;
        }

        PDAThread getThreadArg() {
            int id = getNextIntArg();
            return fThreads.get(id);
        }
    }

    class PDAThread {
        final int fID;

        /** The push down automata data stack (the data stack). */
        final Stack fStack = new Stack();

        /**
         * PDAThread copy of the code. It can differ from the program if
         * performing an evaluation.
         */
        String[] fThreadCode;

        /** PDAThread copy of the labels. */
        Map<String, Integer> fThreadLabels;

        /** The stack of stack frames (the control stack) */
        final List<Frame> fFrames = new LinkedList<Frame>();

        /** Current stack frame (not includced in fFrames) */
        Frame fCurrentFrame;

        /**
         * The run flag is true if the thread is running. If the run flag is
         * false, the thread exits the next time the main instruction loop runs.
         */
        boolean fRun = true;

        String fSuspend = null;

        boolean fStep = false;

        boolean fStepReturn = false;
        
        int fSavedPC;

        boolean fPerformingEval = false;
        
        PDAThread(int id, String function, int pc) {
            fID = id;
            fCurrentFrame = new Frame(function, pc);
            fThreadCode = fCode;
            fThreadLabels = fLabels;
        }
    }

    final Map<Integer, PDAThread> fThreads = new LinkedHashMap<Integer, PDAThread>();

    int fNextThreadId = 1;

    boolean fStarted = true;
    /**
     * The code is stored as an array of strings, each line of the source file
     * being one entry in the array.
     */
    final String[] fCode;

    /** A mapping of labels to indicies in the code array */
    final Map<String, Integer> fLabels;

    /** Each stack frame is a mapping of variable names to values. */
    class Frame {
        final Map<String, Object> fLocalVariables = new LinkedHashMap<String, Object>();
        
        /**
         * The name of the function in this frame
         */
        final String fFunction;

        /**
         * The current program counter in the frame the pc points to the next
         * instruction to be executed
         */
        int fPC;

        Frame(String function, int pc) {
            fFunction = function;
            fPC = pc;
        }
        
        void set(String name, Object value) {
            if (name.startsWith("$")) {
                setRegisterValue(name, value);
            } else {
                fLocalVariables.put(name, value);
            }
        }
        
        Object get(String name) {
            if (name.startsWith("$")) {
                return getRegisterValue(name);
            } else { 
                return fLocalVariables.get(name);
            }
        }
    }

    void setRegisterValue(String name, Object value) {
        Register reg = fRegisters.get(getRegisterPartOfName(name));
        if (reg == null) return;
        String bitFieldName = getBitFieldPartOfName(name);
        if (bitFieldName != null) {
            BitField bitField = reg.fBitFields.get(bitFieldName);
            if (bitField == null) return;
            Integer intValue = null;
            if (value instanceof Integer) {
                intValue = (Integer)value;
            } else if (value instanceof String) {
                intValue = bitField.fMnemonics.get(value);
            }
            if (intValue != null) {
                int bitFieldMask = 2^(bitField.fBitCount - 1);           
                int registerMask = ~(bitFieldMask << bitField.fBitOffset);
                int bitFieldValue = intValue & bitFieldMask;
                reg.fValue = (reg.fValue & registerMask) | (bitFieldValue << bitField.fBitOffset);
            }
        } else if (value instanceof Integer) {
            reg.fValue = ((Integer)value).intValue();
        }
    }

    Object getRegisterValue(String name) {
        Register reg = fRegisters.get(getRegisterPartOfName(name));
        if (reg == null) return null;
        String bitFieldName = getBitFieldPartOfName(name);
        if (bitFieldName != null) {
            BitField bitField = reg.fBitFields.get(bitFieldName);
            if (bitField == null) return null;
            int bitFieldMask = 2^(bitField.fBitCount - 1);           
            int registerMask = bitFieldMask << bitField.fBitOffset;
            return (reg.fValue & registerMask) >> bitField.fBitOffset;
        } else {
            return reg.fValue;
        }
    }
    
    /**
     * Breakpoints are stored per each each line of code.  The boolean indicates
     * whether the whole VM should suspend or just the triggering thread.
     */
    final Map<Integer, Boolean> fBreakpoints = new HashMap<Integer, Boolean>();

    /**
     * The suspend flag is true if the VM should suspend running the program and
     * just listen for debug commands.
     */
    String fSuspendVM;

    /** Flag indicating whether the debugger is performing a step. */
    boolean fStepVM = false;

    /** Flag indicating whether the debugger is performing a step return */
    boolean fStepReturnVM = false;
    
    int fSteppingThread = 0;

    /** Name of the pda program being debugged */
    final String fFilename;

    /** The command line argument to start a debug session. */
    final boolean fDebug;

    /** The port to listen for debug commands on */
    final int fCommandPort;

    /**
     * Command socket for receiving debug commands and sending command responses
     */
    Socket fCommandSocket;

    /** Command socket reader */
    BufferedReader fCommandReceiveStream;

    /** Command socket write stream. */
    OutputStream fCommandResponseStream;

    /** The port to send debug events to */
    final int fEventPort;

    /** Event socket */
    Socket fEventSocket;

    /** Event socket and write stream. */
    OutputStream fEventStream;

    /** The eventstops table holds which events cause suspends and which do not. */
    final Map<String, Boolean> fEventStops = new HashMap<String, Boolean>();
    {
        fEventStops.put("unimpinstr", false);
        fEventStops.put("nosuchlabel", false);
    }

    /**
     * The watchpoints table holds watchpoint information.
     * <p/>
     * variablename_stackframedepth => N
     * <ul> 
     * <li>N = 0 is no watch</li> 
     * <li>N = 1 is read watch</li>
     * <li>N = 2 is write watch</li>
     * <li>N = 3 is both, etc.</li>
     */
    final Map<String, Integer> fWatchpoints = new HashMap<String, Integer>();

    public static void main(String[] args) {
        String programFile = args.length >= 1 ? args[0] : null;
        if (programFile == null) {
            System.err.println("Error: No program specified");
            return;
        }

        String debugFlag = args.length >= 2 ? args[1] : "";
        boolean debug = "-debug".equals(debugFlag);
        int commandPort = 0;
        int eventPort = 0;

        if (debug) {
            String commandPortStr = args.length >= 3 ? args[2] : "";
            try {
                commandPort = Integer.parseInt(commandPortStr);
            } catch (NumberFormatException e) {
                System.err.println("Error: Invalid command port");
                return;
            }

            String eventPortStr = args.length >= 4 ? args[3] : "";
            try {
                eventPort = Integer.parseInt(eventPortStr);
            } catch (NumberFormatException e) {
                System.err.println("Error: Invalid event port");
                return;
            }
        }

        PDAVirtualMachine pdaVM = null;
        try {
            pdaVM = new PDAVirtualMachine(programFile, debug, commandPort, eventPort);
            pdaVM.startDebugger();
        } catch (IOException e) {
            System.err.println("Error: " + e.toString());
            return;
        }
        pdaVM.run();
    }

    PDAVirtualMachine(String inputFile, boolean debug, int commandPort, int eventPort) throws IOException {
        fFilename = inputFile;

        // Load all the code into memory
        FileReader fileReader = new FileReader(inputFile);
        StringWriter stringWriter = new StringWriter();
        List<String> code = new LinkedList<String>();
        int c = fileReader.read();
        while (c != -1) {
            if (c == '\n') {
                code.add(stringWriter.toString().trim());
                stringWriter = new StringWriter();
            } else {
                stringWriter.write(c);
            }
            c = fileReader.read();
        }
        code.add(stringWriter.toString().trim());
        fCode = code.toArray(new String[code.size()]);

        fLabels = mapLabels(fCode);

        fDebug = debug;
        fCommandPort = commandPort;
        fEventPort = eventPort;
    }

    /**
     * Initializes the labels map
     */
    Map<String, Integer> mapLabels(String[] code) {
        Map<String, Integer> labels = new HashMap<String, Integer>();
        for (int i = 0; i < code.length; i++) {
            if (code[i].length() != 0 && code[i].charAt(0) == ':') {
                labels.put(code[i].substring(1), i);
            }
        }
        return labels;
    }

    void sendCommandResponse(String response) {
        try {
            fCommandResponseStream.write(response.getBytes());
            fCommandResponseStream.flush();
        } catch (IOException e) {
        }
    }

    void sendDebugEvent(String event, boolean error) {
        if (fDebug) {
            try {
                fEventStream.write(event.getBytes());
                fEventStream.write('\n');
                fEventStream.flush();
            } catch (IOException e) {
                System.err.println("Error: " + e);
                System.exit(1);
            }
        } else if (error) {
            System.err.println("Error: " + event);
        }
    }

    void startDebugger() throws IOException {
        if (fDebug) {
            System.out.println("-debug " + fCommandPort + " " + fEventPort);
        }

        ServerSocket commandServerSocket = new ServerSocket(fCommandPort);
        fCommandSocket = commandServerSocket.accept();
        fCommandReceiveStream = new BufferedReader(new InputStreamReader(fCommandSocket.getInputStream()));
        fCommandResponseStream = new PrintStream(fCommandSocket.getOutputStream());
        commandServerSocket.close();

        ServerSocket eventServerSocket = new ServerSocket(fEventPort);
        fEventSocket = eventServerSocket.accept();
        fEventStream = new PrintStream(fEventSocket.getOutputStream());
        eventServerSocket.close();

        System.out.println("debug connection accepted");

        fSuspendVM = "client";
    }

    void run() {
        int id = fNextThreadId++;
        fThreads.put(id, new PDAThread(id, "main", 0));
        if (fDebug) {
            sendDebugEvent("started " + id, false);
        }

        boolean allThreadsSuspended = false;
        while (!fThreads.isEmpty()) {
            checkForBreakpoint();
            
            if (fSuspendVM != null) {
                debugUI();
            } else {
                yieldToDebug(allThreadsSuspended);
                if (fSuspendVM != null) {
                    // Received a command to suspend VM, skip executing threads.
                    continue;
                }
            }

            PDAThread[] threadsCopy = fThreads.values().toArray(new PDAThread[fThreads.size()]);
            allThreadsSuspended = true;
            for (PDAThread thread : threadsCopy) {
                if (thread.fSuspend == null) {
                    allThreadsSuspended = false;
                    
                    String instruction = thread.fThreadCode[thread.fCurrentFrame.fPC];
                    thread.fCurrentFrame.fPC++;
                    doOneInstruction(thread, instruction);
                    if (thread.fCurrentFrame.fPC >= thread.fThreadCode.length) {
                        // Thread reached end of code, exit from the thread.
                        thread.fRun = false;
                    } else if (thread.fStepReturn) {
                        // If this thread is in a step-return operation, check
                        // if we've returned from a call.  
                        instruction = thread.fThreadCode[thread.fCurrentFrame.fPC];
                        if ("return".equals(instruction)) {
                            // Note: this will only be triggered if the current 
                            // thread also has the fStepReturn flag set.
                            if (fStepReturnVM) {
                                fSuspendVM = thread.fID + " step";
                            } else {
                                thread.fSuspend = "step";
                            }
                        }
                    }
                    if (!thread.fRun) {
                        sendDebugEvent("exited " + thread.fID, false);
                        fThreads.remove(thread.fID);
                    } else if (thread.fSuspend != null) {
                        sendDebugEvent("suspended " + thread.fID + " " + thread.fSuspend, false);
                        thread.fStep = thread.fStepReturn = thread.fPerformingEval = false;
                    }
                } 
            }
            
            // Force thread context switch to avoid starving out other
            // processes in the system.
            Thread.yield();
        }
        
        sendDebugEvent("terminated", false);
        if (fDebug) {
            try {
                fCommandReceiveStream.close();
                fCommandResponseStream.close();
                fCommandSocket.close();
                fEventStream.close();
                fEventSocket.close();
            } catch (IOException e) {
                System.out.println("Error: " + e);
            }
        }

    }

    void doOneInstruction(PDAThread thread, String instr) {
        StringTokenizer tokenizer = new StringTokenizer(instr);
        String op = tokenizer.nextToken();
        List<String> tokens = new LinkedList<String>();
        while (tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken());
        }
        Args args = new Args(tokens.toArray(new String[tokens.size()]));

        boolean opValid = true;
        if (op.equals("add")) iAdd(thread, args);
        else if (op.equals("branch_not_zero")) iBranchNotZero(thread, args);
        else if (op.equals("call")) iCall(thread, args);
        else if (op.equals("dec")) iDec(thread, args);
        else if (op.equals("def")) iDef(thread, args);
        else if (op.equals("dup")) iDup(thread, args);
        else if (op.equals("exec")) iExec(thread, args);            
        else if (op.equals("halt")) iHalt(thread, args);
        else if (op.equals("output")) iOutput(thread, args);
        else if (op.equals("pop")) iPop(thread, args);
        else if (op.equals("push")) iPush(thread, args);
        else if (op.equals("return")) iReturn(thread, args);
        else if (op.equals("var")) iVar(thread, args);
        else if (op.equals("xyzzy")) iInternalEndEval(thread, args);
        else if (op.startsWith(":")) {} // label
        else if (op.startsWith("#")) {} // comment
        else {
            opValid = false;
        }

        if (!opValid) {
            sendDebugEvent("unimplemented instruction " + op, true);
            if (fEventStops.get("unimpinstr")) {
                fSuspendVM = thread.fID + " event unimpinstr";
                thread.fCurrentFrame.fPC--;
            }
        } else if (thread.fStep) {
            if (fStepVM) {
                fSuspendVM = thread.fID + " step";
                fStepVM = false;
            } else {
                thread.fSuspend = "step";
            }
            thread.fStep = false;
        }
    }

    void checkForBreakpoint() {
        if (fDebug) {
            for (PDAThread thread : fThreads.values()) {
                int pc = thread.fCurrentFrame.fPC;
                // Suspend for breakpoint if:
                // - the VM is not yet set to suspend, for e.g. as a result of step end,
                // - the thread is not yet suspended and is not performing an evaluation
                // - the breakpoints table contains a breakpoint for the given line.
                if (fSuspendVM == null && 
                    thread.fSuspend == null && !thread.fPerformingEval && 
                    fBreakpoints.containsKey(pc)) 
                {
                    if (fBreakpoints.get(pc)) {
                        fSuspendVM = thread.fID + " breakpoint " + pc;
                    } else {
                        thread.fSuspend = "breakpoint " + pc;
                        thread.fStep = thread.fStepReturn = false;
                        sendDebugEvent("suspended " + thread.fID + " " + thread.fSuspend, false);
                    }
                }
            }
        }
    }

    /**
     * After each instruction, we check the debug command channel for control input. If
     * there are commands, process them.
     */
    void yieldToDebug(boolean allThreadsSuspended) {
        if (fDebug) {
            String line = "";
            try {
                if (allThreadsSuspended || fCommandReceiveStream.ready()) {
                    line = fCommandReceiveStream.readLine();
                    processDebugCommand(line);
                }
            } catch (IOException e) {
                System.err.println("Error: " + e);
                System.exit(1);
            }
        }
    }

    /**
     *  Service the debugger commands while the VM is suspended
     */
    void debugUI() {
        if (!fStarted) {
            sendDebugEvent("vmsuspended " + fSuspendVM, false);
        } else {
            fStarted = false;
        }

        // Clear all stepping flags.  In case the VM suspended while
        // a step operation was being performed for the VM or some thread.
        fStepVM = fStepReturnVM = false;
        for (PDAThread thread : fThreads.values()) {
            thread.fSuspend = null;
            thread.fStep = thread.fStepReturn = thread.fPerformingEval = false;
        }
        
        while (fSuspendVM != null) {
            String line = "";
            try {
                line = fCommandReceiveStream.readLine();
            } catch (IOException e) {
                System.err.println("Error: " + e);
                System.exit(1);
                return;
            }
            processDebugCommand(line);
        }

        if (fStepVM || fStepReturnVM) {
            sendDebugEvent("vmresumed step", false);
        } else {
            sendDebugEvent("vmresumed client", false);
        }
    }

    void processDebugCommand(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line.trim());
        if (line.length() == 0) {
            return;
        }

        String command = tokenizer.nextToken();
        List<String> tokens = new LinkedList<String>();
        while (tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken());
        }
        Args args = new Args(tokens.toArray(new String[tokens.size()]));

        if ("children".equals(command)) debugChildren(args);
        else if ("clear".equals(command)) debugClearBreakpoint(args);
        else if ("data".equals(command)) debugData(args);
        else if ("drop".equals(command)) debugDropFrame(args);
        else if ("eval".equals(command)) debugEval(args);
        else if ("eventstop".equals(command)) debugEventStop(args);
        else if ("exit".equals(command)) debugExit();
        else if ("frame".equals(command)) debugFrame(args);
        else if ("groups".equals(command)) debugGroups(args);
        else if ("popdata".equals(command)) debugPop(args);
        else if ("pushdata".equals(command)) debugPush(args);
        else if ("registers".equals(command)) debugRegisters(args);
        else if ("resume".equals(command)) debugResume(args);
        else if ("set".equals(command)) debugSetBreakpoint(args);
        else if ("setdata".equals(command)) debugSetData(args);
        else if ("setvar".equals(command)) debugSetVariable(args);
        else if ("stack".equals(command)) debugStack(args);
        else if ("stackdepth".equals(command)) debugStackDepth(args);
        else if ("state".equals(command)) debugState(args);
        else if ("step".equals(command)) debugStep(args);
        else if ("stepreturn".equals(command)) debugStepReturn(args);
        else if ("suspend".equals(command)) debugSuspend(args);
        else if ("threads".equals(command)) debugThreads();
        else if ("var".equals(command)) debugVar(args);
        else if ("vmresume".equals(command)) debugVMResume();
        else if ("vmsuspend".equals(command)) debugVMSuspend();
        else if ("watch".equals(command)) debugWatch(args);
        else {
            sendCommandResponse("error: invalid command\n");
        }
    }

    void debugChildren(Args args) {
        PDAThread thread = args.getThreadArg();
        if (thread == null) {
            sendCommandResponse("error: invalid thread\n");
            return;
        }

        int sfnumber = args.getNextIntArg();
        String var = args.getNextStringArg();
        
        Frame frame = sfnumber >= thread.fFrames.size()    
            ? thread.fCurrentFrame : thread.fFrames.get(sfnumber);

        String varDot = var + ".";
        List<String> children = new ArrayList<String>();
        for (String localVar : frame.fLocalVariables.keySet()) {
            if (localVar.startsWith(varDot) && localVar.indexOf('.', varDot.length() + 1) == -1) {
                children.add(localVar);
            }
        }

        StringBuffer result = new StringBuffer();
        for (String child : children) {
            result.append(child);
            result.append('|');
        }
        result.append('\n');

        sendCommandResponse(result.toString());
    }
    
    void debugClearBreakpoint(Args args) {
        int line = args.getNextIntArg();

        fBreakpoints.remove(line);
        sendCommandResponse("ok\n");
    }

    private static Pattern fPackPattern = Pattern.compile("%([a-fA-F0-9][a-fA-F0-9])");

    void debugData(Args args) {
        PDAThread thread = args.getThreadArg();
        if (thread == null) {
            sendCommandResponse("error: invalid thread\n");
            return;
        }

        StringBuffer result = new StringBuffer();
        for (Object val : thread.fStack) {
            result.append(val);
            result.append('|');
        }
        result.append('\n');
        sendCommandResponse(result.toString());
    }

    void debugDropFrame(Args args) {
        PDAThread thread = args.getThreadArg();
        if (thread == null) {
            sendCommandResponse("error: invalid thread\n");
            return;
        }

        if (!thread.fFrames.isEmpty()) {
            thread.fCurrentFrame = thread.fFrames.remove(thread.fFrames.size() - 1);
        }
        thread.fCurrentFrame.fPC--;
        sendCommandResponse("ok\n");
        if (fSuspendVM != null) {
            sendDebugEvent("vmresumed drop", false);
            sendDebugEvent("vmsuspended " + thread.fID + " drop", false);
        } else {
            sendDebugEvent("resumed " + thread.fID + " drop", false);
            sendDebugEvent("suspended " + thread.fID + " drop", false);
        }
    }

    void debugEval(Args args) {
        if (fSuspendVM != null) {
            sendCommandResponse("error: cannot evaluate while vm is suspended\n");        
            return;
        }
        
        PDAThread thread = args.getThreadArg();
        if (thread == null) {
            sendCommandResponse("error: invalid thread\n");
            return;
        }
        
        if (thread.fSuspend == null) {
            sendCommandResponse("error: thread running\n");
            return;
        }
        
        StringTokenizer tokenizer = new StringTokenizer(args.getNextStringArg(), "|");
        tokenizer.countTokens();

        int numEvalLines = tokenizer.countTokens();
        thread.fThreadCode = new String[fCode.length + numEvalLines + 1];
        System.arraycopy(fCode, 0, thread.fThreadCode, 0, fCode.length);
        for (int i = 0; i < numEvalLines; i++) {
            String line = tokenizer.nextToken();
            StringBuffer lineBuf = new StringBuffer(line.length());
            Matcher matcher = fPackPattern.matcher(line);
            int lastMatchEnd = 0;
            while (matcher.find()) {
                lineBuf.append(line.substring(lastMatchEnd, matcher.start()));
                String charCode = line.substring(matcher.start() + 1, matcher.start() + 3);
                try {
                    lineBuf.append((char) Integer.parseInt(charCode, 16));
                } catch (NumberFormatException e) {
                }
                lastMatchEnd = matcher.end();
            }
            if (lastMatchEnd < line.length()) {
                lineBuf.append(line.substring(lastMatchEnd));
            }
            thread.fThreadCode[fCode.length + i] = lineBuf.toString();
        }
        thread.fThreadCode[fCode.length + numEvalLines] = "xyzzy";
        thread.fThreadLabels = mapLabels(fCode);

        thread.fSavedPC = thread.fCurrentFrame.fPC;
        thread.fCurrentFrame.fPC = fCode.length;
        thread.fPerformingEval = true;
        
        thread.fSuspend = null;
        
        sendCommandResponse("ok\n");

        sendDebugEvent("resumed " + thread.fID + " eval", false);
    }

    void debugEventStop(Args args) {
        String event = args.getNextStringArg();
        int stop = args.getNextIntArg();
        fEventStops.put(event, stop > 0);
        sendCommandResponse("ok\n");
    }

    void debugExit() {
        sendCommandResponse("ok\n");
        sendDebugEvent("terminated", false);
        System.exit(0);
    }

    void debugFrame(Args args) {
        PDAThread thread = args.getThreadArg();
        if (thread == null) {
            sendCommandResponse("error: invalid thread\n");
            return;
        }

        int sfnumber = args.getNextIntArg();
        Frame frame = null;
        if (sfnumber >= thread.fFrames.size()) {
            frame = thread.fCurrentFrame;
        } else {
            frame = thread.fFrames.get(sfnumber);
        }
        sendCommandResponse(printFrame(frame) + "\n");
    }

    void debugGroups(Args args) {
        TreeSet<String> groups = new TreeSet<String>();
        for (Register reg : fRegisters.values()) {
            groups.add(reg.fGroup);
        }
        StringBuffer response = new StringBuffer();
        for (String group : groups) {
            response.append(group);
            response.append('|');
        }
        response.append('\n');
        sendCommandResponse(response.toString());
    }

    void debugPop(Args args) {
        PDAThread thread = args.getThreadArg();
        if (thread == null) {
            sendCommandResponse("error: invalid thread\n");
            return;
        }

        thread.fStack.pop();
        sendCommandResponse("ok\n");
    }

    void debugPush(Args args) {
        PDAThread thread = args.getThreadArg();
        if (thread == null) {
            sendCommandResponse("error: invalid thread\n");
            return;
        }
        
        Object val = args.getNextIntOrStringArg();
        thread.fStack.push(val);
        sendCommandResponse("ok\n");
    }

    void debugRegisters(Args args) {
        String group = args.getNextStringArg();
        
        StringBuffer response = new StringBuffer();
        for (Register reg : fRegisters.values()) {
            if (group.equals(reg.fGroup)) {
                response.append(reg.fName);
                response.append(' ');
                response.append(reg.fIsWriteable);
                for (BitField bitField : reg.fBitFields.values()) {
                    response.append('|');
                    response.append(bitField.fName);
                    response.append(' ');
                    response.append(bitField.fBitOffset);
                    response.append(' ');
                    response.append(bitField.fBitCount);
                    response.append(' ');
                    for (Map.Entry<String, Integer> mnemonicEntry : bitField.fMnemonics.entrySet()) {
                        response.append(mnemonicEntry.getKey());
                        response.append(' ');
                        response.append(mnemonicEntry.getValue());
                        response.append(' ');
                    }
                }
                
                response.append('#');
            }
        }
        response.append('\n');
        sendCommandResponse(response.toString());
    }
    
    void debugResume(Args args) {
        PDAThread thread = args.getThreadArg();
        if (thread == null) {
            sendCommandResponse("error: invalid thread\n");
            return;
        } 
        if (fSuspendVM != null) {
            sendCommandResponse("error: cannot resume thread when vm is suspended\n");
            return;
        } 
        if (thread.fSuspend == null) {
            sendCommandResponse("error: thread already running\n");
            return;
        } 
        
        thread.fSuspend = null;
        sendDebugEvent("resumed " + thread.fID + " client", false);
        
        sendCommandResponse("ok\n");
    }

    void debugSetBreakpoint(Args args) {
        int line = args.getNextIntArg();
        int stopVM = args.getNextIntArg();
        
        fBreakpoints.put(line, stopVM != 0);
        sendCommandResponse("ok\n");
    }

    void debugSetData(Args args) {
        PDAThread thread = args.getThreadArg();
        if (thread == null) {
            sendCommandResponse("error: invalid thread\n");
            return;
        }
        
        int offset = args.getNextIntArg();
        Object val = args.getNextIntOrStringArg();

        if (offset < thread.fStack.size()) {
            thread.fStack.set(offset, val);
        } else {
            thread.fStack.add(0, val);
        }
        sendCommandResponse("ok\n");
    }

    void debugSetVariable(Args args) {
        PDAThread thread = args.getThreadArg();
        if (thread == null) {
            sendCommandResponse("error: invalid thread\n");
            return;
        }

        int sfnumber = args.getNextIntArg();
        String var = args.getNextStringArg();
        Object val = args.getNextIntOrStringArg();

        if (sfnumber >= thread.fFrames.size()) {
            thread.fCurrentFrame.set(var, val);
        } else {
            thread.fFrames.get(sfnumber).set(var, val);
        }
        sendCommandResponse("ok\n");
    }

    void debugStack(Args args) {
        PDAThread thread = args.getThreadArg();
        if (thread == null) {
            sendCommandResponse("error: invalid thread\n");
            return;
        }

        StringBuffer result = new StringBuffer();
        for (Frame frame : thread.fFrames) {
            result.append(printFrame(frame));
            result.append('#');
        }
        result.append(printFrame(thread.fCurrentFrame));
        result.append('\n');
        sendCommandResponse(result.toString());
    }

    void debugStackDepth(Args args) {
        PDAThread thread = args.getThreadArg();
        if (thread == null) {
            sendCommandResponse("error: invalid thread\n");
            return;
        }
        sendCommandResponse( Integer.toString(thread.fFrames.size() + 1) + "\n" );
    }


    /**
     * The stack frame output is: frame # frame # frame ... where each frame is:
     * filename | line number | function name | var | var | var | var ...
     */
    private String printFrame(Frame frame) {
        StringBuffer buf = new StringBuffer();
        buf.append(fFilename);
        buf.append('|');
        buf.append(frame.fPC);
        buf.append('|');
        buf.append(frame.fFunction);
        for (String var : frame.fLocalVariables.keySet()) {
            if (var.indexOf('.') == -1) {
                buf.append('|');
                buf.append(var);
            }
        }
        return buf.toString();
    }

    void debugState(Args args) {
        PDAThread thread = args.getThreadArg();
        String response = null;
        if (thread == null) {
            response = fSuspendVM == null ? "running" : fSuspendVM;
        } else if (fSuspendVM != null) {
            response = "vm";
        } else {
            response = thread.fSuspend == null ? "running" : thread.fSuspend;
        }
        sendCommandResponse(response + "\n");
    }
    
    void debugStep(Args args) {
        PDAThread thread = args.getThreadArg();
        if (thread == null) {
            sendCommandResponse("error: invalid thread\n");
            return;
        } 

        // Set suspend to null to allow the debug loop to exit back to the 
        // instruction loop and thus run an instruction. However, we want to 
        // come back to the debug loop right away, so the step flag is set to 
        // true which will cause the suspend flag to get set to true when we 
        // get to the next instruction.
        if (fSuspendVM != null) {
            // All threads are suspended, so suspend all threads again when 
            // step completes.
            fSuspendVM = null;
            fStepVM = true;
            // Also mark the thread that initiated the step to mark it as
            // the triggering thread when suspending.
            thread.fStep = true;
        } else {
            if (thread.fSuspend == null) {
                sendCommandResponse("error: thread already running\n");
                return;
            }
            thread.fSuspend = null;
            thread.fStep = true;
            sendDebugEvent("resumed " + thread.fID + " step", false);
        }
        sendCommandResponse("ok\n");
    }

    void debugStepReturn(Args args) {
        PDAThread thread = args.getThreadArg();
        if (thread == null) {
            sendCommandResponse("error: invalid thread\n");
            return;
        } 
        
        if (fSuspendVM != null) {
            fSuspendVM = null;
            fStepReturnVM = true;
            thread.fStepReturn = true;
        } else {
            if (thread.fSuspend == null) {
                sendCommandResponse("error: thread running\n");
                return;
            }
            thread.fSuspend = null;
            thread.fStepReturn = true;
            sendDebugEvent("resumed " + thread.fID + " step", false);
        }
        sendCommandResponse("ok\n");
    }

    void debugSuspend(Args args) {
        PDAThread thread = args.getThreadArg();
        if (thread == null) {
            sendCommandResponse("error: invalid thread\n");
            return;
        } 
        if (fSuspendVM != null) {
            sendCommandResponse("error: vm already suspended\n");
            return;
        }
        if (thread.fSuspend != null) {
            sendCommandResponse("error: thread already suspended\n");
            return;
        } 
        
        thread.fSuspend = "client";
        sendDebugEvent("suspended " + thread.fID + " client", false);
        sendCommandResponse("ok\n");
    }

    void debugThreads() {
        StringBuffer response = new StringBuffer();
        for (int threadId : fThreads.keySet()) {
            response.append(threadId);
            response.append(' ');
        }
        sendCommandResponse(response.toString().trim() + "\n");
    }

    void debugVar(Args args) {
        PDAThread thread = args.getThreadArg();
        if (thread == null) {
            sendCommandResponse("error: invalid thread\n");
            return;
        }

        int sfnumber = args.getNextIntArg();
        String var = args.getNextStringArg();

        Frame frame = sfnumber >= thread.fFrames.size()    
            ? thread.fCurrentFrame : thread.fFrames.get(sfnumber);
        
        Object val = frame.get(var);
        if (val == null) {
            sendCommandResponse("error: variable undefined\n");
        } else {
            sendCommandResponse(val.toString() + "\n");
        }
    }

    void debugVMResume() {
        if (fSuspendVM == null) {
            sendCommandResponse("error: vm already running\n");
            return;
        } 

        fSuspendVM = null;
        sendCommandResponse("ok\n");
    }

    void debugVMSuspend() {
        if (fSuspendVM != null) {
            sendCommandResponse("error: vm already suspended\n");
            return;
        }

        fSuspendVM = "client";
        sendCommandResponse("ok\n");
    }

    void debugWatch(Args args) {
        String funcAndVar = args.getNextStringArg();
        int flags = args.getNextIntArg();
        fWatchpoints.put(funcAndVar, flags);
        sendCommandResponse("ok\n");
    }

    void iAdd(PDAThread thread, Args args) {
        Object val1 = thread.fStack.pop();
        Object val2 = thread.fStack.pop();
        if (val1 instanceof Integer && val2 instanceof Integer) {
            int intVal1 = ((Integer) val1).intValue();
            int intVal2 = ((Integer) val2).intValue();
            thread.fStack.push(intVal1 + intVal2);
        } else {
            thread.fStack.push(-1);
        }
    }

    void iBranchNotZero(PDAThread thread, Args args) {
        Object val = thread.fStack.pop();
        if (val instanceof Integer && ((Integer) val).intValue() != 0) {
            String label = args.getNextStringArg();
            if (thread.fThreadLabels.containsKey(label)) {
                thread.fCurrentFrame.fPC = thread.fThreadLabels.get(label);
            } else {
                sendDebugEvent("no such label " + label, true);
                if (fEventStops.get("nosuchlabel")) {
                    fSuspendVM = thread.fID + " event nosuchlabel";
                    thread.fStack.push(val);
                    thread.fCurrentFrame.fPC--;
                }
            }
        }
    }

    void iCall(PDAThread thread, Args args) {
        String label = args.getNextStringArg();
        if (thread.fThreadLabels.containsKey(label)) {
            thread.fFrames.add(thread.fCurrentFrame);
            thread.fCurrentFrame = new Frame(label, thread.fThreadLabels.get(label));
        } else {
            sendDebugEvent("no such label " + label, true);
            if (fEventStops.get("nosuchlabel")) {
                fSuspendVM = thread.fID + " event nosuchlabel";
                thread.fCurrentFrame.fPC--;
            }
        }
    }

    void iDec(PDAThread thread, Args args) {
        Object val = thread.fStack.pop();
        if (val instanceof Integer) {
            val = new Integer(((Integer) val).intValue() - 1);
        }
        thread.fStack.push(val);
    }

    void iDef(PDAThread thread, Args args) {
        String type = args.getNextStringArg();

        String name = args.getNextStringArg();
        String regName = getRegisterPartOfName(name);
        String bitFieldName = getBitFieldPartOfName(name);

        if ("register".equals(type)) {
            Register reg = new Register(regName); 
            reg.fGroup = args.getNextStringArg();
            fRegisters.put(regName, reg);
            reg.fIsWriteable = args.getNextBooleanArg();
        } else if ("bitfield".equals(type)) {
            Register reg = fRegisters.get(regName);
            if (reg == null) return;
            BitField bitField = new BitField(bitFieldName);
            bitField.fBitOffset = args.getNextIntArg();
            bitField.fBitCount = args.getNextIntArg();
            reg.fBitFields.put(bitFieldName, bitField);
        } else if ("mnemonic".equals(type)) {
            Register reg = fRegisters.get(regName);
            if (reg == null) return;
            BitField bitField = reg.fBitFields.get(bitFieldName);
            if (bitField == null) return;
            bitField.fMnemonics.put(args.getNextStringArg(), args.getNextIntArg());
        }
        sendDebugEvent("registers", false);
    }

    private String getRegisterPartOfName(String name) {
        if (name.startsWith("$")) {
            int end = name.indexOf('.');
            end = end != -1 ? end : name.length();
            return name.substring(1, end);
        }
        return null;
    }

    private String getBitFieldPartOfName(String name) {
        int start = name.indexOf('.');
        if (name.startsWith("$") && start != -1) {
            return name.substring(start + 1, name.length());
        }
        return null;        
    }

    void iDup(PDAThread thread, Args args) {
        Object val = thread.fStack.pop();
        thread.fStack.push(val);
        thread.fStack.push(val);
    }
    
    void iExec(PDAThread thread, Args args) {
        String label = args.getNextStringArg();
        if (fLabels.containsKey(label)) {
            int id = fNextThreadId++;
            fThreads.put(id, new PDAThread(id, label, fLabels.get(label)));
            sendDebugEvent("started " + id, false);
        } else {
            sendDebugEvent("no such label " + label, true);
            if (fEventStops.get("nosuchlabel")) {
                thread.fSuspend = "event nosuchlabel";
                thread.fCurrentFrame.fPC--;
            }
        }
    }

    void iHalt(PDAThread thread, Args args) {
        thread.fRun = false;
    }

    void iOutput(PDAThread thread, Args args) {
        System.out.println(thread.fStack.pop());
    }

    void iPop(PDAThread thread, Args args) {
        String arg = args.getNextStringArg();
        if (arg.startsWith("$")) {
            String var = arg.substring(1);
            thread.fCurrentFrame.set(var, thread.fStack.pop());
            String key = thread.fCurrentFrame.fFunction + "::" + var;
            if (fWatchpoints.containsKey(key) && (fWatchpoints.get(key) & 2) != 0) {
                fSuspendVM = thread.fID + " watch write " + key;
            }
        } else {
            thread.fStack.pop();
        }
    }

    void iPush(PDAThread thread, Args args) {
        String arg = args.getNextStringArg();
        while (arg.length() != 0) {
            if (arg.startsWith("$")) {
                String var = arg.substring(1);
                Object val = thread.fCurrentFrame.get(var);
                if (val == null) val = "<undefined>";
                thread.fStack.push(val);
                String key = thread.fCurrentFrame.fFunction + "::" + var;
                if (fWatchpoints.containsKey(key) && (fWatchpoints.get(key) & 1) != 0) {
                    fSuspendVM = thread.fID + " watch read " + key;
                }
            } else {
                Object val = arg;
                try {
                    val = Integer.parseInt(arg);
                } catch (NumberFormatException e) {
                }
                thread.fStack.push(val);
            }
            
            arg = args.getNextStringArg();
        }
    }

    void iReturn(PDAThread thread, Args args) {
        if (!thread.fFrames.isEmpty()) {
            thread.fCurrentFrame = thread.fFrames.remove(thread.fFrames.size() - 1);
        } else {
            // Execution returned from the top frame, which means this thread
            // should exit.
            thread.fRun = false;
        }
    }

    void iVar(PDAThread thread, Args args) {
        String var = args.getNextStringArg();
        thread.fCurrentFrame.set(var, 0);
    }

    void iInternalEndEval(PDAThread thread, Args args) {
        Object result = thread.fStack.pop();
        thread.fThreadCode = fCode;
        thread.fThreadLabels = fLabels;
        thread.fCurrentFrame.fPC = thread.fSavedPC;
        sendDebugEvent("evalresult " + result, false);
        thread.fSuspend = "eval";
        thread.fPerformingEval = false;
    }

}

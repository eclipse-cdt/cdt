/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.pdavm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 */
@SuppressWarnings("serial")
public class PDAVirtualMachine {

    class Stack extends LinkedList<Object> {
        public Object pop() {
            return isEmpty() ? 0 : remove(size() - 1);
        }
        
        public void push(Object value) {
            add(value);
        }
    }

    class Args {
        private final String[] fArgs;
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
            } catch (NumberFormatException e) {}
            return 0;
        }
        
        Object getNextIntOrStringArg() {
            String arg = getNextStringArg();
            try {
                return Integer.parseInt(arg);
            } catch (NumberFormatException e) {}
            return arg;            
        }
    }

    /** The push down automata data stack (the data stack). */
    private final Stack fStack = new Stack();

    /**
     * The code is stored as an array of strings, each line of
     * the source file being one entry in the array.
     */
    private String[] fCode;

    /** A mapping of labels to indicies in the code array */
    private Map<String,Integer> fLabels = new HashMap<String, Integer>();
    
    /** Each stack frame is a mapping of variable names to values. */
    class Frame extends LinkedHashMap<String, Object> {
        /**
         * The name of the function in this frame
         */
        final String fFunction;

        /**
         * The current program counter in the frame
         * the pc points to the next instruction to be executed
         */
        int fPC;

        public Frame(String function, int pc) {
            fFunction = function;
            fPC = pc;
        }
    }
    
    /** The stack of stack frames (the control stack) */
    private final List<Frame> fFrames = new LinkedList<Frame>();

    /** Current stack frame (not includced in fFrames) */
    private Frame fCurrentFrame = new Frame("main", 0);
    
    /**
     * Breakpoints are stored as a boolean for each line of code
     * if the boolean is true, there is a breakpoint on that line
     */
    private final Map<Integer, Boolean> fBreakpoints = new HashMap<Integer, Boolean>();

    /**
     * The run flag is true if the VM is running.
     * If the run flag is false, the VM exits the
     * next time the main instruction loop runs.
     */
    private boolean fRun = true;

    /**
     * The suspend flag is true if the VM should suspend
     * running the program and just listen for debug commands.
     */
    private String fSuspend;

    /** Flag indicating whether the debugger is performing a step. */
    private boolean fStep = false;
    
    /** Flag indicating whether the debugger is performing a step return */
    private boolean fStepReturn = false;
    
    /** Flag indicating whether the started event was sent. */
    private boolean fStarted = true;

    /** Name of the pda program being debugged */
    private final String fFilename;    

    /**The command line argument to start a debug session. */
    private final boolean fDebug;

    /** The port to listen for debug commands on */
    private final int fCommandPort;

    /** Command socket for receiving debug commands and sending command responses */    
    private Socket fCommandSocket;

    /** Command socket reader */
    private BufferedReader fCommandReceiveStream;
    
    /** Command socket write stream. */
    private OutputStream fCommandResponseStream;

    /** The port to send debug events to */
    private final int fEventPort;
    
    /** Event socket */
    private Socket fEventSocket;
    
    /** Event socket and write stream. */
    private OutputStream fEventStream;

    /** The eventstops table holds which events cause suspends and which do not. */
    private final Map<String, Boolean> fEventStops =  new HashMap<String, Boolean>();
    {
        fEventStops.put("unimpinstr", false);
        fEventStops.put("nosuchlabel", false);
    }

    /**
     * The watchpoints table holds watchpoint information.
     *  variablename_stackframedepth => N
     *  N = 0 is no watch
     *  N = 1 is read watch
     *  N = 2 is write watch
     *  N = 3 is both, etc.
     */
    private final Map<String, Integer> fWatchpoints = new HashMap<String, Integer>();
    
    public String[] fSavedCode;
    public Map<String, Integer> fSavedLables;
    public int fSavedPC;
    
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
    
    public PDAVirtualMachine(String inputFile, boolean debug, int commandPort, int eventPort) throws IOException{
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

        mapLabels();
        
        fDebug = debug;
        fCommandPort = commandPort;
        fEventPort = eventPort;
        if (fDebug) {
            fSuspend = "client";
        }
    }
    
    /**
     * Initializes the labels map
     */
    private void mapLabels() {
        fLabels = new HashMap<String, Integer>();
        for (int i = 0; i < fCode.length; i++) {
            if (fCode[i].length() != 0 && fCode[i].charAt(0) == ':') {
                fLabels.put(fCode[i].substring(1), i);
            }
        }
        
    }
    
    private void sendCommandResponse(String response) {
        try {
            fCommandResponseStream.write(response.getBytes());
            fCommandResponseStream.flush();
        } catch (IOException e) {}
    }

    private void sendDebugEvent(String event, boolean error) {
        if (fDebug) {
            try {
                fEventStream.write(event.getBytes());
                fEventStream.write('\n');
                fEventStream.flush();
            } catch (IOException e) {
                System.err.println("Error: " + e);
                fRun = false;
            }
        } else if (error) {
            System.err.println("Error: " + event);
        }
    }
    
    private void startDebugger() throws IOException {
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
        
        fSuspend = "client";
        sendDebugEvent("started", false);
    }
    
    public void run() {
        while (fRun) {
            checkForBreakpoint();
            if (fSuspend != null) {
                debugUI();
            }
            yieldToDebug();
            String instruction = fCode[fCurrentFrame.fPC];
            fCurrentFrame.fPC++;
            doOneInstruction(instruction);
            if (fCurrentFrame.fPC > fCode.length) {
                fRun = false;
            } else if (fStepReturn) {
                instruction = fCode[fCurrentFrame.fPC];
                if ("return".equals(instruction)) {
                    fSuspend = "step";
                }
            }
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
    
    private void doOneInstruction(String instr) {
        if (instr.startsWith(":")) {
            // label
            if (fStep) {
                fSuspend = "step";
            }
        } else if (instr.startsWith("#")) {
            // comment
        } else {
            StringTokenizer tokenizer = new StringTokenizer(instr);
            String op = tokenizer.nextToken();
            List<String> tokens = new LinkedList<String>();
            while (tokenizer.hasMoreTokens()) {
                tokens.add(tokenizer.nextToken());
            }
            Args args = new Args(tokens.toArray(new String[tokens.size()]));

            boolean opValid = true;
            if (op.equals("add")) iAdd(args);
            else if (op.equals("branch_not_zero")) iBranchNotZero(args);
            else if (op.equals("call")) iCall(args);
            else if (op.equals("dec")) iDec(args);
            else if (op.equals("dup")) iDup(args);
            else if (op.equals("halt")) iHalt(args);
            else if (op.equals("output")) iOutput(args);
            else if (op.equals("pop")) iPop(args);
            else if (op.equals("push")) iPush(args);
            else if (op.equals("return")) iReturn(args);
            else if (op.equals("var")) iVar(args);
            else if (op.equals("xyzzy")) iInternalEndEval(args);
            else {
                opValid = false;
            }
            
            if (!opValid) {
                sendDebugEvent("unimplemented instruction " + op, true);
                if (fEventStops.get("unimpinstr")) {
                    fSuspend = "event unimpinstr";
                    fCurrentFrame.fPC--;
                }
            } else if (fStep) {
                fSuspend = "step";
            }
        }
    }
    
    private void checkForBreakpoint() {
        if (fDebug) {
            int pc = fCurrentFrame.fPC;
            if (!"eval".equals(fSuspend) && fBreakpoints.containsKey(pc) && fBreakpoints.get(pc)) {
                fSuspend = "breakpoint " + pc;
            }
        }
    }
    
    /**
     * For each instruction, we check the debug co-routine for
     * control input. If there is input, we process it.
     */
    private void yieldToDebug() {
        if (fDebug) {
            String line = "";
            try {
                if (fCommandReceiveStream.ready()) {
                    line = fCommandReceiveStream.readLine();
                    processDebugCommand(line);
                }
            } catch (IOException e) {
                System.err.println("Error: " + e);
                fRun = false;
                return;
            }
        }
    }
    
    private void debugUI() {
        if (fSuspend == null) {
            return;
        }
        
        if (!fStarted) {
            sendDebugEvent("suspended " + fSuspend, false);
        } else {
            fStarted = false;
        }
        
        fStep = false;
        fStepReturn = false;
        
        while (fSuspend != null) {
            String line = "";
            try {
                line = fCommandReceiveStream.readLine();
            } catch (IOException e) {
                System.err.println("Error: " + e);
                fRun = false;
                return;
            }
            processDebugCommand(line);
        }
        if (fStep) {
            sendDebugEvent("resumed step", false);
        } else {
            sendDebugEvent("resumed client", false);
        }
    }
    
    private void processDebugCommand(String line) {
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

        if ("clear".equals(command)) debugClearBreakpoint(args);
        else if ("data".equals(command)) debugData();
        else if ("drop".equals(command)) debugDropFrame();
        else if ("eval".equals(command)) debugEval(args);
        else if ("eventstop".equals(command)) debugEventStop(args);
        else if ("exit".equals(command)) debugExit();
        else if ("popdata".equals(command)) debugPop();
        else if ("pushdata".equals(command)) debugPush(args);
        else if ("resume".equals(command)) debugResume();
        else if ("set".equals(command)) debugSetBreakpoint(args);
        else if ("setdata".equals(command)) debugSetData(args);
        else if ("setvar".equals(command)) debugSetVariable(args);
        else if ("stack".equals(command)) debugStack();
        else if ("step".equals(command)) debugStep();
        else if ("stepreturn".equals(command)) debugStepReturn();
        else if ("suspend".equals(command)) debugSuspend();
        else if ("var".equals(command)) debugVar(args);
        else if ("watch".equals(command)) debugWatch(args);
    }
    
    private void debugClearBreakpoint(Args args) {
        int line = args.getNextIntArg();
        
        fBreakpoints.put(line, false);
        sendCommandResponse("ok\n");
    }

    private static Pattern fPackPattern = Pattern.compile("%([a-fA-F0-9][a-fA-F0-9])");
    private void debugEval(Args args) {
        
        StringTokenizer tokenizer = new StringTokenizer(args.getNextStringArg(), "|");
        tokenizer.countTokens();
        
        fSavedCode = fCode;
        int numEvalLines = tokenizer.countTokens();
        fCode = new String[fSavedCode.length + numEvalLines + 1];
        System.arraycopy(fSavedCode, 0, fCode, 0, fSavedCode.length);
        for (int i = 0; i < numEvalLines; i++) {
            String line = tokenizer.nextToken();
            StringBuffer lineBuf = new StringBuffer(line.length());
            Matcher matcher = fPackPattern.matcher(line);
            int lastMatchEnd = 0;
            while (matcher.find()) {
                lineBuf.append(line.substring(lastMatchEnd, matcher.start()));
                String charCode = line.substring(matcher.start() + 1, matcher.start() + 3);
                try {
                    lineBuf.append((char)Integer.parseInt(charCode, 16));
                } catch (NumberFormatException e) {}
                lastMatchEnd = matcher.end();
            }
            if (lastMatchEnd < line.length()) {
                lineBuf.append(line.substring(lastMatchEnd));
            }
            fCode[fSavedCode.length + i] = lineBuf.toString();
        }
        fCode[fSavedCode.length + numEvalLines] = "xyzzy";
        mapLabels(); 
        
        fSavedPC = fCurrentFrame.fPC;
        fCurrentFrame.fPC = fSavedCode.length;
        
        sendCommandResponse("ok\n");
        
        fSuspend = null;
    }
    
    private void debugData() {
        StringBuffer result = new StringBuffer();
        for (Object val : fStack) {
            result.append(val);
            result.append('|');
        }
        result.append('\n');
        sendCommandResponse(result.toString());
    }
    
    private void debugDropFrame() {
        if (!fFrames.isEmpty()) {
            fCurrentFrame = fFrames.remove(fFrames.size() - 1);
        }
        fCurrentFrame.fPC--;
        sendCommandResponse("ok\n");
        sendDebugEvent( "resumed drop", false );
        sendDebugEvent( "suspended drop", false );
    }
    
    private void debugEventStop(Args args) {
        String event = args.getNextStringArg();
        int stop = args.getNextIntArg();
        fEventStops.put(event, stop > 0);
        sendCommandResponse("ok\n");
    }

    private void debugExit() {
        sendCommandResponse("ok\n");
        sendDebugEvent( "terminated", false );
        System.exit(0);
    }
    
    private void debugPop() {
        fStack.pop();
        sendCommandResponse("ok\n");
    }
    
    private void debugPush(Args args) {
        Object val = args.getNextIntOrStringArg();
        fStack.push(val);
        sendCommandResponse("ok\n");
    }

    private void debugResume() {
        fSuspend = null;
        sendCommandResponse("ok\n");
    }

    private void debugSetBreakpoint(Args args) {
        int line = args.getNextIntArg();
        
        fBreakpoints.put(line, true);
        sendCommandResponse("ok\n");
    }
    
    private void debugSetData(Args args) {
        int offset = args.getNextIntArg();
        Object val = args.getNextIntOrStringArg();

        if (offset < fStack.size()) {
            fStack.set(offset, val);
        } else {
            fStack.add(0, val);
        }
        sendCommandResponse("ok\n");
    }

    private void debugSetVariable(Args args) {
        int sfnumber = args.getNextIntArg();
        String var = args.getNextStringArg();
        Object val = args.getNextIntOrStringArg();
        
        if (sfnumber > fFrames.size()) {
            fCurrentFrame.put(var, val);
        } else {
            fFrames.get(sfnumber).put(var, val);
        }
        sendCommandResponse("ok\n");        
    }
    
    private void debugStack() {
        StringBuffer result = new StringBuffer();
        for (Frame frame : fFrames) {
            result.append(printFrame(frame));
            result.append('#');
        }
        result.append(printFrame(fCurrentFrame));
        result.append('\n');
        sendCommandResponse(result.toString());
    }


    /**
     * The stack frame output is:
     * frame # frame # frame ...
     * where each frame is:
     * filename | line number | function name | var | var | var | var ...
     */
    private String printFrame(Frame frame) {
        StringBuffer buf = new StringBuffer();
        buf.append(fFilename);
        buf.append('|');
        buf.append(frame.fPC);
        buf.append('|');
        buf.append(frame.fFunction);
        for (String var : frame.keySet()) {
            buf.append('|');
            buf.append(var);
        }
        return buf.toString();
    }

    private void debugStep() {
        // set suspend to 0 to allow the debug loop to exit back to
        // the instruction loop and thus run an instruction. However,
        // we want to come back to the debug loop right away, so the
        // step flag is set to true which will cause the suspend flag
        // to get set to true when we get to the next instruction.
        fStep = true;
        fSuspend = null;
        sendCommandResponse("ok\n");        
    }

    private void debugStepReturn() {
        fStepReturn = true;
        fSuspend = null;
        sendCommandResponse("ok\n");        
    }
    
    private void debugSuspend() {
        fSuspend = "client";
        sendCommandResponse("ok\n");        
    }
    
    private void debugVar(Args args) {
        int sfnumber = args.getNextIntArg();
        String var = args.getNextStringArg();

        if (sfnumber >= fFrames.size()) {
            sendCommandResponse(fCurrentFrame.get(var).toString() + "\n");
        } else {
            sendCommandResponse(fFrames.get(sfnumber).get(var).toString() + "\n");
        }
    }
    
    private void debugWatch(Args args) {
        String funcAndVar = args.getNextStringArg();
        int flags = args.getNextIntArg();
        fWatchpoints.put(funcAndVar, flags);
        sendCommandResponse("ok\n");        
    }

    private void iAdd(Args args) {
        Object val1 = fStack.pop();
        Object val2 = fStack.pop();
        if (val1 instanceof Integer && val2 instanceof Integer) {
            int intVal1 = ((Integer)val1).intValue();
            int intVal2 = ((Integer)val2).intValue();
            fStack.push(intVal1 + intVal2);
        } else {
            fStack.push(-1);
        }
    }

    private void iBranchNotZero(Args args) {
        Object val = fStack.pop();
        if( val instanceof Integer && ((Integer)val).intValue() != 0 ) {
            String label = args.getNextStringArg();
            if (fLabels.containsKey(label)) {
                fCurrentFrame.fPC = fLabels.get(label);
            } else {
                sendDebugEvent("no such label " + label, true);
                if( fEventStops.get("nosuchlabel") ) {
                    fSuspend = "event nosuchlabel";
                    fStack.push(val);
                    fCurrentFrame.fPC--;
                }
            } 
        }
    }

    private void iCall(Args args) {
        String label = args.getNextStringArg();
        if (fLabels.containsKey(label)) {
            fFrames.add(fCurrentFrame);
            fCurrentFrame = new Frame(label, fLabels.get(label));
        } else {
            sendDebugEvent("no such label " + label, true);
            if( fEventStops.get("nosuchlabel") ) {
                fSuspend = "event nosuchlabel";
                fCurrentFrame.fPC--;
            }
        } 
    }

    private void iDec(Args args) {
        Object val = fStack.pop();
        if (val instanceof Integer) {
            val = new Integer( ((Integer)val).intValue() - 1 );
        }
        fStack.push(val);
    }        

    private void iDup(Args args) {
        Object val = fStack.pop();
        fStack.push(val);
        fStack.push(val);
    }        

    private void iHalt(Args args) {
        fRun = false;
    }        

    private void iOutput(Args args) {
        System.out.println(fStack.pop());
    }        

    private void iPop(Args args) {
        String arg = args.getNextStringArg();
        if (arg.startsWith("$")) {
            String var = arg.substring(1);
            fCurrentFrame.put(var, fStack.pop());
            String key = fCurrentFrame.fFunction + "::" + var;
            if (fWatchpoints.containsKey(key) && (fWatchpoints.get(key) & 2) != 0) {
                fSuspend = "watch write " + key;
            }
        } else {
            fStack.pop();
        }
    }        

    private void iPush(Args args) {
        String arg = args.getNextStringArg();
        if (arg.startsWith("$")) {
            String var = arg.substring(1);
            Object val = fCurrentFrame.containsKey(var) ? fCurrentFrame.get(var) : "<undefined>";
            fStack.push(val);
            String key = fCurrentFrame.fFunction + "::" + var;
            if (fWatchpoints.containsKey(key) && (fWatchpoints.get(key) & 1) != 0) {
                fSuspend = "watch read " + key;
            }
        } else {
            Object val = arg;
            try {
                val = Integer.parseInt(arg);
            } catch (NumberFormatException e) {}
            fStack.push(val);                    
        }
    }        
    private void iReturn(Args args) {
        if (!fFrames.isEmpty()) {
            fCurrentFrame = fFrames.remove(fFrames.size() - 1);
        } 
    }
    private void iVar(Args args) {
        String var = args.getNextStringArg();
        fCurrentFrame.put(var, 0);
    }        

    private void iInternalEndEval(Args args) {
        Object result = fStack.pop();
        fCode = fSavedCode;
        fLabels = fSavedLables;
        fCurrentFrame.fPC = fSavedPC;
        sendDebugEvent("evalresult " + result, false);
        fSuspend = "eval";
    }        
    
    
}

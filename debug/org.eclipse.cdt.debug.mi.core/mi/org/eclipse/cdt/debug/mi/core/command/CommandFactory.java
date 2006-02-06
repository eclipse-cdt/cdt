/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command;


/**
 * Factory to create GDB commands.
 */
public class CommandFactory {

	String fMIVersion;

	protected CommandFactory() {
	}

	public CommandFactory(String miVersion) {
		fMIVersion = miVersion;
	}

	public void setMIVersion(String miVersion) {
		fMIVersion = miVersion;
	}

	public String getMIVersion() {
		return fMIVersion;
	}

	public MIBreakAfter createMIBreakAfter(int brknum, int count) {
		return new MIBreakAfter(getMIVersion(), brknum, count);
	}
	
	public MIBreakCondition createMIBreakCondition (int brknum, String expr) {
		return new MIBreakCondition(getMIVersion(), brknum, expr);
	}

	public MIBreakDelete createMIBreakDelete (int[] brknum) {
		return new MIBreakDelete(getMIVersion(), brknum);
	}

	public MIBreakDisable createMIBreakDisable(int[] brknum) {
		return new MIBreakDisable(getMIVersion(), brknum);
	}

	public MIBreakEnable createMIBreakEnable(int[] brknum) {
		return new MIBreakEnable(getMIVersion(), brknum);
	}

	public MIBreakInsert createMIBreakInsert(boolean isTemporary, boolean isHardware,
			 String condition, int ignoreCount, String line, int tid) {
		return new MIBreakInsert(getMIVersion(), isTemporary, isHardware, condition, ignoreCount, line, tid);
	}

	public MIBreakInsert createMIBreakInsert(String func) {
		return new MIBreakInsert(getMIVersion(), func);
	}

	public MIBreakList createMIBreakList() {
		return new MIBreakList(getMIVersion());
	}

	public MIBreakWatch createMIBreakWatch(boolean access, boolean read, String expression) {
		return new MIBreakWatch(getMIVersion(), access, read, expression);
	}

	public MIDataDisassemble createMIDataDisassemble(String start, String end, boolean mixed) {
		return new MIDataDisassemble(getMIVersion(), start, end, mixed);
	}

	public MIDataDisassemble createMIDataDisassemble(String file, int linenum, int lines, boolean mixed) {
		return new MIDataDisassemble(getMIVersion(), file, linenum, lines, mixed);
	}

	public MIDataEvaluateExpression createMIDataEvaluateExpression(String expression) {
		return new MIDataEvaluateExpression(getMIVersion(), expression);
	}

	public MIDataListChangedRegisters createMIDataListChangedRegisters() {
		return new MIDataListChangedRegisters(getMIVersion());
	}

	public MIDataListRegisterNames createMIDataListRegisterNames() {
		return new MIDataListRegisterNames(getMIVersion());
	}

	public MIDataListRegisterNames createMIDataListRegisterNames(int[] regnos) {
		return new MIDataListRegisterNames(getMIVersion(), regnos);
	}

	public MIDataListRegisterValues createMIDataListRegisterValues(int fmt, int[] regnos) {
		return new MIDataListRegisterValues(getMIVersion(), fmt, regnos);
	}

	public MIDataWriteRegisterValues createMIDataWriteRegisterValues(int fmt, int[] regnos, String[] values) {
		return new MIDataWriteRegisterValues(getMIVersion(), fmt, regnos, values);
	}

	public MIDataReadMemory createMIDataReadMemory(long offset, String address,
							int wordFormat, int wordSize,
							int rows, int cols, Character asChar) {
		return new MIDataReadMemory(getMIVersion(), offset, address, wordFormat, wordSize,
						rows, cols, asChar);
	}

	public MIDataWriteMemory createMIDataWriteMemory(long offset, String address,
							int wordFormat, int wordSize,
							String value) {
		return new MIDataWriteMemory(getMIVersion(), offset, address, wordFormat, wordSize, value);
	}

	public MIEnvironmentCD createMIEnvironmentCD(String pathdir) {
		return new MIEnvironmentCD(getMIVersion(), pathdir);
	}

	public MIEnvironmentDirectory createMIEnvironmentDirectory(boolean reset, String[] pathdirs) {
		return new MIEnvironmentDirectory(getMIVersion(), reset, pathdirs);
	}

	public MIEnvironmentPath createMIEnvironmentPath(String[] paths) {
		return new MIEnvironmentPath(getMIVersion(), paths);
	}

	public MIEnvironmentPWD createMIEnvironmentPWD() {
		return new MIEnvironmentPWD(getMIVersion());
	}

	/**
	 * @param params
	 * @return
	 */
	public MIGDBSetEnvironment createMIGDBSetEnvironment(String[] params) {
		return new MIGDBSetEnvironment(getMIVersion(), params);
	}

	public CLIExecAbort createCLIExecAbort() {
		return new CLIExecAbort();
	}

	public MIExecArguments createMIExecArguments(String[] args) {
		return new MIExecArguments(getMIVersion(), args);
	}

	public MIExecContinue createMIExecContinue() {
		return new MIExecContinue(getMIVersion());
	}

	public MIExecFinish createMIExecFinish() {
		return new MIExecFinish(getMIVersion());
	}

	public MIExecInterrupt createMIExecInterrupt() {
		// return null here to signal that we do not support
		// -exec-interrupt and to use to drop a signal to gdb
		// instead via the MIProcess class
		return null;
	}

	public MIExecNext createMIExecNext(int count) {
		return new MIExecNext(getMIVersion(), count);
	}

	public MIExecNextInstruction createMIExecNextInstruction(int count) {
		return new MIExecNextInstruction(getMIVersion(), count);
	}

	public MIExecReturn createMIExecReturn() {
		return new MIExecReturn(getMIVersion());
	}

	public MIExecReturn createMIExecReturn(String arg) {
		return new MIExecReturn(getMIVersion(), arg);
	}

	public MIExecRun createMIExecRun(String[] args) {
		return new MIExecRun(getMIVersion(), args);
	}

	public MIExecStep createMIExecStep(int count) {
		return new MIExecStep(getMIVersion(), count);
	}

	public MIExecStepInstruction createMIExecStepInstruction(int count) {
		return new MIExecStepInstruction(getMIVersion(), count);
	}

	public MIExecUntil createMIExecUntil(String location) {
		return new MIExecUntil(getMIVersion(), location);
	}

	public CLIJump createCLIJump(String location) {
		return new CLIJump(location);
	}

	public MIFileExecFile createMIFileExecFile(String file) {
		return new MIFileExecFile(getMIVersion(), file);
	}

	public MIFileSymbolFile createMIFileSymbolFile(String file) {
		return new MIFileSymbolFile(getMIVersion(), file);
	}

	public MIGDBExit createMIGDBExit() {
		return new MIGDBExit(getMIVersion());
	}

	public MIGDBSet createMIGDBSet(String[] params) {
		return new MIGDBSet(getMIVersion(), params);
	}

	public MIGDBSetAutoSolib createMIGDBSetAutoSolib(boolean set) {
		return new MIGDBSetAutoSolib(getMIVersion(), set);
	}

	public MIGDBSetStopOnSolibEvents createMIGDBSetStopOnSolibEvents(boolean set) {
		return new MIGDBSetStopOnSolibEvents(getMIVersion(), set);
	}

	public MIGDBSetSolibSearchPath createMIGDBSetSolibSearchPath(String[] params) {
		return new MIGDBSetSolibSearchPath(getMIVersion(), params);
	}

	public MIGDBSetBreakpointPending createMIGDBSetBreakpointPending(boolean set) {
		return new MIGDBSetBreakpointPending(getMIVersion(), set);
	}

	public MIGDBShow createMIGDBShow(String[] params) {
		return new MIGDBShow(getMIVersion(), params);
	}

	public MIGDBShowPrompt createMIGDBShowPrompt() {
		return new MIGDBShowPrompt(getMIVersion());
	}

	public MIGDBShowExitCode createMIGDBShowExitCode() {
		return new MIGDBShowExitCode(getMIVersion());
	}

	public MIGDBShowDirectories createMIGDBShowDirectories() {
		return new MIGDBShowDirectories(getMIVersion());
	}

	public MIGDBShowSolibSearchPath createMIGDBShowSolibSearchPath() {
		return new MIGDBShowSolibSearchPath(getMIVersion());
	}

	public MIGDBShowAddressSize createMIGDBShowAddressSize() {
		return new MIGDBShowAddressSize(getMIVersion());
	}

	public MIGDBShowEndian createMIGDBShowEndian() {
		return new MIGDBShowEndian(getMIVersion());
	}

	public MIStackInfoDepth createMIStackInfoDepth() {
		return new MIStackInfoDepth(getMIVersion());
	}

	public MIStackInfoDepth createMIStackInfoDepth(int depth) {
		return new MIStackInfoDepth(getMIVersion(), depth);
	}

	public MIStackListArguments createMIStackListArguments(boolean showValue) {
		return new MIStackListArguments(getMIVersion(), showValue);
	}

	public MIStackListArguments createMIStackListArguments(boolean showValue, int lowFrame, int highFrame) {
		return new MIStackListArguments(getMIVersion(), showValue, lowFrame, highFrame);
	}

	public MIStackListFrames createMIStackListFrames() {
		return new MIStackListFrames(getMIVersion());
	}

	public MIStackListFrames createMIStackListFrames(int lowFrame, int highFrame) {
		return new MIStackListFrames(getMIVersion(), lowFrame, highFrame);
	}

	public MIStackListLocals createMIStackListLocals(boolean showValues) {
		return new MIStackListLocals(getMIVersion(), showValues);
	}

	public MIStackSelectFrame createMIStackSelectFrame(int frameNum) {
		return new MIStackSelectFrame(getMIVersion(), frameNum);
	}

	public CLITargetAttach createCLITargetAttach(int pid) {
		return new CLITargetAttach(pid);
	}

	public MITargetDetach createMITargetDetach() {
		return new MITargetDetach(getMIVersion());
	}

	public MITargetSelect createMITargetSelect(String[] params) {
		return new MITargetSelect(getMIVersion(), params);
	}

	public MIThreadListIds createMIThreadListIds() {
		return new MIThreadListIds(getMIVersion());
	}

	public CLIInfoThreads createCLIInfoThreads() {
		return new CLIInfoThreads();
	}

	public MIThreadSelect createMIThreadSelect(int threadNum) {
		return new MIThreadSelect(getMIVersion(), threadNum);
	}

	public CLIInfoSharedLibrary createCLIInfoSharedLibrary() {
		return new CLIInfoSharedLibrary();
	}

	public CLISharedLibrary createCLISharedLibrary() {
		return new CLISharedLibrary();
	}

	public CLISharedLibrary createCLISharedLibrary(String name) {
		return new CLISharedLibrary(name);
	}

	public CLIWhatis createCLIWhatis(String name) {
		return new CLIWhatis(name);
	}

	public CLIInfoSignals createCLIInfoSignals() {
		return new CLIInfoSignals();
	}

	public CLIInfoSignals createCLIInfoSignals(String name) {
		return new CLIInfoSignals(name);
	}

	public CLIHandle createCLIHandle(String arg) {
		return new CLIHandle(arg);
	}

	public CLISignal createCLISignal(String arg) {
		return new CLISignal(arg);
	}

	public CLIPType createCLIPType(String name) {
		return new CLIPType(name);
	}

	public CLIInfoProgram createCLIInfoProgram() {
		return new CLIInfoProgram();
	}

	public MIVarCreate createMIVarCreate(String expression) {
		return new MIVarCreate(getMIVersion(), expression);
	}

	public MIVarCreate createMIVarCreate(String name, String frameAddr, String expression) {
		return new MIVarCreate(getMIVersion(), name, frameAddr, expression);
	}

	public MIVarDelete createMIVarDelete(String name) {
		return new MIVarDelete(getMIVersion(), name);
	}

	public MIVarSetFormat createMIVarSetFormat(String name, int format) {
		return new MIVarSetFormat(getMIVersion(), name, format);
	}

	public MIVarShowFormat createMIVarShowFormat(String name) {
		return new MIVarShowFormat(getMIVersion(), name);
	}

	public MIVarInfoNumChildren createMIVarInfoNumChildren(String name) {
		return new MIVarInfoNumChildren(getMIVersion(), name);
	}

	public MIVarListChildren createMIVarListChildren(String name) {
		return new MIVarListChildren(getMIVersion(), name);
	}

	public MIVarInfoType createMIVarInfoType(String name) {
		return new MIVarInfoType(getMIVersion(), name);
	}

	public MIVarInfoExpression createMIVarInfoExpression(String name) {
		return new MIVarInfoExpression(getMIVersion(), name);
	}

	public MIVarShowAttributes createMIVarShowAttributes(String name) {
		return new MIVarShowAttributes(getMIVersion(), name);
	}

	public MIVarEvaluateExpression createMIVarEvaluateExpression(String name) {
		return new MIVarEvaluateExpression(getMIVersion(), name);
	}

	public MIVarAssign createMIVarAssign(String name, String expr) {
		return new MIVarAssign(getMIVersion(), name, expr);
	}

	public MIVarUpdate createMIVarUpdate() {
		return new MIVarUpdate(getMIVersion());
	}

	public MIVarUpdate createMIVarUpdate(String name) {
		return new MIVarUpdate(getMIVersion(), name);
	}

	public MIInterpreterExecConsole createMIInterpreterExecConsole(String cmd) {
		return new MIInterpreterExecConsole(getMIVersion(), cmd);
	}
}

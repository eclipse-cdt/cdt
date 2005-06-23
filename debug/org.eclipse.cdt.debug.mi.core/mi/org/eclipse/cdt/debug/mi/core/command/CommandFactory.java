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

	public MIBreakAfter createMIBreakAfter(int brknum, int count) {
		return new MIBreakAfter(brknum, count);
	}
	
	public MIBreakCondition createMIBreakCondition (int brknum, String expr) {
		return new MIBreakCondition(brknum, expr);
	}

	public MIBreakDelete createMIBreakDelete (int[] brknum) {
		return new MIBreakDelete(brknum);
	}

	public MIBreakDisable createMIBreakDisable(int[] brknum) {
		return new MIBreakDisable(brknum);
	}

	public MIBreakEnable createMIBreakEnable(int[] brknum) {
		return new MIBreakEnable(brknum);
	}

	public MIBreakInsert createMIBreakInsert(boolean isTemporary, boolean isHardware,
			 String condition, int ignoreCount, String line, int tid) {
		return new MIBreakInsert(isTemporary, isHardware, condition, ignoreCount, line, tid);
	}

	public MIBreakInsert createMIBreakInsert(String func) {
		return new MIBreakInsert(func);
	}

	public MIBreakList createMIBreakList() {
		return new MIBreakList();
	}

	public MIBreakWatch createMIBreakWatch(boolean access, boolean read, String expression) {
		return new MIBreakWatch(access, read, expression);
	}

	public MIDataDisassemble createMIDataDisassemble(String start, String end, boolean mixed) {
		return new MIDataDisassemble(start, end, mixed);
	}

	public MIDataDisassemble createMIDataDisassemble(String file, int linenum, int lines, boolean mixed) {
		return new MIDataDisassemble(file, linenum, lines, mixed);
	}

	public MIDataEvaluateExpression createMIDataEvaluateExpression(String expression) {
		return new MIDataEvaluateExpression(expression);
	}

	public MIDataListChangedRegisters createMIDataListChangedRegisters() {
		return new MIDataListChangedRegisters();
	}

	public MIDataListRegisterNames createMIDataListRegisterNames() {
		return new MIDataListRegisterNames();
	}

	public MIDataListRegisterNames createMIDataListRegisterNames(int[] regnos) {
		return new MIDataListRegisterNames(regnos);
	}

	public MIDataListRegisterValues createMIDataListRegisterValues(int fmt, int[] regnos) {
		return new MIDataListRegisterValues(fmt, regnos);
	}

	public MIDataWriteRegisterValues createMIDataWriteRegisterValues(int fmt, int[] regnos, String[] values) {
		return new MIDataWriteRegisterValues(fmt, regnos, values);
	}

	public MIDataReadMemory createMIDataReadMemory(long offset, String address,
							int wordFormat, int wordSize,
							int rows, int cols, Character asChar) {
		return new MIDataReadMemory(offset, address, wordFormat, wordSize,
						rows, cols, asChar);
	}

	public MIDataWriteMemory createMIDataWriteMemory(long offset, String address,
							int wordFormat, int wordSize,
							String value) {
		return new MIDataWriteMemory(offset, address, wordFormat, wordSize, value);
	}

	public MIEnvironmentCD createMIEnvironmentCD(String pathdir) {
		return new MIEnvironmentCD(pathdir);
	}

	public MIEnvironmentDirectory createMIEnvironmentDirectory(boolean reset, String[] pathdirs) {
		return new MIEnvironmentDirectory(reset, pathdirs);
	}

	public MIEnvironmentPath createMIEnvironmentPath(String[] paths) {
		return new MIEnvironmentPath(paths);
	}

	public MIEnvironmentPWD createMIEnvironmentPWD() {
		return new MIEnvironmentPWD();
	}

	/**
	 * @param params
	 * @return
	 */
	public MIGDBSetEnvironment createMIGDBSetEnvironment(String[] params) {
		return new MIGDBSetEnvironment(params);
	}

	public MIExecAbort createMIExecAbort() {
		return new MIExecAbort();
	}

	public MIExecArguments createMIExecArguments(String[] args) {
		return new MIExecArguments(args);
	}

	public MIExecContinue createMIExecContinue() {
		return new MIExecContinue();
	}

	public MIExecFinish createMIExecFinish() {
		return new MIExecFinish();
	}

	public MIExecInterrupt createMIExecInterrupt() {
		// return null here to signal that we do not support
		// -exec-interrupt and to use to drop a signal to gdb
		// instead via the MIProcess class
		return null;
	}

	public MIExecNext createMIExecNext(int count) {
		return new MIExecNext(count);
	}

	public MIExecNextInstruction createMIExecNextInstruction(int count) {
		return new MIExecNextInstruction(count);
	}

	public MIExecReturn createMIExecReturn() {
		return new MIExecReturn();
	}

	public MIExecReturn createMIExecReturn(String arg) {
		return new MIExecReturn(arg);
	}

	public MIExecRun createMIExecRun(String[] args) {
		return new MIExecRun(args);
	}

	public MIExecStep createMIExecStep(int count) {
		return new MIExecStep(count);
	}

	public MIExecStepInstruction createMIExecStepInstruction(int count) {
		return new MIExecStepInstruction(count);
	}

	public MIExecUntil createMIExecUntil(String location) {
		return new MIExecUntil(location);
	}

	public MIJump createMIJump(String location) {
		return new MIJump(location);
	}

	public MIFileExecFile createMIFileExecFile(String file) {
		return new MIFileExecFile(file);
	}

	public MIFileSymbolFile createMIFileSymbolFile(String file) {
		return new MIFileSymbolFile(file);
	}

	public MIGDBExit createMIGDBExit() {
		return new MIGDBExit();
	}

	public MIGDBSet createMIGDBSet(String[] params) {
		return new MIGDBSet(params);
	}

	public MIGDBSetAutoSolib createMIGDBSetAutoSolib(boolean set) {
		return new MIGDBSetAutoSolib(set);
	}

	public MIGDBSetStopOnSolibEvents createMIGDBSetStopOnSolibEvents(boolean set) {
		return new MIGDBSetStopOnSolibEvents(set);
	}

	public MIGDBSetSolibSearchPath createMIGDBSetSolibSearchPath(String[] params) {
		return new MIGDBSetSolibSearchPath(params);
	}

	public MIGDBShow createMIGDBShow(String[] params) {
		return new MIGDBShow(params);
	}

	public MIGDBShowExitCode createMIGDBShowExitCode() {
		return new MIGDBShowExitCode();
	}

	public MIGDBShowDirectories createMIGDBShowDirectories() {
		return new MIGDBShowDirectories();
	}

	public MIGDBShowSolibSearchPath createMIGDBShowSolibSearchPath() {
		return new MIGDBShowSolibSearchPath();
	}

	public MIGDBShowAddressSize createMIGDBShowAddressSize() {
		return new MIGDBShowAddressSize();
	}

	public MIGDBShowEndian createMIGDBShowEndian() {
		return new MIGDBShowEndian();
	}

	public MIStackInfoDepth createMIStackInfoDepth() {
		return new MIStackInfoDepth();
	}

	public MIStackInfoDepth createMIStackInfoDepth(int depth) {
		return new MIStackInfoDepth(depth);
	}

	public MIStackListArguments createMIStackListArguments(boolean showValue) {
		return new MIStackListArguments(showValue);
	}

	public MIStackListArguments createMIStackListArguments(boolean showValue, int lowFrame, int highFrame) {
		return new MIStackListArguments(showValue, lowFrame, highFrame);
	}

	public MIStackListFrames createMIStackListFrames() {
		return new MIStackListFrames();
	}

	public MIStackListFrames createMIStackListFrames(int lowFrame, int highFrame) {
		return new MIStackListFrames(lowFrame, highFrame);
	}

	public MIStackListLocals createMIStackListLocals(boolean showValues) {
		return new MIStackListLocals(showValues);
	}

	public MIStackSelectFrame createMIStackSelectFrame(int frameNum) {
		return new MIStackSelectFrame(frameNum);
	}

	public MITargetAttach createMITargetAttach(int pid) {
		return new MITargetAttach(pid);
	}

	public MITargetDetach createMITargetDetach() {
		return new MITargetDetach();
	}

	public MITargetSelect createMITargetSelect(String[] params) {
		return new MITargetSelect(params);
	}

	public MIThreadListIds createMIThreadListIds() {
		return new MIThreadListIds();
	}

	public MIInfoThreads createMIInfoThreads() {
		return new MIInfoThreads();
	}

	public MIThreadSelect createMIThreadSelect(int threadNum) {
		return new MIThreadSelect(threadNum);
	}

	public MIInfoSharedLibrary createMIInfoSharedLibrary() {
		return new MIInfoSharedLibrary();
	}

	public MISharedLibrary createMISharedLibrary() {
		return new MISharedLibrary();
	}

	public MISharedLibrary createMISharedLibrary(String name) {
		return new MISharedLibrary(name);
	}

	public MIWhatis createMIWhatis(String name) {
		return new MIWhatis(name);
	}

	public MIInfoSignals createMIInfoSignals() {
		return new MIInfoSignals();
	}

	public MIInfoSignals createMIInfoSignals(String name) {
		return new MIInfoSignals(name);
	}

	public MIHandle createMIHandle(String arg) {
		return new MIHandle(arg);
	}

	public MISignal createMISignal(String arg) {
		return new MISignal(arg);
	}

	public MIPType createMIPType(String name) {
		return new MIPType(name);
	}

	public MIInfoProgram createMIInfoProgram() {
		return new MIInfoProgram();
	}

	public MIVarCreate createMIVarCreate(String expression) {
		return new MIVarCreate(expression);
	}

	public MIVarCreate createMIVarCreate(String name, String frameAddr, String expression) {
		return new MIVarCreate(name, frameAddr, expression);
	}

	public MIVarDelete createMIVarDelete(String name) {
		return new MIVarDelete(name);
	}

	public MIVarSetFormat createMIVarSetFormat(String name, int format) {
		return new MIVarSetFormat(name, format);
	}

	public MIVarShowFormat createMIVarShowFormat(String name) {
		return new MIVarShowFormat(name);
	}

	public MIVarInfoNumChildren createMIVarInfoNumChildren(String name) {
		return new MIVarInfoNumChildren(name);
	}

	public MIVarListChildren createMIVarListChildren(String name) {
		return new MIVarListChildren(name);
	}

	public MIVarInfoType createMIVarInfoType(String name) {
		return new MIVarInfoType(name);
	}

	public MIVarInfoExpression createMIVarInfoExpression(String name) {
		return new MIVarInfoExpression(name);
	}

	public MIVarShowAttributes createMIVarShowAttributes(String name) {
		return new MIVarShowAttributes(name);
	}

	public MIVarEvaluateExpression createMIVarEvaluateExpression(String name) {
		return new MIVarEvaluateExpression(name);
	}

	public MIVarAssign createMIVarAssign(String name, String expr) {
		return new MIVarAssign(name, expr);
	}

	public MIVarUpdate createMIVarUpdate() {
		return new MIVarUpdate();
	}

	public MIVarUpdate createMIVarUpdate(String name) {
		return new MIVarUpdate(name);
	}

}

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

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
						 String condition, int ignoreCount, String line) {
		return new MIBreakInsert(isTemporary, isHardware, condition, ignoreCount, line);
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

	public MIDataListRegisterNames createMIDataListRegisterNames(int[] regno) {
		return new MIDataListRegisterNames(regno);
	}

	public MIDataListRegisterValues createMIDataLIstRegisterValues(int fmt, int[] regno) {
		return new MIDataListRegisterValues(fmt, regno);
	}

	public MIDataReadMemory createMIDataReadMemory(long offset, String address,
							int wordFormat, int wordSize,
							int rows, int cols, Character asChar) {
		return new MIDataReadMemory(offset, address, wordFormat, wordSize,
						rows, cols, asChar);
	}

	public MIEnvironmentCD createMIEnvironmentCD(String pathdir) {
		return new MIEnvironmentCD(pathdir);
	}

	public MIEnvironmentDirectory createMIEnvironmentDirectory(String pathdir) {
		return new MIEnvironmentDirectory(pathdir);
	}

	public MIEnvironmentPath createMIEnvironmentPath(String[] paths) {
		return new MIEnvironmentPath(paths);
	}

	public MIEnvironmentPWD createMIEnvironmentPWD() {
		return new MIEnvironmentPWD();
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
		return new MIExecInterrupt();
	}

	public MIExecNext createMIExecNext() {
		return new MIExecNext();
	}

	public MIExecNextInstruction createMIExecNextInstruction() {
		return new MIExecNextInstruction();
	}

	public MIExecReturn createMIExecReturn() {
		return new MIExecReturn();
	}

	public MIExecRun createMIExecRun(String[] args) {
		return new MIExecRun(args);
	}

	public MIExecStep createMIExecStep() {
		return new MIExecStep();
	}

	public MIExecStepInstruction createMIExecStepInstruction() {
		return new MIExecStepInstruction();
	}

	public MIExecUntil createMIExecUntil(String location) {
		return new MIExecUntil(location);
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

	public MIGDBShow createMIGDBShow(String[] params) {
		return new MIGDBShow(params);
	}

	public MIGDBShowExitCode createMIGDBShowExitCode() {
		return new MIGDBShowExitCode();
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

	public MIThreadSelect createMIThreadSelect(int threadNum) {
		return new MIThreadSelect(threadNum);
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

	public MIVarUpdate createMIUpdate(String name) {
		return new MIVarUpdate(name);
	}

}

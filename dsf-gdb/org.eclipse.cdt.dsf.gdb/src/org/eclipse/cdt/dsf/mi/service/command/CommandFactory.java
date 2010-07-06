/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     ENEA Software AB - CLI command extension - fix for bug 190277
 *     Ericsson - Implementation for DSF-GDB
 *     Anna Dushistova (Mentor Graphics) - [318322] Add set solib-absolute-prefix
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.IModuleDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIAttach;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLICatch;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIDetach;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIExecAbort;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIInfoProgram;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIInfoSharedLibrary;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIInfoThreads;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIJump;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIPasscount;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIRecord;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLISource;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIThread;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLITrace;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIUnsetEnv;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakAfter;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakCommands;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakCondition;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakDelete;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakDisable;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakEnable;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakInsert;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakList;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakPasscount;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakWatch;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataDisassemble;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataEvaluateExpression;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataListRegisterNames;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataListRegisterValues;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataReadMemory;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataWriteMemory;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIEnvironmentCD;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIEnvironmentDirectory;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecContinue;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecFinish;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecInterrupt;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecJump;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecNext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecNextInstruction;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecReturn;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecReverseContinue;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecReverseNext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecReverseNextInstruction;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecReverseStep;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecReverseStepInstruction;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecRun;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecStep;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecStepInstruction;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecUncall;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecUntil;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIFileExecAndSymbols;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIFileExecFile;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIFileSymbolFile;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBExit;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSet;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetArgs;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetAutoSolib;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetEnv;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetNonStop;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetPagination;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetSolibAbsolutePrefix;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetSolibSearchPath;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetTargetAsync;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBShowExitCode;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIInferiorTTYSet;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIInterpreterExec;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIInterpreterExecConsole;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIListThreadGroups;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIStackInfoDepth;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIStackListArguments;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIStackListFrames;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIStackListLocals;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIStackSelectFrame;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITargetAttach;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITargetDetach;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITargetDownload;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITargetSelect;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITargetSelectCore;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITargetSelectTFile;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIThreadInfo;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIThreadListIds;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIThreadSelect;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITraceDefineVariable;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITraceFind;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITraceFindFrameNumber;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITraceFindNone;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITraceListVariables;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITraceSave;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITraceStart;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITraceStatus;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITraceStop;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIVarAssign;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIVarCreate;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIVarDelete;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIVarEvaluateExpression;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIVarInfoExpression;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIVarInfoNumChildren;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIVarInfoPathExpression;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIVarInfoType;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIVarListChildren;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIVarSetFormat;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIVarShowAttributes;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIVarShowFormat;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIVarUpdate;
import org.eclipse.cdt.dsf.mi.service.command.output.CLICatchInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIInfoProgramInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIInfoSharedLibraryInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIInfoThreadsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIThreadInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.CLITraceInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakListInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataDisassembleInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataListRegisterNamesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataListRegisterValuesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataReadMemoryInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataWriteMemoryInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGDBShowExitCodeInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStackInfoDepthInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStackListArgumentsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStackListFramesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStackListLocalsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MITargetDownloadInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIThreadInfoInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIThreadListIdsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MITraceFindInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MITraceListVariablesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MITraceStatusInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MITraceStopInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarAssignInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarCreateInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarDeleteInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarEvaluateExpressionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarInfoExpressionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarInfoNumChildrenInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarInfoPathExpressionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarInfoTypeInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarListChildrenInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarSetFormatInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarShowAttributesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarShowFormatInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarUpdateInfo;


/**
 * Factory to create MI/CLI commands.
 * 
 * @since 3.0
 */
public class CommandFactory {

	public ICommand<MIInfo> createCLIAttach(IDMContext ctx, int pid) {
		return new CLIAttach(ctx, pid);
	}

	public ICommand<MIInfo> createCLIAttach(ICommandControlDMContext ctx, String pid) {
		return new CLIAttach(ctx, pid);
	}

	public ICommand<CLICatchInfo> createCLICatch(IBreakpointsTargetDMContext ctx, String event, String[] args) {
		return new CLICatch(ctx, event, args);
	}
	
	public ICommand<MIInfo> createCLIDetach(IDMContext ctx) {
		return new CLIDetach(ctx);
	}

	public ICommand<MIInfo> createCLIExecAbort(ICommandControlDMContext ctx) {
		return new CLIExecAbort(ctx);
	}

	public ICommand<CLIInfoProgramInfo> createCLIInfoProgram(IContainerDMContext ctx) {
		return new CLIInfoProgram(ctx);
	}

	public ICommand<CLIInfoSharedLibraryInfo> createCLIInfoSharedLibrary(ISymbolDMContext ctx) {
		return new CLIInfoSharedLibrary(ctx);
	}

	public ICommand<CLIInfoSharedLibraryInfo> createCLIInfoSharedLibrary(IModuleDMContext ctx) {
		return new CLIInfoSharedLibrary(ctx);
	}

	public ICommand<CLIInfoThreadsInfo> createCLIInfoThreads(IContainerDMContext ctx) {
		return new CLIInfoThreads(ctx);
	}

	public ICommand<MIInfo> createCLIJump(IExecutionDMContext ctx, String location) {
		return new CLIJump(ctx, location);
	}

	public ICommand<MIInfo> createCLIPasscount(IBreakpointsTargetDMContext ctx, int breakpoint, int passcount) {
		return new CLIPasscount(ctx, breakpoint, passcount);
	}

	public ICommand<MIInfo> createCLIRecord(ICommandControlDMContext ctx, boolean enable) {
		return new CLIRecord(ctx, enable);
	}

	public ICommand<MIInfo> createCLISource(ICommandControlDMContext ctx, String file) {
		return new CLISource(ctx, file);
	}

	public ICommand<CLIThreadInfo> createCLIThread(IContainerDMContext ctx) {
		return new CLIThread(ctx);
	}

	public ICommand<CLITraceInfo> createCLITrace(IBreakpointsTargetDMContext ctx, String location) {
		return new CLITrace(ctx, location);
	}

	public ICommand<CLITraceInfo> createCLITrace(IBreakpointsTargetDMContext ctx, String location, String condition) {
		return new CLITrace(ctx, location, condition);
	}

	public ICommand<MIInfo> createCLIUnsetEnv(ICommandControlDMContext ctx) {
		return new CLIUnsetEnv(ctx);
	}

	public ICommand<MIInfo> createCLIUnsetEnv(ICommandControlDMContext ctx, String name) {
		return new CLIUnsetEnv(ctx, name);
	}

	public ICommand<MIInfo> createMIBreakAfter(IBreakpointsTargetDMContext ctx, int breakpoint, int ignoreCount) {
		return new MIBreakAfter(ctx, breakpoint, ignoreCount);
	}

	public ICommand<MIInfo> createMIBreakCommands(IBreakpointsTargetDMContext ctx, int breakpoint, String[] commands) {
		return new MIBreakCommands(ctx, breakpoint, commands);
	}
	
	public ICommand<MIInfo> createMIBreakCondition(IBreakpointsTargetDMContext ctx, int breakpoint, String condition) {
		return new MIBreakCondition(ctx, breakpoint, condition);
	}

	public ICommand<MIInfo> createMIBreakDelete(IBreakpointsTargetDMContext ctx, int[] array) {
		return new MIBreakDelete(ctx, array);
	}

	public ICommand<MIInfo> createMIBreakDisable(IBreakpointsTargetDMContext ctx, int[] array) {
		return new MIBreakDisable(ctx, array);
	}

	public ICommand<MIInfo> createMIBreakEnable(IBreakpointsTargetDMContext ctx, int[] array) {
		return new MIBreakEnable(ctx, array);
	}

	public ICommand<MIBreakInsertInfo> createMIBreakInsert(IBreakpointsTargetDMContext ctx, String func) {
		return new MIBreakInsert(ctx, func);
	}

	public ICommand<MIBreakInsertInfo> createMIBreakInsert(IBreakpointsTargetDMContext ctx, boolean isTemporary, 
			boolean isHardware, String condition, int ignoreCount,
			String line, int tid) {
		return new MIBreakInsert(ctx, isTemporary, isHardware, condition, ignoreCount, line, tid);
	}

	public ICommand<MIBreakInsertInfo> createMIBreakInsert(IBreakpointsTargetDMContext ctx, boolean isTemporary,
			boolean isHardware, String condition, int ignoreCount, 
			String location, int tid, boolean disabled, boolean isTracepoint) {
		return new MIBreakInsert(ctx, isTemporary, isHardware, condition, ignoreCount, location, tid, disabled, isTracepoint);
	}

	public ICommand<MIBreakListInfo> createMIBreakList(IBreakpointsTargetDMContext ctx) {
		return new MIBreakList(ctx);
	}

	public ICommand<MIInfo> createMIBreakPasscount(IBreakpointsTargetDMContext ctx, int tracepoint, int passCount) {
		return new MIBreakPasscount(ctx, tracepoint, passCount);
	}

	public ICommand<MIBreakInsertInfo> createMIBreakWatch(IBreakpointsTargetDMContext ctx, boolean isRead, boolean isWrite, String expression) {
		return new MIBreakWatch(ctx, isRead, isWrite, expression);
	}

	public ICommand<MIDataDisassembleInfo> createMIDataDisassemble(IDisassemblyDMContext ctx, String start, String end, boolean mode) {
		return new MIDataDisassemble(ctx, start, end, mode);
	}

	public ICommand<MIDataDisassembleInfo> createMIDataDisassemble(IDisassemblyDMContext ctx, String file, int linenum, int lines, boolean mode) {
		return new MIDataDisassemble(ctx, file, linenum, lines, mode);
	}

	public ICommand<MIDataEvaluateExpressionInfo> createMIDataEvaluateExpression(ICommandControlDMContext ctx, String expr) {
		return new MIDataEvaluateExpression<MIDataEvaluateExpressionInfo>(ctx, expr);
	}

	public ICommand<MIDataEvaluateExpressionInfo> createMIDataEvaluateExpression(IMIExecutionDMContext execDmc, String expr) {
		return new MIDataEvaluateExpression<MIDataEvaluateExpressionInfo>(execDmc, expr);
	}

	public ICommand<MIDataEvaluateExpressionInfo> createMIDataEvaluateExpression(IFrameDMContext frameDmc, String expr) {
		return new MIDataEvaluateExpression<MIDataEvaluateExpressionInfo>(frameDmc, expr);
	}

	public ICommand<MIDataEvaluateExpressionInfo> createMIDataEvaluateExpression(IExpressionDMContext exprDmc) {
		return new MIDataEvaluateExpression<MIDataEvaluateExpressionInfo>(exprDmc);
	}

	public ICommand<MIDataListRegisterNamesInfo> createMIDataListRegisterNames(IContainerDMContext ctx) {
		return new MIDataListRegisterNames(ctx);
	}

	public ICommand<MIDataListRegisterNamesInfo> createMIDataListRegisterNames(IContainerDMContext ctx, int [] regnos) {
		return new MIDataListRegisterNames(ctx, regnos);
	}

	public ICommand<MIDataListRegisterValuesInfo> createMIDataListRegisterValues(IMIExecutionDMContext ctx, int fmt) {
		return new MIDataListRegisterValues(ctx, fmt);
	}

	public ICommand<MIDataListRegisterValuesInfo> createMIDataListRegisterValues(IMIExecutionDMContext ctx, int fmt, int [] regnos) {
		return new MIDataListRegisterValues(ctx, fmt, regnos);
	}

	public ICommand<MIDataReadMemoryInfo> createMIDataReadMemory(IDMContext ctx, long offset, String address, 
			int word_format, int word_size, int rows, int cols,
			Character asChar) {
		return new MIDataReadMemory(ctx, offset, address, word_format, word_size, rows, cols, asChar);
	}

	public ICommand<MIDataWriteMemoryInfo> createMIDataWriteMemory(IDMContext ctx, long offset, String address, 
			int wordFormat, int wordSize, String value) {
		return new MIDataWriteMemory(ctx, offset, address, wordFormat, wordSize, value);
	}

	public ICommand<MIInfo> createMIEnvironmentCD(ICommandControlDMContext ctx, String path) {
		return new MIEnvironmentCD(ctx, path);
	}

	public ICommand<MIInfo> createMIEnvironmentDirectory(IDMContext ctx, String[] paths, boolean reset) {
		return new MIEnvironmentDirectory(ctx, paths, reset);
	}

	public ICommand<MIInfo> createMIExecContinue(IExecutionDMContext dmc) {
		return new MIExecContinue(dmc);
	}

	public ICommand<MIInfo> createMIExecContinue(IExecutionDMContext dmc, boolean allThreads) {
		return new MIExecContinue(dmc, allThreads);
	}

	public ICommand<MIInfo> createMIExecContinue(IExecutionDMContext dmc, String groupId) {
		return new MIExecContinue(dmc, groupId);
	}

	public ICommand<MIInfo> createMIExecFinish(IFrameDMContext dmc) {
		return new MIExecFinish(dmc);
	}

	public ICommand<MIInfo> createMIExecInterrupt(IExecutionDMContext dmc) {
		return new MIExecInterrupt(dmc);
	}

	public ICommand<MIInfo> createMIExecInterrupt(IExecutionDMContext dmc, boolean allThreads) {
		return new MIExecInterrupt(dmc, allThreads);
	}

	public ICommand<MIInfo> createMIExecInterrupt(IExecutionDMContext dmc, String groupId) {
		return new MIExecInterrupt(dmc, groupId);
	}

	public ICommand<MIInfo> createMIExecJump(IExecutionDMContext ctx, String location) {
		return new MIExecJump(ctx, location);
	}

	public ICommand<MIInfo> createMIExecNext(IExecutionDMContext dmc) {
		return new MIExecNext(dmc);
	}
	
	public ICommand<MIInfo> createMIExecNext(IExecutionDMContext dmc, int count) {
		return new MIExecNext(dmc, count);
	}

	public ICommand<MIInfo> createMIExecNextInstruction(IExecutionDMContext dmc) {
		return new MIExecNextInstruction(dmc);
	}

	public ICommand<MIInfo> createMIExecNextInstruction(IExecutionDMContext dmc, int count) {
		return new MIExecNextInstruction(dmc, count);
	}

	public ICommand<MIInfo> createMIExecReturn(IFrameDMContext dmc) {
		return new MIExecReturn(dmc);
	}

	public ICommand<MIInfo> createMIExecReturn(IFrameDMContext dmc, String arg) {
		return new MIExecReturn(dmc, arg);
	}

	public ICommand<MIInfo> createMIExecReverseContinue(IExecutionDMContext dmc) {
		return new MIExecReverseContinue(dmc);
	}

	public ICommand<MIInfo> createMIExecReverseNext(IExecutionDMContext dmc) {
		return new MIExecReverseNext(dmc);
	}
	
	public ICommand<MIInfo> createMIExecReverseNext(IExecutionDMContext dmc, int count) {
		return new MIExecReverseNext(dmc, count);
	}
	
	public ICommand<MIInfo> createMIExecReverseNextInstruction(IExecutionDMContext dmc) {
		return new MIExecReverseNextInstruction(dmc);
	}

	public ICommand<MIInfo> createMIExecReverseNextInstruction(IExecutionDMContext dmc, int count) {
		return new MIExecReverseNextInstruction(dmc, count);
	}

	public ICommand<MIInfo> createMIExecReverseStep(IExecutionDMContext dmc) {
		return new MIExecReverseStep(dmc);
	}
	
	public ICommand<MIInfo> createMIExecReverseStep(IExecutionDMContext dmc, int count) {
		return new MIExecReverseStep(dmc, count);
	}
	
	public ICommand<MIInfo> createMIExecReverseStepInstruction(IExecutionDMContext dmc) {
		return new MIExecReverseStepInstruction(dmc);
	}

	public ICommand<MIInfo> createMIExecReverseStepInstruction(IExecutionDMContext dmc, int count) {
		return new MIExecReverseStepInstruction(dmc, count);
	}

	public ICommand<MIInfo> createMIExecRun(IExecutionDMContext dmc) {
		return new MIExecRun(dmc);
	}

	public ICommand<MIInfo> createMIExecRun(IExecutionDMContext dmc, String[] args) {
		return new MIExecRun(dmc, args);
	}

	public ICommand<MIInfo> createMIExecStep(IExecutionDMContext dmc) {
		return new MIExecStep(dmc);
	}
	
	public ICommand<MIInfo> createMIExecStep(IExecutionDMContext dmc, int count) {
		return new MIExecStep(dmc, count);
	}
	
	public ICommand<MIInfo> createMIExecStepInstruction(IExecutionDMContext dmc) {
		return new MIExecStepInstruction(dmc);
	}

	public ICommand<MIInfo> createMIExecStepInstruction(IExecutionDMContext dmc, int count) {
		return new MIExecStepInstruction(dmc, count);
	}

	public ICommand<MIInfo> createMIExecUncall(IFrameDMContext dmc) {
		return new MIExecUncall(dmc);
	}

	public ICommand<MIInfo> createMIExecUntil(IExecutionDMContext dmc) {
		return new MIExecUntil(dmc);
	}

	public ICommand<MIInfo> createMIExecUntil(IExecutionDMContext dmc, String loc) {
		return new MIExecUntil(dmc, loc);
	}

	public ICommand<MIInfo> createMIFileExecAndSymbols(ICommandControlDMContext dmc, String file) {
		return new MIFileExecAndSymbols(dmc, file);
	}

	public ICommand<MIInfo> createMIFileExecAndSymbols(ICommandControlDMContext dmc) {
		return new MIFileExecAndSymbols(dmc);
	}

	public ICommand<MIInfo> createMIFileExecFile(ICommandControlDMContext dmc, String file) {
		return new MIFileExecFile(dmc, file);
	}

	public ICommand<MIInfo> createMIFileExecFile(ICommandControlDMContext dmc) {
		return new MIFileExecFile(dmc);
	}

	public ICommand<MIInfo> createMIFileSymbolFile(ICommandControlDMContext dmc, String file) {
		return new MIFileSymbolFile(dmc, file);
	}

	public ICommand<MIInfo> createMIFileSymbolFile(ICommandControlDMContext dmc) {
		return new MIFileSymbolFile(dmc);
	}

	public ICommand<MIInfo> createMIGDBExit(IDMContext ctx) {
		return new MIGDBExit(ctx);
	}

	public ICommand<MIInfo> createMIGDBSet(IDMContext ctx, String[] params) {
		return new MIGDBSet(ctx, params);
	}

	public ICommand<MIInfo> createMIGDBSetArgs(ICommandControlDMContext dmc) {
		return new MIGDBSetArgs(dmc);
	}

	public ICommand<MIInfo> createMIGDBSetArgs(ICommandControlDMContext dmc, String arguments) {
		return new MIGDBSetArgs(dmc, arguments);
	}

	public ICommand<MIInfo> createMIGDBSetAutoSolib(ICommandControlDMContext ctx, boolean isSet) {
		return new MIGDBSetAutoSolib(ctx, isSet);
	}

	public ICommand<MIInfo> createMIGDBSetEnv(ICommandControlDMContext dmc, String name) {
		return new MIGDBSetEnv(dmc, name);
	}

	public ICommand<MIInfo> createMIGDBSetEnv(ICommandControlDMContext dmc, String name, String value) {
		return new MIGDBSetEnv(dmc, name, value);
	}

	public ICommand<MIInfo> createMIGDBSetNonStop(ICommandControlDMContext ctx, boolean isSet) {
		return new MIGDBSetNonStop(ctx, isSet);
	}

	public ICommand<MIInfo> createMIGDBSetPagination(ICommandControlDMContext ctx, boolean isSet) {
		return new MIGDBSetPagination(ctx, isSet);
	}

	public ICommand<MIInfo> createMIGDBSetSolibAbsolutePrefix(ICommandControlDMContext ctx, String prefix) {
		return new MIGDBSetSolibAbsolutePrefix(ctx, prefix);
	}
	
	public ICommand<MIInfo> createMIGDBSetSolibSearchPath(ICommandControlDMContext ctx, String[] paths) {
		return new MIGDBSetSolibSearchPath(ctx, paths);
	}

	public ICommand<MIInfo> createMIGDBSetTargetAsync(ICommandControlDMContext ctx, boolean isSet) {
		return new MIGDBSetTargetAsync(ctx, isSet);
	}

	public ICommand<MIGDBShowExitCodeInfo> createMIGDBShowExitCode(ICommandControlDMContext ctx) {
		return new MIGDBShowExitCode(ctx);
	}

	public ICommand<MIInfo> createMIInferiorTTYSet(ICommandControlDMContext dmc, String tty) {
		return new MIInferiorTTYSet(dmc, tty);
	}

	public ICommand<MIInfo> createMIInterpreterExec(IDMContext ctx, String interpreter, String cmd) {
		return new MIInterpreterExec<MIInfo>(ctx, interpreter, cmd);
	}
	
	public ICommand<MIInfo> createMIInterpreterExecConsole(IDMContext ctx, String cmd) {
		return new MIInterpreterExecConsole<MIInfo>(ctx, cmd);
	}

	public ICommand<MIListThreadGroupsInfo> createMIListThreadGroups(ICommandControlDMContext ctx) {
		return new MIListThreadGroups(ctx);
	}

	public ICommand<MIListThreadGroupsInfo> createMIListThreadGroups(ICommandControlDMContext ctx, String groupId) {
		return new MIListThreadGroups(ctx, groupId);
	}

	public ICommand<MIListThreadGroupsInfo> createMIListThreadGroups(ICommandControlDMContext ctx, boolean listAll) {
		return new MIListThreadGroups(ctx, listAll);
	}

	public ICommand<MIStackInfoDepthInfo> createMIStackInfoDepth(IMIExecutionDMContext ctx) {
		return new MIStackInfoDepth(ctx);
	}

	public ICommand<MIStackInfoDepthInfo> createMIStackInfoDepth(IMIExecutionDMContext ctx, int maxDepth) {
		return new MIStackInfoDepth(ctx, maxDepth);
	}

	public ICommand<MIStackListArgumentsInfo> createMIStackListArguments(IMIExecutionDMContext execDmc, boolean showValues) {
		return new MIStackListArguments(execDmc, showValues);
	}

	public ICommand<MIStackListArgumentsInfo> createMIStackListArguments(IFrameDMContext frameDmc, boolean showValues) {
		return new MIStackListArguments(frameDmc, showValues);
	}

	public ICommand<MIStackListArgumentsInfo> createMIStackListArguments(IMIExecutionDMContext execDmc, boolean showValues, int low, int high) {
		return new MIStackListArguments(execDmc, showValues, low, high);
	}

	public ICommand<MIStackListFramesInfo> createMIStackListFrames(IMIExecutionDMContext execDmc) {
		return new MIStackListFrames(execDmc);
	}

	public ICommand<MIStackListFramesInfo> createMIStackListFrames(IMIExecutionDMContext execDmc, int low, int high) {
		return new MIStackListFrames(execDmc, low, high);
	}

	public ICommand<MIStackListLocalsInfo> createMIStackListLocals(IFrameDMContext frameCtx, boolean printValues) {
		return new MIStackListLocals(frameCtx, printValues);
	}

	public ICommand<MIInfo> createMIStackSelectFrame(IDMContext ctx, int frameNum) {
		return new MIStackSelectFrame(ctx, frameNum);
	}

	public ICommand<MIInfo> createMITargetAttach(ICommandControlDMContext ctx, String groupId) {
		return new MITargetAttach(ctx, groupId);
	}

	public ICommand<MIInfo> createMITargetDetach(ICommandControlDMContext ctx, String groupId) {
		return new MITargetDetach(ctx, groupId);
	}

    public ICommand<MIInfo> createMITargetSelect(IDMContext ctx, String[] params) {
        return new MITargetSelect(ctx, params);
    }

    public ICommand<MIInfo> createMITargetSelect(IDMContext ctx, String host, String port, boolean extended) {
        return new MITargetSelect(ctx, host, port, extended);
    }

	public ICommand<MIInfo> createMITargetSelect(IDMContext ctx, String serialDevice, boolean extended) {
		return new MITargetSelect(ctx, serialDevice, extended);
	}

	public ICommand<MIInfo> createMITargetSelectCore(IDMContext ctx, String coreFilePath) {
		return new MITargetSelectCore(ctx, coreFilePath);
	}

	public ICommand<MIInfo> createMITargetSelectTFile(IDMContext ctx, String traceFilePath) {
		return new MITargetSelectTFile(ctx, traceFilePath);
	}

    public ICommand<MITargetDownloadInfo> createMITargetDownload(ICommandControlDMContext ctx) {
        return new MITargetDownload(ctx);
    }

    public ICommand<MITargetDownloadInfo> createMITargetDownload(ICommandControlDMContext ctx, String file) {
        return new MITargetDownload(ctx, file);
    }

	public ICommand<MIThreadInfoInfo> createMIThreadInfo(ICommandControlDMContext dmc) {
		return new MIThreadInfo(dmc);
	}

	public ICommand<MIThreadInfoInfo> createMIThreadInfo(ICommandControlDMContext dmc, String threadId) {
		return new MIThreadInfo(dmc, threadId);
	}

	public ICommand<MIThreadListIdsInfo> createMIThreadListIds(IContainerDMContext contDmc) {
		return new MIThreadListIds(contDmc);
	}

	public ICommand<MIInfo> createMIThreadSelect(IDMContext ctx, int threadNum) {
		return new MIThreadSelect(ctx, threadNum);
	}

	public ICommand<MIInfo> createMIThreadSelect(IDMContext ctx, String threadNum) {
		return new MIThreadSelect(ctx, threadNum);
	}

	public ICommand<MIInfo> createMITraceDefineVariable(ITraceTargetDMContext ctx, String varName) {
		return new MITraceDefineVariable(ctx, varName);
	}

	public ICommand<MIInfo> createMITraceDefineVariable(ITraceTargetDMContext ctx, String varName, String varValue) {
		return new MITraceDefineVariable(ctx, varName, varValue);
	}

	public ICommand<MITraceFindInfo> createMITraceFind(ITraceTargetDMContext ctx, String[] params) {
		return new MITraceFind(ctx, params);
	}
	public ICommand<MITraceFindInfo> createMITraceFindFrameNumber(ITraceTargetDMContext ctx, int frameReference) {
		return new MITraceFindFrameNumber(ctx, frameReference);
	}
	public ICommand<MITraceFindInfo> createMITraceFindNone(ITraceTargetDMContext ctx) {
		return new MITraceFindNone(ctx);
	}

	public ICommand<MITraceListVariablesInfo> createMITraceListVariables(ITraceTargetDMContext ctx) {
		return new MITraceListVariables(ctx);
	}

	public ICommand<MIInfo> createMITraceSave(ITraceTargetDMContext ctx, String file, boolean remoteSave) {
		return new MITraceSave(ctx, file, remoteSave);
	}

	public ICommand<MIInfo> createMITraceStart(ITraceTargetDMContext ctx) {
		return new MITraceStart(ctx);
	}

	public ICommand<MITraceStatusInfo> createMITraceStatus(ITraceTargetDMContext ctx) {
		return new MITraceStatus(ctx);
	}

	public ICommand<MITraceStopInfo> createMITraceStop(ITraceTargetDMContext ctx) {
		return new MITraceStop(ctx);
	}

	public ICommand<MIVarAssignInfo> createMIVarAssign(ICommandControlDMContext ctx, String name, String expression) {
		return new MIVarAssign(ctx, name, expression);
	}

	public ICommand<MIVarCreateInfo> createMIVarCreate(IExpressionDMContext dmc, String expression) {
		return new MIVarCreate(dmc, expression);
	}

	public ICommand<MIVarCreateInfo> createMIVarCreate(IExpressionDMContext dmc, String name, String expression) {
		return new MIVarCreate(dmc, name, expression);
	}

	public ICommand<MIVarCreateInfo> createMIVarCreate(IExpressionDMContext dmc, String name, String frameAddr, String expression) {
		return new MIVarCreate(dmc, name, frameAddr, expression);
	}

	public ICommand<MIVarDeleteInfo> createMIVarDelete(ICommandControlDMContext dmc, String name) {
		return new MIVarDelete(dmc, name);
	}

	public ICommand<MIVarEvaluateExpressionInfo> createMIVarEvaluateExpression(ICommandControlDMContext dmc, String name) {
		return new MIVarEvaluateExpression(dmc, name);
	}

	public ICommand<MIVarInfoExpressionInfo> createMIVarInfoExpression(ICommandControlDMContext ctx, String name) {
		return new MIVarInfoExpression(ctx, name);
	}

	public ICommand<MIVarInfoNumChildrenInfo> createMIVarInfoNumChildren(IExpressionDMContext ctx, String name) {
		return new MIVarInfoNumChildren(ctx, name);
	}

	public ICommand<MIVarInfoPathExpressionInfo> createMIVarInfoPathExpression(ICommandControlDMContext dmc, String name) {
		return new MIVarInfoPathExpression(dmc, name);
	}

	public ICommand<MIVarInfoTypeInfo> createMIVarInfoType(ICommandControlDMContext ctx, String name) {
		return new MIVarInfoType(ctx, name);
	}

	public ICommand<MIVarListChildrenInfo> createMIVarListChildren(ICommandControlDMContext ctx, String name) {
		return new MIVarListChildren(ctx, name);
	}

	public ICommand<MIVarSetFormatInfo> createMIVarSetFormat(ICommandControlDMContext ctx, String name, String fmt) {
		return new MIVarSetFormat(ctx, name, fmt);
	}

	public ICommand<MIVarShowAttributesInfo> createMIVarShowAttributes(ICommandControlDMContext ctx, String name) {
		return new MIVarShowAttributes(ctx, name);
	}

	public ICommand<MIVarShowFormatInfo> createMIVarShowFormat(ICommandControlDMContext ctx, String name) {
		return new MIVarShowFormat(ctx, name);
	}

	public ICommand<MIVarUpdateInfo> createMIVarUpdate(ICommandControlDMContext dmc, String name) {
		return new MIVarUpdate(dmc, name);
	}
}
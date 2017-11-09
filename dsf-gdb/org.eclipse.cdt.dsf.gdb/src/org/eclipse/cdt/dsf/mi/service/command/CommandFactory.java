/*******************************************************************************
 * Copyright (c) 2000, 2017 QNX Software Systems and others.
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
 *     Vladimir Prus (CodeSourcery) - Support for -data-read-memory-bytes (bug 322658)
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *     Onur Akdemir (TUBITAK BILGEM-ITI) - Multi-process debugging (Bug 237306)
 *     Abeer Bagul - Support for -exec-arguments (bug 337687)
 *     Marc Khouzam (Ericsson) - New methods for new MIDataDisassemble (Bug 357073)
 *     Marc Khouzam (Ericsson) - New method for new MIGDBSetPythonPrintStack (Bug 367788)
 *     Mathias Kunter - New methods for handling different charsets (Bug 370462)
 *     Anton Gorenkov - A preference to use RTTI for variable types determination (Bug 377536)
 *     Vladimir Prus (Mentor Graphics) - Support for -info-os (Bug 360314)
 *     John Dallaway - Support for -data-write-memory-bytes (Bug 387793)
 *     Alvaro Sanchez-Leon (Ericsson) - Make Registers View specific to a frame (Bug (323552)
 *     Philippe Gil (AdaCore) - Add show/set language CLI commands (Bug 421541)
 *     Dmitry Kozlov (Mentor Graphics) - New trace-related methods (Bug 390827)
 *     Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Support 16 bit addressable size (Bug 426730)
 *     Marc Khouzam (Ericsson) - Support for dynamic printf (Bug 400638)
 *     Marc Khouzam (Ericsson) - Support for -gdb-version (Bug 455408)
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command;

import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.ReverseDebugMethod;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.IModuleDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIAddressableSize;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIAttach;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLICatch;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIDetach;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIExecAbort;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIInferior;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIInfoBreak;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIInfoProgram;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIInfoRecord;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIInfoSharedLibrary;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIInfoThreads;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIJump;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIMaintenance;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIPasscount;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIRecord;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIRemoteGet;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLISharedLibrary;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIShowEndian;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLISource;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIThread;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLITrace;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLITraceDump;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIUnsetEnv;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIUnsetSubstitutePath;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIAddInferior;
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
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDPrintfInsert;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataDisassemble;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataEvaluateExpression;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataListRegisterNames;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataListRegisterValues;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataReadMemory;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataReadMemoryBytes;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataWriteMemory;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataWriteMemoryBytes;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIEnablePrettyPrinting;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIEnvironmentCD;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIEnvironmentDirectory;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecArguments;
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
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetBreakpointPending;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetCharset;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetCircularTraceBuffer;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetDPrintfStyle;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetDetachOnFork;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetDisconnectedTracing;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetEnv;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetHostCharset;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetLanguage;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetNewConsole;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetNonStop;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetPagination;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetPrintObject;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetPrintSevenbitStrings;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetPythonPrintStack;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetRecordFullStopAtLimit;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetSchedulerLocking;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetSolibAbsolutePrefix;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetSolibSearchPath;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetTargetAsync;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetTargetCharset;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetTargetWideCharset;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetTraceNotes;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetTraceUser;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBShowExitCode;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBShowLanguage;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBShowNewConsole;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBVersion;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIInferiorTTYSet;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIInfoOs;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIInterpreterExec;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIInterpreterExecConsole;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIInterpreterExecConsoleKill;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIListFeatures;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIListThreadGroups;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIRemoveInferior;
import org.eclipse.cdt.dsf.mi.service.command.commands.MISetSubstitutePath;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIStackInfoDepth;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIStackListArguments;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIStackListFrames;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIStackListLocals;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIStackSelectFrame;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITargetAttach;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITargetDetach;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITargetDisconnect;
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
import org.eclipse.cdt.dsf.mi.service.command.commands.MIVarSetUpdateRange;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIVarShowAttributes;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIVarShowFormat;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIVarUpdate;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIAddressableSizeInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.CLICatchInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIInfoBreakInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIInfoProgramInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIInfoRecordInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIInfoSharedLibraryInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIInfoThreadsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIShowEndianInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIThreadInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.CLITraceDumpInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.CLITraceInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIAddInferiorInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakListInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataDisassembleInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataListRegisterNamesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataListRegisterValuesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataReadMemoryBytesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataReadMemoryInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataWriteMemoryInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGDBShowExitCodeInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGDBShowLanguageInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGDBShowNewConsoleInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGDBVersionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfoOsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListFeaturesInfo;
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

	/**
	 * @since 4.4
	 */
	public ICommand<CLIAddressableSizeInfo> createCLIAddressableSize(IMemoryDMContext ctx) {
		return new CLIAddressableSize(ctx);
	}

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

	/** @since 5.2 */
	public ICommand<MIInfo> createCLIInferior(ICommandControlDMContext ctx, String inferiorId) {
		return new CLIInferior(ctx, inferiorId);
	}

	/** @since 4.2 */
	public ICommand<CLIInfoBreakInfo> createCLIInfoBreak(IDMContext ctx) {
		return new CLIInfoBreak(ctx);
	}
	
	/** @since 4.2 */
	public ICommand<CLIInfoBreakInfo> createCLIInfoBreak(IDMContext ctx, int bpRef) {
		return new CLIInfoBreak(ctx, bpRef);
	}

	public ICommand<CLIInfoProgramInfo> createCLIInfoProgram(IContainerDMContext ctx) {
		return new CLIInfoProgram(ctx);
	}

	/** @since 5.0*/
	public ICommand<CLIInfoRecordInfo> createCLIInfoRecord(ICommandControlDMContext ctx) {
		return new CLIInfoRecord(ctx);
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

	/** @since 4.0 */
	public ICommand<MIInfo> createCLIMaintenance(ICommandControlDMContext ctx, String subCommand) {
		return new CLIMaintenance(ctx, subCommand);
	}

	/** @since 5.0 */
	public ICommand<MIInfo> createCLIPasscount(IBreakpointsTargetDMContext ctx, String breakpoint, int passcount) {
		return new CLIPasscount(ctx, breakpoint, passcount);
	}

	public ICommand<MIInfo> createCLIRecord(ICommandControlDMContext ctx, boolean enable) {
		return new CLIRecord(ctx, enable);
	}

	/** @since 5.0*/
	public ICommand<MIInfo> createCLIRecord(ICommandControlDMContext ctx, ReverseDebugMethod traceMethod) {
		return new CLIRecord(ctx, traceMethod);
	}

	/** @since 4.1 */
	public ICommand<MIInfo> createCLIRemoteGet(ICommandControlDMContext ctx, String remoteFile, String localFile) {
		return new CLIRemoteGet(ctx, remoteFile, localFile);
	}

	/** @since 4.6 */
	public ICommand<MIInfo> createCLISharedLibrary(ISymbolDMContext ctx) {
		return new CLISharedLibrary(ctx);
	}

	/** @since 4.6 */
	public ICommand<MIInfo> createCLISharedLibrary(ISymbolDMContext ctx, String name) {
		return new CLISharedLibrary(ctx, name);
	}

	/**
	 * @since 4.2
	 */
	public ICommand<CLIShowEndianInfo> createCLIShowEndian(IMemoryDMContext ctx) {
		return new CLIShowEndian(ctx);
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

	/** @since 4.0 */
	public ICommand<CLITraceDumpInfo> createCLITraceDump(ITraceRecordDMContext ctx) {
		return new CLITraceDump(ctx);
	}

	public ICommand<MIInfo> createCLIUnsetEnv(ICommandControlDMContext ctx) {
		return new CLIUnsetEnv(ctx);
	}

	public ICommand<MIInfo> createCLIUnsetEnv(ICommandControlDMContext ctx, String name) {
		return new CLIUnsetEnv(ctx, name);
	}

	/** @since 5.0 */
	public ICommand<MIInfo> createCLIUnsetSubstitutePath(ISourceLookupDMContext ctx) {
		return new CLIUnsetSubstitutePath(ctx);
	}

	/** @since 4.0 */
	public ICommand<MIAddInferiorInfo> createMIAddInferior(ICommandControlDMContext ctx) {
		return new MIAddInferior(ctx);
	}
	
	/** @since 5.0 */
	public ICommand<MIInfo> createMIBreakAfter(IBreakpointsTargetDMContext ctx, String breakpoint, int ignoreCount) {
		return new MIBreakAfter(ctx, breakpoint, ignoreCount);
	}

	/** @since 5.0 */
	public ICommand<MIInfo> createMIBreakCommands(IBreakpointsTargetDMContext ctx, String breakpoint, String[] commands) {
		return new MIBreakCommands(ctx, breakpoint, commands);
	}
	
	/** @since 5.0 */
	public ICommand<MIInfo> createMIBreakCondition(IBreakpointsTargetDMContext ctx, String breakpoint, String condition) {
		return new MIBreakCondition(ctx, breakpoint, condition);
	}

	/** @since 5.0 */
	public ICommand<MIInfo> createMIBreakDelete(IBreakpointsTargetDMContext ctx, String[] array) {
		return new MIBreakDelete(ctx, array);
	}

	/** @since 5.0 */
	public ICommand<MIInfo> createMIBreakDisable(IBreakpointsTargetDMContext ctx, String[] array) {
		return new MIBreakDisable(ctx, array);
	}

	/** @since 5.0 */
	public ICommand<MIInfo> createMIBreakEnable(IBreakpointsTargetDMContext ctx, String[] array) {
		return new MIBreakEnable(ctx, array);
	}

	public ICommand<MIBreakInsertInfo> createMIBreakInsert(IBreakpointsTargetDMContext ctx, String func) {
		return new MIBreakInsert(ctx, func, false);
	}

	/**
     * @since 5.0
     */
	public ICommand<MIBreakInsertInfo> createMIBreakInsert(IBreakpointsTargetDMContext ctx, boolean isTemporary, 
			boolean isHardware, String condition, int ignoreCount,
			String line, String tid) {
		return new MIBreakInsert(ctx, isTemporary, isHardware, condition, ignoreCount, line, tid, false);
	}

	/**
     * @since 5.0
     */
	public ICommand<MIBreakInsertInfo> createMIBreakInsert(IBreakpointsTargetDMContext ctx, boolean isTemporary,
			boolean isHardware, String condition, int ignoreCount, 
			String location, String tid, boolean disabled, boolean isTracepoint) {
		return new MIBreakInsert(ctx, isTemporary, isHardware, condition, ignoreCount, location, tid, disabled, isTracepoint, false);
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

	/** @since 4.1 */
	public ICommand<MIDataDisassembleInfo> createMIDataDisassemble(IDisassemblyDMContext ctx, String start, String end, int mode) {
		return new MIDataDisassemble(ctx, start, end, mode);
	}

	public ICommand<MIDataDisassembleInfo> createMIDataDisassemble(IDisassemblyDMContext ctx, String file, int linenum, int lines, boolean mode) {
		return new MIDataDisassemble(ctx, file, linenum, lines, mode);
	}

	/** @since 4.1 */
	public ICommand<MIDataDisassembleInfo> createMIDataDisassemble(IDisassemblyDMContext ctx, String file, int linenum, int lines, int mode) {
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

	/**
	 * @since 4.3
	 */
	public ICommand<MIDataListRegisterValuesInfo> createMIDataListRegisterValues(IFrameDMContext ctx, int fmt) {
		return new MIDataListRegisterValues(ctx, fmt);
	}

	/**
	 * @since 4.3
	 */
	public ICommand<MIDataListRegisterValuesInfo> createMIDataListRegisterValues(IFrameDMContext ctx, int fmt, int [] regnos) {
		return new MIDataListRegisterValues(ctx, fmt, regnos);
	}

	public ICommand<MIDataReadMemoryInfo> createMIDataReadMemory(IDMContext ctx, long offset, String address, 
			int word_format, int word_size, int rows, int cols,
			Character asChar) {
		return new MIDataReadMemory(ctx, offset, address, word_format, word_size, rows, cols, asChar);
	}

	/** @since 4.0 */
	public ICommand<MIDataReadMemoryBytesInfo> createMIDataReadMemoryBytes(IDMContext ctx, String address, 
			long offset, int num_bytes) {
		return new MIDataReadMemoryBytes(ctx, address, offset, num_bytes);
	}

	/**
	 * @since 4.4
	 */
	public ICommand<MIDataReadMemoryBytesInfo> createMIDataReadMemoryBytes(IDMContext ctx, String address, 
			long offset, int word_count, int word_size) {
		return new MIDataReadMemoryBytes(ctx, address, offset, word_count, word_size);
	}
	
	public ICommand<MIDataWriteMemoryInfo> createMIDataWriteMemory(IDMContext ctx, long offset, String address, 
			int wordFormat, int wordSize, String value) {
		return new MIDataWriteMemory(ctx, offset, address, wordFormat, wordSize, value);
	}

	/** @since 4.2 */
	public ICommand<MIInfo> createMIDataWriteMemoryBytes(IDMContext ctx, String address, byte[] contents) {
		return new MIDataWriteMemoryBytes(ctx, address, contents);
	}
	
	/** @since 4.4 */
	public ICommand<MIBreakInsertInfo> createMIDPrintfInsert(IBreakpointsTargetDMContext ctx, boolean isTemporary,
			String condition, int ignoreCount, int tid, boolean disabled, String location, String printfStr) {
		return new MIDPrintfInsert(ctx, isTemporary, condition, ignoreCount, tid, disabled, true, location, printfStr);
	}

	/** @since 4.0 */
	public ICommand<MIInfo> createMIEnablePrettyPrinting(ICommandControlDMContext ctx) {
		return new MIEnablePrettyPrinting(ctx);
	}

	public ICommand<MIInfo> createMIEnvironmentCD(ICommandControlDMContext ctx, String path) {
		return new MIEnvironmentCD(ctx, path);
	}

	public ICommand<MIInfo> createMIEnvironmentDirectory(IDMContext ctx, String[] paths, boolean reset) {
		return new MIEnvironmentDirectory(ctx, paths, reset);
	}

	/** @since 4.0 */
	public ICommand<MIInfo> createMIExecArguments(IMIContainerDMContext ctx, String[] args) {
		return new MIExecArguments(ctx, args);
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

	/**  @since 4.0 */
	public ICommand<MIInfo> createMIFileExecAndSymbols(IMIContainerDMContext dmc, String file) {
		return new MIFileExecAndSymbols(dmc, file);
	}

	/** @since 4.0 */
	public ICommand<MIInfo> createMIFileExecAndSymbols(IMIContainerDMContext dmc) {
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

	/** @since 4.0 */
	public ICommand<MIInfo> createMIGDBSetArgs(IMIContainerDMContext dmc) {
		return new MIGDBSetArgs(dmc);
	}

	/** @since 4.0 */
	public ICommand<MIInfo> createMIGDBSetArgs(IMIContainerDMContext dmc, String[] arguments) {
		return new MIGDBSetArgs(dmc, arguments);
	}
	
	public ICommand<MIInfo> createMIGDBSetAutoSolib(ICommandControlDMContext ctx, boolean isSet) {
		return new MIGDBSetAutoSolib(ctx, isSet);
	}

	/** @since 4.0 */
	public ICommand<MIInfo> createMIGDBSetBreakpointPending(ICommandControlDMContext ctx, boolean enable) {
		return new MIGDBSetBreakpointPending(ctx, enable);
	}

	/** @since 4.1 */
	public ICommand<MIInfo> createMIGDBSetCharset(ICommandControlDMContext ctx, String charset) {
		return new MIGDBSetCharset(ctx, charset);
	}

	/** @since 4.4 */
	public ICommand<MIInfo> createMIGDBSetCircularTraceBuffer(ITraceTargetDMContext ctx, boolean useCircularBuffer) {
		return new MIGDBSetCircularTraceBuffer(ctx, useCircularBuffer);
	}
	
	/** @since 4.0 */
	public ICommand<MIInfo> createMIGDBSetDetachOnFork(ICommandControlDMContext ctx, boolean detach) {
		return new MIGDBSetDetachOnFork(ctx, detach);
	}

	/** @since 4.4 */
	public ICommand<MIInfo> createMIGDBSetDisconnectedTracing(ITraceTargetDMContext ctx, boolean disconnectedTracing) {
		return new MIGDBSetDisconnectedTracing(ctx, disconnectedTracing);
	}

	/** @since 4.4 */
	public ICommand<MIInfo> createMIGDBSetDPrintfStyle(ICommandControlDMContext ctx, String style) {
		return new MIGDBSetDPrintfStyle(ctx, style);
	}	

	public ICommand<MIInfo> createMIGDBSetEnv(ICommandControlDMContext dmc, String name) {
		return new MIGDBSetEnv(dmc, name);
	}

	public ICommand<MIInfo> createMIGDBSetEnv(ICommandControlDMContext dmc, String name, String value) {
		return new MIGDBSetEnv(dmc, name, value);
	}

	/** @since 4.1 */
	public ICommand<MIInfo> createMIGDBSetHostCharset(ICommandControlDMContext ctx, String hostCharset) {
		return new MIGDBSetHostCharset(ctx, hostCharset);
	}

	/** @since 4.3 */
	public ICommand<MIInfo> createMIGDBSetLanguage(IDMContext ctx, String language) {
		return new MIGDBSetLanguage(ctx, language);
	}

	/** @since 5.4*/
	public ICommand<MIInfo> createMIGDBSetNewConsole(IDMContext ctx, boolean isSet) {
		return new MIGDBSetNewConsole(ctx, isSet);
	}

	public ICommand<MIInfo> createMIGDBSetNonStop(ICommandControlDMContext ctx, boolean isSet) {
		return new MIGDBSetNonStop(ctx, isSet);
	}

	public ICommand<MIInfo> createMIGDBSetPagination(ICommandControlDMContext ctx, boolean isSet) {
		return new MIGDBSetPagination(ctx, isSet);
	}

	/** @since 4.1 */
	public ICommand<MIInfo> createMIGDBSetPrintObject(ICommandControlDMContext ctx, boolean enable) {
		return new MIGDBSetPrintObject(ctx, enable);
	}

	/** @since 4.1 */
	public ICommand<MIInfo> createMIGDBSetPrintSevenbitStrings(ICommandControlDMContext ctx, boolean enable) {
		return new MIGDBSetPrintSevenbitStrings(ctx, enable);
	}

	/** @since 4.1 */
	public ICommand<MIInfo> createMIGDBSetPythonPrintStack(ICommandControlDMContext ctx, String option) {
		return new MIGDBSetPythonPrintStack(ctx, option);
	}	

	/** @since 5.2 */
	public ICommand<MIInfo> createMIGDBSetRecordFullStopAtLimit(ICommandControlDMContext ctx, boolean isSet) {
		return new MIGDBSetRecordFullStopAtLimit(ctx, isSet);
	}
	
	/** @since 4.1 */
	public ICommand<MIInfo> createMIGDBSetSchedulerLocking(ICommandControlDMContext ctx, String mode) {
		return new MIGDBSetSchedulerLocking(ctx, mode);
	}

	/** @since 4.1 */
	public ICommand<MIInfo> createMIGDBSetTargetCharset(ICommandControlDMContext ctx, String targetCharset) {
		return new MIGDBSetTargetCharset(ctx, targetCharset);
	}
	
	/** @since 4.1 */
	public ICommand<MIInfo> createMIGDBSetTargetWideCharset(ICommandControlDMContext ctx, String targetWideCharset) {
		return new MIGDBSetTargetWideCharset(ctx, targetWideCharset);
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

	/** @since 4.4 */
	public ICommand<MIInfo> createMIGDBSetTraceNotes(ITraceTargetDMContext ctx, String notes) {
		return new MIGDBSetTraceNotes(ctx, notes);
	}

	/** @since 4.4*/
	public ICommand<MIInfo> createMIGDBSetTraceUser(ITraceTargetDMContext ctx, String userName) {
		return new MIGDBSetTraceUser(ctx, userName);
	}

	public ICommand<MIGDBShowExitCodeInfo> createMIGDBShowExitCode(ICommandControlDMContext ctx) {
		return new MIGDBShowExitCode(ctx);
	}

	/** @since 5.4 */
	public ICommand<MIGDBShowNewConsoleInfo> createMIGDBShowNewConsole(IDMContext ctx) {
		return new MIGDBShowNewConsole(ctx);
	}

	/** @since 4.3 */
	public ICommand<MIGDBShowLanguageInfo> createMIGDBShowLanguage(IDMContext ctx) {
		return new MIGDBShowLanguage(ctx);
	}

	/** @since 4.6 */
	public ICommand<MIGDBVersionInfo> createMIGDBVersion(ICommandControlDMContext ctx) {
		return new MIGDBVersion(ctx);
	}
	
	/** @since 4.0 */
	public ICommand<MIInfo> createMIInferiorTTYSet(IMIContainerDMContext dmc, String tty) {
		return new MIInferiorTTYSet(dmc, tty);
	}

	/**
	 * @since 4.2
	 */
	public ICommand<MIInfoOsInfo> createMIInfoOS(IDMContext ctx) {
		return new MIInfoOs(ctx);
	}

	/**
	 * @since 4.2
	 */
	public ICommand<MIInfoOsInfo> createMIInfoOS(IDMContext ctx, String resourceClass) {
		return new MIInfoOs(ctx, resourceClass);
	}
	
	public ICommand<MIInfo> createMIInterpreterExec(IDMContext ctx, String interpreter, String cmd) {
		return new MIInterpreterExec<MIInfo>(ctx, interpreter, cmd);
	}
	
	public ICommand<MIInfo> createMIInterpreterExecConsole(IDMContext ctx, String cmd) {
		return new MIInterpreterExecConsole<MIInfo>(ctx, cmd);
	}

	/** @since 4.0 */
	public ICommand<MIInfo> createMIInterpreterExecConsoleKill(IMIContainerDMContext ctx) {
		return new MIInterpreterExecConsoleKill(ctx);
	}

	/** @since 4.0 */
	public ICommand<MIListFeaturesInfo> createMIListFeatures(ICommandControlDMContext ctx) {
		return new MIListFeatures(ctx);
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

	/** @since 4.1 */
	public ICommand<MIListThreadGroupsInfo> createMIListThreadGroups(ICommandControlDMContext ctx, boolean listAll, boolean recurse) {
		return new MIListThreadGroups(ctx, listAll, recurse);
	}

	/** @since 4.0 */
	public ICommand<MIInfo> createMIRemoveInferior(ICommandControlDMContext ctx, String groupId) {
		return new MIRemoveInferior(ctx, groupId);
	}

	/** @since 5.0 */
	public ICommand<MIInfo> createMISetSubstitutePath(ISourceLookupDMContext context, String from, String to) {
		return new MISetSubstitutePath(context, from, to);
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

	/** @since 4.0 */
	public ICommand<MIInfo> createMITargetAttach(IMIContainerDMContext ctx, String groupId) {
		return new MITargetAttach(ctx, groupId);
	}

	/** @since 4.0 */
	public ICommand<MIInfo> createMITargetAttach(IMIContainerDMContext ctx, String groupId, boolean interrupt) {
		return new MITargetAttach(ctx, groupId, interrupt);
	}

	/** @since 5.4 */
	public ICommand<MIInfo> createMITargetAttach(IMIContainerDMContext ctx, String groupId, boolean interrupt, boolean extraNewline) {
		return new MITargetAttach(ctx, groupId, interrupt, extraNewline);
	}

	public ICommand<MIInfo> createMITargetDetach(ICommandControlDMContext ctx, String groupId) {
		return new MITargetDetach(ctx, groupId);
	}

	/** @since 4.0 */
	public ICommand<MIInfo> createMITargetDetach(IMIContainerDMContext ctx) {
		return new MITargetDetach(ctx);
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

    /** @since 4.1 */
    public ICommand<MIInfo> createMITargetDisconnect(ICommandControlDMContext ctx) {
        return new MITargetDisconnect(ctx);
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

	/** @since 4.0 */
	public ICommand<MIVarListChildrenInfo> createMIVarListChildren(ICommandControlDMContext ctx, String name, int from, int to) {
		return new MIVarListChildren(ctx, name, from, to);
	}

	public ICommand<MIVarSetFormatInfo> createMIVarSetFormat(ICommandControlDMContext ctx, String name, String fmt) {
		return new MIVarSetFormat(ctx, name, fmt);
	}

	/** @since 4.0 */
	public ICommand<MIInfo> createMIVarSetUpdateRange(ICommandControlDMContext ctx,String name, int from, int to) {
		return new MIVarSetUpdateRange(ctx, name, from, to);
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

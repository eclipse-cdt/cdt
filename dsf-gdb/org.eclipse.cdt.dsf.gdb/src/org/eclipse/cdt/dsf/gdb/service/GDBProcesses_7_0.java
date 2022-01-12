/*******************************************************************************
 * Copyright (c) 2008, 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Onur Akdemir (TUBITAK BILGEM-ITI) - Multi-process debugging (Bug 237306)
 *     John Dallaway - GDB 7.x MI thread details field ignored (Bug 325556)
 *     Marc Khouzam (Ericsson) - Make each thread an IDisassemblyDMContext (bug 352748)
 *     Andy Jin (QNX) - Not output thread osId as a string when it is null (Bug 397039)
 *     Marc Khouzam (Ericsson) - Move IBreakpointsTargetDMContext from MIContainerDMC
 *                               to GDBContainerDMC to ease inheritance (Bug 389945)
 *     Marc Khouzam (Ericsson) - Support for exited processes in the debug view (bug 407340)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerSuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.BufferedCommandControl;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.InferiorRuntimeProcess;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl.MIRunMode;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.MIInferiorProcess;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadGroupCreatedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadGroupExitedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo.IThreadGroupInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MINotifyAsyncOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIThread;
import org.eclipse.cdt.dsf.mi.service.command.output.MIThreadInfoInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.pty.PersistentPTY;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.osgi.framework.BundleContext;

/**
 * This class implements the IProcesses interface for GDB 7.0
 * which supports the new -list-thread-groups command.
 */
public class GDBProcesses_7_0 extends AbstractDsfService implements IGDBProcesses, ICachingService, IEventListener {

	/**
	 * The maximum amount of exited processes we can show.
	 * Each one is shown in the debug view.
	 */
	private final static int MAX_NUMBER_EXITED_PROCESS = 5;

	// Below is the context hierarchy that is implemented between the
	// MIProcesses service and the MIRunControl service for the MI
	// implementation of DSF:
	//
	//                        MIControlDMContext (ICommandControlDMContext)
	//                                |
	//                          MIProcessDMC (IProcess)
	//                             /     \
	//                            /       \
	//                 MIContainerDMC     MIThreadDMC (IThread)
	//                  (IContainer)         /
	//                          \           /
	//                         MIExecutionDMC
	//                          (IExecution)
	//

	/**
	 * Context representing a thread in GDB/MI
	 */
	@Immutable
	private static class MIExecutionDMC extends AbstractDMContext
			implements IMIExecutionDMContext, IDisassemblyDMContext {
		/**
		 * String ID that is used to identify the thread in the GDB/MI protocol.
		 */
		private final String fThreadId;

		/**
		 * Constructor for the context.  It should not be called directly by clients.
		 * Instead clients should call {@link IMIProcesses#createExecutionContext()}
		 * to create instances of this context based on the thread ID.
		 * <p/>
		 *
		 * @param sessionId Session that this context belongs to.
		 * @param containerDmc The container that this context belongs to.
		 * @param threadDmc The thread context parents of this context.
		 * @param threadId GDB/MI thread identifier.
		 */
		protected MIExecutionDMC(String sessionId, IContainerDMContext containerDmc, IThreadDMContext threadDmc,
				String threadId) {
			super(sessionId,
					containerDmc == null && threadDmc == null ? new IDMContext[0]
							: containerDmc == null ? new IDMContext[] { threadDmc }
									: threadDmc == null ? new IDMContext[] { containerDmc }
											: new IDMContext[] { containerDmc, threadDmc });
			fThreadId = threadId;
		}

		/**
		 * Returns the GDB/MI thread identifier of this context.
		 * @return
		 */
		@Override
		public String getThreadId() {
			return fThreadId;
		}

		/* Unused; reintroduce if needed
		public String getId(){
			return fThreadId;
		}
		*/

		@Override
		public String toString() {
			return baseToString() + ".thread[" + fThreadId + "]"; //$NON-NLS-1$//$NON-NLS-2$
		}

		@Override
		public boolean equals(Object obj) {
			return baseEquals(obj) && ((MIExecutionDMC) obj).fThreadId.equals(fThreadId);
		}

		@Override
		public int hashCode() {
			return baseHashCode() ^ fThreadId.hashCode();
		}
	}

	/**
	 * Context representing a thread group of GDB/MI.
	 */
	@Immutable
	static class MIContainerDMC extends AbstractDMContext implements IMIContainerDMContext, IDisassemblyDMContext {
		/**
		 * String ID that is used to identify the thread group in the GDB/MI protocol.
		 */
		private final String fId;

		/**
		 * Constructor for the context.  It should not be called directly by clients.
		 * Instead clients should call {@link IMIProcesses#createContainerContext
		 * to create instances of this context based on the group name.
		 *
		 * @param sessionId Session that this context belongs to.
		 * @param processDmc The process context that is the parent of this context.
		 * @param groupId GDB/MI thread group identifier.
		 */
		public MIContainerDMC(String sessionId, IProcessDMContext processDmc, String groupId) {
			super(sessionId, processDmc == null ? new IDMContext[0] : new IDMContext[] { processDmc });
			fId = groupId;
		}

		/**
		 * Returns the GDB/MI thread group identifier of this context.
		 */
		@Override
		public String getGroupId() {
			return fId;
		}

		@Override
		public String toString() {
			return baseToString() + ".threadGroup[" + fId + "]"; //$NON-NLS-1$//$NON-NLS-2$
		}

		@Override
		public boolean equals(Object obj) {
			return baseEquals(obj)
					&& (((MIContainerDMC) obj).fId == null ? fId == null : ((MIContainerDMC) obj).fId.equals(fId));
		}

		@Override
		public int hashCode() {
			return baseHashCode() ^ (fId == null ? 0 : fId.hashCode());
		}
	}

	private static class GDBContainerDMC extends MIContainerDMC
			implements IMemoryDMContext, IBreakpointsTargetDMContext {
		public GDBContainerDMC(String sessionId, IProcessDMContext processDmc, String groupId) {
			super(sessionId, processDmc, groupId);
		}
	}

	/**
	 * Context representing a thread.
	 * @since 4.0
	 */
	@Immutable
	protected static class MIThreadDMC extends AbstractDMContext implements IThreadDMContext {
		/**
		 * ID used by GDB to refer to threads.
		 */
		private final String fId;

		/**
		 * Constructor for the context.  It should not be called directly by clients.
		 * Instead clients should call {@link IMIProcesses#createThreadContext}
		 * to create instances of this context based on the thread ID.
		 * <p/>
		 *
		 * @param sessionId Session that this context belongs to.
		 * @param processDmc The process that this thread belongs to.
		 * @param id thread identifier.
		 */
		public MIThreadDMC(String sessionId, IProcessDMContext processDmc, String id) {
			super(sessionId, processDmc == null ? new IDMContext[0] : new IDMContext[] { processDmc });
			fId = id;
		}

		/**
		 * Returns the thread identifier of this context.
		 * @return
		 */
		public String getId() {
			return fId;
		}

		@Override
		public String toString() {
			return baseToString() + ".OSthread[" + fId + "]"; //$NON-NLS-1$//$NON-NLS-2$
		}

		@Override
		public boolean equals(Object obj) {
			return baseEquals(obj)
					&& (((MIThreadDMC) obj).fId == null ? fId == null : ((MIThreadDMC) obj).fId.equals(fId));
		}

		@Override
		public int hashCode() {
			return baseHashCode() ^ (fId == null ? 0 : fId.hashCode());
		}
	}

	@Immutable
	private static class MIProcessDMC extends AbstractDMContext implements IMIProcessDMContext {
		/**
		 * ID given by the OS.
		 */
		private final String fId;

		/**
		 * Constructor for the context.  It should not be called directly by clients.
		 * Instead clients should call {@link IMIProcesses#createProcessContext}
		 * to create instances of this context based on the PID.
		 * <p/>
		 *
		 * @param sessionId Session that this context belongs to.
		 * @param controlDmc The control context parent of this process.
		 * @param id process identifier.
		 */
		public MIProcessDMC(String sessionId, ICommandControlDMContext controlDmc, String id) {
			super(sessionId, controlDmc == null ? new IDMContext[0] : new IDMContext[] { controlDmc });
			fId = id;
		}

		@Override
		public String getProcId() {
			return fId;
		}

		@Override
		public String toString() {
			return baseToString() + ".proc[" + fId + "]"; //$NON-NLS-1$//$NON-NLS-2$
		}

		@Override
		public boolean equals(Object obj) {
			// We treat the UNKNOWN_PROCESS_ID as a wildcard.  Any processId (except null) will be considered
			// equal to the UNKNOWN_PROCESS_ID.  This is important because before starting a process, we don't
			// have a pid yet, but we still need to create a process context, and we must use UNKNOWN_PROCESS_ID.
			// Bug 336890

			if (!baseEquals(obj)) {
				return false;
			}

			MIProcessDMC other = (MIProcessDMC) obj;
			if (fId == null || other.fId == null) {
				return fId == null && other.fId == null;
			}

			// Now that we know neither is null, check for UNKNOWN_PROCESS_ID wildcard
			if (fId.equals(MIProcesses.UNKNOWN_PROCESS_ID) || other.fId.equals(MIProcesses.UNKNOWN_PROCESS_ID)) {
				return true;
			}

			return fId.equals(other.fId);
		}

		@Override
		public int hashCode() {
			// We cannot use fId in the hashCode.  This is because we support
			// the wildCard MIProcesses.UNKNOWN_PROCESS_ID which is equal to any other fId.
			// But we also need the hashCode of the wildCard to be the same
			// as the one of all other fIds, which is why we need a constant hashCode
			// See bug 336890
			return baseHashCode();
		}
	}

	/**
	 * A process context representing a process that has exited.
	 * Since an exited process no longer has a pid, we need another way
	 * of characterizing it.  We use the groupId instead.
	 * Note that with GDB 7.0 and 7.1, the groupId is the pid, so that
	 * does not help us, but since we only handle single-process debugging
	 * for those versions of GDB, we don't need any id to know we are
	 * dealing with our single process.
	 * Starting with GDB 7.2, we handle multi-process, but then we
	 * can use the groupId as a persistent identifier of each process,
	 * even an exited one.
	 * @since 4.7
	 */
	@Immutable
	protected static class MIExitedProcessDMC extends MIProcessDMC {
		private final String fGroupId;

		public MIExitedProcessDMC(String sessionId, ICommandControlDMContext controlDmc, String pid, String groupId) {
			super(sessionId, controlDmc, pid);
			fGroupId = groupId;
		}

		public String getGroupId() {
			return fGroupId;
		}

		@Override
		public String toString() {
			return super.toString() + ".group[" + getGroupId() + "]"; //$NON-NLS-1$//$NON-NLS-2$
		}

		@Override
		public boolean equals(Object obj) {
			if (!super.equals(obj)) {
				return false;
			}

			MIExitedProcessDMC other = (MIExitedProcessDMC) obj;
			if (fGroupId == null || other.fGroupId == null) {
				return fGroupId == null && other.fGroupId == null;
			}

			return fGroupId.equals(other.fGroupId);
		}

		@Override
		public int hashCode() {
			return super.hashCode() ^ (fGroupId == null ? 0 : fGroupId.hashCode());
		}
	}

	/**
	 * The data of a corresponding thread or process.
	 */
	@Immutable
	protected static class MIThreadDMData implements IThreadDMData {
		final String fName;
		final String fId;

		public MIThreadDMData(String name, String id) {
			fName = name;
			fId = id;
		}

		@Override
		public String getId() {
			return fId;
		}

		@Override
		public String getName() {
			return fName;
		}

		@Override
		public boolean isDebuggerAttached() {
			return true;
		}
	}

	/**
	 * The data corresponding to an exited process.
	 * @since 4.7
	 */
	@Immutable
	protected static class MIExitedProcessDMData implements IGdbThreadExitedDMData {
		final String fName;
		final String fId;
		final Integer fExitCode;

		public MIExitedProcessDMData(String name, String id, Integer exitCode) {
			fName = name;
			fId = id;
			fExitCode = exitCode;
		}

		@Override
		public String getId() {
			return fId;
		}

		@Override
		public String getName() {
			return fName;
		}

		@Override
		public boolean isDebuggerAttached() {
			return false;
		}

		@Override
		public Integer getExitCode() {
			return fExitCode;
		}
	}

	/**
	 * This class provides an implementation of both a process context and process data.
	 * It is used to be able to return a list of processes including their data all at once.
	 * @since 4.0
	 */
	@Immutable
	protected static class MIProcessDMCAndData extends MIProcessDMC implements IGdbThreadDMData2 {
		final String fName;
		// Note that cores are only available from GDB 7.1.
		final String[] fCores;
		final String fOwner;
		final String fDescription;

		public MIProcessDMCAndData(String sessionId, ICommandControlDMContext controlDmc, String id, String name,
				String[] cores, String owner) {
			this(sessionId, controlDmc, id, name, cores, owner, null);
		}

		/**
		 * @since 5.6
		 */
		public MIProcessDMCAndData(String sessionId, ICommandControlDMContext controlDmc, String id, String name,
				String[] cores, String owner, String description) {
			super(sessionId, controlDmc, id);
			fName = name;
			fCores = cores;
			fOwner = owner;
			fDescription = description;
		}

		@Override
		public String getId() {
			return getProcId();
		}

		@Override
		public String getName() {
			return fName;
		}

		/**
		 * @since 5.6
		 */
		@Override
		public String getDescription() {
			return fDescription;
		}

		@Override
		public boolean isDebuggerAttached() {
			return true;
		}

		@Override
		public String[] getCores() {
			return fCores;
		}

		@Override
		public String getOwner() {
			return fOwner;
		}

		@Override
		public String toString() {
			return baseToString() + ".proc[" + getId() + "," + getName() + "," + getOwner() + "]"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
		}

		@Override
		public boolean equals(Object obj) {
			return super.equals(obj)
					&& (((MIProcessDMCAndData) obj).fName == null ? fName == null
							: ((MIProcessDMCAndData) obj).fName.equals(fName))
					&& (((MIProcessDMCAndData) obj).fOwner == null ? fOwner == null
							: ((MIProcessDMCAndData) obj).fOwner.equals(fOwner));
		}

		@Override
		public int hashCode() {
			return super.hashCode() ^ (fName == null ? 0 : fName.hashCode()) ^ (fOwner == null ? 0 : fOwner.hashCode());
		}
	}

	/**
	 * Event indicating that an container (debugged process) has started.  This event
	 * implements the {@link IStartedMDEvent} from the IRunControl service.
	 */
	public static class ContainerStartedDMEvent extends AbstractDMEvent<IExecutionDMContext>
			implements IStartedDMEvent {
		public ContainerStartedDMEvent(IContainerDMContext context) {
			super(context);
		}
	}

	/**
	 * Event indicating that an container is no longer being debugged.  This event
	 * implements the {@link IExitedMDEvent} from the IRunControl service.
	 */
	public static class ContainerExitedDMEvent extends AbstractDMEvent<IExecutionDMContext> implements IExitedDMEvent {
		public ContainerExitedDMEvent(IContainerDMContext context) {
			super(context);
		}
	}

	/**
	 * @since 4.7
	 */
	protected static class ProcessRemovedDMEvent extends AbstractDMEvent<IThreadDMContext>
			implements IThreadRemovedDMEvent {
		public ProcessRemovedDMEvent(IProcessDMContext context) {
			super(context);
		}
	}

	/**
	 *  A map of thread id to thread group id.  We use this to find out to which threadGroup a thread belongs.
	 */
	private Map<String, String> fThreadToGroupMap = new HashMap<>();
	/**
	 *  A map of thread group id to process id.  We use this to find out to which pid a group refers.
	 */
	private Map<String, String> fGroupToPidMap = new HashMap<>();

	private IGDBControl fCommandControl;
	private IGDBBackend fBackend;
	private CommandFactory fCommandFactory;

	// A cache for commands about the threadGroups
	private CommandCache fContainerCommandCache;

	//A cache for commands about the threads
	private CommandCache fThreadCommandCache;

	// A temporary cache to avoid using -list-thread-groups --available more than once at the same time.
	// We cannot cache this command because it lists all available processes, which can
	// change at any time.  However, it is inefficient to send more than one of this command at
	// the same time.  This cache will help us avoid that.  The idea is that we cache the command,
	// but as soon as it returns, we clear the cache.  So the cache will only trigger for those
	// overlapping situations.  Using this cache also allows to handle the all-stop case
	// when the target can be unavailable and instead of hanging, the cache will return an error.
	private CommandCache fListThreadGroupsAvailableCache;

	// A map of process id to process names.  A name is fetched whenever we start
	// debugging a process, and removed when we stop.
	// This allows us to make sure that if a pid is re-used, we will not use an
	// old name for it.  Bug 275497
	// This map also serves as a list of processes we are currently debugging.
	// This is important because we cannot always ask GDB for the list, since it may
	// be running at the time.  Bug 303503
	private Map<String, String> fDebuggedProcessesAndNames = new HashMap<>();

	/**
	 * A map that keeps track of the PTY associated with an inferior (groupId)
	 */
	private Map<String, PTY> fGroupIdToPTYMap = new HashMap<>();
	/**
	 * A list of groupIds that have exited.
	 */
	private List<String> fExitedGroupId = new ArrayList<>();

	/**
	* Information about an exited process
	* @since 4.7
	*/
	protected class ExitedProcInfo {
		private String pid;
		private String name;
		private Integer exitCode;

		public ExitedProcInfo(String aPid, String aName) {
			pid = aPid;
			name = aName;
		}

		protected String getPid() {
			return pid;
		}

		protected String getName() {
			return name;
		}

		protected Integer getExitCode() {
			return exitCode;
		}

		protected void setExitCode(Integer code) {
			exitCode = code;
		}
	}

	/**
	* A LRU (least-recently-used) map that limits the amount of exited process list.
	* Once the limit is reached, oldest exited processes are automatically removed
	* when new ones are inserted.  This avoids the risk of growing the list
	* of exited processes too much and showing too many in the debug view.
	*/
	private class LRUExitedProcessMap extends LinkedHashMap<String, ExitedProcInfo> {
		public static final long serialVersionUID = 0;

		@Override
		protected boolean removeEldestEntry(Entry<String, ExitedProcInfo> eldest) {
			return size() > MAX_NUMBER_EXITED_PROCESS;
		}
	}

	/**
	* Map of groupId to ExitedProcInfo.
	* This map contains the information of each process that has exited.
	* Note that we must maintain this information ourselves since GDB
	* sometimes prunes its list of inferiors, which implies we cannot
	* count on GDB to keep track of exited processes.
	*/
	private Map<String, ExitedProcInfo> fProcExitedMap = new LRUExitedProcessMap();

	/**
	 * Set of groupId of processes that we detached from.
	 * The content is very short-lived as it is only kept until
	 * we receive the =thread-group-exited event from GDB
	 * and need to know if the process in question was detached from.
	 * Using this set, we can know if we should store the process
	 * in the fExitedProcesses map or not.
	 */
	private Set<String> fProcDetachedSet = new HashSet<>();

	private static final String FAKE_THREAD_ID = "0"; //$NON-NLS-1$

	/**
	 * Keeps track of how many processes we are currently connected to
	 */
	private int fNumConnected;

	/**
	 * Keeps track if we are dealing with the very first process of GDB.
	 */
	private boolean fInitialProcess = true;

	public GDBProcesses_7_0(DsfSession session) {
		super(session);
	}

	/**
	 * This method initializes this service.
	 *
	 * @param requestMonitor
	 *            The request monitor indicating the operation is finished
	 */
	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
			@Override
			protected void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	/**
	 * This method initializes this service after our superclass's initialize()
	 * method succeeds.
	 *
	 * @param requestMonitor
	 *            The call-back object to notify when this service's
	 *            initialization is done.
	 */
	private void doInitialize(RequestMonitor requestMonitor) {

		fCommandControl = getServicesTracker().getService(IGDBControl.class);
		fBackend = getServicesTracker().getService(IGDBBackend.class);
		BufferedCommandControl bufferedCommandControl = new BufferedCommandControl(fCommandControl, getExecutor(), 2);

		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

		// These caches store the result of a command when received; also, these caches
		// are manipulated when receiving events.  Currently, events are received after
		// three scheduling of the executor, while command results after only one.  This
		// can cause problems because command results might be processed before an event
		// that actually arrived before the command result.
		// To solve this, we use a bufferedCommandControl that will delay the command
		// result by two scheduling of the executor.
		// See bug 280461
		fContainerCommandCache = new CommandCache(getSession(), bufferedCommandControl);
		fContainerCommandCache.setContextAvailable(fCommandControl.getContext(), true);
		fThreadCommandCache = new CommandCache(getSession(), bufferedCommandControl);
		fThreadCommandCache.setContextAvailable(fCommandControl.getContext(), true);

		// No need to use the bufferedCommandControl for the listThreadGroups cache
		// because it is not being affected by events.
		fListThreadGroupsAvailableCache = new CommandCache(getSession(), fCommandControl);
		fListThreadGroupsAvailableCache.setContextAvailable(fCommandControl.getContext(), true);

		getSession().addServiceEventListener(this, null);
		fCommandControl.addEventListener(this);

		// Register this service.
		register(new String[] { IProcesses.class.getName(), IMIProcesses.class.getName(), IGDBProcesses.class.getName(),
				GDBProcesses_7_0.class.getName() }, new Hashtable<String, String>());

		requestMonitor.done();
	}

	/**
	 * This method shuts down this service. It unregisters the service, stops
	 * receiving service events, and calls the superclass shutdown() method to
	 * finish the shutdown process.
	 *
	 * @return void
	 */
	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		unregister();
		getSession().removeServiceEventListener(this);
		fCommandControl.removeEventListener(this);
		super.shutdown(requestMonitor);
	}

	/**
	 * @return The bundle context of the plug-in to which this service belongs.
	 */
	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}

	/** @since 4.0 */
	protected Map<String, String> getThreadToGroupMap() {
		return fThreadToGroupMap;
	}

	/** @since 4.0 */
	protected Map<String, String> getGroupToPidMap() {
		return fGroupToPidMap;
	}

	/** @since 4.0 */
	protected int getNumConnected() {
		return fNumConnected;
	}

	/** @since 4.0 */
	protected void setNumConnected(int num) {
		fNumConnected = num;
	}

	/** @since 4.0 */
	protected boolean isInitialProcess() {
		return fInitialProcess;
	}

	/** @since 4.0 */
	protected void setIsInitialProcess(boolean isInitial) {
		fInitialProcess = isInitial;
	}

	/**@since 4.7 */
	protected Map<String, ExitedProcInfo> getExitedProcesses() {
		return fProcExitedMap;
	}

	/** @since 4.7 */
	protected Set<String> getDetachedProcesses() {
		return fProcDetachedSet;
	}

	/**
	 * Returns the groupId that is associated with the provided pId
	 * @since 4.0
	 */
	protected String getGroupFromPid(String pid) {
		if (pid == null)
			return null;

		for (Map.Entry<String, String> entry : getGroupToPidMap().entrySet()) {
			if (pid.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	@Override
	public IThreadDMContext createThreadContext(IProcessDMContext processDmc, String threadId) {
		return new MIThreadDMC(getSession().getId(), processDmc, threadId);
	}

	@Override
	public IProcessDMContext createProcessContext(ICommandControlDMContext controlDmc, String pid) {
		return new MIProcessDMC(getSession().getId(), controlDmc, pid);
	}

	/**
	 * Create a special context describing a process that has exited.
	 * @param controlDmc Its parent context.
	 * @param groupId The GDB groupId to which this process refers to.  Since an exited process no longer
	 *                has a pid, we use this id to characterize it uniquely.
	 * 				  Note that with GDB 7.0 and 7.1, the groupId is the pid, so that
	 *                does not help us, but since we only handle single-process debugging
	 *                for those versions of GDB, we don't need any id to know we are
	 *                dealing with our single process.
	 *                Starting with GDB 7.2, we handle multi-process, but then we
	 *                can use the groupId as a persistent identifier of each process,
	 *                even an exited one.
	 */
	private IProcessDMContext createExitedProcessContext(ICommandControlDMContext controlDmc, String pid,
			String groupId) {
		return new MIExitedProcessDMC(getSession().getId(), controlDmc, pid, groupId);
	}

	@Override
	public IMIExecutionDMContext createExecutionContext(IContainerDMContext containerDmc, IThreadDMContext threadDmc,
			String threadId) {
		return new MIExecutionDMC(getSession().getId(), containerDmc, threadDmc, threadId);
	}

	@Override
	public IMIContainerDMContext createContainerContext(IProcessDMContext processDmc, String groupId) {
		return new GDBContainerDMC(getSession().getId(), processDmc, groupId);
	}

	@Override
	public IMIContainerDMContext createContainerContextFromThreadId(ICommandControlDMContext controlDmc,
			String threadId) {
		String groupId = getThreadToGroupMap().get(threadId);
		if (groupId == null) {
			// this can happen if the threadId was 'all'
			// In such a case, we choose the first process we find
			// This works when we run a single process
			// but will break for multi-process!!!
			if (getThreadToGroupMap().isEmpty()) {
				groupId = MIProcesses.UNIQUE_GROUP_ID;
			} else {
				Collection<String> values = getThreadToGroupMap().values();
				for (String value : values) {
					groupId = value;
					break;
				}
			}
		}

		return createContainerContextFromGroupId(controlDmc, groupId);
	}

	/** @since 4.0 */
	@Override
	public IMIContainerDMContext createContainerContextFromGroupId(ICommandControlDMContext controlDmc,
			String groupId) {
		if (groupId == null || groupId.length() == 0) {
			// This happens when we are doing non-attach, so for GDB < 7.2, we know that in that case
			// we are single process, so lets see if we have the group in our map.
			assert getGroupToPidMap().size() <= 1 : "More than one process in our map"; //$NON-NLS-1$
			if (getGroupToPidMap().size() == 1) {
				for (String key : getGroupToPidMap().keySet()) {
					groupId = key;
					break;
				}
			}
		}

		String pid = getGroupToPidMap().get(groupId);
		if (pid == null) {
			// For GDB 7.0 and 7.1, the groupId is the pid, so we can use it directly
			pid = groupId;
		}
		IProcessDMContext processDmc = createProcessContext(controlDmc, pid);
		return createContainerContext(processDmc, groupId);
	}

	@Override
	public IMIExecutionDMContext[] getExecutionContexts(IMIContainerDMContext containerDmc) {
		if (isExitedProcess(containerDmc)) {
			// No threads for an exited process
			return new IMIExecutionDMContext[0];
		}

		String groupId = containerDmc.getGroupId();
		List<IMIExecutionDMContext> execDmcList = new ArrayList<>();
		Iterator<Map.Entry<String, String>> iterator = getThreadToGroupMap().entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, String> entry = iterator.next();
			if (entry.getValue().equals(groupId)) {
				String threadId = entry.getKey();
				IProcessDMContext procDmc = DMContexts.getAncestorOfType(containerDmc, IProcessDMContext.class);
				IMIExecutionDMContext execDmc = createExecutionContext(containerDmc,
						createThreadContext(procDmc, threadId), threadId);
				execDmcList.add(execDmc);
			}
		}
		return execDmcList.toArray(new IMIExecutionDMContext[0]);
	}

	@Override
	public void getExecutionData(IThreadDMContext dmc, final DataRequestMonitor<IThreadDMData> rm) {
		if (dmc instanceof MIExitedProcessDMC) {
			ExitedProcInfo info = getExitedProcesses().get(((MIExitedProcessDMC) dmc).getGroupId());
			if (info != null) {
				rm.done(new MIExitedProcessDMData(info.getName(), info.getPid(), info.getExitCode()));
			} else {
				// This can happen for example, when restarting an exited process,
				// where we've deleted the process from our table, but it has
				// yet to be cleaned up from the view
				rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
						"Unavailable info about exited process", null)); //$NON-NLS-1$
			}

			return;
		}

		if (dmc instanceof IMIProcessDMContext) {
			String id = ((IMIProcessDMContext) dmc).getProcId();
			String name = null;
			if (fBackend.getSessionType() == SessionType.CORE || "42000".equals(id)) { //$NON-NLS-1$
				// For the Core session, the process is no longer running.
				// Therefore, we cannot get its name with the -list-thread-groups command.
				// As for id 42000, it is a special id used by GDB to indicate the real proc
				// id is not known.  This will happen in a Remote session, when we use
				// -target-select remote instead of -target-select extended-remote.
				//
				// So, we take the name from the binary we are using.
				name = fBackend.getProgramPath().lastSegment();
				// Also, the pid we get from GDB is 1 or 42000, which is not correct.
				// I haven't found a good way to get the pid yet, so let's not show it.
				id = null;
			} else {
				if (fDebuggedProcessesAndNames.containsKey(id)) {
					name = fDebuggedProcessesAndNames.get(id);
					assert name != null;
					if (name == null) {
						// Should not happen, but just in case...use the
						// binary file name (absolute path)
						name = fBackend.getProgramPath().toOSString();
						fDebuggedProcessesAndNames.put(id, name);
					} else if (name.isEmpty()) {
						// We know of the process but haven't fetched its name yet.
						// Let's fetch it now.
						// GDB is debugging a new process. Let's fetch its
						// name and remember it. In order to get the name,
						// we have to request all running processes, not
						// just the ones being debugged. We got a lot more
						// information when we request all processes.
						final String finalPId = id;
						fListThreadGroupsAvailableCache.execute(
								fCommandFactory.createMIListThreadGroups(fCommandControl.getContext(), true),
								new DataRequestMonitor<MIListThreadGroupsInfo>(getExecutor(), null) {
									@Override
									protected void handleCompleted() {
										// We cannot actually cache this command since the process
										// list may change.  But this cache allows to avoid overlapping
										// sending of this command and proper handling if the target is
										// unavailable.
										fListThreadGroupsAvailableCache.reset();

										// Note that the output of the "-list-thread-groups --available" command
										// still shows the pid as a groupId, even for GDB 7.2.
										String name = null;
										if (isSuccess()) {
											for (IThreadGroupInfo groupInfo : getData().getGroupList()) {
												if (groupInfo.getPid().equals(finalPId)) {
													name = groupInfo.getName();
													fDebuggedProcessesAndNames.put(finalPId, name);
													break;
												}
											}
										} else {
											// Looks like this gdb doesn't truly support
											// "-list-thread-groups --available". Get the
											// process list natively if we're debugging locally
											if (fBackend.getSessionType() == SessionType.LOCAL) {
												try {
													IProcessList list = CCorePlugin.getDefault().getProcessList();
													if (list != null) {
														int pId_int = Integer.parseInt(finalPId);
														for (IProcessInfo procInfo : list.getProcessList()) {
															if (procInfo.getPid() == pId_int) {
																name = procInfo.getName();
																fDebuggedProcessesAndNames.put(finalPId, name);
																break;
															}
														}
													}
												} catch (Exception e) {
													rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
															REQUEST_FAILED, "Could not get process name", e)); //$NON-NLS-1$
												}
											}
										}

										if (name == null) {
											// No way to get the name right now, so use the binary file name (absolute path)
											name = fBackend.getProgramPath().toOSString();
											fDebuggedProcessesAndNames.put(finalPId, name);
										}
										rm.done(new MIThreadDMData(name, finalPId));
									}
								});
						return;
					}
				} else {
					// We don't have the name in our map.  This could happen
					// if a process has terminated but the
					// debug session is not terminated because the preference
					// to keep GDB running has been selected or because there
					// are other processes part of that session.
					name = "Unknown name"; //$NON-NLS-1$
				}
			}
			rm.setData(new MIThreadDMData(name, id));
			rm.done();
		} else if (dmc instanceof MIThreadDMC) {
			final MIThreadDMC threadDmc = (MIThreadDMC) dmc;

			ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
			fThreadCommandCache.execute(fCommandFactory.createMIThreadInfo(controlDmc, threadDmc.getId()),
					new DataRequestMonitor<MIThreadInfoInfo>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							IThreadDMData threadData = null;
							if (getData().getThreadList().length != 0) {
								MIThread thread = getData().getThreadList()[0];
								if (thread.getThreadId().equals(threadDmc.getId())) {
									String id = ""; //$NON-NLS-1$
									if (thread.getOsId() != null) {
										id = thread.getOsId();
									}
									// append thread details (if any) to the thread ID
									// as for GDB 6.x with CLIInfoThreadsInfo#getOsId()
									final String details = thread.getDetails();
									if (details != null && !details.isEmpty()) {
										if (!id.isEmpty())
											id += " "; //$NON-NLS-1$
										id += "(" + details + ")"; //$NON-NLS-1$ //$NON-NLS-2$
									}
									// We must indicate and empty id by using null
									if (id.isEmpty())
										id = null;

									threadData = new MIThreadDMData("", id); //$NON-NLS-1$
								}
							}

							if (threadData != null) {
								rm.setData(threadData);
							} else {
								rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
										"Could not get thread info", null)); //$NON-NLS-1$
							}
							rm.done();
						}
					});
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid DMC type", null)); //$NON-NLS-1$
			rm.done();
		}
	}

	@Override
	public void getDebuggingContext(IThreadDMContext dmc, DataRequestMonitor<IDMContext> rm) {
		if (dmc instanceof MIExitedProcessDMC) {
			MIExitedProcessDMC exitedProc = (MIExitedProcessDMC) dmc;
			IMIContainerDMContext containerDmc = createContainerContext(exitedProc, exitedProc.getGroupId());
			rm.setData(containerDmc);
		} else if (dmc instanceof MIProcessDMC) {
			MIProcessDMC procDmc = (MIProcessDMC) dmc;
			IMIContainerDMContext containerDmc = createContainerContext(procDmc, getGroupFromPid(procDmc.getProcId()));
			rm.setData(containerDmc);
		} else if (dmc instanceof MIThreadDMC) {
			MIThreadDMC threadDmc = (MIThreadDMC) dmc;
			IMIProcessDMContext procDmc = DMContexts.getAncestorOfType(dmc, IMIProcessDMContext.class);
			IMIContainerDMContext containerDmc = createContainerContext(procDmc, getGroupFromPid(procDmc.getProcId()));
			rm.setData(createExecutionContext(containerDmc, threadDmc, threadDmc.getId()));
		} else {
			rm.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid thread context.", null)); //$NON-NLS-1$
		}

		rm.done();
	}

	/** @since 4.0 */
	protected boolean doIsDebuggerAttachSupported() {
		return fBackend.getIsAttachSession() && fNumConnected == 0;
	}

	@Override
	public void isDebuggerAttachSupported(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
		rm.setData(doIsDebuggerAttachSupported());
		rm.done();
	}

	@Override
	public void attachDebuggerToProcess(IProcessDMContext procCtx, DataRequestMonitor<IDMContext> rm) {
		attachDebuggerToProcess(procCtx, null, rm);
	}

	/**
	 * @since 4.0
	 */
	@Override
	public void attachDebuggerToProcess(final IProcessDMContext procCtx, final String binaryPath,
			final DataRequestMonitor<IDMContext> dataRm) {
		if (procCtx instanceof IMIProcessDMContext) {
			if (!doIsDebuggerAttachSupported()) {
				dataRm.setStatus(
						new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Attach not supported.", null)); //$NON-NLS-1$
				dataRm.done();
				return;
			}

			// Use a sequence for better control of each step
			ImmediateExecutor.getInstance().execute(new Sequence(getExecutor(), dataRm) {

				private IMIContainerDMContext fContainerDmc;

				private Step[] steps = new Step[] {
						// For remote attach, we must set the binary first
						// For a local attach, GDB can figure out the binary automatically,
						// so we don't specify it.
						new Step() {
							@Override
							public void execute(RequestMonitor rm) {

								if (isInitialProcess()) {
									// To be proper, set the initialProcess variable to false
									// it may be necessary for a class that extends this class
									setIsInitialProcess(false);
								}

								// There is no groupId until we attach, so we can use the default groupId
								fContainerDmc = createContainerContext(procCtx, MIProcesses.UNIQUE_GROUP_ID);

								if (binaryPath != null) {
									fCommandControl.queueCommand(
											fCommandFactory.createMIFileExecAndSymbols(fContainerDmc, binaryPath),
											new ImmediateDataRequestMonitor<MIInfo>(rm));
									return;
								}

								rm.done();
							}
						},
						// Attach to the process
						new Step() {
							@Override
							public void execute(RequestMonitor rm) {
								// For non-stop mode, we do a non-interrupting attach
								// Bug 333284
								boolean shouldInterrupt = true;
								IMIRunControl runControl = getServicesTracker().getService(IMIRunControl.class);
								if (runControl != null && runControl.getRunMode() == MIRunMode.NON_STOP) {
									shouldInterrupt = false;
								}

								fCommandControl.queueCommand(
										fCommandFactory.createMITargetAttach(fContainerDmc,
												((IMIProcessDMContext) procCtx).getProcId(), shouldInterrupt),
										new DataRequestMonitor<MIInfo>(getExecutor(), rm));
							}
						}, new Step() {
							@Override
							public void execute(RequestMonitor rm) {
								// By now, GDB has reported the groupId that was created for this process
								fContainerDmc = createContainerContext(procCtx,
										getGroupFromPid(((IMIProcessDMContext) procCtx).getProcId()));

								// Store the fully formed container context so it can be returned to the caller.
								dataRm.setData(fContainerDmc);

								// Initialize memory data for this process.
								IGDBMemory memory = getServicesTracker().getService(IGDBMemory.class);
								IMemoryDMContext memContext = DMContexts.getAncestorOfType(fContainerDmc,
										IMemoryDMContext.class);
								if (memory == null || memContext == null) {
									rm.done();
									return;
								}
								memory.initializeMemoryData(memContext, rm);
							}
						}, new Step() {
							@Override
							public void execute(RequestMonitor rm) {
								// Start tracking breakpoints.
								MIBreakpointsManager bpmService = getServicesTracker()
										.getService(MIBreakpointsManager.class);
								bpmService.startTrackingBpForProcess(fContainerDmc, rm);
							}
						},
						// Turn on reverse debugging if it was enabled as a launch option
						new Step() {
							@Override
							public void execute(RequestMonitor rm) {
								doReverseDebugStep(procCtx, rm);
							}
						}, };

				@Override
				public Step[] getSteps() {
					return steps;
				}
			});
		} else {
			dataRm.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid process context.", null)); //$NON-NLS-1$
			dataRm.done();
		}
	}

	/** @since 5.0 */
	protected void doReverseDebugStep(final IProcessDMContext procCtx, RequestMonitor rm) {
		// Turn on reverse debugging if it was enabled as a launch option
		IReverseRunControl reverseService = getServicesTracker().getService(IReverseRunControl.class);
		if (reverseService != null) {
			ILaunch launch = procCtx.getAdapter(ILaunch.class);
			if (launch != null) {
				try {
					boolean reverseEnabled = launch.getLaunchConfiguration().getAttribute(
							IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE,
							IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_DEFAULT);
					if (reverseEnabled) {
						reverseService.enableReverseMode(fCommandControl.getContext(), true, rm);
						return;
					}
				} catch (CoreException e) {
					// Ignore, just don't set reverse
				}
			}
		}
		rm.done();
	}

	/** @since 4.0 */
	protected boolean doCanDetachDebuggerFromProcess() {
		return fNumConnected > 0;
	}

	private boolean isExitedProcess(IDMContext dmc) {
		return DMContexts.getAncestorOfType(dmc, MIExitedProcessDMC.class) != null;
	}

	@Override
	public void canDetachDebuggerFromProcess(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
		MIExitedProcessDMC exitedProc = DMContexts.getAncestorOfType(dmc, MIExitedProcessDMC.class);
		if (exitedProc != null) {
			// Allow to use the disconnect button to remove an exited process
			rm.done(true);
			return;
		}
		rm.done(doCanDetachDebuggerFromProcess());
	}

	@Override
	public void detachDebuggerFromProcess(final IDMContext dmc, final RequestMonitor rm) {
		MIExitedProcessDMC exitedProc = DMContexts.getAncestorOfType(dmc, MIExitedProcessDMC.class);
		if (exitedProc != null) {
			// For an exited process, remove the entry from our table to stop showing it, and
			// remove the entry from the launch itself to remove the process's console
			String groupId = exitedProc.getGroupId();
			getExitedProcesses().remove(groupId);
			removeProcessFromLaunch(groupId);
			getSession().dispatchEvent(new ProcessRemovedDMEvent(exitedProc), null);
			return;
		}

		ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
		final IMIProcessDMContext procDmc = DMContexts.getAncestorOfType(dmc, IMIProcessDMContext.class);

		if (controlDmc != null && procDmc != null) {
			if (!doCanDetachDebuggerFromProcess()) {
				rm.setStatus(
						new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Detach not supported.", null)); //$NON-NLS-1$
				rm.done();
				return;
			}

			IMIRunControl runControl = getServicesTracker().getService(IMIRunControl.class);
			if (runControl != null && !runControl.isTargetAcceptingCommands()) {
				fBackend.interrupt();
			}

			// Remember that this process was detached so we don't show it as an exited process.
			// We must set this before sending the detach command to gdb to avoid race conditions
			final IMIContainerDMContext containerDmc = DMContexts.getAncestorOfType(dmc, IMIContainerDMContext.class);
			if (containerDmc != null) {
				getDetachedProcesses().add(containerDmc.getGroupId());
			}
			fCommandControl.queueCommand(fCommandFactory.createMITargetDetach(controlDmc, procDmc.getProcId()),
					new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
						@Override
						protected void handleFailure() {
							// The detach failed
							if (containerDmc != null) {
								getDetachedProcesses().remove(containerDmc.getGroupId());
							}
							super.handleFailure();
						}
					});
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid context.", null)); //$NON-NLS-1$
			rm.done();
		}
	}

	@Override
	public void canTerminate(IThreadDMContext thread, DataRequestMonitor<Boolean> rm) {
		if (thread instanceof MIExitedProcessDMC) {
			// Allow pressing the terminate button to remove an exited process
			rm.setData(true);
		} else {
			rm.setData(true);
		}
		rm.done();
	}

	@Override
	public void isDebugNewProcessSupported(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
		rm.setData(doIsDebugNewProcessSupported());
		rm.done();
	}

	/** @since 4.0 */
	protected boolean doIsDebugNewProcessSupported() {
		return false;
	}

	@Override
	public void debugNewProcess(IDMContext dmc, String file, Map<String, Object> attributes,
			DataRequestMonitor<IDMContext> rm) {

		// Store the current value of the initialProcess variable because we will use it later
		// and we are about to change it.
		boolean isInitial = isInitialProcess();
		if (isInitialProcess()) {
			setIsInitialProcess(false);
		} else {
			// If we are trying to create another process than the initial one, see if we are allowed
			if (!doIsDebugNewProcessSupported()) {
				rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
						"Not allowed to create a new process", null)); //$NON-NLS-1$
				rm.done();
				return;
			}
		}

		ImmediateExecutor.getInstance()
				.execute(getDebugNewProcessSequence(getExecutor(), isInitial, dmc, file, attributes, rm));
	}

	/**
	 * Return the sequence that is to be used to create a new process the specified process.
	 * Allows others to extend more easily.
	 * @since 4.0
	 */
	protected Sequence getDebugNewProcessSequence(DsfExecutor executor, boolean isInitial, IDMContext dmc, String file,
			Map<String, Object> attributes, DataRequestMonitor<IDMContext> rm) {
		return new DebugNewProcessSequence(executor, isInitial, dmc, file, attributes, rm);
	}

	@Override
	public void getProcessesBeingDebugged(final IDMContext dmc, final DataRequestMonitor<IDMContext[]> rm) {
		final ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
		final IMIContainerDMContext containerDmc = DMContexts.getAncestorOfType(dmc, IMIContainerDMContext.class);
		if (containerDmc != null) {
			if (isExitedProcess(containerDmc)) {
				// No threads for an exited process
				rm.done(new IMIExecutionDMContext[0]);
				return;
			}

			fThreadCommandCache.execute(fCommandFactory.createMIListThreadGroups(controlDmc, containerDmc.getGroupId()),
					new DataRequestMonitor<MIListThreadGroupsInfo>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							rm.setData(makeExecutionDMCs(containerDmc, getData().getThreadInfo().getThreadList()));
							rm.done();
						}
					});
		} else {

			final DataRequestMonitor<IMIContainerDMContext[]> addExitedDRM = new ImmediateDataRequestMonitor<IMIContainerDMContext[]>(
					rm) {
				@Override
				protected void handleCompleted() {
					List<IMIContainerDMContext> containerDmcs = new ArrayList<>(Arrays.asList(getData()));

					// Add the exited processes to our list in reverse order of insertion so that
					// the latest exited process is at the top
					List<Entry<String, ExitedProcInfo>> entries = new ArrayList<>(getExitedProcesses().entrySet());
					for (int i = entries.size() - 1; i >= 0; i--) {
						Entry<String, ExitedProcInfo> entry = entries.get(i);
						String groupId = entry.getKey();
						String pid = entry.getValue().getPid();
						IProcessDMContext processDmc = createExitedProcessContext(controlDmc, pid, groupId);
						containerDmcs.add(createContainerContext(processDmc, groupId));
					}

					rm.done(containerDmcs.toArray(new IMIContainerDMContext[containerDmcs.size()]));
				}
			};

			fContainerCommandCache.execute(fCommandFactory.createMIListThreadGroups(controlDmc),
					new DataRequestMonitor<MIListThreadGroupsInfo>(getExecutor(), addExitedDRM) {
						@Override
						protected void handleSuccess() {
							addExitedDRM.done(makeContainerDMCs(controlDmc, getData().getGroupList()));
						}

						@Override
						protected void handleFailure() {
							// If the target is not available, generate the list ourselves
							IMIContainerDMContext[] containerDmcs = new IMIContainerDMContext[getGroupToPidMap()
									.size()];
							int i = 0;
							for (String groupId : getGroupToPidMap().keySet()) {
								containerDmcs[i++] = createContainerContextFromGroupId(controlDmc, groupId);
							}
							addExitedDRM.done(containerDmcs);
						}
					});
		}
	}

	private IExecutionDMContext[] makeExecutionDMCs(IContainerDMContext containerDmc, MIThread[] threadInfos) {
		final IProcessDMContext procDmc = DMContexts.getAncestorOfType(containerDmc, IProcessDMContext.class);

		if (threadInfos.length == 0) {
			// Main thread always exist even if it is not reported by GDB.
			// So create thread-id = 0 when no thread is reported.
			// This hack is necessary to prevent AbstractMIControl from issuing a thread-select
			// because it doesn't work if the application was not compiled with pthread.
			return new IMIExecutionDMContext[] { createExecutionContext(containerDmc,
					createThreadContext(procDmc, FAKE_THREAD_ID), FAKE_THREAD_ID) };
		} else {
			IExecutionDMContext[] executionDmcs = new IMIExecutionDMContext[threadInfos.length];
			for (int i = 0; i < threadInfos.length; i++) {
				String threadId = threadInfos[i].getThreadId();
				executionDmcs[i] = createExecutionContext(containerDmc, createThreadContext(procDmc, threadId),
						threadId);
			}
			return executionDmcs;
		}
	}

	private IMIContainerDMContext[] makeContainerDMCs(ICommandControlDMContext controlDmc, IThreadGroupInfo[] groups) {
		// This is a workaround for post-mortem tracing because the early GDB release
		// does not report a process when we do -list-thread-group
		// GDB 7.2 will properly report the process so this
		// code can be removed when GDB 7.2 is released
		// START OF WORKAROUND
		if (groups.length == 0 && fBackend.getSessionType() == SessionType.CORE) {
			return new IMIContainerDMContext[] {
					createContainerContextFromGroupId(controlDmc, MIProcesses.UNIQUE_GROUP_ID) };
		}
		// END OF WORKAROUND to be removed when GDB 7.2 is available

		// With GDB 7.1, we can receive a bogus process when we are not debugging anything
		// -list-thread-groups
		// ^done,groups=[{id="0",type="process",pid="0"}]
		// As for GDB 7.2, the pid field is missing altogether in this case
		// -list-thread-groups
		// ^done,groups=[{id="i1",type="process"}]
		// Just ignore that entry
		List<IMIContainerDMContext> containerDmcs = new ArrayList<>(groups.length);
		for (IThreadGroupInfo group : groups) {
			if (group.getPid() == null || group.getPid().isEmpty() || group.getPid().equals("0")) { //$NON-NLS-1$
				continue;
			}
			String groupId = group.getGroupId();
			containerDmcs.add(createContainerContextFromGroupId(controlDmc, groupId));
		}
		return containerDmcs.toArray(new IMIContainerDMContext[containerDmcs.size()]);
	}

	@Override
	public void getRunningProcesses(final IDMContext dmc, final DataRequestMonitor<IProcessDMContext[]> rm) {
		final ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
		if (controlDmc != null) {
			fListThreadGroupsAvailableCache.execute(fCommandFactory.createMIListThreadGroups(controlDmc, true),
					new DataRequestMonitor<MIListThreadGroupsInfo>(getExecutor(), rm) {
						@Override
						protected void handleCompleted() {
							// We cannot actually cache this command since the process
							// list may change.  But this cache allows to avoid overlapping
							// sending of this command.
							fListThreadGroupsAvailableCache.reset();

							if (isSuccess()) {
								rm.setData(makeProcessDMCAndData(controlDmc, getData().getGroupList()));
							} else {
								// Looks like this gdb doesn't truly support
								// "-list-thread-groups --available". If we're
								// debugging locally, resort to getting the
								// list natively (as we do with gdb 6.8). If
								// we're debugging remotely, the user is out
								// of luck
								if (fBackend.getSessionType() == SessionType.LOCAL) {
									IProcessList list = null;
									try {
										list = CCorePlugin.getDefault().getProcessList();
									} catch (CoreException e) {
									}

									if (list == null) {
										rm.setData(new IProcessDMContext[0]);
									} else {
										IProcessInfo[] procInfos = list.getProcessList();
										rm.setData(makeProcessDMCAndData(controlDmc, procInfos));
									}
								} else {
									rm.setData(new IProcessDMContext[0]);
								}
							}
							rm.done();
						}
					});
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid context.", null)); //$NON-NLS-1$
			rm.done();
		}

	}

	/**
	 * Create the joint process DMC and data based on IProcessInfo, which is a local listing.
	 * @since 4.0
	 */
	protected MIProcessDMCAndData[] makeProcessDMCAndData(ICommandControlDMContext controlDmc,
			IProcessInfo[] processes) {
		MIProcessDMCAndData[] procDmcs = new MIProcessDMCAndData[processes.length];
		for (int i = 0; i < procDmcs.length; i++) {
			procDmcs[i] = new MIProcessDMCAndData(controlDmc.getSessionId(), controlDmc,
					Integer.toString(processes[i].getPid()), processes[i].getName(), null, null);
		}
		return procDmcs;
	}

	/**
	 * Create the joint process DMC and data based on IThreadGroupInfo, which is obtained from GDB.
	 * @since 4.0
	 */
	protected MIProcessDMCAndData[] makeProcessDMCAndData(ICommandControlDMContext controlDmc,
			IThreadGroupInfo[] processes) {
		MIProcessDMCAndData[] procDmcs = new MIProcessDMCAndData[processes.length];
		int i = 0;
		for (IThreadGroupInfo process : processes) {
			procDmcs[i++] = new MIProcessDMCAndData(controlDmc.getSessionId(), controlDmc, process.getGroupId(),
					process.getName(), process.getCores(), process.getUser(), process.getDesciption());
		}
		return procDmcs;
	}

	@Override
	public void isRunNewProcessSupported(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
		rm.setData(false);
		rm.done();
	}

	@Override
	public void runNewProcess(IDMContext dmc, String file, Map<String, Object> attributes,
			DataRequestMonitor<IProcessDMContext> rm) {
		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
		rm.done();
	}

	@Override
	public void terminate(IThreadDMContext thread, final RequestMonitor rm) {
		if (thread instanceof MIExitedProcessDMC) {
			// For an exited process, remove the entry from our table to stop showing it, and
			// remove the entry from the launch itself to remove the process's console
			String groupId = ((MIExitedProcessDMC) thread).getGroupId();
			getExitedProcesses().remove(groupId);
			removeProcessFromLaunch(groupId);
			getSession().dispatchEvent(new ProcessRemovedDMEvent((IProcessDMContext) thread), null);
		} else if (fBackend.getSessionType() == SessionType.CORE) {
			// For a core session, there is no concept of killing the inferior,
			// so lets kill GDB
			fCommandControl.terminate(rm);
		} else if (thread instanceof IMIProcessDMContext) {
			getDebuggingContext(thread, new ImmediateDataRequestMonitor<IDMContext>(rm) {
				@Override
				protected void handleSuccess() {
					if (getData() instanceof IMIContainerDMContext) {
						IMIRunControl runControl = getServicesTracker().getService(IMIRunControl.class);
						if (runControl != null && !runControl.isTargetAcceptingCommands()) {
							fBackend.interrupt();
						}

						fCommandControl.queueCommand(
								fCommandFactory.createMIInterpreterExecConsoleKill((IMIContainerDMContext) getData()),
								new ImmediateDataRequestMonitor<MIInfo>(rm));
					} else {
						rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR,
								"Invalid process context.", null)); //$NON-NLS-1$
						rm.done();
					}
				}
			});
		} else {
			rm.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid process context.", null)); //$NON-NLS-1$
			rm.done();
		}
	}

	/** @since 4.0 */
	@Override
	public void canRestart(IContainerDMContext containerDmc, DataRequestMonitor<Boolean> rm) {
		if (fBackend.getIsAttachSession() || fBackend.getSessionType() == SessionType.CORE) {
			rm.setData(false);
			rm.done();
			return;
		}

		// Before GDB6.8, the Linux gdbserver would restart a new
		// process when getting a -exec-run but the communication
		// with GDB had a bug and everything hung.
		// with GDB6.8 the program restarts properly one time,
		// but on a second attempt, gdbserver crashes.
		// So, lets just turn off the Restart for Remote debugging
		if (fBackend.getSessionType() == SessionType.REMOTE) {
			rm.setData(false);
			rm.done();
			return;
		}

		rm.setData(true);
		rm.done();
	}

	/**
	 * Creates the container context that is to be used for the new process that will
	 * be created by the restart operation.
	 * This container does not have its pid yet, while the container of the process
	 * that is being restarted does have its pid.
	 * Also, for GDB 7.0 and 7.1, the groupId being the pid, we cannot use the old
	 * container's groupId, but must use the default groupId until the pid is created.
	 *
	 * @since 4.0
	 */
	protected IMIContainerDMContext createContainerContextForRestart(String groupId) {
		IProcessDMContext processDmc = createProcessContext(fCommandControl.getContext(),
				MIProcesses.UNKNOWN_PROCESS_ID);
		// Don't use the groupId passed in, since it is the old pid.
		return createContainerContext(processDmc, MIProcesses.UNIQUE_GROUP_ID);
	}

	/** @since 4.0 */
	@Override
	public void restart(IContainerDMContext containerDmc, final Map<String, Object> attributes,
			final DataRequestMonitor<IContainerDMContext> rm) {
		// Before performing the restart, check if the process is properly suspended.
		// For such a case, we usually use IMIRunControl.isTargetAcceptingCommands().
		// However, in non-stop, although the target is accepting command, a restart
		// won't work because it needs to be able to set breakpoints.  So, to allow
		// for breakpoints to be set, we make sure process is actually suspended.
		//
		// The other way to make this work is to have the restart code set the breakpoints
		// using the breakpoint service, instead of sending the breakpoint command directly.
		// This required more changes than suspending the process, so it was not done
		// just yet.
		// Bug 246740

		final String groupId = ((IMIContainerDMContext) containerDmc).getGroupId();

		// This request monitor actually performs the restart
		RequestMonitor restartRm = new ImmediateRequestMonitor(rm) {
			@Override
			protected void handleSuccess() {
				// For a restart, we are given the container context of the original process.  However, we want to start
				// a new process with a new pid, so we should create a container for it, and not use the old container with the old pid.
				// Pass in the groupId because starting with GDB 7.2, we must re-use the same groupId.
				IContainerDMContext newContainerDmc = createContainerContextForRestart(groupId);

				startOrRestart(newContainerDmc, attributes, true,
						new ImmediateDataRequestMonitor<IContainerDMContext>(rm) {
							@Override
							protected void handleCompleted() {
								// In case the process we restarted was already exited, remove it from our list
								// We do this here for GDB 7.1, because we know the proper groupId here which
								// will change when the new restarted process will start.  For GDB >= 7.2
								// the groupId is fixed so we don't have to do this right away, but it won't hurt.
								getExitedProcesses().remove(groupId);

								setData(getData());
								super.handleCompleted();
							}
						});
			}
		};

		IRunControl runControl = getServicesTracker().getService(IRunControl.class);
		if (runControl != null && !runControl.isSuspended(containerDmc)) {
			// The process is running.  Let's suspended it before doing the restart
			runControl.suspend(containerDmc, restartRm);
		} else {
			// The process is already suspended, we can just trigger the restart
			restartRm.done();
		}
	}

	/** @since 4.0 */
	@Override
	public void start(IContainerDMContext containerDmc, Map<String, Object> attributes,
			DataRequestMonitor<IContainerDMContext> rm) {
		startOrRestart(containerDmc, attributes, false, rm);
	}

	/** @since 4.0 */
	protected void startOrRestart(IContainerDMContext containerDmc, Map<String, Object> attributes, boolean restart,
			DataRequestMonitor<IContainerDMContext> rm) {
		ImmediateExecutor.getInstance()
				.execute(getStartOrRestartProcessSequence(getExecutor(), containerDmc, attributes, restart, rm));
	}

	/**
	 * Return the sequence that is to be used to start or restart the specified process.
	 * Allows others to extend more easily.
	 * @since 4.0
	 */
	protected Sequence getStartOrRestartProcessSequence(DsfExecutor executor, IContainerDMContext containerDmc,
			Map<String, Object> attributes, boolean restart, DataRequestMonitor<IContainerDMContext> rm) {
		return new StartOrRestartProcessSequence_7_0(executor, containerDmc, attributes, restart, rm);
	}

	/**
	 * Removes the process with the specified groupId from the launch.
	 *
	 * @return The label used for the console of that process.
	 */
	private String removeProcessFromLaunch(String groupId) {
		ILaunch launch = (ILaunch) getSession().getModelAdapter(ILaunch.class);
		IProcess[] launchProcesses = launch.getProcesses();
		for (IProcess process : launchProcesses) {
			if (process instanceof InferiorRuntimeProcess) {
				String groupAttribute = process.getAttribute(IGdbDebugConstants.INFERIOR_GROUPID_ATTR);

				// if the groupAttribute is not set in the process we know we are dealing
				// with single process debugging so the one process is the one we want.
				// If the groupAttribute is set, then we must make sure it is the proper inferior
				if (groupAttribute == null || groupAttribute.equals(MIProcesses.UNIQUE_GROUP_ID)
						|| groupAttribute.equals(groupId)) {
					launch.removeProcess(process);
					return process.getLabel();
				}
			}
		}
		return null;
	}

	/**
	 * Add the specified process to the launch.
	 */
	private void addProcessToLaunch(Process inferior, String groupId, String label) {
		// Add the inferior to the launch.
		// This cannot be done on the executor or things deadlock.
		DebugPlugin.getDefault().asyncExec(() -> {
			// Add the inferior
			// Need to go through DebugPlugin.newProcess so that we can use
			// the overrideable process factory to allow others to override.
			// First set attribute to specify we want to create an inferior process.
			// Bug 210366
			ILaunch launch = (ILaunch) getSession().getModelAdapter(ILaunch.class);
			Map<String, String> attributes = new HashMap<>();
			attributes.put(IGdbDebugConstants.PROCESS_TYPE_CREATION_ATTR,
					IGdbDebugConstants.INFERIOR_PROCESS_CREATION_VALUE);
			IProcess runtimeInferior = DebugPlugin.newProcess(launch, inferior, label != null ? label : "", //$NON-NLS-1$
					attributes);
			// Now set the inferior groupId
			runtimeInferior.setAttribute(IGdbDebugConstants.INFERIOR_GROUPID_ATTR, groupId);
		});
	}

	/**
	 * @since 5.2
	 */
	@Override
	public void addInferiorToLaunch(IContainerDMContext containerDmc, String label, PTY pty, RequestMonitor rm) {
		if (containerDmc instanceof IMIContainerDMContext) {
			String groupId = ((IMIContainerDMContext) containerDmc).getGroupId();
			// Create an MIInferiorProcess to track the new instance of the process,
			// remove the old one from the launch, and add the new one to the launch.
			Process inferiorProcess;
			if (pty == null) {
				inferiorProcess = createInferiorProcess(containerDmc, fBackend.getMIOutputStream());
			} else {
				fGroupIdToPTYMap.put(groupId, pty);
				inferiorProcess = createInferiorProcess(containerDmc, pty);
			}
			addProcessToLaunch(inferiorProcess, groupId, label);
		}
		rm.done();
	}

	@DsfServiceEventHandler
	public void eventDispatched(final MIThreadGroupCreatedEvent e) {
		IProcessDMContext procDmc = e.getDMContext();
		IMIContainerDMContext containerDmc = e.getGroupId() != null ? createContainerContext(procDmc, e.getGroupId())
				: null;
		getSession().dispatchEvent(new ContainerStartedDMEvent(containerDmc), getProperties());
	}

	@DsfServiceEventHandler
	public void eventDispatched(final MIThreadGroupExitedEvent e) {
		IProcessDMContext procDmc = e.getDMContext();
		IMIContainerDMContext containerDmc = e.getGroupId() != null ? createContainerContext(procDmc, e.getGroupId())
				: null;
		getSession().dispatchEvent(new ContainerExitedDMEvent(containerDmc), getProperties());
	}

	@DsfServiceEventHandler
	public void eventDispatched(IResumedDMEvent e) {
		if (e instanceof IContainerResumedDMEvent) {
			// This will happen in all-stop mode
			fContainerCommandCache.setContextAvailable(e.getDMContext(), false);
			fThreadCommandCache.setContextAvailable(e.getDMContext(), false);
			fListThreadGroupsAvailableCache.setContextAvailable(e.getDMContext(), false);
		} else {
			// This will happen in non-stop mode
			// Keep target available for Container commands
		}
	}

	/** @since 5.2 */
	protected MIInferiorProcess createInferiorProcess(IContainerDMContext container, OutputStream outputStream) {
		return new MIInferiorProcess(container, outputStream);
	}

	/** @since 5.2 */
	protected MIInferiorProcess createInferiorProcess(IContainerDMContext container, PTY pty) {
		return new MIInferiorProcess(container, pty);
	}

	private void handleRestartingProcess(IMIContainerDMContext containerDmc) {
		String label = removeProcessFromLaunch(containerDmc.getGroupId());
		if (label != null) {
			// We only add the process to the launch if the original process was part of the launch.
			// For example, in the attach case, there is no process added to the launch
			// We re-use the same PTY as the one used before the restart.
			addInferiorToLaunch(containerDmc, label, fGroupIdToPTYMap.get(containerDmc.getGroupId()),
					new ImmediateRequestMonitor());
		}
	}

	@DsfServiceEventHandler
	public void eventDispatched(ISuspendedDMEvent e) {
		if (e instanceof IContainerSuspendedDMEvent) {
			// This will happen in all-stop mode
			fContainerCommandCache.setContextAvailable(fCommandControl.getContext(), true);
			fThreadCommandCache.setContextAvailable(fCommandControl.getContext(), true);
			fListThreadGroupsAvailableCache.setContextAvailable(fCommandControl.getContext(), true);
		} else {
			// This will happen in non-stop mode
		}

		// If user is debugging a gdb target that doesn't send thread
		// creation events, make sure we don't use cached thread
		// information. Reset the cache after every suspend. See bugzilla
		// 280631
		try {
			if (fBackend.getUpdateThreadListOnSuspend()) {
				// We need to clear the cache for the context that we use to fill the cache,
				// and it is the controDMC in this case.
				ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(e.getDMContext(),
						ICommandControlDMContext.class);
				fThreadCommandCache.reset(controlDmc);
			}
		} catch (CoreException exc) {
		}
	}

	// Event handler when a thread or threadGroup starts
	@DsfServiceEventHandler
	public void eventDispatched(IStartedDMEvent e) {
		if (e.getDMContext() instanceof IMIContainerDMContext) {
			String groupId = ((IMIContainerDMContext) e.getDMContext()).getGroupId();
			if (fExitedGroupId.remove(groupId)) {
				// The process in question is restarting.
				handleRestartingProcess((IMIContainerDMContext) e.getDMContext());
			}

			fContainerCommandCache.reset();
			fNumConnected++;
		} else {
			fThreadCommandCache.reset();
		}
	}

	// Event handler when a thread or a threadGroup exits
	@DsfServiceEventHandler
	public void eventDispatched(IExitedDMEvent e) {
		if (e.getDMContext() instanceof IMIContainerDMContext) {
			fExitedGroupId.add(((IMIContainerDMContext) e.getDMContext()).getGroupId());

			fContainerCommandCache.reset();

			assert fNumConnected > 0;
			fNumConnected--;

			if (fNumConnected == 0 && Platform.getPreferencesService().getBoolean(GdbPlugin.PLUGIN_ID,
					IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, true, null)) {
				// If the last process we are debugging finishes and does not restart
				// let's terminate GDB.  We wait a small delay to see if the process will restart.
				// We also do this for a remote attach session, since the 'auto terminate' preference
				// is enabled.  If users want to keep the session alive to attach to another process,
				// they can simply disable that preference
				getExecutor().schedule(new DsfRunnable() {
					@Override
					public void run() {
						// Verify the process didn't restart by checking that we still have nothing connected
						if (fNumConnected == 0) {
							fCommandControl.terminate(new ImmediateRequestMonitor());
						}
					}
				}, 500, TimeUnit.MILLISECONDS);
			}
		} else {
			fThreadCommandCache.reset();
		}
	}

	/**
	 * @since 5.2
	 */
	@DsfServiceEventHandler
	public void eventDispatched(ICommandControlShutdownDMEvent e) {
		// Now that the debug session is over, close the persistent PTY streams
		for (PTY pty : fGroupIdToPTYMap.values()) {
			if (pty instanceof PersistentPTY) {
				try {
					((PersistentPTY) pty).closeStreams();
				} catch (IOException e1) {
				}
			}
		}
		fGroupIdToPTYMap.clear();

		fExitedGroupId.clear();
	}

	@Override
	public void flushCache(IDMContext context) {
		fContainerCommandCache.reset(context);
		fThreadCommandCache.reset(context);
		// Not technically needed since we are supposed to have
		// cleared this cache as soon as the it gets the answer
		// from GDB; but to be more future-proof, might as well
		// clear it here also.
		fListThreadGroupsAvailableCache.reset(context);
	}

	/*
	 * Catch =thread-created/exited and =thread-group-exited events to update our
	 * groupId to threadId map.
	 */
	@Override
	public void eventReceived(Object output) {
		for (MIOOBRecord oobr : ((MIOutput) output).getMIOOBRecords()) {
			if (oobr instanceof MINotifyAsyncOutput) {
				MINotifyAsyncOutput exec = (MINotifyAsyncOutput) oobr;
				String miEvent = exec.getAsyncClass();
				if ("thread-created".equals(miEvent) || "thread-exited".equals(miEvent)) { //$NON-NLS-1$ //$NON-NLS-2$
					String threadId = null;
					String groupId = null;

					MIResult[] results = exec.getMIResults();
					for (int i = 0; i < results.length; i++) {
						String var = results[i].getVariable();
						MIValue val = results[i].getMIValue();
						if (var.equals("group-id")) { //$NON-NLS-1$
							if (val instanceof MIConst) {
								groupId = ((MIConst) val).getString();
							}
						} else if (var.equals("id")) { //$NON-NLS-1$
							if (val instanceof MIConst) {
								threadId = ((MIConst) val).getString();
							}
						}
					}

					if ("thread-created".equals(miEvent)) { //$NON-NLS-1$
						// Update the thread to groupId map with the new groupId
						getThreadToGroupMap().put(threadId, groupId);
					} else {
						getThreadToGroupMap().remove(threadId);
					}
					// "thread-group-created" was used before GDB 7.2, while "thread-group-started" is used with GDB 7.2
				} else if ("thread-group-created".equals(miEvent) || "thread-group-started".equals(miEvent)) { //$NON-NLS-1$ //$NON-NLS-2$
					String groupId = null;
					String pId = null;

					MIResult[] results = exec.getMIResults();
					for (int i = 0; i < results.length; i++) {
						String var = results[i].getVariable();
						MIValue val = results[i].getMIValue();
						if (var.equals("id")) { //$NON-NLS-1$
							if (val instanceof MIConst) {
								groupId = ((MIConst) val).getString().trim();
							}
						} else if (var.equals("pid")) { //$NON-NLS-1$
							// Available starting with GDB 7.2
							if (val instanceof MIConst) {
								pId = ((MIConst) val).getString().trim();
							}
						}
					}

					if (pId == null) {
						// Before GDB 7.2, the groupId was the pid of the process
						pId = groupId;
					}

					if (groupId != null) {
						// In case the process that just started was already exited (so we are dealing
						// with a restart), remove it from our list.
						// Do this here to handle the restart case triggered by GDB itself
						// (user typing 'run' from the GDB console).  In this case, we don't know yet
						// we are dealing with a restart, but when we see the process come back, we
						// know to remove it from the exited list.  Note that this won't work
						// for GDB 7.1 because the groupId of the new process is not the same as the old
						// one.  Not worth fixing for such an old version.
						getExitedProcesses().remove(groupId);

						getGroupToPidMap().put(groupId, pId);

						// Mark that we know this new process, but don't fetch its
						// name until it is requested.
						fDebuggedProcessesAndNames.put(pId, ""); //$NON-NLS-1$
					}
				} else if ("thread-group-exited".equals(miEvent)) { //$NON-NLS-1$
					String groupId = null;

					MIResult[] results = exec.getMIResults();
					for (int i = 0; i < results.length; i++) {
						String var = results[i].getVariable();
						MIValue val = results[i].getMIValue();
						if (var.equals("id")) { //$NON-NLS-1$
							if (val instanceof MIConst) {
								groupId = ((MIConst) val).getString().trim();
							}
						}
					}

					if (groupId != null) {
						String pId = getGroupToPidMap().remove(groupId);

						// GDB is no longer debugging this process.  Remove it from our list
						String name = fDebuggedProcessesAndNames.remove(pId);
						if (!getDetachedProcesses().remove(groupId)) {
							// If the process was not detached,
							// store it in the list of exited processes.
							getExitedProcesses().put(groupId, new ExitedProcInfo(pId, name));
						}

						// Remove any entries for that group from our thread to group map
						// When detaching from a group, we won't have received any thread-exited event
						// but we don't want to keep those entries.
						if (getThreadToGroupMap().containsValue(groupId)) {
							Iterator<Map.Entry<String, String>> iterator = getThreadToGroupMap().entrySet().iterator();
							while (iterator.hasNext()) {
								if (iterator.next().getValue().equals(groupId)) {
									iterator.remove();
								}
							}
						}
					}
				}
			}
		}
	}
}

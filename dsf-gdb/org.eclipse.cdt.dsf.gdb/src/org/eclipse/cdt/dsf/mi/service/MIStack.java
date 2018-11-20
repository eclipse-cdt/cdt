/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson			  - Modified for handling of multiple execution contexts
 *     Marc Khouzam (Ericsson) - Show return value of the method when doing a step-return (Bug 341731)
 *     Elena Laskavaia (Qnx Software Systems) - Stack Frames cache and error recovery
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerSuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.command.BufferedCommandControl;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordSelectedChangedDMEvent;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.events.IMIDMEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIFunctionFinishedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIArg;
import org.eclipse.cdt.dsf.mi.service.command.output.MIFrame;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStackInfoDepthInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStackListArgumentsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStackListFramesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStackListLocalsInfo;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class MIStack extends AbstractDsfService implements IStack, ICachingService {
	private static final int DEFAULT_STACK_DEPTH = 5;

	protected static class MIFrameDMC extends AbstractDMContext implements IFrameDMContext {
		private final int fLevel;

		public MIFrameDMC(String sessionId, IExecutionDMContext execDmc, int level) {
			super(sessionId, new IDMContext[] { execDmc });
			fLevel = level;
		}

		@Override
		public int getLevel() {
			return fLevel;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof MIFrameDMC)) {
				return false;
			}

			return super.baseEquals(other) && ((MIFrameDMC) other).fLevel == fLevel;
		}

		@Override
		public int hashCode() {
			return super.baseHashCode() ^ fLevel;
		}

		@Override
		public String toString() {
			return baseToString() + ".frame[" + fLevel + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected static class MIVariableDMC extends AbstractDMContext implements IVariableDMContext {
		public enum Type {
			ARGUMENT, LOCAL,
			/** @since 4.4 */
			RETURN_VALUES
		}

		private final Type fType;
		private final int fIndex;

		public MIVariableDMC(MIStack service, IFrameDMContext frame, Type type, int index) {
			super(service, new IDMContext[] { frame });
			fIndex = index;
			fType = type;
		}

		public int getIndex() {
			return fIndex;
		}

		public Type getType() {
			return fType;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof MIVariableDMC)) {
				return false;
			}

			return super.baseEquals(other) && ((MIVariableDMC) other).fType == fType
					&& ((MIVariableDMC) other).fIndex == fIndex;
		}

		@Override
		public int hashCode() {
			int typeFactor = 0;
			if (fType == Type.LOCAL) {
				typeFactor = 2;
			} else if (fType == Type.ARGUMENT) {
				typeFactor = 3;
			} else if (fType == Type.RETURN_VALUES) {
				typeFactor = 4;
			}
			return super.baseHashCode() ^ typeFactor ^ fIndex;
		}

		@Override
		public String toString() {
			return baseToString() + ".variable(" + fType + ")[" + fIndex + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	/**
	 * Same as with frame objects, this is a base class for the IVariableDMData object that uses an MIArg object to
	 * provide the data.  Sub-classes must supply the MIArg object.
	 */
	private static class VariableData implements IVariableDMData {
		private MIArg fMIArg;

		public VariableData(MIArg arg) {
			fMIArg = arg;
		}

		@Override
		public String getName() {
			return fMIArg.getName();
		}

		@Override
		public String getValue() {
			return fMIArg.getValue();
		}

		@Override
		public String toString() {
			return fMIArg.toString();
		}
	}

	private CommandCache fMICommandCache;
	private CommandFactory fCommandFactory;

	/**
	 * Class to track stack depth and debug frames for our internal cache
	 */
	private static class FramesCacheInfo {
		// If this set to true our knowledge of stack depths is limited to current depth, i.e
		// we only know that stack depth is at least "stackDepth" but it could be more
		private boolean limited = true;
		// The actual depth we received
		private int stackDepth = -1;
		private final List<FrameData> frames = new ArrayList<>();

		/**
		 * Return currently cached stack depth if cache value if valid, otherwise return -1.
		 *
		 * Cache value is valid if previous limited requests were with limits of this request.
		 *
		 * @param maxDepth
		 * @return
		 */
		public int getStackDepth(int maxDepth) {
			if (!limited) {
				return stackDepth;
			}
			if (maxDepth > 0 && stackDepth >= maxDepth) {
				return stackDepth;
			}
			return -1;
		}

		public void setStackDepth(int returned, int requested) {
			if (returned <= 0) {
				return; // no valid depths, not updating
			}
			if (returned < requested) {
				// since we did not reach the limit, cache is valid for unlimited range
				limited = false;
			} else if (requested <= 0) {
				// if it was unlimited now stackDepth cache is valid in all ranges
				limited = false;
			}
			if (returned > stackDepth) {
				// that should only increase (and only if increased requested depth)
				stackDepth = returned;
			}
		}

		/**
		 * Return currently cached stack depth, and if not cache available return default depth
		 *
		 * @return
		 */
		public int getValidStackDepth() {
			if (stackDepth <= 0) {
				return DEFAULT_STACK_DEPTH;
			}
			return stackDepth;
		}

		public void updateFrameData(FrameData frame) {
			try {
				int level = frame.getMIFrame().getLevel();
				if (stackDepth < level + 1) {
					stackDepth = level + 1;
				}
				while (level >= frames.size()) {
					frames.add(null);
				}
				frames.set(level, frame);
			} catch (Exception e) {
				// cannot afford throwing runtime exceptions
				GdbPlugin.log(e);
			}
		}

		public FrameData getFrameData(int level) {
			try {
				if (level < 0 || level >= frames.size()) {
					return null;
				}
				return frames.get(level);
			} catch (Exception e) {
				// cannot afford throwing runtime exceptions
				GdbPlugin.log(e);
			}
			return null;
		}
	}

	/**
	   A Map of threadId -> FramesCacheInfo, that can be cleared based on a context.
	   We use this cache for a few reasons:
	   <br>
	   First, two commands such as
	   <pre>
	   -stack-info-depth 11
	   -stack-info-depth 2
	   </pre>
	   would both be sent to GDB because the command cache sees them as different.
	   This cache allows us to know that if we already asked for a stack depth
	   we can potentially re-use the answer.
	   <br>
	   The same concept is applicable for the -stack-list-frames command with different limits.
	   Also, the stack depth can be deduced from the frames list, so we don't need to ask gdb for it again.
	   <p>
	   The second reason is that gdb is unreliable when it comes to returning frames. The MI protocol only allows to reply
	   with data or with error. When gdb is unwinding sometimes it gets both, and while the console CLI protocol has no
	   problem with that, for MI, gdb replies randomly, sometimes with data, sometimes with error. If we cache the valid data
	   it will eliminate the issue with invalid data on subsequent invocations. We don't cache errors.
	 */
	@SuppressWarnings("serial")
	private class FramesCache extends HashMap<String, FramesCacheInfo> {
		public void clear(IDMContext context) {
			final IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
			if (execDmc != null) {
				remove(execDmc.getThreadId());
			} else {
				clear();
			}
		}

		public FramesCacheInfo getThreadFramesCache(String threadId) {
			FramesCacheInfo info = get(threadId);
			if (info == null) {
				put(threadId, info = new FramesCacheInfo());
			}
			return info;
		}

		public FramesCacheInfo update(String threadId, int stackDepth, int maxRequestedStackDepth) {
			FramesCacheInfo info = getThreadFramesCache(threadId);
			info.setStackDepth(stackDepth, maxRequestedStackDepth);
			return info;
		}

		public FramesCacheInfo update(String threadId, MIStackListFramesInfo framesInfo) {
			FramesCacheInfo info = getThreadFramesCache(threadId);
			if (framesInfo != null) {
				int len = framesInfo.getMIFrames().length;
				for (int i = 0; i < len; i++) {
					info.updateFrameData(new FrameDataFromMIStackFrameListInfo(framesInfo, i));
				}
			}
			return info;
		}
	}

	private FramesCache fFramesCache = new FramesCache();

	private MIStoppedEvent fCachedStoppedEvent;
	private IRunControl fRunControl;

	/**
	 * Indicates that we are currently visualizing trace data.
	 * In this case, some errors should not be reported.
	 */
	private boolean fTraceVisualization;

	/**
	 * A Map of a return value for each thread.
	 * A return value is stored when the user performs a step-return,
	 * and it cleared as soon as that thread executes again.
	 */
	private Map<IMIExecutionDMContext, VariableData> fThreadToReturnVariable = new HashMap<>();

	public MIStack(DsfSession session) {
		super(session);
	}

	/**
	 * Base class for the IFrameDMData object that uses an MIFrame object to
	 * provide the data.  Sub-classes must provide the MIFrame object
	 */
	private abstract class FrameData implements IFrameDMData {
		protected abstract MIFrame getMIFrame();

		@Override
		public IAddress getAddress() {
			String addr = getMIFrame().getAddress();
			if (addr == null || addr.length() == 0) {
				return new Addr32(0);
			}
			if (addr.startsWith("0x")) { //$NON-NLS-1$
				addr = addr.substring(2);
			}
			if (addr.length() <= 8) {
				return new Addr32(getMIFrame().getAddress());
			} else {
				return new Addr64(getMIFrame().getAddress());
			}
		}

		@Override
		public int getColumn() {
			return 0;
		}

		@Override
		public String getFile() {
			return getMIFrame().getFile();
		}

		@Override
		public int getLine() {
			return getMIFrame().getLine();
		}

		@Override
		public String getFunction() {
			return getMIFrame().getFunction();
		}

		@Override
		public String getModule() {
			return ""; //$NON-NLS-1$
		}

		@Override
		public String toString() {
			return getMIFrame().toString();
		}
	}

	private class FrameDataFromStoppedEvent extends FrameData {
		private final MIStoppedEvent fEvent;

		FrameDataFromStoppedEvent(MIStoppedEvent event) {
			fEvent = event;
		}

		@Override
		protected MIFrame getMIFrame() {
			return fEvent.getFrame();
		}
	}

	private class FrameDataFromMIStackFrameListInfo extends FrameData {
		private MIStackListFramesInfo fFrameDataCacheInfo;
		private int fFrameIndex;

		FrameDataFromMIStackFrameListInfo(MIStackListFramesInfo info, int index) {
			fFrameDataCacheInfo = info;
			fFrameIndex = index;
		}

		@Override
		protected MIFrame getMIFrame() {
			return fFrameDataCacheInfo.getMIFrames()[fFrameIndex];
		}
	}

	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}

	@Override
	public void initialize(final RequestMonitor rm) {
		super.initialize(new ImmediateRequestMonitor(rm) {
			@Override
			protected void handleSuccess() {
				doInitialize(rm);
			}
		});
	}

	private void doInitialize(RequestMonitor rm) {
		ICommandControlService commandControl = getServicesTracker().getService(ICommandControlService.class);
		BufferedCommandControl bufferedCommandControl = new BufferedCommandControl(commandControl, getExecutor(), 2);

		// This cache stores the result of a command when received; also, this cache
		// is manipulated when receiving events.  Currently, events are received after
		// three scheduling of the executor, while command results after only one.  This
		// can cause problems because command results might be processed before an event
		// that actually arrived before the command result.
		// To solve this, we use a bufferedCommandControl that will delay the command
		// result by two scheduling of the executor.
		// See bug 280461
		fMICommandCache = new CommandCache(getSession(), bufferedCommandControl);
		fMICommandCache.setContextAvailable(commandControl.getContext(), true);
		fRunControl = getServicesTracker().getService(IRunControl.class);

		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

		getSession().addServiceEventListener(this, null);
		register(new String[] { IStack.class.getName(), MIStack.class.getName() }, new Hashtable<String, String>());
		rm.done();
	}

	@Override
	public void shutdown(RequestMonitor rm) {
		unregister();
		getSession().removeServiceEventListener(this);
		fMICommandCache.reset();
		super.shutdown(rm);
	}

	/**
	 * Creates a frame context.  This method is intended to be used by other MI
	 * services and sub-classes which need to create a frame context directly.
	 * <p>
	 * Sub-classes can override this method to provide custom stack frame
	 * context implementation.
	 * </p>
	 * @param execDmc Execution context that this frame is to be a child of.
	 * @param level Level of the new context.
	 * @return A new frame context.
	 */
	public IFrameDMContext createFrameDMContext(IExecutionDMContext execDmc, int level) {
		return new MIFrameDMC(getSession().getId(), execDmc, level);
	}

	@Override
	public void getFrames(final IDMContext ctx, final DataRequestMonitor<IFrameDMContext[]> rm) {
		getFrames(ctx, 0, ALL_FRAMES, rm);
	}

	@Override
	public void getFrames(final IDMContext ctx, final int startIndex, final int endIndex,
			final DataRequestMonitor<IFrameDMContext[]> rm) {
		if (startIndex < 0 || endIndex > 0 && endIndex < startIndex) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
					"Invalid stack frame range [" + startIndex + ',' + endIndex + ']', null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		final IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(ctx, IMIExecutionDMContext.class);

		if (execDmc == null) {
			rm.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context " + ctx, null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		// Make sure the thread is stopped but only if we are not visualizing trace data
		if (!fTraceVisualization && !fRunControl.isSuspended(execDmc)) {
			rm.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Context is running: " + ctx, null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		if (startIndex == 0 && endIndex == 0) {
			// Try to retrieve the top stack frame from the cached stopped event.
			if (fCachedStoppedEvent != null && fCachedStoppedEvent.getFrame() != null
					&& execDmc.equals(fCachedStoppedEvent.getDMContext())) {
				rm.setData(new IFrameDMContext[] {
						createFrameDMContext(execDmc, fCachedStoppedEvent.getFrame().getLevel()) });
				rm.done();
				return;
			}
		}

		String threadId = execDmc.getThreadId();
		// if requested stack limit is bigger then currently cached this call will return -1
		final int maxDepth = endIndex > 0 ? endIndex + 1 : -1;
		int depth = fFramesCache.getThreadFramesCache(threadId).getStackDepth(maxDepth);
		if (depth > 0) { // our stack depth cache is good so we can use it to fill levels array
			rm.setData(getDMFrames(execDmc, startIndex, endIndex, depth));
			rm.done();
			return;
		}
		getStackDepth(execDmc, maxDepth, new DataRequestMonitor<Integer>(getExecutor(), rm) {
			@Override
			protected void handleCompleted() {
				// This function does not actually use debug frames but only returns array of levels,
				// we will use cached stack depth to populate this array,
				// getStackDepth call would have updated cache for us.
				// We use same handler on success or error, since gdb is unreliable when comes to frame retrieval
				// we will return frames array even if we get error when attempting to get stack depth.
				int stackDepth = fFramesCache.getThreadFramesCache(threadId).getValidStackDepth();
				rm.done(getDMFrames(execDmc, startIndex, endIndex, stackDepth));
			}
		});
	}

	private IFrameDMContext[] getDMFrames(final IMIExecutionDMContext execDmc, int startIndex, int endIndex,
			int stackDepth) {
		if (endIndex > stackDepth - 1 || endIndex < 0) {
			endIndex = stackDepth - 1;
		}
		if (startIndex > endIndex) {
			return new IFrameDMContext[] {};
		}
		int length = endIndex - startIndex + 1;
		IFrameDMContext[] frameDMCs = new MIFrameDMC[length];
		for (int i = 0; i < length; i++) {
			frameDMCs[i] = createFrameDMContext(execDmc, i + startIndex);
		}
		return frameDMCs;
	}

	private ICommand<MIStackListFramesInfo> createMIStackListFrames(final IMIExecutionDMContext execDmc) {
		return fCommandFactory.createMIStackListFrames(execDmc);
	}

	private ICommand<MIStackListFramesInfo> createMIStackListFrames(final IMIExecutionDMContext execDmc,
			final int startIndex, final int endIndex) {
		final ICommand<MIStackListFramesInfo> miStackListCmd;
		if (endIndex >= 0) {
			miStackListCmd = fCommandFactory.createMIStackListFrames(execDmc, startIndex, endIndex);
		} else {
			miStackListCmd = fCommandFactory.createMIStackListFrames(execDmc);
		}
		return miStackListCmd;
	}

	@Override
	public void getTopFrame(final IDMContext ctx, final DataRequestMonitor<IFrameDMContext> rm) {
		final IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(ctx, IMIExecutionDMContext.class);
		if (execDmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context" + ctx, null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		// Try to retrieve the top stack frame from the cached stopped event.
		if (fCachedStoppedEvent != null && fCachedStoppedEvent.getFrame() != null
				&& execDmc.equals(fCachedStoppedEvent.getDMContext())) {
			rm.setData(createFrameDMContext(execDmc, fCachedStoppedEvent.getFrame().getLevel()));
			rm.done();
			return;
		}

		// If stopped event is not available or doesn't contain frame info,
		// query top stack frame
		getFrames(ctx, 0, 0, new DataRequestMonitor<IFrameDMContext[]>(getExecutor(), rm) {
			@Override
			protected void handleSuccess() {
				rm.setData(getData()[0]);
				rm.done();
			}
		});
	}

	@Override
	public void getFrameData(final IFrameDMContext frameDmc, final DataRequestMonitor<IFrameDMData> rm) {
		if (!(frameDmc instanceof MIFrameDMC)) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
					"Invalid context type " + frameDmc, null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		final MIFrameDMC miFrameDmc = (MIFrameDMC) frameDmc;

		final IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(frameDmc, IMIExecutionDMContext.class);
		if (execDmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
					"No execution context found in " + frameDmc, null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		String threadId = execDmc.getThreadId();
		final int frameLevel = miFrameDmc.fLevel;
		FrameData fd = fFramesCache.getThreadFramesCache(threadId).getFrameData(frameLevel);
		if (fd != null) {
			rm.setData(fd);
			rm.done();
			return;
		}

		// If requested frame is the top stack frame, try to retrieve it from
		// the stopped event data.

		// Retrieve the top stack frame from the stopped event only if the selected thread is the one on which stopped event
		// is raised
		if (frameLevel == 0) {
			if (fCachedStoppedEvent != null && fCachedStoppedEvent.getFrame() != null
					&& (execDmc.equals(fCachedStoppedEvent.getDMContext()) || fTraceVisualization)) {
				try {
					rm.setData(new FrameDataFromStoppedEvent(fCachedStoppedEvent));
					return;
				} finally {
					rm.done();
				}
			}
		}

		// If not, retrieve the full list of frame data.
		fMICommandCache.execute(createMIStackListFrames(execDmc),
				new DataRequestMonitor<MIStackListFramesInfo>(getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						FramesCacheInfo info = fFramesCache.update(threadId, getData());
						FrameData frameData = info.getFrameData(frameLevel);
						if (frameData == null) {
							rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
									"Invalid frame " + frameDmc, null)); //$NON-NLS-1$
						} else {
							rm.done(frameData);
						}
					}

					@Override
					protected void handleError() {
						// We're seeing gdb in some cases fail when it's being asked for the stack
						// frames with no limits, but the same command succeeds if the request is limited
						// to one frame. So try again with a limit of 1.
						// It's better to show just one frame than none at all
						fMICommandCache.execute(createMIStackListFrames(execDmc, frameLevel, frameLevel),
								new DataRequestMonitor<MIStackListFramesInfo>(getExecutor(), rm) {
									@Override
									protected void handleSuccess() {
										FrameData frameData = fFramesCache.update(threadId, getData())
												.getFrameData(frameLevel);
										if (frameData == null) {
											rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
													"Invalid frame " + frameDmc, null)); //$NON-NLS-1$
										} else {
											rm.done(frameData);
										}
									}
								});
					}
				});
	}

	@Override
	public void getArguments(final IFrameDMContext frameDmc, final DataRequestMonitor<IVariableDMContext[]> rm) {
		final IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(frameDmc, IMIExecutionDMContext.class);
		if (execDmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
					"No execution context found in " + frameDmc, null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		// If requested frame is the top stack frame, try to retrieve it from
		// the stopped event data.
		if (frameDmc.getLevel() == 0 && fCachedStoppedEvent != null && fCachedStoppedEvent.getFrame() != null
				&& execDmc.equals(fCachedStoppedEvent.getDMContext())
				&& fCachedStoppedEvent.getFrame().getArgs() != null) {
			rm.setData(
					makeVariableDMCs(frameDmc, MIVariableDMC.Type.ARGUMENT, fCachedStoppedEvent.getFrame().getArgs()));
			rm.done();
			return;
		}

		// If not, retrieve the full list of frame data.  Although we only need one frame
		// for this call, it will be stored the cache and made available for other calls.
		fMICommandCache.execute(
				// We don't actually need to ask for the values in this case, but since
				// we will ask for them right after, it is more efficient to ask for them now
				// so as to cache the result.  If the command fails, then we will ask for
				// the result without the values
				// Don't ask for value when we are visualizing trace data, since some
				// data will not be there, and the command will fail
				fCommandFactory.createMIStackListArguments(execDmc, true),
				new DataRequestMonitor<MIStackListArgumentsInfo>(getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						// Find the index to the correct MI frame object.
						// Note: this is a short-cut, but it won't work once we implement retrieving
						// partial lists of stack frames.
						int idx = frameDmc.getLevel();
						if (idx == -1 || idx >= getData().getMIFrames().length) {
							rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
									"Invalid frame " + frameDmc, null)); //$NON-NLS-1$
							rm.done();
							return;
						}

						// Create the variable array out of MIArg array.
						MIArg[] args = getData().getMIFrames()[idx].getArgs();
						if (args == null) {
							args = new MIArg[0];
						}
						rm.setData(makeVariableDMCs(frameDmc, MIVariableDMC.Type.ARGUMENT, args));
						rm.done();
					}

					@Override
					protected void handleError() {
						// If the command fails it can be because we asked for values.
						// This can happen with uninitialized values and pretty printers (bug 307614).
						// Since asking for values was simply an optimization
						// to store the command in the cache, let's retry the command without asking for values.
						fMICommandCache.execute(fCommandFactory.createMIStackListArguments(execDmc, false),
								new DataRequestMonitor<MIStackListArgumentsInfo>(getExecutor(), rm) {
									@Override
									protected void handleSuccess() {
										// Find the index to the correct MI frame object.
										// Note: this is a short-cut, but it won't work once we implement retrieving
										// partial lists of stack frames.
										int idx = frameDmc.getLevel();
										if (idx == -1 || idx >= getData().getMIFrames().length) {
											rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
													"Invalid frame " + frameDmc, null)); //$NON-NLS-1$
											rm.done();
											return;
										}

										// Create the variable array out of MIArg array.
										MIArg[] args = getData().getMIFrames()[idx].getArgs();
										if (args == null) {
											args = new MIArg[0];
										}
										rm.setData(makeVariableDMCs(frameDmc, MIVariableDMC.Type.ARGUMENT, args));
										rm.done();
									}
								});
					}
				});
	}

	@Override
	public void getVariableData(IVariableDMContext variableDmc, final DataRequestMonitor<IVariableDMData> rm) {
		if (!(variableDmc instanceof MIVariableDMC)) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
					"Invalid context type " + variableDmc, null)); //$NON-NLS-1$
			rm.done();
			return;
		}
		final MIVariableDMC miVariableDmc = (MIVariableDMC) variableDmc;

		// Extract the frame DMC from the variable DMC.
		final MIFrameDMC frameDmc = DMContexts.getAncestorOfType(variableDmc, MIFrameDMC.class);
		if (frameDmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
					"No frame context found in " + variableDmc, null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		final IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(frameDmc, IMIExecutionDMContext.class);
		if (execDmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
					"No execution context found in " + frameDmc, null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		// Check if the stopped event can be used to extract the variable value.
		if (miVariableDmc.fType == MIVariableDMC.Type.ARGUMENT && frameDmc.fLevel == 0 && fCachedStoppedEvent != null
				&& fCachedStoppedEvent.getFrame() != null && execDmc.equals(fCachedStoppedEvent.getDMContext())
				&& fCachedStoppedEvent.getFrame().getArgs() != null) {
			if (miVariableDmc.fIndex >= fCachedStoppedEvent.getFrame().getArgs().length) {
				rm.setStatus(
						new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Invalid variable " + miVariableDmc, null)); //$NON-NLS-1$
				rm.done();
				return;
			}

			rm.setData(new VariableData(fCachedStoppedEvent.getFrame().getArgs()[miVariableDmc.fIndex]));
			rm.done();
			return;
		}

		if (miVariableDmc.fType == MIVariableDMC.Type.ARGUMENT) {
			fMICommandCache.execute(
					// Don't ask for value when we are visualizing trace data, since some
					// data will not be there, and the command will fail
					fCommandFactory.createMIStackListArguments(execDmc, true),
					new DataRequestMonitor<MIStackListArgumentsInfo>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							// Find the correct frame and argument
							if (frameDmc.fLevel >= getData().getMIFrames().length
									|| miVariableDmc.fIndex >= getData().getMIFrames()[frameDmc.fLevel]
											.getArgs().length) {
								rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
										"Invalid variable " + miVariableDmc, null)); //$NON-NLS-1$
								rm.done();
								return;
							}

							// Create the data object.
							rm.setData(new VariableData(
									getData().getMIFrames()[frameDmc.fLevel].getArgs()[miVariableDmc.fIndex]));
							rm.done();
						}

						@Override
						protected void handleError() {
							// Unable to get the values.  This can happen with uninitialized values and pretty printers (bug 307614)
							// Let's try to ask for the arguments without their values, which is better than nothing
							fMICommandCache.execute(fCommandFactory.createMIStackListArguments(execDmc, false),
									new DataRequestMonitor<MIStackListArgumentsInfo>(getExecutor(), rm) {
										@Override
										protected void handleSuccess() {
											// Find the correct frame and argument
											if (frameDmc.fLevel >= getData().getMIFrames().length
													|| miVariableDmc.fIndex >= getData().getMIFrames()[frameDmc.fLevel]
															.getArgs().length) {
												rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
														INVALID_HANDLE, "Invalid variable " + miVariableDmc, null)); //$NON-NLS-1$
												rm.done();
												return;
											}

											// Create the data object.
											rm.setData(new VariableData(getData().getMIFrames()[frameDmc.fLevel]
													.getArgs()[miVariableDmc.fIndex]));
											rm.done();
										}
									});
						}
					});
		} else if (miVariableDmc.fType == MIVariableDMC.Type.LOCAL) {
			fMICommandCache.execute(
					// Don't ask for value when we are visualizing trace data, since some
					// data will not be there, and the command will fail
					fCommandFactory.createMIStackListLocals(frameDmc, !fTraceVisualization),
					new DataRequestMonitor<MIStackListLocalsInfo>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							// Create the data object.
							MIArg[] locals = getData().getLocals();
							if (locals.length > miVariableDmc.fIndex) {
								rm.setData(new VariableData(locals[miVariableDmc.fIndex]));
							} else {
								rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
										"Invalid variable " + miVariableDmc, null)); //$NON-NLS-1$
							}
							rm.done();
						}

						@Override
						protected void handleError() {
							// Unable to get the value.  This can happen with uninitialized values and pretty printers (bug 307614).
							// Let's try to ask for the variables without their values, which is better than nothing
							fMICommandCache.execute(fCommandFactory.createMIStackListLocals(frameDmc, false),
									new DataRequestMonitor<MIStackListLocalsInfo>(getExecutor(), rm) {
										@Override
										protected void handleSuccess() {
											// Create the data object.
											MIArg[] locals = getData().getLocals();
											if (locals.length > miVariableDmc.fIndex) {
												rm.setData(new VariableData(locals[miVariableDmc.fIndex]));
											} else {
												rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
														INVALID_HANDLE, "Invalid variable " + miVariableDmc, null)); //$NON-NLS-1$
											}
											rm.done();
										}
									});
						}
					});
		} else if (miVariableDmc.fType == MIVariableDMC.Type.RETURN_VALUES) {
			VariableData var = fThreadToReturnVariable.get(execDmc);
			if (var != null) {
				rm.setData(var);
			} else {
				rm.setStatus(
						new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Return value not found", null)); //$NON-NLS-1$
			}
			rm.done();
		} else {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
					"Invalid variable type " + miVariableDmc.fType, null)); //$NON-NLS-1$
		}

	}

	private MIVariableDMC[] makeVariableDMCs(IFrameDMContext frame, MIVariableDMC.Type type, MIArg[] miArgs) {
		// Use LinkedHashMap in order to keep the original ordering.
		// We don't currently support variables with the same name in the same frame,
		// so we only keep the first one.
		// Bug 327621 and 328573
		Map<String, MIVariableDMC> variableNames = new LinkedHashMap<>();

		for (int i = 0; i < miArgs.length; i++) {
			String name = miArgs[i].getName();
			MIVariableDMC var = variableNames.get(name);

			if (var == null) {
				variableNames.put(name, new MIVariableDMC(this, frame, type, i));
			}
		}

		return variableNames.values().toArray(new MIVariableDMC[0]);
	}

	/**
	 * Retrieves variables which are used to store the return values of functions.
	 */
	private void getReturnValues(IFrameDMContext frameDmc, DataRequestMonitor<IVariableDMContext[]> rm) {
		IVariableDMContext[] values = new IVariableDMContext[0];

		// Return values are only relevant for the top stack-frame
		if (!fTraceVisualization && frameDmc.getLevel() == 0) {
			IMIExecutionDMContext threadDmc = DMContexts.getAncestorOfType(frameDmc, IMIExecutionDMContext.class);
			VariableData var = fThreadToReturnVariable.get(threadDmc);
			if (var != null) {
				values = new IVariableDMContext[1];
				values[0] = new MIVariableDMC(this, frameDmc, MIVariableDMC.Type.RETURN_VALUES, 0);
			}
		}
		rm.done(values);
	}

	@Override
	public void getLocals(final IFrameDMContext frameDmc, final DataRequestMonitor<IVariableDMContext[]> rm) {

		final List<IVariableDMContext> localsList = new ArrayList<>();

		final CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm) {
			@Override
			protected void handleSuccess() {
				rm.setData(localsList.toArray(new IVariableDMContext[localsList.size()]));
				rm.done();
			}
		};
		countingRm.setDoneCount(3);

		// First show any return values of methods
		getReturnValues(frameDmc, new DataRequestMonitor<IVariableDMContext[]>(getExecutor(), countingRm) {
			@Override
			protected void handleSuccess() {
				localsList.addAll(Arrays.asList(getData()));
				countingRm.done();
			}
		});

		// Then show arguments
		getArguments(frameDmc, new DataRequestMonitor<IVariableDMContext[]>(getExecutor(), countingRm) {
			@Override
			protected void handleSuccess() {
				localsList.addAll(Arrays.asList(getData()));
				countingRm.done();
			}
		});

		// Finally get the local variables
		fMICommandCache.execute(
				// We don't actually need to ask for the values in this case, but since
				// we will ask for them right after, it is more efficient to ask for them now
				// so as to cache the result.  If the command fails, then we will ask for
				// the result without the values
				// Don't ask for value when we are visualizing trace data, since some
				// data will not be there, and the command will fail
				fCommandFactory.createMIStackListLocals(frameDmc, !fTraceVisualization),
				new DataRequestMonitor<MIStackListLocalsInfo>(getExecutor(), countingRm) {
					@Override
					protected void handleSuccess() {
						localsList.addAll(Arrays
								.asList(makeVariableDMCs(frameDmc, MIVariableDMC.Type.LOCAL, getData().getLocals())));
						countingRm.done();
					}

					@Override
					protected void handleError() {
						// If the command fails it can be because we asked for values.
						// This can happen with uninitialized values and pretty printers (bug 307614).
						// Since asking for values was simply an optimization
						// to store the command in the cache, let's retry the command without asking for values.
						fMICommandCache.execute(fCommandFactory.createMIStackListLocals(frameDmc, false),
								new DataRequestMonitor<MIStackListLocalsInfo>(getExecutor(), countingRm) {
									@Override
									protected void handleSuccess() {
										localsList.addAll(Arrays.asList(makeVariableDMCs(frameDmc,
												MIVariableDMC.Type.LOCAL, getData().getLocals())));
										countingRm.done();
									}
								});
					}
				});
	}

	@Override
	public void getStackDepth(final IDMContext dmc, final int maxDepth, final DataRequestMonitor<Integer> rm) {
		final IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(dmc, IMIExecutionDMContext.class);
		if (execDmc != null) {
			// Make sure the thread is stopped
			if (!fTraceVisualization && !fRunControl.isSuspended(execDmc)) {
				rm.setData(0);
				rm.done();
				return;
			}

			String threadId = execDmc.getThreadId();
			// Check our internal cache first because different commands can
			// still be re-used.
			int depth = fFramesCache.getThreadFramesCache(threadId).getStackDepth(maxDepth);
			if (depth > 0) {
				rm.setData(depth);
				rm.done();
				return;
			}

			ICommand<MIStackInfoDepthInfo> depthCommand = null;
			if (maxDepth > 0) {
				depthCommand = fCommandFactory.createMIStackInfoDepth(execDmc, maxDepth);
			} else {
				depthCommand = fCommandFactory.createMIStackInfoDepth(execDmc);
			}

			fMICommandCache.execute(depthCommand, new DataRequestMonitor<MIStackInfoDepthInfo>(getExecutor(), rm) {
				@Override
				protected void handleSuccess() {
					// Store result in our internal cache
					int stackDepth = getData().getDepth();
					fFramesCache.update(threadId, stackDepth, maxDepth);
					rm.setData(stackDepth);
					rm.done();
				}

				@Override
				protected void handleError() {
					if (fTraceVisualization) {
						// when visualizing trace data with GDB 7.2, the command
						// -stack-info-depth will return an error if we ask for any level
						// that GDB does not know about.  We would have to iteratively
						// try different depths until we found the deepest that succeeds.
						// That is too much of a hack, especially since GDB 7.3 answers correctly.
						// For 7.2, we can safely say we have one stack
						// frame, which is going to be the case for 95% of the cases.
						// To have more stack frames, the user would have to have collected
						// the registers and enough stack memory for GDB to build another frame.
						rm.setData(1);
						rm.done();
					} else {
						// gdb fails when being asked for the stack depth but stack frames command succeeds
						// it seems like an overkill but it will cached and ui later will ask for it anyway
						fMICommandCache.execute(createMIStackListFrames(execDmc, 0, maxDepth - 1),
								new DataRequestMonitor<MIStackListFramesInfo>(getExecutor(), rm) {
									@Override
									protected void handleSuccess() {
										FramesCacheInfo info = fFramesCache.update(threadId, getData());
										int depth = info.getValidStackDepth();
										fFramesCache.update(threadId, depth, maxDepth); // update maxDepth for stack depth cache
										rm.done(depth);
									}

									@Override
									protected void handleError() {
										// Lets return that we have 5 frames, if we return just 1 front end will never ask
										// for more. There is chance that gdb will actually return correct frames later
										// and one frame is not enough in many case to debug anything
										rm.done(fFramesCache.getThreadFramesCache(threadId).getValidStackDepth());
									}
								});
					}
				}
			});
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
			rm.done();
		}
	}

	/**
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@DsfServiceEventHandler
	public void eventDispatched(IResumedDMEvent e) {
		fMICommandCache.setContextAvailable(e.getDMContext(), false);
		if (e.getReason() != StateChangeReason.STEP) {
			fCachedStoppedEvent = null;
			fMICommandCache.reset();
			fFramesCache.clear();
		}

		handleReturnValues(e);
	}

	private void handleReturnValues(IResumedDMEvent e) {
		// Whenever the execution resumes, we can clear any
		// return values of previous methods for the resuming
		// thread context.  For all-stop mode, we get a container event here,
		// and we can clear the entire list, which should contain at most one
		// value for all-stop.
		if (e instanceof IContainerResumedDMEvent) {
			// All-stop mode
			assert fThreadToReturnVariable.size() <= 1;
			fThreadToReturnVariable.clear();
		} else {
			// Non-stop mode
			IDMContext ctx = e.getDMContext();
			if (ctx instanceof IMIExecutionDMContext) {
				fThreadToReturnVariable.remove(ctx);
			} else if (ctx instanceof IContainerDMContext) {
				fThreadToReturnVariable.clear();
			}
		}
	}

	/**
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 1.1
	 */
	@DsfServiceEventHandler
	public void eventDispatched(ISuspendedDMEvent e) {
		fMICommandCache.setContextAvailable(e.getDMContext(), true);
		fMICommandCache.reset();
		fFramesCache.clear();

		handleReturnValues(e);
	}

	private void handleReturnValues(ISuspendedDMEvent e) {
		// Process MIFunctionFinishedEvent from within the ISuspendedDMEvent
		// instead of MIStoppedEvent.
		// This avoids a race conditions where the actual MIFunctionFinishedEvent
		// can arrive here, faster that a preceding IResumedDMEvent
		if (e instanceof IMIDMEvent) {
			Object miEvent = ((IMIDMEvent) e).getMIEvent();
			if (miEvent instanceof MIFunctionFinishedEvent) {
				// When returning out of a function, we want to show the return value
				// for the thread that finished the call.  To do that, we store
				// the variable in which GDB stores that return value, and we do
				// that for the proper thread.

				IMIExecutionDMContext finishedEventThread = null;
				if (e instanceof IContainerSuspendedDMEvent) {
					// All-stop mode
					IExecutionDMContext[] triggerContexts = ((IContainerSuspendedDMEvent) e).getTriggeringContexts();
					if (triggerContexts.length != 0 && triggerContexts[0] instanceof IMIExecutionDMContext) {
						finishedEventThread = (IMIExecutionDMContext) triggerContexts[0];
					}
				} else {
					// Non-stop mode
					IDMContext ctx = e.getDMContext();
					if (ctx instanceof IMIExecutionDMContext) {
						finishedEventThread = (IMIExecutionDMContext) ctx;
					}
				}

				if (finishedEventThread != null) {
					String name = ((MIFunctionFinishedEvent) miEvent).getGDBResultVar();
					String value = ((MIFunctionFinishedEvent) miEvent).getReturnValue();

					if (name != null && !name.isEmpty() && value != null && !value.isEmpty()) {
						fThreadToReturnVariable.put(finishedEventThread, new VariableData(new MIArg(name, value)));
					}
				}
			}
		}
	}

	/**
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 1.1
	 */
	@DsfServiceEventHandler
	public void eventDispatched(IMIDMEvent e) {
		if (e.getMIEvent() instanceof MIStoppedEvent) {
			fCachedStoppedEvent = (MIStoppedEvent) e.getMIEvent();
		}
	}

	/** @since 3.0 */
	@DsfServiceEventHandler
	public void eventDispatched(ITraceRecordSelectedChangedDMEvent e) {
		if (e.isVisualizationModeEnabled()) {
			fTraceVisualization = true;
		} else {
			fTraceVisualization = false;
			fCachedStoppedEvent = null;
		}
	}

	/**
	 * {@inheritDoc}
	 * @since 1.1
	 */
	@Override
	public void flushCache(IDMContext context) {
		fMICommandCache.reset(context);
		fFramesCache.clear(context);
		fCachedStoppedEvent = null;
	}

}

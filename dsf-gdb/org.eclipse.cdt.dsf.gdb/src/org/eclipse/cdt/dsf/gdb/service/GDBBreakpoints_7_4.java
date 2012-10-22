/*******************************************************************************
 * Copyright (c) 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.breakpointactions.BreakpointActionManager;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICTracepoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpointsExtension;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.CollectAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.EvaluateAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.ITracepointAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.TracepointActionManager;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.WhileSteppingAction;
import org.eclipse.cdt.dsf.mi.service.IMIBreakpointsExtension;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MINotifyAsyncOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MITuple;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;

/**
 * @since 4.2
 */
public class GDBBreakpoints_7_4 extends GDBBreakpoints_7_2 implements IEventListener, IMIBreakpointsExtension {

    // Breakpoint notifications
    private static final String BREAKPOINT_PREFIX = "breakpoint-"; //$NON-NLS-1$
    private static final String BREAKPOINT_CREATED = BREAKPOINT_PREFIX + "created"; //$NON-NLS-1$
    private static final String BREAKPOINT_MODIFIED = BREAKPOINT_PREFIX + "modified"; //$NON-NLS-1$
    private static final String BREAKPOINT_DELETED = BREAKPOINT_PREFIX + "deleted"; //$NON-NLS-1$

    // GDB tracepoint commands
    private static final String TC_COLLECT = "collect "; //$NON-NLS-1$
    private static final String TC_TEVAL = "teval "; //$NON-NLS-1$
    private static final String TC_WHILE_STEPPING = "while-stepping "; //$NON-NLS-1$
    private static final String TC_END = "end"; //$NON-NLS-1$
    
	private IMICommandControl fConnection;
	private boolean fIsTracking = false;

	// Collection of breakpoints created from the GDB console or outside of Eclipse
	private Map<IBreakpointsTargetDMContext, Map<Integer, MIBreakpoint>> fCreatedTargetBreakpoints;

	// Collection of breakpoints deleted from the GDB console or outside of Eclipse
	private Map<IBreakpointsTargetDMContext, Set<Integer>> fDeletedTargetBreakpoints;
	
	// Collection of pending breakpoint modifications
	private Map<IBreakpointsTargetDMContext, Map<Integer, List<MIBreakpoint>>> fPendingModifications;

	public GDBBreakpoints_7_4(DsfSession session) {
		super(session);
		fCreatedTargetBreakpoints = new HashMap<IBreakpointsTargetDMContext, Map<Integer, MIBreakpoint>>();
		fDeletedTargetBreakpoints = new HashMap<IBreakpoints.IBreakpointsTargetDMContext, Set<Integer>>();
		fPendingModifications = new HashMap<IBreakpointsTargetDMContext, Map<Integer, List<MIBreakpoint>>>();
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

	private void doInitialize(final RequestMonitor rm) {
		fConnection = getServicesTracker().getService(IMICommandControl.class);
		if (fConnection == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Service is not available")); //$NON-NLS-1$
			rm.done();
			return;
		}
		fConnection.addEventListener(this);

		// Register this service
		register(new String[] { IBreakpoints.class.getName(),
		                        IBreakpointsExtension.class.getName(),
								MIBreakpoints.class.getName(),
								GDBBreakpoints_7_0.class.getName(),
								GDBBreakpoints_7_2.class.getName(),
								GDBBreakpoints_7_4.class.getName() },
				new Hashtable<String, String>());

		rm.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		fCreatedTargetBreakpoints.clear();
		fDeletedTargetBreakpoints.clear();
		fPendingModifications.clear();
		ICommandControl control = getCommandControl();
		if (control != null) {
			control.removeEventListener(this);
		}
        unregister();
		super.shutdown(requestMonitor);
	}

	@Override
	public void eventReceived(Object output) {
		if (output instanceof MIOutput) {
			MIOOBRecord[] records = ((MIOutput)output).getMIOOBRecords();
			for(MIOOBRecord r : records) {
				if (r instanceof MINotifyAsyncOutput) {
					MINotifyAsyncOutput notifyOutput = (MINotifyAsyncOutput)r;
					String asyncClass = notifyOutput.getAsyncClass();
					if (BREAKPOINT_CREATED.equals(asyncClass)) {
						MIBreakpoint bpt = getMIBreakpointFromOutput(notifyOutput);
						if (bpt != null)
							targetBreakpointCreated(bpt);
					}
					else if (BREAKPOINT_DELETED.equals(asyncClass)) {
						int id = getMIBreakpointIdFromOutput(notifyOutput);
						if (id != 0)
							targetBreakpointDeleted(id);
					}
					else if (BREAKPOINT_MODIFIED.equals(asyncClass)) {
						MIBreakpoint bpt = getMIBreakpointFromOutput(notifyOutput);
						if (bpt != null)
							targetBreakpointModified(bpt);
					}
				}
			}
		}
	}

	@Override
	public void breakpointTrackingStarted() {
		fIsTracking = true;
	}

	@Override
	public void breakpointTrackingStopped() {
		fIsTracking = false;
	}

	protected IMICommandControl getCommandControl() {
		return fConnection;
	}
	
	private MIBreakpoint getMIBreakpointFromOutput(MINotifyAsyncOutput notifyOutput) {
		MIBreakpoint bpt = null;
		MIResult[] results = notifyOutput.getMIResults();
		for(int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue val = results[i].getMIValue();
			if (var.equals("bkpt")) { //$NON-NLS-1$
				if (val instanceof MITuple) {
					bpt = new MIBreakpoint((MITuple)val);
					break;
				}
			}
		}
		return bpt;
	}

	private int getMIBreakpointIdFromOutput(MINotifyAsyncOutput notifyOutput) {
		MIResult[] results = notifyOutput.getMIResults();
		for(int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue val = results[i].getMIValue();
			if (var.equals("id") && val != null && val instanceof MIConst) { //$NON-NLS-1$
            	try {
					return Integer.parseInt(((MIConst)val).getCString().trim());
				}
				catch(NumberFormatException e) {
					GdbPlugin.log(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Invalid breakpoint id")); //$NON-NLS-1$
				}
			}
		}
		return 0;
	}

	protected void targetBreakpointCreated(final MIBreakpoint miBpt) {
		if (miBpt.isCatchpoint())
			return;
		ICommandControlService commandControl = getCommandControl();
		final MIBreakpointsManager bm = getServicesTracker().getService(MIBreakpointsManager.class);
		ISourceLookup sourceLookup = getServicesTracker().getService(ISourceLookup.class);
		if (commandControl == null || sourceLookup == null || bm == null)
			return;

		final IBreakpointsTargetDMContext bpTargetDMC = getBreakpointsTargetContext(commandControl, miBpt);
		if (bpTargetDMC == null)
			return;

		Map<Integer, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(bpTargetDMC);
		if (contextBreakpoints == null) {
			contextBreakpoints = createNewBreakpointMap(bpTargetDMC);
		}
		contextBreakpoints.put(Integer.valueOf(miBpt.getNumber()), new MIBreakpointDMData(miBpt));

		Map<Integer, MIBreakpoint> targetMap = fCreatedTargetBreakpoints.get(bpTargetDMC);
		if (targetMap == null) {
			targetMap = new HashMap<Integer, MIBreakpoint>();
			fCreatedTargetBreakpoints.put(bpTargetDMC, targetMap);
		}
		targetMap.put(Integer.valueOf(miBpt.getNumber()), miBpt);

    	String debuggerPath = (miBpt.getFullName() != null && !miBpt.getFullName().isEmpty()) ? 
    			miBpt.getFullName() : miBpt.getFile();
        getSource(
        	sourceLookup,
        	bpTargetDMC,
        	debuggerPath, 
        	new DataRequestMonitor<String>(getExecutor(), null) {
            	@Override
            	@ConfinedToDsfExecutor( "fExecutor" )
            	protected void handleSuccess() {
            		String fileName = getData();
            		if (fileName == null)
            			fileName = miBpt.getFullName();
        			ICBreakpoint plBpt = getPlatformBreakpoint(miBpt, fileName);
					try {
						if (plBpt != null) {
							// A corresponding platform breakpoint already exists. 
							// If the breakpoint tracking has already started we need
							// to increment the install count, otherwise the breakpoint 
							// will be processed as an initial breakpoint.
							if (fIsTracking)
								bm.breakpointAdded(plBpt);
							// Make sure the platform breakpoint's parameters are synchronized 
							// with the target breakpoint.
							targetBreakpointModified(miBpt);
						}
						else {
							createPlatformBreakpoint(fileName, miBpt);
							// Apply all pending modifications
							Map<Integer, List<MIBreakpoint>> map = fPendingModifications.remove(bpTargetDMC);
							if (map != null) {
								List<MIBreakpoint> list = map.get(Integer.valueOf(miBpt.getNumber()));
								if (list != null) {
									for (MIBreakpoint mod : list) {
										targetBreakpointModified(mod);
									}
								}
							}
							else {
							}
						}
					}
					catch(CoreException e) {
						GdbPlugin.log(getStatus());
					}
            		super.handleSuccess();
            	}
            });
	}

	protected void targetBreakpointDeleted(int id) {
		MIBreakpointsManager bm = getServicesTracker().getService(MIBreakpointsManager.class);
		if (bm == null)
			return;
		for (IBreakpointsTargetDMContext bpTargetDMC : getBreakpointTargetContexts(id)) {
			Set<Integer> set = fDeletedTargetBreakpoints.get(bpTargetDMC);
			if (set == null) {
				set = new HashSet<Integer>();
				fDeletedTargetBreakpoints.put(bpTargetDMC, set);
			}
			set.add(Integer.valueOf(id));
			IBreakpoint plBpt = bm.findPlatformBreakpoint(
					new MIBreakpointDMContext(this, new IDMContext[] { bpTargetDMC }, id));
			if (plBpt instanceof ICBreakpoint) {
				bm.uninstallBreakpoint(bpTargetDMC, (ICBreakpoint)plBpt, new RequestMonitor(getExecutor(), null));
			}
		}
	}

	protected void targetBreakpointModified(final MIBreakpoint miBpt) {
		if (miBpt.isCatchpoint())
			return;
		ICommandControlService commandControl = getCommandControl();
		final MIBreakpointsManager bm = getServicesTracker().getService(MIBreakpointsManager.class);
		if (commandControl != null && bm != null) {
			final IBreakpointsTargetDMContext bpTargetDMC = getBreakpointsTargetContext(commandControl, miBpt);
			if (bpTargetDMC == null)
				return;
			final Map<Integer, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(bpTargetDMC);
			if (contextBreakpoints == null)
				return;
			IBreakpoint b = bm.findPlatformBreakpoint(
					new MIBreakpointDMContext(this, new IDMContext[] { bpTargetDMC }, miBpt.getNumber()));
			if (!(b instanceof ICBreakpoint)) {
				ISourceLookup sourceLookup = getServicesTracker().getService(ISourceLookup.class);
		        if (sourceLookup != null) {
		        	String debuggerPath = (miBpt.getFullName() != null && !miBpt.getFullName().isEmpty()) ? 
		        			miBpt.getFullName() : miBpt.getFile();
		            getSource(
		            	sourceLookup,
		            	bpTargetDMC,
		            	debuggerPath, 
		            	new DataRequestMonitor<String>(getExecutor(), null) {
			            	@Override
			            	@ConfinedToDsfExecutor("fExecutor")
			            	protected void handleSuccess() {
			            		String fileName = getData();
			            		if (fileName != null) {
			            			ICBreakpoint plBpt = getPlatformBreakpoint(miBpt, fileName);
									if (plBpt != null) {
										targetBreakpointModified(plBpt, miBpt, contextBreakpoints);
			            			}
									else {
										// At this point the corresponding platform breakpoint may not be created yet.
										// The reason is the source lookup job runs in a separate thread, so there is 
										// a race between the creation and modification source lookup jobs.
										// Add the current modification data to the list of pending modifications.
										// It will be picked up and applied when the platform breakpoint is created.
										Map<Integer, List<MIBreakpoint>> map = fPendingModifications.get(bpTargetDMC);
										if (map == null) {
											map = new HashMap<Integer, List<MIBreakpoint>>();
											fPendingModifications.put(bpTargetDMC, map);
										}
										List<MIBreakpoint> list = map.get(Integer.valueOf(miBpt.getNumber()));
										if (list == null) {
											list = new ArrayList<MIBreakpoint>();
											map.put(Integer.valueOf(miBpt.getNumber()), list);
										}
										list.add(miBpt);
									}
			            		}
			            		super.handleSuccess();
			            	}
			            });
		        }
			}
			else {
				ICBreakpoint plBpt = (ICBreakpoint)b;
				targetBreakpointModified(plBpt, miBpt, contextBreakpoints);
			}
		}
	}

	private void targetBreakpointModified(
			ICBreakpoint plBpt, 
			MIBreakpoint miBpt, 
			Map<Integer, MIBreakpointDMData> contextBreakpoints) {
		MIBreakpointDMData oldData = contextBreakpoints.get(Integer.valueOf(miBpt.getNumber()));
		contextBreakpoints.put(Integer.valueOf(miBpt.getNumber()), new MIBreakpointDMData(miBpt));
		try {
			if (plBpt.isEnabled() != miBpt.isEnabled()) {
				plBpt.setEnabled(miBpt.isEnabled());
			}
			if (!plBpt.getCondition().equals(miBpt.getCondition())) {
				plBpt.setCondition(miBpt.getCondition());
			}
			if (plBpt.getIgnoreCount() != miBpt.getIgnoreCount()) {
				plBpt.setIgnoreCount(miBpt.getIgnoreCount());
			}
			if (oldData.isPending() != miBpt.isPending()) {
				if (miBpt.isPending())
					plBpt.decrementInstallCount();
				else
					plBpt.incrementInstallCount();
			}
			if (plBpt instanceof ICTracepoint
				&& miBpt.isTracepoint()
				&& ((ICTracepoint)plBpt).getPassCount() != miBpt.getPassCount()) {
				((ICTracepoint)plBpt).setPassCount(miBpt.getPassCount());
			}
			if (plBpt instanceof ICTracepoint
				&& miBpt.isTracepoint()
				&& !miBpt.getCommands().equals(plBpt.getMarker().getAttribute(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE))) {
				StringBuilder sb = new StringBuilder();
				boolean first = true;
				String[] commands = miBpt.getCommands().split(TracepointActionManager.TRACEPOINT_ACTION_DELIMITER);
				for (ITracepointAction action : getActionsFromCommands(commands)) {
					if (first)
						first = false;
					else
						sb.append(TracepointActionManager.TRACEPOINT_ACTION_DELIMITER);
					sb.append(action.getName());
				}
				plBpt.getMarker().setAttribute(
					BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE, sb.toString());
			}
		}
		catch(CoreException e) {
			contextBreakpoints.put(Integer.valueOf(miBpt.getNumber()), oldData);
			GdbPlugin.log(e.getStatus());
		}
	}

	private ICBreakpoint getPlatformBreakpoint(MIBreakpoint miBpt, String fileName) {
    	ICBreakpoint result = null;
    	for (IBreakpoint b : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints()) {
    		if (b instanceof ICTracepoint 
    			&& miBpt.isTracepoint()
    			&& isPlatformTracepoint((ICTracepoint)b, miBpt, fileName)) {
    			 result = (ICBreakpoint)b;
    		}
    		if (b instanceof ICWatchpoint 
    			&& miBpt.isWatchpoint()
    	    	&& isPlatformWatchpoint((ICWatchpoint)b, miBpt)) {
    			result = (ICBreakpoint)b;
    		}
    		if (b instanceof ICLineBreakpoint 
    			&& !miBpt.isWatchpoint()
    			&& !miBpt.isCatchpoint()
    			&& !miBpt.isTracepoint()
    	    	&& isPlatformLineBreakpoint((ICLineBreakpoint)b, miBpt, fileName)) {
    			result = (ICBreakpoint)b;
    		}
    	}
    	return result;
	}

	private void createPlatformBreakpoint(String fileName, MIBreakpoint miBpt) throws CoreException {
		if (miBpt.isWatchpoint()) {
			createPlatformWatchpoint(fileName, miBpt);
		}
		else if (miBpt.isTracepoint()) {
			createPlatformTracepoint(fileName, miBpt);
		}
		else {
			createPlatformLocationBreakpoint(fileName, miBpt);
		}
	}

	private void createPlatformLocationBreakpoint(String fileName, MIBreakpoint miBpt) throws CoreException {
		if (isAddressBreakpoint(miBpt)) {
			createPlatformAddressBreakpoint(fileName, miBpt);
		}
		else if (isFunctionBreakpoint(miBpt)) {
			createPlatformFunctionBreakpoint(fileName, miBpt);
		}
		else {
			createPlatformLineBreakpoint(fileName, miBpt);
		}
	}

	private void createPlatformAddressBreakpoint(String fileName, MIBreakpoint miBpt) {
		IResource resource = null;
		if (fileName == null || fileName.isEmpty())
			resource = ResourcesPlugin.getWorkspace().getRoot();
		else {
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(
					URI.create(String.format("file:/%s", fileName))); //$NON-NLS-1$
			if (files.length > 0) {
				resource = files[0];
			}
			else {
				resource = ResourcesPlugin.getWorkspace().getRoot();
			}
		}

		int type = 0;
		if (miBpt.isTemporary())
			type |= ICBreakpointType.TEMPORARY;
		if (miBpt.isHardware())
			type |= ICBreakpointType.HARDWARE;

		try {
			CDIDebugModel.createAddressBreakpoint(
					null, 
					null, 
					resource, 
					type, 
					getPlatformAddress(miBpt.getAddress()), 
					miBpt.isEnabled(), 
					miBpt.getIgnoreCount(), 
					miBpt.getCondition(),
					true);
		}
		catch(CoreException e) {
			GdbPlugin.log(e.getStatus());
		}
		catch(NumberFormatException e) {
			GdbPlugin.log(new Status(IStatus.ERROR, GdbPlugin.getUniqueIdentifier(), 
					String.format("Invalid breakpoint addres: %s", miBpt.getAddress()))); //$NON-NLS-1$
		}
	}

	private void createPlatformFunctionTracepoint(String fileName, MIBreakpoint miBpt) {
		IResource resource = null;
		if (fileName == null || fileName.isEmpty())
			resource = ResourcesPlugin.getWorkspace().getRoot();
		else {
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(
					URI.create(String.format("file:/%s", fileName))); //$NON-NLS-1$
			if (files.length > 0) {
				resource = files[0];
			}
			else {
				resource = ResourcesPlugin.getWorkspace().getRoot();
			}
		}

		int type = 0;
		if (miBpt.isTemporary())
			type |= ICBreakpointType.TEMPORARY;
		if (miBpt.isHardware())
			type |= ICBreakpointType.HARDWARE;
		
		try {
			CDIDebugModel.createFunctionBreakpoint(
					fileName, 
					resource, 
					type, 
					miBpt.getFunction(), 
					-1, 
					-1, 
					miBpt.getLine(), 
					miBpt.isEnabled(), 
					miBpt.getIgnoreCount(), 
					miBpt.getCondition(),
					true);
		}
		catch(CoreException e) {
			GdbPlugin.log(e.getStatus());
		}
	}

	private void createPlatformLineTracepoint(String fileName, MIBreakpoint miBpt) throws CoreException {
		IResource resource = null;
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(
				URI.create(String.format("file:/%s", fileName))); //$NON-NLS-1$
		if (files.length > 0) {
			resource = files[0];
		}
		
		int type = 0;
		if (miBpt.isTemporary())
			type |= ICBreakpointType.TEMPORARY;
		if (miBpt.isHardware())
			type |= ICBreakpointType.HARDWARE;
		
		CDIDebugModel.createLineTracepoint(
				fileName, 
				resource, 
				type, 
				miBpt.getLine(), 
				miBpt.isEnabled(), 
				miBpt.getIgnoreCount(), 
				miBpt.getCondition(), 
				true);
	}

	private void createPlatformTracepoint(String fileName, MIBreakpoint miBpt) throws CoreException {
		if (isAddressBreakpoint(miBpt)) {
			createPlatformAddressTracepoint(fileName, miBpt);
		}
		else if (isFunctionBreakpoint(miBpt)) {
			createPlatformFunctionTracepoint(fileName, miBpt);
		}
		else {
			createPlatformLineTracepoint(fileName, miBpt);
		}
	}

	private void createPlatformAddressTracepoint(String fileName, MIBreakpoint miBpt) {
		IResource resource = null;
		if (fileName == null || fileName.isEmpty())
			resource = ResourcesPlugin.getWorkspace().getRoot();
		else {
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(
					URI.create(String.format("file:/%s", fileName))); //$NON-NLS-1$
			if (files.length > 0) {
				resource = files[0];
			}
			else {
				resource = ResourcesPlugin.getWorkspace().getRoot();
			}
		}

		int type = 0;
		if (miBpt.isTemporary())
			type |= ICBreakpointType.TEMPORARY;
		if (miBpt.isHardware())
			type |= ICBreakpointType.HARDWARE;

		try {
			CDIDebugModel.createAddressTracepoint(
					null, 
					null, 
					resource, 
					type,
					miBpt.getLine(),
					getPlatformAddress(miBpt.getAddress()), 
					miBpt.isEnabled(), 
					miBpt.getIgnoreCount(), 
					miBpt.getCondition(),
					true);
		}
		catch(CoreException e) {
			GdbPlugin.log(e.getStatus());
		}
		catch(NumberFormatException e) {
			GdbPlugin.log(new Status(IStatus.ERROR, GdbPlugin.getUniqueIdentifier(), 
					String.format("Invalid breakpoint addres: %s", miBpt.getAddress()))); //$NON-NLS-1$
		}
	}

	private void createPlatformFunctionBreakpoint(String fileName, MIBreakpoint miBpt) {
		IResource resource = null;
		if (fileName == null || fileName.isEmpty())
			resource = ResourcesPlugin.getWorkspace().getRoot();
		else {
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(
					URI.create(String.format("file:/%s", fileName))); //$NON-NLS-1$
			if (files.length > 0) {
				resource = files[0];
			}
			else {
				resource = ResourcesPlugin.getWorkspace().getRoot();
			}
		}

		int type = 0;
		if (miBpt.isTemporary())
			type |= ICBreakpointType.TEMPORARY;
		if (miBpt.isHardware())
			type |= ICBreakpointType.HARDWARE;
		
		try {
			CDIDebugModel.createFunctionBreakpoint(
					fileName, 
					resource, 
					type, 
					miBpt.getFunction(), 
					-1, 
					-1, 
					miBpt.getLine(), 
					miBpt.isEnabled(), 
					miBpt.getIgnoreCount(), 
					miBpt.getCondition(),
					true);
		}
		catch(CoreException e) {
			GdbPlugin.log(e.getStatus());
		}
	}

	private void createPlatformLineBreakpoint(String fileName, MIBreakpoint miBpt) throws CoreException {
		IResource resource = null;
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(
				URI.create(String.format("file:/%s", fileName))); //$NON-NLS-1$
		if (files.length > 0) {
			resource = files[0];
		}
		if (resource == null)
			resource = ResourcesPlugin.getWorkspace().getRoot();

		int type = 0;
		if (miBpt.isTemporary())
			type |= ICBreakpointType.TEMPORARY;
		if (miBpt.isHardware())
			type |= ICBreakpointType.HARDWARE;
		
		CDIDebugModel.createLineBreakpoint(
				fileName, 
				resource, 
				type, 
				miBpt.getLine(), 
				miBpt.isEnabled(), 
				miBpt.getIgnoreCount(), 
				miBpt.getCondition(), 
				true);
	}

	private void createPlatformWatchpoint(String fileName, MIBreakpoint miBpt) throws CoreException {
		IResource resource = null;
		if (fileName == null || fileName.isEmpty())
			resource = ResourcesPlugin.getWorkspace().getRoot();
		else {
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(
					URI.create(String.format("file:/%s", fileName))); //$NON-NLS-1$
			if (files.length > 0) {
				resource = files[0];
			}
			else {
				resource = ResourcesPlugin.getWorkspace().getRoot();
			}
		}
		
		CDIDebugModel.createWatchpoint(
				fileName, 
				resource, 
				miBpt.isAccessWatchpoint() || miBpt.isWriteWatchpoint(),
				miBpt.isAccessWatchpoint() || miBpt.isReadWatchpoint(),
				miBpt.getExpression(),
				miBpt.isEnabled(), 
				miBpt.getIgnoreCount(), 
				miBpt.getCondition(), 
				true);
	}

	@Override
	protected void addBreakpoint(IBreakpointsTargetDMContext context, Map<String, Object> attributes, DataRequestMonitor<IBreakpointDMContext> finalRm) {
		// Skip the breakpoints set from the console or from outside of Eclipse 
		// because they are already installed on the target.
		MIBreakpoint miBpt = getTargetBreakpoint(context, attributes);
		if (miBpt != null) {
			removeCreatedTargetBreakpoint(context, miBpt);
			MIBreakpointDMData newBreakpoint = new MIBreakpointDMData(miBpt);
			getBreakpointMap(context).put(newBreakpoint.getNumber(), newBreakpoint);
			IBreakpointDMContext dmc = new MIBreakpointDMContext(this, new IDMContext[] { context }, newBreakpoint.getNumber());
			finalRm.setData(dmc);

			getSession().dispatchEvent(new BreakpointAddedEvent(dmc), getProperties());

			finalRm.done();
			return;
		}
		super.addBreakpoint(context, attributes, finalRm);
	}

	@Override
	protected void addTracepoint(IBreakpointsTargetDMContext context, Map<String, Object> attributes, DataRequestMonitor<IBreakpointDMContext> drm) {
		// Skip the breakpoints set from the console or from outside of Eclipse 
		// because they are already installed on the target.
		MIBreakpoint miBpt = getTargetBreakpoint(context, attributes);
		if (miBpt != null) {
			removeCreatedTargetBreakpoint(context, miBpt);
			MIBreakpointDMData newBreakpoint = new MIBreakpointDMData(miBpt);
			getBreakpointMap(context).put(newBreakpoint.getNumber(), newBreakpoint);
			IBreakpointDMContext dmc = new MIBreakpointDMContext(this, new IDMContext[] { context }, newBreakpoint.getNumber());
			drm.setData(dmc);

			getSession().dispatchEvent(new BreakpointAddedEvent(dmc), getProperties());

			drm.done();
			return;
		}
		super.addTracepoint(context, attributes, drm);
	}

	@Override
	protected void addWatchpoint(IBreakpointsTargetDMContext context, Map<String, Object> attributes, DataRequestMonitor<IBreakpointDMContext> drm) {
		// Skip the breakpoints set from the console or from outside of Eclipse 
		// because they are already installed on the target.
		MIBreakpoint miBpt = getTargetBreakpoint(context, attributes);
		if (miBpt != null) {
			removeCreatedTargetBreakpoint(context, miBpt);
			MIBreakpointDMData newBreakpoint = new MIBreakpointDMData(miBpt);
			getBreakpointMap(context).put(newBreakpoint.getNumber(), newBreakpoint);
			IBreakpointDMContext dmc = new MIBreakpointDMContext(this, new IDMContext[] { context }, newBreakpoint.getNumber());
			drm.setData(dmc);

			getSession().dispatchEvent(new BreakpointAddedEvent(dmc), getProperties());

			drm.done();
			return;
		}
		super.addWatchpoint(context, attributes, drm);
	}

	@Override
	protected void deleteBreakpoint(IBreakpointsTargetDMContext context, int reference, RequestMonitor finalRm) {
		if (isTargetBreakpointDeleted(context, reference, true)) {
			finalRm.done();
			return;
		}
		super.deleteBreakpoint(context, reference, finalRm);
	}

	private IBreakpointsTargetDMContext getBreakpointsTargetContext(ICommandControlService commandControl, MIBreakpoint miBpt) {
		IMIProcesses processes = getServicesTracker().getService(IMIProcesses.class);
		if (processes == null)
			return null;
		String threadId = (miBpt != null) ? miBpt.getThreadId() : null;
		IContainerDMContext contContext = null;
		if (threadId == null || threadId.length() == 0)
			contContext = processes.createContainerContextFromGroupId(commandControl.getContext(), MIProcesses.UNIQUE_GROUP_ID);
		else
			contContext = processes.createContainerContextFromThreadId(commandControl.getContext(), threadId);
		if (contContext == null)
			return null;
		return DMContexts.getAncestorOfType(contContext, IBreakpointsTargetDMContext.class);
	}

	private MIBreakpoint getTargetBreakpoint(IBreakpointsTargetDMContext context, Map<String, Object> attributes) {
		Map<Integer, MIBreakpoint> map = fCreatedTargetBreakpoints.get(context);
		if (map == null)
			return null;
		String type = (String)attributes.get(MIBreakpoints.BREAKPOINT_TYPE);
		if (MIBreakpoints.BREAKPOINT.equals(type)) {
			return getTargetLineBreakpoint(
				map.values(),
				(String)attributes.get(MIBreakpoints.FILE_NAME), 
				(Integer)attributes.get(MIBreakpoints.LINE_NUMBER),
				(String)attributes.get(MIBreakpoints.FUNCTION),
				(String)attributes.get(MIBreakpoints.ADDRESS),
				(Boolean)attributes.get(MIBreakpointDMData.IS_HARDWARE),
				(Boolean)attributes.get(MIBreakpointDMData.IS_TEMPORARY));
			
		}
		else if (MIBreakpoints.TRACEPOINT.equals(type)) {
			return getTargetTracepoint(
				map.values(),
				(String)attributes.get(MIBreakpoints.FILE_NAME), 
				(Integer)attributes.get(MIBreakpoints.LINE_NUMBER),
				(String)attributes.get(MIBreakpoints.FUNCTION),
				(String)attributes.get(MIBreakpoints.ADDRESS),
				(Boolean)attributes.get(MIBreakpointDMData.IS_HARDWARE),
				(Boolean)attributes.get(MIBreakpointDMData.IS_TEMPORARY));
		}
		else if (MIBreakpoints.WATCHPOINT.equals(type)) {
			return getTargetWatchpoint(
				map.values(),
				(String)attributes.get(MIBreakpoints.EXPRESSION), 
				(Boolean)attributes.get(MIBreakpoints.READ),
				(Boolean)attributes.get(MIBreakpoints.WRITE),
				(Boolean)attributes.get(MIBreakpointDMData.IS_HARDWARE),
				(Boolean)attributes.get(MIBreakpointDMData.IS_TEMPORARY));
			
		}
		return null;
	}
	
	private MIBreakpoint getTargetLineBreakpoint(
			Collection<MIBreakpoint> targetBreakpoints, 
			String fileName, 
			Integer lineNumber,
			String function,
			String address,
			Boolean isHardware, 
			Boolean isTemporary) {
		for (MIBreakpoint miBpt : targetBreakpoints) {
			if (miBpt.isWatchpoint() || miBpt.isCatchpoint() || miBpt.isTracepoint())
				continue;
			if (isFunctionBreakpoint(miBpt) && function != null && !function.equals(miBpt.getFunction()))
				continue;
			if (isAddressBreakpoint(miBpt) && address != null 
				&& !address.equals(getPlatformAddress(miBpt.getAddress()).toHexAddressString())) 
					continue;
			if (isLineBreakpoint(miBpt)) {
				if (fileName != null && !fileName.equals(miBpt.getFullName()))
					continue;
				if (lineNumber != null && lineNumber.intValue() != miBpt.getLine())
					continue;
			}
			if (isHardware != null && isHardware.booleanValue() != miBpt.isHardware())
				continue;
			if (isTemporary != null && isTemporary.booleanValue() != miBpt.isHardware())
				continue;
			return miBpt;
		}
		return null;
	}
	
	private MIBreakpoint getTargetTracepoint(
			Collection<MIBreakpoint> targetBreakpoints, 
			String fileName, 
			Integer lineNumber,
			String function,
			String address,
			Boolean isHardware, 
			Boolean isTemporary) {
		for (MIBreakpoint miBpt : targetBreakpoints) {
			if (!miBpt.isTracepoint())
				continue;
			if (isFunctionBreakpoint(miBpt) && function != null && !function.equals(miBpt.getFunction()))
				continue;
			if (isAddressBreakpoint(miBpt) && address != null 
				&& !address.equals(getPlatformAddress(miBpt.getAddress()).toHexAddressString())) 
					continue;
			if (isLineBreakpoint(miBpt)) {
				if (fileName != null && !fileName.equals(miBpt.getFullName()))
					continue;
				if (lineNumber != null && lineNumber.intValue() != miBpt.getLine())
					continue;
			}
			if (isHardware != null && isHardware.booleanValue() != miBpt.isHardware())
				continue;
			if (isTemporary != null && isTemporary.booleanValue() != miBpt.isHardware())
				continue;
			return miBpt;
		}
		return null;
	}
	
	private MIBreakpoint getTargetWatchpoint(
			Collection<MIBreakpoint> targetBreakpoints, 
			String expression,
			boolean readAccess,
			boolean writeAccess,
			Boolean isHardware, 
			Boolean isTemporary) {
		for (MIBreakpoint miBpt : targetBreakpoints) {
			if (!miBpt.isWatchpoint())
				continue;
			if (expression != null && !expression.equals(miBpt.getExpression()))
				continue;
			if (readAccess && writeAccess && !miBpt.isAccessWatchpoint())
				continue;
			if (readAccess && !writeAccess && !miBpt.isReadWatchpoint())
				continue;
			if (!readAccess && writeAccess && !miBpt.isWriteWatchpoint())
				continue;
			if (isHardware != null && isHardware.booleanValue() != miBpt.isHardware())
				continue;
			if (isTemporary != null && isTemporary.booleanValue() != miBpt.isHardware())
				continue;
			return miBpt;
		}
		return null;
	}

	private void removeCreatedTargetBreakpoint(IBreakpointsTargetDMContext context, MIBreakpoint miBpt) {
		Map<Integer, MIBreakpoint> map = fCreatedTargetBreakpoints.get(context);
		if (map != null) {
			map.remove(Integer.valueOf(miBpt.getNumber()));
		}
	}

    private boolean isPlatformLineBreakpoint(ICLineBreakpoint plBpt, MIBreakpoint miBpt, String fileName) {
    	if (plBpt instanceof ICAddressBreakpoint) {
    		return isAddressBreakpoint(miBpt) ? 
    			isPlatformAddressBreakpoint((ICAddressBreakpoint)plBpt, miBpt) : false;
    	}
    	if (plBpt instanceof ICFunctionBreakpoint) {
    		return isFunctionBreakpoint(miBpt) ? 
    			isPlatformFunctionBreakpoint((ICFunctionBreakpoint)plBpt, miBpt) : false;
    	}
    	try {
    		if (fileName != null && !fileName.equals(plBpt.getSourceHandle()))
    			return false;
			if (plBpt.getLineNumber() != miBpt.getLine())
				return false;
			return true;
		}
		catch(CoreException e) {
			GdbPlugin.log(e.getStatus());
		}
    	return false;
    }

    private boolean isPlatformFunctionBreakpoint(ICFunctionBreakpoint plBpt, MIBreakpoint miBpt) {
		try {
			return (plBpt.getFunction() != null && plBpt.getFunction().equals(miBpt.getFunction()));
		}
		catch(CoreException e) {
			GdbPlugin.log(e.getStatus());
		}
		return false;
    }

    private boolean isPlatformAddressBreakpoint(ICAddressBreakpoint plBpt, MIBreakpoint miBpt) {
		try {
			return (plBpt.getAddress() != null 
					&& plBpt.getAddress().equals(getPlatformAddress(miBpt.getAddress()).toHexAddressString()));
		}
		catch(CoreException e) {
			GdbPlugin.log(e.getStatus());
		}
		return false;
    }

    private boolean isPlatformWatchpoint(ICWatchpoint plBpt, MIBreakpoint miBpt) {
    	try {
			if (plBpt.getExpression() != null && plBpt.getExpression().equals(miBpt.getExpression()) ) {
				if (miBpt.isAccessWatchpoint())
					return plBpt.isWriteType() && plBpt.isReadType();
				else if (miBpt.isReadWatchpoint())
					return !plBpt.isWriteType() && plBpt.isReadType();
				else if (miBpt.isWriteWatchpoint())
					return plBpt.isWriteType() && !plBpt.isReadType();
			}
		}
		catch(CoreException e) {
			GdbPlugin.log(e.getStatus());
		}
    	return false;
    }
    
    private boolean isPlatformTracepoint(ICTracepoint plBpt, MIBreakpoint miBpt, String fileName) {
    	return isPlatformLineBreakpoint(plBpt, miBpt, fileName);
    }

    private boolean isTargetBreakpointDeleted(IBreakpointsTargetDMContext context, int bpId, boolean remove) {
    	Set<Integer> set = fDeletedTargetBreakpoints.get(context);
    	if (set != null )
    		return (remove) ? set.remove(Integer.valueOf(bpId)) : set.contains(Integer.valueOf(bpId));
    	return false;
    }    

    /**
     * Returns the list of tracepoint actions generated from the given command string.
     * If the corresponding action for a command doesn't exist in TracepointActionManager 
     * the new action is created and added.
     *   
     * @param commands list of gdb tracepoint commands separated by TracepointActionManager.TRACEPOINT_ACTION_DELIMITER
     */
    private ITracepointAction[] getActionsFromCommands(String[] commands) {
    	List<ITracepointAction> list = new ArrayList<ITracepointAction>();
    	TracepointActionManager tam = TracepointActionManager.getInstance();
    	WhileSteppingAction whileStepping = null;
    	List<ITracepointAction> subActions = null;
    	for (String command : commands) {
    		// Check if an action for this command exists
        	boolean found = false;
        	for (ITracepointAction action :tam.getActions()) {
        		if (command.equals(action.getSummary())) {
        			if (whileStepping == null || subActions == null)
        				list.add(action);
        			else
        				subActions.add(action);
        			found = true;
        			break;
        		}
        	}
        	if (!found) {
        		// Create a new action if an action for this command doesn't exists
        		ITracepointAction action = null;
            	if (command.startsWith(TC_COLLECT))
            		action = createCollectAction(command.substring(TC_COLLECT.length()));
            	if (command.startsWith(TC_TEVAL))
            		action = createEvaluateAction(command.substring(TC_TEVAL.length()));
            	if (command.startsWith(TC_WHILE_STEPPING)) {
            		whileStepping = createWhileSteppingAction(command.substring(TC_WHILE_STEPPING.length()));
            		if (whileStepping != null)
            			subActions = new ArrayList<ITracepointAction>();
            	}
            	if (command.equals(TC_END)) {
            		if (whileStepping == null || subActions == null)
            			continue;
                	StringBuilder sb = new StringBuilder();
                	boolean first = true;
                	for (ITracepointAction a : subActions) {
                		if (first)
                			first = false;
                		else
                			sb.append(',');
                		sb.append(a.getName());
                	}
                	whileStepping.setSubActionsNames(sb.toString());
                	whileStepping.setSubActionsContent(sb.toString());
                	action = whileStepping;
                	// Search for existing action for this 'while-stepping' command
                	for (ITracepointAction a :tam.getActions()) {
                		if (whileStepping.getSummary().equals(a.getSummary())) {
                			action = a;
                			found = true;
                			break;
                		}
                	}
                	whileStepping = null;
                	subActions.clear();
                	subActions = null;
            	}
        		if (action != null) {
        			if (!found)
        				TracepointActionManager.getInstance().addAction(action);
        			if (whileStepping == null || subActions == null) {
	        			list.add(action);
        			}
        			else {
    					subActions.add(action);
        			}
        		}
        	}
    	}
    	TracepointActionManager.getInstance().saveActionData();
    	return list.toArray(new ITracepointAction[list.size()]);
    }

    private CollectAction createCollectAction(String collectStr) {
    	CollectAction action = new CollectAction();
    	action.setName(TracepointActionManager.getInstance().makeUniqueActionName(action.getDefaultName()));
    	action.setCollectString(collectStr);
    	return action;
    }

    private EvaluateAction createEvaluateAction(String evalStr) {
    	EvaluateAction action = new EvaluateAction();
    	action.setName(TracepointActionManager.getInstance().makeUniqueActionName(action.getDefaultName()));
    	action.setEvalString(evalStr);
    	return action;
    }

    private WhileSteppingAction createWhileSteppingAction(String str) {
    	WhileSteppingAction action = new WhileSteppingAction();
    	action.setName(TracepointActionManager.getInstance().makeUniqueActionName(action.getDefaultName()));
    	try {
			action.setStepCount(Integer.parseInt(str.trim()));
		}
		catch(NumberFormatException e) {
			return null;
		}
    	return action;
    }

    protected void getSource(
    	ISourceLookup sourceLookup,
    	IBreakpointsTargetDMContext bpTargetDMC, 
    	String debuggerPath, 
    	final DataRequestMonitor<String> rm) {
    	
        ISourceLookupDMContext srcDmc = DMContexts.getAncestorOfType(bpTargetDMC, ISourceLookupDMContext.class);
        if (srcDmc == null) {
        	rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "No source lookup context")); //$NON-NLS-1$
        	rm.done();
        	return;
        }

        if (debuggerPath == null || debuggerPath.isEmpty()) {
        	rm.done();
        	return;
        }

        sourceLookup.getSource(
        	srcDmc, 
        	debuggerPath, 
        	new DataRequestMonitor<Object>(getExecutor(), rm) {
            	@Override
            	@ConfinedToDsfExecutor( "fExecutor" )
            	protected void handleSuccess() {
            		String fileName = null;
            		if (getData() instanceof IFile) {
            			fileName = ((IFile)getData()).getLocation().toOSString();
            		}
            		else if (getData() instanceof ITranslationUnit) {
            			IPath location = ((ITranslationUnit)getData()).getLocation();
            			if (location != null)
            				fileName = location.toOSString();
            		}
            		else if (getData() instanceof LocalFileStorage) {
            			fileName = ((LocalFileStorage)getData()).getFile().getAbsolutePath();
            		}
            		rm.setData(fileName);
            		rm.done();
            	}
            });
    }

    private boolean isFunctionBreakpoint(MIBreakpoint miBpt) {
    	return miBpt.getOriginalLocation().equals(miBpt.getFunction());
    }

    private boolean isAddressBreakpoint(MIBreakpoint miBpt) {
    	return miBpt.getOriginalLocation().startsWith("*"); //$NON-NLS-1$
    }

    private boolean isLineBreakpoint(MIBreakpoint miBpt) {
    	return !isFunctionBreakpoint(miBpt) && !isAddressBreakpoint(miBpt);
    }

    private IAddress getPlatformAddress(String miAddress) {
		int radix = 10;
		if (miAddress.startsWith("0x")) { //$NON-NLS-1$
			radix = 16;
			miAddress = miAddress.substring(2);
		}
		return new Addr64(new BigInteger(miAddress, radix));
    }
}

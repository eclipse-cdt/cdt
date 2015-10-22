/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ********************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.ArrayList;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.command.BufferedCommandControl;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIITSetDefine;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIInfoItsets;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfoItsetsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfoItsetsInfo.ITSet;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This class handles all the logic of managing groups backed
 * by GDB's ITSets.
 * It allows to create, modify and remove groups and keeps track of
 * all existing groups.
 * 
 * @since 4.9
 */
public class GDBGrouping_7_11 extends GDBGrouping {
	private ICommandControlService fCommandControl;
	private IGDBProcesses fProcesses;
	private CommandCache fITSetCache;
	
	public GDBGrouping_7_11(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
			@Override
			protected void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	private void doInitialize(RequestMonitor requestMonitor) {
		fCommandControl = getServicesTracker().getService(ICommandControlService.class);
		fProcesses = getServicesTracker().getService(IGDBProcesses.class);

		BufferedCommandControl bufferedCommandControl = new BufferedCommandControl(fCommandControl, fCommandControl.getExecutor(), 2);

		// This cache stores the result of a command when received; also, this cache
		// is manipulated when receiving events.  Currently, events are received after
		// three scheduling of the executor, while command results after only one.  This
		// can cause problems because command results might be processed before an event
		// that actually arrived before the command result.
		// To solve this, we use a bufferedCommandControl that will delay the command
		// result by two scheduling of the executor.
		// See bug 280461
		fITSetCache = new CommandCache(getSession(), bufferedCommandControl);
		fITSetCache.setContextAvailable(fCommandControl.getContext(), true);
		
		requestMonitor.done();
	}

	@Override
	protected boolean doCanGroup(IExecutionDMContext[] contexts) {
		for (IExecutionDMContext elem : contexts) {
			if (elem instanceof IGroupDMContext) {
				// GDB does not support grouping groups inside other groups.
				return false;
			}
		}
		return super.doCanGroup(contexts);
	}
	
	/**
	 * Creates a group from an array of IExecutionDMContext and returns
	 * a corresponding IGroupDMContext.
	 */
	@Override
	public void group(final IExecutionDMContext[] contexts, final DataRequestMonitor<IContainerDMContext> rm) {
		if (!doCanGroup(contexts)) {
	   		rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Unable to group specified contexts", null)); //$NON-NLS-1$
	   		return;
		}

		// Create a new group with the specified elements
		final String name = newGroupName();
		String content = createITSetContent(contexts);
		fCommandControl.queueCommand(
			new MIITSetDefine(fCommandControl.getContext(), name, content), 
			new DataRequestMonitor<MIInfo>(getSession().getExecutor(), rm) {
				@Override
				protected void handleSuccess() {
					fITSetCache.reset();
					
					MIUserGroupDMC newGroupDmc = new MIUserGroupDMC(getSession(), new IDMContext[] { fCommandControl.getContext() }, name);
					
					ContainerLayoutDMContext layoutDmc = new ContainerLayoutDMContext( 
							getSession().getId(), new IDMContext[] { newGroupDmc });
					ContainerLayoutChangedEvent event = new ContainerLayoutChangedEvent(layoutDmc); 
					getSession().dispatchEvent(event, getProperties());

					rm.done(newGroupDmc);
				};
			});
	}

	private String createITSetContent(IExecutionDMContext[] contexts)	{
		StringBuilder content = new StringBuilder();
		
		for (IExecutionDMContext dmc : contexts) {
			if (dmc instanceof IMIExecutionDMContext) {
				content.append(((IMIExecutionDMContext)dmc).getThreadId());
			} else if (dmc instanceof IMIContainerDMContext) {
				content.append(((IMIContainerDMContext)dmc).getGroupId());
			} else {
				assert dmc instanceof MIUserGroupDMC;
				if (dmc instanceof MIUserGroupDMC) {
					content.append(((MIUserGroupDMC)dmc).getId());
				}
				else continue;
			}
			content.append(',');
		}
		return content.deleteCharAt(content.length()-1).toString();
	}
	
	@Override
	public void ungroup(IExecutionDMContext[] contexts, RequestMonitor rm) {
		rm.done();
	}

	@Override
	public void getExecutionContexts(final IContainerDMContext containerDmc, final DataRequestMonitor<IExecutionDMContext[]> rm) {
		// First check if we are looking for children in the group-all hierarchy
		final MIUserGroupDMC group = DMContexts.getAncestorOfType(containerDmc, MIUserGroupDMC.class);
		if (group != null && group.getId().equals(GROUP_ALL_NAME)) {
			final IDMContext finalContext = containerDmc instanceof IGroupDMContext ? fCommandControl.getContext() : containerDmc;

			// For group-all, get the real list of processes or threads from the IProcesses service
			fProcesses.getProcessesBeingDebugged(
				finalContext,
				new DataRequestMonitor<IDMContext[]>(fProcesses.getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						if (getData() instanceof IExecutionDMContext[]) {
							rm.done((IExecutionDMContext[])getData());
						} else {
							rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Invalid contexts", null)); //$NON-NLS-1$
						}
					}
				});
			return;
		}

		if (containerDmc == null) {
			// For the top-level node, return all the groups
			fITSetCache.execute(
				new MIInfoItsets(fCommandControl.getContext()), 
				new DataRequestMonitor<MIInfoItsetsInfo>(getSession().getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						ArrayList<IGroupDMContext> groups = new ArrayList<>();
						ITSet[] sets = getData().getITSets();
						if (sets.length == 0) {
							// There has been no group created at all
							// Let's just return the normal processes and threads
							getNonGroupedContexts(containerDmc, rm);
							return;
						}
						
						for (ITSet itSet : sets) {
							groups.add(new MIUserGroupDMC(getSession(), new IDMContext[] { fCommandControl.getContext() }, itSet.getId()));
						}
						
						// Add CDT's group-all as the last one
						groups.add(new MIUserGroupDMC(getSession(), new IDMContext[] { fCommandControl.getContext() }, GROUP_ALL_NAME));
						rm.done(groups.toArray(new IExecutionDMContext[groups.size()]));
					};
				});
			return;
		}

		if (containerDmc instanceof IGroupDMContext) {
			assert containerDmc instanceof MIUserGroupDMC;
			
			// For a specific group, return its children
			fITSetCache.execute(
				new MIInfoItsets(fCommandControl.getContext(), ((MIUserGroupDMC)containerDmc).getId()), 
				new DataRequestMonitor<MIInfoItsetsInfo>(getSession().getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						ITSet[] sets = getData().getITSets();
						assert sets.length == 1;
						
						ArrayList<IExecutionDMContext> childrenDmcs = new ArrayList<>();

						String childrenStr = sets[0].getContent();
						String[] children = childrenStr.split(","); //$NON-NLS-1$
						for (String child : children) {
							if (child.startsWith("i")) { //$NON-NLS-1$
								// The child is a process
								childrenDmcs.add(fProcesses.createContainerContextFromGroupId(fCommandControl.getContext(), child));
							} else {
								// The child is a thread
								IContainerDMContext parentContainer = fProcesses.createContainerContextFromThreadId(fCommandControl.getContext(), child);
								IProcessDMContext processDmc = DMContexts.getAncestorOfType(parentContainer, IProcessDMContext.class);
								IThreadDMContext threadDmc = fProcesses.createThreadContext(processDmc, child);
								childrenDmcs.add(fProcesses.createExecutionContext(parentContainer, threadDmc, child));
							}
						}
						rm.done(childrenDmcs.toArray(new IExecutionDMContext[childrenDmcs.size()]));
					};
				});
			return;
		}

		if (containerDmc instanceof IMIContainerDMContext) {
			// For a process, return all its children using the process service
			fProcesses.getProcessesBeingDebugged(
				containerDmc,
				new DataRequestMonitor<IDMContext[]>(fProcesses.getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						if (getData() instanceof IExecutionDMContext[]) {
							rm.done((IExecutionDMContext[])getData());
						} else {
							rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Invalid returned contexts", null)); //$NON-NLS-1$
						}
					}
				});				
			return;
		}
			
		rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Unexpected container context", null)); //$NON-NLS-1$
	}
	
	@Override
	public void getExecutionData(IGroupDMContext group, final DataRequestMonitor<IGroupDMData> rm) {
		if (group instanceof MIUserGroupDMC) {
			if (((MIUserGroupDMC)group).getId().equals(GROUP_ALL_NAME)) {
				rm.done(new MIUserGroupDMData(GROUP_ALL_NAME, GROUP_ALL_NAME));
				return;
			}

			MIUserGroupDMC groupDmc = (MIUserGroupDMC)group;
			fITSetCache.execute(
					new MIInfoItsets(fCommandControl.getContext(), groupDmc.getId()), 
					new DataRequestMonitor<MIInfoItsetsInfo>(getSession().getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							ITSet[] sets = getData().getITSets();
							assert sets.length == 1;
							rm.done(new MIUserGroupDMData(sets[0].getId(), sets[0].getName()));
						};
					});
		} else {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Invalid type of context", null)); //$NON-NLS-1$
		}
	}
	
	@Override
	public void flushCache(IDMContext context) {
		fITSetCache.reset(context);
	}
}

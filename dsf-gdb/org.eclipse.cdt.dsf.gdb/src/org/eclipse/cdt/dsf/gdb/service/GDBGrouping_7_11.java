/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ********************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.ArrayList;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.BufferedCommandControl;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIITSetDefine;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIITSetUndefine;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIInfoItsets;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIListThreadGroups;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIListThreadGroups.GroupType;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfoItsetsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfoItsetsInfo.ITSet;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo.IThreadGroupInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MINotifyAsyncOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIThread;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This class handles all the logic of managing groups backed
 * by GDB's ITSets.
 * It allows to create, modify and remove groups and keeps track of
 * all existing groups.
 * 
 * @since 5.1
 */
public class GDBGrouping_7_11 extends GDBGrouping implements IEventListener {
	private ICommandControlService fCommandControl;
	private IGDBProcesses fProcesses;
	private CommandCache fITSetCache;
	
	// Should we show the groupAll group
	private boolean fShowGroupAll;
	
	private boolean fShowBuildinGroups = true;
	
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
		
		fCommandControl.addEventListener(this);
		
		requestMonitor.done();
	}
	
	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		fCommandControl.removeEventListener(this);
		super.shutdown(requestMonitor);
	}

	@Override
	protected boolean doCanGroup(IExecutionDMContext[] contexts) {
		// allow to group any execution context, with the exception of the 
		// automatically created group "group all"
		for (IExecutionDMContext elem : contexts) {
			if (elem instanceof MIUserGroupDMC) {
				if (((MIUserGroupDMC)elem).getName().equals(GROUP_ALL_NAME)) {
					return false;
				}
			}
		}
		return true;
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
		final String content = createITSetContent(contexts);
		fCommandControl.queueCommand(
			new MIITSetDefine(fCommandControl.getContext(), name, content), 
			new DataRequestMonitor<MIInfo>(getSession().getExecutor(), rm) {
				@Override
				protected void handleSuccess() {
					// Once a first group is created, we always show the groupAll group
					// even if all the other groups are deleted.
					fShowGroupAll = true;
					// groups have changed - flush the cache
					flushCache();
					
					// Find the group id of newly created group
					fITSetCache.execute(
						new MIInfoItsets(fCommandControl.getContext()), 
						new DataRequestMonitor<MIInfoItsetsInfo>(getExecutor(), rm) {
							@Override
							protected void handleSuccess() {
								ITSet[] sets = getData().getITSets();
								
								for (ITSet itSet : sets) {
									if (itSet.getName().equals(name)) {
										MIUserGroupDMC newGroupDmc = new MIUserGroupDMC(getSession(), new IDMContext[] { fCommandControl.getContext() }, itSet.getId(), name);
										rm.done(newGroupDmc);
										return;
									}
								}
						   		rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Unable to find group " + name, null)); //$NON-NLS-1$
							};
						});
				};
			});
	}

	private String createITSetContent(IExecutionDMContext[] contexts)	{
		StringBuilder content = new StringBuilder();
		
		for (IExecutionDMContext dmc : contexts) {
			if (dmc instanceof IMIExecutionDMContext) {
				// FIXME: this only works when a single inferior is present
				// we should instead use the per-inferior TID (ex: 1.4, 2.10, etc)
				// ATM, the TID present in the execution context is the global one. 
				// Changing it to the per-inferior id should be done in a separate patch
				content.append("tt1."); //$NON-NLS-1$
//				content.append("tt"); //$NON-NLS-1$  // remove previous line and un-comment this line when the 
				                                     // IExecutionDMContext has been switched to per-inferior TID
				content.append(((IMIExecutionDMContext)dmc).getThreadId());
			} else if (dmc instanceof IMIContainerDMContext) {
				content.append("i"); //$NON-NLS-1$
				content.append(((IMIContainerDMContext)dmc).getGroupId());
			} else if (dmc instanceof MIUserGroupDMC) {
				content.append(((MIUserGroupDMC)dmc).getName());
			} else {
				assert false : "Not supported by GDB"; //$NON-NLS-1$
			}
			content.append(',');
		}
		return content.deleteCharAt(content.length()-1).toString();
	}
	
	@Override
	public void ungroup(final IExecutionDMContext[] contexts, final RequestMonitor rm) {
		if (!doCanUngroup(contexts)) {
	   		rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Unable to un-group specified contexts", null)); //$NON-NLS-1$
	   		return;
		}

		CountingRequestMonitor crm = new CountingRequestMonitor(getExecutor(), rm) {
			@Override
			protected void handleCompleted() {
				// groups have changed - flush the cache
				flushCache();
			}
		};
		
		int count = 0;
		for (IExecutionDMContext context : contexts) {
			if (!(context instanceof IGroupDMContext)) {
				// Ignore elements that are not groups
				continue;
			}
			
			IGroupDMContext groupDmc = (IGroupDMContext)context;
			if (groupDmc instanceof MIUserGroupDMC && ((MIUserGroupDMC)groupDmc).getId().equals(GROUP_ALL_NAME)) {
				// We don't delete the group-all even if the user selected it.
				continue;
			}
			
			// Delete the group and its references
			deleteGroup(groupDmc, crm);
			count++;
		}
		crm.setDoneCount(count);
	}

	protected void deleteGroup(IGroupDMContext groupDmc, final RequestMonitor rm) {
		if (!(groupDmc instanceof MIUserGroupDMC)) {
			rm.done();
			return;
		}
		
		final String groupId = ((MIUserGroupDMC)groupDmc).getId();
		// Map the group id to a name
		fITSetCache.execute(
			new MIInfoItsets(fCommandControl.getContext()), 
			new DataRequestMonitor<MIInfoItsetsInfo>(getExecutor(), rm) {
				@Override
				protected void handleSuccess() {
					ITSet[] sets = getData().getITSets();
					
					for (ITSet itSet : sets) {
						if (itSet.getId().equals(groupId)) {
							fCommandControl.queueCommand(
									new MIITSetUndefine(fCommandControl.getContext(), itSet.getName()), 
									new DataRequestMonitor<MIInfo>(getExecutor(), rm));
							return;
						}
					}
			   		rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Unable to find group " + groupId, null)); //$NON-NLS-1$
				};
			});
	}
	
	@Override
	public void getExecutionContexts(final IContainerDMContext containerDmc, final DataRequestMonitor<IExecutionDMContext[]> rm) {
		// First check if we are looking for children in the group-all hierarchy
//		final MIUserGroupDMC group = DMContexts.getAncestorOfType(containerDmc, MIUserGroupDMC.class);
//		if (group != null && group.getId().equals(GROUP_ALL_NAME)) {
//			final IContainerDMContext finalContext = containerDmc instanceof IGroupDMContext ? null : containerDmc;
//
//			// For group-all, get the real list of processes or threads from the IProcesses service
//			getNonGroupContexts(finalContext, rm);
//			return;
//		}

		if (containerDmc == null) {
			// For the top-level node, return all the groups
			fITSetCache.execute(
					new MIListThreadGroups(fCommandControl.getContext(),MIListThreadGroups.GroupType.GROUP_TYPE_USER_DEFINED),
					new DataRequestMonitor<MIListThreadGroupsInfo>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							ArrayList<IGroupDMContext> groups = new ArrayList<>();
							IThreadGroupInfo[] groupList = getData().getGroupList();
							
							if (groupList.length == 0) {
//							if (!fShowGroupAll && groupList.length == 0) {
								// There has been no group created at all
								// Let's just return the normal processes and threads
								getNonGroupContexts(containerDmc, rm);
								return;
							}
							
							for (int i = 0; i < groupList.length; i++) {
								groups.add(new MIUserGroupDMC(getSession(), new IDMContext[] { fCommandControl.getContext() }, groupList[i].getGroupId(), groupList[i].getName()));
							}
							// Add CDT's group-all as the last one
//							//groups.add(new MIUserGroupDMC(getSession(), new IDMContext[] { fCommandControl.getContext() }, GROUP_ALL_NAME));
//							
//							// hack since group all is not discoverable with "info itset"
//							groups.add(new MIUserGroupDMC(getSession(), new IDMContext[] { fCommandControl.getContext() }, "0", "running"));
//							groups.add(new MIUserGroupDMC(getSession(), new IDMContext[] { fCommandControl.getContext() }, "-1", "stopped"));
//							groups.add(new MIUserGroupDMC(getSession(), new IDMContext[] { fCommandControl.getContext() }, "u-2", "aall"));

							rm.done(groups.toArray(new IExecutionDMContext[groups.size()]));
						}
						
					});
			return;
		}

		if (containerDmc instanceof IGroupDMContext) {
			assert containerDmc instanceof MIUserGroupDMC;
			MIUserGroupDMC group = (MIUserGroupDMC)containerDmc;
			
			// For a specific group, return its children
			fITSetCache.execute(
					new MIListThreadGroups(fCommandControl.getContext(),group.getId()),
					new DataRequestMonitor<MIListThreadGroupsInfo>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							ArrayList<IExecutionDMContext> childrenDmcs = new ArrayList<>();
							MIThread[] threads = getData().getThreadInfo().getThreadList();
							
							for (MIThread thread : threads) {
								IContainerDMContext parentContainer = fProcesses.createContainerContextFromThreadId(fCommandControl.getContext(), thread.getThreadId());
								IProcessDMContext processDmc = DMContexts.getAncestorOfType(parentContainer, IProcessDMContext.class);
								IThreadDMContext threadDmc = fProcesses.createThreadContext(processDmc, thread.getThreadId());
								childrenDmcs.add(fProcesses.createExecutionContext(parentContainer, threadDmc, thread.getThreadId()));
							}
							
							rm.done(childrenDmcs.toArray(new IExecutionDMContext[childrenDmcs.size()]));
						}
					}
			);
			
			return;
		}

		if (containerDmc instanceof IMIContainerDMContext) {
			// For a process, return all its children using the process service
			getNonGroupContexts(containerDmc, rm);
			return;
		}
			
		rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Unexpected container context", null)); //$NON-NLS-1$
	}
	
	@Override
	public void getExecutionData(IGroupDMContext group, final DataRequestMonitor<IGroupDMData> rm) {
		if (group instanceof MIUserGroupDMC) {
			if (((MIUserGroupDMC)group).getId().equals(GROUP_ALL_NAME)) {
				rm.done(new MIUserGroupDMData(GROUP_ALL_NAME, GROUP_ALL_NAME, null));
				return;
			}
			
			final MIUserGroupDMC groupDmc = (MIUserGroupDMC)group;
			fITSetCache.execute(
					new MIListThreadGroups(fCommandControl.getContext(),MIListThreadGroups.GroupType.GROUP_TYPE_USER_DEFINED),
//					new MIListThreadGroups(fCommandControl.getContext(),MIListThreadGroups.GroupType.GROUP_TYPE_BUILTIN),
					new DataRequestMonitor<MIListThreadGroupsInfo>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							IThreadGroupInfo[] groupList = getData().getGroupList();
							for (int i = 0; i < groupList.length; i++) {
								// look for specific group
								if (groupList[i].getGroupId().equals(groupDmc.getId())) {
									rm.done(new MIUserGroupDMData(groupDmc.getId(), groupList[i].getName(), groupList[i].getSpec()));
								}
							}
						}
					}
			);
		} else {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Invalid type of context", null)); //$NON-NLS-1$
		}
	}

	public void flushCache() {
		flushCache(null);
	}
	@Override
	public void flushCache(IDMContext context) {
		fITSetCache.reset(context);
	}
	


	@Override
	public void eventReceived(Object output) {
		for (MIOOBRecord oobr : ((MIOutput)output).getMIOOBRecords()) {
    		if (oobr instanceof MINotifyAsyncOutput) {
    			// Parse the string and dispatch the corresponding event
    			MINotifyAsyncOutput exec = (MINotifyAsyncOutput) oobr;
    			String miEvent = exec.getAsyncClass();
//    			if ("named-itset-created".equals(miEvent) || "named-itset-deleted".equals(miEvent)) { //$NON-NLS-1$ //$NON-NLS-2$
    			if ("thread-group-added".equals(miEvent) || "thread-group-removed".equals(miEvent)) { //$NON-NLS-1$ //$NON-NLS-2$	
    				flushCache();
    				String type = null;
    				String id = null;
    				String name = null;
    				String spec = null;
    				
    				MIResult[] results = exec.getMIResults();
    				for (int i = 0; i < results.length; i++) {
    					String var = results[i].getVariable();
    					MIValue val = results[i].getMIValue();
    					if (var.equals("type")) { //$NON-NLS-1$
    						if (val instanceof MIConst) {
    							type = ((MIConst) val).getString();
    						}
    					} else if (var.equals("name")) { //$NON-NLS-1$
    						if (val instanceof MIConst) {
    							name = ((MIConst) val).getString();
    						}
    					} else if (var.equals("spec")) { //$NON-NLS-1$
    		    			if (val instanceof MIConst) {
    							spec = ((MIConst) val).getString();
    		    			}
    		    		} else if (var.equals("id")) { //$NON-NLS-1$
    		    			if (val instanceof MIConst) {
    							id = ((MIConst) val).getString();
    						}
    		    		}
    				}
    				
    				// check the group type - here we only care about ITSets-related groups
    				if (type.equals(GroupType.GROUP_TYPE_USER_DEFINED.getFlag()) || 
    					type.equals(GroupType.GROUP_TYPE_BUILTIN.getFlag()) ) 
    				{
    					// continue
    				}
    				else {
    					return;
    				}
    				
		    		// create DSF event and dispatch
    				AbstractDMEvent<IGroupDMContext> event = null;
    				assert name != null && id != null;
    				MIUserGroupDMC groupDmc = new MIUserGroupDMC(getSession(), new IDMContext[] { fCommandControl.getContext() }, id, name);
    				
    				if ("thread-group-added".equals(miEvent)) { //$NON-NLS-1$
    					assert spec != null;
    					event = new GroupCreatedEvent(groupDmc); 
    				}
    				else if ("nthread-group-removed".equals(miEvent)) { //$NON-NLS-1$
    					event = new GroupDeletedEvent(groupDmc); 
    				}
    				else {
		    			assert false;	// earlier check should have guaranteed this isn't possible
		    		}
    				
    		    	fCommandControl.getSession().dispatchEvent(event, fCommandControl.getProperties());
    			}
    		}
    	}
	}
	
	@Override
	public void isGroupingEnabled(DataRequestMonitor<Boolean> rm) {
		if (fShowGroupAll) {
			rm.done(true);
		}
		else {
			rm.done(false);
		}
	}
	
	/** Invoked when a thread or process starts. */
	@Override
	@DsfServiceEventHandler
	public void handleEvent(IStartedDMEvent event) {
		flushCache();
	}

	/** Invoked when a thread or process exits. */
	@Override
	@DsfServiceEventHandler
	public void handleEvent(IExitedDMEvent event) {
		flushCache();
		
		// send GroupChangedEvent for all groups, since we can't know which one(s) 
		// might have contained the exited thread by now (since it's gone)
		getExecutionContexts(null, new DataRequestMonitor<IExecutionDMContext[]>(getExecutor(), null) {
			@Override
			protected void handleSuccess() {
				IExecutionDMContext[] currentGroups = getData();
				
				for (int i = 0; i < currentGroups.length; i++) {
					if (currentGroups[i] instanceof MIUserGroupDMC) {
						MIUserGroupDMC group = ((MIUserGroupDMC)currentGroups[i]);
						GroupChangedEvent changeEvent = new GroupChangedEvent(group); 
						getSession().dispatchEvent(changeEvent, getProperties());
					}
				}
			}
		});
	}
	
}

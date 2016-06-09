/*******************************************************************************
 * Copyright (c) 2011, 2015 Texas Instruments, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dobrin Alexiev (Texas Instruments) - initial API and implementation (bug 336876)
 ********************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.internal.provisional.service.IExecutionContextTranslator;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * The grouping service implementation.
 * This service delegates that details of grouping functionality
 * to a UserGroupManager class.
 *  
 * @since 5.0
 */
public class GDBGrouping extends AbstractDsfService implements IGDBGrouping, ICachingService {

	protected static final String GROUP_ALL_NAME = "GroupAll"; //$NON-NLS-1$
	private UserGroupData fGroupAllData;

	/** Base class for any kind of element of a group, i.e threads, processes, groups */
	protected interface ExecNodeDesc {}

	/** Thread element part of a group */
	protected static class ThreadNodeDesc implements ExecNodeDesc {
		private IMIExecutionDMContext fThread;

		public ThreadNodeDesc(IMIExecutionDMContext thread) {
			fThread = thread;
		}

		public IMIExecutionDMContext getThreadContext() {
			return fThread;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ThreadNodeDesc)) {
				return false;
			}

			ThreadNodeDesc other = (ThreadNodeDesc)obj; 
			return fThread.equals(other.fThread); 
		}

		@Override
		public int hashCode() {
			return fThread.hashCode();
		}

		@Override
		public String toString() {
			return fThread.getThreadId();
		}
	}

	/** Process element part of a group */
	protected static class ProcessNodeDesc implements ExecNodeDesc {
		// The same process can have different children when part of different groups,
		// so we need to keep track of the parent hierarchy to tell them apart.
		// For example, if a user groups thread1 into group1, we will insert proc1
		// as the parent of thread1, but proc1 might have more threads than just
		// thread1; therefore, we need to remember that for group1, proc1 only
		// has thread1 as a child, while for group2, it may have thread2.  To remember
		// this, we need an identifier that knows to which group the proc belongs.
		private IMIContainerDMContext fContainerProc;

		public ProcessNodeDesc(IMIContainerDMContext dmc) {
			fContainerProc = dmc;
		}

		public IMIContainerDMContext getProcessContext() {
			return fContainerProc;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ProcessNodeDesc)) {
				return false;
			}

			ProcessNodeDesc other = (ProcessNodeDesc)obj; 
			return fContainerProc.equals(other.fContainerProc); 
		}

		@Override
		public int hashCode() {
			return fContainerProc.hashCode();
		}

		@Override
		public String toString() {
			return fContainerProc.toString();
		}
	}

	/** Group element */
	protected static class UserGroupNodeDesc implements ExecNodeDesc {
		private IGroupDMContext fGroup;

		public UserGroupNodeDesc(IGroupDMContext group) {
			fGroup = group;
		}

		public IGroupDMContext getGroupContext() {
			return fGroup;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof UserGroupNodeDesc)) {
				return false;
			}

			UserGroupNodeDesc other = (UserGroupNodeDesc)obj; 
			return fGroup.equals(other.fGroup);
		}

		@Override
		public int hashCode() {
			return fGroup.hashCode();
		}
	}

	/**
	 * Represents a group and its children.
	 */
	protected static class UserGroupData {

		// The group node itself
		private UserGroupNodeDesc fGroup;
		// The children of that group, which can be groups, processes or threads
		// note: using LinkedHashSet to preserve order
		private Set<ExecNodeDesc> fChildren = new LinkedHashSet<>();

		public UserGroupData(UserGroupNodeDesc node) {
			fGroup = node;
		}

		public UserGroupNodeDesc getNode() {
			return fGroup;
		}

		public ExecNodeDesc[] getChildren() {
			return fChildren.toArray(new ExecNodeDesc[fChildren.size()]);
		}

		public void addChild(ExecNodeDesc child) {
			fChildren.add(child);
		}

		public boolean hasChild(ExecNodeDesc child) {
			return fChildren.contains(child); 
		}

		public boolean removeChild(ExecNodeDesc child) {
			return fChildren.remove(child);
		}

		@Override
		public String toString() {
			return fGroup.toString() + 
					"(" + fChildren.size() + " children)";   //$NON-NLS-1$//$NON-NLS-2$
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof UserGroupData)) {
				return false;
			}
			UserGroupData o = (UserGroupData)other;
			return o.fGroup.equals(fGroup) &&
					o.fChildren.equals(fChildren);
		}

		@Override
		public int hashCode() {
			return fGroup.hashCode() + fChildren.hashCode();
		}
	}

	protected static class ContainerLayoutDMContext extends AbstractDMContext 
	implements IGroupDMContext {

		public ContainerLayoutDMContext(String sessionId, IDMContext[] parents) {
			super(sessionId, parents);
		}

		@Override
		public boolean equals(Object obj) {
			return baseEquals(obj);
		}

		@Override
		public int hashCode() {
			return baseHashCode();
		}

		@Override
		public String toString() {
			return super.baseToString();
		}
	}

	protected class GroupCreatedEvent extends AbstractDMEvent<IGroupDMContext> 
	implements IGroupCreatedEvent 
	{
		public GroupCreatedEvent(IGroupDMContext context) {
			super(context);
		}
	}

	protected class GroupDeletedEvent extends AbstractDMEvent<IGroupDMContext> 
	implements IGroupDeletedEvent {
		public GroupDeletedEvent(IGroupDMContext context) {
			super(context);
		}
	}

	protected class GroupChangedEvent extends AbstractDMEvent<IGroupDMContext> 
	implements IGroupChangedEvent {
		public GroupChangedEvent(IGroupDMContext context) {
			super(context);
		}
	}

	/**
	 * This class represents a user group.
	 */
	public static class MIUserGroupDMC extends AbstractDMContext implements IGroupDMContext {

		private String fId;
		private String fName;

		public MIUserGroupDMC(DsfSession session, IDMContext[] parents, String id) {
			this(session, parents, id, id);
		}
		public MIUserGroupDMC(DsfSession session, IDMContext[] parents, String id, String name) {
			super(session, parents);
			fId = id;
			fName = name;
		}

		public String getId() {
			return fId;
		}
		public String getName() {
			return fName;
		}

		@Override
		public String toString() {
			return baseToString() + ".userGroup[" + fId + "]"; //$NON-NLS-1$ //$NON-NLS-2$ 
		}

		@Override
		public boolean equals(Object obj) {
			return baseEquals(obj)
					&& (((MIUserGroupDMC) obj).fId == null ? fId == null
					: ((MIUserGroupDMC) obj).fId.equals(fId));
		}

		@Override
		public int hashCode() {
			return baseHashCode() ^ (fId == null ? 0 : fId.hashCode());
		}
	}

	protected static class MIUserGroupDMData implements IGroupDMData {
		private String fId;
		private String fName;
		private String fSpec;

		public MIUserGroupDMData(String id, String name, String spec) {
			fId = id;
			fName = name;
			fSpec = spec;
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
		public String getSpec() {
			return fSpec;
		}
	}

	private ICommandControlService fCommandControl;
	private IGDBProcesses fProcesses;
	private int fNewGroupId = 1;
	private ArrayList<UserGroupData> fGroupList;

	/** Current set of session event listeners. */
	protected List<Object> dsfSessionListeners = new ArrayList<Object>();


	public GDBGrouping(DsfSession session) {
		super(session);
	}


	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
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

		register(new String[] { IGDBGrouping.class.getName(),
				IExecutionContextTranslator.class.getName() },
				new Hashtable<String, String>());

		addDSFServiceEventListener(this);

		requestMonitor.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		unregister();		
		removeDSFServiceEventListener(this);
		super.shutdown(requestMonitor);
	}

	protected boolean doCanGroup(IExecutionDMContext[] contexts) {
		// We allow a group of one element, to act as a filter
		// e.g., group thread 36 so that only that one is visible
		if (contexts == null || contexts.length < 1) {
			return false;
		}

		// do not allow groups to be grouped (i.e. group in group)
		for (IExecutionDMContext c : contexts) {
			if (c instanceof MIUserGroupDMC) {
				return false;
			}
		}
		return true;
	}

	protected boolean doCanUngroup(IExecutionDMContext[] contexts) {
		if (contexts == null || contexts.length == 0) {
			return false;
		}
		
		// Deny ungrouping if any element is not a group or is 
		// the persistent group-all group
		for (IExecutionDMContext context : contexts) {
			if (!(context instanceof IGroupDMContext)) {
				// Un-group only applies to group elements
				return false;
			}
			else {
				if (((MIUserGroupDMC)context).getId().equals(GROUP_ALL_NAME)) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public void canGroup(IExecutionDMContext[] contexts, DataRequestMonitor<Boolean> rm) {
		rm.done(doCanGroup(contexts));
	}

	@Override
	public void canUngroup(IExecutionDMContext[] contexts, DataRequestMonitor<Boolean> rm) {
		rm.done(doCanUngroup(contexts));
	}

	// Design decision:
	//  Selecting a thread automatically includes its parent process, but not any sibling thread
	//  Selecting a process includes that process, and all its threads, present and future
	//  If the user explicitly selects a process, and explicitly selects a thread of that process
	//    we will include the process (and its present and future threads), and we will -also-
	//    include the process with its child being the selected thread.
	//  We can see that we now have two types of 'processes' in a group hierarchy:
	//    1- The whole process which include present and future threads (when the process is selected explicitly)
	//    2- An 'informational' process entry which just shows the user the parent of the thread(s) selected
	//  If the user selects a group, we will include the group and its children
	//  If the user explicitly selects the same thread more than once, we will keep it once,
	//    even if the thread was selected from a group
	//  Same goes for both types of processes
	//  Same goes for groups	
	/**
	 * Creates a group from an array of IExecutionDMContext and returns
	 * a corresponding IGroupDMContext.
	 */
	@Override
	public void group(IExecutionDMContext[] contexts, DataRequestMonitor<IContainerDMContext> rm) {
		if (!doCanGroup(contexts)) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Unable to group specified contexts", null)); //$NON-NLS-1$
			return;
		}

		if (fGroupList == null) {
			// We are about to create the first group.  Create the list and add the default group in it.
			MIUserGroupDMC groupAllDmc = new MIUserGroupDMC(getSession(), new IDMContext[] { fCommandControl.getContext() }, GROUP_ALL_NAME);
			UserGroupNodeDesc groupAll = new UserGroupNodeDesc(groupAllDmc);
			fGroupAllData = new UserGroupData(groupAll);
			fGroupList = new ArrayList<>();
			fGroupList.add(fGroupAllData);

			// Notify listeners about the new "GroupAll" group
			GroupCreatedEvent event = new GroupCreatedEvent(groupAllDmc); 
			getSession().dispatchEvent(event, getProperties());
		}

		// Create a new group with the specified elements
		String name = newGroupName();
		MIUserGroupDMC newGroupDmc = new MIUserGroupDMC(getSession(), new IDMContext[] { fCommandControl.getContext() }, name);
		UserGroupNodeDesc newGroupDesc = new UserGroupNodeDesc(newGroupDmc);
		UserGroupData newGroupData = new UserGroupData(newGroupDesc);
		fGroupList.add(0, newGroupData);

		// Add all the children to the new group
		addChildren(contexts, newGroupData);

		// Notify of the new group
		GroupCreatedEvent event = new GroupCreatedEvent(newGroupDmc); 
		getSession().dispatchEvent(event, getProperties());

		rm.done(newGroupDmc);
	}

	/**
	 * Adds the specified children to the specified groupData.
	 * 
	 * @param children The list of children to be added
	 * @param groupData The group to which the children will be added
	 */
	protected void addChildren(IExecutionDMContext[] children, UserGroupData groupData) {
		for (IExecutionDMContext child : children) {
			if (child instanceof IMIExecutionDMContext) {
				groupData.addChild(new ThreadNodeDesc((IMIExecutionDMContext)child));
			} else if (child instanceof IMIContainerDMContext) {
				groupData.addChild(new ProcessNodeDesc((IMIContainerDMContext)child));
			} else {
				assert child instanceof MIUserGroupDMC;
				if (child instanceof MIUserGroupDMC) {
					if (((MIUserGroupDMC)child).getId().equals(GROUP_ALL_NAME)) {
						// We don't allow to group the group-all even if the user selected it.
						continue;
					}

					UserGroupNodeDesc childGroup = new UserGroupNodeDesc((MIUserGroupDMC)child);
					groupData.addChild(childGroup);
				}
			}
		}
	}

	// Ungroup expects the user to select the user group node(s)
	// It will remove those groups and their content, while leaving group-all as before
	// which holds all threads and processes.
	@Override
	public void ungroup(IExecutionDMContext[] contexts, RequestMonitor rm) {
		if (!doCanUngroup(contexts)) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Unable to un-group specified contexts", null)); //$NON-NLS-1$
			return;
		}

		for (IExecutionDMContext context : contexts) {
			if (!(context instanceof IGroupDMContext)) {
				// Ignore elements that are not groups
				continue;
			}

			IGroupDMContext groupDMC = (IGroupDMContext)context;
			if (groupDMC instanceof MIUserGroupDMC && ((MIUserGroupDMC)groupDMC).getId().equals(GROUP_ALL_NAME)) {
				// We don't delete the group-all even if the user selected it.
				continue;
			}

			// Delete the group and its references
			deleteGroup(new UserGroupNodeDesc(groupDMC));

			// send notification
			GroupDeletedEvent event = new GroupDeletedEvent(groupDMC); 
			getSession().dispatchEvent(event, getProperties());
		}
		rm.done();
	}

	/**
	 * Completely removes a group from the internal data structures.
	 * 
	 * @param groupDMC The context of the group to be removed.
	 */
	protected void deleteGroup(UserGroupNodeDesc group) {
		// Remove the group
		fGroupList.remove(findNode(group));
	}

	@Override
	public void getExecutionContexts(IContainerDMContext containerDmc, final DataRequestMonitor<IExecutionDMContext[]> rm) {
		if (fGroupList == null || fGroupList.size() == 0) {
			// There has been no group created at all
			getNonGroupContexts(containerDmc, rm);
			return;
		}

		// First check if we are looking for children in the group-all hierarchy
		final MIUserGroupDMC group = DMContexts.getAncestorOfType(containerDmc, MIUserGroupDMC.class);
		if (group != null && group.getId().equals(GROUP_ALL_NAME)) {
			final IContainerDMContext finalContext = containerDmc instanceof IGroupDMContext ? null : containerDmc;

			// For group-all, get the real list of processes or threads from the IProcesses service
			getNonGroupContexts(finalContext, rm);
			return;
		}

		if (containerDmc == null) {
			// For the top-level node, return all the groups
			ArrayList<IGroupDMContext> groups = new ArrayList<>();
			for (UserGroupData data : fGroupList) {
				groups.add(data.getNode().getGroupContext());
			}
			rm.done(groups.toArray(new IExecutionDMContext[groups.size()]));
			return;
		}

		if (containerDmc instanceof MIUserGroupDMC) {
			// For a specific group, return all its children
			UserGroupData groupData = findNode(new UserGroupNodeDesc((MIUserGroupDMC)containerDmc));
			if (groupData == null) {
				String groupId = ((MIUserGroupDMC)containerDmc).getId();
				rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Cannot find specified group: " + groupId, null)); //$NON-NLS-1$
				return;
			}

			ArrayList<IExecutionDMContext> childrenDmcs = new ArrayList<>();
			for (ExecNodeDesc childDesc : groupData.getChildren()) {
				if (childDesc instanceof UserGroupNodeDesc) {
					childrenDmcs.add(((UserGroupNodeDesc)childDesc).getGroupContext());
				} else if (childDesc instanceof ProcessNodeDesc) {
					childrenDmcs.add(((ProcessNodeDesc)childDesc).getProcessContext());
				} else if (childDesc instanceof ThreadNodeDesc) {
					childrenDmcs.add(((ThreadNodeDesc)childDesc).getThreadContext());
				} else {
					assert false;
				}
			}
			rm.done(childrenDmcs.toArray(new IExecutionDMContext[childrenDmcs.size()]));
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
	public void getExecutionData(IGroupDMContext group, DataRequestMonitor<IGroupDMData> rm) {
		if (group instanceof MIUserGroupDMC) {
			MIUserGroupDMC groupDmc = (MIUserGroupDMC)group;
			rm.done(new MIUserGroupDMData(groupDmc.getId(), groupDmc.getId(), null));
		} else {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid type of context", null)); //$NON-NLS-1$
		}
	}

	@Override
	public void isGroupingEnabled(DataRequestMonitor<Boolean> rm) {
		if (fGroupList != null && fGroupList.size() > 0) {
			rm.done(true);
		}
		else {
			rm.done(false);
		}
	}
	
	@Override
	// note: it's assumed ATM that we do not have recursive groups (i.e. group-in-group).
	// This implementation will need to change if we want that.
	public void getGroups(final IExecutionDMContext wantedCtx, boolean recurse, DataRequestMonitor<IGroupDMContext[]> rm) {
		// group-in-group not allowed,
		if (wantedCtx instanceof IGroupDMContext) {
			rm.done(new IGroupDMContext[0]);
			return;
		}
		
		final ArrayList<IGroupDMContext> resultGroupList = new ArrayList<IGroupDMContext>();
		
		CountingRequestMonitor crm = new CountingRequestMonitor(getExecutor(), rm) {
			@Override
			protected void handleCompleted() {
				// done searching all groups
				IGroupDMContext[] result = new IGroupDMContext[resultGroupList.size()];
				rm.done(resultGroupList.toArray(result));
			}
		};
		
		if (fGroupList != null) {
			// for each group
			for (final UserGroupData grp : fGroupList) {
				getExecutionContexts(grp.getNode().getGroupContext(), new DataRequestMonitor<IExecutionDMContext[]>(getExecutor(), crm) {
					@Override
					protected void handleSuccess() {
						IExecutionDMContext[] currentGroupContent = getData();
						
						// current group directly contains wanted context?
						for (int i = 0; i < currentGroupContent.length; i++) {
							if (currentGroupContent[i].equals(wantedCtx)) {
								resultGroupList.add(grp.getNode().getGroupContext());
								crm.done();
								return;
							}
						}
						
						if (recurse) {
							// Context not in group.  Then verify if current group contains 
							// the parent of the wanted context.  i.e. we look for a thread, 
							// but the group contains it's process.
							//
							for (int i = 0; i < currentGroupContent.length; i++) {
								for (IDMContext wantedParents : wantedCtx.getParents()) {
									if (wantedParents.equals(currentGroupContent[i])) {
										resultGroupList.add(grp.getNode().getGroupContext());
										crm.done();
										return;
									}
								}
							}
						}
						crm.done();
					}
				});
			}
			crm.setDoneCount(fGroupList.size());
		}
		else {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid type of context", null)); //$NON-NLS-1$
		}
	}
	

	protected void getNonGroupContexts(final IContainerDMContext containerDmc, final DataRequestMonitor<IExecutionDMContext[]> rm) {
		// In this case, we fetch the list of children from the processes service
		fProcesses.getProcessesBeingDebugged(
				containerDmc == null ? fCommandControl.getContext() : containerDmc,
						new DataRequestMonitor<IDMContext[]>(getExecutor(), rm) {
							@Override
							protected void handleSuccess() {
								if (getData() instanceof IExecutionDMContext[]) {
									rm.done((IExecutionDMContext[])getData());
								} else {
									rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid contexts", null)); //$NON-NLS-1$
								}
							}
						});
	}

	protected String newGroupName() {
		// note: removing dash because "itset view <group name>" chokes on it
		// note2: there is also an issue with group names starting with certain letters
		//        "i,t,g", for example. So we go with "Set" instead of "Group" to mitigate.
		// note3: when a user creates a group on the command-line / console, we can't do
		//        anything about it... 
		return "Set" + new Integer(fNewGroupId++).toString(); //$NON-NLS-1$
	}

	private UserGroupData findNode(UserGroupNodeDesc node) {
		for (UserGroupData current : fGroupList) { 
			if (current.getNode().equals(node)) {
				return current;
			}
		}
		return null;
	}


	/** Adds a service event listener. */
	protected void addDSFServiceEventListener(Object listener)
	{
		final Object listener_f = listener;
		final DsfSession session_f = getSession();
		if (session_f != null) {
			try {
				session_f.getExecutor().execute(new DsfRunnable() {
					@Override
					public void run() {
						session_f.addServiceEventListener(listener_f, null);
						dsfSessionListeners.add(listener_f);
					}
				});
			} catch (RejectedExecutionException e) {
				// Session is shut down.
			}
		}
	}

	/** Removes a service event listener. */
	protected void removeDSFServiceEventListener(Object listener)
	{
		final Object listener_f = listener;
		final DsfSession session_f = getSession();
		if (session_f != null) {
			try {
				session_f.getExecutor().execute(new DsfRunnable() {
					@Override
					public void run() {
						if (dsfSessionListeners != null) {
							session_f.removeServiceEventListener(listener_f);
							dsfSessionListeners.remove(listener_f);
						}
					}
				});
			} catch (RejectedExecutionException e) {
				// Session is shut down.
			}
		}
	}

	@Override
	public void flushCache(IDMContext context) {
	}



	/** Invoked when a thread or process exits. */
	@DsfServiceEventHandler
	public void handleEvent(IStartedDMEvent event) {
		if (fGroupList == null) {
			return;
		}
		// TODO: complete? Send GroupModifiedEvent to listeners? 
	}

	/** Invoked when a thread or process exits. */
	@DsfServiceEventHandler
	public void handleEvent(IExitedDMEvent event) {
		if (fGroupList == null) {
			return;
		}

		final CountingRequestMonitor multiRm = new CountingRequestMonitor(getExecutor(), null);
		multiRm.setDoneCount(fGroupList.size());

		final IExecutionDMContext exitedThreadOrProcess = event.getDMContext();
		// Remove element from any group it's in
		for (final UserGroupData group : fGroupList) {
			// get group's children
			getExecutionContexts( group.getNode().fGroup, new DataRequestMonitor<IExecutionDMContext[]>(getExecutor(), multiRm) {
				@Override
				protected void handleSuccess() {
					for (IExecutionDMContext childCtx : getData()) {
						if (exitedThreadOrProcess.equals(childCtx)) {
							// remove this execution context from group
							for (ExecNodeDesc child : group.getChildren()) {
								if (child instanceof ThreadNodeDesc) {
									if (exitedThreadOrProcess.equals(((ThreadNodeDesc)child).getThreadContext())) {
										group.removeChild(child);
										GroupChangedEvent changeEvent = new GroupChangedEvent(group.getNode().getGroupContext()); 
										getSession().dispatchEvent(changeEvent, getProperties());
										break;
									}
								}
							}
						}
					}
					multiRm.done();	
				}
			});
		}

		// group-all - by definition contains any exiting thread
		ContainerLayoutDMContext layoutDmc = new ContainerLayoutDMContext( 
				getSession().getId(), new IDMContext[] { fGroupAllData.getNode().getGroupContext() });

		GroupChangedEvent changeEvent = new GroupChangedEvent(layoutDmc); 
		getSession().dispatchEvent(changeEvent, getProperties());
	}

}

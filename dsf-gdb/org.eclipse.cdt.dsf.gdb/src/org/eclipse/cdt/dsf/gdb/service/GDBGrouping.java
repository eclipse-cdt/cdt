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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

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
			return Integer.toString(fThread.getThreadId());
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
	 * Represents a group and its children and parents.
	 */
	protected static class UserGroupData {
		
		// The group node itself
		private UserGroupNodeDesc fGroup;
		// The children of that group, which can be groups, processes or threads
		private Set<ExecNodeDesc> fChildren = new HashSet<>();
		// Immediate parents of this group.
		// This allows us to quickly delete a group by knowing which
		// other group is affected
		private Set<UserGroupNodeDesc> fParents = new HashSet<>();
		
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
		
		public UserGroupNodeDesc[] getParents() {
			return fParents.toArray(new UserGroupNodeDesc[fParents.size()]);
		}

		public void addParent(UserGroupNodeDesc parent) {
			fParents.add(parent);
		}
		
		public boolean hasParent(ExecNodeDesc parent) {
			return fParents.contains(parent); 
		}

		public boolean removeParent(ExecNodeDesc parent) {
			return fParents.remove(parent);
		}

		@Override
		public String toString() {
			return fGroup.toString() + 
					"(" + fChildren.size() + " children, " +   //$NON-NLS-1$//$NON-NLS-2$
					fParents.size() + " parents)"; //$NON-NLS-1$
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof UserGroupData)) {
				return false;
			}
			UserGroupData o = (UserGroupData)other;
			return o.fGroup.equals(fGroup) &&
					o.fChildren.equals(fChildren) &&
					o.fParents.equals(fParents);
		}
		
		@Override
		public int hashCode() {
			return fGroup.hashCode() + fChildren.hashCode();
		}
	}
	
	protected static class ContainerLayoutDMContext extends AbstractDMContext 
	implements IGroupChangedDMContext {
		
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
	
	protected class GroupAddedEvent extends AbstractDMEvent<IGroupChangedDMContext> 
	implements IGroupAddedEvent {
		public GroupAddedEvent(IGroupChangedDMContext context) {
			super(context);
		}
	}
	
	protected class GroupDeletedEvent extends AbstractDMEvent<IGroupChangedDMContext> 
	implements IGroupDeletedEvent {
		public GroupDeletedEvent(IGroupChangedDMContext context) {
			super(context);
		}
	}
	
	protected class GroupModifiedEvent extends AbstractDMEvent<IGroupChangedDMContext> 
	implements IGroupModifiedEvent {
		public GroupModifiedEvent(IGroupChangedDMContext context) {
			super(context);
		}
	}
	
	/**
	 * This class represents a user group.
	 */
	protected static class MIUserGroupDMC extends AbstractDMContext implements IGroupDMContext {
		
		private String fId;
		
		public MIUserGroupDMC(DsfSession session, IDMContext[] parents, String id) {
			super(session, parents);
			fId = id;
		}
		
		public String getId() {
			return fId;
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
		
		public MIUserGroupDMData(String id, String name) {
			fId = id;
			fName = name;
		}
		
		@Override
		public String getId() {
			return fId;
		}
		
		@Override
		public String getName() {
			return fName;
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
		
		if (contexts.length == 1 &&
				contexts[0] instanceof MIUserGroupDMC && 
				((MIUserGroupDMC)contexts[0]).getId().equals(GROUP_ALL_NAME)) {
			// If the user only selects the group-all, we don't allow to group.
			return false;
		}

		return true;
	}
	
	protected boolean doCanUngroup(IExecutionDMContext[] contexts) {
		if (contexts == null || contexts.length == 0) {
			return false;
		}

		if (contexts.length == 1 &&
				contexts[0] instanceof MIUserGroupDMC && 
				((MIUserGroupDMC)contexts[0]).getId().equals(GROUP_ALL_NAME)) {
			// If the user only selects the group-all, we don't allow to un-group.
			return false;
		}
		
		for (IExecutionDMContext context : contexts) {
			if (!(context instanceof IGroupDMContext)) {
				// Un-group only applies to group elements
				return false;
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
	//    we will include the process (and it present and future threads), and we will -also-
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
			ContainerLayoutDMContext layoutDmc = new ContainerLayoutDMContext( 
					getSession().getId(), new IDMContext[] { groupAllDmc });
			GroupAddedEvent event = new GroupAddedEvent(layoutDmc); 
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
		ContainerLayoutDMContext layoutDmc = new ContainerLayoutDMContext( 
				getSession().getId(), new IDMContext[] { newGroupDmc });
		GroupAddedEvent event = new GroupAddedEvent(layoutDmc); 
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
					
					// This child group is part of the new group.
					// Keep track of that info in the parent list of the child.
					UserGroupData childData = findNode(childGroup);
					childData.addParent(groupData.getNode());
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
		
		boolean deletedAGroup = false;
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
			deletedAGroup = true;
			deleteGroup(new UserGroupNodeDesc(groupDMC));
		}

		if (deletedAGroup) {
			ContainerLayoutDMContext layoutDmc = new ContainerLayoutDMContext( 
					getSession().getId(), new IDMContext[] { contexts[0] }); //TODO
			GroupDeletedEvent event = new GroupDeletedEvent(layoutDmc); 
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
		UserGroupData groupData = findNode(group);

		// First remove that group from any children list
		for (UserGroupNodeDesc parentGroup : groupData.getParents()) {
			UserGroupData parentData = findNode(parentGroup);
			parentData.removeChild(groupData.getNode());
			if (parentData.getChildren().length == 0) {
				// The parent group has no more children, delete it also.
				deleteGroup(parentGroup);
			}
		}
		
		// Next remove that group from any parent list
		for (ExecNodeDesc child : groupData.getChildren()) {
			if (child instanceof UserGroupNodeDesc) {
				UserGroupData childData = findNode((UserGroupNodeDesc)child);
				childData.removeParent(groupData.getNode());
			}
		}
		
		// Now remove the group itself
		fGroupList.remove(groupData);
	}
	
//	@Override
//	/** 
//	 * Recursive version of getExecutionContexts(). Goes through all container contexts recursively and builds
//	 * an array of all execution contexts
//	 * @param containerDmc
//	 * @param rm
//	 */
//	protected void getExecutionContextsRecursive(IContainerDMContext containerDmc, final DataRequestMonitor<IExecutionDMContext[]> rm) {
//		
//	}
	
	
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
			rm.done(new MIUserGroupDMData(groupDmc.getId(), groupDmc.getId()));
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
	public void getGroupsContainingExecutionContext(IExecutionDMContext wantedCtx, DataRequestMonitor<IGroupDMContext[]> rm) {
		if (fGroupList != null) {
			ArrayList<IGroupDMContext> groupList = new ArrayList<IGroupDMContext>();
					
			// go through all groups
			for (UserGroupData grp : fGroupList) {
				// group-all is build in a special way
				if (grp.equals(fGroupAllData)) {
					groupList.add(grp.getNode().getGroupContext());
					continue;
				}
				
				ExecNodeDesc[] children = grp.getChildren();
				for (ExecNodeDesc child : children) {
					// thread?
					if (child instanceof ThreadNodeDesc) {
						ThreadNodeDesc thread = (ThreadNodeDesc)child;
						// thread context we are looking for?
						if (thread.getThreadContext().equals(wantedCtx)) {
							groupList.add(grp.getNode().getGroupContext());
						}
					}
					// process
					else if (child instanceof ProcessNodeDesc) {
						ProcessNodeDesc proc = (ProcessNodeDesc)child;
						IMIContainerDMContext procCtx = proc.getProcessContext();

						// looking for process
						if (wantedCtx instanceof IMIContainerDMContext) {
							// process is the execution context we are looking for?
							if (procCtx.equals(wantedCtx)) {
								groupList.add(grp.getNode().getGroupContext());
							}
						}
					}
				}
			}
			
			rm.done(groupList.toArray(new IGroupDMContext[groupList.size()]));
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
		return "Group-" + new Integer(fNewGroupId++).toString(); //$NON-NLS-1$
	}

	private UserGroupData findNode(UserGroupNodeDesc node) {
		for (UserGroupData current : fGroupList) { 
			if (current.getNode().equals(node)) {
				return current;
			}
		}
		return null;
	}

//		private ArrayList<UserGroupData> findNodesWithParent( ExecNodeDesc parent) {
//			ArrayList<UserGroupData> ret = new ArrayList<UserGroupData>(); 
////			for( UserGroupData current : fGroupList) 
////				if( current.fParent.equals(parent))
////					ret.add(current);
//			return ret;
//		}
	//	
////		private UserGroupData findThreadEntry( IMIExecutionDMContext threadDmc) {
////			for( UserGroupData current : fGroupList) 
////				if( current.hasChild(new ThreadNodeDesc(threadDmc)))
////						return current;
////			return null;
////		}
	//
//		private void moveToNewEntry( UserGroupData newEntry, ExecNodeDesc childDesc) {
	//
//			// if childDesc already exists, sets its parent.  
//			UserGroupData existingNode = findNode( childDesc);
////			if( existingNode != null)
////				existingNode.fParent = newEntry.getNode();
//			
//			// if other parents are having this child, remove the child from their lists. 
//			for( UserGroupData current : fGroupList)
//				current.getChildren().remove(childDesc);
//			
//			// add the entry to the newEntry childList.  
//			newEntry.addChild(childDesc);
//		}
		
//		private IContainerDMContext createContainerPath( IContainerDMContext container, IMIExecutionDMContext threadDmc) {
//			
//			UserGroupData current = findThreadEntry(threadDmc);
//			if( current == null)
//				return container;
//			
//			// insert the Group Contexts from top to bottom.
//			ArrayList<UserGroupData> path = new ArrayList<UserGroupData>(); 
//			
//			do {
//				path.add(current);
//				current = findNode(current.fParent); 
//			} while( current != null);
//			
//			IContainerDMContext containerDmc = container;
//			for( int i = 0; i < path.size(); ++i) {
//				String groupName = path.get(path.size()-i-1).getNode().fId; 
//				containerDmc = new MIUserGroupDMC(container.getSessionId(),
//					new IDMContext[] { containerDmc }, groupName);
//			}
//			
//			return containerDmc;
//		}
		
		public IContainerDMContext[] getChildContainers(IContainerDMContext container) {
			
//			if (container == null) {
//				return null; //TODO return all groups
//			}
//			
//			if (container instanceof MIUserGroupDMC) {
//				MIUserGroupDMC parentGroup = (MIUserGroupDMC) container;
//				String id = parentGroup.getId();
//				UserGroupData data = findNode(new UserGroupNodeDesc(id));
//				if (data == null) {
//					return new IContainerDMContext[0];
//				}
//				
//				ExecNodeDesc parent = data.getNode();	
	//
////				ArrayList<UserGroupData> children = findNodesWithParent(parent);
////				ArrayList<IContainerDMContext> dmcs = new ArrayList<IContainerDMContext>();  
////				for (UserGroupData current : children) { 
////					dmcs.add(new MIUserGroupDMC(container.getSessionId(),
////						new IDMContext[] { container }, current.getNode().fId));
////				}
////				return dmcs.toArray(new IContainerDMContext[0]);
//			}

			return new IContainerDMContext[0];
		}

//		public IExecutionDMContext[] getExecutionContexts(IContainerDMContext containerDmc, IExecutionDMContext[] threads) {
//			
//			// if there are no groups added - return the original thread list. 
//			if (fGroupList == null || fGroupList.size() == 0 || containerDmc == null)
//				return threads;
	//
//			// if the container is a group, return its children only if they belong to 
//			// the original list provided. 
//			if (containerDmc instanceof MIUserGroupDMC) {
//				String groupId = ((MIUserGroupDMC)containerDmc).getId();
//				UserGroupData groupData = findNode(new UserGroupNodeDesc(groupId));
//				if (groupData != null)
//					return groupData.filterThreads(threads);
//				return new IExecutionDMContext[0];
//			}
	//
//			// we are dealing with a process and need to find its threads
//			// that are not part of any group 
//			return getUngroupedThreads(threads);
//		}

//		/**
//		 * Returns all threads that don't belong to any of the groups from allThreads.
//		 * 
//		 * @param allThreads - the list of all threads. 
//		 * @return Returns all threads that don't belong to any of the groups from allThreads.
//		 */
//		private IExecutionDMContext[] getUngroupedThreads( IExecutionDMContext[] allThreads) {
//			ArrayList< IExecutionDMContext> ret = new ArrayList< IExecutionDMContext>();
//			for( IExecutionDMContext thread : allThreads) {
//				IMIExecutionDMContext threadCntx = DMContexts.getAncestorOfType(thread, IMIExecutionDMContext.class);
//				if( threadCntx != null) {
//					boolean threadInsideGroup = false;
//					for( UserGroupData cur : fGroupList) {
//							if( cur.hasChild(new ThreadNodeDesc(threadCntx))) {
//								threadInsideGroup = true;
//								break;
//							}
//					}
//					if( !threadInsideGroup) 
//						ret.add(thread);
//				}
//			}
//			return ret.toArray( new IExecutionDMContext[0]);
//		}
		
//		private IContainerDMContext getLowestCommonParent( IExecutionDMContext[] contexts) {
	//
//			int lowestLevel = getLevel(contexts[0]);
//			for( int i = 1; i < contexts.length; ++i) {
//				int currentLevel = getLevel(contexts[i]);
//				if( currentLevel < lowestLevel)
//					lowestLevel = currentLevel;
//			}
//			
//			if( lowestLevel < 1)
//				return null;
//			
//			for( int level = lowestLevel-1; level >= 0; --level) {
//				boolean sameParent = true;
//				IContainerDMContext first = getParentAtLevel(contexts[0],level);
//				for( int i = 1; i < contexts.length; ++i) {
//					IContainerDMContext current = getParentAtLevel(contexts[i],level);
//					if( !current.equals(first)) {
//						sameParent = false;
//						break;
//					}
//				}
//				if( sameParent)
//					return first;
//			}
//			return null;
//		}
		
//		/**
//		 * One of these contexts is a parent ( grand.... parent) of any one of the other contexts.  
//		 * @param contexts
//		 * @return
//		 */
//		private boolean contextsAreRelated( IExecutionDMContext[] contexts) {
//			for( int i = 0; i < contexts.length; ++i)  
//				for( int j = i+1; j < contexts.length; ++j)
//					if( isParentOf( contexts[i], contexts[j]) || isParentOf( contexts[j], contexts[i]))
//						return true;
//			return false;
//		}

//		private boolean isParentOf( IExecutionDMContext parent, IExecutionDMContext child) {
//			IExecutionDMContext current = child;
//			do {
//				if( current.equals(parent))
//					return true;
//				current = DMContexts.getParentOfType(current, IContainerDMContext.class);
//			} while( current != null);
//			return false;
//		}
	//	
//		/**
//		 * The level in the hierarchy of the container contexts.    
//		 * 
//		 * @param context
//		 * @return 0 if the execution context is a top level context.
//		 *  
//		 */
//		private int getLevel( IExecutionDMContext context) {
//			
//			int level = -1;
//			IExecutionDMContext parent = context;
//			do {
//				parent = DMContexts.getParentOfType(parent, IContainerDMContext.class);
//				level++;
//			} while( parent != null);
//			return level;
//		}
	//
//		/**
//		 * 
//		 * @param context
//		 * @param level
//		 * @return
//		 */
//		private IContainerDMContext getParentAtLevel( IExecutionDMContext context, int level) {
//			
//			int delta = getLevel( context) - level;
//			if( delta <= 0)
//				return null;
//			
//			IContainerDMContext parent = DMContexts.getParentOfType(context, IContainerDMContext.class);
//			for( int i = 1; i < delta; ++i) 
//				parent = DMContexts.getParentOfType(parent, IContainerDMContext.class);
//			return parent;
//		}

		
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
							System.out.println("^$^$^$^$^$^$^$$^^$ adding DSF session listener: " + getSession().toString()); //$NON-NLS-1$
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
								System.out.println("^&^&^&^&^&^&^&^&^&^ Removing DSF session listener: " + getSession().toString()); //$NON-NLS-1$
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
		public void handleEvent(IExitedDMEvent event) {
			System.out.println("GDBGrouping # handleEvent(IExitedDMEvent)"); //$NON-NLS-1$
			System.out.println("thread that exited: " + event.getDMContext()); //$NON-NLS-1$
			
			if (fGroupList == null) {
				return;
			}
			
			IExecutionDMContext exitedThreadOrProcess = event.getDMContext();
			// remove it from any group
			
			for (UserGroupData group : fGroupList) {
				boolean groupChanged = false;
				for (ExecNodeDesc child : group.getChildren()) {
					if (child instanceof ThreadNodeDesc) {
						if (exitedThreadOrProcess.equals(((ThreadNodeDesc)child).getThreadContext())) {
							groupChanged = true;
							// remove this execution context from group
							group.removeChild(child);
							System.out.println("==================== removing thread from group: " + child + group); //$NON-NLS-1$
						}
					}
				}
				if (groupChanged) {
					ContainerLayoutDMContext layoutDmc = new ContainerLayoutDMContext( 
							getSession().getId(), new IDMContext[] { group.getNode().getGroupContext() });
					
					GroupModifiedEvent changeEvent = new GroupModifiedEvent(layoutDmc); 
					getSession().dispatchEvent(changeEvent, getProperties());
				}
			}
			
		}
}

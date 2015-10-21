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
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.internal.provisional.service.IExecutionContextTranslator;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * The grouping service implementation.
 * This service delegates that details of grouping functionality
 * to a UserGroupManager class.
 *  
 * @since 4.9
 */
public class GdbGrouping extends AbstractDsfService 
	implements IGdbGrouping {
	
	private static final String GROUP_ALL_NAME = "GroupAll"; //$NON-NLS-1$
	
	/** Base class for any kind of element of a group, i.e threads, processes, groups */
	private interface ExecNodeDesc {}
	
	/** Thread element part of a group */
	private static class ThreadNodeDesc implements ExecNodeDesc {
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
	private static class ProcessNodeDesc implements ExecNodeDesc {
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
	private static class UserGroupNodeDesc implements ExecNodeDesc {
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
	 * Represents a node in a group hierarchy.
	 * It can be a group itself, or a process or a thread.
	 */
	private static class UserGroupData {
		
		private ExecNodeDesc fNode;
		private Set<ExecNodeDesc> fChildren = new HashSet<>();
		
		public UserGroupData(ExecNodeDesc node) {
			fNode = node;
		}
		
		public ExecNodeDesc getNode() {
			return fNode;
		}

		public void setNode(ExecNodeDesc node) {
			fNode = node;
		}

		public ExecNodeDesc[] getChildren() {
			return fChildren.toArray(new ExecNodeDesc[fChildren.size()]);
		}

		public void addChild(ExecNodeDesc child) {
			assert !(fNode instanceof ThreadNodeDesc);   // A thread node cannot have children at all
			assert !(fNode instanceof ProcessNodeDesc);  // The only children a process can have are all its threads, 
														 // so we don't store them as children but instead rely
														 // on the IProcesses service
			fChildren.add(child);
		}
		
		public boolean hasChild(ExecNodeDesc child) {
			return fChildren.contains(child); 
		}
		
//		IExecutionDMContext[] filterThreads(IExecutionDMContext[] allThreads) {
//			ArrayList< IExecutionDMContext> ret = new ArrayList<IExecutionDMContext>();
//			for(IExecutionDMContext currentThread : allThreads) {
//				IMIExecutionDMContext threadCntx = DMContexts.getAncestorOfType(currentThread, IMIExecutionDMContext.class);
//				if (threadCntx != null) 
//					if (getChildren().contains(new ThreadNodeDesc(threadCntx)))
//						ret.add(currentThread);
//			}
//			return ret.toArray(new IExecutionDMContext[0]);
//		}
		
		@Override
		public String toString() {
			return fNode.toString() + "(" + fChildren.size() + " children)"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	private static class ContainerLayoutDMContext extends AbstractDMContext 
	implements IContainerLayoutDMContext {
		
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
	}		
	
	private class ContainerLayoutChangedEvent extends AbstractDMEvent<IContainerLayoutDMContext> 
	implements IContainerLayoutChangedEvent {
		public ContainerLayoutChangedEvent(IContainerLayoutDMContext context) {
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

	public GdbGrouping(DsfSession session) {
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
		
		register(new String[] { IGdbGrouping.class.getName(),
                                IExecutionContextTranslator.class.getName() },
                 new Hashtable<String, String>());
		
		requestMonitor.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		unregister();
		super.shutdown(requestMonitor);
	}

	protected boolean doCanGroup(IExecutionDMContext[] contexts) {
		//TODO should we allow a group of one element, to act as a filter?
		// e.g., group thread 36 so that only that one is visible?
		if (contexts == null || contexts.length < 2) {
			return false;
		}
		return true;
	}
	
	protected boolean doCanUngroup(IExecutionDMContext[] contexts) {
		if (contexts == null || contexts.length == 0) {
			return false;
		}

		for (IExecutionDMContext context : contexts) {
			if (!(context instanceof IGroupDMContext)) {
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
			UserGroupData groupAllData = new UserGroupData(groupAll);
			fGroupList = new ArrayList<>();
			fGroupList.add(groupAllData);
		}
		
		// Create a new group with the specified elements
		String name = newGroupName();
		MIUserGroupDMC newGroupDmc = new MIUserGroupDMC(getSession(), new IDMContext[] { fCommandControl.getContext() }, name);
		UserGroupNodeDesc groupDesc = new UserGroupNodeDesc(newGroupDmc);
		UserGroupData newGroupData = new UserGroupData(groupDesc);
		fGroupList.add(0, newGroupData);

		// Add all the children to the new group
		for (IExecutionDMContext dmc : contexts) {
			if (dmc instanceof IMIExecutionDMContext) {
				newGroupData.addChild(new ThreadNodeDesc((IMIExecutionDMContext)dmc));
			} else if (dmc instanceof IMIContainerDMContext) {
				newGroupData.addChild(new ProcessNodeDesc((IMIContainerDMContext)dmc));
			} else {
				assert dmc instanceof MIUserGroupDMC;
				if (dmc instanceof MIUserGroupDMC) {
					newGroupData.addChild(new UserGroupNodeDesc((MIUserGroupDMC)dmc));
				}
			}
		}
		
		ContainerLayoutDMContext layoutDmc = new ContainerLayoutDMContext( 
				getSession().getId(), new IDMContext[] { newGroupDmc });
		ContainerLayoutChangedEvent event = new ContainerLayoutChangedEvent(layoutDmc); 
		getSession().dispatchEvent(event, getProperties());

		rm.done(newGroupDmc);
	}

	// The first version of ungroup expects the user to select the user group node(s)
	// It will remove that group and make all its children to be children of the parent 
	// of the user group.
	@Override
	public void ungroup(IExecutionDMContext[] contexts, RequestMonitor rm) {
		
		ArrayList< MIUserGroupDMC> removedGroups = new ArrayList< MIUserGroupDMC>(); 
		for( IExecutionDMContext context : contexts) {
			
			if(!( context instanceof MIUserGroupDMC))
				continue;
			MIUserGroupDMC userGroupDMC = (MIUserGroupDMC)context;
			UserGroupNodeDesc userGroupDesc = new UserGroupNodeDesc(userGroupDMC);
			UserGroupData userGroupEntry = findNode( userGroupDesc);
			if( userGroupEntry == null)
				continue;

			// move the parent of all children entries to be the parent of the parent. 
			for( ExecNodeDesc child : userGroupEntry.getChildren())  {
				UserGroupData childEntry = findNode( child);
//				if( childEntry != null)
//					childEntry.fParent = userGroupEntry.fParent; 
			}

			// if there is a entry for the parent move all children to it.  
//			UserGroupData parentEntry = findNode( userGroupEntry.fParent);
//			if( parentEntry != null) {
//				parentEntry.getChildren().remove( userGroupDesc);
//				for( ExecNodeDesc child : userGroupEntry.getChildren())  
//					parentEntry.getChildren().add( child);
//			}
				
			fGroupList.remove(userGroupEntry);
			removedGroups.add(userGroupDMC);
			
		}
		//return removedGroups.toArray(new IContainerDMContext[0]);
		ContainerLayoutDMContext layoutDmc = new ContainerLayoutDMContext( 
				getSession().getId(), new IDMContext[] { contexts[0] }); //TODO
		ContainerLayoutChangedEvent event = new ContainerLayoutChangedEvent(layoutDmc); 
		getSession().dispatchEvent(event, getProperties());
		rm.done();
	}

    @Override
	public void getExecutionContexts(IContainerDMContext containerDmc, final DataRequestMonitor<IExecutionDMContext[]> rm) {
		
		if (fGroupList == null || fGroupList.size() == 0) {
			// There has been no group created at all
			getNonGroupedContexts(containerDmc, rm);
			return;
		}

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
			ArrayList<IGroupDMContext> groups = new ArrayList<>();
			for (UserGroupData data : fGroupList) {
				if (data.getNode() instanceof UserGroupNodeDesc) {
					groups.add(((UserGroupNodeDesc)data.getNode()).getGroupContext());
				}
			}
			rm.done(groups.toArray(new IExecutionDMContext[groups.size()]));
			return;
		}

		if (containerDmc instanceof MIUserGroupDMC) {
			// For a specific group, return all its children
			UserGroupData groupData = findNode(new UserGroupNodeDesc((MIUserGroupDMC)containerDmc));
			assert groupData != null;
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
	public void getExecutionData(IGroupDMContext group, DataRequestMonitor<IGroupDMData> rm) {
		if (group instanceof MIUserGroupDMC) {
			MIUserGroupDMC groupDmc = (MIUserGroupDMC)group;
			rm.done(new MIUserGroupDMData(groupDmc.getId(), groupDmc.getId()));
		} else {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid type of context", null)); //$NON-NLS-1$
		}
	}

	protected void getNonGroupedContexts(final IContainerDMContext containerDmc, final DataRequestMonitor<IExecutionDMContext[]> rm) {
		// When calling this method, we are dealing with a container that is not part of a group hierarchy.
		assert DMContexts.getAncestorOfType(containerDmc, IGroupDMContext.class) == null;

		// In this case, we fetch the list of children from the processes service
		IMIProcesses procService = getServicesTracker().getService(IMIProcesses.class);
		ICommandControlService control = getServicesTracker().getService(ICommandControlService.class);
		if (procService == null || control == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Cannot find service", null)); //$NON-NLS-1$
			return;
		}

		procService.getProcessesBeingDebugged(
			containerDmc == null ? control.getContext() : containerDmc,
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



		
	private String newGroupName() {
		return "Group " + new Integer(fNewGroupId++).toString(); //$NON-NLS-1$
	}

	private UserGroupData findNode(ExecNodeDesc node) {
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
		
}

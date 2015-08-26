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
package org.eclipse.cdt.dsf.gdb.internal.provisional.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.provisional.service.IMIExecutionContextTranslator.IGroupDMContext;
import org.eclipse.cdt.dsf.gdb.internal.provisional.service.MIExecutionContextTranslator.MIUserGroupDMC;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This class handles all the logic of managing groups.
 * It allows to create, modify and remove groups and keeps track of
 * all existing groups.
 * 
 * All groups
 */
public class MIUserGroupManager {
	
	public static final String GROUP_ALL_NAME = "GroupAll"; //$NON-NLS-1$
	
	/** Base class for any kind of element of a group, i.e threads, processes, groups */
	private interface ExecNodeDesc {}
	
	/** Thread element part of a group */
	private static class ThreadNodeDesc implements ExecNodeDesc {
		// Although the same thread can be part of different groups,
		// they remain the same for our purposes, so we only need to store
		// and id.
		private String fThreadId;
		
		public ThreadNodeDesc(String id) {
			fThreadId = id;
		}
		
		public String getThreadId() {
			return fThreadId;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ThreadNodeDesc)) {
				return false;
			}
			
			ThreadNodeDesc other = (ThreadNodeDesc)obj; 
			return fThreadId.equals(other.fThreadId); 
		}

		@Override
		public int hashCode() {
			return fThreadId.hashCode();
		}
		
		@Override
		public String toString() {
			return fThreadId;
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
		// Each group, although they can be represented more than once,
		// will always have the same children, so we only need to store
		// and id.
		private String fId;
		
		public UserGroupNodeDesc(String id) {
			fId = id;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof UserGroupNodeDesc)) {
				return false;
			}
			
			UserGroupNodeDesc other = (UserGroupNodeDesc)obj; 
			return fId.equals(other.fId);
		}

		@Override
		public int hashCode() {
			return fId.hashCode();
		}

		@Override
		public String toString() {
			return fId;
		}
	}
	
	/**
	 * Represents a node in a group hierarchy.
	 * It can be a group itself, or a process or a thread.
	 */
	private static class UserGroupData {
		
		private ExecNodeDesc fNode;
		private ArrayList<ExecNodeDesc> fChildren = new ArrayList<>();
		
		public UserGroupData(ExecNodeDesc node) {
			fNode = node;
		}
		
		public ExecNodeDesc getNode() {
			return fNode;
		}

		public void setNode(ExecNodeDesc node) {
			fNode = node;
		}

		public ArrayList<ExecNodeDesc> getChildren() {
			return fChildren;
		}

		public void addChild(ExecNodeDesc child) {
			assert !(fNode instanceof ThreadNodeDesc);
			assert !(fNode instanceof UserGroupNodeDesc && child instanceof ThreadNodeDesc);
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
	
	private final DsfSession fSession;
	private final ICommandControlService fCommandControl;
	private final IGDBProcesses fProcesses;
	private int fNewGroupId = 1;
	private ArrayList<UserGroupData> fGroupList;
	
	public MIUserGroupManager(DsfSession session, DsfServicesTracker tracker) {
	    fSession = session;
		fCommandControl = tracker.getService(ICommandControlService.class);
		fProcesses = tracker.getService(IGDBProcesses.class);
	}

	protected boolean canGroup(IExecutionDMContext[] contexts) {
		//TODO shoudl we allow a group of one element, to act as a filter?
		// e.g., group thread 36 so that only that one is visible?
		if (contexts == null || contexts.length < 2) {
			return false;
		}
		return true;
	}
	
	protected boolean canUngroup(IExecutionDMContext[] contexts) {
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
	public IGroupDMContext group(IExecutionDMContext[] contexts) {
		if (!canGroup(contexts)) {
			return null;
		}

		if (fGroupList == null) {
			// We are about to create the first group.  Create the list and add the default group in it.
			UserGroupNodeDesc groupAll = new UserGroupNodeDesc(GROUP_ALL_NAME);
			UserGroupData groupAllData = new UserGroupData(groupAll);
			fGroupList = new ArrayList<>();
			fGroupList.add(groupAllData);
		}
		
		// Create a new group with the specified elements
		String name = newGroupName();
		MIUserGroupDMC newGroupDmc = new MIUserGroupDMC(fSession, new IDMContext[] { fCommandControl.getContext() }, name);
		UserGroupNodeDesc groupDesc = new UserGroupNodeDesc(name);
		UserGroupData newGroupData = new UserGroupData(groupDesc);
		fGroupList.add(0, newGroupData);

		// Filter out unnecessary contexts and create the elements
		// Create unique elements for each type by using sets to remove duplicates
		Set<ProcessNodeDesc> processes = new HashSet<>();
		Set<UserGroupNodeDesc> groups = new HashSet<>();
		for (IExecutionDMContext dmc : contexts) {
			if (dmc instanceof IMIExecutionDMContext) {
				// When we want to group a thread, we actually group its parent process first
				IMIProcessDMContext procDmc = DMContexts.getAncestorOfType(dmc, IMIProcessDMContext.class);
				IProcessDMContext newProcDmc = fProcesses.createProcessContext(newGroupDmc, procDmc.getProcId());
				IMIContainerDMContext threadGroupDmc = DMContexts.getAncestorOfType(dmc, IMIContainerDMContext.class);
				IMIContainerDMContext newContainerProcDmc = fProcesses.createContainerContext(newProcDmc, threadGroupDmc.getGroupId());
				ProcessNodeDesc parentProc = new ProcessNodeDesc(newContainerProcDmc);
				processes.add(parentProc);
				
				// Now add the thread to the process itself in our group list
				ThreadNodeDesc threadDesc = new ThreadNodeDesc(Integer.toString(((IMIExecutionDMContext)dmc).getThreadId()));

				UserGroupData parentData = findNode(parentProc);
				if (parentData == null) {
					parentData = new UserGroupData(parentProc);
					fGroupList.add(parentData);
				}
				if (!parentData.hasChild(threadDesc)) {
					// That process does not have this thread as part of its group yet
					parentData.addChild(threadDesc);
				}
			} else if (dmc instanceof IMIContainerDMContext) {
				// Create the new hierarchical context first
				IMIProcessDMContext procDmc = DMContexts.getAncestorOfType(dmc, IMIProcessDMContext.class);
				IProcessDMContext newProcDmc = fProcesses.createProcessContext(newGroupDmc, procDmc.getProcId());
				IMIContainerDMContext newContainerProcDmc = fProcesses.createContainerContext(newProcDmc, ((IMIContainerDMContext)dmc).getGroupId());
				processes.add(new ProcessNodeDesc(newContainerProcDmc));
			} else {
				assert dmc instanceof MIUserGroupDMC;
				if (dmc instanceof MIUserGroupDMC) {
					groups.add(new UserGroupNodeDesc(((MIUserGroupDMC)dmc).getId()));
				}
			}
		}

		for (ExecNodeDesc elem : groups) {
			newGroupData.addChild(elem);
		}

		for (ExecNodeDesc elem : processes) {
			newGroupData.addChild(elem);
		}

		return newGroupDmc;
	}

	// The first version of ungroup expects the user to select the user group node(s)
	// It will remove that group and make all its children to be children of the parent 
	// of the user group.  
	IContainerDMContext[] ungroup( IExecutionDMContext[] contexts) {
		
		ArrayList< MIUserGroupDMC> removedGroups = new ArrayList< MIUserGroupDMC>(); 
		for( IExecutionDMContext context : contexts) {
			
			if(!( context instanceof MIUserGroupDMC))
				continue;
			MIUserGroupDMC userGroupDMC = (MIUserGroupDMC)context;
			UserGroupNodeDesc userGroupDesc = new UserGroupNodeDesc(userGroupDMC.getId());
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
		return removedGroups.toArray(new IContainerDMContext[0]);
	}
	
	private String newGroupName() {
		return "Group "+new Integer(fNewGroupId++).toString(); //$NON-NLS-1$
	}
	
	private UserGroupData findNode(ExecNodeDesc node) {
		for (UserGroupData current : fGroupList) { 
			if (current.getNode().equals(node)) {
				return current;
			}
		}
		return null;
	}

//	private ArrayList<UserGroupData> findNodesWithParent( ExecNodeDesc parent) {
//		ArrayList<UserGroupData> ret = new ArrayList<UserGroupData>(); 
////		for( UserGroupData current : fGroupList) 
////			if( current.fParent.equals(parent))
////				ret.add(current);
//		return ret;
//	}
//	
////	private UserGroupData findThreadEntry( IMIExecutionDMContext threadDmc) {
////		for( UserGroupData current : fGroupList) 
////			if( current.hasChild(new ThreadNodeDesc(threadDmc)))
////					return current;
////		return null;
////	}
//
//	private void moveToNewEntry( UserGroupData newEntry, ExecNodeDesc childDesc) {
//
//		// if childDesc already exists, sets its parent.  
//		UserGroupData existingNode = findNode( childDesc);
////		if( existingNode != null)
////			existingNode.fParent = newEntry.getNode();
//		
//		// if other parents are having this child, remove the child from their lists. 
//		for( UserGroupData current : fGroupList)
//			current.getChildren().remove(childDesc);
//		
//		// add the entry to the newEntry childList.  
//		newEntry.addChild(childDesc);
//	}
	
//	private IContainerDMContext createContainerPath( IContainerDMContext container, IMIExecutionDMContext threadDmc) {
//		
//		UserGroupData current = findThreadEntry(threadDmc);
//		if( current == null)
//			return container;
//		
//		// insert the Group Contexts from top to bottom.
//		ArrayList<UserGroupData> path = new ArrayList<UserGroupData>(); 
//		
//		do {
//			path.add(current);
//			current = findNode(current.fParent); 
//		} while( current != null);
//		
//		IContainerDMContext containerDmc = container;
//		for( int i = 0; i < path.size(); ++i) {
//			String groupName = path.get(path.size()-i-1).getNode().fId; 
//			containerDmc = new MIUserGroupDMC(container.getSessionId(),
//				new IDMContext[] { containerDmc }, groupName);
//		}
//		
//		return containerDmc;
//	}
	
	public IContainerDMContext[] getChildContainers(IContainerDMContext container) {
		
//		if (container == null) {
//			return null; //TODO return all groups
//		}
//		
//		if (container instanceof MIUserGroupDMC) {
//			MIUserGroupDMC parentGroup = (MIUserGroupDMC) container;
//			String id = parentGroup.getId();
//			UserGroupData data = findNode(new UserGroupNodeDesc(id));
//			if (data == null) {
//				return new IContainerDMContext[0];
//			}
//			
//			ExecNodeDesc parent = data.getNode();	
//
////			ArrayList<UserGroupData> children = findNodesWithParent(parent);
////			ArrayList<IContainerDMContext> dmcs = new ArrayList<IContainerDMContext>();  
////			for (UserGroupData current : children) { 
////				dmcs.add(new MIUserGroupDMC(container.getSessionId(),
////					new IDMContext[] { container }, current.getNode().fId));
////			}
////			return dmcs.toArray(new IContainerDMContext[0]);
//		}

		return new IContainerDMContext[0];
	}
	
	public void getExecutionContexts(IContainerDMContext containerDmc, final DataRequestMonitor<IExecutionDMContext[]> rm) {
		
		if (fGroupList == null || fGroupList.size() == 0) {
			// There has been no group created at all
			rm.setData(null);
			rm.done();
			return;
		}

		final MIUserGroupDMC groupAll = DMContexts.getAncestorOfType(containerDmc, MIUserGroupDMC.class);
		if (groupAll != null && groupAll.getId().equals(GROUP_ALL_NAME)) {
			final IDMContext finalContext = containerDmc instanceof IGroupDMContext ? fCommandControl.getContext() : containerDmc;

			fProcesses.getProcessesBeingDebugged(
				finalContext,
				new DataRequestMonitor<IDMContext[]>(fProcesses.getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						if (getData() instanceof IExecutionDMContext[]) {
							IExecutionDMContext[] dmcs = (IExecutionDMContext[])getData();

							Set<IExecutionDMContext> nodes = new HashSet<>();
							for (IExecutionDMContext dmc : dmcs) {
								if (dmc instanceof IMIContainerDMContext) {
									// Create the new hierarchical context first
									IMIProcessDMContext procDmc = DMContexts.getAncestorOfType(dmc, IMIProcessDMContext.class);
									IProcessDMContext newProcDmc = fProcesses.createProcessContext(groupAll, procDmc.getProcId());
									IMIContainerDMContext newContainerProcDmc = fProcesses.createContainerContext(newProcDmc, ((IMIContainerDMContext)dmc).getGroupId());
									nodes.add(newContainerProcDmc);
								} else {
									assert dmc instanceof IMIExecutionDMContext;
									nodes.add(dmc);
								}
							}
							rm.done(nodes.toArray(new IExecutionDMContext[nodes.size()]));
						} else {
							rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Invalid contexts", null)); //$NON-NLS-1$
						}
					}
				});
			return;
		}

		if (containerDmc == null) {
			// Return all the groups
			ArrayList<IGroupDMContext> groups = new ArrayList<>();
			for (UserGroupData data : fGroupList) {
				if (data.getNode() instanceof UserGroupNodeDesc) {
					groups.add(new MIUserGroupDMC(fSession, 
							                      new IDMContext[] { fCommandControl.getContext() }, 
							                      ((UserGroupNodeDesc)data.getNode()).fId));
				}
			}
			rm.done(groups.toArray(new IExecutionDMContext[groups.size()]));
			return;
		}

		if (containerDmc instanceof MIUserGroupDMC) {
			// Return all children of this group (which can only be processes or groups)
			String groupId = ((MIUserGroupDMC)containerDmc).getId();
			UserGroupData groupData = findNode(new UserGroupNodeDesc(groupId));
			assert groupData != null;
			if (groupData == null) {
				rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Cannot find specified group: " + groupId, null)); //$NON-NLS-1$
				return;
			}
			
			ArrayList<IExecutionDMContext> childrenDmcs = new ArrayList<>();
			for (ExecNodeDesc childDesc : groupData.getChildren()) {
				if (childDesc instanceof UserGroupNodeDesc) {
					childrenDmcs.add(new MIUserGroupDMC(fSession, 
		                                                new IDMContext[] { containerDmc }, 
		                                                ((UserGroupNodeDesc)childDesc).fId));
				} else if (childDesc instanceof ProcessNodeDesc) {
					childrenDmcs.add(((ProcessNodeDesc)childDesc).getProcessContext());
				} else {
					assert false;
				}
			}
			rm.done(childrenDmcs.toArray(new IExecutionDMContext[childrenDmcs.size()]));
			return;
		}

		if (containerDmc instanceof IMIContainerDMContext) {
			// Return all children of this process (which can only be threads)
			UserGroupData procData = findNode(new ProcessNodeDesc((IMIContainerDMContext)containerDmc));
			if (procData == null) {
				// The original process with all its threads, so we get all threads from the process service
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
	        			});				return;
			}
			
			// If the children are stored in the entry, it means the user selected those threads
			// specifically and we should only use them.  Instead, if there are no children, it means
			// the user selected the process itself when creating the group, and we should return
			// all its current running threads.
			if (!procData.getChildren().isEmpty()) {	
				ArrayList<IExecutionDMContext> childrenDmcs = new ArrayList<>();
				for (ExecNodeDesc childDesc : procData.getChildren()) {
					if (childDesc instanceof ThreadNodeDesc) {
						String threadId = ((ThreadNodeDesc)childDesc).getThreadId();
						IProcessDMContext procDmc = DMContexts.getAncestorOfType(containerDmc, IProcessDMContext.class);
						IThreadDMContext threadDmc = fProcesses.createThreadContext(procDmc, threadId);
						childrenDmcs.add(fProcesses.createExecutionContext(containerDmc, threadDmc, threadId));
					} else {
						assert false;
					}
				}
				rm.done(childrenDmcs.toArray(new IExecutionDMContext[childrenDmcs.size()]));
			} else {
				assert false;
			}
			return;
		}

		rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Unexpected container context", null)); //$NON-NLS-1$
	}
	
//	public IExecutionDMContext[] getExecutionContexts(IContainerDMContext containerDmc, IExecutionDMContext[] threads) {
//		
//		// if there are no groups added - return the original thread list. 
//		if (fGroupList == null || fGroupList.size() == 0 || containerDmc == null)
//			return threads;
//
//		// if the container is a group, return its children only if they belong to 
//		// the original list provided. 
//		if (containerDmc instanceof MIUserGroupDMC) {
//			String groupId = ((MIUserGroupDMC)containerDmc).getId();
//			UserGroupData groupData = findNode(new UserGroupNodeDesc(groupId));
//			if (groupData != null)
//				return groupData.filterThreads(threads);
//			return new IExecutionDMContext[0];
//		}
//
//		// we are dealing with a process and need to find its threads
//		// that are not part of any group 
//		return getUngroupedThreads(threads);
//	}

//	/**
//	 * Returns all threads that don't belong to any of the groups from allThreads.
//	 * 
//	 * @param allThreads - the list of all threads. 
//	 * @return Returns all threads that don't belong to any of the groups from allThreads.
//	 */
//	private IExecutionDMContext[] getUngroupedThreads( IExecutionDMContext[] allThreads) {
//		ArrayList< IExecutionDMContext> ret = new ArrayList< IExecutionDMContext>();
//		for( IExecutionDMContext thread : allThreads) {
//			IMIExecutionDMContext threadCntx = DMContexts.getAncestorOfType(thread, IMIExecutionDMContext.class);
//			if( threadCntx != null) {
//				boolean threadInsideGroup = false;
//				for( UserGroupData cur : fGroupList) {
//						if( cur.hasChild(new ThreadNodeDesc(threadCntx))) {
//							threadInsideGroup = true;
//							break;
//						}
//				}
//				if( !threadInsideGroup) 
//					ret.add(thread);
//			}
//		}
//		return ret.toArray( new IExecutionDMContext[0]);
//	}
	
	/**
	 * 
	 * @param contexts
	 * @return
	 */
//	private IContainerDMContext getLowestCommonParent( IExecutionDMContext[] contexts) {
//
//		int lowestLevel = getLevel(contexts[0]);
//		for( int i = 1; i < contexts.length; ++i) {
//			int currentLevel = getLevel(contexts[i]);
//			if( currentLevel < lowestLevel)
//				lowestLevel = currentLevel;
//		}
//		
//		if( lowestLevel < 1)
//			return null;
//		
//		for( int level = lowestLevel-1; level >= 0; --level) {
//			boolean sameParent = true;
//			IContainerDMContext first = getParentAtLevel(contexts[0],level);
//			for( int i = 1; i < contexts.length; ++i) {
//				IContainerDMContext current = getParentAtLevel(contexts[i],level);
//				if( !current.equals(first)) {
//					sameParent = false;
//					break;
//				}
//			}
//			if( sameParent)
//				return first;
//		}
//		return null;
//	}
	
//	/**
//	 * One of these contexts is a parent ( grand.... parent) of any one of the other contexts.  
//	 * @param contexts
//	 * @return
//	 */
//	private boolean contextsAreRelated( IExecutionDMContext[] contexts) {
//		for( int i = 0; i < contexts.length; ++i)  
//			for( int j = i+1; j < contexts.length; ++j)
//				if( isParentOf( contexts[i], contexts[j]) || isParentOf( contexts[j], contexts[i]))
//					return true;
//		return false;
//	}

	/**
	 * Return true if parent is a parent of child. 
	 * @param parent
	 * @param child
	 * @return true if parent is a parent of child.
	 */
//	private boolean isParentOf( IExecutionDMContext parent, IExecutionDMContext child) {
//		IExecutionDMContext current = child;
//		do {
//			if( current.equals(parent))
//				return true;
//			current = DMContexts.getParentOfType(current, IContainerDMContext.class);
//		} while( current != null);
//		return false;
//	}
//	
//	/**
//	 * The level in the hierarchy of the container contexts.    
//	 * 
//	 * @param context
//	 * @return 0 if the execution context is a top level context.
//	 *  
//	 */
//	private int getLevel( IExecutionDMContext context) {
//		
//		int level = -1;
//		IExecutionDMContext parent = context;
//		do {
//			parent = DMContexts.getParentOfType(parent, IContainerDMContext.class);
//			level++;
//		} while( parent != null);
//		return level;
//	}
//
//	/**
//	 * 
//	 * @param context
//	 * @param level
//	 * @return
//	 */
//	private IContainerDMContext getParentAtLevel( IExecutionDMContext context, int level) {
//		
//		int delta = getLevel( context) - level;
//		if( delta <= 0)
//			return null;
//		
//		IContainerDMContext parent = DMContexts.getParentOfType(context, IContainerDMContext.class);
//		for( int i = 1; i < delta; ++i) 
//			parent = DMContexts.getParentOfType(parent, IContainerDMContext.class);
//		return parent;
//	}
	
}

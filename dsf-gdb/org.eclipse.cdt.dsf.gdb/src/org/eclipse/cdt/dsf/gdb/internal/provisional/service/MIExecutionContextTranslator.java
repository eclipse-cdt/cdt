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
import java.util.Arrays;
import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
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
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * EXPERIMENTAL. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 * 
 * @since 4.0
 * @experimental
 */
public class MIExecutionContextTranslator extends AbstractDsfService 
	implements IMIExecutionContextTranslator {

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
	
	private class ContainerLayoutChangedEvent 
		extends AbstractDMEvent<IContainerLayoutDMContext> 
		implements IContainerLayoutChangedEvent {

		public ContainerLayoutChangedEvent(IContainerLayoutDMContext context) {
			super(context);
		}
	}

	private interface ExecNodeDesc {
		
	}
	
	private class ThreadNodeDesc implements ExecNodeDesc{
		IMIExecutionDMContext threadDmc;
		
		ThreadNodeDesc( IMIExecutionDMContext threadDmc) {
			this.threadDmc = threadDmc;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!( obj instanceof ThreadNodeDesc))
				return false;
			
			ThreadNodeDesc other = (ThreadNodeDesc)obj; 
			return threadDmc.equals(other.threadDmc); 
		}

		@Override
		public int hashCode() {
			return threadDmc.hashCode();
		}
	}

	private class MIContainerNodeDesc implements ExecNodeDesc{
		IMIContainerDMContext miContainerDMC;
		
		MIContainerNodeDesc( IMIContainerDMContext miContainerDMC) {
			this.miContainerDMC = miContainerDMC;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!( obj instanceof MIContainerNodeDesc))
				return false;
			
			MIContainerNodeDesc other = (MIContainerNodeDesc)obj; 
			return miContainerDMC.equals(other.miContainerDMC); 
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}
	}

	private class UserGroupNodeDesc implements ExecNodeDesc{
		String id;
		
		UserGroupNodeDesc( String id) {
			this.id = id;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!( obj instanceof UserGroupNodeDesc))
				return false;
			
			UserGroupNodeDesc other = (UserGroupNodeDesc)obj; 
			return id.equals(other.id);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}
	
	
	/**
	 * Represents a user-defined group.
	 */
	private class UserGroupData {
		
		UserGroupNodeDesc node;
		ExecNodeDesc parent;
		ArrayList<ExecNodeDesc> children = new ArrayList<ExecNodeDesc>();
		
		/**
		 * 
		 * @param parent - can be null if the group exists right below the launch.  
		 */
		UserGroupData( UserGroupNodeDesc node, ExecNodeDesc parent) {
			this.node = node;
			this.parent = parent;
		}
		
		void addChild( ExecNodeDesc child) {
			children.add( child);
		}
		
		boolean hasChild( ExecNodeDesc child) {
			return children.contains(child); 
		}
		
		IExecutionDMContext[] filterThreads(IExecutionDMContext[] allThreads) {
			ArrayList< IExecutionDMContext> ret = new ArrayList<IExecutionDMContext>();
			for(IExecutionDMContext currentThread : allThreads) {
				IMIExecutionDMContext threadCntx = DMContexts.getAncestorOfType(currentThread, IMIExecutionDMContext.class);
				if (threadCntx != null) 
					if (children.contains(new ThreadNodeDesc(threadCntx)))
						ret.add(currentThread);
			}
			return ret.toArray(new IExecutionDMContext[0]);
		}		
	}
	
	/**
	 * This class handles all the logic of managing groups.
	 * It allows to create, modify and remove groups and keeps track of
	 * all existing groups.
	 * 
	 * All groups
	 */
	protected class UserGroupManager {
		private int fNewGroupId = 0;
		private ArrayList<UserGroupData> fGroupList = new ArrayList<>();

		/**
		 * Creates a group from an array of IExecutionDMContext and returns
		 * a corresponding IGroupDMContext.
		 * 
		 * Groups that can be created:
		 * - Any set of threads even from different processes
		 * 		o if all threads belong to the same process, the group will be a child of that process
		 * 		o if threads belong to different processes, the group will be a child of the launch
		 * - Any set of processes
		 */
		public IGroupDMContext group(IExecutionDMContext[] contexts) {
			
	    	IContainerDMContext commonParent = getLowestCommonParent(contexts);
	    	if( commonParent != null) {
	    		
	    		String name = newGroupName();
				UserGroupNodeDesc groupDesc = new UserGroupNodeDesc(name);
				ExecNodeDesc parentDesc = null;
	    		if( commonParent instanceof UserGroupDMC) {
	    			String parentGroupId = ((UserGroupDMC)commonParent).getId();
	    			UserGroupData data = findNode(new UserGroupNodeDesc(parentGroupId));
	    			parentDesc = data.node;
	    		}
	    		else if( commonParent instanceof IMIContainerDMContext){
	    			parentDesc = new MIContainerNodeDesc((IMIContainerDMContext)commonParent);
	    		}
	    		
				UserGroupData groupData = new UserGroupData( groupDesc, parentDesc);
				for( IExecutionDMContext cntx : contexts) {
					ExecNodeDesc childDesc = null;
					
					if( cntx instanceof UserGroupDMC){
						String childGroupId = ((UserGroupDMC)cntx).getId();
						childDesc = new UserGroupNodeDesc(childGroupId);
					}
					else if( !(cntx instanceof IContainerDMContext)) { 
						IMIExecutionDMContext threadCntx = DMContexts.getAncestorOfType(cntx, IMIExecutionDMContext.class);
						if( threadCntx != null)
							childDesc = new ThreadNodeDesc(threadCntx);
					}
					if( childDesc != null) {
						// remove it from the list of previous parent.
						moveToNewEntry( groupData, childDesc);						
					}
				}
				fGroupList.add(groupData);
	    		
	        	UserGroupDMC groupDmc = new UserGroupDMC(commonParent.getSessionId(),
	   				new IDMContext[] { commonParent }, name);
	        	return groupDmc;
	    	}
			
			return null;
		}

		// The first version of ungroup expects the user to select the user group node(s)
		// It will remove that group and make all its children to be children of the parent 
		// of the user group.  
		IContainerDMContext[] ungroup( IExecutionDMContext[] contexts) {
			
			ArrayList< UserGroupDMC> removedGroups = new ArrayList< UserGroupDMC>(); 
			for( IExecutionDMContext context : contexts) {
				
				if(!( context instanceof UserGroupDMC))
					continue;
				UserGroupDMC userGroupDMC = (UserGroupDMC)context;
				UserGroupNodeDesc userGroupDesc = new UserGroupNodeDesc(userGroupDMC.getId());
				UserGroupData userGroupEntry = findNode( userGroupDesc);
				if( userGroupEntry == null)
					continue;

				// move the parent of all children entries to be the parent of the parent. 
				for( ExecNodeDesc child : userGroupEntry.children)  {
					UserGroupData childEntry = findNode( child);
					if( childEntry != null)
						childEntry.parent = userGroupEntry.parent; 
				}

				// if there is a entry for the parent move all children to it.  
				UserGroupData parentEntry = findNode( userGroupEntry.parent);
				if( parentEntry != null) {
					parentEntry.children.remove( userGroupDesc);
					for( ExecNodeDesc child : userGroupEntry.children)  
						parentEntry.children.add( child);
				}
					
				fGroupList.remove(userGroupEntry);
				removedGroups.add(userGroupDMC);
				
			}
			return removedGroups.toArray(new IContainerDMContext[0]);
		}
		
		String newGroupName() {
			fNewGroupId++;
			return "Group "+new Integer(fNewGroupId).toString(); //$NON-NLS-1$
		}
		
		UserGroupData findNode( ExecNodeDesc node) {
			for( UserGroupData current : fGroupList) 
				if( current.node.equals(node))
					return current;
			return null;
		}

		ArrayList<UserGroupData> findNodesWithParent( ExecNodeDesc parent) {
			ArrayList<UserGroupData> ret = new ArrayList<UserGroupData>(); 
			for( UserGroupData current : fGroupList) 
				if( current.parent.equals(parent))
					ret.add(current);
			return ret;
		}
		
		UserGroupData findThreadEntry( IMIExecutionDMContext threadDmc) {
			for( UserGroupData current : fGroupList) 
				if( current.hasChild(new ThreadNodeDesc(threadDmc)))
						return current;
			return null;
		}

		void moveToNewEntry( UserGroupData newEntry, ExecNodeDesc childDesc) {

			// if childDesc already exists, sets its parent.  
			UserGroupData existingNode = findNode( childDesc);
			if( existingNode != null)
				existingNode.parent = newEntry.node;
			
			// if other parents are having this child, remove the child from their lists. 
			for( UserGroupData current : fGroupList)
				current.children.remove(childDesc);
			
			// add the entry to the newEntry childList.  
			newEntry.addChild(childDesc);
		}
		
		IContainerDMContext createContainerPath( IContainerDMContext container, IMIExecutionDMContext threadDmc) {
			
			UserGroupData current = findThreadEntry(threadDmc);
			if( current == null)
				return container;
			
			// insert the Group Contexts from top to bottom.
			ArrayList<UserGroupData> path = new ArrayList<UserGroupData>(); 
			
			do {
				path.add(current);
				current = findNode(current.parent); 
			} while( current != null);
			
			IContainerDMContext containerDmc = container;
			for( int i = 0; i < path.size(); ++i) {
				String groupName = path.get(path.size()-i-1).node.id; 
				containerDmc = new UserGroupDMC(container.getSessionId(),
					new IDMContext[] { containerDmc }, groupName);
			}
			
			return containerDmc;
		}
		
		public IContainerDMContext[] getChildContainers( IContainerDMContext container) {
			
			ExecNodeDesc parent = null;
			if (container instanceof IMIContainerDMContext) {
				parent = new MIContainerNodeDesc((IMIContainerDMContext)container);
			}
			else if (container instanceof UserGroupDMC) {
				UserGroupDMC parentGroup = (UserGroupDMC) container;
				String id = parentGroup.getId();
				UserGroupData data = findNode( new UserGroupNodeDesc(id));
				if( data == null)
					return new IContainerDMContext[0];
				parent = data.node;	
			}
			else
				return new IContainerDMContext[0];
			
			ArrayList<UserGroupData> children = findNodesWithParent(parent);
			ArrayList<IContainerDMContext> dmcs = new ArrayList<IContainerDMContext>();  
			for( UserGroupData current : children) 
				dmcs.add( new UserGroupDMC( container.getSessionId(),
					new IDMContext[] { container }, current.node.id));
			return dmcs.toArray(new IContainerDMContext[0]);
		}
		
		public IExecutionDMContext[] getExecutionContexts(IContainerDMContext containerDmc, IExecutionDMContext[] threads) {
			
			// if there are no groups added - return the original thread list. 
			if (fGroupList == null || fGroupList.size() == 0 || containerDmc == null)
				return threads;

			// if the container is a group, return its children only if they belong to 
			// the original list provided. 
			if (containerDmc instanceof UserGroupDMC) {
				String groupId = ((UserGroupDMC)containerDmc).getId();
				UserGroupData groupData = findNode(new UserGroupNodeDesc(groupId));
				if (groupData != null)
					return groupData.filterThreads(threads);
				return new IExecutionDMContext[0];
			}

			// we are dealing with a process and need to find its threads
			// that are not part of any group 
			return getUngroupedThreads(threads);
		}

		/**
		 * Returns all threads that don't belong to any of the groups from allThreads.
		 * 
		 * @param allThreads - the list of all threads. 
		 * @return Returns all threads that don't belong to any of the groups from allThreads.
		 */
		private IExecutionDMContext[] getUngroupedThreads( IExecutionDMContext[] allThreads) {
			ArrayList< IExecutionDMContext> ret = new ArrayList< IExecutionDMContext>();
			for( IExecutionDMContext thread : allThreads) {
				IMIExecutionDMContext threadCntx = DMContexts.getAncestorOfType(thread, IMIExecutionDMContext.class);
				if( threadCntx != null) {
					boolean threadInsideGroup = false;
					for( UserGroupData cur : fGroupList) {
							if( cur.hasChild(new ThreadNodeDesc(threadCntx))) {
								threadInsideGroup = true;
								break;
							}
					}
					if( !threadInsideGroup) 
						ret.add(thread);
				}
			}
			return ret.toArray( new IExecutionDMContext[0]);
		}
	}
	
	private UserGroupManager groupManager = new UserGroupManager();	
	protected static class UserGroupDMC extends AbstractDMContext implements IGroupDMContext {

		String fId;

		public UserGroupDMC(String sessionId, IDMContext[] parents, String id) {
			super(sessionId, parents);
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
					&& (((UserGroupDMC) obj).fId == null ? fId == null
							: ((UserGroupDMC) obj).fId.equals(fId));
		}

		@Override
		public int hashCode() {
			return baseHashCode() ^ (fId == null ? 0 : fId.hashCode());
		}
	}
	
	protected static class UserGroupDMData implements IGroupDMData {
		private String fId;
		private String fName;
		
		public UserGroupDMData(String id, String name) {
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
	
	public MIExecutionContextTranslator(DsfSession session) {
		super(session);
	}

	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new RequestMonitor(getExecutor(), requestMonitor) {
			@Override
			protected void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	private void doInitialize(RequestMonitor requestMonitor) {
		register(new String[] { 
			IMIExecutionContextTranslator.class.getName(),
			IExecutionContextTranslator.class.getName() },
			new Hashtable<String, String>());
		requestMonitor.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		unregister();
		super.shutdown(requestMonitor);
	}

	/**
	 * 
	 * @param contexts
	 * @return
	 */
	private IContainerDMContext getLowestCommonParent( IExecutionDMContext[] contexts) {

		int lowestLevel = getLevel(contexts[0]);
		for( int i = 1; i < contexts.length; ++i) {
			int currentLevel = getLevel(contexts[i]);
			if( currentLevel < lowestLevel)
				lowestLevel = currentLevel;
		}
		
		if( lowestLevel < 1)
			return null;
		
		for( int level = lowestLevel-1; level >= 0; --level) {
			boolean sameParent = true;
			IContainerDMContext first = getParentAtLevel(contexts[0],level);
			for( int i = 1; i < contexts.length; ++i) {
				IContainerDMContext current = getParentAtLevel(contexts[i],level);
				if( !current.equals(first)) {
					sameParent = false;
					break;
				}
			}
			if( sameParent)
				return first;
		}
		return null;
	}
	
	/**
	 * One of these contexts is a parent ( grand.... parent) of any one of the other contexts.  
	 * @param contexts
	 * @return
	 */
	private boolean contextsAreRelated( IExecutionDMContext[] contexts) {
		for( int i = 0; i < contexts.length; ++i)  
			for( int j = i+1; j < contexts.length; ++j)
				if( isParentOf( contexts[i], contexts[j]) || isParentOf( contexts[j], contexts[i]))
					return true;
		return false;
	}

	/**
	 * Return true if parent is a parent of child. 
	 * @param parent
	 * @param child
	 * @return true if parent is a parent of child.
	 */
	private boolean isParentOf( IExecutionDMContext parent, IExecutionDMContext child) {
		IExecutionDMContext current = child;
		do {
			if( current.equals(parent))
				return true;
			current = DMContexts.getParentOfType(current, IContainerDMContext.class);
		} while( current != null);
		return false;
	}
	
	/**
	 * The level in the hierarchy of the container contexts.    
	 * 
	 * @param context
	 * @return 0 if the execution context is a top level context.
	 *  
	 */
	private int getLevel( IExecutionDMContext context) {
		
		int level = -1;
		IExecutionDMContext parent = context;
		do {
			parent = DMContexts.getParentOfType(parent, IContainerDMContext.class);
			level++;
		} while( parent != null);
		return level;
	}

	/**
	 * 
	 * @param context
	 * @param level
	 * @return
	 */
	private IContainerDMContext getParentAtLevel( IExecutionDMContext context, int level) {
		
		int delta = getLevel( context) - level;
		if( delta <= 0)
			return null;
		
		IContainerDMContext parent = DMContexts.getParentOfType(context, IContainerDMContext.class);
		for( int i = 1; i < delta; ++i) 
			parent = DMContexts.getParentOfType(parent, IContainerDMContext.class);
		return parent;
	}
	
	protected boolean canGroup(IExecutionDMContext[] contexts) {
		if (contexts == null || contexts.length < 2) {
			return false;
		}
		return !contextsAreRelated(contexts);
	}
	
	protected boolean canUngroup(IExecutionDMContext[] contexts) {
		if (contexts == null || contexts.length == 0) {
			return false;
		}

		for (IExecutionDMContext context : contexts) {
			if (!(context instanceof UserGroupDMC)) {
				return false;
			}
		}
		
		return true;
	}

    @Override
	public void canGroup(IExecutionDMContext[] contexts, DataRequestMonitor<Boolean> rm) {
   		rm.done(canGroup(contexts));
    }
    
    @Override
	public void canUngroup(IExecutionDMContext[] contexts, DataRequestMonitor<Boolean> rm) {
		rm.done(canUngroup(contexts));
    }
    
    @Override
	public void group(IExecutionDMContext[] contexts, final DataRequestMonitor<IContainerDMContext> rm) {
    	if (canGroup(contexts)) {
    		IGroupDMContext groupDmc = groupManager.group(contexts);
    		if (groupDmc != null) {
	    		ContainerLayoutDMContext layoutDmc = new ContainerLayoutDMContext( 
	    			groupDmc.getSessionId(), new IDMContext[] { groupDmc });
	    		ContainerLayoutChangedEvent event = new ContainerLayoutChangedEvent(layoutDmc); 
	        	getSession().dispatchEvent(event, getProperties());
	        	rm.done(groupDmc);
	        	return;
    		}
    	}
    	
    	rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unable to group specified contexts", null)); //$NON-NLS-1$
    }
    
    @Override
	public void ungroup(IExecutionDMContext[] contexts, RequestMonitor rm) {
    	if (canUngroup(contexts)) {
    		IContainerDMContext[] ungroupedDMCs = groupManager.ungroup(contexts);
    		if (ungroupedDMCs != null) {
	    		ContainerLayoutDMContext layoutDmc = new ContainerLayoutDMContext( 
	    			ungroupedDMCs[0].getSessionId(), new IDMContext[] { ungroupedDMCs[0] }); //TODO
	    		ContainerLayoutChangedEvent event = new ContainerLayoutChangedEvent(layoutDmc); 
	        	getSession().dispatchEvent(event, getProperties());
	        	rm.done();
	        	return;
    		}
    	}
    	
   		rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unable to un-group specified contexts", null)); //$NON-NLS-1$
    }

    	@Override
	public IContainerDMContext createContainerPath(
		IContainerDMContext container, IMIExecutionDMContext threadDmc) {
		return groupManager.createContainerPath(container, threadDmc);			
	}

	@Override
	public IContainerDMContext[] getChildContainers( IContainerDMContext container) {
		return groupManager.getChildContainers(container);
	}


    @Override
	public void getExecutionContexts(final IContainerDMContext containerDmc, final DataRequestMonitor<IExecutionDMContext[]> rm) {
		ICommandControlService control = getServicesTracker().getService(ICommandControlService.class);
		if (control == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "No CommandControl service", null)); //$NON-NLS-1$
			return;
		}
		
		// Fetch the groups that are children of this container
		final IExecutionDMContext[] childrenGroups = getChildContainers(containerDmc);
    	
    	IContainerDMContext topMostContainer = containerDmc;
    	
    	// to obtain the list of threads from the IMIProcesses service we need to pass 
    	// the process container, not the user user group.
    	// khou: dobrin did this because groups could not contain procs so the top
    	// container must be a proc
		IContainerDMContext top = DMContexts.getTopMostAncestorOfType(containerDmc, IContainerDMContext.class);
		
		// when the process container is passed to DMContexts.getTopMostAncestorOfType
		// it will return null, in that case we don't need to adjust the containerDmc.
		if (top != null) {
			topMostContainer = top;
		}

		// now delegate to the process service to find threads that are children of this container. 
        IMIProcesses procService = getServicesTracker().getService(IMIProcesses.class);
		procService.getProcessesBeingDebugged(
			topMostContainer == null ? control.getContext() : topMostContainer,
			new DataRequestMonitor<IDMContext[]>(getExecutor(), rm) {
				@Override
				protected void handleSuccess() {
					// return only the threads that belong to containerDmc directly (i.e. that are not in a group)  
					if (getData() instanceof IExecutionDMContext[]) {
						IExecutionDMContext[] allThreads = (IExecutionDMContext[])getData();
						IExecutionDMContext[] filteredThreads = groupManager.getExecutionContexts(containerDmc, allThreads);
						ArrayList<IExecutionDMContext> children = new ArrayList<>();
						children.addAll(Arrays.asList(childrenGroups));
						children.addAll(Arrays.asList(filteredThreads));
						rm.setData(children.toArray(new IExecutionDMContext[children.size()]));
					} else {
						rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid contexts", null)); //$NON-NLS-1$
					}
					rm.done();
				}
			});
	}
    

	@Override
	public void getExecutionData(IGroupDMContext group, DataRequestMonitor<IGroupDMData> rm) {
		if (group instanceof UserGroupDMC) {
			UserGroupDMC groupDmc = (UserGroupDMC)group;
			rm.done(new UserGroupDMData(groupDmc.getId(), groupDmc.getId()));
		} else {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid type of context", null)); //$NON-NLS-1$
		}
	}
	
    @Override
	public IDMContext getStableContext( IExecutionDMContext executionDMContext){
    	IMIExecutionDMContext thredDMContext = DMContexts.getAncestorOfType(executionDMContext, IMIExecutionDMContext.class);
    	return thredDMContext;
	}

	@Override
	public void getExecutionAndContainerContexts( final IContainerDMContext containerDmc, final DataRequestMonitor<IExecutionDMContext[]> rm){

		final IMIExecutionContextTranslator translator = 
			getServicesTracker().getService(IMIExecutionContextTranslator.class);
		if(translator == null) {
			getExecutionContexts(containerDmc, rm);
			return;
		}
		
		getExecutionContexts(containerDmc, 
			new DataRequestMonitor<IExecutionDMContext[]>(getExecutor(), rm){
                @Override
                protected void handleSuccess() {
					IExecutionDMContext[] threadChildren = getData();
					IContainerDMContext[] containerChildren = translator.getChildContainers(containerDmc);
			    	IExecutionDMContext[] children = new IExecutionDMContext[threadChildren.length + containerChildren.length];
					int i = 0;
					for( i = 0; i < threadChildren.length; ++i) 
						children[i] = threadChildren[i];
					for( i = 0; i < containerChildren.length; ++i) 
						children[threadChildren.length+i] = containerChildren[i];
					rm.setData(children);
					rm.done();
                }				
			});
	}
}

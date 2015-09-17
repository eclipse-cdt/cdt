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

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.internal.provisional.service.IExecutionContextTranslator;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
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

//	private static class ContainerLayoutDMContext extends AbstractDMContext 
//		implements IContainerLayoutDMContext {
//		
//		public ContainerLayoutDMContext(String sessionId, IDMContext[] parents) {
//			super(sessionId, parents);
//		}
//
//		@Override
//		public boolean equals(Object obj) {
//			return baseEquals(obj);
//		}
//
//		@Override
//		public int hashCode() {
//			return baseHashCode();
//		}
//	}		
//	
//	private class ContainerLayoutChangedEvent 
//		extends AbstractDMEvent<IContainerLayoutDMContext> 
//		implements IContainerLayoutChangedEvent {
//
//		public ContainerLayoutChangedEvent(IContainerLayoutDMContext context) {
//			super(context);
//		}
//	}

    /**
     * This class represents a user group.
     */
	public static class MIUserGroupDMC extends AbstractDMContext implements IGroupDMContext {

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
	
	private MIUserGroupManager fGroupManager;

	public MIExecutionContextTranslator(DsfSession session) {
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
		
		fGroupManager = createGroupManager();
		
		register(new String[] { IMIExecutionContextTranslator.class.getName(),
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
	 * Creates the MI User Group Manager to be used by this  service.
	 * Extending classes may override to provide a custom services tracker. 
	 */
	protected MIUserGroupManager createGroupManager() {
		return new MIUserGroupManager(getSession(), getServicesTracker());
	}

    @Override
	public void canGroup(IExecutionDMContext[] contexts, DataRequestMonitor<Boolean> rm) {
   		rm.done(fGroupManager.canGroup(contexts));
    }
    
    @Override
	public void canUngroup(IExecutionDMContext[] contexts, DataRequestMonitor<Boolean> rm) {
		rm.done(fGroupManager.canUngroup(contexts));
    }
    
    @Override
	public void group(IExecutionDMContext[] contexts, DataRequestMonitor<IContainerDMContext> rm) {
    	if (fGroupManager.canGroup(contexts)) {
    		IGroupDMContext groupDmc = fGroupManager.group(contexts);
    		if (groupDmc != null) {
//	    		ContainerLayoutDMContext layoutDmc = new ContainerLayoutDMContext( 
//	    			groupDmc.getSessionId(), new IDMContext[] { groupDmc });
//	    		ContainerLayoutChangedEvent event = new ContainerLayoutChangedEvent(layoutDmc); 
//	        	getSession().dispatchEvent(event, getProperties());
	        	rm.done(groupDmc);
	        	return;
    		}
    	}
    	
    	rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unable to group specified contexts", null)); //$NON-NLS-1$
    }
    
    @Override
	public void ungroup(IExecutionDMContext[] contexts, RequestMonitor rm) {
    	if (fGroupManager.canUngroup(contexts)) {
    		IContainerDMContext[] ungroupedDMCs = fGroupManager.ungroup(contexts);
    		if (ungroupedDMCs != null) {
//	    		ContainerLayoutDMContext layoutDmc = new ContainerLayoutDMContext( 
//	    			ungroupedDMCs[0].getSessionId(), new IDMContext[] { ungroupedDMCs[0] }); //TODO
//	    		ContainerLayoutChangedEvent event = new ContainerLayoutChangedEvent(layoutDmc); 
//	        	getSession().dispatchEvent(event, getProperties());
	        	rm.done();
	        	return;
    		}
    	}
    	
   		rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unable to un-group specified contexts", null)); //$NON-NLS-1$
    }

    @Override
	public void getExecutionContexts(final IContainerDMContext containerDmc, final DataRequestMonitor<IExecutionDMContext[]> rm) {
    	fGroupManager.getExecutionContexts(containerDmc, new ImmediateDataRequestMonitor<IExecutionDMContext[]>(rm) {
    		@Override
    		protected void handleSuccess() {
    			if (getData() != null) {
    				// If the group manager has found the children, just return them.
    				rm.done(getData());
    				return;
    			}

    			// If the group manager has not found any children, it means we are not dealing with
    			// a container that is part of a group hierarchy.
    			assert DMContexts.getAncestorOfType(containerDmc, IGroupDMContext.class) == null;

    			// Just fetch the list of children from the processes service
    			IMIProcesses procService = getServicesTracker().getService(IMIProcesses.class);
    			if (procService == null) {
    				rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "No process service", null)); //$NON-NLS-1$
    				return;
    			}

    			ICommandControlService control = getServicesTracker().getService(ICommandControlService.class);
    			if (control == null) {
    				rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "No CommandControl service", null)); //$NON-NLS-1$
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
    	});
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
	
	
	
	/// DO WE NEED THE BELOW ?
	//TODO
    @Override
	public IContainerDMContext createContainerPath(
		IContainerDMContext container, IMIExecutionDMContext threadDmc) {
//		return fGroupManager.createContainerPath(container, threadDmc);
			return null;
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
		if (translator == null) {
			getExecutionContexts(containerDmc, rm);
			return;
		}
		
		getExecutionContexts(containerDmc, 
			new DataRequestMonitor<IExecutionDMContext[]>(getExecutor(), rm){
                @Override
                protected void handleSuccess() {
					IExecutionDMContext[] threadChildren = getData();
					IContainerDMContext[] containerChildren = fGroupManager.getChildContainers(containerDmc);
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

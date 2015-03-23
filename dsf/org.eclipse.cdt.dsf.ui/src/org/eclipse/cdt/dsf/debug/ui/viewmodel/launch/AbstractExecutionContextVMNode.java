/*******************************************************************************
 * Copyright (c) 2010, 2015 Texas Instruments, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dobrin Alexiev (Texas Instruments) - initial API and implementation (bug 240208)
********************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.launch;

import java.util.ArrayList;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerSuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController.SteppingTimedOutEvent;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

/**
 * This class is a base class of AbstractThreadVMNode and AbstractContainerVMNode.
 * It contains common functionality between these classes.
 * 
 * The main reason this class is introduced is to allow the debug view to 
 * show multiple levels of execution containers and properly handle the delta generation.  
 * 
 * In the longer term we would like to merge the classes AbstractContainerVMNode and 
 * AbstractThreadVMNode. That will make the implementation of both classes 
 * more generic and robust in the case of recursive containers.
 *  
 * Having this class as a base for both AbstractContainerVMNode and 
 * AbstractThreadVMNode enables us to merge them in the future.
 * 
 * Originally DefaultVMModelProxyStrategy didn't accept recursive containers for 
 * generating deltas, even though they are accepted and supported by
 * AbstractDMVMProvider for viewing. 
 * The approach I took to support recursive containers for delta generation is to have 
 * the VMNodes generate their deltas  level by level, instead of one whole delta at once. 
 * That required changes in identifying which is the correct context for each of the events. 
 *
 * See: https://bugs.eclipse.org/bugs/show_bug.cgi?id=240208
 * 
 * @since 2.2
 * @experimental
 */
public abstract class AbstractExecutionContextVMNode extends AbstractDMVMNode
{
	/**
	 * List that keeps track of which events are considered leave events for 
	 * delta creation. 
	 */
    protected ArrayList<Class<?>> leafEventTypes = new ArrayList<>();
    
	/**
	 * List that keeps track of which events are considered container events for 
	 * delta creation. 
	 */
    protected ArrayList<Class<?>> containerEventTypes = new ArrayList<>();
    
    /** 
     * Constructor. 
     * Pass the parameter to the base class's constructor.
     * 
     * @param session
     * @param dmcClassType
     * 
     * @see #setChildNodes(IVMNode[])
     */
	public AbstractExecutionContextVMNode(AbstractDMVMProvider provider,
		DsfSession session, Class<? extends IDMContext> dmcClassType) {
		super(provider, session, dmcClassType);
	}

	/**
	 * Adds the events that common DSF classes relay on. 
	 */
	protected void addCommonEventTypes() {

		// non container events. 
    	addEventType(ISuspendedDMEvent.class,false);
    	addEventType(IResumedDMEvent.class, false); 
    	addEventType(FullStackRefreshEvent.class, false);
    	addEventType(SteppingTimedOutEvent.class, false);
    	addEventType(ExpandStackEvent.class, false);
		
    	// container events. 
    	addEventType(IContainerSuspendedDMEvent.class,true);
    	addEventType(IContainerResumedDMEvent.class, true);
	}

	/**
	 * When DSF debugger define custom events for which the containers and threads 
	 * nodes needs to be update they can need to register these events using this 
	 * function, so the proper recursive deltas are being created. 
	 *  
	 * @param eventClass
	 */
	protected void addEventType(Class<? extends IDMEvent<?>> eventClass, boolean containerEvent) {
		if (containerEvent) { 
			containerEventTypes.add(eventClass);
		} else { 
			leafEventTypes.add(eventClass);
		}
	}

	/**
	 * If DSF debuggers overrides the behavior of the AbstractThreadVMNode 
	 * or AbstractContainerVMNode, some event are no longer needed the derived 
	 * VMNdoe can call this method to remove some events. 
	 *  
	 * @param eventClass
	 * @param containerEvent
	 */
	protected void removeEventType(Class<?> eventClass, boolean containerEvent) {
		if (containerEvent) {
			containerEventTypes.remove(eventClass);
		} else {
			leafEventTypes.remove(eventClass);
		}
	}
	
	
	/**
	 * When we support recursive containers we want to make sure the immediate parent is returned only.
	 *
	 * @param parentDelta 
	 * @param e
	 * @param rm - request monitor
	 * @return true if the context is set by the method. 
	 */
    protected boolean getContextsForRecursiveVMNode(VMDelta parentDelta, Object e, DataRequestMonitor<IVMContext[]> rm) {
    	
    	IExecutionDMContext leafContext = null;
    	if (isExecutionContainerEvent(e)) {
    		leafContext = getLeafContextForContainerEvent(e);
    	}
    	else if (isExecutionLeafEvent(e)) {
    		leafContext = getLeafContextForLeafEvent(e);
    	}
		if (leafContext != null) {
			setImmediateParentAsContexts(leafContext, parentDelta, rm);
			return true;
    	}
		return false;
    }
    
    /**
     * Make sure we build the delta for the recursive containers one level at a time.
     * 
     * @param e - the events. 
     * @param parentDelta
     * @param nodeOffset
     * @param rm
	 * @return true if the delta is built by this method. 
     */
	protected boolean buildDeltaForRecursiveVMNode(Object e, final VMDelta parentDelta, int nodeOffset, RequestMonitor rm) {

    	IExecutionDMContext leafContext = null;
    	if (isExecutionContainerEvent(e)) {
    		leafContext = getLeafContextForContainerEvent(e);
    	}
    	else if (isExecutionLeafEvent(e)) {
    		leafContext = getLeafContextForLeafEvent(e);
    	}
		if (leafContext != null) {
			addOneLevelToDelta(leafContext, parentDelta, rm);
			return true;
    	}
		return false;
	}
    
    /**
     * When the deltas are generated one level at a time we needs to distinguish  
     * between container and regular event to return the proper context for the event.    
     * 
     * @param event
     * @return
     */
	protected IExecutionDMContext getLeafContextForContainerEvent(Object event) {
    	
    	IExecutionDMContext leafEC = null;
    	IExecutionDMContext[] triggeringContext = null;

    	if (isExecutionContainerEvent(event)) {
			if (event instanceof IContainerSuspendedDMEvent) {
				IContainerSuspendedDMEvent typedEvent = (IContainerSuspendedDMEvent)event;   
				triggeringContext = typedEvent.getTriggeringContexts();
			}
			if (event instanceof IContainerResumedDMEvent) {
				IContainerResumedDMEvent typedEvent = (IContainerResumedDMEvent)event;   
				triggeringContext = typedEvent.getTriggeringContexts();
			}
    	}
		
		if (triggeringContext != null && triggeringContext.length > 0){
			leafEC =  triggeringContext[0];
		}
		
		return leafEC;
    }

    /**
     * When the deltas are generated one level at a time we needs to distinguish  
     * between container and regular event to return the proper context for the event.    
     * 
     * @param event
     * @return
     */
	protected IExecutionDMContext getLeafContextForLeafEvent(Object event) {
    	
    	IExecutionDMContext leafEC = null;
    	
    	if (event instanceof IDMEvent<?>) { 
    		if (isExecutionLeafEvent(event)) {
	    		IDMEvent<?> typedEvent = (IDMEvent<?>)event;
	    		IDMContext dmContext = typedEvent.getDMContext();
	    		if (dmContext instanceof IExecutionDMContext) {
	    			leafEC = (IExecutionDMContext)dmContext;
	    		}
    		}
    	}
    	
		return leafEC;
    }
    
	/**
	 * Considers the parent delta when we construct the next level. 
	 * 
	 * @param leafContext
	 * @param parentDelta
	 * @param requestMonitor
	 */
    protected void addOneLevelToDelta(IExecutionDMContext leafContext, VMDelta parentDelta, RequestMonitor requestMonitor) {
    	assert leafContext != null;
		if (parentDelta.getElement() instanceof ILaunch) {
			IContainerDMContext topContainer = 
				DMContexts.getTopMostAncestorOfType(leafContext, IContainerDMContext.class);
			
			// It is possible for a thread node to be an immediate child of a launch node
			// with no container node in between.  
			if (topContainer != null)
				parentDelta.addNode(createVMContext(topContainer), 0, IModelDelta.NO_CHANGE);
		}
		else if (parentDelta.getElement() instanceof IDMVMContext) {
			IDMVMContext vmContext = (IDMVMContext)parentDelta.getElement();
			IDMContext dmContext = vmContext.getDMContext(); 
			IExecutionDMContext current = DMContexts.getParentOfType(leafContext, IContainerDMContext.class);
			while (current != null) {
				IContainerDMContext parent = DMContexts.getParentOfType(current, IContainerDMContext.class);
				if (dmContext.equals(parent)) {
					parentDelta.addNode(createVMContext(current), 0, IModelDelta.NO_CHANGE);
					break;
				}
				current = parent;
			}
		}
		requestMonitor.done();
    }

    /**
     * Based on the event (container or not) set the proper context that is the immediate 
     * parent one level at a time. 
     * 
     * @param leafContext
     * @param parentDelta
     * @param rm
     */
    protected void setImmediateParentAsContexts(IExecutionDMContext leafContext, 
    	VMDelta parentDelta, DataRequestMonitor<IVMContext[]> rm){

    	assert leafContext != null;
    	IVMContext[] all = null;
		if (parentDelta.getElement() instanceof ILaunch) {
			IContainerDMContext topContainer = 
				DMContexts.getTopMostAncestorOfType(leafContext, IContainerDMContext.class);
			if (topContainer != null) {
				all = new IVMContext[] { createVMContext(topContainer) };
			}
			else {
				// the thread is directly a child node of the launch node (no container in the middle).
				all = new IVMContext[] { createVMContext(leafContext) };
			}
		}
		else if (parentDelta.getElement() instanceof IDMVMContext) {
			IDMVMContext vmContext = (IDMVMContext)parentDelta.getElement();
			IDMContext dmContext = vmContext.getDMContext(); 
			IExecutionDMContext current = leafContext;
			while (current != null) {
				IContainerDMContext parent = DMContexts.getParentOfType(current, IContainerDMContext.class);
				if (dmContext.equals(parent)) {
					all = new IVMContext[] { createVMContext(current)};
					break;
				}
				current = parent;
			}
		}
		if (all == null) {
			all = new IVMContext[0];
		}
		rm.setData(all);
		rm.done();
	}
	
    /**
     * Returns whether the event should be considered a container event or not. 
     * 
     * @param event
     * @return
     */
    protected boolean isExecutionContainerEvent(Object event) {
    	if (event != null) {
    		for (Class<?> clazz : containerEventTypes)
    			if (clazz.isAssignableFrom(event.getClass())) {
    				return true;
    			}
    	}
	    return false;
	}

    /**
     * Returns whether the event should be use to generate delta for each of the levels.  
     * 
     * @param event
     * @return
     */
    protected boolean isExecutionLeafEvent(Object event) {
    	if (event != null) {
	    	for (Class<?> clazz : leafEventTypes) {
	    		if (clazz.isAssignableFrom(event.getClass())) {
	    			return true;
	    		}
	    	}
    	}
	    return false;
	}
}

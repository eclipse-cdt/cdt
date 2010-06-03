/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;

/**
 * Special type of the view model node, which can be used as a root node
 * for a hierarchy.  The root node of a layout hierarchy has to implement this
 * interface.
 * 
 * @since 1.0
 */
public interface IRootVMNode extends IVMNode{
    
    /**
     * Returns whether the given event should be processed for delta generation.
     * Root node is different than other nodes in that there is only one root
     * element in the view model provider hierarchy.  This method allows the root
     * node to match up the root object of the provider with the given event.  If 
     * the root node can determine that the given event does not apply to the root
     * object, it should return false so that the event is ignored.
     *  
     * @param rootObject The root object of the VM provider 
     * @param event
     * @return
     */
    public boolean isDeltaEvent(Object rootObject, Object event);

	/**
	 * The VM proxy calls this to produce the starting point for a delta, which
	 * can be a single delta node or a chain of them. It is a variant of
	 * {@link IVMNode#buildDelta(Object, ViewModelDelta, org.eclipse.cdt.dsf.concurrent.RequestMonitor)}
	 * that does not require a parent delta object since we will return the root
	 * section of the final delta's tree. The root model element in our
	 * associated viewer is not necessarily of our type (IVMNode). A VMProvider
	 * may be representing only a lower sub-tree of the content in the viewer
	 * (the other content may be coming from other VM Providers and/or sources
	 * outside DSF altogether). In that case, this method should create a chain
	 * of delta nodes that reflect the path to the VMProvider's root element,
	 * since deltas sent to the viewer must take into account the entire model.
	 * However, the specific delta node returned should be the one associated
	 * with [rootObject]--i.e., the last node in the chain. 
	 * 
	 * @param rootObject
	 *            the root model element being represented by our VMProvider
	 * @param event
	 *            event being processed
	 * @param rm
	 *            result notification, contains the delta for [rootObject], with
	 *            parents if [rootObject] is not the root node of the viewer.
	 */
	public void createRootDelta(Object rootObject, Object event, DataRequestMonitor<VMDelta> rm);
}

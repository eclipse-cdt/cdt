/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.ui.viewmodel;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

/**
 * Special type of the view model layout node, which can be used as a root node
 * for a hierarchy.  The root node of a layout hierarchy has to implement this
 * interface.
 */
@SuppressWarnings("restriction")
public interface IVMRootLayoutNode extends IVMLayoutNode{
    
    /**
     * Version of the {@link IVMLayoutNode#buildDelta(Object, ViewModelDelta, org.eclipse.dd.dsf.concurrent.RequestMonitor)}
     * method, which creates and returns the root node of the delta.  It does 
     * not require a parent object for the delta, as this is the root node. 
     * @param event Event to process.
     * @param rm Result notification, contains the root of the delta.
     */
    public void createDelta(Object event, DataRequestMonitor<IModelDelta> rm);
}

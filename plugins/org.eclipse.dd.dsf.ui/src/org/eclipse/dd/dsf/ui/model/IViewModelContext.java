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
package org.eclipse.dd.dsf.ui.model;

import org.eclipse.core.runtime.IAdaptable;

/**
 * View model element which is stored as the data object of nodes in the viewer.
 */
public interface IViewModelContext extends IAdaptable {
    
    /**
     * Returns the schema node that originated this element.
     */
    public IViewModelSchemaNode getSchemaNode();
    
    /**
     * Returns the parent of this element in the viewer layout.
     * @return
     */
    public IViewModelContext getParent();
}

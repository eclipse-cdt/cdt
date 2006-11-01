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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dd.dsf.concurrent.Immutable;

/**
 * View model element which is stored as the data object of nodes in the viewer.
 */
@Immutable
public interface IVMContext extends IAdaptable {
    
    /**
     * Returns the layout node that originated this element.
     */
    public IVMLayoutNode getLayoutNode();
    
    /**
     * Returns the parent of this element in the viewer layout.
     * @return
     */
    public IVMContext getParent();
}

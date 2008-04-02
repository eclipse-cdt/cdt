/*******************************************************************************
 * Copyright (c) 2008 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.disassembly;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;

/**
 * A context sensitive document update request.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * 
 * Use the element path instead of this interface?
 * 
 * This interface is experimental
 */
public interface IDocumentUpdate extends IViewerUpdate {

    /**
     * Returns the root element associated with this request.
     * 
     * @return the root element
     */
    public Object getRootElement();
    
    /**
     * Returns the base element associated with this request.
     * 
     * @return the base element
     */
    public Object getBaseElement();
}

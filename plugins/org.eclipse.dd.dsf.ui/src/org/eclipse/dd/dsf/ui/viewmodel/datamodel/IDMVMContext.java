/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.ui.viewmodel.datamodel;

import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;

/**
 * Interface for a view model context based on a DSF data model context.  
 */
public interface IDMVMContext extends IVMContext {
    
    public static Object REFRESH_EVENT = new Object();
    
    /**
     * returns the data model context that this view model context wraps.
     */
    public IDMContext getDMContext();
}

/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;

/**
 * A retargetable action target which allows a debugger to refresh all of its
 * active views with fresh data from the debug target.
 * 
 * @since 1.1
 */
public interface IRefreshAllTarget {
    
    /**
     * Refreshes the debugger data of the given debug context.  
     * @param debugContext The active window debug context.
     * 
     * @throws CoreException 
     */
    public void refresh(ISelection debugContext) throws CoreException;
}

/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;

/**
 * View Model extension to IVMModelProxy interface.  This extension
 * allows access to the viewer.
 * 
 * @since 1.1
 */
public interface IVMModelProxyExtension extends IVMModelProxy {

    /**
     * Returns the viewer.
     */
    public Viewer getViewer();

    /**
     * Returns the viewer input that was set to the viewer when this proxy 
     * was created.  
     */
    public Object getViewerInput();

    /**
     * Returns the full path for the root element.  If the path is empty, it
     * means that the root element is the viewer input.
     * @return
     */
    public TreePath getRootPath();
}

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
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch;

import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.DefaultVMModelProxyStrategy;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.viewers.TreePath;

/**
 * 
 */
public class LaunchVMModelProxyStrategy extends DefaultVMModelProxyStrategy {

    final private TreePath fRootPath;
    
    public LaunchVMModelProxyStrategy(AbstractVMProvider provider, Object rootElement) {
        super(provider, rootElement);
        fRootPath = new TreePath( new Object[] { rootElement });
    }
    
    @Override
    public Object getViewerInput() {
        return DebugPlugin.getDefault().getLaunchManager();
    }
    
    @Override
    public TreePath getRootPath() {
        return fRootPath;
    }
}

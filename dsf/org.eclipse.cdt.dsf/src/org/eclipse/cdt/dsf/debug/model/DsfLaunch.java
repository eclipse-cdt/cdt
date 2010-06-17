/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.model;

import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ISourceLocator;

/**
 * A Launch class to use for debuggers using the DSF.  This base class
 * ensures that changed and terminated listeners are called using a 
 * job, and thus not on a DSF services' session thread. 
 * 
 * @since 2.1
 */
public class DsfLaunch extends Launch {

    public DsfLaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator) {
        super(launchConfiguration, mode, locator);
        
        // Just set this attribute to any value.  It's presence indicates that the
        // "Show Full Path" action is supported in the debug view.
        // see org.eclipse.cdt.debug.internal.ui.actions.ShowFullPathsAction
        setAttribute(ICDebugInternalConstants.SHOW_FULL_PATHS_PREF_KEY, ""); //$NON-NLS-1$
    }
    
    @Override
    protected void fireChanged() {
        DebugPlugin.getDefault().asyncExec(new Runnable() {
            public void run() {
                DsfLaunch.super.fireChanged();
            }
        });
    }
    
    @Override
    protected void fireTerminate() {
        DebugPlugin.getDefault().asyncExec(new Runnable() {
            public void run() {
                DsfLaunch.super.fireTerminate();
            }
        });
    }
}

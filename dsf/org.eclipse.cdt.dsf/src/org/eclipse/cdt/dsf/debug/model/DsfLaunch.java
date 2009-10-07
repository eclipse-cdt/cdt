/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
    }
    
    @Override
    protected void fireChanged() {
        new Job("Dispatch DSF Launch Changed event.") { //$NON-NLS-1$
            { 
                setSystem(true);
            }
            
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                DsfLaunch.super.fireChanged();
                return Status.OK_STATUS;
            }
        };
    }
    
    @Override
    protected void fireTerminate() {
        new Job("Dispatch DSF Launch Terminate event.") { //$NON-NLS-1$
            { 
                setSystem(true);
            }
            
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                DsfLaunch.super.fireTerminate();
                return Status.OK_STATUS;
            }
        };
    }
}

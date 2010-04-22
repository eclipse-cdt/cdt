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
import org.eclipse.core.runtime.jobs.ISchedulingRule;
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

	/*
	 * Used to indicate that this launch supports the "Show Full Path" action in the debug view.
	 * This constant must have the same value as ICDebugPreferenceConstants.PREF_SHOW_FULL_PATHS
	 * We have our own copy to avoid a dependency.
	 */
	private static final String PREF_SHOW_FULL_PATHS = "org.eclipse.cdt.debug.ui.cDebug.show_full_paths"; //$NON-NLS-1$

    private class EventSchedulingRule implements ISchedulingRule {
        
        DsfLaunch fLaunch = DsfLaunch.this;
        
        public boolean isConflicting(ISchedulingRule rule) {
            if (rule instanceof EventSchedulingRule) {
                return fLaunch.equals( ((EventSchedulingRule)rule).fLaunch );
            }
            return false;
        }
        
        public boolean contains(ISchedulingRule rule) {
            if (rule instanceof EventSchedulingRule) {
                return fLaunch.equals( ((EventSchedulingRule)rule).fLaunch );
            }
            return false;
        }
    };
    
    private EventSchedulingRule fEventSchedulingRule = new EventSchedulingRule(); 
    
    public DsfLaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator) {
        super(launchConfiguration, mode, locator);
        
        // Just set this attribute to any value.  It's presence indicates that the
        // "Show Full Path" action is supported in the debug view.
        // see org.eclipse.cdt.debug.internal.ui.actions.ShowFullPathsAction
        setAttribute(PREF_SHOW_FULL_PATHS, ""); //$NON-NLS-1$
    }
    
    @Override
    protected void fireChanged() {
        new Job("Dispatch DSF Launch Changed event.") { //$NON-NLS-1$
            { 
                setSystem(true);
                setRule(fEventSchedulingRule);
            }
            
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                DsfLaunch.super.fireChanged();
                return Status.OK_STATUS;
            }
            
        }.schedule();
    }
    
    @Override
    protected void fireTerminate() {
        new Job("Dispatch DSF Launch Terminate event.") { //$NON-NLS-1$
            { 
                setSystem(true);
                setRule(fEventSchedulingRule);
            }
            
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                DsfLaunch.super.fireTerminate();
                return Status.OK_STATUS;
            }
        }.schedule();
    }
}

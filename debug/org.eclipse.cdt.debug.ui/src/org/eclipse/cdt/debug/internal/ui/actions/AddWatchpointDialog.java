/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.cdi.model.ICDIMemorySpaceManagement;
import org.eclipse.swt.widgets.Shell;

/**
 * This class was moved to the org.eclipse.cdt.debug.internal.ui.actions.breakpoints 
 * package.  This class is left here for backward compatibility for extenders that
 * reference this internal class (see Bug 374983).
 * 
 * @deprecated Replaced by opening a properties dialog on a new breakpoint.
 */
public class AddWatchpointDialog extends org.eclipse.cdt.debug.internal.ui.actions.breakpoints.AddWatchpointDialog {

    public AddWatchpointDialog( Shell parentShell, ICDIMemorySpaceManagement memMgmt ) {
        super(parentShell, memMgmt);
    }
    
}

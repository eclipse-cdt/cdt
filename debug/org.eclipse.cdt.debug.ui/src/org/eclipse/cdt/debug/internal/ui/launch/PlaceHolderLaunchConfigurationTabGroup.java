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
package org.eclipse.cdt.debug.internal.ui.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * This implementation is used where the composition of the tab group is defined
 * via the launchConfigurationTabGroups extension-point. Thus we just provide an
 * empty array of tab objects. The platform will add the ones declared in the
 * extension.
 * 
 * @since 6.0
 */
public class PlaceHolderLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {
	
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#createTabs(org.eclipse.debug.ui.ILaunchConfigurationDialog, java.lang.String)
     */
    public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
        setTabs(new ILaunchConfigurationTab[0]);
    }
}

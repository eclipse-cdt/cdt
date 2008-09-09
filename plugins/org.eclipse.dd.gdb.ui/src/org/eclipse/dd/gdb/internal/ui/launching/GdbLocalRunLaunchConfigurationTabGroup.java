/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Ericsson             - Modified for DSF
 *******************************************************************************/
package org.eclipse.dd.gdb.internal.ui.launching;

import org.eclipse.dd.gdb.internal.provisional.service.SessionType;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;

public class GdbLocalRunLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#createTabs(org.eclipse.debug.ui.ILaunchConfigurationDialog, java.lang.String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode)  {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
			new CMainTab(),
			new CArgumentsTab(),
			new CDebuggerTab(SessionType.LOCAL, false),
			new SourceLookupTab(),
//			new EnvironmentTab(),
			new CommonTab() 
		};
		setTabs(tabs);
	}
}

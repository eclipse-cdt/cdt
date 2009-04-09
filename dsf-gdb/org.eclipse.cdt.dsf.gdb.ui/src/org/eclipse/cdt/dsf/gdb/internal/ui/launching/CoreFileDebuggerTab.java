/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.ui.ICDebuggerPage;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.swt.widgets.Control;

/**
 * @since 2.0
 */
public class CoreFileDebuggerTab extends CDebuggerTab {
    public CoreFileDebuggerTab() {
        super(SessionType.CORE, false);
    }
    
	@Override
	protected void loadDynamicDebugArea() {
		// Dispose of any current child widgets in the tab holder area
		Control[] children = getDynamicTabHolder().getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].dispose();
		}

		ICDebugConfiguration debugConfig = getConfigForCurrentDebugger();
		if (debugConfig == null) {
			setDynamicTab(null);
		} else {
			ICDebuggerPage tab = new GdbCoreDebuggerPage();
			setDynamicTab(tab);
		}
		setDebugConfig(debugConfig);

		if (getDynamicTab() == null) {
			return;
		}
		// Ask the dynamic UI to create its Control
		getDynamicTab().setLaunchConfigurationDialog(getLaunchConfigurationDialog());
		getDynamicTab().createControl(getDynamicTabHolder());
		getDynamicTab().getControl().setVisible(true);
		getDynamicTabHolder().layout(true);
		contentsChanged();
	}
}

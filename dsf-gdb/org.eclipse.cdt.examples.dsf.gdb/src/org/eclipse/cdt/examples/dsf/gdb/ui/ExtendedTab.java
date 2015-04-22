/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb.ui;

import org.eclipse.cdt.launch.ui.CLaunchConfigurationTab;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class ExtendedTab extends CLaunchConfigurationTab {

	@Override
	public String getName() {
		return "Extra"; //$NON-NLS-1$
	}
	
	@Override
	public String getId() {
		return "org.eclipse.cdt.examples.dsf.gdb.extra"; //$NON-NLS-1$
	}

	@Override
	public Image getImage() {
		return DebugUITools.getImage(IInternalDebugUIConstants.IMG_OBJS_COMMON_TAB);
	}
	
	@Override
	public void createControl(Composite parent) {
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
	}
}

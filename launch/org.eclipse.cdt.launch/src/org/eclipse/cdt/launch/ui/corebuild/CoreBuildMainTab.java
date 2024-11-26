/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.launch.ui.corebuild;

import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @since 9.1
 */
public class CoreBuildMainTab extends AbstractLaunchConfigurationTab {

	private Text projectName;

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());

		Label label = new Label(comp, SWT.NONE);
		label.setText("This launch configuration was automatically created.");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		label = new Label(comp, SWT.NONE);
		label.setText("Project:");

		projectName = new Text(comp, SWT.READ_ONLY | SWT.BORDER);
		projectName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		setControl(comp);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// none
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			for (IResource resource : configuration.getMappedResources()) {
				if (resource instanceof IProject) {
					projectName.setText(resource.getName());
					break;
				}
			}
		} catch (CoreException e) {
			LaunchUIPlugin.log(e.getStatus());
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return "Main";
	}

}

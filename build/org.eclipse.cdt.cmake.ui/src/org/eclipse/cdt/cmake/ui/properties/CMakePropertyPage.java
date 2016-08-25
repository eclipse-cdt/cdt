/*******************************************************************************
 * Copyright (c) 2016 IAR Systems AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IAR Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.cmake.ui.properties;

import java.io.IOException;

import org.eclipse.cdt.cmake.ui.internal.Messages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Property page for CMake projects. The only thing we have here at the moment is a button
 * to launch the CMake GUI configurator (cmake-qt-gui).
 * 
 * We assume that the build directory is in project/build/configname, which is where
 * the CMake project wizard puts it. We also assume that "cmake-gui" is in the user's 
 * PATH.
 */
public class CMakePropertyPage extends PropertyPage {

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());

		Button b = new Button(composite, SWT.NONE);
		b.setText(Messages.CMakePropertyPage_LaunchCMakeGui);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IProject project = (IProject) getElement();
				try {
					String configName = project.getActiveBuildConfig().getName();
					String sourceDir = project.getLocation().toOSString();
					String buildDir = project.getLocation().append("build").append(configName).toOSString(); //$NON-NLS-1$
					
					Runtime.getRuntime().exec(new String[] { "cmake-gui", "-H" + sourceDir, "-B" + buildDir }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} catch (CoreException | IOException e1) {
					MessageDialog.openError(parent.getShell(), Messages.CMakePropertyPage_FailedToStartCMakeGui_Title, 
							Messages.CMakePropertyPage_FailedToStartCMakeGui_Body + e1.getMessage());
				}
			}
		});
		
		return composite;
	}
}

/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.ui.internal.properties;

import java.io.IOException;

import org.eclipse.cdt.cmake.core.internal.Activator;
import org.eclipse.cdt.cmake.core.properties.ICMakeProperties;
import org.eclipse.cdt.cmake.core.properties.ICMakePropertiesController;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Displays CMake build-platform specific project properties.
 *
 * @author Martin Weber
 */
public class OverridesPropertyPage extends PropertyPage {
	public OverridesPropertyPage() {
	}

	private ICMakePropertiesController propertiesController;
	private ICMakeProperties properties;
	private LinuxOverridesTab linuxOverridesTab;
	private WindowsOverridesTab windowsOverridesTab;

	@Override
	protected Control createContents(Composite parent) {
		final IProject project = (IProject) getElement();
		try {
			IBuildConfiguration config = project.getActiveBuildConfig();
			ICBuildConfiguration cconfig = config.getAdapter(ICBuildConfiguration.class);
			propertiesController = cconfig.getAdapter(ICMakePropertiesController.class);
			properties = propertiesController.get();
		} catch (IOException | CoreException ex) {
			ErrorDialog.openError(parent.getShell(), Messages.CMakePropertyPage_failed_to_load_properties, null,
					Activator.errorStatus(ex.getMessage(), ex));
		}

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(composite);
		{
			final Label label = new Label(composite, SWT.WRAP);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
			label.setText(Messages.OverridesPropertyPage_hlp_overrides);
		}
		TabFolder tabFolder = new TabFolder(composite, SWT.TOP);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		{
			final TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText("Linux (default)"); //$NON-NLS-1$
			linuxOverridesTab = new LinuxOverridesTab(tabFolder, SWT.NONE);
			GridDataFactory.defaultsFor(linuxOverridesTab).align(SWT.FILL, SWT.FILL).applyTo(linuxOverridesTab);
			tabItem.setControl(linuxOverridesTab);
			linuxOverridesTab.updateView(properties.getLinuxOverrides());
		}
		{
			final TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText("Windows"); //$NON-NLS-1$
			windowsOverridesTab = new WindowsOverridesTab(tabFolder, SWT.NONE);
			GridDataFactory.defaultsFor(windowsOverridesTab).align(SWT.FILL, SWT.FILL).applyTo(windowsOverridesTab);
			tabItem.setControl(windowsOverridesTab);
			windowsOverridesTab.updateView(properties.getWindowsOverrides());
		}

		return composite;
	}

	@Override
	public boolean performOk() {
		linuxOverridesTab.updateModel(properties.getLinuxOverrides());
		windowsOverridesTab.updateModel(properties.getWindowsOverrides());
		try {
			propertiesController.save(properties);
		} catch (IOException ex) {
			ErrorDialog.openError(getShell(), Messages.CMakePropertyPage_failed_to_save_properties, null,
					Activator.errorStatus(ex.getMessage(), ex));
			return false;
		}
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		properties.getLinuxOverrides().reset();
		properties.getWindowsOverrides().reset();
		linuxOverridesTab.updateView(properties.getLinuxOverrides());
		windowsOverridesTab.updateView(properties.getWindowsOverrides());
		super.performDefaults();
	}

}

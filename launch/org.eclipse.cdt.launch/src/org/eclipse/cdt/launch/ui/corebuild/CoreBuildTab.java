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

import org.eclipse.cdt.launch.internal.ui.LaunchImages;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Launch configuration tab for adjusting Core Build settings. Contents of tab depends on the nature
 * of the project which determines what build system is being used.
 *
 * @since 9.1
 */
public class CoreBuildTab extends AbstractLaunchConfigurationTab {

	private Composite container;
	private IProject activeProject;
	private ILaunchConfigurationTab activeTab;

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		container.setLayout(layout);
		setControl(container);
		defaultTab();
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		if (activeTab != null) {
			activeTab.setDefaults(configuration);
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		IProject project = getProject(configuration);
		if (project == null) {
			defaultTab();
		} else if (!project.equals(activeProject)) {
			activeProject = project;
			activeTab = getTab(project);
			if (activeTab == null) {
				defaultTab();
			} else {
				for (Control child : container.getChildren()) {
					child.dispose();
				}

				activeTab.createControl(container);
				activeTab.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			}
		}

		if (activeTab != null) {
			activeTab.setLaunchConfigurationDialog(getLaunchConfigurationDialog());
			activeTab.initializeFrom(configuration);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (activeTab != null) {
			activeTab.performApply(configuration);
		}
	}

	@Override
	public String getName() {
		return LaunchMessages.CoreBuildTab_Build;
	}

	@Override
	public Image getImage() {
		return LaunchImages.get(LaunchImages.IMG_VIEW_CORE_BUILD_TAB);
	}

	private IProject getProject(ILaunchConfiguration configuration) {
		try {
			for (IResource resource : configuration.getMappedResources()) {
				if (resource instanceof IProject) {
					return (IProject) resource;
				}
			}
		} catch (CoreException e) {
			LaunchUIPlugin.log(e.getStatus());
		}

		return null;
	}

	private void defaultTab() {
		// Clear out old contents
		for (Control child : container.getChildren()) {
			child.dispose();
		}

		Composite comp = new Composite(container, SWT.NONE);
		comp.setLayout(new GridLayout());

		Label label = new Label(comp, SWT.NONE);
		label.setText(LaunchMessages.CoreBuildTab_NoOptions);

		activeTab = null;
	}

	private ILaunchConfigurationTab getTab(IProject project) {
		try {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint point = registry.getExtensionPoint(LaunchUIPlugin.PLUGIN_ID, "coreBuildTab"); //$NON-NLS-1$
			String[] natures = project.getDescription().getNatureIds();
			for (IConfigurationElement element : point.getConfigurationElements()) {
				String nature = element.getAttribute("nature"); //$NON-NLS-1$
				if (nature != null) {
					for (String n : natures) {
						if (n.equals(nature)) {
							return (ILaunchConfigurationTab) element.createExecutableExtension("tabClass"); //$NON-NLS-1$
						}
					}
				}
			}
		} catch (CoreException e) {
			LaunchUIPlugin.log(e.getStatus());
		}

		return null;
	}

}

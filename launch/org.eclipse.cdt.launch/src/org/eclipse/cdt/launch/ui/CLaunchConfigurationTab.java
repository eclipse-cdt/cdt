/*******************************************************************************
 * Copyright (c) 2005, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ken Ryall (Nokia) - bug 178731
 *******************************************************************************/
package org.eclipse.cdt.launch.ui;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;

public abstract class CLaunchConfigurationTab extends AbstractLaunchConfigurationTab {
	/**
	 * Returns the current C element context from which to initialize default
	 * settings, or <code>null</code> if none. Note, if possible we will
	 * return the IBinary based on config entry as this may be more usefull then
	 * just the project.
	 *
	 * @return C element context.
	 */
	protected ICElement getContext(ILaunchConfiguration config, String platform) {
		String projectName = null;
		String programName = null;
		IWorkbenchPage page = LaunchUIPlugin.getActivePage();
		Object obj = null;
		try {
			projectName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
			programName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String) null);
			if (programName != null) {
				programName = VariablesPlugin.getDefault().getStringVariableManager()
						.performStringSubstitution(programName);
			}
		} catch (CoreException e) {
		}
		if (projectName != null && !projectName.isEmpty()) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
			if (cProject != null && cProject.exists()) {
				obj = cProject;
			}
		} else {
			if (page != null) {
				ISelection selection = page.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection ss = (IStructuredSelection) selection;
					if (!ss.isEmpty()) {
						obj = ss.getFirstElement();
					}
				}
			}
		}
		if (obj instanceof IResource) {
			ICElement ce = CoreModel.getDefault().create((IResource) obj);
			if (ce == null) {
				IProject pro = ((IResource) obj).getProject();
				ce = CoreModel.getDefault().create(pro);
			}
			obj = ce;
		}
		if (obj instanceof ICElement) {
			if (platform != null && !platform.equals("*")) { //$NON-NLS-1$
				ICDescriptor descriptor;
				try {
					descriptor = CCorePlugin.getDefault()
							.getCProjectDescription(((ICElement) obj).getCProject().getProject(), false);
					if (descriptor != null) {
						String projectPlatform = descriptor.getPlatform();
						if (!projectPlatform.equals(platform) && !projectPlatform.equals("*")) { //$NON-NLS-1$
							obj = null;
						}
					}
				} catch (CoreException e) {
				}
			}
			if (obj != null) {
				if (programName == null || programName.isEmpty()) {
					return (ICElement) obj;
				}
				ICElement ce = (ICElement) obj;
				IProject project;
				project = (IProject) ce.getCProject().getResource();
				IPath programFile = project.getFile(programName).getLocation();
				ce = CCorePlugin.getDefault().getCoreModel().create(programFile);
				if (ce != null && ce.exists()) {
					return ce;
				}
				return (ICElement) obj;
			}
		}
		if (page != null) {
			IEditorPart part = page.getActiveEditor();
			if (part != null) {
				IEditorInput input = part.getEditorInput();
				if (input instanceof IFileEditorInput) {
					IFile file = ((IFileEditorInput) input).getFile();
					if (file != null) {
						ICElement ce = CoreModel.getDefault().create(file);
						if (ce == null) {
							IProject pro = file.getProject();
							ce = CoreModel.getDefault().create(pro);
						}
						return ce;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Set the C project attribute based on the ICElement.
	 */
	protected void initializeCProject(ICElement cElement, ILaunchConfigurationWorkingCopy config) {
		ICProject cProject = cElement.getCProject();
		String name = null;
		if (cProject != null && cProject.exists()) {
			name = cProject.getElementName();
			config.setMappedResources(new IResource[] { cProject.getProject() });

			ICProjectDescription projDes = CCorePlugin.getDefault().getProjectDescription(cProject.getProject());
			if (projDes != null) {
				String buildConfigID = projDes.getActiveConfiguration().getId();
				config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, buildConfigID);
			}

		}
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, name);
	}

	protected String getPlatform(ILaunchConfiguration config) {
		String platform = Platform.getOS();
		try {
			return config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PLATFORM, platform);
		} catch (CoreException e) {
			return platform;
		}
	}

	/**
	 * Creates a button that allows user to insert build variables.
	 *
	 * @since 7.1
	 */
	protected Button createVariablesButton(Composite parent, String label, final Text textField) {
		Button variablesButton = createPushButton(parent, label, null);
		variablesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				handleVariablesButtonSelected(textField);
			}
		});
		return variablesButton;
	}

	/**
	 * A variable entry button has been pressed for the given text
	 * field. Prompt the user for a variable and enter the result
	 * in the given field.
	 */
	private void handleVariablesButtonSelected(Text textField) {
		String variable = getVariable();
		if (variable != null) {
			textField.insert(variable);
		}
	}

	/**
	 * Prompts the user to choose and configure a variable and returns
	 * the resulting string, suitable to be used as an attribute.
	 */
	private String getVariable() {
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
		dialog.open();
		return dialog.getVariableExpression();
	}
}

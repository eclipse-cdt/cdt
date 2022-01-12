/*******************************************************************************
 * Copyright (c) 2005, 2014 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     Anna Dushistova (MontaVista) - [366771] Converter fails to convert a CDT makefile project
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IConvertManagedBuildObject;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class ConvertTargetDialog extends Dialog {
	final private String title;
	protected org.eclipse.swt.widgets.List convertersList;
	private IProject project;
	private Map<String, IConfigurationElement> conversionElements;
	private IConfigurationElement selectedConversionElement;
	private static boolean isConversionSuccessful = false;

	public static final String PREFIX = "ProjectConvert"; //$NON-NLS-1$
	public static final String CONVERTERS_LIST = PREFIX + ".convertersList"; //$NON-NLS-1$

	/**
	 * @param parentShell the parent shell
	 * @param project the project to convert
	 * @param title the title of the dialog
	 */
	protected ConvertTargetDialog(Shell parentShell, IProject project, String title) {
		super(parentShell);
		this.title = title;
		setProject(project);

		IProjectType projectType = getProjectType();
		if (projectType != null) {
			conversionElements = ManagedBuildManager.getConversionElements(projectType);
		}
		for (IBuildObject tc : getProjectToolchains()) {
			Map<String, IConfigurationElement> converters = ManagedBuildManager.getConversionElements(tc);
			if (converters != null) {
				if (conversionElements == null) {
					conversionElements = converters;
				} else {
					conversionElements.putAll(converters);
				}
			}
		}

		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			handleConverterSelection();
			IConvertManagedBuildObject convertBuildObject = null;
			try {
				convertBuildObject = (IConvertManagedBuildObject) getSelectedConversionElement()
						.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				ManagedBuilderUIPlugin.log(e);
			}
			if (convertBuildObject != null) {
				String fromId = getSelectedConversionElement().getAttribute("fromId"); //$NON-NLS-1$
				String toId = getSelectedConversionElement().getAttribute("toId"); //$NON-NLS-1$

				IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
				if (info != null) {
					IManagedProject managedProject = info.getManagedProject();
					if (managedProject != null) {
						if (convertBuildObject.convert(managedProject, fromId, toId, true) == null) {
							setConversionSuccessful(false);
						} else {
							setConversionSuccessful(true);
						}
					} else {
						setConversionSuccessful(false);
					}
				} else {
					setConversionSuccessful(false);
				}
			} else {
				setConversionSuccessful(false);
			}
		}
		super.buttonPressed(buttonId);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null)
			shell.setText(title);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setFont(parent.getFont());
		comp.setLayout(new GridLayout(1, true));
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create the converters list group area
		final Group convertersListGroup = new Group(comp, SWT.NONE);
		convertersListGroup.setFont(parent.getFont());
		convertersListGroup.setText(Messages.ProjectConvert_convertersList);
		convertersListGroup.setLayout(new GridLayout(1, false));
		convertersListGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create the current config List
		convertersList = new org.eclipse.swt.widgets.List(convertersListGroup,
				SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		convertersList.setFont(convertersListGroup.getFont());
		GridData data = new GridData(GridData.FILL_BOTH);
		convertersList.setLayoutData(data);
		convertersList.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				convertersList = null;
			}
		});
		convertersList.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				validateState();
			}
		});
		Object[] objs = getConversionElements().keySet().toArray();
		String[] names = new String[objs.length];
		for (int i = 0; i < objs.length; i++) {
			Object object = objs[i];
			names[i] = (String) object;
		}
		convertersList.setItems(names);
		validateState();
		return comp;
	}

	private void handleConverterSelection() {
		// Determine which configuration was selected
		int selectionIndex = convertersList.getSelectionIndex();

		String selectedConverterName = convertersList.getItem(selectionIndex);

		IConfigurationElement selectedElement = getConversionElements().get(selectedConverterName);
		setSelectedConversionElement(selectedElement);
		return;
	}

	private void validateState() {
		Button b = getButton(IDialogConstants.OK_ID);
		if (b != null)
			b.setEnabled(convertersList.getSelectionIndex() != -1);
	}

	private Map<String, IConfigurationElement> getConversionElements() {
		if (conversionElements == null) {
			conversionElements = new HashMap<>();
		}
		return conversionElements;
	}

	private IProjectType getProjectType() {
		IProjectType projectType = null;

		// Get the projectType from project.
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		if (info != null) {
			IManagedProject managedProject = info.getManagedProject();
			if (managedProject != null) {
				projectType = managedProject.getProjectType();
			}
		}
		return projectType;
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public IConfigurationElement getSelectedConversionElement() {
		return selectedConversionElement;
	}

	public void setSelectedConversionElement(IConfigurationElement selectedConversionElement) {
		this.selectedConversionElement = selectedConversionElement;
	}

	public static boolean isConversionSuccessful() {
		return isConversionSuccessful;
	}

	public void setConversionSuccessful(boolean isConversionSuccessful) {
		ConvertTargetDialog.isConversionSuccessful = isConversionSuccessful;
	}

	private List<IBuildObject> getProjectToolchains() {
		List<IBuildObject> projectToolchains = new ArrayList<>();

		// Get the projectType from project.
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		if (info != null) {
			IConfiguration[] configs = info.getManagedProject().getConfigurations();
			for (IConfiguration config : configs) {
				IToolChain toolchain = config.getToolChain();
				if (toolchain != null) {
					projectToolchains.add(toolchain);
				}
			}
		}
		return projectToolchains;
	}
}

/*******************************************************************************
 * Copyright (c) 2002, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.newui.CDTHelpContextIds;
import org.eclipse.cdt.ui.wizards.NewCProjectWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * Class that implements the project type and configuration selection page in the new
 * project wizard for managed builder projects.
 *
 * @since 1.2
 */
public class CProjectPlatformPage extends WizardPage {
	/*
	 * Dialog variables and string constants
	 */
	private static final String PREFIX = "PlatformBlock"; //$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; //$NON-NLS-1$
	private static final String TIP = PREFIX + ".tip"; //$NON-NLS-1$
	private static final String CONFIG_LABEL = LABEL + ".configs"; //$NON-NLS-1$
	private static final String TARGET_LABEL = LABEL + ".platform"; //$NON-NLS-1$
	private static final String TARGET_TIP = TIP + ".platform"; //$NON-NLS-1$
	private static final String FORCEDCONFIG_TIP = TIP + ".forcedconfigs"; //$NON-NLS-1$

	// support for exporting data to custom wizard pages
	public static final String PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.platformPage"; //$NON-NLS-1$
	public static final String PROJECT_TYPE = "projectType"; //$NON-NLS-1$
	public static final String TOOLCHAIN = "toolchain"; //$NON-NLS-1$
	public static final String NATURE = "nature"; //$NON-NLS-1$

	protected Text platformSelection;
	private List<Object> selectedConfigurations;
	protected IProjectType projectType;
	protected Button showAllConfigs;
	protected boolean showAllConfigsForced;
	protected CheckboxTableViewer tableViewer;
	protected IConfiguration configurations[];

	/**
	 * Constructor.
	 * @param pageName
	 * @param wizard
	 */
	public CProjectPlatformPage(String pageName) {
		super(pageName);
		setPageComplete(false);
		projectType = ManagedBuildManager
				.getExtensionProjectType("org.eclipse.linuxtools.cdt.autotools.core.projectType"); //$NON-NLS-1$
		selectedConfigurations = new ArrayList<>(0);
		showAllConfigsForced = false;
	}

	@Override
	public boolean canFlipToNextPage() {
		return validatePage() && getNextPage() != null;
	}

	private void createConfigSelectionGroup(Composite parent) {
		// Create the group composite
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create a check box table of valid configurations
		final Label configLabel = new Label(composite, SWT.LEFT);
		configLabel.setFont(composite.getFont());
		configLabel.setText(AutotoolsWizardMessages.getResourceString(CONFIG_LABEL));

		Table table = new Table(composite,
				SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		// Add a table layout to the table
		TableLayout tableLayout = new TableLayout();
		table.setHeaderVisible(false);
		table.setLayout(tableLayout);

		// Add the viewer
		tableViewer = new CheckboxTableViewer(table);
		tableViewer.setLabelProvider(new ConfigurationLabelProvider());
		tableViewer.setContentProvider(new ConfigurationContentProvider());
		tableViewer.addSelectionChangedListener(e -> handleConfigurationSelectionChange());

	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		// Create the composite control for the tab
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Setup the help information
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, CDTHelpContextIds.MAN_PROJ_PLATFORM_HELP);

		// Create the widgets
		createTypeSelectGroup(composite);
		createConfigSelectionGroup(composite);

		// Publish which project type has been chosen with the custom wizard page manager
		MBSCustomPageManager.addPageProperty(PAGE_ID, PROJECT_TYPE, projectType.getId());

		// Select configuration
		populateConfigurations();
		setPageComplete(validatePage());

		// Do the nasty
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
	}

	private void createTypeSelectGroup(Composite parent) {
		// Create the group composite
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create the platform selection label and combo widgets
		final Label platformLabel = new Label(composite, SWT.LEFT);
		platformLabel.setFont(composite.getFont());
		platformLabel.setText(AutotoolsWizardMessages.getResourceString(TARGET_LABEL));

		platformSelection = new Text(composite, SWT.READ_ONLY);
		//		platformSelection = new Combo(composite, SWT.READ_ONLY | SWT.BORDER);
		platformSelection.setFont(composite.getFont());
		platformSelection.setToolTipText(AutotoolsWizardMessages.getResourceString(TARGET_TIP));
		platformSelection.setText("GNU Autotools"); //$NON-NLS-1$
		platformSelection.addDisposeListener(e -> platformSelection = null);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		// Make this the same as NewCProjectWizardPage.SIZING_TEXT_FIELD_WIDTH
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH + 50;
		platformSelection.setLayoutData(gd);
	}

	public IProject getProject() {
		return ((NewCProjectWizard) getWizard()).getNewProject();
	}

	/**
	 * @return
	 */
	public IConfiguration[] getSelectedConfigurations() {
		return selectedConfigurations.toArray(new IConfiguration[selectedConfigurations.size()]);
	}

	/**
	 * Returns the selected project type.
	 *
	 * @return IProjectType Selected type or <code>null</code> if an invalid selection
	 * has been made.
	 */
	public IProjectType getProjectType() {
		return projectType;
	}

	private void handleConfigurationSelectionChange() {
		// Get the selections from the table viewer
		selectedConfigurations.clear();
		selectedConfigurations.addAll(Arrays.asList(tableViewer.getCheckedElements()));

		// support for publishing the toolchains for the selected configs so that custom wizard
		// pages will know which toolchains have been selected

		// get the toolchains from the selected configs and put them into a set
		Set<IToolChain> toolchainSet = new LinkedHashSet<>();
		for (int k = 0; k < selectedConfigurations.size(); k++) {
			IConfiguration config = (IConfiguration) selectedConfigurations.get(k);
			IToolChain toolchain = config.getToolChain();
			toolchainSet.add(toolchain);
		}

		// publish the set of selected toolchains with the custom page manager
		MBSCustomPageManager.addPageProperty(PAGE_ID, TOOLCHAIN, toolchainSet);

		// TODO: Don't know where this goes and how to find true nature
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.NATURE,
				CProjectNature.C_NATURE_ID);

		setPageComplete(validatePage());
	}

	/**
	 * Populate the table viewer with either all known configurations
	 * or only with the supported configurations depending on whether a user
	 * has chosen to display unsupported configurations or not
	 * By default, only supported configurations are selected.
	 */
	private void populateConfigurations() {
		if (projectType == null)
			return;
		IConfiguration selected[] = null;

		configurations = filterSupportedConfigurations(projectType.getConfigurations());
		selected = configurations;

		// Check for buildable configs on this platform
		if (selected.length == 0) {
			// Indicate that there are no buildable configurations on this platform for this project
			// type and that all configurations will be selected
			setMessage(AutotoolsWizardMessages.getResourceString(FORCEDCONFIG_TIP), WARNING);
		} else {
			setMessage(null, NONE);
		}

		tableViewer.setInput(configurations);
		tableViewer.setCheckedElements(selected);
		handleConfigurationSelectionChange();
	}

	/**
	 * Returns the array of supported configurations found in the configurations
	 * passed to this method
	 */
	IConfiguration[] filterSupportedConfigurations(IConfiguration cfgs[]) {
		ArrayList<IConfiguration> supported = new ArrayList<>();
		String os = Platform.getOS();
		String arch = Platform.getOSArch();

		for (int i = 0; i < cfgs.length; i++) {
			// First, filter on supported state
			if (cfgs[i].isSupported()) {
				// Now, apply the OS and ARCH filters to determine if the configuration should be shown
				// Determine if the configuration's tool-chain supports this OS & Architecture.
				IToolChain tc = cfgs[i].getToolChain();
				List<String> osList = Arrays.asList(tc.getOSList());
				if (osList.contains("all") || osList.contains(os)) { //$NON-NLS-1$
					List<String> archList = Arrays.asList(tc.getArchList());
					if (archList.contains("all") || archList.contains(arch)) { //$NON-NLS-1$
						supported.add(cfgs[i]);
					}
				}
			}
		}
		return supported.toArray(new IConfiguration[supported.size()]);
	}

	/**
	 * @return
	 */
	private boolean validatePage() {
		// TODO some validation ... maybe
		if ((tableViewer.getCheckedElements()).length > 0) {
			setErrorMessage(null);
			return true;
		} else {
			setErrorMessage(AutotoolsWizardMessages.getResourceString("PlatformBlock.message.error.noconfigs")); //$NON-NLS-1$
			return false;
		}
	}
}

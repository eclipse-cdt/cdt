/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigScope;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredPathContainer;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.preferences.TabFolderLayout;
import org.eclipse.cdt.make.ui.IMakeHelpContextIds;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 *  A dialog to set scanner config discovery options.
 *
 * @deprecated as of CDT 4.0. This tab was used to set preferences/properties
 * for 3.X style projects.
 *
 * @author vhirsl
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@Deprecated
public class DiscoveryOptionsBlock extends AbstractDiscoveryOptionsBlock {
	private static final String MISSING_BUILDER_MSG = "ScannerConfigOptionsDialog.label.missingBuilderInformation"; //$NON-NLS-1$

	private static final String DIALOG_TITLE = "ScannerConfigOptionsDialog.title"; //$NON-NLS-1$
	private static final String DIALOG_DESCRIPTION = "ScannerConfigOptionsDialog.description"; //$NON-NLS-1$
	private static final String SC_GROUP_LABEL = "ScannerConfigOptionsDialog.scGroup.label"; //$NON-NLS-1$
	private static final String SC_ENABLED_BUTTON = "ScannerConfigOptionsDialog.scGroup.enabled.button"; //$NON-NLS-1$
	private static final String SC_PROBLEM_REPORTING_ENABLED_BUTTON = "ScannerConfigOptionsDialog.scGroup.problemReporting.enabled.button"; //$NON-NLS-1$
	private static final String SC_SELECTED_PROFILE_COMBO = "ScannerConfigOptionsDialog.scGroup.selectedProfile.combo"; //$NON-NLS-1$
	//    private static final String BO_PROVIDER_GROUP_LABEL = "ScannerConfigOptionsDialog.boProvider.group.label"; //$NON-NLS-1$
	private static final String SC_APPLY_PROGRESS_MESSAGE = "ScannerConfigOptionsDialog.apply.progressMessage"; //$NON-NLS-1$

	private Button scEnabledButton;
	private Button scProblemReportingEnabledButton;
	private Combo profileComboBox;
	private Composite profileComp;

	private boolean needsSCNature = false;
	private boolean fCreatePathContainer = false;
	//    private boolean isValid = true;
	//    private boolean persistedProfileChanged = false; // new persisted selected profile different than the old one

	/**
	 *
	 */
	public DiscoveryOptionsBlock() {
		super(MakeUIPlugin.getResourceString(DIALOG_TITLE));
		setDescription(MakeUIPlugin.getResourceString(DIALOG_DESCRIPTION));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#setContainer(org.eclipse.cdt.ui.dialogs.ICOptionContainer)
	 */
	@Override
	public void setContainer(ICOptionContainer container) {
		super.setContainer(container);
		if (container.getProject() == null) {
			fCreatePathContainer = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		// Create the composite control for the tab
		int tabColumns = 2;
		Font font = parent.getFont();
		Composite composite = ControlFactory.createComposite(parent, 1);
		((GridLayout) composite.getLayout()).marginHeight = 0;
		((GridLayout) composite.getLayout()).marginWidth = 0;
		((GridLayout) composite.getLayout()).verticalSpacing = 0;
		((GridData) composite.getLayoutData()).horizontalAlignment = GridData.FILL_HORIZONTAL;
		composite.setFont(font);
		setControl(composite);

		MakeUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(getControl(),
				IMakeHelpContextIds.SCANNER_CONFIG_DISCOVERY_OPTIONS);

		// create a composite for general scanner config discovery options
		Composite scComp = ControlFactory.createComposite(composite, 1);
		((GridLayout) scComp.getLayout()).marginHeight = 0;
		((GridLayout) scComp.getLayout()).marginTop = 5;
		scComp.setFont(font);

		// Create a group for scanner config discovery
		if (createScannerConfigControls(scComp, tabColumns)) {
			// create a composite for discovery profile options
			profileComp = new Composite(composite, SWT.NULL);
			//        ((GridLayout)profileComp.getLayout()).marginHeight = 5;
			//        ((GridLayout)profileComp.getLayout()).marginWidth = 5;
			//        ((GridLayout)profileComp.getLayout()).verticalSpacing = 5;
			profileComp.setFont(font);
			profileComp.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
			profileComp.setLayout(new TabFolderLayout());

			// Must set the composite parent to super class.
			setCompositeParent(profileComp);
			// fire a change event, to quick start.
			handleDiscoveryProfileChanged();
			// enable controls depending on the state of auto discovery
			enableAllControls();
		}
		parent.layout(true);
	}

	private boolean createScannerConfigControls(Composite parent, int numColumns) {
		// Check if it is an old project
		IProject project = getContainer().getProject();
		boolean showMissingBuilder = false;
		try {
			if (project != null && project.hasNature(MakeProjectNature.NATURE_ID)
					&& !project.hasNature(ScannerConfigNature.NATURE_ID)) {
				needsSCNature = true; // legacy project
			}
		} catch (CoreException e) {
			showMissingBuilder = true;
		}

		if (showMissingBuilder || (!needsSCNature && !isInitialized())) {
			ControlFactory.createEmptySpace(parent);
			ControlFactory.createLabel(parent, MakeUIPlugin.getResourceString(MISSING_BUILDER_MSG));
			return false;
		}

		Group scGroup = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(SC_GROUP_LABEL), numColumns);
		scGroup.setFont(parent.getFont());
		((GridData) scGroup.getLayoutData()).grabExcessHorizontalSpace = true;
		((GridData) scGroup.getLayoutData()).horizontalSpan = numColumns;
		((GridData) scGroup.getLayoutData()).horizontalAlignment = GridData.FILL;

		// Add main SCD checkbox
		scEnabledButton = ControlFactory.createCheckBox(scGroup, MakeUIPlugin.getResourceString(SC_ENABLED_BUTTON));
		scEnabledButton.setFont(parent.getFont());
		((GridData) scEnabledButton.getLayoutData()).horizontalSpan = numColumns;
		((GridData) scEnabledButton.getLayoutData()).grabExcessHorizontalSpace = true;
		// VMIR* old projects will have discovery disabled by default
		scEnabledButton.setSelection(needsSCNature ? false
				: (getBuildInfo().isAutoDiscoveryEnabled()
						&& !getBuildInfo().getSelectedProfileId().equals(ScannerConfigProfileManager.NULL_PROFILE_ID)));
		scEnabledButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				enableAllControls();
			}
		});
		//      handleScannerConfigEnable(); Only if true in VMIR*

		// Add problem reporting checkbox
		scProblemReportingEnabledButton = ControlFactory.createCheckBox(scGroup,
				MakeUIPlugin.getResourceString(SC_PROBLEM_REPORTING_ENABLED_BUTTON));
		scProblemReportingEnabledButton.setFont(parent.getFont());
		((GridData) scProblemReportingEnabledButton.getLayoutData()).horizontalSpan = numColumns;
		((GridData) scProblemReportingEnabledButton.getLayoutData()).grabExcessHorizontalSpace = true;
		scProblemReportingEnabledButton.setSelection(getBuildInfo().isProblemReportingEnabled());

		// Add profile combo box
		Label label = ControlFactory.createLabel(scGroup, MakeUIPlugin.getResourceString(SC_SELECTED_PROFILE_COMBO));
		((GridData) label.getLayoutData()).grabExcessHorizontalSpace = false;

		profileComboBox = new Combo(scGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		profileComboBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getBuildInfo().setSelectedProfileId(getCurrentProfileId());
				handleDiscoveryProfileChanged();
			}
		});
		// fill the combobox and set the initial value
		for (Iterator<String> items = getDiscoveryProfileIdList().iterator(); items.hasNext();) {
			String profileId = items.next();
			String pageName = getDiscoveryProfileName(profileId);
			if (pageName != null) {
				profileComboBox.add(pageName);
				if (profileId.equals(getBuildInfo().getSelectedProfileId())) {
					profileComboBox.setText(pageName);
				}
			}
		}
		profileComboBox.setEnabled(scEnabledButton.getSelection());

		return true;
	}

	/**
	 *
	 */
	private void enableAllControls() {
		boolean isSCDEnabled = scEnabledButton.getSelection();
		scProblemReportingEnabledButton.setEnabled(isSCDEnabled);
		profileComboBox.setEnabled(isSCDEnabled);
		profileComp.setVisible(isSCDEnabled);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		if (!visible) {
			if (!checkDialogForChanges()) {
				createBuildInfo();
				restoreFromBuildinfo(getBuildInfo());
				enableAllControls();
				handleDiscoveryProfileChanged();

				getCurrentPage().performDefaults();
			}
		}
		super.setVisible(visible);
		enableAllControls();
	}

	/* (non-Javadoc)
	* @see org.eclipse.cdt.make.ui.dialogs.AbstractDiscoveryOptionsBlock#getCurrentProfileId()
	*/
	@Override
	protected String getCurrentProfileId() {
		int pos = profileComboBox.getSelectionIndex();
		if (pos >= 0) {
			String selectedProfileName = profileComboBox.getItem(pos);
			return getDiscoveryProfileId(selectedProfileName);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(MakeUIPlugin.getResourceString(SC_APPLY_PROGRESS_MESSAGE), 3);

		// init buildInfo
		final IProject project = getContainer().getProject();
		// Create new build info in case of new C++ project wizard
		createBuildInfo();

		if (getBuildInfo() != null) {
			populateBuildInfo(getBuildInfo());
			monitor.worked(1);

			if (scEnabledButton.getSelection()) {
				getCurrentPage().performApply();
			}
			monitor.worked(1);

			if (project != null) {
				configureProject(project, monitor);
			}
			getBuildInfo().save();
			if (isProfileDifferentThenPersisted()) {
				if (project != null) {
					changeDiscoveryContainer(project);
				}
				updatePersistedProfile();
			}
		}
		monitor.done();
	}

	private void configureProject(IProject project, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(MakeUIPlugin.getResourceString(SC_APPLY_PROGRESS_MESSAGE), 3);

		if (needsSCNature) {
			ScannerConfigNature.addScannerConfigNature(project);
			needsSCNature = false;
			fCreatePathContainer = true;
		}
		if (fCreatePathContainer) {
			createDiscoveredPathContainer(project, monitor);
			fCreatePathContainer = false;
		}
	}

	private void createDiscoveredPathContainer(IProject project, IProgressMonitor monitor) throws CModelException {
		IPathEntry container = CoreModel.newContainerEntry(DiscoveredPathContainer.CONTAINER_ID);
		ICProject cProject = CoreModel.getDefault().create(project);
		if (cProject != null) {
			IPathEntry[] entries = cProject.getRawPathEntries();
			List<IPathEntry> newEntries = new ArrayList<>(Arrays.asList(entries));
			if (!newEntries.contains(container)) {
				newEntries.add(container);
				cProject.setRawPathEntries(newEntries.toArray(new IPathEntry[newEntries.size()]), monitor);
			}
		}
		// create a new discovered scanner config store
		MakeCorePlugin.getDefault().getDiscoveryManager().removeDiscoveredInfo(project);
	}

	private void changeDiscoveryContainer(IProject project) {
		String profileId = getBuildInfo().getSelectedProfileId();
		ScannerConfigScope profileScope = ScannerConfigProfileManager.getInstance().getSCProfileConfiguration(profileId)
				.getProfileScope();
		List<IResource> changedResources = new ArrayList<>();
		//        changedResources.add(project.getFullPath());
		changedResources.add(project);
		MakeCorePlugin.getDefault().getDiscoveryManager().changeDiscoveredContainer(project, profileScope,
				changedResources);
	}

	private void populateBuildInfo(IScannerConfigBuilderInfo2 buildInfo) {
		if (buildInfo != null) {
			buildInfo.setAutoDiscoveryEnabled(scEnabledButton.getSelection());
			String profileName = profileComboBox.getItem(profileComboBox.getSelectionIndex());
			buildInfo.setSelectedProfileId(getDiscoveryProfileId(profileName));
			buildInfo.setProblemReportingEnabled(scProblemReportingEnabledButton.getSelection());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */

	@Override
	public void performDefaults() {
		if (!isInitialized() && !needsSCNature) {
			// Missing builder info on a non-legacy project
			return;
		}
		createDefaultBuildInfo();

		restoreFromBuildinfo(getBuildInfo());
		enableAllControls();

		getCurrentPage().performDefaults();

		handleDiscoveryProfileChanged();
	}

	private void restoreFromBuildinfo(IScannerConfigBuilderInfo2 buildInfo) {
		if (buildInfo != null) {
			scEnabledButton.setSelection(buildInfo.isAutoDiscoveryEnabled()
					&& !buildInfo.getSelectedProfileId().equals(ScannerConfigProfileManager.NULL_PROFILE_ID));
			String profileId = buildInfo.getSelectedProfileId();
			profileComboBox.setText(getDiscoveryProfileName(profileId));
			scProblemReportingEnabledButton.setSelection(buildInfo.isProblemReportingEnabled());
		}
	}

}

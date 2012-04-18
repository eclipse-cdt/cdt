/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.ui.launcher;

import org.eclipse.cdt.launch.ui.CLaunchConfigurationTab;
import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.internal.launcher.ITestsLaunchConfigurationConstants;
import org.eclipse.cdt.testsrunner.internal.launcher.TestsRunnerProviderInfo;
import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerProviderInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


/**
 * A launch configuration tab that displays and edits different testing options
 * (e.g. Tests Runner provider plug-in).
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CTestingTab extends CLaunchConfigurationTab {
	
	/**
	 * Tab identifier used for ordering of tabs added using the 
	 * <code>org.eclipse.debug.ui.launchConfigurationTabs</code>
	 * extension point.
	 */
	private static final String TAB_ID = "org.eclipse.cdt.testsrunner.testingTab"; //$NON-NLS-1$

	private static final String TESTING_PROCESS_FACTORY_ID = "org.eclipse.cdt.testsrunner.TestingProcessFactory"; //$NON-NLS-1$

	/** Shows the list of available Tests Runner provider plug-ins. */
	private Combo testsRunnerProviderCombo;
	
	/** Shows the description for the currently selected Tests Runner provider plug-in. */
	private Label testsRunnerProviderDescriptionLabel;

	@Override
	public void createControl(Composite parent) {
		Composite pageComposite = new Composite(parent, SWT.NONE);
		GridLayout pageCompositeLayout = new GridLayout(2, false);
		pageCompositeLayout.horizontalSpacing = 40;
		pageComposite.setLayout(pageCompositeLayout);

		// Create a tests runner selector
		new Label(pageComposite, SWT.NONE).setText(UILauncherMessages.CTestingTab_tests_runner_label);
		testsRunnerProviderCombo = new Combo(pageComposite, SWT.READ_ONLY | SWT.DROP_DOWN);
		testsRunnerProviderCombo.add(UILauncherMessages.CTestingTab_tests_runner_is_not_set);
		testsRunnerProviderCombo.setData("0", null); //$NON-NLS-1$
		
		// Add all the tests runners
    	for (TestsRunnerProviderInfo testsRunnerProviderInfo : TestsRunnerPlugin.getDefault().getTestsRunnerProvidersManager().getTestsRunnersProviderInfo()) {
    		testsRunnerProviderCombo.setData(Integer.toString(testsRunnerProviderCombo.getItemCount()), testsRunnerProviderInfo);
    		testsRunnerProviderCombo.add(testsRunnerProviderInfo.getName());
    	}
		
		testsRunnerProviderCombo.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				testsRunnerProviderDescriptionLabel.setText(getCurrentTestsRunnerDescription());
				updateLaunchConfigurationDialog();
			}
		});
		
		// Create a tests runner description label 
		testsRunnerProviderDescriptionLabel = new Label(pageComposite, SWT.WRAP);
		GridData testsRunnerProviderLabelGD = new GridData(GridData.FILL_BOTH);
		testsRunnerProviderLabelGD.horizontalSpan = 2;
		testsRunnerProviderLabelGD.horizontalAlignment = GridData.FILL;
		testsRunnerProviderDescriptionLabel.setLayoutData(testsRunnerProviderLabelGD);
		
		GridData pageCompositeGD = new GridData(GridData.FILL_BOTH);
		pageCompositeGD.horizontalAlignment = GridData.FILL;
		pageCompositeGD.grabExcessHorizontalSpace = true;
		pageComposite.setLayoutData(pageCompositeGD);
		setControl(pageComposite);
	}
	
	/**
	 * Returns the information for the currently selected Tests Runner provider
	 * plug-in.
	 * 
	 * @return Tests Runner provide plug-in information
	 */
	private ITestsRunnerProviderInfo getCurrentTestsRunnerProviderInfo() {
		return getTestsRunnerProviderInfo(testsRunnerProviderCombo.getSelectionIndex());
	}

	/**
	 * Returns the information for the Tests Runner provide plug-in specified by
	 * index.
	 * 
	 * @param comboIndex index in combo widget
	 * @return Tests Runner provide plug-in information
	 */
	private ITestsRunnerProviderInfo getTestsRunnerProviderInfo(int comboIndex) {
		return (ITestsRunnerProviderInfo)testsRunnerProviderCombo.getData(Integer.toString(comboIndex));
	}

	/**
	 * Returns the description for the currently selected Tests Runner provide
	 * plug-in.
	 * 
	 * @return the description
	 */
	private String getCurrentTestsRunnerDescription() {
		ITestsRunnerProviderInfo testsRunnerProvider = getCurrentTestsRunnerProviderInfo();
		if (testsRunnerProvider != null) {
			return testsRunnerProvider.getDescription();
		} else {
			return UILauncherMessages.CTestingTab_no_tests_runner_label;
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {
		return getCurrentTestsRunnerProviderInfo() != null;
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(ITestsLaunchConfigurationConstants.ATTR_TESTS_RUNNER, (String) null);
		config.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, TESTING_PROCESS_FACTORY_ID);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			String testsRunnerId = configuration.getAttribute(ITestsLaunchConfigurationConstants.ATTR_TESTS_RUNNER, (String) null);
			int comboIndex = 0;
			for (int i = 1; i < testsRunnerProviderCombo.getItemCount(); i++) {
				if (getTestsRunnerProviderInfo(i).getId().equals(testsRunnerId)) {
					comboIndex = i;
					break;
				}
			}
			testsRunnerProviderCombo.select(comboIndex);
			
		} catch (CoreException e) {
			TestsRunnerPlugin.log(e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		ITestsRunnerProviderInfo testsRunnerProvider = getCurrentTestsRunnerProviderInfo();
		String testsRunnerProviderId = testsRunnerProvider != null ? testsRunnerProvider.getId() : null;
		configuration.setAttribute(ITestsLaunchConfigurationConstants.ATTR_TESTS_RUNNER, testsRunnerProviderId);
		configuration.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, TESTING_PROCESS_FACTORY_ID);
	}

	@Override
	public String getId() {
		return TAB_ID;
	}

	@Override
	public String getName() {
		return UILauncherMessages.CTestingTab_tab_name; 
	}

	@Override
	public String getErrorMessage() {
		String m = super.getErrorMessage();
		if (m == null) {
			if (getCurrentTestsRunnerProviderInfo() == null) {
				return UILauncherMessages.CTestingTab_no_tests_runner_error;
			}
		}
		return m;
	}

	@Override
	public Image getImage() {
		return TestsRunnerPlugin.createAutoImage("obj16/test_notrun.gif"); //$NON-NLS-1$
	}

}

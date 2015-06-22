/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.dialogs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationDialog;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPresentationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupExtension;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

@SuppressWarnings("restriction")
public class NewLaunchConfigEditPage extends WizardPage {
	ILaunchConfigurationWorkingCopy workingCopy;
	ILaunchConfigurationTabGroup tabGroup;
	private Text nameText;
	private CTabFolder tabFolder;
	private LaunchConfigurationDialog launchConfigurationDialog = new LaunchConfigurationDialogFake();
	private LaunchConfigurationManager launchConfigurationMgr = DebugUIPlugin.getDefault().getLaunchConfigurationManager();

	public NewLaunchConfigEditPage() {
		super(Messages.NewLaunchConfigEditPage_0);
		setTitle(Messages.NewLaunchConfigEditPage_1);
		setDescription(Messages.NewLaunchConfigEditPage_2);
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		Label label = new Label(comp, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText(Messages.NewLaunchConfigEditPage_3 + ":"); //$NON-NLS-1$
		nameText = new Text(comp, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		ColorRegistry reg = JFaceResources.getColorRegistry();
		Color c1 = reg.get("org.eclipse.ui.workbench.ACTIVE_TAB_BG_START"), //$NON-NLS-1$
		c2 = reg.get("org.eclipse.ui.workbench.ACTIVE_TAB_BG_END"); //$NON-NLS-1$
		tabFolder = new CTabFolder(comp, SWT.BORDER | SWT.NO_REDRAW_RESIZE | SWT.FLAT);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 2;
		tabFolder.setLayoutData(gridData);
		tabFolder.setSimple(false);
		tabFolder.setSelectionBackground(new Color[] { c1, c2 }, new int[] { 100 }, true);
		tabFolder.setSelectionForeground(reg.get("org.eclipse.ui.workbench.ACTIVE_TAB_TEXT_COLOR")); //$NON-NLS-1$

		setControl(comp);
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String name = nameText.getText().trim();
				workingCopy.rename(name);

				String errMessage = checkName(name);
				if (errMessage == null) {
					validateFields();
				} else {
					setErrorMessage(errMessage);
				}
			}
		});
		validateFields();
	}

	private String checkName(String name) {
		try {
			if (name.isEmpty()) {
				return Messages.NewLaunchConfigEditPage_4;
			}
			
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			if (manager.isExistingLaunchConfigurationName(name)) {
				ILaunchConfiguration config = ((LaunchManager) manager).findLaunchConfiguration(name);
				if (config != workingCopy.getOriginal()) {
					return (Messages.NewLaunchConfigEditPage_5);
				}
			}
		} catch (Exception e) {
			Activator.log(e);
			return (e.getLocalizedMessage());
		}
		return null;
	}


	void changeLaunchConfigType(ILaunchConfigurationType type) {
		if (type == null)
			return;
		try {
			String initialMode = ((NewLaunchConfigWizard) getWizard()).modePage.selectedGroup.getMode();
			workingCopy = type.newInstance(null, Messages.NewLaunchConfigEditPage_6);
			tabGroup = LaunchConfigurationPresentationManager.getDefault().getTabGroup(workingCopy, initialMode);
			for (CTabItem item : tabFolder.getItems())
				item.dispose();
			LaunchConfigurationsDialog.setCurrentlyVisibleLaunchConfigurationDialog(launchConfigurationDialog);
			tabGroup.createTabs(launchConfigurationDialog, initialMode);
			boolean firstTab = true;
			for (ILaunchConfigurationTab tab : tabGroup.getTabs()) {
				tab.setLaunchConfigurationDialog(launchConfigurationDialog);
				tab.createControl(tabFolder);
				tab.setDefaults(workingCopy);
				if (firstTab) {
					firstTab = false;
				}
			}

			// Do this after all the tabs have their controls created
			for (ILaunchConfigurationTab tab : tabGroup.getTabs()) {
				tab.initializeFrom(workingCopy);

				CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
				tabItem.setText(tab.getName());
				tabItem.setImage(!tab.isValid(workingCopy) && tab.getErrorMessage() != null ?
						launchConfigurationMgr.getErrorTabImage(tab) : tab.getImage());
				tabItem.setControl(tab.getControl());
			}

			// Clean up any created configs before we set the name and trigger
			// any validation
			((NewLaunchConfigWizard) getWizard()).cleanUpConfigs();

			tabFolder.setSelection(0);
			nameText.setText(workingCopy.getName());
		} catch (CoreException e) {
			Activator.log(e);
			return;
		}
	}

	boolean performFinish() {
		if (workingCopy == null)
			return false;
		for (ILaunchConfigurationTab tab : tabGroup.getTabs())
			tab.performApply(workingCopy);
		LaunchConfigurationsDialog.setCurrentlyVisibleLaunchConfigurationDialog(null);
		return true;
	}

	public void validateFields() {
		// page is not complete unless we finish validation successfully
		setPageComplete(false);
		if (workingCopy == null)
			return;
		String message = null;
		String old_msg = getErrorMessage();
		setErrorMessage(null);
		message = checkName(workingCopy.getName());
		if (message == null) {
			ILaunchConfigurationTab[] tabs = tabGroup.getTabs();
			int tLen = tabs.length;
			int tfLen = tabFolder.getItems().length;
			for (int i = 0; i < tLen; i++) {
				ILaunchConfigurationTab tab = tabs[i];
				try {
					tab.isValid(workingCopy);
					message = tab.getErrorMessage();
				} catch (Exception e) {
					// if createControl hasn't been called yet can throw exception..
					// like the NPE issue in CTestingTab
					message = e.getMessage();
				}
				// this is similar to what LaunchConfigurationTabGroupViewer.refresh() does, which is not available in this case
				if (tLen == tfLen &&
						(old_msg == null && message != null || old_msg != null && message == null)) {
					CTabItem item = tabFolder.getItem(i);
					if (item != null) {
						item.setImage(message != null ? launchConfigurationMgr.getErrorTabImage(tab)
								: tab.getImage());
					}
				}
				if (message != null) {
					break;
				}
			}
		}
		setErrorMessage(message);
		if (getErrorMessage() != null) {
			setPageComplete(false);
		} else {
			setPageComplete(true);
		}
	}

	private class LaunchConfigurationDialogFake extends LaunchConfigurationDialog {
		public LaunchConfigurationDialogFake() {
			super(NewLaunchConfigEditPage.this.getShell(), null, null);
		}

		@Override
		protected ILaunchConfiguration getLaunchConfiguration() {
			return workingCopy;
		}

		@Override
		public LaunchGroupExtension getLaunchGroup() {
			return NewLaunchConfigEditPage.this.getLaunchGroup();
		}

		@Override
		public String getMode() {
			return NewLaunchConfigEditPage.this.getMode();
		}

		@Override
		public void run(boolean fork, boolean cancelable,
				IRunnableWithProgress runnable)
				throws InvocationTargetException, InterruptedException {
			// ignore
		}

		@Override
		public void updateButtons() {
		}

		@Override
		public void updateMessage() {
			validateFields();
		}

		@Override
		public void setName(String name) {
			// ignore
		}

		@Override
		public String generateName(String name) {
			if (name == null)
				return ""; //$NON-NLS-1$
			return DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName(name);
		}

		@Override
		public ILaunchConfigurationTab[] getTabs() {
			return tabGroup.getTabs();
		}

		@Override
		public ILaunchConfigurationTab getActiveTab() {
			int i = tabFolder.getSelectionIndex();
			return tabGroup.getTabs()[i];
		}


		@Override
		public void setActiveTab(ILaunchConfigurationTab tab) {
			ILaunchConfigurationTab[] tabs = tabGroup.getTabs();
			int tLen = tabs.length;
			for (int i = 0; i < tLen; i++) {
				ILaunchConfigurationTab tabi = tabs[i];
				if (tabi.equals(tab)) {
					setActiveTab(i);
					break;
				}
			}
		}

		@Override
		public void setActiveTab(int index) {
			tabFolder.setSelection(index);
		}
	}

	public String getMode() {
		return ((NewLaunchConfigWizard) getWizard()).modePage.selectedGroup.getMode();
	}

	public LaunchGroupExtension getLaunchGroup() {
		try {
			if (workingCopy == null)
				return null;
			ILaunchGroup group = DebugUIPlugin.getDefault().getLaunchConfigurationManager()
					.getLaunchGroup(workingCopy.getType(), getMode());
			if (group == null) {
				return null;
			}
			LaunchGroupExtension groupExt = DebugUIPlugin.getDefault().getLaunchConfigurationManager()
					.getLaunchGroup(group.getIdentifier());
			return groupExt;
		} catch (CoreException e) {
			return null;
		}
	}
}

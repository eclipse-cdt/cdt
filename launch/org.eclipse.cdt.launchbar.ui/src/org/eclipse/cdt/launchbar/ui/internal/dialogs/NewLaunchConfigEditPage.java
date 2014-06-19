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
package org.eclipse.cdt.launchbar.ui.internal.dialogs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.launchbar.ui.internal.Activator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPresentationManager;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
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

public class NewLaunchConfigEditPage extends WizardPage {

	ILaunchConfigurationWorkingCopy workingCopy;
	ILaunchConfigurationTabGroup tabGroup;
	private Text nameText;
	private CTabFolder tabFolder;
	private LaunchConfigurationDialog launchConfigurationDialog = new LaunchConfigurationDialog();
	private LaunchConfigurationManager launchConfigurationMgr = DebugUIPlugin.getDefault().getLaunchConfigurationManager();

	public NewLaunchConfigEditPage() {
		super("NewLaunchConfigEditPage");
		setTitle("Launch Configuration Properties");
		setDescription("Edit the new launch configuration properties");
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));

		Label label = new Label(comp, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Name:");

		nameText = new Text(comp, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				workingCopy.rename(nameText.getText());
				checkName();
			}
		});

		ColorRegistry reg = JFaceResources.getColorRegistry();
		Color c1 = reg.get("org.eclipse.ui.workbench.ACTIVE_TAB_BG_START"), //$NON-NLS-1$
			  c2 = reg.get("org.eclipse.ui.workbench.ACTIVE_TAB_BG_END"); //$NON-NLS-1$
		tabFolder = new CTabFolder(comp, SWT.BORDER | SWT.NO_REDRAW_RESIZE | SWT.FLAT);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 2;
		tabFolder.setLayoutData(gridData);
		tabFolder.setSimple(false);
		tabFolder.setSelectionBackground(new Color[] {c1, c2},	new int[] {100}, true);
		tabFolder.setSelectionForeground(reg.get("org.eclipse.ui.workbench.ACTIVE_TAB_TEXT_COLOR")); //$NON-NLS-1$

		checkName();
		changeLaunchConfigType();

		setControl(comp);
	}

	private void checkName() {
		if (workingCopy == null)
			return;

		try {
			if (workingCopy.getName().isEmpty()) {
				setErrorMessage("Name can not be empty");
				setPageComplete(false);
			} else if (DebugPlugin.getDefault().getLaunchManager().isExistingLaunchConfigurationName(workingCopy.getName())) {
				setErrorMessage("A configuration with this name already exists");
				setPageComplete(false);
			} else {
				setErrorMessage(null);
				setPageComplete(true);
			}
		} catch (CoreException exc) {
			setErrorMessage(exc.getLocalizedMessage());
		}
	}

	void changeLaunchConfigType() {
		ILaunchConfigurationType type = ((NewLaunchConfigWizard)getWizard()).typePage.type;
		if (type == null)
			return;

		try {
			String initialMode = ((NewLaunchConfigWizard)getWizard()).modePage.selectedGroup.getMode();
			workingCopy = type.newInstance(null, "New Configuration");
			tabGroup = LaunchConfigurationPresentationManager.getDefault().getTabGroup(workingCopy, initialMode);
			nameText.setText(workingCopy.getName());

			for (CTabItem item : tabFolder.getItems())
				item.dispose();

			tabGroup.createTabs(launchConfigurationDialog, initialMode);
			boolean firstTab = true;
			for (ILaunchConfigurationTab tab : tabGroup.getTabs()) {
				tab.setLaunchConfigurationDialog(launchConfigurationDialog);
				tab.createControl(tabFolder);
				tab.setDefaults(workingCopy);
				if (firstTab) {
					firstTab = false;
					// tab.setDefaults likely renames it
					nameText.setText(workingCopy.getName());
				}
				tab.initializeFrom(workingCopy);

				CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
				tabItem.setText(tab.getName());
				tabItem.setImage(!tab.isValid(workingCopy) && tab.getErrorMessage() != null ?
						launchConfigurationMgr.getErrorTabImage(tab) : tab.getImage());
				tabItem.setControl(tab.getControl());
			}

			tabFolder.setSelection(0);
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

		return true;
	}

	private class LaunchConfigurationDialog implements ILaunchConfigurationDialog {

		@Override
		public void run(boolean fork, boolean cancelable,
				IRunnableWithProgress runnable)
				throws InvocationTargetException, InterruptedException {
			// TODO Auto-generated method stub
		}

		@Override
		public void updateButtons() {
		}

		@Override
		public void updateMessage() {
			String message = null;
			String old_msg = getErrorMessage();
			ILaunchConfigurationTab[] tabs = tabGroup.getTabs();
			ILaunchConfigurationTab tab;
			CTabItem item;
			int tLen = tabs.length;
			int tfLen = tabFolder.getItems().length;
			for (int i = 0; i < tLen; i++) {
				tab = tabs[i];
				try {
					tab.isValid(workingCopy);
					message = tab.getErrorMessage();
				} catch(Exception e) {
					// if createControl hasn't been called yet can throw exception..
					// like the NPE issue in CTestingTab
					message = e.getMessage();
				}
				// this is similar to what LaunchConfigurationTabGroupViewer.refresh() does, which is not available in this case
				if (tLen == tfLen &&
						(old_msg == null && message != null || old_msg != null && message == null)) {
					item = tabFolder.getItem(i);
					if (item != null) {
						item.setImage(message != null ? launchConfigurationMgr.getErrorTabImage(tab)
								: tab.getImage());
					}
				}

				if (message != null) {
					break;
				}
			}
			setErrorMessage(message);
		}

		@Override
		public void setName(String name) {
			// TODO Auto-generated method stub

		}

		@Override
		public String generateName(String name) {
			if (name == null)
				return "";
			return DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName(name);
		}

		@Override
		public ILaunchConfigurationTab[] getTabs() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ILaunchConfigurationTab getActiveTab() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getMode() {
			return ((NewLaunchConfigWizard)getWizard()).modePage.selectedGroup.getMode();
		}

		@Override
		public void setActiveTab(ILaunchConfigurationTab tab) {
			// TODO Auto-generated method stub
		}

		@Override
		public void setActiveTab(int index) {
			// TODO Auto-generated method stub
		}
	}
}

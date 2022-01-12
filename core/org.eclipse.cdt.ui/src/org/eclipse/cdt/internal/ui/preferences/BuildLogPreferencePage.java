/*******************************************************************************
 * Copyright (c) 2010, 2015 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.cdt.internal.ui.buildconsole.BuildConsoleManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Preference page defining build log properties.
 */
public class BuildLogPreferencePage extends PropertyPage implements ICOptionContainer {
	private boolean isProjectLevel;
	private Button enableLoggingCheckbox;
	private Button browseButton;
	private Text logLocationText;
	private Label logLocationLabel;

	@Override
	protected Control createContents(Composite parent) {
		IProject project = getProject();
		isProjectLevel = project != null;
		if (isProjectLevel) {
			BuildConsoleManager consoleManager = getConsoleManager();
			Preferences prefs = consoleManager.getBuildLogPreferences(project);

			Composite contents = ControlFactory.createCompositeEx(parent, 3, GridData.FILL_BOTH);
			((GridLayout) contents.getLayout()).makeColumnsEqualWidth = false;

			ControlFactory.createEmptySpace(contents, 3);

			// [v] Enable Logging
			enableLoggingCheckbox = ControlFactory.createCheckBox(contents,
					PreferencesMessages.BuildLogPreferencePage_EnableLogging);
			((GridData) enableLoggingCheckbox.getLayoutData()).horizontalSpan = 2;
			boolean keepLog = prefs.getBoolean(BuildConsoleManager.KEY_KEEP_LOG,
					BuildConsoleManager.CONSOLE_KEEP_LOG_DEFAULT);
			enableLoggingCheckbox.setSelection(keepLog);
			enableLoggingCheckbox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateEnablements();
				}
			});

			ControlFactory.createEmptySpace(contents, 3);

			// Log file location: [....................]
			logLocationLabel = ControlFactory.createLabel(contents,
					PreferencesMessages.BuildLogPreferencePage_LogLocation);
			((GridData) logLocationLabel.getLayoutData()).grabExcessHorizontalSpace = false;

			logLocationText = ControlFactory.createTextField(contents, SWT.SINGLE | SWT.BORDER);
			String logLocation = prefs.get(BuildConsoleManager.KEY_LOG_LOCATION,
					consoleManager.getDefaultConsoleLogLocation(project));
			logLocationText.setText(logLocation);
			logLocationText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
				}
			});
			((GridData) logLocationText.getLayoutData()).widthHint = new PixelConverter(parent)
					.convertWidthInCharsToPixels(40);

			// [Browse...]
			browseButton = ControlFactory.createPushButton(contents, PreferencesMessages.BuildLogPreferencePage_Browse);
			((GridData) browseButton.getLayoutData()).horizontalAlignment = GridData.END;
			browseButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
					dialog.setText(PreferencesMessages.BuildLogPreferencePage_ChooseLogFile);
					String fileName = logLocationText.getText();
					IPath logFolder = new Path(fileName).removeLastSegments(1);
					dialog.setFilterPath(logFolder.toOSString());
					String chosenFile = dialog.open();
					if (chosenFile != null) {
						logLocationText.setText(chosenFile);
					}
				}

			});

			updateEnablements();
		}
		return parent;
	}

	@Override
	protected void performDefaults() {
		if (isProjectLevel) {
			IProject project = getProject();
			BuildConsoleManager consoleManager = getConsoleManager();
			Preferences prefs = consoleManager.getBuildLogPreferences(project);
			prefs.put(BuildConsoleManager.KEY_LOG_LOCATION, consoleManager.getDefaultConsoleLogLocation(project));
			prefs.putBoolean(BuildConsoleManager.KEY_KEEP_LOG, BuildConsoleManager.CONSOLE_KEEP_LOG_DEFAULT);
			try {
				prefs.flush();
			} catch (BackingStoreException e) {
				CUIPlugin.log(e);
			}
			logLocationText.setText(prefs.get(BuildConsoleManager.KEY_LOG_LOCATION,
					consoleManager.getDefaultConsoleLogLocation(project)));
			enableLoggingCheckbox.setSelection(
					prefs.getBoolean(BuildConsoleManager.KEY_KEEP_LOG, BuildConsoleManager.CONSOLE_KEEP_LOG_DEFAULT));
			updateEnablements();
		}
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		if (isProjectLevel) {
			BuildConsoleManager consoleManager = getConsoleManager();
			Preferences prefs = consoleManager.getBuildLogPreferences(getProject());
			prefs.put(BuildConsoleManager.KEY_LOG_LOCATION, logLocationText.getText());
			prefs.putBoolean(BuildConsoleManager.KEY_KEEP_LOG, enableLoggingCheckbox.getSelection());
			try {
				prefs.flush();
			} catch (BackingStoreException e) {
				CUIPlugin.log(e);
			}
		}
		return true;
	}

	@Override
	public IProject getProject() {
		IProject project = null;
		IAdaptable elem = getElement();
		if (elem instanceof IProject) {
			project = (IProject) elem;
		} else if (elem != null) {
			project = elem.getAdapter(IProject.class);
		}
		return project;
	}

	private BuildConsoleManager getConsoleManager() {
		return (BuildConsoleManager) CUIPlugin.getDefault().getConsoleManager();
	}

	@Override
	@Deprecated
	public org.eclipse.core.runtime.Preferences getPreferences() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateContainer() {
	}

	private void updateEnablements() {
		boolean isLoggingEnabled = enableLoggingCheckbox.getSelection();
		logLocationLabel.setEnabled(isLoggingEnabled);
		logLocationText.setEnabled(isLoggingEnabled);
		browseButton.setEnabled(isLoggingEnabled);
	}
}

/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.cdt.ui.dialogs;

import java.io.File;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MachOBinaryParserPage extends AbstractCOptionPage {

	public final static String PREF_CPPFILT_PATH = CUIPlugin.PLUGIN_ID + ".cppfilt"; //$NON-NLS-1$

	protected Text fCPPFiltCommandText;

	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		String cppfilt = fCPPFiltCommandText.getText().trim();

		monitor.beginTask(CUIMessages.BinaryParserPage_task_savingAttributes, 1);
		IProject proj = getContainer().getProject();
		if (proj != null) {
			String parserID = ""; //$NON-NLS-1$
			ICConfigExtensionReference[] cext = null;
			ICProjectDescription desc = CCorePlugin.getDefault().getProjectDescription(proj, true);
			if (desc != null) {
				ICConfigurationDescription cfgDesc = desc.getDefaultSettingConfiguration();
				if (cfgDesc != null) {
					cext = cfgDesc.get(CCorePlugin.BINARY_PARSER_UNIQ_ID);
				}
			}
			if (cext != null && cext.length > 0) {
				IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CUIPlugin.PLUGIN_ID,
						"BinaryParserPage"); //$NON-NLS-1$
				IConfigurationElement[] infos = point.getConfigurationElements();
				for (int i = 0; i < infos.length; i++) {
					String id = infos[i].getAttribute("parserID"); //$NON-NLS-1$
					String clazz = infos[i].getAttribute("class"); //$NON-NLS-1$
					String ego = getRealBinaryParserPage().getClass().getName();
					if (clazz != null && clazz.equals(ego)) {
						parserID = id;
						break;
					}
				}
				for (int i = 0; i < cext.length; i++) {
					if (cext[i].getID().equals(parserID)) {

						String orig = cext[i].getExtensionData("c++filt"); //$NON-NLS-1$
						if (orig == null || !orig.equals(cppfilt)) {
							cext[i].setExtensionData("c++filt", cppfilt); //$NON-NLS-1$
						}
					}
				}
				CCorePlugin.getDefault().setProjectDescription(proj, desc);
			}
		} else {
			Preferences store = getContainer().getPreferences();
			if (store != null) {
				store.setValue(PREF_CPPFILT_PATH, cppfilt);
			}
		}
	}

	/**
	 * If this class is inherited from then this method MUST be implemented
	 * in the derived class.
	 */
	protected Object getRealBinaryParserPage() {
		return this;
	}

	@Override
	public void performDefaults() {
		String cppfilt = null;
		IProject proj = getContainer().getProject();
		Preferences store = getContainer().getPreferences();
		if (store != null) {
			if (proj != null) {
				cppfilt = store.getString(PREF_CPPFILT_PATH);
			} else {
				cppfilt = store.getDefaultString(PREF_CPPFILT_PATH);
			}
			fCPPFiltCommandText.setText((cppfilt == null || cppfilt.length() == 0) ? "c++filt" : cppfilt); //$NON-NLS-1$
		}
	}

	@Override
	public void createControl(Composite parent) {
		Group comp = new Group(parent, SWT.SHADOW_ETCHED_IN);
		comp.setText(CUIMessages.BinaryParserBlock_binaryParserOptions);
		comp.setLayout(new GridLayout(2, true));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		((GridLayout) comp.getLayout()).makeColumnsEqualWidth = false;

		Label label = ControlFactory.createLabel(comp, CUIMessages.BinaryParserPage_label_cppfiltCommand);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		fCPPFiltCommandText = ControlFactory.createTextField(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fCPPFiltCommandText.setLayoutData(gd);
		fCPPFiltCommandText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent evt) {
				//updateLaunchConfigurationDialog();
			}
		});
		Button button = ControlFactory.createPushButton(comp, CUIMessages.BinaryParserPage_label_browse);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleCPPFiltButtonSelected();
				//updateLaunchConfigurationDialog();
			}

			private void handleCPPFiltButtonSelected() {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText(CUIMessages.BinaryParserPage_label_cppfiltCommand);
				String command = fCPPFiltCommandText.getText().trim();
				int lastSeparatorIndex = command.lastIndexOf(File.separator);
				if (lastSeparatorIndex != -1) {
					dialog.setFilterPath(command.substring(0, lastSeparatorIndex));
				}
				String res = dialog.open();
				if (res == null) {
					return;
				}
				fCPPFiltCommandText.setText(res);
			}
		});

		setControl(comp);
		initialziedValues();
	}

	private void initialziedValues() {
		String cppfilt = null;
		IProject proj = getContainer().getProject();
		if (proj != null) {
			try {
				ICConfigExtensionReference[] cext = CCorePlugin.getDefault().getDefaultBinaryParserExtensions(proj);
				if (cext.length > 0) {
					cppfilt = cext[0].getExtensionData("c++filt"); //$NON-NLS-1$
				}
			} catch (CoreException e) {
			}
		} else {
			Preferences store = getContainer().getPreferences();
			if (store != null) {
				cppfilt = store.getString(PREF_CPPFILT_PATH);
			}
		}
		fCPPFiltCommandText.setText((cppfilt == null || cppfilt.length() == 0) ? "c++filt" : cppfilt); //$NON-NLS-1$
	}

}

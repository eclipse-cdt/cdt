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
 * Abstract base binary parser page for GNU binutils based binary parsers
 */
public abstract class AbstractGNUBinaryParserPage extends AbstractCOptionPage {

	public final static String PREF_ADDR2LINE_PATH = CUIPlugin.PLUGIN_ID + ".addr2line"; //$NON-NLS-1$
	public final static String PREF_CPPFILT_PATH = CUIPlugin.PLUGIN_ID + ".cppfilt"; //$NON-NLS-1$

	protected Text fAddr2LineCommandText;
	protected Text fCPPFiltCommandText;
	private String parserID = null;

	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		String addr2line = fAddr2LineCommandText.getText().trim();
		String cppfilt = fCPPFiltCommandText.getText().trim();

		monitor.beginTask(CUIMessages.BinaryParserPage_task_savingAttributes, 1);
		IProject proj = getContainer().getProject();
		if (proj != null) {
			ICConfigExtensionReference[] cext = null;
			ICProjectDescription desc = CCorePlugin.getDefault().getProjectDescription(proj, true);
			if (desc != null) {
				ICConfigurationDescription cfgDesc = desc.getDefaultSettingConfiguration();
				if (cfgDesc != null) {
					cext = cfgDesc.get(CCorePlugin.BINARY_PARSER_UNIQ_ID);
				}
			}
			if (cext != null && cext.length > 0) {
				initializeParserId();
				for (int i = 0; i < cext.length; i++) {
					if (cext[i].getID().equals(parserID)) {
						String orig = cext[i].getExtensionData("addr2line"); //$NON-NLS-1$
						if (orig == null || !orig.equals(addr2line)) {
							cext[i].setExtensionData("addr2line", addr2line); //$NON-NLS-1$
						}
						orig = cext[i].getExtensionData("c++filt"); //$NON-NLS-1$
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
				store.setValue(PREF_ADDR2LINE_PATH, addr2line);
				store.setValue(PREF_CPPFILT_PATH, cppfilt);
			}
		}
	}

	private void initializeParserId() {
		if (parserID == null) {
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CUIPlugin.PLUGIN_ID,
					"BinaryParserPage"); //$NON-NLS-1$
			IConfigurationElement[] infos = point.getConfigurationElements();
			for (int i = 0; i < infos.length; i++) {
				String id = infos[i].getAttribute("parserID"); //$NON-NLS-1$
				String clazz = infos[i].getAttribute("class"); //$NON-NLS-1$
				String ego = getRealBinaryParserPage().getClass().getName();
				if (clazz != null && clazz.equals(ego)) {
					parserID = id;
					return;
				}
			}
			parserID = ""; //$NON-NLS-1$
		}
	}

	/**
	 * If this class is inherited from then this method MUST be implemented
	 * in the derived class.
	 */
	abstract protected AbstractGNUBinaryParserPage getRealBinaryParserPage();

	@Override
	public void performDefaults() {
		String addr2line = null;
		String cppfilt = null;
		IProject proj = getContainer().getProject();
		Preferences store = getContainer().getPreferences();
		if (store != null) {
			if (proj != null) {
				addr2line = store.getString(PREF_ADDR2LINE_PATH);
				cppfilt = store.getString(PREF_CPPFILT_PATH);
			} else {
				addr2line = store.getDefaultString(PREF_ADDR2LINE_PATH);
				cppfilt = store.getDefaultString(PREF_CPPFILT_PATH);
			}
			fAddr2LineCommandText.setText((addr2line == null || addr2line.length() == 0) ? "addr2line" : addr2line); //$NON-NLS-1$
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

		Label label = ControlFactory.createLabel(comp, CUIMessages.BinaryParserPage_label_addr2lineCommand);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		fAddr2LineCommandText = ControlFactory.createTextField(comp, SWT.SINGLE | SWT.BORDER);
		fAddr2LineCommandText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent evt) {
				//updateLaunchConfigurationDialog();
			}
		});

		Button button = ControlFactory.createPushButton(comp, CUIMessages.BinaryParserPage_label_browse);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleAddr2LineButtonSelected();
				//updateLaunchConfigurationDialog();
			}

			private void handleAddr2LineButtonSelected() {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText(CUIMessages.BinaryParserPage_label_addr2lineCommand);
				String command = fAddr2LineCommandText.getText().trim();
				int lastSeparatorIndex = command.lastIndexOf(File.separator);
				if (lastSeparatorIndex != -1) {
					dialog.setFilterPath(command.substring(0, lastSeparatorIndex));
				}
				String res = dialog.open();
				if (res == null) {
					return;
				}
				fAddr2LineCommandText.setText(res);
			}
		});

		label = ControlFactory.createLabel(comp, CUIMessages.BinaryParserPage_label_cppfiltCommand);
		gd = new GridData();
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
		button = ControlFactory.createPushButton(comp, CUIMessages.BinaryParserPage_label_browse1);
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
		String addr2line = null;
		String cppfilt = null;
		IProject proj = getContainer().getProject();
		if (proj != null) {
			try {
				ICConfigExtensionReference[] cext = CCorePlugin.getDefault().getDefaultBinaryParserExtensions(proj);
				if (cext.length > 0) {
					initializeParserId();
					for (int i = 0; i < cext.length; i++) {
						if (cext[i].getID().equals(parserID)) {
							addr2line = cext[0].getExtensionData("addr2line"); //$NON-NLS-1$
							cppfilt = cext[0].getExtensionData("c++filt"); //$NON-NLS-1$
							break;
						}
					}
				}
			} catch (CoreException e) {
			}
		} else {
			Preferences store = getContainer().getPreferences();
			if (store != null) {
				addr2line = store.getString(PREF_ADDR2LINE_PATH);
				cppfilt = store.getString(PREF_CPPFILT_PATH);
			}
		}
		fAddr2LineCommandText.setText((addr2line == null || addr2line.length() == 0) ? "addr2line" : addr2line); //$NON-NLS-1$
		fCPPFiltCommandText.setText((cppfilt == null || cppfilt.length() == 0) ? "c++filt" : cppfilt); //$NON-NLS-1$
	}

}

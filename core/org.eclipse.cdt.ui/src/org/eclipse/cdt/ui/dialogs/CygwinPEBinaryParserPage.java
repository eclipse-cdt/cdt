/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.dialogs;

import java.io.File;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
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
 */
public class CygwinPEBinaryParserPage extends AbstractCOptionPage {

	public final static String PREF_ADDR2LINE_PATH = CUIPlugin.PLUGIN_ID + ".addr2line"; //$NON-NLS-1$
	public final static String PREF_CPPFILT_PATH = CUIPlugin.PLUGIN_ID + ".cppfilt"; //$NON-NLS-1$
	public final static String PREF_CYGPATH_PATH = CUIPlugin.PLUGIN_ID + ".cygpath"; //$NON-NLS-1$
	public final static String PREF_NM_PATH = CUIPlugin.PLUGIN_ID + ".nm"; //$NON-NLS-1$

	protected Text fAddr2LineCommandText;
	protected Text fCPPFiltCommandText;
	protected Text fCygPathCommandText;
	protected Text fNMCommandText;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		String addr2line = fAddr2LineCommandText.getText().trim();
		String cppfilt = fCPPFiltCommandText.getText().trim();
		String cygpath = fCygPathCommandText.getText().trim();
		String nm = fNMCommandText.getText().trim();

		monitor.beginTask(CUIMessages.getString("BinaryParserPage.task.savingAttributes"), 1); //$NON-NLS-1$
		IProject proj = getContainer().getProject();
		if (proj != null) {
			String parserID = ""; //$NON-NLS-1$
			ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(proj, false);
			ICExtensionReference[] cext = cdesc.get(CCorePlugin.BINARY_PARSER_UNIQ_ID);
			if (cext.length > 0) {
				IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CUIPlugin.PLUGIN_ID, "BinaryParserPage"); //$NON-NLS-1$
				IConfigurationElement[] infos = point.getConfigurationElements();
				for (int i = 0; i < infos.length; i++) {
					String id = infos[i].getAttribute("parserID"); //$NON-NLS-1$
					String clazz = infos[i].getAttribute("class"); //$NON-NLS-1$
					String ego = getClass().getName();
					if (clazz != null && clazz.equals(ego)) {
						parserID = id;
						break;
					}
				}
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
						orig = cext[i].getExtensionData("cygpath"); //$NON-NLS-1$
						if (orig == null || !orig.equals(cygpath)) {
							cext[i].setExtensionData("cygpath", cygpath); //$NON-NLS-1$
						}
						orig = cext[i].getExtensionData("nm"); //$NON-NLS-1$
						if (orig == null || !orig.equals(nm)) {
							cext[i].setExtensionData("nm", nm); //$NON-NLS-1$
						}
					}
				}
			}
		} else {
			Preferences store = getContainer().getPreferences();
			if (store != null) {
				store.setValue(PREF_ADDR2LINE_PATH, addr2line);
				store.setValue(PREF_CPPFILT_PATH, cppfilt);
				store.setValue(PREF_CYGPATH_PATH, cygpath);
				store.setValue(PREF_NM_PATH, nm);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	public void performDefaults() {
		String addr2line = null;
		String cppfilt = null;
		String cygpath = null;
		String nm = null;
		IProject proj = getContainer().getProject();
		Preferences store = getContainer().getPreferences();
		if (store != null) {
			if (proj != null) {
				addr2line = store.getString(PREF_ADDR2LINE_PATH);
				cppfilt = store.getString(PREF_CPPFILT_PATH);
				cygpath = store.getString(PREF_CYGPATH_PATH);
				nm = store.getString(PREF_NM_PATH);
			} else {
				addr2line = store.getDefaultString(PREF_ADDR2LINE_PATH);
				cppfilt = store.getDefaultString(PREF_CPPFILT_PATH);
				cygpath = store.getDefaultString(PREF_CYGPATH_PATH);
				nm = store.getDefaultString(PREF_NM_PATH);
			}
			fAddr2LineCommandText.setText((addr2line == null || addr2line.length() == 0) ? "addr2line" : addr2line); //$NON-NLS-1$;
			fCPPFiltCommandText.setText((cppfilt == null || cppfilt.length() == 0) ? "c++filt" : cppfilt); //$NON-NLS-1$;
			fCygPathCommandText.setText((cygpath == null || cygpath.length() == 0) ? "cygpath" : cygpath); //$NON-NLS-1$;
			fNMCommandText.setText((nm == null || nm.length() == 0) ? "nm" : nm); //$NON-NLS-1$;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite composite) {
		Group comp = new Group(composite, SWT.SHADOW_ETCHED_IN);
		comp.setText(CUIMessages.getString("BinaryParserBlock.binaryParserOptions")); //$NON-NLS-1$
		
		comp.setLayout(new GridLayout(2, true));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		((GridLayout) comp.getLayout()).makeColumnsEqualWidth = false;

		Label label = ControlFactory.createLabel(comp, CUIMessages.getString("BinaryParserPage.label.addr2lineCommand")); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		fAddr2LineCommandText = ControlFactory.createTextField(comp, SWT.SINGLE | SWT.BORDER);
		fAddr2LineCommandText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				//updateLaunchConfigurationDialog();
			}
		});

		Button button = ControlFactory.createPushButton(comp, CUIMessages.getString("BinaryParserPage.label.browse")); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent evt) {
				handleAddr2LineButtonSelected();
				//updateLaunchConfigurationDialog();
			}

			private void handleAddr2LineButtonSelected() {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText(CUIMessages.getString("BinaryParserPage.label.addr2lineCommand")); //$NON-NLS-1$
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

		label = ControlFactory.createLabel(comp, CUIMessages.getString("BinaryParserPage.label.cppfiltCommand")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		fCPPFiltCommandText = ControlFactory.createTextField(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fCPPFiltCommandText.setLayoutData(gd);
		fCPPFiltCommandText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				//updateLaunchConfigurationDialog();
			}
		});
		button = ControlFactory.createPushButton(comp, CUIMessages.getString("BinaryParserPage.label.browse1")); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent evt) {
				handleCPPFiltButtonSelected();
				//updateLaunchConfigurationDialog();
			}

			private void handleCPPFiltButtonSelected() {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText(CUIMessages.getString("BinaryParserPage.label.cppfiltCommand")); //$NON-NLS-1$
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

		label = ControlFactory.createLabel(comp, CUIMessages.getString("BinaryParserPage.label.cygpathCommand")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		fCygPathCommandText = ControlFactory.createTextField(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fCygPathCommandText.setLayoutData(gd);
		fCygPathCommandText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				//updateLaunchConfigurationDialog();
			}
		});
		button = ControlFactory.createPushButton(comp, CUIMessages.getString("BinaryParserPage.label.browse2")); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent evt) {
				handleCygPathButtonSelected();
				//updateLaunchConfigurationDialog();
			}

			private void handleCygPathButtonSelected() {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText(CUIMessages.getString("BinaryParserPage.label.cygpathCommand")); //$NON-NLS-1$
				String command = fCygPathCommandText.getText().trim();
				int lastSeparatorIndex = command.lastIndexOf(File.separator);
				if (lastSeparatorIndex != -1) {
					dialog.setFilterPath(command.substring(0, lastSeparatorIndex));
				}
				String res = dialog.open();
				if (res == null) {
					return;
				}
				fCygPathCommandText.setText(res);
			}
		});


		label = ControlFactory.createLabel(comp, CUIMessages.getString("BinaryParserPage.label.nmCommand")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		fNMCommandText = ControlFactory.createTextField(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fNMCommandText.setLayoutData(gd);
		fNMCommandText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				//updateLaunchConfigurationDialog();
			}
		});
		button = ControlFactory.createPushButton(comp, CUIMessages.getString("BinaryParserPage.label.browse2")); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent evt) {
				handleCygPathButtonSelected();
				//updateLaunchConfigurationDialog();
			}

			private void handleCygPathButtonSelected() {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText(CUIMessages.getString("BinaryParserPage.label.nmCommand")); //$NON-NLS-1$
				String command = fNMCommandText.getText().trim();
				int lastSeparatorIndex = command.lastIndexOf(File.separator);
				if (lastSeparatorIndex != -1) {
					dialog.setFilterPath(command.substring(0, lastSeparatorIndex));
				}
				String res = dialog.open();
				if (res == null) {
					return;
				}
				fNMCommandText.setText(res);
			}
		});

		setControl(comp);
		initializeValues();
	}

	private void initializeValues() {
		String addr2line = null;
		String cppfilt = null;
		String cygpath = null;
		String nm = null;
		IProject proj = getContainer().getProject();
		if (proj != null) {
			try {
				ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(proj, false);
				ICExtensionReference[] cext = cdesc.get(CCorePlugin.BINARY_PARSER_UNIQ_ID);
				if (cext.length > 0) {
					addr2line = cext[0].getExtensionData("addr2line"); //$NON-NLS-1$;
					cppfilt = cext[0].getExtensionData("c++filt"); //$NON-NLS-1$;
					cygpath = cext[0].getExtensionData("cygpath"); //$NON-NLS-1$;
					nm = cext[0].getExtensionData("nm"); //$NON-NLS-1$;
				}
			} catch (CoreException e) {
			}
		} else {
			Preferences store = getContainer().getPreferences();
			if (store != null) {
				addr2line = store.getString(PREF_ADDR2LINE_PATH);
				cppfilt = store.getString(PREF_CPPFILT_PATH);
				cygpath = store.getString(PREF_CYGPATH_PATH);
				nm = store.getString(PREF_NM_PATH);
			}
		}
		fAddr2LineCommandText.setText((addr2line == null || addr2line.length() == 0) ? "addr2line" : addr2line); //$NON-NLS-1$;
		fCPPFiltCommandText.setText((cppfilt == null || cppfilt.length() == 0) ? "c++filt" : cppfilt); //$NON-NLS-1$;
		fCygPathCommandText.setText((cygpath == null || cygpath.length() == 0) ? "cygpath" : cygpath); //$NON-NLS-1$;
		fNMCommandText.setText((nm == null || nm.length() == 0) ? "nm" : nm); //$NON-NLS-1$;
	}
}

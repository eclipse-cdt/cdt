/***********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.ui.dialogs;

import java.io.File;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 */
public class GNUElfBinaryParserPage extends AbstractCOptionPage {

	protected Text fAddr2LineCommandText;
	protected Text fCPPFiltCommandText;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("Saving Attributes", 1);
		IProject proj = getContainer().getProject();
		if (proj != null) {
			ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(proj);
			String addr2line = fAddr2LineCommandText.getText().trim();
			String cppfilt = fCPPFiltCommandText.getText().trim();
			ICExtensionReference[] cext = cdesc.get(CCorePlugin.BINARY_PARSER_UNIQ_ID);
			if (cext.length == 0) {
				// The value was not save yet and we need to save it now
				// to apply the changes.  Search the extension for our own ID
				IPluginDescriptor descriptor = CUIPlugin.getDefault().getDescriptor();
				IExtensionPoint point = descriptor.getExtensionPoint("BinaryParserPage"); //$NON-NLS-1$
				IConfigurationElement[] infos = point.getConfigurationElements();
				for (int i = 0; i < infos.length; i++) {
					String id = infos[i].getAttribute("parserID"); //$NON-NLS-1$
					String clazz = infos[i].getAttribute("class"); //$NON-NLS-1$
					String ego = getClass().getName();
					if (clazz != null && clazz.equals(ego)) {
						cdesc.remove(CCorePlugin.BINARY_PARSER_UNIQ_ID);
						cdesc.create(CCorePlugin.BINARY_PARSER_UNIQ_ID, id);
					}
				}
				// Try again.
				cext = cdesc.get(CCorePlugin.BINARY_PARSER_UNIQ_ID);
			}
			if (cext.length > 0) {
				String orig = cext[0].getExtensionData("addr2line");
				if (orig == null || !orig.equals(addr2line)) {
					cext[0].setExtensionData("addr2line", addr2line);
				}
				orig = cext[0].getExtensionData("c++filt");
				if (orig == null || !orig.equals(cppfilt)) {
					cext[0].setExtensionData("c++filt", cppfilt);
				}
			} 
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	public void performDefaults() {
		String addr2line = null;
		String cppfilt = null;
		IProject proj = getContainer().getProject();
		if (proj != null) {
			try {
				ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(proj);
				ICExtensionReference[] cext = cdesc.get(CCorePlugin.BINARY_PARSER_UNIQ_ID);
				if (cext.length > 0) {
					addr2line = cext[0].getExtensionData("addr2line");
					cppfilt = cext[0].getExtensionData("c++filt");
				}
			} catch (CoreException e) {
			}
		}
		fAddr2LineCommandText.setText((addr2line == null) ? "addr2line" : addr2line);
		fCPPFiltCommandText.setText((cppfilt == null) ? "c++filt" : cppfilt);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite comp = ControlFactory.createCompositeEx(parent, 2, GridData.FILL_HORIZONTAL);
		((GridLayout) comp.getLayout()).makeColumnsEqualWidth = false;

		Label label = ControlFactory.createLabel(comp, "addr2line Command:");
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		fAddr2LineCommandText = ControlFactory.createTextField(comp, SWT.SINGLE | SWT.BORDER);
		fAddr2LineCommandText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				//updateLaunchConfigurationDialog();
			}
		});

		Button button = ControlFactory.createPushButton(comp, "&Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleAddr2LineButtonSelected();
				//updateLaunchConfigurationDialog();
			}

			private void handleAddr2LineButtonSelected() {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText("addr2line Command");
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

		label = ControlFactory.createLabel(comp, "c++filt Command:");
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
		button = ControlFactory.createPushButton(comp, "&Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleCPPFiltButtonSelected();
				//updateLaunchConfigurationDialog();
			}

			private void handleCPPFiltButtonSelected() {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText("c++filt Command");
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
		performDefaults();
	}

}

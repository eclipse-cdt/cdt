/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.internal.ui;

import java.io.File;

import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class GDBDebuggerPage extends AbstractLaunchConfigurationTab {

	protected Text fGDBCommandText;
	protected Text fGDBInitText;
	private Button fAutoSoLibButton;
	private Button fGDBButton;

	public void createControl(Composite parent) {
		GridData gd;
		Label label;
		Button button;
		Composite comp, subComp;

		comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setControl(comp);

		subComp = new Composite(comp, SWT.NONE);
		GridLayout gdbLayout = new GridLayout();
		gdbLayout.numColumns = 2;
		gdbLayout.marginHeight = 0;
		gdbLayout.marginWidth = 0;
		subComp.setLayout(gdbLayout);
		subComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(subComp, SWT.NONE);
		label.setText("GDB debugger:");
		gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		fGDBCommandText = new Text(subComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fGDBCommandText.setLayoutData(gd);
		fGDBCommandText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		button = createPushButton(subComp, "&Browse...", null);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleGDBButtonSelected();
				updateLaunchConfigurationDialog();
			}
			private void handleGDBButtonSelected() {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText("GDB Command");
				String gdbCommand = fGDBCommandText.getText().trim();
				int lastSeparatorIndex = gdbCommand.lastIndexOf(File.separator);
				if (lastSeparatorIndex != -1) {
					dialog.setFilterPath(gdbCommand.substring(0, lastSeparatorIndex));
				}
				String res = dialog.open();
				if (res == null) {
					return;
				}
				fGDBCommandText.setText(res);
			}
		});

		subComp = new Composite(comp, SWT.NONE);
		gdbLayout = new GridLayout();
		gdbLayout.numColumns = 2;
		gdbLayout.marginHeight = 0;
		gdbLayout.marginWidth = 0;
		subComp.setLayout(gdbLayout);
		subComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(subComp, SWT.NONE);
		label.setText("GDB command file:");
		gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		fGDBInitText = new Text(subComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fGDBInitText.setLayoutData(gd);
		fGDBInitText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		button = createPushButton(subComp, "&Browse...", null);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleGDBInitButtonSelected();
				updateLaunchConfigurationDialog();
			}
			private void handleGDBInitButtonSelected() {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText("GDB command file");
				String gdbCommand = fGDBInitText.getText().trim();
				int lastSeparatorIndex = gdbCommand.lastIndexOf(File.separator);
				if (lastSeparatorIndex != -1) {
					dialog.setFilterPath(gdbCommand.substring(0, lastSeparatorIndex));
				}
				String res = dialog.open();
				if (res == null) {
					return;
				}
				fGDBInitText.setText(res);
			}
		});
		label = new Label(comp,SWT.WRAP);
		label.setText("(Warning: Some commands in this file may interfere with the startup operation of the debugger, for example \"run\".)");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = 200;
		label.setLayoutData(gd);

		fAutoSoLibButton = new Button(comp, SWT.CHECK);
		fAutoSoLibButton.setText("Load shared library symbols automatically");
		fAutoSoLibButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb");
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_GDB_INIT, "");
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_AUTO_SOLIB, true);
	}

	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		boolean valid = fGDBCommandText.getText().length() != 0;
		if (valid) {
			setErrorMessage(null);
			setMessage(null);
		} else {
			setErrorMessage("Debugger executable must be specified");
			setMessage(null);
		}
		return valid;
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		String gdbCommand = "gdb";
		String gdbInit = "";
		boolean autosolib = false;
		try {
			gdbCommand = configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb");
			gdbInit = configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_GDB_INIT, "");
			autosolib = configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_AUTO_SOLIB, true);
		} catch (CoreException e) {
		}
		fGDBCommandText.setText(gdbCommand);
		fGDBInitText.setText(gdbInit);
		fAutoSoLibButton.setSelection(autosolib);
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String gdbStr = fGDBCommandText.getText();
		gdbStr.trim();
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, gdbStr);
		gdbStr = fGDBInitText.getText();
		gdbStr.trim();
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_GDB_INIT, gdbStr);
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_AUTO_SOLIB, fAutoSoLibButton.getSelection());
	}

	public String getName() {
		return "GDB Debugger Options";
	}
	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getShell()
	 */
	protected Shell getShell() {
		return super.getShell();
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
	 */
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
	}

}

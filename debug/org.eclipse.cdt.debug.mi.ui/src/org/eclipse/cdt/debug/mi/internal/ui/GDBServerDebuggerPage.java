/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Monta Vista - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.internal.ui;

import org.eclipse.cdt.debug.mi.core.IGDBServerMILaunchConfigurationConstants;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GDBServerDebuggerPage extends AbstractLaunchConfigurationTab {
	
	protected Text fDebuggerCommandText;
	protected Button fTCPButton;
	protected Button fAsyncButton;
	protected Text fHostText;
	protected Text fHostPort;
	protected Text fAsyncDev;
	private Button fAutoSoLibButton;

	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		comp.setLayout(topLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayoutData(gd);
		setControl(comp);	
		
		createVerticalSpacer(comp, 2);
		
		Label debugCommandLabel= new Label(comp, SWT.NONE);
		debugCommandLabel.setText("GDBServer gdb executable:");
		
		fDebuggerCommandText= new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fDebuggerCommandText.setLayoutData(gd);
		fDebuggerCommandText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		Composite radioComp = new Composite(comp, SWT.NONE);
		GridLayout radioLayout = new GridLayout(2, true);
		radioLayout.marginHeight = 0;
		radioLayout.marginWidth = 0;
		radioComp.setLayout(radioLayout);
		gd = new GridData();
		gd.horizontalSpan = 2;
		radioComp.setLayoutData(gd);
		fTCPButton = createRadioButton(radioComp, "Connect using TCP");
		fTCPButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean isTcp = fTCPButton.getSelection();
				fHostPort.setEnabled(isTcp);
				fHostText.setEnabled(isTcp);
				updateLaunchConfigurationDialog();
			}
		});
		fAsyncButton = createRadioButton(radioComp, "Connect using a serial port");
		fAsyncButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fAsyncDev.setEnabled(fAsyncButton.getSelection());
				updateLaunchConfigurationDialog();
			}
		});


		Label hostTextLabel= new Label(comp, SWT.NONE);
		hostTextLabel.setText("GDBServer TCP host:");
		
		fHostText= new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fHostText.setLayoutData(gd);
		fHostText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		Label hostPortLabel= new Label(comp, SWT.NONE);
		hostPortLabel.setText("GDBServer TCP port:");
		
		fHostPort= new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fHostPort.setLayoutData(gd);
		fHostPort.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		Label asyncDevLabel= new Label(comp, SWT.NONE);
		asyncDevLabel.setText("Serial device:");
		
		fAsyncDev= new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fAsyncDev.setLayoutData(gd);
		fAsyncDev.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		fTCPButton.setSelection(true);
		fAsyncButton.setSelection(false);
		fHostText.setEnabled(true);
		fHostPort.setEnabled(true);
		fAsyncDev.setEnabled(false);
		fHostPort.setEnabled(true);
		fHostText.setEnabled(true);
		fAsyncDev.setEnabled(false);

		createVerticalSpacer(comp, 2);

		fAutoSoLibButton = new Button(comp, SWT.CHECK ) ;
		fAutoSoLibButton.setText("Load shared library symbols automatically");
		gd = new GridData();
		gd.horizontalSpan = 2;
		fAutoSoLibButton.setLayoutData(gd);
/*
		ListEditor listEditor = new ListEditor("1", "Shared library search paths:", comp) {
			protected String createList(String[] items) {
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < items.length; i++) {
					buf.append(items[i]);
					buf.append(';');
				}
				return buf.toString();
			}
			protected String getNewInputObject() {
//				StringInputDialog dialog= new StringInputDialog(comp.getShell(), "Library Path", null, "Enter a library path", "", null);
//				if (dialog.open() == dialog.OK) {
//					return dialog.getValue();
//				} else {
//					return null;
//				}
				return null;
			}

			protected String[] parseString(String list) {
				StringTokenizer st = new StringTokenizer(list, ";");
				ArrayList v = new ArrayList();
				while (st.hasMoreElements()) {
					v.add(st.nextElement());
				}
				return (String[]) v.toArray(new String[v.size()]);
			}

		};
*/		
		
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		String attr;
		
		attr = null;
		try {
			attr = configuration.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_DEBUG_NAME, (String) null);
		} catch (CoreException e) {
		}
		if (attr == null) {
			configuration.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb");
		}

		/* The booleans should already be correct. */

		attr = null;
		try {	
			attr = configuration.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_HOST, (String) null);
		} catch (CoreException e) {
		}
		if (attr == null) {
			configuration.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_HOST, "");
		}
		attr = null;
		try {	
			attr = configuration.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_PORT, (String) null);
		} catch (CoreException e) {
		}
		if (attr == null) {
			configuration.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_PORT, "");
		}
		attr = null;
		try {	
			attr = configuration.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_DEV, (String) null);
		} catch (CoreException e) {
		}
		if (attr == null) {
			configuration.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_DEV, "/dev/ttyS0");
		}
	}

	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		boolean valid;
		
		valid= fDebuggerCommandText.getText().length() != 0;
		setErrorMessage(null);
		setMessage(null);
		if (!valid) {
			setErrorMessage("Debugger executable must be specified");
			setMessage(null);
		}
		if (valid) {
			if (fTCPButton.getSelection()) {
				valid = ((fHostText.getText().length() != 0)
						 && (fHostPort.getText().length() != 0));
				if (!valid) {
					setErrorMessage("If TCP is selected, host and port must be specified");
					setMessage(null);
				}
			} else {
				valid = fAsyncDev.getText().length() != 0;
				if (!valid) {
					setErrorMessage("If Async is selected, device must be specified");
					setMessage(null);
				}
			}
		}

		return valid;
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		String debuggerCommand = "gdb";
		boolean isTcp = false;
		String hostText = "";
		String hostPort = "";
		String asyncDev = "/dev/ttyS0";
		boolean autosolib = false;
		try {
			debuggerCommand = configuration.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb");
			autosolib = configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB, false);
		} catch (CoreException e) {
		}
		try {
			isTcp = configuration.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP, false);
			hostText = configuration.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_HOST, "");
			hostPort = configuration.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_PORT, "");
			asyncDev = configuration.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_DEV, "");
		} catch (CoreException e) {
		}
		fDebuggerCommandText.setText(debuggerCommand);
		fTCPButton.setSelection(isTcp);
		fAsyncButton.setSelection(!isTcp);
		fHostText.setText(hostText);
		fHostPort.setText(hostPort);
		fAsyncDev.setText(asyncDev);
		fHostText.setEnabled(isTcp);
		fHostPort.setEnabled(isTcp);
		fAsyncDev.setEnabled(!isTcp);
		fAutoSoLibButton.setSelection(autosolib);		
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String debuggerCommand = fDebuggerCommandText.getText();
		String hostText = fHostText.getText();
		String hostPort = fHostPort.getText();
		String asyncDev = fAsyncDev.getText();
		debuggerCommand.trim();
		hostText.trim();
		hostPort.trim();
		asyncDev.trim();
		configuration.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_DEBUG_NAME, debuggerCommand);
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB, fAutoSoLibButton.getSelection());
		configuration.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP, fTCPButton.getSelection());
		configuration.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_HOST, hostText);
		configuration.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_PORT, hostPort);
		configuration.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_DEV, asyncDev);
	}

	public String getName() {
		return "GDBServer Debugger Options";
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
	 */
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
	}
}

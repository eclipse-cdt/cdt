/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.internal.ui;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CDebuggerPage extends AbstractLaunchConfigurationTab {
	
	protected Text fDebuggerCommandText;
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
		debugCommandLabel.setText("MI Debugger:");
		
		fDebuggerCommandText= new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fDebuggerCommandText.setLayoutData(gd);
		fDebuggerCommandText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		createVerticalSpacer(comp, 2);

		fAutoSoLibButton = new Button(comp, SWT.CHECK ) ;
		fAutoSoLibButton.setText("Load shared library symbols automaticly");
		gd = new GridData();
		gd.horizontalSpan = 2;
		fAutoSoLibButton.setLayoutData(gd);

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
		
		
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb");
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_AUTO_SOLIB, false);
	}

	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		boolean valid= fDebuggerCommandText.getText().length() != 0;
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
		String debuggerCommand = "gdb";
		boolean autosolib = false;
		try {
			debuggerCommand = configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb");
			autosolib = configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_AUTO_SOLIB, false);
		} catch (CoreException e) {
		}
		fDebuggerCommandText.setText(debuggerCommand);
		fAutoSoLibButton.setSelection(autosolib);		
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String debuggerCommand = fDebuggerCommandText.getText();
		debuggerCommand.trim();
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, debuggerCommand);
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_AUTO_SOLIB, fAutoSoLibButton.getSelection());
	}

	public String getName() {
		return "GDB/MI Debugger Options";
	}
}

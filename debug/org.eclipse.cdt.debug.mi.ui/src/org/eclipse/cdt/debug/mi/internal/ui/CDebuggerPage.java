/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.internal.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.launch.ICDTLaunchConfigurationConstants;
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

public class CDebuggerPage extends AbstractLaunchConfigurationTab {
	protected Text fDebuggerCommandText;
	
	protected static final Map EMPTY_MAP = new HashMap(1);

	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayoutData(gd);
		
		createVerticalSpacer(comp, 2);
		
		Label debugCommandLabel= new Label(comp, SWT.NONE);
		debugCommandLabel.setText("Debugger executable:");
		
		fDebuggerCommandText= new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fDebuggerCommandText.setLayoutData(gd);
		fDebuggerCommandText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		fDebuggerCommandText.setText(getCommand());

		setControl(comp);	
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		Map attributeMap = new HashMap(1);
//		attributeMap.put(ATTR_DEBUGGER_COMMAND, getCommand());
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP, attributeMap);
	}

	private String getCommand() {
		return "gdb";
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
		String debuggerCommand= null;
		try {
			Map attributeMap = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP, EMPTY_MAP);
			if (attributeMap != null) {
//				debuggerCommand = (String) attributeMap.get(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND);
				if (debuggerCommand == null) {
					debuggerCommand = getCommand();
				}
			}
		} catch(CoreException ce) {
//			JDIDebugUIPlugin.log(ce);		
		}
		fDebuggerCommandText.setText(debuggerCommand);
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String debuggerCommand = fDebuggerCommandText.getText();
		Map attributeMap = new HashMap(1);
//		attributeMap.put(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_SPECIFIC_ATTRS_MAPVA_COMMAND, debuggerCommand);
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP, attributeMap);	

	}

	public String getName() {
		return "GDB/MI Debugger Options";
	}
}

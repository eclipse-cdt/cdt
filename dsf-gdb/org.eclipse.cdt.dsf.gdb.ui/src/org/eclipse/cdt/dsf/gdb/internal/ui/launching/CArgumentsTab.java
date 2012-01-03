/*******************************************************************************
 * Copyright (c) 2005, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * IBM Corporation
 * Ericsson             - Updated for DSF
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.LaunchMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


/**
 * A launch configuration tab that displays and edits program arguments,
 * and working directory launch configuration attributes.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 */
public class CArgumentsTab extends CLaunchConfigurationTab {

    /**
     * Tab identifier used for ordering of tabs added using the 
     * <code>org.eclipse.debug.ui.launchConfigurationTabs</code>
     * extension point.
     *   
     * @since 2.0
     */
    public static final String TAB_ID = "org.eclipse.cdt.dsf.gdb.launch.argumentsTab"; //$NON-NLS-1$
        
	// Program arguments UI widgets
	protected Label fPrgmArgumentsLabel;
	protected Text fPrgmArgumentsText;
	protected Button fArgumentVariablesButton;
	
	// Working directory
	protected WorkingDirectoryBlock fWorkingDirectoryBlock = new WorkingDirectoryBlock();

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
    @Override
	public void createControl(Composite parent) {
    Font font = parent.getFont();
    Composite comp = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(1, true);
    comp.setLayout(layout);
    comp.setFont(font);

    GridData gd = new GridData(GridData.FILL_BOTH);
    comp.setLayoutData(gd);
    setControl(comp);
    
		GdbUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(getControl(), ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_ARGUMNETS_TAB);
		
		createArgumentComponent(comp, 1);

		fWorkingDirectoryBlock.createControl(comp);
	}

	protected void createArgumentComponent(Composite comp, int horizontalSpan) {
    Font font = comp.getFont();
    Group group = new Group(comp, SWT.NONE);
    group.setFont(font);
    group.setLayout(new GridLayout());
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.horizontalSpan = horizontalSpan;
    group.setLayoutData(gd);
    
		group.setText(LaunchMessages.getString("CArgumentsTab.C/C++_Program_Arguments")); //$NON-NLS-1$
		fPrgmArgumentsText = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		fPrgmArgumentsText.getAccessible().addAccessibleListener(
				new AccessibleAdapter() {                       
                    @Override
					public void getName(AccessibleEvent e) {
                            e.result = LaunchMessages.getString("CArgumentsTab.C/C++_Program_Arguments"); //$NON-NLS-1$
                    }
                }
		);
    gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 40;
    gd.widthHint = 100;
		fPrgmArgumentsText.setLayoutData(gd);
    fPrgmArgumentsText.setFont(font);
		fPrgmArgumentsText.addModifyListener(new ModifyListener() {
            @Override
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		fArgumentVariablesButton= createPushButton(group, LaunchMessages.getString("CArgumentsTab.Variables"), null); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		fArgumentVariablesButton.setLayoutData(gd);
		fArgumentVariablesButton.addSelectionListener(new SelectionAdapter() {
			
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				handleVariablesButtonSelected(fPrgmArgumentsText);
			}
			
		});
		addControlAccessibleListener(fArgumentVariablesButton, fArgumentVariablesButton.getText()); // need to strip the mnemonic from buttons
	}

	/**
	 * A variable entry button has been pressed for the given text
	 * field. Prompt the user for a variable and enter the result
	 * in the given field.
	 */
	protected void handleVariablesButtonSelected(Text textField) {
		String variable = getVariable();
		if (variable != null) {
			textField.append(variable);
		}
	}

	/**
	 * Prompts the user to choose and configure a variable and returns
	 * the resulting string, suitable to be used as an attribute.
	 */
	private String getVariable() {
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
		dialog.open();
		return dialog.getVariableExpression();
	}
	
	public void addControlAccessibleListener(Control control, String controlName) {
		//strip mnemonic (&)
		String[] strs = controlName.split("&"); //$NON-NLS-1$
		StringBuffer stripped = new StringBuffer();
		for (int i = 0; i < strs.length; i++) {
			stripped.append(strs[i]);
		}
		control.getAccessible().addAccessibleListener(new ControlAccessibleListener(stripped.toString()));
	}
	
	private class ControlAccessibleListener extends AccessibleAdapter {
		private String controlName;
		ControlAccessibleListener(String name) {
			controlName = name;
		}
		@Override
		public void getName(AccessibleEvent e) {
			e.result = controlName;
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration config) {
		return fWorkingDirectoryBlock.isValid(config);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
    @Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String) null);
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
    @Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			fPrgmArgumentsText.setText(configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "")); //$NON-NLS-1$
			fWorkingDirectoryBlock.initializeFrom(configuration);
		}
		catch (CoreException e) {
			setErrorMessage(LaunchMessages.getFormattedString("Launch.common.Exception_occurred_reading_configuration_EXCEPTION", e.getStatus().getMessage())); //$NON-NLS-1$
			GdbUIPlugin.log(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
    @Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(
			ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
			getAttributeValueFrom(fPrgmArgumentsText));
		fWorkingDirectoryBlock.performApply(configuration);
	}

	/**
	 * Retuns the string in the text widget, or <code>null</code> if empty.
	 * 
	 * @return text or <code>null</code>
	 */
	protected String getAttributeValueFrom(Text text) {
		String content = text.getText().trim();
		// bug #131513 - eliminate Windows \r line delimiter
		content = content.replaceAll("\r\n", "\n");  //$NON-NLS-1$//$NON-NLS-2$
		if (content.length() > 0) {
			return content;
		}
		return null;
	}

    @Override
    public String getId() {
        return TAB_ID;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
    @Override
	public String getName() {
		return LaunchMessages.getString("CArgumentsTab.Arguments"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setLaunchConfigurationDialog(org.eclipse.debug.ui.ILaunchConfigurationDialog)
	 */
	@Override
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
		fWorkingDirectoryBlock.setLaunchConfigurationDialog(dialog);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		String m = super.getErrorMessage();
		if (m == null) {
			return fWorkingDirectoryBlock.getErrorMessage();
		}
		return m;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getMessage()
	 */
	@Override
	public String getMessage() {
		String m = super.getMessage();
		if (m == null) {
			return fWorkingDirectoryBlock.getMessage();
		}
		return m;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return LaunchImages.get(LaunchImages.IMG_VIEW_ARGUMENTS_TAB);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
	 */
	@Override
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
	}
}

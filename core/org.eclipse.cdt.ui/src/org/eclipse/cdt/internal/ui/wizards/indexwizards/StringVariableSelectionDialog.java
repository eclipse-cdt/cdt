/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.wizards.indexwizards;

import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import org.eclipse.cdt.ui.CUIPlugin;

/**
 * Copied from org.eclipse.debug.ui
 * @since 4.0
 */
public class StringVariableSelectionDialog extends ElementListSelectionDialog {
	
	// variable description
	private Text fDescriptionText;
	// the argument value
	private Text fArgumentText;
	private String fArgumentValue;

	/**
	 * Constructs a new string substitution variable selection dialog.
	 *  
	 * @param parent parent shell
	 */
	public StringVariableSelectionDialog(Shell parent) {
		super(parent, new StringVariableLabelProvider());
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setTitle(Messages.StringVariableSelectionDialog_title); 
		setMessage(Messages.StringVariableSelectionDialog_message); 
		setMultipleSelection(false);
		setElements(VariablesPlugin.getDefault().getStringVariableManager().getVariables());
	}
	
	/**
	 * Returns the variable expression the user generated from this
	 * dialog, or <code>null</code> if none.
	 *  
	 * @return variable expression the user generated from this
	 * dialog, or <code>null</code> if none
	 */
	public String getVariableExpression() {
		Object[] selected = getResult();
		if (selected != null && selected.length == 1) {
			IStringVariable variable = (IStringVariable)selected[0];
			StringBuffer buffer = new StringBuffer();
			buffer.append("${"); //$NON-NLS-1$
			buffer.append(variable.getName());
			if (fArgumentValue != null && fArgumentValue.length() > 0) {
				buffer.append(":"); //$NON-NLS-1$
				buffer.append(fArgumentValue);
			}
			buffer.append("}"); //$NON-NLS-1$
			return buffer.toString();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);
		createArgumentArea((Composite)control);
		return control;
	}

	/**
	 * Creates an area to display a description of the selected variable
	 * and a button to configure the variable's argument.
	 * 
	 * @param parent parent widget
	 */
	private void createArgumentArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		container.setLayoutData(gd);
		container.setFont(parent.getFont());
				
		Label desc = new Label(container, SWT.NONE);
		desc.setFont(parent.getFont());
		desc.setText(Messages.StringVariableSelectionDialog_columnArgument); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		desc.setLayoutData(gd);		
		
		Composite args = new Composite(container, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		args.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		args.setLayoutData(gd);
		args.setFont(container.getFont());
		
		fArgumentText = new Text(args, SWT.BORDER);
		fArgumentText.setFont(container.getFont());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fArgumentText.setLayoutData(gd);		
		fArgumentText.getAccessible().addAccessibleListener(new AccessibleAdapter() {                       
            @Override
			public void getName(AccessibleEvent e) {
                    e.result = Messages.StringVariableSelectionDialog_columnArgument;
            }
		});
		
		desc = new Label(container, SWT.NONE);
		desc.setFont(parent.getFont());
		desc.setText(Messages.StringVariableSelectionDialog_columnDescription); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		desc.setLayoutData(gd);
		
		fDescriptionText = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		fDescriptionText.setFont(container.getFont());
		fDescriptionText.setEditable(false);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.heightHint = 50;
		fDescriptionText.setLayoutData(gd);
	}

	/**
	 * Update variable description and argument button enablement.
	 * 
	 * @see org.eclipse.ui.dialogs.AbstractElementListSelectionDialog#handleSelectionChanged()
	 */
	@Override
	protected void handleSelectionChanged() {
		super.handleSelectionChanged();
		Object[] objects = getSelectedElements();
		boolean argEnabled = false;
		String text = null;
		if (objects.length == 1) {
			IStringVariable variable = (IStringVariable)objects[0];
			 if (variable instanceof IDynamicVariable) {
			 	argEnabled = ((IDynamicVariable)variable).supportsArgument();
			 }
			 text = variable.getDescription();
		}
		if (text == null) {
			text = ""; //$NON-NLS-1$
		}
		fArgumentText.setEnabled(argEnabled);
		fDescriptionText.setText(text);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		fArgumentValue = fArgumentText.getText().trim();
		super.okPressed();
	}

	/**
	 * Returns the name of the section that this dialog stores its settings in
	 * 
	 * @return String
	 */
	private String getDialogSettingsSectionName() {
		return CUIPlugin.PLUGIN_ID + ".STRING_VARIABLE_SELECTION_DIALOG_SECTION"; //$NON-NLS-1$
	}
	
	 /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
     */
    @Override
	protected IDialogSettings getDialogBoundsSettings() {
    	 IDialogSettings settings = CUIPlugin.getDefault().getDialogSettings();
         IDialogSettings section = settings.getSection(getDialogSettingsSectionName());
         if (section == null) {
             section = settings.addNewSection(getDialogSettingsSectionName());
         } 
         return section;
    }
}

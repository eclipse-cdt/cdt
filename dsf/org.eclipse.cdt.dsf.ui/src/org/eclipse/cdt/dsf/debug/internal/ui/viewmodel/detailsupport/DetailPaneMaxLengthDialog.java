/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation 
 *     Wind River Systems, Inc. - extended implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.detailsupport;

import org.eclipse.cdt.dsf.debug.internal.ui.IDsfDebugHelpContextIds;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * Provides a dialog for changing the maximum length allowed in the detail pane
 * 
 * @since 3.0
 */
public class DetailPaneMaxLengthDialog extends TrayDialog {

	private static final String SETTINGS_ID = DsfUIPlugin.PLUGIN_ID + ".MAX_DETAILS_LENGTH_DIALOG"; //$NON-NLS-1$
	
	private Text fTextWidget;
	private Text fErrorTextWidget;
	private String fErrorMessage;
	private String fValue;
	private IInputValidator fValidator;
	
	/**
	 * Constructs a new dialog on the given shell.
	 * 
	 * @param parent shell
	 */
	public DetailPaneMaxLengthDialog(Shell parent) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fValue = Integer.toString(DsfUIPlugin.getDefault().getPreferenceStore().getInt(IDebugUIConstants.PREF_MAX_DETAIL_LENGTH));
		fValidator = new IInputValidator() {
					@Override
					public String isValid(String newText) {
						try {
							int num = Integer.parseInt(newText);
							if (num < 0) {
								return MessagesForDetailPane.PaneMaxLengthDialog_IntegerCannotBeNegative;
							}
						} catch (NumberFormatException e) {
							return MessagesForDetailPane.PaneMaxLengthDialog_EnterAnInteger;
						}
						return null;
					}
				
				};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionDialog#getDialogBoundsSettings()
	 */
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings settings = DsfUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(SETTINGS_ID);
		if (section == null) {
			section = settings.addNewSection(SETTINGS_ID);
		} 
		return section;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		getShell().setText(MessagesForDetailPane.PaneMaxLengthDialog_ConfigureDetails);
		Control contents = super.createContents(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getDialogArea(), IDsfDebugHelpContextIds.DETAIL_PANE_MAX_LENGTH_ACTION);
		return contents;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        Label label = new Label(composite, SWT.WRAP);
        label.setText(MessagesForDetailPane.PaneMaxLengthDialog_MaxCharactersToDisplay);
        GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
        data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        label.setLayoutData(data);
        label.setFont(parent.getFont());
        fTextWidget = new Text(composite, SWT.SINGLE | SWT.BORDER);
        fTextWidget.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
        fTextWidget.setText(fValue);
        fTextWidget.addModifyListener(new ModifyListener() {
            @Override
			public void modifyText(ModifyEvent e) {
                validateInput();
                fValue = fTextWidget.getText();
            }
        });
        fErrorTextWidget = new Text(composite, SWT.READ_ONLY);
        fErrorTextWidget.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        fErrorTextWidget.setBackground(fErrorTextWidget.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        setErrorMessage(fErrorMessage);
        applyDialogFont(composite);
        return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		String text = getValue();
		try {
			DsfUIPlugin.getDefault().getPreferenceStore().setValue(IDebugUIConstants.PREF_MAX_DETAIL_LENGTH, Integer.parseInt(text));
		} 
		catch (NumberFormatException e) {
			DsfUIPlugin.log(e);
		}
		super.okPressed();
	}
	
	/**
     * Returns the string typed into this input dialog.
     * 
     * @return the input string
     * @since 3.3
     */
    public String getValue() {
        return fValue;
    }
	
    /**
     * Validates the current input
     * @since 3.3
     */
    private void validateInput() {
        String errorMessage = null;
        if (fValidator != null) {
            errorMessage = fValidator.isValid(fTextWidget.getText());
        }
        setErrorMessage(errorMessage);
    }
    
    /**
     * Sets the current error message or none if null
     * @param errorMessage
     * @since 3.3
     */
    public void setErrorMessage(String errorMessage) {
    	fErrorMessage = errorMessage;
    	if (fErrorTextWidget != null && !fErrorTextWidget.isDisposed()) {
    		fErrorTextWidget.setText(errorMessage == null ? "" : errorMessage); //$NON-NLS-1$
    		fErrorTextWidget.getParent().update();
    		// Access the ok button by id, in case clients have overridden button creation.
    		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=113643
    		Control button = getButton(IDialogConstants.OK_ID);
    		if (button != null) {
    			button.setEnabled(errorMessage == null);
    		}
    	}
    }	
}

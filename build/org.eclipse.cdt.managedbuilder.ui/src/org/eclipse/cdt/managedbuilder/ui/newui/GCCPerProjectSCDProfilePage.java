/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.newui;

import java.io.File;

import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.NewUIMessages;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * SCD per project profile property/preference page
 * 
 * @author vhirsl
 */
public class GCCPerProjectSCDProfilePage extends AbstractDiscoveryPage {
    private static final String SI_PROVIDER_PARSER_ENABLED_BUTTON = PREFIX + ".siProvider.parser.enabled.button"; //$NON-NLS-1$
    private static final String SI_PROVIDER_COMMAND_LABEL = PREFIX + ".siProvider.command.label"; //$NON-NLS-1$
    private static final String SI_PROVIDER_BROWSE_BUTTON = PREFIX + ".siProvider.browse.button"; //$NON-NLS-1$
    private static final String SI_PROVIDER_COMMAND_DIALOG = PREFIX + ".siProvider.browse.runCommandDialog"; //$NON-NLS-1$
    private static final String SI_PROVIDER_COMMAND_ERROR_MESSAGE= PREFIX + ".siProvider.command.errorMessage"; //$NON-NLS-1$
    private static final String providerId = "specsFile";  //$NON-NLS-1$
    
    private Button sipEnabledButton;
    private Text sipRunCommandText;
    
    private boolean isValid = true;

    public void createSpecific(Composite parent) {
    	ControlFactory.createSeparator(parent, 3);
        
        // si provider enabled checkbox
        sipEnabledButton = ControlFactory.createCheckBox(parent, NewUIMessages.getResourceString(SI_PROVIDER_PARSER_ENABLED_BUTTON));
        sipEnabledButton.setFont(parent.getFont());
        ((GridData)sipEnabledButton.getLayoutData()).horizontalSpan = 3;
        ((GridData)sipEnabledButton.getLayoutData()).grabExcessHorizontalSpace = true;
        sipEnabledButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                bopLoadButton.setEnabled(sipEnabledButton.getSelection());
                getContainer().getBuildInfo().setProviderOutputParserEnabled(providerId, sipEnabledButton.getSelection());
            }
        });
        
        // si command label
        Label siCommandLabel = ControlFactory.createLabel(parent, NewUIMessages.getResourceString(SI_PROVIDER_COMMAND_LABEL));
        ((GridData) siCommandLabel.getLayoutData()).horizontalSpan = 3;

        // text field
        sipRunCommandText = ControlFactory.createTextField(parent, SWT.SINGLE | SWT.BORDER);
        //((GridData) sipRunCommandText.getLayoutData()).horizontalSpan = 2;
        sipRunCommandText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String cmd = sipRunCommandText.getText().trim();
                isValid = (cmd.length() > 0);
                if (isValid)    
                    getContainer().getBuildInfo().setProviderRunCommand(providerId, cmd);
            }
        });
        
        // si browse button
        Button siBrowseButton = ControlFactory.createPushButton(parent, NewUIMessages.getResourceString(SI_PROVIDER_BROWSE_BUTTON));
        ((GridData) siBrowseButton.getLayoutData()).widthHint = AbstractCPropertyTab.BUTTON_WIDTH; 
        siBrowseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                handleSIPBrowseButtonSelected();
            }
            private void handleSIPBrowseButtonSelected() {
                FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
                dialog.setText(NewUIMessages.getResourceString(SI_PROVIDER_COMMAND_DIALOG));
                String fileName = sipRunCommandText.getText().trim();
                int lastSeparatorIndex = fileName.lastIndexOf(File.separator);
                if (lastSeparatorIndex != -1) {
                    dialog.setFilterPath(fileName.substring(0, lastSeparatorIndex));
                }
                String res = dialog.open();
                if (res == null) {
                    return;
                }
                sipRunCommandText.setText(res);
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#isValid()
     */
    public boolean isValid() { return isValid; }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
     */
    public String getErrorMessage() {
        return (isValid) ? null : NewUIMessages.getResourceString(SI_PROVIDER_COMMAND_ERROR_MESSAGE);
    }
    
    public void initializeValues() {
        bopEnabledButton.setSelection(getContainer().getBuildInfo().isBuildOutputParserEnabled());
        setBopOpenFileText(getContainer().getBuildInfo().getBuildOutputFilePath());
        sipEnabledButton.setSelection(getContainer().getBuildInfo().isProviderOutputParserEnabled(providerId));
        sipRunCommandText.setText(getContainer().getBuildInfo().getProviderRunCommand(providerId));
    }

    protected void handlebopEnabledButtonPress() {
        getContainer().getBuildInfo().setBuildOutputParserEnabled(bopEnabledButton.getSelection());
    }

}

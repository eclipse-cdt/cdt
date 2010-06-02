/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.dialogs;

import java.io.File;

import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
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
 * SCD per project profile property/preference page
 * 
 * @author vhirsl
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MBSPerProjectSCDProfilePage extends AbstractDiscoveryPage {
    private static final String providerId = "specsFile";  //$NON-NLS-1$
    
    private Button sipEnabledButton;
    private Text sipRunCommandText;
    private boolean isValid = true;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.ui.dialogs.AbstractCOptionPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite page = ControlFactory.createComposite(parent, 1);
        
        // Add the profile UI contribution.
        Group profileGroup = ControlFactory.createGroup(page,
                MakeUIPlugin.getResourceString("ScannerConfigOptionsDialog.profile.group.label"), 3); //$NON-NLS-1$
        
        GridData gd = (GridData) profileGroup.getLayoutData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        ((GridLayout) profileGroup.getLayout()).makeColumnsEqualWidth = false;
        
        // si provider enabled checkbox
        sipEnabledButton = ControlFactory.createCheckBox(profileGroup, SI_ENABLE);
        ((GridData)sipEnabledButton.getLayoutData()).horizontalSpan = 3;
        ((GridData)sipEnabledButton.getLayoutData()).grabExcessHorizontalSpace = true;
        sipEnabledButton.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e) {         
            }
        });
        
        // si command label
        Label siCommandLabel = ControlFactory.createLabel(profileGroup, SI_COMMAND);
        ((GridData) siCommandLabel.getLayoutData()).horizontalSpan = 3;

        // text field
        sipRunCommandText = ControlFactory.createTextField(profileGroup, SWT.SINGLE | SWT.BORDER);
        //((GridData) sipRunCommandText.getLayoutData()).horizontalSpan = 2;
        sipRunCommandText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                handleModifyRunCommandText();
            }
        });
        
        // si browse button
        Button siBrowseButton = ControlFactory.createPushButton(profileGroup, SI_BROWSE);
        ((GridData) siBrowseButton.getLayoutData()).minimumWidth = 120;
        siBrowseButton.addSelectionListener(new SelectionAdapter() {

            @Override
			public void widgetSelected(SelectionEvent event) {
                handleSIPBrowseButtonSelected();
            }

            private void handleSIPBrowseButtonSelected() {
                FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
                dialog.setText(SI_DIALOG);
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
        setControl(page);
        initializeValues();
    }

    private void handleModifyRunCommandText() {
        String cmd = sipRunCommandText.getText().trim();
        isValid = (cmd.length() > 0) ? true : false;
        getContainer().updateContainer();
    }

    private void initializeValues() {
        sipEnabledButton.setSelection(getContainer().getBuildInfo().isProviderOutputParserEnabled(providerId));
        sipRunCommandText.setText(getContainer().getBuildInfo().getProviderRunCommand(providerId));
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#isValid()
     */
    @Override
	public boolean isValid() {
        return isValid;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
     */
    @Override
	public String getErrorMessage() {
        return (isValid) ? null : SI_ERROR;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.ui.dialogs.AbstractDiscoveryPage#populateBuildInfo(org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2)
     */
    @Override
	protected void populateBuildInfo(IScannerConfigBuilderInfo2 buildInfo) {
        if (buildInfo != null) {
            buildInfo.setBuildOutputFileActionEnabled(true);
            buildInfo.setProviderOutputParserEnabled(providerId, sipEnabledButton.getSelection());
            buildInfo.setProviderRunCommand(providerId, sipRunCommandText.getText().trim());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.ui.dialogs.AbstractDiscoveryPage#restoreFromBuildinfo(org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2)
     */
    @Override
	protected void restoreFromBuildinfo(IScannerConfigBuilderInfo2 buildInfo) {
        if (buildInfo != null) {
            sipEnabledButton.setSelection(buildInfo.isProviderOutputParserEnabled(providerId));
            sipRunCommandText.setText(buildInfo.getProviderRunCommand(providerId));
        }
    }

}

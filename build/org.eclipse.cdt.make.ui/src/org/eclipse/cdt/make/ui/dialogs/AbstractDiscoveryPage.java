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

import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Abstract SCD profile page
 * 
 * @author vhirsl
 */
public abstract class AbstractDiscoveryPage extends DialogPage {
    /**
     * @deprecated since CDT 6.1
     */
    @Deprecated
    protected static final String PREFIX = "ScannerConfigOptionsDialog"; //$NON-NLS-1$

    protected static final String PROFILE_GROUP_LABEL = "ScannerConfigOptionsDialog.profile.group.label"; //$NON-NLS-1$
 
    protected static final String B_ENABLE = MakeUIPlugin.getResourceString("ScannerConfigOptionsDialog.boProvider.parser.enabled.button"); //$NON-NLS-1$
    protected static final String L_OPEN = MakeUIPlugin.getResourceString("ScannerConfigOptionsDialog.boProvider.open.label"); //$NON-NLS-1$
    protected static final String B_BROWSE = MakeUIPlugin.getResourceString("ScannerConfigOptionsDialog.boProvider.browse.button"); //$NON-NLS-1$
    protected static final String F_OPEN = MakeUIPlugin.getResourceString("ScannerConfigOptionsDialog.boProvider.browse.openFileDialog"); //$NON-NLS-1$
    protected static final String B_LOAD = MakeUIPlugin.getResourceString("ScannerConfigOptionsDialog.boProvider.load.button"); //$NON-NLS-1$
    
    protected static final String SI_ENABLE = MakeUIPlugin.getResourceString("ScannerConfigOptionsDialog.siProvider.parser.enabled.button"); //$NON-NLS-1$
    protected static final String SI_COMMAND = MakeUIPlugin.getResourceString("ScannerConfigOptionsDialog.siProvider.command.label"); //$NON-NLS-1$
    /** @since 7.0 */
    protected static final String SI_ARGS = MakeUIPlugin.getResourceString("ScannerConfigOptionsDialog.siProvider.args.label"); //$NON-NLS-1$
    protected static final String SI_BROWSE = MakeUIPlugin.getResourceString("ScannerConfigOptionsDialog.siProvider.browse.button"); //$NON-NLS-1$
    protected static final String SI_DIALOG = MakeUIPlugin.getResourceString("ScannerConfigOptionsDialog.siProvider.browse.runCommandDialog"); //$NON-NLS-1$
    protected static final String SI_ERROR  = MakeUIPlugin.getResourceString("ScannerConfigOptionsDialog.siProvider.command.errorMessage"); //$NON-NLS-1$
    
    protected AbstractDiscoveryOptionsBlock fContainer; // parent
    
    /**
     * @return Returns the fContainer.
     */
    protected AbstractDiscoveryOptionsBlock getContainer() {
        return fContainer;
    }
    /**
     * @param container The fContainer to set.
     */
    public void setContainer(AbstractDiscoveryOptionsBlock container) {
        fContainer = container;
    }
    /**
     * 
     */
    public AbstractDiscoveryPage() {
        super();
    }

    public AbstractDiscoveryPage(String title) {
        super(title);
    }

    public AbstractDiscoveryPage(String title, ImageDescriptor image) {
        super(title, image);
    }

    protected Button addVariablesButton(Composite parent, final Text control) {
        Button variablesButton = ControlFactory.createPushButton(parent,
                MakeUIPlugin.getResourceString("ScannerConfigOptionsDialog.common.variables.button")); //$NON-NLS-1$
        ((GridData) variablesButton.getLayoutData()).widthHint = 
                SWTUtil.getButtonWidthHint(variablesButton);
        
        variablesButton.addSelectionListener(new SelectionAdapter() {
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
			public void widgetSelected(SelectionEvent arg0) {
                handleVariablesButtonSelected(control);
            }
        });
        return variablesButton;
    }

    /**
     * A variable entry button has been pressed for the given text field. Prompt
     * the user for a variable and enter the result in the given field.
     */
    private void handleVariablesButtonSelected(Text textField) {
        String variable = getVariable();
        if (variable != null) {
            textField.append(variable);
        }
    }

    /**
     * Prompts the user to choose and configure a variable and returns the
     * resulting string, suitable to be used as an attribute.
     */
    private String getVariable() {
        StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
        dialog.open();
        return dialog.getVariableExpression();
    }

    protected abstract boolean isValid();
    protected abstract void populateBuildInfo(IScannerConfigBuilderInfo2 buildInfo);
    protected abstract void restoreFromBuildinfo(IScannerConfigBuilderInfo2 buildInfo);
    
    public void performApply() {
        IScannerConfigBuilderInfo2 buildInfo = getContainer().getBuildInfo();
        
        populateBuildInfo(buildInfo);
    }
    
    public void performDefaults() {
        IScannerConfigBuilderInfo2 buildInfo = getContainer().getBuildInfo();
        
        restoreFromBuildinfo(buildInfo);
    }
    
}

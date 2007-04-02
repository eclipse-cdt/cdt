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
    protected static final String PREFIX = "ScannerConfigOptionsDialog"; //$NON-NLS-1$
    protected static final String PROFILE_GROUP_LABEL = PREFIX + ".profile.group.label"; //$NON-NLS-1$
    private static final String VARIABLES_BUTTON = PREFIX + ".common.variables.button"; //$NON-NLS-1$
 
    private static final String BO_PROVIDER_PARSER_ENABLED_BUTTON = PREFIX + ".boProvider.parser.enabled.button"; //$NON-NLS-1$
    private static final String BO_PROVIDER_OPEN_LABEL = PREFIX + ".boProvider.open.label"; //$NON-NLS-1$
    private static final String BO_PROVIDER_BROWSE_BUTTON = PREFIX + ".boProvider.browse.button"; //$NON-NLS-1$
    private static final String BO_PROVIDER_OPEN_FILE_DIALOG = PREFIX + ".boProvider.browse.openFileDialog"; //$NON-NLS-1$
    private static final String BO_PROVIDER_LOAD_BUTTON = PREFIX + ".boProvider.load.button"; //$NON-NLS-1$
    
    private static final String SI_PROVIDER_PARSER_ENABLED_BUTTON = PREFIX + ".siProvider.parser.enabled.button"; //$NON-NLS-1$
    private static final String SI_PROVIDER_COMMAND_LABEL = PREFIX + ".siProvider.command.label"; //$NON-NLS-1$
    private static final String SI_PROVIDER_BROWSE_BUTTON = PREFIX + ".siProvider.browse.button"; //$NON-NLS-1$
    private static final String SI_PROVIDER_COMMAND_DIALOG = PREFIX + ".siProvider.browse.runCommandDialog"; //$NON-NLS-1$
    private static final String SI_PROVIDER_COMMAND_ERROR_MESSAGE= PREFIX + ".siProvider.command.errorMessage"; //$NON-NLS-1$
    
    protected static final String B_ENABLE = MakeUIPlugin.getResourceString(BO_PROVIDER_PARSER_ENABLED_BUTTON);
    protected static final String L_OPEN = MakeUIPlugin.getResourceString(BO_PROVIDER_OPEN_LABEL);
    protected static final String B_BROWSE = MakeUIPlugin.getResourceString(BO_PROVIDER_BROWSE_BUTTON);
    protected static final String F_OPEN = MakeUIPlugin.getResourceString(BO_PROVIDER_OPEN_FILE_DIALOG);
    protected static final String B_LOAD = MakeUIPlugin.getResourceString(BO_PROVIDER_LOAD_BUTTON);
    
    protected static final String SI_ENABLE = MakeUIPlugin.getResourceString(SI_PROVIDER_PARSER_ENABLED_BUTTON);
    protected static final String SI_COMMAND = MakeUIPlugin.getResourceString(SI_PROVIDER_COMMAND_LABEL);
    protected static final String SI_BROWSE = MakeUIPlugin.getResourceString(SI_PROVIDER_BROWSE_BUTTON);
    protected static final String SI_DIALOG = MakeUIPlugin.getResourceString(SI_PROVIDER_COMMAND_DIALOG);
    protected static final String SI_ERROR  = MakeUIPlugin.getResourceString(SI_PROVIDER_COMMAND_ERROR_MESSAGE);
    
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

    /**
     * @param title
     */
    public AbstractDiscoveryPage(String title) {
        super(title);
    }

    /**
     * @param title
     * @param image
     */
    public AbstractDiscoveryPage(String title, ImageDescriptor image) {
        super(title, image);
    }

    protected Button addVariablesButton(Composite parent, final Text control) {
        Button variablesButton = ControlFactory.createPushButton(parent,
                MakeUIPlugin.getResourceString(VARIABLES_BUTTON));
        ((GridData) variablesButton.getLayoutData()).widthHint = 
                SWTUtil.getButtonWidthHint(variablesButton);
        
        variablesButton.addSelectionListener(new SelectionAdapter() {
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
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

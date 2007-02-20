/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.newui;

import java.io.File;

import org.eclipse.cdt.make.internal.core.scannerconfig.jobs.BuildOutputReaderJob;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.NewUIMessages;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Abstract SCD profile page
 * 
 * @author vhirsl
 */
public abstract class AbstractDiscoveryPage extends DialogPage {
    protected static final String PREFIX = "ScannerConfigOptionsDialog"; //$NON-NLS-1$
    protected static final String PROFILE_GROUP_LABEL = PREFIX + ".profile.group.label"; //$NON-NLS-1$
    protected static final String VARIABLES_BUTTON = PREFIX + ".common.variables.button"; //$NON-NLS-1$
    private static final String BO_PROVIDER_PARSER_ENABLED_BUTTON = PREFIX + ".boProvider.parser.enabled.button"; //$NON-NLS-1$
    private static final String BO_PROVIDER_OPEN_LABEL = PREFIX + ".boProvider.open.label"; //$NON-NLS-1$
    private static final String BO_PROVIDER_LOAD_BUTTON = PREFIX + ".boProvider.load.button"; //$NON-NLS-1$
    private static final String BO_PROVIDER_BROWSE_BUTTON = PREFIX + ".boProvider.browse.button"; //$NON-NLS-1$
    private static final String BO_PROVIDER_OPEN_FILE_DIALOG = PREFIX + ".boProvider.browse.openFileDialog"; //$NON-NLS-1$
    
    protected IBuildInfoContainer fContainer; // parent
    protected Shell shell;
    
    // thread synchronization
    protected static Object lock = AbstractDiscoveryPage.class;
    protected static AbstractDiscoveryPage instance;
    protected static boolean loadButtonInitialEnabled = true;

    // controls affected by LOAD button
    // if descendant do not use it, it's not obligatory to initialize them.
    protected Button bopLoadButton;
    protected Button bopEnabledButton;
    protected Text bopOpenFileText;
    
    public AbstractDiscoveryPage() {  super(); }
    public AbstractDiscoveryPage(String title) { super(title); }
    public AbstractDiscoveryPage(String title, ImageDescriptor image) { super(title, image); }
    protected IBuildInfoContainer getContainer() { return fContainer; }
    protected void setContainer(IBuildInfoContainer container) { fContainer = container; }
    /**
     * @param title
     * @param image
     */

    protected Button addVariablesButton(Composite parent, final Text control) {
    	shell = parent.getShell();
        Button variablesButton = ControlFactory.createPushButton(parent,
        		AbstractCPropertyTab.VARIABLESBUTTON_NAME);
        ((GridData) variablesButton.getLayoutData()).widthHint = AbstractCPropertyTab.BUTTON_WIDTH;
        variablesButton.addSelectionListener(new SelectionAdapter() {
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
        String s = AbstractCPropertyTab.getVariableDialog(shell, fContainer.getConfiguration());
        if (s != null) textField.append(s);
    }
 
    public abstract void initializeValues();
    protected abstract void handlebopEnabledButtonPress();
    protected abstract void createSpecific(Composite parent);
    
    // Handle Load button press
    protected void handleBOPLoadFileButtonSelected() {
           loadButtonInitialEnabled = false;
           bopLoadButton.setEnabled(false);
           
           IProject project = getContainer().getProject();
           Job readerJob = new BuildOutputReaderJob(project, getContainer().getContext(), getContainer().getBuildInfo());
           readerJob.setPriority(Job.LONG);
           readerJob.addJobChangeListener(new JobChangeAdapter() {
               public void done(IJobChangeEvent event) {
                   synchronized (lock) {
                       if (!instance.shell.isDisposed()) {
                           instance.shell.getDisplay().asyncExec(new Runnable() {
                               public void run() {
                                   if (!instance.shell.isDisposed()) {
                                       loadButtonInitialEnabled = instance.bopEnabledButton.getSelection() && handleModifyOpenFileText();
                                       instance.bopLoadButton.setEnabled(loadButtonInitialEnabled);
                                   }
                                   else {
                                       loadButtonInitialEnabled = true;
                                   }
                               }
                           });
                       }
                       else {
                           loadButtonInitialEnabled = true;
                       }
                   }
               }
           });
           readerJob.schedule();
       }

    protected boolean handleModifyOpenFileText() {
        String fileName = getBopOpenFileText();
        bopLoadButton.setEnabled(bopEnabledButton.getSelection() &&
                                 fileName.length() > 0 &&
                                 (new File(fileName)).exists());
        return bopLoadButton.getEnabled();
    }

    protected String getBopOpenFileText() {
        // from project relative path to absolute path
        String fileName = bopOpenFileText.getText().trim();
        if (fileName.length() > 0) {
            IPath filePath = new Path(fileName);
            if (!filePath.isAbsolute()) {
                if (getContainer().getProject() != null) {
                    IPath projectPath = getContainer().getProject().getLocation();
                    filePath = projectPath.append(filePath);
                    fileName = filePath.toString();
                }
            }
        }
        return fileName;
    }

    protected void setBopOpenFileText(String fileName) {
        // from absolute path to project relative path
        if (fileName.length() > 0) {
            IPath filePath = new Path(fileName);
            if (filePath.isAbsolute()) {
                if (getContainer().getProject() != null) {
                    IPath projectPath = getContainer().getProject().getLocation();
                    if (projectPath.isPrefixOf(filePath)) {
                        filePath = filePath.removeFirstSegments(projectPath.segmentCount());
                        filePath = filePath.setDevice(null);
                        fileName = filePath.toString();
                    }
                }
            }
        }
        bopOpenFileText.setText(fileName);
    }
    
    /**
     * A part of "createControl()" method: creates common widgets
     * @param parent
     * @return
     */
    public void createControl(Composite parent) {
        Group profileGroup = ControlFactory.createGroup(parent, NewUIMessages.getResourceString(PROFILE_GROUP_LABEL), 3);
        GridData gd = (GridData) profileGroup.getLayoutData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        ((GridLayout) profileGroup.getLayout()).makeColumnsEqualWidth = false;

        // Add bop enabled checkbox
        bopEnabledButton = ControlFactory.createCheckBox(profileGroup, NewUIMessages.getResourceString(BO_PROVIDER_PARSER_ENABLED_BUTTON));
        bopEnabledButton.setFont(parent.getFont());
        ((GridData)bopEnabledButton.getLayoutData()).horizontalSpan = 3;
        ((GridData)bopEnabledButton.getLayoutData()).grabExcessHorizontalSpace = true;
        bopEnabledButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	handlebopEnabledButtonPress();
                handleModifyOpenFileText();
            }
        });
        
        // load label
        Label loadLabel = ControlFactory.createLabel(profileGroup, NewUIMessages.getResourceString(BO_PROVIDER_OPEN_LABEL));
        ((GridData) loadLabel.getLayoutData()).horizontalSpan = 2;

        // load button
        bopLoadButton = ControlFactory.createPushButton(profileGroup, NewUIMessages.getResourceString(BO_PROVIDER_LOAD_BUTTON));
        ((GridData) bopLoadButton.getLayoutData()).widthHint = AbstractCPropertyTab.BUTTON_WIDTH;
        bopLoadButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                handleBOPLoadFileButtonSelected();
            }
        });
        if (getContainer().getProject() == null) {  // project properties
            bopLoadButton.setVisible(false);
        }
        // text field
        bopOpenFileText = ControlFactory.createTextField(profileGroup, SWT.SINGLE | SWT.BORDER);
        bopOpenFileText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                getContainer().getBuildInfo().setBuildOutputFilePath(getBopOpenFileText());
                handleModifyOpenFileText();
            }
        });
        bopLoadButton.setEnabled(loadButtonInitialEnabled && handleModifyOpenFileText());
        
        // browse button
        Button browseButton = ControlFactory.createPushButton(profileGroup, NewUIMessages.getResourceString(BO_PROVIDER_BROWSE_BUTTON));
        ((GridData) browseButton.getLayoutData()).widthHint = AbstractCPropertyTab.BUTTON_WIDTH;
        browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                handleBOPBrowseButtonSelected();
            }
            private void handleBOPBrowseButtonSelected() {
                FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
                dialog.setText(NewUIMessages.getResourceString(BO_PROVIDER_OPEN_FILE_DIALOG));
                String fileName = getBopOpenFileText();
                IPath filterPath;
                if (fileName.length() == 0 && getContainer().getProject() != null) {
                    filterPath = getContainer().getProject().getLocation();
                }
                else {
                    IPath filePath = new Path(fileName);
                    filterPath = filePath.removeLastSegments(1).makeAbsolute();
                }
                dialog.setFilterPath(filterPath.toOSString());
                String res = dialog.open();
                if (res == null) {
                    return;
                }
                setBopOpenFileText(res);
            }
        });

        // variable button
        addVariablesButton(profileGroup, bopOpenFileText);

        createSpecific(profileGroup);

        setControl(profileGroup);
        // set the shell variable; must be after setControl
        synchronized (lock) {
            shell = getShell();
            instance = this;
        }
        
        // this parameter should be always set.  
        getContainer().getBuildInfo().setBuildOutputFileActionEnabled(true);
    }
}

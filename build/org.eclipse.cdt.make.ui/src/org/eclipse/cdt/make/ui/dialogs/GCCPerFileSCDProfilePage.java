/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.dialogs;

import java.io.File;
import java.util.List;

import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.internal.core.scannerconfig.jobs.BuildOutputReaderJob;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
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
 * SCD per project profile property/preference page
 *
 * @author vhirsl
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class GCCPerFileSCDProfilePage extends AbstractDiscoveryPage {

    private Button bopEnabledButton;
    private Text bopOpenFileText;
    private Button bopLoadButton;

    // thread syncronization
    private static Object lock = GCCPerFileSCDProfilePage.class;
    private Shell shell;
    private static GCCPerFileSCDProfilePage instance;
    private static boolean loadButtonInitialEnabled = true;

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
	public void createControl(Composite parent) {
        Composite page = ControlFactory.createComposite(parent, 1);
//        ((GridData) page.getLayoutData()).grabExcessVerticalSpace = true;
//        ((GridData) page.getLayoutData()).verticalAlignment = GridData.FILL;

        // Add the profile UI contribution.
        Group profileGroup = ControlFactory.createGroup(page,
                MakeUIPlugin.getResourceString("ScannerConfigOptionsDialog.profile.group.label"), 3); //$NON-NLS-1$

        GridData gd = (GridData) profileGroup.getLayoutData();
        gd.grabExcessHorizontalSpace = true;
//        PixelConverter converter = new PixelConverter(profileGroup);
//        gd.heightHint = converter.convertVerticalDLUsToPixels(DEFAULT_HEIGHT);
        ((GridLayout) profileGroup.getLayout()).makeColumnsEqualWidth = false;

        // Add bop enabled checkbox
        bopEnabledButton = ControlFactory.createCheckBox(profileGroup, B_ENABLE);
//        bopEnabledButton.setFont(parent.getFont());
        ((GridData)bopEnabledButton.getLayoutData()).horizontalSpan = 3;
        ((GridData)bopEnabledButton.getLayoutData()).grabExcessHorizontalSpace = true;
        bopEnabledButton.addSelectionListener(new SelectionAdapter() {

            @Override
			public void widgetSelected(SelectionEvent e) {
                handleModifyOpenFileText();
            }

        });

        // load label
        Label loadLabel = ControlFactory.createLabel(profileGroup, L_OPEN);
        ((GridData) loadLabel.getLayoutData()).horizontalSpan = 2;

        // load button
        bopLoadButton = ControlFactory.createPushButton(profileGroup, B_LOAD);
        ((GridData) bopLoadButton.getLayoutData()).minimumWidth = 120;
        bopLoadButton.addSelectionListener(new SelectionAdapter() {

            @Override
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
            @Override
			public void modifyText(ModifyEvent e) {
                handleModifyOpenFileText();
            }
        });
        bopLoadButton.setEnabled(loadButtonInitialEnabled && handleModifyOpenFileText());

        // browse button
        Button browseButton = ControlFactory.createPushButton(profileGroup, B_BROWSE);
        ((GridData) browseButton.getLayoutData()).minimumWidth = 120;
        browseButton.addSelectionListener(new SelectionAdapter() {

            @Override
			public void widgetSelected(SelectionEvent event) {
                handleBOPBrowseButtonSelected();
            }

            private void handleBOPBrowseButtonSelected() {
                FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
                dialog.setText(F_OPEN);
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

        setControl(page);
        // set the shell variable; must be after setControl
        //lock.acquire();
        synchronized (lock) {
            shell = getShell();
            instance = this;
        }
        //lock.release();
        initializeValues();
    }

    protected boolean handleModifyOpenFileText() {
        String fileName = getBopOpenFileText();
        bopLoadButton.setEnabled(bopEnabledButton.getSelection() &&
                                 fileName.length() > 0 &&
                                 (new File(fileName)).exists());
        return bopLoadButton.getEnabled();
    }

    private String getBopOpenFileText() {
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

    private void setBopOpenFileText(String fileName) {
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

    private void initializeValues() {
        bopEnabledButton.setSelection(getContainer().getBuildInfo().isBuildOutputParserEnabled());
        setBopOpenFileText(getContainer().getBuildInfo().getBuildOutputFilePath());
    }

    private void handleBOPLoadFileButtonSelected() {
        if (!getContainer().checkDialogForChanges()) return;
        loadButtonInitialEnabled = false;
        bopLoadButton.setEnabled(false);

        // populate buildInfo to be used by the reader job
        populateBuildInfo(getContainer().getBuildInfo());
        IProject project = getContainer().getProject();
        Job readerJob = new BuildOutputReaderJob(project, getContainer().getBuildInfo());
        readerJob.setPriority(Job.LONG);
        readerJob.addJobChangeListener(new JobChangeAdapter() {

            @Override
			public void done(IJobChangeEvent event) {
                //lock.acquire();
                synchronized (lock) {
                    if (!instance.shell.isDisposed()) {
                        instance.shell.getDisplay().asyncExec(new Runnable() {

                            @Override
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
                //lock.release();
            }

        });
        readerJob.schedule();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.ui.dialogs.AbstractDiscoveryPage#isValid()
     */
    @Override
	protected boolean isValid() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.ui.dialogs.AbstractDiscoveryPage#populateBuildInfo(org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2)
     */
    @Override
	protected void populateBuildInfo(IScannerConfigBuilderInfo2 buildInfo) {
        if (buildInfo != null) {
            buildInfo.setBuildOutputFileActionEnabled(true);
            buildInfo.setBuildOutputFilePath(getBopOpenFileText());
            buildInfo.setBuildOutputParserEnabled(bopEnabledButton.getSelection());
            buildInfo.setProviderOutputParserEnabled(getProviderIDForSelectedProfile(), bopEnabledButton.getSelection());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.ui.dialogs.AbstractDiscoveryPage#restoreFromBuildinfo(org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2)
     */
    @Override
	protected void restoreFromBuildinfo(IScannerConfigBuilderInfo2 buildInfo) {
        if (buildInfo != null) {
            setBopOpenFileText(buildInfo.getBuildOutputFilePath());
            bopEnabledButton.setSelection(buildInfo.isBuildOutputParserEnabled());
        }
    }

    private String getProviderIDForSelectedProfile() {
    	IScannerConfigBuilderInfo2 builderInfo = getContainer().getBuildInfo();
    	// Provider IDs for selected profile
    	List<String> providerIDs = builderInfo.getProviderIdList();
    	if(providerIDs.size() == 0)
    		return ""; //$NON-NLS-1$
    	return providerIDs.iterator().next();
    }

}

/***********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.make.ui.dialogs;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * TODO Provide description
 * 
 * @author vhirsl
 */
public abstract class AbstractDiscoveryPage extends AbstractCOptionPage {

    private Preferences fPrefs;
    private IScannerConfigBuilderInfo2 fBuildInfo;
    private boolean fInitialized = false;
    
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

    /**
     * @return Returns the fPrefs.
     */
    protected Preferences getPrefs() {
        return fPrefs;
    }
    /**
     * @return Returns the fBuildInfo.
     */
    protected IScannerConfigBuilderInfo2 getBuildInfo() {
        return fBuildInfo;
    }
    /**
     * @return Returns the fInitialized.
     */
    protected boolean isInitialized() {
        return fInitialized;
    }
    /**
     * @param initialized The fInitialized to set.
     */
    protected void setInitialized(boolean initialized) {
        fInitialized = initialized;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#setContainer(org.eclipse.cdt.ui.dialogs.ICOptionContainer)
     */
    public void setContainer(ICOptionContainer container) {
        super.setContainer(container);
        
        fPrefs = getContainer().getPreferences();
        IProject project = getContainer().getProject();

        fInitialized = true;
        if (project != null) {
            try {
                fBuildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(project);
            } catch (CoreException e) {
                // missing builder information (builder disabled or legacy project) 
                fInitialized = false;
                fBuildInfo = null;
            }
        } else {
            fBuildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(fPrefs, false);
        }
    }

    /**
     * Create build info based on project properties
     * @param project
     * @return
     */
    protected IScannerConfigBuilderInfo2 createBuildInfo(IProject project) {
        IScannerConfigBuilderInfo2 bi = null;
        if (project != null) {
            try {
                bi = ScannerConfigProfileManager.createScannerConfigBuildInfo2(project);
            } catch (CoreException e) {
                // disabled builder... just log it 
                MakeCorePlugin.log(e);
            }
        }
        else {
            bi = ScannerConfigProfileManager.createScannerConfigBuildInfo2(fPrefs, false);
        }
        return bi;
    }
    
    /**
     * Create build info based on preferences
     * @return
     */
    protected IScannerConfigBuilderInfo2 createBuildInfo() {
        IScannerConfigBuilderInfo2 bi = null;
        // Populate with the default values
        if (getContainer().getProject() != null) {
            // get the preferences
            bi = ScannerConfigProfileManager.createScannerConfigBuildInfo2(fPrefs, false);
        } else {
            // get the defaults
            bi = ScannerConfigProfileManager.createScannerConfigBuildInfo2(fPrefs, true);
        }
        return bi;
    }
    
}

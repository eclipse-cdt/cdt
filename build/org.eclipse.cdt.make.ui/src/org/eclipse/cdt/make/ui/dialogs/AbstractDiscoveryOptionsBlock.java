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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.preferences.TabFolderLayout;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;

/**
 * Fremework for loading profile option pages
 * 
 * @author vhirsl
 */
public abstract class AbstractDiscoveryOptionsBlock extends AbstractCOptionPage {
    private Preferences fPrefs;
    private IScannerConfigBuilderInfo2 fBuildInfo;
    private boolean fInitialized = false;

    private Map fProfilePageMap = null;

    // Composite parent provided by the block.
    private Composite fCompositeParent;
    private AbstractDiscoveryPage fCurrentPage;

    /**
     * @return Returns the project.
     */
    protected IProject getProject() {
        return getContainer().getProject();
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
    /**
     * Create a profile page only on request
     * 
     * @author vhirsl
     */
    protected static class DiscoveryProfilePageConfiguration {

        AbstractDiscoveryPage page;
        IConfigurationElement fElement;

        public DiscoveryProfilePageConfiguration(IConfigurationElement element) {
            fElement = element;
        }

        public AbstractDiscoveryPage getPage() throws CoreException {
            if (page == null) {
                page = (AbstractDiscoveryPage) fElement.createExecutableExtension("class"); //$NON-NLS-1$
            }
            return page;
        }
        public String getName() {
            return fElement.getAttribute("name"); //$NON-NLS-1$
        }
    }

    /**
     * @param title
     */
    public AbstractDiscoveryOptionsBlock(String title) {
        super(title);
        initializeProfilePageMap();
    }

    /**
     * @param title
     * @param image
     */
    public AbstractDiscoveryOptionsBlock(String title, ImageDescriptor image) {
        super(title, image);
        initializeProfilePageMap();
    }

    /**
     * 
     */
    private void initializeProfilePageMap() {
        fProfilePageMap = new HashMap(5);
        
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(MakeUIPlugin.getPluginId(), "DiscoveryProfilePage"); //$NON-NLS-1$
        IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].getName().equals("profilePage")) { //$NON-NLS-1$
                String id = infos[i].getAttribute("profileId"); //$NON-NLS-1$
                fProfilePageMap.put(id, new DiscoveryProfilePageConfiguration(infos[i]));
            }
        }
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

    protected void updateContainer() {
        getContainer().updateContainer();
    }

    /**
     * @param project
     */
    protected void createBuildInfo() {
        if (getProject() != null) {
            try {
                // get the project properties
                fBuildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(getProject());
            }
            catch (CoreException e) {
                fBuildInfo = null;
            }
        }
        else {
            // get the preferences
            fBuildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(fPrefs, false);
        }
    }

    /**
     * Create build info based on preferences
     */
    protected void createDefaultBuildInfo() {
        // Populate with the default values
        if (getProject() != null) {
            // get the preferences
            fBuildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(fPrefs, false);
        } else {
            // get the defaults
            fBuildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(fPrefs, true);
        }
    }
    
    protected Composite getCompositeParent() {
        return fCompositeParent;
    }

    protected void setCompositeParent(Composite parent) {
        fCompositeParent = parent;
        fCompositeParent.setLayout(new TabFolderLayout());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            handleDiscoveryProfileChanged();
        }
    }

    /**
     * Notification that the user changed the selection of the Binary Parser.
     */
    protected void handleDiscoveryProfileChanged() {
        if (getCompositeParent() == null) {
            return;
        }
        String profileId = getCurrentProfileId();
        AbstractDiscoveryPage page = getDiscoveryProfilePage(profileId);
        if (page != null) {
            if (page.getControl() == null) {
                Composite parent = getCompositeParent();
                page.setContainer(this);
                page.createControl(parent);
                parent.layout(true);
            }
            if (fCurrentPage != null) {
                fCurrentPage.setVisible(false);
            }
            page.setVisible(true);
        }
        setCurrentPage(page);
    }

    protected AbstractDiscoveryPage getCurrentPage() {
        return fCurrentPage;
    }

    protected void setCurrentPage(AbstractDiscoveryPage page) {
        fCurrentPage = page;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.ui.dialogs.AbstractDiscoveryPage#isValid()
     */
    public boolean isValid() {
        return (getCurrentPage() == null) ? true : getCurrentPage().isValid();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
     */
    public String getErrorMessage() {
        return getCurrentPage().getErrorMessage();
    }
    
    protected AbstractDiscoveryPage getDiscoveryProfilePage(String profileId) {
        DiscoveryProfilePageConfiguration configElement = 
                (DiscoveryProfilePageConfiguration) fProfilePageMap.get(profileId);
        if (configElement != null) {
            try {
                return configElement.getPage();
            } catch (CoreException e) {
            }
        }
        return null;
    }

    protected String getDiscoveryProfileName(String profileId) {
        DiscoveryProfilePageConfiguration configElement = 
                (DiscoveryProfilePageConfiguration) fProfilePageMap.get(profileId);
        if (configElement != null) {
            return configElement.getName();
        }
        return null;
    }
    
    protected String getDiscoveryProfileId(String profileName) {
        for (Iterator I = fProfilePageMap.keySet().iterator(); I.hasNext();) {
            String profileId = (String) I.next();
            String confProfileName = getDiscoveryProfileName(profileId);
            if (profileName.equals(confProfileName)) {
                return profileId;
            }
        }
        return null;
    }
    
    protected List getDiscoveryProfileIdList() {
        return new ArrayList(fProfilePageMap.keySet());
    }
    
    protected abstract String getCurrentProfileId();
    
}

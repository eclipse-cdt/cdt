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

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;

/**
 * Fremework for loading profile option pages
 * 
 * @author vhirsl
 */
public abstract class AbstractDiscoveryOptionsBlock extends AbstractDiscoveryPage {
    private Map fProfilePageMap = null;

    // Composite parent provided by the block.
    private Composite fCompositeParent;
    private ICOptionPage fCurrentPage;

    /**
     * Create a profile page only on request
     * 
     * @author vhirsl
     */
    protected static class DiscoveryProfilePageConfiguration {

        ICOptionPage page;
        IConfigurationElement fElement;

        public DiscoveryProfilePageConfiguration(IConfigurationElement element) {
            fElement = element;
        }

        public ICOptionPage getPage() throws CoreException {
            if (page == null) {
                page = (ICOptionPage) fElement.createExecutableExtension("class"); //$NON-NLS-1$
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

    protected Composite getCompositeParent() {
        return fCompositeParent;
    }

    protected void setCompositeParent(Composite parent) {
        fCompositeParent = parent;
//        fCompositeParent.setLayout(new TabFolderLayout());
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
        ICOptionPage page = getDiscoveryProfilePage(profileId);
        if (page != null) {
            if (page.getControl() == null) {
                Composite parent = getCompositeParent();
                page.setContainer(getContainer());
                page.createControl(parent);
                parent.layout(true);
            } else {
                page.setVisible(false);
            }
            page.setVisible(true);
        }
        setCurrentPage(page);
    }

    protected ICOptionPage getCurrentPage() {
        return fCurrentPage;
    }

    protected void setCurrentPage(ICOptionPage page) {
        fCurrentPage = page;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#isValid()
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
    
    protected ICOptionPage getDiscoveryProfilePage(String profileId) {
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

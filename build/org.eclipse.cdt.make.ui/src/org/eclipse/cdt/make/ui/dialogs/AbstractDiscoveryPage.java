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

import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Abstract SCD profile page
 * 
 * @author vhirsl
 */
public abstract class AbstractDiscoveryPage extends DialogPage {
    protected static final String PREFIX = "ScannerConfigOptionsDialog"; //$NON-NLS-1$
    protected static final String PROFILE_GROUP_LABEL = PREFIX + ".profile.group.label"; //$NON-NLS-1$
    
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
    protected void setContainer(AbstractDiscoveryOptionsBlock container) {
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

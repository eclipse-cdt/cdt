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

import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.ui.dialogs.AbstractDiscoveryOptionsBlock;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.ICPropertyProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.widgets.Composite;

/**
 * This class is created to support backward compatibility 
 * with make.ui discovery pages.
 * It is subclassed from AbstractDiscoveryOptionsBlock 
 * just to be passed to existing discovery pages.
 * All methods referenced by these pages are rewritten.
 */
public class DiscoveryPageWrapper extends AbstractDiscoveryOptionsBlock {

	ICPropertyProvider page = null;
	IBuildInfoContainer container = null;
	
	public DiscoveryPageWrapper(ICPropertyProvider _page, IBuildInfoContainer c) {
		super(AbstractCPropertyTab.EMPTY_STR);
		page = _page;
		container = c;
	}
	public IScannerConfigBuilderInfo2 getBuildInfo() {
		return container.getBuildInfo();
	}
	public IProject getProject() { 
		return page.getProject(); 
	}
    public String getErrorMessage() { 
    	return AbstractCPropertyTab.EMPTY_STR; 
    }
    public Preferences getPrefs() {
        return page.getPreferences();
    }
    public boolean isProfileDifferentThenPersisted() { return true; }
    public boolean isInitialized() { return true; } 
    public boolean isValid() { return true; }
	public boolean checkDialogForChanges() { return true; }

	public void callPerformApply() {}
    public void setInitialized(boolean initialized) {}
    public void setContainer(ICOptionContainer container) {}
	public void updateContainer() {}
	public void updatePersistedProfile() {}
    public void setVisible(boolean visible) {}
	
	protected String getCurrentProfileId() { return null; }
	public void createControl(Composite parent) {}
	public void performApply(IProgressMonitor monitor) throws CoreException {}
	public void performDefaults() {}
}

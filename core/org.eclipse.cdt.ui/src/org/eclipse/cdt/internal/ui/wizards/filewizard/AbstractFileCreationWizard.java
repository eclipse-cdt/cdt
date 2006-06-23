/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.filewizard;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public abstract class AbstractFileCreationWizard extends NewElementWizard {
    
    protected AbstractFileCreationWizardPage fPage = null;
    
    public AbstractFileCreationWizard() {
        super();
        setDefaultPageImageDescriptor(CPluginImages.DESC_WIZBAN_NEW_FILE);
        setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
        setWindowTitle(NewFileWizardMessages.getString("AbstractFileCreationWizard.title")); //$NON-NLS-1$
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.ui.wizards.NewElementWizard#canRunForked()
     */
    protected boolean canRunForked() {
    	return true;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.ui.wizards.NewElementWizard#finishPage(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
        fPage.createFile(monitor); // use the full progress monitor
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */
    public boolean performFinish() {
        boolean res = super.performFinish();
        if (res) {
            //TODO need prefs option for opening editor
            boolean openInEditor = true;
            
			ITranslationUnit headerTU = fPage.getCreatedFileTU();
			if (headerTU != null) {
				IResource resource= headerTU.getResource();
				selectAndReveal(resource);
				if (openInEditor) {
					openResource((IFile) resource);
				}
			}
        }
        return res;
    }
}

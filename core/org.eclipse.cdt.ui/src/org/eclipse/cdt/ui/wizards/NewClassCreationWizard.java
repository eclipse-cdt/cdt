/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.cdt.internal.ui.wizards.classwizard.NewClassCreationWizardPage;
import org.eclipse.cdt.internal.ui.wizards.classwizard.NewClassWizardMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class NewClassCreationWizard extends NewElementWizard {
    
    private NewClassCreationWizardPage fPage;
    
    public NewClassCreationWizard() {
        super();
        setDefaultPageImageDescriptor(CPluginImages.DESC_WIZBAN_NEWCLASS);
        setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
        setWindowTitle(NewClassWizardMessages.getString("NewClassCreationWizard.title")); //$NON-NLS-1$
    }
    
    /*
     * @see Wizard#createPages
     */
    public void addPages() {
        super.addPages();
        fPage = new NewClassCreationWizardPage();
        addPage(fPage);
        fPage.init(getSelection());
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.ui.wizards.NewElementWizard#canRunForked()
     */
    protected boolean canRunForked() {
        return !fPage.isEnclosingTypeSelected();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.ui.wizards.NewElementWizard#finishPage(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
        fPage.createClass(monitor); // use the full progress monitor
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
            
			ITranslationUnit bodyTU = fPage.getCreatedSourceTU();
			if (bodyTU != null) {
				IResource resource= bodyTU.getResource();
				selectAndReveal(resource);
				if (openInEditor) {
					openResource((IFile) resource);
				}
			}
			ITranslationUnit headerTU = fPage.getCreatedHeaderTU();
			if (headerTU != null) {
				IResource resource = headerTU.getResource();
				selectAndReveal(resource);
				if (openInEditor) {
					openResource((IFile) resource);
				}
			}
        }
        return res;
    }
}

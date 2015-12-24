/*******************************************************************************
 * Copyright (c) 2004, 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.NewClassCreationWizardPage;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.wizards.classwizard.NewClassWizardMessages;

public class NewClassCreationWizard extends NewElementWizard {
    private NewClassCreationWizardPage fPage;
    
    public NewClassCreationWizard() {
        super();
        setDefaultPageImageDescriptor(CPluginImages.DESC_WIZBAN_NEWCLASS);
        setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
        setWindowTitle(NewClassWizardMessages.NewClassCreationWizard_title); 
    }
    
    /*
     * @see Wizard#createPages
     */
    @Override
	public void addPages() {
        super.addPages();
        fPage = new NewClassCreationWizardPage();
        addPage(fPage);
        fPage.init(getSelection());
    }
    
    @Override
	protected boolean canRunForked() {
        return !fPage.isNamespaceSelected();
    }
    
    @Override
	protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
        fPage.createClass(monitor); // use the full progress monitor
    }
    
    @Override
	public boolean performFinish() {
        boolean finished = super.performFinish();
        if (finished) {
            if (fPage.openClassInEditor()) {
                IFile source = fPage.getCreatedSourceFile();
                if (source != null) {
                    selectAndReveal(source);
                    openResource(source);
                }
                IFile header = fPage.getCreatedHeaderFile();
                if (header != null) {
                    selectAndReveal(header);
                    openResource(header);
                }
            }
        }
        return finished;
    }
}

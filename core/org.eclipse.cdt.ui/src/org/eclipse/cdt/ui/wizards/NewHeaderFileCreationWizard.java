/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.wizards.filewizard.AbstractFileCreationWizard;
import org.eclipse.cdt.internal.ui.wizards.filewizard.NewFileWizardMessages;
import org.eclipse.cdt.internal.ui.wizards.filewizard.NewHeaderFileCreationWizardPage;
import org.eclipse.cdt.ui.CUIPlugin;

public class NewHeaderFileCreationWizard extends AbstractFileCreationWizard {
    
    public NewHeaderFileCreationWizard() {
        super();
        setDefaultPageImageDescriptor(CPluginImages.DESC_WIZBAN_NEW_HEADERFILE);
        setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
        setWindowTitle(NewFileWizardMessages.getString("NewHeaderFileCreationWizard.title")); //$NON-NLS-1$
    }
    
    /*
     * @see Wizard#createPages
     */
    public void addPages() {
        super.addPages();
        fPage = new NewHeaderFileCreationWizardPage();
        addPage(fPage);
        fPage.init(getSelection());
    }
}

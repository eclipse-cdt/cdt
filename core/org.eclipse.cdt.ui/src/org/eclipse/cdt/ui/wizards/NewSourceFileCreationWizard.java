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
package org.eclipse.cdt.ui.wizards;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.wizards.filewizard.AbstractFileCreationWizard;
import org.eclipse.cdt.internal.ui.wizards.filewizard.NewFileWizardMessages;
import org.eclipse.cdt.internal.ui.wizards.filewizard.NewSourceFileCreationWizardPage;
import org.eclipse.cdt.ui.CUIPlugin;

public class NewSourceFileCreationWizard extends AbstractFileCreationWizard {
    
    public NewSourceFileCreationWizard() {
        super();
        setDefaultPageImageDescriptor(CPluginImages.DESC_WIZBAN_NEW_SOURCEFILE);
        setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
        setWindowTitle(NewFileWizardMessages.getString("NewSourceFileCreationWizard.title")); //$NON-NLS-1$
    }
    
    /*
     * @see Wizard#createPages
     */
    public void addPages() {
        super.addPages();
        fPage = new NewSourceFileCreationWizardPage();
        addPage(fPage);
        fPage.init(getSelection());
    }
}

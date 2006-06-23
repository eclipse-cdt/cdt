/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.browser.typeinfo.TypeSelectionDialog;
import org.eclipse.swt.widgets.Shell;

public class EnclosingClassSelectionDialog extends TypeSelectionDialog {
    
    private static final String DIALOG_SETTINGS = EnclosingClassSelectionDialog.class.getName();
    private static final int[] VISIBLE_TYPES = { ICElement.C_CLASS };
    
    public EnclosingClassSelectionDialog(Shell parent) {
        super(parent);
        setTitle(NewClassWizardMessages.getString("EnclosingClassSelectionDialog.title")); //$NON-NLS-1$
        setMessage(NewClassWizardMessages.getString("EnclosingClassSelectionDialog.message")); //$NON-NLS-1$
        setDialogSettings(DIALOG_SETTINGS);
        setVisibleTypes(VISIBLE_TYPES);
        setFilter("*", true); //$NON-NLS-1$
    }
}

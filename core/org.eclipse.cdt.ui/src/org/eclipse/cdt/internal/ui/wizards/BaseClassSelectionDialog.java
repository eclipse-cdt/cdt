/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - adapted for use in CDT
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.browser.typeinfo.TypeSelectionDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * A dialog to select a type from a list of types. The selected type will be
 * opened in the editor.
 * 
 * @deprecated use NewBaseClassSelectionDialog instead
 */
public class BaseClassSelectionDialog extends TypeSelectionDialog {

		 private static final String DIALOG_SETTINGS= BaseClassSelectionDialog.class.getName();
		 private static final int[] fVisibleTypes= {ICElement.C_CLASS, ICElement.C_STRUCT};

		 /**
		  * Constructs an instance of <code>OpenTypeDialog</code>.
		  * @param parent  the parent shell.
		  */
		 public BaseClassSelectionDialog(Shell parent) {
		 		 super(parent);
		 		 setTitle(NewWizardMessages.getString("BaseClassSelectionDialog.title")); //$NON-NLS-1$
		 		 setMessage(NewWizardMessages.getString("BaseClassSelectionDialog.message")); //$NON-NLS-1$
		 		 setDialogSettings(DIALOG_SETTINGS);
		 		 setVisibleTypes(fVisibleTypes);
		 		 setFilter("*", true); //$NON-NLS-1$		 		 
		 }
}

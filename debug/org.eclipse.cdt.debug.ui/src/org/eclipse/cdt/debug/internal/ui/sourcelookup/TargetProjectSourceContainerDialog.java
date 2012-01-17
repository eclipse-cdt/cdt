/*******************************************************************************
 * Copyright (c) 2010 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	  Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup;

import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * The dialog for configuring a source container for a project associated with the launch target.
 */
public class TargetProjectSourceContainerDialog extends SelectionDialog {
	private boolean fAddReferencedProjects;

    public TargetProjectSourceContainerDialog(Shell parentShell) {
        super(parentShell);
        setTitle(SourceLookupUIMessages.TargetProjectSourceContainerDialog_title);
        setMessage(SourceLookupUIMessages.TargetProjectSourceContainerDialog_description);
    }

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Font font = parent.getFont();
		
        // Page group
        Composite composite = (Composite) super.createDialogArea(parent);
        initializeDialogUnits(composite);

        createMessageArea(composite);

		final Button addRequired = new Button(composite, SWT.CHECK);
		addRequired.setText(SourceLookupUIMessages.TargetProjectSourceContainerDialog_referencedLabel);  
		addRequired.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fAddReferencedProjects = addRequired.getSelection();
			}
		});
		addRequired.setSelection(fAddReferencedProjects);
		addRequired.setFont(font);		
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(),
				ICDebugHelpContextIds.ADD_TARGET_PROJECT_CONTAINER_DIALOG);
        applyDialogFont(composite);
		return composite;
	}

	/**
	 * Returns whether the user has selected to add referenced projects.
	 * 
	 * @return whether the user has selected to add referenced projects
	 */
	public boolean isAddReferencedProjects() {
		return fAddReferencedProjects;
	}
}

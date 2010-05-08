/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.dialogs;

import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog that allows to customise problems
 * 
 */
public class CustomizeProblemDialog extends TitleAreaDialog {
	private CustomizeProblemComposite comp;
	private IProblem problem;

	/**
	 * @param parentShell
	 * @param selectedProblem
	 */
	public CustomizeProblemDialog(Shell parentShell, IProblem selectedProblem) {
		super(parentShell);
		this.problem = selectedProblem;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/**
	 * Stores edit values into problem working copy
	 * @param problem - problem working copy
	 */
	public void save(IProblemWorkingCopy problem) {
		comp.save(problem);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(CodanUIMessages.CustomizeProblemDialog_Title);
		setTitle(problem.getName());
		setMessage(CodanUIMessages.CustomizeProblemDialog_Message);
		Composite area = (Composite) super.createDialogArea(parent);
		comp = new CustomizeProblemComposite(area, problem);
		GridData ld = new GridData(GridData.FILL_BOTH);
		ld.minimumHeight=300;
		comp.setLayoutData(ld);
		return area;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		save((IProblemWorkingCopy) problem);
		super.okPressed();
	}
}

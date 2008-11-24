/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import org.eclipse.cdt.internal.ui.refactoring.NameNVisibilityInformation;

/**
 * The wizard page for Extract Local Variable Refactoring, creates the UI page.
 * 
 * @author Tom Ball
 */
public class ExtractLocalVariableRefactoringWizard extends RefactoringWizard {
	private InputPage page;
	private final NameNVisibilityInformation info;

	public ExtractLocalVariableRefactoringWizard(Refactoring refactoring,
			NameNVisibilityInformation info) {
		super(refactoring, WIZARD_BASED_USER_INTERFACE);
		this.info = info;
	}

	@Override
	protected void addUserInputPages() {
		page = new InputPage(Messages.ExtractLocalVariable, info);
		addPage(page);
	}
}

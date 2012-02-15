/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Tom Ball (Google) - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * The wizard page for Extract Local Variable Refactoring, creates the UI page.
 * 
 * @author Tom Ball
 */
public class ExtractLocalVariableRefactoringWizard extends RefactoringWizard {
	private InputPage page;

	public ExtractLocalVariableRefactoringWizard(ExtractLocalVariableRefactoring refactoring) {
		super(refactoring, WIZARD_BASED_USER_INTERFACE);
	}

	@Override
	protected void addUserInputPages() {
		ExtractLocalVariableRefactoring refactoring = (ExtractLocalVariableRefactoring) getRefactoring();
		page = new InputPage(Messages.ExtractLocalVariable, refactoring.getRefactoringInfo());
		addPage(page);
	}
}

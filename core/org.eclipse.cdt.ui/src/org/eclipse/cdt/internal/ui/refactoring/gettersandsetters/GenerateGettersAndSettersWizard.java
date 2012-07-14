/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

/**
 * @author Thomas Corbat
 */
public class GenerateGettersAndSettersWizard extends RefactoringWizard {
	public GenerateGettersAndSettersWizard(
			GenerateGettersAndSettersRefactoring refactoring) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(Messages.GenerateGettersAndSettersInputPage_Name);
	}

	@Override
	protected void addUserInputPages() {
		UserInputWizardPage page = new GenerateGettersAndSettersInputPage(
				((GenerateGettersAndSettersRefactoring) getRefactoring()).getContext());
		addPage(page);
	}
}

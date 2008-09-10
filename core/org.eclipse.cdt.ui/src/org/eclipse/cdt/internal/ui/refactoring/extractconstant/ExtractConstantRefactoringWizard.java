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
package org.eclipse.cdt.internal.ui.refactoring.extractconstant;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import org.eclipse.cdt.internal.ui.refactoring.MethodContext.ContextType;
import org.eclipse.cdt.internal.ui.refactoring.dialogs.ExtractInputPage;

/**
 * The wizard page for Extract Constant Refactoring, creates the UI page.
 */
public class ExtractConstantRefactoringWizard extends RefactoringWizard {

	private ExtractInputPage page;
	private final ExtractConstantInfo info;

	public ExtractConstantRefactoringWizard(Refactoring refactoring, ExtractConstantInfo info) {
		super(refactoring, WIZARD_BASED_USER_INTERFACE);
		this.info = info;
	}

	@Override
	protected void addUserInputPages() {
		page = new InputPage(Messages.ExtractConstantRefactoring_ExtractConst, info, info.getMContext().getType() == ContextType.METHOD); 
		addPage(page);

	}
}

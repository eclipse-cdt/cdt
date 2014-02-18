/*******************************************************************************
 * Copyright (c) 2013 Simon Taddiken
 * University of Bremen.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Simon Taddiken (University of Bremen)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.pullup;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescriptor;

public class PushDownRefactoringDescriptor extends CRefactoringDescriptor {

	public PushDownRefactoringDescriptor(String project, String description,
			String comment, Map<String, String> arguments) {
		super(PushDownRefactoring.ID, project, description, comment, 
				RefactoringDescriptor.MULTI_CHANGE, arguments);
	}

	
	
	@Override
	public CRefactoring createRefactoring(RefactoringStatus status) throws CoreException {
		final CRefactoring refactoring = new PushDownRefactoring(this.getTranslationUnit(), 
				this.getSelection(), this.getCProject());
		return refactoring;
	}
}

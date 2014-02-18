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
package org.eclipse.cdt.internal.ui.refactoring.inlinetemp;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescriptor;

public class InlineTempRefactoringDescriptor extends CRefactoringDescriptor {

	public InlineTempRefactoringDescriptor( String project, String description, 
			String comment, int flags, Map<String, String> arguments) {
		super(InlineTempRefactoring.ID, project, description, comment, 
				MULTI_CHANGE, arguments);
	}

	
	
	@Override
	public CRefactoring createRefactoring(RefactoringStatus status) throws CoreException {
		InlineTempRefactoring ref = new InlineTempRefactoring(
				this.getTranslationUnit(), this.getSelection(), this.getCProject());
		return ref;
	}
}

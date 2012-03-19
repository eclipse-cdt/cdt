/*******************************************************************************
 * Copyright (c) 2009, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Institute for Software (IFS)- initial API and implementation
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescriptor;

/**
 * @author Emanuel Graf IFS
 */
public class ExtractLocalVariableRefactoringDescriptor extends CRefactoringDescriptor {
	static protected final String NAME = "name";  //$NON-NLS-1$

	public ExtractLocalVariableRefactoringDescriptor(String project, String description,
			String comment, Map<String, String> arguments) {
		super(ExtractLocalVariableRefactoring.ID, project, description, comment,
				RefactoringDescriptor.MULTI_CHANGE, arguments);
	}

	@Override
	public CRefactoring createRefactoring(RefactoringStatus status) throws CoreException {
		ISelection selection = getSelection();
		ICProject proj = getCProject();
		ExtractLocalVariableRefactoring refactoring =
				new ExtractLocalVariableRefactoring(getTranslationUnit(), selection, proj);
		refactoring.getRefactoringInfo().setName(arguments.get(NAME));
		return refactoring;
	}
}

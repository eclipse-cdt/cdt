/*******************************************************************************
 * Copyright (c) 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.hidemethod;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescription;

/**
 * @author Emanuel Graf IFS
 *
 */
public class HideMethodRefactoringDescription extends CRefactoringDescription {

	public HideMethodRefactoringDescription(String project, String description, String comment,
			Map<String, String> arguments) {
		super(HideMethodRefactoring.ID, project, description, comment, RefactoringDescriptor.STRUCTURAL_CHANGE, arguments);
	}

	@Override
	public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
		IFile file;
		ICProject proj;
		
		proj = getCProject();
		file = getFile();
		ISelection selection = getSelection();
		return new HideMethodRefactoring(file, selection, null, proj);
	}

}

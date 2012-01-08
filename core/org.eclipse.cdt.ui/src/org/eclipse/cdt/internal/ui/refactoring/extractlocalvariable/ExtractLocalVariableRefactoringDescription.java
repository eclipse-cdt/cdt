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
package org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescription;
import org.eclipse.cdt.internal.ui.refactoring.NameNVisibilityInformation;

/**
 * @author Emanuel Graf IFS
 *
 */
public class ExtractLocalVariableRefactoringDescription extends CRefactoringDescription {
	
	static protected final String NAME = "name";  //$NON-NLS-1$

	public ExtractLocalVariableRefactoringDescription(String project, String description,
			String comment, Map<String, String> arguments) {
		super(ExtractLocalVariableRefactoring.ID, project, description, comment,
				RefactoringDescriptor.MULTI_CHANGE, arguments);
	}

	@Override
	public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
		IFile file;
		NameNVisibilityInformation info = new NameNVisibilityInformation();
		ICProject proj;
		
		info.setName(arguments.get(NAME));
		
		proj = getCProject();
		
		file = getFile();
		
		ISelection selection = getSelection();
		return new ExtractLocalVariableRefactoring(file, selection, info, proj);
	}
}

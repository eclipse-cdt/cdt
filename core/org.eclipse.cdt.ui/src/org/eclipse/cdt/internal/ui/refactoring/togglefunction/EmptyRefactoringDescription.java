/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * 		Martin Schwab & Thomas Kallenberg - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescription;

class EmptyRefactoringDescription extends CRefactoringDescription {
	@SuppressWarnings("nls")
	public EmptyRefactoringDescription() {
		super("id", "proj", "desc", "comment", 0, new HashMap<String, String>());
	}

	@Override
	public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
		return new ToggleRefactoring(getFile(), (TextSelection)getSelection(), getCProject());
	}
}
/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.internal.corext.refactoring.base.*;


public interface IRenameProcessor extends IRefactoringProcessor {

	public String getCurrentElementName();
	
	public RefactoringStatus checkNewElementName(String newName) throws CoreException;
	
	public void setNewElementName(String newName);
	
	public String getNewElementName();
	
}

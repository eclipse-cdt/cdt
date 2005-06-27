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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.corext.refactoring.base.*;

/**
 * Represents a refactoring that renames an <code>ICElement</code>.
 */
public interface IRenameRefactoring extends IRefactoring {
	
	/**
	 * Sets new name for the entity that this refactoring is working on.
	 */
	public void setNewName(String newName);
	
	/**
	 * Get the name for the entity that this refactoring is working on.
	 */
	public String getNewName();

	/**
	 * Gets the current name of the entity that this refactoring is working on.
	 */
	public String getCurrentName();
	
	/**
	 * Checks if the new name is valid for the entity that this refactoring renames.
	 */
	public RefactoringStatus checkNewName(String newName) throws CModelException;
}

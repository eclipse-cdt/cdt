/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/ 

package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.core.runtime.CoreException;


/**
 * Interface for all the names in the index. These constitute either a
 * declaration or a reference.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @since 4.0
 */
public interface IIndexName extends IName {
	/**
	 * Returns the file the name belongs to.
	 * @throws CoreException 
	 */
	public IIndexFile getFile() throws CoreException;
	
	/**
	 * Returns the character offset of the location of the name.
	 */
	public int getNodeOffset();

	/**
	 * Returns the length of the name.
	 */
	public int getNodeLength();
	
	/**
	 * Returns the name of the definition that contains this name. 
	 * May return <code>null</code>.
	 * Currently this is implemented for function and method definitions, only.
	 */
	public IIndexName getEnclosingDefinition() throws CoreException;
	
	/**
	 * Returns the names of the references contained in this definition. 
	 * Returns <code>null</code>, if the name is not a definition.
	 * 
	 * Currently the method works with function definitions, only.
	 */
	public IIndexName[] getEnclosedNames() throws CoreException;

	/**
	 * Returns whether a declaration is a base-class specifier.
	 */
	public boolean isBaseSpecifier() throws CoreException;

}

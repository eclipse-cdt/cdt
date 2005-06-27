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


import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;

/**
 * A data structure describing the resource modification resulting from 
 * applying a certain refactoring.
 * 
 * @since 3.0
 */
public interface IResourceModifications {
	
	/**
	 * Returns the list of resources to be created.
	 * 
	 * @return the list of resources to be created
	 */
	public List getCreate();

	/**
	 * Returns the list of resources to be deleted.
	 * 
	 * @return the list of resources to be deleted
	 */
	public List getDelete();

	/**
	 * Returns the list of resources to be copied.
	 * 
	 * @return the list of resources to be copied
	 */
	public List getCopy();

	/**
	 * Returns the copy target.
	 * 
	 * @return the copy target
	 */
	public IContainer getCopyTarget();

	/**
	 * Returns the list of resources to be moved.
	 * 
	 * @return the list of resources to be moved
	 */
	public List getMove();

	/**
	 * Returns the move target
	 * 
	 * @return the move target
	 */
	public IContainer getMoveTarget();

	/**
	 * Returns the resource to be renamed
	 * 
	 * @return the resourcr to be renamed
	 */
	public IResource getRename();

	/**
	 * Returns the new name of the resource to be renamed
	 * 
	 * @return the new resource name
	 */
	public String getNewName();
	
	/**
	 * Returns an array of participants that want to participate
	 * in the resource modifications described by this data
	 * structure.
	 * 
	 * @param processor the main processor of the overall refactoring
	 * @return an array of participants
	 */
//	public IRefactoringParticipant[] getParticipants(IRefactoringProcessor processor, SharableParticipants shared) throws CoreException;

}

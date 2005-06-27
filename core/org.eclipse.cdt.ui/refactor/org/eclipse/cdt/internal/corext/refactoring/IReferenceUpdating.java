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

public interface IReferenceUpdating {

	/**
	 * Checks if this refactoring object is capable of updating references to the renamed element.
	 */
	public boolean canEnableUpdateReferences();

	/**
	 * If <code>canUpdateReferences</code> returns <code>true</code>, then this method is used to
	 * inform the refactoring object whether references should be updated.
	 * This call can be ignored if  <code>canUpdateReferences</code> returns <code>false</code>.
	 */	
	public void setUpdateReferences(boolean update);

	/**
	 * If <code>canUpdateReferences</code> returns <code>true</code>, then this method is used to
	 * ask the refactoring object whether references should be updated.
	 * This call can be ignored if  <code>canUpdateReferences</code> returns <code>false</code>.
	 */		
	public boolean getUpdateReferences();

}


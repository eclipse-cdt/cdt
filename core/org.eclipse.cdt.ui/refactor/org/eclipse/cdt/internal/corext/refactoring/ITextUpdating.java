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



public interface ITextUpdating {

	/**
	 * Performs a dynamic check whether this refactoring object is capable of updating references to the renamed element.
	 */
	public boolean canEnableTextUpdating();
	
	/**
	 * If <code>canEnableTextUpdating</code> returns <code>true</code>, then this method is used to
	 * ask the refactoring object whether references in JavaDoc comments should be updated.
	 * This call can be ignored if  <code>canEnableTextUpdating</code> returns <code>false</code>.
	 */		
	public boolean getUpdateJavaDoc();

	/**
	 * If <code>canEnableTextUpdating</code> returns <code>true</code>, then this method is used to
	 * ask the refactoring object whether references in regular (non JavaDoc) comments should be updated.
	 * This call can be ignored if  <code>canEnableTextUpdating</code> returns <code>false</code>.
	 */			
	public boolean getUpdateComments();
	
	/**
	 * If <code>canEnableTextUpdating</code> returns <code>true</code>, then this method is used to
	 * ask the refactoring object whether references in string literals should be updated.
	 * This call can be ignored if  <code>canEnableTextUpdating</code> returns <code>false</code>.
	 */		
	public boolean getUpdateStrings();
	
	/**
	 * If <code>canEnableTextUpdating</code> returns <code>true</code>, then this method is used to
	 * inform the refactoring object whether references in JavaDoc comments should be updated.
	 * This call can be ignored if  <code>canEnableTextUpdating</code> returns <code>false</code>.
	 */	
	public void setUpdateJavaDoc(boolean update);

	/**
	 * If <code>canEnableTextUpdating</code> returns <code>true</code>, then this method is used to
	 * inform the refactoring object whether references in regular (non JavaDoc)  comments should be updated.
	 * This call can be ignored if  <code>canEnableTextUpdating</code> returns <code>false</code>.
	 */		
	public void setUpdateComments(boolean update);
	
	/**
	 * If <code>canEnableTextUpdating</code> returns <code>true</code>, then this method is used to
	 * inform the refactoring object whether references in string literals should be updated.
	 * This call can be ignored if  <code>canEnableTextUpdating</code> returns <code>false</code>.
	 */	
	public void setUpdateStrings(boolean update);
	
	/**
	 * Returns the current name of the element to be renamed.
	 * 
	 * @return the current name of the element to be renamed
	 */
	public String getCurrentElementName();
	
	/**
	 * Returns the new name of the element
	 * 
	 * @return the new element name
	 */
	public String getNewElementName();
}




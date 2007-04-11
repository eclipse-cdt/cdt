package org.eclipse.rse.internal.useractions.ui;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/**
 * Objects implementing this interface are passed to 
 * {@link SystemCmdSubstVarList} parse a given command 
 * string for variables defined in the substitution list. 
 * For each match, this object is called back to retrieve 
 * the substition value, given the variable name (including 
 * its prefix). It will also pass back the context object 
 * given to it. Presumably this is a currently selected object.
 */
public interface ISystemSubstitutor {
	/**
	 * Return the string to substitute for the given substitution
	 *  variable, given the current context object. This object will
	 *  be passed whatever was passed into the doSubstitution method.
	 * <p>It is VERY IMPORTANT to return null if you can't do the 
	 * substitution for some reason! This is a clue to the algorithm
	 * that no change was made and increases performance.
	 */
	public String getSubstitutionValue(String substitutionVariable, Object context);
}

/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.core.dom;

import java.util.LinkedList;
import java.util.List;

public class SimpleDeclaration extends Declaration {

	// List of DeclSpecifier
	private List declSpecifiers = new LinkedList();
	
	/**
	 * Returns the declSpecifiers.
	 * @return List
	 */
	public List getDeclSpecifiers() {
		return declSpecifiers;
	}

	// List of Declarators
	private List declarators = new LinkedList();
	
	/**
	 * Returns the declarators.
	 * @return List
	 */
	public List getDeclarators() {
		return declarators;
	}

}

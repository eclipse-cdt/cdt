/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This is the general purpose exception that is thrown for resolving semantic
 * aspects of an illegal binding.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class DOMException extends Exception {
	private static final long serialVersionUID = 0;
	
	IProblemBinding problemBinding;

	/**
	 * @param problem the binding for throwing
	 */
	public DOMException(IProblemBinding problem) {
		problemBinding = problem;
	}

	/**
	 * Returns the problem associated w/this exception.
	 * 
	 * @return problem
	 */
	public IProblemBinding getProblem() {
		return problemBinding;
	}
}

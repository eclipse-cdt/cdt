/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;

/**
 * C Enumeration decl specifier. Allows for "restrict enum X { a, b, c };
 * 
 * @author jcamelon
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICASTEnumerationSpecifier extends ICASTDeclSpecifier,
		IASTEnumerationSpecifier {

	/**
	 * @since 5.1
	 */
	public ICASTEnumerationSpecifier copy();
}

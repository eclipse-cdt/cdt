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

import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;

/**
 * C's elaborated type specifier. (same as IASTElaboratedTypeSpecifier, except
 * for the addition of the restrict keyword.
 * 
 * @author jcamelon
 */
public interface ICASTElaboratedTypeSpecifier extends
		IASTElaboratedTypeSpecifier, ICASTDeclSpecifier {

}

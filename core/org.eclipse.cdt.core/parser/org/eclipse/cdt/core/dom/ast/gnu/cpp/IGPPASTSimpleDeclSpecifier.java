/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;

/**
 * @author jcamelon
 */
public interface IGPPASTSimpleDeclSpecifier extends IGPPASTDeclSpecifier,
        ICPPASTSimpleDeclSpecifier {

	public static final int t_Complex = ICPPASTSimpleDeclSpecifier.t_last + 1;
	public static final int t_Imaginary = ICPPASTSimpleDeclSpecifier.t_last + 2;
	public static final int t_last = t_Imaginary;

}

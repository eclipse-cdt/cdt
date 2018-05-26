/*******************************************************************************
 * Copyright (c) 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Hansruedi Patzen (IFS) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;

/**
 * Capture for a lambda expression, introduced in C++0x.
 * 
 * @since 6.5
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTInitCapture extends ICPPASTCapture, IASTDeclaration {
	ASTNodeProperty IDENTIFIER = new ASTNodeProperty("ICPPASTInitCapture - IDENTIFIER [IASTName]"); //$NON-NLS-1$

	@Override
	ICPPASTInitCapture copy();

	@Override
	ICPPASTInitCapture copy(CopyStyle style);

	/**
	 * @since 6.5
	 */
	IASTDeclarator getDeclarator();

	/**
	 * @since 6.5
	 */
	void setDeclarator(IASTDeclarator declarator);
}

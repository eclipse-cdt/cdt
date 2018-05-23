/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/


package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator.RefQualifier;

/**
 * @since 6.5
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTStructuredBindingDeclaration extends IASTSimpleDeclaration, IASTNameOwner {
	/**
	 * <code>IDENTIFIER</code> represents the relationship between an
	 * <code>ICPPASTStructuredBindingDeclaration</code> and its
	 * <code>IASTName</code>s.
	 */
	public static final ASTNodeProperty IDENTIFIER = new ASTNodeProperty(
			"ICPPASTStructuredBindingDeclaration.IDENTIFIER - IASTName for ICPPASTStructuredBindingDeclaration"); //$NON-NLS-1$

	
	RefQualifier getRefQualifier(); //TODO: Doc
	IASTName[] getNames(); //TODO: Doc
	IASTInitializer getInitializer(); //TODO: Doc
}

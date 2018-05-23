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
 * This is a structured binding declaration which contains a sequence names,
 * in square brackets, that decompose an initializer.
 * <p>
 * Example: <code>auto & [x, y] = coordinate;</code>
 * 
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

	/**
	 * <code>INITIALIZER</code> represents the relationship between an
	 * <code>ICPPASTStructuredBindingDeclaration</code> and its <code>IASTInitializer</code>.
	 */
	public static final ASTNodeProperty INITIALIZER = new ASTNodeProperty(
			"ICPPASTStructuredBindingDeclaration.INITIALIZER - IASTInitializer for ICPPASTStructuredBindingDeclaration"); //$NON-NLS-1$

	/**
	 * Returns the <code>RefQualifier</code> of the structured binding. For either lvalue or
	 * rvalue reference qualifiers.
	 * 
	 * @return The <code>RefQualifier</code> or <code>null</code> if the structured binding
	 * does not have a reference qualifier.
	 * @see RefQualifier
	 */
	RefQualifier getRefQualifier();

	/**
	 * Returns the list of names declared by this structured binding declaration.
	 * 
	 * @return All declared names of the structured binding as<code>IASTName[]</code>
	 * @see IASTName
	 */
	IASTName[] getNames();

	/**
	 * Returns the initializer of the structured binding declaration.
	 * 
	 * @return The <code>IASTInitializer</code> of this structured binding.
	 * @see IASTInitializer
	 */
	IASTInitializer getInitializer();
}

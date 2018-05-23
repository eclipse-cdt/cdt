/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.dom.ast.cpp;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator.RefQualifier;

/**
 * This is a structured binding declaration which contains a sequence names,
 * in square brackets, that decompose an initializer.
 * <p>
 * Examples:
 * <ul>
 * <li><code>auto [x, y]{coordinate};</code></li>
 * <li><code>auto & [x, y](coordinate);</code></li>
 * <li><code>auto && [x, y] = createCoordinte();</code></li>
 * </ul>
 *
 * @since 6.8
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTStructuredBindingDeclaration
		extends IASTSimpleDeclaration, IASTNameOwner, IASTImplicitNameOwner {
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
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>For <code>auto [x, y] = coordinate;</code> it returns the empty <code>Optional</code></li>
	 * <li>For <code>auto & [x, y] = coordinate;</code> it returns <code>Optional.of(RefQualifier.LVALUE)</code></li>
	 * <li>For <code>auto && [x, y] = createCoordinte();</code> it returns <code>Optional.of(RefQualifier.RVALUE)</code></li>
	 * </ul>
	 *
	 * @return The returned <code>Optional</code> contains the <code>RefQualifier</code> or the empty <code>Optional</code>
	 * if the structured binding does not have a reference qualifier.
	 * @see RefQualifier
	 * @see Optional
	 */
	Optional<RefQualifier> getRefQualifier();

	/**
	 * Returns the list of names declared by this structured binding declaration.
	 * <p>
	 * Example: For <code>auto & [x, y] = coordinate;</code> it returns the names <code>x</code> and <code>y</code>.
	 *
	 * @return All declared names of the structured binding as<code>IASTName[]</code>
	 * @see IASTName
	 */
	IASTName[] getNames();

	/**
	 * Returns the initializer of the structured binding declaration.
	 *
	 * This will not be present if the structured binding is part of a range-based for loop.
	 *
	 *  * Examples:
	 * <ul>
	 * <li>For <code>auto [x, y]{coordinate};</code> it returns an <code>ICPPASTInitializerList</code></li>
	 * <li>For <code>auto & [x, y](coordinate);</code> it returns an <code>ICPPASTConstructorInitializer</code></li>
	 * <li>For <code>auto && [x, y] = createCoordinte();</code> it returns an <code>IASTEqualsInitializer</code></li>
	 * </ul>
	 *
	 * @return The <code>Optional</code> <code>IASTInitializer</code> of this structured binding.
	 * @see IASTInitializer
	 * @see ICPPASTInitializerList
	 * @see ICPPASTConstructorInitializer
	 * @see IASTEqualsInitializer
	 */
	Optional<IASTInitializer> getInitializer();
}

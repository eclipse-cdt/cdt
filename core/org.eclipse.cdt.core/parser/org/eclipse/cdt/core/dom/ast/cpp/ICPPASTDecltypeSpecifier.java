/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

/**
 * C++ AST node for decltype-specifiers.
 *
 * Currently, this class is only used to represent decltype-specifiers
 * in qualified names, not in decl-specifiers (in decl-specifiers,
 * a decltype-specifier is represented as an ICPPASTSimpleDeclSpecifier).
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 5.6
 */
public interface ICPPASTDecltypeSpecifier extends ICPPASTNameSpecifier {
	ICPPASTExpression getDecltypeExpression();
}

/*******************************************************************************
 * Copyright (c) 2012, 2014 Institute for Software, HSR Hochschule fuer Technik
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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTAttributeOwner;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;

/**
 * Represents a C++ alias declaration.
 * e.g. struct Type {}; using Alias = Type;
 * @since 5.5
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTAliasDeclaration extends IASTDeclaration, IASTNameOwner, IASTAttributeOwner {
	public static final ICPPASTAliasDeclaration[] EMPTY_ALIAS_DECLARATION_ARRAY = {};

	/**
	 * <code>ALIAS_NAME</code> is the name that is brought into the local scope.
	 */
	public static final ASTNodeProperty ALIAS_NAME = new ASTNodeProperty(
			"ICPPASTAliasDeclaration.ALIAS_NAME - Introduced alias name"); //$NON-NLS-1$

	/**
	 * <code>MAPPING_TYPE<ID/code> represents the pre-existing type id which
	 * the new symbol aliases.
	 */
	public static final ASTNodeProperty TARGET_TYPEID = new ASTNodeProperty(
			"ICPPASTAliasDeclaration.TARGET_TYPEID - Pre-existing type ID the new symbol aliases"); //$NON-NLS-1$

	/**
	 * Returns the alias name.
	 *
	 * @return <code>IASTName</code>
	 */
	public IASTName getAlias();

	/**
	 * Sets the alias name.
	 *
	 * @param aliasName <code>IASTName</code>
	 */
	public void setAlias(IASTName aliasName);

	/**
	 * Returns the mapping type id.
	 *
	 * @return <code>ICPPASTTypeId</code>
	 */
	public ICPPASTTypeId getMappingTypeId();

	/**
	 * Sets the mapping type id.
	 *
	 * @param mappingTypeId <code>ICPPASTTypeId</code>
	 */
	public void setMappingTypeId(ICPPASTTypeId mappingTypeId);

	@Override
	public ICPPASTAliasDeclaration copy();

	@Override
	public ICPPASTAliasDeclaration copy(CopyStyle style);
}

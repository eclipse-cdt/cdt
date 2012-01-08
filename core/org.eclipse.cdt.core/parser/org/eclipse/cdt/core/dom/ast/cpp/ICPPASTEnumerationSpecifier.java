/*******************************************************************************
 * Copyright (c) 2010, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;

/**
 * <code> enum struct : unsigned int {...}</code>
 * 
 * @since 5.2
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTEnumerationSpecifier extends IASTEnumerationSpecifier, ICPPASTDeclSpecifier {
	
	public static final ASTNodeProperty BASE_TYPE = new ASTNodeProperty(
			"ICPPASTEnumerationSpecifier.BASE_TYPE [ICPPASTDeclSpecifier]"); //$NON-NLS-1$

	@Override
	public ICPPASTEnumerationSpecifier copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTEnumerationSpecifier copy(CopyStyle style);

	/**
	 * Not allowed on frozen ast.
	 */
	public void setIsScoped(boolean isScoped);
	
	/**
	 * An enum is scoped if it uses the enumeration head 
	 * <code>enum class</code> or <code>enum struct</code>
	 */
	public boolean isScoped();

	/**
	 * Not allowed on frozen ast.
	 */
	public void setIsOpaque(boolean isOpaque);
	
	/**
	 * An opaque specifier does not have a body.
	 */
	public boolean isOpaque();

	/**
	 * Not allowed on frozen ast.
	 */
	public void setBaseType(ICPPASTDeclSpecifier baseType);
	
	/**
	 * Returns the base type for this enum or <code>null</code> if it was not specified.
	 */
	public ICPPASTDeclSpecifier getBaseType();

	/**
	 * Returns the scope containing the enumerators of this enumeration, or <code>null</code> if the specifier
	 * is opaque.
	 */
	public ICPPScope getScope();
}

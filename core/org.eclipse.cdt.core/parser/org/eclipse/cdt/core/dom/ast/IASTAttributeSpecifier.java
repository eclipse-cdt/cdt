/*******************************************************************************
 * Copyright (c) 2014, 2015 Institute for Software, HSR Hochschule fuer Technik and others
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Represents a C++11 (ISO/IEC 14882:2011 7.6.1)
 * or a GCC attribute specifier (http://gcc.gnu.org/onlinedocs/gcc/Attribute-Syntax.html).
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.7
 */
public interface IASTAttributeSpecifier extends IASTNode {
	public static final IASTAttributeSpecifier[] EMPTY_ATTRIBUTE_SPECIFIER_ARRAY = {};
	public static final ASTNodeProperty ATTRIBUTE = 
			new ASTNodeProperty("IASTAttributeSpecifier.ATTRIBUTE"); //$NON-NLS-1$

	/**
	 * Returns the attributes of the specifier.
	 * @deprecated Use IASTAttributeList.getAttributes() instead.
	 */
	@Deprecated
	public abstract IASTAttribute[] getAttributes();

	/**
	 * Adds an attribute to the specifier.
	 * @deprecated Use IASTAttributeList.addAttribute() instead.
	 */
	@Deprecated
	public abstract void addAttribute(IASTAttribute attribute);

	@Override
	public IASTAttributeSpecifier copy();

	@Override
	public IASTAttributeSpecifier copy(CopyStyle style);
}
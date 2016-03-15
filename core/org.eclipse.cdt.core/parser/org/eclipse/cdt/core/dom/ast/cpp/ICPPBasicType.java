/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IBasicType;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPBasicType extends IBasicType {
	/**
	 * @deprecated, don't use the constant, more flags may be added for supporting future c++ standards.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int LAST = IS_LONG_LONG;

	/** 
	 * @since 4.0
	 * @deprecated use {@link #getModifiers()}, instead.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public int getQualifierBits();

	/**
	 * @deprecated Use the type-safe version getKind(), instead.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_bool = ICPPASTSimpleDeclSpecifier.t_bool;

	/**
	 * @deprecated Use the type-safe version getKind(), instead.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_wchar_t = ICPPASTSimpleDeclSpecifier.t_wchar_t;
}

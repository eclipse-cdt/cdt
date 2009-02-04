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
	public static final int IS_LONG     = 1;
	public static final int IS_SHORT    = 1 << 1;
	public static final int IS_SIGNED   = 1 << 2;
	public static final int IS_UNSIGNED = 1 << 3;
	public static final int IS_COMPLEX = 1 << 4;	// for gpp-types
	public static final int IS_IMAGINARY = 1 << 5;  // for gpp-types
	public static final int IS_LONG_LONG = 1 << 6;  // for gpp-types
	public static final int LAST = IS_LONG_LONG;

	// Extra types
	public static final int t_bool = ICPPASTSimpleDeclSpecifier.t_bool;

	public static final int t_wchar_t = ICPPASTSimpleDeclSpecifier.t_wchar_t;

	/** 
	 * @return a combination of qualifiers.
	 * @since 4.0
	 */
	public int getQualifierBits();
}

/*******************************************************************************
 * Copyright (c) 2008, 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAttribute;

/**
 * Represents a C++11 (ISO/IEC 14882:2011 7.6.1 [dcl.attr.grammar]) attribute specifier.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.7
 */
public interface ICPPASTAttributeSpecifier extends IASTAttribute {
	/**
	 * Returns the attributes of the specifier.
	 */
	public abstract ICPPASTAttribute[] getAttributes();

	/**
	 * Adds an attribute to the specifier.
	 */
	public abstract void addAttribute(ICPPASTAttribute attribute);

	@Override
	public ICPPASTAttributeSpecifier copy();

	@Override
	public ICPPASTAttributeSpecifier copy(CopyStyle style);
}
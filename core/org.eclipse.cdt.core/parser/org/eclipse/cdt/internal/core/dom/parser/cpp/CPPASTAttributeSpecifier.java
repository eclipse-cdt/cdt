/*******************************************************************************
 * Copyright (c) 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAttribute;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttribute;

/**
 * Represents a C++ attribute specifier, containing attributes.
 */
public class CPPASTAttributeSpecifier extends ASTAttribute implements ICPPASTAttributeSpecifier {
	private ICPPASTAttribute[] attributes = ICPPASTAttribute.EMPTY_CPP_ATTRIBUTE_ARRAY;

	public CPPASTAttributeSpecifier() {
		super(new char[0], null);
	}

	@Override
	public void addAttribute(ICPPASTAttribute attribute) {
		attributes = ArrayUtil.append(attributes, attribute);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPASTAttributeSpecifier#getAttributes()
	 */
	@Override
	public ICPPASTAttribute[] getAttributes() {
		attributes = ArrayUtil.trim(attributes);
		return attributes;
	}

	@Override
	public ICPPASTAttributeSpecifier copy(CopyStyle style) {
		ICPPASTAttributeSpecifier copy = copy(new CPPASTAttributeSpecifier(), style);
		for (ICPPASTAttribute attribute : ArrayUtil.trim(attributes)){
			copy.addAttribute(attribute.copy(style));
		}
		return copy;
	}

	@Override
	public ICPPASTAttributeSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}
}

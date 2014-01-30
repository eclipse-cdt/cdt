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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAttribute;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttribute;

/**
 * Represents a C++ attribute specifier, containing attributes.
 */
public class CPPASTAttributeSpecifier extends ASTAttribute implements ICPPASTAttributeSpecifier {
	private ICPPASTAttribute[] attributes = ICPPASTAttribute.EMPTY_CPP_ATTRIBUTE_ARRAY;

	public CPPASTAttributeSpecifier() {
		super(CharArrayUtils.EMPTY_CHAR_ARRAY, null);
	}

	@Override
	public void addAttribute(ICPPASTAttribute attribute) {
		assertNotFrozen();
		attributes = ArrayUtil.append(attributes, attribute);
	}

	@Override
	public ICPPASTAttribute[] getAttributes() {
		attributes = ArrayUtil.trim(attributes);
		return attributes;
	}

	@Override
	public ICPPASTAttributeSpecifier copy(CopyStyle style) {
		CPPASTAttributeSpecifier copy = copy(new CPPASTAttributeSpecifier(), style);
		copy.attributes = ArrayUtil.trim(attributes, true);
		for (int i = 0; i < copy.attributes.length; i++) {
			copy.attributes[i] = copy.attributes[i].copy(style);
		}
		return copy;
	}

	@Override
	public ICPPASTAttributeSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitAttributes) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			}
		}

		for (ICPPASTAttribute attribute : getAttributes()) {
			if (!attribute.accept(action))
				return false;
		}

		if (action.shouldVisitAttributes && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}
}

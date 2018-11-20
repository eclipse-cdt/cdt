/*******************************************************************************
 * Copyright (c) 2012, 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 * 	   Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTAlignmentSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTAttributeList;
import org.eclipse.cdt.core.dom.ast.IASTAttributeOwner;
import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.gnu.IGCCASTAttributeList;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPASTAttributeSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * Classes that implement IASTAttributeOwner interface may extend this class.
 */
public abstract class ASTAttributeOwner extends ASTNode implements IASTAttributeOwner {
	private IASTAttributeSpecifier[] attributeSpecifiers = IASTAttributeSpecifier.EMPTY_ATTRIBUTE_SPECIFIER_ARRAY;

	@Override
	public IASTAttribute[] getAttributes() {
		IASTAttribute[] attributes = IASTAttribute.EMPTY_ATTRIBUTE_ARRAY;
		for (IASTAttributeSpecifier attributeSpecifier : getAttributeSpecifiers()) {
			if (attributeSpecifier instanceof IASTAttributeList) {
				attributes = ArrayUtil.addAll(attributes, ((IASTAttributeList) attributeSpecifier).getAttributes());
			}
		}
		return attributes;
	}

	@Override
	@Deprecated
	public void addAttribute(IASTAttribute attribute) {
	}

	@Override
	public IASTAttributeSpecifier[] getAttributeSpecifiers() {
		attributeSpecifiers = ArrayUtil.trim(attributeSpecifiers);
		return attributeSpecifiers;
	}

	@Override
	public void addAttributeSpecifier(IASTAttributeSpecifier attributeSpecifier) {
		assertNotFrozen();
		if (attributeSpecifier != null) {
			attributeSpecifier.setParent(this);
			attributeSpecifier.setPropertyInParent(ATTRIBUTE_SPECIFIER);
			attributeSpecifiers = ArrayUtil.append(attributeSpecifiers, attributeSpecifier);
		}
	}

	protected <T extends ASTAttributeOwner> T copy(T copy, CopyStyle style) {
		for (IASTAttributeSpecifier attributeSpecifier : getAttributeSpecifiers()) {
			copy.addAttributeSpecifier(attributeSpecifier.copy(style));
		}
		return super.copy(copy, style);
	}

	protected boolean acceptByAttributeSpecifiers(ASTVisitor action) {
		return visitAttributes(action, attributeSpecifiers);
	}

	private boolean visitAttributes(ASTVisitor action, IASTAttributeSpecifier[] attributeSpecifiers) {
		for (IASTAttributeSpecifier attributeSpecifier : attributeSpecifiers) {
			if (attributeSpecifier == null)
				break;
			if (!attributeSpecifier.accept(action))
				return false;
		}
		return true;
	}

	protected boolean acceptByGCCAttributeSpecifiers(ASTVisitor action) {
		for (IASTAttributeSpecifier attributeSpecifier : attributeSpecifiers) {
			if (!(attributeSpecifier instanceof IGCCASTAttributeList))
				continue;
			if (!attributeSpecifier.accept(action))
				return false;
		}
		return true;
	}

	protected boolean acceptByCPPAttributeSpecifiers(ASTVisitor action) {
		for (IASTAttributeSpecifier attributeSpecifier : attributeSpecifiers) {
			if (!(attributeSpecifier instanceof ICPPASTAttributeSpecifier))
				continue;
			if (!attributeSpecifier.accept(action))
				return false;
		}
		return true;
	}

	/*
	 * Having this here allows CPPASTAttributeOwner to implement IASTAmbiguityParent
	 * without needing to access the field attributeSpecifiers.
	 */
	protected void replace(IASTNode child, IASTNode other) {
		if (child instanceof IASTAlignmentSpecifier && other instanceof IASTAlignmentSpecifier) {
			for (int i = 0; i < attributeSpecifiers.length; ++i) {
				if (attributeSpecifiers[i] == child) {
					attributeSpecifiers[i] = (IASTAttributeSpecifier) other;
					other.setParent(child.getParent());
					other.setPropertyInParent(child.getPropertyInParent());
					return;
				}
			}
		}
	}
}
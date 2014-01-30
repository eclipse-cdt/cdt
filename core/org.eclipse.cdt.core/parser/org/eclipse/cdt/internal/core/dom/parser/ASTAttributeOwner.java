/*******************************************************************************
 * Copyright (c) 2012, 2014 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 * 	   Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTAttributeOwner;
import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTAttributeSpecifier;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPASTAttributeSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.IUnaryPredicate;

/**
 * Classes that implement IASTAttributeOwner interface may extend this class.
 */
public abstract class ASTAttributeOwner extends ASTNode implements IASTAttributeOwner {
	private final class AttributeFilter<E extends IASTAttributeSpecifier> implements IUnaryPredicate<IASTAttributeSpecifier> {
		Class<E> filterType;

		public AttributeFilter(Class<E> filterType) {
			this.filterType = filterType;
		}

		@Override
		public boolean apply(IASTAttributeSpecifier attributeSpecifier) {
			return filterType.isInstance(attributeSpecifier);
		}
	}

	private IASTAttributeSpecifier[] attributeSpecifiers = IASTAttributeSpecifier.EMPTY_ATTRIBUTE_SPECIFIER_ARRAY;

	@Override
	public IASTAttribute[] getAttributes() {
		IASTAttribute[] attributes = IASTAttribute.EMPTY_ATTRIBUTE_ARRAY;
		for (IASTAttributeSpecifier attributeSpecifier : getAttributeSpecifiers()) {
			attributes = ArrayUtil.addAll(attributes, attributeSpecifier.getAttributes());
		}
		return attributes;
	}

	@Override
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
			attributeSpecifier.setPropertyInParent(ATTRIBUTE);
			attributeSpecifiers = ArrayUtil.append(attributeSpecifiers, attributeSpecifier);
		}
	}

	@Override
	public IASTAttributeSpecifier[] getGNUAttributeSpecifiers() {
		AttributeFilter<IGNUASTAttributeSpecifier> attributeFilter = new AttributeFilter<>(IGNUASTAttributeSpecifier.class);
		return ArrayUtil.filter(getAttributeSpecifiers(), attributeFilter);
	}

	@Override
	public IASTAttributeSpecifier[] getCPPAttributeSpecifiers() {
		AttributeFilter<ICPPASTAttributeSpecifier> attributeFilter = new AttributeFilter<>(ICPPASTAttributeSpecifier.class);
		return ArrayUtil.filter(getAttributeSpecifiers(), attributeFilter);
	}

	protected <T extends ASTAttributeOwner> T copy(T copy, CopyStyle style) {
		for (IASTAttribute attribute : getAttributes()) {
			copy.addAttribute(attribute.copy(style));
		}
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

	protected boolean acceptByGNUAttributeSpecifiers(ASTVisitor action) {
		AttributeFilter<IGNUASTAttributeSpecifier> attributeFilter = new AttributeFilter<>(IGNUASTAttributeSpecifier.class);
		IASTAttributeSpecifier[] filteredAttribtues = ArrayUtil.filter(attributeSpecifiers, attributeFilter);
		return visitAttributes(action, filteredAttribtues);
	}

	protected boolean acceptByCPPAttributeSpecifiers(ASTVisitor action) {
		AttributeFilter<ICPPASTAttributeSpecifier> attributeFilter = new AttributeFilter<>(ICPPASTAttributeSpecifier.class);
		IASTAttributeSpecifier[] filteredAttribtues = ArrayUtil.filter(attributeSpecifiers, attributeFilter);
		return visitAttributes(action, filteredAttribtues);
	}
}
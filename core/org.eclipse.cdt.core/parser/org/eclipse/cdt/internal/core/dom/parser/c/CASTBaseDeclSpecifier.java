/*******************************************************************************
 *  Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTAlignmentSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public abstract class CASTBaseDeclSpecifier extends ASTAttributeOwner
		implements ICASTDeclSpecifier, IASTAmbiguityParent {

	protected int storageClass;
	protected boolean isConst;
	protected boolean isVolatile;
	protected boolean isRestrict;
	protected boolean isInline;
	protected IASTAlignmentSpecifier[] alignmentSpecifiers = IASTAlignmentSpecifier.EMPTY_ALIGNMENT_SPECIFIER_ARRAY;

	@Override
	public boolean isRestrict() {
		return isRestrict;
	}

	@Override
	public int getStorageClass() {
		return storageClass;
	}

	@Override
	public boolean isConst() {
		return isConst;
	}

	@Override
	public boolean isVolatile() {
		return isVolatile;
	}

	@Override
	public boolean isInline() {
		return isInline;
	}

	@Override
	public IASTAlignmentSpecifier[] getAlignmentSpecifiers() {
		return alignmentSpecifiers;
	}

	@Override
	public void setStorageClass(int storageClass) {
		assertNotFrozen();
		this.storageClass = storageClass;
	}

	@Override
	public void setConst(boolean value) {
		assertNotFrozen();
		this.isConst = value;
	}

	@Override
	public void setVolatile(boolean value) {
		assertNotFrozen();
		this.isVolatile = value;
	}

	@Override
	public void setRestrict(boolean value) {
		assertNotFrozen();
		this.isRestrict = value;
	}

	@Override
	public void setInline(boolean value) {
		assertNotFrozen();
		this.isInline = value;
	}

	@Override
	public void setAlignmentSpecifiers(IASTAlignmentSpecifier[] alignmentSpecifiers) {
		assertNotFrozen();
		for (IASTAlignmentSpecifier specifier : alignmentSpecifiers) {
			specifier.setParent(this);
			specifier.setPropertyInParent(ALIGNMENT_SPECIFIER);
		}
		this.alignmentSpecifiers = alignmentSpecifiers;
	}

	protected <T extends CASTBaseDeclSpecifier> T copy(T copy, CopyStyle style) {
		copy.storageClass = storageClass;
		copy.isConst = isConst;
		copy.isVolatile = isVolatile;
		copy.isRestrict = isRestrict;
		copy.isInline = isInline;
		copy.alignmentSpecifiers = new IASTAlignmentSpecifier[alignmentSpecifiers.length];
		for (int i = 0; i < alignmentSpecifiers.length; ++i) {
			copy.alignmentSpecifiers[i] = alignmentSpecifiers[i].copy(style);
			copy.alignmentSpecifiers[i].setParent(copy);
		}
		return super.copy(copy, style);
	}

	protected boolean visitAlignmentSpecifiers(ASTVisitor visitor) {
		for (IASTAlignmentSpecifier specifier : alignmentSpecifiers) {
			if (!specifier.accept(visitor)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child instanceof IASTAlignmentSpecifier && other instanceof IASTAlignmentSpecifier) {
			for (int i = 0; i < alignmentSpecifiers.length; ++i) {
				if (alignmentSpecifiers[i] == child) {
					alignmentSpecifiers[i] = (IASTAlignmentSpecifier) other;
					other.setParent(child.getParent());
					other.setPropertyInParent(child.getPropertyInParent());
					return;
				}
			}
		}
	}
}

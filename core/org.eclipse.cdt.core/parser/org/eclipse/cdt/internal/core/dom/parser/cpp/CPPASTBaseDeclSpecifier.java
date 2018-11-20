/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTAlignmentSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;

/**
 * Base for all C++ declaration specifiers.
 */
public abstract class CPPASTBaseDeclSpecifier extends CPPASTAttributeOwner implements ICPPASTDeclSpecifier {
	private boolean isConst;
	private boolean isConstexpr;
	private boolean isExplicit;
	private boolean isFriend;
	private boolean isInline;
	private boolean isRestrict;
	private boolean isThreadLocal;
	private boolean isVirtual;
	private boolean isVolatile;
	private int storageClass;

	@Override
	public final boolean isConst() {
		return isConst;
	}

	@Override
	public final void setConst(boolean value) {
		assertNotFrozen();
		isConst = value;
	}

	@Override
	public final boolean isConstexpr() {
		return isConstexpr;
	}

	@Override
	public final void setConstexpr(boolean value) {
		assertNotFrozen();
		isConstexpr = value;
	}

	@Override
	public final boolean isFriend() {
		return isFriend;
	}

	@Override
	public final void setFriend(boolean value) {
		assertNotFrozen();
		isFriend = value;
	}

	@Override
	public final int getStorageClass() {
		return storageClass;
	}

	@Override
	public final void setStorageClass(int storageClass) {
		assertNotFrozen();
		this.storageClass = storageClass;
	}

	@Override
	public final boolean isThreadLocal() {
		return isThreadLocal;
	}

	@Override
	public final void setThreadLocal(boolean value) {
		assertNotFrozen();
		isThreadLocal = value;
	}

	@Override
	public final boolean isVolatile() {
		return isVolatile;
	}

	@Override
	public final void setVolatile(boolean value) {
		assertNotFrozen();
		isVolatile = value;
	}

	@Override
	public final boolean isRestrict() {
		return isRestrict;
	}

	@Override
	public final void setRestrict(boolean value) {
		assertNotFrozen();
		isRestrict = value;
	}

	@Override
	public final boolean isInline() {
		return isInline;
	}

	@Override
	public final void setInline(boolean value) {
		assertNotFrozen();
		this.isInline = value;
	}

	@Override
	public final boolean isVirtual() {
		return isVirtual;
	}

	@Override
	public final void setVirtual(boolean value) {
		assertNotFrozen();
		isVirtual = value;
	}

	@Override
	public final boolean isExplicit() {
		return isExplicit;
	}

	@Override
	public final void setExplicit(boolean value) {
		assertNotFrozen();
		this.isExplicit = value;
	}

	@Deprecated
	@Override
	public IASTAlignmentSpecifier[] getAlignmentSpecifiers() {
		return null;
	}

	@Deprecated
	@Override
	public void setAlignmentSpecifiers(IASTAlignmentSpecifier[] alignmentSpecifiers) {
	}

	protected <T extends CPPASTBaseDeclSpecifier> T copy(T copy, CopyStyle style) {
		CPPASTBaseDeclSpecifier target = copy;
		target.isExplicit = isExplicit;
		target.isFriend = isFriend;
		target.isInline = isInline;
		target.isConst = isConst;
		target.isConstexpr = isConstexpr;
		target.isRestrict = isRestrict;
		target.isThreadLocal = isThreadLocal;
		target.isVolatile = isVolatile;
		target.storageClass = storageClass;
		target.isVirtual = isVirtual;
		return super.copy(copy, style);
	}

	/**
	 * Provided for debugging purposes, only.
	 */
	@Override
	public String toString() {
		return ASTStringUtil.getSignatureString(this, null);
	}
}

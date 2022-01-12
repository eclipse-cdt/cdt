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
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTClassVirtSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;

/**
 * c++ specific composite type specifier
 */
public class CPPASTCompositeTypeSpecifier extends CPPASTBaseDeclSpecifier implements ICPPASTCompositeTypeSpecifier {
	private int fKey;
	private IASTName fName;
	private CPPClassScope fScope;
	private IASTDeclaration[] fAllDeclarations;
	private IASTDeclaration[] fActiveDeclarations;
	private int fDeclarationsPos = -1;
	private ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier[] baseSpecs;
	private int baseSpecsPos = -1;
	private boolean fAmbiguitiesResolved;
	private ICPPASTClassVirtSpecifier virtSpecifier;

	public CPPASTCompositeTypeSpecifier() {
	}

	public CPPASTCompositeTypeSpecifier(int k, IASTName n) {
		this.fKey = k;
		setName(n);
	}

	public void setAmbiguitiesResolved() {
		if (!fAmbiguitiesResolved && fScope != null) {
			fScope.createImplicitMembers();
		}
		fAmbiguitiesResolved = true;
	}

	@Override
	public CPPASTCompositeTypeSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTCompositeTypeSpecifier copy(CopyStyle style) {
		CPPASTCompositeTypeSpecifier copy = new CPPASTCompositeTypeSpecifier(fKey,
				fName == null ? null : fName.copy(style));
		for (IASTDeclaration member : getMembers())
			copy.addMemberDeclaration(member == null ? null : member.copy(style));
		for (ICPPASTBaseSpecifier baseSpecifier : getBaseSpecifiers())
			copy.addBaseSpecifier(baseSpecifier == null ? null : baseSpecifier.copy(style));
		copy.setVirtSpecifier(virtSpecifier == null ? null : virtSpecifier.copy(style));
		return super.copy(copy, style);
	}

	@Override
	public ICPPASTBaseSpecifier[] getBaseSpecifiers() {
		if (baseSpecs == null)
			return ICPPASTBaseSpecifier.EMPTY_BASESPECIFIER_ARRAY;
		baseSpecs = ArrayUtil.trimAt(ICPPASTBaseSpecifier.class, baseSpecs, baseSpecsPos);
		return baseSpecs;
	}

	@Override
	public void addBaseSpecifier(ICPPASTBaseSpecifier baseSpec) {
		assertNotFrozen();
		if (baseSpec != null) {
			baseSpec.setParent(this);
			baseSpec.setPropertyInParent(BASE_SPECIFIER);
			baseSpecs = ArrayUtil.appendAt(ICPPASTBaseSpecifier.class, baseSpecs, ++baseSpecsPos, baseSpec);
		}
	}

	@Override
	public int getKey() {
		return fKey;
	}

	@Override
	public void setKey(int key) {
		assertNotFrozen();
		fKey = key;
	}

	@Override
	public IASTName getName() {
		return fName;
	}

	@Override
	public void setName(IASTName name) {
		assertNotFrozen();
		this.fName = name;
		if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(TYPE_NAME);
		}
	}

	@Override
	public IASTDeclaration[] getMembers() {
		IASTDeclaration[] active = fActiveDeclarations;
		if (active == null) {
			active = ASTQueries.extractActiveDeclarations(fAllDeclarations, fDeclarationsPos + 1);
			fActiveDeclarations = active;
		}
		return active;
	}

	@Override
	public final IASTDeclaration[] getDeclarations(boolean includeInactive) {
		if (includeInactive) {
			fAllDeclarations = ArrayUtil.trimAt(IASTDeclaration.class, fAllDeclarations, fDeclarationsPos);
			return fAllDeclarations;
		}
		return getMembers();
	}

	@Override
	public void addMemberDeclaration(IASTDeclaration decl) {
		if (decl == null)
			return;

		// ignore inactive visibility labels
		if (decl instanceof ICPPASTVisibilityLabel && !decl.isActive())
			return;

		assertNotFrozen();
		decl.setParent(this);
		decl.setPropertyInParent(decl instanceof ICPPASTVisibilityLabel ? VISIBILITY_LABEL : MEMBER_DECLARATION);
		fAllDeclarations = ArrayUtil.appendAt(IASTDeclaration.class, fAllDeclarations, ++fDeclarationsPos, decl);
		fActiveDeclarations = null;
	}

	@Override
	public final void addDeclaration(IASTDeclaration decl) {
		addMemberDeclaration(decl);
	}

	@Override
	public ICPPClassScope getScope() {
		if (fScope == null) {
			fScope = new CPPClassScope(this);
			if (fAmbiguitiesResolved) {
				fScope.createImplicitMembers();
			}
		}
		return fScope;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclSpecifiers) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (!acceptByAttributeSpecifiers(action))
			return false;

		if (fName != null && !fName.accept(action))
			return false;

		if (virtSpecifier != null && !virtSpecifier.accept(action))
			return false;

		ICPPASTBaseSpecifier[] bases = getBaseSpecifiers();
		for (int i = 0; i < bases.length; i++) {
			if (!bases[i].accept(action))
				return false;
		}

		IASTDeclaration[] decls = getDeclarations(action.includeInactiveNodes);
		for (int i = 0; i < decls.length; i++) {
			if (!decls[i].accept(action))
				return false;
		}

		if (action.shouldVisitDeclSpecifiers && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}

	@Override
	public int getRoleForName(IASTName name) {
		if (name == this.fName)
			return r_definition;
		return r_unclear;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		assert child.isActive() == other.isActive();
		for (int i = 0; i <= fDeclarationsPos; ++i) {
			if (fAllDeclarations[i] == child) {
				other.setParent(child.getParent());
				other.setPropertyInParent(child.getPropertyInParent());
				fAllDeclarations[i] = (IASTDeclaration) other;
				fActiveDeclarations = null;
				return;
			}
		}
		super.replace(child, other);
	}

	@Override
	public boolean isFinal() {
		return virtSpecifier != null;
	}

	@Override
	@Deprecated
	public void setFinal(boolean value) {
		assertNotFrozen();
		// Do nothing here. Use setVirtSpecifier() instead.
	}

	@Override
	public ICPPASTClassVirtSpecifier getVirtSpecifier() {
		return virtSpecifier;
	}

	@Override
	public void setVirtSpecifier(ICPPASTClassVirtSpecifier virtSpecifier) {
		assertNotFrozen();
		this.virtSpecifier = virtSpecifier;
		if (virtSpecifier != null) {
			virtSpecifier.setParent(this);
			virtSpecifier.setPropertyInParent(CLASS_VIRT_SPECIFIER);
		}
	}
}

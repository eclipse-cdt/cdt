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
 *    John Camelon (IBM) - Initial API and implementation
 *    Bryan Wilkinson (QNX)
 *    Markus Schorn (Wind River Systems)
 *    Michael Woski
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICPPASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

/**
 * Base class specifier
 */
public class CPPASTBaseSpecifier extends ASTNode implements ICPPASTBaseSpecifier, ICPPASTCompletionContext {
	private boolean isVirtual;
	private int visibility;
	private ICPPASTNameSpecifier nameSpecifier;
	private boolean fIsPackExpansion;

	public CPPASTBaseSpecifier() {
	}

	public CPPASTBaseSpecifier(ICPPASTNameSpecifier nameSpecifier) {
		setNameSpecifier(nameSpecifier);
	}

	public CPPASTBaseSpecifier(ICPPASTNameSpecifier nameSpecifier, int visibility, boolean isVirtual) {
		this.isVirtual = isVirtual;
		this.visibility = visibility;
		setNameSpecifier(nameSpecifier);
	}

	@Override
	public CPPASTBaseSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTBaseSpecifier copy(CopyStyle style) {
		CPPASTBaseSpecifier copy = new CPPASTBaseSpecifier(nameSpecifier == null ? null : nameSpecifier.copy(style));
		copy.isVirtual = isVirtual;
		copy.visibility = visibility;
		copy.fIsPackExpansion = fIsPackExpansion;
		return copy(copy, style);
	}

	@Override
	public boolean isVirtual() {
		return isVirtual;
	}

	@Override
	public void setVirtual(boolean value) {
		assertNotFrozen();
		isVirtual = value;
	}

	@Override
	public int getVisibility() {
		return visibility;
	}

	@Override
	public void setVisibility(int visibility) {
		assertNotFrozen();
		this.visibility = visibility;
	}

	@Override
	@Deprecated
	public IASTName getName() {
		if (nameSpecifier instanceof IASTName) {
			return (IASTName) nameSpecifier;
		}
		throw new UnsupportedOperationException("Cannot call getName() on base-specifier whose name-specifier " //$NON-NLS-1$
				+ "is not a name. Use getNameSpecifier() instead."); //$NON-NLS-1$
	}

	@Override
	@Deprecated
	public void setName(IASTName name) {
		setNameSpecifier((ICPPASTName) name);
	}

	@Override
	public ICPPASTNameSpecifier getNameSpecifier() {
		return nameSpecifier;
	}

	@Override
	public void setNameSpecifier(ICPPASTNameSpecifier nameSpecifier) {
		assertNotFrozen();
		this.nameSpecifier = nameSpecifier;
		if (nameSpecifier != null) {
			nameSpecifier.setParent(this);
			nameSpecifier.setPropertyInParent(NAME_SPECIFIER);
		}
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitBaseSpecifiers) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (nameSpecifier != null && !nameSpecifier.accept(action))
			return false;

		if (action.shouldVisitBaseSpecifiers && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}

	@Override
	public int getRoleForName(IASTName n) {
		if (nameSpecifier == n)
			return r_reference;
		return r_unclear;
	}

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix, String[] namespaces) {
		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix, namespaces);

		ICPPClassType classType = null;
		if (getParent() instanceof CPPASTCompositeTypeSpecifier) {
			IASTName className = ((CPPASTCompositeTypeSpecifier) getParent()).getName();
			IBinding binding = className.resolveBinding();
			if (binding instanceof ICPPClassType) {
				classType = (ICPPClassType) binding;
			}
		}

		int j = 0;
		for (int i = 0; i < bindings.length; i++) {
			IBinding binding = bindings[i];
			if (binding instanceof IType) {
				IType type = (IType) binding;

				while (type instanceof ITypedef || type instanceof ICPPAliasTemplate) {
					type = type instanceof ITypedef ? ((ITypedef) type).getType()
							: ((ICPPAliasTemplate) type).getType();
				}

				if (type instanceof ICPPClassType) {
					int key = ((ICPPClassType) type).getKey();
					if ((key == ICPPClassType.k_class || key == ICPPClassType.k_struct
							|| type instanceof ICPPDeferredClassInstance || type instanceof ICPPUnknownMemberClass)
							&& (classType == null || !type.isSameType(classType))) {
						if (i != j)
							bindings[j] = binding;
						j++;
					}
				} else if (type instanceof ICPPTemplateTypeParameter) {
					if (i != j)
						bindings[j] = binding;
					j++;
				}
			}
		}

		if (j < bindings.length)
			return Arrays.copyOfRange(bindings, 0, j);
		return bindings;
	}

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return findBindings(n, isPrefix, null);
	}

	@Override
	public boolean isPackExpansion() {
		return fIsPackExpansion;
	}

	@Override
	public void setIsPackExpansion(boolean val) {
		assertNotFrozen();
		fIsPackExpansion = val;
	}
}

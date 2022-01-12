/*******************************************************************************
 * Copyright (c) 2004, 2018 IBM Corporation and others.
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
 *     Bryan Wilkinson (QNX)
 *     Markus Schorn (Wind River Systems)
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICPPASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.model.IEnumeration;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalNameOwner;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.parser.util.ContentAssistMatcherFactory;
import org.eclipse.core.runtime.Assert;

/**
 * Qualified name, which can contain any other name (unqualified, operator-name, conversion name,
 * template id).
 */
public class CPPASTQualifiedName extends CPPASTNameBase implements ICPPASTQualifiedName, ICPPASTCompletionContext {
	private ICPPASTNameSpecifier[] fQualifier;
	private int fQualifierPos = -1;
	private ICPPASTName fLastName;
	private boolean fIsFullyQualified;
	private char[] fSignature;

	public CPPASTQualifiedName(ICPPASTName lastName) {
		if (lastName != null)
			setLastName(lastName);
	}

	/**
	 * @deprecated Prefer CPPASTQualifierName(ICPPASTName) instead.
	 */
	@Deprecated
	public CPPASTQualifiedName() {
	}

	@Override
	public CPPASTQualifiedName copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTQualifiedName copy(CopyStyle style) {
		CPPASTQualifiedName copy = new CPPASTQualifiedName(fLastName == null ? null : fLastName.copy(style));
		for (ICPPASTNameSpecifier nameSpecifier : getQualifier()) {
			copy.addNameSpecifier(nameSpecifier == null ? null : nameSpecifier.copy(style));
		}
		copy.setFullyQualified(fIsFullyQualified);
		return copy(copy, style);
	}

	@Override
	public final IBinding resolvePreBinding() {
		// The whole qualified name resolves to the same thing as the last name.
		return getLastName().resolvePreBinding();
	}

	@Override
	public IBinding resolveBinding() {
		// The whole qualified name resolves to the same thing as the last name.
		IASTName lastName = getLastName();
		return lastName == null ? null : lastName.resolveBinding();
	}

	@Override
	public final IBinding getPreBinding() {
		// The whole qualified name resolves to the same thing as the last name.
		return getLastName().getPreBinding();
	}

	@Override
	public IBinding getBinding() {
		return getLastName().getBinding();
	}

	@Override
	public void setBinding(IBinding binding) {
		getLastName().setBinding(binding);
	}

	@Override
	public void addName(IASTName name) {
		if (fLastName != null)
			addNameSpecifier(fLastName);
		setLastName((ICPPASTName) name);
	}

	@Override
	public void setLastName(ICPPASTName lastName) {
		assertNotFrozen();
		assert !(lastName instanceof ICPPASTQualifiedName);
		fLastName = lastName;
		fLastName.setParent(this);
		fLastName.setPropertyInParent(SEGMENT_NAME);
	}

	@Override
	public void addNameSpecifier(ICPPASTNameSpecifier nameSpecifier) {
		assertNotFrozen();
		assert !(nameSpecifier instanceof ICPPASTQualifiedName);
		if (nameSpecifier != null) {
			fQualifier = ArrayUtil.appendAt(ICPPASTNameSpecifier.class, fQualifier, ++fQualifierPos, nameSpecifier);
			nameSpecifier.setParent(this);
			nameSpecifier.setPropertyInParent(SEGMENT_NAME);
		}
	}

	@Override
	public ICPPASTNameSpecifier[] getQualifier() {
		if (fQualifierPos < 0)
			return ICPPASTNameSpecifier.EMPTY_NAME_SPECIFIER_ARRAY;

		fQualifier = ArrayUtil.trimAt(ICPPASTNameSpecifier.class, fQualifier, fQualifierPos);
		return fQualifier;
	}

	@Override
	public ICPPASTNameSpecifier[] getAllSegments() {
		ICPPASTNameSpecifier[] result = new ICPPASTNameSpecifier[fQualifierPos + (fLastName == null ? 1 : 2)];
		int idx = 0;
		for (ICPPASTNameSpecifier nameSpecifier : getQualifier())
			result[idx++] = nameSpecifier;
		if (fLastName != null)
			result[fQualifierPos + 1] = fLastName;
		return result;
	}

	@Override
	public IASTName getLastName() {
		return fLastName;
	}

	@Override
	public char[] getSimpleID() {
		return fLastName.getSimpleID();
	}

	@Override
	public char[] getLookupKey() {
		return fLastName.getLookupKey();
	}

	@Override
	public char[] toCharArray() {
		if (fSignature == null) {
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i <= fQualifierPos; i++) {
				if (i > 0 || fIsFullyQualified) {
					buf.append(Keywords.cpCOLONCOLON);
				}
				buf.append(fQualifier[i].toCharArray());
			}
			if (fQualifierPos >= 0 || fIsFullyQualified) {
				buf.append(Keywords.cpCOLONCOLON);
			}
			buf.append(fLastName.toCharArray());

			final int len = buf.length();
			fSignature = new char[len];
			buf.getChars(0, len, fSignature, 0);
		}
		return fSignature;
	}

	@Override
	public boolean isFullyQualified() {
		return fIsFullyQualified;
	}

	@Override
	public void setFullyQualified(boolean isFullyQualified) {
		assertNotFrozen();
		this.fIsFullyQualified = isFullyQualified;
	}

	/**
	 * @deprecated there is no need to set the signature, it will be computed lazily.
	 */
	@Deprecated
	public void setSignature(String signature) {
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitNames) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		for (ICPPASTNameSpecifier nameSpecifier : getQualifier()) {
			if (!nameSpecifier.accept(action))
				return false;
		}
		// Pointer-to-member qualified names have a dummy name as the last part of the name,
		// don't visit it.
		if (fLastName != null && fLastName.getLookupKey().length > 0 && !fLastName.accept(action))
			return false;

		if (action.shouldVisitNames) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		return true;
	}

	@Override
	public int getRoleOfName(boolean allowResolution) {
		IASTNode parent = getParent();
		if (parent instanceof IASTInternalNameOwner) {
			return ((IASTInternalNameOwner) parent).getRoleForName(this, allowResolution);
		}
		if (parent instanceof IASTNameOwner) {
			return ((IASTNameOwner) parent).getRoleForName(this);
		}
		return IASTNameOwner.r_unclear;
	}

	@Override
	public int getRoleForName(IASTName n) {
		for (int i = 0; i <= fQualifierPos; ++i) {
			if (fQualifier[i] == n)
				return r_reference;
		}
		if (getLastName() == n) {
			IASTNode p = getParent();
			if (p instanceof IASTNameOwner) {
				return ((IASTNameOwner) p).getRoleForName(this);
			}
		}
		return r_unclear;
	}

	@Override
	public boolean isConversionOrOperator() {
		final IASTName lastName = getLastName();
		if (lastName instanceof ICPPASTConversionName || lastName instanceof ICPPASTOperatorName) {
			return true;
		}

		// check templateId's name
		if (lastName instanceof ICPPASTTemplateId) {
			IASTName tempName = ((ICPPASTTemplateId) lastName).getTemplateName();
			if (tempName instanceof ICPPASTConversionName || tempName instanceof ICPPASTOperatorName) {
				return true;
			}
		}

		return false;
	}

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix, String[] namespaces) {
		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix, namespaces);

		ICPPClassType classQualifier = getClassQualifier();
		if (classQualifier != null) {
			final boolean isDeclaration = getParent().getParent() instanceof IASTSimpleDeclaration;
			final boolean isUsingDecl = getParent() instanceof ICPPASTUsingDeclaration;
			List<IBinding> filtered = filterClassScopeBindings(classQualifier, bindings, isDeclaration);
			if ((isDeclaration || isUsingDecl)
					&& nameMatches(classQualifier.getNameCharArray(), n.getLookupKey(), isPrefix)) {
				ICPPConstructor[] constructors = classQualifier.getConstructors();
				for (int i = 0; i < constructors.length; i++) {
					if (!constructors[i].isImplicit()) {
						filtered.add(constructors[i]);
					}
				}
			}
			return filtered.toArray(new IBinding[filtered.size()]);
		}

		return bindings;
	}

	// Are we taking the address of a qualified name?
	private boolean isAddressOf() {
		return getParent() instanceof IASTIdExpression && getParent().getParent() instanceof IASTUnaryExpression
				&& ((IASTUnaryExpression) getParent().getParent()).getOperator() == IASTUnaryExpression.op_amper;
	}

	// Are we inside a using-declaration?
	private boolean inUsingDecl() {
		return getParent() instanceof ICPPASTUsingDeclaration;
	}

	private boolean canBeFieldAccess(ICPPClassType baseClass) {
		IASTNode parent = getParent();
		if (parent instanceof IASTFieldReference) {
			return true;
		}
		if (parent instanceof IASTIdExpression) {
			IScope scope = CPPVisitor.getContainingScope(this);
			try {
				while (scope != null) {
					if (scope instanceof ICPPClassScope) {
						ICPPClassType classType = ((ICPPClassScope) scope).getClassType();
						if (SemanticUtil.calculateInheritanceDepth(classType, baseClass) >= 0) {
							return true;
						}
					}
					scope = scope.getParent();
				}
			} catch (DOMException e) {
			}
		}
		return false;
	}

	private ICPPClassType getClassQualifier() {
		if (fQualifierPos < 0) {
			return null;
		}

		IBinding binding = fQualifier[fQualifierPos].resolveBinding();

		while (binding instanceof ITypedef) {
			ITypedef typedef = (ITypedef) binding;
			IType type = typedef.getType();
			if (type instanceof IBinding) {
				binding = (IBinding) type;
			} else {
				binding = null;
			}
		}

		return binding instanceof ICPPClassType ? (ICPPClassType) binding : null;
	}

	public static boolean canBeFieldAccess(CPPASTQualifiedName qname) {
		ICPPClassType classQualifier = qname.getClassQualifier();
		if (classQualifier == null) {
			return true;
		}
		return qname.canBeFieldAccess(classQualifier);
	}

	private List<IBinding> filterClassScopeBindings(ICPPClassType classType, IBinding[] bindings,
			final boolean isDeclaration) {
		List<IBinding> filtered = new ArrayList<>();
		final boolean allowNonstatic = canBeFieldAccess(classType) || isAddressOf() || inUsingDecl();
		final IBinding templateDefinition = (classType instanceof ICPPTemplateInstance)
				? ((ICPPTemplateInstance) classType).getTemplateDefinition()
				: null;

		for (final IBinding binding : bindings) {
			if (binding instanceof IField) {
				IField field = (IField) binding;
				if (!allowNonstatic && !field.isStatic())
					continue;
			} else if (binding instanceof ICPPMethod) {
				ICPPMethod method = (ICPPMethod) binding;
				if (method.isImplicit())
					continue;
				if (!isDeclaration) {
					if (method.isDestructor() || method instanceof ICPPConstructor
							|| (!allowNonstatic && !method.isStatic()))
						continue;
				}
			} else if (binding instanceof IEnumerator || binding instanceof IEnumeration) {
				if (isDeclaration)
					continue;
			} else if (templateDefinition == binding) {
				// This solves bug 456101 (template instance refering to itself).
				// A<T>:: should not get a binding to its own template definition.
				continue;
			} else if (classType instanceof ICPPDeferredClassInstance && binding instanceof ICPPClassSpecialization) {
				// during heuristic resolution of ICPPDeferredClassInstance's we
				// might have found a partial specialization; those have their own template
				// definition but share the same primary template as the ICPPDeferredClassInstance
				ICPPClassType template = ((ICPPClassSpecialization) binding).getSpecializedBinding();
				if (template instanceof ICPPClassTemplatePartialSpecialization) {
					template = (ICPPClassType) ((ICPPClassTemplatePartialSpecialization) template).getPrimaryTemplate();
				}
				if (template.isSameType((IType) templateDefinition))
					continue;
			} else if (binding instanceof IType) {
				if (classType.isSameType((IType) binding))
					continue;
			}
			filtered.add(binding);
		}

		return filtered;
	}

	private static boolean nameMatches(char[] potential, char[] name, boolean isPrefix) {
		if (isPrefix)
			return ContentAssistMatcherFactory.getInstance().match(name, potential);
		return CharArrayUtils.equals(potential, name);
	}

	@Override
	protected IBinding createIntermediateBinding() {
		Assert.isLegal(false);
		return null;
	}

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return findBindings(n, isPrefix, null);
	}
}

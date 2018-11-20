/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Represents structs and unions.
 */
public class CStructure extends PlatformObject implements ICompositeType, ICInternalBinding {

	public static class CStructureProblem extends ProblemBinding implements ICompositeType {
		public CStructureProblem(IASTNode node, int id, char[] arg) {
			super(node, id, arg);
		}

		@Override
		public IField findField(String name) {
			return null;
		}

		@Override
		public IScope getCompositeScope() {
			return this;
		}

		@Override
		public IField[] getFields() {
			return IField.EMPTY_FIELD_ARRAY;
		}

		@Override
		public int getKey() {
			return k_struct;
		}
	}

	private IASTName[] declarations;
	private IASTName definition;
	private boolean checked;
	private ICompositeType typeInIndex;

	public CStructure(IASTName name) {
		if (name.getPropertyInParent() == IASTCompositeTypeSpecifier.TYPE_NAME) {
			definition = name;
		} else {
			declarations = new IASTName[] { name };
		}
		name.setBinding(this);
	}

	@Override
	public IASTNode getPhysicalNode() {
		return definition != null ? (IASTNode) definition : (IASTNode) declarations[0];
	}

	private void checkForDefinition() {
		if (!checked && definition == null) {
			IASTNode declSpec = declarations[0].getParent();
			if (declSpec instanceof ICASTElaboratedTypeSpecifier) {
				IASTDeclSpecifier spec = CVisitor.findDefinition((ICASTElaboratedTypeSpecifier) declSpec);
				if (spec instanceof ICASTCompositeTypeSpecifier) {
					ICASTCompositeTypeSpecifier compTypeSpec = (ICASTCompositeTypeSpecifier) spec;
					definition = compTypeSpec.getName();
					definition.setBinding(this);
				}
			}

			if (definition == null && typeInIndex == null) {
				final IASTTranslationUnit translationUnit = declSpec.getTranslationUnit();
				IIndex index = translationUnit.getIndex();
				if (index != null) {
					typeInIndex = (ICompositeType) index.adaptBinding(this);
				}
			}
		}
		checked = true;
	}

	@Override
	public String getName() {
		if (definition != null)
			return definition.toString();

		return declarations[0].toString();
	}

	@Override
	public char[] getNameCharArray() {
		if (definition != null)
			return definition.toCharArray();

		return declarations[0].toCharArray();
	}

	@Override
	public IScope getScope() throws DOMException {
		IASTDeclSpecifier declSpec = (IASTDeclSpecifier) ((definition != null) ? (IASTNode) definition.getParent()
				: declarations[0].getParent());
		IScope scope = CVisitor.getContainingScope(declSpec);
		while (scope instanceof ICCompositeTypeScope) {
			scope = scope.getParent();
		}
		return scope;
	}

	@Override
	public IField[] getFields() {
		checkForDefinition();
		if (definition == null) {
			return new IField[] { new CField.CFieldProblem(this, declarations[0],
					IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray()) };
		}
		ICASTCompositeTypeSpecifier compSpec = (ICASTCompositeTypeSpecifier) definition.getParent();
		IField[] fields = collectFields(compSpec, IField.EMPTY_FIELD_ARRAY);
		return ArrayUtil.trim(fields);
	}

	private IField[] collectFields(ICASTCompositeTypeSpecifier compSpec, IField[] fields) {
		IASTDeclaration[] members = compSpec.getMembers();
		if (members.length > 0) {
			if (fields == null)
				fields = new IField[members.length];
			for (IASTDeclaration node : members) {
				if (node instanceof IASTSimpleDeclaration) {
					IASTDeclarator[] declarators = ((IASTSimpleDeclaration) node).getDeclarators();
					if (declarators.length == 0) {
						IASTDeclSpecifier declspec = ((IASTSimpleDeclaration) node).getDeclSpecifier();
						if (declspec instanceof ICASTCompositeTypeSpecifier) {
							fields = collectFields((ICASTCompositeTypeSpecifier) declspec, fields);
						}
					} else {
						for (IASTDeclarator declarator : declarators) {
							IASTName name = ASTQueries.findInnermostDeclarator(declarator).getName();
							IBinding binding = name.resolveBinding();
							if (binding instanceof IField)
								fields = ArrayUtil.append(fields, (IField) binding);
						}
					}
				}
			}
		}
		return fields;
	}

	@Override
	public IField findField(String name) {
		IScope scope = getCompositeScope();
		if (scope == null) {
			return new CField.CFieldProblem(this, declarations[0], IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND,
					getNameCharArray());
		}

		final CASTName astName = new CASTName(name.toCharArray());
		astName.setPropertyInParent(CVisitor.STRING_LOOKUP_PROPERTY);
		IBinding binding = scope.getBinding(astName, true);
		if (binding instanceof IField)
			return (IField) binding;

		return null;
	}

	@Override
	public int getKey() {
		return definition != null ? ((IASTCompositeTypeSpecifier) definition.getParent()).getKey()
				: ((IASTElaboratedTypeSpecifier) declarations[0].getParent()).getKind();
	}

	@Override
	public IScope getCompositeScope() {
		checkForDefinition();
		if (definition != null) {
			return ((IASTCompositeTypeSpecifier) definition.getParent()).getScope();
		}
		// fwd-declarations must be backed up from the index
		if (typeInIndex != null) {
			IScope scope = typeInIndex.getCompositeScope();
			if (scope instanceof ICCompositeTypeScope)
				return scope;
		}
		return null;
	}

	@Override
	public Object clone() {
		IType t = null;
		try {
			t = (IType) super.clone();
		} catch (CloneNotSupportedException e) {
			// not going to happen
		}
		return t;
	}

	public void addDefinition(ICASTCompositeTypeSpecifier compositeTypeSpec) {
		if (compositeTypeSpec.isActive()) {
			definition = compositeTypeSpec.getName();
			compositeTypeSpec.getName().setBinding(this);
		}
	}

	public void addDeclaration(IASTName decl) {
		if (!decl.isActive() || decl.getPropertyInParent() != IASTElaboratedTypeSpecifier.TYPE_NAME)
			return;

		decl.setBinding(this);
		if (declarations == null || declarations.length == 0) {
			declarations = new IASTName[] { decl };
			return;
		}
		IASTName first = declarations[0];
		if (((ASTNode) first).getOffset() > ((ASTNode) decl).getOffset()) {
			declarations[0] = decl;
			decl = first;
		}
		declarations = ArrayUtil.append(IASTName.class, declarations, decl);
	}

	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof ITypedef || type instanceof IIndexBinding)
			return type.isSameType(this);
		return false;
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}

	@Override
	public IASTNode[] getDeclarations() {
		return declarations;
	}

	@Override
	public IASTNode getDefinition() {
		return definition;
	}

	@Override
	public IBinding getOwner() {
		IASTNode node = definition;
		if (node == null) {
			if (declarations != null && declarations.length > 0) {
				node = declarations[0];
			}
		}
		IBinding result = CVisitor.findEnclosingFunction(node); // local or global
		if (result != null)
			return result;

		if (definition != null && isAnonymous()) {
			return CVisitor.findDeclarationOwner(definition, false);
		}
		return null;
	}

	@Override
	public boolean isAnonymous() {
		if (getNameCharArray().length > 0 || definition == null)
			return false;

		IASTCompositeTypeSpecifier spec = ((IASTCompositeTypeSpecifier) definition.getParent());
		if (spec != null) {
			IASTNode node = spec.getParent();
			if (node instanceof IASTSimpleDeclaration) {
				if (((IASTSimpleDeclaration) node).getDeclarators().length == 0) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * For debugging purposes, only.
	 */
	@Override
	public String toString() {
		return getName();
	}
}

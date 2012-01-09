/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Sergey Prigogin (Google)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Binding for a class type.
 */
public class CPPClassType extends PlatformObject implements ICPPInternalClassTypeMixinHost {
	
	public static class CPPClassTypeProblem extends ProblemBinding implements ICPPClassType {
		public CPPClassTypeProblem(IASTName name, int id) {
			super(name, id);
		}
		public CPPClassTypeProblem(IASTNode node, int id, char[] arg) {
			super(node, id, arg);
		}
		@Override
		public ICPPBase[] getBases() {
			return ICPPBase.EMPTY_BASE_ARRAY;
		}
		@Override
		public IField[] getFields() {
			return IField.EMPTY_FIELD_ARRAY;
		}
		@Override
		public ICPPField[] getDeclaredFields() {
			return ICPPField.EMPTY_CPPFIELD_ARRAY;
		}
		@Override
		public ICPPMethod[] getMethods() {
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		}
		@Override
		public ICPPMethod[] getAllDeclaredMethods() {
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		}
		@Override
		public ICPPMethod[] getDeclaredMethods() {
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		}
		@Override
		public ICPPConstructor[] getConstructors() {
			return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
		}
		@Override
		public int getKey() {
			return k_class;
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
		public IBinding[] getFriends() {
			return IBinding.EMPTY_BINDING_ARRAY;
		}
		@Override
		public ICPPClassType[] getNestedClasses() {
			return ICPPClassType.EMPTY_CLASS_ARRAY;
		}
	}

	private IASTName definition;
	private IASTName[] declarations;
	private boolean checked = false;
	private ICPPClassType typeInIndex;

	public CPPClassType(IASTName name, IBinding indexBinding) {
		if (name instanceof ICPPASTQualifiedName) {
			IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
			name = ns[ns.length - 1];
		}
		IASTNode parent = name.getParent();
		while (parent instanceof IASTName)
			parent = parent.getParent();

		if (parent instanceof IASTCompositeTypeSpecifier)
			definition = name;
		else 
			declarations = new IASTName[] { name };
		name.setBinding(this);
		if (indexBinding instanceof ICPPClassType && indexBinding instanceof IIndexBinding) {
			typeInIndex= (ICPPClassType) indexBinding;
		}
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
	public void checkForDefinition() {
		// Ambiguity resolution ensures that definitions are resolved.
		if (!checked) {
			if (definition == null && typeInIndex == null) {
				IIndex index= getPhysicalNode().getTranslationUnit().getIndex();
				if (index != null) {
					typeInIndex= (ICPPClassType) index.adaptBinding(this);
				}
			}
			checked = true;
		}
	}

	@Override
	public ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier() {
		if (definition != null) {
			IASTNode node = definition;
			while (node instanceof IASTName)
				node = node.getParent();
			if (node instanceof ICPPASTCompositeTypeSpecifier)
				return (ICPPASTCompositeTypeSpecifier)node;
		}
		return null;
	}
	
	private ICPPASTElaboratedTypeSpecifier getElaboratedTypeSpecifier() {
		if (declarations != null) {
			IASTNode node = declarations[0];
			while (node instanceof IASTName)
				node = node.getParent();
			if (node instanceof ICPPASTElaboratedTypeSpecifier)
				return (ICPPASTElaboratedTypeSpecifier)node;
		}
		return null;
	}

	@Override
	public final String getName() {
		return new String(getNameCharArray());
	}

	@Override
	public char[] getNameCharArray() {
		return (definition != null) ? definition.getSimpleID() : declarations[0].getSimpleID();
	}

	@Override
	public IScope getScope() {
		IASTName name = definition != null ? definition : declarations[0];

		IScope scope = CPPVisitor.getContainingScope(name);
		if (definition == null && name.getPropertyInParent() != ICPPASTQualifiedName.SEGMENT_NAME) {
			IASTNode node = declarations[0].getParent().getParent();
			if (node instanceof IASTSimpleDeclaration && ((IASTSimpleDeclaration) node).getDeclarators().length == 0 
					&& !getElaboratedTypeSpecifier().isFriend()) {
				// 3.3.1.5 class-key identifier ;
			} else {
				while (scope instanceof ICPPClassScope || scope instanceof ICPPFunctionScope) {
					try {
						scope = scope.getParent();
					} catch (DOMException e1) {
					}
				}
			}
		}
		return scope;
	}

	@Override
	public ICPPClassScope getCompositeScope() {
		checkForDefinition();
		if (definition != null) {
			return getCompositeTypeSpecifier().getScope();
		}
		// fwd-declarations must be backed up from the index
		if (typeInIndex != null) {
			IScope scope = typeInIndex.getCompositeScope();
			if (scope instanceof ICPPClassScope)
				return (ICPPClassScope) scope;
		}
		return null;
	}

	public IASTNode getPhysicalNode() {
		return definition != null ? (IASTNode) definition : declarations[0];
	}

	@Override
	public int getKey() {
		if (definition != null)
			return getCompositeTypeSpecifier().getKey();

		return getElaboratedTypeSpecifier().getKind();
	}

	@Override
	public void addDefinition(IASTNode node) {
		if (node instanceof ICPPASTCompositeTypeSpecifier) {
			definition = ((ICPPASTCompositeTypeSpecifier)node).getName();
		} else {
			assert false;
		}
	}
	
	@Override
	public void addDeclaration(IASTNode node) {
		if (node instanceof ICPPASTElaboratedTypeSpecifier) {
			IASTName name = ((ICPPASTElaboratedTypeSpecifier) node).getName();

			if (declarations == null) {
				declarations = new IASTName[] { name };
				return;
			}

			// Keep the lowest offset declaration in [0]
			if (declarations.length > 0 && ((ASTNode)node).getOffset() < ((ASTNode) declarations[0]).getOffset()) {
				declarations = ArrayUtil.prepend(IASTName.class, declarations, name);
			} else {
				declarations = ArrayUtil.append(IASTName.class, declarations, name);
			}
		} else {
			assert false;
		}
	}

	@Override
	public String[] getQualifiedName() {
		return CPPVisitor.getQualifiedName(this);
	}

	@Override
	public char[][] getQualifiedNameCharArray() {
		return CPPVisitor.getQualifiedNameCharArray(this);
	}

	@Override
	public boolean isGloballyQualified() throws DOMException {
		IScope scope = getScope();
		while (scope != null) {
			if (scope instanceof ICPPBlockScope)
				return false;
			scope = scope.getParent();
		}
		return true;
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
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
	public ICPPBase[] getBases() {
		return ClassTypeHelper.getBases(this);
	}

	@Override
	public IField[] getFields() {
		return ClassTypeHelper.getFields(this);
	}

	@Override
	public ICPPField[] getDeclaredFields() {
		return ClassTypeHelper.getDeclaredFields(this);
	}

	@Override
	public ICPPMethod[] getMethods() {
		return ClassTypeHelper.getMethods(this);
	}

	@Override
	public ICPPMethod[] getAllDeclaredMethods() {
		return ClassTypeHelper.getAllDeclaredMethods(this);
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() {
		return ClassTypeHelper.getDeclaredMethods(this);
	}

	@Override
	public ICPPConstructor[] getConstructors() {
		return ClassTypeHelper.getConstructors(this);
	}

	@Override
	public IBinding[] getFriends() {
		return ClassTypeHelper.getFriends(this);
	}
	
	@Override
	public ICPPClassType[] getNestedClasses() {
		return ClassTypeHelper.getNestedClasses(this);
	}

	@Override
	public IField findField(String name) {
		return ClassTypeHelper.findField(this, name);
	}
	
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}
	
	/**
	 * For debugging purposes, only.
	 */
	@Override
	public String toString() {
		return getName(); 
	}

	@Override
	public IBinding getOwner() {
		if (definition != null) {
			return CPPVisitor.findNameOwner(definition, true);
		}
		return CPPVisitor.findDeclarationOwner(declarations[0], true);
	}
	
	@Override
	public boolean isAnonymous() {
		if (getNameCharArray().length > 0) 
			return false;
		
		ICPPASTCompositeTypeSpecifier spec= getCompositeTypeSpecifier(); 
		if (spec != null) {
			IASTNode node= spec.getParent();
			if (node instanceof IASTSimpleDeclaration) {
				if (((IASTSimpleDeclaration) node).getDeclarators().length == 0) {
					return true;
				}
			}
		}
		return false;
	}
}

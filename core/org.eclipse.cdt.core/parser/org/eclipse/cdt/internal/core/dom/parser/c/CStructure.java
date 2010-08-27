/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Markus Schorn (Wind River Systems) 
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
		public IField findField(String name) throws DOMException {
			throw new DOMException(this);
		}
		public IScope getCompositeScope() throws DOMException {
			throw new DOMException(this);
		}
		public IField[] getFields() throws DOMException {
			throw new DOMException(this);
		}
		public int getKey() throws DOMException {
			throw new DOMException(this);
		}
		public boolean isAnonymous() throws DOMException {
			throw new DOMException(this);
		}
	}

	private IASTName[] declarations = null;
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
	
    public IASTNode getPhysicalNode() {
        return (definition != null) ? (IASTNode)definition : (IASTNode)declarations[0];
    }
    
	private void checkForDefinition() {
		if (!checked && definition == null) {
			IASTNode declSpec = declarations[0].getParent();
			if (declSpec instanceof ICASTElaboratedTypeSpecifier) {
				IASTDeclSpecifier spec = CVisitor.findDefinition((ICASTElaboratedTypeSpecifier) declSpec);
				if (spec instanceof ICASTCompositeTypeSpecifier) {
					ICASTCompositeTypeSpecifier compTypeSpec = (ICASTCompositeTypeSpecifier) spec;
					definition= compTypeSpec.getName();
					definition.setBinding(this);
				}
			}

			if (definition == null && typeInIndex == null) {
				final IASTTranslationUnit translationUnit = declSpec.getTranslationUnit();
				IIndex index= translationUnit.getIndex();
				if (index != null) {
					typeInIndex= (ICompositeType) index.adaptBinding(this);
				}
			}
		}
		checked = true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		if (definition != null)
			return definition.toString();

		return declarations[0].toString();
	}
	public char[] getNameCharArray() {
		if (definition != null)
			return definition.toCharArray();

		return declarations[0].toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() throws DOMException {
	    IASTDeclSpecifier declSpec = (IASTDeclSpecifier) ((definition != null) ? (IASTNode)definition.getParent() : declarations[0].getParent());
		IScope scope = CVisitor.getContainingScope(declSpec);
		while(scope instanceof ICCompositeTypeScope) {
			scope = scope.getParent();
		}
		return scope;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getFields()
	 */
	public IField[] getFields() throws DOMException {
		checkForDefinition();
		if (definition == null) {
			return new IField[] { 
					new CField.CFieldProblem(declarations[0], IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray()) 
			};
		}
	    ICASTCompositeTypeSpecifier compSpec = (ICASTCompositeTypeSpecifier) definition.getParent();
		IField[] fields = collectFields(compSpec, null);
		return (IField[]) ArrayUtil.trim(IField.class, fields);
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
							fields= collectFields((ICASTCompositeTypeSpecifier) declspec, fields);
						}
					} else {
						for (IASTDeclarator declarator : declarators) {
							IASTName name = ASTQueries.findInnermostDeclarator(declarator).getName();
							IBinding binding = name.resolveBinding();
							if (binding != null)
								fields = (IField[]) ArrayUtil.append(IField.class, fields, binding);
						}
					}
				}
			}
		}
		return fields;
	}

	public IField findField(String name) throws DOMException {
		IScope scope = getCompositeScope();
		if (scope == null) {
			return new CField.CFieldProblem(declarations[0], IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray());
		}

		final CASTName astName = new CASTName(name.toCharArray());
		astName.setPropertyInParent(CVisitor.STRING_LOOKUP_PROPERTY);
		IBinding binding = scope.getBinding(astName, true);
		if (binding instanceof IField)
			return (IField) binding;
	
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getKey()
	 */
	public int getKey() {
		return (definition != null) ? ((IASTCompositeTypeSpecifier)definition.getParent()).getKey() 
		        					  : ((IASTElaboratedTypeSpecifier)declarations[0].getParent()).getKind();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getCompositeScope()
	 */
	public IScope getCompositeScope() {
		checkForDefinition();
		if (definition != null) {
			return ((IASTCompositeTypeSpecifier)definition.getParent()).getScope();
		}
		// fwd-declarations must be backed up from the index
		if (typeInIndex != null) {
			try {
				IScope scope = typeInIndex.getCompositeScope();
				if (scope instanceof ICCompositeTypeScope)
					return scope;
			} catch (DOMException e) {
				// index bindings don't throw DOMExeptions.
			}
		}
		return null;
	}
	
    @Override
	public Object clone() {
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch (CloneNotSupportedException e) {
            //not going to happen
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
		IASTName first= declarations[0];
		if (((ASTNode) first).getOffset() > ((ASTNode) decl).getOffset()) {
			declarations[0]= decl;
			decl= first;
		}
		declarations= (IASTName[]) ArrayUtil.append(IASTName.class, declarations, decl);
	}


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
     */
	public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof ITypedef || type instanceof IIndexBinding)
			return type.isSameType(this);
		return false;
	}
    
	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}

	public IASTNode[] getDeclarations() {
		return declarations;
	}

	public IASTNode getDefinition() {
		return definition;
	}
	
	public IBinding getOwner() throws DOMException {
		IASTNode node= definition;
		if (node == null) {
			if (declarations != null && declarations.length > 0) { 
				node= declarations[0];
			}
		}
		IBinding result= CVisitor.findEnclosingFunction(node); // local or global
		if (result != null)
			return result;
		
		if (definition != null && isAnonymous()) {
			return CVisitor.findDeclarationOwner(definition, false);
		}
		return null;
	}
	
	public boolean isAnonymous() throws DOMException {
		if (getNameCharArray().length > 0 || definition == null) 
			return false;
		
		IASTCompositeTypeSpecifier spec= ((IASTCompositeTypeSpecifier)definition.getParent());
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

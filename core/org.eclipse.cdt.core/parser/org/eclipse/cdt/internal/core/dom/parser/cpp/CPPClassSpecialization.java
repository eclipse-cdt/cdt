/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM) - Initial API and implementation
 *    Bryan Wilkinson (QNX)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * Specialization of a class.
 */
public class CPPClassSpecialization extends CPPSpecialization 
		implements ICPPClassSpecialization, ICPPInternalClassTypeMixinHost {

	private ICPPClassSpecializationScope specScope;
	private ObjectMap specializationMap= ObjectMap.EMPTY_MAP;
	private boolean checked;

	public CPPClassSpecialization(ICPPClassType specialized, IBinding owner, ICPPTemplateParameterMap argumentMap) {
		super(specialized, owner, argumentMap);
	}

	
	@Override
	public ICPPClassType getSpecializedBinding() {
		return (ICPPClassType) super.getSpecializedBinding();
	}
	
	public IBinding specializeMember(IBinding original) {		
		synchronized(this) {
			IBinding result= (IBinding) specializationMap.get(original);
			if (result != null) 
				return result;
		}
		
		IBinding result= CPPTemplates.createSpecialization(this, original);
		synchronized(this) {
			IBinding concurrent= (IBinding) specializationMap.get(original);
			if (concurrent != null) 
				return concurrent;
			if (specializationMap == ObjectMap.EMPTY_MAP)
				specializationMap = new ObjectMap(2);
			specializationMap.put(original, result);
			return result;
		}
	}
	
	private class FindDefinitionAction extends ASTVisitor {
		private char [] nameArray = CPPClassSpecialization.this.getNameCharArray();
		public IASTName result = null;

		{
			shouldVisitNames          = true;
			shouldVisitDeclarations   = true;
			shouldVisitDeclSpecifiers = true;
			shouldVisitDeclarators    = true;
		}

		@Override
		public int visit( IASTName name ){
			if( name instanceof ICPPASTTemplateId )
				return PROCESS_SKIP;
			if( name instanceof ICPPASTQualifiedName )
				return PROCESS_CONTINUE;
			char[] c = name.getLookupKey();

			if (name.getParent() instanceof ICPPASTQualifiedName) {
				IASTName[] ns = ((ICPPASTQualifiedName) name.getParent()).getNames();
				if (ns[ns.length - 1] != name)
					return PROCESS_CONTINUE;
				name = (IASTName) name.getParent();
			}

			if (name.getParent() instanceof ICPPASTCompositeTypeSpecifier && CharArrayUtils.equals(c, nameArray)) {
				IBinding binding = name.resolveBinding();
				if (binding == CPPClassSpecialization.this) {
					if (name instanceof ICPPASTQualifiedName) {
						IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
						name = ns[ns.length - 1];
					}
					result = name;
					return PROCESS_ABORT;
				}
			}
			return PROCESS_CONTINUE; 
		}

		@Override
		public int visit( IASTDeclaration declaration ){ 
			if(declaration instanceof IASTSimpleDeclaration || declaration instanceof ICPPASTTemplateDeclaration)
				return PROCESS_CONTINUE;
			return PROCESS_SKIP; 
		}
		@Override
		public int visit( IASTDeclSpecifier declSpec ){
			return (declSpec instanceof ICPPASTCompositeTypeSpecifier ) ? PROCESS_CONTINUE : PROCESS_SKIP; 
		}
		@Override
		public int visit( IASTDeclarator declarator ) 			{ return PROCESS_SKIP; }
	}

	public void checkForDefinition() {
		if( !checked && definition == null ) {
			IBinding orig= getSpecializedBinding();
			IASTTranslationUnit tu= null;
			while (orig != null) {
				if (orig instanceof ICPPInternalBinding) {
					IASTNode node= ((ICPPInternalBinding) orig).getDefinition();
					if (node != null)  {
						tu= node.getTranslationUnit();
						if (tu != null)
							break;
					}
				}
				if (!(orig instanceof ICPPSpecialization))
					break;
				orig= ((ICPPSpecialization) orig).getSpecializedBinding();
			}
			if (tu != null) {
				FindDefinitionAction action= new FindDefinitionAction();
				tu.accept( action );
				definition = action.result;
			}
			checked = true;
		}
		return;
	}

	public ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier() {
		IASTNode definition= getDefinition();
		if (definition != null) {
			IASTNode node= definition;
			while (node instanceof IASTName)
				node= node.getParent();
			if (node instanceof ICPPASTCompositeTypeSpecifier)
				return (ICPPASTCompositeTypeSpecifier) node;
		}
		return null;
	}
	
	public ICPPBase[] getBases() {
		ICPPClassSpecializationScope scope= getSpecializationScope();
		if (scope == null)
			return ClassTypeHelper.getBases(this);

		return scope.getBases();
	}

	public ICPPField[] getDeclaredFields() {
		ICPPClassSpecializationScope scope= getSpecializationScope();
		if (scope == null)
			return ClassTypeHelper.getDeclaredFields(this);

		return scope.getDeclaredFields();
	}

	public ICPPMethod[] getDeclaredMethods() {
		ICPPClassSpecializationScope scope= getSpecializationScope();
		if (scope == null)
			return ClassTypeHelper.getDeclaredMethods(this);

		return scope.getDeclaredMethods();
	}

	public ICPPConstructor[] getConstructors() {
		ICPPClassSpecializationScope scope= getSpecializationScope();
		if (scope == null)
			return ClassTypeHelper.getConstructors(this);

		return scope.getConstructors();
	}

	public IBinding[] getFriends() {
		ICPPClassSpecializationScope scope= getSpecializationScope();
		if (scope == null)
			return ClassTypeHelper.getFriends(this);

		return scope.getFriends();
	}
	
	public ICPPClassType[] getNestedClasses() {
		ICPPClassSpecializationScope scope= getSpecializationScope();
		if (scope == null)
			return ClassTypeHelper.getNestedClasses(this);

		return scope.getNestedClasses();
	}


	public IField[] getFields() {
		return ClassTypeHelper.getFields(this);
	}

	public IField findField(String name) {
		return ClassTypeHelper.findField(this, name);
	}

	public ICPPMethod[] getMethods() {
		return ClassTypeHelper.getMethods(this);
	}

	public ICPPMethod[] getAllDeclaredMethods() {
		return ClassTypeHelper.getAllDeclaredMethods(this);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getKey()
	 */
	public int getKey() {
		if (getDefinition() != null)
			return getCompositeTypeSpecifier().getKey();
		
		return (getSpecializedBinding()).getKey();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getCompositeScope()
	 */
	public ICPPClassScope getCompositeScope() {
		final ICPPClassScope specScope= getSpecializationScope();
		if (specScope != null)
			return specScope;
		
		return getCompositeTypeSpecifier().getScope();
	}
	
	private ICPPClassSpecializationScope getSpecializationScope() {
		checkForDefinition();
		if (getDefinition() != null)
			return null;
		
		//implicit specialization: must specialize bindings in scope
		if (specScope == null) {
			specScope = new CPPClassSpecializationScope(this);
		}
		return specScope;		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
	 */
	public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof ITypedef)
			return type.isSameType(this);

		if (type instanceof ICPPClassSpecialization) {
			return isSameClassSpecialization(this, (ICPPClassSpecialization) type);
		}
		return false;
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return this;
	}

	public boolean isAnonymous() {
		if (getNameCharArray().length > 0) 
			return false;
		
		ICPPASTCompositeTypeSpecifier spec= getCompositeTypeSpecifier(); 
		if (spec == null) {
			return getSpecializedBinding().isAnonymous();
		}

		IASTNode node= spec.getParent();
		if (node instanceof IASTSimpleDeclaration) {
			if (((IASTSimpleDeclaration) node).getDeclarators().length == 0) {
				return true;
			}
		}
		return false;
	}


	public static boolean isSameClassSpecialization(ICPPClassSpecialization t1, ICPPClassSpecialization t2) {
		// exclude class template specialization or class instance
		if (t2 instanceof ICPPTemplateInstance || t2 instanceof ICPPTemplateDefinition || 
				t2 instanceof IProblemBinding)
			return false;
		
		if (t1.getKey() != t2.getKey()) 
			return false;
		
		if (!CharArrayUtils.equals(t1.getNameCharArray(), t2.getNameCharArray()))
			return false;
		
		// the argument map is not significant for comparing specializations, the map is
		// determined by the owner of the specialization. This is different for instances,
		// which have a separate implementation for isSameType().
		final IBinding owner1= t1.getOwner();
		final IBinding owner2= t2.getOwner();
		
		// for a specialization that is not an instance the owner has to be a class-type
		if (owner1 instanceof ICPPClassType == false || owner2 instanceof ICPPClassType == false)
			return false;

		return ((ICPPClassType) owner1).isSameType((ICPPClassType) owner2);
	}
}

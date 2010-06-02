/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTVisibilityLabel;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;

/**
 * Represents a function or method and adds some useful helper methods to
 * determine if methods are in the same class.
 *
 */
public class MethodContext {
	public enum ContextType{ NONE, FUNCTION, METHOD }

	private ContextType type;
	private IASTName declarationName;
	private ICPPASTQualifiedName qname;

	public ContextType getType() {
		return type;
	}

	public void setType(ContextType type) {
		this.type = type;
	}

	public void setMethodDeclarationName(IASTName tmpname) {
		this.declarationName=tmpname;
	}

	public IASTName getMethodDeclarationName(){
		return declarationName;
	}
	
	public IASTDeclaration getMethodDeclaration(){
		IASTNode parent = declarationName.getParent().getParent();
		if (parent instanceof IASTDeclaration) {
			return (IASTDeclaration) parent;
		}
		return null;
	}
	
	public ICPPASTVisibilityLabel getMethodDeclarationASTVisibility(){
		ICPPASTVisibilityLabel label = new CPPASTVisibilityLabel();
		ICPPMember member = ((ICPPMember)qname.resolveBinding());			

		try {
			label.setVisibility(member.getVisibility());
		} catch (DOMException e) {
			CUIPlugin.log(e);
		}
		return label;
	}
	
	public Visibility getMethodDeclarationVisibility(){
		return Visibility.getVisibility(declarationName);
	}

	public void setMethodQName(ICPPASTQualifiedName qname) {
		this.qname = qname;
	}
	
	public ICPPASTQualifiedName getMethodQName() {
		return qname;
	}
	
	public static boolean isSameClass(ICPPASTQualifiedName qname1, ICPPASTQualifiedName qname2) {
		ICPPClassType bind1 = getClassBinding(qname1);
		ICPPClassType bind2 = getClassBinding(qname2);
		return bind1.equals(bind2);
	}
	
	public static boolean isSameOrSubClass(MethodContext context1, MethodContext contextOfSameOrSubclass) {
		ICPPInternalBinding bind1 = getICPPInternalBinding(context1);
		ICPPInternalBinding subclassBind = getICPPInternalBinding(contextOfSameOrSubclass);
		if(isSameClass(bind1,subclassBind)){
			return true;
		}
		return isSubclass(bind1,subclassBind);
	}
	
	private static boolean isSubclass(ICPPInternalBinding bind1, ICPPInternalBinding subclassBind) {
		if (subclassBind instanceof ICPPClassType) {
			ICPPClassType classType = (ICPPClassType) subclassBind;
			ICPPBase[] bases;
			try {
				bases = classType.getBases();
			} catch (DOMException e) {
				return false;
			}
			for (ICPPBase base : bases) {
				if(isSameClass(base,bind1)){
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isSameClass(MethodContext context1, MethodContext context2) {
		ICPPInternalBinding bind1 = getICPPInternalBinding(context1);
		ICPPInternalBinding bind2 = getICPPInternalBinding(context2);

		return isSameClass(bind1,bind2);
	}
	
	private static boolean isSameClass(ICPPBase base, ICPPInternalBinding bind2) {
		try {
			IBinding bind1 = base.getBaseClass();
			IScope scope1 = bind1.getScope();
			if(scope1 == null)
				return false;
			IASTNode node1 = ASTInternal.getPhysicalNodeOfScope(scope1);
			
			IScope scope2 = bind2.getScope();
			if(scope2 == null)
				return false;
			IASTNode node2 = ASTInternal.getPhysicalNodeOfScope(scope2);
			
			if( node1.equals(node2) ){
				if (bind1 instanceof ICPPInternalBinding) {
					ICPPInternalBinding bind1int = (ICPPInternalBinding) bind1;
					return bind1int.getDefinition().equals(bind2.getDefinition());
				}
				return false;
			}
			return false;
		} catch (DOMException e) {
			return false;
		}	
	}
	
	private static boolean isSameClass(ICPPInternalBinding bind1, ICPPInternalBinding bind2) {
		try {
			IScope scope1 = bind1.getScope();
			if(scope1 == null)
				return false;
			IASTNode node1 = ASTInternal.getPhysicalNodeOfScope(scope1);
			
			IScope scope2 = bind2.getScope();
			if(scope2 == null)
				return false;
			IASTNode node2 = ASTInternal.getPhysicalNodeOfScope(scope2);
			
			if( node1.equals(node2) ){
				return bind1.getDefinition().equals(bind2.getDefinition());
			}
			return false;
		} catch (DOMException e) {
			return false;
		}	
	}
	
	public static ICPPInternalBinding getICPPInternalBinding(MethodContext context) {
		IASTName decl = context.getMethodDeclarationName();
		IASTNode node = decl;
		while(node != null){
			if (node instanceof ICPPASTCompositeTypeSpecifier) {
				ICPPASTCompositeTypeSpecifier type = (ICPPASTCompositeTypeSpecifier) node;
				IASTName classname = type.getName();
				ICPPInternalBinding bind = (ICPPInternalBinding)classname.resolveBinding();
				 
				return bind;
			}
			
			node = node.getParent();
		}
		
		return null;
	}
	
	public boolean isInline() {
		return qname == null;
	}
	
	private static ICPPClassType getClassBinding(ICPPASTQualifiedName qname){
		IASTName classname = qname.getNames()[qname.getNames().length - 2];
		ICPPClassType bind = (ICPPClassType)classname.resolveBinding(); 
		return bind;
	}

	public static boolean isSameClass(IASTName declName1,
			IASTName declName2) {
		ICPPClassType bind1 = getClassBinding(declName1);
		ICPPClassType bind2 = getClassBinding(declName2);
		return bind1.equals(bind2);
	}

	private static ICPPClassType getClassBinding(IASTName declName1) {
		if (declName1.getParent().getParent().getParent() instanceof ICPPASTCompositeTypeSpecifier) {
			ICPPASTCompositeTypeSpecifier compTypeSpec = (ICPPASTCompositeTypeSpecifier) declName1.getParent().getParent().getParent();
			return (ICPPClassType) compTypeSpec.getName().resolveBinding();
		}
		return null;
	}
}

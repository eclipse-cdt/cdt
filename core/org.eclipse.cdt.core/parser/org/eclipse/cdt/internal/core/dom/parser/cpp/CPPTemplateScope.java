/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
/*
 * Created on Mar 11, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;

/**
 * @author aniefer
 */
public class CPPTemplateScope extends CPPScope implements ICPPTemplateScope {

	//private ICPPTemplateDefinition primaryDefinition;
	/**
	 * @param physicalNode
	 */
	public CPPTemplateScope(IASTNode physicalNode) {
		super(physicalNode);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope#getTemplateDefinition()
	 */
	public ICPPTemplateDefinition getTemplateDefinition() {
//		if( primaryDefinition == null ){
//			//primaryDefinition = CPPTemplates.getTemplateDefinition( this );
//			ICPPASTTemplateDeclaration template = (ICPPASTTemplateDeclaration) getPhysicalNode();
//			IASTDeclaration decl = template.getDeclaration();
//			return new CPPTemplateDefinition( decl );
//		}
//		return primaryDefinition;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#getScopeName()
	 */
	public IASTName getScopeName() {
		// TODO Auto-generated method stub
		return null;
	}

	public IScope getParent() {
	    ICPPASTTemplateDeclaration templateDecl = (ICPPASTTemplateDeclaration) getPhysicalNode();
	    IASTName name = CPPTemplates.getTemplateName( templateDecl );
	    IASTNode p = name.getParent();
	    if( p instanceof ICPPASTQualifiedName ){
	        ICPPASTQualifiedName qual = (ICPPASTQualifiedName) p;
	        IASTName [] names = qual.getNames();
	        int i = 0;
			for( ; i < names.length; i++ ){
				if( names[i] == name ) break;
			}
			if( i > 0 ){
			    try {
					IBinding binding = names[i - 1].resolveBinding();
					if( binding instanceof ICPPClassType ){
						return ((ICPPClassType)binding).getCompositeScope();
					} else if( binding instanceof ICPPNamespace ){
						return ((ICPPNamespace)binding).getNamespaceScope();
					} else if( binding instanceof ICPPInternalUnknown ){
					    return ((ICPPInternalUnknown)binding).getUnknownScope();
					} else if( binding instanceof IProblemBinding ){
						if( binding instanceof ICPPScope )
							return (IScope) binding;
						return new CPPScope.CPPScopeProblem( names[i-1], IProblemBinding.SEMANTIC_BAD_SCOPE, names[i-1].toCharArray() );
					}
			    } catch( DOMException e ){
			        return e.getProblem(); 
			    }
			} else if( qual.isFullyQualified() ){
			    return qual.getTranslationUnit().getScope();
			}
	    }
	    while( templateDecl.getParent() instanceof ICPPASTTemplateDeclaration )
	    	templateDecl = (ICPPASTTemplateDeclaration) templateDecl.getParent();
	    return CPPVisitor.getContainingScope( templateDecl );
	}
}

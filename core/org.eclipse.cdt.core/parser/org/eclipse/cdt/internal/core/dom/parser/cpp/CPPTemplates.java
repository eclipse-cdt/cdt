/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
/*
 * Created on Mar 11, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;

/**
 * @author aniefer
 */
public class CPPTemplates {

	public static IASTName getTemplateParameterName( ICPPASTTemplateParameter param ){
		if( param instanceof ICPPASTSimpleTypeTemplateParameter )
			return ((ICPPASTSimpleTypeTemplateParameter)param).getName();
		else if( param instanceof ICPPASTTemplatedTypeTemplateParameter )
			return ((ICPPASTTemplatedTypeTemplateParameter)param).getName();
		else if( param instanceof ICPPASTParameterDeclaration )
			return ((ICPPASTParameterDeclaration)param).getDeclarator().getName();
		return null;
	}
	
	public static IBinding createBinding( ICPPASTTemplateParameter templateParameter ){
		ICPPTemplateScope scope = (ICPPTemplateScope) getContainingScope( templateParameter );
		IASTName name = getTemplateParameterName( templateParameter );
		IBinding binding = null;

		try {
			binding = scope.getBinding( name, false );
			if( binding == null ){
				binding = new CPPTemplateParameter( name );
		        scope.addName( name );
    	    }
        } catch ( DOMException e ) {
            binding = e.getProblem();
        }
        
	    return binding;
	}
	static public ICPPScope getContainingScope( IASTNode node ){
		while( node != null ){
			if( node instanceof ICPPASTTemplateParameter ){
				IASTNode parent = node.getParent();
				if( parent instanceof ICPPASTTemplateDeclaration ){
					return ((ICPPASTTemplateDeclaration)parent).getScope();
				}
			}
			node = node.getParent();
		}
		
		return null;
	}

	/**
	 * @param id
	 * @return
	 */
	public static IBinding createBinding(ICPPASTTemplateId id) {
		IASTName templateName = id.getTemplateName();
		IBinding template = templateName.resolveBinding();
//		if( template != null && template instanceof ICPPTemplateDefinition ){
//			return ((ICPPTemplateDefinition)template).instantiate( id.getTemplateArguments() );
//		}
		return template;
	}

	/**
	 * @param scope
	 * @return
	 */
	public static ICPPTemplateDefinition getTemplateDefinition(ICPPTemplateScope scope) {
		if( scope != null ) {}
		return null;
	}
}

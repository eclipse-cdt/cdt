/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Dec 1, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;

/**
 * @author aniefer
 */
public class CPPNamespace implements ICPPNamespace {
	private static final char[] EMPTY_CHAR_ARRAY = { };
	
	ICPPASTNamespaceDefinition [] namespaceDefinitions = null;
	ICPPASTTranslationUnit tu = null;
	public CPPNamespace( ICPPASTNamespaceDefinition nsDef ){
		namespaceDefinitions = new ICPPASTNamespaceDefinition[] { nsDef };
	}
	
	/**
	 * @param unit
	 */
	public CPPNamespace(CPPASTTranslationUnit unit) {
		tu = unit;
	}

	public void addDefinition( ICPPASTNamespaceDefinition nsDef ){
		if( namespaceDefinitions == null )
			return;
		for( int i = 0; i < namespaceDefinitions.length; i++ ){
			if( namespaceDefinitions[i] == null ){
				namespaceDefinitions[i] = nsDef;
				return;
			}
		}
		ICPPASTNamespaceDefinition [] temp = new ICPPASTNamespaceDefinition[ namespaceDefinitions.length * 2 ];
		System.arraycopy( namespaceDefinitions, 0, temp, 0, namespaceDefinitions.length );
		temp[ namespaceDefinitions.length ] = nsDef;
		namespaceDefinitions = temp;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace#getNamespaceScope()
	 */
	public ICPPNamespaceScope getNamespaceScope() {
		
		return (ICPPNamespaceScope) ( tu != null ? tu.getScope() : namespaceDefinitions[0].getScope() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return tu != null ? null : namespaceDefinitions[0].getName().toString(); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		return tu != null ? EMPTY_CHAR_ARRAY : namespaceDefinitions[0].getName().toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return tu != null ? null : CPPVisitor.getContainingScope( namespaceDefinitions[0] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getPhysicalNode()
	 */
	public IASTNode getPhysicalNode() {
		return ( tu != null ? (IASTNode) tu : namespaceDefinitions[0] );
	}

}

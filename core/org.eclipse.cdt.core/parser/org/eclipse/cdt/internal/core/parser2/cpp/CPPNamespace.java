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
package org.eclipse.cdt.internal.core.parser2.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;

/**
 * @author aniefer
 */
public class CPPNamespace implements ICPPNamespace {
	ICPPASTNamespaceDefinition [] namespaceDefinitions = null;

	public CPPNamespace( ICPPASTNamespaceDefinition nsDef ){
		namespaceDefinitions = new ICPPASTNamespaceDefinition[] { nsDef };
	}
	
	public void addDefinition( ICPPASTNamespaceDefinition nsDef ){
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
		return (ICPPNamespaceScope) namespaceDefinitions[0].getScope();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return namespaceDefinitions[0].getName().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		return namespaceDefinitions[0].getName().toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return CPPVisitor.getContainingScope( namespaceDefinitions[0] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getPhysicalNode()
	 */
	public IASTNode getPhysicalNode() {
		return namespaceDefinitions[0];
	}

}

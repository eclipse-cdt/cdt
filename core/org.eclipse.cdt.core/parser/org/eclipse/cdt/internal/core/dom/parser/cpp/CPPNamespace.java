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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * @author aniefer
 */
public class CPPNamespace implements ICPPNamespace, ICPPBinding {
	private static final char[] EMPTY_CHAR_ARRAY = { };
	
	IASTName [] namespaceDefinitions = null;
	ICPPNamespaceScope scope = null;
	
	ICPPASTTranslationUnit tu = null;
	public CPPNamespace( IASTName nsDef ){
	    findAllDefinitions( nsDef );
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDeclarations()
     */
    public IASTNode[] getDeclarations() {
        return namespaceDefinitions;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDefinition()
     */
    public IASTNode getDefinition() {
        return ( tu != null ) ? tu : (IASTNode) namespaceDefinitions[0];
    }

	static private class NamespaceCollector extends CPPVisitor.CPPBaseVisitorAction {
	    private char [] name;
	    public List namespaces = Collections.EMPTY_LIST;
	    public int processResult = PROCESS_SKIP;
	    
	    public NamespaceCollector( IASTName name  ){
	        processNamespaces = true;
	        processDeclarations = true;
	        this.name = name.toCharArray();
	    }

	    public int processNamespace( ICPPASTNamespaceDefinition namespace) {
	        if( CharArrayUtils.equals( namespace.getName().toCharArray(), name ) ){
	            if( namespaces == Collections.EMPTY_LIST )
	                namespaces = new ArrayList();
	            namespaces.add( namespace.getName() );
	        }
	        if( processResult == PROCESS_CONTINUE ){
	            processResult = PROCESS_SKIP;
	            return PROCESS_CONTINUE;
	        }
	            
	        return processResult; 
	    }
	    
	    public int processDeclaration( IASTDeclaration declaration ){
	        return PROCESS_SKIP;
	    }
	}
	
	private void findAllDefinitions( IASTName namespaceName ){
	    NamespaceCollector collector = new NamespaceCollector( namespaceName );
	    ICPPASTNamespaceDefinition nsDef = (ICPPASTNamespaceDefinition) namespaceName.getParent();
	    IASTNode node = nsDef.getParent();
	    
	    if( node instanceof IASTTranslationUnit )
	        CPPVisitor.visitTranslationUnit( (IASTTranslationUnit) node, collector );
	    else if( node instanceof ICPPASTNamespaceDefinition ){
	        collector.processResult = CPPVisitor.CPPBaseVisitorAction.PROCESS_CONTINUE;
	        CPPVisitor.visitNamespaceDefinition( (ICPPASTNamespaceDefinition) node, collector );
	    }
	    
	    int size = collector.namespaces.size();
	    namespaceDefinitions = new IASTName [ size ];
	    for( int i = 0; i < size; i++ ){
	        namespaceDefinitions[i] = (IASTName) collector.namespaces.get(i);
	        ((CPPASTName)namespaceDefinitions[i]).setBinding( this );
	    }
	}
	
	public IASTName [] getNamespaceDefinitions() {
	    return namespaceDefinitions;
	}
	
	/**
	 * @param unit
	 */
	public CPPNamespace(CPPASTTranslationUnit unit) {
		tu = unit;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace#getNamespaceScope()
	 */
	public ICPPNamespaceScope getNamespaceScope() {
		if( scope == null ){
		    if( tu != null )
		        scope = (ICPPNamespaceScope) tu.getScope();
		    else
		        scope = new CPPNamespaceScope( namespaceDefinitions[0].getParent() );
		}
		return scope;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return tu != null ? null : namespaceDefinitions[0].toString(); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		return tu != null ? EMPTY_CHAR_ARRAY : namespaceDefinitions[0].toCharArray();
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

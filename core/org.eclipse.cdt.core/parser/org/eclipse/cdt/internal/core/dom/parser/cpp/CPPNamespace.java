/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * @author aniefer
 */
public class CPPNamespace implements ICPPNamespace, ICPPInternalBinding {
    public static class CPPNamespaceDelegate extends CPPDelegate implements ICPPNamespace {
        public CPPNamespaceDelegate( IASTName name, ICPPNamespace binding ) {
            super( name, binding );
        }
        public ICPPNamespaceScope getNamespaceScope() throws DOMException {
            return ((ICPPNamespace)getBinding()).getNamespaceScope();
        }
    }
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

	static private class NamespaceCollector extends CPPASTVisitor {
	    private char [] name;
	    public List namespaces = Collections.EMPTY_LIST;
	    public int processResult = PROCESS_SKIP;
	    
	    public NamespaceCollector( IASTName name  ){
	        shouldVisitNamespaces = true;
	        shouldVisitDeclarations = true;
	        this.name = name.toCharArray();
	    }

	    public int visit( ICPPASTNamespaceDefinition namespace) {
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
	    
	    public int visit( IASTDeclaration declaration ){
	        if( declaration instanceof ICPPASTLinkageSpecification )
	            return PROCESS_CONTINUE;
	        return PROCESS_SKIP;
	    }
	}
	
	private void findAllDefinitions( IASTName namespaceName ){
	    NamespaceCollector collector = new NamespaceCollector( namespaceName );
	    ICPPASTNamespaceDefinition nsDef = (ICPPASTNamespaceDefinition) namespaceName.getParent();
	    IASTNode node = nsDef.getParent();
	    
	    while( node instanceof ICPPASTLinkageSpecification )
	        node = node.getParent();
	  
	    if( node instanceof ICPPASTNamespaceDefinition ){
	        collector.processResult = ASTVisitor.PROCESS_CONTINUE;
	    }
	    
	    node.accept( collector );
	    
	    int size = collector.namespaces.size();
	    namespaceDefinitions = new IASTName [ size ];
	    for( int i = 0; i < size; i++ ){
	        namespaceDefinitions[i] = (IASTName) collector.namespaces.get(i);
	        namespaceDefinitions[i].setBinding( this );
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

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedName()
     */
    public String[] getFullyQualifiedName() {
        return CPPVisitor.getQualifiedName( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedNameCharArray()
     */
    public char[][] getFullyQualifiedNameCharArray() {
        return CPPVisitor.getQualifiedNameCharArray( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedName()
     */
    public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedNameCharArray()
     */
    public char[][] getQualifiedNameCharArray() {
        return CPPVisitor.getQualifiedNameCharArray( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
     */
    public boolean isGloballyQualified() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public ICPPDelegate createDelegate( IASTName name ) {
        return new CPPNamespaceDelegate( name, this );
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDefinition(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDefinition(IASTNode node) {
		if( node instanceof IASTName )
			namespaceDefinitions = (IASTName[]) ArrayUtil.append( IASTName.class, namespaceDefinitions, node );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDeclaration(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDeclaration(IASTNode node) {
		addDefinition( node );
	}

}

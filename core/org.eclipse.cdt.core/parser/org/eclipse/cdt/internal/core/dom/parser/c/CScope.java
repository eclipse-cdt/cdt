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
 * Created on Nov 25, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * @author aniefer
 */
public class CScope implements ICScope {
	/**
	 * ISO C:99 6.2.3 there are seperate namespaces for various categories of
	 * identifiers: - label names ( labels have ICFunctionScope ) - tags of
	 * structures or unions : NAMESPACE_TYPE_TAG - members of structures or
	 * unions ( members have ICCompositeTypeScope ) - all other identifiers :
	 * NAMESPACE_TYPE_OTHER
	 */
	public static final int NAMESPACE_TYPE_TAG = 0;
	public static final int NAMESPACE_TYPE_OTHER = 1;
	
    private IASTNode physicalNode = null;
    private boolean isFullyCached = false;
    
    private CharArrayObjectMap [] bindings = { CharArrayObjectMap.EMPTY_MAP, CharArrayObjectMap.EMPTY_MAP };
    
    public CScope( IASTNode physical ){
        physicalNode = physical;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
     */
    public IScope getParent() {
        return CVisitor.getContainingScope( physicalNode );
    }

    protected static class CollectNamesAction extends CASTVisitor {
        private char [] name;
        private IASTName [] result = null;
        CollectNamesAction( char [] n ){
            name = n;
            shouldVisitNames = true;
        }
        public int visit( IASTName n ){
            ASTNodeProperty prop = n.getPropertyInParent();
            if( prop == IASTElaboratedTypeSpecifier.TYPE_NAME ||
                prop == IASTCompositeTypeSpecifier.TYPE_NAME ||
                prop == IASTDeclarator.DECLARATOR_NAME )
            {
                if( CharArrayUtils.equals( n.toCharArray(), name ) )
                    result = (IASTName[]) ArrayUtil.append( IASTName.class, result, n );    
            }
            
            return PROCESS_CONTINUE; 
        }
        public int visit( IASTStatement statement ){
            if( statement instanceof IASTDeclarationStatement )
                return PROCESS_CONTINUE;
            return PROCESS_SKIP;
        }
        public IASTName [] getNames(){
            return (IASTName[]) ArrayUtil.trim( IASTName.class, result );
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
     */
    public IBinding[] find( String name ) throws DOMException {
        return CVisitor.findBindings( this, name );
    }

    public IBinding getBinding( int namespaceType, char [] name ){
        IASTName n = (IASTName) bindings[namespaceType].get( name );
        return ( n != null ) ? n.resolveBinding() : null;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICScope#removeBinding(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public void removeBinding(IBinding binding) {
        int type = ( binding instanceof ICompositeType || binding instanceof IEnumeration ) ? 
				NAMESPACE_TYPE_TAG : NAMESPACE_TYPE_OTHER;

		if( bindings[type] != CharArrayObjectMap.EMPTY_MAP ) {
			bindings[type].remove( binding.getNameCharArray(), 0, binding.getNameCharArray().length);
		}
		isFullyCached = false;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getPhysicalNode()
     */
    public IASTNode getPhysicalNode() {
        return physicalNode;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICScope#addName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void addName( IASTName name ) {
        int type = getNamespaceType( name );
        if( bindings[type] == CharArrayObjectMap.EMPTY_MAP )
            bindings[type] = new CharArrayObjectMap(1);
        
        char [] n = name.toCharArray();
        IASTName current = (IASTName) bindings[type].get( n );
        if( current == null || ((CASTName)current).getOffset() > ((CASTName) name).getOffset() ){
            bindings[type].put( n, name );
        }
    }

    private int getNamespaceType( IASTName name ){
        ASTNodeProperty prop = name.getPropertyInParent();
        if( prop == IASTCompositeTypeSpecifier.TYPE_NAME ||
            prop == IASTElaboratedTypeSpecifier.TYPE_NAME ||
            prop == IASTEnumerationSpecifier.ENUMERATION_NAME ||
            prop == CVisitor.STRING_LOOKUP_TAGS_PROPERTY )
        {
            return NAMESPACE_TYPE_TAG;
        }
        
        return NAMESPACE_TYPE_OTHER;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICScope#getBinding(org.eclipse.cdt.core.dom.ast.IASTName, boolean)
     */
    public IBinding getBinding( IASTName name, boolean resolve ) {
        char [] c = name.toCharArray();
        if( c.length == 0  ){
            return null;
        }
        
        int type = getNamespaceType( name );
        Object o = bindings[type].get( name.toCharArray() );
        
        if( o == null ) 
            return null;
        
        if( o instanceof IBinding )
            return (IBinding) o;

        if( (resolve || ((IASTName)o).getBinding() != null) && ( o != name ) )
            return ((IASTName)o).resolveBinding();

        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICScope#setFullyCached(boolean)
     */
    public void setFullyCached( boolean b ){
        isFullyCached = b;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICScope#isFullyCached()
     */
    public boolean isFullyCached(){
        return isFullyCached;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#getScopeName()
	 */
	public IASTName getScopeName() throws DOMException {
		if( physicalNode instanceof IASTCompositeTypeSpecifier ){
			return ((IASTCompositeTypeSpecifier) physicalNode).getName();
		}
		return null;
	}
}

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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTVisitor;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;
import org.eclipse.cdt.core.dom.ast.c.ICScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.c.CScope.CollectNamesAction;

/**
 * @author aniefer
 */
public class CCompositeTypeScope implements ICCompositeTypeScope {
    private ICASTCompositeTypeSpecifier compositeTypeSpec = null;
    
    private CharArrayObjectMap bindings = CharArrayObjectMap.EMPTY_MAP;
    
    public CCompositeTypeScope( ICASTCompositeTypeSpecifier compTypeSpec ){
        compositeTypeSpec = compTypeSpec;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICScope#addBinding(org.eclipse.cdt.core.dom.ast.IBinding)
     */
    public void addBinding( IBinding binding ) {
        if( bindings == CharArrayObjectMap.EMPTY_MAP )
            bindings = new CharArrayObjectMap( 1 );
        bindings.put( binding.getNameCharArray(), binding );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICScope#getBinding(int, char[])
     */
    public IBinding getBinding( int namespaceType, char[] name ) {
        if( namespaceType == ICScope.NAMESPACE_TYPE_OTHER )
            return getBinding( name );
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope#getBinding(char[])
     */
    public IBinding getBinding( char[] name ) {
        return (IBinding) bindings.get( name );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
     */
    public IScope getParent() {
        return CVisitor.getContainingScope( compositeTypeSpec );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
     */
    public IBinding[] find( String name ) {
        IASTNode node = getPhysicalNode();
        IASTTranslationUnit tu = node.getTranslationUnit();
        IASTVisitor visitor = tu.getVisitor();
        
        CollectNamesAction action = new CollectNamesAction( name.toCharArray() );
        visitor.visitDeclSpecifier( compositeTypeSpec, action );
        
        IASTName [] names = action.getNames();
        IBinding [] result = null;
        for( int i = 0; i < names.length; i++ ){
            IBinding b = names[i].resolveBinding();
            if( b == null ) continue;
            try {
                if( b.getScope() == this )
                    result = (IBinding[]) ArrayUtil.append( IBinding.class, result, b );
            } catch ( DOMException e ) {
            }
        }
            
        return (IBinding[]) ArrayUtil.trim( IBinding.class, result );
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICScope#removeBinding(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public void removeBinding(IBinding binding) {
		if( bindings != CharArrayObjectMap.EMPTY_MAP ) {
			bindings.remove( binding.getNameCharArray(), 0, binding.getNameCharArray().length);
		}
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getPhysicalNode()
     */
    public IASTNode getPhysicalNode() {
        return compositeTypeSpec;
    }
}

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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTVisitor;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * @author aniefer
 */
public class CScope implements ICScope {
    private IASTNode physicalNode = null;
    
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

    protected static class CollectNamesAction extends ICASTVisitor.CBaseVisitorAction {
        private char [] name;
        private IASTName [] result = null;
        CollectNamesAction( char [] n ){
            name = n;
            processNames = true;
        }
        public int processName( IASTName n ){
            ASTNodeProperty prop = n.getPropertyInParent();
            if( prop == IASTElaboratedTypeSpecifier.TYPE_NAME ||
                prop == IASTCompositeTypeSpecifier.TYPE_NAME ||
                prop == IASTDeclarator.DECLARATOR_NAME )
            {
                if( CharArrayUtils.equals( n.toCharArray(), name ) )
                    result = (IASTName[]) ArrayUtil.append( IASTName.class, result, name );    
            }
            
            return PROCESS_CONTINUE; 
        }
        public int processStatement( IASTStatement statement ){
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
    public IBinding[] find( String name ) {
        IASTNode node = getPhysicalNode();
        IASTTranslationUnit tu = node.getTranslationUnit();
        IASTVisitor visitor = tu.getVisitor();
        
        CollectNamesAction action = new CollectNamesAction( name.toCharArray() );
        if( node instanceof IASTTranslationUnit )
            visitor.visitTranslationUnit( action );
        else if( node instanceof IASTStatement )
            visitor.visitStatement( (IASTStatement) node, action );
        
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

    public void addBinding( IBinding binding ) {
        int type = ( binding instanceof ICompositeType || binding instanceof IEnumeration ) ? 
                						NAMESPACE_TYPE_TAG : NAMESPACE_TYPE_OTHER;

        if( bindings[type] == CharArrayObjectMap.EMPTY_MAP )
            bindings[type] = new CharArrayObjectMap(1);
        bindings[type].put( binding.getNameCharArray(), binding );
    }
    
    public IBinding getBinding( int namespaceType, char [] name ){
        return (IBinding) bindings[namespaceType].get( name );
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
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getPhysicalNode()
     */
    public IASTNode getPhysicalNode() {
        return physicalNode;
    }
}

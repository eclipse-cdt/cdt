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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPSemantics.LookupData;

/**
 * @author aniefer
 */
public class CPPFunctionScope extends CPPScope implements ICPPFunctionScope {

    private CharArrayObjectMap labels = CharArrayObjectMap.EMPTY_MAP;
    
	/**
	 * @param physicalNode
	 */
	public CPPFunctionScope(IASTFunctionDeclarator physicalNode) {
		super(physicalNode);
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#addBinding(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public void addBinding(IBinding binding) {
	    //3.3.4 only labels have function scope
	    if( !( binding instanceof ILabel ) )
	        return;
	    
	    if( labels == CharArrayObjectMap.EMPTY_MAP )
	        labels = new CharArrayObjectMap( 2 );
	    
	    labels.put( binding.getNameCharArray(), binding );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#getBinding(int, char[])
	 */
	public IBinding getBinding( IASTName name ) {
	    return (IBinding) labels.get( name.toCharArray() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
	 */
	public IBinding[] find(String name) {
	    char [] n = name.toCharArray();
	    if( labels.containsKey( n ) )
	        return new IBinding[] { (IBinding) labels.get( n ) };

	    LookupData data = new LookupData( n );
		try {
            data.foundItems = CPPSemantics.lookupInScope( data, this, null, null );
        } catch ( DOMException e ) {
        }
        
        if( data.foundItems != null ){
            IASTName [] ns = (IASTName[]) data.foundItems;
            ObjectSet set = new ObjectSet( ns.length );
            for( int i = 0; i < ns.length && ns[i] != null; i++ ){
                set.put( ns[i].resolveBinding() );
            }
            return (IBinding[]) ArrayUtil.trim( IBinding.class, set.keyArray(), true );
        }
	    
		return new IBinding[0];
	}
	
	public IScope getParent() throws DOMException {
	    //we can't just resolve the function and get its parent scope, since there are cases where that 
	    //could loop since resolving functions requires resolving their parameter types
	    IASTFunctionDeclarator fdtor = (IASTFunctionDeclarator) getPhysicalNode();
	    IASTName name = fdtor.getName();
	    if( name instanceof ICPPASTQualifiedName ){
	        IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
	        if( ns.length > 1){
	            IBinding binding = ns[ ns.length - 2 ].resolveBinding();
	            if( binding instanceof ICPPClassType )
	                return ((ICPPClassType)binding).getCompositeScope();
	            else if( binding instanceof ICPPNamespace )
	                return ((ICPPNamespace)binding).getNamespaceScope();
	            return binding.getScope();
	        }
	    } 
	        
	    return CPPVisitor.getContainingScope( fdtor );
	}

}

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
 * Created on Nov 29, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;

/**
 * @author aniefer
 */
public class CPPNamespaceScope extends CPPScope implements ICPPNamespaceScope{
    private static class ScopeMap extends CharArrayObjectMap {
        private boolean[] resolvedTable;
        public ScopeMap( int initialSize ) {
            super( initialSize );
            resolvedTable = new boolean[capacity()];
        }
        protected void resize(int size) {
    		boolean[] oldResolvedTable = resolvedTable;
    		resolvedTable = new boolean[size];
    		System.arraycopy(oldResolvedTable, 0, resolvedTable, 0, oldResolvedTable.length);
    		super.resize(size);
    	}
        public boolean isFullyResolved( char [] name ){
            int i = lookup(name, 0, name.length);
            if( i >= 0 )
                return resolvedTable[i];
            return false;
        }
        public void setFullyResolved( char [] name ){
            int i = lookup(name, 0, name.length);
            if( i >= 0 )
                resolvedTable[i] = true;
        }
    }
	private ScopeMap bindings = null;
	//private boolean checkForAdditionalBindings = true;
	
	public CPPNamespaceScope( IASTNode physicalNode ) {
		super( physicalNode );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#addBinding(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public void addBinding(IBinding binding) {
		if( bindings == null )
			bindings = new ScopeMap(1);
		char [] c = binding.getNameCharArray();
		Object o = bindings.get( c );
		if( o != null ){
		    if( o instanceof IBinding[] ){
		        bindings.put( c, ArrayUtil.append( IBinding.class, (Object[]) o, binding ) );
		    } else {
		        bindings.put( c, new IBinding[] { (IBinding) o, binding } );
		    }
		} else {
		    bindings.put( c, binding );
//		    if( checkForAdditionalBindings ){
//		        //need to ensure we have all bindings that correspond to this char[]
//		        checkForAdditionalBindings = false;
//		        LookupData data = new LookupData( c );
//				try {
//                    data.foundItems = CPPSemantics.lookupInScope( data, this, null, null );
//                } catch ( DOMException e ) {
//                }
//                if( data.foundItems != null ){
//                    IASTName [] ns = (IASTName[]) data.foundItems;
//                    for( int i = 0; i < ns.length && ns[i] != null; i++ ){
//                        ns[i].resolveBinding();
//                    }
//                }
//                checkForAdditionalBindings = true;
//		    }
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#getBinding(int, char[])
	 */
	public IBinding getBinding( IASTName name ) {
	    char [] c = name.toCharArray();
	    if( bindings == null )
	        return null;
	    
	    Object obj = bindings.get( c );
	    if( obj != null ){
	        if( obj instanceof IBinding[] ){
	            obj = CPPSemantics.resolveAmbiguities( name,  (IBinding[]) obj );
	        }
	    }
		return (IBinding) obj;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
	 */
	public IBinding[] find(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setFullyResolved( IASTName name ){
	    if( bindings != null )
	        bindings.setFullyResolved( name.toCharArray() );
	}
	
	public boolean isFullyResolved( IASTName name ){
	    if( bindings != null )
	        return bindings.isFullyResolved( name.toCharArray() );
	    return false;
	}
}

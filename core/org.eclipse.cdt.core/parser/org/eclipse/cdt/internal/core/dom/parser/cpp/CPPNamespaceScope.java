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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPSemantics.LookupData;

/**
 * @author aniefer
 */
public class CPPNamespaceScope extends CPPScope implements ICPPNamespaceScope{
	private CharArrayObjectMap bindings = CharArrayObjectMap.EMPTY_MAP;
	private boolean checkForAdditionalBindings = true;
	
	public CPPNamespaceScope( IASTNode physicalNode ) {
		super( physicalNode );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#addBinding(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public void addBinding(IBinding binding) {
		if( bindings == CharArrayObjectMap.EMPTY_MAP )
			bindings = new CharArrayObjectMap(1);
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
		    if( checkForAdditionalBindings ){
		        //need to ensure we have all bindings that correspond to this char[]
		        checkForAdditionalBindings = false;
		        LookupData data = new LookupData( c );
				try {
                    data.foundItems = CPPSemantics.lookupInScope( data, this, null, null );
                } catch ( DOMException e ) {
                }
                if( data.foundItems != null ){
                    IASTName [] ns = (IASTName[]) data.foundItems;
                    for( int i = 0; i < ns.length && ns[i] != null; i++ ){
                        ns[i].resolveBinding();
                    }
                }
                checkForAdditionalBindings = true;
		    }
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#getBinding(int, char[])
	 */
	public IBinding getBinding( IASTName name ) {
	    char [] c = name.toCharArray();
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
}

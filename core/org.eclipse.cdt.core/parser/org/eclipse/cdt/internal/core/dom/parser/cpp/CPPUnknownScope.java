/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *******************************************************************************/

/*
 * Created on May 3, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;

/**
 * @author aniefer
 */
public class CPPUnknownScope implements ICPPScope, IASTInternalScope {
    private IBinding binding = null;
    private IASTName scopeName = null;
    private CharArrayObjectMap map = null;
    /**
     * 
     */
    public CPPUnknownScope( IBinding binding, IASTName name ) {
        super();
        this.scopeName = name;
        this.binding = binding;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getScopeName()
     */
    public IName getScopeName() {
        return scopeName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
     */
    public IScope getParent() throws DOMException {
        return binding.getScope();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
     */
    public IBinding[] find( String name ) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getPhysicalNode()
     */
    public IASTNode getPhysicalNode() {
        return scopeName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#addName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void addName( IASTName name ) {

    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#removeBinding(org.eclipse.cdt.core.dom.ast.IBinding)
     */
    public void removeBinding( IBinding binding1 ) {

    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getBinding(org.eclipse.cdt.core.dom.ast.IASTName, boolean)
     */
    public IBinding getBinding( IASTName name, boolean resolve ) {
        if( map == null )
            map = new CharArrayObjectMap(2);
        
        char [] c = name.toCharArray();
        if( map.containsKey( c ) ){
            return (IBinding) map.get( c );
        }
        
        IBinding b = new CPPUnknownClass( this, binding, name );
        name.setBinding( b );
        map.put( c, b );

        return b;
    }
    
    public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup) {
        if( map == null )
            map = new CharArrayObjectMap(2);
        
        char [] c = name.toCharArray();
        
	    IBinding[] result = null;
	    if (prefixLookup) {
	    	Object[] keys = map.keyArray();
	    	for (int i = 0; i < keys.length; i++) {
	    		char[] key = (char[]) keys[i];
	    		if (CharArrayUtils.equals(key, 0, c.length, c, true)) {
	    			result = (IBinding[]) ArrayUtil.append(IBinding.class, result, map.get(key));
	    		}
	    	}
	    } else {
	    	result = new IBinding[] { (IBinding) map.get( c ) };
	    }
        
	    result = (IBinding[]) ArrayUtil.trim(IBinding.class, result);
	    return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#setFullyCached(boolean)
     */
    public void setFullyCached( boolean b ) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#isFullyCached()
     */
    public boolean isFullyCached() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#flushCache()
     */
    public void flushCache() {
    }

	public void addBinding(IBinding aBinding) {
		// do nothing, this is part of template magic and not a normal scope
	}

}

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

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;

/**
 * @author aniefer
 */
public class CPPFunctionScope extends CPPScope implements ICPPFunctionScope {

    private CharArrayObjectMap labels = CharArrayObjectMap.EMPTY_MAP;
    
	/**
	 * @param physicalNode
	 */
	public CPPFunctionScope(IASTFunctionDefinition physicalNode) {
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
	public List find(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public IScope getParent() {
		IASTFunctionDefinition fn = (IASTFunctionDefinition) getPhysicalNode();
		IFunction function = (IFunction) fn.getDeclarator().getName().resolveBinding();
		if( function instanceof ICPPMethod ){
			return function.getScope();
		}
		return super.getParent();
	}

}

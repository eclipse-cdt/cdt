/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;

/**
 * @author jcamelon
 */
public class CASTEnumerationSpecifier extends CASTBaseDeclSpecifier implements
        ICASTEnumerationSpecifier {

    private IASTName name;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier#addEnumerator(org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator)
     */
    public void addEnumerator(IASTEnumerator enumerator) {
        if( enumerators == null )
        {
            enumerators = new IASTEnumerator[ DEFAULT_ENUMERATOR_LIST_SIZE ];
            currentIndex = 0;
        }
        if( enumerators.length == currentIndex )
        {
            IASTEnumerator [] old = enumerators;
            enumerators = new IASTEnumerator[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                enumerators[i] = old[i];
        }
        enumerators[ currentIndex++ ] = enumerator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier#getEnumerators()
     */
    public IASTEnumerator[] getEnumerators() {        
        if( enumerators == null ) return IASTEnumerator.EMPTY_ENUMERATOR_ARRAY;
        removeNullEnumerators();
        return enumerators;
    }

    private void removeNullEnumerators() {
        int nullCount = 0; 
        for( int i = 0; i < enumerators.length; ++i )
            if( enumerators[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTEnumerator [] old = enumerators;
        int newSize = old.length - nullCount;
        enumerators = new IASTEnumerator[ newSize ];
        for( int i = 0; i < newSize; ++i )
            enumerators[i] = old[i];
        currentIndex = newSize;
    }

    private int currentIndex = 0;    
    private IASTEnumerator [] enumerators = null;
    private static final int DEFAULT_ENUMERATOR_LIST_SIZE = 4;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier#setName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier#getName()
     */
    public IASTName getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier#getUnpreprocessedSignature()
     */
    public String getUnpreprocessedSignature() {
       return getName().toString() == null ? "" : getName().toString(); //$NON-NLS-1$
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( name != null ) if( !name.accept( action ) ) return false;
        IASTEnumerator[] etors = getEnumerators();
        for ( int i = 0; i < etors.length; i++ ) {
            if( !etors[i].accept( action ) ) return false;
        }
        return true;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNameOwner#getRoleForName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public int getRoleForName(IASTName name) {
		if( this.name == name )
			return r_declaration;
		return r_unclear;
	}

}

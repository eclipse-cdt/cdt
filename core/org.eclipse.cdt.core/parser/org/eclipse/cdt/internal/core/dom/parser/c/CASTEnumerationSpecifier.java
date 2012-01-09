/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Yuan Zhang / Beth Tibbitts (IBM Research)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalEnumerationSpecifier;

/**
 * AST node for enumeration specifiers.
 */
public class CASTEnumerationSpecifier extends CASTBaseDeclSpecifier 
		implements IASTInternalEnumerationSpecifier, ICASTEnumerationSpecifier {

    private IASTName name;
	private boolean valuesComputed= false;

    
    public CASTEnumerationSpecifier() {
	}

	public CASTEnumerationSpecifier(IASTName name) {
		setName(name);
	}

	@Override
	public CASTEnumerationSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CASTEnumerationSpecifier copy(CopyStyle style) {
		CASTEnumerationSpecifier copy = new CASTEnumerationSpecifier();
		copyEnumerationSpecifier(copy, style);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	protected void copyEnumerationSpecifier(CASTEnumerationSpecifier copy, CopyStyle style) {
		copyBaseDeclSpec(copy);
		copy.setName(name == null ? null : name.copy(style));
		for(IASTEnumerator enumerator : getEnumerators())
			copy.addEnumerator(enumerator == null ? null : enumerator.copy(style));
	}
	
	
	@Override
	public boolean startValueComputation() {
		if (valuesComputed)
			return false;
		
		valuesComputed= true;
		return true;
	}

	@Override
	public void addEnumerator(IASTEnumerator enumerator) {
        assertNotFrozen();
    	if (enumerator != null) {
    		enumerator.setParent(this);
			enumerator.setPropertyInParent(ENUMERATOR);
    		enumerators = ArrayUtil.appendAt( IASTEnumerator.class, enumerators, ++enumeratorsPos, enumerator );
    	}
    }

    
    @Override
	public IASTEnumerator[] getEnumerators() {        
        if( enumerators == null ) return IASTEnumerator.EMPTY_ENUMERATOR_ARRAY;
        enumerators = ArrayUtil.trimAt( IASTEnumerator.class, enumerators, enumeratorsPos );
        return enumerators;
    }

    private IASTEnumerator [] enumerators = null;
    private int enumeratorsPos=-1;

 
    @Override
	public void setName(IASTName name) {
        assertNotFrozen();
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(ENUMERATION_NAME);
		}
    }

    @Override
	public IASTName getName() {
        return name;
    }

    @Override
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
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

	@Override
	public int getRoleForName(IASTName n ) {
		if( this.name == n  )
			return r_definition;
		return r_unclear;
	}

}

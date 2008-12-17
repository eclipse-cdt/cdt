/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Yuan Zhang / Beth Tibbitts (IBM Research)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author jcamelon
 */
public class CASTDesignatedInitializer extends ASTNode implements ICASTDesignatedInitializer {

    private IASTInitializer rhs;

    
    public CASTDesignatedInitializer() {
	}

	public CASTDesignatedInitializer(IASTInitializer operandInitializer) {
		setOperandInitializer(operandInitializer);
	}

	public CASTDesignatedInitializer copy() {
		CASTDesignatedInitializer copy = new CASTDesignatedInitializer(rhs == null ? null : rhs.copy());
		for(ICASTDesignator designator : getDesignators())
			copy.addDesignator(designator == null ? null : designator.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public void addDesignator(ICASTDesignator designator) {
        assertNotFrozen();
    	if (designator != null) {
    		designator.setParent(this);
    		designator.setPropertyInParent(DESIGNATOR);
    		designators = (ICASTDesignator[]) ArrayUtil.append( ICASTDesignator.class, designators, ++designatorsPos, designator );
    	}
    }

    
    public ICASTDesignator[] getDesignators() {
        if( designators == null ) return ICASTDesignatedInitializer.EMPTY_DESIGNATOR_ARRAY;
        designators = (ICASTDesignator[]) ArrayUtil.removeNullsAfter( ICASTDesignator.class, designators, designatorsPos );
        return designators;
    }

    private ICASTDesignator [] designators = null;
    int designatorsPos=-1;
    
    
    public IASTInitializer getOperandInitializer() {
        return rhs;
    }

    
    public void setOperandInitializer(IASTInitializer rhs) {
        assertNotFrozen();
        this.rhs = rhs;
        if (rhs != null) {
			rhs.setParent(this);
			rhs.setPropertyInParent(OPERAND);
		}
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitInitializers ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        ICASTDesignator [] ds = getDesignators();
        for ( int i = 0; i < ds.length; i++ ) {
            if( !ds[i].accept( action ) ) return false;
        }
        if( rhs != null ) if( !rhs.accept( action ) ) return false;

        if( action.shouldVisitInitializers ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

}

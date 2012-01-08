/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Yuan Zhang / Beth Tibbitts (IBM Research)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Implementation for designated initializers
 */
public class CASTDesignatedInitializer extends ASTNode implements ICASTDesignatedInitializer, IASTAmbiguityParent {

    private IASTInitializerClause rhs;
    private ICASTDesignator [] designators = null;
    private int designatorsPos=-1;
     
    public CASTDesignatedInitializer() {
	}

	public CASTDesignatedInitializer(IASTInitializerClause init) {
		setOperand(init);
	}

	@Override
	public CASTDesignatedInitializer copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CASTDesignatedInitializer copy(CopyStyle style) {
		CASTDesignatedInitializer copy = new CASTDesignatedInitializer(rhs == null ? null
				: rhs.copy(style));
		for (ICASTDesignator designator : getDesignators())
			copy.addDesignator(designator == null ? null : designator.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public void addDesignator(ICASTDesignator designator) {
        assertNotFrozen();
    	if (designator != null) {
    		designator.setParent(this);
    		designator.setPropertyInParent(DESIGNATOR);
    		designators = ArrayUtil.appendAt( ICASTDesignator.class, designators, ++designatorsPos, designator );
    	}
    }

    
    @Override
	public ICASTDesignator[] getDesignators() {
        if( designators == null ) return ICASTDesignatedInitializer.EMPTY_DESIGNATOR_ARRAY;
        designators = ArrayUtil.trimAt( ICASTDesignator.class, designators, designatorsPos );
        return designators;
    }

    
    
    @Override
	public IASTInitializerClause getOperand() {
        return rhs;
	}

	@Override
	public void setOperand(IASTInitializerClause operand) {
        assertNotFrozen();
        this.rhs = operand;
        if (rhs != null) {
			rhs.setParent(this);
			rhs.setPropertyInParent(OPERAND);
		}
	}

	@Override
	@Deprecated
	public IASTInitializer getOperandInitializer() {
		if (rhs instanceof IASTInitializer) {
			return (IASTInitializer) rhs;
		}
		if (rhs instanceof IASTExpression) {
			CASTEqualsInitializer init = new CASTEqualsInitializer(((IASTExpression)rhs).copy());
			init.setParent(this);
			init.setPropertyInParent(OPERAND);
			return init;
		}
        return null;
    }

    @Override
	@Deprecated
    public void setOperandInitializer(IASTInitializer rhs) {
    	if (rhs instanceof IASTEqualsInitializer) {
    		setOperand(((IASTEqualsInitializer) rhs).getInitializerClause());
    	} else if (rhs instanceof IASTInitializerClause) {
    		setOperand((IASTInitializerClause) rhs);
    	} else {
    		setOperand(null);
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

    @Override
	public void replace(IASTNode child, IASTNode other) {
    	if (child == rhs) {
    		other.setPropertyInParent(child.getPropertyInParent());
    		other.setParent(child.getParent());
    		rhs =  (IASTInitializerClause) other;
    	}
    }
}

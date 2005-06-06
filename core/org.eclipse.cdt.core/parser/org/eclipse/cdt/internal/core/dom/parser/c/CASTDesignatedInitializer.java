/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author jcamelon
 */
public class CASTDesignatedInitializer extends CASTNode implements
        ICASTDesignatedInitializer {

    private IASTInitializer rhs;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer#addDesignator(org.eclipse.cdt.core.dom.ast.c.ICASTDesignator)
     */
    public void addDesignator(ICASTDesignator designator) {
    	if (designator != null) {
    		designatorsPos++;
    		designators = (ICASTDesignator[]) ArrayUtil.append( ICASTDesignator.class, designators, designator );
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer#getDesignators()
     */
    public ICASTDesignator[] getDesignators() {
        if( designators == null ) return ICASTDesignatedInitializer.EMPTY_DESIGNATOR_ARRAY;
        designators = (ICASTDesignator[]) ArrayUtil.removeNullsAfter( ICASTDesignator.class, designators, designatorsPos );
        return designators;
    }

    private ICASTDesignator [] designators = null;
    int designatorsPos=-1;
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer#getRHSInitializer()
     */
    public IASTInitializer getOperandInitializer() {
        return rhs;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer#setRHSInitializer(org.eclipse.cdt.core.dom.ast.IASTInitializer)
     */
    public void setOperandInitializer(IASTInitializer rhs) {
        this.rhs = rhs;
    }

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
        return true;
    }

}

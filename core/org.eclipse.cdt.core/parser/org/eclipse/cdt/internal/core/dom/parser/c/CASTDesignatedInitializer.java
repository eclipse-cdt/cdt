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

import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;

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
        if( designators == null )
        {
            designators = new ICASTDesignator[ DEFAULT_DESIGNATORS_LIST_SIZE ];
            currentIndex = 0;
        }
        if( designators.length == currentIndex )
        {
            ICASTDesignator [] old = designators;
            designators = new ICASTDesignator[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                designators[i] = old[i];
        }
        designators[ currentIndex++ ] = designator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer#getDesignators()
     */
    public ICASTDesignator[] getDesignators() {
        if( designators == null ) return ICASTDesignatedInitializer.EMPTY_DESIGNATOR_ARRAY;
        removeNullDesignators();
        return designators;
    }

    private void removeNullDesignators() {
        int nullCount = 0; 
        for( int i = 0; i < designators.length; ++i )
            if( designators[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        ICASTDesignator [] old = designators;
        int newSize = old.length - nullCount;
        designators = new ICASTDesignator[ newSize ];
        for( int i = 0; i < newSize; ++i )
            designators[i] = old[i];
        currentIndex = newSize;
    }

    private int currentIndex = 0;    
    private ICASTDesignator [] designators = null;
    private static final int DEFAULT_DESIGNATORS_LIST_SIZE = 2;
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

}

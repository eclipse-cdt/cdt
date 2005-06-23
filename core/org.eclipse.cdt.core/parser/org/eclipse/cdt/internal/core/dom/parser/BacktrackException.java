/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTProblem;

/**
 * @author jcamelon
 */
public class BacktrackException extends Exception {
    private IASTProblem problem;
    private int offset, length; 

    /**
     * @param p
     */
    public void initialize(IASTProblem p) {
        reset();
        problem = p;
    }

    /**
     * 
     */
    private void reset() {
        problem = null;
        offset = 0;
        length = 0;
    }
    /**
     * @return Returns the problem.
     */
    public final IASTProblem getProblem() {
        return problem;
    }

    /**
     * @param startingOffset
     * @param endingOffset
     * @param f TODO
     */
    public void initialize(int start, int l ) {
        reset();
        offset = start;
        length = l;
    }


    /**
     * @return Returns the length.
     */
    public int getLength() {
        return length;
    }
    /**
     * @return Returns the offset.
     */
    public int getOffset() {
        return offset;
    }
}

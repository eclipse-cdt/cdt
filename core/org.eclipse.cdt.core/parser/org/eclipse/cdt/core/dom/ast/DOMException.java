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
 * Created on Jan 31, 2005
 */
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPSemantics;

/**
 * This is the general purpose exception that is thrown for resolving semantic 
 * aspects of an illegal binding.  
 * 
 * @author aniefer
 */
public class DOMException extends Exception {
    IProblemBinding problemBinding;

    
    /**
     * @param problem binding for throwing
     * 
     */
    public DOMException( IProblemBinding problem ) {
        super( problem != null ? problem.getMessage() : CPPSemantics.EMPTY_NAME );
        problemBinding = problem;
    }

    /**
     * Get the problem associated w/this exception.
     * 
     * @return problem
     */
    public IProblemBinding getProblem(){
        return problemBinding;
    }
}

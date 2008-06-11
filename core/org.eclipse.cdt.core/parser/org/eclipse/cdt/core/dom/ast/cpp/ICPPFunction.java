/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

/*
 * Created on Mar 15, 2005
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IFunction;

/**
 * @author aniefer
 */
public interface ICPPFunction extends IFunction, ICPPBinding {

    /**
     * does this function have the mutable storage class specifier
     * @throws DOMException
     */
    public boolean isMutable() throws DOMException;
    
    /**
     * is this an inline function
     */
    public boolean isInline() throws DOMException;
    
    /**
     * Returns whether this function is declared as extern "C".
     * @since 5.0
     */
    public boolean isExternC() throws DOMException;
}

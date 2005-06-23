/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
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
     * @return
     * @throws DOMException
     */
    public boolean isMutable() throws DOMException;
    
    /**
     * is this an inline function
     */
    public boolean isInline() throws DOMException;
}

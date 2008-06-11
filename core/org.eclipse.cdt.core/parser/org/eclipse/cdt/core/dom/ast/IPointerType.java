/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Dec 8, 2004
 */
package org.eclipse.cdt.core.dom.ast;

/**
 * @author aniefer
 */
public interface IPointerType extends IType {
    /**
     * get the type that this is a pointer to
     * @throws DOMException
     */
    public IType getType() throws DOMException;
    
    /**
     * is this a const pointer
     */
    public boolean isConst();
    
    /** 
     * is this a volatile pointer
     */
    public boolean isVolatile();
}

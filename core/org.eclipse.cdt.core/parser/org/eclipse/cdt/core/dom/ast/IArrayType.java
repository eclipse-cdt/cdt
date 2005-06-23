/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Dec 13, 2004
 */
package org.eclipse.cdt.core.dom.ast;

/**
 * @author aniefer
 */
public interface IArrayType extends IType {
    /**
     * get the type that this is an array of
     * @return
     * @throws DOMException
     */
    IType getType() throws DOMException;
    
    /**
     * get the expression that represents the size of this array
     * @return
     * @throws DOMException
     */
    IASTExpression getArraySizeExpression() throws DOMException;
}

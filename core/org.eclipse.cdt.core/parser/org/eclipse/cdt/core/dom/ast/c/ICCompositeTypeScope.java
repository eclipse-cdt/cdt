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
 * Created on Nov 25, 2004
 */
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * @author aniefer
 */
public interface ICCompositeTypeScope extends ICScope {
    /**
     * get the binding for the member that has been previous added to this scope 
     * and that matches the given name.
     * @param name
     * @return
     * @throws DOMException
     */
    public IBinding getBinding( char[] name ) throws DOMException;
}

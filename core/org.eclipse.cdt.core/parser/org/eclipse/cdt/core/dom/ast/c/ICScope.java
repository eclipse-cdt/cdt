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
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * @author aniefer
 */
public interface ICScope extends IScope {
    public static final int NAMESPACE_TYPE_TAG = 0;
    public static final int NAMESPACE_TYPE_OTHER = 1;
    
    void addBinding( IBinding binding ) throws DOMException;
    void removeBinding( IBinding binding ) throws DOMException;
    public IBinding getBinding( int namespaceType, char [] name ) throws DOMException;
}

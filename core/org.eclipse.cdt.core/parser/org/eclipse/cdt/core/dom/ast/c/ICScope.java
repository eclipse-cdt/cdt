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
    /**
     * ISO C:99 6.2.3
     * there are seperate namespaces for various categories of identifiers:
     * - label names ( labels have ICFunctionScope )
     * - tags of structures or unions : NAMESPACE_TYPE_TAG
     * - members of structures or unions ( members have ICCompositeTypeScope )
     * - all other identifiers : NAMESPACE_TYPE_OTHER
     */
    public static final int NAMESPACE_TYPE_TAG = 0;
    public static final int NAMESPACE_TYPE_OTHER = 1;
    
    /**
     * add a binding to this scope
     * @param binding
     * @throws DOMException
     */
    void addBinding( IBinding binding ) throws DOMException;
    
    /** 
     * remove the given binding from this scope 
     * @param binding
     * @throws DOMException
     */
    void removeBinding( IBinding binding ) throws DOMException;
    
    /**
     * Get the binding that has previously been added to this scope that matches
     * the given name and is in the appropriate namespace
     * @param namespaceType : either NAMESPACE_TYPE_TAG or NAMESPACE_TYPE_OTHER
     * @param name
     * @return
     * @throws DOMException
     */
    public IBinding getBinding( int namespaceType, char [] name ) throws DOMException;
}

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
 * Created on Mar 15, 2005
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * ICPPDelegate is meant to represent IBindings that are identical in most
 * ways to another binding.  Namespace aliases and using declarations will both
 * be represented by delegates
 *
 * @author aniefer
 */
public interface ICPPDelegate extends ICPPBinding, Cloneable {
    /**
     * Kinds of things delegates can represent
     */
    public static final int NAMESPACE_ALIAS   = 1;
    public static final int USING_DECLARATION = 2;
    
    /**
     * What type of delegate is this?
     * @return
     */
    public int getDelegateType();
    
    /**
     * get the original binding that we are a delegate of
     * @return
     */
    public IBinding getBinding();
}

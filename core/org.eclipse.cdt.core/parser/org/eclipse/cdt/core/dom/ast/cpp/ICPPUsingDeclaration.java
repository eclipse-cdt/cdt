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
 * Created on Mar 16, 2005
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;

/**
 * A using declaration introduces a name into the declarative region in which 
 * it appears, that name is a synonym of some entity declared elsewhere
 * 
 * The using declaration is both a declaration of a new binding and a reference to a
 * previously declared binding
 * @author aniefer
 */
public interface ICPPUsingDeclaration extends ICPPBinding {
    /**
     * Return an array of bindings that were declared by this using declaration.
     * Each of these bindings delegates to some previously declared binding to which it
     * is a reference. 
     * @return
     */
    ICPPDelegate [] getDelegates() throws DOMException;
}

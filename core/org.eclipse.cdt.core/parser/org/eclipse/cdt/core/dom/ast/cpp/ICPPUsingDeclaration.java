/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * A using declaration introduces a name into the declarative region in which 
 * it appears, that name is a synonym of some entity declared elsewhere
 * 
 * The using declaration is both a declaration of a new binding and a reference to a
 * previously declared binding
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPUsingDeclaration extends ICPPBinding {	
    /**
     * Return an array of bindings that were declared by this using declaration.
     * Each of these bindings delegates to some previously declared binding to which it
     * is a reference. 
     */
    IBinding[] getDelegates();
}

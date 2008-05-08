/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * Represents a binding found within a template definition that cannot be resolved until
 * the template gets instantiated.
 * 
 * This interface should be made public
 * @since 5.0
 */
public interface ICPPUnknownBinding extends ICPPBinding {
	
	/**
	 * Returns the binding of the scope containing this binding.
	 * @since 5.0
	 */
	public ICPPBinding getContainerBinding();

	/**
	 * Returns the scope this binding represents.
	 * @throws DOMException 
	 */
    public ICPPScope getUnknownScope() throws DOMException;

	/**
	 * Returns a the name of the unknown binding that has to be searched in the parent scope.
	 * The ast-node may not be rooted in an ast-tree. May be <code>null</code>.
	 */
	public IASTName getUnknownName();

	/**
	 * Resolves unknown type to another unknown type that is a step closer to the final
	 * name resolution.
	 * @param parentBinding a new parent binding, usually a result of partial resolution
	 *        of the original parent binding, or <code>null</code> for deferred template 
	 *        instantiations.
	 * @param argMap template argument map.
	 * @return a partially resolved, but still unknown, binding.
	 */
    public IBinding resolvePartially(ICPPUnknownBinding parentBinding, ObjectMap argMap);
}

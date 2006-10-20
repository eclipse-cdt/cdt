/*******************************************************************************
 * Copyright (c) 2006 Symbian Software and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.bid;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * A factory for instances of binding identitys
 */
public interface IBindingIdentityFactory {
	/**
	 * Return an IBindingIdentity instance for the named binding. No assumption
	 * is made about whether the IBinding parameter is from the PDOM or DOM
	 * @param binding the binding to create a IBindingIdentity for
	 * @return a binding identity instance
	 * @throws CoreException
	 */
	public ILocalBindingIdentity getLocalBindingIdentity(IBinding binding) throws CoreException;
	
	/*
	 * aftodo - we might want to introduce a true binding identity (i.e. identifies globally
	 * not within a scope). I've no client code for this though.
	 * 
	 * public IBindingIdentity getBindingIdentity(IBinding binding) throws CoreException
	 */
}

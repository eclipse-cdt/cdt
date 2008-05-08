/*******************************************************************************
 * Copyright (c) 2008 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;

/**
 * Scope corresponding to an unknown binding.
 * 
 * @author Sergey Prigogin
 */
public interface ICPPInternalUnknownScope extends IASTInternalScope {

	/**
	 * @return Returns the binding corresponding to the scope.
	 */
	public abstract ICPPBinding getScopeBinding();
}

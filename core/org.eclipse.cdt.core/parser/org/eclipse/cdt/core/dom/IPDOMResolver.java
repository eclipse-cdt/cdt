/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.core.runtime.IAdaptable;

/**
 * This is the interface used by the DOM to help with resolving
 * bindings amongst other things.
 * 
 * @author Doug Schaefer
 */
public interface IPDOMResolver extends IAdaptable {

	public IBinding resolveBinding(IASTName name);
	
	public IASTName[] getDeclarations(IBinding binding);
	
	public IASTName[] getDefinitions(IBinding binding);

}

/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.base;



/**
 * A composite change consisting of a list of changes. Performing a composite
 * change peforms all managed changes. Managed changes can be either primitive
 * or composite changes.
 * Clients can implement this interface if they want their <code>IChange</code> to be treated as composites.
 * <p>
 * <bf>NOTE:<bf> This class/interface is part of an interim API that is still under development 
 * and expected to change significantly before reaching stability. It is being made available at 
 * this early stage to solicit feedback from pioneering adopters on the understanding that any 
 * code that uses this API will almost certainly be broken (repeatedly) as the API evolves.</p>
 */
public interface ICompositeChange extends IChange {

	/**
	 * Returns the set of changes this composite change consists of. If the composite
	 * change doesn't have any children, <code>null</code> is returned.
	 * @return an array of changes this composite change consists of
	 */
	public IChange[] getChildren();
}

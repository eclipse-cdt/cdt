/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface for all nodes that can be visited by a {@link IPDOMVisitor}.
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPDOMNode {

	/**
	 * Visits the children of this node.
	 */
	public void accept(IPDOMVisitor visitor) throws CoreException;
	
	/**
	 * Frees memory allocated by this node, the node may no longer be used.
	 * @param linkage the linkage the node belongs to.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void delete(PDOMLinkage linkage) throws CoreException;
}

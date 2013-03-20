/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eidsness - Initial implementation
 */
package org.eclipse.cdt.core.dom.ast;

import java.util.Collection;
import java.util.Collections;

/**
 * This interface must be implemented by contributors to the org.eclipse.cdt.core.astChildProvider
 * extension point.  The extension point allows the structure of the AST to be extended by non-core
 * plugins.
 */
public interface IASTChildProvider {

	/**
	 * An empty collection to mark cases where the contributor does not have any
	 * extra children to add.
	 */
	public static final Collection<IASTNode> NONE
		= Collections.unmodifiableCollection(Collections.<IASTNode> emptyList());

	/**
	 * Creates and returns a collection of extra children for the given node.  Returns
	 * #NONE if there are no extra children for this node.
	 * @see #NONE
	 */
	public Collection<IASTNode> getChildren(IASTExpression expr);
}

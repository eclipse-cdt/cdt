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

/**
 * This interface must be implemented by contributors to the org.eclipse.cdt.core.astChildProvider
 * extension point.  The extension point allows the structure of the AST to be extended by non-core
 * plugins.
 */
public interface IASTChildProvider {
	/**
	 * Creates and returns a collection of extra children for the given node.  The return
	 * value will not be modified by the implementation of the extension-point.
	 */
	public Collection<IASTNode> getChildren(IASTExpression expr);
}

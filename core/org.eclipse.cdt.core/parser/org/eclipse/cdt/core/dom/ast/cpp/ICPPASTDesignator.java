/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sergey Prigogin (Google) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * Base interface for all C-style designators.
 * @since 5.12
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTDesignator extends IASTNode {
	public static final ICPPASTDesignator[] EMPTY_ARRAY = {};

	@Override
	public ICPPASTDesignator copy();

	@Override
	public ICPPASTDesignator copy(CopyStyle style);
}

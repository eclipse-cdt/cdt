/*******************************************************************************
 * Copyright (c) 2005 - 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Mike Kucera - simplification
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

/**
 * This interface represents a C++ overloaded operator member function name.
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTOperatorName extends ICPPASTName {
	
	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTOperatorName copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTOperatorName copy(CopyStyle style);
}

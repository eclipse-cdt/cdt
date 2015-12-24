/*******************************************************************************
 * Copyright (c) 2009, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Thomas Corbat (IFS) - Added copy methods
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTTypeId;

/**
 * Type ids in C++.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.2
 */
public interface ICPPASTTypeId extends IASTTypeId, ICPPASTPackExpandable {
	/**
	 * @since 5.5
	 */
	@Override
	public ICPPASTTypeId copy();

	/**
	 * @since 5.5
	 */
	@Override
	public ICPPASTTypeId copy(CopyStyle style);
}

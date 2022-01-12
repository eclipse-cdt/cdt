/*******************************************************************************
 * Copyright (c) 2009, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTInitializerList;

/**
 * Braced initializer list.
 *
 * @since 5.2
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTInitializerList extends IASTInitializerList, ICPPASTInitializerClause, ICPPASTPackExpandable {
	@Override
	ICPPASTInitializerList copy();

	/**
	 * @since 5.3
	 */
	@Override
	ICPPASTInitializerList copy(CopyStyle style);
}

/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;

/**
 * @deprecated Use {@link ICPPASTPointerToMember}, instead.
 * @noreference This interface is not intended to be referenced by clients.
 */
@Deprecated
public interface IGPPASTPointerToMember extends IGPPASTPointer, ICPPASTPointerToMember {
	/**
	 * @since 5.1
	 */
	@Override
	public IGPPASTPointerToMember copy();
}

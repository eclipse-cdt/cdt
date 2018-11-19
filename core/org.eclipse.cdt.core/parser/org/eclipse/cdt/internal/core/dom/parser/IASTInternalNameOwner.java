/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;

/**
 * Provides additional methods for internal use by the name resolution.
 */
public interface IASTInternalNameOwner extends IASTNameOwner {
	/**
	 * Get the role for the name. If the name needs to be resolved to determine that and
	 * <code>allowResolution</code> is set to <code>false</code>, then {@link IASTNameOwner#r_unclear}
	 * is returned.
	 *
	 * @param n a name to determine the role of.
	 * @param allowResolution whether or not resolving the name is allowed.
	 * @return r_definition, r_declaration, r_reference or r_unclear.
	 */
	public int getRoleForName(IASTName n, boolean allowResolution);
}

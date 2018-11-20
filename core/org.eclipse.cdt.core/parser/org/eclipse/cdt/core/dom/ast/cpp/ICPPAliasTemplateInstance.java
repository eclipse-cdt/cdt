/*******************************************************************************
 * Copyright (c) 2012, 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ITypedef;

/**
 * Represents an instance of an alias template (14.5.7).
 * @since 5.5
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPAliasTemplateInstance extends ITypedef, ICPPTemplateInstance {
	/**
	 * Returns the alias template specialized by this instance.
	 *
	 * @since 6.4
	 */
	@Override
	public ICPPAliasTemplate getTemplateDefinition();
}

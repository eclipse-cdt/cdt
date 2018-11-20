/*******************************************************************************
 * Copyright (c) 2015, 2016 Institute for Software, HSR Hochschule fuer Technik
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
 *     Lukas Wegmann (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

/**
 * A partially specialized variable or class template.
 *
 * @since 6.0
 */
public interface ICPPPartialSpecialization extends ICPPTemplateDefinition {
	public static final ICPPPartialSpecialization[] EMPTY_ARRAY = {};

	/**
	 * Returns the ICPPTemplateDefinition which this is a specialization of.
	 */
	public ICPPTemplateDefinition getPrimaryTemplate();

	/**
	 * Returns the arguments of this partial specialization.
	 */
	public ICPPTemplateArgument[] getTemplateArguments();
}

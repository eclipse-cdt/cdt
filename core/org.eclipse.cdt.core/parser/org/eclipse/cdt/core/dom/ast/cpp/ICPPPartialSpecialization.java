/*******************************************************************************
 * Copyright (c) 2015, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

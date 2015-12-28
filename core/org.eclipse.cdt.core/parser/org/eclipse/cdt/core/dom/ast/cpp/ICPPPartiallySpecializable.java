/*******************************************************************************
 * Copyright (c) 2015 Institute for Software, HSR Hochschule fuer Technik  
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
 * Something that can be partially specialized. Hence, a class or a variable template
 * but not a function template.
 * 
 * @since 5.12
 */
public interface ICPPPartiallySpecializable extends ICPPTemplateDefinition {
	/**
	 * Returns the partial specializations of this template.
	 */
	public ICPPPartialSpecialization[] getPartialSpecializations();
}

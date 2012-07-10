/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * For an instantiation of a class template, the members of that instantiation will be
 * specializations of the members of the original class template.
 * For an instantiation of a function template, the parameters will be specializations 
 * of the parameters of the original function template.
 * Specializations can also be explicitly defined.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPSpecialization extends ICPPBinding {
	/**
	 * Return the binding that this specialization specializes.
	 * @return the original binding that this is a specialization of
	 */ 
	public IBinding getSpecializedBinding();
	
	/**
	 * Returns the mapping of template parameters to values.
	 * @since 5.1
	 */
	public ICPPTemplateParameterMap getTemplateParameterMap();
	
	/**
	 * @deprecated use {@link #getTemplateParameterMap()}, instead.
	 */
	@Deprecated
	public ObjectMap getArgumentMap();
}

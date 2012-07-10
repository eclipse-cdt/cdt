/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IType;

/**
 * This interface represents an instantiation of a class or function template.
 * An instantiated template is a specialization of that template.
 * 
 * An instance of a class template will also implement ICPPClassType and similarly
 * a function template instance will also implement ICPPFunction (or even ICPPMethod 
 * or ICPPConstructor as appropriate)
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ICPPTemplateInstance extends ICPPSpecialization {
	/**
	 * @since 5.1
	 */
	ICPPTemplateInstance[] EMPTY_TEMPLATE_INSTANCE_ARRAY = {};

	/**
	 * get the template that this was instantiated from
	 */
	public ICPPTemplateDefinition getTemplateDefinition();
	
	/**
	 * Returns the template arguments of this instance.
	 * @since 5.1
	 */
	public ICPPTemplateArgument[] getTemplateArguments();
	
	/**
	 * Explicit specializations are modeled as instances of a template. 
	 * Returns <code>true</code> if this binding is an explicit specialization.
	 * @since 5.2
	 */
	public boolean isExplicitSpecialization();
	
	/**
	 * @deprecated Replaced by {@link #getTemplateArguments()}.
	 */
	@Deprecated
	public IType[] getArguments();
}

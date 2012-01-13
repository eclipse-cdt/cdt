/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * This interface represents a class template partial specialization.  A partial specialization is
 * a class template in its own right.
 * 
 * e.g.:
 * template <class T> class A {};     //the primary class template
 * template <class T> class A<T*> {}; //a partial specialization of the primary class template
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPClassTemplatePartialSpecialization extends ICPPClassTemplate {
	public static final ICPPClassTemplatePartialSpecialization[] EMPTY_PARTIAL_SPECIALIZATION_ARRAY = new ICPPClassTemplatePartialSpecialization[0];
	
	/**
	 * get the ICPPTemplateDefinition which this is a specialization of
	 */
	public ICPPClassTemplate getPrimaryClassTemplate();


	/**
	 * Returns the arguments of this partial specialization.
	 * @since 5.1
	 */
	public ICPPTemplateArgument[] getTemplateArguments();
	
	/**
	 * @deprecated use {@link #getTemplateArguments()}, instead.
	 */
	@Deprecated
	public IType [] getArguments() throws DOMException;
}

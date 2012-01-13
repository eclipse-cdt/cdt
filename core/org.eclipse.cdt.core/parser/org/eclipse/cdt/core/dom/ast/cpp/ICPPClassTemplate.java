/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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


/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPClassTemplate extends ICPPTemplateDefinition, ICPPClassType {
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations();
	
	/**
	 * Returns a deferred instance that allows lookups within this class template. 
	 * @since 5.1
	 */
	public ICPPTemplateInstance asDeferredInstance();
}

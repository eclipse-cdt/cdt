/*******************************************************************************
 * Copyright (c) 2007, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;

/**
 * Interface for deferred class template instances. 
 */
public interface ICPPDeferredClassInstance extends ICPPUnknownClassType, ICPPTemplateInstance {

	/**
	 * Returns the class template for the deferred instantiation.
	 */
	ICPPClassTemplate getClassTemplate();
	
	/**
	 * Returns the mapping of the template parameters of the primary class template to the
	 * arguments of this instance.
	 * @since 5.1
	 */
	@Override
	public ICPPTemplateParameterMap getTemplateParameterMap();
}

/*******************************************************************************
 * Copyright (c) 2007, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;

/**
 * Interface for deferred class template instances.
 */
public interface ICPPDeferredClassInstance
		extends ICPPUnknownBinding, ICPPUnknownType, ICPPClassType, ICPPTemplateInstance {
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

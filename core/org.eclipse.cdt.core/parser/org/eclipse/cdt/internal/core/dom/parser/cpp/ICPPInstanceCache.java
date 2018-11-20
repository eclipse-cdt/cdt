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
 * 	   QNX - Initial API and implementation
 *     Sergey Prigogin (Google)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;

/**
 * Caches instances per template, the template definitions need to implement this interface
 */
public interface ICPPInstanceCache {
	/**
	 * Attempts to cache an instance with this template
	 */
	public void addInstance(ICPPTemplateArgument[] arguments, ICPPTemplateInstance instance);

	/**
	 * Attempts to get a cached instance from this template
	 */
	public ICPPTemplateInstance getInstance(ICPPTemplateArgument[] arguments);

	/**
	 * Returns an array of all cached instances
	 */
	public ICPPTemplateInstance[] getAllInstances();
}

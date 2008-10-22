/*******************************************************************************
 * Copyright (c) 2007, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bryan Wilkinson (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

/**
 * Interface for deferred instances. A deferred instance is not actually instantiated yet,
 * because the correct template cannot be selected until all information is available.
 */
public interface ICPPDeferredTemplateInstance extends ICPPTemplateInstance {
	
	/**
	 * Returns an empty map, because template parameters cannot be mapped until
	 * all of the arguments are resolved.
	 * @since 5.1
	 */
	public ICPPTemplateParameterMap getTemplateParameterMap();
}

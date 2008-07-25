/*******************************************************************************
 * Copyright (c) 2007, 2008 QNX Software Systems and others.
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredTemplateInstance;

/**
 * Interface for deferred class template instances. 
 */
public interface ICPPDeferredClassInstance extends ICPPUnknownClassType, ICPPDeferredTemplateInstance {

	/**
	 * Returns the class template for the deferred instantiation.
	 */
	ICPPClassTemplate getClassTemplate();
}

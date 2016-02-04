/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;

/**
 * Interface for deferred alias template instances.
 */
public interface ICPPDeferredAliasTemplateInstance
		extends ICPPTemplateInstance, ICPPUnknownBinding, ICPPUnknownType {
	/**
	 * Returns the alias template corresponding to this instance.
	 */
	@Override
	public ICPPAliasTemplate getTemplateDefinition();
}

/*******************************************************************************
 * Copyright (c) 2007, 2008 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;

/**
 * Common interface for PDOM template definitions. 
 */
public interface IPDOMCPPTemplateParameterOwner {
	ICPPTemplateParameter adaptTemplateParameter(ICPPTemplateParameter param);
}

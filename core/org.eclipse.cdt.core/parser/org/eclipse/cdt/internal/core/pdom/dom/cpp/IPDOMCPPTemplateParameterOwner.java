/*******************************************************************************
 * Copyright (c) 2007, 2008 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

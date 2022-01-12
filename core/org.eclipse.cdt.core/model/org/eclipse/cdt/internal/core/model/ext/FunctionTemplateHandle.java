/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model.ext;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionTemplate;

public class FunctionTemplateHandle extends FunctionTemplateDeclarationHandle implements IFunctionTemplate {

	public FunctionTemplateHandle(ICElement parent, ICPPFunctionTemplate func) throws DOMException {
		super(parent, ICElement.C_TEMPLATE_FUNCTION, func);
	}

	public FunctionTemplateHandle(ICElement parent, ICPPTemplateInstance func) throws DOMException {
		super(parent, ICElement.C_TEMPLATE_FUNCTION, func);
	}

}

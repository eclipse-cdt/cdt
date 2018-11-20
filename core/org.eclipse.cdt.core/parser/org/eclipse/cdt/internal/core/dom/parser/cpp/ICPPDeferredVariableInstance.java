/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplate;

/**
 * Represents an instantiation of a variable template that cannot be performed because of dependent arguments.
 */
public interface ICPPDeferredVariableInstance extends ICPPUnknownBinding, ICPPVariableInstance {
	@Override
	ICPPVariableTemplate getTemplateDefinition();
}

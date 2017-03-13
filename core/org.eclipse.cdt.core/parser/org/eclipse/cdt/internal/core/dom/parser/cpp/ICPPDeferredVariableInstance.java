/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;

/**
 * Represents the scope of a template-template parameter.
 */
public class CPPTemplateTemplateParameterScope extends CPPScope {

	public CPPTemplateTemplateParameterScope(ICPPASTTemplatedTypeTemplateParameter parameter) {
		super(parameter);
	}

	@Override
	public EScopeKind getKind() {
		return EScopeKind.eLocal;
	}

	@Override
	public IName getScopeName() {
		return ((ICPPASTTemplatedTypeTemplateParameter) getPhysicalNode()).getName();
	}
}

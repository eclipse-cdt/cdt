/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
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

/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

public class CPPTemplateScope extends CPPScope implements ICPPTemplateScope {

	public CPPTemplateScope(IASTNode physicalNode) {
		super(physicalNode);
	}

	@Override
	public EScopeKind getKind() {
		return EScopeKind.eTemplateDeclaration;
	}

	@Override
	public ICPPASTTemplateDeclaration getTemplateDeclaration() {
		return (ICPPASTTemplateDeclaration) getPhysicalNode();
	}

	@Override
	public IName getScopeName() {
		ICPPASTTemplateDeclaration template = (ICPPASTTemplateDeclaration) getPhysicalNode();
		return CPPTemplates.getTemplateName(template);
	}

	@Override
	public IScope getParent() {
		return CPPVisitor.getContainingNonTemplateScope(getPhysicalNode());
	}
}

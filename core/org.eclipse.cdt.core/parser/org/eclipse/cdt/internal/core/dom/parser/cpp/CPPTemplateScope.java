/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM Corporation) - Initial API and implementation 
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
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
	public ICPPTemplateDefinition getTemplateDefinition() {
		return null;
	}
	
	@Override
	public ICPPASTTemplateDeclaration getTemplateDeclaration() {
		return (ICPPASTTemplateDeclaration) getPhysicalNode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#getScopeName()
	 */
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

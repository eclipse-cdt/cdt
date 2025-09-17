/*******************************************************************************
 * Copyright (c) 2025 Igor V. Kovalenko.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor V. Kovalenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

public class CPPASTAmbiguousTypeConstraintVsNonTypeTemplateArgument extends ASTAmbiguousNode
		implements ICPPASTTemplateParameter {
	private List<IASTNode> fNodes = new ArrayList<>(2);
	private IScope fScope;
	private ICPPASTTemplateParameter fParameter;

	@Override
	protected void beforeResolution() {
		fScope = CPPVisitor.getContainingScope(this);
		if (fScope instanceof ICPPASTInternalScope internalScope) {
			internalScope.populateCache();
		}
	}

	@Override
	protected void beforeAlternative(IASTNode alternative) {
		cleanupScope();
		if (alternative instanceof ICPPASTTemplateParameter parameter) {
			if (fScope instanceof ICPPASTInternalScope internalScope) {
				fParameter = parameter;
				CPPSemantics.populateCache(internalScope, fParameter);
			}
		}
	}

	private void cleanupScope() {
		if (fScope instanceof ICPPASTInternalScope internalScope && fParameter != null) {
			internalScope.removeNestedFromCache(fParameter);
		}
	}

	@Override
	protected void afterResolution(ASTVisitor resolver, IASTNode best) {
		beforeAlternative(best);
		fParameter = null;
		fScope = null;
	}

	@Override
	public boolean isParameterPack() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ICPPASTTemplateParameter copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ICPPASTTemplateParameter copy(CopyStyle style) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTNode[] getNodes() {
		return fNodes.toArray(new IASTNode[fNodes.size()]);
	}

	public void addTypeConstraint(ICPPASTSimpleTypeTemplateParameter typeConstraint) {
		assertNotFrozen();
		addNode(typeConstraint);
	}

	public void addNonTypeParameter(ICPPASTParameterDeclaration nonTypeParameter) {
		assertNotFrozen();
		addNode(nonTypeParameter);
	}

	private void addNode(IASTNode node) {
		fNodes.add(node);
		node.setParent(this);
		node.setPropertyInParent(ICPPASTTemplateDeclaration.PARAMETER);
	}
}

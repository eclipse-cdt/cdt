/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
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
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICPPASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldDesignator;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

/**
 * Implementation of field designator.
 */
public class CPPASTFieldDesignator extends ASTNode implements ICPPASTFieldDesignator, ICPPASTCompletionContext {
	private IASTName name;

	public CPPASTFieldDesignator() {
	}

	public CPPASTFieldDesignator(IASTName name) {
		setName(name);
	}

	@Override
	public CPPASTFieldDesignator copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTFieldDesignator copy(CopyStyle style) {
		CPPASTFieldDesignator copy = new CPPASTFieldDesignator(name == null ? null : name.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTName getName() {
		return name;
	}

	@Override
	public void setName(IASTName name) {
		assertNotFrozen();
		this.name = name;
		if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(FIELD_NAME);
		}
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDesignators) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		if (name != null && !name.accept(action))
			return false;
		if (action.shouldVisitDesignators && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return findBindings(n, isPrefix, null);
	}

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix, String[] namespaces) {
		return CPPSemantics.findBindingsForContentAssist(n, isPrefix, namespaces);
	}
}

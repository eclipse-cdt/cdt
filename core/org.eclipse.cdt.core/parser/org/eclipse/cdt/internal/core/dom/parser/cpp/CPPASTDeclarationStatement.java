/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecDeclarationStatement;

/**
 * @author jcamelon
 */
public class CPPASTDeclarationStatement extends ASTNode
		implements IASTDeclarationStatement, IASTAmbiguityParent, ICPPExecutionOwner {
	private IASTDeclaration declaration;

	public CPPASTDeclarationStatement() {
	}

	public CPPASTDeclarationStatement(IASTDeclaration declaration) {
		setDeclaration(declaration);
	}

	@Override
	public CPPASTDeclarationStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTDeclarationStatement copy(CopyStyle style) {
		CPPASTDeclarationStatement copy = new CPPASTDeclarationStatement();
		copy.setDeclaration(declaration == null ? null : declaration.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTDeclaration getDeclaration() {
		return declaration;
	}

	@Override
	public void setDeclaration(IASTDeclaration declaration) {
		assertNotFrozen();
		this.declaration = declaration;
		if (declaration != null) {
			declaration.setParent(this);
			declaration.setPropertyInParent(DECLARATION);
		}
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitStatements) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (declaration != null && !declaration.accept(action))
			return false;

		if (action.shouldVisitStatements) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		return true;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (declaration == child) {
			other.setParent(child.getParent());
			other.setPropertyInParent(child.getPropertyInParent());
			declaration = (IASTDeclaration) other;
		}
	}

	@Override
	public IASTAttribute[] getAttributes() {
		// Declaration statements don't have attributes.
		return IASTAttribute.EMPTY_ATTRIBUTE_ARRAY;
	}

	@Override
	public void addAttribute(IASTAttribute attribute) {
		// Declaration statements don't have attributes.
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTAttributeSpecifier[] getAttributeSpecifiers() {
		// Declaration statements don't have attributes.
		return IASTAttributeSpecifier.EMPTY_ATTRIBUTE_SPECIFIER_ARRAY;
	}

	@Override
	public void addAttributeSpecifier(IASTAttributeSpecifier attributeSpecifier) {
		// Declaration statements don't have attributes.
		throw new UnsupportedOperationException();
	}

	@Override
	public ICPPExecution getExecution() {
		if (declaration instanceof ICPPExecutionOwner) {
			ICPPExecutionOwner execOwner = (ICPPExecutionOwner) declaration;
			return new ExecDeclarationStatement(execOwner.getExecution());
		}
		return null;
	}
}

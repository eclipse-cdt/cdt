/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator.RefQualifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTStructuredBindingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecIncomplete;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecSimpleDeclaration;
import org.eclipse.jdt.annotation.NonNull;

public class CPPASTStructuredBindingDeclaration extends CPPASTSimpleDeclaration
		implements ICPPASTStructuredBindingDeclaration {

	private RefQualifier refQualifier;
	private IASTName[] names;
	private IASTInitializer initializer;
	private IASTImplicitName implicitInitializerName;

	public CPPASTStructuredBindingDeclaration() {
	}

	public CPPASTStructuredBindingDeclaration(IASTDeclSpecifier declSpecifier, RefQualifier refQualifier,
			IASTName[] names, IASTInitializer initializer) {
		super(declSpecifier);
		this.refQualifier = refQualifier;
		for (IASTName name : names) {
			addName(name);
		}
		setInitializer(initializer);
	}

	@Override
	public RefQualifier getRefQualifier() {
		return refQualifier;
	}

	public void setRefQualifier(RefQualifier refQualifier) {
		assertNotFrozen();
		this.refQualifier = refQualifier;
	}

	@Override
	public IASTName[] getNames() {
		if (names == null) {
			return IASTName.EMPTY_NAME_ARRAY;
		}
		names = ArrayUtil.trim(names);
		return names;
	}

	@Override
	public IASTInitializer getInitializer() {
		return initializer;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclarations) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		IASTDeclSpecifier declSpecifier = getDeclSpecifier();
		if (declSpecifier != null && !declSpecifier.accept(action)) {
			return false;
		}

		for (IASTName name : getNames()) {
			if (!name.accept(action)) {
				return false;
			}
		}

		if (initializer != null && !initializer.accept(action)) {
			return false;
		}

		if (action.shouldVisitImplicitNames && !getImplicitNames()[0].accept(action)) {
			return false;
		}

		if (action.shouldVisitDeclarations) {
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

	protected void addName(IASTName name) {
		assertNotFrozen();
		if (name != null) {
			names = ArrayUtil.append(IASTName.class, names, name);
			name.setParent(this);
			name.setPropertyInParent(IDENTIFIER);
		}
	}

	protected void setInitializer(IASTInitializer initializer) {
		assertNotFrozen();
		if (initializer != null) {
			this.initializer = initializer;
			initializer.setParent(this);
			initializer.setPropertyInParent(INITIALIZER);
		}
	}

	@Override
	public CPPASTStructuredBindingDeclaration copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTStructuredBindingDeclaration copy(CopyStyle style) {
		CPPASTStructuredBindingDeclaration copy = new CPPASTStructuredBindingDeclaration();
		return copy(copy, style);
	}

	protected <T extends CPPASTStructuredBindingDeclaration> T copy(@NonNull T copy, CopyStyle style) {
		copy.setRefQualifier(refQualifier);
		if (initializer != null) {
			copy.setInitializer(initializer.copy(style));
		}

		for (IASTName name : names) {
			if (name == null) {
				break;
			}
			copy.addName(name.copy(style));
		}
		return super.copy(copy, style);
	}

	@Override
	public ICPPExecution getExecution() {
		IASTName[] names = getNames();
		ICPPExecution[] nameExecutions = Arrays.stream(names).map(name -> {
			IBinding binding = name.resolveBinding();
			if (binding instanceof CPPVariable) {
				CPPVariable variable = (CPPVariable) binding;
				ICPPEvaluation initializerEval = variable.getInitializerEvaluation();
				return new ExecDeclarator((ICPPBinding) binding, initializerEval);
			}
			return ExecIncomplete.INSTANCE;
		}).toArray(ICPPExecution[]::new);
		return new ExecSimpleDeclaration(nameExecutions);
	}

	@Override
	public int getRoleForName(IASTName name) {
		return r_definition;
	}

	@Override
	public IASTImplicitName[] getImplicitNames() {
		if (implicitInitializerName == null) {
			ICPPEvaluation initializerEvaluation = CPPVariable.evaluationOfInitializer(initializer);
			if (initializerEvaluation == null) {
				initializerEvaluation = EvalFixed.INCOMPLETE;
			}
			CPPASTImplicitName implicitName = new CPPASTImplicitName(this);
			implicitName.setIsDefinition(true);
			if (initializer != null) {
				implicitName.setOffsetAndLength((ASTNode) initializer);
			}
			implicitInitializerName = implicitName;
			CPPStructuredBindingComposite implicitInitializerVariable = new CPPStructuredBindingComposite(
					implicitInitializerName, initializerEvaluation);
			implicitInitializerName.setBinding(implicitInitializerVariable);
		}
		return new IASTImplicitName[] { implicitInitializerName };
	}
}

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
 *     Hansruedi Patzen (IFS) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator;

import java.util.function.Predicate;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTToken;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTClassVirtSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDecltypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVirtSpecifier;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;

public class CopyReplaceVisitor extends ASTVisitor {
	private ChangeGeneratorTest changeGenereatorTest;
	private Predicate<IASTNode> predicate;

	public CopyReplaceVisitor(ChangeGeneratorTest changeGenereatorTest, Predicate<IASTNode> predicate) {
		super(true);
		this.changeGenereatorTest = changeGenereatorTest;
		this.predicate = predicate;
	}

	private int copyReplace(IASTNode node) {
		if (predicate.test(node)) {
			changeGenereatorTest.addModification(null, ModificationKind.REPLACE, node,
					node.copy(CopyStyle.withLocations));
			return PROCESS_ABORT;
		}
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTTranslationUnit tu) {
		return copyReplace(tu);
	}

	@Override
	public int visit(IASTName name) {
		return copyReplace(name);
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		return copyReplace(declaration);
	}

	@Override
	public int visit(IASTInitializer initializer) {
		return copyReplace(initializer);
	}

	@Override
	public int visit(IASTParameterDeclaration parameterDeclaration) {
		return copyReplace(parameterDeclaration);
	}

	@Override
	public int visit(IASTDeclarator declarator) {
		return copyReplace(declarator);
	}

	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		return copyReplace(declSpec);
	}

	@Override
	public int visit(IASTArrayModifier arrayModifier) {
		return copyReplace(arrayModifier);
	}

	@Override
	public int visit(IASTPointerOperator ptrOperator) {
		return copyReplace(ptrOperator);
	}

	@Override
	public int visit(IASTAttribute attribute) {
		return copyReplace(attribute);
	}

	@Override
	public int visit(IASTAttributeSpecifier specifier) {
		return copyReplace(specifier);
	}

	@Override
	public int visit(IASTToken token) {
		return copyReplace(token);
	}

	@Override
	public int visit(IASTExpression expression) {
		return copyReplace(expression);
	}

	@Override
	public int visit(IASTStatement statement) {
		return copyReplace(statement);
	}

	@Override
	public int visit(IASTTypeId typeId) {
		return copyReplace(typeId);
	}

	@Override
	public int visit(IASTEnumerator enumerator) {
		return copyReplace(enumerator);
	}

	@Override
	public int visit(IASTProblem problem) {
		return copyReplace(problem);
	}

	@Override
	public int visit(ICPPASTBaseSpecifier baseSpecifier) {
		return copyReplace(baseSpecifier);
	}

	@Override
	public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
		return copyReplace(namespaceDefinition);
	}

	@Override
	public int visit(ICPPASTTemplateParameter templateParameter) {
		return copyReplace(templateParameter);
	}

	@Override
	public int visit(ICPPASTCapture capture) {
		return copyReplace(capture);
	}

	@Override
	public int visit(ICASTDesignator designator) {
		return copyReplace(designator);
	}

	@Override
	public int visit(ICPPASTDesignator designator) {
		return copyReplace(designator);
	}

	@Override
	public int visit(ICPPASTVirtSpecifier virtSpecifier) {
		return copyReplace(virtSpecifier);
	}

	@Override
	public int visit(ICPPASTClassVirtSpecifier classVirtSpecifier) {
		return copyReplace(classVirtSpecifier);
	}

	@Override
	public int visit(ICPPASTDecltypeSpecifier decltypeSpecifier) {
		return copyReplace(decltypeSpecifier);
	}
}
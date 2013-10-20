/*******************************************************************************
 * Copyright (c) 2008, 2013 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArrayDeclarator;
import org.eclipse.cdt.core.dom.rewrite.TypeHelper;
import org.eclipse.cdt.core.parser.Keywords;

import org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTPointer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTPointer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReferenceOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.cdt.internal.ui.refactoring.gettersandsetters.AccessorDescriptor.AccessorKind;

public abstract class AccessorFactory {	
	protected final IASTName fieldName;
	protected final IASTDeclarator fieldDeclarator;
	private final IASTDeclSpecifier declSpecifier;
	protected final String accessorName;
	protected boolean passByReference;

	public static AccessorFactory createFactory(AccessorKind kind, IASTName fieldName,
			IASTDeclarator declarator,	String accessorName) {
		if (kind == AccessorKind.GETTER) {
			return new GetterFactory(fieldName, declarator, accessorName);
		} else {
			return new SetterFactory(fieldName, declarator, accessorName);
		}
	}

	protected AccessorFactory(IASTName fieldName, IASTDeclarator fieldDeclarator, String accessorName) {
		this.fieldName = fieldName;
		this.fieldDeclarator = fieldDeclarator;
		this.accessorName = accessorName;
		IASTSimpleDeclaration declaration =
				CPPVisitor.findAncestorWithType(fieldDeclarator, IASTSimpleDeclaration.class);
		this.declSpecifier = declaration.getDeclSpecifier();
		IType type = CPPVisitor.createType(declSpecifier);
		passByReference = TypeHelper.shouldBePassedByReference(type, fieldDeclarator.getTranslationUnit());
	}

	/**
	 * Creates an accessor declaration.
	 */
	public abstract IASTSimpleDeclaration createDeclaration();

	/**
	 * Creates an accessor definition.
	 * 
	 * @param declaratorName name to use for the declarator
	 */
	public abstract IASTFunctionDefinition createDefinition(IASTName declaratorName);

	protected IASTDeclSpecifier getParamOrReturnDeclSpecifier() {
		IASTDeclSpecifier declSpec = declSpecifier.copy(CopyStyle.withLocations);
		if (passByReference || fieldDeclarator instanceof IASTArrayDeclarator) {
			declSpec.setConst(true);
		}
		return declSpec;
	}

	private static class GetterFactory extends AccessorFactory {
		GetterFactory(IASTName fieldName, IASTDeclarator fieldDeclarator, String getterName) {
			super(fieldName, fieldDeclarator, getterName);
		}

		@Override
		public IASTSimpleDeclaration createDeclaration() {
			IASTSimpleDeclaration getter = new CPPASTSimpleDeclaration();
			getter.setDeclSpecifier(getParamOrReturnDeclSpecifier());
			getter.addDeclarator(getGetterDeclarator(new CPPASTName(accessorName.toCharArray())));
			return getter;
		}

		@Override
		public IASTFunctionDefinition createDefinition(IASTName declaratorName) {
			IASTFunctionDefinition getter = new CPPASTFunctionDefinition();
			
			getter.setDeclSpecifier(getParamOrReturnDeclSpecifier());
			IASTDeclarator getterDeclarator = getGetterDeclarator(declaratorName);
			// IASTFunctionDefinition expects the outermost IASTFunctionDeclarator in declarator hierarchy
			while (!(getterDeclarator instanceof IASTFunctionDeclarator)) {
				getterDeclarator = getterDeclarator.getNestedDeclarator();
			}
			getter.setDeclarator((IASTFunctionDeclarator) getterDeclarator);
			getter.setBody(getGetterBody());
			return getter;
		}

		private CPPASTCompoundStatement getGetterBody() {
			CPPASTCompoundStatement compound = new CPPASTCompoundStatement();
			CPPASTReturnStatement returnStatement = new CPPASTReturnStatement();
			CPPASTIdExpression idExpr = new CPPASTIdExpression();
			CPPASTName returnVal = new CPPASTName();
			returnVal.setName(fieldName.toCharArray());
			idExpr.setName(returnVal);
			returnStatement.setReturnValue(idExpr);
			compound.addStatement(returnStatement);
			return compound;
		}

		private IASTDeclarator getGetterDeclarator(IASTName declaratorName) {
			// Copy declarator hierarchy
			IASTDeclarator topDeclarator = fieldDeclarator.copy(CopyStyle.withLocations);
			
			if (topDeclarator instanceof IASTArrayDeclarator) {
				boolean isCpp = topDeclarator instanceof ICPPASTArrayDeclarator;
				IASTDeclarator decl = isCpp ? new CPPASTDeclarator() : new CASTDeclarator();
				decl.setName(topDeclarator.getName());
				decl.setNestedDeclarator(topDeclarator.getNestedDeclarator());
				decl.addPointerOperator(isCpp ? new CPPASTPointer() : new CASTPointer());
				for (IASTPointerOperator pointer : topDeclarator.getPointerOperators()) {
					decl.addPointerOperator(pointer);
				}
				topDeclarator = decl;
			}
			// Find the innermost declarator in hierarchy
			IASTDeclarator innermost = topDeclarator;
			while (innermost.getNestedDeclarator() != null) {
				innermost = innermost.getNestedDeclarator();
			}

			// Create a new innermost function declarator based on the field declarator 
			CPPASTFunctionDeclarator functionDeclarator = new CPPASTFunctionDeclarator();
			functionDeclarator.setConst(true);
			functionDeclarator.setName(declaratorName);

			for (IASTPointerOperator pointer : innermost.getPointerOperators()){
				functionDeclarator.addPointerOperator(pointer.copy(CopyStyle.withLocations));
			}
			if (passByReference) {
				functionDeclarator.addPointerOperator(new CPPASTReferenceOperator(false));
			}
			
			// Replace the innermost with functionDeclarator and return the whole declarator tree
			if (innermost == topDeclarator) {
				// No tree
				return functionDeclarator;
			} else {
				IASTDeclarator parent = (IASTDeclarator) innermost.getParent();
				parent.setNestedDeclarator(functionDeclarator);
				return topDeclarator;
			}
		}
	}

	private static class SetterFactory extends AccessorFactory {
		SetterFactory(IASTName fieldName, IASTDeclarator fieldDeclarator, String setterName) {
			super(fieldName, fieldDeclarator, setterName);
		}

		@Override
		public IASTSimpleDeclaration createDeclaration() {
			IASTSimpleDeclaration setter = new CPPASTSimpleDeclaration();
			setter.setDeclSpecifier(getVoidDeclSpec());
			setter.addDeclarator(getSetterDeclarator(new CPPASTName(accessorName.toCharArray())));
			return setter;
		}

		@Override
		public IASTFunctionDefinition createDefinition(IASTName declaratorName) {
			IASTFunctionDefinition setter = new CPPASTFunctionDefinition();
			setter.setDeclSpecifier(getVoidDeclSpec());		
			setter.setDeclarator(getSetterDeclarator(declaratorName));
			setter.setBody(getSetterBody());
			return setter;
		}

		private CPPASTCompoundStatement getSetterBody() {
			CPPASTCompoundStatement compound = new CPPASTCompoundStatement();
			CPPASTExpressionStatement exprStmt = new CPPASTExpressionStatement();
			CPPASTBinaryExpression binExpr = new CPPASTBinaryExpression();
			IASTDeclarator innerDeclarator = fieldDeclarator;
			while (innerDeclarator.getNestedDeclarator() != null) {
				innerDeclarator = innerDeclarator.getNestedDeclarator();
			}
			IASTName fieldName = innerDeclarator.getName();
			CPPASTName parameterName = getSetterParameterName();
			if (Arrays.equals(fieldName.getSimpleID(), parameterName.getSimpleID())) {
				CPPASTFieldReference fieldRef = new CPPASTFieldReference();
				CPPASTLiteralExpression litExpr = new CPPASTLiteralExpression();
				litExpr.setValue(Keywords.cTHIS); 
				fieldRef.setFieldOwner(litExpr);
				fieldRef.setIsPointerDereference(true);
				fieldRef.setFieldName(fieldName.copy(CopyStyle.withLocations));
				binExpr.setOperand1(fieldRef);
			} else {
				CPPASTIdExpression idExpr = new CPPASTIdExpression(fieldName.copy(CopyStyle.withLocations));
				binExpr.setOperand1(idExpr);
			}
			binExpr.setOperator(IASTBinaryExpression.op_assign);
			CPPASTIdExpression idExpr = new CPPASTIdExpression(parameterName);
			binExpr.setOperand2(idExpr);
			exprStmt.setExpression(binExpr);
			compound.addStatement(exprStmt);
			return compound;
		}

		private CPPASTFunctionDeclarator getSetterDeclarator(IASTName declaratorName) {
			CPPASTFunctionDeclarator declarator = new CPPASTFunctionDeclarator();
			declarator.setName(declaratorName);
			CPPASTParameterDeclaration parameterDeclaration = new CPPASTParameterDeclaration();
			IASTDeclarator parameterDeclarator = fieldDeclarator.copy(CopyStyle.withLocations);
			parameterDeclarator.setName(getSetterParameterName());
			if (passByReference) {
				parameterDeclarator.addPointerOperator(new CPPASTReferenceOperator(false));
			}
			parameterDeclaration.setDeclarator(parameterDeclarator);
			parameterDeclaration.setDeclSpecifier(getParamOrReturnDeclSpecifier());
			declarator.addParameterDeclaration(parameterDeclaration.copy(CopyStyle.withLocations));
			return declarator;
		}

		private CPPASTName getSetterParameterName() {
			String parameterName = GetterSetterNameGenerator.generateSetterParameterName(fieldName);
			return new CPPASTName(parameterName.toCharArray());
		}

		private static CPPASTSimpleDeclSpecifier getVoidDeclSpec() {
			CPPASTSimpleDeclSpecifier declSpecifier = new CPPASTSimpleDeclSpecifier();
			declSpecifier.setType(IASTSimpleDeclSpecifier.t_void);
			return declSpecifier;
		}
	}
}

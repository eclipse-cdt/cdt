/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core.qml.tests;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLListener;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.EcmaScriptProgramContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.FunctionCallContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.FunctionDeclarationContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlAttributeContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlHeaderItemContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlIdentifierContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlImportDeclarationContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlMemberContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlMembersContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlObjectLiteralContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlObjectRootContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlPragmaDeclarationContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlProgramContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlPropertyDeclarationContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlPropertyTypeContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlQualifiedIdContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QualifiedIdContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.SemiContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.SingleExpressionContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.SourceElementContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.SourceElementsContext;

public class AbstractQMLListener implements QMLListener {

	public AbstractQMLListener() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void visitTerminal(TerminalNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitErrorNode(ErrorNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterQmlProgram(QmlProgramContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitQmlProgram(QmlProgramContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterQmlHeaderItem(QmlHeaderItemContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitQmlHeaderItem(QmlHeaderItemContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterQmlImportDeclaration(QmlImportDeclarationContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitQmlImportDeclaration(QmlImportDeclarationContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterQmlQualifiedId(QmlQualifiedIdContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitQmlQualifiedId(QmlQualifiedIdContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterQmlPragmaDeclaration(QmlPragmaDeclarationContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitQmlPragmaDeclaration(QmlPragmaDeclarationContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterQmlObjectRoot(QmlObjectRootContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitQmlObjectRoot(QmlObjectRootContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterQmlObjectLiteral(QmlObjectLiteralContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitQmlObjectLiteral(QmlObjectLiteralContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterQmlMembers(QmlMembersContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitQmlMembers(QmlMembersContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterQmlMember(QmlMemberContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitQmlMember(QmlMemberContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterQmlPropertyType(QmlPropertyTypeContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitQmlPropertyType(QmlPropertyTypeContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterSemi(SemiContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitSemi(SemiContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterSingleExpression(SingleExpressionContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitSingleExpression(SingleExpressionContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterFunctionDeclaration(FunctionDeclarationContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitFunctionDeclaration(FunctionDeclarationContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterEcmaScriptProgram(EcmaScriptProgramContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitEcmaScriptProgram(EcmaScriptProgramContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterSourceElements(SourceElementsContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitSourceElements(SourceElementsContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterSourceElement(SourceElementContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitSourceElement(SourceElementContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterQmlIdentifier(QmlIdentifierContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitQmlIdentifier(QmlIdentifierContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterQmlAttribute(QmlAttributeContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitQmlAttribute(QmlAttributeContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterQmlPropertyDeclaration(QmlPropertyDeclarationContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitQmlPropertyDeclaration(QmlPropertyDeclarationContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterFunctionCall(FunctionCallContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitFunctionCall(FunctionCallContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterQualifiedId(QualifiedIdContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitQualifiedId(QualifiedIdContext ctx) {
		// TODO Auto-generated method stub

	}

}

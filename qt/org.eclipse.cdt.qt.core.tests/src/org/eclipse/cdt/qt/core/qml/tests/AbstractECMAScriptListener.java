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
import org.eclipse.cdt.internal.qt.core.qml.parser.ECMAScriptListener;
import org.eclipse.cdt.internal.qt.core.qml.parser.ECMAScriptParser.EcmaScriptProgramContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.ECMAScriptParser.FunctionCallContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.ECMAScriptParser.FunctionDeclarationContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.ECMAScriptParser.QualifiedIdContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.ECMAScriptParser.SemiContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.ECMAScriptParser.SingleExpressionContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.ECMAScriptParser.SourceElementContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.ECMAScriptParser.SourceElementsContext;

public class AbstractECMAScriptListener implements ECMAScriptListener {

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

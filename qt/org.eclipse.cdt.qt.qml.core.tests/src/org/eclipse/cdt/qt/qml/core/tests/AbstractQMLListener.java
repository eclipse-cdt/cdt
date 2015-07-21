package org.eclipse.cdt.qt.qml.core.tests;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.cdt.qt.qml.core.parser.QMLListener;
import org.eclipse.cdt.qt.qml.core.parser.QMLParser.FunctionDeclarationContext;
import org.eclipse.cdt.qt.qml.core.parser.QMLParser.QmlHeaderItemContext;
import org.eclipse.cdt.qt.qml.core.parser.QMLParser.QmlImportDeclarationContext;
import org.eclipse.cdt.qt.qml.core.parser.QMLParser.QmlMemberContext;
import org.eclipse.cdt.qt.qml.core.parser.QMLParser.QmlMembersContext;
import org.eclipse.cdt.qt.qml.core.parser.QMLParser.QmlObjectLiteralContext;
import org.eclipse.cdt.qt.qml.core.parser.QMLParser.QmlObjectRootContext;
import org.eclipse.cdt.qt.qml.core.parser.QMLParser.QmlPragmaDeclarationContext;
import org.eclipse.cdt.qt.qml.core.parser.QMLParser.QmlProgramContext;
import org.eclipse.cdt.qt.qml.core.parser.QMLParser.QmlPropertyTypeContext;
import org.eclipse.cdt.qt.qml.core.parser.QMLParser.QmlQualifiedIdContext;
import org.eclipse.cdt.qt.qml.core.parser.QMLParser.SemiContext;
import org.eclipse.cdt.qt.qml.core.parser.QMLParser.SingleExpressionContext;

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

}

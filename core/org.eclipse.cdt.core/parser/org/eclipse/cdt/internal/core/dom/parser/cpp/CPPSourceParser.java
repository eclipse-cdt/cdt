package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTAttributeOwner;
import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.AbstractSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.BacktrackException;
import org.eclipse.cdt.internal.core.dom.parser.DeclarationOptions;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;

/**
 * Source Parser for the C++ Language Syntax.
 */
public class CPPSourceParser extends AbstractSourceCodeParser {

	private ICPPASTTranslationUnit compilationUnit;
	private final IIndex index;

	public CPPSourceParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			ICPPParserExtensionConfiguration config, IIndex index) {
		super(scanner, parserMode, logService, CPPNodeFactory.getDefault(), config.getBuiltinBindingsProvider());
		this.index = index;
	}

	@Override
	protected IASTTranslationUnit getCompilationUnit() {
		return compilationUnit;
	}

	@Override
	protected void createCompilationUnit() throws Exception {
		compilationUnit = (ICPPASTTranslationUnit) nodeFactory.newTranslationUnit(scanner);
		compilationUnit.setIndex(index);

		// Add built-in names to the scope.
		if (builtinBindingsProvider != null) {
			if (compilationUnit instanceof ASTTranslationUnit) {
				((ASTTranslationUnit) compilationUnit).setupBuiltinBindings(builtinBindingsProvider);
			}
		}
	}

	@Override
	protected void destroyCompilationUnit() {
		compilationUnit = null;
	}

	@Override
	protected IASTDeclaration declaration(DeclarationOptions option) throws BacktrackException, EndOfFileException {
		return null;
	}

	@Override
	protected IASTStatement parseDeclarationOrExpressionStatement(List<IASTAttributeSpecifier> attributeSpecifiers)
			throws EndOfFileException, BacktrackException {
		// First attempt to parse an expressionStatement
		// Note: the method style cast ambiguity is handled in expression
		// Since it only happens when we are in a statement
		IToken mark = mark();
		IASTExpressionStatement expressionStatement = null;
		IToken afterExpression = null;
		boolean foundSemicolon = false;
		try {
			IASTExpression expression = expression();
			expressionStatement = nodeFactory.newExpressionStatement(expression);
			addAttributeSpecifiers(attributeSpecifiers, expressionStatement);
			setRange(expressionStatement, expression);
			afterExpression = lookahead();

			IToken semi = consumeOrEndOfCompletion(IToken.tSEMI);
			foundSemicolon = true;
			adjustEndOffset(expressionStatement, semi.getEndOffset());
			afterExpression = lookahead();
		} catch (BacktrackException b) {
		}

		backup(mark);

		// Now attempt to parse a declarationStatement
		IASTDeclarationStatement declarationStatement = null;
		try {
			IASTDeclaration d = declaration(DeclarationOptions.LOCAL);
			if (d instanceof IASTAttributeOwner) {
				addAttributeSpecifiers(attributeSpecifiers, (IASTAttributeOwner) d);
			}
			declarationStatement = nodeFactory.newDeclarationStatement(d);
			setRange(declarationStatement, d);
		} catch (BacktrackException exception) {
			IASTNode node = exception.getNodeBeforeProblem();
			final boolean isProblemDecl = node instanceof IASTDeclaration;
			if (expressionStatement == null
					|| (!foundSemicolon && isProblemDecl && node.contains(expressionStatement))) {
				if (isProblemDecl) {
					declarationStatement = nodeFactory.newDeclarationStatement((IASTDeclaration) node);
					exception.initialize(exception.getProblem(), setRange(declarationStatement, node));
				}
				throw exception;
			}
		}

		if (declarationStatement == null) {
			backup(afterExpression);
			if (foundSemicolon)
				return expressionStatement;

			throwBacktrack(createProblem(IProblem.MISSING_SEMICOLON, calculateEndOffset(expressionStatement) - 1, 1),
					expressionStatement);
			return null; // Hint for java-compiler
		}

		if (expressionStatement == null || !foundSemicolon) {
			return declarationStatement;
		}

		// At this point we know we have an ambiguity.
		// Attempt to resolve some ambiguities that are easy to detect.

		// A * B = C;  // A*B cannot be a lvalue.
		// foo() = x;  // foo() can be an lvalue in C++.
		if (expressionStatement.getExpression() instanceof IASTBinaryExpression) {
			IASTBinaryExpression exp = (IASTBinaryExpression) expressionStatement.getExpression();
			if (exp.getOperator() == IASTBinaryExpression.op_assign) {
				IASTExpression left = exp.getOperand1();
				if (left instanceof IASTBinaryExpression
						&& ((IASTBinaryExpression) left).getOperator() == IASTBinaryExpression.op_multiply) {
					return declarationStatement;
				}
			}
		}

		final IASTDeclaration declaration = declarationStatement.getDeclaration();
		if (declaration instanceof IASTSimpleDeclaration) {
			final IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
			IASTDeclSpecifier declspec = simpleDecl.getDeclSpecifier();
			if (declspec instanceof IASTNamedTypeSpecifier) {
				final IASTDeclarator[] declarators = simpleDecl.getDeclarators();

				// x;
				// can be parsed as a named declaration specifier without a declarator
				if (declarators.length == 0) {
					backup(afterExpression);
					return expressionStatement;
				}
			}
		}

		// create and return ambiguity node
		IASTAmbiguousStatement statement = createAmbiguousStatement();
		statement.addStatement(expressionStatement);
		statement.addStatement(declarationStatement);
		return setRange(statement, declarationStatement);
	}

}

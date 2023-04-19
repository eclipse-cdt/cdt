package org.eclipse.cdt.internal.core.dom.parser.c;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTAlignmentSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTAttributeOwner;
import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICNodeFactory;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.AbstractSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.BacktrackException;
import org.eclipse.cdt.internal.core.dom.parser.DeclarationOptions;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;
import org.eclipse.cdt.internal.core.dom.parser.ITemplateIdStrategy;

/**
 * Source Parser for the C Language Syntax.
 */
public class CSourceParser extends AbstractSourceCodeParser {

	private IASTTranslationUnit compilationUnit;
	private IIndex index;

	public static final int INLINE = 0x01, CONST = 0x02, RESTRICT = 0x04, VOLATILE = 0x08, SHORT = 0x10,
			UNSIGNED = 0x20, SIGNED = 0x40, COMPLEX = 0x80, IMAGINARY = 0x100;

	public CSourceParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			ICParserExtensionConfiguration config, IIndex index) {
		super(scanner, parserMode, logService, CNodeFactory.getDefault(), config.getBuiltinBindingsProvider());
		this.index = index;
	}

	@Override
	protected IASTTranslationUnit getCompilationUnit() {
		return compilationUnit;
	}

	@Override
	protected void createCompilationUnit() throws Exception {
		compilationUnit = nodeFactory.newTranslationUnit(scanner);
		compilationUnit.setIndex(index);

		// add built-in names to the scope
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
	protected IASTInitializer optionalInitializer(IASTDeclarator dtor, DeclarationOptions options)
			throws EndOfFileException, BacktrackException {
		if (options.fAllowInitializer && lookaheadTypeWithEndOfFile(1) == IToken.tASSIGN) {
			final int offset = consume().getOffset();
			IASTInitializerClause initClause = initClause();
			IASTEqualsInitializer result = nodeFactory.newEqualsInitializer(initClause);
			return setRange(result, offset, calculateEndOffset(initClause));
		}
		return null;
	}

	@Override
	protected IASTDeclarator initDeclarator(IASTDeclSpecifier declspec, final DeclarationOptions option)
			throws EndOfFileException, BacktrackException, FoundAggregateInitializer {
		IASTDeclarator declarator = declarator(declspec, option);

		final int type = lookaheadTypeWithEndOfFile(1);
		if (type == IToken.tLBRACE) {
			if (!(ASTQueries.findTypeRelevantDeclarator(declarator) instanceof IASTFunctionDeclarator)) {
				throwBacktrack(lookahead(1));
			}
		}

		if (type == IToken.tASSIGN && lookaheadType(2) == IToken.tLBRACE)
			throw new FoundAggregateInitializer(declspec, declarator);

		IASTInitializer initializer = optionalInitializer(declarator, option);
		if (initializer != null) {
			declarator.setInitializer(initializer);
			((ASTNode) declarator).setLength(calculateEndOffset(initializer) - ((ASTNode) declarator).getOffset());
		}
		return declarator;
	}

	@Override
	protected IASTDeclaration declaration(DeclarationOptions option) throws BacktrackException, EndOfFileException {
		switch (lookaheadType(1)) {
		/* asm is technically not part of C
		case IToken.t_asm:
			return asmDeclaration();
		*/
		case IToken.tSEMI:
			IToken semi = consume();
			IASTDeclSpecifier specifier = nodeFactory.newSimpleDeclSpecifier();
			IASTSimpleDeclaration decl = nodeFactory.newSimpleDeclaration(specifier);
			decl.setDeclSpecifier(specifier);
			((ASTNode) specifier).setOffsetAndLength(semi.getOffset(), 0);
			((ASTNode) decl).setOffsetAndLength(semi.getOffset(), semi.getLength());
			return decl;
		}

		return simpleDeclaration(option);
	}

	@Override
	protected IASTName identifier() throws EndOfFileException, BacktrackException {
		final IToken token = lookahead(1);
		IASTName name;
		switch (token.getType()) {
		case IToken.tIDENTIFIER:
			consume();
			name = nodeFactory.newName(token.getCharImage());
			setRange(name, token.getOffset(), token.getEndOffset());
			break;

		case IToken.tCOMPLETION:
		case IToken.tEOC:
			consume();
			name = nodeFactory.newName(token.getCharImage());
			setRange(name, token.getOffset(), token.getEndOffset());
			ASTCompletionNode node = createCompletionNode(token);
			if (node != null)
				node.addName(name);
			return name;

		default:
			throw backtrack;
		}

		return name;
	}

	@Override
	protected IASTExpression expression() throws BacktrackException, EndOfFileException {
		return expression(ExprKind.EXPRESSION);
	}

	@Override
	protected IASTExpression constantExpression() throws BacktrackException, EndOfFileException {
		return expression(ExprKind.CONSTANT);
	}

	@Override
	protected IASTExpression unaryExpression(CastExprCtx ctx, ITemplateIdStrategy strat)
			throws BacktrackException, EndOfFileException {
		switch (lookaheadType(1)) {
		case IToken.tSTAR:
			return unaryExpression(IASTUnaryExpression.op_star, ctx, strat);
		case IToken.tAMPER:
			return unaryExpression(IASTUnaryExpression.op_amper, ctx, strat);
		case IToken.tPLUS:
			return unaryExpression(IASTUnaryExpression.op_plus, ctx, strat);
		case IToken.tMINUS:
			return unaryExpression(IASTUnaryExpression.op_minus, ctx, strat);
		case IToken.tNOT:
			return unaryExpression(IASTUnaryExpression.op_not, ctx, strat);
		case IToken.tBITCOMPLEMENT:
			return unaryExpression(IASTUnaryExpression.op_tilde, ctx, strat);
		case IToken.tINCR:
			return unaryExpression(IASTUnaryExpression.op_prefixIncr, ctx, strat);
		case IToken.tDECR:
			return unaryExpression(IASTUnaryExpression.op_prefixDecr, ctx, strat);
		case IToken.t_sizeof:
			return parseTypeIDInParenthesisOrUnaryExpression(false, consume().getOffset(),
					IASTTypeIdExpression.op_sizeof, IASTUnaryExpression.op_sizeof, ctx, strat);
		case IToken.t__Alignof:
			return parseTypeIDInParenthesisOrUnaryExpression(false, consume().getOffset(),
					IASTTypeIdExpression.op_alignof, IASTUnaryExpression.op_alignOf, ctx, strat);
		default:
			return postfixExpression(ctx, strat);
		}
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
		// foo() = x;  // foo() cannot be an lvalue in C.
		if (expressionStatement.getExpression() instanceof IASTBinaryExpression) {
			IASTBinaryExpression exp = (IASTBinaryExpression) expressionStatement.getExpression();
			if (exp.getOperator() == IASTBinaryExpression.op_assign) {
				IASTExpression left = exp.getOperand1();
				if (left instanceof IASTBinaryExpression
						&& ((IASTBinaryExpression) left).getOperator() == IASTBinaryExpression.op_multiply) {
					return declarationStatement;
				}
				if (left instanceof IASTFunctionCallExpression) {
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

	@Override
	protected IASTExpression expressionWithOptionalTrailingEllipsis() throws BacktrackException, EndOfFileException {
		return expression();
	}

	@Override
	protected IASTTypeId typeIDWithOptionalTrailingEllipsis(DeclarationOptions option)
			throws EndOfFileException, BacktrackException {
		return typeID(option);
	}

	@Override
	protected IASTAlignmentSpecifier createAmbiguousAlignmentSpecifier(IASTAlignmentSpecifier expression,
			IASTAlignmentSpecifier typeId) {
		return new CASTAmbiguousAlignmentSpecifier(expression, typeId);
	}

	@Override
	protected IASTExpression primaryExpression(CastExprCtx ctx, ITemplateIdStrategy strat)
			throws BacktrackException, EndOfFileException {
		IToken token = null;
		IASTLiteralExpression literalExpression = null;
		switch (lookaheadType(1)) {
		// TO DO: we need more literals...
		case IToken.tINTEGER:
			token = consume();
			literalExpression = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_integer_constant,
					token.getImage());
			((ASTNode) literalExpression).setOffsetAndLength(token.getOffset(),
					token.getEndOffset() - token.getOffset());
			return literalExpression;
		case IToken.tFLOATINGPT:
			token = consume();
			literalExpression = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_float_constant,
					token.getImage());
			((ASTNode) literalExpression).setOffsetAndLength(token.getOffset(),
					token.getEndOffset() - token.getOffset());
			return literalExpression;
		case IToken.tSTRING:
		case IToken.tLSTRING:
		case IToken.tUTF16STRING:
		case IToken.tUTF32STRING:
			token = consume();
			literalExpression = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_string_literal,
					token.getImage());
			((ASTNode) literalExpression).setOffsetAndLength(token.getOffset(),
					token.getEndOffset() - token.getOffset());
			return literalExpression;
		case IToken.tCHAR:
		case IToken.tLCHAR:
		case IToken.tUTF16CHAR:
		case IToken.tUTF32CHAR:
			token = consume();
			literalExpression = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_char_constant,
					token.getImage());
			((ASTNode) literalExpression).setOffsetAndLength(token.getOffset(), token.getLength());
			return literalExpression;
		case IToken.tLPAREN:
			token = consume();
			IASTExpression left = expression(ExprKind.EXPRESSION); // instead of expression(), to keep the stack smaller
			int finalOffset = 0;
			switch (lookaheadType(1)) {
			case IToken.tRPAREN:
			case IToken.tEOC:
				finalOffset = consume().getEndOffset();
				break;
			default:
				throwBacktrack(lookahead(1));
			}
			return buildUnaryExpression(IASTUnaryExpression.op_bracketedPrimary, left, token.getOffset(), finalOffset);
		case IToken.tIDENTIFIER:
		case IToken.tCOMPLETION:
		case IToken.tEOC:
			int startingOffset = lookahead(1).getOffset();
			IASTName name = identifier();
			IASTIdExpression idExpression = nodeFactory.newIdExpression(name);
			((ASTNode) idExpression).setOffsetAndLength((ASTNode) name);
			return idExpression;
		default:
			IToken lookahead = lookahead(1);
			startingOffset = lookahead.getOffset();
			throwBacktrack(startingOffset, lookahead.getLength());
			return null;
		}
	}

	@Override
	protected IASTStatement statement() throws EndOfFileException, BacktrackException {
		switch (lookaheadType(1)) {
		// labeled statements
		case IToken.t_case:
			return parseCaseStatement();
		case IToken.t_default:
			return parseDefaultStatement();
		// compound statement
		case IToken.tLBRACE:
			return parseCompoundStatement();
		// selection statement
		case IToken.t_if:
			return parseIfStatement();
		case IToken.t_switch:
			return parseSwitchStatement();
		// iteration statements
		case IToken.t_while:
			return parseWhileStatement();
		case IToken.t_do:
			return parseDoStatement();
		case IToken.t_for:
			return parseForStatement();
		// jump statement
		case IToken.t_break:
			return parseBreakStatement();
		case IToken.t_continue:
			return parseContinueStatement();
		case IToken.t_return:
			return parseReturnStatement();
		case IToken.t_goto:
			return parseGotoStatement();
		case IToken.tSEMI:
			return parseNullStatement();
		default:
			// can be many things:
			// label
			if (lookaheadType(1) == IToken.tIDENTIFIER && lookaheadType(2) == IToken.tCOLON) {
				return parseLabelStatement();
			}

			return parseDeclarationOrExpressionStatement();
		}
	}

	@Override
	protected IASTExpression buildBinaryExpression(int operator, IASTExpression expr, IASTInitializerClause clause,
			int lastOffset) {
		IASTBinaryExpression result = nodeFactory.newBinaryExpression(operator, expr, (IASTExpression) clause);
		int offset = ((ASTNode) expr).getOffset();
		((ASTNode) result).setOffsetAndLength(offset, lastOffset - offset);
		return result;
	}

	@Override
	protected IASTAmbiguousStatement createAmbiguousStatement() {
		return new CASTAmbiguousStatement();
	}

	@Override
	protected IASTAmbiguousExpression createAmbiguousExpression() {
		return new CASTAmbiguousExpression();
	}

	@Override
	protected IASTAmbiguousExpression createAmbiguousBinaryVsCastExpression(IASTBinaryExpression binary,
			IASTCastExpression castExpr) {
		return new CASTAmbiguousBinaryVsCastExpression(binary, castExpr);
	}

	@Override
	protected IASTAmbiguousExpression createAmbiguousCastVsMethodCallExpression(IASTCastExpression castExpr,
			IASTFunctionCallExpression funcCall) {
		return new CASTAmbiguousCastVsFunctionCallExpression(castExpr, funcCall);
	}

	@Override
	protected IASTTypeId typeID(DeclarationOptions option) throws EndOfFileException, BacktrackException {
		if (!canBeTypeSpecifier()) {
			return null;
		}
		final int offset = mark().getOffset();
		IASTDeclSpecifier declSpecifier = null;
		IASTDeclarator declarator = null;

		try {
			Declaration decl = initDeclSpecifierSequenceDeclarator(option, false);
			declSpecifier = decl.leftSpecifier;
			declarator = decl.leftDeclarator;
		} catch (FoundAggregateInitializer lie) {
			// type-ids have not compound initializers
			throwBacktrack(lie.declarator);
		}

		IASTTypeId result = nodeFactory.newTypeId(declSpecifier, declarator);
		setRange(result, offset, figureEndOffset(declSpecifier, declarator));
		return result;
	}

	@Override
	protected Declaration declarationSpecifierSequence(DeclarationOptions option, ITemplateIdStrategy strat)
			throws BacktrackException, EndOfFileException {
		int storageClass = IASTDeclSpecifier.sc_unspecified;
		int simpleType = IASTSimpleDeclSpecifier.t_unspecified;
		int options = 0;
		int isLong = 0;

		IToken returnToken = null;
		ICASTDeclSpecifier result = null;
		ICASTDeclSpecifier altResult = null;
		IASTAlignmentSpecifier[] alignmentSpecifiers = IASTAlignmentSpecifier.EMPTY_ALIGNMENT_SPECIFIER_ARRAY;
		List<IASTAttributeSpecifier> attributes = null;
		try {
			IASTName identifier = null;
			IASTExpression typeofExpression = null;
			IASTProblem problem = null;

			boolean encounteredRawType = false;
			boolean encounteredTypename = false;

			final int offset = lookahead(1).getOffset();
			int endOffset = offset;

			declSpecifiers: for (;;) {
				final int type = lookaheadTypeWithEndOfFile(1);
				switch (type) {
				case 0: // eof
					break declSpecifiers;
				// storage class specifiers
				case IToken.t_auto:
					storageClass = IASTDeclSpecifier.sc_auto;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_register:
					storageClass = IASTDeclSpecifier.sc_register;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_static:
					storageClass = IASTDeclSpecifier.sc_static;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_extern:
					storageClass = IASTDeclSpecifier.sc_extern;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_typedef:
					storageClass = IASTDeclSpecifier.sc_typedef;
					endOffset = consume().getEndOffset();
					break;

				// Method Specifier
				case IToken.t_inline:
					options |= INLINE;
					endOffset = consume().getEndOffset();
					break;

				// Type Qualifiers
				case IToken.t_const:
					options |= CONST;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_volatile:
					options |= VOLATILE;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_restrict:
					options |= RESTRICT;
					endOffset = consume().getEndOffset();
					break;

				// Type Specifiers
				case IToken.t_void:
					if (encounteredTypename)
						break declSpecifiers;
					simpleType = IASTSimpleDeclSpecifier.t_void;
					encounteredRawType = true;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_char:
					if (encounteredTypename)
						break declSpecifiers;
					simpleType = IASTSimpleDeclSpecifier.t_char;
					encounteredRawType = true;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_short:
					if (encounteredTypename)
						break declSpecifiers;
					options |= SHORT;
					encounteredRawType = true;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_int:
					if (encounteredTypename)
						break declSpecifiers;
					simpleType = IASTSimpleDeclSpecifier.t_int;
					encounteredRawType = true;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_long:
					if (encounteredTypename)
						break declSpecifiers;
					isLong++;
					encounteredRawType = true;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_float:
					if (encounteredTypename)
						break declSpecifiers;
					simpleType = IASTSimpleDeclSpecifier.t_float;
					encounteredRawType = true;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_double:
					if (encounteredTypename)
						break declSpecifiers;
					simpleType = IASTSimpleDeclSpecifier.t_double;
					encounteredRawType = true;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_signed:
					if (encounteredTypename)
						break declSpecifiers;
					options |= SIGNED;
					encounteredRawType = true;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_unsigned:
					if (encounteredTypename)
						break declSpecifiers;
					options |= UNSIGNED;
					encounteredRawType = true;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t__Bool:
					if (encounteredTypename)
						break declSpecifiers;
					simpleType = IASTSimpleDeclSpecifier.t_bool;
					encounteredRawType = true;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t__Complex:
					if (encounteredTypename)
						break declSpecifiers;
					options |= COMPLEX;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t__Imaginary:
					if (encounteredTypename)
						break declSpecifiers;
					options |= IMAGINARY;
					endOffset = consume().getEndOffset();
					break;

				case IToken.tIDENTIFIER:
				case IToken.tCOMPLETION:
				case IToken.tEOC:
					if (encounteredTypename || encounteredRawType)
						break declSpecifiers;

					if ((endOffset != offset || option.fAllowEmptySpecifier)
							&& lookaheadType(1) != IToken.tCOMPLETION) {
						altResult = buildSimpleDeclSpec(storageClass, simpleType, options, isLong, typeofExpression,
								offset, endOffset);
						returnToken = mark();
					}

					identifier = identifier();
					endOffset = calculateEndOffset(identifier);
					encounteredTypename = true;
					break;
				case IToken.t_struct:
				case IToken.t_union:
					if (encounteredTypename || encounteredRawType)
						break declSpecifiers;
					try {
						result = structOrUnionSpecifier();
					} catch (BacktrackException exception) {
						result = elaboratedTypeSpecifier();
					}
					endOffset = calculateEndOffset(result);
					encounteredTypename = true;
					break;
				case IToken.t_enum:
					if (encounteredTypename || encounteredRawType)
						break declSpecifiers;
					try {
						result = (ICASTEnumerationSpecifier) enumSpecifier();
					} catch (BacktrackException exception) {
						if (exception.getNodeBeforeProblem() instanceof ICASTDeclSpecifier) {
							result = (ICASTDeclSpecifier) exception.getNodeBeforeProblem();
							problem = exception.getProblem();
							break declSpecifiers;
						} else {
							result = elaboratedTypeSpecifier();
						}
					}
					endOffset = calculateEndOffset(result);
					encounteredTypename = true;
					break;

				case IToken.t__Alignas:
					alignmentSpecifiers = ArrayUtil.append(alignmentSpecifiers, alignmentSpecifier());
					break;

				default:
					break declSpecifiers;
				}

				if (encounteredRawType && encounteredTypename)
					throwBacktrack(lookahead(1));
			}

			// check for empty specification
			if (!encounteredRawType && !encounteredTypename && lookaheadType(1) != IToken.tEOC
					&& !option.fAllowEmptySpecifier) {
				if (offset == endOffset) {
					throwBacktrack(lookahead(1));
				}
			}

			if (result != null) {
				configureDeclSpec(result, storageClass, options);
				if ((options & RESTRICT) != 0) {
					if (result instanceof ICASTCompositeTypeSpecifier) {
						((ICASTCompositeTypeSpecifier) result).setRestrict(true);
					} else if (result instanceof CASTEnumerationSpecifier) {
						((CASTEnumerationSpecifier) result).setRestrict(true);
					} else if (result instanceof CASTElaboratedTypeSpecifier) {
						((CASTElaboratedTypeSpecifier) result).setRestrict(true);
					}
				}
				setRange(result, offset, endOffset);
				if (problem != null)
					throwBacktrack(problem, result);
			} else if (identifier != null) {
				result = buildNamedTypeSpecifier(identifier, storageClass, options, offset, endOffset);
			} else {
				result = buildSimpleDeclSpec(storageClass, simpleType, options, isLong, typeofExpression, offset,
						endOffset);
			}
			result.setAlignmentSpecifiers(ArrayUtil.trim(alignmentSpecifiers));
			addAttributeSpecifiers(attributes, result);
			endOffset = attributesEndOffset(endOffset, attributes);
			setRange(result, offset, endOffset);
		} catch (BacktrackException exception) {
			if (returnToken != null) {
				backup(returnToken);
				result = altResult;
				altResult = null;
				returnToken = null;
			} else {
				throw exception;
			}
		}
		Declaration target = new Declaration();
		target.leftSpecifier = result;
		target.rightSpecifier = altResult;
		target.declaratorToken = returnToken;
		return target;
	}

	/**
	 * Parse a class/struct/union definition.
	 *
	 * classSpecifier : classKey name (baseClause)? "{" (memberSpecification)*
	 * "}"
	 *
	 * @throws BacktrackException to request a backtrack
	 */
	protected ICASTCompositeTypeSpecifier structOrUnionSpecifier() throws BacktrackException, EndOfFileException {
		int classKind = 0;
		IToken mark = mark();
		final int offset = mark.getOffset();

		// class key
		switch (lookaheadType(1)) {
		case IToken.t_struct:
			consume();
			classKind = IASTCompositeTypeSpecifier.k_struct;
			break;
		case IToken.t_union:
			consume();
			classKind = IASTCompositeTypeSpecifier.k_union;
			break;
		default:
			throwBacktrack(lookahead(1));
			return null; // line never reached, hint for the parser.
		}

		// WIP C23
		List<IASTAttributeSpecifier> attributes = new ArrayList<>();

		// class name
		IASTName name = null;
		if (lookaheadType(1) == IToken.tIDENTIFIER) {
			name = identifier();
		}

		if (lookaheadType(1) != IToken.tLBRACE) {
			IToken errorPoint = lookahead(1);
			backup(mark);
			throwBacktrack(errorPoint);
		}

		if (name == null) {
			name = nodeFactory.newName();
		}
		ICASTCompositeTypeSpecifier result = (ICASTCompositeTypeSpecifier) nodeFactory
				.newCompositeTypeSpecifier(classKind, name);
		declarationListInBraces(result, offset, DeclarationOptions.C_MEMBER);
		addAttributeSpecifiers(attributes, result);
		return result;
	}

	protected ICASTElaboratedTypeSpecifier elaboratedTypeSpecifier() throws BacktrackException, EndOfFileException {
		// this is an elaborated class specifier
		IToken token = consume();
		int kind = 0;

		switch (token.getType()) {
		case IToken.t_struct:
			kind = IASTElaboratedTypeSpecifier.k_struct;
			break;
		case IToken.t_union:
			kind = IASTElaboratedTypeSpecifier.k_union;
			break;
		case IToken.t_enum:
			kind = IASTElaboratedTypeSpecifier.k_enum;
			break;
		default:
			backup(token);
			throwBacktrack(token.getOffset(), token.getLength());
		}

		IASTName name = identifier();
		ICASTElaboratedTypeSpecifier result = (ICASTElaboratedTypeSpecifier) nodeFactory
				.newElaboratedTypeSpecifier(kind, name);
		((ASTNode) result).setOffsetAndLength(token.getOffset(), calculateEndOffset(name) - token.getOffset());
		return result;
	}

	protected IASTStatement parseForStatement() throws EndOfFileException, BacktrackException {
		int startOffset;
		startOffset = consume().getOffset();
		consume(IToken.tLPAREN);
		IASTStatement init = initStatement();
		IASTExpression forCondition = null;
		switch (lookaheadType(1)) {
		case IToken.tSEMI:
		case IToken.tEOC:
			break;
		default:
			forCondition = condition(false);
		}
		switch (lookaheadType(1)) {
		case IToken.tSEMI:
			consume();
			break;
		case IToken.tEOC:
			break;
		default:
			throw backtrack;
		}
		IASTExpression iterationExpression = null;
		switch (lookaheadType(1)) {
		case IToken.tRPAREN:
		case IToken.tEOC:
			break;
		default:
			iterationExpression = expression();
		}
		switch (lookaheadType(1)) {
		case IToken.tRPAREN:
			consume();
			break;
		case IToken.tEOC:
			break;
		default:
			throw backtrack;
		}

		IASTForStatement forStatement = nodeFactory.newForStatement(init, forCondition, iterationExpression, null);
		if (lookaheadType(1) != IToken.tEOC) {
			IASTStatement forBody = statement();
			((ASTNode) forStatement).setOffsetAndLength(startOffset, calculateEndOffset(forBody) - startOffset);
			forStatement.setBody(forBody);
		}
		return forStatement;
	}

	protected IASTStatement parseSwitchStatement() throws EndOfFileException, BacktrackException {
		int startOffset;
		startOffset = consume().getOffset();
		consume(IToken.tLPAREN);
		IASTExpression switchCondition = condition(true);
		switch (lookaheadType(1)) {
		case IToken.tRPAREN:
			consume();
			break;
		case IToken.tEOC:
			break;
		default:
			throwBacktrack(lookahead(1));
		}

		IASTStatement switchBody = parseSwitchBody();
		IASTSwitchStatement switch_statement = nodeFactory.newSwitchStatement(switchCondition, switchBody);
		((ASTNode) switch_statement).setOffsetAndLength(startOffset,
				(switchBody != null ? calculateEndOffset(switchBody) : lookahead(1).getEndOffset()) - startOffset);
		return switch_statement;
	}

	protected IASTStatement parseIfStatement() throws EndOfFileException, BacktrackException {
		IASTIfStatement result = null;
		IASTIfStatement ifStatement = null;
		int start = lookahead(1).getOffset();
		ifLoop: while (true) {
			int so = consume(IToken.t_if).getOffset();
			consume(IToken.tLPAREN);
			// condition
			IASTExpression condition = condition(true);
			if (lookaheadType(1) == IToken.tEOC) {
				// Completing in the condition
				IASTIfStatement newIf = nodeFactory.newIfStatement(condition, null, null);

				if (ifStatement != null) {
					ifStatement.setElseClause(newIf);
				}
				return result != null ? result : newIf;
			}
			consume(IToken.tRPAREN);

			IASTStatement thenClause = statement();
			IASTIfStatement newIfStatement = nodeFactory.newIfStatement(null, null, null);
			((ASTNode) newIfStatement).setOffset(so);
			// shouldn't be possible but failure in condition() makes it so
			if (condition != null) {
				newIfStatement.setConditionExpression(condition);
			}
			if (thenClause != null) {
				newIfStatement.setThenClause(thenClause);
				((ASTNode) newIfStatement)
						.setLength(calculateEndOffset(thenClause) - ((ASTNode) newIfStatement).getOffset());
			}
			if (lookaheadType(1) == IToken.t_else) {
				consume();
				if (lookaheadType(1) == IToken.t_if) {
					// an else if, don't recurse, just loop and do another if

					if (ifStatement != null) {
						ifStatement.setElseClause(newIfStatement);
						((ASTNode) ifStatement)
								.setLength(calculateEndOffset(newIfStatement) - ((ASTNode) ifStatement).getOffset());
					}
					if (result == null && ifStatement != null)
						result = ifStatement;
					if (result == null)
						result = newIfStatement;

					ifStatement = newIfStatement;
					continue ifLoop;
				}
				IASTStatement elseStatement = statement();
				newIfStatement.setElseClause(elseStatement);
				if (ifStatement != null) {
					ifStatement.setElseClause(newIfStatement);
					((ASTNode) ifStatement)
							.setLength(calculateEndOffset(newIfStatement) - ((ASTNode) ifStatement).getOffset());
				} else {
					if (result == null)
						result = newIfStatement;
					ifStatement = newIfStatement;
				}
			} else {
				if (thenClause != null)
					((ASTNode) newIfStatement).setLength(calculateEndOffset(thenClause) - start);
				if (ifStatement != null) {
					ifStatement.setElseClause(newIfStatement);
					((ASTNode) newIfStatement).setLength(calculateEndOffset(newIfStatement) - start);
				}
				if (result == null && ifStatement != null)
					result = ifStatement;
				if (result == null)
					result = newIfStatement;

				ifStatement = newIfStatement;
			}
			break ifLoop;
		}

		reconcileLengths(result);
		return result;
	}

	private IASTDeclarator declarator(IASTDeclSpecifier declSpec, DeclarationOptions option)
			throws EndOfFileException, BacktrackException {
		final int startingOffset = lookahead(1).getOffset();
		int endOffset = startingOffset;

		List<IASTPointerOperator> pointerOps = new ArrayList<>(4);
		consumePointerOperators(pointerOps);
		if (!pointerOps.isEmpty()) {
			endOffset = calculateEndOffset(pointerOps.get(pointerOps.size() - 1));
		}

		// C23 has the specifier sequence, so this is a WIP
		List<IASTAttributeSpecifier> attributes = new ArrayList<>();

		// Look for identifier or nested declarator
		final int type = lookaheadType(1);
		if (type == IToken.tIDENTIFIER) {
			if (option.fRequireAbstract)
				throwBacktrack(lookahead(1));

			final IASTName declaratorName = identifier();
			endOffset = calculateEndOffset(declaratorName);
			return declarator(pointerOps, attributes, declaratorName, null, startingOffset, endOffset, option);
		}

		if (type == IToken.tLPAREN) {
			IASTDeclarator cand1 = null;
			IToken cand1End = null;
			// try an abstract method declarator
			if (option.fAllowAbstract) {
				final IToken mark = mark();
				try {
					cand1 = declarator(pointerOps, attributes, nodeFactory.newName(), null, startingOffset, endOffset,
							option);
					if (option.fRequireAbstract)
						return cand1;

					cand1End = lookahead(1);
				} catch (BacktrackException exception) {
				}
				backup(mark);
			}
			// try a nested declarator
			try {
				consume();
				if (lookaheadType(1) == IToken.tRPAREN)
					throwBacktrack(lookahead(1));

				final IASTDeclarator nested = declarator(declSpec, option);
				endOffset = consume(IToken.tRPAREN).getEndOffset();
				final IASTDeclarator cand2 = declarator(pointerOps, attributes, null, nested, startingOffset, endOffset,
						option);
				if (cand1 == null || cand1End == null)
					return cand2;
				final IToken cand2End = lookahead(1);
				if (cand1End == cand2End) {
					CASTAmbiguousDeclarator result = new CASTAmbiguousDeclarator(cand1, cand2);
					((ASTNode) result).setOffsetAndLength((ASTNode) cand1);
					return result;
				}
				// use the longer variant
				if (cand1End.getOffset() < cand2End.getOffset())
					return cand2;

			} catch (BacktrackException exception) {
				if (cand1 == null)
					throw exception;
			}
			backup(cand1End);
			return cand1;
		}

		// try abstract declarator
		if (!option.fAllowAbstract) {
			throwBacktrack(lookahead(1));
		}
		return declarator(pointerOps, attributes, nodeFactory.newName(), null, startingOffset, endOffset, option);
	}

	private IASTDeclarator declarator(final List<IASTPointerOperator> pointerOps,
			List<IASTAttributeSpecifier> attributes, final IASTName declaratorName,
			final IASTDeclarator nestedDeclarator, final int startingOffset, int endOffset,
			final DeclarationOptions option) throws EndOfFileException, BacktrackException {
		IASTDeclarator result = null;
		int type;
		loop: while (true) {
			type = lookaheadTypeWithEndOfFile(1);
			switch (type) {
			case IToken.tLPAREN:
				result = methodDeclarator(isAbstract(declaratorName, nestedDeclarator) ? DeclarationOptions.PARAMETER
						: DeclarationOptions.C_PARAMETER_NON_ABSTRACT);
				setDeclaratorID(result, declaratorName, nestedDeclarator);
				break loop;

			case IToken.tLBRACKET:
				result = arrayDeclarator();
				setDeclaratorID(result, declaratorName, nestedDeclarator);
				break loop;

			case IToken.tCOLON:
				if (!option.fAllowBitField)
					throwBacktrack(lookahead(1));

				result = bitFieldDeclarator();
				setDeclaratorID(result, declaratorName, nestedDeclarator);
				break loop;
			// C23 attributes?
			default:
				break loop;
			}
		}

		if (result == null) {
			result = nodeFactory.newDeclarator(null);
			setDeclaratorID(result, declaratorName, nestedDeclarator);
		} else {
			endOffset = calculateEndOffset(result);
		}

		/*
		if (type != 0 && lookaheadType(1) == IToken.t_asm) { // asm labels bug 226121
			consume();
			endOffset = asmExpression(null).getEndOffset();
		}
		*/

		for (IASTPointerOperator operator : pointerOps) {
			result.addPointerOperator(operator);
		}

		if (attributes != null) {
			for (IASTAttributeSpecifier specifier : attributes) {
				result.addAttributeSpecifier(specifier);
			}
		}

		((ASTNode) result).setOffsetAndLength(startingOffset, endOffset - startingOffset);
		return result;
	}

	private IASTDeclaration simpleDeclaration(final DeclarationOptions declOption)
			throws BacktrackException, EndOfFileException {
		if (lookaheadType(1) == IToken.tLBRACE)
			throwBacktrack(lookahead(1));

		final int firstOffset = lookahead(1).getOffset();
		int endOffset = firstOffset;
		boolean insertSemi = false;

		IASTDeclSpecifier declSpec = null;
		IASTDeclarator dtor = null;
		IASTDeclSpecifier altDeclSpec = null;
		IASTDeclarator altDtor = null;
		IToken markBeforDtor = null;
		try {
			Declaration decl = initDeclSpecifierSequenceDeclarator(declOption, true);
			markBeforDtor = decl.declaratorToken;
			declSpec = decl.leftSpecifier;
			dtor = decl.leftDeclarator;
			altDeclSpec = decl.rightSpecifier;
			altDtor = decl.rightDeclarator;
		} catch (FoundAggregateInitializer lie) {
			declSpec = lie.specifier;
			// scalability: don't keep references to tokens, initializer may be large
			declarationMark = null;
			dtor = addInitializer(lie, declOption);
		} catch (BacktrackException exception) {
			IASTNode node = exception.getNodeBeforeProblem();
			if (node instanceof IASTDeclSpecifier) {
				IASTSimpleDeclaration d = nodeFactory.newSimpleDeclaration((IASTDeclSpecifier) node);
				setRange(d, node);
				throwBacktrack(exception.getProblem(), d);
			}
			throw exception;
		}

		IASTDeclarator[] declarators = IASTDeclarator.EMPTY_DECLARATOR_ARRAY;
		if (dtor != null) {
			declarators = new IASTDeclarator[] { dtor };
			while (lookaheadTypeWithEndOfFile(1) == IToken.tCOMMA) {
				consume();
				try {
					dtor = initDeclarator(declSpec, declOption);
				} catch (FoundAggregateInitializer found) {
					// scalability: don't keep references to tokens, initializer may be large
					declarationMark = null;
					markBeforDtor = null;
					dtor = addInitializer(found, declOption);
				}
				declarators = ArrayUtil.append(IASTDeclarator.class, declarators, dtor);
			}
			declarators = ArrayUtil.removeNulls(IASTDeclarator.class, declarators);
		}

		final int lt1 = lookaheadTypeWithEndOfFile(1);
		switch (lt1) {
		case IToken.tEOC:
			endOffset = figureEndOffset(declSpec, declarators);
			break;
		case IToken.tSEMI:
			endOffset = consume().getEndOffset();
			break;
		case IToken.tLBRACE:
			return methodDefinition(firstOffset, declSpec, declarators);

		default:
			insertSemi = true;
			if (declOption == DeclarationOptions.LOCAL) {
				endOffset = figureEndOffset(declSpec, declarators);
				if (firstOffset != endOffset) {
					break;
				}
			} else {
				if (markBeforDtor != null) {
					endOffset = calculateEndOffset(declSpec);
					if (firstOffset != endOffset && !isOnSameLine(endOffset, markBeforDtor.getOffset())) {
						backup(markBeforDtor);
						declarators = IASTDeclarator.EMPTY_DECLARATOR_ARRAY;
						break;
					}
				}
				endOffset = figureEndOffset(declSpec, declarators);
				if (lt1 == 0) {
					break;
				}
				if (firstOffset != endOffset) {
					if (!isOnSameLine(endOffset, lookahead(1).getOffset())) {
						break;
					}
					if (declarators.length == 1 && declarators[0] instanceof IASTFunctionDeclarator) {
						break;
					}
				}
			}
			throwBacktrack(lookahead(1));
		}

		// no method body
		IASTSimpleDeclaration simpleDeclaration = nodeFactory.newSimpleDeclaration(declSpec);
		for (IASTDeclarator declarator : declarators)
			simpleDeclaration.addDeclarator(declarator);

		setRange(simpleDeclaration, firstOffset, endOffset);
		if (altDeclSpec != null && altDtor != null) {
			simpleDeclaration = new CASTAmbiguousSimpleDeclaration(simpleDeclaration, altDeclSpec, altDtor);
			setRange(simpleDeclaration, firstOffset, endOffset);
		}

		if (insertSemi) {
			IASTProblem problem = createProblem(IProblem.MISSING_SEMICOLON, endOffset - 1, 1);
			throwBacktrack(problem, simpleDeclaration);
		}
		return simpleDeclaration;
	}

	private boolean isAbstract(IASTName declaratorName, IASTDeclarator nestedDeclarator) {
		nestedDeclarator = ASTQueries.findInnermostDeclarator(nestedDeclarator);
		if (nestedDeclarator != null) {
			declaratorName = nestedDeclarator.getName();
		}
		return declaratorName == null || declaratorName.toCharArray().length == 0;
	}

	private void setDeclaratorID(IASTDeclarator declarator, IASTName declaratorName, IASTDeclarator nestedDeclarator) {
		if (nestedDeclarator != null) {
			declarator.setNestedDeclarator(nestedDeclarator);
			declarator.setName(nodeFactory.newName());
		} else {
			declarator.setName(declaratorName);
		}
	}

	private IASTDeclarator methodDeclarator(DeclarationOptions paramOption)
			throws EndOfFileException, BacktrackException {
		IToken last = consume(IToken.tLPAREN);
		int startOffset = last.getOffset();

		boolean seenParameter = false;
		boolean encounteredVarArgs = false;
		List<IASTParameterDeclaration> parameters = null;
		int endOffset = last.getEndOffset();

		paramLoop: while (true) {
			switch (lookaheadType(1)) {
			case IToken.tRPAREN:
			case IToken.tEOC:
				endOffset = consume().getEndOffset();
				break paramLoop;
			case IToken.tELLIPSIS:
				endOffset = consume().getEndOffset();
				encounteredVarArgs = true;
				break;
			case IToken.tCOMMA:
				endOffset = consume().getEndOffset();
				seenParameter = false;
				break;
			default:
				if (seenParameter)
					throwBacktrack(startOffset, endOffset - startOffset);

				IASTParameterDeclaration paramDecl = parameterDeclaration(paramOption);
				endOffset = calculateEndOffset(paramDecl);
				if (parameters == null)
					parameters = new ArrayList<>(4);
				parameters.add(paramDecl);
				seenParameter = true;
				break;
			}
		}
		IASTStandardFunctionDeclarator method = nodeFactory.newFunctionDeclarator(null);
		method.setVarArgs(encounteredVarArgs);
		if (parameters != null) {
			for (IASTParameterDeclaration paramDecl : parameters) {
				method.addParameterDeclaration(paramDecl);
			}
		}
		((ASTNode) method).setOffsetAndLength(startOffset, endOffset - startOffset);
		return method;
	}

	/**
	 * Parse a Pointer Operator.
	 *
	 * ptrOperator : "*" (cvQualifier)* | "&" | ::? nestedNameSpecifier "*"
	 * (cvQualifier)*
	 *
	 * @throws BacktrackException to request a backtrack
	 */
	private void consumePointerOperators(List<IASTPointerOperator> pointerOps)
			throws EndOfFileException, BacktrackException {
		for (;;) {
			// C23 attributes soon?

			IToken mark = mark();
			IToken last = null;

			boolean isConst = false, isVolatile = false, isRestrict = false;

			if (lookaheadType(1) != IToken.tSTAR) {
				backup(mark);
				break;
			}

			last = consume();
			int startOffset = mark.getOffset();
			for (;;) {
				IToken token = lookahead(1);
				switch (lookaheadType(1)) {
				case IToken.t_const:
					last = consume();
					isConst = true;
					break;
				case IToken.t_volatile:
					last = consume();
					isVolatile = true;
					break;
				case IToken.t_restrict:
					last = consume();
					isRestrict = true;
					break;
				}

				if (token == lookahead(1))
					break;
			}

			ICASTPointer pointer = (ICASTPointer) nodeFactory.newPointer();
			((ASTNode) pointer).setOffsetAndLength(startOffset, last.getEndOffset() - startOffset);
			pointer.setConst(isConst);
			pointer.setVolatile(isVolatile);
			pointer.setRestrict(isRestrict);
			pointerOps.add(pointer);
		}
	}

	private void consumeArrayModifiers(List<IASTArrayModifier> arrayMods)
			throws EndOfFileException, BacktrackException {
		while (lookaheadType(1) == IToken.tLBRACKET) {
			// eat the '['
			int startOffset = consume().getOffset();

			boolean isStatic = false;
			boolean isConst = false;
			boolean isRestrict = false;
			boolean isVolatile = false;
			boolean isVarSized = false;

			outerLoop: do {
				switch (lookaheadType(1)) {
				case IToken.t_static:
					isStatic = true;
					consume();
					break;
				case IToken.t_const:
					isConst = true;
					consume();
					break;
				case IToken.t_volatile:
					isVolatile = true;
					consume();
					break;
				case IToken.t_restrict:
					isRestrict = true;
					consume();
					break;
				case IToken.tSTAR:
					isVarSized = true;
					consume();
					break outerLoop;
				default:
					break outerLoop;
				}
			} while (true);

			IASTExpression exp = null;

			if (lookaheadType(1) != IToken.tRBRACKET) {
				if (!(isStatic || isRestrict || isConst || isVolatile))
					exp = expression(ExprKind.ASSIGNMENT);
				else
					exp = constantExpression();
			}
			int lastOffset;
			switch (lookaheadType(1)) {
			case IToken.tRBRACKET:
				lastOffset = consume().getEndOffset();
				break;
			case IToken.tEOC:
				lastOffset = Integer.MAX_VALUE;
				break;
			default:
				throw backtrack;
			}

			ICASTArrayModifier arrayModifier = (ICASTArrayModifier) nodeFactory.newArrayModifier(exp);
			arrayModifier.setStatic(isStatic);
			arrayModifier.setConst(isConst);
			arrayModifier.setVolatile(isVolatile);
			arrayModifier.setRestrict(isRestrict);
			arrayModifier.setVariableSized(isVarSized);
			((ASTNode) arrayModifier).setOffsetAndLength(startOffset, lastOffset - startOffset);
			arrayMods.add(arrayModifier);
		}
	}

	private IASTInitializerClause initClause() throws EndOfFileException, BacktrackException {
		final int offset = lookahead(1).getOffset();
		if (lookaheadType(1) != IToken.tLBRACE)
			return expression(ExprKind.ASSIGNMENT);

		// it's an aggregate initializer
		consume(IToken.tLBRACE);
		IASTInitializerList result = nodeFactory.newInitializerList();

		for (;;) {
			final int checkOffset = lookahead(1).getOffset();
			// required at least one initializer list
			// get designator list
			List<? extends ICASTDesignator> designator = designatorList();
			if (designator == null) {
				IASTInitializerClause clause = initClause();
				if (result.getSize() >= Integer.MAX_VALUE && !ASTQueries.canContainName(clause)) {
					compilationUnit.setHasNodesOmitted(true);
					clause = null;
				}
				// depending on value of skipTrivialItemsInCompoundInitializers initializer may be null
				// in any way add the initializer such that the actual size can be tracked.
				result.addClause(clause);
			} else {
				ICASTDesignatedInitializer desigInitializer = ((ICNodeFactory) nodeFactory)
						.newDesignatedInitializer((IASTInitializerClause) null);
				setRange(desigInitializer, designator.get(0));
				for (ICASTDesignator d : designator) {
					desigInitializer.addDesignator(d);
				}

				if (lookaheadType(1) != IToken.tEOC) {
					consume(IToken.tASSIGN);
					IASTInitializerClause clause = initClause();
					desigInitializer.setOperand(clause);
					adjustLength(desigInitializer, clause);
				}
				result.addClause(desigInitializer);
			}

			// can end with ", }" or "}"
			boolean canContinue = lookaheadType(1) == IToken.tCOMMA;
			if (canContinue)
				consume();

			switch (lookaheadType(1)) {
			case IToken.tRBRACE:
				int lastOffset = consume().getEndOffset();
				setRange(result, offset, lastOffset);
				return result;

			case IToken.tEOC:
				setRange(result, offset, lookahead(1).getOffset());
				return result;
			}

			if (!canContinue || lookahead(1).getOffset() == checkOffset) {
				throwBacktrack(offset, lookahead(1).getEndOffset() - offset);
			}
		}
		// consume the closing brace
	}

	/**
	 * Parse an array declarator starting at the square bracket.
	 */
	private IASTArrayDeclarator arrayDeclarator() throws EndOfFileException, BacktrackException {
		ArrayList<IASTArrayModifier> arrayModifiers = new ArrayList<>(4);
		int start = lookahead(1).getOffset();
		consumeArrayModifiers(arrayModifiers);
		if (arrayModifiers.isEmpty())
			throwBacktrack(lookahead(1));

		final int endOffset = calculateEndOffset(arrayModifiers.get(arrayModifiers.size() - 1));
		final IASTArrayDeclarator declarator = nodeFactory.newArrayDeclarator(null);
		for (IASTArrayModifier modifier : arrayModifiers) {
			declarator.addArrayModifier(modifier);
		}

		((ASTNode) declarator).setOffsetAndLength(start, endOffset - start);
		return declarator;
	}

	/**
	 * Parses for a bit field declarator starting with the colon
	 */
	private IASTFieldDeclarator bitFieldDeclarator() throws EndOfFileException, BacktrackException {
		int start = consume(IToken.tCOLON).getOffset();

		final IASTExpression bitField = constantExpression();
		final int endOffset = calculateEndOffset(bitField);

		IASTFieldDeclarator declarator = nodeFactory.newFieldDeclarator(null, bitField);
		declarator.setBitFieldSize(bitField);

		((ASTNode) declarator).setOffsetAndLength(start, endOffset - start);
		return declarator;
	}

	private IASTDeclaration methodDefinition(int firstOffset, IASTDeclSpecifier declSpec, IASTDeclarator[] declarators)
			throws BacktrackException, EndOfFileException {
		if (declarators.length != 1)
			throwBacktrack(firstOffset, lookahead(1).getEndOffset());

		final IASTDeclarator outerDtor = declarators[0];
		final IASTDeclarator fdtor = ASTQueries.findTypeRelevantDeclarator(outerDtor);
		if (!(fdtor instanceof IASTFunctionDeclarator))
			throwBacktrack(firstOffset, lookahead(1).getEndOffset() - firstOffset);

		IASTFunctionDefinition methodDefinition = nodeFactory.newFunctionDefinition(declSpec,
				(IASTFunctionDeclarator) fdtor, null);

		try {
			IASTStatement statement = handleMethodBody();
			methodDefinition.setBody(statement);
			((ASTNode) methodDefinition).setOffsetAndLength(firstOffset, calculateEndOffset(statement) - firstOffset);

			return methodDefinition;
		} catch (BacktrackException exception) {
			final IASTNode node = exception.getNodeBeforeProblem();
			if (node instanceof IASTCompoundStatement) {
				methodDefinition.setBody((IASTCompoundStatement) node);
				((ASTNode) methodDefinition).setOffsetAndLength(firstOffset, calculateEndOffset(node) - firstOffset);
				throwBacktrack(exception.getProblem(), methodDefinition);
			}
			throw exception;
		}
	}

	private IASTExpression expression(final ExprKind kind) throws EndOfFileException, BacktrackException {
		final boolean allowComma = kind == ExprKind.EXPRESSION;
		boolean allowAssignment = kind != ExprKind.CONSTANT;
		int type;
		int conditionCount = 0;
		BinaryOperator lastOperator = null;
		IASTExpression lastExpression = castExpression(CastExprCtx.DIRECTLY_IN_BINARY_EXPR, null);
		loop: while (true) {
			type = lookaheadType(1);
			switch (type) {
			case IToken.tQUESTION:
				conditionCount++;
				// <logical-or> ? <expression> : <assignment-expression>
				// Precedence: 25 is lower than precedence of logical or; 0 is lower than precedence of expression
				lastOperator = new BinaryOperator(lastOperator, lastExpression, type, 25, 0);
				allowAssignment = true; // assignment expressions will be subsumed by the conditional expression
				break;

			case IToken.tCOLON:
				if (--conditionCount < 0)
					break loop;

				// <logical-or> ? <expression> : <assignment-expression>
				// Precedence: 0 is lower than precedence of expression; 15 is lower than precedence of assignment;
				lastOperator = new BinaryOperator(lastOperator, lastExpression, type, 0, 15);
				allowAssignment = true; // assignment expressions will be subsumed by the conditional expression
				break;

			case IToken.tCOMMA:
				if (!allowComma && conditionCount == 0)
					break loop;
				// Lowest precedence except inside the conditional expression
				lastOperator = new BinaryOperator(lastOperator, lastExpression, type, 10, 11);
				break;

			case IToken.tASSIGN:
			case IToken.tSTARASSIGN:
			case IToken.tDIVASSIGN:
			case IToken.tMODASSIGN:
			case IToken.tPLUSASSIGN:
			case IToken.tMINUSASSIGN:
			case IToken.tSHIFTRASSIGN:
			case IToken.tSHIFTLASSIGN:
			case IToken.tAMPERASSIGN:
			case IToken.tXORASSIGN:
			case IToken.tBITORASSIGN:
				if (!allowAssignment && conditionCount == 0)
					break loop;
				// Assignments group right to left
				lastOperator = new BinaryOperator(lastOperator, lastExpression, type, 21, 20);
				break;

			case IToken.tOR:
				lastOperator = new BinaryOperator(lastOperator, lastExpression, type, 30, 31);
				break;
			case IToken.tAND:
				lastOperator = new BinaryOperator(lastOperator, lastExpression, type, 40, 41);
				break;
			case IToken.tBITOR:
				lastOperator = new BinaryOperator(lastOperator, lastExpression, type, 50, 51);
				break;
			case IToken.tXOR:
				lastOperator = new BinaryOperator(lastOperator, lastExpression, type, 60, 61);
				break;
			case IToken.tAMPER:
				lastOperator = new BinaryOperator(lastOperator, lastExpression, type, 70, 71);
				break;
			case IToken.tEQUAL:
			case IToken.tNOTEQUAL:
				lastOperator = new BinaryOperator(lastOperator, lastExpression, type, 80, 81);
				break;
			case IToken.tGT:
			case IToken.tLT:
			case IToken.tLTEQUAL:
			case IToken.tGTEQUAL:
				lastOperator = new BinaryOperator(lastOperator, lastExpression, type, 90, 91);
				break;
			case IToken.tSHIFTL:
			case IToken.tSHIFTR:
				lastOperator = new BinaryOperator(lastOperator, lastExpression, type, 100, 101);
				break;
			case IToken.tPLUS:
			case IToken.tMINUS:
				lastOperator = new BinaryOperator(lastOperator, lastExpression, type, 110, 111);
				break;
			case IToken.tSTAR:
			case IToken.tDIV:
			case IToken.tMOD:
				lastOperator = new BinaryOperator(lastOperator, lastExpression, type, 120, 121);
				break;
			default:
				break loop;
			}

			consume(); // consume operator
			lastExpression = castExpression(CastExprCtx.DIRECTLY_IN_BINARY_EXPR, null); // next cast expression
		}

		// Check for incomplete conditional expression
		if (type != IToken.tEOC && conditionCount > 0)
			throwBacktrack(lookahead(1));

		return buildExpression(lastOperator, lastExpression);
	}

	private List<? extends ICASTDesignator> designatorList() throws EndOfFileException, BacktrackException {
		final int type = lookaheadType(1);
		if (type == IToken.tDOT || type == IToken.tLBRACKET) {
			List<ICASTDesignator> designatorList = null;
			while (true) {
				switch (lookaheadType(1)) {
				case IToken.tDOT:
					int offset = consume().getOffset();
					IASTName name = identifier();
					ICASTFieldDesignator fieldDesignator = ((ICNodeFactory) nodeFactory).newFieldDesignator(name);
					setRange(fieldDesignator, offset, calculateEndOffset(name));
					if (designatorList == null)
						designatorList = new ArrayList<>(4);
					designatorList.add(fieldDesignator);
					break;

				case IToken.tLBRACKET:
					offset = consume().getOffset();
					IASTExpression constantExpression = expression();
					int lastOffset = consume(IToken.tRBRACKET).getEndOffset();
					ICASTArrayDesignator designator = ((ICNodeFactory) nodeFactory)
							.newArrayDesignator(constantExpression);
					setRange(designator, offset, lastOffset);
					if (designatorList == null)
						designatorList = new ArrayList<>(4);
					designatorList.add(designator);
					break;

				default:
					return designatorList;
				}
			}
		}

		return null;
	}

	private final IASTParameterDeclaration parameterDeclaration(DeclarationOptions option)
			throws BacktrackException, EndOfFileException {
		final IToken current = lookahead(1);
		int startingOffset = current.getOffset();

		IASTDeclSpecifier declSpec = null;
		IASTDeclarator declarator = null;
		IASTDeclSpecifier altDeclSpec = null;
		IASTDeclarator altDeclarator = null;

		try {
			Declaration decl = initDeclSpecifierSequenceDeclarator(option, false);
			declSpec = decl.leftSpecifier;
			declarator = decl.leftDeclarator;
			altDeclSpec = decl.rightSpecifier;
			altDeclarator = decl.rightDeclarator;
		} catch (FoundAggregateInitializer lie) {
			declSpec = lie.specifier;
			declarator = lie.declarator;
		}

		final int length = figureEndOffset(declSpec, declarator) - startingOffset;
		IASTParameterDeclaration result = nodeFactory.newParameterDeclaration(declSpec, declarator);
		((ASTNode) result).setOffsetAndLength(startingOffset, length);
		if (altDeclarator != null && altDeclSpec != null) {
			IASTParameterDeclaration alt = nodeFactory.newParameterDeclaration(altDeclSpec, altDeclarator);
			((ASTNode) alt).setOffsetAndLength(startingOffset, length);
			// order is important, prefer variant with specifier over the one without
			result = new CASTAmbiguousParameterDeclaration(result, alt);
			((ASTNode) result).setOffsetAndLength((ASTNode) alt);
		}
		return result;
	}

	private ICASTSimpleDeclSpecifier buildSimpleDeclSpec(int storageClass, int simpleType, int options, int isLong,
			IASTExpression typeofExpression, int offset, int endOffset) {
		ICASTSimpleDeclSpecifier declSpec = (ICASTSimpleDeclSpecifier) nodeFactory.newSimpleDeclSpecifier();

		configureDeclSpec(declSpec, storageClass, options);
		declSpec.setType(simpleType);
		declSpec.setLong(isLong == 1);
		declSpec.setLongLong(isLong > 1);
		declSpec.setRestrict((options & RESTRICT) != 0);
		declSpec.setUnsigned((options & UNSIGNED) != 0);
		declSpec.setSigned((options & SIGNED) != 0);
		declSpec.setShort((options & SHORT) != 0);
		declSpec.setComplex((options & COMPLEX) != 0);
		declSpec.setImaginary((options & IMAGINARY) != 0);
		if (typeofExpression != null) {
			declSpec.setDeclTypeExpression(typeofExpression);
			typeofExpression.setParent(declSpec);
		}

		((ASTNode) declSpec).setOffsetAndLength(offset, endOffset - offset);
		return declSpec;
	}

	private void configureDeclSpec(IASTDeclSpecifier specifier, int storageClass, int options) {
		specifier.setStorageClass(storageClass);
		specifier.setConst((options & CONST) != 0);
		specifier.setVolatile((options & VOLATILE) != 0);
		specifier.setInline((options & INLINE) != 0);
	}

	private ICASTTypedefNameSpecifier buildNamedTypeSpecifier(IASTName name, int storageClass, int options, int offset,
			int endOffset) {
		ICASTTypedefNameSpecifier declSpec = (ICASTTypedefNameSpecifier) nodeFactory.newTypedefNameSpecifier(name);
		configureDeclSpec(declSpec, storageClass, options);
		declSpec.setRestrict((options & RESTRICT) != 0);
		((ASTNode) declSpec).setOffsetAndLength(offset, endOffset - offset);
		return declSpec;
	}

	private IASTExpression postfixExpression(CastExprCtx ctx, ITemplateIdStrategy strat)
			throws EndOfFileException, BacktrackException {
		IASTExpression firstExpression = null;
		switch (lookaheadType(1)) {
		case IToken.tLPAREN:
			// (type-name) { initializer-list }
			// (type-name) { initializer-list , }
			IToken mark = mark();
			try {
				int offset = consume().getOffset();
				IASTTypeId typeID = typeID(DeclarationOptions.TYPEID);
				consume(IToken.tRPAREN);
				if (lookaheadType(1) == IToken.tLBRACE) {
					IASTInitializer i = (IASTInitializerList) initClause();
					firstExpression = nodeFactory.newTypeIdInitializerExpression(typeID, i);
					setRange(firstExpression, offset, calculateEndOffset(i));
					break;
				}
			} catch (BacktrackException exception) {
			}
			backup(mark);
			firstExpression = primaryExpression(ctx, strat);
			break;

		default:
			firstExpression = primaryExpression(ctx, strat);
			break;
		}

		IASTExpression secondExpression = null;
		for (;;) {
			switch (lookaheadType(1)) {
			case IToken.tLBRACKET:
				// array access
				consume();
				secondExpression = expression();
				int last;
				switch (lookaheadType(1)) {
				case IToken.tRBRACKET:
					last = consume().getEndOffset();
					break;
				case IToken.tEOC:
					last = Integer.MAX_VALUE;
					break;
				default:
					throw backtrack;
				}

				IASTArraySubscriptExpression s = nodeFactory.newArraySubscriptExpression(firstExpression,
						secondExpression);
				((ASTNode) s).setOffsetAndLength(((ASTNode) firstExpression).getOffset(),
						last - ((ASTNode) firstExpression).getOffset());
				firstExpression = s;
				break;
			case IToken.tLPAREN:
				// method call
				int endOffset;
				List<IASTExpression> argList = null;
				consume(IToken.tLPAREN);
				boolean isFirst = true;
				while (true) {
					final int type = lookaheadType(1);
					if (type == IToken.tRPAREN) {
						endOffset = consume().getEndOffset();
						break;
					} else if (type == IToken.tEOC) {
						endOffset = lookahead(1).getEndOffset();
						break;
					}
					if (isFirst) {
						isFirst = false;
					} else {
						consume(IToken.tCOMMA);
					}

					IASTExpression expr = expression(ExprKind.ASSIGNMENT);
					if (argList == null) {
						argList = new ArrayList<>();
					}
					argList.add(expr);
				}

				final IASTExpression[] args;
				if (argList == null) {
					args = IASTExpression.EMPTY_EXPRESSION_ARRAY;
				} else {
					args = argList.toArray(new IASTExpression[argList.size()]);
				}
				IASTFunctionCallExpression f = nodeFactory.newFunctionCallExpression(firstExpression, args);
				firstExpression = setRange(f, firstExpression, endOffset);
				break;
			case IToken.tINCR:
				int offset = consume().getEndOffset();
				firstExpression = buildUnaryExpression(IASTUnaryExpression.op_postFixIncr, firstExpression,
						((ASTNode) firstExpression).getOffset(), offset);
				break;
			case IToken.tDECR:
				offset = consume().getEndOffset();
				firstExpression = buildUnaryExpression(IASTUnaryExpression.op_postFixDecr, firstExpression,
						((ASTNode) firstExpression).getOffset(), offset);
				break;
			case IToken.tDOT:
				// member access
				IToken dot = consume();
				IASTName name = identifier();
				if (name == null)
					throwBacktrack(((ASTNode) firstExpression).getOffset(),
							((ASTNode) firstExpression).getLength() + dot.getLength());
				IASTFieldReference result = nodeFactory.newFieldReference(name, firstExpression);
				result.setIsPointerDereference(false);
				((ASTNode) result).setOffsetAndLength(((ASTNode) firstExpression).getOffset(),
						calculateEndOffset(name) - ((ASTNode) firstExpression).getOffset());
				firstExpression = result;
				break;
			case IToken.tARROW:
				// member access
				IToken arrow = consume();
				name = identifier();
				if (name == null)
					throwBacktrack(((ASTNode) firstExpression).getOffset(),
							((ASTNode) firstExpression).getLength() + arrow.getLength());
				result = nodeFactory.newFieldReference(name, firstExpression);
				result.setIsPointerDereference(true);
				((ASTNode) result).setOffsetAndLength(((ASTNode) firstExpression).getOffset(),
						calculateEndOffset(name) - ((ASTNode) firstExpression).getOffset());
				firstExpression = result;
				break;
			default:
				return firstExpression;
			}
		}
	}

}

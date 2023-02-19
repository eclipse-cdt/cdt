package org.eclipse.cdt.internal.core.dom.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTAlignmentSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTAttributeOwner;
import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationListOwner;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.AbstractParserLogService;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IInactiveCodeToken;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;

/**
 * Abstract class for the regular C and C++ Parser.
 */
public abstract class AbstractSourceCodeParser implements ISourceCodeParser {

	protected final AbstractParserLogService log;
	protected final IScanner scanner;
	protected final ParserMode mode;
	protected final INodeFactory nodeFactory;
	protected final IBuiltinBindingsProvider builtinBindingsProvider;

	protected boolean passing = true;
	protected boolean cancelled = false;
	protected IToken declarationMark;
	protected IToken nextToken;
	protected IToken lastTokenFromScanner;
	protected ASTCompletionNode completionNode;
	protected int backtrackCount = 0;
	protected BacktrackException backtrack = new BacktrackException();

	private boolean activeCode = true;

	public AbstractSourceCodeParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			INodeFactory factory, IBuiltinBindingsProvider provider) {
		this.scanner = scanner;
		this.nodeFactory = factory;
		this.log = wrapLogService(logService);
		this.mode = parserMode;
		this.builtinBindingsProvider = provider;
	}

	@Override
	public IASTTranslationUnit parse() {
		long start = log.isTracing() ? System.currentTimeMillis() : 0;
		processCompilationUnit();
		long end = log.isTracing() ? System.currentTimeMillis() : 0;
		resolveAmbiguities();
		IASTTranslationUnit ast = getCompilationUnit();
		if (log.isTracing()) {
			ITranslationUnit unit = ast.getOriginatingTranslationUnit();
			String name = unit == null ? "<unknown>" : unit.getElementName(); //$NON-NLS-1$
			String message = String.format("Parsed %s: %d ms%s. Ambiguity resolution: %d ms", //$NON-NLS-1$
					name, end - start, passing ? "" : " - parse failure", System.currentTimeMillis() - end); //$NON-NLS-1$//$NON-NLS-2$
			log.traceLog(message);
		}
		destroyCompilationUnit();
		ast.freeze(); // Make the AST immutable.
		return ast;
	}

	@Override
	public void cancel() {
		cancelled = true;
	}

	@Override
	public boolean encounteredError() {
		return !passing;
	}

	@Override
	public IASTCompletionNode getCompletionNode() {
		return completionNode;
	}

	public final IASTExpression buildExpression(BinaryOperator leftChain, IASTInitializerClause expr) {
		BinaryOperator rightChain = null;
		for (;;) {
			if (leftChain == null) {
				if (rightChain == null)
					return (IASTExpression) expr;

				expr = buildExpression((IASTExpression) expr, rightChain);
				rightChain = rightChain.fNext;
			} else if (rightChain != null && leftChain.fRightPrecedence < rightChain.fLeftPrecedence) {
				expr = buildExpression((IASTExpression) expr, rightChain);
				rightChain = rightChain.fNext;
			} else {
				BinaryOperator op = leftChain;
				leftChain = leftChain.fNext;
				expr = op.exchange(expr);
				op.fNext = rightChain;
				rightChain = op;
			}
		}
	}

	protected abstract IASTTranslationUnit getCompilationUnit();

	protected abstract void createCompilationUnit() throws Exception;

	protected abstract void destroyCompilationUnit();

	protected abstract IASTDeclaration declaration(DeclarationOptions option)
			throws BacktrackException, EndOfFileException;

	protected abstract IASTDeclarator initDeclarator(IASTDeclSpecifier specifier, DeclarationOptions option)
			throws EndOfFileException, BacktrackException, FoundAggregateInitializer;

	protected abstract IASTInitializer optionalInitializer(IASTDeclarator dtor, DeclarationOptions options)
			throws EndOfFileException, BacktrackException;

	protected abstract IASTExpression expression() throws BacktrackException, EndOfFileException;

	protected abstract IASTExpression constantExpression() throws BacktrackException, EndOfFileException;

	protected abstract IASTExpression unaryExpression(CastExprCtx ctx, ITemplateIdStrategy strat)
			throws BacktrackException, EndOfFileException;

	protected abstract IASTExpression primaryExpression(CastExprCtx ctx, ITemplateIdStrategy strat)
			throws BacktrackException, EndOfFileException;

	protected abstract IASTStatement statement() throws EndOfFileException, BacktrackException;

	protected abstract IASTExpression buildBinaryExpression(int operator, IASTExpression expr,
			IASTInitializerClause clause, int lastOffset);

	protected abstract IASTAmbiguousExpression createAmbiguousExpression();

	protected abstract IASTAmbiguousStatement createAmbiguousStatement();

	protected abstract IASTAmbiguousExpression createAmbiguousBinaryVsCastExpression(IASTBinaryExpression binary,
			IASTCastExpression castExpr);

	protected abstract IASTAmbiguousExpression createAmbiguousCastVsMethodCallExpression(IASTCastExpression castExpr,
			IASTFunctionCallExpression funcCall);

	protected abstract IASTTypeId typeID(DeclarationOptions option) throws EndOfFileException, BacktrackException;

	protected abstract IASTStatement parseDeclarationOrExpressionStatement(
			List<IASTAttributeSpecifier> attributeSpecifiers) throws EndOfFileException, BacktrackException;

	// Methods for parsing a type-id and an expression with an optional trailing ellipsis.
	// The optional trailing ellipsis can only appear in C++ code, and only the C++ parser
	// allows it, but being able to invoke this from here allows reusing more productions
	// between C and C++, such as alignmentSpecifier().
	protected abstract IASTExpression expressionWithOptionalTrailingEllipsis()
			throws BacktrackException, EndOfFileException;

	protected abstract IASTTypeId typeIDWithOptionalTrailingEllipsis(DeclarationOptions option)
			throws EndOfFileException, BacktrackException;

	/**
	 * Parses for two alternatives of a declspec sequence. If there is a second alternative the token after the second alternative
	 * is returned, such that the parser can continue after both variants.
	 */
	protected abstract Decl declSpecifierSeq(DeclarationOptions option, ITemplateIdStrategy strat)
			throws BacktrackException, EndOfFileException;

	protected Decl declSpecifierSeq(DeclarationOptions option) throws BacktrackException, EndOfFileException {
		return declSpecifierSeq(option, null);
	}

	/**
	 * Parses an identifier.
	 *
	 * @throws BacktrackException request a backtrack
	 */
	protected abstract IASTName identifier() throws EndOfFileException, BacktrackException;

	protected abstract IASTAlignmentSpecifier createAmbiguousAlignmentSpecifier(IASTAlignmentSpecifier expression,
			IASTAlignmentSpecifier typeId);

	protected boolean canBeTypeSpecifier() throws EndOfFileException {
		final int type = lookaheadType(1);
		switch (type) {
		// simple type specifiers:
		case IToken.tIDENTIFIER:
		case IToken.tCOLONCOLON:
		case IToken.t_void:
		case IToken.t_char:
		case IToken.t_char16_t:
		case IToken.t_char32_t:
		case IToken.t_wchar_t:
		case IToken.t_bool:
		case IToken.t_short:
		case IToken.t_int:
		case IToken.t_long:
		case IToken.t_float:
		case IToken.t_double:
		case IToken.t__Bool:
		case IToken.t__Complex:
		case IToken.t__Imaginary:
		case IToken.t_signed:
		case IToken.t_unsigned:
		case IToken.t_decltype:
		case IToken.t_auto:

			// class-specifier:
		case IToken.t_class:
		case IToken.t_struct:
		case IToken.t_union:

			// enum-specifier:
		case IToken.t_enum:

			// elaborated type specifier: (together with class, struct, union, enum)
		case IToken.t_typename:

			// cq-qualifiers
		case IToken.t_const:
		case IToken.t_volatile:
		case IToken.t_restrict:

			// content assist
		case IToken.tCOMPLETION:
			return true;

		default:
			return false;
		}
	}

	protected void processCompilationUnit() {
		try {
			createCompilationUnit();
		} catch (Exception exception) {
			logException("translationUnit::createCompilationUnit()", exception); //$NON-NLS-1$
			return;
		}
		parseCompilationUnit();
	}

	protected void parseCompilationUnit() {
		declarationList(getCompilationUnit(), DeclarationOptions.GLOBAL, false, 0);
	}

	protected void resolveAmbiguities() {
		final IASTTranslationUnit compilationUnit = getCompilationUnit();
		if (compilationUnit instanceof ASTTranslationUnit) {
			((ASTTranslationUnit) compilationUnit).resolveAmbiguities();
		}
	}

	protected AbstractParserLogService wrapLogService(IParserLogService logService) {
		if (logService instanceof AbstractParserLogService) {
			return (AbstractParserLogService) logService;
		}
		return new ParserLogServiceWrapper(logService);
	}

	protected void logException(String methodName, Exception exception) {
		if (!(exception instanceof EndOfFileException) && exception != null) {
			if (log.isTracing()) {
				String message = String.format("Parser: Unexpected exception in %s:%s::%s. w/%s", //$NON-NLS-1$
						methodName, exception.getClass().getName(), exception.getMessage(), scanner);
				log.traceLog(message);
			}
			log.traceException(exception);
		}
	}

	protected ASTCompletionNode createCompletionNode(IToken token) {
		// the preprocessor may deliver tokens for literals or header-names.
		if (completionNode == null && token != null && token.getType() == IToken.tCOMPLETION) {
			completionNode = new ASTCompletionNode(token, getCompilationUnit());
		}
		return completionNode;
	}

	protected IASTAlignmentSpecifier alignmentSpecifier() throws BacktrackException, EndOfFileException {
		int startOffset = consume(IToken.t_alignas, IToken.t__Alignas).getOffset();

		consume(IToken.tLPAREN);

		IASTTypeId typeId = null;
		IASTExpression expression = null;

		// Try parsing a type-id.
		IToken beginning = mark();
		IToken typeIdEnd = null;
		try {
			typeId = typeIDWithOptionalTrailingEllipsis(DeclarationOptions.TYPEID);
			typeIdEnd = mark();
		} catch (BacktrackException exception) {
		}

		// Back up and try parsing an expression.
		backup(beginning);
		try {
			expression = expressionWithOptionalTrailingEllipsis();
		} catch (BacktrackException exception) {
			// If neither parses successfully, throw.
			if (typeId == null) {
				throw exception;
			}
		}

		IASTAlignmentSpecifier result;
		if (typeId == null) {
			// No type id - use the expression.
			result = nodeFactory.newAlignmentSpecifier(expression);
		} else if (expression == null) {
			// No expression - use the type id.
			backup(typeIdEnd);
			result = nodeFactory.newAlignmentSpecifier(typeId);
		} else if (expression.contains(typeId)) { // otherwise, pick the longer one
			if (typeId.contains(expression)) {
				// They are both the same length - ambiguous.
				int endOffset = consume(IToken.tRPAREN).getEndOffset();
				IASTAlignmentSpecifier expressionAlternative = nodeFactory.newAlignmentSpecifier(expression);
				IASTAlignmentSpecifier typeIdAlternative = nodeFactory.newAlignmentSpecifier(typeId);
				setRange(expressionAlternative, startOffset, endOffset);
				setRange(typeIdAlternative, startOffset, endOffset);
				return createAmbiguousAlignmentSpecifier(expressionAlternative, typeIdAlternative);
			} else {
				// Expression is longer - use it.
				result = nodeFactory.newAlignmentSpecifier(expression);
			}
		} else {
			// Type-id is longer - use it.
			backup(typeIdEnd);
			result = nodeFactory.newAlignmentSpecifier(typeId);
		}

		int endOffset = consume(IToken.tRPAREN).getEndOffset();

		setRange(result, startOffset, endOffset);
		return result;
	}

	/**
	 * Parse an enumeration specifier, as according to the ANSI specs in C &
	 * C++. enumSpecifier: "enum" (name)? "{" (enumerator-list) "}"
	 * enumerator-list: enumerator-definition enumerator-list ,
	 * enumerator-definition enumerator-definition: enumerator enumerator =
	 * constant-expression enumerator: identifier
	 *
	 * @throws BacktrackException request a backtrack
	 */
	protected IASTEnumerationSpecifier enumSpecifier() throws BacktrackException, EndOfFileException {
		final IToken mark = mark();
		final int offset = consume().getOffset();

		IASTName name;
		if (lookaheadType(1) == IToken.tIDENTIFIER) {
			name = identifier();
		} else {
			name = nodeFactory.newName();
		}

		if (lookaheadType(1) != IToken.tLBRACE) {
			backup(mark);
			throwBacktrack(mark);
		}

		final IASTEnumerationSpecifier result = nodeFactory.newEnumerationSpecifier(name);

		int endOffset = enumBody(result);
		return setRange(result, offset, endOffset);
	}

	protected int enumBody(final IASTEnumerationSpecifier result) throws EndOfFileException, BacktrackException {
		boolean needComma = false;
		int endOffset = consume(IToken.tLBRACE).getEndOffset(); // IToken.tLBRACE
		int problemOffset = endOffset;
		try {
			loop: while (true) {
				switch (lookaheadTypeWithEndOfFile(1)) {
				case 0: // eof
					endOffset = getEndOffset();
					break loop;
				case IToken.tRBRACE:
					endOffset = consume().getEndOffset();
					break loop;
				case IToken.tEOC:
					break loop;
				case IToken.tCOMMA:
					if (!needComma) {
						problemOffset = lookahead(1).getOffset();
						throw backtrack;
					}
					endOffset = consume().getEndOffset();
					needComma = false;
					continue loop;
				case IToken.tIDENTIFIER:
				case IToken.tCOMPLETION:
					problemOffset = lookahead(1).getOffset();
					if (needComma)
						throw backtrack;

					final IASTName etorName = identifier();
					final IASTEnumerator enumerator = nodeFactory.newEnumerator(etorName, null);

					// WIP C23
					List<IASTAttributeSpecifier> attributes = new ArrayList<>();
					addAttributeSpecifiers(attributes, enumerator);
					endOffset = attributesEndOffset(calculateEndOffset(etorName), attributes);
					setRange(enumerator, problemOffset, endOffset);

					result.addEnumerator(enumerator);
					if (lookaheadTypeWithEndOfFile(1) == IToken.tASSIGN) {
						problemOffset = consume().getOffset();
						final IASTExpression value = constantExpression();
						enumerator.setValue(value);
						adjustLength(enumerator, value);
						endOffset = calculateEndOffset(value);
					}
					needComma = true;
					continue loop;
				default:
					problemOffset = lookahead(1).getOffset();
					throw backtrack;
				}
			}
		} catch (EndOfFileException eof) {
			throwBacktrack(createProblem(IProblem.SYNTAX_ERROR, problemOffset, getEndOffset() - problemOffset), result);
		} catch (BacktrackException bt) {
			IASTProblem problem = skipProblemEnumerator(problemOffset);
			throwBacktrack(problem, result);
		}
		return endOffset;
	}

	protected IASTProblemDeclaration skipProblemDeclaration(int offset) {
		return skipProblemDeclaration(offset, null);
	}

	protected IASTProblemDeclaration skipProblemDeclaration(int offset, IASTProblem origProblem) {
		passing = false;
		declarationMark = null;
		int endOffset = skipToSemiOrClosingBrace(offset, false);
		IASTProblem problem = createProblem(IProblem.SYNTAX_ERROR, offset, endOffset - offset);
		if (origProblem != null && origProblem.getID() != IProblem.SYNTAX_ERROR) {
			problem.setOriginalProblem(origProblem);
		}
		return buildProblemDeclaration(problem);
	}

	protected IASTProblem createProblem(BacktrackException exception) {
		IASTProblem result = exception.getProblem();
		if (result == null) {
			result = createProblem(IProblem.SYNTAX_ERROR, exception.getOffset(), exception.getLength());
		}
		return result;
	}

	protected IASTDeclaration[] problemDeclaration(int offset, BacktrackException backtrack,
			DeclarationOptions option) {
		passing = false;
		IASTProblem origProblem = createProblem(backtrack);

		// a node was detected by assuming additional tokens (e.g. missing semicolon)
		IASTNode n = backtrack.getNodeBeforeProblem();
		if (n instanceof IASTDeclaration) {
			IToken lookahead = lookaheadWithEndOfFile(1);
			if (lookahead == null || lookahead.getOffset() > offset) {
				declarationMark = null;
				return new IASTDeclaration[] { (IASTDeclaration) n, buildProblemDeclaration(origProblem) };
			}
		}

		if (declarationMark != null && activeCode) {
			IASTDeclaration trailingProblem = null;
			offset = declarationMark.getOffset();

			// try to skip identifiers (undefined macros?)
			IASTDeclaration decl = null;
			int endOffset = 0;
			loop: while (declarationMark != null && declarationMark.getType() == IToken.tIDENTIFIER) {
				endOffset = declarationMark.getEndOffset();
				declarationMark = declarationMark.getNext();
				if (declarationMark != null) {
					backup(declarationMark);
					// avoid creating an empty declaration
					switch (lookaheadTypeWithEndOfFile(1)) {
					case 0: // eof
					case IToken.tEOC:
					case IToken.tSEMI:
						break loop;
					}
					try {
						decl = declaration(option);
						break;
					} catch (BacktrackException exception) {
						n = exception.getNodeBeforeProblem();
						if (n instanceof IASTDeclaration) {
							decl = (IASTDeclaration) n;
							trailingProblem = buildProblemDeclaration(exception.getProblem());
							break;
						}
					} catch (EndOfFileException exception) {
						endOffset = getEndOffset();
						break;
					}
				}
			}
			declarationMark = null;

			if (decl != null) {
				IASTProblem problem = createProblem(IProblem.SYNTAX_ERROR, offset, endOffset - offset);
				IASTDeclaration problemDecl = buildProblemDeclaration(problem);
				if (trailingProblem != null)
					return new IASTDeclaration[] { problemDecl, decl, trailingProblem };
				return new IASTDeclaration[] { problemDecl, decl };
			}
		}

		return new IASTDeclaration[] { skipProblemDeclaration(offset, origProblem) };
	}

	/**
	 * @param result
	 */
	protected void reconcileLengths(IASTIfStatement result) {
		if (result == null) {
			return;
		}
		IASTIfStatement current = result;
		while (current.getElseClause() instanceof IASTIfStatement) {
			current = (IASTIfStatement) current.getElseClause();
		}

		while (current != null) {
			ASTNode node = ((ASTNode) current);
			if (current.getElseClause() != null) {
				ASTNode elseClause = ((ASTNode) current.getElseClause());
				node.setLength(elseClause.getOffset() + elseClause.getLength() - node.getOffset());
			} else {
				ASTNode thenClause = (ASTNode) current.getThenClause();
				if (thenClause != null) {
					node.setLength(thenClause.getOffset() + thenClause.getLength() - node.getOffset());
				}
			}
			if (current.getParent() != null && current.getParent() instanceof IASTIfStatement) {
				current = (IASTIfStatement) current.getParent();
			} else {
				current = null;
			}
		}
	}

	protected IToken asmExpression(StringBuilder content) throws EndOfFileException, BacktrackException {
		IToken token = consume(IToken.tLPAREN);
		boolean needspace = false;
		int open = 1;
		while (open > 0) {
			token = consume();
			switch (token.getType()) {
			case IToken.tLPAREN:
				open++;
				break;
			case IToken.tRPAREN:
				open--;
				break;
			case IToken.tEOC:
				throw new EndOfFileException(token.getOffset());
			}
			if (open > 0 && content != null) {
				if (needspace) {
					content.append(' ');
				}
				content.append(token.getCharImage());
				needspace = true;
			}
		}
		return token;
	}

	protected IASTASMDeclaration buildASMDirective(int offset, String assembly, int lastOffset) {
		IASTASMDeclaration result = nodeFactory.newASMDeclaration(assembly);
		((ASTNode) result).setOffsetAndLength(offset, lastOffset - offset);
		return result;
	}

	protected IASTCastExpression buildCastExpression(int operator, IASTTypeId typeId, IASTExpression operand,
			int offset, int endOffset) {
		IASTCastExpression result = nodeFactory.newCastExpression(operator, typeId, operand);
		((ASTNode) result).setOffsetAndLength(offset, endOffset - offset);
		return result;
	}

	protected IASTDeclaration asmDeclaration() throws EndOfFileException, BacktrackException {
		final int offset = consume().getOffset(); // t_asm
		if (lookaheadType(1) == IToken.t_volatile) {
			consume();
		}

		// No support for assembly in the style of a method

		StringBuilder buffer = new StringBuilder();
		asmExpression(buffer);
		int lastOffset = consume(IToken.tSEMI).getEndOffset();

		return buildASMDirective(offset, buffer.toString(), lastOffset);
	}

	protected IASTStatement handleMethodBody() throws BacktrackException, EndOfFileException {
		declarationMark = null;
		if (mode == ParserMode.QUICK_PARSE || mode == ParserMode.STRUCTURAL_PARSE || !activeCode) {
			int offset = lookahead(1).getOffset();
			IToken last = skipOverCompoundStatement(true);
			IASTCompoundStatement compoundStatement = nodeFactory.newCompoundStatement();
			setRange(compoundStatement, offset, last.getEndOffset());
			return compoundStatement;
		} else if (mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE) {
			if (scanner.isOnTopContext())
				return methodBody();
			int offset = lookahead(1).getOffset();
			IToken last = skipOverCompoundStatement(true);
			IASTCompoundStatement compoundStatement = nodeFactory.newCompoundStatement();
			setRange(compoundStatement, offset, last.getEndOffset());
			return compoundStatement;
		}

		// full parse
		return methodBody();
	}

	/**
	 * Parses a method body.
	 *
	 * @return the compound statement representing the function body.
	 * @throws BacktrackException
	 *             request a backtrack
	 */
	protected IASTCompoundStatement methodBody() throws EndOfFileException, BacktrackException {
		return compoundStatement();
	}

	protected IASTCompoundStatement compoundStatement() throws EndOfFileException, BacktrackException {
		IASTCompoundStatement result = nodeFactory.newCompoundStatement();
		if (lookaheadType(1) == IToken.tEOC)
			return result;

		final int offset = lookahead(1).getOffset();
		int endOffset = consume(IToken.tLBRACE).getOffset();

		int stmtOffset = -1;
		while (true) {
			IToken next = lookaheadWithEndOfFile(1);
			if (next == null) {
				((ASTNode) result).setOffsetAndLength(offset, endOffset - offset);
				throwBacktrack(createProblem(IProblem.SYNTAX_ERROR, endOffset, 0), result);
				return null; // To make Java compiler happy.
			}
			try {
				if (next.getType() == IToken.tEOC)
					break;

				if (next.getType() == IToken.tRBRACE) {
					endOffset = consume().getEndOffset();
					break;
				}

				final int nextOffset = next.getOffset();
				declarationMark = next;
				next = null; // Don't hold on to the token while parsing namespaces, class bodies, etc.

				IASTStatement stmt;
				if (stmtOffset == nextOffset) {
					// no progress
					stmt = skipProblemStatement(stmtOffset);
				} else {
					stmtOffset = nextOffset;
					stmt = statement();
				}
				result.addStatement(stmt);
				endOffset = calculateEndOffset(stmt);
			} catch (BacktrackException exception) {
				final IASTNode beforeProblem = exception.getNodeBeforeProblem();
				final IASTProblem problem = exception.getProblem();
				if (problem != null && beforeProblem instanceof IASTStatement) {
					result.addStatement((IASTStatement) beforeProblem);
					result.addStatement(buildProblemStatement(problem));
					endOffset = calculateEndOffset(beforeProblem);
				} else {
					IASTStatement stmt = skipProblemStatement(stmtOffset);
					result.addStatement(stmt);
					endOffset = calculateEndOffset(stmt);
				}
			} catch (EndOfFileException e) {
				IASTStatement stmt = skipProblemStatement(stmtOffset);
				result.addStatement(stmt);
				endOffset = calculateEndOffset(stmt);
				break;
			} finally {
				declarationMark = null;
			}
		}
		((ASTNode) result).setOffsetAndLength(offset, endOffset - offset);
		return result;
	}

	protected IToken skipOverCompoundStatement(boolean hasSkippedNodes) throws BacktrackException, EndOfFileException {
		// speed up the parser by skipping the body, simply look for matching brace and return
		if (hasSkippedNodes)
			getCompilationUnit().setHasNodesOmitted(true);
		final boolean isActive = activeCode;
		final int codeBranchNesting = getCodeBranchNesting();

		consume(IToken.tLBRACE);
		IToken result = null;
		int depth = 1;
		while (depth > 0) {
			if (!isActive) {
				IToken token = lookaheadToken(1, false);
				final int type = token.getType();
				if (type == IToken.tINACTIVE_CODE_SEPARATOR || type == IToken.tINACTIVE_CODE_END
						|| type == IToken.tINACTIVE_CODE_START) {
					if (!acceptInactiveCodeBoundary(codeBranchNesting))
						throw new EndOfFileException(token.getOffset(), true);
				}
			}
			result = consume();
			switch (result.getType()) {
			case IToken.tRBRACE:
				--depth;
				break;
			case IToken.tLBRACE:
				++depth;
				break;
			case IToken.tEOC:
				throw new EndOfFileException(result.getOffset());
			}
		}
		return result;
	}

	protected IASTProblemExpression skipProblemConditionInParenthesis(int offset) {
		passing = false;
		int compExpr = 0;
		int depth = 0;
		int endOffset = offset;
		loop: try {
			while (true) {
				switch (lookaheadType(1)) {
				case IToken.tEOC:
					endOffset = getEndOffset();
					break loop;
				case IToken.tSEMI:
				case IToken.tLBRACE:
					if (compExpr == 0) {
						break loop;
					}
					break;
				case IToken.tLPAREN:
					depth++;
					if (lookaheadTypeWithEndOfFile(2) == IToken.tLBRACE) {
						if (compExpr == 0) {
							compExpr = depth;
						}
						consume();
					}
					break;
				case IToken.tRPAREN:
					if (--depth < 0) {
						break loop;
					}
					if (depth < compExpr) {
						compExpr = 0;
					}
					break;
				}
				endOffset = consume().getEndOffset();
			}
		} catch (EndOfFileException exception) {
			endOffset = getEndOffset();
		}
		IASTProblem problem = createProblem(IProblem.SYNTAX_ERROR, offset, endOffset - offset);
		return buildProblemExpression(problem);
	}

	protected IASTExpression condition(boolean followedByParenthesis) throws BacktrackException, EndOfFileException {
		IToken mark = mark();
		try {
			IASTExpression expr = expression();
			if (!followedByParenthesis)
				return expr;

			switch (lookaheadType(1)) {
			case IToken.tEOC:
			case IToken.tRPAREN:
				return expr;
			}
		} catch (BacktrackException exception) {
			if (!followedByParenthesis) {
				throw exception;
			}
		}
		backup(mark);
		return skipProblemConditionInParenthesis(mark.getOffset());
	}

	protected IASTProblemStatement skipProblemStatement(int offset) {
		passing = false;
		declarationMark = null;
		int endOffset = skipToSemiOrClosingBrace(offset, false);
		IASTProblem problem = createProblem(IProblem.SYNTAX_ERROR, offset, endOffset - offset);
		return buildProblemStatement(problem);
	}

	protected IASTStatement initStatement() throws BacktrackException, EndOfFileException {
		if (lookaheadType(1) == IToken.tSEMI)
			return parseNullStatement();
		try {
			return parseDeclarationOrExpressionStatement();
		} catch (BacktrackException exception) {
			// A init statement always terminates with a semicolon
			IASTNode before = exception.getNodeBeforeProblem();
			if (before != null) {
				exception.initialize(exception.getProblem());
			}
			throw exception;
		}
	}

	protected IASTDeclarator addInitializer(FoundAggregateInitializer initializer, DeclarationOptions options)
			throws EndOfFileException, BacktrackException {
		final IASTDeclarator declarator = initializer.fDeclarator;
		IASTInitializer i = optionalInitializer(declarator, options);
		if (i != null) {
			declarator.setInitializer(i);
			((ASTNode) declarator).setLength(calculateEndOffset(i) - ((ASTNode) declarator).getOffset());
		}
		return declarator;
	}

	/**
	 * There are many ambiguities in C and C++ between expressions and declarations.
	 * This method will attempt to parse a statement as both an expression and a declaration,
	 * if both parses succeed then an ambiguity node is returned.
	 */
	protected IASTStatement parseDeclarationOrExpressionStatement() throws EndOfFileException, BacktrackException {
		return parseDeclarationOrExpressionStatement(null);
	}

	protected IASTStatement parseNullStatement() throws EndOfFileException, BacktrackException {
		IToken t = consume(); // tSEMI

		IASTNullStatement nullStatement = nodeFactory.newNullStatement();
		((ASTNode) nullStatement).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
		return nullStatement;
	}

	protected IASTStatement parseLabelStatement() throws EndOfFileException, BacktrackException {
		int offset = lookahead(1).getOffset();
		IASTName name = identifier(); // tIDENTIFIER
		consume(IToken.tCOLON); // tCOLON
		IASTStatement nestedStatement = statement();
		int lastOffset = calculateEndOffset(nestedStatement);

		IASTLabelStatement labelStatement = nodeFactory.newLabelStatement(name, nestedStatement);
		setRange(labelStatement, offset, lastOffset);
		return labelStatement;
	}

	protected IASTStatement parseGotoStatement() throws EndOfFileException, BacktrackException {
		int startOffset = consume(IToken.t_goto).getOffset();
		IASTStatement gotoStatement = null;

		IASTName gotoLabelName = identifier();
		gotoStatement = nodeFactory.newGotoStatement(gotoLabelName);

		int lastOffset = consume(IToken.tSEMI).getEndOffset();
		((ASTNode) gotoStatement).setOffsetAndLength(startOffset, lastOffset - startOffset);
		return gotoStatement;
	}

	protected IASTStatement parseDoStatement() throws EndOfFileException, BacktrackException {
		int startOffset;
		startOffset = consume().getOffset(); // t_do
		IASTStatement doBody = statement();

		IASTExpression doCondition = null;
		if (lookaheadType(1) != IToken.tEOC) {
			consume(IToken.t_while);
			consume(IToken.tLPAREN);
			doCondition = condition(true);
		}

		int lastOffset;
		switch (lookaheadType(1)) {
		case IToken.tRPAREN:
		case IToken.tEOC:
			consume();
			break;
		default:
			throw backtrack;
		}

		switch (lookaheadType(1)) {
		case IToken.tSEMI:
		case IToken.tEOC:
			lastOffset = consume().getEndOffset();
			break;
		default:
			throw backtrack;
		}

		IASTDoStatement doStatement = nodeFactory.newDoStatement(doBody, doCondition);
		((ASTNode) doStatement).setOffsetAndLength(startOffset, lastOffset - startOffset);
		return doStatement;
	}

	protected IASTStatement parseBreakStatement() throws EndOfFileException, BacktrackException {
		int startOffset = consume().getOffset(); // t_break
		int lastOffset = consume(IToken.tSEMI).getEndOffset();

		IASTBreakStatement breakStatement = nodeFactory.newBreakStatement();
		((ASTNode) breakStatement).setOffsetAndLength(startOffset, lastOffset - startOffset);
		return breakStatement;
	}

	protected IASTStatement parseContinueStatement() throws EndOfFileException, BacktrackException {
		int startOffset = consume().getOffset(); // t_continue
		int lastOffset = consume(IToken.tSEMI).getEndOffset();

		IASTContinueStatement continueStatement = nodeFactory.newContinueStatement();
		((ASTNode) continueStatement).setOffsetAndLength(startOffset, lastOffset - startOffset);
		return continueStatement;
	}

	protected IASTStatement parseReturnStatement() throws EndOfFileException, BacktrackException {
		final int offset = consume(IToken.t_return).getOffset();

		// Optional expression
		IASTExpression expr = null;
		if (lookaheadType(1) != IToken.tSEMI) {
			expr = expression();
		}
		// Semicolon
		final int endOffset = consumeOrEndOfCompletion(IToken.tSEMI).getEndOffset();

		return setRange(nodeFactory.newReturnStatement(expr), offset, endOffset);
	}

	protected IASTStatement parseSwitchBody() throws EndOfFileException, BacktrackException {
		IASTStatement statement = null;
		if (lookaheadType(1) != IToken.tEOC) {
			statement = statement();
		}

		if (!(statement instanceof IASTCaseStatement) && !(statement instanceof IASTDefaultStatement)) {
			return statement;
		}

		// bug 105334, switch without compound statement
		IASTCompoundStatement compound = nodeFactory.newCompoundStatement();
		((ASTNode) compound).setOffsetAndLength((ASTNode) statement);
		compound.addStatement(statement);

		while (lookaheadType(1) != IToken.tEOC
				&& (statement instanceof IASTCaseStatement || statement instanceof IASTDefaultStatement)) {
			statement = statement();
			compound.addStatement(statement);
		}
		adjustLength(compound, statement);
		return compound;
	}

	protected IASTStatement parseWhileStatement() throws EndOfFileException, BacktrackException {
		int startOffset = consume().getOffset();
		consume(IToken.tLPAREN);
		IASTExpression condition = condition(true);
		switch (lookaheadType(1)) {
		case IToken.tRPAREN:
			consume();
			break;
		case IToken.tEOC:
			break;
		default:
			throwBacktrack(lookahead(1));
		}
		IASTStatement body = null;
		if (lookaheadType(1) != IToken.tEOC)
			body = statement();

		IASTWhileStatement statement = nodeFactory.newWhileStatement(condition, body);
		((ASTNode) statement).setOffsetAndLength(startOffset,
				(body != null ? calculateEndOffset(body) : lookahead(1).getEndOffset()) - startOffset);

		return statement;
	}

	protected IASTStatement parseCompoundStatement() throws EndOfFileException, BacktrackException {
		IASTCompoundStatement compound = compoundStatement();
		return compound;
	}

	protected IASTStatement parseDefaultStatement() throws EndOfFileException, BacktrackException {
		int startOffset = consume(IToken.t_default).getOffset();
		int lastOffset = consume(IToken.tCOLON).getEndOffset();

		IASTDefaultStatement defaultStatement = nodeFactory.newDefaultStatement();
		((ASTNode) defaultStatement).setOffsetAndLength(startOffset, lastOffset - startOffset);
		return defaultStatement;
	}

	protected IASTStatement parseCaseStatement() throws EndOfFileException, BacktrackException {
		int startOffset = consume().getOffset(); // t_case
		IASTExpression caseExpression = constantExpression();
		int type = lookaheadType(1);
		if (type == IToken.tELLIPSIS) {
			consume();
			IASTExpression upperBoundExpression = constantExpression();
			caseExpression = buildBinaryExpression(IASTBinaryExpression.op_ellipses, caseExpression,
					upperBoundExpression, calculateEndOffset(upperBoundExpression));
			type = lookaheadType(1);
		}
		int lastOffset = 0;
		switch (type) {
		case IToken.tCOLON:
		case IToken.tEOC:
			lastOffset = consume().getEndOffset();
			break;
		default:
			throwBacktrack(lookahead(1));
		}

		IASTCaseStatement caseStatement = nodeFactory.newCaseStatement(caseExpression);
		((ASTNode) caseStatement).setOffsetAndLength(startOffset, lastOffset - startOffset);
		return caseStatement;
	}

	protected int figureEndOffset(IASTDeclSpecifier specifier, IASTDeclarator[] declarators) {
		if (declarators.length == 0) {
			return calculateEndOffset(specifier);
		}
		return calculateEndOffset(declarators[declarators.length - 1]);
	}

	protected int figureEndOffset(IASTDeclSpecifier specifier, IASTDeclarator declarator) {
		if (declarator == null || ((ASTNode) declarator).getLength() == 0)
			return calculateEndOffset(specifier);
		return calculateEndOffset(declarator);
	}

	protected boolean isLegalWithoutDestructor(IASTDeclSpecifier specifier) {
		if (specifier instanceof IASTCompositeTypeSpecifier)
			return true;
		if (specifier instanceof IASTElaboratedTypeSpecifier)
			return true;
		if (specifier instanceof IASTEnumerationSpecifier)
			return true;

		return false;
	}

	/**
	 * Consume the next token available only if the type is as specified. In case we reached
	 * the end of completion, no token is consumed and the eoc-token returned.
	 *
	 * @param type
	 *            The type of token that you are expecting.
	 * @return the token that was consumed and removed from our buffer.
	 * @throws BacktrackException
	 *             if lookaheadType(1) != type
	 */
	protected IToken consumeOrEndOfCompletion(int type) throws EndOfFileException, BacktrackException {
		final IToken token = lookahead(1);
		final int tokenType = token.getType();
		if (tokenType != type) {
			if (tokenType == IToken.tEOC)
				return token;
			throwBacktrack(token);
		}
		return consume();
	}

	protected IASTExpression parseTypeIDInParenthesisOrUnaryExpression(boolean exprIsLimitedToParenthesis, int offset,
			int typeExprKind, int unaryExprKind, CastExprCtx ctx, ITemplateIdStrategy strat)
			throws BacktrackException, EndOfFileException {
		IASTTypeId typeid;
		IASTExpression expr = null;
		IToken typeidLA = null;
		IToken mark = mark();
		int endOffset1 = -1;
		int endOffset2 = -1;

		try {
			consume(IToken.tLPAREN);
			int typeidOffset = lookahead(1).getOffset();
			typeid = typeID(DeclarationOptions.TYPEID);
			if (!isValidTypeIDForUnaryExpression(unaryExprKind, typeid)) {
				typeid = null;
			} else {
				switch (lookaheadType(1)) {
				case IToken.tRPAREN:
				case IToken.tEOC:
					endOffset1 = consume().getEndOffset();
					typeidLA = lookahead(1);
					break;
				case IToken.tCOMMA:
					typeid = null;
					break;
				default:
					typeid = null;
					break;
				}
			}
		} catch (BacktrackException e) {
			typeid = null;
		}

		CastAmbiguityMarker marker = null;
		backup(mark);
		try {
			if (exprIsLimitedToParenthesis) {
				consume(IToken.tLPAREN);
				expr = expression();
				endOffset2 = consumeOrEndOfCompletion(IToken.tRPAREN).getEndOffset();
			} else {
				expr = unaryExpression(ctx, strat);
				if (expr instanceof CastAmbiguityMarker) {
					marker = (CastAmbiguityMarker) expr;
					expr = marker.getExpression();
					assert !(expr instanceof CastAmbiguityMarker);
				}
				endOffset2 = calculateEndOffset(expr);
			}
		} catch (BacktrackException exception) {
			if (typeid == null)
				throw exception;
		}

		IASTExpression result1 = null;
		if (typeid != null && endOffset1 >= endOffset2) {
			IASTTypeIdExpression typeIdExpression = nodeFactory.newTypeIdExpression(typeExprKind, typeid);
			setRange(typeIdExpression, offset, endOffset1);
			result1 = typeIdExpression;
			backup(typeidLA);

			if (expr == null || endOffset1 > endOffset2)
				return result1;
		}

		IASTExpression result2 = unaryExprKind == -1 ? expr
				: buildUnaryExpression(unaryExprKind, expr, offset, endOffset2);
		if (marker != null)
			result2 = marker.updateExpression(result2);

		if (result1 == null)
			return result2;

		IASTAmbiguousExpression ambExpr = createAmbiguousExpression();
		ambExpr.addExpression(result1);
		ambExpr.addExpression(result2);
		((ASTNode) ambExpr).setOffsetAndLength((ASTNode) result1);
		return ambExpr;
	}

	/**
	 * Checks whether the type specified by the {@code declSpecifier} is "auto" and
	 * the subsequent token, when looking beyond potential ref-qualifiers (& and &&),
	 * will be a single opening bracket ([) followed by an identifier.
	 */
	protected boolean isAtStartOfStructuredBinding(IASTDeclSpecifier declSpecifier) {
		if (!isAutoType(declSpecifier)) {
			return false;
		}
		int expectedBracketOffset = 1;
		int nextToken = lookaheadTypeWithEndOfFile(expectedBracketOffset);
		if (nextToken == IToken.tAMPER || nextToken == IToken.tAND) {
			expectedBracketOffset++;
		}
		return lookaheadTypeWithEndOfFile(expectedBracketOffset) == IToken.tLBRACKET
				&& lookaheadTypeWithEndOfFile(expectedBracketOffset + 1) == IToken.tIDENTIFIER;
	}

	protected Decl initDeclSpecifierSequenceDeclarator(final DeclarationOptions option,
			boolean acceptCompoundWithoutDtor)
			throws EndOfFileException, FoundAggregateInitializer, BacktrackException {
		return initDeclSpecifierSequenceDeclarator(option, acceptCompoundWithoutDtor, null);
	}

	/**
	 * Parses for two alternatives of a declspec sequence followed by a initDeclarator.
	 * A second alternative is accepted only, if it ends at the same point of the first alternative. Otherwise the
	 * longer alternative is selected.
	 */
	protected Decl initDeclSpecifierSequenceDeclarator(final DeclarationOptions option,
			boolean acceptCompoundWithoutDtor, ITemplateIdStrategy strat)
			throws EndOfFileException, FoundAggregateInitializer, BacktrackException {
		Decl result = declSpecifierSeq(option, strat);

		final int type = lookaheadTypeWithEndOfFile(1);
		if (type == IToken.tEOC)
			return result;

		// support for structured bindings
		if (isAtStartOfStructuredBinding(result.fDeclSpec1)) {
			result.isAtStartOfStructuredBinding = true;
			return result;
		}

		// support simple declarations without declarators
		final boolean acceptEmpty = acceptCompoundWithoutDtor && isLegalWithoutDestructor(result.fDeclSpec1);
		if (acceptEmpty) {
			switch (type) {
			case 0:
			case IToken.tEOC:
			case IToken.tSEMI:
				return result;
			}
		}

		final IToken dtorMark1 = mark();
		final IToken dtorMark2 = result.fDtorToken1;
		final IASTDeclSpecifier declspec1 = result.fDeclSpec1;
		final IASTDeclSpecifier declspec2 = result.fDeclSpec2;
		IASTDeclarator dtor1, dtor2;
		try {
			// declarator for first variant
			dtor1 = initDeclarator(declspec1, option);
		} catch (BacktrackException e) {
			if (acceptEmpty) {
				backup(dtorMark1);
				dtor1 = null;
			} else {
				// try second variant, if possible
				if (dtorMark2 == null)
					throw e;

				backup(dtorMark2);
				dtor2 = initDeclarator(declspec2, option);
				return result.set(declspec2, dtor2, dtorMark2);
			}
		}

		// first variant was a success. If possible, try second one.
		if (dtorMark2 == null) {
			return result.set(declspec1, dtor1, dtorMark1);
		}

		final IToken end = mark();
		backup(dtorMark2);
		try {
			dtor2 = initDeclarator(declspec2, option);
		} catch (BacktrackException e) {
			backup(end);
			return result.set(declspec1, dtor1, dtorMark1);
		}

		final IToken altEnd = mark();
		if (end == altEnd) {
			return result.set(declspec1, dtor1, declspec2, dtor2);
		}
		if (end.getEndOffset() > altEnd.getEndOffset()) {
			backup(end);
			return result.set(declspec1, dtor1, dtorMark1);
		}

		return result.set(declspec2, dtor2, dtorMark2);
	}

	protected IASTExpression unaryExpression(int operator, CastExprCtx ctx, ITemplateIdStrategy strat)
			throws EndOfFileException, BacktrackException {
		final IToken operatorToken = consume();
		IASTExpression operand = castExpression(ctx, strat);

		CastAmbiguityMarker marker = null;
		if (operand instanceof CastAmbiguityMarker) {
			marker = (CastAmbiguityMarker) operand;
			operand = marker.getExpression();
			assert !(operand instanceof CastAmbiguityMarker);
		}

		if (operator == IASTUnaryExpression.op_star && operand instanceof IASTLiteralExpression) {
			IASTLiteralExpression literal = (IASTLiteralExpression) operand;
			switch (literal.getKind()) {
			case IASTLiteralExpression.lk_char_constant:
			case IASTLiteralExpression.lk_float_constant:
			case IASTLiteralExpression.lk_integer_constant:
			case IASTLiteralExpression.lk_true:
			case IASTLiteralExpression.lk_false:
			case IASTLiteralExpression.lk_nullptr:
				throwBacktrack(operatorToken);
			}
		}

		IASTExpression result = buildUnaryExpression(operator, operand, operatorToken.getOffset(),
				calculateEndOffset(operand));
		return marker == null ? result : marker.updateExpression(result);
	}

	protected IASTExpression buildUnaryExpression(int operator, IASTExpression operand, int offset, int lastOffset) {
		IASTUnaryExpression result = nodeFactory.newUnaryExpression(operator, operand);
		setRange(result, offset, lastOffset);
		return result;
	}

	protected boolean canBeCastExpression() throws EndOfFileException {
		IToken m = mark();
		try {
			// The parenthesis cannot be followed by a binary operator
			skipBrackets(IToken.tLPAREN, IToken.tRPAREN, IToken.tSEMI);
			switch (lookaheadTypeWithEndOfFile(1)) {
			case IToken.tAMPERASSIGN:
			case IToken.tARROW:
			case IToken.tARROWSTAR:
			case IToken.tASSIGN:
			case IToken.tBITOR:
			case IToken.tBITORASSIGN:
			case IToken.tCOLON:
			case IToken.tCOMMA:
			case IToken.tDIV:
			case IToken.tDIVASSIGN:
			case IToken.tDOT:
			case IToken.tDOTSTAR:
			case IToken.tEQUAL:
			case IToken.tGT:
			case IToken.tGT_in_SHIFTR:
			case IToken.tGTEQUAL:
			case IToken.tLBRACKET:
			case IToken.tLTEQUAL:
			case IToken.tMINUSASSIGN:
			case IToken.tMOD:
			case IToken.tMODASSIGN:
			case IToken.tNOTEQUAL:
			case IToken.tOR:
			case IToken.tPLUSASSIGN:
			case IToken.tQUESTION:
			case IToken.tRBRACE:
			case IToken.tRBRACKET:
			case IToken.tRPAREN:
			case IToken.tSEMI:
			case IToken.tSHIFTL:
			case IToken.tSHIFTLASSIGN:
			case IToken.tSHIFTR:
			case IToken.tSHIFTRASSIGN:
			case IToken.tSTARASSIGN:
				return false;
			}
			return true;
		} catch (BacktrackException exception) {
			return false;
		} finally {
			backup(m);
		}
	}

	protected final boolean isOnSameLine(int offset, int secondaryOffset) {
		final ILocationResolver resolver = getCompilationUnit().getAdapter(ILocationResolver.class);
		final IASTFileLocation location = resolver.getMappedFileLocation(offset, secondaryOffset - offset + 1);
		return location.getFileName().equals(resolver.getContainingFilePath(offset))
				&& location.getStartingLineNumber() == location.getEndingLineNumber();
	}

	protected final boolean isAutoType(IASTDeclSpecifier specifier) {
		if (specifier instanceof IASTSimpleDeclSpecifier) {
			IASTSimpleDeclSpecifier simpleDeclSpecifier = (IASTSimpleDeclSpecifier) specifier;
			return simpleDeclSpecifier.getType() == IASTSimpleDeclSpecifier.t_auto;
		}
		return false;
	}

	protected final void adjustLength(IASTNode node, IASTNode endNode) {
		final int endOffset = calculateEndOffset(endNode);
		adjustEndOffset(node, endOffset);
	}

	protected final <T extends IASTNode> T adjustEndOffset(T genericNode, final int endOffset) {
		final ASTNode node = (ASTNode) genericNode;
		node.setLength(endOffset - node.getOffset());
		return genericNode;
	}

	protected final int calculateEndOffset(IASTNode genericNode) {
		ASTNode node = (ASTNode) genericNode;
		return node.getOffset() + node.getLength();
	}

	protected final <T extends IASTNode> T setRange(T node, IASTNode from) {
		((ASTNode) node).setOffsetAndLength((ASTNode) from);
		return node;
	}

	protected final <T extends IASTNode> T setRange(T node, IASTNode from, int endOffset) {
		final int offset = ((ASTNode) from).getOffset();
		((ASTNode) node).setOffsetAndLength(offset, endOffset - offset);
		return node;
	}

	protected final <T extends IASTNode> T setRange(T node, int offset, int endOffset) {
		((ASTNode) node).setOffsetAndLength(offset, endOffset - offset);
		return node;
	}

	protected final void skipBrackets(int left, int right, int terminator)
			throws EndOfFileException, BacktrackException {
		consume(left);
		int nesting = 0;
		int braceNesting = 0;
		while (true) {
			final int type = lookaheadType(1);

			if (type == IToken.tEOC)
				throwBacktrack(lookahead(1));

			// Ignore passages inside braces (such as for a statement-expression),
			// as they can basically contain tokens of any kind.
			if (type == IToken.tLBRACE) {
				braceNesting++;
			} else if (type == IToken.tRBRACE) {
				braceNesting--;
			}
			if (braceNesting > 0) {
				consume();
				continue;
			}

			if (type == terminator)
				throwBacktrack(lookahead(1));

			consume();
			if (type == left) {
				nesting++;
			} else if (type == right) {
				if (--nesting < 0) {
					return;
				}
			}
		}
	}

	protected final void declarationListInBraces(final IASTDeclarationListOwner owner, int offset,
			DeclarationOptions options) throws EndOfFileException, BacktrackException {
		// consume brace, if requested
		int codeBranchNesting = getCodeBranchNesting();
		consume(IToken.tLBRACE);
		declarationList(owner, options, true, codeBranchNesting);

		final int type = lookaheadTypeWithEndOfFile(1);
		if (type == IToken.tRBRACE) {
			int endOffset = consume().getEndOffset();
			setRange(owner, offset, endOffset);
			return;
		}

		final int endOffset = getEndOffset();
		setRange(owner, offset, endOffset);
		if (type == IToken.tEOC || (type == 0 && owner instanceof IASTCompositeTypeSpecifier)) {
			return;
		}
		throwBacktrack(createProblem(IProblem.SYNTAX_ERROR, endOffset, 0), owner);
	}

	protected final void addAttributeSpecifiers(List<IASTAttributeSpecifier> specifiers, IASTAttributeOwner owner) {
		assert owner != null;
		if (specifiers != null) {
			for (IASTAttributeSpecifier specifier : specifiers) {
				owner.addAttributeSpecifier(specifier);
			}
		}
	}

	protected final int attributesEndOffset(int endOffset, List<IASTAttributeSpecifier> specifiers) {
		if (specifiers == null || specifiers.isEmpty()) {
			return endOffset;
		}
		ASTNode lastSpecifier = (ASTNode) specifiers.get(specifiers.size() - 1);
		return Math.max(endOffset, calculateEndOffset(lastSpecifier));
	}

	protected final void throwBacktrack(IASTProblem problem, IASTNode node) throws BacktrackException {
		++backtrackCount;
		backtrack.initialize(problem, node);
		throw backtrack;
	}

	protected final void throwBacktrack(int offset, int length) throws BacktrackException {
		++backtrackCount;
		backtrack.initialize(offset, length < 0 ? 0 : length);
		throw backtrack;
	}

	protected final void throwBacktrack(IToken token) throws BacktrackException {
		throwBacktrack(token.getOffset(), token.getLength());
	}

	protected final void throwBacktrack(IASTProblem problem) throws BacktrackException {
		++backtrackCount;
		backtrack.initialize(problem);
		throw backtrack;
	}

	protected final void throwBacktrack(IASTNode inode) throws BacktrackException {
		final ASTNode node = (ASTNode) inode;
		throwBacktrack(node.getOffset(), node.getLength());
	}

	/**
	 * Roll back to a previous point, reseting the queue of tokens.
	 * @param mark a token previously obtained via {@link #mark()}.
	 */
	protected final void backup(IToken mark) {
		nextToken = mark;
	}

	protected final IASTProblem createProblem(int signal, int offset, int length) {
		IASTProblem result = nodeFactory.newProblem(signal, CharArrayUtils.EMPTY, true);
		((ASTNode) result).setOffsetAndLength(offset, length);
		return result;
	}

	/**
	 * Returns the next token, which can be used to reset the input back to
	 * this point in the stream.
	 */
	protected final IToken mark() throws EndOfFileException {
		return lookahead();
	}

	protected final int getCodeBranchNesting() {
		return scanner.getCodeBranchNesting();
	}

	protected final int getEndOffset() {
		if (lastTokenFromScanner == null)
			return 0;
		return lastTokenFromScanner.getEndOffset();
	}

	/**
	 * Returns the next token without advancing. Same as {@code LA(1)}.
	 */
	protected final IToken lookahead() throws EndOfFileException {
		IToken token = nextToken(true);
		checkEndOfInactive(token);
		return token;
	}

	/**
	 * Returns one of the next tokens. With {@code i == 1}, the next token is returned.
	 * @param i number of tokens to look ahead, must be greater than 0.
	 */
	protected final IToken lookahead(int i) throws EndOfFileException {
		IToken token = lookaheadToken(i, true);
		checkEndOfInactive(token);
		return token;
	}

	protected final IToken lookaheadWithEndOfFile(int i) {
		try {
			return lookahead(i);
		} catch (EndOfFileException exception) {
			return null;
		}
	}

	/**
	 * Look ahead in the token list and return the token type.
	 * @param i number of tokens to look ahead, must be greater or equal to 0.
	 * @return The type of that token
	 */
	protected final int lookaheadType(int i) throws EndOfFileException {
		return lookahead(i).getType();
	}

	protected final int lookaheadTypeWithEndOfFile(int i) {
		try {
			return lookaheadType(i);
		} catch (EndOfFileException exception) {
			return 0;
		}
	}

	protected final void skipInactiveCode() throws OffsetLimitReachedException {
		IToken token = nextToken;
		if (activeCode && (token == null || token.getType() != IToken.tINACTIVE_CODE_START))
			return;
		try {
			activeCode = true;
			while (token != null && token.getType() != IToken.tINACTIVE_CODE_END) {
				token = token.getNext();
			}

			if (token != null) {
				nextToken = token.getNext();
			} else {
				nextToken = null;
				scanner.skipInactiveCode();
			}
		} catch (OffsetLimitReachedException exception) {
			if (mode == ParserMode.COMPLETION_PARSE) {
				createCompletionNode(exception.getFinalToken());
				throw exception;
			}
		}
	}

	private final void checkEndOfInactive(IToken token) throws EndOfFileException {
		final int type = token.getType();
		if (type == IToken.tINACTIVE_CODE_SEPARATOR || type == IToken.tINACTIVE_CODE_END)
			throw new EndOfFileException(token.getOffset(), true);
	}

	protected final IToken consume() throws EndOfFileException {
		IToken token = nextToken(true);
		checkEndOfInactive(token);

		nextToken = token.getNext();
		return token;
	}

	protected final IToken consume(int type) throws EndOfFileException, BacktrackException {
		final IToken result = consume();
		if (result.getType() != type)
			throwBacktrack(result);
		return result;
	}

	/**
	 * The next token is consumed. Afterwards its type is checked and a {@link BacktrackException}
	 * is thrown if the type neither matches <code>type1</code> nor <code>type2</code>.
	 */
	protected final IToken consume(int first, int second) throws EndOfFileException, BacktrackException {
		final IToken result = consume();
		final int type = result.getType();
		if (type != first && type != second) {
			throwBacktrack(result);
		}
		return result;
	}

	protected final boolean acceptInactiveCodeBoundary(int nesting) {
		try {
			while (true) {
				IToken token = nextToken(false);
				switch (token.getType()) {
				case IToken.tINACTIVE_CODE_START:
				case IToken.tINACTIVE_CODE_SEPARATOR:
					IInactiveCodeToken it = (IInactiveCodeToken) token;
					if (it.getNewNesting() < nesting
							|| (it.getNewNesting() == nesting && it.getOldNesting() == nesting)) {
						return false;
					}
					activeCode = false;
					nextToken = token.getNext(); // consume the token
					continue;
				case IToken.tINACTIVE_CODE_END:
					it = (IInactiveCodeToken) token;
					if (it.getNewNesting() < nesting
							|| (it.getNewNesting() == nesting && it.getOldNesting() == nesting)) {
						return false;
					}
					activeCode = true;
					nextToken = token.getNext(); // consume the token
					continue;
				default:
					return true;
				}
			}
		} catch (EndOfFileException exception) {
		}
		return true;
	}

	protected final IASTExpression castExpression(CastExprCtx ctx, ITemplateIdStrategy strat)
			throws EndOfFileException, BacktrackException {
		if (lookaheadType(1) == IToken.tLPAREN) {
			final IToken mark = mark();
			final int startingOffset = mark.getOffset();
			final boolean canBeCast = canBeCastExpression();
			consume();
			IASTTypeId typeId = null;
			if (canBeCast) {
				try {
					typeId = typeID(DeclarationOptions.TYPEID);
				} catch (BacktrackException exception) {
				}
			}
			if (typeId != null && lookaheadType(1) == IToken.tRPAREN) {
				consume();
				boolean unaryFailed = false;
				if (ctx == CastExprCtx.eDirectlyInBExpr) {
					switch (lookaheadType(1)) {
					// ambiguity with unary operator
					case IToken.tPLUS:
					case IToken.tMINUS:
					case IToken.tSTAR:
					case IToken.tAMPER:
					case IToken.tAND:
						final int operatorOffset = lookahead(1).getOffset();
						IToken markEnd = mark();
						backup(mark);
						try {
							IASTExpression unary = unaryExpression(CastExprCtx.eInBExpr, strat);
							return new CastAmbiguityMarker(unary, typeId, operatorOffset);
						} catch (BacktrackException exception) {
							backup(markEnd);
							unaryFailed = true;
						}
					}
				}
				try {
					boolean couldBeMethodCall = lookaheadType(1) == IToken.tLPAREN;
					IASTExpression right = castExpression(ctx, strat);

					CastAmbiguityMarker marker = null;
					if (right instanceof CastAmbiguityMarker) {
						marker = (CastAmbiguityMarker) right;
						right = marker.getExpression();
						assert !(right instanceof CastAmbiguityMarker);
					}
					IASTCastExpression result = buildCastExpression(IASTCastExpression.op_cast, typeId, right,
							startingOffset, calculateEndOffset(right));
					if (!unaryFailed && couldBeMethodCall && !(right instanceof IASTCastExpression)) {
						IToken markEnd = mark();
						backup(mark);
						try {
							IASTExpression expr = primaryExpression(ctx, strat);
							IASTFunctionCallExpression fcall = nodeFactory.newFunctionCallExpression(expr,
									(IASTExpression[]) null);
							IASTAmbiguousExpression ambiguity = createAmbiguousCastVsMethodCallExpression(result,
									fcall);
							((ASTNode) ambiguity).setOffsetAndLength((ASTNode) result);
							return marker == null ? ambiguity : marker.updateExpression(ambiguity);
						} catch (BacktrackException exception) {
						} finally {
							backup(markEnd);
						}
					}
					return marker == null ? result : marker.updateExpression(result);
				} catch (BacktrackException exception) {
					if (unaryFailed)
						throw exception;
				}
			}
			backup(mark);
		}
		return unaryExpression(ctx, strat);
	}

	private IASTExpression buildExpression(IASTExpression left, BinaryOperator operator) {
		int operation, unaryOperation = 0;
		final IASTInitializerClause right = operator.fExpression;
		switch (operator.fOperatorToken) {
		case IToken.tQUESTION:
			final IASTInitializerClause negative;
			if (operator.fNext == null || operator.fNext.fOperatorToken != IToken.tCOLON) {
				negative = null;
			} else {
				negative = operator.fNext.fExpression;
				operator.fNext = operator.fNext.fNext;
			}
			IASTConditionalExpression conditionalEx = nodeFactory.newConditionalExpession(left, (IASTExpression) right,
					(IASTExpression) negative);
			setRange(conditionalEx, left);
			if (negative != null) {
				adjustLength(conditionalEx, negative);
			}
			return conditionalEx;

		case IToken.tCOMMA:
			IASTExpressionList list;
			if (left instanceof IASTExpressionList) {
				list = (IASTExpressionList) left;
			} else {
				list = nodeFactory.newExpressionList();
				list.addExpression(left);
				setRange(list, left);
			}
			list.addExpression((IASTExpression) right);
			adjustLength(list, right);
			return list;

		case IToken.tASSIGN:
			operation = IASTBinaryExpression.op_assign;
			break;
		case IToken.tSTARASSIGN:
			operation = IASTBinaryExpression.op_multiplyAssign;
			break;
		case IToken.tDIVASSIGN:
			operation = IASTBinaryExpression.op_divideAssign;
			break;
		case IToken.tMODASSIGN:
			operation = IASTBinaryExpression.op_moduloAssign;
			break;
		case IToken.tPLUSASSIGN:
			operation = IASTBinaryExpression.op_plusAssign;
			break;
		case IToken.tMINUSASSIGN:
			operation = IASTBinaryExpression.op_minusAssign;
			break;
		case IToken.tSHIFTRASSIGN:
			operation = IASTBinaryExpression.op_shiftRightAssign;
			break;
		case IToken.tSHIFTLASSIGN:
			operation = IASTBinaryExpression.op_shiftLeftAssign;
			break;
		case IToken.tAMPERASSIGN:
			operation = IASTBinaryExpression.op_binaryAndAssign;
			break;
		case IToken.tXORASSIGN:
			operation = IASTBinaryExpression.op_binaryXorAssign;
			break;
		case IToken.tBITORASSIGN:
			operation = IASTBinaryExpression.op_binaryOrAssign;
			break;
		case IToken.tOR:
			operation = IASTBinaryExpression.op_logicalOr;
			break;
		case IToken.tAND:
			operation = IASTBinaryExpression.op_logicalAnd;
			break;
		case IToken.tBITOR:
			operation = IASTBinaryExpression.op_binaryOr;
			break;
		case IToken.tXOR:
			operation = IASTBinaryExpression.op_binaryXor;
			break;
		case IToken.tAMPER:
			operation = IASTBinaryExpression.op_binaryAnd;
			unaryOperation = IASTUnaryExpression.op_amper;
			break;
		case IToken.tEQUAL:
			operation = IASTBinaryExpression.op_equals;
			break;
		case IToken.tNOTEQUAL:
			operation = IASTBinaryExpression.op_notequals;
			break;
		case IToken.tGT:
			operation = IASTBinaryExpression.op_greaterThan;
			break;
		case IToken.tLT:
			operation = IASTBinaryExpression.op_lessThan;
			break;
		case IToken.tLTEQUAL:
			operation = IASTBinaryExpression.op_lessEqual;
			break;
		case IToken.tGTEQUAL:
			operation = IASTBinaryExpression.op_greaterEqual;
			break;
		case IToken.tSHIFTL:
			operation = IASTBinaryExpression.op_shiftLeft;
			break;
		case IToken.tSHIFTR:
			operation = IASTBinaryExpression.op_shiftRight;
			break;
		case IToken.tPLUS:
			operation = IASTBinaryExpression.op_plus;
			unaryOperation = IASTUnaryExpression.op_plus;
			break;
		case IToken.tMINUS:
			operation = IASTBinaryExpression.op_minus;
			unaryOperation = IASTUnaryExpression.op_minus;
			break;
		case IToken.tSTAR:
			operation = IASTBinaryExpression.op_multiply;
			unaryOperation = IASTUnaryExpression.op_star;
			break;
		case IToken.tDIV:
			operation = IASTBinaryExpression.op_divide;
			break;
		case IToken.tMOD:
			operation = IASTBinaryExpression.op_modulo;
			break;
		case IToken.tDOTSTAR:
			operation = IASTBinaryExpression.op_pmdot;
			break;
		case IToken.tARROWSTAR:
			operation = IASTBinaryExpression.op_pmarrow;
			break;

		default:
			assert false;
			return null;
		}

		IASTExpression result = buildBinaryExpression(operation, left, right, calculateEndOffset(right));
		final CastAmbiguityMarker marker = operator.fAmbiguityMarker;
		if (marker != null) {
			if (unaryOperation != 0) {
				result = createCastVsBinaryExpressionAmbiguity((IASTBinaryExpression) result, marker.getTypeIdForCast(),
						unaryOperation, marker.getUnaryOperatorOffset());
			} else {
				assert false;
			}
		}
		return result;
	}

	private boolean isValidTypeIDForUnaryExpression(int unaryExprKind, IASTTypeId typeid) {
		if (typeid == null)
			return false;
		if (unaryExprKind == IASTUnaryExpression.op_sizeof) {
			// 5.3.3.1
			if (ASTQueries.findTypeRelevantDeclarator(typeid.getAbstractDeclarator()) instanceof IASTFunctionDeclarator)
				return false;
		}
		return true;
	}

	private int skipToSemiOrClosingBrace(int offset, boolean eatBrace) {
		passing = false;
		declarationMark = null;
		int depth = 0;
		int endOffset;
		loop: try {
			endOffset = lookahead(1).getOffset();
			while (true) {
				switch (lookaheadType(1)) {
				case IToken.tEOC:
					endOffset = getEndOffset();
					break loop;
				case IToken.tSEMI:
					if (depth == 0) {
						endOffset = consume().getEndOffset();
						break loop;
					}
					break;
				case IToken.tLBRACE:
					++depth;
					break;
				case IToken.tRBRACE:
					if (--depth <= 0) {
						if (depth == 0 || offset == endOffset || eatBrace) {
							endOffset = consume().getEndOffset(); // consume closing brace
						}
						if (lookaheadTypeWithEndOfFile(1) == IToken.tSEMI) {
							endOffset = consume().getEndOffset();
						}
						break loop;
					}
					break;
				}
				endOffset = consume().getEndOffset();
			}
		} catch (EndOfFileException exception) {
			endOffset = getEndOffset();
		}
		return endOffset;
	}

	private final IToken lookaheadToken(int i, boolean skipInactive) throws EndOfFileException {
		assert i >= 0;
		if (cancelled) {
			throw new ParseError(ParseError.ParseErrorKind.TIMEOUT_OR_CANCELLED);
		}
		IToken token = nextToken(skipInactive);
		for (; i > 1; --i) {
			token = token.getNext();
			if (token == null)
				token = fetchToken(skipInactive);
		}
		return token;
	}

	private final IToken fetchToken(boolean skipInactive) throws EndOfFileException {
		try {
			IToken token = scanner.nextToken();
			if (skipInactive) {
				while (token.getType() == IToken.tINACTIVE_CODE_START) {
					scanner.skipInactiveCode();
					token = scanner.nextToken();
				}
			}
			if (lastTokenFromScanner != null)
				lastTokenFromScanner.setNext(token);
			lastTokenFromScanner = token;
			return token;
		} catch (OffsetLimitReachedException exception) {
			if (mode != ParserMode.COMPLETION_PARSE)
				throw new EndOfFileException(exception.getEndOffset());
			IToken completionToken = exception.getFinalToken();
			createCompletionNode(completionToken);
			if (exception.getOriginator() == OffsetLimitReachedException.ORIGIN_INACTIVE_CODE) {
				// If completion is invoked inside inactive code, there is no AST from which
				// to give the completion node a completion name, so we invent a name.
				// The invented name is not hooked up to the AST, but does have an offset
				// and length, so it can provide an accurate point of reference in
				// declaredBefore().
				IASTName completionName = nodeFactory.newInactiveCompletionName(completionToken.getCharImage(),
						getCompilationUnit());
				((ASTNode) completionName).setOffsetAndLength(completionToken.getOffset(), completionToken.getLength());
				completionNode.addName(completionName);

				// Consume the completion token so we don't try to parse an AST fragment from it.
				consume();
			}
			throw exception;
		}
	}

	private final IToken nextToken(boolean skipInactive) throws EndOfFileException {
		IToken token = nextToken;
		if (token != null)
			return token;

		token = fetchToken(skipInactive);
		nextToken = token;
		return token;
	}

	private final void declarationList(final IASTDeclarationListOwner unit, DeclarationOptions options,
			boolean upToBrace, int codeBranchNesting) {
		final boolean wasActive = activeCode;
		while (true) {
			final boolean ok = acceptInactiveCodeBoundary(codeBranchNesting);
			if (!ok) {
				// We left to an enclosing code branch. If we started in inactive code, it's time to leave.
				if (!wasActive)
					return;

				// If we started in active code, we need to skip the outer and therefore unrelated
				// inactive branches until we hit active code again.
				try {
					skipInactiveCode();
				} catch (OffsetLimitReachedException exception) {
					return;
				}
				codeBranchNesting = Math.min(getCodeBranchNesting() + 1, codeBranchNesting);

				// We could be at the start of inactive code so restart the loop.
				continue;
			}

			final boolean active = activeCode;
			IToken next = lookaheadWithEndOfFile(1);
			if (next == null || next.getType() == IToken.tEOC)
				return;

			if (upToBrace && next.getType() == IToken.tRBRACE && active == wasActive) {
				return;
			}

			final int offset = next.getOffset();
			declarationMark = next;
			next = null; // Don't hold on to the token while parsing namespaces, class bodies, etc.
			try {
				IASTDeclaration declaration = declaration(options);
				if (((ASTNode) declaration).getLength() == 0 && lookaheadTypeWithEndOfFile(1) != IToken.tEOC) {
					declaration = skipProblemDeclaration(offset);
				}
				addDeclaration(unit, declaration, active);
			} catch (BacktrackException exception) {
				IASTDeclaration[] decls = problemDeclaration(offset, exception, options);
				for (IASTDeclaration declaration : decls) {
					addDeclaration(unit, declaration, active);
				}
			} catch (EndOfFileException exception) {
				IASTDeclaration declaration = skipProblemDeclaration(offset);
				addDeclaration(unit, declaration, active);
				if (!exception.endsInactiveCode()) {
					break;
				}
			} finally {
				declarationMark = null;
			}
		}
	}

	protected static final ASTMarkInactiveVisitor MARK_INACTIVE = new ASTMarkInactiveVisitor();

	private void addDeclaration(final IASTDeclarationListOwner parent, IASTDeclaration declaration,
			final boolean active) {
		if (!active) {
			declaration.accept(MARK_INACTIVE);
		}
		parent.addDeclaration(declaration);
	}

	private IASTProblemDeclaration buildProblemDeclaration(IASTProblem problem) {
		IASTProblemDeclaration declaration = nodeFactory.newProblemDeclaration(problem);
		((ASTNode) declaration).setOffsetAndLength(((ASTNode) problem));
		return declaration;
	}

	private IASTProblemStatement buildProblemStatement(IASTProblem problem) {
		IASTProblemStatement statement = nodeFactory.newProblemStatement(problem);
		((ASTNode) statement).setOffsetAndLength(((ASTNode) problem));
		return statement;
	}

	private IASTProblemExpression buildProblemExpression(IASTProblem problem) {
		final IASTProblemExpression expr = nodeFactory.newProblemExpression(problem);
		((ASTNode) expr).setOffsetAndLength(((ASTNode) problem));
		return expr;
	}

	private IASTExpression createCastVsBinaryExpressionAmbiguity(IASTBinaryExpression expr, final IASTTypeId typeid,
			int unaryOperator, int unaryOffset) {
		IASTUnaryExpression unary = nodeFactory.newUnaryExpression(unaryOperator, null);
		((ASTNode) unary).setOffset(unaryOffset);
		IASTCastExpression castExpr = buildCastExpression(IASTCastExpression.op_cast, typeid, unary, 0, 0);
		IASTExpression result = createAmbiguousBinaryVsCastExpression(expr, castExpr);
		((ASTNode) result).setOffsetAndLength((ASTNode) expr);
		return result;
	}

	private IASTProblem skipProblemEnumerator(int offset) {
		passing = false;
		final int endOffset = skipToSemiOrClosingBrace(offset, true);
		return createProblem(IProblem.SYNTAX_ERROR, offset, endOffset - offset);
	}

	protected static final class ASTMarkInactiveVisitor extends ASTGenericVisitor {
		public ASTMarkInactiveVisitor() {
			super(true);
			this.shouldVisitAmbiguousNodes = true;
		}

		@Override
		protected int genericVisit(IASTNode node) {
			((ASTNode) node).setInactive();
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(ASTAmbiguousNode node) {
			node.setInactive();
			IASTNode[] alternatives = node.getNodes();
			for (IASTNode alternative : alternatives) {
				if (!alternative.accept(this))
					return PROCESS_ABORT;
			}
			return PROCESS_CONTINUE;
		}
	}

	protected static class Decl {
		public IASTDeclSpecifier fDeclSpec1;
		public IASTDeclSpecifier fDeclSpec2;

		public IASTDeclarator fDtor1;
		public IASTDeclarator fDtor2;
		public IToken fDtorToken1;

		public boolean isAtStartOfStructuredBinding = false;

		public Decl() {
		}

		public Decl set(IASTDeclSpecifier declspec, IASTDeclarator dtor, IToken dtorToken) {
			fDeclSpec1 = declspec;
			fDtor1 = dtor;
			fDtorToken1 = dtorToken;
			fDeclSpec2 = null;
			fDtor2 = null;
			return this;
		}

		public Decl set(IASTDeclSpecifier declspec1, IASTDeclarator dtor1, IASTDeclSpecifier declspec2,
				IASTDeclarator dtor2) {
			fDeclSpec1 = declspec1;
			fDtor1 = dtor1;
			fDtorToken1 = null;
			fDeclSpec2 = declspec2;
			fDtor2 = dtor2;
			return this;
		}
	}

	protected static class FoundAggregateInitializer extends Exception {
		public final IASTDeclarator fDeclarator;
		public final IASTDeclSpecifier fDeclSpec;

		public FoundAggregateInitializer(IASTDeclSpecifier declSpec, IASTDeclarator d) {
			fDeclSpec = declSpec;
			fDeclarator = d;
		}
	}

	protected static enum ExprKind {
		eExpression, eAssignment, eConstant
	}

	protected static enum CastExprCtx {
		eDirectlyInBExpr, eInBExpr, eNotInBExpr
	}

	private final static class CastAmbiguityMarker extends ASTNode implements IASTExpression {
		private IASTExpression fExpression;
		private final IASTTypeId fTypeIdForCast;
		private final int fUnaryOperatorOffset;

		CastAmbiguityMarker(IASTExpression unary, IASTTypeId typeIdForCast, int unaryOperatorOffset) {
			fExpression = unary;
			fTypeIdForCast = typeIdForCast;
			fUnaryOperatorOffset = unaryOperatorOffset;
		}

		public CastAmbiguityMarker updateExpression(IASTExpression expression) {
			fExpression = expression;
			return this;
		}

		public IASTExpression getExpression() {
			return fExpression;
		}

		public IASTTypeId getTypeIdForCast() {
			return fTypeIdForCast;
		}

		public int getUnaryOperatorOffset() {
			return fUnaryOperatorOffset;
		}

		@Override
		public IASTExpression copy() {
			throw new UnsupportedOperationException();
		}

		@Override
		public IASTExpression copy(CopyStyle style) {
			throw new UnsupportedOperationException();
		}

		@Override
		public IType getExpressionType() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isLValue() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ValueCategory getValueCategory() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Models a cast expression followed by an operator. Can be linked into a chain.
	 * This is done right to left, such that a tree of variants can be built.
	 */
	protected static final class BinaryOperator {
		final int fOperatorToken;
		final int fLeftPrecedence;
		final int fRightPrecedence;
		BinaryOperator fNext;
		IASTInitializerClause fExpression;
		final CastAmbiguityMarker fAmbiguityMarker;

		public BinaryOperator(BinaryOperator nextOp, IASTInitializerClause expression, int operatorToken,
				int leftPrecedence, int rightPrecedence) {
			fNext = nextOp;
			fOperatorToken = operatorToken;
			fLeftPrecedence = leftPrecedence;
			fRightPrecedence = rightPrecedence;
			if (expression instanceof CastAmbiguityMarker) {
				fAmbiguityMarker = (CastAmbiguityMarker) expression;
				fExpression = fAmbiguityMarker.fExpression;
				fAmbiguityMarker.fExpression = null;
			} else {
				fExpression = expression;
				fAmbiguityMarker = null;
			}
		}

		public IASTInitializerClause exchange(IASTInitializerClause expr) {
			IASTInitializerClause e = fExpression;
			fExpression = expr;
			return e;
		}

		public IASTInitializerClause getExpression() {
			return fExpression;
		}

		public BinaryOperator getNext() {
			return fNext;
		}

		public void setNext(BinaryOperator next) {
			fNext = next;
		}
	}

	protected interface ITemplateIdStrategy {
		boolean shallParseAsTemplateID(IASTName name);
	}
}

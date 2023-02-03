package org.eclipse.cdt.internal.core.dom.parser.c;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICNodeFactory;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.AbstractSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.BacktrackException;
import org.eclipse.cdt.internal.core.dom.parser.DeclarationOptions;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;

/**
 * Source Parser for the C Language Syntax.
 */
public class CSourceParser extends AbstractSourceCodeParser {

	private IASTTranslationUnit compilationUnit;
	private IIndex index;
	private int fPreventKnrCheck = 0;

	public CSourceParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			ICParserExtensionConfiguration config, IIndex index) {
		super(scanner, parserMode, logService, CNodeFactory.getDefault(), config.supportKnRC(),
				config.getBuiltinBindingsProvider());
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
		case IToken.t_asm:
			return asmDeclaration();
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
		return expression(ExprKind.eExpression);
	}

	@Override
	protected IASTExpression constantExpression() throws BacktrackException, EndOfFileException {
		return expression(ExprKind.eConstant);
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
	protected IASTExpression primaryExpression(CastExprCtx ctx, ITemplateIdStrategy strat)
			throws BacktrackException, EndOfFileException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected IASTStatement statement() throws EndOfFileException, BacktrackException {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Decl declSpecifierSeq(DeclarationOptions option, ITemplateIdStrategy strat)
			throws BacktrackException, EndOfFileException {
		// TODO Auto-generated method stub
		return null;
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

		if (type != 0 && lookaheadType(1) == IToken.t_asm) { // asm labels bug 226121
			consume();
			endOffset = asmExpression(null).getEndOffset();
		}

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
			Decl decl = initDeclSpecifierSequenceDeclarator(declOption, true);
			markBeforDtor = decl.fDtorToken1;
			declSpec = decl.fDeclSpec1;
			dtor = decl.fDtor1;
			altDeclSpec = decl.fDeclSpec2;
			altDtor = decl.fDtor2;
		} catch (FoundAggregateInitializer lie) {
			declSpec = lie.fDeclSpec;
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

		// check for K&R C parameters (0 means it's not K&R C)
		if (fPreventKnrCheck == 0 && supportKnRC) {
			fPreventKnrCheck++;
			try {
				final int numKnRCParms = countKnRCParms();
				if (numKnRCParms > 0) { // KnR C parameters were found
					IASTName[] parmNames = new IASTName[numKnRCParms];
					IASTDeclaration[] parmDeclarations = new IASTDeclaration[numKnRCParms];

					boolean seenParameter = false;
					for (int i = 0; i <= parmNames.length; i++) {
						switch (lookaheadType(1)) {
						case IToken.tCOMMA:
							last = consume();
							parmNames[i] = identifier();
							seenParameter = true;
							break;
						case IToken.tIDENTIFIER:
							if (seenParameter)
								throwBacktrack(startOffset, last.getEndOffset() - startOffset);

							parmNames[i] = identifier();
							seenParameter = true;
							break;
						case IToken.tRPAREN:
							last = consume();
							break;
						default:
							break;
						}
					}

					// now that the parameter names are parsed, parse the parameter declarations
					// count for parameter declarations <= count for parameter names.
					int endOffset = last.getEndOffset();
					for (int i = 0; i < numKnRCParms && lookaheadType(1) != IToken.tLBRACE; i++) {
						try {
							IASTDeclaration decl = simpleDeclaration(DeclarationOptions.LOCAL);
							IASTSimpleDeclaration ok = checkKnrParameterDeclaration(decl, parmNames);
							if (ok != null) {
								parmDeclarations[i] = ok;
								endOffset = calculateEndOffset(ok);
							} else {
								final ASTNode node = (ASTNode) decl;
								parmDeclarations[i] = createKnRCProblemDeclaration(node.getOffset(), node.getLength());
								endOffset = calculateEndOffset(node);
							}
						} catch (BacktrackException b) {
							parmDeclarations[i] = createKnRCProblemDeclaration(b.getOffset(), b.getLength());
							endOffset = b.getOffset() + b.getLength();
						}
					}

					parmDeclarations = ArrayUtil.removeNulls(IASTDeclaration.class, parmDeclarations);
					ICASTKnRFunctionDeclarator functionDecltor = ((ICNodeFactory) nodeFactory)
							.newKnRFunctionDeclarator(parmNames, parmDeclarations);
					((ASTNode) functionDecltor).setOffsetAndLength(startOffset, endOffset - startOffset);
					return functionDecltor;
				}
			} finally {
				fPreventKnrCheck--;
			}
		}

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
					exp = expression(ExprKind.eAssignment);
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
			return expression(ExprKind.eAssignment);

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
		final boolean allowComma = kind == ExprKind.eExpression;
		boolean allowAssignment = kind != ExprKind.eConstant;
		int type;
		int conditionCount = 0;
		BinaryOperator lastOperator = null;
		IASTExpression lastExpression = castExpression(CastExprCtx.eDirectlyInBExpr, null);
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
			lastExpression = castExpression(CastExprCtx.eDirectlyInBExpr, null); // next cast expression
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
			fPreventKnrCheck++;
			Decl decl = initDeclSpecifierSequenceDeclarator(option, false);
			declSpec = decl.fDeclSpec1;
			declarator = decl.fDtor1;
			altDeclSpec = decl.fDeclSpec2;
			altDeclarator = decl.fDtor2;
		} catch (FoundAggregateInitializer lie) {
			declSpec = lie.fDeclSpec;
			declarator = lie.fDeclarator;
		} finally {
			fPreventKnrCheck--;
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

	@Deprecated
	private int countKnRCParms() {
		IToken mark = null;
		int parmCount = 0;
		boolean previousWasIdentifier = false;

		try {
			mark = mark();

			// starts at the beginning of the parameter list
			for (;;) {
				if (lookaheadType(1) == IToken.tCOMMA) {
					consume();
					previousWasIdentifier = false;
				} else if (lookaheadType(1) == IToken.tIDENTIFIER) {
					consume();
					if (previousWasIdentifier) {
						backup(mark);
						return 0; // i.e. KnR C won't have int f(typedef x)
									// char
									// x; {}
					}
					previousWasIdentifier = true;
					parmCount++;
				} else if (lookaheadType(1) == IToken.tRPAREN) {
					if (!previousWasIdentifier) {
						// if the first token encountered is tRPAREN then it's not K&R C
						// the first token when counting K&R C parms is always an identifier
						backup(mark);
						return 0;
					}
					consume();
					break;
				} else {
					backup(mark);
					return 0; // i.e. KnR C won't have int f(char) char x; {}
				}
			}

			// if the next token is a tSEMI then the declaration was a regular
			// declaration statement i.e. int f(type_def);
			final int type = lookaheadType(1);
			if (type == IToken.tSEMI || type == IToken.tLBRACE) {
				backup(mark);
				return 0;
			}

			// look ahead for the start of the function body, if end of file is
			// found then return 0 parameters found (implies not KnR C)
			int previous = -1;
			while (lookaheadType(1) != IToken.tLBRACE) {
				// fix for 100104: check if the parameter declaration is a valid one
				try {
					simpleDeclaration(DeclarationOptions.LOCAL);
				} catch (BacktrackException e) {
					backup(mark);
					return 0;
				}

				final IToken token = lookahead(1);
				if (token.getType() == IToken.tEOC)
					break;

				final int next = token.hashCode();
				if (next == previous) { // infinite loop detected
					break;
				}
				previous = next;
			}

			backup(mark);
			return parmCount;
		} catch (EndOfFileException exception) {
			if (mark != null)
				backup(mark);

			return 0;
		}
	}

	@Deprecated
	private IASTProblemDeclaration createKnRCProblemDeclaration(int offset, int length) throws EndOfFileException {
		IASTProblem problem = createProblem(IProblem.SYNTAX_ERROR, offset, length);
		IASTProblemDeclaration declaration = nodeFactory.newProblemDeclaration(problem);
		((ASTNode) declaration).setOffsetAndLength((ASTNode) problem);

		// consume until LBRACE is found (to leave off at the function body and
		// continue from there)
		IToken previous = null;
		IToken next = null;
		while (lookaheadType(1) != IToken.tLBRACE) {
			next = consume();
			if (next == previous || next.getType() == IToken.tEOC) { // infinite loop detected
				break;
			}
			previous = next;
		}

		return declaration;
	}

	@Deprecated
	private IASTSimpleDeclaration checkKnrParameterDeclaration(IASTDeclaration decl, final IASTName[] paramNames) {
		if (!(decl instanceof IASTSimpleDeclaration))
			return null;

		IASTSimpleDeclaration declaration = ((IASTSimpleDeclaration) decl);
		IASTDeclarator[] decltors = declaration.getDeclarators();
		for (IASTDeclarator decltor : decltors) {
			boolean decltorOk = false;
			final char[] nchars = ASTQueries.findInnermostDeclarator(decltor).getName().toCharArray();
			for (IASTName parmName : paramNames) {
				if (CharArrayUtils.equals(nchars, parmName.toCharArray())) {
					decltorOk = true;
					break;
				}
			}
			if (!decltorOk)
				return null;
		}
		return declaration;
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

					IASTExpression expr = expression(ExprKind.eAssignment);
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

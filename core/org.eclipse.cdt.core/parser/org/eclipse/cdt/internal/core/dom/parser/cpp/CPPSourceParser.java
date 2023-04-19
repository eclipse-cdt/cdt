package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemTypeId;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTToken;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAliasDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAlignmentSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAmbiguousTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAttribute;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAttributeList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTClassVirtSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDecltypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier.ScopeStyle;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFoldExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator.RefQualifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionWithTryBlock;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression.CaptureDefault;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPackExpandable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTRangeBasedForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTStaticAssertDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTStructuredBindingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVirtSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVirtSpecifier.SpecifierKind;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.IToken.ContextSensitiveTokenType;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.CollectionUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.AbstractSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.BacktrackException;
import org.eclipse.cdt.internal.core.dom.parser.DeclarationOptions;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;
import org.eclipse.cdt.internal.core.dom.parser.ITemplateIdStrategy;
import org.eclipse.cdt.internal.core.dom.parser.cpp.NameOrTemplateIDVariants.BranchPoint;
import org.eclipse.cdt.internal.core.dom.parser.cpp.NameOrTemplateIDVariants.Variant;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Source Parser for the C++ Language Syntax.
 */
public class CPPSourceParser extends AbstractSourceCodeParser {

	private static final int DEFAULT_PARAM_LIST_SIZE = 4;
	private static final int DEFAULT_CATCH_HANDLER_LIST_SIZE = 4;
	protected static final int DEFAULT_DESIGNATOR_LIST_SIZE = 4;
	private static final int TEMPLATE_ARGUMENT_NESTING_DEPTH_LIMIT = 192;

	private final static int INLINE = 0x1, CONST = 0x2, CONSTEXPR = 0x4, VOLATILE = 0x10, SHORT = 0x20, UNSIGNED = 0x40,
			SIGNED = 0x80, COMPLEX = 0x100, IMAGINARY = 0x200, VIRTUAL = 0x400, EXPLICIT = 0x800, FRIEND = 0x1000,
			THREAD_LOCAL = 0x2000;
	private static final int FORBID_IN_EMPTY_DECLSPEC = CONST | VOLATILE | SHORT | UNSIGNED | SIGNED | COMPLEX
			| IMAGINARY | THREAD_LOCAL;

	private ICPPASTTranslationUnit compilationUnit;
	private final IIndex index;

	private int templateArgumentNestingDepth = 0;
	private TemplateIdStrategy templateParameterListStrategy;
	private char[] currentClassName;
	private Map<String, ContextSensitiveTokenType> contextSensitiveTokens;
	private int methodBodyCount;
	private char[] additionalNumericalSuffixes;

	public CPPSourceParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			ICPPParserExtensionConfiguration config, IIndex index) {
		super(scanner, parserMode, logService, CPPNodeFactory.getDefault(), config.getBuiltinBindingsProvider());
		this.index = index;
		contextSensitiveTokens = createContextSensitiveTokenMap(config);
		scanner.setSplitShiftROperator(true);
		additionalNumericalSuffixes = scanner.getAdditionalNumericLiteralSuffixes(); // TODO Change the way this is set?
	}

	public Object castExpressionForBinaryExpression(ITemplateIdStrategy strategy, final BinaryExprCtx context)
			throws EndOfFileException, BacktrackException {

		if (context == BinaryExprCtx.EXPR_IN_PRIMARY_EXPRESSION && lookaheadTypeWithEndOfFile(1) == IToken.tELLIPSIS) {
			// Ellipsis of fold-expression
			IToken foldToken = consume();
			return buildFoldExpressionToken(foldToken.getOffset(), foldToken.getEndOffset());
		}

		if (strategy != null) {
			return castExpression(CastExprCtx.DIRECTLY_IN_BINARY_EXPR, strategy);
		}

		TemplateIdStrategy strat = new TemplateIdStrategy();
		Variant variants = null;
		IASTExpression singleResult = null;
		IASTName[] firstNames = null;

		final IToken mark = mark();
		IToken lastToken = null;
		while (true) {
			try {
				IASTExpression e = castExpression(CastExprCtx.DIRECTLY_IN_BINARY_EXPR, strat);
				if (variants == null) {
					if (singleResult == null || lastToken == null) {
						singleResult = e;
						firstNames = strat.getTemplateNames();
					} else {
						variants = new Variant(null, singleResult, firstNames, lastToken.getOffset());
						singleResult = null;
						firstNames = null;
					}
				}
				lastToken = lookahead();
				if (variants != null) {
					variants = new Variant(variants, e, strat.getTemplateNames(), lastToken.getOffset());
				}
				if (!strat.setNextAlternative(false /* previous alternative parsed ok */)) {
					break;
				}
			} catch (BacktrackException exception) {
				if (!strat.setNextAlternative(true /* previous alternative failed to parse */)) {
					if (lastToken == null)
						throw exception;

					backup(lastToken);
					break;
				}
			}
			backup(mark);
		}
		return variants != null ? variants : singleResult;
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
		List<IASTAttributeSpecifier> attributes = attributes();

		switch (lookaheadType(1)) {
		case IToken.t_asm:
			return asmDeclaration(); // Unlike C, asm is a part of C++
		case IToken.t_namespace:
			return namespaceDefinitionOrAlias();
		case IToken.t_using:
			return usingClause(attributes);
		case IToken.t_static_assert:
			return staticAssertDeclaration();
		case IToken.t_export:
		case IToken.t_template:
			return templateDeclaration(option);
		case IToken.t_extern:
			if (lookaheadType(2) == IToken.tSTRING)
				return linkageSpecification();
			if (lookaheadType(2) == IToken.t_template)
				return templateDeclaration(option);
			break;
		case IToken.t_static:
		case IToken.t_inline:
			if (lookaheadType(2) == IToken.t_namespace) {
				return namespaceDefinitionOrAlias();
			}
			break;
		case IToken.tSEMI:
			IToken token = consume();
			IASTSimpleDeclSpecifier declspec = nodeFactory.newSimpleDeclSpecifier();
			IASTSimpleDeclaration decl = nodeFactory.newSimpleDeclaration(declspec);
			((ASTNode) declspec).setOffsetAndLength(token.getOffset(), 0);
			((ASTNode) decl).setOffsetAndLength(token.getOffset(), token.getLength());
			return decl;
		case IToken.t_public:
		case IToken.t_protected:
		case IToken.t_private:
			if (option == DeclarationOptions.CPP_MEMBER) {
				token = consume();
				int key = token.getType();
				int endOffset = consume(IToken.tCOLON).getEndOffset();
				ICPPASTVisibilityLabel label = ((ICPPNodeFactory) nodeFactory).newVisibilityLabel(visibilityFrom(key));
				setRange(label, token.getOffset(), endOffset);
				return label;
			}
			break;
		}

		try {
			return simpleDeclaration(option, attributes);
		} catch (BacktrackException exception) {
			if (option != DeclarationOptions.CPP_MEMBER || declarationMark == null)
				throw exception;
			BacktrackException orig = new BacktrackException(exception); // copy the exception
			IToken mark = mark();
			backup(declarationMark);
			try {
				return usingDeclaration(declarationMark.getOffset());
			} catch (BacktrackException other) {
				backup(mark);
				throw orig; // throw original exception;
			}
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
		} catch (BacktrackException exception) {
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

	@Override
	protected IASTDeclarator initDeclarator(IASTDeclSpecifier specifier, DeclarationOptions option)
			throws EndOfFileException, BacktrackException, FoundAggregateInitializer {
		final IToken mark = mark();
		IASTDeclarator destructor = null;
		IToken end1 = null;
		IASTDeclarator dtor2 = null;
		BacktrackException back = null;
		try {
			destructor = initDeclarator(DeclaratorStrategy.PREFER_METHOD, specifier, option);
			verifyDeclarator(specifier, destructor, option);

			int type = lookaheadTypeWithEndOfFile(1);
			switch (type) {
			case 0:
				return destructor;

			case IToken.tLBRACE:
				if (option.fCanBeFollowedByBrace
						|| ASTQueries.findTypeRelevantDeclarator(destructor) instanceof IASTFunctionDeclarator)
					return destructor;

				destructor = null;
				throwBacktrack(lookahead(1));
				break;

			case IToken.tCOLON:
				// a colon can be used after a type-id in a conditional expression
				if (option != DeclarationOptions.CPP_MEMBER && option != DeclarationOptions.GLOBAL)
					break;
				//$FALL-THROUGH$
			case IToken.t_throw:
			case IToken.t_try:
			case IToken.t_const:
			case IToken.t_volatile:
			case IToken.tASSIGN: // defaulted or deleted function definition
				if (option == DeclarationOptions.TYPEID_TRAILING_RETURN_TYPE
						|| ASTQueries.findTypeRelevantDeclarator(destructor) instanceof IASTFunctionDeclarator) {
					return destructor;
				} else {
					destructor = null;
					throwBacktrack(lookahead(1));
				}
			}

			if (!(destructor instanceof IASTFunctionDeclarator))
				return destructor;

			end1 = lookahead(1);
		} catch (BacktrackException exception) {
			back = exception;
		}

		if (!option.fAllowCtorStyleInitializer || !canHaveConstructorInitializer(specifier, destructor)) {
			if (back != null)
				throw back;
			return destructor;
		}

		backup(mark);
		try {
			dtor2 = initDeclarator(DeclaratorStrategy.PREFER_NESTED, specifier, option);
			if (destructor == null) {
				return dtor2;
			}
		} catch (BacktrackException exception) {
			if (destructor != null) {
				backup(end1);
				return destructor;
			}
			throw exception;
		}

		// we have an ambiguity
		if (end1 != null && lookahead(1).getEndOffset() != end1.getEndOffset()) {
			backup(end1);
			return destructor;
		}

		if (methodBodyCount != 0) {
			// prefer the variable prototype:
			IASTDeclarator h = destructor;
			destructor = dtor2;
			dtor2 = h;
		}
		CPPASTAmbiguousDeclarator dtor = new CPPASTAmbiguousDeclarator(destructor, dtor2);
		dtor.setOffsetAndLength((ASTNode) destructor);
		return dtor;
	}

	@Override
	protected IASTCompoundStatement methodBody() throws EndOfFileException, BacktrackException {
		methodBodyCount++;
		try {
			return super.methodBody();
		} finally {
			methodBodyCount--;
		}
	}

	@Override
	protected IASTInitializer optionalInitializer(IASTDeclarator dtor, DeclarationOptions options)
			throws EndOfFileException, BacktrackException {
		final int type = lookaheadTypeWithEndOfFile(1);

		// = initializer-clause
		if (type == IToken.tASSIGN) {
			return equalsInitalizerClause(lookaheadTypeWithEndOfFile(2) == IToken.tLBRACE && specifiesArray(dtor));
		}

		// braced-init-list
		if (options.fAllowBracedInitializer && type == IToken.tLBRACE) {
			return bracedInitList(false, false);
		}

		// (expression-list)
		if (options.fAllowCtorStyleInitializer && type == IToken.tLPAREN) {
			return constructorStyleInitializer(false);
		}
		return null;
	}

	@Override
	protected ICPPASTExpression expression() throws BacktrackException, EndOfFileException {
		return expression(ExprKind.EXPRESSION, BinaryExprCtx.EXPR_NOT_IN_TEMPLATE_ID, null, null);
	}

	@Override
	protected ICPPASTExpression constantExpression() throws BacktrackException, EndOfFileException {
		return expression(ExprKind.CONSTANT, BinaryExprCtx.EXPR_NOT_IN_TEMPLATE_ID, null, null);
	}

	@Override
	protected IASTExpression unaryExpression(CastExprCtx context, ITemplateIdStrategy strategy)
			throws BacktrackException, EndOfFileException {
		switch (lookaheadType(1)) {
		case IToken.tSTAR:
			return unaryExpression(IASTUnaryExpression.op_star, context, strategy);
		case IToken.tAMPER:
			return unaryExpression(IASTUnaryExpression.op_amper, context, strategy);
		case IToken.tPLUS:
			return unaryExpression(IASTUnaryExpression.op_plus, context, strategy);
		case IToken.tMINUS:
			return unaryExpression(IASTUnaryExpression.op_minus, context, strategy);
		case IToken.tNOT:
			return unaryExpression(IASTUnaryExpression.op_not, context, strategy);
		case IToken.tBITCOMPLEMENT:
			return unaryExpression(IASTUnaryExpression.op_tilde, context, strategy);
		case IToken.tINCR:
			return unaryExpression(IASTUnaryExpression.op_prefixIncr, context, strategy);
		case IToken.tDECR:
			return unaryExpression(IASTUnaryExpression.op_prefixDecr, context, strategy);
		case IToken.t_new:
			return newExpression();
		case IToken.t_delete:
			return deleteExpression();
		case IToken.tCOLONCOLON:
			switch (lookaheadType(2)) {
			case IToken.t_new:
				return newExpression();
			case IToken.t_delete:
				return deleteExpression();
			default:
				return postfixExpression(context, strategy);
			}
		case IToken.t_sizeof:
			if (lookaheadTypeWithEndOfFile(2) == IToken.tELLIPSIS) {
				int offset = consume().getOffset(); // sizeof
				consume(); // ...
				return parseTypeIDInParenthesisOrUnaryExpression(true, offset,
						IASTTypeIdExpression.op_sizeofParameterPack, IASTUnaryExpression.op_sizeofParameterPack,
						context, strategy);
			}
			return parseTypeIDInParenthesisOrUnaryExpression(false, consume().getOffset(),
					IASTTypeIdExpression.op_sizeof, IASTUnaryExpression.op_sizeof, context, strategy);
		case IToken.t_alignof:
			return parseTypeIDInParenthesisOrUnaryExpression(false, consume().getOffset(),
					IASTTypeIdExpression.op_alignof, IASTUnaryExpression.op_alignOf, context, strategy);

		default:
			return postfixExpression(context, strategy);
		}
	}

	@Override
	protected IASTExpression primaryExpression(CastExprCtx ctx, ITemplateIdStrategy strat)
			throws BacktrackException, EndOfFileException {
		IToken token = null;
		IASTLiteralExpression literalExpr = null;
		IASTLiteralExpression literalExprWithRange = null;

		switch (lookaheadType(1)) {
		case IToken.tINTEGER:
			token = consume();
			literalExpr = ((ICPPNodeFactory) nodeFactory).newLiteralExpression(
					IASTLiteralExpression.lk_integer_constant, token.getImage(), additionalNumericalSuffixes);
			literalExprWithRange = setRange(literalExpr, token.getOffset(), token.getEndOffset());
			break;
		case IToken.tFLOATINGPT:
			token = consume();
			literalExpr = ((ICPPNodeFactory) nodeFactory).newLiteralExpression(IASTLiteralExpression.lk_float_constant,
					token.getImage(), additionalNumericalSuffixes);
			literalExprWithRange = setRange(literalExpr, token.getOffset(), token.getEndOffset());
			break;
		case IToken.tSTRING:
		case IToken.tLSTRING:
		case IToken.tUTF16STRING:
		case IToken.tUTF32STRING:
		case IToken.tUSER_DEFINED_STRING_LITERAL:
			literalExprWithRange = stringLiteral();
			break;
		case IToken.tCHAR:
		case IToken.tLCHAR:
		case IToken.tUTF16CHAR:
		case IToken.tUTF32CHAR:
		case IToken.tUSER_DEFINED_CHAR_LITERAL:
			token = consume();
			literalExpr = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_char_constant, token.getImage());
			literalExprWithRange = setRange(literalExpr, token.getOffset(), token.getEndOffset());
			break;
		case IToken.t_false:
			token = consume();
			literalExpr = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_false, token.getImage());
			return setRange(literalExpr, token.getOffset(), token.getEndOffset());
		case IToken.t_true:
			token = consume();
			literalExpr = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_true, token.getImage());
			return setRange(literalExpr, token.getOffset(), token.getEndOffset());
		case IToken.t_nullptr:
			token = consume();
			literalExpr = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_nullptr, token.getImage());
			return setRange(literalExpr, token.getOffset(), token.getEndOffset());

		case IToken.t_this:
			token = consume();
			literalExpr = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_this, token.getImage());
			return setRange(literalExpr, token.getOffset(), token.getEndOffset());
		case IToken.tLPAREN:
			token = consume();
			int finalOffset = 0;
			IASTExpression left = expression(ExprKind.EXPRESSION, BinaryExprCtx.EXPR_IN_PRIMARY_EXPRESSION, null, null); // instead of expression(), to keep the stack smaller
			switch (lookaheadType(1)) {
			case IToken.tRPAREN:
			case IToken.tEOC:
				finalOffset = consume().getEndOffset();
				break;
			default:
				throwBacktrack(lookahead(1));
			}
			if (left instanceof ICPPASTFoldExpression || left instanceof IASTProblemExpression) {
				return setRange(left, token.getOffset(), finalOffset);
			} else {
				return buildUnaryExpression(IASTUnaryExpression.op_bracketedPrimary, left, token.getOffset(),
						finalOffset);
			}
		case IToken.tIDENTIFIER:
		case IToken.tCOLONCOLON:
		case IToken.t_operator:
		case IToken.tCOMPLETION:
		case IToken.tBITCOMPLEMENT:
		case IToken.t_decltype: {
			IASTName name = qualifiedName(ctx, strat);
			// A qualified-name's last name can sometimes be empty in a declaration (e.g. in "int A::*x",
			// "A::" is a valid qualified-name with an empty last name), but not in an expression
			// (unless we are invoking code completion at the '::').
			if (name.getLookupKey().length == 0 && lookaheadType(1) != IToken.tEOC)
				throwBacktrack(lookahead(1));
			IASTIdExpression idExpression = nodeFactory.newIdExpression(name);
			return setRange(idExpression, name);
		}
		case IToken.tLBRACKET:
			return lambdaExpression();

		default:
			IToken lookahead = lookahead(1);
			int startingOffset = lookahead.getOffset();
			throwBacktrack(startingOffset, lookahead.getLength());
			return null;
		}

		// if (supportUserDefinedLiterals) {
		IToken lookahead = lookahead(1);
		int offset = ((ASTNode) literalExprWithRange).getOffset();
		int length = ((ASTNode) literalExprWithRange).getLength();
		if (isIdentifierOrKeyword(lookahead)) {
			if ((offset + length) != lookahead.getOffset()) {
				return literalExprWithRange;
			}
			consume();
			((CPPASTLiteralExpression) literalExprWithRange).setSuffix(lookahead.getCharImage());
			setRange(literalExprWithRange, offset, lookahead.getEndOffset());
		}
		// }

		return literalExprWithRange;
	}

	@Override
	protected IASTStatement statement() throws EndOfFileException, BacktrackException {
		int startOffset = lookahead(1).getOffset();
		List<IASTAttributeSpecifier> attributes = attributes();

		IASTStatement statement = null;
		switch (lookaheadType(1)) {
		// labeled statements
		case IToken.t_case:
			statement = parseCaseStatement();
			break;
		case IToken.t_default:
			statement = parseDefaultStatement();
			break;
		// compound statement
		case IToken.tLBRACE:
			statement = parseCompoundStatement();
			break;
		// selection statement
		case IToken.t_if:
			statement = parseIfStatement();
			break;
		case IToken.t_switch:
			statement = parseSwitchStatement();
			break;
		// iteration statements
		case IToken.t_while:
			statement = parseWhileStatement();
			break;
		case IToken.t_do:
			statement = parseDoStatement();
			break;
		case IToken.t_for:
			statement = parseForStatement();
			break;
		// jump statement
		case IToken.t_break:
			statement = parseBreakStatement();
			break;
		case IToken.t_continue:
			statement = parseContinueStatement();
			break;
		case IToken.t_return:
			statement = parseReturnStatement();
			break;
		case IToken.t_goto:
			statement = parseGotoStatement();
			break;
		case IToken.tSEMI:
			statement = parseNullStatement();
			break;
		case IToken.t_try:
			statement = parseTryStatement();
			break;
		default:
			// can be many things:
			// label
			if (lookaheadType(1) == IToken.tIDENTIFIER && lookaheadType(2) == IToken.tCOLON) {
				statement = parseLabelStatement();
				break;
			}

			return parseDeclarationOrExpressionStatement(attributes);
		}
		addAttributeSpecifiers(attributes, statement);

		int endOffset = calculateEndOffset(statement);
		return setRange(statement, startOffset, endOffset);
	}

	@Override
	protected IASTExpression buildBinaryExpression(int operator, IASTExpression expr, IASTInitializerClause clause,
			int lastOffset) {
		IASTBinaryExpression result = ((ICPPNodeFactory) nodeFactory).newBinaryExpression(operator, expr, clause);
		int offset = ((ASTNode) expr).getOffset();
		((ASTNode) result).setOffsetAndLength(offset, lastOffset - offset);
		return result;
	}

	@Override
	protected IASTAmbiguousExpression createAmbiguousExpression() {
		return new CPPASTAmbiguousExpression();
	}

	@Override
	protected IASTAmbiguousStatement createAmbiguousStatement() {
		return new CPPASTAmbiguousStatement();
	}

	@Override
	protected IASTAmbiguousExpression createAmbiguousBinaryVsCastExpression(IASTBinaryExpression binary,
			IASTCastExpression castExpr) {
		return new CPPASTAmbiguousBinaryVsCastExpression(binary, castExpr);
	}

	@Override
	protected IASTAmbiguousExpression createAmbiguousCastVsMethodCallExpression(IASTCastExpression castExpr,
			IASTFunctionCallExpression methodCall) {
		return new CPPASTAmbiguousCastVsFunctionCallExpression(castExpr, methodCall);
	}

	@Override
	protected IASTTypeId typeID(DeclarationOptions option) throws EndOfFileException, BacktrackException {
		return typeID(option, null);
	}

	@Override
	protected IASTExpression expressionWithOptionalTrailingEllipsis() throws BacktrackException, EndOfFileException {
		IASTExpression result = expression();
		if (lookaheadType(1) == IToken.tELLIPSIS) {
			result = createPackExpansion(result, consume());
		}
		return result;
	}

	@Override
	protected IASTTypeId typeIDWithOptionalTrailingEllipsis(DeclarationOptions option)
			throws EndOfFileException, BacktrackException {
		ICPPASTTypeId result = (ICPPASTTypeId) typeID(option);
		if (lookaheadType(1) == IToken.tELLIPSIS) {
			createPackExpansion(result, consume());
		}
		return result;
	}

	/**
	 * This function parses a declaration specifier sequence, as according to
	 * the ANSI C++ specification.
	 * declSpecifier :
	 * 		"register" | "static" | "extern" | "mutable" |
	 * 		"inline" | "virtual" | "explicit" |
	 * 		"typedef" | "friend" | "constexpr" |
	 * 		"const" | "volatile" |
	 * 		"short" | "long" | "signed" | "unsigned" | "int" |
	 * 		"char" | "wchar_t" | "bool" | "float" | "double" | "void" |
	 *	  "auto" |
	 * 		("typename")? name |
	 * 		{ "class" | "struct" | "union" } classSpecifier |
	 * 		{"enum"} enumSpecifier
	 */
	@Override
	protected Declaration declarationSpecifierSequence(DeclarationOptions option, ITemplateIdStrategy strat)
			throws BacktrackException, EndOfFileException {
		return declarationSpecifierSequence(option, false, strat);
	}

	@Override
	protected IASTName identifier() throws EndOfFileException, BacktrackException {
		switch (lookaheadType(1)) {
		case IToken.tIDENTIFIER:
		case IToken.tCOMPLETION:
		case IToken.tEOC:
			return buildName(-1, consume(), false);
		}

		throw backtrack;
	}

	@Override
	protected IASTAlignmentSpecifier createAmbiguousAlignmentSpecifier(IASTAlignmentSpecifier expression,
			IASTAlignmentSpecifier typeID) {
		return new CPPASTAmbiguousAlignmentSpecifier(expression, typeID);
	}

	@Override
	protected ICPPASTAttribute singleAttribute() throws EndOfFileException, BacktrackException {
		// Get an identifier including keywords
		IToken nameToken = identifierOrKeyword();
		IToken scopeToken = null;
		IASTToken argumentClause = null;
		boolean packExpansion = false;

		// Check for scoped attribute
		if (lookaheadType(1) == IToken.tCOLONCOLON) {
			consume();
			scopeToken = nameToken;
			nameToken = identifierOrKeyword();
		}
		int endOffset = nameToken.getEndOffset();

		// Check for arguments
		if (lookaheadType(1) == IToken.tLPAREN) {
			IToken token = consume();
			argumentClause = balancedTokenSequence(token.getEndOffset(), IToken.tRPAREN);
			endOffset = consume(IToken.tRPAREN).getEndOffset();
		}

		// Check for pack expansion
		if (lookaheadType(1) == IToken.tELLIPSIS) {
			packExpansion = true;
			endOffset = consumeOrEndOfCompletion(IToken.tELLIPSIS).getEndOffset();
		}
		char[] attributeName = nameToken.getCharImage();
		char[] scopeName = scopeToken != null ? scopeToken.getCharImage() : null;
		ICPPASTAttribute result = ((ICPPNodeFactory) nodeFactory).newAttribute(attributeName, scopeName, argumentClause,
				packExpansion);
		setRange(result, nameToken.getOffset(), endOffset);
		return result;
	}

	@Override
	protected boolean isLegalWithoutDestructor(IASTDeclSpecifier declSpec) {
		if (declSpec instanceof IASTElaboratedTypeSpecifier) {
			return ((IASTElaboratedTypeSpecifier) declSpec).getKind() != IASTElaboratedTypeSpecifier.k_enum;
		} else if (declSpec instanceof ICPPASTNamedTypeSpecifier && ((ICPPASTNamedTypeSpecifier) declSpec).isFriend()) {
			return true;
		}
		return super.isLegalWithoutDestructor(declSpec);
	}

	@Override
	protected IASTStatement parseReturnStatement() throws EndOfFileException, BacktrackException {
		final int offset = consume(IToken.t_return).getOffset(); // t_return

		// Optional expression
		IASTInitializerClause expr = null;
		final int type = lookaheadType(1);
		if (type == IToken.tLBRACE) {
			expr = bracedInitList(true, false);
		} else if (type != IToken.tSEMI) {
			expr = expression();
		}
		// Semicolon
		final int endOffset = consumeOrEndOfCompletion(IToken.tSEMI).getEndOffset();

		return setRange(((ICPPNodeFactory) nodeFactory).newReturnStatement(expr), offset, endOffset);
	}

	protected ICPPASTAmbiguousTemplateArgument createAmbiguousTemplateArgument() {
		return new CPPASTAmbiguousTemplateArgument();
	}

	protected IASTStatement parseSwitchStatement() throws EndOfFileException, BacktrackException {
		int startOffset;
		startOffset = consume().getOffset();
		consume(IToken.tLPAREN);

		ICPPASTSwitchStatement switchStatement = ((ICPPNodeFactory) nodeFactory).newSwitchStatement();
		// init-statement
		IToken mark = mark();
		try {
			IASTStatement statement = initStatement();
			switchStatement.setInitializerStatement(statement);
		} catch (BacktrackException exception) {
			backup(mark);
		}

		IASTNode switchCondition = cppStyleCondition(IToken.tRPAREN);
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
		((ASTNode) switchStatement).setOffsetAndLength(startOffset,
				(switchBody != null ? calculateEndOffset(switchBody) : lookahead(1).getEndOffset()) - startOffset);
		if (switchCondition instanceof IASTExpression) {
			switchStatement.setControllerExpression((IASTExpression) switchCondition);
		} else if (switchCondition instanceof IASTDeclaration) {
			switchStatement.setControllerDeclaration((IASTDeclaration) switchCondition);
		}

		if (switchBody != null) {
			switchStatement.setBody(switchBody);
		}

		return switchStatement;
	}

	protected IASTStatement parseTryStatement() throws EndOfFileException, BacktrackException {
		int startOffset = consume().getOffset();
		IASTStatement tryBlock = compoundStatement();
		List<ICPPASTCatchHandler> catchHandlers = new ArrayList<>(DEFAULT_CATCH_HANDLER_LIST_SIZE);
		catchHandlerSequence(catchHandlers);
		ICPPASTTryBlockStatement tryStatement = ((ICPPNodeFactory) nodeFactory).newTryBlockStatement(tryBlock);
		((ASTNode) tryStatement).setOffset(startOffset);

		for (int i = 0; i < catchHandlers.size(); ++i) {
			ICPPASTCatchHandler handler = catchHandlers.get(i);
			tryStatement.addCatchHandler(handler);
			((ASTNode) tryStatement).setLength(calculateEndOffset(handler) - startOffset);
		}
		return tryStatement;
	}

	protected IASTStatement parseIfStatement() throws EndOfFileException, BacktrackException {
		ICPPASTIfStatement result = null;
		ICPPASTIfStatement ifStatement = null;
		int start = lookahead(1).getOffset();
		ifLoop: for (;;) {
			int so = consume(IToken.t_if).getOffset();
			ICPPASTIfStatement newIfStatement = ((ICPPNodeFactory) nodeFactory).newIfStatement();
			// constexpr if
			if (lookaheadType(1) == IToken.t_constexpr) {
				consume();
				newIfStatement.setIsConstexpr(true);
			}
			consume(IToken.tLPAREN);
			// init-statement
			IToken mark = mark();
			try {
				IASTStatement statement = initStatement();
				newIfStatement.setInitializerStatement(statement);
			} catch (BacktrackException e) {
				backup(mark);
			}
			// condition
			IASTNode condition = cppStyleCondition(IToken.tRPAREN);
			if (lookaheadType(1) == IToken.tEOC) {
				// Completing in the condition
				if (condition instanceof IASTExpression)
					newIfStatement.setConditionExpression((IASTExpression) condition);
				else if (condition instanceof IASTDeclaration)
					newIfStatement.setConditionDeclaration((IASTDeclaration) condition);

				if (ifStatement != null) {
					ifStatement.setElseClause(newIfStatement);
				}
				return result != null ? result : newIfStatement;
			}
			consume(IToken.tRPAREN);

			IASTStatement thenClause = statement();
			((ASTNode) newIfStatement).setOffset(so);
			if (condition != null && (condition instanceof IASTExpression || condition instanceof IASTDeclaration))
			// shouldn't be possible but failure in condition() makes it so
			{
				if (condition instanceof IASTExpression)
					newIfStatement.setConditionExpression((IASTExpression) condition);
				else if (condition instanceof IASTDeclaration)
					newIfStatement.setConditionDeclaration((IASTDeclaration) condition);
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

	protected IASTStatement parseForStatement() throws EndOfFileException, BacktrackException {
		final int offset = consume(IToken.t_for).getOffset();
		consume(IToken.tLPAREN);
		IToken mark = mark();
		IASTStatement forStatement;
		try {
			forStatement = startRangeBasedForLoop();
		} catch (BacktrackException exception) {
			backup(mark);
			forStatement = startRegularForLoop();
		}
		mark = null;
		int endOffset = consumeOrEndOfCompletion(IToken.tRPAREN).getEndOffset();

		if (lookaheadType(1) != IToken.tEOC) {
			IASTStatement body = statement();
			if (forStatement instanceof ICPPASTRangeBasedForStatement) {
				((ICPPASTRangeBasedForStatement) forStatement).setBody(body);
			} else {
				((IASTForStatement) forStatement).setBody(body);
			}
			endOffset = calculateEndOffset(body);
		}
		return setRange(forStatement, offset, endOffset);
	}

	protected IASTNode cppStyleCondition(int expectToken) throws BacktrackException, EndOfFileException {
		IASTExpression expr = null;
		IASTSimpleDeclaration declaration = null;
		IToken end = null;

		IToken mark = mark();
		try {
			declaration = simpleSingleDeclaration(DeclarationOptions.CONDITION);
			end = lookahead(1);
			final int type = end.getType();
			if (type != expectToken && type != IToken.tEOC) {
				end = null;
				declaration = null;
			}
		} catch (BacktrackException exception) {
		}

		backup(mark);
		try {
			expr = expression();

			final IToken end2 = lookahead(1);
			final int type = end2.getType();
			if (type != expectToken && type != IToken.tEOC) {
				throwBacktrack(end2);
			}
			if (end == null)
				return expr;

			final int endOffset = end.getOffset();
			final int endOffset2 = end2.getOffset();
			if (endOffset == endOffset2) {
				CPPASTAmbiguousCondition ambig = new CPPASTAmbiguousCondition(expr, declaration);
				setRange(ambig, expr);
				return ambig;
			}

			if (endOffset < endOffset2)
				return expr;
		} catch (BacktrackException exception) {
			if (end == null) {
				if (expectToken == IToken.tRPAREN) {
					backup(mark);
					return skipProblemConditionInParenthesis(mark.getOffset());
				}
				throw exception;
			}
		}
		backup(end);
		return declaration;
	}

	/**
	 * Parse a declarator, as according to the ANSI C++ specification.
	 * declarator : (ptrOperator)* directDeclarator
	 * directDeclarator :
	 *	declaratorId |
	 *	directDeclarator "(" parameterDeclarationClause ")" (cvQualifier)* (exceptionSpecification)* |
	 *	directDeclarator "[" (constantExpression)? "]" |
	 *	"(" declarator")" |
	 *	directDeclarator "(" parameterDeclarationClause ")" (oldKRParameterDeclaration)*
	 *
	 * declaratorId : name
	 * @return declarator that this parsing produced.
	 * @throws BacktrackException
	 *			 request a backtrack
	 */
	protected IASTDeclarator declarator(DeclaratorStrategy strategy, DeclarationOptions option)
			throws EndOfFileException, BacktrackException {
		final int startingOffset = lookahead(1).getOffset();
		int endOffset = startingOffset;

		List<? extends IASTPointerOperator> pointerOperators = consumePointerOperators();
		if (pointerOperators != null) {
			endOffset = calculateEndOffset(pointerOperators.get(pointerOperators.size() - 1));
		}

		List<IASTAttributeSpecifier> attributes = new ArrayList<>();

		// Look for identifier or nested declarator
		boolean hasEllipsis = false;
		if (option.fAllowParameterPacks && lookaheadType(1) == IToken.tELLIPSIS) {
			consume();
			hasEllipsis = true;
		}
		final int type = lookaheadType(1);
		switch (type) {
		case IToken.tBITCOMPLEMENT:
		case IToken.t_operator:
		case IToken.tCOLONCOLON:
		case IToken.tIDENTIFIER:
		case IToken.tCOMPLETION:
			if (option.fRequireAbstract) {
				// We might have a virt-specifier following a type-id in a trailing-return-type.
				ContextSensitiveTokenType contextSensitiveType = getContextSensitiveType(lookahead(1));
				if (contextSensitiveType == ContextSensitiveTokenType.OVERRIDE
						|| contextSensitiveType == ContextSensitiveTokenType.FINAL) {
					// In that case, we're done parsing the declarator of the type-id.
					break;
				} else {
					// Otherwise, we have what looks like a name, but we're not expecting one.
					throwBacktrack(lookahead(1));
				}
			}

			final IASTName declaratorName = !option.fRequireSimpleName ? qualifiedName() : identifier();
			endOffset = calculateEndOffset(declaratorName);
			return declarator(pointerOperators, hasEllipsis, declaratorName, null, startingOffset, endOffset, strategy,
					option, attributes);
		}

		if (type == IToken.tLPAREN) {
			IASTDeclarator cand1 = null;
			IToken cand1End = null;
			// try an abstract method declarator
			if (option.fAllowAbstract && option.fAllowFunctions) {
				final IToken mark = mark();
				try {
					cand1 = declarator(pointerOperators, hasEllipsis, nodeFactory.newName(), null, startingOffset,
							endOffset, strategy, option, attributes);
					if (option.fRequireAbstract || !option.fAllowNested || hasEllipsis)
						return cand1;

					cand1End = lookahead(1);
				} catch (BacktrackException exception) {
				}
				backup(mark);
			}

			// type-ids for new or operator-id:
			if (!option.fAllowNested || hasEllipsis) {
				if (option.fAllowAbstract) {
					return declarator(pointerOperators, hasEllipsis, nodeFactory.newName(), null, startingOffset,
							endOffset, strategy, option, attributes);
				}
				throwBacktrack(lookahead(1));
			}

			// try a nested declarator
			try {
				consume();
				if (lookaheadType(1) == IToken.tRPAREN)
					throwBacktrack(lookahead(1));

				final IASTDeclarator nested = declarator(DeclaratorStrategy.PREFER_METHOD, option);
				endOffset = consume(IToken.tRPAREN).getEndOffset();
				final IASTDeclarator cand2 = declarator(pointerOperators, hasEllipsis, nodeFactory.newName(), nested,
						startingOffset, endOffset, strategy, option, attributes);
				if (cand1 == null || cand1End == null)
					return cand2;
				final IToken cand2End = lookahead(1);
				if (cand1End == cand2End) {
					CPPASTAmbiguousDeclarator result = new CPPASTAmbiguousDeclarator(cand1, cand2);
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
			// bit-fields may be abstract
			if (!option.fAllowBitField || lookaheadType(1) != IToken.tCOLON)
				throwBacktrack(lookahead(1));
		}
		return declarator(pointerOperators, hasEllipsis, nodeFactory.newName(), null, startingOffset, endOffset,
				strategy, option, attributes);
	}

	/**
	 * Parse a class/struct/union definition. classSpecifier : classKey attribute-specifier-sequence? name
	 * (baseClause)? "{" (memberSpecification)* "}"
	 *
	 * @throws BacktrackException
	 *			 request a backtrack
	 */
	protected ICPPASTCompositeTypeSpecifier classSpecifier() throws BacktrackException, EndOfFileException {
		int classKind = 0;
		IToken mark = mark();
		final int offset = mark.getOffset();

		// class key
		switch (lookaheadType(1)) {
		case IToken.t_class:
			consume();
			classKind = ICPPASTCompositeTypeSpecifier.k_class;
			break;
		case IToken.t_struct:
			consume();
			classKind = IASTCompositeTypeSpecifier.k_struct;
			break;
		case IToken.t_union:
			consume();
			classKind = IASTCompositeTypeSpecifier.k_union;
			break;
		default:
			throwBacktrack(mark);
			return null; // line is never reached, hint for the parser
		}

		List<IASTAttributeSpecifier> attributes = attributes();

		// class name
		IASTName name = null;
		if (lookaheadType(1) == IToken.tIDENTIFIER) {
			name = qualifiedName();
		} else {
			name = nodeFactory.newName();
		}

		ICPPASTCompositeTypeSpecifier astClassSpecifier = ((ICPPNodeFactory) nodeFactory)
				.newCompositeTypeSpecifier(classKind, name);
		addAttributeSpecifiers(attributes, astClassSpecifier);

		// class virtual specifier
		if (lookaheadType(1) == IToken.tIDENTIFIER) {
			classVirtualSpecifier(astClassSpecifier);
		}

		// base clause
		if (lookaheadType(1) == IToken.tCOLON) {
			try {
				baseClause(astClassSpecifier);
			} catch (BacktrackException exception) {
				// Couldn't parse a base-clause.
				// Backtrack and try an elaborated-type-specifier instead.
				backup(mark);
				throw exception;
			}
			// content assist within the base-clause
			if (lookaheadType(1) == IToken.tEOC) {
				return astClassSpecifier;
			}
		}

		if (lookaheadType(1) != IToken.tLBRACE) {
			IToken errorPoint = lookahead(1);
			backup(mark);
			throwBacktrack(errorPoint);
		}
		mark = null; // don't hold on to tokens while parsing the members.
		final char[] outerName = currentClassName;
		currentClassName = name.getLookupKey();

		try {
			declarationListInBraces(astClassSpecifier, offset, DeclarationOptions.CPP_MEMBER);
		} finally {
			currentClassName = outerName;
		}
		return astClassSpecifier;
	}

	/**
	 * Parse a base clause for a class specification.
	 * base-clause:
	 *	: base-specifier-list
	 * base-specifier-list:
	 *	base-specifier
	 *	base-specifier-list, base-specifier
	 */
	private void baseClause(ICPPASTCompositeTypeSpecifier astClassSpec) throws EndOfFileException, BacktrackException {
		consume(IToken.tCOLON);
		for (;;) {
			ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier specifier = baseSpecifier();
			astClassSpec.addBaseSpecifier(specifier);

			if (lookaheadType(1) == IToken.tELLIPSIS) {
				specifier.setIsPackExpansion(true);
				adjustEndOffset(specifier, consume().getEndOffset());
			}

			if (lookaheadType(1) != IToken.tCOMMA) {
				return;
			}

			consume();
		}
	}

	/**
	 * base-specifier:
	 *	::? nested-name-specifier? class-name
	 *	virtual access-specifier? ::? nested-name-specifier? class-name
	 *	access-specifier virtual? ::? nested-name-specifier? class-name
	 *
	 * access-specifier: private | protected | public
	 * @return
	 */
	private ICPPASTBaseSpecifier baseSpecifier() throws EndOfFileException, BacktrackException {
		int startOffset = lookahead(1).getOffset();
		boolean isVirtual = false;
		int visibility = 0;
		ICPPASTNameSpecifier nameSpec = null;
		loop: for (;;) {
			switch (lookaheadType(1)) {
			case IToken.t_virtual:
				isVirtual = true;
				consume();
				break;
			case IToken.t_public:
				visibility = ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.v_public;
				consume();
				break;
			case IToken.t_protected:
				visibility = ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.v_protected;
				consume();
				break;
			case IToken.t_private:
				visibility = ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.v_private;
				consume();
				break;
			default:
				break loop;
			}
		}
		nameSpec = nameSpecifier();
		ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier baseSpecifier = ((ICPPNodeFactory) nodeFactory)
				.newBaseSpecifier(nameSpec, visibility, isVirtual);
		setRange(baseSpecifier, startOffset, calculateEndOffset(nameSpec));
		return baseSpecifier;
	}

	/**
	 * Parses a declaration with the given options.
	 */
	protected IASTDeclaration simpleDeclaration(DeclarationOptions declOption, List<IASTAttributeSpecifier> attributes)
			throws BacktrackException, EndOfFileException {
		if (lookaheadType(1) == IToken.tLBRACE)
			throwBacktrack(lookahead(1));

		final int firstOffset = attributesStartOffset(lookahead(1).getOffset(), attributes);
		int endOffset = firstOffset;
		boolean insertSemi = false;

		IASTDeclSpecifier declSpec = null;
		IASTDeclarator dtor = null;
		IASTDeclSpecifier altDeclSpec = null;
		IASTDeclarator altDtor = null;
		IToken markBeforeDtor = null;
		boolean isAtStartOfStructuredBinding = false;
		try {
			Declaration decl = initDeclSpecifierSequenceDeclarator(declOption, true);
			markBeforeDtor = decl.declaratorToken;
			declSpec = decl.leftSpecifier;
			dtor = decl.leftDeclarator;
			altDeclSpec = decl.rightSpecifier;
			altDtor = decl.rightDeclarator;
			isAtStartOfStructuredBinding = decl.isAtStartOfStructuredBinding;
		} catch (FoundAggregateInitializer lie) {
			declSpec = lie.specifier;
			// scalability: don't keep references to tokens, initializer may be large
			declarationMark = null;
			dtor = addInitializer(lie, declOption);
		} catch (BacktrackException exception) {
			IASTNode node = exception.getNodeBeforeProblem();
			if (node instanceof IASTDeclSpecifier && isLegalWithoutDestructor((IASTDeclSpecifier) node)) {
				IASTSimpleDeclaration d = nodeFactory.newSimpleDeclaration((IASTDeclSpecifier) node);
				setRange(d, node);
				throwBacktrack(exception.getProblem(), d);
			}
			throw exception;
		}

		if (isAtStartOfStructuredBinding && declSpec instanceof ICPPASTSimpleDeclSpecifier) {
			ICPPASTSimpleDeclSpecifier simpleDeclSpecifier = (ICPPASTSimpleDeclSpecifier) declSpec;
			return structuredBinding(simpleDeclSpecifier, attributes);
		}

		IASTDeclarator[] declarators = IASTDeclarator.EMPTY_DECLARATOR_ARRAY;
		if (dtor != null) {
			declarators = new IASTDeclarator[] { dtor };
			if (!declOption.fSingleDtor) {
				while (lookaheadTypeWithEndOfFile(1) == IToken.tCOMMA) {
					consume();
					try {
						dtor = initDeclarator(declSpec, declOption);
					} catch (FoundAggregateInitializer e) {
						// scalability: don't keep references to tokens, initializer may be large
						declarationMark = null;
						markBeforeDtor = null;
						dtor = addInitializer(e, declOption);
					}
					declarators = ArrayUtil.append(IASTDeclarator.class, declarators, dtor);
				}
				declarators = ArrayUtil.removeNulls(IASTDeclarator.class, declarators);
			}
		}

		final int type = lookaheadTypeWithEndOfFile(1);
		switch (type) {
		case IToken.tEOC:
			endOffset = figureEndOffset(declSpec, declarators);
			break;
		case IToken.tSEMI:
			endOffset = consume().getEndOffset();
			break;
		case IToken.tCOLON:
			if (declOption == DeclarationOptions.RANGE_BASED_FOR) {
				endOffset = figureEndOffset(declSpec, declarators);
				break;
			}
			//$FALL-THROUGH$
		case IToken.t_try:
		case IToken.tLBRACE:
		case IToken.tASSIGN: // defaulted or deleted function definition
			if (declarators.length != 1 || !declOption.fAllowFunctionDefinition)
				throwBacktrack(lookahead(1));

			dtor = declarators[0];
			if (altDeclSpec != null && altDtor != null && dtor != null
					&& !(ASTQueries.findTypeRelevantDeclarator(dtor) instanceof IASTFunctionDeclarator)) {
				declSpec = altDeclSpec;
				dtor = altDtor;
			}
			ICPPASTFunctionDefinition functionDefinition = methodDefinition(firstOffset, declSpec, dtor);
			addAttributeSpecifiers(attributes, functionDefinition);
			return functionDefinition;

		default:
			insertSemi = true;
			if (declOption == DeclarationOptions.LOCAL) {
				endOffset = figureEndOffset(declSpec, declarators);
				break;
			} else {
				if (isLegalWithoutDestructor(declSpec) && markBeforeDtor != null
						&& !isOnSameLine(calculateEndOffset(declSpec), markBeforeDtor.getOffset())) {
					backup(markBeforeDtor);
					declarators = IASTDeclarator.EMPTY_DECLARATOR_ARRAY;
					endOffset = calculateEndOffset(declSpec);
					break;
				}
				endOffset = figureEndOffset(declSpec, declarators);
				if (type == 0 || !isOnSameLine(endOffset, lookahead(1).getOffset())) {
					break;
				}
				if (declarators.length == 1 && declarators[0] instanceof IASTFunctionDeclarator) {
					break;
				}
			}
			throwBacktrack(lookahead(1));
		}

		// no function body

		final boolean isAmbiguous = altDeclSpec != null && altDtor != null && declarators.length == 1;
		IASTSimpleDeclaration simpleDeclaration;
		if (isAmbiguous) {
			// class C { C(T); };  // if T is a type this is a constructor, so
			// prefer the empty declspec, it shall be used if both variants show no problems
			simpleDeclaration = nodeFactory.newSimpleDeclaration(altDeclSpec);
			simpleDeclaration.addDeclarator(altDtor);
		} else {
			simpleDeclaration = nodeFactory.newSimpleDeclaration(declSpec);
			for (IASTDeclarator declarator : declarators) {
				simpleDeclaration.addDeclarator(declarator);
			}
		}

		setRange(simpleDeclaration, firstOffset, endOffset);
		if (isAmbiguous) {
			simpleDeclaration = new CPPASTAmbiguousSimpleDeclaration(simpleDeclaration, declSpec, dtor);
			setRange(simpleDeclaration, firstOffset, endOffset);
		}

		if (insertSemi) {
			IASTProblem problem = createProblem(IProblem.MISSING_SEMICOLON, endOffset - 1, 1);
			throwBacktrack(problem, simpleDeclaration);
		}
		addAttributeSpecifiers(attributes, simpleDeclaration);
		return simpleDeclaration;
	}

	/**
	 * Represents the amalgamation of template declarations, template
	 * instantiations and specializations in the ANSI C++ grammar.
	 * template-declaration: export? template < template-parameter-list > (No longer a part of C++)
	 * declaration explicit-instantiation: template declaration
	 * explicit-specialization: template <>declaration
	 * @param option
	 *
	 * @throws BacktrackException
	 *			 request for a backtrack
	 */
	protected IASTDeclaration templateDeclaration(DeclarationOptions option)
			throws EndOfFileException, BacktrackException {
		final int offset = lookahead(1).getOffset();
		int explicitInstMod = 0;
		switch (lookaheadType(1)) { /* No longer part of C++
									case IToken.t_export:
									exported = true;
									consume();
									break; */
		case IToken.t_extern:
			consume();
			explicitInstMod = ICPPASTExplicitTemplateInstantiation.EXTERN;
			break;
		}

		consume(IToken.t_template);

		if (lookaheadType(1) != IToken.tLT) {
			// explicit-instantiation
			IASTDeclaration declaration = declaration(option);
			ICPPASTExplicitTemplateInstantiation instantiation = ((ICPPNodeFactory) nodeFactory)
					.newExplicitTemplateInstantiation(declaration);
			instantiation.setModifier(explicitInstMod);
			setRange(instantiation, offset, calculateEndOffset(declaration));
			return instantiation;
		}

		// Modifiers for explicit instantiations
		if (explicitInstMod != 0) {
			throwBacktrack(lookahead(1));
		}
		consume(IToken.tLT);
		if (lookaheadType(1) == IToken.tGT) {
			// explicit-specialization
			consume();
			IASTDeclaration declaration = declaration(option);
			ICPPASTTemplateSpecialization templateSpecialization = ((ICPPNodeFactory) nodeFactory)
					.newTemplateSpecialization(declaration);
			setRange(templateSpecialization, offset, calculateEndOffset(declaration));
			return templateSpecialization;
		}

		List<ICPPASTTemplateParameter> parms = outerTemplateParameterList();
		if (lookaheadType(1) != IToken.tEOC) {
			consume(IToken.tGT, IToken.tGT_in_SHIFTR);
		}
		IASTDeclaration declaration = declaration(option);
		ICPPASTTemplateDeclaration templateDecl = ((ICPPNodeFactory) nodeFactory).newTemplateDeclaration(declaration);
		setRange(templateDecl, offset, calculateEndOffset(declaration));
		templateDecl.setExported(/*exported*/ false);
		for (int i = 0; i < parms.size(); ++i) {
			ICPPASTTemplateParameter parm = parms.get(i);
			templateDecl.addTemplateParameter(parm);
		}
		return templateDecl;
	}

	/**
	 * template-parameter-list: template-parameter template-parameter-list ,
	 * template-parameter template-parameter: type-parameter
	 * parameter-declaration type-parameter: class identifier? class identifier? =
	 * type-id typename identifier? typename identifier? = type-id template <
	 * template-parameter-list > class identifier? template <
	 * template-parameter-list > class identifier? = id-expression template-id:
	 * template-name < template-argument-list?> template-name: identifier
	 * template-argument-list: template-argument template-argument-list ,
	 * template-argument template-argument: assignment-expression type-id
	 * id-expression
	 *
	 * @throws BacktrackException
	 *			 request for a backtrack
	 */
	protected List<ICPPASTTemplateParameter> outerTemplateParameterList()
			throws BacktrackException, EndOfFileException {
		templateParameterListStrategy = new TemplateIdStrategy();
		try {
			List<ICPPASTTemplateParameter> result = new ArrayList<>(DEFAULT_PARAM_LIST_SIZE);
			IToken mark = mark();
			for (;;) {
				try {
					return templateParameterList(result);
				} catch (BacktrackException exception) {
					if (!templateParameterListStrategy
							.setNextAlternative(true /* previous alternative failed to parse */)) {
						templateParameterListStrategy = null;
						throw exception;
					}
					result.clear();
					backup(mark);
				}
			}
		} finally {
			templateParameterListStrategy = null;
		}
	}

	/**
	 * The merger of using-declaration and using-directive in ANSI C++ grammar.
	 * using-declaration: using typename? ::? nested-name-specifier
	 * unqualified-id ; using :: unqualified-id ; using-directive: using
	 * namespace ::? nested-name-specifier? namespace-name ;
	 *
	 * @throws BacktrackException
	 *			 request for a backtrack
	 */
	protected IASTDeclaration usingClause(List<IASTAttributeSpecifier> attributes)
			throws EndOfFileException, BacktrackException {
		final int offset = consume().getOffset();

		if (lookaheadType(1) == IToken.t_namespace) {
			// using-directive
			int endOffset = consume().getEndOffset();
			IASTName name = null;
			switch (lookaheadType(1)) {
			case IToken.tIDENTIFIER:
			case IToken.tCOLONCOLON:
			case IToken.tCOMPLETION:
				name = qualifiedName();
				break;
			default:
				throwBacktrack(offset, endOffset - offset);
			}

			switch (lookaheadType(1)) {
			case IToken.tSEMI:
			case IToken.tEOC:
				endOffset = consume().getEndOffset();
				break;
			default:
				throw backtrack;
			}
			ICPPASTUsingDirective directive = ((ICPPNodeFactory) nodeFactory).newUsingDirective(name);

			addAttributeSpecifiers(attributes, directive);

			return setRange(directive, offset, endOffset);
		}

		if (lookaheadType(1) == IToken.tIDENTIFIER && (lookaheadType(2) == IToken.tASSIGN
				|| (lookaheadType(2) == IToken.tLBRACKET && lookaheadType(3) == IToken.tLBRACKET))) {
			return aliasDeclaration(offset);
		}
		ICPPASTUsingDeclaration result = usingDeclaration(offset);
		return result;
	}

	/**
	 * Serves as the namespace declaration portion of the ANSI C++ grammar.
	 * namespace-definition: namespace identifier { namespace-body } | namespace {
	 * namespace-body } namespace-body: declaration-seq?
	 *
	 * @throws BacktrackException
	 *			 request a backtrack
	 */
	protected IASTDeclaration namespaceDefinitionOrAlias() throws BacktrackException, EndOfFileException {
		final int offset = lookahead().getOffset();
		int endOffset;
		boolean isInline = false;

		if (lookaheadType(1) == IToken.t_inline) {
			consume();
			isInline = true;
		}
		consume(IToken.t_namespace);

		List<IASTAttributeSpecifier> attributeSpecifiers = attributes();

		// optional name
		ICPPASTName name = null;
		if (lookaheadType(1) == IToken.tIDENTIFIER) {
			name = qualifiedName();
			endOffset = calculateEndOffset(name);
		} else {
			name = (ICPPASTName) nodeFactory.newName();
		}

		if (lookaheadType(1) == IToken.tLBRACE) {
			ICPPASTNamespaceDefinition outer = null;
			ICPPASTNamespaceDefinition inner = null;
			if (name instanceof ICPPASTQualifiedName) {
				// Handle C++17 nested namespace definition.
				ICPPASTNameSpecifier[] qualifier = ((ICPPASTQualifiedName) name).getQualifier();
				for (ICPPASTNameSpecifier specifier : qualifier) {
					if (!(specifier instanceof ICPPASTName)) {
						// No decltype-specifiers in nested namespace definition.
						throwBacktrack(specifier);
						return null;
					}
					ICPPASTName segment = (ICPPASTName) specifier;
					ICPPASTNamespaceDefinition namespace = ((ICPPNodeFactory) nodeFactory)
							.newNamespaceDefinition(segment);
					if (outer == null || inner == null) { // second half of condition is just to avoid warning
						outer = namespace;
					} else {
						inner.addDeclaration(namespace);
					}
					inner = namespace;
				}
			}
			IASTName lastName = name.getLastName();
			ICPPASTNamespaceDefinition namespace = ((ICPPNodeFactory) nodeFactory).newNamespaceDefinition(lastName);
			if (outer == null || inner == null) { // second half of condition is just to avoid warning
				outer = namespace;
			} else {
				inner.addDeclaration(namespace);
			}
			namespace.setIsInline(isInline);
			declarationListInBraces(namespace, offset, DeclarationOptions.GLOBAL);
			endOffset = getEndOffset();
			if (namespace != outer) {
				// For a C++17 nested namespace definition, we need to set the offset/length of
				// the enclosing namespace declaration nodes (declarationListInBraces() does it
				// for the inner one).
				for (IASTNode parent = namespace.getParent(); parent != null; parent = parent.getParent()) {
					setRange(parent, offset, endOffset);
					if (parent == outer) {
						break;
					}
				}
			}
			addAttributeSpecifiers(attributeSpecifiers, namespace);
			return outer;
		}

		if (lookaheadType(1) == IToken.tASSIGN) {
			endOffset = consume().getEndOffset();
			if (name.toString() == null || name instanceof ICPPASTQualifiedName) {
				throwBacktrack(offset, endOffset - offset);
				return null;
			}

			IASTName qualifiedName = qualifiedName();
			endOffset = consumeOrEndOfCompletion(IToken.tSEMI).getEndOffset();

			ICPPASTNamespaceAlias alias = ((ICPPNodeFactory) nodeFactory).newNamespaceAlias(name, qualifiedName);
			((ASTNode) alias).setOffsetAndLength(offset, endOffset - offset);
			return alias;
		}
		throwBacktrack(lookahead(1));
		return null;
	}

	// C++11 attributes
	protected List<IASTAttributeSpecifier> attributes() throws EndOfFileException, BacktrackException {
		List<IASTAttributeSpecifier> specifiers = null;

		while ((lookaheadTypeWithEndOfFile(1) == IToken.tLBRACKET && lookaheadTypeWithEndOfFile(2) == IToken.tLBRACKET)
				|| lookaheadTypeWithEndOfFile(1) == IToken.t_alignas) {
			if (specifiers == null)
				specifiers = new ArrayList<>();
			if (lookaheadTypeWithEndOfFile(1) == IToken.t_alignas) {
				specifiers.add((ICPPASTAlignmentSpecifier) alignmentSpecifier());
			} else {
				int offset = consumeOrEndOfCompletion(IToken.tLBRACKET).getOffset();
				consumeOrEndOfCompletion(IToken.tLBRACKET);
				ICPPASTAttributeList attributeList = ((ICPPNodeFactory) nodeFactory).newAttributeList();
				while (lookaheadType(1) != IToken.tRBRACKET) {
					if (lookaheadType(1) == IToken.tCOMMA)
						consume();
					ICPPASTAttribute attribute = singleAttribute();
					attributeList.addAttribute(attribute);

				}
				consumeOrEndOfCompletion(IToken.tRBRACKET);
				int endOffset = consumeOrEndOfCompletion(IToken.tRBRACKET).getEndOffset();
				setRange(attributeList, offset, endOffset);
				specifiers.add(attributeList);
			}
		}
		return specifiers;
	}

	protected ICPPASTTypeId typeID(DeclarationOptions option, ITemplateIdStrategy strategy)
			throws EndOfFileException, BacktrackException {
		if (!canBeTypeSpecifier()) {
			throwBacktrack(lookahead(1));
		}
		final int offset = lookahead().getOffset();
		IASTDeclSpecifier declSpecifier = null;
		IASTDeclarator declarator = null;

		try {
			Declaration decl = initDeclSpecifierSequenceDeclarator(option, false, strategy);
			declSpecifier = decl.leftSpecifier;
			declarator = decl.leftDeclarator;
		} catch (FoundAggregateInitializer lie) {
			// type-ids have no initializers
			throwBacktrack(lie.declarator);
		}
		ICPPASTTypeId result = (ICPPASTTypeId) nodeFactory.newTypeId(declSpecifier, declarator);
		setRange(result, offset, figureEndOffset(declSpecifier, declarator));
		return result;
	}

	/**
	 * alias-declaration
	 *	using identifier attribute-specifier-sequence? = type-id ;
	 *
	 * @throws EndOfFileException
	 */
	private IASTDeclaration aliasDeclaration(final int offset) throws EndOfFileException, BacktrackException {
		IToken identifierToken = consume();
		IASTName aliasName = buildName(-1, identifierToken, false);

		List<IASTAttributeSpecifier> attributes = attributes();

		consume();

		ICPPASTTypeId aliasedType = (ICPPASTTypeId) typeID(DeclarationOptions.TYPEID);

		final int nextToken = lookaheadType(1);
		if (nextToken != IToken.tSEMI && nextToken != IToken.tEOC) {
			throw backtrack;
		}
		int endOffset = consume().getEndOffset();

		ICPPASTAliasDeclaration aliasDeclaration = ((ICPPNodeFactory) nodeFactory).newAliasDeclaration(aliasName,
				aliasedType);
		addAttributeSpecifiers(attributes, aliasDeclaration);

		return setRange(aliasDeclaration, offset, endOffset);
	}

	private ICPPASTUsingDeclaration usingDeclaration(final int offset) throws EndOfFileException, BacktrackException {
		boolean typeName = false;
		if (lookaheadType(1) == IToken.t_typename) {
			typeName = true;
			consume();
		}

		IASTName name = qualifiedName();
		int end;
		switch (lookaheadType(1)) {
		case IToken.tSEMI:
		case IToken.tEOC:
			end = consume().getEndOffset();
			break;
		default:
			throw backtrack;
		}

		ICPPASTUsingDeclaration result = ((ICPPNodeFactory) nodeFactory).newUsingDeclaration(name);
		((ASTNode) result).setOffsetAndLength(offset, end - offset);
		result.setIsTypename(typeName);
		return result;
	}

	protected int visibilityFrom(int type) {
		switch (type) {
		case IToken.t_public:
			return ICPPASTVisibilityLabel.v_public;
		case IToken.t_protected:
			return ICPPASTVisibilityLabel.v_protected;
		case IToken.t_private:
			return ICPPASTVisibilityLabel.v_private;
		}
		return 0;
	}

	/**
	 * Implements Linkage specification in the ANSI C++ grammar.
	 * linkageSpecification : extern "string literal" declaration | extern
	 * "string literal" { declaration-sequence }
	 *
	 * @throws BacktrackException
	 *			 request for a backtrack
	 */
	protected ICPPASTLinkageSpecification linkageSpecification() throws EndOfFileException, BacktrackException {
		int offset = consume().getOffset(); // t_extern
		String specification = consume().getImage(); // tString
		ICPPASTLinkageSpecification linkage = ((ICPPNodeFactory) nodeFactory).newLinkageSpecification(specification);

		if (lookaheadType(1) == IToken.tLBRACE) {
			declarationListInBraces(linkage, offset, DeclarationOptions.GLOBAL);
			return linkage;
		}
		// single declaration

		IASTDeclaration declaration = declaration(DeclarationOptions.GLOBAL);
		linkage.addDeclaration(declaration);
		setRange(linkage, offset, calculateEndOffset(declaration));
		return linkage;
	}

	/**
	 * This routine parses a parameter declaration
	 *
	 * @throws BacktrackException
	 *			 request a backtrack
	 */
	protected ICPPASTParameterDeclaration parameterDeclaration() throws BacktrackException, EndOfFileException {
		final int startOffset = lookahead(1).getOffset();

		List<IASTAttributeSpecifier> attributes = attributes();

		IASTDeclSpecifier declSpec = null;
		IASTDeclarator declarator;
		try {
			Declaration decl = initDeclSpecifierSequenceDeclarator(DeclarationOptions.PARAMETER, false);
			declSpec = decl.leftSpecifier;
			declarator = decl.leftDeclarator;
		} catch (FoundAggregateInitializer lie) {
			declSpec = lie.specifier;
			declarator = addInitializer(lie, DeclarationOptions.PARAMETER);
		}

		final ICPPASTParameterDeclaration param = (ICPPASTParameterDeclaration) nodeFactory
				.newParameterDeclaration(declSpec, declarator);
		final int endOffset = figureEndOffset(declSpec, declarator);
		setRange(param, startOffset, endOffset);
		addAttributeSpecifiers(attributes, param);
		return param;
	}

	protected IASTName[] identifierList() throws EndOfFileException, BacktrackException {
		List<IASTName> result = new ArrayList<>();
		result.add(identifier());
		while (lookaheadType(1) == IToken.tCOMMA) {
			consume();
			result.add(identifier());
		}
		return result.toArray(new IASTName[result.size()]);
	}

	protected void catchHandlerSequence(List<ICPPASTCatchHandler> collection)
			throws EndOfFileException, BacktrackException {
		if (lookaheadType(1) == IToken.tEOC)
			return;

		if (lookaheadType(1) != IToken.t_catch)
			throwBacktrack(lookahead(1)); // error, need at least one

		int type = lookaheadType(1);
		while (type == IToken.t_catch) {
			int startOffset = consume().getOffset();
			consume(IToken.tLPAREN);
			boolean isEllipsis = false;
			IASTDeclaration declaration = null;
			try {
				if (lookaheadType(1) == IToken.tELLIPSIS) {
					consume(IToken.tELLIPSIS);
					isEllipsis = true;
				} else {
					declaration = simpleSingleDeclaration(DeclarationOptions.EXCEPTION);
				}
				if (lookaheadType(1) != IToken.tEOC)
					consume(IToken.tRPAREN);
			} catch (BacktrackException exception) {
				passing = false;
				IASTProblem problem = createProblem(exception);
				IASTProblemDeclaration problemDeclaration = nodeFactory.newProblemDeclaration(problem);
				((ASTNode) problemDeclaration).setOffsetAndLength(((ASTNode) problem));
				declaration = problemDeclaration;
			}

			ICPPASTCatchHandler handler = ((ICPPNodeFactory) nodeFactory).newCatchHandler(declaration, null);

			if (lookaheadType(1) != IToken.tEOC) {
				IASTStatement compoundStatement = catchBlockCompoundStatement();
				((ASTNode) handler).setOffsetAndLength(startOffset,
						calculateEndOffset(compoundStatement) - startOffset);
				handler.setIsCatchAll(isEllipsis);
				if (compoundStatement != null) {
					handler.setCatchBody(compoundStatement);
				}
			}

			collection.add(handler);
			type = lookaheadTypeWithEndOfFile(1);
		}
	}

	protected IASTStatement catchBlockCompoundStatement() throws BacktrackException, EndOfFileException {
		if (mode == ParserMode.QUICK_PARSE || mode == ParserMode.STRUCTURAL_PARSE || !activeCode) {
			int offset = lookahead(1).getOffset();
			IToken last = skipOverCompoundStatement(true);
			IASTCompoundStatement statement = nodeFactory.newCompoundStatement();
			setRange(statement, offset, last.getEndOffset());
			return statement;
		} else if (mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE) {
			if (scanner.isOnTopContext())
				return compoundStatement();
			int offset = lookahead(1).getOffset();
			IToken last = skipOverCompoundStatement(true);
			IASTCompoundStatement statement = nodeFactory.newCompoundStatement();
			setRange(statement, offset, last.getEndOffset());
			return statement;
		}
		return compoundStatement();
	}

	/**
	 * ctor-initializer:
	 * 	  : mem-initializer-list
	 * mem-initializer-list:
	 * 	  mem-initializer ...?
	 * 	  mem-initializer ...?, mem-initializer-list
	 * mem-initializer:
	 * 	  mem-initializer-id (expression-list?)
	 * 	  mem-initializer-id braced-init-list
	 * mem-initializer-id:
	 * 	  ::? nested-name-specifier? class-name
	 * 	  identifier
	 */
	protected void constructorInitializer(ICPPASTFunctionDefinition definition)
			throws EndOfFileException, BacktrackException {
		consume(IToken.tCOLON);
		loop: while (true) {
			final int offset = lookahead(1).getOffset();
			final IASTName name = qualifiedName();
			final IASTInitializer init;
			int endOffset;
			if (lookaheadType(1) != IToken.tEOC) {
				init = bracedOrConstructorStyleInitializer();
				endOffset = calculateEndOffset(init);
			} else {
				init = null;
				endOffset = calculateEndOffset(name);
			}
			ICPPASTConstructorChainInitializer ctorInitializer = ((ICPPNodeFactory) nodeFactory)
					.newConstructorChainInitializer(name, init);
			if (lookaheadType(1) == IToken.tELLIPSIS) {
				ctorInitializer.setIsPackExpansion(true);
				endOffset = consume().getEndOffset();
			}
			definition.addMemberInitializer(setRange(ctorInitializer, offset, endOffset));

			if (lookaheadType(1) == IToken.tCOMMA) {
				consume();
			} else {
				break loop;
			}
		}
	}

	/**
	 * Parse an elaborated type specifier.
	 *
	 * @throws BacktrackException
	 *			 request a backtrack
	 */
	protected ICPPASTElaboratedTypeSpecifier elaboratedTypeSpecifier() throws BacktrackException, EndOfFileException {
		// this is an elaborated class specifier
		final int type = lookaheadType(1);
		int eck = 0;

		switch (type) {
		case IToken.t_class:
			eck = ICPPASTElaboratedTypeSpecifier.k_class;
			break;
		case IToken.t_struct:
			eck = IASTElaboratedTypeSpecifier.k_struct;
			break;
		case IToken.t_union:
			eck = IASTElaboratedTypeSpecifier.k_union;
			break;
		case IToken.t_enum:
			eck = IASTElaboratedTypeSpecifier.k_enum;
			break;
		default:
			throwBacktrack(lookahead(1));
		}

		final int offset = consume().getOffset();

		List<IASTAttributeSpecifier> attributes = attributes();

		IASTName name = qualifiedName();
		ICPPASTElaboratedTypeSpecifier elaboratedTypeSpecifier = ((ICPPNodeFactory) nodeFactory)
				.newElaboratedTypeSpecifier(eck, name);
		addAttributeSpecifiers(attributes, elaboratedTypeSpecifier);
		return setRange(elaboratedTypeSpecifier, offset, calculateEndOffset(name));
	}

	protected IASTExpression specialCastExpression(int kind) throws EndOfFileException, BacktrackException {
		final int offset = lookahead(1).getOffset();
		final int optype = consume().getType();
		consume(IToken.tLT);
		final IASTTypeId typeID = typeID(DeclarationOptions.TYPEID);
		final IToken gt = lookahead(1);
		if (gt.getType() == IToken.tGT || gt.getType() == IToken.tGT_in_SHIFTR) {
			consume();
		} else if (gt.getType() != IToken.tEOC) {
			throwBacktrack(gt);
		}
		consumeOrEndOfCompletion(IToken.tLPAREN);
		IASTExpression operand = null;
		if (lookaheadType(1) != IToken.tEOC) {
			operand = expression();
		}
		final int endOffset = consumeOrEndOfCompletion(IToken.tRPAREN).getEndOffset();
		int operator;
		switch (optype) {
		case IToken.t_dynamic_cast:
			operator = ICPPASTCastExpression.op_dynamic_cast;
			break;
		case IToken.t_static_cast:
			operator = ICPPASTCastExpression.op_static_cast;
			break;
		case IToken.t_reinterpret_cast:
			operator = ICPPASTCastExpression.op_reinterpret_cast;
			break;
		case IToken.t_const_cast:
			operator = ICPPASTCastExpression.op_const_cast;
			break;
		default:
			operator = IASTCastExpression.op_cast;
			break;
		}
		return buildCastExpression(operator, typeID, operand, offset, endOffset);
	}

	protected IASTExpression deleteExpression() throws EndOfFileException, BacktrackException {
		int startingOffset = lookahead(1).getOffset();
		boolean global = false;
		if (lookaheadType(1) == IToken.tCOLONCOLON) {
			// global scope
			consume();
			global = true;
		}

		consume(IToken.t_delete);

		boolean vectored = false;
		if (lookaheadType(1) == IToken.tLBRACKET) {
			// array delete
			consume();
			consume(IToken.tRBRACKET);
			vectored = true;
		}
		IASTExpression castExpression = castExpression(CastExprCtx.NOT_IN_BINARY_EXPR, null);
		ICPPASTDeleteExpression deleteExpression = ((ICPPNodeFactory) nodeFactory).newDeleteExpression(castExpression);
		((ASTNode) deleteExpression).setOffsetAndLength(startingOffset,
				calculateEndOffset(castExpression) - startingOffset);
		deleteExpression.setIsGlobal(global);
		deleteExpression.setIsVectored(vectored);
		return deleteExpression;
	}

	/**
	 * Parse a new-expression. There is room for ambiguities. With P for placement, T for typeid,
	 * and I for initializer the potential patterns (with the new omitted) are:
	 * easy: 	T, T(I)
	 * medium: 	(P) T(I), (P) (T)(I)
	 * hard:	(T), (P) T, (P) (T), (T)(I)
	 */
	protected IASTExpression newExpression() throws BacktrackException, EndOfFileException {
		IToken token = lookahead(1);
		int offset = token.getOffset();

		final boolean isGlobal = token.getType() == IToken.tCOLONCOLON;
		if (isGlobal) {
			consume();
		}
		consume(IToken.t_new);
		if (lookaheadType(1) == IToken.tLPAREN) {
			consume();

			// consider placement first (P) ...
			List<ICPPASTInitializerClause> placement = null;
			IASTTypeId typeid = null;
			boolean isNewTypeID = true;
			IASTInitializer init = null;
			int endOffset = 0;
			IToken mark = mark();
			IToken end = null;
			try {
				placement = expressionList();
				endOffset = consumeOrEndOfCompletion(IToken.tRPAREN).getEndOffset();

				final int type = lookaheadType(1);
				if (type == IToken.tEOC) {
					return newExpression(isGlobal, placement, typeid, isNewTypeID, init, offset, endOffset);
				}
				if (type == IToken.tLPAREN) {
					// (P)(T) ...
					isNewTypeID = false;
					consume(IToken.tLPAREN);
					typeid = typeID(DeclarationOptions.TYPEID);
					endOffset = consumeOrEndOfCompletion(IToken.tRPAREN).getEndOffset();
				} else {
					// (P) T ...
					typeid = typeID(DeclarationOptions.TYPEID_NEW);
					endOffset = calculateEndOffset(typeid);
				}
				end = lookahead(1);
			} catch (BacktrackException exception) {
				placement = null;
				typeid = null;
			}

			if (typeid != null && placement != null) {
				// (P)(T)(I) or (P) T (I)
				int lt1 = lookaheadType(1);
				if (lt1 == IToken.tEOC)
					return newExpression(isGlobal, placement, typeid, isNewTypeID, init, offset, endOffset);

				if (lt1 == IToken.tLPAREN || lt1 == IToken.tLBRACE) {
					init = bracedOrConstructorStyleInitializer();
					endOffset = calculateEndOffset(init);
					return newExpression(isGlobal, placement, typeid, isNewTypeID, init, offset, endOffset);
				}
			}

			// (T) ...
			backup(mark);
			IASTTypeId typeid2 = null;
			IASTInitializer init2 = null;
			int endOffset2;
			try {
				typeid2 = typeID(DeclarationOptions.TYPEID);
				endOffset2 = consumeOrEndOfCompletion(IToken.tRPAREN).getEndOffset();

				final int type = lookaheadType(1);
				if (type == IToken.tEOC)
					return newExpression(isGlobal, null, typeid2, false, init2, offset, endOffset2);

				if (type == IToken.tLPAREN || type == IToken.tLBRACE) {
					if (placement != null && ASTQueries.findTypeRelevantDeclarator(
							typeid2.getAbstractDeclarator()) instanceof IASTArrayDeclarator) {
						throwBacktrack(lookahead(1));
					}

					// (T)(I)
					init2 = bracedOrConstructorStyleInitializer();
					endOffset2 = calculateEndOffset(init2);
				}
			} catch (BacktrackException exception) {
				if (placement == null)
					throw exception;
				endOffset2 = -1;
			}

			if (placement == null || endOffset2 > endOffset)
				return newExpression(isGlobal, null, typeid2, false, init2, offset, endOffset2);

			if (endOffset != endOffset2) {
				backup(end);
				return newExpression(isGlobal, placement, typeid, isNewTypeID, init, offset, endOffset);
			}

			// ambiguity:
			IASTExpression expr = newExpression(isGlobal, placement, typeid, isNewTypeID, init, offset, endOffset);
			IASTExpression expression = newExpression(isGlobal, null, typeid2, false, init2, offset, endOffset2);
			IASTAmbiguousExpression ambiguity = createAmbiguousExpression();
			ambiguity.addExpression(expr);
			ambiguity.addExpression(expression);
			((ASTNode) ambiguity).setOffsetAndLength((ASTNode) expr);
			return ambiguity;
		}

		// T ...
		final IASTTypeId typeid = typeID(DeclarationOptions.TYPEID_NEW);
		int endOffset = calculateEndOffset(typeid);
		IASTInitializer init = null;
		final int type = lookaheadType(1);
		if (type == IToken.tLPAREN || type == IToken.tLBRACE) {
			// T(I)
			init = bracedOrConstructorStyleInitializer();
			endOffset = calculateEndOffset(init);
		}
		return newExpression(isGlobal, null, typeid, true, init, offset, endOffset);
	}

	private IASTExpression newExpression(boolean isGlobal, List<? extends IASTInitializerClause> clause,
			IASTTypeId typeid, boolean isNewTypeId, IASTInitializer init, int offset, int endOffset) {
		IASTInitializerClause[] plcmtArray = null;
		if (clause != null && !clause.isEmpty()) {
			plcmtArray = clause.toArray(new IASTInitializerClause[clause.size()]);
		}
		ICPPASTNewExpression result = ((ICPPNodeFactory) nodeFactory).newNewExpression(plcmtArray, init, typeid);
		result.setIsGlobal(isGlobal);
		result.setIsNewTypeId(isNewTypeId);
		((ASTNode) result).setOffsetAndLength(offset, endOffset - offset);
		return result;
	}

	//	Look for "for-range-declaration : for-range-initializer"
	//	for-range-declaration:
	//			attribute-specifier-sequence? type-specifier-sequence declarator
	//	for-range-initializer:
	//		expression
	//		braced-init-list
	private ICPPASTRangeBasedForStatement startRangeBasedForLoop() throws EndOfFileException, BacktrackException {
		List<IASTAttributeSpecifier> attributes = attributes();
		IASTDeclaration declaration = simpleDeclaration(DeclarationOptions.RANGE_BASED_FOR, attributes);
		consume(IToken.tCOLON);
		IASTInitializerClause init = null;
		switch (lookaheadType(1)) {
		case IToken.tEOC:
			break;
		case IToken.tLBRACE:
			init = bracedInitList(false, false);
			break;
		default:
			init = expression();
		}

		ICPPASTRangeBasedForStatement result = ((ICPPNodeFactory) nodeFactory).newRangeBasedForStatement();
		result.setDeclaration(declaration);
		result.setInitializerClause(init);
		return result;
	}

	private IASTForStatement startRegularForLoop() throws BacktrackException, EndOfFileException {
		final IASTStatement initStatement = initStatement();
		IASTNode condition = null;
		IASTExpression iterExpr = null;

		int type = lookaheadType(1);
		if (type != IToken.tSEMI && type != IToken.tEOC) {
			condition = cppStyleCondition(IToken.tSEMI);
		}
		consumeOrEndOfCompletion(IToken.tSEMI);

		type = lookaheadType(1);
		if (type != IToken.tRPAREN && type != IToken.tEOC) {
			iterExpr = expression();
		}

		ICPPASTForStatement result = ((ICPPNodeFactory) nodeFactory).newForStatement();
		result.setInitializerStatement(initStatement);
		if (condition instanceof IASTExpression) {
			result.setConditionExpression((IASTExpression) condition);
		} else if (condition instanceof IASTDeclaration) {
			result.setConditionDeclaration((IASTDeclaration) condition);
		}
		result.setIterationExpression(iterExpr);
		return result;
	}

	private void consumeArrayModifiers(DeclarationOptions option, List<IASTArrayModifier> collection)
			throws EndOfFileException, BacktrackException {
		boolean allowExpression = option == DeclarationOptions.TYPEID_NEW;
		while (lookaheadType(1) == IToken.tLBRACKET) {
			int startOffset = consume().getOffset(); // eat the '['

			IASTExpression expression = null;
			if (lookaheadType(1) != IToken.tRBRACKET && lookaheadType(1) != IToken.tEOC) {
				expression = allowExpression ? expression() : constantExpression();
				allowExpression = false;
			}
			int endOffset;
			switch (lookaheadType(1)) {
			case IToken.tRBRACKET:
			case IToken.tEOC:
				endOffset = consume().getEndOffset();
				break;
			default:
				throw backtrack;
			}
			IASTArrayModifier arrayModifier = nodeFactory.newArrayModifier(expression);

			List<IASTAttributeSpecifier> attributes = attributes();
			addAttributeSpecifiers(attributes, arrayModifier);
			endOffset = attributesEndOffset(endOffset, attributes);

			setRange(arrayModifier, startOffset, endOffset);

			collection.add(arrayModifier);
		}
		return;
	}

	/**
	 * Parse a Pointer Operator.
	 * ptrOperator	: "*" attribute-specifier-seq? (cvQualifier)*
	 * 				| "&" attribute-specifier-seq?
	 * 				| "&&" attribute-specifier-seq?
	 * 				| ::? nestedNameSpecifier "*" attribute-specifier-seq? (cvQualifier)*
	 *
	 * @throws BacktrackException
	 *			 request a backtrack
	 */
	private List<? extends IASTPointerOperator> consumePointerOperators()
			throws EndOfFileException, BacktrackException {
		List<IASTPointerOperator> result = null;
		for (;;) {
			IToken mark = mark();
			final int startOffset = mark.getOffset();

			List<IASTAttributeSpecifier> attributes = new ArrayList<>();

			final int lt1 = lookaheadType(1);
			if (lt1 == IToken.tAMPER || lt1 == IToken.tAND) {
				IToken endToken = consume();
				final int offset = endToken.getOffset();

				ICPPASTReferenceOperator refOp = ((ICPPNodeFactory) nodeFactory)
						.newReferenceOperator(lt1 == IToken.tAND);
				setRange(refOp, offset, endToken.getEndOffset());

				attributes = CollectionUtils.merge(attributes, attributes());
				addAttributeSpecifiers(attributes, refOp);

				if (result != null) {
					result.add(refOp);
					return result;
				}
				return Collections.singletonList(refOp);
			}

			boolean isConst = false, isVolatile = false;
			IASTName name = null;
			int coloncolon = lookaheadType(1) == IToken.tCOLONCOLON ? 1 : 0;
			loop: while (lookaheadTypeWithEndOfFile(coloncolon + 1) == IToken.tIDENTIFIER) {
				switch (lookaheadTypeWithEndOfFile(coloncolon + 2)) {
				case IToken.tCOLONCOLON:
					coloncolon += 2;
					break;
				case IToken.tLT:
					coloncolon = 1;
					break loop;
				default:
					coloncolon = 0;
					break loop;
				}
			}
			if (coloncolon != 0) {
				try {
					name = qualifiedName();
					if (name.getLookupKey().length != 0) {
						backup(mark);
						return result;
					}
				} catch (BacktrackException exception) {
					backup(mark);
					return result;
				}
			}
			if (lookaheadTypeWithEndOfFile(1) != IToken.tSTAR) {
				backup(mark);
				return result;
			}

			int endOffset = consume().getEndOffset();
			loop: for (;;) {
				switch (lookaheadTypeWithEndOfFile(1)) {
				case IToken.t_const:
					endOffset = consume().getEndOffset();
					isConst = true;
					break;
				case IToken.t_volatile:
					endOffset = consume().getEndOffset();
					isVolatile = true;
					break;
				default:
					break loop;
				}
			}

			IASTPointer pointer;
			if (name != null) {
				pointer = ((ICPPNodeFactory) nodeFactory).newPointerToMember(name);
			} else {
				pointer = nodeFactory.newPointer();
			}
			pointer.setConst(isConst);
			pointer.setVolatile(isVolatile);
			pointer.setRestrict(false);
			if (result == null) {
				result = new ArrayList<>(4);
			}

			attributes = CollectionUtils.merge(attributes, attributes());
			addAttributeSpecifiers(attributes, pointer);
			endOffset = attributesEndOffset(endOffset, attributes);
			setRange(pointer, startOffset, endOffset);

			result.add(pointer);
		}
	}

	private IASTDeclarator declarator(List<? extends IASTPointerOperator> pointerOperators, boolean hasEllipsis,
			IASTName declaratorName, IASTDeclarator nestedDeclarator, int startingOffset, int endOffset,
			DeclaratorStrategy strategy, DeclarationOptions option, List<IASTAttributeSpecifier> attributes)
			throws EndOfFileException, BacktrackException {
		ICPPASTDeclarator result = null;
		loop: while (true) {
			final int type = lookaheadTypeWithEndOfFile(1);
			switch (type) {
			case IToken.tLPAREN:
				if (option.fAllowFunctions && strategy == DeclaratorStrategy.PREFER_METHOD) {
					result = methodDeclarator(false);
					setDeclaratorID(result, hasEllipsis, declaratorName, nestedDeclarator);
				}
				break loop;

			case IToken.tLBRACKET:
				if (lookaheadTypeWithEndOfFile(2) == IToken.tLBRACKET) {
					attributes = CollectionUtils.merge(attributes, attributes());
					break;
				}
				result = arrayDeclarator(option);
				setDeclaratorID(result, hasEllipsis, declaratorName, nestedDeclarator);
				break loop;

			case IToken.tCOLON:
				if (!option.fAllowBitField || nestedDeclarator != null)
					break loop; // no backtrack because typeid can be followed by colon

				result = bitFieldDeclarator();
				setDeclaratorID(result, hasEllipsis, declaratorName, nestedDeclarator);
				break loop;

			default:
				break loop;
			}
		}

		if (result == null) {
			result = ((ICPPNodeFactory) nodeFactory).newDeclarator(null);
			setDeclaratorID(result, hasEllipsis, declaratorName, nestedDeclarator);
		} else {
			endOffset = calculateEndOffset(result);
		}

		if (pointerOperators != null) {
			for (IASTPointerOperator operator : pointerOperators) {
				result.addPointerOperator(operator);
			}
		}

		addAttributeSpecifiers(attributes, result);
		endOffset = attributesEndOffset(endOffset, attributes);
		setRange(result, startingOffset, endOffset);

		return result;
	}

	/**
	 * static_assert-declaration:
	 *		static_assert (constant-expression);
	 *			OR
	 *		static_assert (constant-expression , string-literal);
	 */
	private ICPPASTStaticAssertDeclaration staticAssertDeclaration() throws EndOfFileException, BacktrackException {
		int offset = consume(IToken.t_static_assert).getOffset();
		consume(IToken.tLPAREN);
		IASTExpression expr = constantExpression();
		int endOffset = calculateEndOffset(expr);
		ICPPASTLiteralExpression message = null;
		if (lookaheadType(1) == IToken.tCOMMA) {
			consume(IToken.tCOMMA);
			message = stringLiteral();
		}
		ICPPASTStaticAssertDeclaration assertion = ((ICPPNodeFactory) nodeFactory).newStaticAssertion(expr, message);
		if (lookaheadType(1) != IToken.tEOC) {
			consume(IToken.tRPAREN);
			endOffset = consume(IToken.tSEMI).getEndOffset();
		}
		return setRange(assertion, offset, endOffset);
	}

	/**
	 *	primary-expression
	 *	postfix-expression [ expression ]
	 *	postfix-expression [ braced-init-list ]
	 *	postfix-expression (expression-list_opt)
	 *	simple-type-specifier (expression-list_opt)
	 *	simple-type-specifier braced-init-list
	 *	typename-specifier (expression-list_opt)
	 *	typename-specifier braced-init-list
	 *	postfix-expression . templateopt id-expression
	 *	postfix-expression -> templateopt id-expression
	 *	postfix-expression . pseudo-destructor-name
	 *	postfix-expression -> pseudo-destructor-name
	 *	postfix-expression ++
	 *	postfix-expression --
	 *	dynamic_cast < type-id > (expression)
	 *	static_cast < type-id > (expression)
	 *	reinterpret_cast < type-id > (expression)
	 *	const_cast < type-id > (expression)
	 *	typeid (expression)
	 *	typeid (type-id)
	 */
	private IASTExpression postfixExpression(CastExprCtx context, ITemplateIdStrategy strategy)
			throws EndOfFileException, BacktrackException {
		IASTExpression firstExpression = null;
		boolean isTemplate = false;
		int offset;

		switch (lookaheadType(1)) {
		case IToken.t_dynamic_cast:
			firstExpression = specialCastExpression(ICPPASTCastExpression.op_dynamic_cast);
			break;
		case IToken.t_static_cast:
			firstExpression = specialCastExpression(ICPPASTCastExpression.op_static_cast);
			break;
		case IToken.t_reinterpret_cast:
			firstExpression = specialCastExpression(ICPPASTCastExpression.op_reinterpret_cast);
			break;
		case IToken.t_const_cast:
			firstExpression = specialCastExpression(ICPPASTCastExpression.op_const_cast);
			break;

		case IToken.t_typeid:
			// 'typeid' (expression)
			// 'typeid' (type-id)
			firstExpression = parseTypeIDInParenthesisOrUnaryExpression(true, consume().getOffset(),
					ICPPASTTypeIdExpression.op_typeid, ICPPASTUnaryExpression.op_typeid, context, strategy);
			break;

		case IToken.t_noexcept:
			// 'noexcept' (expression)
			offset = consume().getOffset(); // noexcept
			consume(IToken.tLPAREN); // (
			firstExpression = expression();
			firstExpression = nodeFactory.newUnaryExpression(IASTUnaryExpression.op_noexcept, firstExpression);
			final int endOffset = consume(IToken.tRPAREN).getEndOffset(); //)
			setRange(firstExpression, offset, endOffset);
			break;

		// typename-specifier (expression-list_opt)
		// typename-specifier braced-init-list
		// simple-type-specifier (expression-list_opt)
		// simple-type-specifier braced-init-list
		case IToken.t_typename:
		case IToken.t_char:
		case IToken.t_char16_t:
		case IToken.t_char32_t:
		case IToken.t_wchar_t:
		case IToken.t_bool:
		case IToken.t_short:
		case IToken.t_int:
		case IToken.t_long:
		case IToken.t_signed:
		case IToken.t_unsigned:
		case IToken.t_float:
		case IToken.t_double:
		case IToken.t_decltype:
		case IToken.t_void:
			if (lookaheadType(1) == IToken.t_decltype) {
				// Might be an id-expression starting with a decltype-specifier.
				IToken marked = mark();
				try {
					firstExpression = primaryExpression(context, strategy);
					break;
				} catch (BacktrackException exception) {
					backup(marked);
				}
			}
			firstExpression = simpleTypeConstructorExpression(simpleTypeSpecifier());
			break;

		default:
			firstExpression = primaryExpression(context, strategy);
			if (firstExpression instanceof IASTIdExpression && lookaheadType(1) == IToken.tLBRACE) {
				IASTName name = ((IASTIdExpression) firstExpression).getName();
				ICPPASTDeclSpecifier declSpec = ((ICPPNodeFactory) nodeFactory).newTypedefNameSpecifier(name);
				firstExpression = simpleTypeConstructorExpression(setRange(declSpec, name));
			}
			break;
		}

		for (;;) {
			switch (lookaheadType(1)) {
			case IToken.tLBRACKET:
				// postfix-expression [ expression ]
				// postfix-expression [ braced-init-list ]
				consume(IToken.tLBRACKET);
				IASTInitializerClause expression;
				if (lookaheadType(1) == IToken.tLBRACE) {
					expression = bracedInitList(false, false);
				} else {
					expression = expression();
				}
				int endOffset = consumeOrEndOfCompletion(IToken.tRBRACKET).getEndOffset();
				IASTArraySubscriptExpression subscript = ((ICPPNodeFactory) nodeFactory)
						.newArraySubscriptExpression(firstExpression, expression);
				firstExpression = setRange(subscript, firstExpression, endOffset);
				break;
			case IToken.tLPAREN:
				// postfix-expression (expression-list_opt)
				// simple-type-specifier (expression-list_opt)  // cannot be distinguished
				consume(IToken.tLPAREN);
				IASTInitializerClause[] initArray;
				if (lookaheadType(1) == IToken.tRPAREN) {
					initArray = IASTExpression.EMPTY_EXPRESSION_ARRAY;
				} else {
					final List<ICPPASTInitializerClause> exprList = expressionList();
					initArray = exprList.toArray(new IASTInitializerClause[exprList.size()]);
				}
				endOffset = consumeOrEndOfCompletion(IToken.tRPAREN).getEndOffset();

				IASTFunctionCallExpression callExpr = nodeFactory.newFunctionCallExpression(firstExpression, initArray);
				firstExpression = setRange(callExpr, firstExpression, endOffset);
				break;

			case IToken.tINCR:
				endOffset = consume().getEndOffset();
				firstExpression = buildUnaryExpression(IASTUnaryExpression.op_postFixIncr, firstExpression,
						((ASTNode) firstExpression).getOffset(), endOffset);
				break;
			case IToken.tDECR:
				endOffset = consume().getEndOffset();
				firstExpression = buildUnaryExpression(IASTUnaryExpression.op_postFixDecr, firstExpression,
						((ASTNode) firstExpression).getOffset(), endOffset);
				break;

			case IToken.tDOT:
				// member access
				IToken dot = consume();
				if (lookaheadType(1) == IToken.t_template) {
					consume();
					isTemplate = true;
				}

				IASTName name = qualifiedName(context, strategy);

				if (name == null)
					throwBacktrack(((ASTNode) firstExpression).getOffset(),
							((ASTNode) firstExpression).getLength() + dot.getLength());

				ICPPASTFieldReference fieldReference = ((ICPPNodeFactory) nodeFactory).newFieldReference(name,
						firstExpression);
				fieldReference.setIsPointerDereference(false);
				fieldReference.setIsTemplate(isTemplate);
				((ASTNode) fieldReference).setOffsetAndLength(((ASTNode) firstExpression).getOffset(),
						calculateEndOffset(name) - ((ASTNode) firstExpression).getOffset());
				firstExpression = fieldReference;
				break;
			case IToken.tARROW:
				// member access
				IToken arrow = consume();

				if (lookaheadType(1) == IToken.t_template) {
					consume();
					isTemplate = true;
				}

				name = qualifiedName(context, strategy);

				if (name == null)
					throwBacktrack(((ASTNode) firstExpression).getOffset(),
							((ASTNode) firstExpression).getLength() + arrow.getLength());

				fieldReference = ((ICPPNodeFactory) nodeFactory).newFieldReference(name, firstExpression);
				fieldReference.setIsPointerDereference(true);
				fieldReference.setIsTemplate(isTemplate);
				((ASTNode) fieldReference).setOffsetAndLength(((ASTNode) firstExpression).getOffset(),
						calculateEndOffset(name) - ((ASTNode) firstExpression).getOffset());
				firstExpression = fieldReference;
				break;
			default:
				return firstExpression;
			}
		}
	}

	private IASTExpression simpleTypeConstructorExpression(ICPPASTDeclSpecifier declSpec)
			throws EndOfFileException, BacktrackException {
		IASTInitializer initializer = bracedOrConstructorStyleInitializer();
		ICPPASTSimpleTypeConstructorExpression result = ((ICPPNodeFactory) nodeFactory)
				.newSimpleTypeConstructorExpression(declSpec, initializer);
		return setRange(result, declSpec, calculateEndOffset(initializer));
	}

	private Declaration declarationSpecifierSequence(final DeclarationOptions option, final boolean single,
			ITemplateIdStrategy strat) throws BacktrackException, EndOfFileException {
		int storageClass = IASTDeclSpecifier.sc_unspecified;
		int simpleType = IASTSimpleDeclSpecifier.t_unspecified;
		int options = 0;
		int isLong = 0;

		IToken returnToken = null;
		ICPPASTDeclSpecifier result = null;
		ICPPASTDeclSpecifier altResult = null;
		List<IASTAttributeSpecifier> attributes = null;
		try {
			IASTName identifier = null;
			IASTExpression typeofExpression = null;
			IASTProblem problem = null;

			boolean isTypename = false;
			boolean encounteredRawType = false;
			boolean encounteredTypename = false;

			final int offset = lookahead(1).getOffset();
			int endOffset = offset;

			declSpecifiers: for (;;) {
				final int type = lookaheadTypeWithEndOfFile(1);
				switch (type) {
				case 0: // encountered eof
					break declSpecifiers;
				// storage class specifiers
				case IToken.t_auto:
					if (encounteredTypename)
						break declSpecifiers;
					simpleType = IASTSimpleDeclSpecifier.t_auto;
					encounteredRawType = true;
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
				case IToken.t_thread_local:
					options |= THREAD_LOCAL; // thread_local may appear with static or extern
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_mutable:
					storageClass = IASTDeclSpecifier.sc_mutable;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_typedef:
					storageClass = IASTDeclSpecifier.sc_typedef;
					endOffset = consume().getEndOffset();
					break;
				// function specifiers
				case IToken.t_inline:
					options |= INLINE;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_virtual:
					options |= VIRTUAL;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_explicit:
					options |= EXPLICIT;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_friend:
					options |= FRIEND;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_constexpr:
					options |= CONSTEXPR;
					endOffset = consume().getEndOffset();
					break;
				// type specifier
				case IToken.t_const:
					options |= CONST;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_volatile:
					options |= VOLATILE;
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
				case IToken.t_short:
					if (encounteredTypename)
						break declSpecifiers;
					options |= SHORT;
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
				case IToken.t_char:
					if (encounteredTypename)
						break declSpecifiers;
					simpleType = IASTSimpleDeclSpecifier.t_char;
					encounteredRawType = true;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_wchar_t:
					if (encounteredTypename)
						break declSpecifiers;
					simpleType = IASTSimpleDeclSpecifier.t_wchar_t;
					encounteredRawType = true;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_char16_t:
					if (encounteredTypename)
						break declSpecifiers;
					simpleType = IASTSimpleDeclSpecifier.t_char16_t;
					encounteredRawType = true;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_char32_t:
					if (encounteredTypename)
						break declSpecifiers;
					simpleType = IASTSimpleDeclSpecifier.t_char32_t;
					encounteredRawType = true;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_bool:
					if (encounteredTypename)
						break declSpecifiers;
					simpleType = IASTSimpleDeclSpecifier.t_bool;
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
				case IToken.t_void:
					if (encounteredTypename)
						break declSpecifiers;
					simpleType = IASTSimpleDeclSpecifier.t_void;
					encounteredRawType = true;
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_typename:
					if (encounteredTypename || encounteredRawType)
						break declSpecifiers;
					consume();
					identifier = qualifiedName();
					endOffset = calculateEndOffset(identifier);
					isTypename = true;
					encounteredTypename = true;
					break;
				case IToken.tBITCOMPLEMENT:
				case IToken.tCOLONCOLON:
				case IToken.tIDENTIFIER:
				case IToken.tCOMPLETION:
					if (encounteredRawType || encounteredTypename)
						break declSpecifiers;

					if (option != null && option.fAllowEmptySpecifier && lookaheadType(1) != IToken.tCOMPLETION) {
						if ((options & FORBID_IN_EMPTY_DECLSPEC) == 0
								&& storageClass == IASTDeclSpecifier.sc_unspecified) {
							altResult = buildSimpleDeclSpec(storageClass, simpleType, options, isLong, typeofExpression,
									offset, endOffset);
							returnToken = mark();
						}
					}

					identifier = qualifiedName(CastExprCtx.NOT_IN_BINARY_EXPR, strat);
					if (identifier.getLookupKey().length == 0 && lookaheadType(1) != IToken.tEOC)
						throwBacktrack(lookahead(1));

					if (identifier.getLastName() instanceof ICPPASTTemplateName) {
						isTypename = true;
					}

					endOffset = calculateEndOffset(identifier);
					encounteredTypename = true;
					break;
				case IToken.t_class:
				case IToken.t_struct:
				case IToken.t_union:
					if (encounteredTypename || encounteredRawType)
						break declSpecifiers;
					if (option != null && option.fAllowCompositeSpecifier) {
						try {
							result = classSpecifier();
						} catch (BacktrackException exception) {
							result = elaboratedTypeSpecifier();
						}
					} else {
						result = elaboratedTypeSpecifier();
					}
					endOffset = calculateEndOffset(result);
					encounteredTypename = true;
					break;

				case IToken.t_enum:
					if (encounteredTypename || encounteredRawType)
						break declSpecifiers;
					try {
						result = enumDeclaration(option != null && option.fAllowOpaqueEnum);
					} catch (BacktrackException exception) {
						if (exception.getNodeBeforeProblem() instanceof ICPPASTDeclSpecifier) {
							result = (ICPPASTDeclSpecifier) exception.getNodeBeforeProblem();
							problem = exception.getProblem();
							break declSpecifiers;
						}
						throw exception;
					}
					endOffset = calculateEndOffset(result);
					encounteredTypename = true;
					break;

				case IToken.t_decltype:
					if (encounteredRawType || encounteredTypename)
						throwBacktrack(lookahead(1));

					// A decltype-specifier could be the first element
					// in a qualified name, in which case we'll have
					// a named-type-specifier.
					IToken marked = mark();
					try {
						identifier = qualifiedName();
						endOffset = calculateEndOffset(identifier);
						encounteredTypename = true;
						break;
					} catch (BacktrackException exception) {
						backup(marked);
					}

					// Otherwise we have a simple-decl-specifier.
					consume(IToken.t_decltype);
					consume(IToken.tLPAREN);
					if (lookaheadType(1) == IToken.t_auto) {
						simpleType = IASTSimpleDeclSpecifier.t_decltype_auto;
						consume(IToken.t_auto);
					} else {
						simpleType = IASTSimpleDeclSpecifier.t_decltype;
						typeofExpression = expression();
					}
					endOffset = consumeOrEndOfCompletion(IToken.tRPAREN).getEndOffset();

					encounteredTypename = true;
					break;

				default:
					break declSpecifiers;
				}

				if (encounteredRawType && encounteredTypename)
					throwBacktrack(lookahead(1));

				if (single)
					break declSpecifiers;
			}

			// check for empty specification
			if (!encounteredRawType && !encounteredTypename && lookaheadType(1) != IToken.tEOC
					&& (option == null || !option.fAllowEmptySpecifier)) {
				throwBacktrack(lookahead(1));
			}

			attributes = CollectionUtils.merge(attributes, attributes());

			if (result != null) {
				configureDeclSpec(result, storageClass, options);
				setRange(result, offset, endOffset);
				if (problem != null) {
					throwBacktrack(problem, result);
				}
			} else if (identifier != null) {
				result = buildNamedTypeSpecifier(identifier, isTypename, storageClass, options, offset, endOffset);
			} else {
				result = buildSimpleDeclSpec(storageClass, simpleType, options, isLong, typeofExpression, offset,
						endOffset);
			}
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

	private ICPPASTNamedTypeSpecifier buildNamedTypeSpecifier(IASTName name, boolean isTypename, int storageClass,
			int options, int offset, int endOffset) {
		ICPPASTNamedTypeSpecifier declSpec = ((ICPPNodeFactory) nodeFactory).newTypedefNameSpecifier(name);
		declSpec.setIsTypename(isTypename);
		configureDeclSpec(declSpec, storageClass, options);
		((ASTNode) declSpec).setOffsetAndLength(offset, endOffset - offset);
		return declSpec;
	}

	private ICPPASTSimpleDeclSpecifier buildSimpleDeclSpec(int storageClass, int simpleType, int options, int isLong,
			IASTExpression typeofExpression, int offset, int endOffset) {
		ICPPASTSimpleDeclSpecifier specifier = ((ICPPNodeFactory) nodeFactory).newSimpleDeclSpecifier();

		configureDeclSpec(specifier, storageClass, options);

		specifier.setType(simpleType);
		specifier.setLong(isLong == 1);
		specifier.setLongLong(isLong > 1);
		specifier.setShort((options & SHORT) != 0);
		specifier.setUnsigned((options & UNSIGNED) != 0);
		specifier.setSigned((options & SIGNED) != 0);
		specifier.setComplex((options & COMPLEX) != 0);
		specifier.setImaginary((options & IMAGINARY) != 0);
		specifier.setDeclTypeExpression(typeofExpression);

		((ASTNode) specifier).setOffsetAndLength(offset, endOffset - offset);
		return specifier;
	}

	/**
	 * Parse a class virtual specifier for a class specification.
	 * class-virt-specifier:
	 *	final
	 * @param astClassSpecifier
	 */
	private void classVirtualSpecifier(ICPPASTCompositeTypeSpecifier astClassSpecifier)
			throws EndOfFileException, BacktrackException {
		IToken token = lookahead();
		ContextSensitiveTokenType contextSensitiveType = getContextSensitiveType(token);
		if (contextSensitiveType == ContextSensitiveTokenType.FINAL) {
			consume();
			ICPPASTClassVirtSpecifier specifier = ((ICPPNodeFactory) nodeFactory)
					.newClassVirtSpecifier(ICPPASTClassVirtSpecifier.SpecifierKind.Final);
			setRange(specifier, token.getOffset(), token.getOffset() + token.getLength());
			astClassSpecifier.setVirtSpecifier(specifier);
		}
	}

	private ContextSensitiveTokenType getContextSensitiveType(IToken token) {
		if (!(token.getType() == IToken.tIDENTIFIER)) {
			return null;
		}
		return contextSensitiveTokens.get(new String(token.getCharImage()));
	}

	private Map<String, ContextSensitiveTokenType> createContextSensitiveTokenMap(
			ICPPParserExtensionConfiguration config) {
		Map<String, ContextSensitiveTokenType> result = new HashMap<>();
		result.put(Keywords.OVERRIDE, ContextSensitiveTokenType.OVERRIDE);
		result.put(Keywords.FINAL, ContextSensitiveTokenType.FINAL);
		result.putAll(config.getAdditionalContextSensitiveKeywords());
		return result;
	}

	private ICPPASTDeclSpecifier simpleTypeSpecifier() throws BacktrackException, EndOfFileException {
		Declaration declaration = declarationSpecifierSequence(null, true, null);
		return (ICPPASTDeclSpecifier) declaration.leftSpecifier;
	}

	private ICPPASTLiteralExpression stringLiteral() throws EndOfFileException, BacktrackException {
		switch (lookaheadType(1)) {
		case IToken.tSTRING:
		case IToken.tLSTRING:
		case IToken.tUTF16STRING:
		case IToken.tUTF32STRING:
		case IToken.tUSER_DEFINED_STRING_LITERAL:
			break;
		default:
			throwBacktrack(lookahead(1));
		}
		IToken token = consume();
		ICPPASTLiteralExpression literal = (ICPPASTLiteralExpression) nodeFactory
				.newLiteralExpression(IASTLiteralExpression.lk_string_literal, token.getImage());
		return setRange(literal, token.getOffset(), token.getEndOffset());
	}

	private ICPPASTName qualifiedName() throws BacktrackException, EndOfFileException {
		ICPPASTNameSpecifier nameSpec = nameSpecifier();
		if (!(nameSpec instanceof ICPPASTName)) {
			// decltype-specifier without following ::
			throwBacktrack(nameSpec);
		}
		return (ICPPASTName) nameSpec;
	}

	/**
	 * Parses a qualified name.
	 */
	private ICPPASTName qualifiedName(CastExprCtx context, ITemplateIdStrategy strategy)
			throws BacktrackException, EndOfFileException {
		ICPPASTNameSpecifier nameSpec = nameSpecifier(context, strategy);
		if (!(nameSpec instanceof ICPPASTName)) {
			// decltype-specifier without following ::
			throwBacktrack(nameSpec);
		}
		return (ICPPASTName) nameSpec;
	}

	private ICPPASTNameSpecifier nameSpecifier() throws BacktrackException, EndOfFileException {
		return ambiguousNameSpecifier(CastExprCtx.NOT_IN_BINARY_EXPR);
	}

	/**
	 * Parses a name specifier.
	 */
	private ICPPASTNameSpecifier nameSpecifier(CastExprCtx context, ITemplateIdStrategy strategy)
			throws BacktrackException, EndOfFileException {
		if (strategy == null)
			return ambiguousNameSpecifier(context);

		ICPPASTQualifiedName qname = null;
		ICPPASTNameSpecifier nameSpec = null;
		final int offset = lookahead(1).getOffset();
		int endOffset = offset;
		if (lookaheadType(1) == IToken.tCOLONCOLON) {
			endOffset = consume().getEndOffset();
			qname = ((ICPPNodeFactory) nodeFactory).newQualifiedName(null);
			qname.setFullyQualified(true);
		}

		boolean mustBeLast = false;
		boolean haveName = false;
		loop: while (true) {
			boolean keywordTemplate = false;
			if (qname != null && lookaheadType(1) == IToken.t_template) {
				consume();
				keywordTemplate = true;
			}

			int destructorOffset = -1;
			if (lookaheadType(1) == IToken.tBITCOMPLEMENT) {
				destructorOffset = consume().getOffset();
				mustBeLast = true;
			}

			switch (lookaheadType(1)) {
			case IToken.tIDENTIFIER:
			case IToken.tCOMPLETION:
			case IToken.tEOC:
				IToken nt = consume();
				nameSpec = (ICPPASTName) buildName(destructorOffset, nt, keywordTemplate);
				break;

			case IToken.t_operator:
				nameSpec = (ICPPASTName) operatorID();
				break;

			case IToken.t_decltype:
				// A decltype-specifier must be the first component of a qualified name.
				if (qname != null)
					throwBacktrack(lookahead(1));

				nameSpec = decltypeSpecifier();
				break;

			default:
				if (!haveName || destructorOffset >= 0 || keywordTemplate) {
					throwBacktrack(lookahead(1));
				}
				nameSpec = ((ICPPNodeFactory) nodeFactory).newName(CharArrayUtils.EMPTY);
				if (qname != null) {
					registerNameSpecifier(qname, nameSpec);
				}
				break loop;
			}

			haveName = true;

			// Check for template-id
			if (nameSpec instanceof IASTName && lookaheadTypeWithEndOfFile(1) == IToken.tLT) {
				IASTName name = (IASTName) nameSpec;
				final boolean inBinaryExpression = context != CastExprCtx.NOT_IN_BINARY_EXPR;
				final int haveArgs = haveTemplateArguments(inBinaryExpression);
				boolean templateID = true;
				if (!keywordTemplate) {
					if (haveArgs == NO_TEMPLATE_ID) {
						templateID = false;
					} else if (haveArgs == AMBIGUOUS_TEMPLATE_ID) {
						templateID = strategy.shallParseAsTemplateID(name);
					}
				}
				if (templateID) {
					if (haveArgs == NO_TEMPLATE_ID)
						throwBacktrack(lookahead(1));

					nameSpec = (ICPPASTName) registerTemplateArguments(name, strategy);
				}
			}

			endOffset = calculateEndOffset(nameSpec);
			if (qname != null) {
				registerNameSpecifier(qname, nameSpec);
			}

			if (lookaheadTypeWithEndOfFile(1) != IToken.tCOLONCOLON)
				break loop;

			if (mustBeLast)
				throwBacktrack(lookahead(1));

			endOffset = consume().getEndOffset(); // ::
			if (qname == null) {
				qname = ((ICPPNodeFactory) nodeFactory).newQualifiedName(null);
				registerNameSpecifier(qname, nameSpec);
			}
		}
		if (qname != null) {
			setRange(qname, offset, endOffset);
			nameSpec = qname;
		}
		return nameSpec;
	}

	private ICPPASTNameSpecifier ambiguousNameSpecifier(CastExprCtx context)
			throws BacktrackException, EndOfFileException {
		TemplateIdStrategy strategy = new TemplateIdStrategy();
		IToken mark = mark();
		while (true) {
			try {
				return nameSpecifier(context, strategy);
			} catch (BacktrackException exception) {
				if (exception.isFatal()) {
					throw exception;
				}
				if (strategy.setNextAlternative(true /* previous alternative failed to parse */)) {
					backup(mark);
				} else {
					throw exception;
				}
			}
		}
	}

	private IASTSimpleDeclaration simpleSingleDeclaration(DeclarationOptions options)
			throws BacktrackException, EndOfFileException {
		final int startOffset = lookahead(1).getOffset();
		IASTDeclSpecifier declSpec;
		IASTDeclarator declarator;

		List<IASTAttributeSpecifier> attributes = attributes();
		try {
			Declaration decl = initDeclSpecifierSequenceDeclarator(options, true);
			declSpec = decl.leftSpecifier;
			declarator = decl.leftDeclarator;
		} catch (FoundAggregateInitializer lie) {
			declSpec = lie.specifier;
			declarator = addInitializer(lie, options);
		}

		final int endOffset = figureEndOffset(declSpec, declarator);
		final IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(declSpec);
		if (declarator != null)
			declaration.addDeclarator(declarator);
		((ASTNode) declaration).setOffsetAndLength(startOffset, endOffset - startOffset);
		addAttributeSpecifiers(attributes, declaration);
		return declaration;
	}

	private IASTName operatorID() throws BacktrackException, EndOfFileException {
		final IToken firstToken = consume(IToken.t_operator);
		int endOffset = firstToken.getEndOffset();
		IASTTypeId typeId = null;
		OverloadableOperator op = null;
		final int type = lookaheadType(1);
		switch (type) {
		case IToken.tLPAREN:
			op = OverloadableOperator.PAREN; // operator ()
			consume();
			endOffset = consume(IToken.tRPAREN).getEndOffset();
			break;
		case IToken.tLBRACKET:
			op = OverloadableOperator.BRACKET; // operator []
			consume();
			endOffset = consume(IToken.tRBRACKET).getEndOffset();
			break;
		case IToken.t_new:
		case IToken.t_delete:
			if (lookaheadType(2) == IToken.tLBRACKET) {
				op = type == IToken.t_new ? OverloadableOperator.NEW_ARRAY : OverloadableOperator.DELETE_ARRAY;
				consume();
				consume();
				endOffset = consume(IToken.tRBRACKET).getEndOffset();
			} else {
				IToken t = consume();
				endOffset = t.getEndOffset();
				op = OverloadableOperator.valueOf(t);
			}
			break;
		case IToken.tGT_in_SHIFTR:
			consume();
			endOffset = consume(IToken.tGT_in_SHIFTR).getEndOffset();
			op = OverloadableOperator.SHIFTR;
			break;
		case IToken.tSTRING: // User defined literal T operator "" SUFFIX
		{
			IToken strOp = consume();

			// Should be an empty string
			if (strOp.getLength() == 2) {
				endOffset = strOp.getEndOffset();

				IToken udlOperator = identifierOrKeyword();

				char[] operatorName = CharArrayUtils.concat(firstToken.getCharImage(), " ".toCharArray()); //$NON-NLS-1$
				operatorName = CharArrayUtils.concat(operatorName, strOp.getCharImage());
				operatorName = CharArrayUtils.concat(operatorName, udlOperator.getCharImage());

				IASTName name = ((ICPPNodeFactory) nodeFactory).newOperatorName(operatorName);
				setRange(name, firstToken.getOffset(), udlOperator.getEndOffset());
				return name;
			}
			break;
		}
		case IToken.tUSER_DEFINED_STRING_LITERAL: // User defined literal T operator ""SUFFIX
		{
			IToken strOp = consume();
			String image = strOp.getImage();
			int startQuote = image.indexOf('"');
			int endQuote = image.lastIndexOf('"');
			if (startQuote != -1 && endQuote == startQuote + 1) {
				char[] operatorName = CharArrayUtils.concat(firstToken.getCharImage(), " ".toCharArray()); //$NON-NLS-1$
				operatorName = CharArrayUtils.concat(operatorName, strOp.getCharImage());
				IASTName name = ((ICPPNodeFactory) nodeFactory).newOperatorName(operatorName);
				setRange(name, firstToken.getOffset(), strOp.getEndOffset());
				return name;
			}
			break;
		}
		default:
			op = OverloadableOperator.valueOf(lookahead(1));
			if (op != null) {
				endOffset = consume().getEndOffset();
			}
			break;
		}

		if (op != null) {
			IASTName name = ((ICPPNodeFactory) nodeFactory).newOperatorName(op.toCharArray());
			setRange(name, firstToken.getOffset(), endOffset);
			return name;
		}

		// must be a conversion function
		typeId = typeID(DeclarationOptions.TYPEID_CONVERSION);

		IASTName name = ((ICPPNodeFactory) nodeFactory).newConversionName(typeId);
		setRange(name, firstToken.getOffset(), calculateEndOffset(typeId));
		return name;
	}

	private List<ICPPASTTemplateParameter> templateParameterList(List<ICPPASTTemplateParameter> result)
			throws EndOfFileException, BacktrackException {
		boolean needComma = false;
		for (;;) {
			final int type = lookaheadType(1);
			if (type == IToken.tGT || type == IToken.tEOC || type == IToken.tGT_in_SHIFTR) {
				return result;
			}

			if (needComma) {
				consume(IToken.tCOMMA);
			} else {
				needComma = true;
			}

			result.add(templateParameter());
		}
	}

	private ICPPASTTemplateParameter templateParameter() throws EndOfFileException, BacktrackException {
		final int ltype = lookaheadType(1);
		final IToken start = mark();
		if (ltype == IToken.t_class || ltype == IToken.t_typename) {
			try {
				int type = (ltype == IToken.t_class ? ICPPASTSimpleTypeTemplateParameter.st_class
						: ICPPASTSimpleTypeTemplateParameter.st_typename);
				boolean parameterPack = false;
				IASTName identifierName = null;
				IASTTypeId defaultValue = null;
				int endOffset = consume().getEndOffset();

				if (lookaheadType(1) == IToken.tELLIPSIS) {
					parameterPack = true;
					endOffset = consume().getOffset();
				}
				if (lookaheadType(1) == IToken.tIDENTIFIER) { // optional identifier
					identifierName = identifier();
					endOffset = calculateEndOffset(identifierName);
				} else {
					identifierName = nodeFactory.newName();
					setRange(identifierName, endOffset, endOffset);
				}
				if (lookaheadType(1) == IToken.tASSIGN) { // optional = type-id
					if (parameterPack)
						throw backtrack;
					consume();
					defaultValue = typeID(DeclarationOptions.TYPEID); // type-id
					endOffset = calculateEndOffset(defaultValue);
				}

				// Check if followed by comma
				switch (lookaheadType(1)) {
				case IToken.tGT:
				case IToken.tEOC:
				case IToken.tGT_in_SHIFTR:
				case IToken.tCOMMA:
					ICPPASTSimpleTypeTemplateParameter param = ((ICPPNodeFactory) nodeFactory)
							.newSimpleTypeTemplateParameter(type, identifierName, defaultValue);
					param.setIsParameterPack(parameterPack);
					setRange(param, start.getOffset(), endOffset);
					return param;
				}
			} catch (BacktrackException exception) {
			}
			// Can be a non-type template parameter, see bug 333285
			backup(start);
		} else if (ltype == IToken.t_template) {
			boolean parameterPack = false;
			IASTName identifierName = null;
			IASTExpression defaultValue = null;

			consume();
			consume(IToken.tLT);
			List<ICPPASTTemplateParameter> paramList = templateParameterList(new ArrayList<>());
			consume(IToken.tGT, IToken.tGT_in_SHIFTR);

			int kind = lookaheadType(1);
			if (kind != IToken.t_class && kind != IToken.t_typename) {
				throw backtrack;
			}

			int endOffset = consume(kind).getEndOffset();

			if (lookaheadType(1) == IToken.tELLIPSIS) {
				parameterPack = true;
				endOffset = consume().getOffset();
			}

			if (lookaheadType(1) == IToken.tIDENTIFIER) { // optional identifier
				identifierName = identifier();
				endOffset = calculateEndOffset(identifierName);
				if (lookaheadType(1) == IToken.tASSIGN) { // optional = type-id
					if (parameterPack)
						throw backtrack;

					consume();
					defaultValue = primaryExpression(CastExprCtx.NOT_IN_BINARY_EXPR, null);
					endOffset = calculateEndOffset(defaultValue);
				}
			} else {
				identifierName = nodeFactory.newName();
			}

			ICPPASTTemplatedTypeTemplateParameter param = ((ICPPNodeFactory) nodeFactory)
					.newTemplatedTypeTemplateParameter(identifierName, defaultValue);
			param.setIsParameterPack(parameterPack);
			param.setParameterType(kind == IToken.t_class ? ICPPASTTemplatedTypeTemplateParameter.tt_class
					: ICPPASTTemplatedTypeTemplateParameter.tt_typename);
			setRange(param, start.getOffset(), endOffset);

			for (int i = 0; i < paramList.size(); ++i) {
				ICPPASTTemplateParameter p = paramList.get(i);
				param.addTemplateParameter(p);
			}
			return param;
		}

		// Try non-type template parameter
		return parameterDeclaration();
	}

	private List<IASTNode> templateArgumentList(ITemplateIdStrategy strat)
			throws EndOfFileException, BacktrackException {
		if (templateArgumentNestingDepth++ >= TEMPLATE_ARGUMENT_NESTING_DEPTH_LIMIT) {
			throwBacktrack(createProblem(IProblem.TEMPLATE_ARGUMENT_NESTING_DEPTH_LIMIT_EXCEEDED,
					lookahead(1).getOffset(), 1));
		}
		try {
			int startingOffset = lookahead(1).getOffset();
			int endOffset = 0;
			List<IASTNode> list = null;

			boolean needComma = false;
			int type = lookaheadType(1);
			while (type != IToken.tGT && type != IToken.tGT_in_SHIFTR && type != IToken.tEOC) {
				if (needComma) {
					if (type != IToken.tCOMMA) {
						throwBacktrack(startingOffset, endOffset - startingOffset);
					}
					consume();
				} else {
					needComma = true;
				}

				IASTNode node = templateArgument(strat);
				if (list == null) {
					list = new ArrayList<>();
				}
				list.add(node);
				type = lookaheadType(1);
			}
			if (list == null) {
				return Collections.emptyList();
			}
			return list;
		} finally {
			--templateArgumentNestingDepth;
		}
	}

	private IASTNode templateArgument(ITemplateIdStrategy strategy) throws EndOfFileException, BacktrackException {
		IToken argStart = mark();
		int markBranchPoint = ((TemplateIdStrategy) strategy).getCurrentBranchPoint();
		ICPPASTTypeId typeId = null;
		int type = 0;
		try {
			typeId = typeID(DeclarationOptions.TYPEID, strategy);
			type = lookaheadType(1);
		} catch (BacktrackException e) {
			if (e.isFatal()) {
				throw e;
			}
		}

		if (typeId != null && (type == IToken.tCOMMA || type == IToken.tGT || type == IToken.tGT_in_SHIFTR
				|| type == IToken.tEOC || type == IToken.tELLIPSIS)) {
			// This is potentially a type-id, now check ambiguity with expression.
			IToken typeIdEnd = mark();
			IASTNamedTypeSpecifier namedTypeSpec = null;
			IASTName name = null;
			try {
				// If the type-id consists of a name, that name could be or contain
				// a template-id, with template arguments of its own, which can
				// themselves be ambiguous. If we parse the name anew as an
				// id-expression, our complexity becomes exponential in the nesting
				// depth of template-ids (bug 316704). To avoid this, we do not
				// re-parse the name, but instead synthesize an id-expression from
				// it, and then continue parsing an expression from the id-expression
				// onwards (as the id-expression could be the beginning of a larger
				// expression).
				IASTIdExpression idExpression = null;
				IASTDeclSpecifier declSpec = typeId.getDeclSpecifier();
				if (declSpec instanceof IASTNamedTypeSpecifier) {
					namedTypeSpec = (IASTNamedTypeSpecifier) declSpec;
					name = namedTypeSpec.getName();
					if (name.contains(typeId)) {
						idExpression = setRange(nodeFactory.newIdExpression(name), name);

						// If the name was one of the completion names, add it to the completion
						// node again now that it has a new parent. This ensures that completion
						// proposals are offered for both contexts that the name appears in.
						ASTCompletionNode completionNode = (ASTCompletionNode) getCompletionNode();
						if (completionNode != null && completionNode.containsName(name)) {
							completionNode.addName(name);
						}
					}
				}

				// Parse an expression, starting with the id-expression synthesized
				// above if there is one, otherwise starting from the beginning of
				// the argument.
				if (idExpression == null)
					backup(argStart);
				IASTExpression expression = expression(ExprKind.ASSIGNMENT, BinaryExprCtx.EXPR_IN_TEMPLATE_ID, idExpression,
						strategy);

				// At this point we have a valid type-id and a valid expression.
				// We prefer the longer one.
				if (!typeId.contains(expression)) {
					// The expression is longer.
					if (lookaheadType(1) == IToken.tELLIPSIS) {
						expression = createPackExpansion(expression, consume());
					}
					return expression;
				} else if (expression.contains(typeId)) {
					// The two are of the same length - ambiguous.
					if (lookaheadType(1) == IToken.tELLIPSIS) {
						IToken ellipsis = consume();
						createPackExpansion(typeId, ellipsis);
						expression = createPackExpansion(expression, ellipsis);
					}
					ICPPASTAmbiguousTemplateArgument ambiguity = createAmbiguousTemplateArgument();
					ambiguity.addTypeId(typeId);
					ambiguity.addExpression(expression);
					return ambiguity;
				}
				// The type-id is longer, use it.
			} catch (BacktrackException exception) {
				if (exception.isFatal()) {
					throw exception;
				}
				// Failed to parse an expression, use the type id.
			}

			// Clean up after our failed attempt to parse an expression.
			backup(typeIdEnd);
			if (name != null && namedTypeSpec != null) {
				// When we synthesized the id-expression, it took ownership
				// of the name. Give ownership back to the type-id.
				namedTypeSpec.setName(name);
			}

			// Use the type-id.
			if (lookaheadType(1) == IToken.tELLIPSIS) {
				createPackExpansion(typeId, consume());
			}
			return typeId;
		}

		// Not a type-id, parse as expression.
		backup(argStart);
		((TemplateIdStrategy) strategy).backupToBranchPoint(markBranchPoint);
		IASTExpression expr = expression(ExprKind.ASSIGNMENT, BinaryExprCtx.EXPR_IN_TEMPLATE_ID, null, strategy);
		if (lookaheadType(1) == IToken.tELLIPSIS) {
			expr = createPackExpansion(expr, consume());
		}
		return expr;
	}

	private IASTName registerTemplateArguments(IASTName templateName, ITemplateIdStrategy strat)
			throws EndOfFileException, BacktrackException {
		// Parse for template arguments
		consume(IToken.tLT);
		List<IASTNode> list = templateArgumentList(strat);
		IToken end = lookahead(1);
		switch (end.getType()) {
		case IToken.tGT_in_SHIFTR:
		case IToken.tGT:
			consume();
			break;
		case IToken.tEOC:
			break;
		default:
			throw backtrack;
		}
		return buildTemplateID(templateName, end.getEndOffset(), list);
	}

	private ICPPASTTemplateId buildTemplateID(IASTName templateName, int endOffset, List<IASTNode> args) {
		ICPPASTTemplateId result = ((ICPPNodeFactory) nodeFactory).newTemplateId(templateName);
		setRange(result, ((ASTNode) templateName).getOffset(), endOffset);
		for (IASTNode node : args) {
			if (node instanceof IASTTypeId) {
				result.addTemplateArgument((IASTTypeId) node);
			} else if (node instanceof IASTExpression) {
				result.addTemplateArgument((IASTExpression) node);
			} else if (node instanceof ICPPASTAmbiguousTemplateArgument) {
				result.addTemplateArgument((ICPPASTAmbiguousTemplateArgument) node);
			}
		}
		return result;
	}

	private IASTExpression lambdaExpression() throws EndOfFileException, BacktrackException {
		final int offset = lookahead().getOffset();

		ICPPASTLambdaExpression lambdaExpr = ((ICPPNodeFactory) nodeFactory).newLambdaExpression();

		// Lambda introducer
		consume(IToken.tLBRACKET);
		boolean needComma = false;
		switch (lookaheadType(1)) {
		case IToken.tASSIGN:
			lambdaExpr.setCaptureDefault(CaptureDefault.BY_COPY);
			consume();
			needComma = true;
			break;
		case IToken.tAMPER:
			final int type = lookaheadType(2);
			if (type == IToken.tCOMMA || type == IToken.tRBRACKET) {
				lambdaExpr.setCaptureDefault(CaptureDefault.BY_REFERENCE);
				consume();
				needComma = true;
			}
			break;
		}
		loop: for (;;) {
			switch (lookaheadType(1)) {
			case IToken.tEOC:
				return setRange(lambdaExpr, offset, lookahead().getEndOffset());
			case IToken.tRBRACKET:
				consume();
				break loop;
			}

			if (needComma) {
				consume(IToken.tCOMMA);
			}

			ICPPASTCapture capture = capture();
			lambdaExpr.addCapture(capture);
			needComma = true;
		}

		if (lookaheadType(1) == IToken.tLPAREN) {
			ICPPASTFunctionDeclarator dtor = methodDeclarator(true);
			lambdaExpr.setDeclarator(dtor);
			if (lookaheadType(1) == IToken.tEOC)
				return setRange(lambdaExpr, offset, calculateEndOffset(dtor));
		}

		IASTCompoundStatement body = methodBody();
		lambdaExpr.setBody(body);
		return setRange(lambdaExpr, offset, calculateEndOffset(body));
	}

	private ICPPASTCapture capture() throws EndOfFileException, BacktrackException {
		final int offset = lookahead().getOffset();
		ICPPASTCapture result = ((ICPPNodeFactory) nodeFactory).newCapture();
		boolean referenceCapture = false;

		switch (lookaheadType(1)) {
		case IToken.t_this:
			result.setIsByReference(true);
			return setRange(result, offset, consume().getEndOffset());
		case IToken.tAMPER:
			consume();
			referenceCapture = true;
			break;
		case IToken.tSTAR:
			if (lookaheadType(2) == IToken.t_this) {
				consume();
				return setRange(result, offset, consume().getEndOffset());
			}
			break;
		}

		final IASTName identifier = identifier();
		result.setIdentifier(identifier);
		result.setIsByReference(referenceCapture);
		setRange(result, offset, calculateEndOffset(identifier));

		switch (lookaheadType(1)) {
		case IToken.tASSIGN:
			result = createInitCapture(identifier, equalsInitalizerClause(false), referenceCapture, offset);
			break;
		case IToken.tLBRACE:
		case IToken.tLPAREN:
			result = createInitCapture(identifier, bracedOrConstructorStyleInitializer(), referenceCapture, offset);
			break;
		}

		if (lookaheadType(1) == IToken.tELLIPSIS) {
			// Note this will probably change with C++20 such that the
			// pack expansion of a CPPASTInitCapture will be part of the IASTDeclarator
			// and not the capture. [See: P0780R2]
			result.setIsPackExpansion(true);
			return setRange(result, offset, consume().getEndOffset());
		}

		return result;
	}

	private ICPPASTInitCapture createInitCapture(IASTName identifier, IASTInitializer initializer, boolean isReference,
			int offset) throws EndOfFileException, BacktrackException {
		ICPPASTDeclarator declarator = ((ICPPNodeFactory) nodeFactory).newDeclarator(identifier);
		declarator.setInitializer(initializer);
		if (isReference) {
			declarator.addPointerOperator(((ICPPNodeFactory) nodeFactory).newReferenceOperator(false));
		}
		setRange(declarator, offset, calculateEndOffset(initializer));
		ICPPASTInitCapture initCapture = ((ICPPNodeFactory) nodeFactory).newInitCapture(declarator);
		return setRange(initCapture, offset, calculateEndOffset(initializer));
	}

	/**
	 * Parses a decltype-specifier.
	 */
	private ICPPASTDecltypeSpecifier decltypeSpecifier() throws EndOfFileException, BacktrackException {
		int start = consume(IToken.t_decltype).getOffset();
		consume(IToken.tLPAREN);
		ICPPASTExpression decltypeExpression = expression();
		int end = consume(IToken.tRPAREN).getEndOffset();
		ICPPASTDecltypeSpecifier decltypeSpec = ((ICPPNodeFactory) nodeFactory)
				.newDecltypeSpecifier(decltypeExpression);
		setRange(decltypeSpec, start, end);
		return decltypeSpec;
	}

	private void registerNameSpecifier(ICPPASTQualifiedName qname, ICPPASTNameSpecifier nameSpec) {
		if (nameSpec instanceof IASTName)
			qname.addName((IASTName) nameSpec);
		else
			qname.addNameSpecifier(nameSpec);
	}

	private IASTName buildName(int destructorOffset, IToken token, boolean keywordTemplate) {
		IASTName name;
		if (destructorOffset < 0) {
			if (keywordTemplate) {
				name = ((ICPPNodeFactory) nodeFactory).newTemplateName(token.getCharImage());
			} else {
				name = nodeFactory.newName(token.getCharImage());
			}
			setRange(name, token.getOffset(), token.getEndOffset());
		} else {
			char[] nchars = token.getCharImage();
			final int len = nchars.length;
			char[] image = new char[len + 1];
			image[0] = '~';
			System.arraycopy(nchars, 0, image, 1, len);
			name = nodeFactory.newName(image);
			setRange(name, destructorOffset, token.getEndOffset());
		}
		switch (token.getType()) {
		case IToken.tEOC:
		case IToken.tCOMPLETION:
			ASTCompletionNode node = createCompletionNode(token);
			if (node != null)
				node.addName(name);
			break;
		}
		return name;
	}

	private void configureDeclSpec(ICPPASTDeclSpecifier declSpec, int storageClass, int options) {
		declSpec.setStorageClass(storageClass);
		declSpec.setConst((options & CONST) != 0);
		declSpec.setConstexpr((options & CONSTEXPR) != 0);
		declSpec.setVolatile((options & VOLATILE) != 0);
		declSpec.setInline((options & INLINE) != 0);
		declSpec.setFriend((options & FRIEND) != 0);
		declSpec.setVirtual((options & VIRTUAL) != 0);
		declSpec.setExplicit((options & EXPLICIT) != 0);
		declSpec.setThreadLocal((options & THREAD_LOCAL) != 0);
	}

	private ICPPASTDeclSpecifier enumDeclaration(boolean allowOpaque) throws BacktrackException, EndOfFileException {
		IToken mark = mark();
		final int offset = consume(IToken.t_enum).getOffset();
		int endOffset = 0;
		boolean isScoped = false;
		ScopeStyle scopeStyle = ScopeStyle.NONE;
		IASTName name = null;
		ICPPASTDeclSpecifier baseType = null;
		List<IASTAttributeSpecifier> attributes = null;

		try {
			int lt1 = lookaheadType(1);
			if (lt1 == IToken.t_class || lt1 == IToken.t_struct) {
				scopeStyle = (lt1 == IToken.t_class) ? ScopeStyle.CLASS : ScopeStyle.STRUCT;
				isScoped = true;
				consume();
			}

			attributes = attributes();

			if (isScoped || lookaheadType(1) == IToken.tIDENTIFIER) {
				// A qualified-name can appear here if an enumeration declared at class scope is
				// being defined out of line.
				name = qualifiedName();
				endOffset = calculateEndOffset(name);
			}

			if (lookaheadType(1) == IToken.tCOLON) {
				consume();
				baseType = simpleTypeSpecifierSequence();
				endOffset = calculateEndOffset(baseType);
			}
		} catch (BacktrackException exception) {
			backup(mark);
			return elaboratedTypeSpecifier();
		}

		final int type = lookaheadType(1);
		final boolean isDef = type == IToken.tLBRACE || (type == IToken.tEOC && baseType != null);
		final boolean isOpaque = !isDef && allowOpaque && type == IToken.tSEMI;
		if (!isDef && !isOpaque) {
			backup(mark);
			return elaboratedTypeSpecifier();
		}
		mark = null;

		if (isOpaque && !isScoped && baseType == null)
			throwBacktrack(lookahead(1));

		if (name == null) {
			if (isOpaque)
				throwBacktrack(lookahead(1));
			name = nodeFactory.newName();
		}

		final ICPPASTEnumerationSpecifier result = ((ICPPNodeFactory) nodeFactory).newEnumerationSpecifier(scopeStyle,
				name, baseType);
		result.setIsOpaque(isOpaque);
		if (type == IToken.tLBRACE) {
			endOffset = enumBody(result);
		}
		assert endOffset != 0;
		addAttributeSpecifiers(attributes, result);
		return setRange(result, offset, endOffset);
	}

	private ICPPASTDeclSpecifier simpleTypeSpecifierSequence() throws BacktrackException, EndOfFileException {
		Declaration declaration = declarationSpecifierSequence(null, false, null);
		return (ICPPASTDeclSpecifier) declaration.leftSpecifier;
	}

	/**
	 * Parses the initDeclarator construct of the ANSI C++ spec. initDeclarator :
	 * declarator ("=" initializerClause | "(" expressionList ")")?
	 *
	 * @return declarator that this parsing produced.
	 * @throws BacktrackException request a backtrack
	 * @throws FoundAggregateInitializer
	 */
	private IASTDeclarator initDeclarator(DeclaratorStrategy strategy, IASTDeclSpecifier declspec,
			DeclarationOptions option) throws EndOfFileException, BacktrackException, FoundAggregateInitializer {
		final IASTDeclarator dtor = declarator(strategy, option);
		if (option.fAllowInitializer) {
			final IASTDeclarator typeRelevantDeclarator = ASTQueries.findTypeRelevantDeclarator(dtor);
			if (option != DeclarationOptions.PARAMETER && typeRelevantDeclarator instanceof IASTFunctionDeclarator) {
				// Function declarations don't have initializers.
				// For member functions we need to consider virtual specifiers and pure-virtual syntax.
				if (option == DeclarationOptions.CPP_MEMBER) {
					optionalVirtualSpecifierSequence((ICPPASTFunctionDeclarator) typeRelevantDeclarator);
					int lt1 = lookaheadTypeWithEndOfFile(1);
					if (lt1 == IToken.tASSIGN && lookaheadTypeWithEndOfFile(2) == IToken.tINTEGER) {
						consume();
						IToken t = consume();
						char[] image = t.getCharImage();
						if (image.length != 1 || image[0] != '0') {
							throwBacktrack(t);
						}
						((ICPPASTFunctionDeclarator) typeRelevantDeclarator).setPureVirtual(true);
						adjustEndOffset(dtor, t.getEndOffset()); // We can only adjust the offset of the outermost dtor.
					}
				}
			} else {
				if (lookaheadTypeWithEndOfFile(1) == IToken.tASSIGN && lookaheadTypeWithEndOfFile(2) == IToken.tLBRACE)
					throw new FoundAggregateInitializer(declspec, dtor);

				IASTInitializer initializer = optionalInitializer(dtor, option);
				if (initializer != null) {
					if (initializer instanceof IASTInitializerList
							&& ((IASTInitializerList) initializer).getSize() == 0) {
						// Avoid ambiguity between constructor with body and variable with initializer
						switch (lookaheadTypeWithEndOfFile(1)) {
						case IToken.tCOMMA:
						case IToken.tSEMI:
						case IToken.tRPAREN:
							break;
						case 0:
							throw backtrack;
						default:
							throwBacktrack(lookahead(1));
						}
					}
					dtor.setInitializer(initializer);
					adjustLength(dtor, initializer);
				}
			}
		}
		return dtor;
	}

	private ICPPASTExpression expression(final ExprKind kind, final BinaryExprCtx ctx, IASTInitializerClause expr,
			ITemplateIdStrategy strat) throws EndOfFileException, BacktrackException {
		final boolean allowComma = kind == ExprKind.EXPRESSION;
		boolean allowAssignment = kind != ExprKind.CONSTANT;

		if (allowAssignment && lookaheadType(1) == IToken.t_throw) {
			return throwExpression();
		}

		final int startOffset = expr != null ? ((ASTNode) expr).getOffset() : lookahead(1).getOffset();
		int type;
		int conditionCount = 0;
		BinaryOperator lastOperator = null;
		NameOrTemplateIDVariants variants = null;

		IToken variantMark = mark();
		if (expr == null) {
			Object expression = castExpressionForBinaryExpression(strat, ctx);
			if (expression instanceof IASTExpression) {
				expr = (IASTExpression) expression;
			} else {
				variants = new NameOrTemplateIDVariants();

				final Variant variant = (Variant) expression;
				expr = variant.getExpression();
				variants.addBranchPoint(variant.getNext(), lastOperator, allowAssignment, conditionCount);
			}
		}

		boolean stopWithNextOperator = false;
		castExprLoop: for (;;) {
			// Typically after a binary operator there cannot be a throw expression
			boolean allowThrow = false;
			// Brace initializers are allowed on the right hand side of an expression
			boolean allowBraceInitializer = false;

			boolean doneExpression = false;
			BacktrackException failure = null;
			final int opOffset = lookahead().getOffset();
			type = stopWithNextOperator ? IToken.tSEMI : lookaheadType(1);
			switch (type) {
			case IToken.tQUESTION:
				conditionCount++;
				// <logical-or> ? <expression> : <assignment-expression>
				// Precedence: 25 is lower than precedence of logical or; 0 is lower than precedence of expression
				lastOperator = new BinaryOperator(lastOperator, expr, type, 25, 0);
				allowAssignment = true; // assignment expressions will be subsumed by the conditional expression
				allowThrow = true;
				break;

			case IToken.tCOLON:
				if (--conditionCount < 0) {
					doneExpression = true;
				} else {
					// <logical-or> ? <expression> : <assignment-expression>
					// Precedence: 0 is lower than precedence of expression; 15 is lower than precedence of assignment;
					lastOperator = new BinaryOperator(lastOperator, expr, type, 0, 15);
					allowAssignment = true; // assignment expressions will be subsumed by the conditional expression
					allowThrow = true;
				}
				break;

			case IToken.tCOMMA:
				allowThrow = true;
				if (!allowComma && conditionCount == 0) {
					doneExpression = true;
				} else {
					// Lowest precedence except inside the conditional expression
					lastOperator = new BinaryOperator(lastOperator, expr, type, 10, 11);
				}
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
				if (!allowAssignment && conditionCount == 0) {
					doneExpression = true;
				} else {
					// Assignments group right to left
					lastOperator = new BinaryOperator(lastOperator, expr, type, 21, 20);
					allowBraceInitializer = true;
				}
				break;

			case IToken.tOR:
				lastOperator = new BinaryOperator(lastOperator, expr, type, 30, 31);
				break;
			case IToken.tAND:
				lastOperator = new BinaryOperator(lastOperator, expr, type, 40, 41);
				break;
			case IToken.tBITOR:
				lastOperator = new BinaryOperator(lastOperator, expr, type, 50, 51);
				break;
			case IToken.tXOR:
				lastOperator = new BinaryOperator(lastOperator, expr, type, 60, 61);
				break;
			case IToken.tAMPER:
				lastOperator = new BinaryOperator(lastOperator, expr, type, 70, 71);
				break;
			case IToken.tEQUAL:
			case IToken.tNOTEQUAL:
				lastOperator = new BinaryOperator(lastOperator, expr, type, 80, 81);
				break;
			case IToken.tGT:
				if (ctx == BinaryExprCtx.EXPR_IN_TEMPLATE_ID) {
					doneExpression = true;
					break;
				}
				//$FALL-THROUGH$
			case IToken.tLT:
			case IToken.tLTEQUAL:
			case IToken.tGTEQUAL:
				lastOperator = new BinaryOperator(lastOperator, expr, type, 90, 91);
				break;
			case IToken.tTHREEWAYCOMPARISON:
				lastOperator = new BinaryOperator(lastOperator, expr, type, 95, 96);
				break;
			case IToken.tGT_in_SHIFTR:
				if (ctx == BinaryExprCtx.EXPR_IN_TEMPLATE_ID) {
					doneExpression = true;
					break;
				}
				if (lookaheadType(2) != IToken.tGT_in_SHIFTR) {
					IToken token = lookahead(1);
					backtrack.initialize(token.getOffset(), token.getLength());
					failure = backtrack;
					break;
				}

				type = IToken.tSHIFTR; // convert back
				consume(); // consume the extra token
				//$FALL-THROUGH$
			case IToken.tSHIFTL:
			case IToken.tSHIFTR:
				lastOperator = new BinaryOperator(lastOperator, expr, type, 100, 101);
				break;
			case IToken.tPLUS:
			case IToken.tMINUS:
				lastOperator = new BinaryOperator(lastOperator, expr, type, 110, 111);
				break;
			case IToken.tSTAR:
			case IToken.tDIV:
			case IToken.tMOD:
				lastOperator = new BinaryOperator(lastOperator, expr, type, 120, 121);
				break;
			case IToken.tDOTSTAR:
			case IToken.tARROWSTAR:
				lastOperator = new BinaryOperator(lastOperator, expr, type, 130, 131);
				break;
			default:
				doneExpression = true;
				break;
			}

			// Close variants
			if (failure == null) {
				if (doneExpression) {
					if (variants != null && !variants.hasRightBound(opOffset)) {
						// We have a longer variant, ignore this one.
						backtrack.initialize(opOffset, 1);
						failure = backtrack;
					} else {
						break castExprLoop;
					}
				}
				// Close variants with matching end
				if (variants != null && lastOperator != null) {
					variants.closeVariants(opOffset, lastOperator);
				}
			}

			if (failure == null && !doneExpression) {
				// Determine next cast-expression
				consume(); // consumes the operator
				stopWithNextOperator = false;
				try {
					if (allowThrow && lookaheadType(1) == IToken.t_throw) {
						// Throw expression
						expr = throwExpression();
						type = lookaheadType(1);
						if (type != IToken.tCOLON && type != IToken.tCOMMA)
							stopWithNextOperator = true;
					} else if (allowBraceInitializer && lookaheadType(1) == IToken.tLBRACE) {
						// Brace initializer
						expr = bracedInitList(true, false);
						type = lookaheadType(1);
						if (type != IToken.tCOLON && type != IToken.tCOMMA)
							stopWithNextOperator = true;
					} else {
						// Could be ellipsis of any right fold expression or ellipsis of binary left fold expression
						Object expression = castExpressionForBinaryExpression(strat, ctx);

						if (expression instanceof IASTExpression) {
							expr = (IASTExpression) expression;
						} else {
							final Variant ae = (Variant) expression;
							expr = ae.getExpression();
							if (variants == null)
								variants = new NameOrTemplateIDVariants();

							variants.addBranchPoint(ae.getNext(), lastOperator, allowAssignment, conditionCount);
						}
					}
					continue castExprLoop;
				} catch (BacktrackException exception) {
					failure = exception;
				}
			}

			// We need a new variant
			Variant variant = variants == null ? null : variants.selectFallback();
			if (variant == null) {
				if (failure != null)
					throw failure;
				throwBacktrack(lookahead(1));
			} else {
				// Restore variant and continue
				BranchPoint varPoint = variant.getOwner();
				allowAssignment = varPoint.isAllowAssignment();
				conditionCount = varPoint.getConditionCount();
				lastOperator = (BinaryOperator) varPoint.getLeftOperator();
				expr = variant.getExpression();

				backup(variantMark);
				int offset = variant.getRightOffset();
				while (lookahead().getOffset() < offset) {
					consume();
				}
				variantMark = mark();
			}
		}

		// Check for incomplete conditional expression
		if (type != IToken.tEOC && conditionCount > 0)
			throwBacktrack(lookahead(1));

		if (variants != null) {
			BinaryOperator end = new BinaryOperator(lastOperator, expr, -1, 0, 0);
			variants.closeVariants(lookahead(1).getOffset(), end);
			variants.removeInvalid(end);
			if (!variants.isEmpty()) {
				CPPASTTemplateIDAmbiguity result = new CPPASTTemplateIDAmbiguity(this, end,
						variants.getOrderedBranchPoints());
				setRange(result, startOffset, calculateEndOffset(expr));
				return result;
			}
		}

		return (ICPPASTExpression) buildExpression(lastOperator, expr);
	}

	private ICPPASTExpression throwExpression() throws EndOfFileException, BacktrackException {
		IToken throwToken = consume();
		IASTExpression throwExpression = null;
		try {
			throwExpression = expression();
		} catch (BacktrackException exception) {
			backup(throwToken);
			consume();
		}
		int o = throwExpression != null ? calculateEndOffset(throwExpression) : throwToken.getEndOffset();
		return (ICPPASTExpression) buildUnaryExpression(ICPPASTUnaryExpression.op_throw, throwExpression,
				throwToken.getOffset(), o); // fix for 95225
	}

	/**
	 * Makes a fast check whether there could be template arguments.
	 * -1: no, 0: ambiguous, 1: yes
	 */
	private static final int NO_TEMPLATE_ID = -1;
	private static final int AMBIGUOUS_TEMPLATE_ID = 0;
	private static final int TEMPLATE_ID = 1;

	private int haveTemplateArguments(boolean inBinaryExpression) throws EndOfFileException, BacktrackException {
		final IToken mark = mark();
		try {
			consume();
			int nk = 0;
			int depth = 0;
			int angleDepth = 1;
			int limit = 10000;

			while (--limit > 0) {
				switch (consume().getType()) {
				case IToken.tEOC:
				case IToken.tCOMPLETION:
					return AMBIGUOUS_TEMPLATE_ID;

				case IToken.tLT:
					if (nk == 0) {
						angleDepth++;
					}
					break;
				case IToken.tGT_in_SHIFTR:
				case IToken.tGT:
					if (nk == 0) {
						--angleDepth;
						if (!inBinaryExpression)
							return angleDepth == 0 ? TEMPLATE_ID : AMBIGUOUS_TEMPLATE_ID;

						int end = endsTemplateIDInBinaryExpression();
						if (end == NO_TEMPLATE_ID) {
							if (angleDepth == 0)
								return NO_TEMPLATE_ID;
						} else {
							return AMBIGUOUS_TEMPLATE_ID;
						}
					}
					break;
				case IToken.tLBRACKET:
					if (nk == 0) {
						nk = IToken.tLBRACKET;
						depth = 0;
					} else if (nk == IToken.tLBRACKET) {
						depth++;
					}
					break;
				case IToken.tRBRACKET:
					if (nk == IToken.tLBRACKET) {
						if (--depth < 0) {
							nk = 0;
						}
					}
					break;
				case IToken.tLPAREN:
					if (nk == 0) {
						nk = IToken.tLPAREN;
						depth = 0;
					} else if (nk == IToken.tLPAREN) {
						depth++;
					}
					break;

				case IToken.tRPAREN:
					if (nk == IToken.tLPAREN) {
						if (--depth < 0) {
							nk = 0;
						}
					}
					break;

				// In C++11, braces can occur at the top level in a template-argument,
				// if an object of class type is being created via uniform initialization,
				// and that class type has a constexpr conversion operator to a type
				// that's valid as the type of a non-type template parameter.
				case IToken.tLBRACE:
					if (nk == 0) {
						nk = IToken.tLBRACE;
						depth = 0;
					} else if (nk == IToken.tLBRACE) {
						depth++;
					}
					break;

				case IToken.tRBRACE:
					if (nk == 0) {
						return NO_TEMPLATE_ID;
					} else if (nk == IToken.tLBRACE) {
						if (--depth < 0) {
							nk = 0;
						}
					}
					break;

				case IToken.tSEMI:
					if (nk == 0) {
						return NO_TEMPLATE_ID;
					}
					break;
				}
			}
			return AMBIGUOUS_TEMPLATE_ID;
		} catch (EndOfFileException ignored) {
			return NO_TEMPLATE_ID;
		} finally {
			backup(mark);
		}
	}

	/**
	 * If '>' is followed by an expression, then it denotes the binary operator,
	 * else it is the end of a template-id, or special-cast.
	 */
	private int endsTemplateIDInBinaryExpression() {
		int type = lookaheadTypeWithEndOfFile(1);
		switch (type) {
		// Can be start of expression, or the scope operator applied to the template-id
		case IToken.tCOLONCOLON: // 'CT<int>::member' or 'c<1 && 2 > ::g'
			return AMBIGUOUS_TEMPLATE_ID;

		// Can be start of expression or the function-call operator applied to a template-id
		case IToken.tLPAREN: // 'ft<int>(args)' or 'c<1 && 2 > (x+y)'
			return AMBIGUOUS_TEMPLATE_ID;

		// Start of unary expression
		case IToken.tMINUS:
		case IToken.tPLUS:
		case IToken.tAMPER:
		case IToken.tSTAR:
		case IToken.tNOT:
		case IToken.tBITCOMPLEMENT:
		case IToken.tINCR:
		case IToken.tDECR:
		case IToken.t_new:
		case IToken.t_delete:
		case IToken.t_sizeof:
		case IToken.t_alignof:
			return NO_TEMPLATE_ID;

		// Start of a postfix expression
		case IToken.t_typename:
		case IToken.t_char:
		case IToken.t_char16_t:
		case IToken.t_char32_t:
		case IToken.t_wchar_t:
		case IToken.t_bool:
		case IToken.t_short:
		case IToken.t_int:
		case IToken.t_long:
		case IToken.t_signed:
		case IToken.t_unsigned:
		case IToken.t_float:
		case IToken.t_double:
		case IToken.t_dynamic_cast:
		case IToken.t_static_cast:
		case IToken.t_reinterpret_cast:
		case IToken.t_const_cast:
		case IToken.t_typeid:
			return NO_TEMPLATE_ID;

		// Start of a primary expression
		case IToken.tINTEGER:
		case IToken.tFLOATINGPT:
		case IToken.tSTRING:
		case IToken.tLSTRING:
		case IToken.tUTF16STRING:
		case IToken.tUTF32STRING:
		case IToken.tCHAR:
		case IToken.tLCHAR:
		case IToken.tUTF16CHAR:
		case IToken.tUTF32CHAR:
		case IToken.t_false:
		case IToken.t_true:
		case IToken.t_this:
		case IToken.tIDENTIFIER:
		case IToken.t_operator:
		case IToken.tCOMPLETION:
			return NO_TEMPLATE_ID;

		// Tokens that end an expression
		case IToken.tSEMI:
		case IToken.tCOMMA:
		case IToken.tLBRACE:
		case IToken.tRBRACE:
		case IToken.tRBRACKET:
		case IToken.tRPAREN:
		case IToken.tELLIPSIS: // pack-expansion
		case IToken.t_struct:
		case IToken.t_class:
		case IToken.t_template:
			return TEMPLATE_ID;

		// Binary operator
		case IToken.tGT:
		case IToken.tGT_in_SHIFTR:
		case IToken.tEQUAL:
			return TEMPLATE_ID;

		default:
			return AMBIGUOUS_TEMPLATE_ID;
		}
	}

	private IASTExpression buildFoldExpressionToken(int firstOffset, int lastOffset) {
		IASTExpression result = ((ICPPNodeFactory) nodeFactory).newFoldExpressionToken();
		((ASTNode) result).setOffsetAndLength(firstOffset, lastOffset - firstOffset);
		return result;
	}

	private IASTExpression createPackExpansion(IASTExpression expr, IToken ellipsis) {
		IASTExpression result = ((ICPPNodeFactory) nodeFactory).newPackExpansionExpression(expr);
		return setRange(result, expr, ellipsis.getEndOffset());
	}

	private void createPackExpansion(ICPPASTTypeId id, IToken consume) {
		final int endOffset = consume.getEndOffset();
		adjustEndOffset(id, endOffset);
		id.setIsPackExpansion(true);
	}

	/**
	 * braced-init-list:
	 *	 { initializer-list ,opt }
	 *	 { }
	 */
	private ICPPASTInitializerList bracedInitList(boolean allowSkipping, boolean allowDesignators)
			throws EndOfFileException, BacktrackException {
		int offset = consume(IToken.tLBRACE).getOffset();

		// { }
		if (lookaheadType(1) == IToken.tRBRACE) {
			return setRange((ICPPASTInitializerList) nodeFactory.newInitializerList(), offset,
					consume().getEndOffset());
		}

		// { initializer-list ,opt }
		List<ICPPASTInitializerClause> initList = initializerList(allowSkipping, allowDesignators);
		if (lookaheadType(1) == IToken.tCOMMA)
			consume();

		int endOffset = consumeOrEndOfCompletion(IToken.tRBRACE).getEndOffset();
		ICPPASTInitializerList result = (ICPPASTInitializerList) nodeFactory.newInitializerList();
		for (IASTInitializerClause init : initList) {
			result.addClause(init);
		}
		return setRange(result, offset, endOffset);
	}

	/**
	 * initializerList:
	 *	initializer-clause ...opt
	 *	initializer-list , initializer-clause ...opt
	 */
	private List<ICPPASTInitializerClause> initializerList(boolean allowSkipping, boolean allowDesignators)
			throws EndOfFileException, BacktrackException {
		List<ICPPASTInitializerClause> result = new ArrayList<>();
		// List of initializer clauses
		loop: while (true) {
			List<ICPPASTDesignator> designators = null;
			IToken mark = mark();
			if (allowDesignators) {
				designators = designatorList();
			}

			if (designators != null) {
				try {
					ICPPASTDesignatedInitializer desigInitializer = ((ICPPNodeFactory) nodeFactory)
							.newDesignatedInitializer(null);
					setRange(desigInitializer, designators.get(0));
					for (ICPPASTDesignator designator : designators) {
						desigInitializer.addDesignator(designator);
					}

					if (lookaheadType(1) != IToken.tEOC) {
						consume(IToken.tASSIGN);

						ICPPASTInitializerClause clause = initClause(false);
						desigInitializer.setOperand(clause);
						adjustLength(desigInitializer, clause);
					}
					result.add(desigInitializer);
				} catch (BacktrackException exception) {
					backup(mark);
					designators = null; // Retry without designators.
				}
			}

			if (designators == null) {
				// Clause may be null, add to initializer anyways, so that the size can be computed.
				ICPPASTInitializerClause clause = initClause(allowSkipping);
				if (allowSkipping && result.size() >= Integer.MAX_VALUE && !ASTQueries.canContainName(clause)) {
					compilationUnit.setHasNodesOmitted(true);
					clause = null;
				}
				if (lookaheadType(1) == IToken.tELLIPSIS) {
					final int endOffset = consume(IToken.tELLIPSIS).getEndOffset();
					if (clause instanceof ICPPASTPackExpandable) {
						// Mark initializer lists directly as pack expansions
						((ICPPASTPackExpandable) clause).setIsPackExpansion(true);
						adjustEndOffset(clause, endOffset);
					} else if (clause instanceof IASTExpression) {
						// Wrap pack expanded assignment expressions
						ICPPASTExpression packExpansion = ((ICPPNodeFactory) nodeFactory)
								.newPackExpansionExpression((IASTExpression) clause);
						clause = setRange(packExpansion, clause, endOffset);
					}
				}
				result.add(clause);
			}

			if (lookaheadType(1) != IToken.tCOMMA)
				break;
			switch (lookaheadType(2)) {
			case IToken.tRBRACE:
			case IToken.tRPAREN:
			case IToken.tEOC:
				break loop;
			}
			consume(IToken.tCOMMA);
		}
		return result;
	}

	private List<ICPPASTDesignator> designatorList() throws EndOfFileException, BacktrackException {
		IToken mark = mark();
		try {
			final int type = lookaheadType(1);
			if (type == IToken.tDOT || type == IToken.tLBRACKET) {
				List<ICPPASTDesignator> designatorList = null;
				while (true) {
					switch (lookaheadType(1)) {
					case IToken.tDOT:
						int offset = consume().getOffset();
						IASTName n = identifier();
						ICPPASTFieldDesignator fieldDesignator = ((ICPPNodeFactory) nodeFactory).newFieldDesignator(n);
						setRange(fieldDesignator, offset, calculateEndOffset(n));
						if (designatorList == null)
							designatorList = new ArrayList<>(DEFAULT_DESIGNATOR_LIST_SIZE);
						designatorList.add(fieldDesignator);
						break;

					case IToken.tLBRACKET:
						offset = consume().getOffset();
						ICPPASTExpression constantExpression = expression(); {
						int lastOffset = consume(IToken.tRBRACKET).getEndOffset();
						ICPPASTArrayDesignator designator = ((ICPPNodeFactory) nodeFactory)
								.newArrayDesignator(constantExpression);
						setRange(designator, offset, lastOffset);
						if (designatorList == null)
							designatorList = new ArrayList<>(DEFAULT_DESIGNATOR_LIST_SIZE);
						designatorList.add(designator);
					}
						break;

					default:
						return designatorList;
					}
				}
			}
		} catch (BacktrackException exception) {
			backup(mark);
		}

		return null;
	}

	/**
	 * initializer-clause:
	 *   assignment-expression
	 *   braced-init-list
	 */
	private ICPPASTInitializerClause initClause(boolean allowSkipping) throws EndOfFileException, BacktrackException {
		// braced-init-list
		if (lookaheadType(1) == IToken.tLBRACE) {
			return bracedInitList(allowSkipping, true);
		}

		// assignment expression
		TemplateIdStrategy strategy = templateParameterListStrategy;
		final BinaryExprCtx context = strategy != null ? BinaryExprCtx.EXPR_IN_TEMPLATE_ID : BinaryExprCtx.EXPR_NOT_IN_TEMPLATE_ID;
		return expression(ExprKind.ASSIGNMENT, context, null, strategy);
	}

	private ICPPASTFunctionDefinition methodDefinition(final int firstOffset, IASTDeclSpecifier declSpec,
			IASTDeclarator outerDtor) throws EndOfFileException, BacktrackException {
		final IASTDeclarator dtor = ASTQueries.findTypeRelevantDeclarator(outerDtor);
		if (!(dtor instanceof ICPPASTFunctionDeclarator))
			throwBacktrack(firstOffset, lookahead(1).getEndOffset() - firstOffset);

		ICPPASTFunctionDefinition fdef;
		if (lookaheadType(1) == IToken.t_try) {
			consume();
			fdef = ((ICPPNodeFactory) nodeFactory).newFunctionTryBlock(declSpec, (ICPPASTFunctionDeclarator) dtor,
					null);
		} else {
			fdef = (ICPPASTFunctionDefinition) nodeFactory.newFunctionDefinition(declSpec,
					(ICPPASTFunctionDeclarator) dtor, null);
		}
		if (lookaheadType(1) == IToken.tASSIGN) {
			consume();
			IToken kind = consume();
			switch (kind.getType()) {
			case IToken.t_default:
				fdef.setIsDefaulted(true);
				break;
			case IToken.t_delete:
				fdef.setIsDeleted(true);
				break;
			default:
				throwBacktrack(kind);
			}
			return setRange(fdef, firstOffset, consume(IToken.tSEMI).getEndOffset());
		}

		if (lookaheadType(1) == IToken.tCOLON) {
			constructorInitializer(fdef);
		}

		try {
			IASTStatement body = handleMethodBody();
			fdef.setBody(body);
			setRange(fdef, firstOffset, calculateEndOffset(body));
		} catch (BacktrackException exception) {
			final IASTNode node = exception.getNodeBeforeProblem();
			if (node instanceof IASTCompoundStatement && !(fdef instanceof ICPPASTFunctionWithTryBlock)) {
				fdef.setBody((IASTCompoundStatement) node);
				setRange(fdef, firstOffset, calculateEndOffset(node));
				throwBacktrack(exception.getProblem(), fdef);
			}
			throw exception;
		}

		if (fdef instanceof ICPPASTFunctionWithTryBlock) {
			ICPPASTFunctionWithTryBlock tryblock = (ICPPASTFunctionWithTryBlock) fdef;
			List<ICPPASTCatchHandler> handlers = new ArrayList<>(DEFAULT_CATCH_HANDLER_LIST_SIZE);
			catchHandlerSequence(handlers);
			ICPPASTCatchHandler last = null;
			for (ICPPASTCatchHandler catchHandler : handlers) {
				tryblock.addCatchHandler(catchHandler);
				last = catchHandler;
			}
			if (last != null) {
				adjustLength(tryblock, last);
			}
		}
		return fdef;
	}

	private List<ICPPASTInitializerClause> expressionList() throws EndOfFileException, BacktrackException {
		return initializerList(false, false);
	}

	private RefQualifier optionalRefQualifier() throws EndOfFileException {
		int nextToken = lookaheadType(1);
		switch (nextToken) {
		case IToken.tAMPER:
			consume();
			return RefQualifier.LVALUE;

		case IToken.tAND:
			consume();
			return RefQualifier.RVALUE;

		default:
			return null;
		}
	}

	private IASTInitializer bracedOrConstructorStyleInitializer() throws EndOfFileException, BacktrackException {
		final int type = lookaheadType(1);
		if (type == IToken.tLPAREN) {
			return constructorStyleInitializer(true);
		}
		return bracedInitList(false, true);
	}

	/**
	 * (expression-list_opt)
	 */
	private ICPPASTConstructorInitializer constructorStyleInitializer(boolean optionalExpressionList)
			throws EndOfFileException, BacktrackException {
		IASTInitializerClause[] initArray;
		int offset = consume(IToken.tLPAREN).getOffset();

		// ()
		if (optionalExpressionList && lookaheadType(1) == IToken.tRPAREN) {
			initArray = IASTExpression.EMPTY_EXPRESSION_ARRAY;
		} else {
			final List<ICPPASTInitializerClause> exprList = expressionList();
			initArray = exprList.toArray(new IASTInitializerClause[exprList.size()]);
		}
		int endOffset = consumeOrEndOfCompletion(IToken.tRPAREN).getEndOffset();
		return setRange(((ICPPNodeFactory) nodeFactory).newConstructorInitializer(initArray), offset, endOffset);
	}

	private IASTEqualsInitializer equalsInitalizerClause(boolean allowSkipping)
			throws EndOfFileException, BacktrackException {
		// Check for deleted or defaulted method syntax.
		final int type = lookaheadTypeWithEndOfFile(2);
		if (type == IToken.t_delete || type == IToken.t_default) {
			return null;
		}

		int offset = consume(IToken.tASSIGN).getOffset();
		IASTInitializerClause initClause = initClause(allowSkipping);
		IASTEqualsInitializer initExpr = nodeFactory.newEqualsInitializer(initClause);
		return setRange(initExpr, offset, calculateEndOffset(initClause));
	}

	private ICPPASTStructuredBindingDeclaration structuredBinding(ICPPASTSimpleDeclSpecifier simpleDeclSpecifier,
			List<IASTAttributeSpecifier> attributes) throws BacktrackException, EndOfFileException {
		RefQualifier refQualifier = optionalRefQualifier();
		consume(IToken.tLBRACKET);
		IASTName[] identifiers = identifierList();
		int endOffset = consume(IToken.tRBRACKET).getEndOffset();

		IASTInitializer initializer = null;
		if (lookaheadType(1) != IToken.tCOLON) {
			switch (lookaheadType(1)) {
			case IToken.tASSIGN:
				initializer = equalsInitalizerClause(false);
				break;
			case IToken.tLBRACE:
			case IToken.tLPAREN:
				initializer = bracedOrConstructorStyleInitializer();
				break;
			}

			endOffset = consume(IToken.tSEMI).getEndOffset();
		}

		ICPPASTStructuredBindingDeclaration structuredBinding = ((ICPPNodeFactory) nodeFactory)
				.newStructuredBindingDeclaration(simpleDeclSpecifier, refQualifier, identifiers, initializer);
		setRange(structuredBinding, simpleDeclSpecifier, endOffset);
		addAttributeSpecifiers(attributes, structuredBinding);
		return structuredBinding;
	}

	/**
	 * Tries to detect illegal versions of declarations
	 */
	private void verifyDeclarator(IASTDeclSpecifier declspec, IASTDeclarator dtor, DeclarationOptions opt)
			throws BacktrackException {
		if (CPPVisitor.doesNotSpecifyType(declspec)) {
			if (ASTQueries.findTypeRelevantDeclarator(dtor) instanceof IASTFunctionDeclarator) {
				boolean isQualified = false;
				IASTName name = ASTQueries.findInnermostDeclarator(dtor).getName();
				if (name instanceof ICPPASTQualifiedName) {
					isQualified = true;
					name = name.getLastName();
				}
				if (name instanceof ICPPASTTemplateId)
					name = ((ICPPASTTemplateId) name).getTemplateName();

				// accept conversion operator
				if (name instanceof ICPPASTConversionName)
					return;

				if (opt == DeclarationOptions.CPP_MEMBER) {
					// Accept constructor and destructor within class body
					final char[] nchars = name.getLookupKey();
					if (nchars.length > 0 && currentClassName != null) {
						final int start = nchars[0] == '~' ? 1 : 0;
						if (CharArrayUtils.equals(nchars, start, nchars.length - start, currentClassName))
							return;
					}

					// Accept constructors and destructors of other classes as friends
					if (declspec instanceof ICPPASTDeclSpecifier && ((ICPPASTDeclSpecifier) declspec).isFriend())
						return;
				} else if (isQualified) {
					// Accept qualified constructor or destructor outside of class body
					return;
				}
			}

			ASTNode node = (ASTNode) dtor;
			throwBacktrack(node.getOffset(), node.getLength());
		}
	}

	/**
	 * virt-specifier-seq
	 *	virt-specifier
	 *	virt-specifier-seq virt-specifier
	 *
	 * virt-specifier:
	 *	override
	 *	final
	 * @throws EndOfFileException
	 * @throws BacktrackException
	 */
	private void optionalVirtualSpecifierSequence(ICPPASTFunctionDeclarator typeRelevantDtor)
			throws EndOfFileException, BacktrackException {
		while (true) {
			IToken token = lookaheadWithEndOfFile(1);
			ContextSensitiveTokenType contextSensitiveType = getContextSensitiveType(token);
			if (contextSensitiveType == null) {
				break;
			}
			consume();
			SpecifierKind specifierKind;
			if (contextSensitiveType == ContextSensitiveTokenType.OVERRIDE) {
				specifierKind = ICPPASTVirtSpecifier.SpecifierKind.Override;
			} else if (contextSensitiveType == ContextSensitiveTokenType.FINAL) {
				specifierKind = ICPPASTVirtSpecifier.SpecifierKind.Final;
			} else {
				break;
			}
			ICPPASTVirtSpecifier spec = ((ICPPNodeFactory) nodeFactory).newVirtSpecifier(specifierKind);
			int endOffset = token.getOffset() + token.getLength();
			setRange(spec, token.getOffset(), endOffset);
			typeRelevantDtor.addVirtSpecifier(spec);
			adjustEndOffset(typeRelevantDtor, endOffset);
		}
	}

	private void setDeclaratorID(ICPPASTDeclarator declarator, boolean hasEllipsis, IASTName declaratorName,
			IASTDeclarator nestedDeclarator) {
		if (nestedDeclarator != null) {
			declarator.setNestedDeclarator(nestedDeclarator);
			declarator.setName(nodeFactory.newName());
		} else {
			declarator.setName(declaratorName);
		}
		declarator.setDeclaresParameterPack(hasEllipsis);
	}

	/**
	 * Parse a method declarator starting with the left parenthesis.
	 */
	private ICPPASTFunctionDeclarator methodDeclarator(boolean isLambdaDeclarator)
			throws EndOfFileException, BacktrackException {
		IToken last = consume(IToken.tLPAREN);
		final int startOffset = last.getOffset();
		int endOffset = last.getEndOffset();

		final ICPPASTFunctionDeclarator method = ((ICPPNodeFactory) nodeFactory).newFunctionDeclarator(null);
		ICPPASTParameterDeclaration parameters = null;
		paramLoop: while (true) {
			switch (lookaheadType(1)) {
			case IToken.tRPAREN:
			case IToken.tEOC:
				endOffset = consume().getEndOffset();
				break paramLoop;
			case IToken.tELLIPSIS:
				consume();
				endOffset = consume(IToken.tRPAREN).getEndOffset();
				method.setVarArgs(true);
				break paramLoop;
			case IToken.tCOMMA:
				if (parameters == null)
					throwBacktrack(lookahead(1));
				endOffset = consume().getEndOffset();
				parameters = null;
				break;
			default:
				if (parameters != null)
					throwBacktrack(startOffset, endOffset - startOffset);

				parameters = parameterDeclaration();
				method.addParameterDeclaration(parameters);
				endOffset = calculateEndOffset(parameters);
				break;
			}
		}
		// Handle ambiguity between parameter pack and varargs.
		if (parameters != null) {
			ICPPASTDeclarator dtor = parameters.getDeclarator();
			if (dtor != null && !(dtor instanceof IASTAmbiguousDeclarator)) {
				if (dtor.declaresParameterPack() && dtor.getNestedDeclarator() == null && dtor.getInitializer() == null
						&& dtor.getName().getSimpleID().length == 0) {
					((IASTAmbiguityParent) method).replace(parameters,
							new CPPASTAmbiguousParameterDeclaration(parameters));
				}
			}
		}

		List<IASTAttributeSpecifier> attributes = new ArrayList<>();

		// cv-qualifiers
		if (isLambdaDeclarator) {
			specloop: while (true) {
				switch (lookaheadType(1)) {
				case IToken.t_mutable:
					method.setMutable(true);
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_constexpr:
					method.setConstexpr(true);
					endOffset = consume().getEndOffset();
					break;
				default:
					break specloop;
				}
			}
		} else {
			cvloop: while (true) {
				switch (lookaheadType(1)) {
				case IToken.t_const:
					method.setConst(true);
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_volatile:
					method.setVolatile(true);
					endOffset = consume().getEndOffset();
					break;
				default:
					break cvloop;
				}
			}
		}

		// ref-qualifiers
		RefQualifier refQualifier = optionalRefQualifier();
		if (refQualifier != null) {
			method.setRefQualifier(refQualifier);
			endOffset = getEndOffset();
		}

		// throws clause
		if (lookaheadType(1) == IToken.t_throw) {
			method.setEmptyExceptionSpecification();
			consume(); // throw
			consume(IToken.tLPAREN);

			thloop: for (;;) {
				switch (lookaheadType(1)) {
				case IToken.tRPAREN:
				case IToken.tEOC:
					endOffset = consume().getEndOffset();
					break thloop;
				case IToken.tCOMMA:
					consume();
					break;
				default:
					int thoffset = lookahead(1).getOffset();
					try {
						ICPPASTTypeId typeId = (ICPPASTTypeId) typeID(DeclarationOptions.TYPEID);
						if (lookaheadType(1) == IToken.tELLIPSIS) {
							typeId.setIsPackExpansion(true);
							adjustEndOffset(typeId, consume().getEndOffset());
						}
						method.addExceptionSpecificationTypeId(typeId);
					} catch (BacktrackException exception) {
						int thendoffset = lookahead(1).getOffset();
						if (thoffset == thendoffset) {
							thendoffset = consume().getEndOffset();
						}
						IASTProblem p = createProblem(IProblem.SYNTAX_ERROR, thoffset, thendoffset - thoffset);
						IASTProblemTypeId typeIdProblem = ((ICPPNodeFactory) nodeFactory).newProblemTypeId(p);
						((ASTNode) typeIdProblem).setOffsetAndLength(((ASTNode) p));
						method.addExceptionSpecificationTypeId(typeIdProblem);
					}
					break;
				}
			}
		}

		// noexcept specification
		if (lookaheadType(1) == IToken.t_noexcept) {
			consume(); // noexcept
			IASTExpression expression = ICPPASTFunctionDeclarator.NOEXCEPT_DEFAULT;
			endOffset = getEndOffset();
			if (lookaheadType(1) == IToken.tLPAREN) {
				consume(); // (
				expression = expression();
				consume(IToken.tRPAREN); //)
				endOffset = getEndOffset();
			}
			method.setNoexceptExpression((ICPPASTExpression) expression);
		}

		attributes = CollectionUtils.merge(attributes, attributes());
		addAttributeSpecifiers(attributes, method);
		endOffset = attributesEndOffset(endOffset, attributes);

		if (lookaheadType(1) == IToken.tARROW) {
			consume();
			IASTTypeId typeId = typeID(DeclarationOptions.TYPEID_TRAILING_RETURN_TYPE);
			method.setTrailingReturnType(typeId);
			endOffset = calculateEndOffset(typeId);
		}

		return setRange(method, startOffset, endOffset);
	}

	/**
	 * Parse an array declarator starting at the square bracket.
	 */
	private ICPPASTArrayDeclarator arrayDeclarator(DeclarationOptions option)
			throws EndOfFileException, BacktrackException {
		ArrayList<IASTArrayModifier> arrayModifiers = new ArrayList<>(4);
		int start = lookahead(1).getOffset();
		consumeArrayModifiers(option, arrayModifiers);
		if (arrayModifiers.isEmpty())
			throwBacktrack(lookahead(1));

		final int endOffset = calculateEndOffset(arrayModifiers.get(arrayModifiers.size() - 1));
		final ICPPASTArrayDeclarator declarator = ((ICPPNodeFactory) nodeFactory).newArrayDeclarator(null);
		for (IASTArrayModifier modifier : arrayModifiers) {
			declarator.addArrayModifier(modifier);
		}

		((ASTNode) declarator).setOffsetAndLength(start, endOffset - start);
		return declarator;
	}

	/**
	 * Parses for a bit field declarator starting with the colon
	 */
	private ICPPASTFieldDeclarator bitFieldDeclarator() throws EndOfFileException, BacktrackException {
		int start = consume(IToken.tCOLON).getOffset();

		final IASTExpression bitField = constantExpression();
		final int endOffset = calculateEndOffset(bitField);

		ICPPASTFieldDeclarator declarator = ((ICPPNodeFactory) nodeFactory).newFieldDeclarator(null, bitField);
		((ASTNode) declarator).setOffsetAndLength(start, endOffset - start);
		return declarator;
	}

	private boolean canHaveConstructorInitializer(IASTDeclSpecifier specifier, IASTDeclarator declarator) {
		if (specifier instanceof ICPPASTDeclSpecifier) {
			ICPPASTDeclSpecifier cppspec = (ICPPASTDeclSpecifier) specifier;
			if (cppspec.isFriend()) {
				return false;
			}
			if (cppspec.getStorageClass() == IASTDeclSpecifier.sc_typedef) {
				return false;
			}
		}

		if (specifier instanceof ICPPASTSimpleDeclSpecifier) {
			ICPPASTSimpleDeclSpecifier sspec = (ICPPASTSimpleDeclSpecifier) specifier;
			if (CPPVisitor.doesNotSpecifyType(specifier)) {
				return false;
			}
			if (sspec.getType() == IASTSimpleDeclSpecifier.t_void && declarator != null
					&& declarator.getPointerOperators().length == 0 && declarator.getNestedDeclarator() == null) {
				return false;
			}
		}

		if (declarator != null) {
			IASTName name = ASTQueries.findInnermostDeclarator(declarator).getName().getLastName();
			if (name instanceof ICPPASTTemplateId) {
				name = ((ICPPASTTemplateId) name).getTemplateName();
			}
			if (name instanceof ICPPASTOperatorName || name instanceof ICPPASTConversionName)
				return false;
		}

		return true;
	}

	private boolean specifiesArray(IASTDeclarator declarator) {
		declarator = ASTQueries.findTypeRelevantDeclarator(declarator);
		return declarator instanceof IASTArrayDeclarator;
	}

	private enum BinaryExprCtx {
		EXPR_IN_TEMPLATE_ID, EXPR_NOT_IN_TEMPLATE_ID, EXPR_IN_PRIMARY_EXPRESSION
	}

	protected static enum DeclaratorStrategy {
		PREFER_METHOD, PREFER_NESTED
	}

}

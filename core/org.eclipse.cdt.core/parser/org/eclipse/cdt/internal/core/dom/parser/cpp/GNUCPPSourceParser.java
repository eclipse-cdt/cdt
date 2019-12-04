/*******************************************************************************
 * Copyright (c) 2002, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *	   John Camelon (IBM Corporation) - initial API and implementation
 *	   Markus Schorn (Wind River Systems)
 *	   Bryan Wilkinson (QNX) - https://bugs.eclipse.org/bugs/show_bug.cgi?id=151207
 *	   Ed Swartz (Nokia)
 *	   Mike Kucera (IBM)
 *	   Andrew Ferguson (Symbian)
 *	   Sergey Prigogin (Google)
 *	   Thomas Corbat (IFS)
 *	   Anders Dahlberg (Ericsson) - bug 84144
 *	   Nathan Ridge
 *	   Richard Eames
 *	   Alexander Nyßen (itemis AG) - bug 475908
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTAlignmentSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
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
import org.eclipse.cdt.core.dom.ast.IASTProblemTypeId;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTToken;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNaryTypeIdExpression;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeTransformationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVirtSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVirtSpecifier.SpecifierKind;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUnaryTypeTransformation;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTArrayRangeDesignator;
import org.eclipse.cdt.core.dom.parser.IExtensionToken;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IGCCToken;
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
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.BacktrackException;
import org.eclipse.cdt.internal.core.dom.parser.DeclarationOptions;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.NameOrTemplateIDVariants.BranchPoint;
import org.eclipse.cdt.internal.core.dom.parser.cpp.NameOrTemplateIDVariants.Variant;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * This is our implementation of the IParser interface, serving as a parser for
 * GNU C and C++. From time to time we will make reference to the ANSI ISO
 * specifications.
 */
public class GNUCPPSourceParser extends AbstractGNUSourceCodeParser {
	private static final int DEFAULT_PARM_LIST_SIZE = 4;
	private static final int DEFAULT_CATCH_HANDLER_LIST_SIZE = 4;
	private static final int TEMPLATE_ARGUMENT_NESTING_DEPTH_LIMIT = 192;

	// This is a parameter to the protected function {@link #declarator(DtorStrategy, DeclarationOptions)}
	// so it needs to be protected too.
	protected static enum DtorStrategy {
		PREFER_FUNCTION, PREFER_NESTED
	}

	private final boolean allowCPPRestrict;
	private final boolean supportExtendedTemplateSyntax;
	private final boolean supportAutoTypeSpecifier;
	private final boolean supportUserDefinedLiterals;
	private final boolean supportGCCStyleDesignators;

	private final IIndex index;
	protected ICPPASTTranslationUnit translationUnit;

	private int functionBodyCount;
	private int templateArgumentNestingDepth = 0;
	private char[] currentClassName;
	private char[] additionalNumericalSuffixes;

	private TemplateIdStrategy fTemplateParameterListStrategy;

	private Map<String, ContextSensitiveTokenType> fContextSensitiveTokens;

	public GNUCPPSourceParser(IScanner scanner, ParserMode mode, IParserLogService log,
			ICPPParserExtensionConfiguration config) {
		this(scanner, mode, log, config, null);
	}

	public GNUCPPSourceParser(IScanner scanner, ParserMode mode, IParserLogService log,
			ICPPParserExtensionConfiguration config, IIndex index) {
		super(scanner, log, mode, CPPNodeFactory.getDefault(), config.supportStatementsInExpressions(),
				config.supportTypeofUnaryExpressions(), config.supportAlignOfUnaryExpression(), config.supportKnRC(),
				config.supportAttributeSpecifiers(), config.supportDeclspecSpecifiers(),
				config.getBuiltinBindingsProvider());
		allowCPPRestrict = config.allowRestrictPointerOperators();
		supportExtendedTemplateSyntax = config.supportExtendedTemplateSyntax();
		supportParameterInfoBlock = config.supportParameterInfoBlock();
		supportExtendedSizeofOperator = config.supportExtendedSizeofOperator();
		supportFunctionStyleAsm = config.supportFunctionStyleAssembler();
		supportGCCStyleDesignators = config.supportGCCStyleDesignators();
		functionCallCanBeLValue = true;
		supportAutoTypeSpecifier = true;
		supportUserDefinedLiterals = config.supportUserDefinedLiterals();
		this.index = index;
		scanner.setSplitShiftROperator(true);
		fContextSensitiveTokens = createContextSensitiveTokenMap(config);
		additionalNumericalSuffixes = scanner.getAdditionalNumericLiteralSuffixes();
	}

	@Override
	protected ICPPNodeFactory getNodeFactory() {
		return (ICPPNodeFactory) super.getNodeFactory();
	}

	private Map<String, ContextSensitiveTokenType> createContextSensitiveTokenMap(
			ICPPParserExtensionConfiguration config) {
		Map<String, ContextSensitiveTokenType> result = new HashMap<>();
		result.put(Keywords.OVERRIDE, ContextSensitiveTokenType.OVERRIDE);
		result.put(Keywords.FINAL, ContextSensitiveTokenType.FINAL);
		result.putAll(config.getAdditionalContextSensitiveKeywords());
		return result;
	}

	private ContextSensitiveTokenType getContextSensitiveType(IToken token) {
		if (!(token.getType() == IToken.tIDENTIFIER)) {
			return null;
		}
		return fContextSensitiveTokens.get(new String(token.getCharImage()));
	}

	@Override
	protected IASTName identifier() throws EndOfFileException, BacktrackException {
		switch (LT(1)) {
		case IToken.tIDENTIFIER:
		case IToken.tCOMPLETION:
		case IToken.tEOC:
			return buildName(-1, consume(), false);
		}

		throw backtrack;
	}

	private ICPPASTNameSpecifier nameSpecifier() throws BacktrackException, EndOfFileException {
		return ambiguousNameSpecifier(CastExprCtx.eNotInBExpr);
	}

	private ICPPASTNameSpecifier ambiguousNameSpecifier(CastExprCtx ctx) throws BacktrackException, EndOfFileException {
		TemplateIdStrategy strat = new TemplateIdStrategy();
		IToken m = mark();
		while (true) {
			try {
				return nameSpecifier(ctx, strat);
			} catch (BacktrackException e) {
				if (e.isFatal()) {
					throw e;
				}
				if (strat.setNextAlternative(true /* previous alternative failed to parse */)) {
					backup(m);
				} else {
					throw e;
				}
			}
		}
	}

	/**
	 * Parses a name specifier.
	 */
	private ICPPASTNameSpecifier nameSpecifier(CastExprCtx ctx, ITemplateIdStrategy strat)
			throws BacktrackException, EndOfFileException {
		if (strat == null)
			return ambiguousNameSpecifier(ctx);

		ICPPASTQualifiedName qname = null;
		ICPPASTNameSpecifier nameSpec = null;
		final int offset = LA(1).getOffset();
		int endOffset = offset;
		if (LT(1) == IToken.tCOLONCOLON) {
			endOffset = consume().getEndOffset();
			qname = getNodeFactory().newQualifiedName(null);
			qname.setFullyQualified(true);
		}

		boolean mustBeLast = false;
		boolean haveName = false;
		loop: while (true) {
			boolean keywordTemplate = false;
			if (qname != null && LT(1) == IToken.t_template) {
				consume();
				keywordTemplate = true;
			}

			int destructorOffset = -1;
			if (LT(1) == IToken.tBITCOMPLEMENT) {
				destructorOffset = consume().getOffset();
				mustBeLast = true;
			}

			switch (LT(1)) {
			case IToken.tIDENTIFIER:
			case IToken.tCOMPLETION:
			case IToken.tEOC:
				IToken nt = consume();
				nameSpec = (ICPPASTName) buildName(destructorOffset, nt, keywordTemplate);
				break;

			case IToken.t_operator:
				nameSpec = (ICPPASTName) operatorId();
				break;

			case IToken.t_decltype:
				// A decltype-specifier must be the first component of a qualified name.
				if (qname != null)
					throwBacktrack(LA(1));

				nameSpec = decltypeSpecifier();
				break;

			default:
				if (!haveName || destructorOffset >= 0 || keywordTemplate) {
					throwBacktrack(LA(1));
				}
				nameSpec = getNodeFactory().newName(CharArrayUtils.EMPTY);
				if (qname != null) {
					addNameSpecifier(qname, nameSpec);
				}
				break loop;
			}

			haveName = true;

			// Check for template-id
			if (nameSpec instanceof IASTName && LTcatchEOF(1) == IToken.tLT) {
				IASTName name = (IASTName) nameSpec;
				final boolean inBinaryExpression = ctx != CastExprCtx.eNotInBExpr;
				final int haveArgs = haveTemplateArguments(inBinaryExpression);
				boolean templateID = true;
				if (!keywordTemplate) {
					if (haveArgs == NO_TEMPLATE_ID) {
						templateID = false;
					} else if (haveArgs == AMBIGUOUS_TEMPLATE_ID) {
						templateID = strat.shallParseAsTemplateID(name);
					}
				}
				if (templateID) {
					if (haveArgs == NO_TEMPLATE_ID)
						throwBacktrack(LA(1));

					nameSpec = (ICPPASTName) addTemplateArguments(name, strat);
				}
			}

			endOffset = calculateEndOffset(nameSpec);
			if (qname != null) {
				addNameSpecifier(qname, nameSpec);
			}

			if (LTcatchEOF(1) != IToken.tCOLONCOLON)
				break loop;

			if (mustBeLast)
				throwBacktrack(LA(1));

			endOffset = consume().getEndOffset(); // ::
			if (qname == null) {
				qname = getNodeFactory().newQualifiedName(null);
				addNameSpecifier(qname, nameSpec);
			}
		}
		if (qname != null) {
			setRange(qname, offset, endOffset);
			nameSpec = qname;
		}
		return nameSpec;
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
	private ICPPASTName qualifiedName(CastExprCtx ctx, ITemplateIdStrategy strat)
			throws BacktrackException, EndOfFileException {
		ICPPASTNameSpecifier nameSpec = nameSpecifier(ctx, strat);
		if (!(nameSpec instanceof ICPPASTName)) {
			// decltype-specifier without following ::
			throwBacktrack(nameSpec);
		}
		return (ICPPASTName) nameSpec;
	}

	private void addNameSpecifier(ICPPASTQualifiedName qname, ICPPASTNameSpecifier nameSpec) {
		if (nameSpec instanceof IASTName)
			qname.addName((IASTName) nameSpec);
		else
			qname.addNameSpecifier(nameSpec);
	}

	private IASTName buildName(int destructorOffset, IToken nt, boolean keywordTemplate) {
		IASTName name;
		if (destructorOffset < 0) {
			if (keywordTemplate) {
				name = getNodeFactory().newTemplateName(nt.getCharImage());
			} else {
				name = getNodeFactory().newName(nt.getCharImage());
			}
			setRange(name, nt.getOffset(), nt.getEndOffset());
		} else {
			char[] nchars = nt.getCharImage();
			final int len = nchars.length;
			char[] image = new char[len + 1];
			image[0] = '~';
			System.arraycopy(nchars, 0, image, 1, len);
			name = getNodeFactory().newName(image);
			setRange(name, destructorOffset, nt.getEndOffset());
		}
		switch (nt.getType()) {
		case IToken.tEOC:
		case IToken.tCOMPLETION:
			ASTCompletionNode node = createCompletionNode(nt);
			if (node != null)
				node.addName(name);
			break;
		}
		return name;
	}

	private IASTName addTemplateArguments(IASTName templateName, ITemplateIdStrategy strat)
			throws EndOfFileException, BacktrackException {
		// Parse for template arguments
		consume(IToken.tLT);
		List<IASTNode> list = templateArgumentList(strat);
		IToken end = LA(1);
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
		ICPPASTTemplateId result = getNodeFactory().newTemplateId(templateName);
		setRange(result, ((ASTNode) templateName).getOffset(), endOffset);
		for (IASTNode n : args) {
			if (n instanceof IASTTypeId) {
				result.addTemplateArgument((IASTTypeId) n);
			} else if (n instanceof IASTExpression) {
				result.addTemplateArgument((IASTExpression) n);
			} else if (n instanceof ICPPASTAmbiguousTemplateArgument) {
				result.addTemplateArgument((ICPPASTAmbiguousTemplateArgument) n);
			}
		}
		return result;
	}

	/**
	 * Parses a decltype-specifier.
	 */
	private ICPPASTDecltypeSpecifier decltypeSpecifier() throws EndOfFileException, BacktrackException {
		int start = consume(IToken.t_decltype).getOffset();
		consume(IToken.tLPAREN);
		ICPPASTExpression decltypeExpression = expression();
		int end = consume(IToken.tRPAREN).getEndOffset();
		ICPPASTDecltypeSpecifier decltypeSpec = getNodeFactory().newDecltypeSpecifier(decltypeExpression);
		setRange(decltypeSpec, start, end);
		return decltypeSpec;
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
		int lt1 = LTcatchEOF(1);
		switch (lt1) {
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
		case IGCCToken.t___alignof__:
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

	private List<IASTNode> templateArgumentList(ITemplateIdStrategy strat)
			throws EndOfFileException, BacktrackException {
		if (templateArgumentNestingDepth >= TEMPLATE_ARGUMENT_NESTING_DEPTH_LIMIT) {
			throwBacktrack(
					createProblem(IProblem.TEMPLATE_ARGUMENT_NESTING_DEPTH_LIMIT_EXCEEDED, LA(1).getOffset(), 1));
		}
		++templateArgumentNestingDepth;
		try {
			int startingOffset = LA(1).getOffset();
			int endOffset = 0;
			List<IASTNode> list = null;

			boolean needComma = false;
			int lt1 = LT(1);
			while (lt1 != IToken.tGT && lt1 != IToken.tGT_in_SHIFTR && lt1 != IToken.tEOC) {
				if (needComma) {
					if (lt1 != IToken.tCOMMA) {
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
				lt1 = LT(1);
			}
			if (list == null) {
				return Collections.emptyList();
			}
			return list;
		} finally {
			--templateArgumentNestingDepth;
		}
	}

	private IASTNode templateArgument(ITemplateIdStrategy strat) throws EndOfFileException, BacktrackException {
		IToken argStart = mark();
		int markBranchPoint = ((TemplateIdStrategy) strat).getCurrentBranchPoint();
		ICPPASTTypeId typeId = null;
		int lt1 = 0;
		try {
			typeId = typeId(DeclarationOptions.TYPEID, strat);
			lt1 = LT(1);
		} catch (BacktrackException e) {
			if (e.isFatal()) {
				throw e;
			}
		}

		if (typeId != null && (lt1 == IToken.tCOMMA || lt1 == IToken.tGT || lt1 == IToken.tGT_in_SHIFTR
				|| lt1 == IToken.tEOC || lt1 == IToken.tELLIPSIS)) {
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
						idExpression = setRange(getNodeFactory().newIdExpression(name), name);

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
				IASTExpression expression = expression(ExprKind.eAssignment, BinaryExprCtx.eInTemplateID, idExpression,
						strat);

				// At this point we have a valid type-id and a valid expression.
				// We prefer the longer one.
				if (!typeId.contains(expression)) {
					// The expression is longer.
					if (LT(1) == IToken.tELLIPSIS) {
						expression = addPackExpansion(expression, consume());
					}
					return expression;
				} else if (expression.contains(typeId)) {
					// The two are of the same length - ambiguous.
					if (LT(1) == IToken.tELLIPSIS) {
						IToken ellipsis = consume();
						addPackExpansion(typeId, ellipsis);
						expression = addPackExpansion(expression, ellipsis);
					}
					ICPPASTAmbiguousTemplateArgument ambiguity = createAmbiguousTemplateArgument();
					ambiguity.addTypeId(typeId);
					ambiguity.addExpression(expression);
					return ambiguity;
				}
				// The type-id is longer, use it.
			} catch (BacktrackException e) {
				if (e.isFatal()) {
					throw e;
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
			if (LT(1) == IToken.tELLIPSIS) {
				addPackExpansion(typeId, consume());
			}
			return typeId;
		}

		// Not a type-id, parse as expression.
		backup(argStart);
		((TemplateIdStrategy) strat).backupToBranchPoint(markBranchPoint);
		IASTExpression expr = expression(ExprKind.eAssignment, BinaryExprCtx.eInTemplateID, null, strat);
		if (LT(1) == IToken.tELLIPSIS) {
			expr = addPackExpansion(expr, consume());
		}
		return expr;
	}

	private void addPackExpansion(ICPPASTTypeId typeId, IToken consume) {
		final int endOffset = consume.getEndOffset();
		adjustEndOffset(typeId, endOffset);
		typeId.setIsPackExpansion(true);
	}

	private IASTExpression addPackExpansion(IASTExpression expr, IToken ellipsis) {
		IASTExpression result = getNodeFactory().newPackExpansionExpression(expr);
		return setRange(result, expr, ellipsis.getEndOffset());
	}

	private IASTName operatorId() throws BacktrackException, EndOfFileException {
		final IToken firstToken = consume(IToken.t_operator);
		int endOffset = firstToken.getEndOffset();
		IASTTypeId typeId = null;
		OverloadableOperator op = null;
		final int lt1 = LT(1);
		switch (lt1) {
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
			if (LT(2) == IToken.tLBRACKET) {
				op = lt1 == IToken.t_new ? OverloadableOperator.NEW_ARRAY : OverloadableOperator.DELETE_ARRAY;
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

				IASTName name = getNodeFactory().newOperatorName(operatorName);
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
				IASTName name = getNodeFactory().newOperatorName(operatorName);
				setRange(name, firstToken.getOffset(), strOp.getEndOffset());
				return name;
			}
			break;
		}
		default:
			op = OverloadableOperator.valueOf(LA(1));
			if (op != null) {
				endOffset = consume().getEndOffset();
			}
			break;
		}

		if (op != null) {
			IASTName name = getNodeFactory().newOperatorName(op.toCharArray());
			setRange(name, firstToken.getOffset(), endOffset);
			return name;
		}

		// must be a conversion function
		typeId = typeId(DeclarationOptions.TYPEID_CONVERSION);

		IASTName name = getNodeFactory().newConversionName(typeId);
		setRange(name, firstToken.getOffset(), calculateEndOffset(typeId));
		return name;
	}

	/**
	 * Information for the parser, whether a binary expression is parsed in the context of a
	 * template-id an ambiguous template-id (one where the '<' could be a greater sign) or
	 * else where.
	 */
	private enum BinaryExprCtx {
		eInTemplateID, eNotInTemplateID
	}

	@Override
	protected ICPPASTExpression expression() throws BacktrackException, EndOfFileException {
		return expression(ExprKind.eExpression, BinaryExprCtx.eNotInTemplateID, null, null);
	}

	@Override
	protected ICPPASTExpression constantExpression() throws BacktrackException, EndOfFileException {
		return expression(ExprKind.eConstant, BinaryExprCtx.eNotInTemplateID, null, null);
	}

	private ICPPASTExpression expression(final ExprKind kind, final BinaryExprCtx ctx, IASTInitializerClause expr,
			ITemplateIdStrategy strat) throws EndOfFileException, BacktrackException {
		final boolean allowComma = kind == ExprKind.eExpression;
		boolean allowAssignment = kind != ExprKind.eConstant;

		if (allowAssignment && LT(1) == IToken.t_throw) {
			return throwExpression();
		}

		final int startOffset = expr != null ? ((ASTNode) expr).getOffset() : LA(1).getOffset();
		int lt1;
		int conditionCount = 0;
		BinaryOperator lastOperator = null;
		NameOrTemplateIDVariants variants = null;

		IToken variantMark = mark();
		if (expr == null) {
			Object e = castExpressionForBinaryExpression(strat);
			if (e instanceof IASTExpression) {
				expr = (IASTExpression) e;
			} else {
				variants = new NameOrTemplateIDVariants();

				final Variant variant = (Variant) e;
				expr = variant.getExpression();
				variants.addBranchPoint(variant.getNext(), null, allowAssignment, conditionCount);
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
			final int opOffset = LA().getOffset();
			lt1 = stopWithNextOperator ? IToken.tSEMI : LT(1);
			switch (lt1) {
			case IToken.tQUESTION:
				conditionCount++;
				// <logical-or> ? <expression> : <assignment-expression>
				// Precedence: 25 is lower than precedence of logical or; 0 is lower than precedence of expression
				lastOperator = new BinaryOperator(lastOperator, expr, lt1, 25, 0);
				allowAssignment = true; // assignment expressions will be subsumed by the conditional expression
				allowThrow = true;
				break;

			case IToken.tCOLON:
				if (--conditionCount < 0) {
					doneExpression = true;
				} else {
					// <logical-or> ? <expression> : <assignment-expression>
					// Precedence: 0 is lower than precedence of expression; 15 is lower than precedence of assignment;
					lastOperator = new BinaryOperator(lastOperator, expr, lt1, 0, 15);
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
					lastOperator = new BinaryOperator(lastOperator, expr, lt1, 10, 11);
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
					lastOperator = new BinaryOperator(lastOperator, expr, lt1, 21, 20);
					allowBraceInitializer = true;
				}
				break;

			case IToken.tOR:
				lastOperator = new BinaryOperator(lastOperator, expr, lt1, 30, 31);
				break;
			case IToken.tAND:
				lastOperator = new BinaryOperator(lastOperator, expr, lt1, 40, 41);
				break;
			case IToken.tBITOR:
				lastOperator = new BinaryOperator(lastOperator, expr, lt1, 50, 51);
				break;
			case IToken.tXOR:
				lastOperator = new BinaryOperator(lastOperator, expr, lt1, 60, 61);
				break;
			case IToken.tAMPER:
				lastOperator = new BinaryOperator(lastOperator, expr, lt1, 70, 71);
				break;
			case IToken.tEQUAL:
			case IToken.tNOTEQUAL:
				lastOperator = new BinaryOperator(lastOperator, expr, lt1, 80, 81);
				break;
			case IToken.tGT:
				if (ctx == BinaryExprCtx.eInTemplateID) {
					doneExpression = true;
					break;
				}
				//$FALL-THROUGH$
			case IToken.tLT:
			case IToken.tLTEQUAL:
			case IToken.tGTEQUAL:
			case IGCCToken.tMAX:
			case IGCCToken.tMIN:
				lastOperator = new BinaryOperator(lastOperator, expr, lt1, 90, 91);
				break;
			case IToken.tGT_in_SHIFTR:
				if (ctx == BinaryExprCtx.eInTemplateID) {
					doneExpression = true;
					break;
				}
				if (LT(2) != IToken.tGT_in_SHIFTR) {
					IToken token = LA(1);
					backtrack.initialize(token.getOffset(), token.getLength());
					failure = backtrack;
					break;
				}

				lt1 = IToken.tSHIFTR; // convert back
				consume(); // consume the extra token
				//$FALL-THROUGH$
			case IToken.tSHIFTL:
			case IToken.tSHIFTR:
				lastOperator = new BinaryOperator(lastOperator, expr, lt1, 100, 101);
				break;
			case IToken.tPLUS:
			case IToken.tMINUS:
				lastOperator = new BinaryOperator(lastOperator, expr, lt1, 110, 111);
				break;
			case IToken.tSTAR:
			case IToken.tDIV:
			case IToken.tMOD:
				lastOperator = new BinaryOperator(lastOperator, expr, lt1, 120, 121);
				break;
			case IToken.tDOTSTAR:
			case IToken.tARROWSTAR:
				lastOperator = new BinaryOperator(lastOperator, expr, lt1, 130, 131);
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
					if (lt1 == IToken.tQUESTION && LT(1) == IToken.tCOLON) {
						// Missing sub-expression after '?' (gnu-extension)
						expr = null;
					} else if (allowThrow && LT(1) == IToken.t_throw) {
						// Throw expression
						expr = throwExpression();
						lt1 = LT(1);
						if (lt1 != IToken.tCOLON && lt1 != IToken.tCOMMA)
							stopWithNextOperator = true;
					} else if (allowBraceInitializer && LT(1) == IToken.tLBRACE) {
						// Brace initializer
						expr = bracedInitList(true, false);
						lt1 = LT(1);
						if (lt1 != IToken.tCOLON && lt1 != IToken.tCOMMA)
							stopWithNextOperator = true;
					} else {
						Object e = castExpressionForBinaryExpression(strat);
						if (e instanceof IASTExpression) {
							expr = (IASTExpression) e;
						} else {
							final Variant ae = (Variant) e;
							expr = ae.getExpression();
							if (variants == null)
								variants = new NameOrTemplateIDVariants();

							variants.addBranchPoint(ae.getNext(), lastOperator, allowAssignment, conditionCount);
						}
					}
					continue castExprLoop;
				} catch (BacktrackException e) {
					failure = e;
				}
			}

			// We need a new variant
			Variant variant = variants == null ? null : variants.selectFallback();
			if (variant == null) {
				if (failure != null)
					throw failure;
				throwBacktrack(LA(1));
			} else {
				// Restore variant and continue
				BranchPoint varPoint = variant.getOwner();
				allowAssignment = varPoint.isAllowAssignment();
				conditionCount = varPoint.getConditionCount();
				lastOperator = varPoint.getLeftOperator();
				expr = variant.getExpression();

				backup(variantMark);
				int offset = variant.getRightOffset();
				while (LA().getOffset() < offset) {
					consume();
				}
				variantMark = mark();
			}
		}

		// Check for incomplete conditional expression
		if (lt1 != IToken.tEOC && conditionCount > 0)
			throwBacktrack(LA(1));

		if (variants != null) {
			BinaryOperator end = new BinaryOperator(lastOperator, expr, -1, 0, 0);
			variants.closeVariants(LA(1).getOffset(), end);
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

	public Object castExpressionForBinaryExpression(ITemplateIdStrategy s)
			throws EndOfFileException, BacktrackException {
		if (s != null) {
			return castExpression(CastExprCtx.eDirectlyInBExpr, s);
		}

		TemplateIdStrategy strat = new TemplateIdStrategy();
		Variant variants = null;
		IASTExpression singleResult = null;
		IASTName[] firstNames = null;

		final IToken mark = mark();
		IToken lastToken = null;
		while (true) {
			try {
				IASTExpression e = castExpression(CastExprCtx.eDirectlyInBExpr, strat);
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
				lastToken = LA();
				if (variants != null) {
					variants = new Variant(variants, e, strat.getTemplateNames(), lastToken.getOffset());
				}
				if (!strat.setNextAlternative(false /* previous alternative parsed ok */)) {
					break;
				}
			} catch (BacktrackException e) {
				if (!strat.setNextAlternative(true /* previous alternative failed to parse */)) {
					if (lastToken == null)
						throw e;

					backup(lastToken);
					break;
				}
			}
			backup(mark);
		}
		return variants != null ? variants : singleResult;
	}

	@Override
	protected IASTExpression buildBinaryExpression(int operator, IASTExpression expr1, IASTInitializerClause expr2,
			int lastOffset) {
		IASTBinaryExpression result = getNodeFactory().newBinaryExpression(operator, expr1, expr2);
		int o = ((ASTNode) expr1).getOffset();
		((ASTNode) result).setOffsetAndLength(o, lastOffset - o);
		return result;
	}

	private ICPPASTExpression throwExpression() throws EndOfFileException, BacktrackException {
		IToken throwToken = consume();
		IASTExpression throwExpression = null;
		try {
			throwExpression = expression();
		} catch (BacktrackException bte) {
			backup(throwToken);
			consume();
		}
		int o = throwExpression != null ? calculateEndOffset(throwExpression) : throwToken.getEndOffset();
		return (ICPPASTExpression) buildUnaryExpression(ICPPASTUnaryExpression.op_throw, throwExpression,
				throwToken.getOffset(), o); // fix for 95225
	}

	protected IASTExpression deleteExpression() throws EndOfFileException, BacktrackException {
		int startingOffset = LA(1).getOffset();
		boolean global = false;
		if (LT(1) == IToken.tCOLONCOLON) {
			// global scope
			consume();
			global = true;
		}

		consume(IToken.t_delete);

		boolean vectored = false;
		if (LT(1) == IToken.tLBRACKET) {
			// array delete
			consume();
			consume(IToken.tRBRACKET);
			vectored = true;
		}
		IASTExpression castExpression = castExpression(CastExprCtx.eNotInBExpr, null);
		ICPPASTDeleteExpression deleteExpression = getNodeFactory().newDeleteExpression(castExpression);
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
		IToken la = LA(1);
		int offset = la.getOffset();

		final boolean isGlobal = la.getType() == IToken.tCOLONCOLON;
		if (isGlobal) {
			consume();
		}
		consume(IToken.t_new);
		if (LT(1) == IToken.tLPAREN) {
			consume();

			// consider placement first (P) ...
			List<ICPPASTInitializerClause> plcmt = null;
			IASTTypeId typeid = null;
			boolean isNewTypeId = true;
			IASTInitializer init = null;
			int endOffset = 0;
			IToken mark = mark();
			IToken end = null;
			try {
				plcmt = expressionList();
				endOffset = consumeOrEOC(IToken.tRPAREN).getEndOffset();

				final int lt1 = LT(1);
				if (lt1 == IToken.tEOC) {
					return newExpression(isGlobal, plcmt, typeid, isNewTypeId, init, offset, endOffset);
				}
				if (lt1 == IToken.tLPAREN) {
					// (P)(T) ...
					isNewTypeId = false;
					consume(IToken.tLPAREN);
					typeid = typeId(DeclarationOptions.TYPEID);
					endOffset = consumeOrEOC(IToken.tRPAREN).getEndOffset();
				} else {
					// (P) T ...
					typeid = typeId(DeclarationOptions.TYPEID_NEW);
					endOffset = calculateEndOffset(typeid);
				}
				end = LA(1);
			} catch (BacktrackException e) {
				plcmt = null;
				typeid = null;
			}

			if (typeid != null && plcmt != null) {
				// (P)(T)(I) or (P) T (I)
				int lt1 = LT(1);
				if (lt1 == IToken.tEOC)
					return newExpression(isGlobal, plcmt, typeid, isNewTypeId, init, offset, endOffset);

				if (lt1 == IToken.tLPAREN || lt1 == IToken.tLBRACE) {
					init = bracedOrCtorStyleInitializer();
					endOffset = calculateEndOffset(init);
					return newExpression(isGlobal, plcmt, typeid, isNewTypeId, init, offset, endOffset);
				}
			}

			// (T) ...
			backup(mark);
			IASTTypeId typeid2 = null;
			IASTInitializer init2 = null;
			int endOffset2;
			try {
				typeid2 = typeId(DeclarationOptions.TYPEID);
				endOffset2 = consumeOrEOC(IToken.tRPAREN).getEndOffset();

				final int lt1 = LT(1);
				if (lt1 == IToken.tEOC)
					return newExpression(isGlobal, null, typeid2, false, init2, offset, endOffset2);

				if (lt1 == IToken.tLPAREN || lt1 == IToken.tLBRACE) {
					if (plcmt != null && ASTQueries.findTypeRelevantDeclarator(
							typeid2.getAbstractDeclarator()) instanceof IASTArrayDeclarator) {
						throwBacktrack(LA(1));
					}

					// (T)(I)
					init2 = bracedOrCtorStyleInitializer();
					endOffset2 = calculateEndOffset(init2);
				}
			} catch (BacktrackException e) {
				if (plcmt == null)
					throw e;
				endOffset2 = -1;
			}

			if (plcmt == null || endOffset2 > endOffset)
				return newExpression(isGlobal, null, typeid2, false, init2, offset, endOffset2);

			if (endOffset != endOffset2) {
				backup(end);
				return newExpression(isGlobal, plcmt, typeid, isNewTypeId, init, offset, endOffset);
			}

			// ambiguity:
			IASTExpression ex1 = newExpression(isGlobal, plcmt, typeid, isNewTypeId, init, offset, endOffset);
			IASTExpression ex2 = newExpression(isGlobal, null, typeid2, false, init2, offset, endOffset2);
			IASTAmbiguousExpression ambiguity = createAmbiguousExpression();
			ambiguity.addExpression(ex1);
			ambiguity.addExpression(ex2);
			((ASTNode) ambiguity).setOffsetAndLength((ASTNode) ex1);
			return ambiguity;
		}

		// T ...
		final IASTTypeId typeid = typeId(DeclarationOptions.TYPEID_NEW);
		int endOffset = calculateEndOffset(typeid);
		IASTInitializer init = null;
		final int lt1 = LT(1);
		if (lt1 == IToken.tLPAREN || lt1 == IToken.tLBRACE) {
			// T(I)
			init = bracedOrCtorStyleInitializer();
			endOffset = calculateEndOffset(init);
		}
		return newExpression(isGlobal, null, typeid, true, init, offset, endOffset);
	}

	private IASTExpression newExpression(boolean isGlobal, List<? extends IASTInitializerClause> plcmt,
			IASTTypeId typeid, boolean isNewTypeId, IASTInitializer init, int offset, int endOffset) {
		IASTInitializerClause[] plcmtArray = null;
		if (plcmt != null && !plcmt.isEmpty()) {
			plcmtArray = plcmt.toArray(new IASTInitializerClause[plcmt.size()]);
		}
		ICPPASTNewExpression result = getNodeFactory().newNewExpression(plcmtArray, init, typeid);
		result.setIsGlobal(isGlobal);
		result.setIsNewTypeId(isNewTypeId);
		((ASTNode) result).setOffsetAndLength(offset, endOffset - offset);
		return result;
	}

	@Override
	protected IASTExpression unaryExpression(CastExprCtx ctx, ITemplateIdStrategy strat)
			throws EndOfFileException, BacktrackException {
		switch (LT(1)) {
		case IToken.tSTAR:
			return unaryExpression(IASTUnaryExpression.op_star, ctx, strat);
		case IToken.tAMPER:
			return unaryExpression(IASTUnaryExpression.op_amper, ctx, strat);
		case IToken.tAND:
			return unaryExpression(IASTUnaryExpression.op_labelReference, ctx, strat);
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
		case IToken.t_new:
			return newExpression();
		case IToken.t_delete:
			return deleteExpression();
		case IToken.tCOLONCOLON:
			switch (LT(2)) {
			case IToken.t_new:
				return newExpression();
			case IToken.t_delete:
				return deleteExpression();
			default:
				return postfixExpression(ctx, strat);
			}
		case IToken.t_sizeof:
			if (LTcatchEOF(2) == IToken.tELLIPSIS) {
				int offset = consume().getOffset(); // sizeof
				consume(); // ...
				return parseTypeidInParenthesisOrUnaryExpression(true, offset,
						IASTTypeIdExpression.op_sizeofParameterPack, IASTUnaryExpression.op_sizeofParameterPack, ctx,
						strat);
			}
			return parseTypeidInParenthesisOrUnaryExpression(false, consume().getOffset(),
					IASTTypeIdExpression.op_sizeof, IASTUnaryExpression.op_sizeof, ctx, strat);
		case IToken.t_alignof:
		case IGCCToken.t___alignof__:
			return parseTypeidInParenthesisOrUnaryExpression(false, consume().getOffset(),
					IASTTypeIdExpression.op_alignof, IASTUnaryExpression.op_alignOf, ctx, strat);

		case IGCCToken.tTT_has_nothrow_assign:
		case IGCCToken.tTT_has_nothrow_constructor:
		case IGCCToken.tTT_has_nothrow_copy:
		case IGCCToken.tTT_has_trivial_assign:
		case IGCCToken.tTT_has_trivial_constructor:
		case IGCCToken.tTT_has_trivial_copy:
		case IGCCToken.tTT_has_trivial_destructor:
		case IGCCToken.tTT_has_virtual_destructor:
		case IGCCToken.tTT_is_abstract:
		case IGCCToken.tTT_is_base_of:
		case IGCCToken.tTT_is_class:
		case IGCCToken.tTT_is_empty:
		case IGCCToken.tTT_is_enum:
		case IGCCToken.tTT_is_final:
		case IGCCToken.tTT_is_literal_type:
		case IGCCToken.tTT_is_pod:
		case IGCCToken.tTT_is_polymorphic:
		case IGCCToken.tTT_is_standard_layout:
		case IGCCToken.tTT_is_trivial:
		case IGCCToken.tTT_is_union:
		case IGCCToken.tTT_is_trivially_copyable:
		case IGCCToken.tTT_is_trivially_constructible:
		case IGCCToken.tTT_is_trivially_assignable:
		case IGCCToken.tTT_is_constructible:
			return parseTypeTrait();

		default:
			return postfixExpression(ctx, strat);
		}
	}

	private IASTExpression parseTypeTrait() throws EndOfFileException, BacktrackException {
		IToken first = consume();
		final boolean isBinary = isBinaryTrait(first);
		final boolean isNary = isNaryTrait(first);

		consume(IToken.tLPAREN);
		ICPPASTTypeId[] operands = new ICPPASTTypeId[isBinary ? 2 : 1];
		operands[0] = typeId(DeclarationOptions.TYPEID);
		if (isBinary) {
			consumeOrEOC(IToken.tCOMMA);
			if (LT(1) != IToken.tEOC) {
				operands[1] = typeId(DeclarationOptions.TYPEID);
			}
		} else if (isNary) {
			while (LTcatchEOF(1) == IToken.tCOMMA) {
				consume();
				if (LT(1) != IToken.tEOC) {
					ICPPASTTypeId operand = typeId(DeclarationOptions.TYPEID);
					// n-ary type traits can contain pack expansions
					if (LT(1) == IToken.tELLIPSIS) {
						addPackExpansion(operand, consume());
					}
					operands = ArrayUtil.append(operands, operand);
				}
			}
			operands = ArrayUtil.removeNulls(operands);
		}
		int endOffset = consumeOrEOC(IToken.tRPAREN).getEndOffset();
		IASTExpression result;
		if (isNary) {
			result = getNodeFactory().newNaryTypeIdExpression(getNaryTypeTraitOperator(first), operands);
		} else if (isBinary) {
			result = getNodeFactory().newBinaryTypeIdExpression(getBinaryTypeTraitOperator(first), operands[0],
					operands[1]);
		} else {
			result = getNodeFactory().newTypeIdExpression(getUnaryTypeTraitOperator(first), operands[0]);
		}
		return setRange(result, first.getOffset(), endOffset);
	}

	private boolean isBinaryTrait(IToken first) {
		switch (first.getType()) {
		case IGCCToken.tTT_is_base_of:
		case IGCCToken.tTT_is_trivially_assignable:
			return true;
		}
		return false;
	}

	private boolean isNaryTrait(IToken operatorToken) {
		switch (operatorToken.getType()) {
		case IGCCToken.tTT_is_trivially_constructible:
		case IGCCToken.tTT_is_constructible:
			return true;
		}
		return false;
	}

	private IASTBinaryTypeIdExpression.Operator getBinaryTypeTraitOperator(IToken first) {
		switch (first.getType()) {
		case IGCCToken.tTT_is_base_of:
			return IASTBinaryTypeIdExpression.Operator.__is_base_of;
		case IGCCToken.tTT_is_trivially_assignable:
			return IASTBinaryTypeIdExpression.Operator.__is_trivially_assignable;
		}

		assert false;
		return null;
	}

	private ICPPASTNaryTypeIdExpression.Operator getNaryTypeTraitOperator(IToken operatorToken) {
		switch (operatorToken.getType()) {
		case IGCCToken.tTT_is_trivially_constructible:
			return ICPPASTNaryTypeIdExpression.Operator.__is_trivially_constructible;
		case IGCCToken.tTT_is_constructible:
			return ICPPASTNaryTypeIdExpression.Operator.__is_constructible;
		}

		assert false;
		return null;
	}

	private int getUnaryTypeTraitOperator(IToken first) {
		switch (first.getType()) {
		case IGCCToken.tTT_has_nothrow_assign:
			return IASTTypeIdExpression.op_has_nothrow_assign;
		case IGCCToken.tTT_has_nothrow_constructor:
			return IASTTypeIdExpression.op_has_nothrow_constructor;
		case IGCCToken.tTT_has_nothrow_copy:
			return IASTTypeIdExpression.op_has_nothrow_copy;
		case IGCCToken.tTT_has_trivial_assign:
			return IASTTypeIdExpression.op_has_trivial_assign;
		case IGCCToken.tTT_has_trivial_constructor:
			return IASTTypeIdExpression.op_has_trivial_constructor;
		case IGCCToken.tTT_has_trivial_copy:
			return IASTTypeIdExpression.op_has_trivial_copy;
		case IGCCToken.tTT_has_trivial_destructor:
			return IASTTypeIdExpression.op_has_trivial_destructor;
		case IGCCToken.tTT_has_virtual_destructor:
			return IASTTypeIdExpression.op_has_virtual_destructor;
		case IGCCToken.tTT_is_abstract:
			return IASTTypeIdExpression.op_is_abstract;
		case IGCCToken.tTT_is_class:
			return IASTTypeIdExpression.op_is_class;
		case IGCCToken.tTT_is_empty:
			return IASTTypeIdExpression.op_is_empty;
		case IGCCToken.tTT_is_enum:
			return IASTTypeIdExpression.op_is_enum;
		case IGCCToken.tTT_is_final:
			return IASTTypeIdExpression.op_is_final;
		case IGCCToken.tTT_is_literal_type:
			return IASTTypeIdExpression.op_is_literal_type;
		case IGCCToken.tTT_is_pod:
			return IASTTypeIdExpression.op_is_pod;
		case IGCCToken.tTT_is_polymorphic:
			return IASTTypeIdExpression.op_is_polymorphic;
		case IGCCToken.tTT_is_standard_layout:
			return IASTTypeIdExpression.op_is_standard_layout;
		case IGCCToken.tTT_is_trivial:
			return IASTTypeIdExpression.op_is_trivial;
		case IGCCToken.tTT_is_union:
			return IASTTypeIdExpression.op_is_union;
		case IGCCToken.tTT_is_trivially_copyable:
			return IASTTypeIdExpression.op_is_trivially_copyable;
		}
		assert false;
		return 0;
	}

	/**
	 * postfix-expression:
	 *	[gnu-extension, compound literals in c++]
	 *	   (type-name) { initializer-list }
	 *	   (type-name) { initializer-list , }
	 *
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
	private IASTExpression postfixExpression(CastExprCtx ctx, ITemplateIdStrategy strat)
			throws EndOfFileException, BacktrackException {
		IASTExpression firstExpression = null;
		boolean isTemplate = false;
		int offset;

		switch (LT(1)) {
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
			firstExpression = parseTypeidInParenthesisOrUnaryExpression(true, consume().getOffset(),
					ICPPASTTypeIdExpression.op_typeid, ICPPASTUnaryExpression.op_typeid, ctx, strat);
			break;

		case IToken.t_noexcept:
			// 'noexcept' (expression)
			offset = consume().getOffset(); // noexcept
			consume(IToken.tLPAREN); // (
			firstExpression = expression();
			firstExpression = getNodeFactory().newUnaryExpression(IASTUnaryExpression.op_noexcept, firstExpression);
			final int endOffset = consume(IToken.tRPAREN).getEndOffset(); //)
			setRange(firstExpression, offset, endOffset);
			break;

		case IToken.tLPAREN:
			// Gnu-extension: compound literals in c++
			// (type-name) { initializer-list }
			// (type-name) { initializer-list , }
			IToken m = mark();
			try {
				if (canBeCompoundLiteral()) {
					offset = consume().getOffset();
					IASTTypeId t = typeId(DeclarationOptions.TYPEID);
					consume(IToken.tRPAREN);
					if (LT(1) == IToken.tLBRACE) {
						IASTInitializer i = bracedInitList(false, false);
						firstExpression = getNodeFactory().newTypeIdInitializerExpression(t, i);
						setRange(firstExpression, offset, calculateEndOffset(i));
						break;
					}
				}
			} catch (BacktrackException bt) {
			}
			backup(m);
			firstExpression = primaryExpression(ctx, strat);
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
		case IGCCToken.t_typeof:
			if (LT(1) == IToken.t_decltype) {
				// Might be an id-expression starting with a decltype-specifier.
				IToken marked = mark();
				try {
					firstExpression = primaryExpression(ctx, strat);
					break;
				} catch (BacktrackException e) {
					backup(marked);
				}
			}
			firstExpression = simpleTypeConstructorExpression(simpleTypeSpecifier());
			break;

		default:
			firstExpression = primaryExpression(ctx, strat);
			if (firstExpression instanceof IASTIdExpression && LT(1) == IToken.tLBRACE) {
				IASTName name = ((IASTIdExpression) firstExpression).getName();
				ICPPASTDeclSpecifier declSpec = getNodeFactory().newTypedefNameSpecifier(name);
				firstExpression = simpleTypeConstructorExpression(setRange(declSpec, name));
			}
			break;
		}

		for (;;) {
			switch (LT(1)) {
			case IToken.tLBRACKET:
				// postfix-expression [ expression ]
				// postfix-expression [ braced-init-list ]
				consume(IToken.tLBRACKET);
				IASTInitializerClause expression;
				if (LT(1) == IToken.tLBRACE) {
					expression = bracedInitList(false, false);
				} else {
					expression = expression();
				}
				int endOffset = consumeOrEOC(IToken.tRBRACKET).getEndOffset();
				IASTArraySubscriptExpression s = getNodeFactory().newArraySubscriptExpression(firstExpression,
						expression);
				firstExpression = setRange(s, firstExpression, endOffset);
				break;
			case IToken.tLPAREN:
				// postfix-expression (expression-list_opt)
				// simple-type-specifier (expression-list_opt)  // cannot be distinguished
				consume(IToken.tLPAREN);
				IASTInitializerClause[] initArray;
				if (LT(1) == IToken.tRPAREN) {
					initArray = IASTExpression.EMPTY_EXPRESSION_ARRAY;
				} else {
					final List<ICPPASTInitializerClause> exprList = expressionList();
					initArray = exprList.toArray(new IASTInitializerClause[exprList.size()]);
				}
				endOffset = consumeOrEOC(IToken.tRPAREN).getEndOffset();

				IASTFunctionCallExpression fce = getNodeFactory().newFunctionCallExpression(firstExpression, initArray);
				firstExpression = setRange(fce, firstExpression, endOffset);
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
				if (LT(1) == IToken.t_template) {
					consume();
					isTemplate = true;
				}

				IASTName name = qualifiedName(ctx, strat);

				if (name == null)
					throwBacktrack(((ASTNode) firstExpression).getOffset(),
							((ASTNode) firstExpression).getLength() + dot.getLength());

				ICPPASTFieldReference fieldReference = getNodeFactory().newFieldReference(name, firstExpression);
				fieldReference.setIsPointerDereference(false);
				fieldReference.setIsTemplate(isTemplate);
				((ASTNode) fieldReference).setOffsetAndLength(((ASTNode) firstExpression).getOffset(),
						calculateEndOffset(name) - ((ASTNode) firstExpression).getOffset());
				firstExpression = fieldReference;
				break;
			case IToken.tARROW:
				// member access
				IToken arrow = consume();

				if (LT(1) == IToken.t_template) {
					consume();
					isTemplate = true;
				}

				name = qualifiedName(ctx, strat);

				if (name == null)
					throwBacktrack(((ASTNode) firstExpression).getOffset(),
							((ASTNode) firstExpression).getLength() + arrow.getLength());

				fieldReference = getNodeFactory().newFieldReference(name, firstExpression);
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

	@Override
	protected IASTAmbiguousExpression createAmbiguousExpression() {
		return new CPPASTAmbiguousExpression();
	}

	@Override
	protected IASTAmbiguousExpression createAmbiguousBinaryVsCastExpression(IASTBinaryExpression binary,
			IASTCastExpression castExpr) {
		return new CPPASTAmbiguousBinaryVsCastExpression(binary, castExpr);
	}

	@Override
	protected IASTAmbiguousExpression createAmbiguousCastVsFunctionCallExpression(IASTCastExpression castExpr,
			IASTFunctionCallExpression funcCall) {
		return new CPPASTAmbiguousCastVsFunctionCallExpression(castExpr, funcCall);
	}

	protected ICPPASTAmbiguousTemplateArgument createAmbiguousTemplateArgument() {
		return new CPPASTAmbiguousTemplateArgument();
	}

	private IASTExpression simpleTypeConstructorExpression(ICPPASTDeclSpecifier declSpec)
			throws EndOfFileException, BacktrackException {
		IASTInitializer initializer = bracedOrCtorStyleInitializer();
		ICPPASTSimpleTypeConstructorExpression result = getNodeFactory().newSimpleTypeConstructorExpression(declSpec,
				initializer);
		return setRange(result, declSpec, calculateEndOffset(initializer));
	}

	@Override
	protected IASTExpression primaryExpression(CastExprCtx ctx, ITemplateIdStrategy strat)
			throws EndOfFileException, BacktrackException {
		IToken t = null;
		IASTLiteralExpression literalExpr = null;
		IASTLiteralExpression literalExprWithRange = null;

		switch (LT(1)) {
		case IToken.tINTEGER:
			t = consume();
			literalExpr = getNodeFactory().newLiteralExpression(IASTLiteralExpression.lk_integer_constant, t.getImage(),
					additionalNumericalSuffixes);
			literalExprWithRange = setRange(literalExpr, t.getOffset(), t.getEndOffset());
			break;
		case IToken.tFLOATINGPT:
			t = consume();
			literalExpr = getNodeFactory().newLiteralExpression(IASTLiteralExpression.lk_float_constant, t.getImage(),
					additionalNumericalSuffixes);
			literalExprWithRange = setRange(literalExpr, t.getOffset(), t.getEndOffset());
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
			t = consume();
			literalExpr = getNodeFactory().newLiteralExpression(IASTLiteralExpression.lk_char_constant, t.getImage());
			literalExprWithRange = setRange(literalExpr, t.getOffset(), t.getEndOffset());
			break;
		case IToken.t_false:
			t = consume();
			literalExpr = getNodeFactory().newLiteralExpression(IASTLiteralExpression.lk_false, t.getImage());
			return setRange(literalExpr, t.getOffset(), t.getEndOffset());
		case IToken.t_true:
			t = consume();
			literalExpr = getNodeFactory().newLiteralExpression(IASTLiteralExpression.lk_true, t.getImage());
			return setRange(literalExpr, t.getOffset(), t.getEndOffset());
		case IToken.t_nullptr:
			t = consume();
			literalExpr = getNodeFactory().newLiteralExpression(IASTLiteralExpression.lk_nullptr, t.getImage());
			return setRange(literalExpr, t.getOffset(), t.getEndOffset());

		case IToken.t_this:
			t = consume();
			literalExpr = getNodeFactory().newLiteralExpression(IASTLiteralExpression.lk_this, t.getImage());
			return setRange(literalExpr, t.getOffset(), t.getEndOffset());
		case IToken.tLPAREN:
			if (supportStatementsInExpressions && LT(2) == IToken.tLBRACE) {
				return compoundStatementExpression();
			}
			t = consume();
			int finalOffset = 0;
			IASTExpression lhs = expression(ExprKind.eExpression, BinaryExprCtx.eNotInTemplateID, null, null); // instead of expression(), to keep the stack smaller
			switch (LT(1)) {
			case IToken.tRPAREN:
			case IToken.tEOC:
				finalOffset = consume().getEndOffset();
				break;
			default:
				throwBacktrack(LA(1));
			}
			return buildUnaryExpression(IASTUnaryExpression.op_bracketedPrimary, lhs, t.getOffset(), finalOffset);
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
			if (name.getLookupKey().length == 0 && LT(1) != IToken.tEOC)
				throwBacktrack(LA(1));
			IASTIdExpression idExpression = getNodeFactory().newIdExpression(name);
			return setRange(idExpression, name);
		}
		case IToken.tLBRACKET:
			return lambdaExpression();

		default:
			IToken la = LA(1);
			int startingOffset = la.getOffset();
			throwBacktrack(startingOffset, la.getLength());
			return null;
		}

		if (supportUserDefinedLiterals) {
			IToken la = LA(1);
			int offset = ((ASTNode) literalExprWithRange).getOffset();
			int length = ((ASTNode) literalExprWithRange).getLength();
			if (isIdentifierOrKeyword(la)) {
				if ((offset + length) != la.getOffset()) {
					return literalExprWithRange;
				}
				consume();
				((CPPASTLiteralExpression) literalExprWithRange).setSuffix(la.getCharImage());
				setRange(literalExprWithRange, offset, la.getEndOffset());
			}
		}

		return literalExprWithRange;
	}

	private ICPPASTLiteralExpression stringLiteral() throws EndOfFileException, BacktrackException {
		switch (LT(1)) {
		case IToken.tSTRING:
		case IToken.tLSTRING:
		case IToken.tUTF16STRING:
		case IToken.tUTF32STRING:
		case IToken.tUSER_DEFINED_STRING_LITERAL:
			break;
		default:
			throwBacktrack(LA(1));
		}
		IToken t = consume();
		ICPPASTLiteralExpression r = getNodeFactory().newLiteralExpression(IASTLiteralExpression.lk_string_literal,
				t.getImage());
		return setRange(r, t.getOffset(), t.getEndOffset());
	}

	private IASTExpression lambdaExpression() throws EndOfFileException, BacktrackException {
		final int offset = LA().getOffset();

		ICPPASTLambdaExpression lambdaExpr = getNodeFactory().newLambdaExpression();

		// Lambda introducer
		consume(IToken.tLBRACKET);
		boolean needComma = false;
		switch (LT(1)) {
		case IToken.tASSIGN:
			lambdaExpr.setCaptureDefault(CaptureDefault.BY_COPY);
			consume();
			needComma = true;
			break;
		case IToken.tAMPER:
			final int lt2 = LT(2);
			if (lt2 == IToken.tCOMMA || lt2 == IToken.tRBRACKET) {
				lambdaExpr.setCaptureDefault(CaptureDefault.BY_REFERENCE);
				consume();
				needComma = true;
			}
			break;
		}
		loop: while (true) {
			switch (LT(1)) {
			case IToken.tEOC:
				return setRange(lambdaExpr, offset, LA().getEndOffset());
			case IToken.tRBRACKET:
				consume();
				break loop;
			}

			if (needComma) {
				consume(IToken.tCOMMA);
			}

			ICPPASTCapture cap = capture();
			lambdaExpr.addCapture(cap);
			needComma = true;
		}

		if (LT(1) == IToken.tLPAREN) {
			ICPPASTFunctionDeclarator dtor = functionDeclarator(true);
			lambdaExpr.setDeclarator(dtor);
			if (LT(1) == IToken.tEOC)
				return setRange(lambdaExpr, offset, calculateEndOffset(dtor));
		}

		IASTCompoundStatement body = functionBody();
		lambdaExpr.setBody(body);
		return setRange(lambdaExpr, offset, calculateEndOffset(body));
	}

	private ICPPASTInitCapture createInitCapture(IASTName identifier, IASTInitializer initializer, boolean isReference,
			int offset) throws EndOfFileException, BacktrackException {
		ICPPASTDeclarator declarator = getNodeFactory().newDeclarator(identifier);
		declarator.setInitializer(initializer);
		if (isReference) {
			declarator.addPointerOperator(getNodeFactory().newReferenceOperator(false));
		}
		setRange(declarator, offset, calculateEndOffset(initializer));
		ICPPASTInitCapture initCapture = getNodeFactory().newInitCapture(declarator);
		return setRange(initCapture, offset, calculateEndOffset(initializer));
	}

	private ICPPASTCapture capture() throws EndOfFileException, BacktrackException {
		final int offset = LA().getOffset();
		ICPPASTCapture result = getNodeFactory().newCapture();
		boolean referenceCapture = false;

		switch (LT(1)) {
		case IToken.t_this:
			result.setIsByReference(true);
			return setRange(result, offset, consume().getEndOffset());
		case IToken.tAMPER:
			consume();
			referenceCapture = true;
			break;
		case IToken.tSTAR:
			if (LT(2) == IToken.t_this) {
				consume();
				return setRange(result, offset, consume().getEndOffset());
			}
			break;
		}

		final IASTName identifier = identifier();
		result.setIdentifier(identifier);
		result.setIsByReference(referenceCapture);
		setRange(result, offset, calculateEndOffset(identifier));

		switch (LT(1)) {
		case IToken.tASSIGN:
			result = createInitCapture(identifier, equalsInitalizerClause(false), referenceCapture, offset);
			break;
		case IToken.tLBRACE:
		case IToken.tLPAREN:
			result = createInitCapture(identifier, bracedOrCtorStyleInitializer(), referenceCapture, offset);
			break;
		}

		if (LT(1) == IToken.tELLIPSIS) {
			// Note this will probably change with C++20 such that the
			// pack expansion of a CPPASTInitCapture will be part of the IASTDeclarator
			// and not the capture. [See: P0780R2]
			result.setIsPackExpansion(true);
			return setRange(result, offset, consume().getEndOffset());
		}

		return result;
	}

	protected IASTExpression specialCastExpression(int kind) throws EndOfFileException, BacktrackException {
		final int offset = LA(1).getOffset();
		final int optype = consume().getType();
		consume(IToken.tLT);
		final IASTTypeId typeID = typeId(DeclarationOptions.TYPEID);
		final IToken gt = LA(1);
		if (gt.getType() == IToken.tGT || gt.getType() == IToken.tGT_in_SHIFTR) {
			consume();
		} else if (gt.getType() != IToken.tEOC) {
			throwBacktrack(gt);
		}
		consumeOrEOC(IToken.tLPAREN);
		IASTExpression operand = null;
		if (LT(1) != IToken.tEOC) {
			operand = expression();
		}
		final int endOffset = consumeOrEOC(IToken.tRPAREN).getEndOffset();
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

		if (LT(1) == IToken.t_namespace) {
			// using-directive
			int endOffset = consume().getEndOffset();
			IASTName name = null;
			switch (LT(1)) {
			case IToken.tIDENTIFIER:
			case IToken.tCOLONCOLON:
			case IToken.tCOMPLETION:
				name = qualifiedName();
				break;
			default:
				throwBacktrack(offset, endOffset - offset);
			}

			attributes = CollectionUtils.merge(attributes, __attribute_decl_seq(supportAttributeSpecifiers, false));

			switch (LT(1)) {
			case IToken.tSEMI:
			case IToken.tEOC:
				endOffset = consume().getEndOffset();
				break;
			default:
				throw backtrack;
			}
			ICPPASTUsingDirective astUD = getNodeFactory().newUsingDirective(name);

			addAttributeSpecifiers(attributes, astUD);

			return setRange(astUD, offset, endOffset);
		}

		if (LT(1) == IToken.tIDENTIFIER
				&& (LT(2) == IToken.tASSIGN || (LT(2) == IToken.tLBRACKET && LT(3) == IToken.tLBRACKET))) {
			return aliasDeclaration(offset);
		}
		ICPPASTUsingDeclaration result = usingDeclaration(offset);
		return result;
	}

	/**
	 * alias-declaration
	 *	using identifier attribute-specifier-seq? = type-id ;
	 *
	 * @throws EndOfFileException
	 */
	private IASTDeclaration aliasDeclaration(final int offset) throws EndOfFileException, BacktrackException {
		IToken identifierToken = consume();
		IASTName aliasName = buildName(-1, identifierToken, false);

		List<IASTAttributeSpecifier> attributes = attributeSpecifierSeq();

		consume();

		ICPPASTTypeId aliasedType = typeId(DeclarationOptions.TYPEID);

		final int nextToken = LT(1);
		if (nextToken != IToken.tSEMI && nextToken != IToken.tEOC) {
			throw backtrack;
		}
		int endOffset = consume().getEndOffset();

		ICPPASTAliasDeclaration aliasDeclaration = getNodeFactory().newAliasDeclaration(aliasName, aliasedType);
		addAttributeSpecifiers(attributes, aliasDeclaration);

		return setRange(aliasDeclaration, offset, endOffset);
	}

	private ICPPASTUsingDeclaration usingDeclaration(final int offset) throws EndOfFileException, BacktrackException {
		boolean typeName = false;
		if (LT(1) == IToken.t_typename) {
			typeName = true;
			consume();
		}

		IASTName name = qualifiedName();
		int end;
		switch (LT(1)) {
		case IToken.tSEMI:
		case IToken.tEOC:
			end = consume().getEndOffset();
			break;
		default:
			throw backtrack;
		}

		ICPPASTUsingDeclaration result = getNodeFactory().newUsingDeclaration(name);
		((ASTNode) result).setOffsetAndLength(offset, end - offset);
		result.setIsTypename(typeName);
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
		IASTExpression e = constantExpression();
		int endOffset = calculateEndOffset(e);
		ICPPASTLiteralExpression message = null;
		if (LT(1) == IToken.tCOMMA) {
			consume(IToken.tCOMMA);
			message = stringLiteral();
		}
		ICPPASTStaticAssertDeclaration assertion = getNodeFactory().newStaticAssertion(e, message);
		if (LT(1) != IToken.tEOC) {
			consume(IToken.tRPAREN);
			endOffset = consume(IToken.tSEMI).getEndOffset();
		}
		return setRange(assertion, offset, endOffset);
	}

	/**
	 * Implements Linkage specification in the ANSI C++ grammar.
	 * linkageSpecification : extern "string literal" declaration | extern
	 * "string literal" { declaration-seq }
	 *
	 * @throws BacktrackException
	 *			 request for a backtrack
	 */
	protected ICPPASTLinkageSpecification linkageSpecification() throws EndOfFileException, BacktrackException {
		int offset = consume().getOffset(); // t_extern
		String spec = consume().getImage(); // tString
		ICPPASTLinkageSpecification linkage = getNodeFactory().newLinkageSpecification(spec);

		if (LT(1) == IToken.tLBRACE) {
			declarationListInBraces(linkage, offset, DeclarationOptions.GLOBAL);
			return linkage;
		}
		// single declaration

		IASTDeclaration d = declaration(DeclarationOptions.GLOBAL);
		linkage.addDeclaration(d);
		setRange(linkage, offset, calculateEndOffset(d));
		return linkage;
	}

	/**
	 * Represents the amalgamation of template declarations, template
	 * instantiations and specializations in the ANSI C++ grammar.
	 * template-declaration: export? template < template-parameter-list >
	 * declaration explicit-instantiation: template declaration
	 * explicit-specialization: template <>declaration
	 * @param option
	 *
	 * @throws BacktrackException
	 *			 request for a backtrack
	 */
	protected IASTDeclaration templateDeclaration(DeclarationOptions option)
			throws EndOfFileException, BacktrackException {
		final int offset = LA(1).getOffset();
		boolean exported = false;
		int explicitInstMod = 0;
		switch (LT(1)) {
		case IToken.t_export:
			exported = true;
			consume();
			break;
		case IToken.t_extern:
			consume();
			explicitInstMod = ICPPASTExplicitTemplateInstantiation.EXTERN;
			break;
		case IToken.t_static:
			consume();
			explicitInstMod = ICPPASTExplicitTemplateInstantiation.STATIC;
			break;
		case IToken.t_inline:
			consume();
			explicitInstMod = ICPPASTExplicitTemplateInstantiation.INLINE;
			break;
		}

		consume(IToken.t_template);

		if (LT(1) != IToken.tLT) {
			// explicit-instantiation
			IASTDeclaration d = declaration(option);
			ICPPASTExplicitTemplateInstantiation ti = getNodeFactory().newExplicitTemplateInstantiation(d);
			ti.setModifier(explicitInstMod);
			setRange(ti, offset, calculateEndOffset(d));
			return ti;
		}

		// Modifiers for explicit instantiations
		if (explicitInstMod != 0) {
			throwBacktrack(LA(1));
		}
		consume(IToken.tLT);
		if (LT(1) == IToken.tGT) {
			// explicit-specialization
			consume();
			IASTDeclaration d = declaration(option);
			ICPPASTTemplateSpecialization templateSpecialization = getNodeFactory().newTemplateSpecialization(d);
			setRange(templateSpecialization, offset, calculateEndOffset(d));
			return templateSpecialization;
		}

		List<ICPPASTTemplateParameter> parms = outerTemplateParameterList();
		if (LT(1) != IToken.tEOC) {
			consume(IToken.tGT, IToken.tGT_in_SHIFTR);
		}
		IASTDeclaration d = declaration(option);
		ICPPASTTemplateDeclaration templateDecl = getNodeFactory().newTemplateDeclaration(d);
		setRange(templateDecl, offset, calculateEndOffset(d));
		templateDecl.setExported(exported);
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
		fTemplateParameterListStrategy = new TemplateIdStrategy();
		try {
			List<ICPPASTTemplateParameter> result = new ArrayList<>(DEFAULT_PARM_LIST_SIZE);
			IToken m = mark();
			while (true) {
				try {
					return templateParameterList(result);
				} catch (BacktrackException e) {
					if (!fTemplateParameterListStrategy
							.setNextAlternative(true /* previous alternative failed to parse */)) {
						fTemplateParameterListStrategy = null;
						throw e;
					}
					result.clear();
					backup(m);
				}
			}
		} finally {
			fTemplateParameterListStrategy = null;
		}
	}

	private List<ICPPASTTemplateParameter> templateParameterList(List<ICPPASTTemplateParameter> result)
			throws EndOfFileException, BacktrackException {
		boolean needComma = false;
		for (;;) {
			final int lt1 = LT(1);
			if (lt1 == IToken.tGT || lt1 == IToken.tEOC || lt1 == IToken.tGT_in_SHIFTR) {
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
		final int lt1 = LT(1);
		final IToken start = mark();
		if (lt1 == IToken.t_class || lt1 == IToken.t_typename) {
			try {
				int type = (lt1 == IToken.t_class ? ICPPASTSimpleTypeTemplateParameter.st_class
						: ICPPASTSimpleTypeTemplateParameter.st_typename);
				boolean parameterPack = false;
				IASTName identifierName = null;
				IASTTypeId defaultValue = null;
				int endOffset = consume().getEndOffset();

				if (LT(1) == IToken.tELLIPSIS) {
					parameterPack = true;
					endOffset = consume().getOffset();
				}
				if (LT(1) == IToken.tIDENTIFIER) { // optional identifier
					identifierName = identifier();
					endOffset = calculateEndOffset(identifierName);
				} else {
					identifierName = getNodeFactory().newName();
					setRange(identifierName, endOffset, endOffset);
				}
				if (LT(1) == IToken.tASSIGN) { // optional = type-id
					if (parameterPack)
						throw backtrack;
					consume();
					defaultValue = typeId(DeclarationOptions.TYPEID); // type-id
					endOffset = calculateEndOffset(defaultValue);
				}

				// Check if followed by comma
				switch (LT(1)) {
				case IToken.tGT:
				case IToken.tEOC:
				case IToken.tGT_in_SHIFTR:
				case IToken.tCOMMA:
					ICPPASTSimpleTypeTemplateParameter tpar = getNodeFactory().newSimpleTypeTemplateParameter(type,
							identifierName, defaultValue);
					tpar.setIsParameterPack(parameterPack);
					setRange(tpar, start.getOffset(), endOffset);
					return tpar;
				}
			} catch (BacktrackException bt) {
			}
			// Can be a non-type template parameter, see bug 333285
			backup(start);
		} else if (lt1 == IToken.t_template) {
			boolean parameterPack = false;
			IASTName identifierName = null;
			IASTExpression defaultValue = null;

			consume();
			consume(IToken.tLT);
			List<ICPPASTTemplateParameter> tparList = templateParameterList(new ArrayList<>());
			consume(IToken.tGT, IToken.tGT_in_SHIFTR);

			int kind = LT(1);
			if (kind != IToken.t_class && kind != IToken.t_typename) {
				throw backtrack;
			}

			int endOffset = consume(kind).getEndOffset();

			if (LT(1) == IToken.tELLIPSIS) {
				parameterPack = true;
				endOffset = consume().getOffset();
			}

			if (LT(1) == IToken.tIDENTIFIER) { // optional identifier
				identifierName = identifier();
				endOffset = calculateEndOffset(identifierName);
				if (LT(1) == IToken.tASSIGN) { // optional = type-id
					if (parameterPack)
						throw backtrack;

					consume();
					defaultValue = primaryExpression(CastExprCtx.eNotInBExpr, null);
					endOffset = calculateEndOffset(defaultValue);
				}
			} else {
				identifierName = getNodeFactory().newName();
			}

			ICPPASTTemplatedTypeTemplateParameter tpar = getNodeFactory()
					.newTemplatedTypeTemplateParameter(identifierName, defaultValue);
			tpar.setIsParameterPack(parameterPack);
			tpar.setParameterType(kind == IToken.t_class ? ICPPASTTemplatedTypeTemplateParameter.tt_class
					: ICPPASTTemplatedTypeTemplateParameter.tt_typename);
			setRange(tpar, start.getOffset(), endOffset);

			for (int i = 0; i < tparList.size(); ++i) {
				ICPPASTTemplateParameter p = tparList.get(i);
				tpar.addTemplateParameter(p);
			}
			return tpar;
		}

		// Try non-type template parameter
		return parameterDeclaration();
	}

	/**
	 * The most abstract construct within a translationUnit : a declaration.
	 * declaration : {"asm"} asmDefinition | {"namespace"} namespaceDefinition |
	 * {"using"} usingDeclaration | {"export"|"template"} templateDeclaration |
	 * {"extern"} linkageSpecification | simpleDeclaration Notes: - folded in
	 * blockDeclaration - merged alternatives that required same LA -
	 * functionDefinition into simpleDeclaration - namespaceAliasDefinition into
	 * namespaceDefinition - usingDirective into usingDeclaration -
	 * explicitInstantiation and explicitSpecialization into templateDeclaration
	 * - fetched attributes at the beginning to avoid arbitrary lookahead
	 *
	 * @throws BacktrackException
	 *			 request a backtrack
	 */
	@Override
	protected IASTDeclaration declaration(DeclarationOptions option) throws EndOfFileException, BacktrackException {
		// Allow GNU-style attributes both before standard attribute / alignment specifiers, and after.
		List<IASTAttributeSpecifier> attributes = __attribute_decl_seq(supportAttributeSpecifiers,
				supportDeclspecSpecifiers);
		attributes = CollectionUtils.merge(attributes, attributeSpecifierSeq());
		attributes = CollectionUtils.merge(attributes,
				__attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers));

		switch (LT(1)) {
		case IToken.t_asm:
			return asmDeclaration();
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
			if (LT(2) == IToken.tSTRING)
				return linkageSpecification();
			if (LT(2) == IToken.t_template)
				return templateDeclaration(option);
			break;
		case IToken.t_static:
		case IToken.t_inline:
			if (supportExtendedTemplateSyntax && LT(2) == IToken.t_template)
				return templateDeclaration(option);
			if (LT(2) == IToken.t_namespace) {
				return namespaceDefinitionOrAlias();
			}
			break;
		case IToken.tSEMI:
			IToken t = consume();
			IASTSimpleDeclSpecifier declspec = getNodeFactory().newSimpleDeclSpecifier();
			IASTSimpleDeclaration decl = getNodeFactory().newSimpleDeclaration(declspec);
			((ASTNode) declspec).setOffsetAndLength(t.getOffset(), 0);
			((ASTNode) decl).setOffsetAndLength(t.getOffset(), t.getLength());
			return decl;
		case IToken.t_public:
		case IToken.t_protected:
		case IToken.t_private:
			if (option == DeclarationOptions.CPP_MEMBER) {
				t = consume();
				int key = t.getType();
				int endOffset = consume(IToken.tCOLON).getEndOffset();
				ICPPASTVisibilityLabel label = getNodeFactory().newVisibilityLabel(token2Visibility(key));
				setRange(label, t.getOffset(), endOffset);
				return label;
			}
			break;
		}

		try {
			return simpleDeclaration(option, attributes);
		} catch (BacktrackException e) {
			if (option != DeclarationOptions.CPP_MEMBER || declarationMark == null)
				throw e;
			BacktrackException orig = new BacktrackException(e); // copy the exception
			IToken mark = mark();
			backup(declarationMark);
			try {
				return usingDeclaration(declarationMark.getOffset());
			} catch (BacktrackException e2) {
				backup(mark);
				throw orig; // throw original exception;
			}
		}
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
		final int offset = LA().getOffset();
		int endOffset;
		boolean isInline = false;

		if (LT(1) == IToken.t_inline) {
			consume();
			isInline = true;
		}
		consume(IToken.t_namespace);

		List<IASTAttributeSpecifier> attributeSpecifiers = attributeSpecifierSeq();

		// optional name
		ICPPASTName name = null;
		if (LT(1) == IToken.tIDENTIFIER) {
			name = qualifiedName();
			endOffset = calculateEndOffset(name);
		} else {
			name = getNodeFactory().newName();
		}

		// bug 195701, gcc 4.2 allows visibility attribute for namespaces.
		attributeSpecifiers = CollectionUtils.merge(attributeSpecifiers, __attribute_decl_seq(true, false));

		if (LT(1) == IToken.tLBRACE) {
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
					ICPPASTNamespaceDefinition ns = getNodeFactory().newNamespaceDefinition(segment);
					if (outer == null || inner == null) { // second half of condition is just to avoid warning
						outer = ns;
					} else {
						inner.addDeclaration(ns);
					}
					inner = ns;
				}
			}
			IASTName lastName = name.getLastName();
			ICPPASTNamespaceDefinition ns = getNodeFactory().newNamespaceDefinition(lastName);
			if (outer == null || inner == null) { // second half of condition is just to avoid warning
				outer = ns;
			} else {
				inner.addDeclaration(ns);
			}
			ns.setIsInline(isInline);
			declarationListInBraces(ns, offset, DeclarationOptions.GLOBAL);
			endOffset = getEndOffset();
			if (ns != outer) {
				// For a C++17 nested namespace definition, we need to set the offset/length of
				// the enclosing namespace declaration nodes (declarationListInBraces() does it
				// for the inner one).
				for (IASTNode parent = ns.getParent(); parent != null; parent = parent.getParent()) {
					setRange(parent, offset, endOffset);
					if (parent == outer) {
						break;
					}
				}
			}
			addAttributeSpecifiers(attributeSpecifiers, ns);
			return outer;
		}

		if (LT(1) == IToken.tASSIGN) {
			endOffset = consume().getEndOffset();
			if (name.toString() == null || name instanceof ICPPASTQualifiedName) {
				throwBacktrack(offset, endOffset - offset);
				return null;
			}

			IASTName qualifiedName = qualifiedName();
			endOffset = consumeOrEOC(IToken.tSEMI).getEndOffset();

			ICPPASTNamespaceAlias alias = getNodeFactory().newNamespaceAlias(name, qualifiedName);
			((ASTNode) alias).setOffsetAndLength(offset, endOffset - offset);
			return alias;
		}
		throwBacktrack(LA(1));
		return null;
	}

	@Override
	protected List<IASTAttributeSpecifier> anyAttributes(boolean allowAttrib, boolean allowDeclspec)
			throws BacktrackException, EndOfFileException {
		List<IASTAttributeSpecifier> attributes = super.anyAttributes(allowAttrib, allowDeclspec);
		attributes = CollectionUtils.merge(attributes, attributeSpecifierSeq());
		return attributes;
	}

	protected List<IASTAttributeSpecifier> attributeSpecifierSeq() throws EndOfFileException, BacktrackException {
		List<IASTAttributeSpecifier> specifiers = null;

		while ((LTcatchEOF(1) == IToken.tLBRACKET && LTcatchEOF(2) == IToken.tLBRACKET)
				|| LTcatchEOF(1) == IToken.t_alignas) {
			if (specifiers == null)
				specifiers = new ArrayList<>();
			if (LTcatchEOF(1) == IToken.t_alignas) {
				specifiers.add((ICPPASTAlignmentSpecifier) alignmentSpecifier());
			} else {
				int offset = consumeOrEOC(IToken.tLBRACKET).getOffset();
				consumeOrEOC(IToken.tLBRACKET);
				ICPPASTAttributeList attributeList = getNodeFactory().newAttributeList();
				while (LT(1) != IToken.tRBRACKET) {
					if (LT(1) == IToken.tCOMMA)
						consume();
					ICPPASTAttribute attribute = singleAttribute();
					attributeList.addAttribute(attribute);

				}
				consumeOrEOC(IToken.tRBRACKET);
				int endOffset = consumeOrEOC(IToken.tRBRACKET).getEndOffset();
				setRange(attributeList, offset, endOffset);
				specifiers.add(attributeList);
			}
		}
		return specifiers;
	}

	@Override
	protected ICPPASTAttribute singleAttribute() throws EndOfFileException, BacktrackException {
		// Get an identifier including keywords
		IToken nameToken = identifierOrKeyword();
		IToken scopeToken = null;
		IASTToken argumentClause = null;
		boolean packExpansion = false;

		// Check for scoped attribute
		if (LT(1) == IToken.tCOLONCOLON) {
			consume();
			scopeToken = nameToken;
			nameToken = identifierOrKeyword();
		}
		int endOffset = nameToken.getEndOffset();

		// Check for arguments
		if (LT(1) == IToken.tLPAREN) {
			IToken t = consume();
			argumentClause = balancedTokenSeq(t.getEndOffset(), IToken.tRPAREN);
			//endOffset = calculateEndOffset(argumentClause);
			endOffset = consume(IToken.tRPAREN).getEndOffset();
		}

		// Check for pack expansion
		if (LT(1) == IToken.tELLIPSIS) {
			packExpansion = true;
			endOffset = consumeOrEOC(IToken.tELLIPSIS).getEndOffset();
		}
		char[] attributeName = nameToken.getCharImage();
		char[] scopeName = scopeToken != null ? scopeToken.getCharImage() : null;
		ICPPASTAttribute result = getNodeFactory().newAttribute(attributeName, scopeName, argumentClause,
				packExpansion);
		setRange(result, nameToken.getOffset(), endOffset);
		return result;
	}

	@Override
	protected boolean isLegalWithoutDtor(IASTDeclSpecifier declSpec) {
		if (declSpec instanceof IASTElaboratedTypeSpecifier) {
			return ((IASTElaboratedTypeSpecifier) declSpec).getKind() != IASTElaboratedTypeSpecifier.k_enum;
		} else if (declSpec instanceof ICPPASTNamedTypeSpecifier && ((ICPPASTNamedTypeSpecifier) declSpec).isFriend()) {
			return true;
		}
		return super.isLegalWithoutDtor(declSpec);
	}

	private RefQualifier optionalRefQualifier() throws EndOfFileException {
		int nextToken = LT(1);
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

	private ICPPASTStructuredBindingDeclaration structuredBinding(ICPPASTSimpleDeclSpecifier simpleDeclSpecifier,
			List<IASTAttributeSpecifier> attributes) throws BacktrackException, EndOfFileException {
		RefQualifier refQualifier = optionalRefQualifier();
		consume(IToken.tLBRACKET);
		IASTName[] identifiers = identifierList();
		int endOffset = consume(IToken.tRBRACKET).getEndOffset();

		IASTInitializer initializer = null;
		if (LT(1) != IToken.tCOLON) {
			switch (LT(1)) {
			case IToken.tASSIGN:
				initializer = equalsInitalizerClause(false);
				break;
			case IToken.tLBRACE:
			case IToken.tLPAREN:
				initializer = bracedOrCtorStyleInitializer();
				break;
			}

			endOffset = consume(IToken.tSEMI).getEndOffset();
		}

		ICPPASTStructuredBindingDeclaration structuredBinding = getNodeFactory()
				.newStructuredBindingDeclaration(simpleDeclSpecifier, refQualifier, identifiers, initializer);
		setRange(structuredBinding, simpleDeclSpecifier, endOffset);
		addAttributeSpecifiers(attributes, structuredBinding);
		return structuredBinding;
	}

	/**
	 * Parses a declaration with the given options.
	 */
	protected IASTDeclaration simpleDeclaration(DeclarationOptions declOption, List<IASTAttributeSpecifier> attributes)
			throws BacktrackException, EndOfFileException {
		if (LT(1) == IToken.tLBRACE)
			throwBacktrack(LA(1));

		final int firstOffset = attributesStartOffset(LA(1).getOffset(), attributes);
		int endOffset = firstOffset;
		boolean insertSemi = false;

		IASTDeclSpecifier declSpec = null;
		IASTDeclarator dtor = null;
		IASTDeclSpecifier altDeclSpec = null;
		IASTDeclarator altDtor = null;
		IToken markBeforDtor = null;
		boolean isAtStartOfStructuredBinding = false;
		try {
			Decl decl = declSpecifierSequence_initDeclarator(declOption, true);
			markBeforDtor = decl.fDtorToken1;
			declSpec = decl.fDeclSpec1;
			dtor = decl.fDtor1;
			altDeclSpec = decl.fDeclSpec2;
			altDtor = decl.fDtor2;
			isAtStartOfStructuredBinding = decl.isAtStartOfStructuredBinding;
		} catch (FoundAggregateInitializer lie) {
			declSpec = lie.fDeclSpec;
			// scalability: don't keep references to tokens, initializer may be large
			declarationMark = null;
			dtor = addInitializer(lie, declOption);
		} catch (BacktrackException e) {
			IASTNode node = e.getNodeBeforeProblem();
			if (node instanceof IASTDeclSpecifier && isLegalWithoutDtor((IASTDeclSpecifier) node)) {
				IASTSimpleDeclaration d = getNodeFactory().newSimpleDeclaration((IASTDeclSpecifier) node);
				setRange(d, node);
				throwBacktrack(e.getProblem(), d);
			}
			throw e;
		}

		if (isAtStartOfStructuredBinding && declSpec instanceof ICPPASTSimpleDeclSpecifier) {
			ICPPASTSimpleDeclSpecifier simpleDeclSpecifier = (ICPPASTSimpleDeclSpecifier) declSpec;
			return structuredBinding(simpleDeclSpecifier, attributes);
		}

		IASTDeclarator[] declarators = IASTDeclarator.EMPTY_DECLARATOR_ARRAY;
		if (dtor != null) {
			declarators = new IASTDeclarator[] { dtor };
			if (!declOption.fSingleDtor) {
				while (LTcatchEOF(1) == IToken.tCOMMA) {
					consume();
					try {
						dtor = initDeclarator(declSpec, declOption);
					} catch (FoundAggregateInitializer e) {
						// scalability: don't keep references to tokens, initializer may be large
						declarationMark = null;
						markBeforDtor = null;
						dtor = addInitializer(e, declOption);
					}
					declarators = ArrayUtil.append(IASTDeclarator.class, declarators, dtor);
				}
				declarators = ArrayUtil.removeNulls(IASTDeclarator.class, declarators);
			}
		}

		final int lt1 = LTcatchEOF(1);
		switch (lt1) {
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
				throwBacktrack(LA(1));

			dtor = declarators[0];
			if (altDeclSpec != null && altDtor != null && dtor != null
					&& !(ASTQueries.findTypeRelevantDeclarator(dtor) instanceof IASTFunctionDeclarator)) {
				declSpec = altDeclSpec;
				dtor = altDtor;
			}
			ICPPASTFunctionDefinition functionDefinition = functionDefinition(firstOffset, declSpec, dtor);
			addAttributeSpecifiers(attributes, functionDefinition);
			return functionDefinition;

		default:
			insertSemi = true;
			if (declOption == DeclarationOptions.LOCAL) {
				endOffset = figureEndOffset(declSpec, declarators);
				break;
			} else {
				if (isLegalWithoutDtor(declSpec) && markBeforDtor != null
						&& !isOnSameLine(calculateEndOffset(declSpec), markBeforDtor.getOffset())) {
					backup(markBeforDtor);
					declarators = IASTDeclarator.EMPTY_DECLARATOR_ARRAY;
					endOffset = calculateEndOffset(declSpec);
					break;
				}
				endOffset = figureEndOffset(declSpec, declarators);
				if (lt1 == 0 || !isOnSameLine(endOffset, LA(1).getOffset())) {
					break;
				}
				if (declarators.length == 1 && declarators[0] instanceof IASTFunctionDeclarator) {
					break;
				}
			}
			throwBacktrack(LA(1));
		}

		// no function body

		final boolean isAmbiguous = altDeclSpec != null && altDtor != null && declarators.length == 1;
		IASTSimpleDeclaration simpleDeclaration;
		if (isAmbiguous) {
			// class C { C(T); };  // if T is a type this is a constructor, so
			// prefer the empty declspec, it shall be used if both variants show no problems
			simpleDeclaration = getNodeFactory().newSimpleDeclaration(altDeclSpec);
			simpleDeclaration.addDeclarator(altDtor);
		} else {
			simpleDeclaration = getNodeFactory().newSimpleDeclaration(declSpec);
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

	private ICPPASTFunctionDefinition functionDefinition(final int firstOffset, IASTDeclSpecifier declSpec,
			IASTDeclarator outerDtor) throws EndOfFileException, BacktrackException {
		final IASTDeclarator dtor = ASTQueries.findTypeRelevantDeclarator(outerDtor);
		if (!(dtor instanceof ICPPASTFunctionDeclarator))
			throwBacktrack(firstOffset, LA(1).getEndOffset() - firstOffset);

		ICPPASTFunctionDefinition fdef;
		if (LT(1) == IToken.t_try) {
			consume();
			fdef = getNodeFactory().newFunctionTryBlock(declSpec, (ICPPASTFunctionDeclarator) dtor, null);
		} else {
			fdef = getNodeFactory().newFunctionDefinition(declSpec, (ICPPASTFunctionDeclarator) dtor, null);
		}
		if (LT(1) == IToken.tASSIGN) {
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

		if (LT(1) == IToken.tCOLON) {
			ctorInitializer(fdef);
		}

		try {
			IASTStatement body = handleFunctionBody();
			fdef.setBody(body);
			setRange(fdef, firstOffset, calculateEndOffset(body));
		} catch (BacktrackException bt) {
			final IASTNode n = bt.getNodeBeforeProblem();
			if (n instanceof IASTCompoundStatement && !(fdef instanceof ICPPASTFunctionWithTryBlock)) {
				fdef.setBody((IASTCompoundStatement) n);
				setRange(fdef, firstOffset, calculateEndOffset(n));
				throwBacktrack(bt.getProblem(), fdef);
			}
			throw bt;
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
	protected void ctorInitializer(ICPPASTFunctionDefinition fdef) throws EndOfFileException, BacktrackException {
		consume(IToken.tCOLON);
		loop: while (true) {
			final int offset = LA(1).getOffset();
			final IASTName name = qualifiedName();
			final IASTInitializer init;
			int endOffset;
			if (LT(1) != IToken.tEOC) {
				init = bracedOrCtorStyleInitializer();
				endOffset = calculateEndOffset(init);
			} else {
				init = null;
				endOffset = calculateEndOffset(name);
			}
			ICPPASTConstructorChainInitializer ctorInitializer = getNodeFactory().newConstructorChainInitializer(name,
					init);
			if (LT(1) == IToken.tELLIPSIS) {
				ctorInitializer.setIsPackExpansion(true);
				endOffset = consume().getEndOffset();
			}
			fdef.addMemberInitializer(setRange(ctorInitializer, offset, endOffset));

			if (LT(1) == IToken.tCOMMA) {
				consume();
			} else {
				break loop;
			}
		}
	}

	/**
	 * This routine parses a parameter declaration
	 *
	 * @throws BacktrackException
	 *			 request a backtrack
	 */
	protected ICPPASTParameterDeclaration parameterDeclaration() throws BacktrackException, EndOfFileException {
		final int startOffset = LA(1).getOffset();

		List<IASTAttributeSpecifier> attributes = attributeSpecifierSeq();

		if (LT(1) == IToken.tLBRACKET && supportParameterInfoBlock) {
			skipBrackets(IToken.tLBRACKET, IToken.tRBRACKET, 0);
		}

		IASTDeclSpecifier declSpec = null;
		IASTDeclarator declarator;
		try {
			Decl decl = declSpecifierSequence_initDeclarator(DeclarationOptions.PARAMETER, false);
			declSpec = decl.fDeclSpec1;
			declarator = decl.fDtor1;
		} catch (FoundAggregateInitializer lie) {
			declSpec = lie.fDeclSpec;
			declarator = addInitializer(lie, DeclarationOptions.PARAMETER);
		}

		final ICPPASTParameterDeclaration parm = getNodeFactory().newParameterDeclaration(declSpec, declarator);
		final int endOffset = figureEndOffset(declSpec, declarator);
		setRange(parm, startOffset, endOffset);
		addAttributeSpecifiers(attributes, parm);
		return parm;
	}

	private final static int INLINE = 0x1, CONST = 0x2, CONSTEXPR = 0x4, RESTRICT = 0x8, VOLATILE = 0x10, SHORT = 0x20,
			UNSIGNED = 0x40, SIGNED = 0x80, COMPLEX = 0x100, IMAGINARY = 0x200, VIRTUAL = 0x400, EXPLICIT = 0x800,
			FRIEND = 0x1000, THREAD_LOCAL = 0x2000;
	private static final int FORBID_IN_EMPTY_DECLSPEC = CONST | RESTRICT | VOLATILE | SHORT | UNSIGNED | SIGNED
			| COMPLEX | IMAGINARY | THREAD_LOCAL;

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
	protected Decl declSpecifierSeq(final DeclarationOptions option, ITemplateIdStrategy strat)
			throws BacktrackException, EndOfFileException {
		return declSpecifierSeq(option, false, strat);
	}

	private ICPPASTDeclSpecifier simpleTypeSpecifier() throws BacktrackException, EndOfFileException {
		Decl d = declSpecifierSeq(null, true, null);
		return (ICPPASTDeclSpecifier) d.fDeclSpec1;
	}

	private ICPPASTDeclSpecifier simpleTypeSpecifierSequence() throws BacktrackException, EndOfFileException {
		Decl d = declSpecifierSeq(null, false, null);
		return (ICPPASTDeclSpecifier) d.fDeclSpec1;
	}

	private Decl declSpecifierSeq(final DeclarationOptions option, final boolean single, ITemplateIdStrategy strat)
			throws BacktrackException, EndOfFileException {
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

			final int offset = LA(1).getOffset();
			int endOffset = offset;

			declSpecifiers: for (;;) {
				final int lt1 = LTcatchEOF(1);
				switch (lt1) {
				case 0: // encountered eof
					break declSpecifiers;
				// storage class specifiers
				case IToken.t_auto:
					if (supportAutoTypeSpecifier) {
						if (encounteredTypename)
							break declSpecifiers;
						simpleType = IASTSimpleDeclSpecifier.t_auto;
						encounteredRawType = true;
						endOffset = consume().getEndOffset();
						break;
					} else {
						storageClass = IASTDeclSpecifier.sc_auto;
						endOffset = consume().getEndOffset();
					}
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
				case IToken.t_restrict:
					options |= RESTRICT;
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
				case IGCCToken.t__int128:
					if (encounteredTypename)
						break declSpecifiers;
					simpleType = IASTSimpleDeclSpecifier.t_int128;
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
				case IGCCToken.t__float128:
					if (encounteredTypename)
						break declSpecifiers;
					simpleType = IASTSimpleDeclSpecifier.t_float128;
					encounteredRawType = true;
					endOffset = consume().getEndOffset();
					break;
				case IGCCToken.t_decimal32:
					if (encounteredTypename)
						break declSpecifiers;
					simpleType = IASTSimpleDeclSpecifier.t_decimal32;
					encounteredRawType = true;
					endOffset = consume().getEndOffset();
					break;
				case IGCCToken.t_decimal64:
					if (encounteredTypename)
						break declSpecifiers;
					simpleType = IASTSimpleDeclSpecifier.t_decimal64;
					encounteredRawType = true;
					endOffset = consume().getEndOffset();
					break;
				case IGCCToken.t_decimal128:
					if (encounteredTypename)
						break declSpecifiers;
					simpleType = IASTSimpleDeclSpecifier.t_decimal128;
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

					if (option != null && option.fAllowEmptySpecifier && LT(1) != IToken.tCOMPLETION) {
						if ((options & FORBID_IN_EMPTY_DECLSPEC) == 0
								&& storageClass == IASTDeclSpecifier.sc_unspecified) {
							altResult = buildSimpleDeclSpec(storageClass, simpleType, options, isLong, typeofExpression,
									offset, endOffset);
							returnToken = mark();
						}
					}

					identifier = qualifiedName(CastExprCtx.eNotInBExpr, strat);
					if (identifier.getLookupKey().length == 0 && LT(1) != IToken.tEOC)
						throwBacktrack(LA(1));

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
						} catch (BacktrackException bt) {
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
					} catch (BacktrackException bt) {
						if (bt.getNodeBeforeProblem() instanceof ICPPASTDeclSpecifier) {
							result = (ICPPASTDeclSpecifier) bt.getNodeBeforeProblem();
							problem = bt.getProblem();
							break declSpecifiers;
						}
						throw bt;
					}
					endOffset = calculateEndOffset(result);
					encounteredTypename = true;
					break;

				case IGCCToken.t__attribute__: // if __attribute__ is after the declSpec
					if (!supportAttributeSpecifiers)
						throwBacktrack(LA(1));
					attributes = CollectionUtils.merge(attributes, __attribute_decl_seq(true, false));
					break;
				case IGCCToken.t__declspec: // __declspec precedes the identifier
					if (identifier != null || !supportDeclspecSpecifiers)
						throwBacktrack(LA(1));
					attributes = CollectionUtils.merge(attributes, __attribute_decl_seq(false, true));
					break;

				case IGCCToken.t_typeof:
					if (encounteredRawType || encounteredTypename)
						throwBacktrack(LA(1));

					simpleType = IASTSimpleDeclSpecifier.t_typeof;
					consume(IGCCToken.t_typeof);
					typeofExpression = parseTypeidInParenthesisOrUnaryExpression(false, LA(1).getOffset(),
							IASTTypeIdExpression.op_typeof, -1, CastExprCtx.eNotInBExpr, null);

					encounteredTypename = true;
					endOffset = calculateEndOffset(typeofExpression);
					break;

				case IToken.t_decltype:
					if (encounteredRawType || encounteredTypename)
						throwBacktrack(LA(1));

					// A decltype-specifier could be the first element
					// in a qualified name, in which case we'll have
					// a named-type-specifier.
					IToken marked = mark();
					try {
						identifier = qualifiedName();
						endOffset = calculateEndOffset(identifier);
						encounteredTypename = true;
						break;
					} catch (BacktrackException e) {
						backup(marked);
					}

					// Otherwise we have a simple-decl-specifier.
					consume(IToken.t_decltype);
					consume(IToken.tLPAREN);
					if (LT(1) == IToken.t_auto) {
						simpleType = IASTSimpleDeclSpecifier.t_decltype_auto;
						consume(IToken.t_auto);
					} else {
						simpleType = IASTSimpleDeclSpecifier.t_decltype;
						typeofExpression = expression();
					}
					endOffset = consumeOrEOC(IToken.tRPAREN).getEndOffset();

					encounteredTypename = true;
					break;

				case IGCCToken.tTT_underlying_type:
					if (encounteredRawType || encounteredTypename)
						throwBacktrack(LA(1));

					result = typeTransformationSpecifier(DeclarationOptions.TYPEID);
					endOffset = calculateEndOffset(result);
					encounteredTypename = true;
					break;

				default:
					if (lt1 >= IExtensionToken.t__otherDeclSpecModifierFirst
							&& lt1 <= IExtensionToken.t__otherDeclSpecModifierLast) {
						handleOtherDeclSpecModifier();
						endOffset = LA(1).getOffset();
						break;
					}
					break declSpecifiers;
				}

				if (encounteredRawType && encounteredTypename)
					throwBacktrack(LA(1));

				if (single)
					break declSpecifiers;
			}

			// check for empty specification
			if (!encounteredRawType && !encounteredTypename && LT(1) != IToken.tEOC
					&& (option == null || !option.fAllowEmptySpecifier)) {
				throwBacktrack(LA(1));
			}

			attributes = CollectionUtils.merge(attributes, attributeSpecifierSeq());

			if (result != null) {
				configureDeclSpec(result, storageClass, options);
				// cannot store restrict in the cpp-nodes.
				//			if ((options & RESTRICT) != 0) {
				//			}
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
		} catch (BacktrackException e) {
			if (returnToken != null) {
				backup(returnToken);
				result = altResult;
				altResult = null;
				returnToken = null;
			} else {
				throw e;
			}
		}

		Decl target = new Decl();
		target.fDeclSpec1 = result;
		target.fDeclSpec2 = altResult;
		target.fDtorToken1 = returnToken;
		return target;
	}

	private ICPPASTNamedTypeSpecifier buildNamedTypeSpecifier(IASTName name, boolean isTypename, int storageClass,
			int options, int offset, int endOffset) {
		ICPPASTNamedTypeSpecifier declSpec = getNodeFactory().newTypedefNameSpecifier(name);
		declSpec.setIsTypename(isTypename);
		configureDeclSpec(declSpec, storageClass, options);
		((ASTNode) declSpec).setOffsetAndLength(offset, endOffset - offset);
		return declSpec;
	}

	private ICPPASTSimpleDeclSpecifier buildSimpleDeclSpec(int storageClass, int simpleType, int options, int isLong,
			IASTExpression typeofExpression, int offset, int endOffset) {
		ICPPASTSimpleDeclSpecifier declSpec = getNodeFactory().newSimpleDeclSpecifier();

		configureDeclSpec(declSpec, storageClass, options);

		declSpec.setType(simpleType);
		declSpec.setLong(isLong == 1);
		declSpec.setLongLong(isLong > 1);
		declSpec.setShort((options & SHORT) != 0);
		declSpec.setUnsigned((options & UNSIGNED) != 0);
		declSpec.setSigned((options & SIGNED) != 0);
		declSpec.setComplex((options & COMPLEX) != 0);
		declSpec.setImaginary((options & IMAGINARY) != 0);
		declSpec.setDeclTypeExpression(typeofExpression);

		((ASTNode) declSpec).setOffsetAndLength(offset, endOffset - offset);
		return declSpec;
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
		declSpec.setRestrict((options & RESTRICT) != 0);
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
			int lt1 = LT(1);
			if (lt1 == IToken.t_class || lt1 == IToken.t_struct) {
				scopeStyle = (lt1 == IToken.t_class) ? ScopeStyle.CLASS : ScopeStyle.STRUCT;
				isScoped = true;
				consume();
			}
			// if __attribute__ or __declspec occurs after struct/union/class and before the identifier
			attributes = anyAttributes(supportAttributeSpecifiers, supportDeclspecSpecifiers);

			if (isScoped || LT(1) == IToken.tIDENTIFIER) {
				// A qualified-name can appear here if an enumeration declared at class scope is
				// being defined out of line.
				name = qualifiedName();
				endOffset = calculateEndOffset(name);
			}

			if (LT(1) == IToken.tCOLON) {
				consume();
				baseType = simpleTypeSpecifierSequence();
				endOffset = calculateEndOffset(baseType);
			}
		} catch (BacktrackException e) {
			backup(mark);
			return elaboratedTypeSpecifier();
		}

		final int lt1 = LT(1);
		final boolean isDef = lt1 == IToken.tLBRACE || (lt1 == IToken.tEOC && baseType != null);
		final boolean isOpaque = !isDef && allowOpaque && lt1 == IToken.tSEMI;
		if (!isDef && !isOpaque) {
			backup(mark);
			return elaboratedTypeSpecifier();
		}
		mark = null;

		if (isOpaque && !isScoped && baseType == null)
			throwBacktrack(LA(1));

		if (name == null) {
			if (isOpaque)
				throwBacktrack(LA(1));
			name = getNodeFactory().newName();
		}

		final ICPPASTEnumerationSpecifier result = getNodeFactory().newEnumerationSpecifier(scopeStyle, name, baseType);
		result.setIsOpaque(isOpaque);
		if (lt1 == IToken.tLBRACE) {
			endOffset = enumBody(result);
		}
		assert endOffset != 0;
		addAttributeSpecifiers(attributes, result);
		return setRange(result, offset, endOffset);
	}

	/**
	 * Parse an elaborated type specifier.
	 *
	 * @throws BacktrackException
	 *			 request a backtrack
	 */
	protected ICPPASTElaboratedTypeSpecifier elaboratedTypeSpecifier() throws BacktrackException, EndOfFileException {
		// this is an elaborated class specifier
		final int lt1 = LT(1);
		int eck = 0;

		switch (lt1) {
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
			throwBacktrack(LA(1));
		}

		final int offset = consume().getOffset();

		// if __attribute__ or __declspec occurs after struct/union/class and before the identifier
		List<IASTAttributeSpecifier> attributes = anyAttributes(supportAttributeSpecifiers, supportDeclspecSpecifiers);

		IASTName name = qualifiedName();
		ICPPASTElaboratedTypeSpecifier elaboratedTypeSpecifier = getNodeFactory().newElaboratedTypeSpecifier(eck, name);
		addAttributeSpecifiers(attributes, elaboratedTypeSpecifier);
		return setRange(elaboratedTypeSpecifier, offset, calculateEndOffset(name));
	}

	/**
	 * Parse a type transformation specifier.
	 */
	protected ICPPASTTypeTransformationSpecifier typeTransformationSpecifier(DeclarationOptions options)
			throws BacktrackException, EndOfFileException {
		final int offset = consume(IGCCToken.tTT_underlying_type).getOffset();
		consume(IToken.tLPAREN);
		ICPPASTTypeId operand = typeId(options);
		final int endOffset = consumeOrEOC(IToken.tRPAREN).getEndOffset();
		return setRange(getNodeFactory().newTypeTransformationSpecifier(
				ICPPUnaryTypeTransformation.Operator.underlying_type, operand), offset, endOffset);
	}

	@Override
	protected IASTDeclarator initDeclarator(IASTDeclSpecifier declspec, DeclarationOptions option)
			throws EndOfFileException, BacktrackException, FoundAggregateInitializer {
		final IToken mark = mark();
		IASTDeclarator dtor1 = null;
		IToken end1 = null;
		IASTDeclarator dtor2 = null;
		BacktrackException bt = null;
		try {
			dtor1 = initDeclarator(DtorStrategy.PREFER_FUNCTION, declspec, option);
			verifyDtor(declspec, dtor1, option);

			int lt1 = LTcatchEOF(1);
			switch (lt1) {
			case 0:
				return dtor1;

			case IToken.tLBRACE:
				if (option.fCanBeFollowedByBrace
						|| ASTQueries.findTypeRelevantDeclarator(dtor1) instanceof IASTFunctionDeclarator)
					return dtor1;

				dtor1 = null;
				throwBacktrack(LA(1));
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
						|| ASTQueries.findTypeRelevantDeclarator(dtor1) instanceof IASTFunctionDeclarator) {
					return dtor1;
				} else {
					dtor1 = null;
					throwBacktrack(LA(1));
				}
			}

			if (!(dtor1 instanceof IASTFunctionDeclarator))
				return dtor1;

			end1 = LA(1);
		} catch (BacktrackException e) {
			bt = e;
		}

		if (!option.fAllowCtorStyleInitializer || !canHaveConstructorInitializer(declspec, dtor1)) {
			if (bt != null)
				throw bt;
			return dtor1;
		}

		backup(mark);
		try {
			dtor2 = initDeclarator(DtorStrategy.PREFER_NESTED, declspec, option);
			if (dtor1 == null) {
				return dtor2;
			}
		} catch (BacktrackException e) {
			if (dtor1 != null) {
				backup(end1);
				return dtor1;
			}
			throw e;
		}

		// we have an ambiguity
		if (end1 != null && LA(1).getEndOffset() != end1.getEndOffset()) {
			backup(end1);
			return dtor1;
		}

		if (functionBodyCount != 0) {
			// prefer the variable prototype:
			IASTDeclarator h = dtor1;
			dtor1 = dtor2;
			dtor2 = h;
		}
		CPPASTAmbiguousDeclarator dtor = new CPPASTAmbiguousDeclarator(dtor1, dtor2);
		dtor.setOffsetAndLength((ASTNode) dtor1);
		return dtor;
	}

	/**
	 * Tries to detect illegal versions of declarations
	 */
	private void verifyDtor(IASTDeclSpecifier declspec, IASTDeclarator dtor, DeclarationOptions opt)
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

	private boolean canHaveConstructorInitializer(IASTDeclSpecifier declspec, IASTDeclarator dtor) {
		if (declspec instanceof ICPPASTDeclSpecifier) {
			ICPPASTDeclSpecifier cppspec = (ICPPASTDeclSpecifier) declspec;
			if (cppspec.isFriend()) {
				return false;
			}
			if (cppspec.getStorageClass() == IASTDeclSpecifier.sc_typedef) {
				return false;
			}
		}

		if (declspec instanceof ICPPASTSimpleDeclSpecifier) {
			ICPPASTSimpleDeclSpecifier sspec = (ICPPASTSimpleDeclSpecifier) declspec;
			if (CPPVisitor.doesNotSpecifyType(declspec)) {
				return false;
			}
			if (sspec.getType() == IASTSimpleDeclSpecifier.t_void && dtor != null
					&& dtor.getPointerOperators().length == 0 && dtor.getNestedDeclarator() == null) {
				return false;
			}
		}

		if (dtor != null) {
			IASTName name = ASTQueries.findInnermostDeclarator(dtor).getName().getLastName();
			if (name instanceof ICPPASTTemplateId) {
				name = ((ICPPASTTemplateId) name).getTemplateName();
			}
			if (name instanceof ICPPASTOperatorName || name instanceof ICPPASTConversionName)
				return false;
		}

		return true;
	}

	/**
	 * Parses the initDeclarator construct of the ANSI C++ spec. initDeclarator :
	 * declarator ("=" initializerClause | "(" expressionList ")")?
	 *
	 * @return declarator that this parsing produced.
	 * @throws BacktrackException request a backtrack
	 * @throws FoundAggregateInitializer
	 */
	private IASTDeclarator initDeclarator(DtorStrategy strategy, IASTDeclSpecifier declspec, DeclarationOptions option)
			throws EndOfFileException, BacktrackException, FoundAggregateInitializer {
		final IASTDeclarator dtor = declarator(strategy, option);
		if (option.fAllowInitializer) {
			final IASTDeclarator typeRelevantDtor = ASTQueries.findTypeRelevantDeclarator(dtor);
			if (option != DeclarationOptions.PARAMETER && typeRelevantDtor instanceof IASTFunctionDeclarator) {
				// Function declarations don't have initializers.
				// For member functions we need to consider virtual specifiers and pure-virtual syntax.
				if (option == DeclarationOptions.CPP_MEMBER) {
					optionalVirtSpecifierSeq((ICPPASTFunctionDeclarator) typeRelevantDtor);
					List<IASTAttributeSpecifier> attributeSpecifiers = __attribute_decl_seq(supportAttributeSpecifiers,
							false);
					addAttributeSpecifiers(attributeSpecifiers, dtor);
					int lt1 = LTcatchEOF(1);
					if (lt1 == IToken.tASSIGN && LTcatchEOF(2) == IToken.tINTEGER) {
						consume();
						IToken t = consume();
						char[] image = t.getCharImage();
						if (image.length != 1 || image[0] != '0') {
							throwBacktrack(t);
						}
						((ICPPASTFunctionDeclarator) typeRelevantDtor).setPureVirtual(true);
						adjustEndOffset(dtor, t.getEndOffset()); // We can only adjust the offset of the outermost dtor.
					}
				}
			} else {
				if (LTcatchEOF(1) == IToken.tASSIGN && LTcatchEOF(2) == IToken.tLBRACE)
					throw new FoundAggregateInitializer(declspec, dtor);

				IASTInitializer initializer = optionalInitializer(dtor, option);
				if (initializer != null) {
					if (initializer instanceof IASTInitializerList
							&& ((IASTInitializerList) initializer).getSize() == 0) {
						// Avoid ambiguity between constructor with body and variable with initializer
						switch (LTcatchEOF(1)) {
						case IToken.tCOMMA:
						case IToken.tSEMI:
						case IToken.tRPAREN:
							break;
						case 0:
							throw backtrack;
						default:
							throwBacktrack(LA(1));
						}
					}
					dtor.setInitializer(initializer);
					adjustLength(dtor, initializer);
				}
			}
		}
		return dtor;
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
	private void optionalVirtSpecifierSeq(ICPPASTFunctionDeclarator typeRelevantDtor)
			throws EndOfFileException, BacktrackException {
		while (true) {
			IToken token = LAcatchEOF(1);
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
			ICPPASTVirtSpecifier spec = getNodeFactory().newVirtSpecifier(specifierKind);
			int endOffset = token.getOffset() + token.getLength();
			setRange(spec, token.getOffset(), endOffset);
			typeRelevantDtor.addVirtSpecifier(spec);
			adjustEndOffset(typeRelevantDtor, endOffset);
		}
	}

	private IASTEqualsInitializer equalsInitalizerClause(boolean allowSkipping)
			throws EndOfFileException, BacktrackException {
		// Check for deleted or defaulted function syntax.
		final int lt2 = LTcatchEOF(2);
		if (lt2 == IToken.t_delete || lt2 == IToken.t_default) {
			return null;
		}

		int offset = consume(IToken.tASSIGN).getOffset();
		IASTInitializerClause initClause = initClause(allowSkipping);
		IASTEqualsInitializer initExpr = getNodeFactory().newEqualsInitializer(initClause);
		return setRange(initExpr, offset, calculateEndOffset(initClause));
	}

	/**
	 * initializer:
	 *	brace-or-equal-initializer
	 *	(expression-list)
	 *
	 * brace-or-equal-initializer:
	 *	= initializer-clause
	 *	braced-init-list
	 */
	@Override
	protected IASTInitializer optionalInitializer(IASTDeclarator dtor, DeclarationOptions option)
			throws EndOfFileException, BacktrackException {
		final int lt1 = LTcatchEOF(1);

		// = initializer-clause
		if (lt1 == IToken.tASSIGN) {
			return equalsInitalizerClause(LTcatchEOF(2) == IToken.tLBRACE && specifiesArray(dtor));
		}

		// braced-init-list
		if (option.fAllowBracedInitializer && lt1 == IToken.tLBRACE) {
			return bracedInitList(false, false);
		}

		// (expression-list)
		if (option.fAllowCtorStyleInitializer && lt1 == IToken.tLPAREN) {
			return ctorStyleInitializer(false);
		}
		return null;
	}

	private boolean specifiesArray(IASTDeclarator dtor) {
		dtor = ASTQueries.findTypeRelevantDeclarator(dtor);
		return dtor instanceof IASTArrayDeclarator;
	}

	private IASTInitializer bracedOrCtorStyleInitializer() throws EndOfFileException, BacktrackException {
		final int lt1 = LT(1);
		if (lt1 == IToken.tLPAREN) {
			return ctorStyleInitializer(true);
		}
		return bracedInitList(false, true);
	}

	/**
	 * (expression-list_opt)
	 */
	private ICPPASTConstructorInitializer ctorStyleInitializer(boolean optionalExpressionList)
			throws EndOfFileException, BacktrackException {
		IASTInitializerClause[] initArray;
		int offset = consume(IToken.tLPAREN).getOffset();

		// ()
		if (optionalExpressionList && LT(1) == IToken.tRPAREN) {
			initArray = IASTExpression.EMPTY_EXPRESSION_ARRAY;
		} else {
			final List<ICPPASTInitializerClause> exprList = expressionList();
			initArray = exprList.toArray(new IASTInitializerClause[exprList.size()]);
		}
		int endOffset = consumeOrEOC(IToken.tRPAREN).getEndOffset();
		return setRange(getNodeFactory().newConstructorInitializer(initArray), offset, endOffset);
	}

	private List<ICPPASTInitializerClause> expressionList() throws EndOfFileException, BacktrackException {
		return initializerList(false, false);
	}

	/**
	 * initializer-clause:
	 *   assignment-expression
	 *   braced-init-list
	 */
	private ICPPASTInitializerClause initClause(boolean allowSkipping) throws EndOfFileException, BacktrackException {
		// braced-init-list
		if (LT(1) == IToken.tLBRACE) {
			return bracedInitList(allowSkipping, true);
		}

		// assignment expression
		TemplateIdStrategy strat = fTemplateParameterListStrategy;
		final BinaryExprCtx ctx = strat != null ? BinaryExprCtx.eInTemplateID : BinaryExprCtx.eNotInTemplateID;
		return expression(ExprKind.eAssignment, ctx, null, strat);
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
		if (LT(1) == IToken.tRBRACE) {
			return setRange(getNodeFactory().newInitializerList(), offset, consume().getEndOffset());
		}

		// { initializer-list ,opt }
		List<ICPPASTInitializerClause> initList = initializerList(allowSkipping, allowDesignators);
		if (LT(1) == IToken.tCOMMA)
			consume();

		int endOffset = consumeOrEOC(IToken.tRBRACE).getEndOffset();
		ICPPASTInitializerList result = getNodeFactory().newInitializerList();
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
					ICPPASTDesignatedInitializer desigInitializer = getNodeFactory().newDesignatedInitializer(null);
					setRange(desigInitializer, designators.get(0));
					for (ICPPASTDesignator d : designators) {
						desigInitializer.addDesignator(d);
					}

					if (LT(1) != IToken.tEOC) {
						// GNU extension: the assign operator is optional.
						if (LT(1) == IToken.tASSIGN)
							consume(IToken.tASSIGN);

						ICPPASTInitializerClause clause = initClause(false);
						desigInitializer.setOperand(clause);
						adjustLength(desigInitializer, clause);
					}
					result.add(desigInitializer);
				} catch (BacktrackException e) {
					backup(mark);
					designators = null; // Retry without designators.
				}
			}

			if (designators == null) {
				// Clause may be null, add to initializer anyways, so that the size can be computed.
				ICPPASTInitializerClause clause = initClause(allowSkipping);
				if (allowSkipping && result.size() >= maximumTrivialExpressionsInAggregateInitializers
						&& !ASTQueries.canContainName(clause)) {
					translationUnit.setHasNodesOmitted(true);
					clause = null;
				}
				if (LT(1) == IToken.tELLIPSIS) {
					final int endOffset = consume(IToken.tELLIPSIS).getEndOffset();
					if (clause instanceof ICPPASTPackExpandable) {
						// Mark initializer lists directly as pack expansions
						((ICPPASTPackExpandable) clause).setIsPackExpansion(true);
						adjustEndOffset(clause, endOffset);
					} else if (clause instanceof IASTExpression) {
						// Wrap pack expanded assignment expressions
						ICPPASTExpression packExpansion = getNodeFactory()
								.newPackExpansionExpression((IASTExpression) clause);
						clause = setRange(packExpansion, clause, endOffset);
					}
				}
				result.add(clause);
			}

			if (LT(1) != IToken.tCOMMA)
				break;
			switch (LT(2)) {
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
			final int lt1 = LT(1);
			if (lt1 == IToken.tDOT || lt1 == IToken.tLBRACKET) {
				List<ICPPASTDesignator> designatorList = null;
				while (true) {
					switch (LT(1)) {
					case IToken.tDOT:
						int offset = consume().getOffset();
						IASTName n = identifier();
						ICPPASTFieldDesignator fieldDesignator = getNodeFactory().newFieldDesignator(n);
						setRange(fieldDesignator, offset, calculateEndOffset(n));
						if (designatorList == null)
							designatorList = new ArrayList<>(DEFAULT_DESIGNATOR_LIST_SIZE);
						designatorList.add(fieldDesignator);
						break;

					case IToken.tLBRACKET:
						offset = consume().getOffset();
						ICPPASTExpression constantExpression = expression();
						if (supportGCCStyleDesignators && LT(1) == IToken.tELLIPSIS) {
							consume(IToken.tELLIPSIS);
							ICPPASTExpression constantExpression2 = expression();
							int lastOffset = consume(IToken.tRBRACKET).getEndOffset();
							IGPPASTArrayRangeDesignator designator = getNodeFactory()
									.newArrayRangeDesignatorGPP(constantExpression, constantExpression2);
							setRange(designator, offset, lastOffset);
							if (designatorList == null)
								designatorList = new ArrayList<>(DEFAULT_DESIGNATOR_LIST_SIZE);
							designatorList.add(designator);
						} else {
							int lastOffset = consume(IToken.tRBRACKET).getEndOffset();
							ICPPASTArrayDesignator designator = getNodeFactory().newArrayDesignator(constantExpression);
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

			// If reached identifier and it's not a designator then return empty designator list (bug 84176).
			if (supportGCCStyleDesignators && lt1 == IToken.tIDENTIFIER && LT(2) == IToken.tCOLON) {
				int offset = LA(1).getOffset();
				IASTName n = identifier();
				int lastOffset = consume(IToken.tCOLON).getEndOffset();
				ICPPASTFieldDesignator designator = getNodeFactory().newFieldDesignator(n);
				setRange(designator, offset, lastOffset);
				return Collections.singletonList(designator);
			}
		} catch (BacktrackException bt) {
			backup(mark);
		}

		return null;
	}

	@Override
	protected ICPPASTTypeId typeId(DeclarationOptions option) throws EndOfFileException, BacktrackException {
		return typeId(option, null);
	}

	protected ICPPASTTypeId typeId(DeclarationOptions option, ITemplateIdStrategy strat)
			throws EndOfFileException, BacktrackException {
		if (!canBeTypeSpecifier()) {
			throwBacktrack(LA(1));
		}
		final int offset = LA().getOffset();
		IASTDeclSpecifier declSpecifier = null;
		IASTDeclarator declarator = null;

		try {
			Decl decl = declSpecifierSequence_initDeclarator(option, false, strat);
			declSpecifier = decl.fDeclSpec1;
			declarator = decl.fDtor1;
		} catch (FoundAggregateInitializer lie) {
			// type-ids have no initializers
			throwBacktrack(lie.fDeclarator);
		}
		ICPPASTTypeId result = getNodeFactory().newTypeId(declSpecifier, declarator);
		setRange(result, offset, figureEndOffset(declSpecifier, declarator));
		return result;
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
	protected IASTDeclarator declarator(DtorStrategy strategy, DeclarationOptions option)
			throws EndOfFileException, BacktrackException {
		final int startingOffset = LA(1).getOffset();
		int endOffset = startingOffset;

		List<? extends IASTPointerOperator> pointerOps = consumePointerOperators();
		if (pointerOps != null) {
			endOffset = calculateEndOffset(pointerOps.get(pointerOps.size() - 1));
		}

		// Accept __attribute__ or __declspec between pointer operators and declarator.
		List<IASTAttributeSpecifier> attributes = __attribute_decl_seq(supportAttributeSpecifiers,
				supportDeclspecSpecifiers);

		// Look for identifier or nested declarator
		boolean hasEllipsis = false;
		if (option.fAllowParameterPacks && LT(1) == IToken.tELLIPSIS) {
			consume();
			hasEllipsis = true;
		}
		final int lt1 = LT(1);
		switch (lt1) {
		case IToken.tBITCOMPLEMENT:
		case IToken.t_operator:
		case IToken.tCOLONCOLON:
		case IToken.tIDENTIFIER:
		case IToken.tCOMPLETION:
			if (option.fRequireAbstract) {
				// We might have a virt-specifier following a type-id in a trailing-return-type.
				ContextSensitiveTokenType contextSensitiveType = getContextSensitiveType(LA(1));
				if (contextSensitiveType == ContextSensitiveTokenType.OVERRIDE
						|| contextSensitiveType == ContextSensitiveTokenType.FINAL) {
					// In that case, we're done parsing the declarator of the type-id.
					break;
				} else {
					// Otherwise, we have what looks like a name, but we're not expecting one.
					throwBacktrack(LA(1));
				}
			}

			final IASTName declaratorName = !option.fRequireSimpleName ? qualifiedName() : identifier();
			endOffset = calculateEndOffset(declaratorName);
			return declarator(pointerOps, hasEllipsis, declaratorName, null, startingOffset, endOffset, strategy,
					option, attributes);
		}

		if (lt1 == IToken.tLPAREN) {
			IASTDeclarator cand1 = null;
			IToken cand1End = null;
			// try an abstract function declarator
			if (option.fAllowAbstract && option.fAllowFunctions) {
				final IToken mark = mark();
				try {
					cand1 = declarator(pointerOps, hasEllipsis, getNodeFactory().newName(), null, startingOffset,
							endOffset, strategy, option, attributes);
					if (option.fRequireAbstract || !option.fAllowNested || hasEllipsis)
						return cand1;

					cand1End = LA(1);
				} catch (BacktrackException e) {
				}
				backup(mark);
			}

			// type-ids for new or operator-id:
			if (!option.fAllowNested || hasEllipsis) {
				if (option.fAllowAbstract) {
					return declarator(pointerOps, hasEllipsis, getNodeFactory().newName(), null, startingOffset,
							endOffset, strategy, option, attributes);
				}
				throwBacktrack(LA(1));
			}

			// try a nested declarator
			try {
				consume();
				if (LT(1) == IToken.tRPAREN)
					throwBacktrack(LA(1));

				final IASTDeclarator nested = declarator(DtorStrategy.PREFER_FUNCTION, option);
				endOffset = consume(IToken.tRPAREN).getEndOffset();
				final IASTDeclarator cand2 = declarator(pointerOps, hasEllipsis, getNodeFactory().newName(), nested,
						startingOffset, endOffset, strategy, option, attributes);
				if (cand1 == null || cand1End == null)
					return cand2;
				final IToken cand2End = LA(1);
				if (cand1End == cand2End) {
					CPPASTAmbiguousDeclarator result = new CPPASTAmbiguousDeclarator(cand1, cand2);
					((ASTNode) result).setOffsetAndLength((ASTNode) cand1);
					return result;
				}
				// use the longer variant
				if (cand1End.getOffset() < cand2End.getOffset())
					return cand2;

			} catch (BacktrackException e) {
				if (cand1 == null)
					throw e;
			}
			backup(cand1End);
			return cand1;
		}

		// try abstract declarator
		if (!option.fAllowAbstract) {
			// bit-fields may be abstract
			if (!option.fAllowBitField || LT(1) != IToken.tCOLON)
				throwBacktrack(LA(1));
		}
		return declarator(pointerOps, hasEllipsis, getNodeFactory().newName(), null, startingOffset, endOffset,
				strategy, option, attributes);
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

			// __attribute__ in-between pointers
			List<IASTAttributeSpecifier> attributes = __attribute_decl_seq(supportAttributeSpecifiers, false);

			final int lt1 = LT(1);
			if (lt1 == IToken.tAMPER || lt1 == IToken.tAND) {
				IToken endToken = consume();
				final int offset = endToken.getOffset();

				if (allowCPPRestrict && LT(1) == IToken.t_restrict) {
					endToken = consume();
				}
				ICPPASTReferenceOperator refOp = getNodeFactory().newReferenceOperator(lt1 == IToken.tAND);
				setRange(refOp, offset, endToken.getEndOffset());

				attributes = CollectionUtils.merge(attributes, attributeSpecifierSeq());
				addAttributeSpecifiers(attributes, refOp);

				if (result != null) {
					result.add(refOp);
					return result;
				}
				return Collections.singletonList(refOp);
			}

			boolean isConst = false, isVolatile = false, isRestrict = false;
			IASTName name = null;
			int coloncolon = LT(1) == IToken.tCOLONCOLON ? 1 : 0;
			loop: while (LTcatchEOF(coloncolon + 1) == IToken.tIDENTIFIER) {
				switch (LTcatchEOF(coloncolon + 2)) {
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
				} catch (BacktrackException bt) {
					backup(mark);
					return result;
				}
			}
			if (LTcatchEOF(1) != IToken.tSTAR) {
				backup(mark);
				return result;
			}

			int endOffset = consume().getEndOffset();
			loop: for (;;) {
				switch (LTcatchEOF(1)) {
				case IToken.t_const:
					endOffset = consume().getEndOffset();
					isConst = true;
					break;
				case IToken.t_volatile:
					endOffset = consume().getEndOffset();
					isVolatile = true;
					break;
				case IToken.t_restrict:
					if (!allowCPPRestrict)
						throwBacktrack(LA(1));
					endOffset = consume().getEndOffset();
					isRestrict = true;
					break;
				default:
					break loop;
				}
			}

			IASTPointer pointer;
			if (name != null) {
				pointer = getNodeFactory().newPointerToMember(name);
			} else {
				pointer = getNodeFactory().newPointer();
			}
			pointer.setConst(isConst);
			pointer.setVolatile(isVolatile);
			pointer.setRestrict(isRestrict);
			if (result == null) {
				result = new ArrayList<>(4);
			}

			attributes = CollectionUtils.merge(attributes, attributeSpecifierSeq());
			addAttributeSpecifiers(attributes, pointer);
			endOffset = attributesEndOffset(endOffset, attributes);
			setRange(pointer, startOffset, endOffset);

			result.add(pointer);
		}
	}

	private IASTDeclarator declarator(List<? extends IASTPointerOperator> pointerOps, boolean hasEllipsis,
			IASTName declaratorName, IASTDeclarator nestedDeclarator, int startingOffset, int endOffset,
			DtorStrategy strategy, DeclarationOptions option, List<IASTAttributeSpecifier> attributes)
			throws EndOfFileException, BacktrackException {
		ICPPASTDeclarator result = null;
		loop: while (true) {
			final int lt1 = LTcatchEOF(1);
			switch (lt1) {
			case IToken.tLPAREN:
				if (option.fAllowFunctions && strategy == DtorStrategy.PREFER_FUNCTION) {
					result = functionDeclarator(false);
					setDeclaratorID(result, hasEllipsis, declaratorName, nestedDeclarator);
				}
				break loop;

			case IToken.tLBRACKET:
				if (LTcatchEOF(2) == IToken.tLBRACKET) {
					attributes = CollectionUtils.merge(attributes, attributeSpecifierSeq());
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

			case IGCCToken.t__attribute__: // if __attribute__ is after a declarator
				if (!supportAttributeSpecifiers)
					throwBacktrack(LA(1));
				attributes = CollectionUtils.merge(attributes, __attribute_decl_seq(true, supportDeclspecSpecifiers));
				break;
			case IGCCToken.t__declspec:
				if (!supportDeclspecSpecifiers)
					throwBacktrack(LA(1));
				attributes = CollectionUtils.merge(attributes, __attribute_decl_seq(supportAttributeSpecifiers, true));
				break;
			default:
				break loop;
			}
		}
		attributes = CollectionUtils.merge(attributes,
				__attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers));

		if (result == null) {
			result = getNodeFactory().newDeclarator(null);
			setDeclaratorID(result, hasEllipsis, declaratorName, nestedDeclarator);
		} else {
			endOffset = calculateEndOffset(result);
		}

		if (LTcatchEOF(1) == IToken.t_asm) { // asm labels bug 226121
			consume();
			endOffset = asmExpression(null).getEndOffset();

			attributes = CollectionUtils.merge(attributes,
					__attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers));
		}

		if (pointerOps != null) {
			for (IASTPointerOperator po : pointerOps) {
				result.addPointerOperator(po);
			}
		}

		addAttributeSpecifiers(attributes, result);
		endOffset = attributesEndOffset(endOffset, attributes);
		setRange(result, startingOffset, endOffset);

		return result;
	}

	private void setDeclaratorID(ICPPASTDeclarator declarator, boolean hasEllipsis, IASTName declaratorName,
			IASTDeclarator nestedDeclarator) {
		if (nestedDeclarator != null) {
			declarator.setNestedDeclarator(nestedDeclarator);
			declarator.setName(getNodeFactory().newName());
		} else {
			declarator.setName(declaratorName);
		}
		declarator.setDeclaresParameterPack(hasEllipsis);
	}

	/**
	 * Parse a function declarator starting with the left parenthesis.
	 */
	private ICPPASTFunctionDeclarator functionDeclarator(boolean isLambdaDeclarator)
			throws EndOfFileException, BacktrackException {
		IToken last = consume(IToken.tLPAREN);
		final int startOffset = last.getOffset();
		int endOffset = last.getEndOffset();

		final ICPPASTFunctionDeclarator fc = getNodeFactory().newFunctionDeclarator(null);
		ICPPASTParameterDeclaration pd = null;
		paramLoop: while (true) {
			switch (LT(1)) {
			case IToken.tRPAREN:
			case IToken.tEOC:
				endOffset = consume().getEndOffset();
				break paramLoop;
			case IToken.tELLIPSIS:
				consume();
				endOffset = consume(IToken.tRPAREN).getEndOffset();
				fc.setVarArgs(true);
				break paramLoop;
			case IToken.tCOMMA:
				if (pd == null)
					throwBacktrack(LA(1));
				endOffset = consume().getEndOffset();
				pd = null;
				break;
			default:
				if (pd != null)
					throwBacktrack(startOffset, endOffset - startOffset);

				pd = parameterDeclaration();
				fc.addParameterDeclaration(pd);
				endOffset = calculateEndOffset(pd);
				break;
			}
		}
		// Handle ambiguity between parameter pack and varargs.
		if (pd != null) {
			ICPPASTDeclarator dtor = pd.getDeclarator();
			if (dtor != null && !(dtor instanceof IASTAmbiguousDeclarator)) {
				if (dtor.declaresParameterPack() && dtor.getNestedDeclarator() == null && dtor.getInitializer() == null
						&& dtor.getName().getSimpleID().length == 0) {
					((IASTAmbiguityParent) fc).replace(pd, new CPPASTAmbiguousParameterDeclaration(pd));
				}
			}
		}

		// Consume any number of __attribute__ tokens after the parameters
		List<IASTAttributeSpecifier> attributes = __attribute_decl_seq(supportAttributeSpecifiers, false);

		// cv-qualifiers
		if (isLambdaDeclarator) {
			if (LT(1) == IToken.t_mutable) {
				fc.setMutable(true);
				endOffset = consume().getEndOffset();
			}
		} else {
			cvloop: while (true) {
				switch (LT(1)) {
				case IToken.t_const:
					fc.setConst(true);
					endOffset = consume().getEndOffset();
					break;
				case IToken.t_volatile:
					fc.setVolatile(true);
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
			fc.setRefQualifier(refQualifier);
			endOffset = getEndOffset();
		}

		// throws clause
		if (LT(1) == IToken.t_throw) {
			fc.setEmptyExceptionSpecification();
			consume(); // throw
			consume(IToken.tLPAREN);

			thloop: while (true) {
				switch (LT(1)) {
				case IToken.tRPAREN:
				case IToken.tEOC:
					endOffset = consume().getEndOffset();
					break thloop;
				case IToken.tCOMMA:
					consume();
					break;
				default:
					int thoffset = LA(1).getOffset();
					try {
						ICPPASTTypeId typeId = typeId(DeclarationOptions.TYPEID);
						if (LT(1) == IToken.tELLIPSIS) {
							typeId.setIsPackExpansion(true);
							adjustEndOffset(typeId, consume().getEndOffset());
						}
						fc.addExceptionSpecificationTypeId(typeId);
					} catch (BacktrackException e) {
						int thendoffset = LA(1).getOffset();
						if (thoffset == thendoffset) {
							thendoffset = consume().getEndOffset();
						}
						IASTProblem p = createProblem(IProblem.SYNTAX_ERROR, thoffset, thendoffset - thoffset);
						IASTProblemTypeId typeIdProblem = getNodeFactory().newProblemTypeId(p);
						((ASTNode) typeIdProblem).setOffsetAndLength(((ASTNode) p));
						fc.addExceptionSpecificationTypeId(typeIdProblem);
					}
					break;
				}
			}

			// more __attribute__ after throws
			attributes = CollectionUtils.merge(attributes, __attribute_decl_seq(supportAttributeSpecifiers, false));
		}

		// noexcept specification
		if (LT(1) == IToken.t_noexcept) {
			consume(); // noexcept
			IASTExpression expression = ICPPASTFunctionDeclarator.NOEXCEPT_DEFAULT;
			endOffset = getEndOffset();
			if (LT(1) == IToken.tLPAREN) {
				consume(); // (
				expression = expression();
				consume(IToken.tRPAREN); //)
				endOffset = getEndOffset();
			}
			fc.setNoexceptExpression((ICPPASTExpression) expression);
		}

		attributes = CollectionUtils.merge(attributes, attributeSpecifierSeq());
		addAttributeSpecifiers(attributes, fc);
		endOffset = attributesEndOffset(endOffset, attributes);

		if (LT(1) == IToken.tARROW) {
			consume();
			IASTTypeId typeId = typeId(DeclarationOptions.TYPEID_TRAILING_RETURN_TYPE);
			fc.setTrailingReturnType(typeId);
			endOffset = calculateEndOffset(typeId);
		}

		return setRange(fc, startOffset, endOffset);
	}

	/**
	 * Parse an array declarator starting at the square bracket.
	 */
	private ICPPASTArrayDeclarator arrayDeclarator(DeclarationOptions option)
			throws EndOfFileException, BacktrackException {
		ArrayList<IASTArrayModifier> arrayMods = new ArrayList<>(4);
		int start = LA(1).getOffset();
		consumeArrayModifiers(option, arrayMods);
		if (arrayMods.isEmpty())
			throwBacktrack(LA(1));

		final int endOffset = calculateEndOffset(arrayMods.get(arrayMods.size() - 1));
		final ICPPASTArrayDeclarator d = getNodeFactory().newArrayDeclarator(null);
		for (IASTArrayModifier m : arrayMods) {
			d.addArrayModifier(m);
		}

		((ASTNode) d).setOffsetAndLength(start, endOffset - start);
		return d;
	}

	/**
	 * Parses for a bit field declarator starting with the colon
	 */
	private ICPPASTFieldDeclarator bitFieldDeclarator() throws EndOfFileException, BacktrackException {
		int start = consume(IToken.tCOLON).getOffset();

		final IASTExpression bitField = constantExpression();
		final int endOffset = calculateEndOffset(bitField);

		ICPPASTFieldDeclarator d = getNodeFactory().newFieldDeclarator(null, bitField);
		((ASTNode) d).setOffsetAndLength(start, endOffset - start);
		return d;
	}

	/**
	 * Parse a class/struct/union definition. classSpecifier : classKey attribute-specifier-seq? name
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
		switch (LT(1)) {
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

		// if __attribute__ or __declspec occurs after struct/union/class and before the identifier
		List<IASTAttributeSpecifier> attributes = anyAttributes(supportAttributeSpecifiers, supportDeclspecSpecifiers);

		// class name
		IASTName name = null;
		if (LT(1) == IToken.tIDENTIFIER) {
			name = qualifiedName();
		} else {
			name = getNodeFactory().newName();
		}

		// if __attribute__ or __declspec occurs after struct/union/class identifier and before the { or ;
		attributes = CollectionUtils.merge(attributes,
				__attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers));

		ICPPASTCompositeTypeSpecifier astClassSpecifier = getNodeFactory().newCompositeTypeSpecifier(classKind, name);
		addAttributeSpecifiers(attributes, astClassSpecifier);

		// class virt specifier
		if (LT(1) == IToken.tIDENTIFIER) {
			classVirtSpecifier(astClassSpecifier);
		}

		// base clause
		if (LT(1) == IToken.tCOLON) {
			try {
				baseClause(astClassSpecifier);
			} catch (BacktrackException e) {
				// Couldn't parse a base-clause.
				// Backtrack and try an elaborated-type-specifier instead.
				backup(mark);
				throw e;
			}
			// content assist within the base-clause
			if (LT(1) == IToken.tEOC) {
				return astClassSpecifier;
			}
		}

		if (LT(1) != IToken.tLBRACE) {
			IToken errorPoint = LA(1);
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

	protected int token2Visibility(int type) {
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
			ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier baseSpec = baseSpecifier();
			astClassSpec.addBaseSpecifier(baseSpec);

			if (LT(1) == IToken.tELLIPSIS) {
				baseSpec.setIsPackExpansion(true);
				adjustEndOffset(baseSpec, consume().getEndOffset());
			}

			if (LT(1) != IToken.tCOMMA) {
				return;
			}

			consume();
		}
	}

	/**
	 * Parse a class virtual specifier for a class specification.
	 * class-virt-specifier:
	 *	final
	 * @param astClassSpecifier
	 */
	private void classVirtSpecifier(ICPPASTCompositeTypeSpecifier astClassSpecifier)
			throws EndOfFileException, BacktrackException {
		IToken token = LA();
		ContextSensitiveTokenType contextSensitiveType = getContextSensitiveType(token);
		if (contextSensitiveType == ContextSensitiveTokenType.FINAL) {
			consume();
			ICPPASTClassVirtSpecifier spec = getNodeFactory()
					.newClassVirtSpecifier(ICPPASTClassVirtSpecifier.SpecifierKind.Final);
			setRange(spec, token.getOffset(), token.getOffset() + token.getLength());
			astClassSpecifier.setVirtSpecifier(spec);
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
		int startOffset = LA(1).getOffset();
		boolean isVirtual = false;
		int visibility = 0;
		ICPPASTNameSpecifier nameSpec = null;
		loop: for (;;) {
			switch (LT(1)) {
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
		ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier baseSpec = getNodeFactory().newBaseSpecifier(nameSpec,
				visibility, isVirtual);
		setRange(baseSpec, startOffset, calculateEndOffset(nameSpec));
		return baseSpec;
	}

	protected void catchHandlerSequence(List<ICPPASTCatchHandler> collection)
			throws EndOfFileException, BacktrackException {
		if (LT(1) == IToken.tEOC)
			return;

		if (LT(1) != IToken.t_catch)
			throwBacktrack(LA(1)); // error, need at least one

		int lt1 = LT(1);
		while (lt1 == IToken.t_catch) {
			int startOffset = consume().getOffset();
			consume(IToken.tLPAREN);
			boolean isEllipsis = false;
			IASTDeclaration decl = null;
			try {
				if (LT(1) == IToken.tELLIPSIS) {
					consume(IToken.tELLIPSIS);
					isEllipsis = true;
				} else {
					decl = simpleSingleDeclaration(DeclarationOptions.EXCEPTION);
				}
				if (LT(1) != IToken.tEOC)
					consume(IToken.tRPAREN);
			} catch (BacktrackException bte) {
				failParse();
				IASTProblem p = createProblem(bte);
				IASTProblemDeclaration pd = getNodeFactory().newProblemDeclaration(p);
				((ASTNode) pd).setOffsetAndLength(((ASTNode) p));
				decl = pd;
			}

			ICPPASTCatchHandler handler = getNodeFactory().newCatchHandler(decl, null);

			if (LT(1) != IToken.tEOC) {
				IASTStatement compoundStatement = catchBlockCompoundStatement();
				((ASTNode) handler).setOffsetAndLength(startOffset,
						calculateEndOffset(compoundStatement) - startOffset);
				handler.setIsCatchAll(isEllipsis);
				if (compoundStatement != null) {
					handler.setCatchBody(compoundStatement);
				}
			}

			collection.add(handler);
			lt1 = LTcatchEOF(1);
		}
	}

	private IASTSimpleDeclaration simpleSingleDeclaration(DeclarationOptions options)
			throws BacktrackException, EndOfFileException {
		final int startOffset = LA(1).getOffset();
		IASTDeclSpecifier declSpec;
		IASTDeclarator declarator;

		List<IASTAttributeSpecifier> attributes = attributeSpecifierSeq();
		try {
			Decl decl = declSpecifierSequence_initDeclarator(options, true);
			declSpec = decl.fDeclSpec1;
			declarator = decl.fDtor1;
		} catch (FoundAggregateInitializer lie) {
			declSpec = lie.fDeclSpec;
			declarator = addInitializer(lie, options);
		}

		final int endOffset = figureEndOffset(declSpec, declarator);
		final IASTSimpleDeclaration decl = getNodeFactory().newSimpleDeclaration(declSpec);
		if (declarator != null)
			decl.addDeclarator(declarator);
		((ASTNode) decl).setOffsetAndLength(startOffset, endOffset - startOffset);
		addAttributeSpecifiers(attributes, decl);
		return decl;
	}

	protected IASTStatement catchBlockCompoundStatement() throws BacktrackException, EndOfFileException {
		if (mode == ParserMode.QUICK_PARSE || mode == ParserMode.STRUCTURAL_PARSE || !isActiveCode()) {
			int offset = LA(1).getOffset();
			IToken last = skipOverCompoundStatement(true);
			IASTCompoundStatement cs = getNodeFactory().newCompoundStatement();
			setRange(cs, offset, last.getEndOffset());
			return cs;
		} else if (mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE) {
			if (scanner.isOnTopContext())
				return compoundStatement();
			int offset = LA(1).getOffset();
			IToken last = skipOverCompoundStatement(true);
			IASTCompoundStatement cs = getNodeFactory().newCompoundStatement();
			setRange(cs, offset, last.getEndOffset());
			return cs;
		}
		return compoundStatement();
	}

	@Override
	protected void setupTranslationUnit() throws DOMException {
		translationUnit = getNodeFactory().newTranslationUnit(scanner);
		translationUnit.setIndex(index);

		// Add built-in names to the scope.
		if (builtinBindingsProvider != null) {
			if (translationUnit instanceof ASTTranslationUnit) {
				((ASTTranslationUnit) translationUnit).setupBuiltinBindings(builtinBindingsProvider);
			}
		}
	}

	private void consumeArrayModifiers(DeclarationOptions option, List<IASTArrayModifier> collection)
			throws EndOfFileException, BacktrackException {
		boolean allowExpression = option == DeclarationOptions.TYPEID_NEW;
		while (LT(1) == IToken.tLBRACKET) {
			int startOffset = consume().getOffset(); // eat the '['

			IASTExpression exp = null;
			if (LT(1) != IToken.tRBRACKET && LT(1) != IToken.tEOC) {
				exp = allowExpression ? expression() : constantExpression();
				allowExpression = false;
			}
			int endOffset;
			switch (LT(1)) {
			case IToken.tRBRACKET:
			case IToken.tEOC:
				endOffset = consume().getEndOffset();
				break;
			default:
				throw backtrack;
			}
			IASTArrayModifier arrayMod = getNodeFactory().newArrayModifier(exp);

			List<IASTAttributeSpecifier> attributes = attributeSpecifierSeq();
			addAttributeSpecifiers(attributes, arrayMod);
			endOffset = attributesEndOffset(endOffset, attributes);

			setRange(arrayMod, startOffset, endOffset);

			collection.add(arrayMod);
		}
		return;
	}

	@Override
	protected IASTTranslationUnit getTranslationUnit() {
		return translationUnit;
	}

	@Override
	protected IASTStatement statement() throws EndOfFileException, BacktrackException {
		int startOffset = LA(1).getOffset();
		List<IASTAttributeSpecifier> attributes = attributeSpecifierSeq();

		IASTStatement statement = null;
		switch (LT(1)) {
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
			if (LT(1) == IToken.tIDENTIFIER && LT(2) == IToken.tCOLON) {
				statement = parseLabelStatement();
				break;
			}

			return parseDeclarationOrExpressionStatement(attributes);
		}
		addAttributeSpecifiers(attributes, statement);

		int endOffset = calculateEndOffset(statement);
		return setRange(statement, startOffset, endOffset);
	}

	protected IASTStatement parseTryStatement() throws EndOfFileException, BacktrackException {
		int startO = consume().getOffset();
		IASTStatement tryBlock = compoundStatement();
		List<ICPPASTCatchHandler> catchHandlers = new ArrayList<>(DEFAULT_CATCH_HANDLER_LIST_SIZE);
		catchHandlerSequence(catchHandlers);
		ICPPASTTryBlockStatement tryStatement = getNodeFactory().newTryBlockStatement(tryBlock);
		((ASTNode) tryStatement).setOffset(startO);

		for (int i = 0; i < catchHandlers.size(); ++i) {
			ICPPASTCatchHandler handler = catchHandlers.get(i);
			tryStatement.addCatchHandler(handler);
			((ASTNode) tryStatement).setLength(calculateEndOffset(handler) - startO);
		}
		return tryStatement;
	}

	@Override
	protected void nullifyTranslationUnit() {
		translationUnit = null;
	}

	@Override
	protected IASTStatement parseWhileStatement() throws EndOfFileException, BacktrackException {
		int startOffset = consume().getOffset();
		consume(IToken.tLPAREN);
		IASTNode while_condition = cppStyleCondition(IToken.tRPAREN);
		switch (LT(1)) {
		case IToken.tRPAREN:
			consume();
			break;
		case IToken.tEOC:
			break;
		default:
			throwBacktrack(LA(1));
		}
		IASTStatement while_body = null;
		if (LT(1) != IToken.tEOC)
			while_body = statement();

		IASTWhileStatement while_statement;
		if (while_condition instanceof IASTExpression)
			while_statement = getNodeFactory().newWhileStatement((IASTExpression) while_condition, while_body);
		else
			while_statement = getNodeFactory().newWhileStatement((IASTDeclaration) while_condition, while_body);

		((ASTNode) while_statement).setOffsetAndLength(startOffset,
				(while_body != null ? calculateEndOffset(while_body) : LA(1).getEndOffset()) - startOffset);
		return while_statement;

	}

	protected IASTNode cppStyleCondition(int expectToken) throws BacktrackException, EndOfFileException {
		IASTExpression e = null;
		IASTSimpleDeclaration decl = null;
		IToken end = null;

		IToken mark = mark();
		try {
			decl = simpleSingleDeclaration(DeclarationOptions.CONDITION);
			end = LA(1);
			final int la = end.getType();
			if (la != expectToken && la != IToken.tEOC) {
				end = null;
				decl = null;
			}
		} catch (BacktrackException b) {
		}

		backup(mark);
		try {
			e = expression();

			final IToken end2 = LA(1);
			final int la = end2.getType();
			if (la != expectToken && la != IToken.tEOC) {
				throwBacktrack(end2);
			}
			if (end == null)
				return e;

			final int endOffset = end.getOffset();
			final int endOffset2 = end2.getOffset();
			if (endOffset == endOffset2) {
				CPPASTAmbiguousCondition ambig = new CPPASTAmbiguousCondition(e, decl);
				setRange(ambig, e);
				return ambig;
			}

			if (endOffset < endOffset2)
				return e;
		} catch (BacktrackException bt) {
			if (end == null) {
				if (expectToken == IToken.tRPAREN) {
					backup(mark);
					return skipProblemConditionInParenthesis(mark.getOffset());
				}
				throw bt;
			}
		}
		backup(end);
		return decl;
	}

	@Override
	protected ASTVisitor createAmbiguityNodeVisitor() {
		return new CPPASTAmbiguityResolver();
	}

	@Override
	protected IASTAmbiguousStatement createAmbiguousStatement() {
		return new CPPASTAmbiguousStatement();
	}

	protected IASTStatement parseIfStatement() throws EndOfFileException, BacktrackException {
		ICPPASTIfStatement result = null;
		ICPPASTIfStatement if_statement = null;
		int start = LA(1).getOffset();
		if_loop: while (true) {
			int so = consume(IToken.t_if).getOffset();
			ICPPASTIfStatement new_if_statement = getNodeFactory().newIfStatement();
			// constexpr if
			if (LT(1) == IToken.t_constexpr) {
				consume();
				new_if_statement.setIsConstexpr(true);
			}
			consume(IToken.tLPAREN);
			// init-statement
			IToken mark = mark();
			try {
				IASTStatement statement = initStatement();
				new_if_statement.setInitializerStatement(statement);
			} catch (BacktrackException e) {
				backup(mark);
			}
			// condition
			IASTNode condition = cppStyleCondition(IToken.tRPAREN);
			if (LT(1) == IToken.tEOC) {
				// Completing in the condition
				if (condition instanceof IASTExpression)
					new_if_statement.setConditionExpression((IASTExpression) condition);
				else if (condition instanceof IASTDeclaration)
					new_if_statement.setConditionDeclaration((IASTDeclaration) condition);

				if (if_statement != null) {
					if_statement.setElseClause(new_if_statement);
				}
				return result != null ? result : new_if_statement;
			}
			consume(IToken.tRPAREN);

			IASTStatement thenClause = statement();
			((ASTNode) new_if_statement).setOffset(so);
			if (condition != null && (condition instanceof IASTExpression || condition instanceof IASTDeclaration))
			// shouldn't be possible but failure in condition() makes it so
			{
				if (condition instanceof IASTExpression)
					new_if_statement.setConditionExpression((IASTExpression) condition);
				else if (condition instanceof IASTDeclaration)
					new_if_statement.setConditionDeclaration((IASTDeclaration) condition);
			}
			if (thenClause != null) {
				new_if_statement.setThenClause(thenClause);
				((ASTNode) new_if_statement)
						.setLength(calculateEndOffset(thenClause) - ((ASTNode) new_if_statement).getOffset());
			}
			if (LT(1) == IToken.t_else) {
				consume();
				if (LT(1) == IToken.t_if) {
					// an else if, don't recurse, just loop and do another if

					if (if_statement != null) {
						if_statement.setElseClause(new_if_statement);
						((ASTNode) if_statement)
								.setLength(calculateEndOffset(new_if_statement) - ((ASTNode) if_statement).getOffset());
					}
					if (result == null && if_statement != null)
						result = if_statement;
					if (result == null)
						result = new_if_statement;

					if_statement = new_if_statement;
					continue if_loop;
				}
				IASTStatement elseStatement = statement();
				new_if_statement.setElseClause(elseStatement);
				if (if_statement != null) {
					if_statement.setElseClause(new_if_statement);
					((ASTNode) if_statement)
							.setLength(calculateEndOffset(new_if_statement) - ((ASTNode) if_statement).getOffset());
				} else {
					if (result == null)
						result = new_if_statement;
					if_statement = new_if_statement;
				}
			} else {
				if (thenClause != null)
					((ASTNode) new_if_statement).setLength(calculateEndOffset(thenClause) - start);
				if (if_statement != null) {
					if_statement.setElseClause(new_if_statement);
					((ASTNode) new_if_statement).setLength(calculateEndOffset(new_if_statement) - start);
				}
				if (result == null && if_statement != null)
					result = if_statement;
				if (result == null)
					result = new_if_statement;

				if_statement = new_if_statement;
			}
			break if_loop;
		}

		reconcileLengths(result);
		return result;
	}

	@Override
	protected IASTCompoundStatement functionBody() throws EndOfFileException, BacktrackException {
		++functionBodyCount;
		try {
			return super.functionBody();
		} finally {
			--functionBodyCount;
		}
	}

	protected IASTStatement parseSwitchStatement() throws EndOfFileException, BacktrackException {
		int startOffset;
		startOffset = consume().getOffset();
		consume(IToken.tLPAREN);

		ICPPASTSwitchStatement switch_statement = getNodeFactory().newSwitchStatement();
		// init-statement
		IToken mark = mark();
		try {
			IASTStatement statement = initStatement();
			switch_statement.setInitializerStatement(statement);
		} catch (BacktrackException e) {
			backup(mark);
		}

		IASTNode switch_condition = cppStyleCondition(IToken.tRPAREN);
		switch (LT(1)) {
		case IToken.tRPAREN:
			consume();
			break;
		case IToken.tEOC:
			break;
		default:
			throwBacktrack(LA(1));
		}

		IASTStatement switch_body = parseSwitchBody();
		((ASTNode) switch_statement).setOffsetAndLength(startOffset,
				(switch_body != null ? calculateEndOffset(switch_body) : LA(1).getEndOffset()) - startOffset);
		if (switch_condition instanceof IASTExpression) {
			switch_statement.setControllerExpression((IASTExpression) switch_condition);
		} else if (switch_condition instanceof IASTDeclaration) {
			switch_statement.setControllerDeclaration((IASTDeclaration) switch_condition);
		}

		if (switch_body != null) {
			switch_statement.setBody(switch_body);
		}

		return switch_statement;
	}

	protected IASTStatement parseForStatement() throws EndOfFileException, BacktrackException {
		final int offset = consume(IToken.t_for).getOffset();
		consume(IToken.tLPAREN);
		IToken mark = mark();
		IASTStatement forStmt;
		try {
			forStmt = startRangeBasedForLoop();
		} catch (BacktrackException e) {
			backup(mark);
			forStmt = startTraditionalForLoop();
		}
		mark = null;
		int endOffset = consumeOrEOC(IToken.tRPAREN).getEndOffset();

		if (LT(1) != IToken.tEOC) {
			IASTStatement body = statement();
			if (forStmt instanceof ICPPASTRangeBasedForStatement) {
				((ICPPASTRangeBasedForStatement) forStmt).setBody(body);
			} else {
				((IASTForStatement) forStmt).setBody(body);
			}
			endOffset = calculateEndOffset(body);
		}
		return setRange(forStmt, offset, endOffset);
	}

	//	Look for "for-range-declaration : for-range-initializer"
	//	for-range-declaration:
	//			attribute-specifier-seq? type-specifier-seq declarator
	//	for-range-initializer:
	//		expression
	//		braced-init-list
	private ICPPASTRangeBasedForStatement startRangeBasedForLoop() throws EndOfFileException, BacktrackException {
		List<IASTAttributeSpecifier> attributes = attributeSpecifierSeq();
		IASTDeclaration decl = simpleDeclaration(DeclarationOptions.RANGE_BASED_FOR, attributes);
		consume(IToken.tCOLON);
		IASTInitializerClause init = null;
		switch (LT(1)) {
		case IToken.tEOC:
			break;
		case IToken.tLBRACE:
			init = bracedInitList(false, false);
			break;
		default:
			init = expression();
		}

		ICPPASTRangeBasedForStatement result = getNodeFactory().newRangeBasedForStatement();
		result.setDeclaration(decl);
		result.setInitializerClause(init);
		return result;
	}

	private IASTForStatement startTraditionalForLoop() throws BacktrackException, EndOfFileException {
		final IASTStatement initStmt = initStatement();
		IASTNode condition = null;
		IASTExpression iterExpr = null;

		int lt1 = LT(1);
		if (lt1 != IToken.tSEMI && lt1 != IToken.tEOC) {
			condition = cppStyleCondition(IToken.tSEMI);
		}
		consumeOrEOC(IToken.tSEMI);

		lt1 = LT(1);
		if (lt1 != IToken.tRPAREN && lt1 != IToken.tEOC) {
			iterExpr = expression();
		}

		ICPPASTForStatement result = getNodeFactory().newForStatement();
		result.setInitializerStatement(initStmt);
		if (condition instanceof IASTExpression) {
			result.setConditionExpression((IASTExpression) condition);
		} else if (condition instanceof IASTDeclaration) {
			result.setConditionDeclaration((IASTDeclaration) condition);
		}
		result.setIterationExpression(iterExpr);
		return result;
	}

	@Override
	protected IASTStatement parseReturnStatement() throws EndOfFileException, BacktrackException {
		final int offset = consume(IToken.t_return).getOffset(); // t_return

		// Optional expression
		IASTInitializerClause expr = null;
		final int lt1 = LT(1);
		if (lt1 == IToken.tLBRACE) {
			expr = bracedInitList(true, false);
		} else if (lt1 != IToken.tSEMI) {
			expr = expression();
		}
		// Semicolon
		final int endOffset = consumeOrEOC(IToken.tSEMI).getEndOffset();

		return setRange(getNodeFactory().newReturnStatement(expr), offset, endOffset);
	}

	@Override
	protected IASTExpression expressionWithOptionalTrailingEllipsis() throws BacktrackException, EndOfFileException {
		IASTExpression result = expression();
		if (LT(1) == IToken.tELLIPSIS) {
			result = addPackExpansion(result, consume());
		}
		return result;
	}

	@Override
	protected IASTTypeId typeIdWithOptionalTrailingEllipsis(DeclarationOptions option)
			throws EndOfFileException, BacktrackException {
		ICPPASTTypeId result = typeId(option);
		if (LT(1) == IToken.tELLIPSIS) {
			addPackExpansion(result, consume());
		}
		return result;
	}

	@Override
	protected IASTAlignmentSpecifier createAmbiguousAlignmentSpecifier(IASTAlignmentSpecifier expression,
			IASTAlignmentSpecifier typeId) {
		return new CPPASTAmbiguousAlignmentSpecifier(expression, typeId);
	}

	protected IASTName[] identifierList() throws EndOfFileException, BacktrackException {
		List<IASTName> result = new ArrayList<>();
		result.add(identifier());
		while (LT(1) == IToken.tCOMMA) {
			consume();
			result.add(identifier());
		}
		return result.toArray(new IASTName[result.size()]);
	}
}

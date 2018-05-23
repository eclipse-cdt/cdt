/*******************************************************************************
 * Copyright (c) 2006, 2017 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.formatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTAttributeOwner;
import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAttributeList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTClassVirtSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDecltypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionWithTryBlock;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression.CaptureDefault;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTRangeBasedForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTStructuredBindingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.gnu.IGCCASTAttributeList;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterOptions;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.util.IUnaryPredicate;
import org.eclipse.cdt.core.parser.util.InstanceOfPredicate;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.formatter.align.Alignment;
import org.eclipse.cdt.internal.formatter.align.AlignmentException;
import org.eclipse.cdt.internal.formatter.scanner.Scanner;
import org.eclipse.cdt.internal.formatter.scanner.Token;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.Position;
import org.eclipse.text.edits.TextEdit;

/**
 * This class is responsible for formatting C/C++ source code.
 * Some heuristic is applied in case of syntax errors or other problems
 * to skip those areas, but because of incomplete location information
 * the formatting may fail. The reason of the failure is logged.
 *
 * @since 4.0
 */
public class CodeFormatterVisitor extends ASTVisitor implements ICPPASTVisitor, ICASTVisitor {
	private static boolean DEBUG = Boolean
			.parseBoolean(Platform.getDebugOption("org.eclipse.cdt.core/debug/formatter")); //$NON-NLS-1$

	private static class ASTProblemException extends RuntimeException {
		ASTProblemException(IASTProblem problem) {
			super(problem.getMessageWithLocation());
		}
	}

	private static class ListOptions {
		final int fMode;
		int fSeparatorToken = Token.tCOMMA;
		boolean fSpaceBeforeSeparator;
		boolean fSpaceAfterSeparator = true;
		boolean fSpaceAfterOpeningParen;
		boolean fSpaceBeforeClosingParen;
		boolean fSpaceBetweenEmptyParen;
		boolean fSpaceBeforeOpeningParen;
		int fContinuationIndentation = -1;
		int fTieBreakRule = Alignment.R_INNERMOST;
		CaptureDefault captureDefault;
		int rightToken;
		int leftToken;

		ListOptions(int mode) {
			this.fMode = mode;
			captureDefault = CaptureDefault.UNSPECIFIED;
			rightToken = Token.tRPAREN;
			leftToken = Token.tLPAREN;
		}
	}

	/**
	 * Formats a given token at a given position.
	 * @see #formatList(List, ListOptions, boolean, boolean, Runnable)
	 */
	private class TrailingTokenFormatter implements Runnable {
		private final int tokenType;
		private final int tokenPosition;
		private final boolean spaceBeforeToken;
		private final boolean spaceAfterToken;

		TrailingTokenFormatter(int tokenType, int tokenPosition, boolean spaceBeforeToken, boolean spaceAfterToken) {
			this.tokenType = tokenType;
			this.tokenPosition = tokenPosition;
			this.spaceBeforeToken = spaceBeforeToken;
			this.spaceAfterToken = spaceAfterToken;
		}

		TrailingTokenFormatter(int tokenType, IASTNode containingNode, boolean spaceBeforeToken,
				boolean spaceAfterToken) {
			this(tokenType, findTokenWithinNode(tokenType, containingNode), spaceBeforeToken, spaceAfterToken);
		}

		@Override
		public void run() {
			int offset = scribe.scanner.getCurrentPosition();
			if (tokenPosition < 0 || offset > tokenPosition)
				return;
			if (offset < tokenPosition)
				scribe.restartAtOffset(tokenPosition);
			int token = peekNextToken();
			if (token == tokenType) {
				scribe.undoSpace();
				scribe.printNextToken(tokenType, spaceBeforeToken);
				scribe.printTrailingComment();
				if (spaceAfterToken) {
					scribe.space();
				}
			}
		}
	}

	private static class TokenRange {
		private int offset;
		private int endOffset;

		TokenRange(int offset, int endOffset) {
			this.offset = offset;
			this.endOffset = endOffset;
		}

		int getOffset() {
			return offset;
		}

		int getEndOffset() {
			return endOffset;
		}

		int getLength() {
			return endOffset - offset;
		}
	}

	/**
	 * Formats a trailing semicolon.
	 * @see #formatList(List, ListOptions, boolean, boolean, Runnable)
	 */
	private class TrailingSemicolonFormatter extends TrailingTokenFormatter {
		TrailingSemicolonFormatter(IASTNode node) {
			super(Token.tSEMI, getLastNodeCharacterPosition(node),
					fHasClauseInitStatement ? preferences.insert_space_before_semicolon_in_for
							: preferences.insert_space_before_semicolon,
					false);
		}
	}

	/**
	 * Formats the part of a function declaration following the parameter list.
	 * @see #formatList(List, ListOptions, boolean, boolean, Runnable)
	 */
	private class FunctionDeclaratorTailFormatter implements Runnable {
		private final IASTFunctionDeclarator node;
		private final Runnable continuationFormatter;

		public FunctionDeclaratorTailFormatter(IASTFunctionDeclarator node, Runnable tailFormatter) {
			this.node = node;
			this.continuationFormatter = tailFormatter;
		}

		@Override
		public void run() {
			boolean needSpace = skipConstVolatileRestrict(true);
			// Skip mutable or constexpr keywords for a lambda expression
			needSpace = skipMutableConstexpr() || needSpace;
			int token = peekNextToken();
			// Lambda return value
			if (token == Token.tARROW) {
				scribe.printNextToken(token, preferences.insert_space_before_lambda_return);
				if (preferences.insert_space_after_lambda_return)
					scribe.space();
				if (node.getParent() instanceof ICPPASTLambdaExpression) {
					final IASTTypeId returnValue = ((ICPPASTFunctionDeclarator) node).getTrailingReturnType();
					returnValue.accept(CodeFormatterVisitor.this);
					scribe.printTrailingComment();
					scribe.space();
				}
				token = peekNextToken();
				needSpace = true;
			}
			// Ref-qualifier.
			if (token == Token.tAMPER || token == Token.tAND) {
				scribe.printNextToken(token, true);
				token = peekNextToken();
				needSpace = true;
			}
			if (token == Token.t_throw || token == Token.tIDENTIFIER) {
				if (node instanceof ICPPASTFunctionDeclarator) {
					final IASTTypeId[] exceptionSpecification = ((ICPPASTFunctionDeclarator) node)
							.getExceptionSpecification();
					if (exceptionSpecification != null && token == Token.t_throw)
						formatExceptionSpecification(exceptionSpecification);
				}
				if (peekNextToken() == Token.tIDENTIFIER) {
					Alignment alignment = scribe.createAlignment(Alignment.TRAILING_TEXT, Alignment.M_COMPACT_SPLIT, 1,
							scribe.scanner.getCurrentPosition());

					scribe.enterAlignment(alignment);
					boolean ok = false;
					do {
						try {
							scribe.alignFragment(alignment, 0);
							// Skip the rest of the declarator.
							scribe.printTrailingComment();
							scribe.space();
							if (continuationFormatter != null)
								continuationFormatter.run();
							skipNode(node);
							ok = true;
						} catch (AlignmentException e) {
							scribe.redoAlignment(e);
						}
					} while (!ok);
					scribe.exitAlignment(alignment, true);
				}
			} else {
				// Skip the rest (=0)
				if (needSpace && scribe.printComment()) {
					scribe.space();
				}
				skipNode(node);
				if (continuationFormatter != null)
					continuationFormatter.run();
			}
		}
	}

	/**
	 * Formats a trailing semicolon.
	 * @see #formatList(List, ListOptions, boolean, boolean, Runnable)
	 */
	private class ClosingParensesisTailFormatter implements Runnable {
		private final boolean spaceBeforeClosingParen;
		private final Runnable continuationFormatter;
		private final int parenPosition;
		private final int token;

		public ClosingParensesisTailFormatter(boolean spaceBeforeClosingParen, Runnable tailFormatter, int token) {
			this.spaceBeforeClosingParen = spaceBeforeClosingParen;
			this.continuationFormatter = tailFormatter;
			this.token = token;
			this.parenPosition = scribe.findToken(token);
		}

		public ClosingParensesisTailFormatter(boolean spaceBeforeClosingParen, Runnable tailFormatter) {
			this.spaceBeforeClosingParen = spaceBeforeClosingParen;
			this.continuationFormatter = tailFormatter;
			this.token = Token.tRPAREN;
			this.parenPosition = scribe.findToken(Token.tRPAREN);
		}

		@Override
		public void run() {
			int offset = scribe.scanner.getCurrentPosition();
			if (parenPosition >= 0 && offset <= parenPosition) {
				if (offset < parenPosition)
					scribe.restartAtOffset(parenPosition);
				scribe.undoSpace();
				scribe.printNextToken(token, spaceBeforeClosingParen);
			}
			if (continuationFormatter != null)
				continuationFormatter.run();
		}
	}

	{
		shouldVisitNames = true;
		shouldVisitDeclarations = true;
		shouldVisitInitializers = true;
		shouldVisitParameterDeclarations = true;
		shouldVisitDeclarators = true;
		shouldVisitDeclSpecifiers = true;
		shouldVisitDecltypeSpecifiers = true;
		shouldVisitExpressions = true;
		shouldVisitStatements = true;
		shouldVisitTypeIds = true;
		shouldVisitEnumerators = true;
		shouldVisitTranslationUnit = true;

		shouldVisitBaseSpecifiers = true;
		shouldVisitNamespaces = true;
		shouldVisitTemplateParameters = true;
		shouldVisitVirtSpecifiers = true;
	}

	private final Scanner localScanner;
	private List<InactivePosition> fInactivePreprocessorPositions;
	final DefaultCodeFormatterOptions preferences;
	private final Scribe scribe;

	private boolean fHasClauseInitStatement;
	private boolean fInsideMacroArguments;
	private boolean fExpectSemicolonAfterDeclaration = true;

	private MultiStatus fStatus;
	private IASTTranslationUnit ast;

	public CodeFormatterVisitor(DefaultCodeFormatterOptions preferences, int offset, int length) {
		localScanner = new Scanner() {
			@Override
			public Token nextToken() {
				Token t = super.nextToken();
				while (t != null && (t.isWhiteSpace() || t.isPreprocessor())) {
					t = super.nextToken();
				}
				return t;
			}
		};
		this.preferences = preferences;
		scribe = new Scribe(this, offset, length);
		fInactivePreprocessorPositions = Collections.emptyList();
	}

	/**
	 * @see org.eclipse.cdt.core.formatter.CodeFormatter#format(int, String, int, int, int, String)
	 */
	public TextEdit format(String string, IASTTranslationUnit unit) {
		// reset the scribe
		scribe.reset();

		final long startTime = DEBUG ? System.currentTimeMillis() : 0;

		final char[] compilationUnitSource = string.toCharArray();

		localScanner.setSource(compilationUnitSource);
		scribe.initializeScanner(compilationUnitSource);
		fInactivePreprocessorPositions = collectInactiveCodePositions(unit);
		List<InactivePosition> inactive = collectNoFormatCodePositions(unit);
		inactive.addAll(fInactivePreprocessorPositions);
		scribe.setSkipInactivePositions(inactive);

		fStatus = new MultiStatus(CCorePlugin.PLUGIN_ID, 0, "Formatting problem(s) in '" + unit.getFilePath() + "'", //$NON-NLS-1$//$NON-NLS-2$
				null);
		try {
			unit.accept(this);
		} catch (RuntimeException e) {
			reportFormattingProblem(e);
			if (DEBUG)
				return failedToFormat(e);
		}
		if (DEBUG) {
			System.out.println("Formatting time: " + (System.currentTimeMillis() - startTime)); //$NON-NLS-1$
		}
		return scribe.getRootEdit();
	}

	/**
	 * @return the status collected during formatting
	 */
	IStatus getStatus() {
		return fStatus;
	}

	private TextEdit failedToFormat(RuntimeException e) {
		if (DEBUG) {
			System.out.println("COULD NOT FORMAT: " + e.getMessage()); //$NON-NLS-1$
			System.out.println(scribe.scanner);
			System.out.println(scribe);
			System.out.flush();
			System.err.flush();
			e.printStackTrace();
			System.err.flush();
		}
		return null;
	}

	private void reportFormattingProblem(RuntimeException e) {
		String errorMessage = e.getMessage();
		if (errorMessage == null) {
			errorMessage = "Unknown error"; //$NON-NLS-1$
		}
		fStatus.add(createStatus(errorMessage, e));
	}

	private static IStatus createStatus(String msg, Throwable e) {
		return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, msg, e);
	}

	/*
	 * @see ASTVisitor#visit(IASTTranslationUnit)
	 */
	@Override
	public int visit(IASTTranslationUnit tu) {
		ast = tu;
		// Fake new line
		scribe.lastNumberOfNewLines = 1;
		scribe.startNewLine();
		final int indentLevel = scribe.indentationLevel;
		IASTDeclaration[] decls = tu.getDeclarations();
		formatDeclarations(decls, indentLevel);
		scribe.printEndOfTranslationUnit();
		return PROCESS_SKIP;
	}

	private void formatDeclarations(IASTDeclaration[] decls, final int indentLevel) {
		IASTPreprocessorMacroExpansion[] macroExpansions = ast.getMacroExpansions();
		int m = 0;
		for (int i = 0; i < decls.length; i++) {
			IASTDeclaration declaration = decls[i];
			if (!declaration.isPartOfTranslationUnitFile()) {
				continue;
			}
			try {
				int pos = getCurrentPosition();
				IASTFileLocation declarationLocation = declaration.getFileLocation();
				int declarationOffset = declarationLocation.getNodeOffset();
				for (; m < macroExpansions.length; m++) {
					IASTPreprocessorMacroExpansion macroExpansion = macroExpansions[m];
					IASTFileLocation macroLocation = macroExpansion.getFileLocation();
					int macroOffset = macroLocation.getNodeOffset();
					if (macroOffset > declarationOffset) {
						break;
					}
					int macroEndOffset = macroOffset + macroLocation.getNodeLength();
					if (isFunctionStyleMacroExpansion(macroExpansion) && macroOffset >= pos) {
						// Find the last declaration overlapping with the macro.
						for (int j = i + 1; j < decls.length; j++) {
							IASTDeclaration next = decls[j];
							if (!next.isPartOfTranslationUnitFile()) {
								continue;
							}
							IASTFileLocation nextLocation = next.getFileLocation();
							int nextOffset = nextLocation.getNodeOffset();
							if (macroEndOffset <= nextOffset) {
								break;
							}
							i = j;
							declaration = next;
							declarationLocation = nextLocation;
							declarationOffset = declarationLocation.getNodeOffset();
						}
						int declarationEndOffset = declarationOffset + declarationLocation.getNodeLength();
						if (macroEndOffset <= declarationOffset || macroEndOffset >= declarationEndOffset
								|| macroEndOffset == declarationEndOffset - 1
										&& isSemicolonAtPosition(macroEndOffset)) {
							// The function-style macro expansion either doesn't overlap with
							// the following declaration, or completely covers one or more
							// declarations, with a possible exception for the trailing semicolon
							// of the last one. In both cases formatting is driven by the text of
							// parameters of the macro, not by the expanded code.
							scribe.setTailFormatter(new TrailingTokenFormatter(Token.tSEMI, macroEndOffset,
									preferences.insert_space_before_semicolon, false));
							formatFunctionStyleMacroExpansion(macroExpansion);
						}
					}
				}

				declaration.accept(this);
				scribe.startNewLine();
			} catch (RuntimeException e) {
				// Report, but continue
				reportFormattingProblem(e);
				if (i < decls.length - 1) {
					exitAlignments();
					skipToNode(decls[i + 1]);
					while (scribe.indentationLevel < indentLevel) {
						scribe.indent();
					}
					while (scribe.indentationLevel > indentLevel) {
						scribe.unIndent();
					}
				}
			}
		}
	}

	private boolean isFunctionStyleMacroExpansion(IASTPreprocessorMacroExpansion macroExpansion) {
		IASTName name = macroExpansion.getMacroReference();
		IMacroBinding binding = (IMacroBinding) name.resolveBinding();
		return binding.isFunctionStyle() && binding.getParameterList().length >= 0;
	}

	private void formatFunctionStyleMacroExpansion(IASTPreprocessorMacroExpansion macroExpansion) {
		IASTName name = macroExpansion.getMacroReference();
		IASTFileLocation fileLocation = name.getFileLocation();
		if (fileLocation != null) {
			scribe.printRaw(fileLocation.getNodeOffset(), fileLocation.getNodeLength());
		}
		IMacroBinding binding = (IMacroBinding) name.resolveBinding();
		List<Object> arguments = getMacroArguments(binding.getParameterList().length);

		final ListOptions options = new ListOptions(preferences.alignment_for_arguments_in_method_invocation);
		options.fSeparatorToken = Token.tCOMMA;
		options.fSpaceAfterOpeningParen = preferences.insert_space_after_opening_paren_in_method_invocation;
		options.fSpaceBeforeClosingParen = preferences.insert_space_before_closing_paren_in_method_invocation;
		options.fSpaceBetweenEmptyParen = preferences.insert_space_between_empty_parens_in_method_invocation;
		options.fSpaceBeforeSeparator = preferences.insert_space_before_comma_in_method_invocation_arguments;
		options.fSpaceAfterSeparator = preferences.insert_space_after_comma_in_method_invocation_arguments;
		options.fTieBreakRule = Alignment.R_OUTERMOST;
		fInsideMacroArguments = true;
		try {
			formatList(arguments, options, true, false, scribe.takeTailFormatter());
		} finally {
			fInsideMacroArguments = false;
		}
	}

	/**
	 * Scans macro expansion arguments starting from the current position and returns a list of
	 * arguments where each argument is represented either by a {@link IASTNode} or, if not
	 * possible, by a {@link TokenRange}.
	 */
	private List<Object> getMacroArguments(int expectedNumberOfArguments) {
		List<TokenRange> argumentRanges = new ArrayList<>(expectedNumberOfArguments);
		TokenRange currentArgument = null;
		localScanner.resetTo(getCurrentPosition(), scribe.scannerEndPosition);
		localScanner.getNextToken(); // Skip the opening parenthesis.
		int parenLevel = 0;
		int token;
		while ((token = localScanner.getNextToken()) != -1) {
			int tokenOffset = localScanner.getCurrentTokenStartPosition();
			if (parenLevel == 0 && (token == Token.tCOMMA || token == Token.tRPAREN)) {
				if (currentArgument != null) {
					argumentRanges.add(currentArgument);
					currentArgument = null;
				} else {
					argumentRanges.add(new TokenRange(tokenOffset, tokenOffset));
				}
				if (token == Token.tRPAREN)
					break;
			} else {
				int tokenEndOffset = localScanner.getCurrentPosition();
				if (currentArgument == null) {
					currentArgument = new TokenRange(tokenOffset, tokenEndOffset);
				} else {
					currentArgument.endOffset = tokenEndOffset;
				}

				switch (token) {
				case Token.tLPAREN:
					++parenLevel;
					break;
				case Token.tRPAREN:
					if (parenLevel > 0)
						--parenLevel;
					break;
				}
			}
		}

		List<Object> arguments = new ArrayList<>(argumentRanges.size());
		IASTNodeSelector nodeSelector = ast.getNodeSelector(null);
		for (TokenRange argument : argumentRanges) {
			IASTNode node = nodeSelector.findNodeInExpansion(argument.getOffset(), argument.getLength());
			if (node != null) {
				arguments.add(node);
			} else {
				arguments.add(argument);
			}
		}
		return arguments;
	}

	@Override
	public int visit(IASTDeclaration node) {
		if (!enterNode(node)) {
			return PROCESS_SKIP;
		}
		int result = formatDeclaration(node);
		exitNode(node);
		return result;
	}

	private int formatDeclaration(IASTDeclaration node) {
		int indentLevel = scribe.indentationLevel;
		try {
			if (node instanceof IASTFunctionDefinition) {
				return visit((IASTFunctionDefinition) node);
			} else if (node instanceof ICPPASTStructuredBindingDeclaration) {
				return visit((ICPPASTStructuredBindingDeclaration) node);
			} else if (node instanceof IASTSimpleDeclaration) {
				return visit((IASTSimpleDeclaration) node);
			} else if (node instanceof IASTASMDeclaration) {
				return visit((IASTASMDeclaration) node);
			} else if (node instanceof ICPPASTVisibilityLabel) {
				return visit((ICPPASTVisibilityLabel) node);
			} else if (node instanceof ICPPASTNamespaceDefinition) {
				return visit((ICPPASTNamespaceDefinition) node);
			} else if (node instanceof ICPPASTNamespaceAlias) {
				return visit((ICPPASTNamespaceAlias) node);
			} else if (node instanceof ICPPASTUsingDeclaration) {
				return visit((ICPPASTUsingDeclaration) node);
			} else if (node instanceof ICPPASTUsingDirective) {
				return visit((ICPPASTUsingDirective) node);
			} else if (node instanceof ICPPASTLinkageSpecification) {
				return visit((ICPPASTLinkageSpecification) node);
			} else if (node instanceof ICPPASTTemplateDeclaration) {
				return visit((ICPPASTTemplateDeclaration) node);
			} else if (node instanceof ICPPASTTemplateSpecialization) {
				return visit((ICPPASTTemplateSpecialization) node);
			} else if (node instanceof ICPPASTExplicitTemplateInstantiation) {
				return visit((ICPPASTExplicitTemplateInstantiation) node);
			} else if (node instanceof IASTProblemDeclaration) {
				return visit((IASTProblemDeclaration) node);
			} else {
				formatRaw(node);
			}
		} catch (ASTProblemException e) {
			if (node instanceof IASTProblemDeclaration) {
				throw e;
			} else {
				skipNode(node);
				while (scribe.indentationLevel > indentLevel) {
					scribe.unIndent();
				}
			}
		}
		return PROCESS_SKIP;
	}

	/*
	 * @see ASTVisitor#visit(IASTName)
	 */
	@Override
	public int visit(IASTName node) {
		if (!enterNode(node)) {
			return PROCESS_SKIP;
		}
		if (node instanceof ICPPASTQualifiedName) {
			visit((ICPPASTQualifiedName) node);
		} else if (node instanceof ICPPASTTemplateId) {
			visit((ICPPASTTemplateId) node);
		} else {
			formatRaw(node);
		}
		exitNode(node);
		return PROCESS_SKIP;
	}

	/*
	 * @see ASTVisitor#visit(IASTInitializer)
	 */
	@Override
	public int visit(IASTInitializer node) {
		if (node instanceof ICPPASTConstructorInitializer) {
			return visit((ICPPASTConstructorInitializer) node);
		}

		if (peekNextToken() == Token.tASSIGN) {
			scribe.printNextToken(Token.tASSIGN, preferences.insert_space_before_assignment_operator);
			if (preferences.insert_space_after_assignment_operator) {
				scribe.space();
			}
		}

		if (!enterNode(node)) {
			return PROCESS_SKIP;
		}
		if (node instanceof IASTEqualsInitializer) {
			visit((IASTEqualsInitializer) node);
		} else if (node instanceof IASTInitializerList) {
			visit((IASTInitializerList) node);
		} else if (node instanceof ICASTDesignatedInitializer) {
			visit((ICASTDesignatedInitializer) node);
		} else if (node instanceof ICPPASTDesignatedInitializer) {
			visit((ICPPASTDesignatedInitializer) node);
		} else {
			formatRaw(node);
		}
		exitNode(node);
		return PROCESS_SKIP;
	}

	/*
	 * @see ASTVisitor#visit(IASTParameterDeclaration)
	 */
	@Override
	public int visit(IASTParameterDeclaration node) {
		if (!enterNode(node)) {
			return PROCESS_SKIP;
		}
		// decl-specifier
		final IASTDeclSpecifier declSpec = node.getDeclSpecifier();
		if (declSpec != null) {
			declSpec.accept(this);
		}
		// declarator
		final IASTDeclarator declarator = node.getDeclarator();
		if (declarator != null) {
			boolean needSpace = declarator.getPointerOperators().length > 0 && scribe.printComment();
			if (needSpace) {
				scribe.space();
			}
			declarator.accept(this);
		}
		exitNode(node);
		return PROCESS_SKIP;
	}

	/*
	 * @see ASTVisitor#visit(IASTDeclarator)
	 */
	@Override
	public int visit(IASTDeclarator node) {
		if (!enterNode(node)) {
			return PROCESS_SKIP;
		}

		// Common to all declarators
		final IASTPointerOperator[] pointerOperators = node.getPointerOperators();
		formatPointers(pointerOperators);
		if (node instanceof IASTStandardFunctionDeclarator) {
			if (preferences.insert_new_line_before_identifier_in_function_declaration) {
				scribe.startNewLine();
			} else {
				// Preserve newline if not explicitly requested
				if (scribe.printCommentPreservingNewLines()) {
					scribe.space();
				}
			}
		}
		IASTName name = node.getName();
		IASTDeclarator nestedDecl = node.getNestedDeclarator();
		if (name != null && name.getSimpleID().length != 0 || nestedDecl != null) {
			if (node.getPropertyInParent() != IASTDeclarator.NESTED_DECLARATOR && isFirstDeclarator(node)) {
				// Preserve non-space between pointer operator and name or nested declarator.
				if (pointerOperators.length == 0) {
					scribe.space();
				} else
					scribe.printComment();
			}
			if (name != null)
				name.accept(this);
		}
		if (nestedDecl != null) {
			scribe.printNextToken(Token.tLPAREN, false);
			nestedDecl.accept(this);
			scribe.printNextToken(Token.tRPAREN, false);
		}

		if (node instanceof ICPPASTFunctionDeclarator) {
			return visit((ICPPASTFunctionDeclarator) node);
		} else if (node instanceof IASTStandardFunctionDeclarator) {
			visit((IASTStandardFunctionDeclarator) node);
		} else if (node instanceof ICASTKnRFunctionDeclarator) {
			visit((ICASTKnRFunctionDeclarator) node);
		} else if (node instanceof IASTFieldDeclarator) {
			visit((IASTFieldDeclarator) node);
		} else if (node instanceof IASTArrayDeclarator) {
			visit((IASTArrayDeclarator) node);
		}

		IASTInitializer initializer = node.getInitializer();
		if (initializer != null) {
			initializer.accept(this);
		}
		exitNode(node);
		return PROCESS_SKIP;
	}

	/**
	 * Determine whether the given declarator is the first in a list of declarators (if any).
	 *
	 * @param node  the declarator node
	 * @return <code>true</code> if this node is the first in a list
	 */
	private boolean isFirstDeclarator(IASTDeclarator node) {
		IASTNode parent = node.getParent();
		if (parent instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) parent;
			return simpleDecl.getDeclarators()[0] == node;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(IASTDeclSpecifier)
	 */
	@Override
	public int visit(IASTDeclSpecifier node) {
		if (!enterNode(node)) {
			return PROCESS_SKIP;
		}
		if (node instanceof ICPPASTCompositeTypeSpecifier) {
			visit((ICPPASTCompositeTypeSpecifier) node);
		} else if (node instanceof ICASTCompositeTypeSpecifier) {
			visit((ICASTCompositeTypeSpecifier) node);
		} else if (node instanceof IASTElaboratedTypeSpecifier) {
			visit((IASTElaboratedTypeSpecifier) node);
		} else if (node instanceof IASTEnumerationSpecifier) {
			visit((IASTEnumerationSpecifier) node);
		} else if (node instanceof IASTSimpleDeclSpecifier) {
			visit((IASTSimpleDeclSpecifier) node);
		} else if (node instanceof IASTNamedTypeSpecifier) {
			visit((IASTNamedTypeSpecifier) node);
		} else {
			formatRaw(node);
		}
		exitNode(node);
		return PROCESS_SKIP;
	}

	/*
	 * @see ASTVisitor#visit(ICPPASTDecltypeSpecifier)
	 */
	@Override
	public int visit(ICPPASTDecltypeSpecifier node) {
		formatRaw(node);
		return PROCESS_SKIP;
	}

	/*
	 * @see ASTVisitor#visit(IASTExpression)
	 */
	@Override
	public int visit(IASTExpression node) {
		if (!enterNode(node)) {
			return PROCESS_SKIP;
		}
		if (node instanceof IASTArraySubscriptExpression) {
			visit((IASTArraySubscriptExpression) node);
		} else if (node instanceof IASTConditionalExpression) {
			visit((IASTConditionalExpression) node);
		} else if (node instanceof IASTFunctionCallExpression) {
			visit((IASTFunctionCallExpression) node);
		} else if (node instanceof IASTExpressionList) {
			visit((IASTExpressionList) node);
		} else if (node instanceof IASTTypeIdExpression) {
			visit((IASTTypeIdExpression) node);
		} else if (node instanceof IASTBinaryExpression) {
			visit((IASTBinaryExpression) node);
		} else if (node instanceof IASTLiteralExpression) {
			visit((IASTLiteralExpression) node);
		} else if (node instanceof IASTIdExpression) {
			visit((IASTIdExpression) node);
		} else if (node instanceof IASTCastExpression) {
			visit((IASTCastExpression) node);
		} else if (node instanceof IASTUnaryExpression) {
			visit((IASTUnaryExpression) node);
		} else if (node instanceof IASTFieldReference) {
			visit((IASTFieldReference) node);
		} else if (node instanceof IASTTypeIdInitializerExpression) {
			visit((IASTTypeIdInitializerExpression) node);
		} else if (node instanceof ICPPASTNewExpression) {
			visit((ICPPASTNewExpression) node);
		} else if (node instanceof ICPPASTDeleteExpression) {
			visit((ICPPASTDeleteExpression) node);
		} else if (node instanceof ICPPASTSimpleTypeConstructorExpression) {
			visit((ICPPASTSimpleTypeConstructorExpression) node);
		} else if (node instanceof IASTProblemExpression) {
			visit((IASTProblemExpression) node);
		} else if (node instanceof ICPPASTLambdaExpression) {
			visit((ICPPASTLambdaExpression) node);
		} else {
			formatRaw(node);
		}
		exitNode(node);
		return PROCESS_SKIP;
	}

	public int visit(ICPPASTLambdaExpression node) {
		final int line = scribe.line;

		final ListOptions options = createListOptionsForLambdaCapturesParameters(node);
		ICPPASTCapture[] captures = node.getCaptures();
		formatList(Arrays.asList(captures), options, true, captures.length > 0 && captures[0].isPackExpansion(), null,
				new Runnable() {
					@Override
					public void run() {
						if (options.captureDefault == CaptureDefault.BY_COPY) {
							scribe.printNextToken(Token.tASSIGN, options.fSpaceAfterOpeningParen);
						} else if (options.captureDefault == CaptureDefault.BY_REFERENCE) {
							scribe.printNextToken(Token.tAMPER, options.fSpaceAfterOpeningParen);
						}

						if (options.captureDefault != CaptureDefault.UNSPECIFIED && node.getCaptures().length > 0) {
							scribe.printNextToken(Token.tCOMMA, options.fSpaceBeforeSeparator);
							if (options.fSpaceAfterSeparator)
								scribe.space();
						}
					}
				});

		// declarator
		final ICPPASTFunctionDeclarator declarator = node.getDeclarator();
		skipNonWhitespaceToNode(declarator);
		boolean hasSpace = scribe.printComment();
		boolean hasPointerOps = declarator.getPointerOperators().length > 0;
		boolean needSpace = (hasPointerOps && hasSpace) || (!hasPointerOps && peekNextToken() == Token.tIDENTIFIER);
		if (needSpace) {
			scribe.space();
		}
		Runnable tailFormatter = null;
		IASTStatement bodyStmt = node.getBody();
		if (DefaultCodeFormatterConstants.END_OF_LINE.equals(preferences.brace_position_for_method_declaration)
				&& bodyStmt instanceof IASTCompoundStatement && !startsWithMacroExpansion(bodyStmt)) {
			tailFormatter = new TrailingTokenFormatter(Token.tLBRACE, nodeOffset(bodyStmt),
					preferences.insert_space_before_opening_brace_in_method_declaration, false);
			scribe.setTailFormatter(tailFormatter);
		}
		declarator.accept(this);

		IASTAttributeSpecifier[] attributes = declarator.getAttributeSpecifiers();
		if (attributes.length > 0) {
			formatAttributes(declarator, true, false);
		}

		if (tailFormatter != null) {
			scribe.runTailFormatter();
			scribe.setTailFormatter(null);
		}

		Alignment alignment = scribe.createAlignment(Alignment.LAMBDA_EXPRESSION,
				preferences.alignment_for_lambda_expression, Alignment.R_INNERMOST, 1, getCurrentPosition());
		scribe.enterAlignment(alignment);

		// Body
		if (bodyStmt instanceof IASTCompoundStatement) {
			if (enterNode(bodyStmt)) {
				if (getCurrentPosition() <= nodeOffset(bodyStmt)) {
					formatLeftCurlyBrace(line, preferences.brace_position_for_method_declaration);
				}
				formatBlock((IASTCompoundStatement) bodyStmt, preferences.brace_position_for_method_declaration,
						preferences.insert_space_before_opening_brace_in_method_declaration,
						preferences.indent_statements_compare_to_body);
				exitNode(bodyStmt);
			}
		} else if (bodyStmt != null) {
			bodyStmt.accept(this);
		}
		scribe.printTrailingComment();

		// go back to the previous alignment again:
		scribe.exitAlignment(alignment, true);
		return PROCESS_SKIP;
	}

	/*
	 * @see ASTVisitor#visit(IASTStatement)
	 */
	@Override
	public int visit(IASTStatement node) {
		if (getCurrentPosition() <= nodeOffset(node) && startsWithMacroExpansion(node)) {
			scribe.printCommentPreservingNewLines();
		}
		if (!enterNode(node)) {
			return PROCESS_SKIP;
		}
		int indentLevel = scribe.indentationLevel;
		try {
			if (node instanceof IASTCompoundStatement) {
				visit((IASTCompoundStatement) node);
			} else if (node instanceof IASTNullStatement) {
				visit((IASTNullStatement) node);
			} else if (node instanceof IASTDeclarationStatement) {
				visit((IASTDeclarationStatement) node);
			} else if (node instanceof IASTForStatement) {
				visit((IASTForStatement) node);
			} else if (node instanceof ICPPASTRangeBasedForStatement) {
				visit((ICPPASTRangeBasedForStatement) node);
			} else if (node instanceof IASTIfStatement) {
				visit((IASTIfStatement) node);
			} else if (node instanceof ICPPASTCatchHandler) {
				visit((ICPPASTCatchHandler) node);
			} else if (node instanceof ICPPASTTryBlockStatement) {
				visit((ICPPASTTryBlockStatement) node);
			} else if (node instanceof IASTWhileStatement) {
				visit((IASTWhileStatement) node);
			} else if (node instanceof IASTDoStatement) {
				visit((IASTDoStatement) node);
			} else if (node instanceof IASTSwitchStatement) {
				visit((IASTSwitchStatement) node);
			} else if (node instanceof IASTExpressionStatement) {
				visit((IASTExpressionStatement) node);
			} else if (node instanceof IASTContinueStatement) {
				visit((IASTContinueStatement) node);
			} else if (node instanceof IASTReturnStatement) {
				visit((IASTReturnStatement) node);
			} else if (node instanceof IASTBreakStatement) {
				visit((IASTBreakStatement) node);
			} else if (node instanceof IASTCaseStatement) {
				visit((IASTCaseStatement) node);
			} else if (node instanceof IASTDefaultStatement) {
				visit((IASTDefaultStatement) node);
			} else if (node instanceof IASTGotoStatement) {
				visit((IASTGotoStatement) node);
			} else if (node instanceof IASTLabelStatement) {
				visit((IASTLabelStatement) node);
			} else if (node instanceof IASTProblemStatement) {
				visit((IASTProblemStatement) node);
			} else {
				formatRaw(node);
			}
			exitNode(node);
		} catch (ASTProblemException e) {
			if (node instanceof IASTProblemStatement) {
				throw e;
			} else {
				skipNode(node);
				while (scribe.indentationLevel > indentLevel) {
					scribe.unIndent();
				}
			}
			exitNode(node);
		}
		return PROCESS_SKIP;
	}

	/*
	 * @see ASTVisitor#visit(IASTTypeId)
	 */
	@Override
	public int visit(IASTTypeId node) {
		if (!enterNode(node)) {
			return PROCESS_SKIP;
		}
		if (node instanceof IASTProblemHolder) {
			throw new ASTProblemException(((IASTProblemHolder) node).getProblem());
		}
		// decl-specifier
		final IASTDeclSpecifier declSpec = node.getDeclSpecifier();
		if (declSpec != null) {
			declSpec.accept(this);
		}
		// declarator
		final IASTDeclarator declarator = node.getAbstractDeclarator();
		if (declarator != null) {
			boolean needSpace = declarator.getPointerOperators().length > 0 && scribe.printComment();
			if (needSpace) {
				scribe.space();
			}
			declarator.accept(this);
		}
		exitNode(node);
		return PROCESS_SKIP;
	}

	/*
	 * @see ASTVisitor#visit(IASTEnumerator)
	 */
	@Override
	public int visit(IASTEnumerator enumerator) {
		if (!enterNode(enumerator)) {
			return PROCESS_SKIP;
		}
		// name
		enumerator.getName().accept(this);

		// optional value assignment
		final IASTExpression value = enumerator.getValue();
		if (value != null) {
			scribe.printNextToken(Token.tASSIGN, preferences.insert_space_before_assignment_operator);
			if (preferences.insert_space_after_assignment_operator) {
				scribe.space();
			}
			value.accept(this);
		}
		exitNode(enumerator);
		return PROCESS_SKIP;
	}

	/*
	 * @see ASTVisitor#visit(ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier)
	 */
	@Override
	public int visit(ICPPASTBaseSpecifier specifier) {
		if (!enterNode(specifier)) {
			return PROCESS_SKIP;
		}
		boolean needSpace = false;
		loop: while (true) {
			int token = peekNextToken();
			switch (token) {
			case Token.t_public:
			case Token.t_protected:
			case Token.t_private:
			case Token.t_virtual:
				scribe.printNextToken(token, needSpace);
				needSpace = true;
				break;
			default:
				break loop;
			}
		}
		if (needSpace) {
			scribe.space();
		}
		specifier.getNameSpecifier().accept(this);
		exitNode(specifier);
		return PROCESS_SKIP;
	}

	/*
	 * @see ASTVisitor#visit(ICPPASTNamespaceDefinition)
	 */
	@Override
	public int visit(ICPPASTNamespaceDefinition node) {
		if (!enterNode(node)) {
			return PROCESS_SKIP;
		}
		final int line = scribe.line;

		if (node.isInline()) {
			scribe.printNextToken(Token.t_inline, false);
			scribe.space();
		}

		if (peekNextToken() == Token.tCOLONCOLON) {
			// namespace <name>::<name>
			scribe.printNextToken(Token.tCOLONCOLON, false);
		} else {
			// namespace <name>
			scribe.printNextToken(Token.t_namespace, false);
			scribe.space();
			formatLeadingAttributes(node, ICPPASTAttributeList.TYPE_FILTER);
		}
		boolean isNamedNamespace = !CPPVisitor.isAnonymousNamespace(node);
		if (isNamedNamespace) {
			IASTName name = node.getName();
			name.accept(this);
		}
		if (peekNextToken() == Token.tCOLONCOLON)
			return PROCESS_CONTINUE;
		formatAttributes(node, isNamedNamespace, false, IGCCASTAttributeList.TYPE_FILTER);

		// member declarations
		IASTDeclaration[] memberDecls = node.getDeclarations();
		formatLeftCurlyBrace(line, preferences.brace_position_for_namespace_declaration);
		formatOpeningBrace(preferences.brace_position_for_namespace_declaration,
				preferences.insert_space_before_opening_brace_in_namespace_declaration);
		if (preferences.indent_body_declarations_compare_to_namespace_header) {
			scribe.indent();
		}
		scribe.startNewLine();
		formatDeclarations(memberDecls, scribe.indentationLevel);
		if (preferences.indent_body_declarations_compare_to_namespace_header) {
			scribe.unIndent();
		}
		formatClosingBrace(preferences.brace_position_for_namespace_declaration);
		exitNode(node);
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTLinkageSpecification node) {
		scribe.printComment();
		final int line = scribe.line;
		// extern "<linkage>"
		scribe.printNextToken(Token.t_extern, false);
		scribe.space();
		scribe.printNextToken(Token.tSTRING);

		// member declarations
		IASTDeclaration[] memberDecls = node.getDeclarations();
		if (memberDecls.length == 1 && peekNextToken() != Token.tLBRACE) {
			scribe.space();
			memberDecls[0].accept(this);
		} else {
			formatLeftCurlyBrace(line, preferences.brace_position_for_linkage_declaration);
			formatOpeningBrace(preferences.brace_position_for_linkage_declaration,
					preferences.insert_space_before_opening_brace_in_linkage_declaration);
			if (preferences.indent_body_declarations_compare_to_linkage) {
				scribe.indent();
			}
			scribe.startNewLine();
			for (IASTDeclaration declaration : memberDecls) {
				declaration.accept(this);
				scribe.startNewLine();
			}
			if (preferences.indent_body_declarations_compare_to_linkage) {
				scribe.unIndent();
			}
			formatClosingBrace(preferences.brace_position_for_linkage_declaration);
		}
		return PROCESS_SKIP;
	}

	/**
	 * <pre>
	 *   namespace-alias-definition:
	 * 	      namespace identifier = qualified-namespace-specifier ;
	 * </pre>
	 */
	private int visit(ICPPASTNamespaceAlias node) {
		scribe.printNextToken(Token.t_namespace);
		scribe.space();
		node.getAlias().accept(this);
		scribe.printNextToken(Token.tASSIGN, preferences.insert_space_before_assignment_operator);
		if (preferences.insert_space_after_assignment_operator) {
			scribe.space();
		}
		node.getMappingName().accept(this);
		scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon);
		return PROCESS_SKIP;
	}

	/**
	 * <pre>
	 *   using-declaration:
	 * 	      using typename<sub>opt</sub> ::<sub>opt</sub> nested-name-specifier unqualified-id ;
	 * 	      using ::  unqualified-id ;
	 * </pre>
	 */
	private int visit(ICPPASTUsingDeclaration node) {
		int token = peekNextToken();
		if (token == Token.t_using)
			scribe.printNextToken(token);
		token = peekNextToken();
		if (token == Token.t_typename) {
			scribe.printNextToken(token, true);
		}
		scribe.space();
		node.getName().accept(this);
		token = peekNextToken();
		if (token == Token.tSEMI) {
			scribe.printNextToken(token, preferences.insert_space_before_semicolon);
		}
		return PROCESS_SKIP;
	}

	/**
	 * <pre>
	 * 	 using-directive:
	 * 	      using  namespace ::<sub>opt</sub> nested-name-specifier<sub>opt</sub> namespace-name ;
	 * </pre>
	 */
	private int visit(ICPPASTUsingDirective node) {
		scribe.printNextToken(Token.t_using);
		scribe.printNextToken(Token.t_namespace, true);
		scribe.space();
		node.getQualifiedName().accept(this);
		scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon);
		return PROCESS_SKIP;
	}

	/*
	 * @see ASTVisitor#visit(ICPPASTTemplateParameter)
	 */
	@Override
	public int visit(ICPPASTTemplateParameter node) {
		if (!enterNode(node)) {
			return PROCESS_SKIP;
		}
		try {
			if (node instanceof ICPPASTSimpleTypeTemplateParameter) {
				visit((ICPPASTSimpleTypeTemplateParameter) node);
			} else if (node instanceof ICPPASTTemplatedTypeTemplateParameter) {
				visit((ICPPASTTemplatedTypeTemplateParameter) node);
			} else {
				visit((IASTParameterDeclaration) node);
			}
			exitNode(node);
		} catch (ASTProblemException e) {
			skipNode(node);
			exitNode(node);
		}
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTSimpleTypeTemplateParameter node) {
		switch (node.getParameterType()) {
		case ICPPASTSimpleTypeTemplateParameter.st_class:
			scribe.printNextToken(Token.t_class);
			scribe.space();
			break;
		case ICPPASTSimpleTypeTemplateParameter.st_typename:
			scribe.printNextToken(Token.t_typename);
			scribe.space();
			break;
		default:
			assert false : "Unknown template paramter type"; //$NON-NLS-1$
			formatRaw(node);
			return PROCESS_SKIP;
		}
		node.getName().accept(this);
		IASTTypeId defaultType = node.getDefaultType();
		if (defaultType != null) {
			scribe.printNextToken(Token.tASSIGN, preferences.insert_space_before_assignment_operator);
			if (preferences.insert_space_after_assignment_operator) {
				scribe.space();
			}
			defaultType.accept(this);
		}
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTTemplatedTypeTemplateParameter node) {
		scribe.printNextToken(Token.t_template, scribe.printComment());
		scribe.printNextToken(Token.tLT, preferences.insert_space_before_opening_angle_bracket_in_template_parameters);
		if (preferences.insert_space_after_opening_angle_bracket_in_template_parameters) {
			scribe.space();
		}
		final ICPPASTTemplateParameter[] templateParameters = node.getTemplateParameters();
		if (templateParameters.length > 0) {
			final ListOptions options = new ListOptions(Alignment.M_COMPACT_SPLIT);
			options.fSpaceAfterSeparator = preferences.insert_space_after_comma_in_template_parameters;
			options.fSpaceBeforeSeparator = preferences.insert_space_before_comma_in_template_parameters;
			options.fTieBreakRule = Alignment.R_OUTERMOST;
			formatList(Arrays.asList(templateParameters), options, false, false, null);
		}
		scribe.printNextToken(new int[] { Token.tGT, Token.tSHIFTR },
				preferences.insert_space_before_closing_angle_bracket_in_template_parameters);
		if (preferences.insert_space_after_closing_angle_bracket_in_template_parameters) {
			scribe.space();
		}
		IASTName name = node.getName();
		if (name != null) {
			name.accept(this);
		}
		IASTExpression defaultValue = node.getDefaultValue();
		if (defaultValue != null) {
			scribe.printNextToken(Token.tASSIGN, preferences.insert_space_before_assignment_operator);
			if (preferences.insert_space_after_assignment_operator) {
				scribe.space();
			}
			defaultValue.accept(this);
		}
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTConstructorInitializer node) {
		if (!enterNode(node)) {
			return PROCESS_SKIP;
		}
		// Format like a function call
		formatFunctionCallArguments(node.getArguments());
		exitNode(node);
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTConstructorChainInitializer node) {
		final IASTName member = node.getMemberInitializerId();
		final IASTInitializer init = node.getInitializer();
		if (member != null && init != null) {
			member.accept(this);
			init.accept(this);
		} else {
			formatRaw(node);
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTFunctionDefinition node) {
		scribe.printComment();
		final int line = scribe.line;

		// decl-specifier
		final IASTDeclSpecifier declSpec = node.getDeclSpecifier();
		declSpec.accept(this);

		// declarator
		final IASTFunctionDeclarator declarator = node.getDeclarator();
		skipNonWhitespaceToNode(declarator);
		boolean hasSpace = scribe.printComment();
		boolean hasPointerOps = declarator.getPointerOperators().length > 0;
		boolean needSpace = (hasPointerOps && hasSpace) || (!hasPointerOps && peekNextToken() == Token.tIDENTIFIER);
		if (needSpace) {
			scribe.space();
		}
		Runnable tailFormatter = null;
		IASTStatement bodyStmt = node.getBody();
		if (DefaultCodeFormatterConstants.END_OF_LINE.equals(preferences.brace_position_for_method_declaration)
				&& !hasMemberInitializers(node) && !(node instanceof ICPPASTFunctionWithTryBlock)
				&& bodyStmt instanceof IASTCompoundStatement && !startsWithMacroExpansion(bodyStmt)) {
			tailFormatter = new TrailingTokenFormatter(Token.tLBRACE, nodeOffset(bodyStmt),
					preferences.insert_space_before_opening_brace_in_method_declaration, false);
			scribe.setTailFormatter(tailFormatter);
		}
		declarator.accept(this);

		if (node instanceof ICPPASTFunctionWithTryBlock) {
			scribe.startNewLine();
			scribe.printNextToken(Token.t_try, false);
			scribe.printTrailingComment();
		}

		if (node instanceof ICPPASTFunctionDefinition) {
			ICPPASTFunctionDefinition cppFunctionDefinition = (ICPPASTFunctionDefinition) node;
			final ICPPASTConstructorChainInitializer[] constructorChain = cppFunctionDefinition.getMemberInitializers();
			if (constructorChain != null && constructorChain.length > 0) {
				if (preferences.insert_new_line_before_colon_in_constructor_initializer_list) {
					scribe.printTrailingComment();
					scribe.startNewLine();
					scribe.indentForContinuation();
				}
				scribe.printNextToken(Token.tCOLON,
						!preferences.insert_new_line_before_colon_in_constructor_initializer_list);
				if (!preferences.insert_new_line_after_colon_in_constructor_initializer_list) {
					scribe.space();
				}
				if (preferences.insert_new_line_after_colon_in_constructor_initializer_list) {
					scribe.printTrailingComment();
					scribe.startNewLine();
					if (!preferences.insert_new_line_before_colon_in_constructor_initializer_list)
						scribe.indentForContinuation();
				}
				final ListOptions options = new ListOptions(preferences.alignment_for_constructor_initializer_list);
				options.fTieBreakRule = Alignment.R_OUTERMOST;
				formatList(Arrays.asList(constructorChain), options, false, false, null);
				if (preferences.insert_new_line_after_colon_in_constructor_initializer_list
						|| preferences.insert_new_line_before_colon_in_constructor_initializer_list) {
					scribe.unIndentForContinuation();
				}
			}

			if (cppFunctionDefinition.isDefaulted() || cppFunctionDefinition.isDeleted()) {
				int token = peekNextToken();
				if (token == Token.tASSIGN) {
					if (preferences.insert_space_before_assignment_operator)
						scribe.space();
					scribe.printNextToken(token);
					if (preferences.insert_space_after_assignment_operator)
						scribe.space();
				}
				token = peekNextToken();
				if (token == Token.t_default || token == Token.t_delete) {
					scribe.printNextToken(token);
				}
				if (bodyStmt == null) {
					tailFormatter = new TrailingSemicolonFormatter(node);
					scribe.setTailFormatter(tailFormatter);
				}
			}
		}

		if (tailFormatter != null) {
			scribe.runTailFormatter();
			scribe.setTailFormatter(null);
		}

		// Body
		if (bodyStmt instanceof IASTCompoundStatement) {
			if (enterNode(bodyStmt)) {
				if (getCurrentPosition() <= nodeOffset(bodyStmt)) {
					formatLeftCurlyBrace(line, preferences.brace_position_for_method_declaration);
				}
				formatBlock((IASTCompoundStatement) bodyStmt, preferences.brace_position_for_method_declaration,
						preferences.insert_space_before_opening_brace_in_method_declaration,
						preferences.indent_statements_compare_to_body);
				exitNode(bodyStmt);
			}
		} else if (bodyStmt != null) {
			bodyStmt.accept(this);
		}
		scribe.printTrailingComment();
		scribe.startNewLine();

		if (node instanceof ICPPASTFunctionWithTryBlock) {
			ICPPASTCatchHandler[] catchHandlers = ((ICPPASTFunctionWithTryBlock) node).getCatchHandlers();
			for (ICPPASTCatchHandler catchHandler : catchHandlers) {
				catchHandler.accept(this);
				scribe.printTrailingComment();
				scribe.startNewLine();
			}
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTASMDeclaration node) {
		// TLETODO implement formatting
		formatRaw(node);
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTFunctionDeclarator node) {
		final List<ICPPASTParameterDeclaration> parameters = Arrays.asList(node.getParameters());
		final ListOptions options = createListOptionsForFunctionDeclarationParameters();
		Runnable tailFormatter = scribe.takeTailFormatter();
		formatList(parameters, options, true, node.takesVarArgs(),
				new FunctionDeclaratorTailFormatter(node, tailFormatter));

		return PROCESS_SKIP;
	}

	private void formatExceptionSpecification(final IASTTypeId[] exceptionSpecification) {
		if (exceptionSpecification.length > 0) {
			Alignment alignment = scribe.createAlignment(Alignment.EXCEPTION_SPECIFICATION,
					preferences.alignment_for_throws_clause_in_method_declaration, exceptionSpecification.length,
					getCurrentPosition());

			scribe.enterAlignment(alignment);
			boolean ok = false;
			do {
				try {
					scribe.alignFragment(alignment, 0);
					scribe.printNextToken(Token.t_throw, true);
					scribe.printNextToken(Token.tLPAREN,
							preferences.insert_space_before_opening_paren_in_exception_specification);
					if (preferences.insert_space_after_opening_paren_in_exception_specification) {
						scribe.space();
					}
					exceptionSpecification[0].accept(this);
					for (int i = 1; i < exceptionSpecification.length; i++) {
						scribe.printNextToken(Token.tCOMMA,
								preferences.insert_space_before_comma_in_method_declaration_throws);
						scribe.printTrailingComment();
						if (preferences.insert_space_after_comma_in_method_declaration_throws) {
							scribe.space();
						}
						scribe.alignFragment(alignment, i);
						exceptionSpecification[i].accept(this);
					}
					if (peekNextToken() == Token.tRPAREN) {
						scribe.printNextToken(Token.tRPAREN,
								preferences.insert_space_before_closing_paren_in_exception_specification);
					}
					ok = true;
				} catch (AlignmentException e) {
					scribe.redoAlignment(e);
				}
			} while (!ok);
			scribe.exitAlignment(alignment, true);
		} else {
			scribe.printNextToken(Token.t_throw, true);
			scribe.printNextToken(Token.tLPAREN,
					preferences.insert_space_before_opening_paren_in_exception_specification);
			scribe.printNextToken(Token.tRPAREN,
					preferences.insert_space_between_empty_parens_in_exception_specification);
		}
	}

	private boolean skipConstVolatileRestrict(boolean spaceBefore) {
		return skipTokenWhile(token -> token == Token.t_const || token == Token.t_volatile || token == Token.t_restrict,
				spaceBefore);
	}

	private boolean skipMutableConstexpr() {
		return skipTokenWhile(token -> token == Token.t_mutable || token == Token.t_constexpr, true);
	}

	private boolean skipTokenWhile(Predicate<Integer> pred, boolean spaceBefore) {
		boolean skipped = false;
		int token = peekNextToken();
		while (pred.test(token)) {
			scribe.printNextToken(token, spaceBefore);
			token = peekNextToken();
			if (!spaceBefore && pred.test(token)) {
				scribe.space();
			}
			skipped = true;
		}
		return skipped;
	}

	private int visit(IASTStandardFunctionDeclarator node) {
		final List<IASTParameterDeclaration> parameters = Arrays.asList(node.getParameters());
		final ListOptions options = createListOptionsForFunctionDeclarationParameters();
		formatList(parameters, options, true, node.takesVarArgs(), new TrailingSemicolonFormatter(node));
		return PROCESS_SKIP;
	}

	private ListOptions createListOptionsForFunctionDeclarationParameters() {
		final ListOptions options = new ListOptions(preferences.alignment_for_parameters_in_method_declaration);
		options.fSpaceBeforeOpeningParen = preferences.insert_space_before_opening_paren_in_method_declaration;
		options.fSpaceAfterOpeningParen = preferences.insert_space_after_opening_paren_in_method_declaration;
		options.fSpaceBeforeClosingParen = preferences.insert_space_before_closing_paren_in_method_declaration;
		options.fSpaceBetweenEmptyParen = preferences.insert_space_between_empty_parens_in_method_declaration;
		options.fSpaceBeforeSeparator = preferences.insert_space_before_comma_in_method_declaration_parameters;
		options.fSpaceAfterSeparator = preferences.insert_space_after_comma_in_method_declaration_parameters;
		options.fTieBreakRule = Alignment.R_OUTERMOST;
		return options;
	}

	private ListOptions createListOptionsForLambdaCapturesParameters(ICPPASTLambdaExpression expr) {
		final ListOptions options = new ListOptions(preferences.alignment_for_parameters_in_method_declaration);
		options.fSpaceBeforeOpeningParen = preferences.insert_space_before_opening_paren_in_method_declaration;
		options.fSpaceAfterOpeningParen = preferences.insert_space_after_opening_paren_in_method_declaration;
		options.fSpaceBeforeClosingParen = preferences.insert_space_before_closing_paren_in_method_declaration;
		options.fSpaceBetweenEmptyParen = preferences.insert_space_between_empty_parens_in_method_declaration;
		options.fSpaceBeforeSeparator = preferences.insert_space_before_comma_in_method_declaration_parameters;
		options.fSpaceAfterSeparator = preferences.insert_space_after_comma_in_method_declaration_parameters;
		options.fTieBreakRule = Alignment.R_OUTERMOST;
		options.rightToken = Token.tRBRACKET;
		options.leftToken = Token.tLBRACKET;
		options.captureDefault = expr.getCaptureDefault();
		return options;
	}

	/**
	 * Returns the position of the last character of a node, or -1 if that character is part of
	 * a macro expansion.
	 *
	 * @param node an AST node
	 * @return the position of the last character of a node, or -1 if that character is part of
	 * 		a macro expansion.
	 */
	private static int getLastNodeCharacterPosition(IASTNode node) {
		IASTNodeLocation[] locations = node.getNodeLocations();
		if (locations.length > 0) {
			IASTNodeLocation lastLocation = locations[locations.length - 1];
			if (!(lastLocation instanceof IASTMacroExpansionLocation)) {
				IASTFileLocation fileLocation = lastLocation.asFileLocation();
				return fileLocation.getNodeOffset() + fileLocation.getNodeLength() - 1;
			}
		}
		return -1;
	}

	private void formatLeadingAttributes(IASTAttributeOwner owner) {
		formatAttributes(owner, false, true);
	}

	/**
	 * Formats the attributes leading a node.
	 * Same as {@code formatAttributes(owner, false, true);}
	 * @param owner Node containing attributes
	 * @param filter Filter predicate for specifying which attributes to print
	 */
	private void formatLeadingAttributes(IASTAttributeOwner owner,
			InstanceOfPredicate<IASTAttributeSpecifier> predicate) {
		formatAttributes(owner, false, true, predicate);
	}

	private void formatAttributes(IASTAttributeOwner owner, boolean printLeadingSpace, boolean printTrailingSpace) {
		formatAttributes(owner, printLeadingSpace, printTrailingSpace, unsused -> true);
	}

	/**
	 * Formats the attributes of a given attribute owner.
	 *
	 * @param owner Node containing attributes
	 * @param printLeadingSpace Print a space before the first attribute
	 * @param printTrailingSpace Print a space after the last attribute
	 * @param filter Filter predicate for specifying which attributes to print
	 */
	private void formatAttributes(IASTAttributeOwner owner, boolean printLeadingSpace, boolean printTrailingSpace,
			IUnaryPredicate<IASTAttributeSpecifier> filter) {
		if (owner == null) {
			return;
		}
		IASTAttributeSpecifier[] attributeSpecifiers = owner.getAttributeSpecifiers();
		if (attributeSpecifiers.length > 0) {
			if (printLeadingSpace) {
				scribe.space();
			}
			for (IASTAttributeSpecifier attributeSpecifier : attributeSpecifiers) {
				if (filter.apply(attributeSpecifier)) {
					formatRaw(attributeSpecifier);
				}
			}
			if (printTrailingSpace) {
				scribe.space();
			}
		}
	}

	/**
	 * Align pointers according to user formatter rule. Pointers (or references) can be
	 * left, center or right alignment. Pointers with implicit name will be always left
	 * aligned unless they have nested declarators.
	 * @param pointers The list of all pointers
	 * @param pointer The pointer to be formatted
	 * @param token The token to be used: and, amper, star.
	 */
	private boolean alignPointer(IASTPointerOperator[] pointers, IASTPointerOperator pointer, int token) {
		boolean firstPtr = pointer == pointers[0];
		boolean lastPtr = pointers.length == 1 || pointer == pointers[pointers.length - 1];
		TrailingTokenFormatter tailFormatter = null;
		IASTNode parent = pointer.getParent();
		boolean needSpace = false;
		if (parent instanceof IASTFunctionDeclarator) {
			tailFormatter = new TrailingTokenFormatter(token, pointer.getParent(), false, true);
			tailFormatter.run();
		} else {
			if (parent instanceof IASTDeclarator) {
				char[] simpleId = ((IASTDeclarator) parent).getName().getSimpleID();
				IASTDeclarator nested = ((IASTDeclarator) parent).getNestedDeclarator();
				if ((simpleId == null || simpleId.length == 0) && nested == null) {
					needSpace = true;
					tailFormatter = new TrailingTokenFormatter(token, pointer.getParent(), false, false);
					tailFormatter.run();
					return needSpace;
				}
			}
			if (parent != null && parent.getParent() instanceof IASTParameterDeclaration) {
				needSpace = this.preferences.insert_space_after_pointer_in_method_declaration && lastPtr;
				tailFormatter = new TrailingTokenFormatter(token, pointer.getParent(),
						this.preferences.insert_space_before_pointer_in_method_declaration && firstPtr,
						this.preferences.insert_space_after_pointer_in_method_declaration && lastPtr);
				tailFormatter.run();
			} else if (parent != null && parent.getParent() instanceof IASTSimpleDeclaration) {
				needSpace = this.preferences.insert_space_after_pointer_in_declarator_list && lastPtr;
				IASTSimpleDeclaration simple = (IASTSimpleDeclaration) parent.getParent();
				IASTDeclarator[] declarators = simple.getDeclarators();
				boolean first = declarators.length == 0 || declarators[0].getPointerOperators() == pointers;
				tailFormatter = new TrailingTokenFormatter(token, pointer.getParent(),
						first ? this.preferences.insert_space_before_pointer_in_declarator_list && firstPtr
								: (this.preferences.insert_space_before_pointer_in_declarator_list
										|| this.preferences.insert_space_after_comma_in_declarator_list) && firstPtr,
						this.preferences.insert_space_after_pointer_in_declarator_list && lastPtr);
				tailFormatter.run();
			} else
				scribe.printNextToken(token, false);
		}
		return needSpace;
	}

	/**
	 * Format pointers operators
	 * @param pointers The list of pointers
	 */
	private void formatPointers(IASTPointerOperator[] pointers) {
		for (IASTPointerOperator pointer : pointers) {
			if (scribe.printComment()) {
				scribe.space();
			}
			if (scribe.printModifiers()) {
				scribe.space();
			}
			if (pointer instanceof ICPPASTReferenceOperator) {
				if (((ICPPASTReferenceOperator) pointer).isRValueReference()) {
					alignPointer(pointers, pointer, Token.tAND);
				} else {
					alignPointer(pointers, pointer, Token.tAMPER);
				}
			} else if (pointer instanceof ICPPASTPointerToMember) {
				final ICPPASTPointerToMember ptrToMember = (ICPPASTPointerToMember) pointer;
				final IASTName name = ptrToMember.getName();
				if (name != null) {
					name.accept(this);
				}
				scribe.printNextToken(Token.tSTAR, false);
				if (skipConstVolatileRestrict(false)) {
					scribe.space();
				}
			} else {
				boolean needSpace = alignPointer(pointers, pointer, Token.tSTAR);
				if (skipConstVolatileRestrict(needSpace)) {
					scribe.space();
				}
			}
		}
	}

	private int visit(ICASTKnRFunctionDeclarator node) {
		final List<IASTName> parameters = Arrays.asList(node.getParameterNames());
		ListOptions options = createListOptionsForFunctionDeclarationParameters();
		formatList(parameters, options, true, false, null);

		IASTDeclaration[] parameterDecls = node.getParameterDeclarations();
		scribe.startNewLine();
		scribe.indent();
		try {
			for (IASTDeclaration declaration : parameterDecls) {
				declaration.accept(this);
			}
		} finally {
			scribe.unIndent();
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTFieldDeclarator node) {
		IASTExpression bitFieldSizeExpr = node.getBitFieldSize();
		if (bitFieldSizeExpr != null) {
			scribe.printNextToken(Token.tCOLON, true);
			bitFieldSizeExpr.accept(this);
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTArrayDeclarator node) {
		IASTArrayModifier[] arrayModifiers = node.getArrayModifiers();
		if (arrayModifiers != null) {
			for (IASTArrayModifier arrayModifier : arrayModifiers) {
				scribe.printNextToken(Token.tLBRACKET, preferences.insert_space_before_opening_bracket);
				boolean emptyBrackets = arrayModifier.getConstantExpression() == null
						&& !(arrayModifier instanceof ICASTArrayModifier);
				if (!emptyBrackets) {
					if (preferences.insert_space_after_opening_bracket) {
						scribe.space();
					}
				}
				if (arrayModifier instanceof ICASTArrayModifier) {
					final ICASTArrayModifier cArrayModifier = (ICASTArrayModifier) arrayModifier;
					if (scribe.printModifiers()) {
						scribe.space();
					}
					if (cArrayModifier.isVariableSized()) {
						scribe.printNextToken(Token.tSTAR, scribe.printComment());
					}
					if (scribe.printComment()) {
						scribe.space();
					}
				}
				Runnable tailFormatter = scribe.takeTailFormatter();
				try {
					arrayModifier.accept(this);
				} catch (ASTProblemException e) {
					scribe.skipToToken(Token.tRBRACKET);
				} finally {
					scribe.setTailFormatter(tailFormatter);
				}
				boolean insertSpace = emptyBrackets ? preferences.insert_space_between_empty_brackets
						: preferences.insert_space_before_closing_bracket;
				scribe.printNextToken(Token.tRBRACKET, insertSpace);
			}
		}
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTStructuredBindingDeclaration node) {
		formatLeadingAttributes(node);
		IASTDeclSpecifier declSpec = node.getDeclSpecifier();
		declSpec.accept(this);
		scribe.printNextToken(Token.tLBRACKET, preferences.insert_space_before_opening_structured_binding_name_list);
		List<IASTName> names = Arrays.asList(node.getNames());
		final ListOptions options = new ListOptions(preferences.alignment_for_declarator_list);
		options.fSpaceAfterSeparator = preferences.insert_space_after_comma_in_structured_binding_name_list;
		options.fSpaceBeforeSeparator = preferences.insert_space_before_comma_in_structured_binding_name_list;
		formatList(names, options, false, false, null);
		scribe.printNextToken(Token.tRBRACKET, preferences.insert_space_before_closing_structured_binding_name_list);
		node.getInitializer().ifPresent(this::visit);
		if (fExpectSemicolonAfterDeclaration) {
			scribe.printNextToken(Token.tSEMI);
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTSimpleDeclaration node) {
		formatLeadingAttributes(node);
		IASTDeclSpecifier declSpec = node.getDeclSpecifier();
		declSpec.accept(this);
		final List<IASTDeclarator> declarators = Arrays.asList(node.getDeclarators());
		if (!declarators.isEmpty()) {
			if (declarators.size() == 1 && declarators.get(0) instanceof IASTFunctionDeclarator) {
				if (scribe.printCommentPreservingNewLines()) {
					scribe.space();
				}
			} else {
				if (scribe.printComment()) {
					scribe.space();
				}
			}
			Runnable tailFormatter = fExpectSemicolonAfterDeclaration ? new TrailingSemicolonFormatter(node) : null;
			if (declarators.size() == 1) {
				if (tailFormatter != null) {
					scribe.setTailFormatter(tailFormatter);
					try {
						visit(declarators.get(0));
						scribe.runTailFormatter();
					} finally {
						scribe.setTailFormatter(null);
					}
				} else {
					visit(declarators.get(0));
				}
			} else {
				final ListOptions options = new ListOptions(preferences.alignment_for_declarator_list);
				options.fSpaceAfterSeparator = preferences.insert_space_after_comma_in_declarator_list;
				options.fSpaceBeforeSeparator = preferences.insert_space_before_comma_in_declarator_list;
				formatList(declarators, options, false, false, tailFormatter);
			}
		}
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTTemplateDeclaration node) {
		if (node.isExported()) {
			scribe.printNextToken(Token.t_export);
			scribe.space();
		}
		scribe.printNextToken(Token.t_template);
		scribe.printNextToken(Token.tLT, preferences.insert_space_before_opening_angle_bracket_in_template_parameters);
		if (preferences.insert_space_after_opening_angle_bracket_in_template_parameters) {
			scribe.space();
		}

		// Template parameters
		final ICPPASTTemplateParameter[] templateParameters = node.getTemplateParameters();
		if (templateParameters.length > 0) {
			final ListOptions options = new ListOptions(Alignment.M_COMPACT_SPLIT);
			options.fSpaceAfterSeparator = preferences.insert_space_after_comma_in_template_parameters;
			options.fSpaceBeforeSeparator = preferences.insert_space_before_comma_in_template_parameters;
			formatList(Arrays.asList(templateParameters), options, false, false, null);
		}
		int nextToken = peekNextToken();
		if (nextToken == Token.tGT || nextToken == Token.tSHIFTR) {
			scribe.printNextToken(new int[] { Token.tGT, Token.tSHIFTR },
					preferences.insert_space_before_closing_angle_bracket_in_template_parameters);
			if (preferences.insert_space_after_closing_angle_bracket_in_template_parameters) {
				scribe.space();
			}
		}

		// Declaration
		final IASTDeclaration declaration = node.getDeclaration();
		if (preferences.insert_new_line_after_template_declaration) {
			scribe.startNewLine();
			if (preferences.indent_declaration_compare_to_template_header) {
				scribe.indent();
			}
		} else {
			// Preserve newline if not explicitly requested
			scribe.printCommentPreservingNewLines();
		}

		declaration.accept(this);

		if (preferences.insert_new_line_after_template_declaration) {
			if (preferences.indent_declaration_compare_to_template_header) {
				scribe.unIndent();
			}
		}

		return PROCESS_SKIP;
	}

	/**
	 * <pre>
	 *    explicit-specialization:
	 *            template < > declaration
	 * </pre>
	 */
	private int visit(ICPPASTTemplateSpecialization node) {
		scribe.printNextToken(Token.t_template);
		scribe.printNextToken(Token.tLT, preferences.insert_space_before_opening_angle_bracket_in_template_parameters);
		scribe.printNextToken(Token.tGT, scribe.printComment());
		if (preferences.insert_space_after_closing_angle_bracket_in_template_parameters) {
			scribe.space();
		}
		node.getDeclaration().accept(this);
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTExplicitTemplateInstantiation node) {
		node.getDeclaration().accept(this);
		return PROCESS_SKIP;
	}

	private int visit(IASTSimpleDeclSpecifier node) {
		formatRaw(node);
		return PROCESS_SKIP;
	}

	private int visit(IASTNamedTypeSpecifier node) {
		if (scribe.printModifiers()) {
			scribe.space();
		}
		if (node instanceof ICPPASTNamedTypeSpecifier) {
			if (((ICPPASTNamedTypeSpecifier) node).isTypename()) {
				scribe.printNextToken(Token.t_typename);
				scribe.space();
			}
		}
		node.getName().accept(this);
		return PROCESS_SKIP;
	}

	private int visit(ICASTCompositeTypeSpecifier node) {
		boolean formatAttributes = false;
		scribe.printComment();
		final int line = scribe.line;

		// storage class and other modifiers
		if (scribe.printModifiers()) {
			scribe.space();
		}

		// Consider macro expansion
		if (withinMacroExpansion(node, getCurrentPosition())) {
			scribe.printNextToken(peekNextToken());
			continueNode(node);
			if (scribe.printComment())
				scribe.space();
		}

		switch (node.getKey()) {
		case IASTCompositeTypeSpecifier.k_struct:
			scribe.printNextToken(Token.t_struct, true);
			break;
		case IASTCompositeTypeSpecifier.k_union:
			scribe.printNextToken(Token.t_union, true);
			break;
		default:
			assert false : "Unexpected composite type specifier"; //$NON-NLS-1$
		}

		final IASTName name = node.getName();
		if (name != null) {
			IASTAttributeSpecifier[] attributes = node.getAttributeSpecifiers();
			if (attributes.length > 0) {
				/**
				 * According to GCC docs, attributes can be defined just after struct
				 * or union keywords or just after the closing brace.
				 */
				int token = peekTokenAtPosition(nodeEndOffset(attributes[0]));
				if (token == Token.tLBRACE
						|| (name.getFileLocation() != null && nodeOffset(name) > nodeOffset(attributes[0]))) {
					formatAttributes(node, true, false, IGCCASTAttributeList.TYPE_FILTER);
				} else {
					formatAttributes = true;
				}
			}
			scribe.space();
			name.accept(this);
		}

		// Member declarations
		IASTDeclaration[] memberDecls = node.getMembers();
		formatLeftCurlyBrace(line, preferences.brace_position_for_type_declaration);
		formatOpeningBrace(preferences.brace_position_for_type_declaration,
				preferences.insert_space_before_opening_brace_in_type_declaration);
		if (preferences.indent_body_declarations_compare_to_access_specifier) {
			scribe.indent();
		}
		scribe.startNewLine();
		for (IASTDeclaration declaration : memberDecls) {
			declaration.accept(this);
			scribe.startNewLine();
		}
		if (preferences.indent_body_declarations_compare_to_access_specifier) {
			scribe.unIndent();
		}
		formatClosingBrace(preferences.brace_position_for_type_declaration);
		if (formatAttributes)
			formatAttributes(node, true, false, IGCCASTAttributeList.TYPE_FILTER);
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTCompositeTypeSpecifier node) {
		boolean formatAttributes = false;

		scribe.printComment();
		final int line = scribe.line;

		// Storage class and other modifiers
		if (scribe.printModifiers()) {
			scribe.space();
		}
		final int headerIndent = scribe.numberOfIndentations;

		// Consider macro expansion
		if (withinMacroExpansion(node, getCurrentPosition())) {
			scribe.printNextToken(peekNextToken());
			continueNode(node);
			if (scribe.printComment())
				scribe.space();
		}

		int token = peekNextToken();
		if (token == Token.t_struct || token == Token.t_class || token == Token.t_union) {
			scribe.printNextToken(token, false);
		}

		final IASTName name = node.getName();
		if (name != null) {
			if (token == Token.t_struct || token == Token.t_union) {
				IASTAttributeSpecifier[] attributes = node.getAttributeSpecifiers();
				if (attributes.length > 0) {
					/**
					 * According to GCC docs, attributes can be defined just after struct
					 * or union keywords or just after the closing brace.
					 */
					token = peekTokenAtPosition(nodeEndOffset(attributes[0]));
					if (token == Token.tLBRACE
							|| (name.getFileLocation() != null && nodeOffset(name) > nodeOffset(attributes[0]))) {
						formatAttributes(node, true, false, IGCCASTAttributeList.TYPE_FILTER);
					} else {
						formatAttributes = true;
					}
				}
			}
			scribe.space();
			name.accept(this);
		}

		ICPPASTClassVirtSpecifier virtSpecifier = node.getVirtSpecifier();
		if (virtSpecifier != null) {
			scribe.space();
			virtSpecifier.accept(this);
		}

		// Base specifiers
		final List<ICPPASTBaseSpecifier> baseSpecifiers = Arrays.asList(node.getBaseSpecifiers());
		if (baseSpecifiers.size() > 0) {
			ICPPASTBaseSpecifier baseSpecifier = baseSpecifiers.get(0);
			try {
				if (baseSpecifier.getLeadingSyntax().getType() == IToken.tCOLON) {
					scribe.printNextToken(Token.tCOLON, preferences.insert_space_before_colon_in_base_clause);
					if (preferences.insert_space_after_colon_in_base_clause) {
						scribe.space();
					}
				}
			} catch (UnsupportedOperationException e) {
			} catch (ExpansionOverlapsBoundaryException e) {
			}
			final ListOptions options = new ListOptions(preferences.alignment_for_base_clause_in_type_declaration);
			options.fSpaceAfterSeparator = preferences.insert_space_after_comma_in_base_types;
			options.fSpaceBeforeSeparator = preferences.insert_space_before_comma_in_base_types;
			formatList(baseSpecifiers, options, false, false, null);
		}

		// Member declarations
		formatLeftCurlyBrace(line, preferences.brace_position_for_type_declaration);
		formatOpeningBrace(preferences.brace_position_for_type_declaration,
				preferences.insert_space_before_opening_brace_in_type_declaration);
		final int braceIndent = scribe.numberOfIndentations;
		if (braceIndent > headerIndent) {
			scribe.unIndent();
		}
		if (preferences.indent_access_specifier_compare_to_type_header) {
			scribe.indent();
		}
		if (getCurrentPosition() >= nodeEndOffset(node)) {
			return PROCESS_SKIP;
		}
		scribe.startNewLine();

		IASTDeclaration[] memberDecls = node.getMembers();
		for (int i = 0; i < memberDecls.length; i++) {
			IASTDeclaration declaration = memberDecls[i];
			if (preferences.indent_body_declarations_compare_to_access_specifier) {
				scribe.indent();
			}
			scribe.printComment();
			if (declaration instanceof ICPPASTVisibilityLabel) {
				if (preferences.indent_body_declarations_compare_to_access_specifier) {
					scribe.unIndent();
				}
				IASTDeclaration next = null;
				if (i < memberDecls.length - 1 && !(memberDecls[i + 1] instanceof IASTProblemHolder))
					next = memberDecls[i + 1];
				if (i == memberDecls.length - 1 || next == null || !doNodeLocationsOverlap(declaration, next)) {
					if (getCurrentPosition() <= nodeOffset(declaration))
						scribe.startNewLine();
				}
				declaration.accept(this);
			} else {
				if (!(declaration instanceof IASTProblemHolder)) {
					IASTDeclaration next = null;
					if (i < memberDecls.length - 1 && !(memberDecls[i + 1] instanceof IASTProblemHolder))
						next = memberDecls[i + 1];
					if (i == memberDecls.length - 1 || next == null || !doNodeLocationsOverlap(declaration, next)) {
						if (getCurrentPosition() <= nodeOffset(declaration))
							scribe.startNewLine();
					}
					declaration.accept(this);
				} else {
					skipNode(declaration);
				}
				if (preferences.indent_body_declarations_compare_to_access_specifier) {
					scribe.unIndent();
				}
			}
		}
		scribe.startNewLine();
		if (preferences.indent_body_declarations_compare_to_access_specifier) {
			scribe.indent();
		}
		scribe.printComment();
		if (preferences.indent_body_declarations_compare_to_access_specifier) {
			scribe.unIndent();
		}
		if (preferences.indent_access_specifier_compare_to_type_header) {
			scribe.unIndent();
		}
		if (scribe.numberOfIndentations < braceIndent) {
			scribe.indent();
		}
		formatClosingBrace(preferences.brace_position_for_type_declaration);
		if (formatAttributes)
			formatAttributes(node, true, false, IGCCASTAttributeList.TYPE_FILTER);
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTVisibilityLabel node) {
		if (node.getNodeLocations()[0] instanceof IASTMacroExpansionLocation) {
			skipNode(node);
		} else {
			scribe.printSpaces(preferences.indent_access_specifier_extra_spaces);

			switch (node.getVisibility()) {
			case ICPPASTVisibilityLabel.v_private:
				scribe.printNextToken(Token.t_private, false);
				break;
			case ICPPASTVisibilityLabel.v_protected:
				scribe.printNextToken(Token.t_protected, false);
				break;
			case ICPPASTVisibilityLabel.v_public:
				scribe.printNextToken(Token.t_public, false);
				break;
			}
			if (peekNextToken() != Token.tCOLON) {
				scribe.skipToToken(Token.tCOLON);
			}
			scribe.printNextToken(Token.tCOLON, false/*preferences.insert_space_before_colon_in_access specifier*/);
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTElaboratedTypeSpecifier node) {
		// Storage class and other modifiers
		if (scribe.printModifiers()) {
			scribe.space();
		}

		switch (node.getKind()) {
		case IASTElaboratedTypeSpecifier.k_enum:
			scribe.printNextToken(Token.t_enum, false);
			break;
		case IASTElaboratedTypeSpecifier.k_struct:
			scribe.printNextToken(Token.t_struct, false);
			break;
		case IASTElaboratedTypeSpecifier.k_union:
			scribe.printNextToken(Token.t_union, false);
			break;
		case ICPPASTElaboratedTypeSpecifier.k_class:
			scribe.printNextToken(Token.t_class, false);
			break;
		default:
			assert false : "Unexpected elaborated type specifier"; //$NON-NLS-1$
		}
		scribe.space();
		node.getName().accept(this);
		return PROCESS_SKIP;
	}

	private int visit(IASTEnumerationSpecifier node) {
		scribe.printComment();
		final int line = scribe.line;
		// Storage class and other modifiers
		if (scribe.printModifiers()) {
			scribe.space();
		}

		final int headerIndent = scribe.numberOfIndentations;
		scribe.printNextToken(Token.t_enum, true);
		final IASTName name = node.getName();
		if (name != null) {
			scribe.space();
			name.accept(this);
		}

		ICPPASTEnumerationSpecifier cppNode = null;
		if (node instanceof ICPPASTEnumerationSpecifier) {
			cppNode = (ICPPASTEnumerationSpecifier) node;
			formatLeadingAttributes(cppNode);
			ICPPASTDeclSpecifier baseType = cppNode.getBaseType();
			if (baseType != null) {
				scribe.space();
				scribe.printNextToken(Token.tCOLON);
				scribe.space();
				baseType.accept(this);
			}
		}

		formatLeftCurlyBrace(line, preferences.brace_position_for_type_declaration);
		formatOpeningBrace(preferences.brace_position_for_type_declaration,
				preferences.insert_space_before_opening_brace_in_type_declaration);
		final int braceIndent = scribe.numberOfIndentations;

		scribe.startNewLine();

		if (braceIndent == headerIndent) {
			scribe.indent();
		}
		final int enumIndent = scribe.numberOfIndentations;
		final IASTEnumerator[] enumerators = node.getEnumerators();

		final ListOptions options = new ListOptions(preferences.alignment_for_enumerator_list);
		options.fSpaceBeforeSeparator = preferences.insert_space_before_comma_in_enum_declarations;
		options.fSpaceAfterSeparator = preferences.insert_space_after_comma_in_enum_declarations;
		options.fContinuationIndentation = enumIndent == headerIndent ? 1 : 0;
		formatList(Arrays.asList(enumerators), options, false, false, null);

		// Handle trailing comma
		if (peekNextToken() == Token.tCOMMA) {
			scribe.printNextToken(Token.tCOMMA, options.fSpaceBeforeSeparator);
			if (options.fSpaceAfterSeparator) {
				scribe.space();
			}
		}
		scribe.printTrailingComment();

		if (enumIndent > braceIndent) {
			scribe.unIndent();
		}
		scribe.startNewLine();

		formatClosingBrace(preferences.brace_position_for_type_declaration);
		return PROCESS_SKIP;
	}

	/**
	 * Format a given list of elements according alignment options.
	 *
	 * @param elements the elements to format, which can be either {@link IASTNode}s or
	 *     {@link TokenRange}s.
	 * @param options formatting options
	 * @param encloseInParen indicates whether the list should be enclosed in parentheses
	 * @param addEllipsis indicates whether ellipsis should be added after the last element
	 * @param tailFormatter formatter for the trailing text that should be kept together with
	 * 		the last element of the list.
	 */
	private void formatList(List<?> elements, ListOptions options, boolean encloseInParen, boolean addEllipsis,
			Runnable tailFormatter) {
		formatList(elements, options, encloseInParen, addEllipsis, tailFormatter, null);
	}

	/**
	 * Format a given list of elements according alignment options.
	 *
	 * @param elements the elements to format, which can be either {@link IASTNode}s or
	 *     {@link TokenRange}s.
	 * @param options formatting options
	 * @param encloseInParen indicates whether the list should be enclosed in parentheses
	 * @param addEllipsis indicates whether ellipsis should be added after the last element
	 * @param tailFormatter formatter for the trailing text that should be kept together with
	 * 		the last element of the list.
	 * @param prefix A custom list prefix to format the first element
	 */
	private void formatList(List<?> elements, ListOptions options, boolean encloseInParen, boolean addEllipsis,
			Runnable tailFormatter, Runnable prefix) {
		if (encloseInParen)
			scribe.printNextToken(options.leftToken, options.fSpaceBeforeOpeningParen);

		final int elementsLength = elements.size();
		if (encloseInParen) {
			boolean spaceBeforeClosingParen = elements.isEmpty() && !addEllipsis ? options.fSpaceBetweenEmptyParen
					: options.fSpaceBeforeClosingParen;
			tailFormatter = new ClosingParensesisTailFormatter(spaceBeforeClosingParen, tailFormatter,
					options.rightToken);
		}

		if (prefix != null)
			prefix.run();

		if (!elements.isEmpty() || addEllipsis) {
			if (options.fSpaceAfterOpeningParen) {
				scribe.space();
			}

			final int continuationIndentation = options.fContinuationIndentation >= 0 ? options.fContinuationIndentation
					: preferences.continuation_indentation;
			Alignment alignment = scribe.createAlignment(
					Alignment.LIST_ELEMENTS_PREFIX
							+ (elements.isEmpty() ? "ellipsis" : elements.get(0).getClass().getSimpleName()), //$NON-NLS-1$
					options.fMode, options.fTieBreakRule, elementsLength + (addEllipsis ? 1 : 0), getCurrentPosition(),
					continuationIndentation, false);
			scribe.enterAlignment(alignment);
			boolean ok = false;
			do {
				try {
					int i;
					for (i = 0; i < elementsLength; i++) {
						final Object element = elements.get(i);
						if (i < elementsLength - 1) {
							scribe.setTailFormatter(new TrailingTokenFormatter(options.fSeparatorToken,
									findTokenAfterNodeOrTokenRange(options.fSeparatorToken, element),
									options.fSpaceBeforeSeparator, options.fSpaceAfterSeparator));
						} else {
							scribe.setTailFormatter(tailFormatter);
						}
						scribe.alignFragment(alignment, i);
						if (element instanceof IASTNode) {
							if (element instanceof ICPPASTConstructorChainInitializer) {
								// Constructor chain initializer is a special case.
								visit((ICPPASTConstructorChainInitializer) element);
							} else {
								((IASTNode) element).accept(this);
							}
						} else {
							formatTokenRange((TokenRange) element);
						}
						if (i < elementsLength - 1) {
							scribe.runTailFormatter();
						}
					}
					if (addEllipsis) {
						if (i > 0) {
							scribe.printNextToken(options.fSeparatorToken, options.fSpaceBeforeSeparator);
							scribe.printTrailingComment();
						}
						scribe.alignFragment(alignment, i);
						if (i > 0 && options.fSpaceAfterSeparator) {
							scribe.space();
						}
						scribe.printNextToken(Token.tELIPSE);
					}
					scribe.runTailFormatter();
					ok = true;
				} catch (AlignmentException e) {
					scribe.redoAlignment(e);
				} catch (ASTProblemException e) {
				}
			} while (!ok);
			scribe.exitAlignment(alignment, true);
		} else if (tailFormatter != null) {
			tailFormatter.run();
		}
	}

	private void formatTokenRange(TokenRange tokenRange) {
		scribe.restartAtOffset(tokenRange.getOffset());
		while (getCurrentPosition() < tokenRange.getEndOffset()) {
			boolean hasWhitespace = scribe.printComment();
			int token = peekNextToken();
			scribe.printNextToken(token, hasWhitespace);
		}
	}

	private int visit(ICPPASTTryBlockStatement node) {
		scribe.printNextToken(Token.t_try, scribe.printComment());
		final IASTStatement tryBody = node.getTryBody();
		if (tryBody != null) {
			tryBody.accept(this);
		}
		scribe.printTrailingComment();
		ICPPASTCatchHandler[] catchHandlers = node.getCatchHandlers();
		for (ICPPASTCatchHandler catchHandler : catchHandlers) {
			catchHandler.accept(this);
			scribe.printTrailingComment();
		}
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTCatchHandler node) {
		int token = peekNextToken();
		if (token == Token.t_catch) {
			if (preferences.insert_new_line_before_catch_in_try_statement) {
				scribe.startNewLine();
			}
			scribe.printNextToken(token, true);
		}
		token = peekNextToken();
		if (token == Token.tLPAREN) {
			scribe.printNextToken(token, preferences.insert_space_before_opening_paren_in_catch);
			if (preferences.insert_space_after_opening_paren_in_catch) {
				scribe.space();
			}
		}
		final IASTDeclaration decl = node.getDeclaration();
		if (decl != null) {
			formatInlineDeclaration(decl);
		} else if (node.isCatchAll()) {
			token = peekNextToken();
			if (token == Token.tELIPSE) {
				scribe.printNextToken(token, false /* preferences.insert_space_before_ellipsis */);
				//				if (false /* preferences.insert_space_after_ellipsis */) {
				//					scribe.space();
				//				}
			}
		}
		token = peekNextToken();
		if (token == Token.tRPAREN) {
			scribe.printNextToken(token, preferences.insert_space_before_closing_paren_in_catch);
		}
		final IASTStatement catchBody = node.getCatchBody();
		if (catchBody != null) {
			catchBody.accept(this);
		}
		return PROCESS_SKIP;
	}

	private void formatInlineDeclaration(final IASTDeclaration decl) {
		boolean previousExpectSemicolonAfterDeclaration = fExpectSemicolonAfterDeclaration;
		fExpectSemicolonAfterDeclaration = false;
		try {
			decl.accept(this);
		} finally {
			fExpectSemicolonAfterDeclaration = previousExpectSemicolonAfterDeclaration;
		}
	}

	private int visit(IASTCompoundStatement node) {
		formatBlock(node, preferences.brace_position_for_block, preferences.insert_space_before_opening_brace_in_block,
				preferences.indent_statements_compare_to_block);
		return PROCESS_SKIP;
	}

	private int visit(IASTBreakStatement node) {
		formatLeadingAttributes(node);
		scribe.printNextToken(Token.t_break);
		scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon);
		scribe.printTrailingComment();
		return PROCESS_SKIP;
	}

	private int visit(IASTConditionalExpression node) {
		// Count nested conditional expressions.
		int numConditions = 0;
		for (IASTExpression expression = node; expression instanceof IASTConditionalExpression; expression = ((IASTConditionalExpression) expression)
				.getNegativeResultExpression()) {
			numConditions++;
		}

		Runnable tailFormatter = scribe.takeTailFormatter();

		Alignment alignment = scribe.createAlignment(Alignment.CONDITIONAL_EXPRESSION_CHAIN,
				preferences.alignment_for_conditional_expression_chain, Alignment.R_OUTERMOST, numConditions,
				getCurrentPosition());

		scribe.enterAlignment(alignment);
		boolean ok = false;
		do {
			try {
				IASTConditionalExpression expression = node;
				for (int i = 0;; i++) {
					scribe.alignFragment(alignment, i);
					boolean last = i == numConditions - 1;
					formatConditionalExpression(expression, last ? tailFormatter : null);
					if (last)
						break;
					expression = (IASTConditionalExpression) expression.getNegativeResultExpression();
				}
				ok = true;
			} catch (AlignmentException e) {
				scribe.redoAlignment(e);
			}
		} while (!ok);
		scribe.exitAlignment(alignment, true);

		return PROCESS_SKIP;
	}

	private void formatConditionalExpression(IASTConditionalExpression node, Runnable tailFormatter) {
		scribe.setTailFormatter(new TrailingTokenFormatter(Token.tQUESTION, node,
				preferences.insert_space_before_question_in_conditional,
				preferences.insert_space_after_question_in_conditional));
		node.getLogicalConditionExpression().accept(this);
		scribe.runTailFormatter();

		final IASTExpression positiveExpression = node.getPositiveResultExpression();
		final IASTExpression negativeExpression = node.getNegativeResultExpression();

		Alignment alignment = scribe.createAlignment(Alignment.CONDITIONAL_EXPRESSION,
				preferences.alignment_for_conditional_expression, Alignment.R_OUTERMOST,
				negativeExpression instanceof IASTConditionalExpression ? 1 : 2, getCurrentPosition());

		scribe.enterAlignment(alignment);
		boolean ok = false;
		do {
			try {
				// In case of macros we may have already passed the expression position.
				if (positiveExpression != null && getCurrentPosition() <= nodeOffset(positiveExpression)) {
					scribe.alignFragment(alignment, 0);
				}
				scribe.setTailFormatter(new TrailingTokenFormatter(Token.tCOLON, node,
						preferences.insert_space_before_colon_in_conditional,
						preferences.insert_space_after_colon_in_conditional));
				// A gcc extension allows the positive expression to be omitted.
				if (positiveExpression != null) {
					positiveExpression.accept(this);
				}
				scribe.runTailFormatter();

				if (!(negativeExpression instanceof IASTConditionalExpression)) {
					// In case of macros we may have already passed the expression position.
					if (getCurrentPosition() <= nodeOffset(negativeExpression)) {
						scribe.alignFragment(alignment, 1);
					}
					scribe.setTailFormatter(tailFormatter);
					negativeExpression.accept(this);
					scribe.runTailFormatter();
				}
				ok = true;
			} catch (AlignmentException e) {
				scribe.redoAlignment(e);
			}
		} while (!ok);
		scribe.exitAlignment(alignment, true);
	}

	private int visit(IASTFunctionCallExpression node) {
		Runnable tailFormatter = scribe.takeTailFormatter();
		try {
			node.getFunctionNameExpression().accept(this);
		} finally {
			scribe.setTailFormatter(tailFormatter);
		}
		IASTInitializerClause[] paramExpr = node.getArguments();
		if (peekNextToken() == Token.tIDENTIFIER) {
			skipNode(node);
		} else {
			formatFunctionCallArguments(paramExpr);
		}
		return PROCESS_SKIP;
	}

	/**
	 * Formats given expressions as a function call, ie. enclosed in parenthesis.
	 *
	 * @param args  the argument expressions, may be <code>null</code>
	 */
	private void formatFunctionCallArguments(IASTInitializerClause[] args) {
		// check for macro
		if (peekNextToken() != Token.tLPAREN) {
			if (args == null || args.length == 0 || enclosedInMacroExpansion(args[0])) {
				return;
			}
		}
		final List<IASTInitializerClause> expressions;
		if (args != null) {
			expressions = Arrays.asList(args);
		} else {
			// No arguments
			expressions = Collections.emptyList();
		}
		final ListOptions options = new ListOptions(preferences.alignment_for_arguments_in_method_invocation);
		options.fSeparatorToken = Token.tCOMMA;
		options.fSpaceBeforeOpeningParen = preferences.insert_space_before_opening_paren_in_method_invocation;
		options.fSpaceAfterOpeningParen = preferences.insert_space_after_opening_paren_in_method_invocation;
		options.fSpaceBeforeClosingParen = preferences.insert_space_before_closing_paren_in_method_invocation;
		options.fSpaceBetweenEmptyParen = preferences.insert_space_between_empty_parens_in_method_invocation;
		options.fSpaceBeforeSeparator = preferences.insert_space_before_comma_in_method_invocation_arguments;
		options.fSpaceAfterSeparator = preferences.insert_space_after_comma_in_method_invocation_arguments;
		options.fTieBreakRule = Alignment.R_OUTERMOST;
		formatList(expressions, options, true, false, scribe.takeTailFormatter());
	}

	private int visit(IASTExpressionList node) {
		final List<IASTExpression> expressions = Arrays.asList(node.getExpressions());
		final ListOptions options = new ListOptions(preferences.alignment_for_expression_list);
		options.fSpaceBeforeSeparator = preferences.insert_space_before_comma_in_expression_list;
		options.fSpaceAfterSeparator = preferences.insert_space_after_comma_in_expression_list;
		formatList(expressions, options, false, false, null);
		return PROCESS_SKIP;
	}

	private int visit(IASTIdExpression node) {
		node.getName().accept(this);
		return PROCESS_SKIP;
	}

	private int visit(IASTCastExpression node) {
		Runnable tailFormatter = null;
		switch (node.getOperator()) {
		case IASTCastExpression.op_cast:
			scribe.printNextToken(Token.tLPAREN, false);
			if (preferences.insert_space_after_opening_paren_in_cast) {
				scribe.space();
			}
			tailFormatter = scribe.takeTailFormatter();
			try {
				node.getTypeId().accept(this);
			} finally {
				scribe.setTailFormatter(tailFormatter);
			}
			try {
				if (node.getTypeId().getTrailingSyntax().getType() == IToken.tRPAREN) {
					scribe.printNextToken(Token.tRPAREN, preferences.insert_space_before_closing_paren_in_cast);
					if (preferences.insert_space_after_closing_paren_in_cast) {
						scribe.space();
					}
				}
			} catch (UnsupportedOperationException exc) {
			} catch (ExpansionOverlapsBoundaryException exc) {
				scribe.space();
			}
			// operand
			node.getOperand().accept(this);
			break;
		case ICPPASTCastExpression.op_const_cast:
		case ICPPASTCastExpression.op_dynamic_cast:
		case ICPPASTCastExpression.op_reinterpret_cast:
		case ICPPASTCastExpression.op_static_cast:
			scribe.printNextToken(peekNextToken(), false);
			scribe.printNextToken(Token.tLT,
					preferences.insert_space_before_opening_angle_bracket_in_template_arguments);
			if (preferences.insert_space_after_opening_angle_bracket_in_template_arguments) {
				scribe.space();
			}
			tailFormatter = scribe.takeTailFormatter();
			try {
				node.getTypeId().accept(this);
			} finally {
				scribe.setTailFormatter(tailFormatter);
			}
			scribe.printNextToken(Token.tGT,
					preferences.insert_space_before_closing_angle_bracket_in_template_arguments);
			if (preferences.insert_space_before_opening_paren_in_method_invocation) {
				scribe.space();
			}
			// operand
			scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_method_invocation);
			if (preferences.insert_space_after_opening_paren_in_method_invocation) {
				scribe.space();
			}
			tailFormatter = scribe.takeTailFormatter();
			try {
				node.getOperand().accept(this);
			} finally {
				scribe.setTailFormatter(tailFormatter);
			}
			scribe.printNextToken(Token.tRPAREN, preferences.insert_space_before_closing_paren_in_method_invocation);
			break;
		default:
			skipToNode(node.getOperand());
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTTypeIdExpression node) {
		if (enclosedInMacroExpansion(node)) {
			return PROCESS_SKIP;
		}
		scribe.printNextToken(peekNextToken());
		if (peekNextToken() == IToken.tELLIPSIS) {
			scribe.printNextToken(IToken.tELLIPSIS);
		}
		scribe.printNextToken(Token.tLPAREN);
		node.getTypeId().accept(this);
		if (peekNextToken() == Token.tCOMMA) {
			scribe.printNextToken(Token.tCOMMA, preferences.insert_space_before_comma_in_method_invocation_arguments);
			scribe.printNextToken(peekNextToken(), preferences.insert_space_after_comma_in_method_invocation_arguments);
			scribe.skipToToken(Token.tRPAREN);
		}
		scribe.printNextToken(Token.tRPAREN);
		return PROCESS_SKIP;
	}

	private int visit(IASTEqualsInitializer node) {
		if (node.getPropertyInParent() == IASTInitializerList.NESTED_INITIALIZER) {
			assert false;
			// Nested initializer expression, no need to apply extra alignment
			//			node.getExpression().accept(this);
		} else {
			// Declaration initializer
			Alignment alignment = scribe.createAlignment(Alignment.DECLARATION_INITIALIZER,
					preferences.alignment_for_assignment, Alignment.R_INNERMOST, 1, getCurrentPosition());

			Runnable tailFormatter = scribe.getTailFormatter();
			scribe.enterAlignment(alignment);
			scribe.setTailFormatter(tailFormatter); // Inherit tail formatter from the enclosing alignment
			boolean ok = false;
			do {
				try {
					scribe.alignFragment(alignment, 0);
					node.getInitializerClause().accept(this);
					ok = true;
				} catch (AlignmentException e) {
					scribe.redoAlignment(e);
				}
			} while (!ok);
			scribe.exitAlignment(alignment, true);
		}
		return PROCESS_SKIP;
	}

	private int visit(ICASTDesignatedInitializer node) {
		scribe.printComment();
		ICASTDesignator[] designators = node.getDesignators();
		for (ICASTDesignator designator : designators) {
			designator.accept(this);
			if (scribe.printComment()) {
				scribe.space();
			}
		}

		if (peekNextToken() == Token.tASSIGN) {
			scribe.printNextToken(Token.tASSIGN, preferences.insert_space_before_assignment_operator);
			if (preferences.insert_space_after_assignment_operator) {
				scribe.space();
			}
		}

		Alignment expressionAlignment = scribe.createAlignment(Alignment.DESIGNATED_INITIALIZER,
				preferences.alignment_for_assignment, 1, getCurrentPosition());

		scribe.enterAlignment(expressionAlignment);
		boolean ok = false;
		do {
			try {
				scribe.alignFragment(expressionAlignment, 0);

				IASTInitializerClause initializer = node.getOperand();
				initializer.accept(this);

				ok = true;
			} catch (AlignmentException e) {
				scribe.redoAlignment(e);
			}
		} while (!ok);
		scribe.exitAlignment(expressionAlignment, true);
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTDesignatedInitializer node) {
		scribe.printComment();
		ICPPASTDesignator[] designators = node.getDesignators();
		for (ICPPASTDesignator designator : designators) {
			designator.accept(this);
			if (scribe.printComment()) {
				scribe.space();
			}
		}

		if (peekNextToken() == Token.tASSIGN) {
			scribe.printNextToken(Token.tASSIGN, preferences.insert_space_before_assignment_operator);
			if (preferences.insert_space_after_assignment_operator) {
				scribe.space();
			}
		}

		Alignment expressionAlignment = scribe.createAlignment(Alignment.DESIGNATED_INITIALIZER,
				preferences.alignment_for_assignment, 1, getCurrentPosition());

		scribe.enterAlignment(expressionAlignment);
		boolean ok = false;
		do {
			try {
				scribe.alignFragment(expressionAlignment, 0);

				IASTInitializerClause initializer = node.getOperand();
				initializer.accept(this);

				ok = true;
			} catch (AlignmentException e) {
				scribe.redoAlignment(e);
			}
		} while (!ok);
		scribe.exitAlignment(expressionAlignment, true);
		return PROCESS_SKIP;
	}

	private int visit(IASTInitializerList node) {
		scribe.printComment();

		final List<IASTInitializerClause> initializers = Arrays.asList(node.getClauses());
		if (initializers.isEmpty() && preferences.keep_empty_initializer_list_on_one_line) {
			scribe.printNextToken(Token.tLBRACE, preferences.insert_space_before_opening_brace_in_initializer_list);
			scribe.printNextToken(Token.tRBRACE, preferences.insert_space_between_empty_braces_in_initializer_list);
		} else {
			final int line = scribe.line;
			final String brace_position = preferences.brace_position_for_initializer_list;
			formatLeftCurlyBrace(line, brace_position);
			formatOpeningBrace(brace_position, preferences.insert_space_before_opening_brace_in_initializer_list);
			if (preferences.insert_new_line_after_opening_brace_in_initializer_list) {
				scribe.startNewLine();
			}
			if (preferences.insert_space_after_opening_brace_in_initializer_list) {
				scribe.space();
			}

			final ListOptions options = new ListOptions(preferences.alignment_for_expressions_in_initializer_list);
			options.fSpaceBeforeSeparator = preferences.insert_space_before_comma_in_initializer_list;
			options.fSpaceAfterSeparator = preferences.insert_space_after_comma_in_initializer_list;
			options.fContinuationIndentation = preferences.continuation_indentation_for_initializer_list;
			formatList(initializers, options, false, false, null);

			// handle trailing comma
			if (peekNextToken() == Token.tCOMMA) {
				scribe.printNextToken(Token.tCOMMA, options.fSpaceBeforeSeparator);
				if (options.fSpaceAfterSeparator) {
					scribe.space();
				}
			}

			if (preferences.insert_new_line_before_closing_brace_in_initializer_list) {
				scribe.startNewLine();
			}
			if (preferences.insert_space_before_closing_brace_in_initializer_list) {
				scribe.space();
			}
			formatClosingBrace(brace_position);
		}
		scribe.printTrailingComment();
		return PROCESS_SKIP;
	}

	private int visit(IASTUnaryExpression node) {
		if (enclosedInMacroExpansion(node)) {
			return PROCESS_SKIP;
		}
		final IASTExpression operand = node.getOperand();
		final int operator = node.getOperator();
		switch (operator) {
		case IASTUnaryExpression.op_bracketedPrimary:
			formatParenthesizedExpression(operand);
			break;
		case IASTUnaryExpression.op_prefixIncr:
		case IASTUnaryExpression.op_prefixDecr:
			scribe.printNextToken(peekNextToken(), preferences.insert_space_before_prefix_operator);
			if (preferences.insert_space_after_prefix_operator) {
				scribe.space();
			}
			operand.accept(this);
			break;
		case IASTUnaryExpression.op_postFixIncr:
		case IASTUnaryExpression.op_postFixDecr:
			operand.accept(this);
			scribe.printNextToken(peekNextToken(), preferences.insert_space_before_postfix_operator);
			if (preferences.insert_space_after_postfix_operator) {
				scribe.space();
			}
			break;
		case IASTUnaryExpression.op_sizeof:
			scribe.printNextToken(Token.t_sizeof, scribe.printComment());
			if (peekNextToken() != Token.tLPAREN) {
				scribe.space();
			}
			operand.accept(this);
			break;
		case IASTUnaryExpression.op_sizeofParameterPack:
			scribe.printNextToken(Token.t_sizeof, scribe.printComment());
			scribe.printNextToken(Token.tELIPSE, scribe.printComment());
			scribe.printNextToken(Token.tLPAREN);
			operand.accept(this);
			break;
		case IASTUnaryExpression.op_throw:
			scribe.printNextToken(Token.t_throw, scribe.printComment());
			if (operand != null) {
				if (peekNextToken() != Token.tLPAREN) {
					scribe.space();
				}
				operand.accept(this);
			}
			break;
		case IASTUnaryExpression.op_typeid:
			scribe.printNextToken(Token.t_typeid, scribe.printComment());
			if (peekNextToken() != Token.tLPAREN) {
				scribe.space();
			}
			operand.accept(this);
			break;
		case IASTUnaryExpression.op_alignOf:
		default:
			int operatorToken = peekNextToken();
			boolean forceSpace = Character.isJavaIdentifierStart(peekNextChar());
			scribe.printNextToken(operatorToken, preferences.insert_space_before_unary_operator);
			if (forceSpace || preferences.insert_space_after_unary_operator) {
				scribe.space();
			} else if (operatorToken == Token.tIDENTIFIER && peekNextToken() != Token.tLPAREN) {
				scribe.space();
			}
			operand.accept(this);
			break;
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTBinaryExpression node) {
		if (enclosedInMacroExpansion(node)) {
			return PROCESS_SKIP;
		}
		IASTNode op2 = node.getOperand2();
		if (op2 == null)
			op2 = node.getInitOperand2();
		if (doNodeLocationsOverlap(node.getOperand1(), op2)) {
			// Overlapping of operands is possible if the central part of the binary expression is
			// a result of macro expansion. There is no need to print the operator in such case,
			// so we simply delegate to each of the operands.
			node.getOperand1().accept(this);
			op2.accept(this);
			return PROCESS_SKIP;
		}
		if (isAssignment(node)) {
			return formatAssignment(node);
		}
		if (isOverloadedLeftShift(node)) {
			return formatOverloadedLeftShiftChain(node);
		}

		// To improve speed of the algorithm we flatten homogeneous nested binary expressions
		// to reduce overall depth of the expression tree.
		IASTExpression[] operands = CPPVisitor.getOperandsOfMultiExpression(node);

		Runnable tailFormatter = endsWithMacroExpansion(node) ? null : scribe.takeTailFormatter();

		Alignment alignment = scribe.createAlignment(Alignment.BINARY_EXPRESSION,
				preferences.alignment_for_binary_expression, Alignment.R_OUTERMOST, operands.length,
				getCurrentPosition());

		scribe.enterAlignment(alignment);
		boolean ok = false;
		do {
			try {
				for (int i = 0; i < operands.length; i++) {
					final IASTExpression operand = operands[i];
					// In case of macros we may have already passed the operator position.
					if (i > 0 && getCurrentPosition() < nodeOffset(operand)) {
						scribe.alignFragment(alignment, i);

						// Operator
						final int nextToken = peekNextToken();
						// In case of C++ alternative operators, like 'and', 'or', etc. a space
						boolean forceSpace = Character.isJavaIdentifierStart(peekNextChar());

						switch (node.getOperator()) {
						case IASTBinaryExpression.op_pmdot:
						case IASTBinaryExpression.op_pmarrow:
							scribe.printNextToken(nextToken, false);
							break;

						default:
							scribe.printNextToken(nextToken,
									forceSpace || preferences.insert_space_before_binary_operator);
							if (forceSpace || preferences.insert_space_after_binary_operator) {
								scribe.space();
							}
						}
						scribe.printTrailingComment();
					}
					if (i == alignment.fragmentCount - 1) {
						scribe.setTailFormatter(tailFormatter);
					}
					operand.accept(this);
					scribe.restartAtOffset(nodeEndOffset(operand));
					scribe.printTrailingComment();
				}

				scribe.runTailFormatter();
				ok = true;
			} catch (AlignmentException e) {
				scribe.redoAlignment(e);
			}
		} while (!ok);
		scribe.exitAlignment(alignment, true);
		return PROCESS_SKIP;
	}

	private int formatAssignment(IASTBinaryExpression node) {
		Runnable tailFormatter = scribe.takeTailFormatter();
		final IASTExpression op1 = node.getOperand1();
		// Operand 1
		op1.accept(this);

		// In case of macros we may have already passed the equal sign position.
		IASTNode op2 = node.getOperand2();
		if (op2 == null)
			op2 = node.getInitOperand2();
		if (getCurrentPosition() < nodeOffset(op2)) {
			// Operator
			final int nextToken = peekNextToken();
			// In case of C++ alternative operators, like 'and', 'not', etc. a space
			boolean forceSpace = Character.isJavaIdentifierStart(peekNextChar());

			scribe.printNextToken(nextToken, forceSpace || preferences.insert_space_before_assignment_operator);
			if (forceSpace || preferences.insert_space_after_assignment_operator) {
				scribe.space();
			}
		}

		Alignment expressionAlignment = scribe.createAlignment(Alignment.ASSIGNMENT_EXPRESSION,
				preferences.alignment_for_assignment, Alignment.R_INNERMOST, 1, getCurrentPosition());

		scribe.enterAlignment(expressionAlignment);
		boolean ok = false;
		do {
			try {
				scribe.alignFragment(expressionAlignment, 0);

				scribe.setTailFormatter(tailFormatter);
				// Operand 2
				op2 = node.getOperand2();
				if (op2 == null)
					op2 = node.getInitOperand2();
				op2.accept(this);
				scribe.runTailFormatter();
				ok = true;
			} catch (AlignmentException e) {
				scribe.redoAlignment(e);
			}
		} while (!ok);
		scribe.exitAlignment(expressionAlignment, true);
		return PROCESS_SKIP;
	}

	private boolean isAssignment(IASTBinaryExpression node) {
		switch (node.getOperator()) {
		case IASTBinaryExpression.op_assign:
		case IASTBinaryExpression.op_binaryAndAssign:
		case IASTBinaryExpression.op_binaryOrAssign:
		case IASTBinaryExpression.op_binaryXorAssign:
		case IASTBinaryExpression.op_divideAssign:
		case IASTBinaryExpression.op_minusAssign:
		case IASTBinaryExpression.op_moduloAssign:
		case IASTBinaryExpression.op_multiplyAssign:
		case IASTBinaryExpression.op_plusAssign:
		case IASTBinaryExpression.op_shiftLeftAssign:
		case IASTBinaryExpression.op_shiftRightAssign:
			return true;
		}
		return false;
	}

	private int formatOverloadedLeftShiftChain(IASTBinaryExpression binaryExpression) {
		List<IASTExpression> elements = new ArrayList<>();
		IASTExpression node;
		do {
			elements.add(binaryExpression.getOperand2());
			node = binaryExpression.getOperand1();
			if (!(node instanceof IASTBinaryExpression)) {
				break;
			}
			binaryExpression = (IASTBinaryExpression) node;
		} while (isOverloadedLeftShift(binaryExpression));
		Collections.reverse(elements);

		Runnable tailFormatter = scribe.takeTailFormatter();
		node.accept(this);
		scribe.printComment();
		if (preferences.insert_space_before_binary_operator) {
			scribe.space();
		}

		Alignment alignment = scribe.createAlignment(Alignment.OVERLOADED_LEFT_SHIFT_CHAIN,
				preferences.alignment_for_overloaded_left_shift_chain, Alignment.R_OUTERMOST, elements.size(),
				getCurrentPosition(), preferences.continuation_indentation, false);
		scribe.enterAlignment(alignment);
		boolean ok = false;
		do {
			try {
				for (int i = 0; i < elements.size(); i++) {
					node = elements.get(i);
					// In case of macros we may have already passed the operator position.
					if (getCurrentPosition() < nodeOffset(node)) {
						scribe.alignFragment(alignment, i);
						int token = peekNextToken();
						if (token == Token.tSHIFTL) {
							scribe.printNextToken(token, preferences.insert_space_before_binary_operator);
							scribe.printTrailingComment();
							if (preferences.insert_space_after_binary_operator) {
								scribe.space();
							}
						}
					}
					if (i == alignment.fragmentCount - 1) {
						scribe.setTailFormatter(tailFormatter);
					}
					node.accept(this);
				}
				scribe.runTailFormatter();
				ok = true;
			} catch (AlignmentException e) {
				scribe.redoAlignment(e);
			} catch (ASTProblemException e) {
			}
		} while (!ok);
		scribe.exitAlignment(alignment, true);
		return PROCESS_SKIP;
	}

	private boolean isOverloadedLeftShift(IASTBinaryExpression node) {
		return node.getOperator() == IASTBinaryExpression.op_shiftLeft && node instanceof ICPPASTBinaryExpression
				&& ((ICPPASTBinaryExpression) node).getOverload() != null;
	}

	private int visit(IASTLiteralExpression node) {
		if (node.getKind() == IASTLiteralExpression.lk_string_literal) {
			// Handle concatenation of string literals
			int token;
			boolean needSpace = false;
			final int line = scribe.line;
			boolean indented = false;
			int indentationLevel = scribe.indentationLevel;
			int numberOfIndentations = scribe.numberOfIndentations;
			try {
				final int[] stringLiterals = { Token.tSTRING, Token.tLSTRING, Token.tRSTRING };
				while (true) {
					scribe.printNextToken(stringLiterals, needSpace);
					token = peekNextToken();
					if (token != Token.tSTRING && token != Token.tLSTRING && token != Token.tRSTRING) {
						break;
					}
					scribe.printCommentPreservingNewLines();
					if (!indented && line != scribe.line) {
						Alignment alignment = scribe.currentAlignment;
						if (alignment != null && (alignment.mode & Alignment.M_INDENT_ON_COLUMN) != 0) {
							scribe.indentationLevel = alignment.breakIndentationLevel;
						} else if (alignment != null && (alignment.mode & Alignment.M_INDENT_BY_ONE) != 0) {
							indented = true;
							scribe.indent();
						} else {
							indented = true;
							scribe.indentForContinuation();
						}
					}
					needSpace = true;
				}
			} finally {
				// Restore indentation.
				scribe.indentationLevel = indentationLevel;
				scribe.numberOfIndentations = numberOfIndentations;
			}
		} else {
			scribe.printNextToken(peekNextToken());
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTFieldReference node) {
		IASTExpression expr = node.getFieldOwner();
		if (expr != null) {
			Runnable tailFormatter = scribe.takeTailFormatter();
			try {
				expr.accept(this);
			} finally {
				scribe.setTailFormatter(tailFormatter);
			}
		}
		final IASTName fieldName = node.getFieldName();
		if (fieldName != null) {
			Alignment alignment = scribe.createAlignment(Alignment.FIELD_REFERENCE,
					preferences.alignment_for_member_access, Alignment.R_OUTERMOST, 1, getCurrentPosition());

			scribe.enterAlignment(alignment);
			boolean ok = false;
			do {
				try {
					scribe.alignFragment(alignment, 0);

					scribe.printComment();
					int token = peekNextToken();
					if (token == Token.tARROW || token == Token.tDOT)
						scribe.printNextToken(token, false);
					scribe.printComment();
					if (node instanceof ICPPASTFieldReference) {
						if (((ICPPASTFieldReference) node).isTemplate()) {
							scribe.printNextToken(Token.t_template);
							scribe.space();
						}
					}
					fieldName.accept(this);
					ok = true;
				} catch (AlignmentException e) {
					scribe.redoAlignment(e);
				}
			} while (!ok);
			scribe.exitAlignment(alignment, true);
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTArraySubscriptExpression node) {
		Runnable tailFormatter = scribe.takeTailFormatter();
		try {
			node.getArrayExpression().accept(this);

			scribe.printNextToken(Token.tLBRACKET, preferences.insert_space_before_opening_bracket);
			if (preferences.insert_space_after_opening_bracket) {
				scribe.space();
			}

			node.getArgument().accept(this);

			scribe.printNextToken(Token.tRBRACKET, preferences.insert_space_before_closing_bracket);
		} finally {
			scribe.setTailFormatter(tailFormatter);
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTTypeIdInitializerExpression node) {
		scribe.printComment();
		final int line = scribe.line;

		node.getTypeId().accept(this);

		final String brace_position = preferences.brace_position_for_initializer_list;
		formatLeftCurlyBrace(line, brace_position);
		formatOpeningBrace(brace_position, preferences.insert_space_before_opening_brace_in_initializer_list);
		if (preferences.insert_new_line_after_opening_brace_in_initializer_list) {
			scribe.printNewLine();
		}
		if (preferences.insert_space_after_opening_brace_in_initializer_list) {
			scribe.space();
		}

		node.getInitializer().accept(this);

		if (preferences.insert_new_line_before_closing_brace_in_initializer_list) {
			scribe.startNewLine();
		}
		if (preferences.insert_space_before_closing_brace_in_initializer_list) {
			scribe.space();
		}
		formatClosingBrace(brace_position);
		scribe.printTrailingComment();
		return PROCESS_SKIP;
	}

	/**
	 * <pre>
	 * 	  new-expression:
	 * 	          ::<sub>opt</sub> new new-placement<sub>opt</sub> new-type-id new-initializer<sub>opt</sub>
	 * 	          ::<sub>opt</sub> new new-placement<sub>opt</sub> ( type-id ) new-initializer<sub>opt</sub>
	 * 	  new-placement:
	 * 	          ( expression-list )
	 * 	  new-type-id:
	 * 	          type-specifier-seq new-declarator<sub>opt</sub>
	 * 	  new-declarator:
	 * 	          ptr-operator new-declarator<sub>opt</sub>
	 * 	          direct-new-declarator
	 * 	  direct-new-declarator:
	 * 	          [ expression ]
	 * 	          direct-new-declarator [ constant-expression ]
	 * 	  new-initializer:
	 * 	          ( expression-list<sub>opt</sub> )
	 * </pre>
	 */
	private int visit(ICPPASTNewExpression node) {
		if (node.isGlobal()) {
			scribe.printNextToken(Token.tCOLONCOLON);
		}
		scribe.printNextToken(Token.t_new);
		scribe.space();

		// Placement
		final IASTInitializerClause[] newPlacement = node.getPlacementArguments();
		if (newPlacement != null) {
			Runnable tailFormatter = scribe.takeTailFormatter();
			formatFunctionCallArguments(newPlacement);
			scribe.setTailFormatter(tailFormatter);
		}

		// type-id
		scribe.space();
		final IASTTypeId typeId = node.getTypeId();
		final boolean expectParen = !node.isNewTypeId() && peekNextToken() == Token.tLPAREN;
		if (expectParen) {
			scribe.printNextToken(Token.tLPAREN, false);
		}
		typeId.accept(this);
		if (expectParen) {
			scribe.printNextToken(Token.tRPAREN);
		}

		// initializer
		final IASTInitializer newInitializer = node.getInitializer();
		if (newInitializer != null) {
			visit(newInitializer);
		}
		return PROCESS_SKIP;
	}

	/**
	 * <pre>
	 * 	 delete-expression:
	 * 	      ::<sub>opt</sub> delete cast-expression
	 * 	      ::<sub>opt</sub> delete [ ] cast-expression
	 * </pre>
	 */
	private int visit(ICPPASTDeleteExpression node) {
		if (node.isGlobal()) {
			scribe.printNextToken(Token.tCOLONCOLON);
		}
		scribe.printNextToken(Token.t_delete);
		if (node.isVectored()) {
			scribe.printNextToken(Token.tLBRACKET, preferences.insert_space_before_opening_bracket);
			scribe.printNextToken(Token.tRBRACKET, preferences.insert_space_between_empty_brackets);
		}
		scribe.space();
		node.getOperand().accept(this);
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTSimpleTypeConstructorExpression node) {
		IASTDeclSpecifier declSpec = node.getDeclSpecifier();
		declSpec.accept(this);
		IASTInitializer initializer = node.getInitializer();
		initializer.accept(this);
		return PROCESS_SKIP;
	}

	private int visit(IASTContinueStatement node) {
		formatLeadingAttributes(node);
		scribe.printNextToken(Token.t_continue);
		scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon);
		scribe.printTrailingComment();
		return PROCESS_SKIP;
	}

	private int visit(IASTDoStatement node) {
		int token = peekNextToken();
		if (token == Token.t_do) {
			scribe.printNextToken(token);
		}
		final int line = scribe.line;

		final IASTStatement action = node.getBody();
		formatAction(line, action, preferences.brace_position_for_block);

		if (getCurrentPosition() < nodeEndOffset(node)) {
			if (peekNextToken() == Token.t_while) {
				if (preferences.insert_new_line_before_while_in_do_statement) {
					scribe.startNewLine();
				}
				scribe.printNextToken(Token.t_while, preferences.insert_space_after_closing_brace_in_block);
				scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_while);

				if (preferences.insert_space_after_opening_paren_in_while) {
					scribe.space();
				}

				node.getCondition().accept(this);

				scribe.printNextToken(Token.tRPAREN, preferences.insert_space_before_closing_paren_in_while);
			}
			scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon);
		}
		scribe.printTrailingComment();
		return PROCESS_SKIP;
	}

	private int visit(IASTNullStatement node) {
		if (!fHasClauseInitStatement && nodeOffset(node) == getCurrentPosition()) {
			formatAttributes(node, false, false);
			scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon);
			scribe.printTrailingComment();
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTDeclarationStatement node) {
		node.getDeclaration().accept(this);
		if (!fHasClauseInitStatement) {
			scribe.startNewLine();
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTExpressionStatement node) {
		Runnable semicolonFormatter = null;
		semicolonFormatter = new TrailingSemicolonFormatter(node);
		scribe.setTailFormatter(semicolonFormatter);
		node.getExpression().accept(this);
		semicolonFormatter.run();
		scribe.setTailFormatter(null);
		if (!fHasClauseInitStatement) {
			scribe.startNewLine();
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTForStatement node) {
		if (!startsWithMacroExpansion(node)) {
			scribe.printNextToken(Token.t_for);
		}
		final int line = scribe.line;
		IASTStatement initializerStmt = node.getInitializerStatement();
		IASTStatement body = node.getBody();
		Runnable tailFormatter = null;
		if (!doNodesHaveSameOffset(node, initializerStmt)) {
			scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_for);
			fHasClauseInitStatement = true;
			if (preferences.insert_space_after_opening_paren_in_for) {
				scribe.space();
			}
			if (DefaultCodeFormatterConstants.END_OF_LINE.equals(preferences.brace_position_for_block)
					&& body instanceof IASTCompoundStatement && !startsWithMacroExpansion(body)) {
				tailFormatter = new TrailingTokenFormatter(Token.tLBRACE, nodeOffset(body),
						preferences.insert_space_before_opening_brace_in_block, false);
			}
			tailFormatter = new ClosingParensesisTailFormatter(preferences.insert_space_before_closing_paren_in_for,
					tailFormatter);
		}

		initializerStmt.accept(this);
		if (peekNextToken() == Token.tSEMI) {
			scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon_in_for);
		}

		Alignment alignment = scribe.createAlignment(Alignment.FOR, Alignment.M_COMPACT_SPLIT, Alignment.R_OUTERMOST, 2,
				getCurrentPosition());
		scribe.enterAlignment(alignment);

		boolean ok = false;
		do {
			try {
				try {
					scribe.alignFragment(alignment, 0);
					final IASTExpression condition = node.getConditionExpression();
					if (condition != null) {
						if (preferences.insert_space_after_semicolon_in_for) {
							scribe.space();
						}
						condition.accept(this);
					} else if (node instanceof ICPPASTForStatement) {
						final IASTDeclaration conditionDecl = ((ICPPASTForStatement) node).getConditionDeclaration();
						if (conditionDecl != null) {
							if (preferences.insert_space_after_semicolon_in_for) {
								scribe.space();
							}
							conditionDecl.accept(this);
						}
					}
					if (peekNextToken() == Token.tSEMI) {
						scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon_in_for);
					}

					scribe.setTailFormatter(tailFormatter);
					scribe.alignFragment(alignment, 1);
					IASTExpression iterationExpr = node.getIterationExpression();
					if (iterationExpr != null) {
						if (preferences.insert_space_after_semicolon_in_for) {
							scribe.space();
						}
						iterationExpr.accept(this);
					}
					if (tailFormatter != null) {
						scribe.runTailFormatter();
						scribe.setTailFormatter(null);
					}
				} finally {
					fHasClauseInitStatement = false;
				}
				ok = true;
			} catch (AlignmentException e) {
				scribe.redoAlignment(e);
			}
		} while (!ok);
		scribe.exitAlignment(alignment, true);

		if (body instanceof IASTCompoundStatement && !startsWithMacroExpansion(body)) {
			if (enterNode(body)) {
				if (getCurrentPosition() <= nodeOffset(body)) {
					formatLeftCurlyBrace(line, preferences.brace_position_for_block);
				}
				formatBlock((IASTCompoundStatement) body, preferences.brace_position_for_block,
						preferences.insert_space_before_opening_brace_in_block,
						preferences.indent_statements_compare_to_block);
				exitNode(body);
			}
		} else {
			formatAction(line, body, preferences.brace_position_for_block);
		}
		scribe.printTrailingComment();
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTRangeBasedForStatement node) {
		scribe.printNextToken(Token.t_for);
		final int line = scribe.line;
		scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_for);
		fHasClauseInitStatement = true;
		try {
			if (preferences.insert_space_after_opening_paren_in_for) {
				scribe.space();
			}
			IASTDeclaration declaration = node.getDeclaration();
			formatInlineDeclaration(declaration);
			scribe.printNextToken(Token.tCOLON, true /* preferences.insert_space_before_colon_in_for */);
			final IASTInitializerClause initializer = node.getInitializerClause();
			if (true /*preferences.insert_space_after_colon_in_for*/) {
				scribe.space();
			}
			initializer.accept(this);
		} finally {
			fHasClauseInitStatement = false;
		}
		if (peekNextToken() == Token.tRPAREN) {
			scribe.printNextToken(Token.tRPAREN, preferences.insert_space_before_closing_paren_in_for);
		}

		formatAction(line, node.getBody(), preferences.brace_position_for_block);
		return PROCESS_SKIP;
	}

	private void beginIfClause() {
		scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_if);
		if (preferences.insert_space_after_opening_paren_in_if) {
			scribe.space();
		}
	}

	private int visit(IASTIfStatement node) {
		if (!startsWithMacroExpansion(node)) {
			scribe.printNextToken(Token.t_if);
		}
		IASTNode condition = node.getConditionExpression();
		final IASTStatement thenStatement = node.getThenClause();
		final IASTStatement elseStatement = node.getElseClause();

		fExpectSemicolonAfterDeclaration = false;
		try {
			if (node instanceof ICPPASTIfStatement) {
				ICPPASTIfStatement cppIfStatment = (ICPPASTIfStatement) node;
				if (cppIfStatment.isConstexpr()) {
					scribe.space();
					scribe.printNextToken(Token.t_constexpr);
					scribe.space();
				}
				IASTStatement initStatement = cppIfStatment.getInitializerStatement();
				if (initStatement != null) {
					beginIfClause();
					fHasClauseInitStatement = true;
					initStatement.accept(this);
					if (preferences.insert_space_after_semicolon_in_for) {
						scribe.space();
					}
				}
				if (condition == null) {
					condition = ((ICPPASTIfStatement) node).getConditionDeclaration();
				}
			}
			if (condition == null || !doNodesHaveSameOffset(node, condition)) {
				if (!fHasClauseInitStatement) {
					beginIfClause();
				}
				Runnable tailFormatter = null;
				if (DefaultCodeFormatterConstants.END_OF_LINE.equals(preferences.brace_position_for_block)
						&& thenStatement instanceof IASTCompoundStatement && !startsWithMacroExpansion(thenStatement)) {
					tailFormatter = new TrailingTokenFormatter(Token.tLBRACE, nodeOffset(thenStatement),
							preferences.insert_space_before_opening_brace_in_block, false);
				}
				tailFormatter = new ClosingParensesisTailFormatter(preferences.insert_space_before_closing_paren_in_if,
						tailFormatter);
				scribe.setTailFormatter(tailFormatter);
				if (condition == null || condition instanceof IASTProblemHolder) {
					scribe.skipToToken(Token.tRPAREN);
				} else {
					condition.accept(this);
				}
				scribe.runTailFormatter();
				scribe.setTailFormatter(null);
			} else if (!(condition instanceof IASTProblemHolder)) {
				condition.accept(this);
			}
		} finally {
			fHasClauseInitStatement = false;
			fExpectSemicolonAfterDeclaration = true;
		}

		boolean thenStatementIsBlock = false;
		if (thenStatement != null) {
			if (condition != null && doNodeLocationsOverlap(condition, thenStatement)) {
				thenStatement.accept(this);
			} else if (thenStatement instanceof IASTCompoundStatement && !startsWithMacroExpansion(thenStatement)) {
				final IASTCompoundStatement block = (IASTCompoundStatement) thenStatement;
				thenStatementIsBlock = true;
				final List<IASTStatement> statements = Arrays.asList(block.getStatements());
				if (isGuardClause(block, statements) && elseStatement == null
						&& preferences.keep_guardian_clause_on_one_line) {
					// Specific formatting for guard clauses. A guard clause is a block
					// with a single return or throw statement.
					if (getCurrentPosition() <= nodeOffset(thenStatement)) {
						scribe.printNextToken(Token.tLBRACE, preferences.insert_space_before_opening_brace_in_block);
						scribe.space();
					}
					statements.get(0).accept(this);
					scribe.printNextToken(Token.tRBRACE, true);
					scribe.printTrailingComment();
				} else {
					if (getCurrentPosition() <= nodeOffset(thenStatement)) {
						formatLeftCurlyBrace(scribe.line, preferences.brace_position_for_block);
					}
					thenStatement.accept(this);
					if (elseStatement != null && preferences.insert_new_line_before_else_in_if_statement) {
						scribe.startNewLine();
					}
				}
			} else {
				if (doNodesHaveSameOffset(node, thenStatement)) {
					enterNode(thenStatement);
				}
				if (elseStatement == null && preferences.keep_simple_if_on_one_line) {
					Alignment compactIfAlignment = scribe.createAlignment(Alignment.COMPACT_IF,
							preferences.alignment_for_compact_if, Alignment.R_OUTERMOST, 1, getCurrentPosition(), 1,
							false);
					scribe.enterAlignment(compactIfAlignment);
					boolean ok = false;
					do {
						try {
							scribe.alignFragment(compactIfAlignment, 0);
							scribe.space();
							thenStatement.accept(this);
							ok = true;
						} catch (AlignmentException e) {
							scribe.redoAlignment(e);
						}
					} while (!ok);
					scribe.exitAlignment(compactIfAlignment, true);
				} else if (preferences.keep_then_statement_on_same_line) {
					scribe.space();
					thenStatement.accept(this);
					if (elseStatement != null) {
						scribe.startNewLine();
					}
				} else if (thenStatement instanceof IASTCompoundStatement && !enclosedInMacroExpansion(thenStatement)) {
					thenStatement.accept(this);
				} else {
					scribe.printTrailingComment();
					scribe.startNewLine();
					scribe.indent();
					thenStatement.accept(this);
					if (elseStatement != null) {
						scribe.startNewLine();
					}
					scribe.unIndent();
				}
			}
		}

		if (elseStatement != null) {
			if (condition != null && doNodeLocationsOverlap(condition, elseStatement)) {
				elseStatement.accept(this);
			} else {
				if (peekNextToken() == Token.t_else) {
					if (thenStatementIsBlock) {
						scribe.printNextToken(Token.t_else, preferences.insert_space_after_closing_brace_in_block);
					} else {
						scribe.printNextToken(Token.t_else, true);
					}
				}

				if (elseStatement instanceof IASTCompoundStatement && !enclosedInMacroExpansion(elseStatement)) {
					elseStatement.accept(this);
				} else if (elseStatement instanceof IASTIfStatement) {
					if (!preferences.compact_else_if) {
						scribe.startNewLine();
						scribe.indent();
					}
					scribe.space();
					elseStatement.accept(this);
					if (!preferences.compact_else_if) {
						scribe.unIndent();
					}
				} else if (preferences.keep_else_statement_on_same_line) {
					scribe.space();
					elseStatement.accept(this);
				} else {
					scribe.startNewLine();
					scribe.indent();
					elseStatement.accept(this);
					scribe.unIndent();
				}
			}
		}
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTQualifiedName node) {
		if (node.isFullyQualified()) {
			scribe.printNextToken(Token.tCOLONCOLON);
		}
		for (ICPPASTNameSpecifier nameSpec : node.getQualifier()) {
			nameSpec.accept(this);
			if (peekNextToken() == Token.tCOLONCOLON)
				scribe.printNextToken(Token.tCOLONCOLON);
		}
		if (peekNextToken() == Token.tCOMPL) {
			// destructor
			scribe.printNextToken(Token.tCOMPL, false);
		}
		node.getLastName().accept(this);
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTTemplateId node) {
		IASTName name = node.getTemplateName();
		name.accept(this);
		if (peekNextToken() == Token.tLT) {
			char[] simpleId = name.getSimpleID();
			if (simpleId[simpleId.length - 1] == '<')
				scribe.printNextToken(Token.tLT, true);
			else
				scribe.printNextToken(Token.tLT,
						preferences.insert_space_before_opening_angle_bracket_in_template_arguments);
			if (preferences.insert_space_after_opening_angle_bracket_in_template_arguments) {
				scribe.space();
			}
		}
		final IASTNode[] templateArguments = node.getTemplateArguments();
		if (templateArguments.length > 0) {
			final ListOptions options = new ListOptions(Alignment.M_COMPACT_SPLIT);
			options.fSpaceAfterSeparator = preferences.insert_space_after_comma_in_template_arguments;
			options.fSpaceBeforeSeparator = preferences.insert_space_before_comma_in_template_arguments;
			options.fTieBreakRule = Alignment.R_OUTERMOST;
			formatList(Arrays.asList(templateArguments), options, false, false, null);
		}
		if (peekNextToken() == Token.tSHIFTR) {
			scribe.printComment();
			if (preferences.insert_space_before_closing_angle_bracket_in_template_arguments) {
				scribe.space();
			}
			return PROCESS_SKIP;
		}

		int nextToken = peekNextToken();

		if (nextToken == Token.tGT)
			scribe.printNextToken(Token.tGT,
					preferences.insert_space_before_closing_angle_bracket_in_template_arguments);

		nextToken = peekNextToken();
		if (node.getPropertyInParent() != ICPPASTQualifiedName.SEGMENT_NAME || nextToken == Token.tGT) {
			if (nextToken == Token.tLPAREN) {
				if (preferences.insert_space_before_opening_paren_in_method_invocation)
					scribe.space();
			} else if (preferences.insert_space_after_closing_angle_bracket_in_template_arguments) {
				// Avoid explicit space if followed by '*' or '&'.
				if (nextToken != Token.tSTAR && nextToken != Token.tAMPER)
					scribe.space();
			} else {
				scribe.printComment();
				scribe.needSpace = false;
				scribe.pendingSpace = false;
			}
		}
		return PROCESS_SKIP;
	}

	@Override
	public int visit(ICPPASTClassVirtSpecifier node) {
		if (node.getKind() == ICPPASTClassVirtSpecifier.SpecifierKind.Final) {
			scribe.printNextToken(Token.t_final);
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTReturnStatement node) {
		formatLeadingAttributes(node);
		scribe.printNextToken(Token.t_return);
		final IASTExpression expression = node.getReturnValue();
		if (expression != null) {
			scribe.space();
			expression.accept(this);
		}
		// Sometimes the return expression is null, when it should not be.
		if (expression == null && peekNextToken() != Token.tSEMI) {
			scribe.skipToToken(Token.tSEMI);
		}
		if (peekNextToken() == Token.tSEMI) {
			scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon);
			scribe.printTrailingComment();
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTLabelStatement node) {
		int indentationLevel = scribe.indentationLevel;
		if (!preferences.indent_label_compare_to_statements) {
			scribe.indentationLevel = 0;
		}
		formatLeadingAttributes(node);
		node.getName().accept(this);
		scribe.printNextToken(Token.tCOLON, preferences.insert_space_before_colon_in_labeled_statement);
		if (preferences.insert_space_after_colon_in_labeled_statement) {
			scribe.space();
		}
		if (preferences.insert_new_line_after_label) {
			scribe.startNewLine();
		}
		scribe.indentationLevel = indentationLevel;
		node.getNestedStatement().accept(this);
		return PROCESS_SKIP;
	}

	private int visit(IASTCaseStatement node) {
		IASTExpression constant = node.getExpression();
		formatLeadingAttributes(node);
		if (constant == null) {
			scribe.printNextToken(Token.t_default);
			scribe.printNextToken(Token.tCOLON, preferences.insert_space_before_colon_in_default);
		} else {
			scribe.printNextToken(Token.t_case);
			scribe.space();
			constant.accept(this);
			scribe.printNextToken(Token.tCOLON, preferences.insert_space_before_colon_in_case);
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTDefaultStatement node) {
		formatLeadingAttributes(node);
		scribe.printNextToken(Token.t_default);
		scribe.printNextToken(Token.tCOLON, preferences.insert_space_before_colon_in_default);
		return PROCESS_SKIP;
	}

	private int visit(IASTGotoStatement node) {
		formatLeadingAttributes(node);
		formatRaw(node);
		return PROCESS_SKIP;
	}

	private void beginSwitchClause() {
		scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_switch);
		if (preferences.insert_space_after_opening_paren_in_switch) {
			scribe.space();
		}
	}

	private int visit(IASTSwitchStatement node) {
		final int headerIndent = scribe.numberOfIndentations;
		formatLeadingAttributes(node);
		// 'switch' keyword
		if (!startsWithMacroExpansion(node)) {
			scribe.printNextToken(Token.t_switch);
		}
		IASTNode controller = node.getControllerExpression();
		try {
			// optional init-statement
			if (node instanceof ICPPASTSwitchStatement) {
				ICPPASTSwitchStatement cppSwitchStatement = ((ICPPASTSwitchStatement) node);
				IASTStatement initStatement = cppSwitchStatement.getInitializerStatement();
				if (initStatement != null) {
					beginSwitchClause();
					fHasClauseInitStatement = true;
					initStatement.accept(this);
					if (preferences.insert_space_after_semicolon_in_for) {
						scribe.space();
					}
				}

				if (controller == null) {
					controller = cppSwitchStatement.getControllerDeclaration();
				}
			}

			// Controller expression
			if (!doNodesHaveSameOffset(node, controller) && !fHasClauseInitStatement) {
				beginSwitchClause();
			}
			controller.accept(this);
			if (peekNextToken() == Token.tRPAREN) {
				scribe.printNextToken(Token.tRPAREN, preferences.insert_space_before_closing_paren_in_switch);
			}
		} finally {
			fHasClauseInitStatement = false;
		}
		// switch body
		String brace_position = preferences.brace_position_for_switch;
		boolean hasOpenBrace = false;
		int braceIndent = -1;
		IASTStatement bodyStmt = node.getBody();
		if (!startsWithMacroExpansion(bodyStmt)) {
			boolean insertSpaceBeforeOpeningBrace = preferences.insert_space_before_opening_brace_in_switch;
			formatAttributes(bodyStmt, insertSpaceBeforeOpeningBrace, false);
			hasOpenBrace = peekNextToken() == Token.tLBRACE;
			formatOpeningBrace(brace_position, insertSpaceBeforeOpeningBrace);
			scribe.startNewLine();
			braceIndent = scribe.numberOfIndentations;
			if (braceIndent > headerIndent) {
				scribe.unIndent();
			}
			if (preferences.indent_switchstatements_compare_to_switch) {
				scribe.indent();
			}
		}
		final List<IASTStatement> statements;
		if (bodyStmt instanceof IASTCompoundStatement) {
			statements = Arrays.asList(((IASTCompoundStatement) bodyStmt).getStatements());
		} else {
			statements = Collections.singletonList(bodyStmt);
		}
		if (!enterNode(bodyStmt)) {
			return PROCESS_SKIP;
		}
		final int statementsLength = statements.size();
		if (statementsLength != 0) {
			boolean wasACase = false;
			boolean wasAStatement = false;
			for (int i = 0; i < statementsLength; i++) {
				final IASTStatement statement = statements.get(i);
				if (doNodeLocationsOverlap(controller, statement)) {
					statement.accept(this);
					continue;
				}
				if (statement instanceof IASTCaseStatement || statement instanceof IASTDefaultStatement) {
					if (wasACase) {
						scribe.startNewLine();
					}
					if ((wasACase || wasAStatement) && preferences.indent_switchstatements_compare_to_cases) {
						scribe.unIndent();
					}
					statement.accept(this);
					scribe.printTrailingComment();
					wasACase = true;
					wasAStatement = false;
					if (preferences.indent_switchstatements_compare_to_cases) {
						scribe.indent();
					}
				} else if (statement instanceof IASTBreakStatement) {
					if (preferences.indent_breaks_compare_to_cases) {
						if (!preferences.indent_switchstatements_compare_to_cases) {
							scribe.indent();
						}
					} else {
						if ((wasACase || wasAStatement) && preferences.indent_switchstatements_compare_to_cases) {
							scribe.unIndent();
						}
					}
					if (wasACase) {
						scribe.startNewLine();
					}
					statement.accept(this);
					if (preferences.indent_breaks_compare_to_cases) {
						if (!preferences.indent_switchstatements_compare_to_cases) {
							scribe.unIndent();
						}
					} else if (preferences.indent_switchstatements_compare_to_cases) {
						scribe.indent();
					}
					wasACase = false;
					wasAStatement = true;
				} else if (statement instanceof IASTCompoundStatement && !startsWithMacroExpansion(statement)) {
					String bracePosition;
					if (wasACase) {
						if (preferences.indent_switchstatements_compare_to_cases) {
							scribe.unIndent();
						}
						bracePosition = preferences.brace_position_for_block_in_case;
						try {
							enterNode(statement);
							formatBlock((IASTCompoundStatement) statement, bracePosition,
									preferences.insert_space_after_colon_in_case,
									preferences.indent_statements_compare_to_block);
							exitNode(statement);
						} catch (ASTProblemException e) {
							if (i < statementsLength - 1) {
								final IASTStatement nextStatement = statements.get(i + 1);
								skipToNode(nextStatement);
							}
							exitNode(statement);
						}
						if (preferences.indent_switchstatements_compare_to_cases) {
							scribe.indent();
						}
					} else {
						bracePosition = preferences.brace_position_for_block;
						try {
							enterNode(statement);
							formatBlock((IASTCompoundStatement) statement, bracePosition,
									preferences.insert_space_before_opening_brace_in_block,
									preferences.indent_statements_compare_to_block);
							exitNode(statement);
						} catch (ASTProblemException e) {
							if (i < statementsLength - 1) {
								final IASTStatement nextStatement = statements.get(i + 1);
								skipToNode(nextStatement);
							}
							exitNode(statement);
						}
					}
					wasAStatement = true;
					wasACase = false;
				} else {
					scribe.startNewLine();
					try {
						statement.accept(this);
					} catch (ASTProblemException e) {
						if (i < statementsLength - 1) {
							final IASTStatement nextStatement = statements.get(i + 1);
							skipToNode(nextStatement);
						}
					}
					wasAStatement = true;
					wasACase = false;
				}
				if (!wasACase) {
					scribe.startNewLine();
				}
				scribe.printComment();
			}
			if ((wasACase || wasAStatement) && preferences.indent_switchstatements_compare_to_cases) {
				scribe.unIndent();
			}
		}

		if (!startsWithMacroExpansion(bodyStmt)) {
			if (preferences.indent_switchstatements_compare_to_switch) {
				scribe.unIndent();
			}
			if (scribe.numberOfIndentations < braceIndent) {
				scribe.indent();
			}
			scribe.startNewLine();

			if (hasOpenBrace)
				formatClosingBrace(brace_position);
		}
		exitNode(bodyStmt);
		return PROCESS_SKIP;
	}

	private int visit(IASTWhileStatement node) {
		scribe.printNextToken(Token.t_while);
		final int line = scribe.line;
		scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_while);

		if (preferences.insert_space_after_opening_paren_in_while) {
			scribe.space();
		}
		final IASTExpression condition = node.getCondition();
		if (condition != null) {
			condition.accept(this);
		} else if (node instanceof ICPPASTWhileStatement) {
			final IASTDeclaration conditionDecl = ((ICPPASTWhileStatement) node).getConditionDeclaration();
			if (conditionDecl != null) {
				conditionDecl.accept(this);
			}
		}
		if (peekNextToken() == Token.tRPAREN) {
			scribe.printNextToken(Token.tRPAREN, preferences.insert_space_before_closing_paren_in_while);
		}
		formatAction(line, node.getBody(), preferences.brace_position_for_block);
		return PROCESS_SKIP;
	}

	private int visit(IASTProblemStatement node) {
		throw new ASTProblemException(node.getProblem());
	}

	private int visit(IASTProblemExpression node) {
		throw new ASTProblemException(node.getProblem());
	}

	private int visit(IASTProblemDeclaration node) {
		throw new ASTProblemException(node.getProblem());
	}

	private void formatRaw(IASTNode node) {
		scribe.printComment();
		skipNode(node);
	}

	private void exitAlignments() {
		while (scribe.currentAlignment != null) {
			scribe.exitAlignment(scribe.currentAlignment, true);
		}
	}

	/**
	 * Test whether the next node location is inside a macro expansion. If it is
	 * a macro expansion, formatting will be skipped until the next node outside
	 * the expansion is reached.
	 *
	 * @param node the AST node to be tested
	 * @return <code>false</code> if the node should be skipped
	 */
	private boolean enterNode(IASTNode node) {
		int currentPosition = getCurrentPosition();
		IASTFileLocation nodeLocation = getFileLocation(node);
		int nodeEndOffset = -1;
		if (nodeLocation != null) {
			nodeEndOffset = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
			if (currentPosition > nodeEndOffset)
				return false; // We have already passed the end of the node.
		}
		scribe.enterNode();
		if (node instanceof IASTProblemHolder)
			return false;
		IASTNodeLocation[] locations = node.getNodeLocations();
		if (locations.length == 0) {
			return true;
		} else if (!fInsideMacroArguments && locations[0] instanceof IASTMacroExpansionLocation) {
			IASTMacroExpansionLocation location = (IASTMacroExpansionLocation) locations[0];
			if (locations.length <= 2 && (node instanceof IASTStatement || node instanceof IASTExpression)) {
				IASTPreprocessorMacroExpansion macroExpansion = location.getExpansion();
				IASTFileLocation macroLocation = macroExpansion.getFileLocation();
				int macroOffset = macroLocation.getNodeOffset();
				if (macroOffset >= currentPosition && !scribe.shouldSkip(macroOffset)
						&& (nodeEndOffset == macroOffset + macroLocation.getNodeLength()
								|| locations.length == 2 && isSemicolonLocation(locations[1]))
						&& isFunctionStyleMacroExpansion(macroExpansion)) {
					if (locations.length == 2 && isSemicolonLocation(locations[1])) {
						scribe.setTailFormatter(new TrailingTokenFormatter(Token.tSEMI, locations[1].getNodeOffset(),
								preferences.insert_space_before_semicolon, false));
					}
					formatFunctionStyleMacroExpansion(macroExpansion);
					return false;
				}
			}
			IASTFileLocation expansionLocation = location.asFileLocation();
			int startOffset = expansionLocation.getNodeOffset();
			int endOffset = startOffset + expansionLocation.getNodeLength();
			scribe.skipRange(startOffset, endOffset);
			if (locations.length == 1 && endOffset <= currentPosition) {
				scribe.restartAtOffset(endOffset);
				continueNode(node.getParent());
				return false;
			}
		} else if (nodeLocation != null) {
			scribe.restartAtOffset(nodeLocation.getNodeOffset());
		}
		return true;
	}

	private IASTFileLocation getFileLocation(IASTNode node) {
		return fInsideMacroArguments ? ((ASTNode) node).getImageLocation() : node.getFileLocation();
	}

	/**
	 * Formatting of node is complete. Undo skip region if any.
	 *
	 * @param node
	 */
	private void exitNode(IASTNode node) {
		if (node instanceof IASTProblemHolder) {
			return;
		}
		if (scribe.skipRange()) {
			IASTFileLocation fileLocation = getFileLocation(node);
			if (fileLocation != null) {
				int nodeEndOffset = fileLocation.getNodeOffset() + fileLocation.getNodeLength();
				scribe.restartAtOffset(nodeEndOffset);
			}
		} else if (scribe.currentAlignmentException == null) {
			// print rest of node if any
			skipNode(node);
		}
		continueNode(node.getParent());
	}

	/**
	 * Formatting of node continues after completion of a child node. Establish next skip region.
	 *
	 * @param node
	 */
	private void continueNode(IASTNode node) {
		if (node instanceof IASTProblemHolder || node instanceof IASTTranslationUnit) {
			return;
		}
		IASTFileLocation fileLocation = getFileLocation(node);
		if (fileLocation == null) {
			return;
		}
		int nodeOffset = fileLocation.getNodeOffset();
		int nodeEndOffset = nodeOffset + fileLocation.getNodeLength();
		int currentOffset = getCurrentPosition();
		if (currentOffset > nodeEndOffset) {
			return;
		}
		if (!fInsideMacroArguments) {
			IASTNodeLocation[] locations = node.getNodeLocations();
			for (int i = 0; i < locations.length; i++) {
				IASTNodeLocation nodeLocation = locations[i];
				if (nodeLocation instanceof IASTMacroExpansionLocation) {
					IASTFileLocation expansionLocation = nodeLocation.asFileLocation();
					int startOffset = expansionLocation.getNodeOffset();
					int endOffset = startOffset + expansionLocation.getNodeLength();
					if (currentOffset <= startOffset) {
						break;
					}
					if (currentOffset < endOffset || currentOffset == endOffset && i == locations.length - 1) {
						scribe.skipRange(startOffset, endOffset);
						break;
					}
				}
			}
		}
	}

	private int getNextTokenOffset() {
		localScanner.resetTo(getCurrentPosition(), scribe.scannerEndPosition);
		localScanner.getNextToken();
		return localScanner.getCurrentTokenStartPosition();
	}

	private void skipNode(IASTNode node) {
		final IASTNodeLocation fileLocation = getFileLocation(node);
		if (fileLocation != null && fileLocation.getNodeLength() > 0) {
			final int endOffset = fileLocation.getNodeOffset() + fileLocation.getNodeLength();
			final int currentOffset = getCurrentPosition();
			final int restLength = endOffset - currentOffset;
			if (restLength > 0) {
				scribe.printRaw(currentOffset, restLength);
			}
		}
	}

	private void skipToNode(IASTNode node) {
		final IASTNodeLocation fileLocation = getFileLocation(node);
		if (fileLocation != null) {
			final int startOffset = fileLocation.getNodeOffset();
			final int currentOffset = getCurrentPosition();
			final int restLength = startOffset - currentOffset;
			if (restLength > 0) {
				scribe.printRaw(currentOffset, restLength);
			}
		}
	}

	private void skipNonWhitespaceToNode(IASTNode node) {
		final IASTNodeLocation fileLocation = getFileLocation(node);
		if (fileLocation != null) {
			final int startOffset = fileLocation.getNodeOffset();
			final int nextTokenOffset = getNextTokenOffset();
			if (nextTokenOffset < startOffset) {
				final int currentOffset = getCurrentPosition();
				final int restLength = startOffset - currentOffset;
				if (restLength > 0) {
					scribe.printRaw(currentOffset, restLength);
				}
			}
		}
	}

	/**
	 * Format an expression nested in parenthesis. If the operand is
	 * <code>null</code>, empty parenthesis are expected.
	 *
	 * @param operand
	 */
	private void formatParenthesizedExpression(final IASTExpression operand) {
		scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_parenthesized_expression);
		if (preferences.insert_space_after_opening_paren_in_parenthesized_expression) {
			scribe.space();
		}
		Runnable tailFormatter = scribe.takeTailFormatter();
		try {
			if (operand != null) {
				operand.accept(this);
			}
		} finally {
			scribe.setTailFormatter(tailFormatter);
		}
		if (peekNextToken() != Token.tRPAREN) {
			if (!enclosedInMacroExpansion(operand)) {
				scribe.skipToToken(Token.tRPAREN);
			}
		}
		if (peekNextToken() == Token.tRPAREN) {
			scribe.printNextToken(Token.tRPAREN,
					preferences.insert_space_before_closing_paren_in_parenthesized_expression);
		}
	}

	private void formatAction(final int line, final IASTStatement stmt, String brace_position) {
		if (stmt != null) {
			if (stmt instanceof IASTCompoundStatement && !startsWithMacroExpansion(stmt)) {
				formatLeftCurlyBrace(line, brace_position);
				if (enterNode(stmt)) {
					formatBlock((IASTCompoundStatement) stmt, brace_position,
							preferences.insert_space_before_opening_brace_in_block,
							preferences.indent_statements_compare_to_block);
					exitNode(stmt);
				}
			} else if (stmt instanceof IASTNullStatement) {
				scribe.indent();
				if (preferences.put_empty_statement_on_new_line) {
					scribe.startNewLine();
				}
				stmt.accept(this);
				scribe.unIndent();
			} else {
				// Don't insert a line break if we have already passed the start of the statement.
				// This is possible with macro expansions.
				boolean indented = false;
				if (getCurrentPosition() <= nodeOffset(stmt)) {
					scribe.printTrailingComment();
					scribe.startNewLine();
					scribe.indent();
					indented = true;
				}
				stmt.accept(this);
				if (indented)
					scribe.unIndent();
			}
		}
	}

	private boolean isEmptyStatement(IASTNode node) {
		IASTNodeLocation[] locations = node.getNodeLocations();
		if (locations.length == 1 && node instanceof ASTNode) {
			ASTNode statement = (ASTNode) node;
			IASTFileLocation fileLocation = locations[0].asFileLocation();
			return fileLocation.getNodeLength() == 1 && fileLocation.getNodeOffset() == statement.getOffset();
		}
		return false;
	}

	private boolean startsWithMacroExpansion(IASTNode node) {
		if (fInsideMacroArguments)
			return false;
		IASTNodeLocation[] locations = node.getNodeLocations();
		if (!(node instanceof IASTProblemHolder) && locations.length != 0
				&& locations[0] instanceof IASTMacroExpansionLocation) {
			IASTFileLocation expansionLocation = locations[0].asFileLocation();
			IASTFileLocation fileLocation = getFileLocation(node);
			return expansionLocation.getNodeOffset() == fileLocation.getNodeOffset();
		}
		return false;
	}

	private boolean endsWithMacroExpansion(IASTNode node) {
		if (fInsideMacroArguments)
			return false;
		IASTNodeLocation[] locations = node.getNodeLocations();
		if (!(node instanceof IASTProblemHolder) && locations.length != 0
				&& locations[locations.length - 1] instanceof IASTMacroExpansionLocation) {
			return true;
		}
		return false;
	}

	private boolean enclosedInMacroExpansion(IASTNode node) {
		if (fInsideMacroArguments)
			return false;
		IASTNodeLocation[] locations = node.getNodeLocations();
		return locations.length == 1 && locations[0] instanceof IASTMacroExpansionLocation;
	}

	private boolean withinMacroExpansion(IASTNode node, int offset) {
		if (fInsideMacroArguments)
			return false;
		IASTFileLocation loc = getFileLocation(node);
		if (loc == null || offset < loc.getNodeOffset() || offset >= loc.getNodeOffset() + loc.getNodeLength()) {
			return false;
		}
		IASTNodeLocation[] locations = node.getNodeLocations();
		for (IASTNodeLocation location : locations) {
			IASTFileLocation fileLocation = location.asFileLocation();
			if (fileLocation != null) {
				final int nodeOffset = fileLocation.getNodeOffset();
				final int endOffset = nodeOffset + fileLocation.getNodeLength();
				if (offset >= nodeOffset && offset < endOffset) {
					return location instanceof IASTMacroExpansionLocation;
				}
			}
		}
		return true;
	}

	private int getCurrentPosition() {
		return scribe.scanner.getCurrentPosition();
	}

	/**
	 * Returns <code>true</code> if the given macro expansion is followed by a semicolon on the same
	 * line.
	 */
	private boolean looksLikeStatement(IASTMacroExpansionLocation location) {
		IASTFileLocation fileLocation = location.asFileLocation();
		if (fileLocation == null)
			return false;
		int pos = fileLocation.getNodeOffset() + fileLocation.getNodeLength();
		localScanner.resetTo(pos, scribe.scannerEndPosition);
		Token token = localScanner.nextToken();
		if (token == null || token.getType() != Token.tSEMI)
			return false;
		// Check if the semicolon is on the same line.
		localScanner.resetTo(pos, token.getOffset());
		int c;
		while ((c = localScanner.getNextChar()) != -1) {
			if (c == '\n')
				return false;
		}
		return true;
	}

	/**
	 * Returns true if the two given nodes have overlapping file locations. For nodes that are
	 * normally separated by other tokens this is an indication that they were produced by the same
	 * macro expansion.
	 */
	private boolean doNodeLocationsOverlap(IASTNode node1, IASTNode node2) {
		IASTFileLocation loc1 = getFileLocation(node1);
		IASTFileLocation loc2 = getFileLocation(node2);
		return loc1.getNodeOffset() + loc1.getNodeLength() > loc2.getNodeOffset()
				&& loc1.getNodeOffset() < loc2.getNodeOffset() + loc2.getNodeLength();
	}

	/**
	 * Returns true if the two given nodes have the same offset. For nodes that are normally
	 * separated by other tokens this is an indication that they were produced by the same macro
	 * expansion.
	 */
	private boolean doNodesHaveSameOffset(IASTNode node1, IASTNode node2) {
		return nodeOffset(node1) == nodeOffset(node2);
	}

	private int nodeOffset(IASTNode node) {
		return getFileLocation(node).getNodeOffset();
	}

	private int nodeEndOffset(IASTNode node) {
		IASTFileLocation loc = getFileLocation(node);
		return loc.getNodeOffset() + loc.getNodeLength();
	}

	private void formatBlock(IASTCompoundStatement block, String block_brace_position,
			boolean insertSpaceBeforeOpeningBrace, boolean indentStatements) {
		formatBlockOpening(block, block_brace_position, insertSpaceBeforeOpeningBrace);
		formatOpenedBlock(block, block_brace_position, indentStatements);
	}

	private void formatBlockOpening(IASTCompoundStatement block, String block_brace_position,
			boolean insertSpaceBeforeOpeningBrace) {
		if (startsWithMacroExpansion(block)) {
			if (!looksLikeStatement((IASTMacroExpansionLocation) block.getNodeLocations()[0])) {
				scribe.startNewLine();
				scribe.printComment();
			}
		} else if (getCurrentPosition() <= nodeOffset(block)) {
			formatOpeningBrace(block_brace_position, insertSpaceBeforeOpeningBrace);
		}
	}

	private void formatOpenedBlock(IASTCompoundStatement block, String block_brace_position, boolean indentStatements) {
		final boolean startsWithStatementLikeMacro = startsWithMacroExpansion(block)
				&& looksLikeStatement((IASTMacroExpansionLocation) block.getNodeLocations()[0]);
		final boolean endsWithMacroExpansion = endsWithMacroExpansion(block);
		IASTStatement[] statements = block.getStatements();
		final int statementsLength = statements.length;
		if (statementsLength != 0) {
			if (!startsWithStatementLikeMacro) {
				scribe.startNewLine();
				if (indentStatements)
					scribe.indent();
			}
			formatStatements(Arrays.asList(statements), !endsWithMacroExpansion);
		} else {
			if (!startsWithStatementLikeMacro) {
				if (preferences.insert_new_line_in_empty_block)
					scribe.startNewLine();
				if (indentStatements)
					scribe.indent();
			}
		}
		scribe.printComment();

		if (indentStatements && !startsWithStatementLikeMacro)
			scribe.unIndent();

		if (!endsWithMacroExpansion) {
			formatClosingBrace(block_brace_position);
		} else if (!startsWithMacroExpansion(block)) {
			if (DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(block_brace_position)) {
				scribe.unIndent();
			}
		}
	}

	private void formatLeftCurlyBrace(final int line, final String bracePosition) {
		scribe.formatBrace = true;
		try {
			// Deal with (quite unexpected) comments right before left curly brace.
			scribe.printComment();
			if (DefaultCodeFormatterConstants.NEXT_LINE_ON_WRAP.equals(bracePosition)
					&& (scribe.line > line || scribe.column >= preferences.page_width)) {
				scribe.startNewLine();
			}
		} finally {
			scribe.formatBrace = false;
		}
	}

	private void formatOpeningBrace(String bracePosition, boolean insertSpaceBeforeBrace) {
		if (DefaultCodeFormatterConstants.NEXT_LINE.equals(bracePosition)) {
			scribe.startNewLine();
		} else if (DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(bracePosition)) {
			scribe.startNewLine();
			scribe.indent();
		}
		int token = peekNextToken();
		if (token == Token.tLBRACE) {
			scribe.printNextToken(token, insertSpaceBeforeBrace);
		}
		scribe.printTrailingComment();
	}

	private void formatClosingBrace(String brace_position) {
		int token = peekNextToken();
		if (token == Token.tRBRACE) {
			scribe.printNextToken(token);
		}
		scribe.printTrailingComment();
		if (DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(brace_position)) {
			scribe.unIndent();
		}
	}

	private void formatStatements(final List<IASTStatement> statements, boolean insertNewLineAfterLastStatement) {
		final int statementsLength = statements.size();
		if (statementsLength > 1) {
			IASTStatement firstStatement = statements.get(0);
			try {
				firstStatement.accept(this);
			} catch (ASTProblemException e) {
				skipToNode(statements.get(1));
			}
			final int indentLevel = scribe.indentationLevel;
			for (int i = 1; i < statementsLength - 1; i++) {
				final IASTStatement statement = statements.get(i);
				if ((!(statement instanceof IASTNullStatement) || isEmptyStatement(statement))
						&& !doNodeLocationsOverlap(statement, statements.get(i - 1))) {
					scribe.startNewLine();
				}
				if (!enterNode(statement)) {
					continue;
				}
				try {
					statement.accept(this);
				} catch (RuntimeException e) {
					if (i >= statementsLength - 1) {
						throw e;
					}
					reportFormattingProblem(e);
					exitAlignments();
					skipToNode(statements.get(i + 1));
					while (scribe.indentationLevel < indentLevel) {
						scribe.indent();
					}
					while (scribe.indentationLevel > indentLevel) {
						scribe.unIndent();
					}
				}
			}
			final IASTStatement statement = statements.get(statementsLength - 1);
			if (!(statement instanceof IASTNullStatement) && (!startsWithMacroExpansion(statement)
					|| !looksLikeStatement((IASTMacroExpansionLocation) statement.getNodeLocations()[0]))) {
				scribe.startNewLine();
			}
			statement.accept(this);
		} else {
			final IASTStatement statement = statements.get(0);
			statement.accept(this);
		}
		if (insertNewLineAfterLastStatement) {
			scribe.startNewLine();
		}
	}

	private boolean commentStartsBlock(int start, int end) {
		localScanner.resetTo(start, end);
		if (localScanner.getNextToken() == Token.tLBRACE) {
			switch (localScanner.getNextToken()) {
			case Token.tBLOCKCOMMENT:
			case Token.tLINECOMMENT:
				return true;
			}
		}
		return false;
	}

	private char peekNextChar() {
		if (peekNextToken() != Token.tBADCHAR) {
			char[] text = localScanner.getCurrentTokenSource();
			if (text.length > 0) {
				return text[0];
			}
		}
		return 0;
	}

	private int peekTokenAtPosition(int pos) {
		localScanner.resetTo(pos, scribe.scannerEndPosition);
		int token = localScanner.getNextToken();
		while (token == Token.tBLOCKCOMMENT || token == Token.tLINECOMMENT) {
			token = localScanner.getNextToken();
		}
		return token;
	}

	private int peekNextToken() {
		return peekNextToken(false);
	}

	/**
	 * It returns a position range if offset is included into an inactive
	 * preprocessor region.
	 * @param offset The offset to be checked
	 * @return The region if found, null otherwise
	 */
	private Position getInactivePosAt(int offset) {
		for (Iterator<InactivePosition> iter = fInactivePreprocessorPositions.iterator(); iter.hasNext();) {
			Position pos = iter.next();
			if (pos.includes(offset)) {
				return pos;
			}
		}
		return null;
	}

	private int peekNextToken(boolean ignoreSkip) {
		if (!ignoreSkip && scribe.shouldSkip(getCurrentPosition())) {
			return Token.tBADCHAR;
		}
		localScanner.resetTo(getCurrentPosition(), scribe.scannerEndPosition);
		int token = localScanner.getNextToken();
		int currentStart = localScanner.getCurrentTokenStartPosition();
		Position p = getInactivePosAt(currentStart);
		while ((token == Token.tBLOCKCOMMENT || token == Token.tLINECOMMENT) || p != null) {
			token = localScanner.getNextToken();
			currentStart = localScanner.getCurrentTokenStartPosition();
			p = getInactivePosAt(currentStart);
		}
		return token;
	}

	private boolean isSemicolonLocation(IASTNodeLocation location) {
		return location instanceof IASTFileLocation && location.getNodeLength() == 1
				&& peekTokenAtPosition(location.getNodeOffset()) == Token.tSEMI
				&& localScanner.getCurrentTokenEndPosition() + 1 == location.getNodeOffset() + location.getNodeLength();
	}

	private boolean isSemicolonAtPosition(int pos) {
		return peekTokenAtPosition(pos) == Token.tSEMI && localScanner.getCurrentTokenStartPosition() == pos;
	}

	private boolean isGuardClause(IASTCompoundStatement block, List<IASTStatement> statements) {
		IASTNodeLocation fileLocation = block.getFileLocation();
		if (fileLocation == null) {
			return false;
		}
		int blockStartPosition = nodeOffset(block);
		int blockLength = block.getFileLocation().getNodeLength();
		if (commentStartsBlock(blockStartPosition, blockLength))
			return false;
		final int statementsLength = statements.size();
		if (statementsLength != 1)
			return false;
		if (statements.get(0) instanceof IASTReturnStatement) {
			return true;
		}
		return false;
	}

	/**
	 * Collect source positions of no-format sections in the given translation unit.
	 *
	 * @param translationUnit  the {@link IASTTranslationUnit}, may be <code>null</code>
	 * @return a {@link List} of {@link Position}s
	 */
	private List<InactivePosition> collectNoFormatCodePositions(IASTTranslationUnit translationUnit) {
		if (translationUnit == null || !this.preferences.use_fomatter_comment_tag) {
			return Collections.emptyList();
		}
		String fileName = translationUnit.getFilePath();
		if (fileName == null) {
			return Collections.emptyList();
		}
		List<InactivePosition> positions = new ArrayList<>();
		int inactiveCodeStart = -1;
		boolean inInactiveCode = false;

		IASTComment[] commentsStmts = translationUnit.getComments();

		for (IASTComment commentStmt : commentsStmts) {
			IASTComment statement = commentStmt;
			if (!statement.isPartOfTranslationUnitFile()) {
				// comment is from a different file
				continue;
			}
			IASTNodeLocation nodeLocation = statement.getFileLocation();
			if (nodeLocation == null) {
				continue;
			}

			String comment = new String(statement.getComment());
			/**
			 * According to JDT formatter rules, we need to evaluate the latest tag if both
			 * are defined at the same time in the comment.
			 */
			int offPos = comment.lastIndexOf(this.preferences.comment_formatter_off_tag);
			int onPos = comment.lastIndexOf(this.preferences.comment_formatter_on_tag);
			if (offPos != -1 && offPos > onPos) {
				if (!inInactiveCode) {
					inactiveCodeStart = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
					inInactiveCode = true;
				}
			} else if (onPos != -1 && onPos > offPos) {
				if (inInactiveCode) {
					int inactiveCodeEnd = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
					positions.add(new InactivePosition(inactiveCodeStart, inactiveCodeEnd - inactiveCodeStart, false));
				}
				inInactiveCode = false;
			}
		}
		if (inInactiveCode) {
			positions.add(
					new InactivePosition(inactiveCodeStart, translationUnit.getFileLocation().getNodeLength(), false));
			inInactiveCode = false;
		}
		return positions;
	}

	/**
	 * Collect source positions of preprocessor-hidden branches
	 * in the given translation unit.
	 *
	 * @param translationUnit  the {@link IASTTranslationUnit}, may be <code>null</code>
	 * @return a {@link List} of {@link Position}s
	 */
	private static List<InactivePosition> collectInactiveCodePositions(IASTTranslationUnit translationUnit) {
		if (translationUnit == null) {
			return Collections.emptyList();
		}
		String fileName = translationUnit.getFilePath();
		if (fileName == null) {
			return Collections.emptyList();
		}
		List<InactivePosition> positions = new ArrayList<>();
		int inactiveCodeStart = -1;
		boolean inInactiveCode = false;
		Stack<Boolean> inactiveCodeStack = new Stack<>();

		IASTPreprocessorStatement[] preprocStmts = translationUnit.getAllPreprocessorStatements();

		for (IASTPreprocessorStatement preprocStmt : preprocStmts) {
			IASTPreprocessorStatement statement = preprocStmt;
			if (!statement.isPartOfTranslationUnitFile()) {
				// preprocessor directive is from a different file
				continue;
			}
			IASTNodeLocation nodeLocation = statement.getFileLocation();
			if (nodeLocation == null) {
				continue;
			}
			if (statement instanceof IASTPreprocessorIfStatement) {
				IASTPreprocessorIfStatement ifStmt = (IASTPreprocessorIfStatement) statement;
				inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
				if (!ifStmt.taken()) {
					if (!inInactiveCode) {
						inactiveCodeStart = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
						inInactiveCode = true;
					}
				}
			} else if (statement instanceof IASTPreprocessorIfdefStatement) {
				IASTPreprocessorIfdefStatement ifdefStmt = (IASTPreprocessorIfdefStatement) statement;
				inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
				if (!ifdefStmt.taken()) {
					if (!inInactiveCode) {
						inactiveCodeStart = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
						inInactiveCode = true;
					}
				}
			} else if (statement instanceof IASTPreprocessorIfndefStatement) {
				IASTPreprocessorIfndefStatement ifndefStmt = (IASTPreprocessorIfndefStatement) statement;
				inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
				if (!ifndefStmt.taken()) {
					if (!inInactiveCode) {
						inactiveCodeStart = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
						inInactiveCode = true;
					}
				}
			} else if (statement instanceof IASTPreprocessorElseStatement) {
				IASTPreprocessorElseStatement elseStmt = (IASTPreprocessorElseStatement) statement;
				if (!elseStmt.taken() && !inInactiveCode) {
					inactiveCodeStart = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
					inInactiveCode = true;
				} else if (elseStmt.taken() && inInactiveCode) {
					int inactiveCodeEnd = nodeLocation.getNodeOffset();
					positions.add(new InactivePosition(inactiveCodeStart, inactiveCodeEnd - inactiveCodeStart, true));
					inInactiveCode = false;
				}
			} else if (statement instanceof IASTPreprocessorElifStatement) {
				IASTPreprocessorElifStatement elifStmt = (IASTPreprocessorElifStatement) statement;
				if (!elifStmt.taken() && !inInactiveCode) {
					inactiveCodeStart = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
					inInactiveCode = true;
				} else if (elifStmt.taken() && inInactiveCode) {
					int inactiveCodeEnd = nodeLocation.getNodeOffset();
					positions.add(new InactivePosition(inactiveCodeStart, inactiveCodeEnd - inactiveCodeStart, true));
					inInactiveCode = false;
				}
			} else if (statement instanceof IASTPreprocessorEndifStatement) {
				try {
					boolean wasInInactiveCode = inactiveCodeStack.pop().booleanValue();
					if (inInactiveCode && !wasInInactiveCode) {
						int inactiveCodeEnd = nodeLocation.getNodeOffset();
						positions.add(
								new InactivePosition(inactiveCodeStart, inactiveCodeEnd - inactiveCodeStart, true));
					}
					inInactiveCode = wasInInactiveCode;
				} catch (EmptyStackException e) {
				}
			}
		}
		if (inInactiveCode) {
			// handle dangling #if?
		}
		return positions;
	}

	private boolean hasMemberInitializers(IASTFunctionDefinition node) {
		return node instanceof ICPPASTFunctionDefinition
				&& ((ICPPASTFunctionDefinition) node).getMemberInitializers().length > 0;
	}

	private int findTokenWithinNode(int tokenType, IASTNode node) {
		IASTFileLocation location = getFileLocation(node);
		int endOffset = location.getNodeOffset() + location.getNodeLength();
		return scribe.findToken(tokenType, endOffset);
	}

	private int findTokenAfterNodeOrTokenRange(int tokenType, Object nodeOrTokenRange) {
		int startOffset;
		if (nodeOrTokenRange instanceof IASTNode) {
			IASTFileLocation location = getFileLocation((IASTNode) nodeOrTokenRange);
			startOffset = location.getNodeOffset() + location.getNodeLength();
		} else {
			startOffset = ((TokenRange) nodeOrTokenRange).getEndOffset();
		}
		return scribe.findToken(tokenType, startOffset, scribe.scannerEndPosition - 1);
	}
}

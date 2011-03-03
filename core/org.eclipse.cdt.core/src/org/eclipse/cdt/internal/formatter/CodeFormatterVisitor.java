/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.util.List;
import java.util.Stack;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
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
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionWithTryBlock;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.parser.IToken;
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
	private static boolean DEBUG = "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.cdt.core/debug/formatter")); //$NON-NLS-1$ //$NON-NLS-2$

	private static class ASTProblemException extends RuntimeException {
		ASTProblemException(IASTProblem problem) {
			super(problem.getMessageWithLocation());
		}
	}

	private static class ListOptions {
		final int fMode;
		boolean fUseFallbackMode;
		boolean fSpaceBeforeComma;
		boolean fSpaceAfterComma = true;
		boolean fSpaceAfterOpeningParen;
		boolean fSpaceBeforeClosingParen;
		boolean fSpaceBetweenEmptyParen;
		boolean fSpaceBeforeOpeningParen;
		int fContinuationIndentation = -1;
		int fTieBreakRule = Alignment.R_INNERMOST;

		ListOptions(int mode) {
			this.fMode = mode;
		}
	}

	/*
	 * Formats a given token at a given position.
	 * @see #formatList(List, ListOptions, boolean, boolean, Runnable)
	 */
	class TrailingTokenFormatter implements Runnable {
		private final int tokenType;
		private final int tokenPosition;
		private final boolean spaceBeforeToken;
		private final boolean spaceAfterToken;

		TrailingTokenFormatter(int tokenType, int tokenPosition,
				boolean spaceBeforeToken, boolean spaceAfterToken) {
			this.tokenType = tokenType;
			this.tokenPosition = tokenPosition;
			this.spaceBeforeToken = spaceBeforeToken;
			this.spaceAfterToken = spaceAfterToken;
		}

		public void run() {
			int offset = scribe.scanner.getCurrentPosition();
			if (tokenPosition < 0 || offset > tokenPosition)
				return;
			if (offset < tokenPosition)
				scribe.restartAtOffset(tokenPosition);
			int token= peekNextToken();
			if (token == tokenType) {
				if (scribe.pendingSpace) {
					scribe.pendingSpace = false;
					scribe.needSpace = true;
				}
				scribe.printNextToken(tokenType, spaceBeforeToken);
				scribe.printTrailingComment();
				if (spaceAfterToken) {
					scribe.space();
				}
			}
		}
	}

	/*
	 * Formats a trailing comma.
	 * @see #formatList(List, ListOptions, boolean, boolean, Runnable)
	 */
	class TrailingCommaFormatter extends TrailingTokenFormatter {
		TrailingCommaFormatter(boolean spaceBeforeComma, boolean spaceAfterComma) {
			super(Token.tCOMMA, scribe.findToken(Token.tCOMMA), spaceBeforeComma, spaceAfterComma);
		}
	}

	/*
	 * Formats a trailing semicolon.
	 * @see #formatList(List, ListOptions, boolean, boolean, Runnable)
	 */
	class TrailingSemicolonFormatter extends TrailingTokenFormatter {
		TrailingSemicolonFormatter(IASTNode node) {
			super(Token.tSEMI, getLastNodeCharacterPosition(node),
					fInsideFor ? preferences.insert_space_before_semicolon_in_for :
								 preferences.insert_space_before_semicolon,
					false);
		}
	}

	/*
	 * Formats the part of a function declaration following the parameter list.
	 * @see #formatList(List, ListOptions, boolean, boolean, Runnable)
	 */
	public class CPPFunctionDeclaratorTailFormatter implements Runnable {
		private final ICPPASTFunctionDeclarator node;
		private final Runnable continuationFormatter;

		public CPPFunctionDeclaratorTailFormatter(ICPPASTFunctionDeclarator node,
				Runnable tailFormatter) {
			this.node = node;
			this.continuationFormatter = tailFormatter;
		}

		public void run() {
			boolean needSpace = skipConstVolatileRestrict();
			if (node.getExceptionSpecification() != null && peekNextToken() == Token.t_throw)
				return;
			// Skip the rest (=0)
			if (needSpace && scribe.printComment()) {
				scribe.space();
			}
			skipNode(node);
			if (continuationFormatter != null)
				continuationFormatter.run();
		}
	}

	public class ClosingParensesisTailFormatter implements Runnable {
		private final boolean spaceBeforeClosingParen;
		private final Runnable continuationFormatter;
		private final int parenPosition;

		public ClosingParensesisTailFormatter(boolean spaceBeforeClosingParen,
				Runnable tailFormatter) {
			this.spaceBeforeClosingParen = spaceBeforeClosingParen;
			this.continuationFormatter = tailFormatter;
			this.parenPosition = scribe.findToken(Token.tRPAREN);
		}

		public void run() {
			int offset = scribe.scanner.getCurrentPosition();
			if (parenPosition >= 0 && offset <= parenPosition) {
				if (offset < parenPosition)
					scribe.restartAtOffset(parenPosition);
				if (scribe.pendingSpace) {
					scribe.pendingSpace = false;
					scribe.needSpace = true;
				}
				scribe.printNextToken(Token.tRPAREN, spaceBeforeClosingParen);
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
		shouldVisitExpressions = true;
		shouldVisitStatements = true;
		shouldVisitTypeIds = true;
		shouldVisitEnumerators = true;
		shouldVisitTranslationUnit = true;

		shouldVisitBaseSpecifiers = true;
		shouldVisitNamespaces = true;
		shouldVisitTemplateParameters = true;
	}

	private final Scanner localScanner;
	final DefaultCodeFormatterOptions preferences;
	private final Scribe scribe;

	private boolean fInsideFor;
	private boolean fExpectSemicolonAfterDeclaration= true;

	private MultiStatus fStatus;
	private int fOpenAngleBrackets;

	public CodeFormatterVisitor(DefaultCodeFormatterOptions preferences, int offset, int length) {
		localScanner = new Scanner() {
			@Override
			public Token nextToken() {
				Token t= super.nextToken();
				while (t != null && (t.isWhiteSpace() || t.isPreprocessor())) {
					t= super.nextToken();
				}
				return t;
			}
		};
		this.preferences = preferences;
		scribe = new Scribe(this, offset, length);
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
		scribe.setSkipPositions(collectInactiveCodePositions(unit));

		fStatus= new MultiStatus(CCorePlugin.PLUGIN_ID, 0,
				"Formatting problem(s) in '" + unit.getFilePath() + "'", null); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			unit.accept(this);
		} catch (RuntimeException e) {
			reportFormattingProblem(e);
			if (DEBUG) return failedToFormat(e);
		}
		if (DEBUG){
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
		String errorMessage= e.getMessage();
		if (errorMessage == null) {
			errorMessage= "Unknown error"; //$NON-NLS-1$
		}
		fStatus.add(createStatus(errorMessage, e));
	}

	private static IStatus createStatus(String msg, Throwable e) {
		return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, msg, e);
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTTranslationUnit)
	 */
	@Override
	public int visit(IASTTranslationUnit tu) {
		// fake new line
		scribe.lastNumberOfNewLines = 1;
		scribe.startNewLine();
		final int indentLevel= scribe.indentationLevel;
		IASTPreprocessorMacroExpansion[] macroExpansions = tu.getMacroExpansions();
		int m = 0;
		IASTDeclaration[] decls= tu.getDeclarations();
		for (int i = 0; i < decls.length; i++) {
			IASTDeclaration declaration = decls[i];
			if (!declaration.isPartOfTranslationUnitFile()) {
				continue;
			}
			try {
				int pos = scribe.scanner.getCurrentPosition();
				IASTFileLocation declarationLocation = declaration.getFileLocation();
				int declarartionOffset = declarationLocation.getNodeOffset();
				int declarationEndOffset = declarartionOffset + declarationLocation.getNodeLength();
				for (; m < macroExpansions.length; m++) {
					IASTPreprocessorMacroExpansion macroExpansion = macroExpansions[m];
					IASTFileLocation macroLocation = macroExpansion.getFileLocation();
					int macroOffset = macroLocation.getNodeOffset();
					if (macroOffset > declarartionOffset) {
						break;
					}
					int macroEndOffset = macroOffset + macroLocation.getNodeLength();
					if (isFunctionStyleMacroExpansion(macroExpansion) && macroOffset >= pos &&
							(macroEndOffset <= declarartionOffset || macroEndOffset >= declarationEndOffset)) {
						// The function-style macro expansion either doesn't overlap with
						// the following declaration, or the declaration is completely covered by
						// the macro expansion. In both cases formatting is driven by the text of
						// parameters of the macro, not by the expanded code.
						formatFunctionStyleMacroExpansion(macroExpansion);
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
		scribe.printEndOfTranslationUnit();
		return PROCESS_SKIP;
	}

	private boolean isFunctionStyleMacroExpansion(IASTPreprocessorMacroExpansion macroExpansion) {
		IASTName name = macroExpansion.getMacroReference();
		IMacroBinding binding = (IMacroBinding) name.resolveBinding();
		return binding.isFunctionStyle() && binding.getParameterList().length > 0;
	}

	private void formatFunctionStyleMacroExpansion(IASTPreprocessorMacroExpansion macroExpansion) {
		IASTName name = macroExpansion.getMacroReference();
		IASTFileLocation fileLocation= name.getFileLocation();
		if (fileLocation != null) {
			scribe.printRaw(fileLocation.getNodeOffset(), fileLocation.getNodeLength());
		}
		fileLocation = macroExpansion.getFileLocation();
		scribe.printNextToken(Token.tLPAREN);
		IMacroBinding binding = (IMacroBinding) name.resolveBinding();
		if (preferences.insert_space_after_opening_paren_in_method_invocation) {
			scribe.space();
		}
		final int continuationIndentation = preferences.continuation_indentation;
		Alignment listAlignment = scribe.createAlignment(
				Alignment.MACRO_ARGUMENTS,
				preferences.alignment_for_arguments_in_method_invocation,
				Alignment.R_OUTERMOST,
				binding.getParameterList().length,
				scribe.scanner.getCurrentPosition(),
				continuationIndentation,
				false);
		scribe.enterAlignment(listAlignment);
		boolean ok = false;
		do {
			try {
				int fragment = 0;
				scribe.alignFragment(listAlignment, fragment);
				int parenLevel= 0;
				boolean done = false;
				while (!done) {
					boolean hasWhitespace= scribe.printComment();
					int token = peekNextToken();
					switch (token) {
					case Token.tLPAREN:
						++parenLevel;
						scribe.printNextToken(token, hasWhitespace);
						break;
					case Token.tRPAREN:
						if (parenLevel > 0) {
							--parenLevel;
							scribe.printNextToken(token, hasWhitespace);
						} else {
							if (preferences.insert_space_before_closing_paren_in_method_invocation) {
								scribe.space();
							}
							scribe.printNextToken(token);
							done = true;
						}
						break;
					case Token.tCOMMA:
						if (parenLevel == 0 && preferences.insert_space_before_comma_in_method_invocation_arguments) {
							scribe.space();
						}
						scribe.printNextToken(token);
						if (parenLevel == 0) {
							if (preferences.insert_space_after_comma_in_method_invocation_arguments) {
								scribe.space();
							}
							scribe.printComment();
							++fragment;
							if (fragment < listAlignment.fragmentCount) {
								scribe.alignFragment(listAlignment, fragment);
							}
						}
						break;
					case Token.tSTRING:
					case Token.tLSTRING:
					case Token.tRSTRING:
						boolean needSpace= hasWhitespace;
						while (true) {
							scribe.printNextToken(token, needSpace);
							if (peekNextToken() != token) {
								break;
							}
							scribe.printCommentPreservingNewLines();
							needSpace= true;
						}
						break;
					default:
						scribe.printNextToken(token, hasWhitespace);
					}
				}
				int token = peekNextToken();
				if (token == Token.tSEMI) {
					scribe.printNextToken(token);
					scribe.startNewLine();
				}
				ok = true;
			} catch (AlignmentException e) {
				scribe.redoAlignment(e);
			}
		} while (!ok);
		scribe.exitAlignment(listAlignment, true);
	}

	@Override
	public int visit(IASTDeclaration node) {
		if (!startNode(node)) {
			return PROCESS_SKIP;
		}
		try {
			return formatDeclaration(node);
		} finally {
			endOfNode(node);
		}
	}

	private int formatDeclaration(IASTDeclaration node) {
		int indentLevel= scribe.indentationLevel;
		try {
    		if (node instanceof IASTFunctionDefinition) {
    			return visit((IASTFunctionDefinition) node);
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
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	@Override
	public int visit(IASTName node) {
		if (!startNode(node)) { return PROCESS_SKIP; }
		try {
			if (node instanceof ICPPASTQualifiedName) {
				visit((ICPPASTQualifiedName) node);
			} else if (node instanceof ICPPASTTemplateId) {
				visit((ICPPASTTemplateId) node);
			} else {
				formatRaw(node);
			}
		} finally {
			endOfNode(node);
		}
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTInitializer)
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

		if (!startNode(node)) { return PROCESS_SKIP; }
		try {
			if (node instanceof IASTEqualsInitializer) {
				visit((IASTEqualsInitializer) node);
			} else if (node instanceof IASTInitializerList) {
				visit((IASTInitializerList) node);
			} else if (node instanceof ICASTDesignatedInitializer) {
				visit((ICASTDesignatedInitializer) node);
			} else {
				formatRaw(node);
			}
		} finally {
			endOfNode(node);
		}
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration)
	 */
	@Override
	public int visit(IASTParameterDeclaration node) {
		if (!startNode(node)) { return PROCESS_SKIP; }
		try {
			// decl-specifier
			final IASTDeclSpecifier declSpec= node.getDeclSpecifier();
			if (declSpec != null) {
				declSpec.accept(this);
			}
			// declarator
			final IASTDeclarator declarator= node.getDeclarator();
			if (declarator != null) {
				boolean needSpace= declarator.getPointerOperators().length > 0 && scribe.printComment();
				if (needSpace) {
					scribe.space();
				}
				declarator.accept(this);
			}
		} finally {
			endOfNode(node);
		}
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
	 */
	@Override
	public int visit(IASTDeclarator node) {
		if (!startNode(node)) { return PROCESS_SKIP; }

		try {
			// Common to all declarators
			final IASTPointerOperator[] pointerOperators= node.getPointerOperators();
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
			IASTName name= node.getName();
			if (name != null && name.getSimpleID().length != 0) {
				if (isFirstDeclarator(node)) {
					// Preserve non-space between pointer operator and name
					if (pointerOperators.length == 0 || scribe.printComment()) {
						scribe.space();
					}
				}
				name.accept(this);
			}
			IASTDeclarator nestedDecl= node.getNestedDeclarator();
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

			IASTInitializer initializer= node.getInitializer();
			if (initializer != null) {
				initializer.accept(this);
			}
		} finally {
			endOfNode(node);
		}
		return PROCESS_SKIP;
	}

	/**
	 * Determine whether the given declarator is the first in a list of declarators (if any).
	 * 
	 * @param node  the declarator node
	 * @return <code>true</code> if this node is the first in a list
	 */
	private boolean isFirstDeclarator(IASTDeclarator node) {
		IASTNode parent= node.getParent();
		if (parent instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simpleDecl= (IASTSimpleDeclaration) parent;
			return simpleDecl.getDeclarators()[0] == node;
		}
		return true;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier)
	 */
	@Override
	public int visit(IASTDeclSpecifier node) {
		if (!startNode(node)) { return PROCESS_SKIP; }
		try {
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
		} finally {
			endOfNode(node);
		}
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTExpression)
	 */
	@Override
	public int visit(IASTExpression node) {
		if (!startNode(node)) { return PROCESS_SKIP; }
		try {
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
			} else if (node instanceof ICASTTypeIdInitializerExpression) {
				visit((ICASTTypeIdInitializerExpression) node);
			} else if (node instanceof ICPPASTNewExpression) {
				visit((ICPPASTNewExpression) node);
			} else if (node instanceof ICPPASTDeleteExpression) {
				visit((ICPPASTDeleteExpression) node);
			} else if (node instanceof ICPPASTSimpleTypeConstructorExpression) {
				visit((ICPPASTSimpleTypeConstructorExpression) node);
			} else if (node instanceof IASTProblemExpression) {
				visit((IASTProblemExpression) node);
			} else {
				formatRaw(node);
			}
		} finally {
			endOfNode(node);
		}
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTStatement)
	 */
	@Override
	public int visit(IASTStatement node) {
		if (!startNode(node)) { return PROCESS_SKIP; }
		int indentLevel= scribe.indentationLevel;
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
    		} else if (node instanceof IASTLabelStatement) {
    			visit((IASTLabelStatement) node);
    		} else if (node instanceof IASTProblemStatement) {
    			visit((IASTProblemStatement) node);
    		} else {
    			formatRaw(node);
    		}
		} catch (ASTProblemException e) {
			if (node instanceof IASTProblemStatement) {
				throw e;
			} else {
				skipNode(node);
				while (scribe.indentationLevel > indentLevel) {
					scribe.unIndent();
				}
			}
		} finally {
			endOfNode(node);
		}
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTTypeId)
	 */
	@Override
	public int visit(IASTTypeId node) {
		if (!startNode(node)) { return PROCESS_SKIP; }
		try {
			if (node instanceof IASTProblemHolder) {
				throw new ASTProblemException(((IASTProblemHolder) node).getProblem());
			}
			// decl-specifier
			final IASTDeclSpecifier declSpec= node.getDeclSpecifier();
			if (declSpec != null) {
				declSpec.accept(this);
			}
			// declarator
			final IASTDeclarator declarator= node.getAbstractDeclarator();
			if (declarator != null) {
				boolean needSpace= declarator.getPointerOperators().length > 0 && scribe.printComment();
				if (needSpace) {
					scribe.space();
				}
				declarator.accept(this);
			}
		} finally {
			endOfNode(node);
		}
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator)
	 */
	@Override
	public int visit(IASTEnumerator enumerator) {
		if (!startNode(enumerator)) { return PROCESS_SKIP; }
		try {
			// name
			enumerator.getName().accept(this);

			// optional value assignment
			final IASTExpression value= enumerator.getValue();
			if (value != null) {
				scribe.printNextToken(Token.tASSIGN, preferences.insert_space_before_assignment_operator);
				if (preferences.insert_space_after_assignment_operator) {
					scribe.space();
				}
				value.accept(this);
			}
		} finally {
			endOfNode(enumerator);
		}
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor#visit(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier)
	 */
	@Override
	public int visit(ICPPASTBaseSpecifier specifier) {
		if (!startNode(specifier)) { return PROCESS_SKIP; }
		try {
			boolean needSpace= false;
			loop: while (true) {
				int token= peekNextToken();
				switch (token) {
				case Token.t_public:
				case Token.t_protected:
				case Token.t_private:
				case Token.t_virtual:
					scribe.printNextToken(token, needSpace);
					needSpace= true;
					break;
				default:
					break loop;
				}
			}
			if (needSpace) {
				scribe.space();
			}
			specifier.getName().accept(this);
		} finally {
			endOfNode(specifier);
		}
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor#visit(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition)
	 */
	@Override
	public int visit(ICPPASTNamespaceDefinition node) {
		if (!startNode(node)) { return PROCESS_SKIP; }
		try {
			final int line= scribe.line;
			// namespace <name>
			scribe.printNextToken(Token.t_namespace, false);
			scribe.space();
			node.getName().accept(this);

			// member declarations
			IASTDeclaration[] memberDecls= node.getDeclarations();
			formatLeftCurlyBrace(line, preferences.brace_position_for_namespace_declaration);
			formatOpeningBrace(preferences.brace_position_for_namespace_declaration, preferences.insert_space_before_opening_brace_in_namespace_declaration);
			if (preferences.indent_body_declarations_compare_to_namespace_header) {
				scribe.indent();
			}
			scribe.startNewLine();
			for (IASTDeclaration declaration : memberDecls) {
				declaration.accept(this);
				scribe.startNewLine();
			}
			if (preferences.indent_body_declarations_compare_to_namespace_header) {
				scribe.unIndent();
			}
			formatClosingBrace(preferences.brace_position_for_namespace_declaration);
		} finally {
			endOfNode(node);
		}
		return PROCESS_SKIP;
	}


	private int visit(ICPPASTLinkageSpecification node) {
		scribe.printComment();
		final int line= scribe.line;
		// extern "<linkage>"
		scribe.printNextToken(Token.t_extern, false);
		scribe.space();
		scribe.printNextToken(Token.tSTRING);

		// member declarations
		IASTDeclaration[] memberDecls= node.getDeclarations();
		if (memberDecls.length == 1 && peekNextToken() != Token.tLBRACE) {
			scribe.space();
			memberDecls[0].accept(this);
		} else {
			// TLETODO [formatter] need options for linkage specification
			formatLeftCurlyBrace(line, preferences.brace_position_for_namespace_declaration);
			formatOpeningBrace(preferences.brace_position_for_namespace_declaration, preferences.insert_space_before_opening_brace_in_namespace_declaration);
			if (preferences.indent_body_declarations_compare_to_namespace_header) {
				scribe.indent();
			}
			scribe.startNewLine();
			for (IASTDeclaration declaration : memberDecls) {
				declaration.accept(this);
				scribe.startNewLine();
			}
			if (preferences.indent_body_declarations_compare_to_namespace_header) {
				scribe.unIndent();
			}
			formatClosingBrace(preferences.brace_position_for_namespace_declaration);
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
		scribe.printNextToken(Token.t_using);
		if (node.isTypename()) {
			scribe.printNextToken(Token.t_typename, true);
		}
		scribe.space();
		node.getName().accept(this);
		int token = peekNextToken();
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
	 * @see org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor#visit(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter)
	 */
	@Override
	public int visit(ICPPASTTemplateParameter node) {
		if (!startNode(node)) { return PROCESS_SKIP; }
		try {
    		if (node instanceof ICPPASTSimpleTypeTemplateParameter) {
    			visit((ICPPASTSimpleTypeTemplateParameter) node);
    		} else if (node instanceof ICPPASTTemplatedTypeTemplateParameter) {
    			visit((ICPPASTTemplatedTypeTemplateParameter) node);
    		} else {
    			visit((IASTParameterDeclaration) node);
    		}
		} catch (ASTProblemException e) {
			skipNode(node);
		} finally {
			endOfNode(node);
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
		IASTTypeId defaultType= node.getDefaultType();
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
		final ICPPASTTemplateParameter[] templateParameters= node.getTemplateParameters();
		if (templateParameters.length > 0) {
			final ListOptions options= new ListOptions(Alignment.M_COMPACT_SPLIT);
			options.fSpaceAfterComma= preferences.insert_space_after_comma_in_template_parameters;
			options.fSpaceBeforeComma= preferences.insert_space_before_comma_in_template_parameters;
			formatList(Arrays.asList(templateParameters), options, false, false, null);
		}
		scribe.printNextToken(new int[] { Token.tGT, Token.tSHIFTR }, preferences.insert_space_before_closing_angle_bracket_in_template_parameters);
		if (preferences.insert_space_after_closing_angle_bracket_in_template_parameters) {
			scribe.space();
		}
		IASTName name= node.getName();
		if (name != null) {
			name.accept(this);
		}
		IASTExpression defaultValue= node.getDefaultValue();
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
		if (!startNode(node)) { return PROCESS_SKIP; }
		try {
			// format like a function call
			formatFunctionCallArguments(node.getArguments());
		} finally {
			endOfNode(node);
		}
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTConstructorChainInitializer node) {
		final IASTName member= node.getMemberInitializerId();
		final IASTInitializer init= node.getInitializer();
		if (member!= null && init != null) {
			member.accept(this);
			init.accept(this);
		} else {
			formatRaw(node);
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTFunctionDefinition node) {
		scribe.printComment();
		final int line= scribe.line;

		// decl-specifier
		final IASTDeclSpecifier declSpec= node.getDeclSpecifier();
		declSpec.accept(this);

		// declarator
		final IASTFunctionDeclarator declarator= node.getDeclarator();
		skipNonWhitespaceToNode(declarator);
		boolean hasSpace= scribe.printComment();
		boolean hasPointerOps= declarator.getPointerOperators().length > 0;
		boolean needSpace= (hasPointerOps && hasSpace) || (!hasPointerOps && peekNextToken() == Token.tIDENTIFIER);
		if (needSpace) {
			scribe.space();
		}
		Runnable bodyLeftBraceFormatter = null;
		if (DefaultCodeFormatterConstants.END_OF_LINE.equals(preferences.brace_position_for_method_declaration) &&
				!hasMemberInitializers(node) && !(node instanceof ICPPASTFunctionWithTryBlock)) {
			IASTStatement body = node.getBody();
			if (body instanceof IASTCompoundStatement && !startsWithMacroExpansion(body)) {
				bodyLeftBraceFormatter = new TrailingTokenFormatter(Token.tLBRACE,
						body.getFileLocation().getNodeOffset(),
						preferences.insert_space_before_opening_brace_in_method_declaration, false);
				scribe.setTailFormatter(bodyLeftBraceFormatter);
			}
		}
		declarator.accept(this);

		if (node instanceof ICPPASTFunctionWithTryBlock) {
			scribe.startNewLine();
			scribe.printNextToken(Token.t_try, false);
			scribe.printTrailingComment();
		}

		if (node instanceof ICPPASTFunctionDefinition) {
			final ICPPASTConstructorChainInitializer[] constructorChain=
					((ICPPASTFunctionDefinition) node).getMemberInitializers();
			if (constructorChain != null && constructorChain.length > 0) {
				if (preferences.insert_new_line_before_colon_in_constructor_initializer_list) {
					scribe.printTrailingComment();
					scribe.startNewLine();
					scribe.indentForContinuation();
				}
				scribe.printNextToken(Token.tCOLON, !preferences.insert_new_line_before_colon_in_constructor_initializer_list);
				if (preferences.insert_new_line_before_colon_in_constructor_initializer_list) {
					scribe.space();
				} else {
					scribe.printTrailingComment();
					scribe.startNewLine();
					scribe.indentForContinuation();
				}
				final ListOptions options= new ListOptions(preferences.alignment_for_constructor_initializer_list);
				options.fTieBreakRule = Alignment.R_OUTERMOST;
				formatList(Arrays.asList(constructorChain), options, false, false, null);
				scribe.unIndentForContinuation();
			}
		}

		if (bodyLeftBraceFormatter != null) {
			scribe.setTailFormatter(null);
		}
		// Body
		IASTStatement bodyStmt= node.getBody();
		if (bodyStmt instanceof IASTCompoundStatement) {
			if (startNode(bodyStmt)) {
				try {
					if (scribe.scanner.getCurrentPosition() <= bodyStmt.getFileLocation().getNodeOffset()) {
						formatLeftCurlyBrace(line, preferences.brace_position_for_method_declaration);
					}
					formatBlock((IASTCompoundStatement) bodyStmt,
							preferences.brace_position_for_method_declaration,
							preferences.insert_space_before_opening_brace_in_method_declaration,
							preferences.indent_statements_compare_to_body);
				} finally {
					endOfNode(bodyStmt);
				}
			}
		} else {
			bodyStmt.accept(this);
		}
		scribe.printTrailingComment();
		scribe.startNewLine();

		if (node instanceof ICPPASTFunctionWithTryBlock) {
			ICPPASTCatchHandler[] catchHandlers= ((ICPPASTFunctionWithTryBlock) node).getCatchHandlers();
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
		formatList(parameters, options, true, node.takesVarArgs(),
				new CPPFunctionDeclaratorTailFormatter(node, scribe.getTailFormatter()));

		IASTFileLocation fileLocation= node.getFileLocation();
		if (fileLocation != null &&
				scribe.scanner.getCurrentPosition() < fileLocation.getNodeOffset() + fileLocation.getNodeLength()) {
			skipConstVolatileRestrict();

			final IASTTypeId[] exceptionSpecification= node.getExceptionSpecification();
			if (exceptionSpecification != null && peekNextToken() == Token.t_throw)
				formatExceptionSpecification(exceptionSpecification);
			// Skip the rest (=0)
			scribe.printTrailingComment();
			scribe.space();
			skipNode(node);
		}
		return PROCESS_SKIP;
	}

	private void formatExceptionSpecification(final IASTTypeId[] exceptionSpecification) {
		if (exceptionSpecification.length > 0) {
			Alignment alignment = scribe.createAlignment(
					Alignment.EXCEPTION_SPECIFICATION,
					preferences.alignment_for_throws_clause_in_method_declaration,
					exceptionSpecification.length,
					scribe.scanner.getCurrentPosition());

			scribe.enterAlignment(alignment);
			boolean ok = false;
			do {
				try {
					scribe.alignFragment(alignment, 0);
					scribe.printNextToken(Token.t_throw, true);
					scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_exception_specification);
					if (preferences.insert_space_after_opening_paren_in_exception_specification) {
						scribe.space();
					}
					exceptionSpecification[0].accept(this);
					for (int i = 1; i < exceptionSpecification.length; i++) {
						scribe.printNextToken(Token.tCOMMA, preferences.insert_space_before_comma_in_method_declaration_throws);
						scribe.printTrailingComment();
						if (preferences.insert_space_after_comma_in_method_declaration_throws) {
							scribe.space();
						}
						scribe.alignFragment(alignment, i);
		    			exceptionSpecification[i].accept(this);
					}
					if (peekNextToken() == Token.tRPAREN) {
						scribe.printNextToken(Token.tRPAREN, preferences.insert_space_before_closing_paren_in_exception_specification);
					}
					ok = true;
				} catch (AlignmentException e) {
					scribe.redoAlignment(e);
				}
			} while (!ok);
			scribe.exitAlignment(alignment, true);
		} else {
			scribe.printNextToken(Token.t_throw, true);
			scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_exception_specification);
			scribe.printNextToken(Token.tRPAREN, preferences.insert_space_between_empty_parens_in_exception_specification);
		}
	}

	private boolean skipConstVolatileRestrict() {
		boolean skipped= false;
		int token= peekNextToken();
		while (token == Token.t_const || token == Token.t_volatile || token == Token.t_restrict) {
			scribe.printNextToken(token, true);
			token= peekNextToken();
			skipped= true;
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
		final ListOptions options= new ListOptions(preferences.alignment_for_parameters_in_method_declaration);
		options.fUseFallbackMode= true;
		options.fSpaceBeforeOpeningParen= preferences.insert_space_before_opening_paren_in_method_declaration;
		options.fSpaceAfterOpeningParen= preferences.insert_space_after_opening_paren_in_method_declaration;
		options.fSpaceBeforeClosingParen= preferences.insert_space_before_closing_paren_in_method_declaration;
		options.fSpaceBetweenEmptyParen= preferences.insert_space_between_empty_parens_in_method_declaration;
		options.fSpaceBeforeComma= preferences.insert_space_before_comma_in_method_declaration_parameters;
		options.fSpaceAfterComma= preferences.insert_space_after_comma_in_method_declaration_parameters;
		options.fTieBreakRule = Alignment.R_OUTERMOST;
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
		IASTNodeLocation[] locations= node.getNodeLocations();
		if (locations.length > 0) {
			IASTNodeLocation lastLocation = locations[locations.length - 1];
			if (!(lastLocation instanceof IASTMacroExpansionLocation)) {
				IASTFileLocation fileLocation= lastLocation.asFileLocation();
				return fileLocation.getNodeOffset() + fileLocation.getNodeLength() - 1;
			}
		}
		return -1;
	}

	private void formatPointers(IASTPointerOperator[] pointers) {
		for (IASTPointerOperator pointer : pointers) {
			if (scribe.printComment()) {
				scribe.space();
			}
			if (scribe.printModifiers()) {
				scribe.space();
			}
			if (pointer instanceof ICPPASTReferenceOperator) {
				scribe.printNextToken(Token.tAMPER, false);
			} else if (pointer instanceof ICPPASTPointerToMember) {
				final ICPPASTPointerToMember ptrToMember= (ICPPASTPointerToMember) pointer;
				final IASTName name= ptrToMember.getName();
				if (name != null) {
					name.accept(this);
				}
				scribe.printNextToken(Token.tSTAR, false);
				if (skipConstVolatileRestrict()) {
					scribe.space();
				}
			} else {
				scribe.printNextToken(Token.tSTAR, false);
				if (skipConstVolatileRestrict()) {
					scribe.space();
				}
			}
		}
	}

	private int visit(ICASTKnRFunctionDeclarator node) {
		final List<IASTName> parameters= Arrays.asList(node.getParameterNames());
		ListOptions options= createListOptionsForFunctionDeclarationParameters();
		formatList(parameters, options, true, false, null);

		IASTDeclaration[] parameterDecls= node.getParameterDeclarations();
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
		IASTExpression bitFieldSizeExpr= node.getBitFieldSize();
		if (bitFieldSizeExpr != null) {
			scribe.printNextToken(Token.tCOLON, true);
			bitFieldSizeExpr.accept(this);
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTArrayDeclarator node) {
		IASTArrayModifier[] arrayModifiers= node.getArrayModifiers();
		if (arrayModifiers != null) {
			for (IASTArrayModifier arrayModifier2 : arrayModifiers) {
				IASTArrayModifier arrayModifier = arrayModifier2;
				scribe.printNextToken(Token.tLBRACKET, preferences.insert_space_before_opening_bracket);
				boolean emptyBrackets= arrayModifier.getConstantExpression() == null
						&& !(arrayModifier instanceof ICASTArrayModifier);
				if (!emptyBrackets) {
					if (preferences.insert_space_after_opening_bracket) {
						scribe.space();
					}
				}
				if (arrayModifier instanceof ICASTArrayModifier) {
					final ICASTArrayModifier cArrayModifier= (ICASTArrayModifier) arrayModifier;
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
				try {
					arrayModifier.accept(this);
				} catch (ASTProblemException e) {
					scribe.skipToToken(Token.tRBRACKET);
				}
				boolean insertSpace= emptyBrackets ?
						preferences.insert_space_between_empty_brackets	:
						preferences.insert_space_before_closing_bracket;
				scribe.printNextToken(Token.tRBRACKET, insertSpace);
			}
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTSimpleDeclaration node) {
		IASTDeclSpecifier declSpec= node.getDeclSpecifier();
		declSpec.accept(this);
		final List<IASTDeclarator> declarators= Arrays.asList(node.getDeclarators());
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
			Runnable tailFormatter = fExpectSemicolonAfterDeclaration ?
					new TrailingSemicolonFormatter(node) : null;
			if (declarators.size() == 1) {
				scribe.setTailFormatter(tailFormatter);
				try {
					visit(declarators.get(0));
					scribe.runTailFormatter();
				} finally {
					scribe.setTailFormatter(null);
				}
			} else {
				final ListOptions options= new ListOptions(preferences.alignment_for_declarator_list);
				options.fSpaceAfterComma= preferences.insert_space_after_comma_in_declarator_list;
				options.fSpaceBeforeComma= preferences.insert_space_before_comma_in_declarator_list;
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
		final ICPPASTTemplateParameter[] templateParameters= node.getTemplateParameters();
		if (templateParameters.length > 0) {
			final ListOptions options= new ListOptions(Alignment.M_COMPACT_SPLIT);
			options.fSpaceAfterComma= preferences.insert_space_after_comma_in_template_parameters;
			options.fSpaceBeforeComma= preferences.insert_space_before_comma_in_template_parameters;
			formatList(Arrays.asList(templateParameters), options, false, false, null);
		}
		scribe.printNextToken(new int[] { Token.tGT, Token.tSHIFTR }, preferences.insert_space_before_closing_angle_bracket_in_template_parameters);
		if (preferences.insert_space_after_closing_angle_bracket_in_template_parameters) {
			scribe.space();
		}

		// Declaration
		final IASTDeclaration declaration= node.getDeclaration();
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
		scribe.printComment();
		final int line= scribe.line;

		// storage class and other modifiers
		if (scribe.printModifiers()) {
			scribe.space();
		}

		// Consider macro expansion
		if (withinMacroExpansion(node, scribe.scanner.getCurrentPosition())) {
			continueNode(node);
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

		final IASTName name= node.getName();
		if (name != null) {
			scribe.space();
			name.accept(this);
		}

		// Member declarations
		IASTDeclaration[] memberDecls= node.getMembers();
		formatLeftCurlyBrace(line, preferences.brace_position_for_type_declaration);
		formatOpeningBrace(preferences.brace_position_for_type_declaration, preferences.insert_space_before_opening_brace_in_type_declaration);
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
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTCompositeTypeSpecifier node) {
		scribe.printComment();
		final int line= scribe.line;

		// Storage class and other modifiers
		if (scribe.printModifiers()) {
			scribe.space();
		}
		final int headerIndent= scribe.numberOfIndentations;

		// Consider macro expansion
		if (withinMacroExpansion(node, scribe.scanner.getCurrentPosition())) {
			continueNode(node);
		}

		switch (node.getKey()) {
		case IASTCompositeTypeSpecifier.k_struct:
			scribe.printNextToken(Token.t_struct, false);
			break;
		case IASTCompositeTypeSpecifier.k_union:
			scribe.printNextToken(Token.t_union, false);
			break;
		case ICPPASTCompositeTypeSpecifier.k_class:
			scribe.printNextToken(Token.t_class, false);
			break;
		default:
			assert false : "Unexpected composite type specifier"; //$NON-NLS-1$
		}

		final IASTName name= node.getName();
		if (name != null) {
			scribe.space();
			name.accept(this);
		}

		// Base specifiers
		final List<ICPPASTBaseSpecifier> baseSpecifiers= Arrays.asList(node.getBaseSpecifiers());
		if (baseSpecifiers.size() > 0) {
			ICPPASTBaseSpecifier baseSpecifier = baseSpecifiers.get(0);
			try {
				if (baseSpecifier.getLeadingSyntax().getType() == IToken.tCOLON) {
					scribe.printNextToken(Token.tCOLON, preferences.insert_space_before_colon_in_base_clause);
					if (preferences.insert_space_after_colon_in_base_clause) {
						scribe.space();
					}
				}
			} catch (UnsupportedOperationException exc) {
			} catch (ExpansionOverlapsBoundaryException exc) {
			}
			final ListOptions options= new ListOptions(preferences.alignment_for_base_clause_in_type_declaration);
			options.fSpaceAfterComma= preferences.insert_space_after_comma_in_base_types;
			options.fSpaceBeforeComma= preferences.insert_space_before_comma_in_base_types;
			formatList(baseSpecifiers, options, false, false, null);
		}

		// Member declarations
		formatLeftCurlyBrace(line, preferences.brace_position_for_type_declaration);
		formatOpeningBrace(preferences.brace_position_for_type_declaration, preferences.insert_space_before_opening_brace_in_type_declaration);
		final int braceIndent= scribe.numberOfIndentations;
		if (braceIndent > headerIndent) {
			scribe.unIndent();
		}
		if (preferences.indent_access_specifier_compare_to_type_header) {
			scribe.indent();
		}
		IASTDeclaration[] memberDecls= node.getMembers();
		scribe.startNewLine();
		for (IASTDeclaration declaration : memberDecls) {
			if (preferences.indent_body_declarations_compare_to_access_specifier) {
				scribe.indent();
			}
			scribe.printComment();
			if (declaration instanceof ICPPASTVisibilityLabel) {
				if (preferences.indent_body_declarations_compare_to_access_specifier) {
					scribe.unIndent();
				}
				if (startNode(declaration)) {
					try {
						scribe.startNewLine();
						visit((ICPPASTVisibilityLabel) declaration);
						scribe.startNewLine();
					} finally {
						endOfNode(declaration);
					}
				}
			} else {
				if (startNode(declaration)) {
					try {
						scribe.startNewLine();
						formatDeclaration(declaration);
					} finally {
						endOfNode(declaration);
					}
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
		final int line= scribe.line;
		// Storage class and other modifiers
		if (scribe.printModifiers()) {
			scribe.space();
		}

		final int headerIndent= scribe.numberOfIndentations;
		scribe.printNextToken(Token.t_enum, true);
		final IASTName name= node.getName();
		if (name != null) {
			scribe.space();
			name.accept(this);
		}

		formatLeftCurlyBrace(line, preferences.brace_position_for_type_declaration);
		formatOpeningBrace(preferences.brace_position_for_type_declaration, preferences.insert_space_before_opening_brace_in_type_declaration);
		final int braceIndent= scribe.numberOfIndentations;

		scribe.startNewLine();

        if (braceIndent == headerIndent) {
        	scribe.indent();
        }
        final int enumIndent= scribe.numberOfIndentations;
		final IASTEnumerator[] enumerators= node.getEnumerators();

		final ListOptions options= new ListOptions(preferences.alignment_for_enumerator_list);
		options.fSpaceBeforeComma= preferences.insert_space_before_comma_in_enum_declarations;
		options.fSpaceAfterComma= preferences.insert_space_after_comma_in_enum_declarations;
		options.fContinuationIndentation= enumIndent == headerIndent ? 1 : 0;
		formatList(Arrays.asList(enumerators), options, false, false, null);

		// Handle trailing comma
		if (peekNextToken() == Token.tCOMMA) {
			scribe.printNextToken(Token.tCOMMA, options.fSpaceBeforeComma);
			if (options.fSpaceAfterComma) {
				scribe.space();
			}
		}

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
	 * @param elements the elements to format
	 * @param options formatting options
	 * @param encloseInParen indicates whether the list should be enclosed in parentheses
	 * @param addEllipsis indicates whether ellipsis should be added after the last element
	 * @param tailFormatter formatter for the trailing text that should be kept together with
	 * 		the last element of the list. 
	 */
	private void formatList(List<? extends IASTNode> elements, ListOptions options,
			boolean encloseInParen, boolean addEllipsis, Runnable tailFormatter) {
		if (encloseInParen)
			scribe.printNextToken(Token.tLPAREN, options.fSpaceBeforeOpeningParen);

		final int elementsLength = elements.size();
		if (encloseInParen) {
			boolean spaceBeforeClosingParen = elements.isEmpty() && !addEllipsis ?
					options.fSpaceBetweenEmptyParen : options.fSpaceBeforeClosingParen;
			tailFormatter = new ClosingParensesisTailFormatter(spaceBeforeClosingParen, tailFormatter);
		}

		if (!elements.isEmpty() || addEllipsis) {
			if (options.fSpaceAfterOpeningParen) {
				scribe.space();
			}
			Alignment retryAlignment = null;

			int fallbackMode = options.fUseFallbackMode ?
					getFallbackAlignmentMode(options.fMode) : options.fMode;
			if (fallbackMode != options.fMode) {
				retryAlignment = scribe.createAlignment(
						Alignment.LIST_FALLBACK_TRAP,
						Alignment.M_ONE_PER_LINE_SPLIT,
						Alignment.R_INNERMOST,
						1,
						scribe.scanner.getCurrentPosition(),
						0,
						false);
				scribe.enterAlignment(retryAlignment);
			}
			boolean success = false;
			int mode = options.fMode;
			do {
				if (retryAlignment != null)
					scribe.alignFragment(retryAlignment, 0);

				try {
					final int continuationIndentation= options.fContinuationIndentation >= 0 ?
							options.fContinuationIndentation : preferences.continuation_indentation;
					Alignment alignment = scribe.createAlignment(
							Alignment.LIST_ELEMENTS_PREFIX +
									(elements.isEmpty() ? "ellipsis" : elements.get(0).getClass().getSimpleName()), //$NON-NLS-1$
							mode,
							options.fTieBreakRule,
							elementsLength + (addEllipsis ? 1 : 0),
							scribe.scanner.getCurrentPosition(),
							continuationIndentation,
							false);
					scribe.enterAlignment(alignment);
					boolean ok = false;
					do {
						try {
							int i;
							for (i = 0; i < elementsLength; i++) {
								final IASTNode node= elements.get(i);
								if (i < alignment.fragmentCount - 1) {
									scribe.setTailFormatter(
											new TrailingCommaFormatter(options.fSpaceBeforeComma,
													options.fSpaceAfterComma));
								} else {
									scribe.setTailFormatter(tailFormatter);
								}
								scribe.alignFragment(alignment, i);
								if (node instanceof ICPPASTConstructorChainInitializer) {
									// Constructor chain initializer is a special case.
									visit((ICPPASTConstructorChainInitializer) node);
								} else {
									node.accept(this);
								}
								if (i < alignment.fragmentCount - 1) {
									scribe.runTailFormatter();
								}
							}
							if (addEllipsis) {
								if (i > 0) {
									scribe.printNextToken(Token.tCOMMA, options.fSpaceBeforeComma);
									scribe.printTrailingComment();
								}
								scribe.alignFragment(alignment, i);
								if (i > 0 && options.fSpaceAfterComma) {
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
					success = true;
				} catch (AlignmentException e) {
					if (retryAlignment == null)
						throw e;
					scribe.redoAlignment(e);
				}
				mode = fallbackMode;
			} while (!success);
			if (retryAlignment != null)
				scribe.exitAlignment(retryAlignment, true);
		} else if (tailFormatter != null) {
			tailFormatter.run();
		}
	}

	private int getFallbackAlignmentMode(int alignmentMode) {
		switch (alignmentMode & Alignment.SPLIT_MASK) {
		case Alignment.M_COMPACT_SPLIT:
			alignmentMode = Alignment.M_COMPACT_FIRST_BREAK_SPLIT | (alignmentMode & ~Alignment.SPLIT_MASK); 
		}
		return alignmentMode & ~Alignment.M_INDENT_ON_COLUMN;
	}

	private int visit(ICPPASTTryBlockStatement node) {
		scribe.printNextToken(Token.t_try, scribe.printComment());
		final IASTStatement tryBody= node.getTryBody();
		if (tryBody != null) {
			tryBody.accept(this);
		}
		scribe.printTrailingComment();
		ICPPASTCatchHandler[] catchHandlers= node.getCatchHandlers();
		for (ICPPASTCatchHandler catchHandler : catchHandlers) {
			catchHandler.accept(this);
			scribe.printTrailingComment();
		}
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTCatchHandler node) {
		if (preferences.insert_new_line_before_catch_in_try_statement) {
			scribe.startNewLine();
		}
		scribe.printNextToken(Token.t_catch, true);
		scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_catch);
		if (preferences.insert_space_after_opening_paren_in_catch) {
			scribe.space();
		}
		final IASTDeclaration decl= node.getDeclaration();
		if (decl != null) {
			fExpectSemicolonAfterDeclaration= false;
			try {
				decl.accept(this);
			} finally {
				fExpectSemicolonAfterDeclaration= true;
			}
		} else if (node.isCatchAll()) {
			scribe.printNextToken(Token.tELIPSE, false /* preferences.insert_space_before_ellipsis */);
//			if (false /* preferences.insert_space_after_ellipsis */) {
//				scribe.space();
//			}
		}
		scribe.printNextToken(Token.tRPAREN, preferences.insert_space_before_closing_paren_in_catch);
		final IASTStatement catchBody= node.getCatchBody();
		if (catchBody != null) {
			catchBody.accept(this);
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTCompoundStatement node) {
		formatBlock(node, preferences.brace_position_for_block, preferences.insert_space_before_opening_brace_in_block, preferences.indent_statements_compare_to_block);
		return PROCESS_SKIP;
	}

	private int visit(IASTBreakStatement node) {
		scribe.printNextToken(Token.t_break);
		scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon);
		scribe.printTrailingComment();
		return PROCESS_SKIP;
	}

	private int visit(IASTConditionalExpression node) {
		Runnable tailFormatter = scribe.takeTailFormatter();
		try {
			node.getLogicalConditionExpression().accept(this);
		} finally {
			scribe.setTailFormatter(tailFormatter);
		}
		scribe.printTrailingComment();
		if (preferences.insert_space_before_question_in_conditional) {
			scribe.space();
		}
    	Alignment alignment = scribe.createAlignment(
    			Alignment.CONDITIONAL_EXPRESSION,
    			preferences.alignment_for_conditional_expression,
    			2,
    			scribe.scanner.getCurrentPosition());

    	scribe.enterAlignment(alignment);
    	boolean ok = false;
    	do {
    		try {
    			scribe.alignFragment(alignment, 0);
    			final IASTExpression positiveExpression = node.getPositiveResultExpression();
    			final IASTExpression negativeExpression = node.getNegativeResultExpression();
    			final IASTExpression nextExpression = positiveExpression != null ?
    					positiveExpression : negativeExpression;
    			// In case of macros we may have already passed the question mark position.
    			if (scribe.scanner.getCurrentPosition() < nextExpression.getFileLocation().getNodeOffset()) {
    				scribe.printNextToken(Token.tQUESTION, false);
	    			if (preferences.insert_space_after_question_in_conditional) {
	    				scribe.space();
	    			}
    			}

    			if (positiveExpression != null) { // gcc-extension allows to omit the positive expression.
    				positiveExpression.accept(this);
    			}
    			scribe.printTrailingComment();
    			scribe.alignFragment(alignment, 1);

    			// In case of macros we may have already passed the colon position.
    			if (scribe.scanner.getCurrentPosition() < negativeExpression.getFileLocation().getNodeOffset()) {
    				scribe.printNextToken(Token.tCOLON, preferences.insert_space_before_colon_in_conditional);
	    			if (preferences.insert_space_after_colon_in_conditional) {
	    				scribe.space();
	    			}
    			}

    			negativeExpression.accept(this);

    			ok = true;
    		} catch (AlignmentException e) {
    			scribe.redoAlignment(e);
    		}
    	} while (!ok);
    	scribe.exitAlignment(alignment, true);
    	return PROCESS_SKIP;
    }

	private int visit(IASTFunctionCallExpression node) {
		Runnable tailFormatter = scribe.takeTailFormatter();
		try {
			node.getFunctionNameExpression().accept(this);
		} finally {
			scribe.setTailFormatter(tailFormatter);
		}
		IASTInitializerClause[] paramExpr= node.getArguments();
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
			expressions= Arrays.asList(args);
		} else {
			// No arguments
			expressions= Collections.emptyList();
		}
		final ListOptions options= new ListOptions(preferences.alignment_for_arguments_in_method_invocation);
		options.fUseFallbackMode= true;
		options.fSpaceBeforeOpeningParen= preferences.insert_space_before_opening_paren_in_method_invocation;
		options.fSpaceAfterOpeningParen= preferences.insert_space_after_opening_paren_in_method_invocation;
		options.fSpaceBeforeClosingParen= preferences.insert_space_before_closing_paren_in_method_invocation;
		options.fSpaceBetweenEmptyParen= preferences.insert_space_between_empty_parens_in_method_invocation;
		options.fSpaceBeforeComma= preferences.insert_space_before_comma_in_method_invocation_arguments;
		options.fSpaceAfterComma= preferences.insert_space_after_comma_in_method_invocation_arguments;
		options.fTieBreakRule = Alignment.R_OUTERMOST;
		formatList(expressions, options, true, false, scribe.getTailFormatter());
	}

	private int visit(IASTExpressionList node) {
		final List<IASTExpression> expressions = Arrays.asList(node.getExpressions());
		final ListOptions options= new ListOptions(preferences.alignment_for_expression_list);
		options.fSpaceBeforeComma= preferences.insert_space_before_comma_in_expression_list;
		options.fSpaceAfterComma= preferences.insert_space_after_comma_in_expression_list;
		formatList(expressions, options, false, false, null);
    	return PROCESS_SKIP;
	}

	private int visit(IASTIdExpression node) {
		node.getName().accept(this);
    	return PROCESS_SKIP;
	}

	private int visit(IASTCastExpression node) {
		switch (node.getOperator()) {
		case IASTCastExpression.op_cast:
			scribe.printNextToken(Token.tLPAREN, false);
			if (preferences.insert_space_after_opening_paren_in_cast) {
				scribe.space();
			}
			node.getTypeId().accept(this);
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
			scribe.printNextToken(Token.tLT, preferences.insert_space_before_opening_angle_bracket_in_template_arguments);
			if (preferences.insert_space_after_opening_angle_bracket_in_template_arguments) {
				scribe.space();
			}
			node.getTypeId().accept(this);
			scribe.printNextToken(Token.tGT, preferences.insert_space_before_closing_angle_bracket_in_template_arguments);
			if (preferences.insert_space_after_closing_angle_bracket_in_template_arguments) {
				scribe.space();
			}
			// operand
			scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_method_invocation);
			if (preferences.insert_space_after_opening_paren_in_method_invocation) {
				scribe.space();
			}
			node.getOperand().accept(this);
			scribe.printNextToken(Token.tRPAREN, preferences.insert_space_before_closing_paren_in_method_invocation);
			break;
		default:
			skipToNode(node.getOperand());
		}
    	return PROCESS_SKIP;
	}

	private int visit(IASTTypeIdExpression node) {
		scribe.printNextToken(peekNextToken());
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
			// nested initializer expression, no need to apply extra alignment
			//			node.getExpression().accept(this);
		} else {
			// declaration initializer
	    	Alignment expressionAlignment= scribe.createAlignment(
	    			Alignment.DECLARATION_INITIALIZER,
	    			preferences.alignment_for_assignment,
	    			Alignment.R_INNERMOST,
	    			1,
	    			scribe.scanner.getCurrentPosition());

	    	scribe.enterAlignment(expressionAlignment);
	    	boolean ok = false;
	    	do {
	    		try {
	    			scribe.alignFragment(expressionAlignment, 0);

	   				node.getInitializerClause().accept(this);

	    			ok = true;
	    		} catch (AlignmentException e) {
	    			scribe.redoAlignment(e);
	    		}
	    	} while (!ok);
	    	scribe.exitAlignment(expressionAlignment, true);
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

    	Alignment expressionAlignment= scribe.createAlignment(
    			Alignment.DESIGNATED_INITIALIZER,
    			preferences.alignment_for_assignment,
    			1,
    			scribe.scanner.getCurrentPosition());

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
			final int line= scribe.line;
	        final String brace_position= preferences.brace_position_for_initializer_list;
			formatLeftCurlyBrace(line, brace_position);
	        formatOpeningBrace(brace_position, preferences.insert_space_before_opening_brace_in_initializer_list);
	        if (preferences.insert_new_line_after_opening_brace_in_initializer_list) {
	        	scribe.startNewLine();
	        }
	        if (preferences.insert_space_after_opening_brace_in_initializer_list) {
	        	scribe.space();
	        }

			final ListOptions options= new ListOptions(preferences.alignment_for_expressions_in_initializer_list);
			options.fSpaceBeforeComma= preferences.insert_space_before_comma_in_initializer_list;
			options.fSpaceAfterComma= preferences.insert_space_after_comma_in_initializer_list;
			options.fContinuationIndentation= preferences.continuation_indentation_for_initializer_list;
			formatList(initializers, options, false, false, null);

			// handle trailing comma
			if (peekNextToken() == Token.tCOMMA) {
				scribe.printNextToken(Token.tCOMMA, options.fSpaceBeforeComma);
				if (options.fSpaceAfterComma) {
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
		final IASTExpression operand= node.getOperand();
		final int operator= node.getOperator();
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
			if (peekNextToken() != Token.tLPAREN) {
				scribe.space();
			}
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
			int operatorToken= peekNextToken();
			boolean forceSpace= Character.isJavaIdentifierStart(peekNextChar());
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
		if (isAssignment(node)) {
			return formatAssignment(node);
		}
		Alignment expressionAlignment= scribe.createAlignment(
				Alignment.BINARY_EXPRESSION,
				preferences.alignment_for_binary_expression,
				2,
				scribe.scanner.getCurrentPosition());

		scribe.enterAlignment(expressionAlignment);
    	boolean ok = false;
    	do {
    		try {
    			scribe.alignFragment(expressionAlignment, 0);
    			final IASTExpression op1= node.getOperand1();
    			// operand 1
    			op1.accept(this);

    			scribe.printTrailingComment();
    			scribe.alignFragment(expressionAlignment, 1);

    			// In case of macros we may have already passed the operator position.
    			if (scribe.scanner.getCurrentPosition() < node.getOperand2().getFileLocation().getNodeOffset()) {
	    			// Operator
	    			final int nextToken= peekNextToken();
	    			// In case of C++ alternative operators, like 'and', 'not', etc. a space
	    			boolean forceSpace= Character.isJavaIdentifierStart(peekNextChar());

					switch (node.getOperator()) {
					case IASTBinaryExpression.op_pmdot:
					case IASTBinaryExpression.op_pmarrow:
	    				scribe.printNextToken(nextToken, false);
	    				break;
	    			default:
	    				scribe.printNextToken(nextToken, forceSpace || preferences.insert_space_before_binary_operator);
	    				if (forceSpace || preferences.insert_space_after_binary_operator) {
	    					scribe.space();
	    				}
	    			}
    			}

   				// operand 2
   				final IASTExpression op2= node.getOperand2();
   				op2.accept(this);

    			ok = true;
    		} catch (AlignmentException e) {
    			scribe.redoAlignment(e);
    		}
    	} while (!ok);
    	scribe.exitAlignment(expressionAlignment, true);
    	return PROCESS_SKIP;
	}

	private int formatAssignment(IASTBinaryExpression node) {
		final IASTExpression op1= node.getOperand1();
		// operand 1
		op1.accept(this);

		// In case of macros we may have already passed the equal sign position.
		if (scribe.scanner.getCurrentPosition() < node.getOperand2().getFileLocation().getNodeOffset()) {
			// Operator
			final int nextToken= peekNextToken();
			// In case of C++ alternative operators, like 'and', 'not', etc. a space
			boolean forceSpace= Character.isJavaIdentifierStart(peekNextChar());

			scribe.printNextToken(nextToken, forceSpace || preferences.insert_space_before_assignment_operator);
			if (forceSpace || preferences.insert_space_after_assignment_operator) {
				scribe.space();
			}
		}

    	Alignment expressionAlignment= scribe.createAlignment(
    			Alignment.ASSIGNMENT_EXPRESSION,
    			preferences.alignment_for_assignment,
    			Alignment.R_OUTERMOST,
    			1,
    			scribe.scanner.getCurrentPosition());

    	scribe.enterAlignment(expressionAlignment);
    	boolean ok = false;
    	do {
    		try {
    			scribe.alignFragment(expressionAlignment, 0);

   				// operand 2
   				final IASTExpression op2= node.getOperand2();
   				op2.accept(this);

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

	private int visit(IASTLiteralExpression node) {
		if (node.getKind() == IASTLiteralExpression.lk_string_literal) {
			// handle concatenation of string literals
			int token;
			boolean needSpace= false;
			final int line= scribe.line;
			boolean indented= false;
			try {
				int[] stringLiterals = { Token.tSTRING, Token.tLSTRING, Token.tRSTRING };
				while (true) {
					scribe.printNextToken(stringLiterals, needSpace);
					token= peekNextToken();
					if (token != Token.tSTRING && token != Token.tLSTRING && token != Token.tRSTRING) {
						break;
					}
					scribe.printCommentPreservingNewLines();
					if (!indented && line != scribe.line) {
						indented= true;
						scribe.indentForContinuation();
					}
					needSpace= true;
				}
			} finally {
				if (indented) {
					scribe.unIndentForContinuation();
				}
			}
		} else {
			scribe.printNextToken(peekNextToken());
		}
    	return PROCESS_SKIP;
	}

	private int visit(IASTFieldReference node) {
		IASTExpression expr= node.getFieldOwner();
		if (expr != null) {
			Runnable tailFormatter = scribe.takeTailFormatter();
			try {
				expr.accept(this);
			} finally {
				scribe.setTailFormatter(tailFormatter);
			}
		}
		final IASTName fieldName= node.getFieldName();
		if (fieldName != null) {
	    	Alignment alignment= scribe.createAlignment(
	    			Alignment.FIELD_REFERENCE,
	    			preferences.alignment_for_member_access,
	    			Alignment.R_OUTERMOST,
	    			1,
	    			scribe.scanner.getCurrentPosition());

	    	scribe.enterAlignment(alignment);
	    	boolean ok = false;
	    	do {
	    		try {
	    			scribe.alignFragment(alignment, 0);

					final int operatorToken= node.isPointerDereference() ? Token.tARROW : Token.tDOT;
					scribe.printComment();
					scribe.printNextToken(operatorToken, false);
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

	private int visit(ICASTTypeIdInitializerExpression node) {
		scribe.printComment();
		final int line= scribe.line;

		node.getTypeId().accept(this);

        final String brace_position= preferences.brace_position_for_initializer_list;
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

		// placement
		final IASTInitializerClause[] newPlacement= node.getPlacementArguments();
		if (newPlacement != null) {
			formatFunctionCallArguments(newPlacement);
		}

		// type-id
		scribe.space();
		final IASTTypeId typeId= node.getTypeId();
		final boolean expectParen= !node.isNewTypeId() && peekNextToken() == Token.tLPAREN;
		if (expectParen) {
			scribe.printNextToken(Token.tLPAREN, false);
		}
		typeId.accept(this);
		if (expectParen) {
			scribe.printNextToken(Token.tRPAREN);
		}

		// initializer
		final IASTInitializer newInitializer= node.getInitializer();
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
		scribe.printNextToken(Token.t_continue);
		scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon);
		scribe.printTrailingComment();
		return PROCESS_SKIP;
	}

	private int visit(IASTDoStatement node) {
		scribe.printNextToken(Token.t_do);
		final int line = scribe.line;

		final IASTStatement action = node.getBody();
		formatAction(line, action, preferences.brace_position_for_block);

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
		scribe.printTrailingComment();
		return PROCESS_SKIP;
	}

	private int visit(IASTNullStatement node) {
		if (!fInsideFor) {
			scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon);
			scribe.printTrailingComment();
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTDeclarationStatement node) {
		node.getDeclaration().accept(this);
		if (!fInsideFor) {
			scribe.startNewLine();
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTExpressionStatement node) {
		Runnable semicolonFormatter = null;
		if (!fInsideFor) {
			semicolonFormatter = new TrailingSemicolonFormatter(node);
			scribe.setTailFormatter(semicolonFormatter);
		}
		node.getExpression().accept(this);
		if (semicolonFormatter != null) {
			semicolonFormatter.run();
			scribe.setTailFormatter(null);
		}
		if (!fInsideFor) {
			scribe.startNewLine();
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTForStatement node) {
		scribe.printNextToken(Token.t_for);
	    final int line = scribe.line;
	    scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_for);
		fInsideFor= true;
		if (preferences.insert_space_after_opening_paren_in_for) {
			scribe.space();
		}
		IASTStatement initializerStmt= node.getInitializerStatement();
		initializerStmt.accept(this);
		if (peekNextToken() == Token.tSEMI) {
			scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon_in_for);
		}

		Alignment alignment = scribe.createAlignment(
				Alignment.FOR,
				Alignment.M_COMPACT_SPLIT,
				Alignment.R_OUTERMOST,
				2,
				scribe.scanner.getCurrentPosition());
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

					scribe.alignFragment(alignment, 1);
					IASTExpression iterationExpr= node.getIterationExpression();
					if (iterationExpr != null) {
						if (preferences.insert_space_after_semicolon_in_for) {
							scribe.space();
						}
						iterationExpr.accept(this);
					}
				} finally {
					fInsideFor= false;
				}
				if (peekNextToken() == Token.tRPAREN) {
					scribe.printNextToken(Token.tRPAREN, preferences.insert_space_before_closing_paren_in_for);
				}
				IASTStatement body = node.getBody();
				if (body instanceof IASTCompoundStatement && !startsWithMacroExpansion(body)) {
					formatLeftCurlyBrace(line, preferences.brace_position_for_block);
					if (startNode(body)) {
						try {
							formatBlockOpening((IASTCompoundStatement) body,
									preferences.brace_position_for_block,
									preferences.insert_space_before_opening_brace_in_block);
							ok = true;
							scribe.exitAlignment(alignment, true);
							formatOpenedBlock((IASTCompoundStatement) body,
									preferences.brace_position_for_block,
									preferences.indent_statements_compare_to_block);
						} finally {
							endOfNode(body);
						}
					} else {
						ok = true;
						scribe.exitAlignment(alignment, true);
					}
				} else {
					ok = true;
					scribe.exitAlignment(alignment, true);
					formatAction(line, body, preferences.brace_position_for_block);
				}
			} catch (AlignmentException e) {
				if (ok) {
					throw e;
				}
				scribe.redoAlignment(e);
    		}
    	} while (!ok);

		return PROCESS_SKIP;
	}

	private int visit(ICPPASTRangeBasedForStatement node) {
		scribe.printNextToken(Token.t_for);
		final int line = scribe.line;
		scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_for);
		fInsideFor= true;
		try {
			if (preferences.insert_space_after_opening_paren_in_for) {
				scribe.space();
			}
			IASTDeclaration declaration = node.getDeclaration();
			declaration.accept(this);
			scribe.printNextToken(Token.tCOLON, true /* preferences.insert_space_before_colon_in_for */);
			final IASTInitializerClause initializer = node.getInitializerClause();
			if (true /*preferences.insert_space_after_colon_in_for*/) {
				scribe.space();
			}
			initializer.accept(this);
		} finally {
			fInsideFor= false;
		}
		if (peekNextToken() == Token.tRPAREN) {
			scribe.printNextToken(Token.tRPAREN, preferences.insert_space_before_closing_paren_in_for);
		}

		formatAction(line, node.getBody(), preferences.brace_position_for_block);
		return PROCESS_SKIP;
	}

	private int visit(IASTIfStatement node) {
		scribe.printNextToken(Token.t_if);
        final int line = scribe.line;
        scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_if);
		if (preferences.insert_space_after_opening_paren_in_if) {
			scribe.space();
		}
		IASTExpression condExpr= node.getConditionExpression();
		if (condExpr == null || condExpr instanceof IASTProblemExpression) {
			scribe.skipToToken(Token.tRPAREN);
		} else {
			condExpr.accept(this);
		}
		if (peekNextToken() == Token.tRPAREN) {
			scribe.printNextToken(Token.tRPAREN, preferences.insert_space_before_closing_paren_in_if);
		}
		final IASTStatement thenStatement = node.getThenClause();
		final IASTStatement elseStatement = node.getElseClause();

		boolean thenStatementIsBlock = false;
		if (thenStatement != null) {
			if (thenStatement instanceof IASTCompoundStatement && !startsWithMacroExpansion(thenStatement)) {
				final IASTCompoundStatement block = (IASTCompoundStatement) thenStatement;
				thenStatementIsBlock = true;
				final List<IASTStatement> statements = Arrays.asList(block.getStatements());
				if (isGuardClause(block, statements) && elseStatement == null && preferences.keep_guardian_clause_on_one_line) {
					/*
					 * Need a specific formatting for guard clauses
					 * guard clauses are block with a single return or throw
					 * statement
					 */
					scribe.printNextToken(Token.tLBRACE, preferences.insert_space_before_opening_brace_in_block);
					scribe.space();
					statements.get(0).accept(this);
					scribe.printNextToken(Token.tRBRACE, true);
					scribe.printTrailingComment();
				} else {
					formatLeftCurlyBrace(line, preferences.brace_position_for_block);
					thenStatement.accept(this);
					if (elseStatement != null && preferences.insert_new_line_before_else_in_if_statement) {
						scribe.startNewLine();
					}
				}
			} else {
				if (node.getFileLocation().getNodeOffset() == thenStatement.getFileLocation().getNodeOffset()) {
					startNode(thenStatement);
				}
 				if (elseStatement == null && preferences.keep_simple_if_on_one_line) {
					Alignment compactIfAlignment = scribe.createAlignment(
							Alignment.COMPACT_IF,
							preferences.alignment_for_compact_if,
							Alignment.R_OUTERMOST,
							1,
							scribe.scanner.getCurrentPosition(),
							1,
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
			if (peekNextToken() == Token.t_else) {
				if (thenStatementIsBlock) {
					scribe.printNextToken(Token.t_else, preferences.insert_space_after_closing_brace_in_block);
				} else {
					scribe.printNextToken(Token.t_else, true);
				}
			}

			if (elseStatement instanceof IASTCompoundStatement) {
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
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTQualifiedName node) {
		if (node.isFullyQualified()) {
			scribe.printNextToken(Token.tCOLONCOLON);
		}
		IASTName[] names= node.getNames();
		for (int i = 0; i < names.length - 1; i++) {
			names[i].accept(this);
			scribe.printNextToken(Token.tCOLONCOLON);
		}
		if (peekNextToken() == Token.tCOMPL) {
			// destructor
			scribe.printNextToken(Token.tCOMPL, false);
		}
		names[names.length - 1].accept(this);
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTTemplateId node) {
		IASTName name= node.getTemplateName();
		name.accept(this);
		scribe.printNextToken(Token.tLT, preferences.insert_space_before_opening_angle_bracket_in_template_arguments);
		if (preferences.insert_space_after_opening_angle_bracket_in_template_arguments) {
			scribe.space();
		}
		int angleBrackets = fOpenAngleBrackets++;
		final IASTNode[] templateArguments= node.getTemplateArguments();
		if (templateArguments.length > 0) {
			final ListOptions options= new ListOptions(Alignment.M_COMPACT_SPLIT);
			options.fSpaceAfterComma= preferences.insert_space_after_comma_in_template_arguments;
			options.fSpaceBeforeComma= preferences.insert_space_before_comma_in_template_arguments;
			formatList(Arrays.asList(templateArguments), options, false, false, null);
		}
		if (peekNextToken() == Token.tSHIFTR) {
			if (fOpenAngleBrackets == angleBrackets + 2) {
				fOpenAngleBrackets -= 2;
				scribe.printNextToken(Token.tSHIFTR, preferences.insert_space_before_closing_angle_bracket_in_template_arguments);
			} else {
				scribe.printComment();
				if (preferences.insert_space_before_closing_angle_bracket_in_template_arguments) {
					scribe.space();
				}
				return PROCESS_SKIP;
			}
		} else {
			--fOpenAngleBrackets;
			scribe.printNextToken(Token.tGT, preferences.insert_space_before_closing_angle_bracket_in_template_arguments);
		}
		int nextToken= peekNextToken();
		if (node.getPropertyInParent() != ICPPASTQualifiedName.SEGMENT_NAME || nextToken == Token.tGT) {
			if (preferences.insert_space_after_closing_angle_bracket_in_template_arguments) {
				// avoid explicit space if followed by pointer operator
				if (nextToken != Token.tSTAR && nextToken != Token.tAMPER) {
					scribe.space();
				}
			} else {
				scribe.printComment();
				scribe.needSpace= false;
				scribe.pendingSpace= false;
			}
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTReturnStatement node) {
		scribe.printNextToken(Token.t_return);
		final IASTExpression expression = node.getReturnValue();
		if (expression != null) {
//			if (peekNextToken() != Token.tLPAREN) {
				scribe.space();
//			}
			expression.accept(this);
		}
		// sometimes the return expression is null, when it should not
		if (expression == null && Token.tSEMI != peekNextToken()) {
			scribe.skipToToken(Token.tSEMI);
		}
		scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon);
		scribe.printTrailingComment();
		return PROCESS_SKIP;
	}

	private int visit(IASTLabelStatement node) {
		// TLETODO [formatter] label indentation
		node.getName().accept(this);
		scribe.printNextToken(Token.tCOLON, preferences.insert_space_before_colon_in_labeled_statement);
		if (preferences.insert_space_after_colon_in_labeled_statement) {
			scribe.space();
		}
		node.getNestedStatement().accept(this);
		return PROCESS_SKIP;
	}

	private int visit(IASTCaseStatement node) {
		IASTExpression constant = node.getExpression();
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
		scribe.printNextToken(Token.t_default);
		scribe.printNextToken(Token.tCOLON, preferences.insert_space_before_colon_in_default);
		return PROCESS_SKIP;
	}

	private int visit(IASTSwitchStatement node) {
		final int headerIndent= scribe.numberOfIndentations;
		scribe.printNextToken(Token.t_switch);
		scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_switch);

		if (preferences.insert_space_after_opening_paren_in_switch) {
			scribe.space();
		}

		node.getControllerExpression().accept(this);
		scribe.printNextToken(Token.tRPAREN, preferences.insert_space_before_closing_paren_in_switch);
		/*
		 * switch body
		 */
		String switch_brace = preferences.brace_position_for_switch;
		formatOpeningBrace(switch_brace, preferences.insert_space_before_opening_brace_in_switch);
		scribe.startNewLine();
		final int braceIndent= scribe.numberOfIndentations;
		if (braceIndent > headerIndent) {
			scribe.unIndent();
		}
		if (preferences.indent_switchstatements_compare_to_switch) {
			scribe.indent();
		}
		IASTStatement bodyStmt= node.getBody();
		final List<IASTStatement> statements;
		if (bodyStmt instanceof IASTCompoundStatement) {
			statements= Arrays.asList(((IASTCompoundStatement) bodyStmt).getStatements());
		} else {
			statements= Collections.singletonList(bodyStmt);
		}
		if (!startNode(bodyStmt)) { return PROCESS_SKIP; }
		try {
			final int statementsLength = statements.size();
			if (statementsLength != 0) {
				boolean wasACase = false;
				boolean wasAStatement = false;
				for (int i = 0; i < statementsLength; i++) {
					final IASTStatement statement = statements.get(i);
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
							bracePosition =	preferences.brace_position_for_block_in_case;
							try {
								startNode(statement);
								formatBlock((IASTCompoundStatement) statement, bracePosition,
										preferences.insert_space_after_colon_in_case,
										preferences.indent_statements_compare_to_block);
							} catch (ASTProblemException e) {
								if (i < statementsLength - 1) {
									final IASTStatement nextStatement= statements.get(i + 1);
									skipToNode(nextStatement);
								}
							} finally {
								endOfNode(statement);
							}
							if (preferences.indent_switchstatements_compare_to_cases) {
								scribe.indent();
							}
						} else {
							bracePosition =	preferences.brace_position_for_block;
							try {
								startNode(statement);
								formatBlock((IASTCompoundStatement) statement, bracePosition,
										preferences.insert_space_before_opening_brace_in_block,
										preferences.indent_statements_compare_to_block);
							} catch (ASTProblemException e) {
								if (i < statementsLength - 1) {
									final IASTStatement nextStatement= statements.get(i + 1);
									skipToNode(nextStatement);
								}
							} finally {
								endOfNode(statement);
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
								final IASTStatement nextStatement= statements.get(i + 1);
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

			if (preferences.indent_switchstatements_compare_to_switch) {
				scribe.unIndent();
			}
			if (scribe.numberOfIndentations < braceIndent) {
				scribe.indent();
			}
			scribe.startNewLine();

			formatClosingBrace(switch_brace);
		} finally {
			endOfNode(bodyStmt);
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTWhileStatement node) {
		scribe.printNextToken(Token.t_while);
		final int line = scribe.line;
		scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_while);

		if (preferences.insert_space_after_opening_paren_in_while) {
			scribe.space();
		}
		final IASTExpression condition= node.getCondition();
		if (condition != null) {
			condition.accept(this);
		} else if (node instanceof ICPPASTWhileStatement) {
			final IASTDeclaration conditionDecl= ((ICPPASTWhileStatement) node).getConditionDeclaration();
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
	private boolean startNode(IASTNode node) {
		scribe.startNode();
		if (node instanceof IASTProblemHolder) {
			return false;
		}
		IASTNodeLocation[] locations= node.getNodeLocations();
		if (locations.length == 0) {
		} else if (locations[0] instanceof IASTMacroExpansionLocation) {
			IASTFileLocation expansionLocation= locations[0].asFileLocation();
			int startOffset= expansionLocation.getNodeOffset();
			int endOffset= startOffset + expansionLocation.getNodeLength();
			scribe.skipRange(startOffset, endOffset);
			if (locations.length == 1 && endOffset <= scribe.scanner.getCurrentPosition()) {
				scribe.restartAtOffset(endOffset);
				continueNode(node.getParent());
				return false;
			}
		} else {
			IASTFileLocation fileLocation= node.getFileLocation();
			scribe.restartAtOffset(fileLocation.getNodeOffset());
		}
		return true;
	}

	/**
	 * Formatting of node is complete. Undo skip region if any.
	 * 
	 * @param node
	 */
	private void endOfNode(IASTNode node) {
		if (node instanceof IASTProblemHolder) {
			return;
		}
		if (scribe.skipRange()) {
			IASTFileLocation fileLocation= node.getFileLocation();
			if (fileLocation != null) {
				int nodeEndOffset= fileLocation.getNodeOffset() + fileLocation.getNodeLength();
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
		IASTFileLocation fileLocation= node.getFileLocation();
		if (fileLocation == null) {
			return;
		}
		int nodeOffset= fileLocation.getNodeOffset();
		int nodeEndOffset= nodeOffset + fileLocation.getNodeLength();
		int currentOffset= scribe.scanner.getCurrentPosition();
		if (currentOffset > nodeEndOffset) {
			return;
		}
		IASTNodeLocation[] locations= node.getNodeLocations();
		for (int i= 0; i < locations.length; i++) {
			IASTNodeLocation nodeLocation= locations[i];
			if (nodeLocation instanceof IASTMacroExpansionLocation) {
				IASTFileLocation expansionLocation= nodeLocation.asFileLocation();
				int startOffset= expansionLocation.getNodeOffset();
				int endOffset= startOffset + expansionLocation.getNodeLength();
				if (currentOffset >= startOffset) {
					if (currentOffset < endOffset) {
						scribe.skipRange(startOffset, endOffset);
						break;
					} else if (currentOffset == endOffset && i == locations.length - 1) {
						scribe.skipRange(startOffset, endOffset);
						break;
					}
				} else {
					break;
				}
			}
		}
	}

	private int getNextTokenOffset() {
		localScanner.resetTo(scribe.scanner.getCurrentPosition(), scribe.scannerEndPosition - 1);
		localScanner.getNextToken();
		return localScanner.getCurrentTokenStartPosition();
	}

	private void skipNode(IASTNode node) {
		final IASTNodeLocation fileLocation= node.getFileLocation();
		if (fileLocation != null && fileLocation.getNodeLength() > 0) {
			final int endOffset= fileLocation.getNodeOffset() + fileLocation.getNodeLength();
			final int currentOffset= scribe.scanner.getCurrentPosition();
			final int restLength= endOffset - currentOffset;
			if (restLength > 0) {
				scribe.printRaw(currentOffset, restLength);
			}
		}
	}

	private void skipToNode(IASTNode node) {
		final IASTNodeLocation fileLocation= node.getFileLocation();
		if (fileLocation != null) {
			final int startOffset= fileLocation.getNodeOffset();
			final int currentOffset= scribe.scanner.getCurrentPosition();
			final int restLength= startOffset - currentOffset;
			if (restLength > 0) {
				scribe.printRaw(currentOffset, restLength);
			}
		}
	}

	private void skipNonWhitespaceToNode(IASTNode node) {
		final IASTNodeLocation fileLocation= node.getFileLocation();
		if (fileLocation != null) {
			final int startOffset= fileLocation.getNodeOffset();
			final int nextTokenOffset= getNextTokenOffset();
			if (nextTokenOffset < startOffset) {
				final int currentOffset= scribe.scanner.getCurrentPosition();
				final int restLength= startOffset - currentOffset;
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
		if (preferences.insert_space_after_opening_paren_in_parenthesized_expression ) {
			scribe.space();
		}
		if (operand != null) {
			operand.accept(this);
		}
		if (peekNextToken() != Token.tRPAREN) {
			if (!enclosedInMacroExpansion(operand)) {
				scribe.skipToToken(Token.tRPAREN);
			}
		}
		if (peekNextToken() == Token.tRPAREN) {
			scribe.printNextToken(Token.tRPAREN, preferences.insert_space_before_closing_paren_in_parenthesized_expression);
		}
	}

	private void formatAction(final int line, final IASTStatement stmt, String brace_position) {
		if (stmt != null) {
			if (stmt instanceof IASTCompoundStatement && !startsWithMacroExpansion(stmt)) {
				formatLeftCurlyBrace(line, brace_position);
				if (startNode(stmt)) {
					try {
						formatBlock((IASTCompoundStatement) stmt, brace_position,
								preferences.insert_space_before_opening_brace_in_block,
								preferences.indent_statements_compare_to_block);
					} finally {
						endOfNode(stmt);
					}
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
				if (scribe.scanner.getCurrentPosition() <= stmt.getFileLocation().getNodeOffset()) {
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

	private static boolean startsWithMacroExpansion(IASTNode node) {
		IASTNodeLocation[] locations= node.getNodeLocations();
		if (locations.length == 0) {
		} else if (node instanceof IASTProblemHolder) {
		} else if (locations[0] instanceof IASTMacroExpansionLocation) {
			IASTFileLocation expansionLocation= locations[0].asFileLocation();
			IASTFileLocation fileLocation= node.getFileLocation();
			return expansionLocation.getNodeOffset() == fileLocation.getNodeOffset();
		}
		return false;
	}

	private static boolean endsWithMacroExpansion(IASTNode node) {
		IASTNodeLocation[] locations= node.getNodeLocations();
		if (locations.length == 0) {
		} else if (node instanceof IASTProblemHolder) {
		} else if (locations[locations.length - 1] instanceof IASTMacroExpansionLocation) {
			return true;
		}
		return false;
	}

	private static boolean enclosedInMacroExpansion(IASTNode node) {
		IASTNodeLocation[] locations= node.getNodeLocations();
		return locations.length == 1 && locations[0] instanceof IASTMacroExpansionLocation;
	}

	private static boolean withinMacroExpansion(IASTNode node, int offset) {
		IASTNodeLocation[] locations= node.getNodeLocations();
		for (IASTNodeLocation location : locations) {
			if (location instanceof IASTMacroExpansionLocation) {
				IASTFileLocation fileLocation = location.asFileLocation();
				if (fileLocation != null) {
					final int nodeOffset = fileLocation.getNodeOffset();
					final int endOffset = nodeOffset + fileLocation.getNodeLength();
					if (offset >= nodeOffset && offset < endOffset) {
						return true;
					} else if (offset < nodeOffset) {
						return false;
					}
				}
			}
		}
		return false;
	}

	private void formatBlock(IASTCompoundStatement block, String block_brace_position,
			boolean insertSpaceBeforeOpeningBrace, boolean indentStatements) {
		formatBlockOpening(block, block_brace_position, insertSpaceBeforeOpeningBrace);
		formatOpenedBlock(block, block_brace_position, indentStatements);
	}

	private void formatBlockOpening(IASTCompoundStatement block, String block_brace_position,
			boolean insertSpaceBeforeOpeningBrace) {
		if (!startsWithMacroExpansion(block)) {
			if (scribe.scanner.getCurrentPosition() <= block.getFileLocation().getNodeOffset()) {
				formatOpeningBrace(block_brace_position, insertSpaceBeforeOpeningBrace);
			}
		} else {
			scribe.startNewLine();
			scribe.printComment();
		}
	}

	private void formatOpenedBlock(IASTCompoundStatement block, String block_brace_position,
			boolean indentStatements) {
		final boolean endsWithMacroExpansion= endsWithMacroExpansion(block);
		IASTStatement[] statements = block.getStatements();
		final int statementsLength = statements.length;
		if (statementsLength != 0) {
			scribe.startNewLine();
			if (indentStatements) {
				scribe.indent();
			}
			formatStatements(Arrays.asList(statements), !endsWithMacroExpansion);
		} else {
			if (preferences.insert_new_line_in_empty_block) {
				scribe.startNewLine();
			}
			if (indentStatements) {
				scribe.indent();
			}
		}
		scribe.printComment();

		if (indentStatements) {
			scribe.unIndent();
		}
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
		scribe.printNextToken(Token.tLBRACE, insertSpaceBeforeBrace);
		scribe.printTrailingComment();
	}

	private void formatClosingBrace(String brace_position) {
		scribe.printNextToken(Token.tRBRACE);
		scribe.printTrailingComment();
		if (DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(brace_position)) {
			scribe.unIndent();
		}
	}

	private void formatStatements(final List<IASTStatement> statements, boolean insertNewLineAfterLastStatement) {
		final int statementsLength= statements.size();
		if (statementsLength > 1) {
			IASTStatement previousStatement= statements.get(0);
			try {
				previousStatement.accept(this);
			} catch (ASTProblemException e) {
				skipToNode(statements.get(1));
			}
			final int indentLevel= scribe.indentationLevel;
			for (int i = 1; i < statementsLength - 1; i++) {
				final IASTStatement statement= statements.get(i);
				if (!startNode(statement)) {
					continue;
				}
				final boolean statementIsNullStmt= statement instanceof IASTNullStatement;
				if (!statementIsNullStmt) {
					scribe.startNewLine();
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
				previousStatement= statement;
			}
			final IASTStatement statement= statements.get(statementsLength - 1);
			final boolean statementIsNullStmt= statement instanceof IASTNullStatement;
			if (!statementIsNullStmt) {
				scribe.startNewLine();
			}
			statement.accept(this);
		} else {
			final IASTStatement statement= statements.get(0);
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
			char[] text= localScanner.getCurrentTokenSource();
			if (text.length > 0) {
				return text[0];
			}
		}
		return 0;
	}

	private int peekNextToken() {
		return peekNextToken(false);
	}

	private int peekNextToken(boolean ignoreSkip) {
		if (!ignoreSkip && scribe.shouldSkip(scribe.scanner.getCurrentPosition())) {
			return Token.tBADCHAR;
		}
		localScanner.resetTo(scribe.scanner.getCurrentPosition(), scribe.scannerEndPosition - 1);
		int token = localScanner.getNextToken();
		loop: while (true) {
			switch (token) {
			case Token.tBLOCKCOMMENT:
			case Token.tLINECOMMENT:
				token = localScanner.getNextToken();
				continue loop;
			default:
				break loop;
			}
		}
		return token;
	}

	private boolean isGuardClause(IASTCompoundStatement block, List<IASTStatement> statements) {
		IASTNodeLocation fileLocation= block.getFileLocation();
		if (fileLocation == null) {
			return false;
		}
		int blockStartPosition= block.getFileLocation().getNodeOffset();
		int blockLength= block.getFileLocation().getNodeLength();
		if (commentStartsBlock(blockStartPosition, blockLength)) return false;
		final int statementsLength = statements.size();
		if (statementsLength != 1) return false;
		if (statements.get(0) instanceof IASTReturnStatement) {
			return true;
		}
		return false;
	}

	/**
	 * Collect source positions of preprocessor-hidden branches
	 * in the given translation unit.
	 *
	 * @param translationUnit  the {@link IASTTranslationUnit}, may be <code>null</code>
	 * @return a {@link List} of {@link Position}s
	 */
	private static List<Position> collectInactiveCodePositions(IASTTranslationUnit translationUnit) {
		if (translationUnit == null) {
			return Collections.emptyList();
		}
		String fileName = translationUnit.getFilePath();
		if (fileName == null) {
			return Collections.emptyList();
		}
		List<Position> positions = new ArrayList<Position>();
		int inactiveCodeStart = -1;
		boolean inInactiveCode = false;
		Stack<Boolean> inactiveCodeStack = new Stack<Boolean>();

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
					positions.add(new Position(inactiveCodeStart, inactiveCodeEnd - inactiveCodeStart));
					inInactiveCode = false;
				}
			} else if (statement instanceof IASTPreprocessorElifStatement) {
				IASTPreprocessorElifStatement elifStmt = (IASTPreprocessorElifStatement) statement;
				if (!elifStmt.taken() && !inInactiveCode) {
					inactiveCodeStart = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
					inInactiveCode = true;
				} else if (elifStmt.taken() && inInactiveCode) {
					int inactiveCodeEnd = nodeLocation.getNodeOffset();
					positions.add(new Position(inactiveCodeStart, inactiveCodeEnd - inactiveCodeStart));
					inInactiveCode = false;
				}
			} else if (statement instanceof IASTPreprocessorEndifStatement) {
				try {
					boolean wasInInactiveCode = inactiveCodeStack.pop().booleanValue();
					if (inInactiveCode && !wasInInactiveCode) {
						int inactiveCodeEnd = nodeLocation.getNodeOffset();
						positions.add(new Position(inactiveCodeStart, inactiveCodeEnd - inactiveCodeStart));
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
		return node instanceof ICPPASTFunctionDefinition &&
				((ICPPASTFunctionDefinition) node).getMemberInitializers().length > 0;
	}
}

/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.formatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
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
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansion;
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
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
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
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisiblityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
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
public class CodeFormatterVisitor extends CPPASTVisitor {

	private static boolean DEBUG = "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.cdt.core/debug/formatter")); //$NON-NLS-1$ //$NON-NLS-2$

	private static class ASTProblemException extends RuntimeException {
		private static final long serialVersionUID= 1L;
		private IASTProblem fProblem;
		ASTProblemException(IASTProblem problem) {
			super();
			fProblem= problem;
		}
		/*
		 * @see java.lang.Throwable#getMessage()
		 */
		public String getMessage() {
			String message= fProblem.getMessage();
			if (fProblem.getFileLocation() != null) {
				int line= fProblem.getFileLocation().getStartingLineNumber();
				message += " (line " + line + ')'; //$NON-NLS-1$
			}
			return message;
		}
	}

	private static class ListAlignment {
		public int fMode;
		public boolean fSpaceBeforeComma;
		public boolean fSpaceAfterComma= true;
		public boolean fSpaceAfterOpeningParen;
		public boolean fSpaceBeforeClosingParen;
		public boolean fSpaceBeforeOpeningParen;
		public int fContinuationIndentation= -1;
		public ListAlignment(int mode) {
			fMode= mode;
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
		shouldVisitProblems = true;

		shouldVisitBaseSpecifiers = true;
		shouldVisitNamespaces = true;
		shouldVisitTemplateParameters = true;
	}

	private Scanner localScanner;

	public DefaultCodeFormatterOptions preferences;

	public Scribe scribe;
	private String fTranslationUnitFile;

	private boolean fInsideFor;

	private MultiStatus fStatus;

	public CodeFormatterVisitor(DefaultCodeFormatterOptions preferences, Map settings, int offset, int length) {
		localScanner = new Scanner() {
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

		long startTime = System.currentTimeMillis();

		final char[] compilationUnitSource = string.toCharArray();

		localScanner.setSource(compilationUnitSource);
		scribe.initializeScanner(compilationUnitSource);
		scribe.setSkipPositions(collectInactiveCodePositions(unit));

		fTranslationUnitFile= unit.getFilePath();
		fStatus= new MultiStatus(CCorePlugin.PLUGIN_ID, 0, "Formatting problem(s)", null); //$NON-NLS-1$
		try {
			unit.accept(this);
		} catch (RuntimeException e) {
			reportFormattingProblem(e);
			if (DEBUG) return failedToFormat(e);
		}
		if (DEBUG){
			System.out.println("Formatting time: " + (System.currentTimeMillis() - startTime));  //$NON-NLS-1$
		}
		return scribe.getRootEdit();
	}

	/**
	 * @return the status collected during formatting
	 */
	IStatus getStatus() {
		return fStatus;
	}
	
	private final TextEdit failedToFormat(RuntimeException e) {
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
	public int visit(IASTTranslationUnit tu) {
		// fake new line
		scribe.lastNumberOfNewLines = 1;
		scribe.startNewLine();
		final int indentLevel= scribe.indentationLevel;
		IASTDeclaration[] decls= tu.getDeclarations();
		for (int i = 0; i < decls.length; i++) {
			IASTDeclaration declaration = decls[i];
			if (!fTranslationUnitFile.equals(declaration.getContainingFilename())) {
				continue;
			}
			try {
				declaration.accept(this);
				scribe.startNewLine();
			} catch (RuntimeException e) {
				// report, but continue
				reportFormattingProblem(e);
				if (i < decls.length - 1) {
					exitAlignments();
					skipToNode(decls[i+1]);
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

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
	 */
	public int visit(IASTDeclaration node) {
		int indentLevel= scribe.indentationLevel;
		try {
    		if (node.getNodeLocations()[0] instanceof IASTMacroExpansion) {
    			skipNode(node);
    		} else
    		if (node instanceof IASTFunctionDefinition) {
    			return visit((IASTFunctionDefinition)node);
    		} else if (node instanceof IASTSimpleDeclaration) {
    			return visit((IASTSimpleDeclaration)node);
    		} else if (node instanceof IASTASMDeclaration) {
    			return visit((IASTASMDeclaration)node);
    		} else if (node instanceof ICPPASTVisiblityLabel) {
    			return visit((ICPPASTVisiblityLabel)node);
    		} else if (node instanceof ICPPASTNamespaceDefinition) {
    			return visit((ICPPASTNamespaceDefinition)node);
    		} else if (node instanceof ICPPASTNamespaceAlias) {
    			formatNode(node);
    //			return visit((ICPPASTNamespaceAlias)declaration);
    		} else if (node instanceof ICPPASTUsingDeclaration) {
    			formatNode(node);
    //			return visit((ICPPASTUsingDeclaration)declaration);
    		} else if (node instanceof ICPPASTUsingDirective) {
    			formatNode(node);
    //			return visit((ICPPASTUsingDirective)declaration);
    		} else if (node instanceof ICPPASTLinkageSpecification) {
    			return visit((ICPPASTLinkageSpecification)node);
    		} else if (node instanceof ICPPASTTemplateDeclaration) {
    			return visit((ICPPASTTemplateDeclaration)node);
    		} else if (node instanceof ICPPASTTemplateSpecialization) {
    			return visit((ICPPASTTemplateSpecialization)node);
    		} else if (node instanceof ICPPASTExplicitTemplateInstantiation) {
    			return visit((ICPPASTExplicitTemplateInstantiation)node);
    		} else if (node instanceof IASTProblemDeclaration) {
    			return visit((IASTProblemDeclaration)node);
    		} else {
    			formatNode(node);
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
	public int visit(IASTName node) {
		IASTNodeLocation[] locations= node.getNodeLocations();
		if (locations.length == 0) {
			return PROCESS_SKIP;
		} else if (locations[0] instanceof IASTMacroExpansion) {
			formatNode(node);
		} else
		if (node instanceof ICPPASTQualifiedName) {
			visit((ICPPASTQualifiedName)node);
		} else if (node instanceof ICPPASTTemplateId) {
			visit((ICPPASTTemplateId)node);
		} else {
			formatNode(node);
		}
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTInitializer)
	 */
	public int visit(IASTInitializer node) {
		IASTNodeLocation[] locations= node.getNodeLocations();
		if (locations.length == 0) {
			return PROCESS_SKIP;
		} else if (locations[0] instanceof IASTMacroExpansion) {
			formatNode(node);
			return PROCESS_SKIP;
		}
		if (node instanceof ICPPASTConstructorInitializer) {
			visit((ICPPASTConstructorInitializer)node);
			return PROCESS_SKIP;
		}

		if (peekNextToken() == Token.tASSIGN) {
			scribe.printNextToken(Token.tASSIGN, scribe.printComment());
			if (scribe.printComment()) {
				scribe.space();
			}
		}
		if (node instanceof IASTInitializerExpression) {
			visit((IASTInitializerExpression)node);
		} else if (node instanceof IASTInitializerList) {
			visit((IASTInitializerList)node);
		} else if (node instanceof ICASTDesignatedInitializer) {
			formatNode(node);
		} else {
			formatNode(node);
		}
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration)
	 */
	public int visit(IASTParameterDeclaration parameterDeclaration) {
		formatNode(parameterDeclaration);
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
	 */
	public int visit(IASTDeclarator node) {
		IASTNodeLocation[] locations= node.getNodeLocations();
		if (locations.length == 0) {
			return PROCESS_SKIP;
		} else if (locations[0] instanceof IASTMacroExpansion) {
			formatNode(node);
			return PROCESS_SKIP;
		}
		
		// common to all declarators
		formatPointers(node.getPointerOperators());
		if (scribe.printComment()) {
			scribe.space();
		}
		IASTName name= node.getName();
		if (name != null) {
			name.accept(this);
		}
		IASTDeclarator nestedDecl= node.getNestedDeclarator();
		if (nestedDecl != null) {
			scribe.printNextToken(Token.tLPAREN, false);
			nestedDecl.accept(this);
			scribe.printNextToken(Token.tRPAREN, false);
		}

		if (node instanceof ICPPASTFunctionTryBlockDeclarator) {
			visit((IASTStandardFunctionDeclarator)node);
			skipNode(node);
			return PROCESS_SKIP;
		} else if (node instanceof ICPPASTFunctionDeclarator) {
			return visit((ICPPASTFunctionDeclarator)node);
		} else if (node instanceof IASTStandardFunctionDeclarator) {
			visit((IASTStandardFunctionDeclarator)node);
		} else if (node instanceof ICASTKnRFunctionDeclarator) {
			visit((ICASTKnRFunctionDeclarator)node);
		} else if (node instanceof IASTFieldDeclarator) {
			visit((IASTFieldDeclarator)node);
		} else if (node instanceof IASTArrayDeclarator) {
			visit((IASTArrayDeclarator)node);
		}
		
		IASTInitializer initializer= node.getInitializer();
		if (initializer != null) {
			initializer.accept(this);
		}
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier)
	 */
	public int visit(IASTDeclSpecifier node) {
		IASTNodeLocation[] locations= node.getNodeLocations();
		if (locations.length == 0) {
			return PROCESS_SKIP;
		} else if (locations[0] instanceof IASTMacroExpansion) {
			formatNode(node);
		} else
		if (node instanceof ICPPASTCompositeTypeSpecifier) {
			visit((ICPPASTCompositeTypeSpecifier)node);
		} else if (node instanceof ICASTCompositeTypeSpecifier) {
			visit((ICASTCompositeTypeSpecifier)node);
		} else if (node instanceof ICPPASTElaboratedTypeSpecifier) {
			visit((ICPPASTElaboratedTypeSpecifier)node);
		} else if (node instanceof ICASTElaboratedTypeSpecifier) {
			visit((ICASTElaboratedTypeSpecifier)node);
		} else if (node instanceof IASTEnumerationSpecifier) {
			visit((IASTEnumerationSpecifier)node);
		} else if (node instanceof IASTSimpleDeclSpecifier) {
			visit((IASTSimpleDeclSpecifier)node);
		} else if (node instanceof IASTNamedTypeSpecifier) {
			visit((IASTNamedTypeSpecifier)node);
		} else {
			formatNode(node);
		}
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTExpression)
	 */
	public int visit(IASTExpression node) {
		IASTNodeLocation[] locations= node.getNodeLocations();
		if (locations.length == 0) {
			return PROCESS_SKIP;
		} else if (locations[0] instanceof IASTMacroExpansion) {
			skipNode(node);
		} else if (locations[0].getNodeOffset()+locations[0].getNodeLength() < scribe.scanner.getCurrentPosition()) {
			return PROCESS_SKIP;
		} else
		if (node instanceof IASTConditionalExpression) {
			visit((IASTConditionalExpression)node);
		} else if (node instanceof IASTFunctionCallExpression) {
			visit((IASTFunctionCallExpression)node);
		} else if (node instanceof IASTExpressionList) {
			visit((IASTExpressionList)node);
		} else if (node instanceof IASTTypeIdExpression) {
			visit((IASTTypeIdExpression)node);
		} else if (node instanceof IASTBinaryExpression) {
			visit((IASTBinaryExpression)node);
		} else if (node instanceof IASTLiteralExpression) {
			visit((IASTLiteralExpression)node);
		} else if (node instanceof IASTIdExpression) {
			visit((IASTIdExpression)node);
		} else if (node instanceof IASTCastExpression) {
			visit((IASTCastExpression)node);
		} else if (node instanceof IASTUnaryExpression) {
			visit((IASTUnaryExpression)node);
		} else if (node instanceof IASTProblemExpression) {
			visit((IASTProblemExpression)node);
		} else {
			formatNode(node);
		}
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTStatement)
	 */
	public int visit(IASTStatement node) {
		scribe.printComment();
		int indentLevel= scribe.indentationLevel;
		IASTNodeLocation[] locations= node.getNodeLocations();
		try {
    		if (locations.length == 0) {
    			return PROCESS_SKIP;
    		} else if (locations[0] instanceof IASTMacroExpansion) {
    			skipNode(node);
    		} else if (locations[0].getNodeOffset()+locations[0].getNodeLength() < scribe.scanner.getCurrentPosition()) {
    			return PROCESS_SKIP;
    		} else
    		if (node instanceof IASTCompoundStatement) {
                visit((IASTCompoundStatement)node);
    		} else if (node instanceof IASTNullStatement) {
    			visit((IASTNullStatement)node);
    		} else if (node instanceof IASTDeclarationStatement) {
    			visit((IASTDeclarationStatement)node);
    		} else if (node instanceof ICPPASTForStatement) {
    			// TLETODO [formatter] handle C++ specifics
    			visit((IASTForStatement)node);
    		} else if (node instanceof IASTForStatement) {
    			visit((IASTForStatement)node);
    		} else if (node instanceof IASTIfStatement) {
    			visit((IASTIfStatement)node);
    		} else if (node instanceof ICPPASTWhileStatement) {
    			// TLETODO [formatter] handle C++ specifics
    			visit((IASTWhileStatement)node);
    		} else if (node instanceof IASTWhileStatement) {
    			visit((IASTWhileStatement)node);
    		} else if (node instanceof IASTDoStatement) {
    			visit((IASTDoStatement)node);
    		} else if (node instanceof IASTSwitchStatement) {
    			visit((IASTSwitchStatement)node);
    		} else if (node instanceof IASTExpressionStatement) {
    			visit((IASTExpressionStatement)node);
    		} else if (node instanceof IASTContinueStatement) {
    			visit((IASTContinueStatement)node);
    		} else if (node instanceof IASTReturnStatement) {
    			visit((IASTReturnStatement)node);
    		} else if (node instanceof IASTBreakStatement) {
    			visit((IASTBreakStatement)node);
    		} else if (node instanceof IASTCaseStatement) {
    			visit((IASTCaseStatement)node);
    		} else if (node instanceof IASTDefaultStatement) {
    			visit((IASTDefaultStatement)node);
    		} else if (node instanceof IASTLabelStatement) {
    			visit((IASTLabelStatement)node);
    		} else if (node instanceof IASTProblemStatement) {
    			visit((IASTProblemStatement)node);
    		} else {
    			formatNode(node);
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
		}
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTTypeId)
	 */
	public int visit(IASTTypeId typeId) {
		formatNode(typeId);
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator)
	 */
	public int visit(IASTEnumerator enumerator) {
		formatNode(enumerator);
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTProblem)
	 */
	public int visit(IASTProblem problem) {
		formatNode(problem);
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor#visit(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier)
	 */
	public int visit(ICPPASTBaseSpecifier specifier) {
		formatNode(specifier);
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor#visit(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition)
	 */
	public int visit(ICPPASTNamespaceDefinition node) {
		scribe.printComment();
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
		for (int i = 0; i < memberDecls.length; i++) {
			IASTDeclaration declaration = memberDecls[i];
			if (declaration instanceof ICPPASTVisiblityLabel) {
				visit((ICPPASTVisiblityLabel)declaration);
			} else {
				declaration.accept(this);
			}
			scribe.startNewLine();
		}
		if (preferences.indent_body_declarations_compare_to_namespace_header) {
			scribe.unIndent();
		}
		formatClosingBrace(preferences.brace_position_for_namespace_declaration);
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
		// TLETODO [formatter] need options for linkage specification
		formatLeftCurlyBrace(line, preferences.brace_position_for_namespace_declaration);
		formatOpeningBrace(preferences.brace_position_for_namespace_declaration, preferences.insert_space_before_opening_brace_in_namespace_declaration);
		if (preferences.indent_body_declarations_compare_to_namespace_header) {
			scribe.indent();
		}
		scribe.startNewLine();
		for (int i = 0; i < memberDecls.length; i++) {
			IASTDeclaration declaration = memberDecls[i];
			if (declaration instanceof ICPPASTVisiblityLabel) {
				visit((ICPPASTVisiblityLabel)declaration);
			} else {
				declaration.accept(this);
			}
			scribe.startNewLine();
		}
		if (preferences.indent_body_declarations_compare_to_namespace_header) {
			scribe.unIndent();
		}
		formatClosingBrace(preferences.brace_position_for_namespace_declaration);
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor#visit(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter)
	 */
	public int visit(ICPPASTTemplateParameter parameter) {
		formatNode(parameter);
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTConstructorInitializer node) {
		scribe.printNextToken(Token.tLPAREN, false);
		final IASTExpression value= node.getExpression();
		if (value != null) {
			value.accept(this);
		}
		scribe.printNextToken(Token.tRPAREN, false);
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTConstructorChainInitializer node) {
		final IASTName member= node.getMemberInitializerId();
		if (member!= null) {
			member.accept(this);
			scribe.printNextToken(Token.tLPAREN, false);
			final IASTExpression value= node.getInitializerValue();
			if (value != null) {
				value.accept(this);
			}
			scribe.printNextToken(Token.tRPAREN, false);
		} else {
			formatNode(node);
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTFunctionDefinition node) {
		scribe.printComment();
		final int line= scribe.line;
		IASTDeclSpecifier declSpec= node.getDeclSpecifier();
		declSpec.accept(this);
		IASTFunctionDeclarator decl= node.getDeclarator();
		boolean needSpace= scribe.printComment() || (decl.getPointerOperators().length == 0 && decl.getNestedDeclarator() == null);
		if (needSpace) {
			scribe.space();
		}
		decl.accept(this);
		IASTStatement bodyStmt= node.getBody();
		if (bodyStmt instanceof IASTCompoundStatement) {
	        formatLeftCurlyBrace(line, preferences.brace_position_for_method_declaration);
			formatBlock((IASTCompoundStatement) bodyStmt,
					preferences.brace_position_for_method_declaration,
					preferences.insert_space_before_opening_brace_in_method_declaration,
					preferences.indent_statements_compare_to_body);
		} else {
			bodyStmt.accept(this);
		}
		scribe.printTrailingComment();
		scribe.startNewLine();
		return PROCESS_SKIP;
	}

	private int visit(IASTASMDeclaration node) {
		formatNode(node);
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTFunctionDeclarator node) {
		visit((IASTStandardFunctionDeclarator)node);

		skipConstVolatile();

		final IASTTypeId[] exceptionSpecification= node.getExceptionSpecification();
		if (exceptionSpecification != null) {
			if (peekNextToken() == Token.t_throw) {
				formatExceptionSpecification(exceptionSpecification);
			}
		}

		final ICPPASTConstructorChainInitializer[] constructorChain= node.getConstructorChain();
		if (constructorChain != null && constructorChain.length > 0) {
			// TLETODO [formatter] need special constructor chain alignment
			scribe.printNextToken(Token.tCOLON, true);
			scribe.printTrailingComment();
			scribe.startNewLine();
			scribe.indent();
			final ListAlignment align= new ListAlignment(Alignment.M_COMPACT_SPLIT);
			formatList(Arrays.asList(constructorChain), align, false, false);
			scribe.unIndent();
		} else {
			// skip the rest (=0)
			skipNode(node);
		}

		return PROCESS_SKIP;
	}

	private void formatExceptionSpecification(final IASTTypeId[] exceptionSpecification) {
		// TLETODO [formatter] need special alignment for exception specification
		if (exceptionSpecification.length > 0) {
			Alignment alignment =scribe.createAlignment(
					"exceptionSpecification", //$NON-NLS-1$
					// need configurable alignment
					Alignment.M_COMPACT_SPLIT,
					exceptionSpecification.length,
					scribe.scanner.getCurrentPosition());
	
			scribe.enterAlignment(alignment);
			boolean ok = false;
			do {
				try {
					scribe.alignFragment(alignment, 0);
					scribe.printNextToken(Token.t_throw, true);
					scribe.printNextToken(Token.tLPAREN, scribe.printComment());
					exceptionSpecification[0].accept(this);
					for (int i = 1; i < exceptionSpecification.length; i++) {
						// insert_space_before_comma_in_method_declaration_throws
						scribe.printNextToken(Token.tCOMMA, preferences.insert_space_before_comma_in_array_initializer);
						scribe.printTrailingComment();
						// insert_space_after_comma_in_method_declaration_throws
						if (preferences.insert_space_after_comma_in_array_initializer) {
							scribe.space();
						}
						scribe.alignFragment(alignment, i);
		    			exceptionSpecification[i].accept(this);
					}
					scribe.printNextToken(Token.tRPAREN, scribe.printComment());
					ok = true;
				} catch (AlignmentException e) {
					scribe.redoAlignment(e);
				}
			} while (!ok);
			scribe.exitAlignment(alignment, true);
		} else {
			scribe.printNextToken(Token.t_throw, true);
			scribe.printNextToken(Token.tLPAREN, scribe.printComment());
			scribe.printNextToken(Token.tRPAREN, scribe.printComment());
		}
	}

	private void skipConstVolatile() {
		int token= peekNextToken();
		while (token == Token.t_const || token == Token.t_volatile) {
			scribe.printNextToken(token, true);
			token= peekNextToken();
		}
	}

	private int visit(IASTStandardFunctionDeclarator node) {
		final List parameters = Arrays.asList(node.getParameters());
		final ListAlignment align= new ListAlignment(preferences.alignment_for_parameters_in_method_declaration);
		align.fSpaceBeforeOpeningParen= preferences.insert_space_before_opening_paren_in_method_declaration;
		align.fSpaceAfterOpeningParen= preferences.insert_space_after_opening_paren_in_method_declaration;
		align.fSpaceBeforeClosingParen= preferences.insert_space_before_closing_paren_in_method_declaration;
		align.fSpaceBeforeComma= preferences.insert_space_before_comma_in_method_declaration_parameters;
		align.fSpaceAfterComma= preferences.insert_space_after_comma_in_method_declaration_parameters;
		formatList(parameters, align, true, node.takesVarArgs());

		return PROCESS_SKIP;
	}

	private void formatPointers(IASTPointerOperator[] pointers) {
		for (int i = 0; i < pointers.length; i++) {
			IASTPointerOperator pointer= pointers[i];
			if (scribe.printComment()) {
				scribe.space();
			}
			scribe.printModifiers();
			if (pointer instanceof ICPPASTReferenceOperator) {
				scribe.printNextToken(Token.tAMPER, false);
			} else if (pointer instanceof ICASTPointer) {
				scribe.printNextToken(Token.tSTAR, false);
			} else if (pointer instanceof ICPPASTPointerToMember) {
				final ICPPASTPointerToMember ptrToMember= (ICPPASTPointerToMember)pointer;
				final IASTName name= ptrToMember.getName();
				if (name != null) {
					name.accept(this);
				}
				scribe.printNextToken(Token.tSTAR, false);
			} else {
				formatNode(pointer);
			}
		}
	}

	private int visit(ICASTKnRFunctionDeclarator node) {
		final List parameters= Arrays.asList(node.getParameterNames());
		ListAlignment align= new ListAlignment(preferences.alignment_for_parameters_in_method_declaration);
		align.fSpaceAfterOpeningParen= preferences.insert_space_after_opening_paren_in_method_declaration;
		align.fSpaceBeforeClosingParen= preferences.insert_space_before_closing_paren_in_method_declaration;
		align.fSpaceBeforeComma= preferences.insert_space_before_comma_in_method_declaration_parameters;
		align.fSpaceAfterComma= preferences.insert_space_after_comma_in_method_declaration_parameters;
		formatList(parameters, align, true, false);

		IASTDeclaration[] parameterDecls= node.getParameterDeclarations();
		scribe.startNewLine();
		scribe.indent();
		try {
    		for (int i = 0; i < parameterDecls.length; i++) {
    			IASTDeclaration declaration = parameterDecls[i];
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
			for (int i = 0; i < arrayModifiers.length; i++) {
				IASTArrayModifier arrayModifier = arrayModifiers[i];
				scribe.printNextToken(Token.tLBRACKET, scribe.printComment());
				try {
					arrayModifier.accept(this);
				} catch (ASTProblemException e) {
					scribe.skipToToken(Token.tRBRACKET);
				}
				scribe.printNextToken(Token.tRBRACKET, scribe.printComment());
			}
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTSimpleDeclaration node) {
		IASTDeclSpecifier declSpec= node.getDeclSpecifier();
		declSpec.accept(this);
		final List declarators= Arrays.asList(node.getDeclarators());
		if (declarators.size() > 0) {
			if (scribe.printComment() || peekNextToken() == Token.tIDENTIFIER) {
				scribe.space();
			}
			final ListAlignment align= new ListAlignment(Alignment.M_COMPACT_SPLIT);
			formatList(declarators, align, false, false);
		}
		if (peekNextToken() != Token.tSEMI) {
			scribe.skipToToken(Token.tSEMI);
		}
		scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon);
		scribe.printTrailingComment();
		return PROCESS_SKIP;
	}

	/**
	 * Test whether the given decl specifier is one of 'class', 'struct', 'union' or 'enum'.
	 * @param declSpec
	 * @return true if the decl specifier is one of 'class', 'struct', 'union' or 'enum'
	 */
	protected boolean isCompositeTypeDeclaration(IASTDeclSpecifier declSpec) {
		return declSpec instanceof IASTCompositeTypeSpecifier || declSpec instanceof ICASTEnumerationSpecifier;
	}

	private int visit(ICPPASTTemplateDeclaration node) {
		scribe.printNextToken(Token.t_template, false);
		scribe.printNextToken(Token.tLT, false);
		final ICPPASTTemplateParameter[] templateParameters= node.getTemplateParameters();
		if (templateParameters.length > 0) {
			final ListAlignment align= new ListAlignment(Alignment.M_COMPACT_SPLIT);
			formatList(Arrays.asList(templateParameters), align, false, false);
		}
		scribe.printNextToken(Token.tGT, false);
		scribe.space();
		node.getDeclaration().accept(this);
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTTemplateSpecialization node) {
		node.getDeclaration().accept(this);
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTExplicitTemplateInstantiation node) {
		node.getDeclaration().accept(this);
		return PROCESS_SKIP;
	}

	private int visit(IASTSimpleDeclSpecifier node) {
		formatNode(node);
		return PROCESS_SKIP;
	}

	private int visit(IASTNamedTypeSpecifier node) {
		formatNode(node);
		return PROCESS_SKIP;
	}

	private int visit(ICASTCompositeTypeSpecifier node) {
		scribe.printComment();
		final int line= scribe.line;

		// storage class and other modifiers
		scribe.printModifiers();

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
		scribe.space();
		node.getName().accept(this);

		// member declarations
		IASTDeclaration[] memberDecls= node.getMembers();
		formatLeftCurlyBrace(line, preferences.brace_position_for_type_declaration);
		formatOpeningBrace(preferences.brace_position_for_type_declaration, preferences.insert_space_before_opening_brace_in_type_declaration);
		if (preferences.indent_body_declarations_compare_to_access_specifier) {
			scribe.indent();
		}
		scribe.startNewLine();
		for (int i = 0; i < memberDecls.length; i++) {
			IASTDeclaration declaration = memberDecls[i];
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

		// storage class and other modifiers
		scribe.printModifiers();
		final int headerIndent= scribe.numberOfIndentations;

		switch (node.getKey()) {
		case IASTCompositeTypeSpecifier.k_struct:
			scribe.printNextToken(Token.t_struct, true);
			break;
		case IASTCompositeTypeSpecifier.k_union:
			scribe.printNextToken(Token.t_union, true);
			break;
		case ICPPASTCompositeTypeSpecifier.k_class:
			scribe.printNextToken(Token.t_class, true);
			break;
		default:
			assert false : "Unexpected composite type specifier"; //$NON-NLS-1$
		}
		scribe.space();
		node.getName().accept(this);

		// base specifiers
		final List baseSpecifiers= Arrays.asList(node.getBaseSpecifiers());
		if (baseSpecifiers.size() > 0) {
			scribe.printNextToken(Token.tCOLON, true /*preferences.insert_space_before_colon_in_composite_type_specifier*/);
			scribe.space();
			final ListAlignment align= new ListAlignment(Alignment.M_COMPACT_SPLIT);
			formatList(baseSpecifiers, align, false, false);
		}

		// member declarations
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
		for (int i = 0; i < memberDecls.length; i++) {
			IASTDeclaration declaration = memberDecls[i];
			if (declaration instanceof ICPPASTVisiblityLabel) {
				if (preferences.indent_body_declarations_compare_to_access_specifier) {
					scribe.indent();
				}
				scribe.printComment();
				if (preferences.indent_body_declarations_compare_to_access_specifier) {
					scribe.unIndent();
				}
				visit((ICPPASTVisiblityLabel)declaration);
			} else {
				if (preferences.indent_body_declarations_compare_to_access_specifier) {
					scribe.indent();
				}
				declaration.accept(this);
				scribe.printComment();
				if (preferences.indent_body_declarations_compare_to_access_specifier) {
					scribe.unIndent();
				}
			}
			scribe.startNewLine();
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

	private int visit(ICPPASTVisiblityLabel node) {
		if (!preferences.indent_access_specifier_compare_to_type_header) {
			scribe.unIndent();
		}
		if (node.getNodeLocations()[0] instanceof IASTMacroExpansion) {
			skipNode(node);
		} else {
			switch (node.getVisibility()) {
			case ICPPASTVisiblityLabel.v_private:
				scribe.printNextToken(Token.t_private, false);
				break;
			case ICPPASTVisiblityLabel.v_protected:
				scribe.printNextToken(Token.t_protected, false);
				break;
			case ICPPASTVisiblityLabel.v_public:
				scribe.printNextToken(Token.t_public, false);
				break;
			}
			if (peekNextToken() != Token.tCOLON) {
				scribe.skipToToken(Token.tCOLON);
			}
			scribe.printNextToken(Token.tCOLON, false/*preferences.insert_space_before_colon_in_visibility_label */);
		}
		if (!preferences.indent_access_specifier_compare_to_type_header) {
			scribe.indent();
		}
		return PROCESS_SKIP;
	}

	private int visit(ICASTElaboratedTypeSpecifier node) {
		formatNode(node);
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTElaboratedTypeSpecifier node) {
		formatNode(node);
		return PROCESS_SKIP;
	}

	private int visit(IASTEnumerationSpecifier node) {
		formatNode(node);
		return PROCESS_SKIP;
	}

	/**
	 * Format a given list of elements according alignment options.
	 *
	 * @param elements
	 * @param align
	 * @param encloseInParen
	 * @param addEllipsis
	 */
	private void formatList(List elements, ListAlignment align, boolean encloseInParen, boolean addEllipsis) {
		if (encloseInParen)
			scribe.printNextToken(Token.tLPAREN, align.fSpaceBeforeOpeningParen);

		final int elementsLength = elements.size();
		if (elementsLength != 0 || addEllipsis) {
			if (align.fSpaceAfterOpeningParen) {
				scribe.space();
			}
			final int continuationIndentation= 
				align.fContinuationIndentation >= 0 
					? align.fContinuationIndentation 
					: preferences.continuation_indentation;
			Alignment listAlignment = scribe.createAlignment(
					"listElements_"+align,//$NON-NLS-1$
					align.fMode,
					elementsLength + (addEllipsis ? 1 : 0),
					scribe.scanner.getCurrentPosition(),
					continuationIndentation,
					false);
			scribe.enterAlignment(listAlignment);
			boolean ok = false;
			do {
				try {
					int i;
					for (i = 0; i < elementsLength; i++) {
						if (i > 0) {
							// handle missing parameter
							int token= peekNextToken();
							if (token == Token.tIDENTIFIER) {
								if (!scribe.skipToToken(Token.tCOMMA)) {
									break;
								}
							} else if (token == Token.tRPAREN) {
								if (encloseInParen) {
									break;
								}
								if (!scribe.skipToToken(Token.tCOMMA)) {
									break;
								}
							}
							scribe.printNextToken(Token.tCOMMA, align.fSpaceBeforeComma);
							scribe.printTrailingComment();
						}
						scribe.alignFragment(listAlignment, i);
						if (i > 0 && align.fSpaceAfterComma) {
							scribe.space();
						}
						final IASTNode node= (IASTNode) elements.get(i);
						if (node instanceof ICPPASTConstructorChainInitializer) {
							// this is a special case
							visit((ICPPASTConstructorChainInitializer)node);
						} else {
							node.accept(this);
						}
					}
					if (addEllipsis) {
						if (i > 0) {
							scribe.printNextToken(Token.tCOMMA, align.fSpaceBeforeComma);
							scribe.printTrailingComment();
						}
						scribe.alignFragment(listAlignment, i);
						if (i > 0 && align.fSpaceAfterComma) {
							scribe.space();
						}
						scribe.printNextToken(Token.tELIPSE);
					}
					ok = true;
				} catch (AlignmentException e) {
					scribe.redoAlignment(e);
				} catch (ASTProblemException e) {
					
				}
			} while (!ok);
			scribe.exitAlignment(listAlignment, true);
		}
		if (encloseInParen) {
			// handle missing parameter
			if (peekNextToken() == Token.tIDENTIFIER) {
				scribe.skipToToken(Token.tRPAREN);
			}
			scribe.printNextToken(Token.tRPAREN, align.fSpaceBeforeClosingParen);
		}
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
		node.getLogicalConditionExpression().accept(this);

    	Alignment conditionalExpressionAlignment =scribe.createAlignment(
    			"conditionalExpression", //$NON-NLS-1$
    			preferences.alignment_for_conditional_expression,
    			2,
    			scribe.scanner.getCurrentPosition());

    	scribe.enterAlignment(conditionalExpressionAlignment);
    	boolean ok = false;
    	do {
    		try {
    			scribe.alignFragment(conditionalExpressionAlignment, 0);
    			scribe.printNextToken(Token.tQUESTION, preferences.insert_space_before_question_in_conditional);

    			if (preferences.insert_space_after_question_in_conditional) {
    				scribe.space();
    			}
    			node.getPositiveResultExpression().accept(this);
    			scribe.printTrailingComment();
    			scribe.alignFragment(conditionalExpressionAlignment, 1);
    			scribe.printNextToken(Token.tCOLON, preferences.insert_space_before_colon_in_conditional);

    			if (preferences.insert_space_after_colon_in_conditional) {
    				scribe.space();
    			}
    			node.getNegativeResultExpression().accept(this);

    			ok = true;
    		} catch (AlignmentException e) {
    			scribe.redoAlignment(e);
    		}
    	} while (!ok);
    	scribe.exitAlignment(conditionalExpressionAlignment, true);
    	return PROCESS_SKIP;
    }

	private int visit(IASTFunctionCallExpression node) {
		node.getFunctionNameExpression().accept(this);
		IASTExpression paramExpr= node.getParameterExpression();
		scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_method_invocation);
		if (preferences.insert_space_after_opening_paren_in_method_invocation) {
			scribe.space();
		}
		if (paramExpr != null) {
			paramExpr.accept(this);
		}
		scribe.printNextToken(Token.tRPAREN, preferences.insert_space_before_closing_paren_in_method_invocation);
		if (scribe.printComment()) {
			scribe.space();
		}
    	return PROCESS_SKIP;
	}

	private int visit(IASTExpressionList node) {
		final List expressions = Arrays.asList(node.getExpressions());
		final ListAlignment align= new ListAlignment(preferences.alignment_for_arguments_in_method_invocation);
		align.fSpaceAfterOpeningParen= preferences.insert_space_after_opening_paren_in_method_invocation;
		align.fSpaceBeforeClosingParen= preferences.insert_space_before_closing_paren_in_method_invocation;
		align.fSpaceBeforeComma= preferences.insert_space_before_comma_in_method_invocation_arguments;
		align.fSpaceAfterComma= preferences.insert_space_after_comma_in_method_invocation_arguments;
		formatList(expressions, align, false, false);
    	return PROCESS_SKIP;
	}

	private int visit(IASTIdExpression node) {
		node.getName().accept(this);
    	return PROCESS_SKIP;
	}

	private int visit(IASTCastExpression node) {
		switch (node.getOperator()) {
		case IASTCastExpression.op_cast:
			scribe.printNextToken(Token.tLPAREN, scribe.printComment());
			if (scribe.printComment()) {
				scribe.space();
			}
			node.getTypeId().accept(this);
			scribe.printNextToken(Token.tRPAREN, scribe.printComment());
			if (scribe.printComment()) {
				scribe.space();
			}
			// operand
			node.getOperand().accept(this);
			if (scribe.printComment()) {
				scribe.space();
			}
			break;
		case ICPPASTCastExpression.op_const_cast:
		case ICPPASTCastExpression.op_dynamic_cast:
		case ICPPASTCastExpression.op_reinterpret_cast:
		case ICPPASTCastExpression.op_static_cast:
			scribe.printNextToken(peekNextToken(), scribe.printComment());
			scribe.printNextToken(Token.tLT, scribe.printComment());
			if (scribe.printComment()) {
				scribe.space();
			}
			node.getTypeId().accept(this);
			scribe.printNextToken(Token.tGT, scribe.printComment());
			// operand
			scribe.printNextToken(Token.tLPAREN, scribe.printComment());
			node.getOperand().accept(this);
			scribe.printNextToken(Token.tRPAREN, scribe.printComment());
			if (scribe.printComment()) {
				scribe.space();
			}
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

	private int visit(IASTInitializerExpression node) {
    	Alignment expressionAlignment =scribe.createAlignment(
    			"assignmentExpression", //$NON-NLS-1$
    			// need configurable alignment
    			Alignment.M_COMPACT_SPLIT,
    			1,
    			scribe.scanner.getCurrentPosition());

    	scribe.enterAlignment(expressionAlignment);
    	boolean ok = false;
    	do {
    		try {
    			scribe.alignFragment(expressionAlignment, 0);

    			// r-value
   				node.getExpression().accept(this);

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

		final List initializers = Arrays.asList(node.getInitializers());
		if (initializers.isEmpty() && preferences.keep_empty_array_initializer_on_one_line) {
			scribe.printNextToken(Token.tLBRACE, preferences.insert_space_before_opening_brace_in_array_initializer);
			scribe.printNextToken(Token.tRBRACE, preferences.insert_space_between_empty_braces_in_array_initializer);
		} else {
			final int line= scribe.line;
	        final String brace_position= preferences.brace_position_for_array_initializer;
			formatLeftCurlyBrace(line, brace_position);
	        formatOpeningBrace(brace_position, preferences.insert_space_before_opening_brace_in_array_initializer);
	        if (preferences.insert_new_line_after_opening_brace_in_array_initializer) {
	        	scribe.printNewLine();
	        }
	        if (preferences.insert_space_after_opening_brace_in_array_initializer) {
	        	scribe.space();
	        }

			final ListAlignment align= new ListAlignment(preferences.alignment_for_expressions_in_array_initializer);
			align.fSpaceBeforeComma= preferences.insert_space_before_comma_in_array_initializer;
			align.fSpaceAfterComma= preferences.insert_space_after_comma_in_array_initializer;
			align.fContinuationIndentation= preferences.continuation_indentation_for_array_initializer;
			formatList(initializers, align, false, false);
			
			if (preferences.insert_new_line_before_closing_brace_in_array_initializer) {
				scribe.startNewLine();
			}
			if (preferences.insert_space_before_closing_brace_in_array_initializer) {
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
			scribe.printNextToken(Token.tLPAREN, scribe.printComment());
			operand.accept(this);
			if (peekNextToken() != Token.tRPAREN) {
				scribe.skipToToken(Token.tRPAREN);
			}
			scribe.printNextToken(Token.tRPAREN, scribe.printComment());
			break;
		case IASTUnaryExpression.op_prefixIncr:
			scribe.printNextToken(Token.tINCR, scribe.printComment());
			operand.accept(this);
			break;
		case IASTUnaryExpression.op_prefixDecr:
			scribe.printNextToken(Token.tDECR, scribe.printComment());
			operand.accept(this);
			break;
		case IASTUnaryExpression.op_postFixIncr:
			operand.accept(this);
			scribe.printNextToken(Token.tINCR, scribe.printComment());
			break;
		case IASTUnaryExpression.op_postFixDecr:
			operand.accept(this);
			scribe.printNextToken(Token.tDECR, scribe.printComment());
			break;
		case IASTUnaryExpression.op_minus:
			scribe.printNextToken(Token.tMINUS, scribe.printComment());
			operand.accept(this);
			break;
		case IASTUnaryExpression.op_plus:
			scribe.printNextToken(Token.tPLUS, scribe.printComment());
			operand.accept(this);
			break;
		case IASTUnaryExpression.op_not:
			scribe.printNextToken(Token.tNOT, scribe.printComment());
			operand.accept(this);
			break;
		case IASTUnaryExpression.op_amper:
			scribe.printNextToken(Token.tAMPER, scribe.printComment());
			operand.accept(this);
			break;
		case IASTUnaryExpression.op_star:
			scribe.printNextToken(Token.tSTAR, scribe.printComment());
			operand.accept(this);
			break;
		case IASTUnaryExpression.op_tilde:
			scribe.printNextToken(Token.tCOMPL, scribe.printComment());
			operand.accept(this);
			break;
		case IASTUnaryExpression.op_sizeof:
			scribe.printNextToken(Token.t_sizeof, scribe.printComment());
			if (peekNextToken() != Token.tLPAREN) {
				scribe.space();
			}
			operand.accept(this);
			break;
		default:
			formatNode(node);
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTBinaryExpression node) {
		final IASTExpression op1= node.getOperand1();
		// operand 1
		op1.accept(this);
    	Alignment expressionAlignment =scribe.createAlignment(
    			"binaryExpression", //$NON-NLS-1$
    			// need configurable alignment
    			Alignment.M_COMPACT_SPLIT,
    			1,
    			scribe.scanner.getCurrentPosition());

    	scribe.enterAlignment(expressionAlignment);
    	boolean ok = false;
    	do {
    		try {
    			scribe.alignFragment(expressionAlignment, 0);

    			// operator
    			scribe.printNextToken(peekNextToken(), scribe.printComment());
    			if (scribe.printComment()) {
    				scribe.space();
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

	private int visit(IASTLiteralExpression node) {
		if (node.getNodeLocations().length > 1) {
			// cannot handle embedded macros
			skipNode(node);
		} else if (node.getKind() == IASTLiteralExpression.lk_string_literal) {
			// handle concatentation of string literals
			int token;
			boolean needSpace= false;
			final int line= scribe.line;
			boolean indented= false;
			try {
				while (true) {
					scribe.printNextToken(Token.tSTRING, needSpace);
					token= peekNextToken();
					if (token != Token.tSTRING) {
						break;
					}
					scribe.printCommentPreservingNewLines();
					if (!indented && line != scribe.line) {
						indented= true;
						scribe.indent();
					}
					needSpace= true;
				}
			} finally {
				if (indented) {
					scribe.unIndent();
				}
			}
		} else {
			scribe.printNextToken(peekNextToken(), scribe.printComment());
		}
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
		if (action != null) {
			if (action instanceof IASTCompoundStatement) {
                formatLeftCurlyBrace(line, preferences.brace_position_for_block);
				visit((IASTCompoundStatement)action);
			} else if (action instanceof IASTNullStatement) {
				scribe.indent();
				visit((IASTNullStatement)this);
				scribe.unIndent();
			} else {
				scribe.startNewLine();
				scribe.indent();
				action.accept(this);
				scribe.unIndent();
				scribe.startNewLine();
			}
		}

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
		scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon);
		scribe.printTrailingComment();
		return PROCESS_SKIP;
	}

	private int visit(IASTNullStatement node) {
		if (peekNextToken() == Token.tIDENTIFIER) {
			// probably a macro with empty expansion
			skipToNode(node);
		}
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
		node.getExpression().accept(this);
		if (!fInsideFor) {
			scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon);
			scribe.printTrailingComment();
		}
		return PROCESS_SKIP;
	}

	private int visit(IASTForStatement node) {
		scribe.printNextToken(Token.t_for);
	    final int line = scribe.line;
	    scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_for);
		fInsideFor= true;
		try {
			if (preferences.insert_space_after_opening_paren_in_for) {
				scribe.space();
			}
			IASTStatement initializerStmt= node.getInitializerStatement();
			initializerStmt.accept(this);
			if (peekNextToken() == Token.tSEMI) {
				scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon_in_for);
			}
			final IASTExpression condition = node.getConditionExpression();
			if (condition != null) {
				if (preferences.insert_space_after_semicolon_in_for) {
					scribe.space();
				}
				condition.accept(this);
			}
			if (peekNextToken() == Token.tSEMI) {
				scribe.printNextToken(Token.tSEMI, preferences.insert_space_before_semicolon_in_for);
			}
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
		scribe.printNextToken(Token.tRPAREN, preferences.insert_space_before_closing_paren_in_for);

		formatAction(line, node.getBody(), preferences.brace_position_for_block, preferences.insert_space_before_opening_brace_in_block);
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
		if (condExpr instanceof IASTProblemExpression) {
			scribe.skipToToken(Token.tRPAREN);
		} else { 
			condExpr.accept(this);
		}
		scribe.printNextToken(Token.tRPAREN, preferences.insert_space_before_closing_paren_in_if);

		final IASTStatement thenStatement = node.getThenClause();
		final IASTStatement elseStatement = node.getElseClause();

		boolean thenStatementIsBlock = false;
		if (thenStatement != null) {
			if (thenStatement instanceof IASTCompoundStatement) {
				final IASTCompoundStatement block = (IASTCompoundStatement) thenStatement;
				thenStatementIsBlock = true;
				final List statements = Arrays.asList(block.getStatements());
				if (isGuardClause(block, statements) && elseStatement == null && preferences.keep_guardian_clause_on_one_line) {
					/*
					 * Need a specific formatting for guard clauses
					 * guard clauses are block with a single return or throw
					 * statement
					 */
					scribe.printNextToken(Token.tLBRACE, preferences.insert_space_before_opening_brace_in_block);
					scribe.space();
					((IASTStatement) statements.get(0)).accept(this);
					scribe.printNextToken(Token.tRBRACE, true);
					scribe.printTrailingComment();
				} else {
                    formatLeftCurlyBrace(line, preferences.brace_position_for_block);
					thenStatement.accept(this);
					if (elseStatement != null && (preferences.insert_new_line_before_else_in_if_statement)) {
						scribe.startNewLine();
					}
				}
			} else if (elseStatement == null && preferences.keep_simple_if_on_one_line) {
				Alignment compactIfAlignment = scribe.createAlignment(
						"compactIf", //$NON-NLS-1$
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

		if (elseStatement != null) {
			if (thenStatementIsBlock) {
				scribe.printNextToken(Token.t_else, preferences.insert_space_after_closing_brace_in_block);
			} else {
				scribe.printNextToken(Token.t_else, true);
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
		IASTName[] names= node.getNames();
		for (int i = 0; i < names.length-1; i++) {
			names[i].accept(this);
			scribe.printNextToken(Token.tCOLONCOLON);
		}
		if (peekNextToken() == Token.tCOMPL) {
			// destructor
			scribe.printNextToken(Token.tCOMPL, false);
		}
		scribe.printNextToken(Token.tIDENTIFIER, false);
		return PROCESS_SKIP;
	}

	private int visit(ICPPASTTemplateId node) {
		IASTName name= node.getTemplateName();
		name.accept(this);
		scribe.printNextToken(Token.tLT, false);
		final IASTNode[] templateArguments= node.getTemplateArguments();
		if (templateArguments.length > 0) {
			final ListAlignment align= new ListAlignment(Alignment.M_COMPACT_SPLIT);
			formatList(Arrays.asList(templateArguments), align, false, false);
		}
		scribe.printNextToken(Token.tGT, false);
		return PROCESS_SKIP;
	}

	private int visit(IASTReturnStatement node) {
		scribe.printNextToken(Token.t_return);
		final IASTExpression expression = node.getReturnValue();
		if (expression != null) {
			scribe.space();
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
		scribe.printNextToken(Token.tCOLON, false);
		scribe.space();
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
		final List statements;
		if (bodyStmt instanceof IASTCompoundStatement) {
			statements= Arrays.asList(((IASTCompoundStatement)bodyStmt).getStatements());
		} else {
			statements= Collections.singletonList(bodyStmt);
		}
		final int statementsLength = statements.size();
		boolean wasACase = false;
		boolean wasAStatement = false;
		if (statementsLength != 0) {
			for (int i = 0; i < statementsLength; i++) {
				final IASTStatement statement = (IASTStatement) statements.get(i);
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
						if(!preferences.indent_switchstatements_compare_to_cases) {
							scribe.unIndent();
						}
					} else if (preferences.indent_switchstatements_compare_to_cases) {
						scribe.indent();
					}
					wasACase = false;
					wasAStatement = true;
				} else if (statement instanceof IASTCompoundStatement) {
					String bracePosition;
					if (wasACase) {
						if (preferences.indent_switchstatements_compare_to_cases) {
							scribe.unIndent();
						}
						bracePosition =	preferences.brace_position_for_block_in_case;
						try {
							formatBlock((IASTCompoundStatement) statement, bracePosition,
									preferences.insert_space_after_colon_in_case,
									preferences.indent_statements_compare_to_block);
						} catch (ASTProblemException e) {
							if (i < statementsLength - 1) {
								final IASTStatement nextStatement = (IASTStatement) statements.get(i + 1);
								skipToNode(nextStatement);
							}
						}
						if (preferences.indent_switchstatements_compare_to_cases) {
							scribe.indent();
						}
					} else {
						bracePosition =	preferences.brace_position_for_block;
						try {
							formatBlock((IASTCompoundStatement) statement, bracePosition,
									preferences.insert_space_before_opening_brace_in_block,
									preferences.indent_statements_compare_to_block);
						} catch (ASTProblemException e) {
							if (i < statementsLength - 1) {
								final IASTStatement nextStatement = (IASTStatement) statements.get(i + 1);
								skipToNode(nextStatement);
							}
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
							final IASTStatement nextStatement = (IASTStatement) statements.get(i + 1);
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
		}

		if ((wasACase || wasAStatement) && preferences.indent_switchstatements_compare_to_cases) {
			scribe.unIndent();
		}
		if (preferences.indent_switchstatements_compare_to_switch) {
			scribe.unIndent();
		}
		if (scribe.numberOfIndentations < braceIndent) {
			scribe.indent();
		}
		scribe.startNewLine();
		
		formatClosingBrace(switch_brace);
		return PROCESS_SKIP;
	}

	private int visit(IASTWhileStatement node) {
		scribe.printNextToken(Token.t_while);
		final int line = scribe.line;
		scribe.printNextToken(Token.tLPAREN, preferences.insert_space_before_opening_paren_in_while);

		if (preferences.insert_space_after_opening_paren_in_while) {
			scribe.space();
		}
		node.getCondition().accept(this);

		scribe.printNextToken(Token.tRPAREN, preferences.insert_space_before_closing_paren_in_while);

		formatAction(line, node.getBody(), preferences.brace_position_for_block, preferences.insert_space_before_opening_brace_in_block);
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
	
	private void formatNode(IASTNode node) {
		final IASTNodeLocation[] locations= node.getNodeLocations();
		final IASTNodeLocation minLocation= getMinFileLocation(locations);
		if (minLocation != null) {
			final IASTNodeLocation maxLocation= getMaxFileLocation(locations);
			if (maxLocation != null) {
				final int startOffset= minLocation.getNodeOffset();
				final int endOffset= maxLocation.getNodeOffset() + maxLocation.getNodeLength();
				scribe.printRaw(startOffset, endOffset - startOffset);
			}
		}
	}

	private static IASTFileLocation getMaxFileLocation(IASTNodeLocation[] locations) {
		if (locations == null || locations.length == 0) {
			return null;
		}
		final IASTNodeLocation nodeLocation= locations[locations.length-1];
		if (nodeLocation instanceof IASTFileLocation) {
			return (IASTFileLocation)nodeLocation;
		} else if (nodeLocation instanceof IASTMacroExpansion) {
			IASTNodeLocation[] macroLocations= ((IASTMacroExpansion)nodeLocation).getExpansionLocations();
			return getMaxFileLocation(macroLocations);
		}
		return null;
	}

	private static IASTFileLocation getMinFileLocation(IASTNodeLocation[] locations) {
		if (locations == null || locations.length == 0) {
			return null;
		}
		final IASTNodeLocation nodeLocation= locations[0];
		if (nodeLocation instanceof IASTFileLocation) {
			return (IASTFileLocation)nodeLocation;
		} else if (nodeLocation instanceof IASTMacroExpansion) {
			IASTNodeLocation[] macroLocations= ((IASTMacroExpansion)nodeLocation).getExpansionLocations();
			return getMinFileLocation(macroLocations);
		}
		return null;
	}

	private void exitAlignments() {
		while (scribe.currentAlignment != null) {
			scribe.exitAlignment(scribe.currentAlignment, true);
		}
	}

	private void skipNode(IASTNode node) {
		final IASTNodeLocation[] locations= node.getNodeLocations();
		final IASTNodeLocation maxLocation= getMaxFileLocation(locations);
		if (maxLocation != null) {
			final int endOffset= maxLocation.getNodeOffset() + maxLocation.getNodeLength();
			final int currentOffset= scribe.scanner.getCurrentTokenEndPosition() + 1;
			final int restLength= endOffset - currentOffset;
			if (restLength > 0) {
				scribe.printRaw(currentOffset, restLength);
			}
		}
	}

	private void skipToNode(IASTNode node) {
		final IASTNodeLocation[] locations= node.getNodeLocations();
		final IASTNodeLocation minLocation= getMinFileLocation(locations);
		if (minLocation != null) {
			final int startOffset= minLocation.getNodeOffset();
			final int currentOffset= scribe.scanner.getCurrentTokenEndPosition() + 1;
			final int restLength= startOffset - currentOffset;
			if (restLength > 0) {
				scribe.printRaw(currentOffset, restLength);
			}
		}
	}

	private void formatAction(final int line, final IASTStatement stmt, String brace_position, boolean insertLineForSingleStatement) {
		if (stmt != null) {
			if (stmt instanceof IASTCompoundStatement) {
                formatLeftCurlyBrace(line, brace_position);
				formatBlock((IASTCompoundStatement)stmt, brace_position, preferences.insert_space_before_opening_brace_in_block, preferences.indent_statements_compare_to_block);
			} else if (stmt instanceof IASTNullStatement) {
				scribe.indent();
				if (preferences.put_empty_statement_on_new_line) {
					scribe.startNewLine();
				}
				visit((IASTNullStatement)stmt);
				scribe.unIndent();
			} else {
				scribe.startNewLine();
				scribe.indent();
				stmt.accept(this);
				scribe.unIndent();
				if (insertLineForSingleStatement) {
					scribe.startNewLine();
				}
			}
		}
	}

	private void formatBlock(IASTCompoundStatement block, String block_brace_position, boolean insertSpaceBeforeOpeningBrace, boolean indentStatements) {
		IASTNodeLocation[] locations= block.getNodeLocations();
		if (locations.length == 0) {
			return;
		} else if (locations[0] instanceof IASTMacroExpansion) {
			formatNode(block);
			return;
		}
		formatOpeningBrace(block_brace_position, insertSpaceBeforeOpeningBrace);
		IASTStatement[] statements = block.getStatements();
		final int statementsLength = statements.length;
		if (statementsLength != 0) {
			scribe.startNewLine();
			if (indentStatements) {
				scribe.indent();
			}
			formatStatements(Arrays.asList(statements), true);
			scribe.printComment();

			if (indentStatements) {
				scribe.unIndent();
			}
		} else {
			if (preferences.insert_new_line_in_empty_block) {
				scribe.startNewLine();
			}
			if (indentStatements) {
				scribe.indent();
			}
			scribe.printComment();

			if (indentStatements) {
				scribe.unIndent();
			}
		}
		formatClosingBrace(block_brace_position);
	}

	private void formatLeftCurlyBrace(final int line, final String bracePosition) {
        // deal with (quite unexpected) comments right before lcurly
        scribe.printComment();
        if (DefaultCodeFormatterConstants.NEXT_LINE_ON_WRAP.equals(bracePosition)
                && (scribe.line > line || scribe.column >= preferences.page_width))
        {
            scribe.startNewLine();
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

	private void formatStatements(final List statements, boolean insertNewLineAfterLastStatement) {
		final int statementsLength = statements.size();
		if (statementsLength > 1) {
			IASTStatement previousStatement = (IASTStatement) statements.get(0);
			try {
				previousStatement.accept(this);
			} catch (ASTProblemException e) {
				skipToNode((IASTStatement) statements.get(1));
			}
			final boolean previousStatementIsNullStmt = previousStatement instanceof IASTNullStatement;
			for (int i = 1; i < statementsLength - 1; i++) {
				final IASTStatement statement = (IASTStatement) statements.get(i);
				final boolean statementIsNullStmt = statement instanceof IASTNullStatement;
				if ((previousStatementIsNullStmt && !statementIsNullStmt)
					|| (!previousStatementIsNullStmt && !statementIsNullStmt)) {
					scribe.startNewLine();
				}
				try {
					statement.accept(this);
				} catch (ASTProblemException e) {
					skipToNode((IASTStatement) statements.get(i + 1));
				}
				previousStatement = statement;
			}
			final IASTStatement statement = ((IASTStatement) statements.get(statementsLength - 1));
			final boolean statementIsNullStmt = statement instanceof IASTNullStatement;
			if ((previousStatementIsNullStmt && !statementIsNullStmt)
				|| (!previousStatementIsNullStmt && !statementIsNullStmt)) {
				scribe.startNewLine();
			}
			statement.accept(this);
		} else {
			final IASTStatement statement = (IASTStatement) statements.get(0);
			statement.accept(this);
		}
		if (insertNewLineAfterLastStatement) {
			scribe.startNewLine();
		}
	}

	private boolean commentStartsBlock(int start, int end) {
		localScanner.resetTo(start, end);
		if (localScanner.getNextToken() ==  Token.tLBRACE) {
			switch(localScanner.getNextToken()) {
				case Token.tBLOCKCOMMENT :
				case Token.tLINECOMMENT :
					return true;
			}
		}
		return false;
	}

	protected boolean hasComments() {
		localScanner.resetTo(scribe.scanner.startPosition, scribe.scannerEndPosition - 1);
		int token = localScanner.getNextToken();
		loop: while(true) {
			switch(token) {
			case Token.tBLOCKCOMMENT :
			case Token.tLINECOMMENT :
				return true;
			default:
				break loop;
			}
		}
		return false;
	}

	protected int peekNextToken() {
		localScanner.resetTo(scribe.scanner.getCurrentPosition(), scribe.scannerEndPosition - 1);
		int token = localScanner.getNextToken();
		loop: while(true) {
			switch(token) {
			case Token.tBLOCKCOMMENT :
			case Token.tLINECOMMENT :
				token = localScanner.getNextToken();
				continue loop;
			default:
				break loop;
			}
		}
		return token;
	}

	protected boolean isClosingTemplateToken() {
		localScanner.resetTo(scribe.scanner.getCurrentPosition(), scribe.scannerEndPosition - 1);
		int token = localScanner.getNextToken();
		loop: while(true) {
			switch(token) {
			case Token.tBLOCKCOMMENT :
			case Token.tLINECOMMENT :
				token = localScanner.getNextToken();
				continue loop;
			default:
				break loop;
			}
		}
		switch(token) {
			case Token.tGT :
			case Token.tSHIFTR :
				return true;
		}
		return false;
	}

	private boolean isGuardClause(IASTCompoundStatement block, List statements) {
		IASTNodeLocation[] locations= block.getNodeLocations();
		if (locations.length == 0) {
			return false;
		} else if (locations[0] instanceof IASTMacroExpansion) {
			return false;
		}
		IASTNodeLocation fileLocation= block.getFileLocation();
		if (fileLocation == null) {
			return false;
		}
		int blockStartPosition= block.getFileLocation().getNodeOffset();
		int blockLength= block.getFileLocation().getNodeLength();
		if (commentStartsBlock(blockStartPosition, blockLength)) return false;
		final int statementsLength = statements.size();
		if (statementsLength != 1) return false;
		if(statements.get(0) instanceof IASTReturnStatement) {
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
	private static List collectInactiveCodePositions(IASTTranslationUnit translationUnit) {
		if (translationUnit == null) {
			return Collections.EMPTY_LIST;
		}
		String fileName = translationUnit.getFilePath();
		if (fileName == null) {
			return Collections.EMPTY_LIST;
		}
		List positions = new ArrayList();
		int inactiveCodeStart = -1;
		boolean inInactiveCode = false;
		Stack inactiveCodeStack = new Stack();

		IASTPreprocessorStatement[] preprocStmts = translationUnit.getAllPreprocessorStatements();

		for (int i = 0; i < preprocStmts.length; i++) {
			IASTPreprocessorStatement statement = preprocStmts[i];
			if (!fileName.equals(statement.getContainingFilename())) {
				// preprocessor directive is from a different file
				continue;
			}
			IASTNodeLocation nodeLocation = statement.getFileLocation();
			if (nodeLocation == null) {
				continue;
			}
			if (statement instanceof IASTPreprocessorIfStatement) {
				IASTPreprocessorIfStatement ifStmt = (IASTPreprocessorIfStatement)statement;
				inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
				if (!ifStmt.taken()) {
					if (!inInactiveCode) {
						inactiveCodeStart = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
						inInactiveCode = true;
					}
				}
			} else if (statement instanceof IASTPreprocessorIfdefStatement) {
				IASTPreprocessorIfdefStatement ifdefStmt = (IASTPreprocessorIfdefStatement)statement;
				inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
				if (!ifdefStmt.taken()) {
					if (!inInactiveCode) {
						inactiveCodeStart = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
						inInactiveCode = true;
					}
				}
			} else if (statement instanceof IASTPreprocessorIfndefStatement) {
				IASTPreprocessorIfndefStatement ifndefStmt = (IASTPreprocessorIfndefStatement)statement;
				inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
				if (!ifndefStmt.taken()) {
					if (!inInactiveCode) {
						inactiveCodeStart = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
						inInactiveCode = true;
					}
				}
			} else if (statement instanceof IASTPreprocessorElseStatement) {
				IASTPreprocessorElseStatement elseStmt = (IASTPreprocessorElseStatement)statement;
				if (!elseStmt.taken() && !inInactiveCode) {
					inactiveCodeStart = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
					inInactiveCode = true;
				} else if (elseStmt.taken() && inInactiveCode) {
					int inactiveCodeEnd = nodeLocation.getNodeOffset();
					positions.add(new Position(inactiveCodeStart, inactiveCodeEnd - inactiveCodeStart));
					inInactiveCode = false;
				}
			} else if (statement instanceof IASTPreprocessorElifStatement) {
				IASTPreprocessorElifStatement elifStmt = (IASTPreprocessorElifStatement)statement;
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
					boolean wasInInactiveCode = ((Boolean)inactiveCodeStack.pop()).booleanValue();
					if (inInactiveCode && !wasInInactiveCode) {
						int inactiveCodeEnd = nodeLocation.getNodeOffset();
						positions.add(new Position(inactiveCodeStart, inactiveCodeEnd - inactiveCodeStart));
					}
					inInactiveCode = wasInInactiveCode;
				}
		 		catch( EmptyStackException e) {}
			}
		}
		if (inInactiveCode) {
			// handle dangling #if?
		}
		return positions;
	}

}

/*******************************************************************************
 * Copyright (c) 2026 John Dallaway and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Dallaway - initial implementation (#1455)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public class CSourceStickyLinesProcessor {

	private static final boolean DEBUG = Boolean
			.parseBoolean(Platform.getDebugOption(CUIPlugin.PLUGIN_ID + "/debug/editor/stickyLines")); //$NON-NLS-1$

	private static final List<Class<? extends IASTNode>> STICKY_NODE_CLASSES = List.of(IASTCompositeTypeSpecifier.class,
			IASTDoStatement.class, IASTEnumerationSpecifier.class, IASTForStatement.class, IASTFunctionDefinition.class,
			IASTIfStatement.class, IASTSwitchStatement.class, IASTWhileStatement.class);

	private IDocument fDocument;
	private IASTTranslationUnit fAst;

	public CSourceStickyLinesProcessor(IDocument document, IASTTranslationUnit ast) {
		fDocument = document;
		fAst = ast;
	}

	public List<Integer> calculateStickyLines(int lineNumber) throws BadLocationException {
		final long startTime = System.currentTimeMillis();
		if (DEBUG) {
			System.out.println("Sticky lines request at source line: " + (lineNumber + 1)); //$NON-NLS-1$
		}

		// lineNumber in the zero-based line number as known to the IDocument
		// fileLineNumber is the 1-based line number as known to the IASTTranslationUnit
		final int fileLineNumber = lineNumber + 1;
		if (lineNumber < 1) { // if no possibility of sticky lines
			if (DEBUG) {
				System.out.println("> No processing"); //$NON-NLS-1$
			}
			return Collections.emptyList();
		}

		// locate initial AST node
		final IASTNodeSelector nodeSelector = fAst.getNodeSelector(fAst.getFilePath());
		IASTNode node = null;
		final IRegion region = fDocument.getLineInformation(lineNumber);
		node = nodeSelector.findEnclosingNode(region.getOffset(), region.getLength());
		if (node instanceof IASTPreprocessorStatement) {
			// find enclosing non-preprocessor node using adjusted offset
			final int nodeOffset = node.getFileLocation().getNodeOffset();
			node = nodeSelector.findEnclosingNode(nodeOffset - 1, 2);
		}

		// process sticky ancestor nodes
		final LinkedList<Integer> ancestorLines = new LinkedList<>();
		while (null != node) {
			if (DEBUG) {
				System.out.printf("> Examining AST node: %s (lines %d-%d)\n", node.getClass().getSimpleName(), //$NON-NLS-1$
						node.getFileLocation().getStartingLineNumber(), node.getFileLocation().getEndingLineNumber());
			}
			if (nodeInstanceOfStickyClass(node)) {
				processAncestorStickyNode(node, fileLineNumber, ancestorLines);
			}
			node = node.getParent();
		}

		// process sticky pre-processor nodes and merge with ancestor nodes
		final List<Integer> preprocessorLines = findPreprocessorStickyLines(fileLineNumber);
		final List<Integer> allStickyLines = mergeStickyLines(ancestorLines, preprocessorLines);
		if (DEBUG) {
			System.out.println("> Sticky line count: " + allStickyLines.size()); //$NON-NLS-1$
			System.out.println("> Execution time (ms): " + (System.currentTimeMillis() - startTime)); //$NON-NLS-1$
		}
		return allStickyLines;
	}

	private List<Integer> findPreprocessorStickyLines(int fileLineNumber) {
		final Deque<IASTPreprocessorStatement> stack = new ArrayDeque<>();
		for (IASTPreprocessorStatement statement : fAst.getAllPreprocessorStatements()) {
			if (statement.getFileLocation().getStartingLineNumber() >= fileLineNumber) {
				break;
			}
			if (nodeInstanceOfPreprocessorIfClass(statement) || statement instanceof IASTPreprocessorElifStatement
					|| statement instanceof IASTPreprocessorElseStatement) {
				if (DEBUG) {
					System.out.printf("> Pushing AST preprocessor node: %s (lines %d-%d)\n", //$NON-NLS-1$
							statement.getClass().getSimpleName(), statement.getFileLocation().getStartingLineNumber(),
							statement.getFileLocation().getEndingLineNumber());
				}
				stack.push(statement);
			} else if (statement instanceof IASTPreprocessorEndifStatement) {
				IASTPreprocessorStatement previous = statement;
				while (!stack.isEmpty() && !nodeInstanceOfPreprocessorIfClass(previous)) {
					previous = stack.pop();
					if (DEBUG) {
						System.out.printf("> Popping AST preprocessor node: %s (lines %d-%d)\n", //$NON-NLS-1$
								previous.getClass().getSimpleName(), previous.getFileLocation().getStartingLineNumber(),
								previous.getFileLocation().getEndingLineNumber());
					}
				}
			}
		}
		final LinkedList<Integer> stickyLines = new LinkedList<>();
		stack.forEach(statement -> addStickyLine(statement.getFileLocation().getStartingLineNumber(), stickyLines));
		return stickyLines;
	}

	private List<Integer> mergeStickyLines(List<Integer> lines1, List<Integer> lines2) {
		final Deque<Integer> deque1 = new ArrayDeque<>(lines1);
		final Deque<Integer> deque2 = new ArrayDeque<>(lines2);
		final List<Integer> mergedLines = new LinkedList<>();
		while (null != deque1.peek() && null != deque2.peek()) {
			// assume both lists are sorted in ascending line number order
			if (deque1.peek() < deque2.peek()) {
				mergedLines.add(deque1.pop());
			} else {
				mergedLines.add(deque2.pop());
			}
		}
		mergedLines.addAll(deque1);
		mergedLines.addAll(deque2);
		return mergedLines;
	}

	private void processAncestorStickyNode(IASTNode node, int fileLineNumber, LinkedList<Integer> stickyLines)
			throws BadLocationException {
		final int startingLineNumber = node.getFileLocation().getStartingLineNumber();
		if (startingLineNumber < fileLineNumber) {
			if (node instanceof IASTIfStatement ifStatement) {
				// process possible else clause first
				processElseClause(ifStatement, fileLineNumber, stickyLines);
			} else if (node instanceof IASTSwitchStatement switchStatement) {
				// if node has compound switch body
				if (switchStatement.getBody() instanceof IASTCompoundStatement switchBody) {
					// process case statement in switch body first
					processSwitchBody(switchBody, fileLineNumber, stickyLines);
				}
			}
			addStickyLine(startingLineNumber, stickyLines);
		}
	}

	private void processSwitchBody(IASTCompoundStatement switchBody, int fileLineNumber,
			LinkedList<Integer> stickyLines) {
		final LinkedList<IASTStatement> stickyStatements = new LinkedList<>();
		for (IASTStatement statement : switchBody.getStatements()) {
			final int startingLineNumber = statement.getFileLocation().getStartingLineNumber();
			if (DEBUG) {
				System.out.printf("> Examining AST node: %s (lines %d-%d)\n", //$NON-NLS-1$
						statement.getClass().getSimpleName(), startingLineNumber,
						statement.getFileLocation().getEndingLineNumber());
			}
			// if we have reached the requested line within the switch body
			if (startingLineNumber >= fileLineNumber) {
				// process accumulated case/default statements as sticky
				for (IASTStatement stickyStatement : stickyStatements) {
					final int stickyStartingLineNumber = stickyStatement.getFileLocation().getStartingLineNumber();
					addStickyLine(stickyStartingLineNumber, stickyLines);
				}
				break;
			} else if (statement instanceof IASTCaseStatement || statement instanceof IASTDefaultStatement) {
				// accumulate case/default statements
				stickyStatements.addFirst(statement);
			} else if (statement instanceof IASTBreakStatement || statement instanceof IASTReturnStatement) {
				// clear case/default statements
				stickyStatements.clear();
			}
		}
	}

	private void processElseClause(IASTIfStatement ifStatement, int fileLineNumber, LinkedList<Integer> stickyLines)
			throws BadLocationException {
		final IASTStatement elseClause = ifStatement.getElseClause();
		if (null == elseClause) {
			return; // no else clause
		}
		// locate source line containing 'else' keyword between then/else clauses
		// but search only as far as the line preceding the requested line
		final int startingLineNumber = ifStatement.getThenClause().getFileLocation().getEndingLineNumber();
		final int endingLineNumber = Math.min(elseClause.getFileLocation().getStartingLineNumber(), fileLineNumber - 1);
		for (int fileLineNum = endingLineNumber; fileLineNum >= startingLineNumber; fileLineNum--) {
			if (getDocumentLine(fileLineNum - 1).contains("else")) { //$NON-NLS-1$
				addStickyLine(fileLineNum, stickyLines);
			}
		}
	}

	private void addStickyLine(int fileLineNumber, LinkedList<Integer> stickyLines) {
		final int lineNumber = fileLineNumber - 1;
		// suppress duplicate sticky lines
		if (stickyLines.isEmpty() || (stickyLines.getFirst() > lineNumber)) {
			if (DEBUG) {
				System.out.println("> Sticky line: " + fileLineNumber); //$NON-NLS-1$
			}
			stickyLines.addFirst(lineNumber);
		}
	}

	private boolean nodeInstanceOfStickyClass(IASTNode node) {
		return STICKY_NODE_CLASSES.stream().anyMatch(nodeClass -> nodeClass.isInstance(node));
	}

	private boolean nodeInstanceOfPreprocessorIfClass(IASTNode node) {
		return node instanceof IASTPreprocessorIfStatement || node instanceof IASTPreprocessorIfdefStatement
				|| node instanceof IASTPreprocessorIfndefStatement;
	}

	private String getDocumentLine(int lineNumber) throws BadLocationException {
		final IRegion region = fDocument.getLineInformation(lineNumber);
		return fDocument.get(region.getOffset(), region.getLength());
	}

}

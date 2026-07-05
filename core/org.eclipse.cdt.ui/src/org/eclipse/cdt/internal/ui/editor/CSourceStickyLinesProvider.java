/*******************************************************************************
 * Copyright (c) 2025, 2026 Red Hat and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - reference Java implementation
 *     John Dallaway - initial C/C++ implementation (#1455)
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
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.stickyscroll.IStickyLine;
import org.eclipse.ui.texteditor.stickyscroll.IStickyLinesProvider;
import org.eclipse.ui.texteditor.stickyscroll.StickyLine;

public class CSourceStickyLinesProvider implements IStickyLinesProvider {

	private static final boolean DEBUG = Boolean
			.parseBoolean(Platform.getDebugOption(CUIPlugin.PLUGIN_ID + "/debug/editor/stickyLines")); //$NON-NLS-1$

	private static final List<Class<? extends IASTNode>> STICKY_NODE_CLASSES = List.of(IASTCompositeTypeSpecifier.class,
			IASTDoStatement.class, IASTEnumerationSpecifier.class, IASTForStatement.class, IASTFunctionDefinition.class,
			IASTIfStatement.class, IASTSwitchStatement.class, IASTWhileStatement.class);

	@Override
	public List<IStickyLine> getStickyLines(ISourceViewer sourceViewer, int lineNumber,
			StickyLinesProperties properties) {
		final long startTime = System.currentTimeMillis();
		final List<IStickyLine> stickyLines = new LinkedList<>();
		if (DEBUG) {
			System.out.println("Sticky lines request at source line: " + (lineNumber + 1)); //$NON-NLS-1$
		}

		final IEditorPart editor = properties.editor();
		final ICElement inputElement = getInputElement(editor);
		if (inputElement instanceof ITranslationUnit tu) {
			CUIPlugin.getDefault().getASTProvider().runOnAST(tu, ASTProvider.WAIT_IF_OPEN, null, (lang, ast) -> {
				stickyLines.addAll(calculateStickyLines(sourceViewer, lineNumber, ast));
				return Status.OK_STATUS;
			});
		}
		if (DEBUG) {
			System.out.println("> Sticky line count: " + stickyLines.size()); //$NON-NLS-1$
			System.out.println("> Execution time (ms): " + (System.currentTimeMillis() - startTime)); //$NON-NLS-1$
		}
		return stickyLines;
	}

	private List<IStickyLine> calculateStickyLines(ISourceViewer sourceViewer, int lineNumber,
			IASTTranslationUnit ast) {
		// lineNumber in the zero-based line number as known to the IDocument
		// fileLineNumber is the 1-based line number as known to the AST and the underlying source file
		// textWidgetLineNumber is the the line number as known to the ISourceViewer
		final int fileLineNumber = lineNumber + 1;
		final StyledText textWidget = sourceViewer.getTextWidget();
		final int textWidgetLineNumber = mapLineNumberToWidget(sourceViewer, lineNumber);
		if (textWidgetLineNumber < 1) { // if no possibility of sticky lines
			if (DEBUG) {
				System.out.println("> No processing"); //$NON-NLS-1$
			}
			return Collections.emptyList();
		}

		// locate initial AST node
		final IASTNodeSelector nodeSelector = ast.getNodeSelector(ast.getFilePath());
		final String line = textWidget.getLine(textWidgetLineNumber);
		IASTNode node = null;
		try {
			final int offset = sourceViewer.getDocument().getLineOffset(lineNumber);
			node = nodeSelector.findEnclosingNode(offset, line.length());
			if (node instanceof IASTPreprocessorStatement) {
				// find enclosing non-preprocessor node using adjusted offset
				final int nodeOffset = node.getFileLocation().getNodeOffset();
				node = nodeSelector.findEnclosingNode(nodeOffset - 1, 2);
			}
		} catch (BadLocationException e) {
			ILog.get().error("Error getting line offset for sticky lines", e); //$NON-NLS-1$
		}

		// process sticky ancestor nodes
		final LinkedList<IStickyLine> ancestorLines = new LinkedList<>();
		while (null != node) {
			if (DEBUG) {
				System.out.printf("> Examining AST node: %s (lines %d-%d)\n", node.getClass().getSimpleName(), //$NON-NLS-1$
						node.getFileLocation().getStartingLineNumber(), node.getFileLocation().getEndingLineNumber());
			}
			if (nodeInstanceOfStickyClass(node)) {
				processStickyNode(node, fileLineNumber, sourceViewer, ancestorLines);
			}
			node = node.getParent();
		}

		// process sticky pre-processor nodes and merge with ancestor nodes
		final List<IStickyLine> preprocessorLines = findPreprocessorStickyLines(sourceViewer, fileLineNumber, ast);
		return mergeStickyLines(ancestorLines, preprocessorLines);
	}

	private List<IStickyLine> findPreprocessorStickyLines(ISourceViewer sourceViewer, int fileLineNumber,
			IASTTranslationUnit ast) {
		final Deque<IASTPreprocessorStatement> stack = new ArrayDeque<>();
		for (IASTPreprocessorStatement statement : ast.getAllPreprocessorStatements()) {
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
		final LinkedList<IStickyLine> stickyLines = new LinkedList<>();
		stack.forEach(statement -> addStickyLine(statement.getFileLocation().getStartingLineNumber(), sourceViewer,
				stickyLines));
		return stickyLines;
	}

	private List<IStickyLine> mergeStickyLines(List<IStickyLine> lines1, List<IStickyLine> lines2) {
		final Deque<IStickyLine> deque1 = new ArrayDeque<>(lines1);
		final Deque<IStickyLine> deque2 = new ArrayDeque<>(lines2);
		final List<IStickyLine> mergedLines = new LinkedList<>();
		while (null != deque1.peek() && null != deque2.peek()) {
			// assume both lists are sorted in ascending line number order
			if (deque1.peek().getLineNumber() < deque2.peek().getLineNumber()) {
				mergedLines.add(deque1.pop());
			} else {
				mergedLines.add(deque2.pop());
			}
		}
		mergedLines.addAll(deque1);
		mergedLines.addAll(deque2);
		return mergedLines;
	}

	private void processStickyNode(IASTNode node, int fileLineNumber, ISourceViewer sourceViewer,
			LinkedList<IStickyLine> stickyLines) {
		final int startingLineNumber = node.getFileLocation().getStartingLineNumber();
		if (startingLineNumber < fileLineNumber) {
			if (node instanceof IASTIfStatement ifStatement) {
				// process possible else clause first
				processElseClause(ifStatement, fileLineNumber, sourceViewer, stickyLines);
			} else if (node instanceof IASTSwitchStatement switchStatement) {
				// if node has compound switch body
				if (switchStatement.getBody() instanceof IASTCompoundStatement switchBody) {
					// process case statement in switch body first
					processSwitchBody(switchBody, fileLineNumber, sourceViewer, stickyLines);
				}
			}
			addStickyLine(startingLineNumber, sourceViewer, stickyLines);
		}
	}

	private void processSwitchBody(IASTCompoundStatement switchBody, int fileLineNumber, ISourceViewer sourceViewer,
			LinkedList<IStickyLine> stickyLines) {
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
					addStickyLine(stickyStartingLineNumber, sourceViewer, stickyLines);
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

	private void processElseClause(IASTIfStatement ifStatement, int fileLineNumber, ISourceViewer sourceViewer,
			LinkedList<IStickyLine> stickyLines) {
		final IASTStatement elseClause = ifStatement.getElseClause();
		if (null == elseClause) {
			return; // no else clause
		}
		// locate source line containing 'else' keyword between then/else clauses
		// but search only as far as the line preceding the requested line
		final int startingLineNumber = ifStatement.getThenClause().getFileLocation().getEndingLineNumber();
		final int endingLineNumber = Math.min(elseClause.getFileLocation().getStartingLineNumber(), fileLineNumber - 1);
		final StyledText textWidget = sourceViewer.getTextWidget();
		for (int fileLineNum = endingLineNumber; fileLineNum >= startingLineNumber; fileLineNum--) {
			final int textWidgetLineNumber = mapLineNumberToWidget(sourceViewer, fileLineNum - 1);
			if (textWidget.getLine(textWidgetLineNumber).contains("else")) { //$NON-NLS-1$
				addStickyLine(fileLineNum, sourceViewer, stickyLines);
			}
		}
	}

	private void addStickyLine(int fileLineNumber, ISourceViewer sourceViewer, LinkedList<IStickyLine> stickyLines) {
		final int lineNumber = fileLineNumber - 1;
		// suppress duplicate sticky lines
		if (stickyLines.isEmpty() || (stickyLines.getFirst().getLineNumber() > lineNumber)) {
			if (DEBUG) {
				System.out.println("> Sticky line: " + fileLineNumber); //$NON-NLS-1$
			}
			stickyLines.addFirst(new StickyLine(lineNumber, sourceViewer));
		}
	}

	private boolean nodeInstanceOfStickyClass(IASTNode node) {
		return STICKY_NODE_CLASSES.stream().anyMatch(nodeClass -> nodeClass.isInstance(node));
	}

	private boolean nodeInstanceOfPreprocessorIfClass(IASTNode node) {
		return node instanceof IASTPreprocessorIfStatement || node instanceof IASTPreprocessorIfdefStatement
				|| node instanceof IASTPreprocessorIfndefStatement;
	}

	private ICElement getInputElement(IEditorPart part) {
		final IEditorInput editorInput = part.getEditorInput();
		if (null != editorInput) {
			return CDTUITools.getEditorInputCElement(editorInput);
		}
		return null;
	}

	private int mapLineNumberToWidget(ISourceViewer sourceViewer, int lineNumber) {
		if (sourceViewer instanceof ITextViewerExtension5 extension) {
			return extension.modelLine2WidgetLine(lineNumber); // -1 if line not found
		}
		return lineNumber;
	}

}

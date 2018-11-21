/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Nokia - Refactored from DebugTextHover to remove CDI dependency
 *     Wind River Systems - Bug 200418
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.editors;

import java.util.EmptyStackException;
import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.internal.ui.CDebugUIMessages;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.SharedASTJob;
import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ui.IEditorPart;

/**
 * The text hovering support for C/C++ debugger.
 *
 * @since 7.0
 */
public abstract class AbstractDebugTextHover implements ICEditorTextHover, ITextHoverExtension {

	/**
	 * ASTVisitor checking for side-effect expressions.
	 */
	private static class ExpressionChecker extends ASTVisitor {
		private boolean fValid;

		private ExpressionChecker() {
			shouldVisitExpressions = true;
		}

		private boolean check(IASTNode node) {
			fValid = true;
			node.accept(this);
			return fValid;
		}

		@Override
		public int visit(IASTExpression expression) {
			if (expression instanceof IASTFunctionCallExpression) {
				fValid = false;
			} else if (expression instanceof IASTUnaryExpression) {
				IASTUnaryExpression unaryExpression = (IASTUnaryExpression) expression;
				switch (unaryExpression.getOperator()) {
				case IASTUnaryExpression.op_postFixDecr:
				case IASTUnaryExpression.op_postFixIncr:
				case IASTUnaryExpression.op_prefixIncr:
				case IASTUnaryExpression.op_prefixDecr:
					fValid = false;
					break;
				}
			} else if (expression instanceof IASTBinaryExpression) {
				IASTBinaryExpression binaryExpression = (IASTBinaryExpression) expression;
				switch (binaryExpression.getOperator()) {
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
					fValid = false;
					break;
				}
			} else if (expression instanceof ICPPASTNewExpression) {
				fValid = false;
			} else if (expression instanceof ICPPASTDeleteExpression) {
				fValid = false;
			} else if (expression instanceof IGNUASTCompoundStatementExpression) {
				fValid = false;
			}
			if (!fValid)
				return PROCESS_ABORT;
			return PROCESS_CONTINUE;
		}
	}

	static final private int MAX_HOVER_INFO_SIZE = 100;

	private IEditorPart fEditor;

	/**
	 * @return <code>true</code> if this hover can evaluate an expression
	 */
	protected abstract boolean canEvaluate();

	/**
	 * Compute a value for given expression.
	 *
	 * @param expression
	 * @return a result string or <code>null</code> if the expression could not be evaluated
	 */
	protected abstract String evaluateExpression(String expression);

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (canEvaluate()) {
			String expression = getExpressionText(textViewer, hoverRegion);
			if (expression != null) {
				String result = evaluateExpression(expression);
				if (result == null)
					return null;
				try {
					StringBuilder buffer = new StringBuilder();
					appendVariable(buffer, makeHTMLSafe(expression), makeHTMLSafe(result.trim()));
					if (buffer.length() > 0) {
						return buffer.toString();
					}
				} catch (DebugException x) {
					CDebugUIPlugin.log(x);
				}
			}
		}
		return null;
	}

	@Override
	public IRegion getHoverRegion(ITextViewer viewer, int offset) {
		if (viewer != null)
			return CDebugUIUtils.findWord(viewer.getDocument(), offset);
		return null;
	}

	@Override
	public final void setEditor(IEditorPart editor) {
		if (editor != null) {
			fEditor = editor;
		}
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return null;
	}

	/**
	 * Compute the expression text to be evaluated by the debugger.
	 * <p>
	 * The default implementation uses an AST to compute a valid, side-effect free
	 * expression.
	 * </p>
	 *
	 * @param textViewer  the underlying text viewer
	 * @param hoverRegion  the hover region as returned by {@link #getHoverRegion(ITextViewer, int)}
	 * @return an expression string or <code>null</code> if no valid expression could be computed
	 */
	protected String getExpressionText(ITextViewer textViewer, final IRegion hoverRegion) {
		IDocument document = textViewer.getDocument();
		if (document == null)
			return null;
		String expression = getExpressionTextFromAST(document, hoverRegion);
		if (expression == null) {
			// fallback in case no parsing information is available
			try {
				return document.get(hoverRegion.getOffset(), hoverRegion.getLength());
			} catch (BadLocationException e) {
				// ignored
			}
		} else if (expression.length() == 0) {
			// positively invalid expression
			return null;
		}
		return expression;
	}

	/**
	 * Compute a valid expression from AST if available.
	 *
	 * @param document
	 * @param hoverRegion
	 * @return a valid expression string, an empty string to indicate an invalid
	 *         expression or <code>null</code> if the expression could not be
	 *         validated
	 */
	private String getExpressionTextFromAST(IDocument document, final IRegion hoverRegion) {
		ICElement cElement = CDTUITools.getEditorInputCElement(getEditor().getEditorInput());
		if (!(cElement instanceof ITranslationUnit)) {
			return null;
		}
		final Position expressionPosition = new Position(0);
		SharedASTJob job = new SharedASTJob(CDebugUIMessages.getString("AbstractDebugTextHover.jobName"), //$NON-NLS-1$
				(ITranslationUnit) cElement) {
			@Override
			public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
				if (ast == null) {
					return Status.CANCEL_STATUS;
				}
				int offset = hoverRegion.getOffset();
				int length = hoverRegion.getLength();
				IASTName name = ast.getNodeSelector(null).findEnclosingName(offset, length);
				if (name != null) {
					IASTImageLocation imageLoc = name.getImageLocation();
					if (imageLoc == null) {
						return Status.CANCEL_STATUS;
					}
					int kind = imageLoc.getLocationKind();
					switch (kind) {
					case IASTImageLocation.ARGUMENT_TO_MACRO_EXPANSION:
						computeMacroArgumentExtent(name, expressionPosition);
						break;
					default:
						if (name.getParent() instanceof IASTPreprocessorMacroExpansion) {
							// special case: macro expansion as expression
							IASTNode node = ast.getNodeSelector(null)
									.findEnclosingNodeInExpansion(imageLoc.getNodeOffset(), imageLoc.getNodeLength());
							if (node instanceof IASTExpression) {
								IASTFileLocation exprLoc = node.getFileLocation();
								if (exprLoc.getNodeOffset() == imageLoc.getNodeOffset()) {
									computeExpressionExtent(node, expressionPosition);
								}
							}
						} else {
							computeExpressionExtent(name, expressionPosition);
						}
					}
				} else {
					// not a name, but might still be an expression (e.g. this or a selected expression)
					IASTNode node = ast.getNodeSelector(null).findEnclosingNode(offset, length);
					if (!(node instanceof IASTExpression)) {
						node = ast.getNodeSelector(null).findFirstContainedNode(offset, length);
					}
					if (node instanceof IASTExpression) {
						computeExpressionExtent(node, expressionPosition);
					} else if (node == null && insideInactiveCode(ast, offset) && !insideComment(ast, offset)) {
						return Status.CANCEL_STATUS;
					}
				}
				return Status.OK_STATUS;
			}

			private void computeMacroArgumentExtent(IASTName name, Position pos) {
				IASTImageLocation imageLoc = name.getImageLocation();
				int startOffset = imageLoc.getNodeOffset();
				int endOffset = startOffset + imageLoc.getNodeLength();
				// do some black magic to consider field reference expressions
				IASTNode expr = name.getParent();
				int macroOffset = name.getFileLocation().getNodeOffset();
				if (expr instanceof IASTFieldReference) {
					IASTExpression ownerExpr = ((IASTFieldReference) expr).getFieldOwner();
					while (ownerExpr instanceof IASTFieldReference
							|| ownerExpr instanceof IASTArraySubscriptExpression) {
						if (ownerExpr instanceof IASTArraySubscriptExpression) {
							ownerExpr = ((IASTArraySubscriptExpression) ownerExpr).getArrayExpression();
						} else {
							ownerExpr = ((IASTFieldReference) ownerExpr).getFieldOwner();
						}
					}
					if (ownerExpr instanceof IASTIdExpression) {
						IASTName ownerName = ((IASTIdExpression) ownerExpr).getName();
						IASTImageLocation ownerImageLoc = ownerName.getImageLocation();
						final int nameOffset = ownerImageLoc.getNodeOffset();
						// offset should be inside macro expansion
						if (nameOffset < startOffset && nameOffset > macroOffset) {
							startOffset = nameOffset;
						}
					}
				}
				ExpressionChecker checker = new ExpressionChecker();
				if (checker.check(expr)) {
					pos.offset = startOffset;
					pos.length = endOffset - startOffset;
				}
			}

			private void computeExpressionExtent(IASTNode node0, Position pos) {
				IASTNode node = node0;
				while (node != null && !(node instanceof IASTExpression) && !(node instanceof IASTDeclaration)) {
					node = node.getParent();
				}
				IASTNodeLocation loc = null;
				if (node instanceof IASTExpression && !(node instanceof IASTIdExpression)) {
					ExpressionChecker checker = new ExpressionChecker();
					if (checker.check(node)) {
						loc = node.getFileLocation();
					}
				} else if (node0 instanceof IASTName) {
					// fallback: use simple name
					loc = ((IASTName) node0).getImageLocation();
					if (loc == null) {
						IASTNodeLocation[] locations = node0.getNodeLocations();
						// avoid macro expansions
						if (locations.length == 1 && !(locations[0] instanceof IASTMacroExpansionLocation)) {
							loc = locations[0];
						}
					}
				}
				if (loc != null) {
					pos.offset = loc.getNodeOffset();
					pos.length = loc.getNodeLength();
				}
			}

			private boolean insideInactiveCode(IASTTranslationUnit ast, int offset) {
				int inactiveCodeStart = -1;
				boolean inInactiveCode = false;
				Stack<Boolean> inactiveCodeStack = new Stack<>();

				IASTPreprocessorStatement[] preprocStmts = ast.getAllPreprocessorStatements();

				for (IASTPreprocessorStatement preprocStmt : preprocStmts) {
					IASTPreprocessorStatement statement = preprocStmt;
					if (!statement.isPartOfTranslationUnitFile()) {
						continue;
					}
					IASTNodeLocation nodeLocation = statement.getFileLocation();
					if (nodeLocation == null) {
						continue;
					}
					int nodeOffset = nodeLocation.getNodeOffset();
					int nodeEnd = nodeOffset + nodeLocation.getNodeLength();
					if (nodeOffset <= offset && offset < nodeEnd) {
						// inside preprocessor directive
						return false;
					}
					if (statement instanceof IASTPreprocessorIfStatement) {
						IASTPreprocessorIfStatement ifStmt = (IASTPreprocessorIfStatement) statement;
						inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
						if (!ifStmt.taken()) {
							if (!inInactiveCode) {
								inactiveCodeStart = nodeEnd;
								inInactiveCode = true;
							}
						}
					} else if (statement instanceof IASTPreprocessorIfdefStatement) {
						IASTPreprocessorIfdefStatement ifdefStmt = (IASTPreprocessorIfdefStatement) statement;
						inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
						if (!ifdefStmt.taken()) {
							if (!inInactiveCode) {
								inactiveCodeStart = nodeEnd;
								inInactiveCode = true;
							}
						}
					} else if (statement instanceof IASTPreprocessorIfndefStatement) {
						IASTPreprocessorIfndefStatement ifndefStmt = (IASTPreprocessorIfndefStatement) statement;
						inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
						if (!ifndefStmt.taken()) {
							if (!inInactiveCode) {
								inactiveCodeStart = nodeEnd;
								inInactiveCode = true;
							}
						}
					} else if (statement instanceof IASTPreprocessorElseStatement) {
						IASTPreprocessorElseStatement elseStmt = (IASTPreprocessorElseStatement) statement;
						if (!elseStmt.taken() && !inInactiveCode) {
							inactiveCodeStart = nodeEnd;
							inInactiveCode = true;
						} else if (elseStmt.taken() && inInactiveCode) {
							int inactiveCodeEnd = nodeOffset;
							if (inactiveCodeStart <= offset && offset < inactiveCodeEnd) {
								return true;
							}
							inInactiveCode = false;
						}
					} else if (statement instanceof IASTPreprocessorElifStatement) {
						IASTPreprocessorElifStatement elifStmt = (IASTPreprocessorElifStatement) statement;
						if (!elifStmt.taken() && !inInactiveCode) {
							inactiveCodeStart = nodeEnd;
							inInactiveCode = true;
						} else if (elifStmt.taken() && inInactiveCode) {
							int inactiveCodeEnd = nodeOffset;
							if (inactiveCodeStart <= offset && offset < inactiveCodeEnd) {
								return true;
							}
							inInactiveCode = false;
						}
					} else if (statement instanceof IASTPreprocessorEndifStatement) {
						try {
							boolean wasInInactiveCode = inactiveCodeStack.pop().booleanValue();
							if (inInactiveCode && !wasInInactiveCode) {
								int inactiveCodeEnd = nodeOffset;
								if (inactiveCodeStart <= offset && offset < inactiveCodeEnd) {
									return true;
								}
							}
							inInactiveCode = wasInInactiveCode;
						} catch (EmptyStackException e) {
						}
					}
				}
				return false;
			}

			private boolean insideComment(IASTTranslationUnit ast, int offset) {
				IASTComment[] comments = ast.getComments();
				for (IASTComment comment : comments) {
					if (!comment.isPartOfTranslationUnitFile()) {
						continue;
					}
					IASTNodeLocation location = comment.getFileLocation();
					if (location != null) {
						if (location.getNodeOffset() <= offset
								&& offset < location.getNodeOffset() + location.getNodeLength()) {
							return true;
						}
					}
				}
				return false;
			}
		};
		job.setPriority(Job.SHORT);
		job.setSystem(true);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException exc) {
			job.cancel();
			return ""; //$NON-NLS-1$
		}
		if (!job.getResult().isOK()) {
			// indeterminate result
			return null;
		}
		if (expressionPosition.getLength() > 0) {
			try {
				// Get expression text removing comments, obsolete whitespace, etc.
				StringBuilder result = new StringBuilder();
				ITypedRegion[] partitions = TextUtilities.computePartitioning(document, ICPartitions.C_PARTITIONING,
						expressionPosition.offset, expressionPosition.length, false);
				for (ITypedRegion partition : partitions) {
					if (IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType())
							|| ICPartitions.C_CHARACTER.equals(partition.getType())
							|| ICPartitions.C_STRING.equals(partition.getType())) {
						result.append(document.get(partition.getOffset(), partition.getLength()));
					} else {
						result.append(' ');
					}
				}
				String text = result.toString().replaceAll("(\\r\\n|\\n|\t| )+", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$
				return text;
			} catch (BadLocationException exc) {
			}
		}
		// return empty string to indicate invalid expression
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the debug context the expression should be evaluated against.
	 * The default implementation returns {@link DebugUITools#getDebugContext()}.
	 */
	protected IAdaptable getSelectionAdaptable() {
		return DebugUITools.getDebugContext();
	}

	/**
	 * Returns the editor this hover is associated with.
	 */
	protected final IEditorPart getEditor() {
		return fEditor;
	}

	/**
	 * Append HTML for the given variable to the given buffer
	 */
	private static void appendVariable(StringBuilder buffer, String expression, String value) throws DebugException {
		if (value.length() > MAX_HOVER_INFO_SIZE)
			value = value.substring(0, MAX_HOVER_INFO_SIZE) + " ..."; //$NON-NLS-1$
		buffer.append("<p>"); //$NON-NLS-1$
		buffer.append("<pre>").append(expression).append("</pre>"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append(" = "); //$NON-NLS-1$
		buffer.append("<b><pre>").append(value).append("</pre></b>"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("</p>"); //$NON-NLS-1$
	}

	/**
	 * Replace any characters in the given String that would confuse an HTML parser with their escape sequences.
	 */
	private static String makeHTMLSafe(String string) {
		StringBuilder buffer = new StringBuilder(string.length());
		for (int i = 0; i != string.length(); i++) {
			char ch = string.charAt(i);
			switch (ch) {
			case '&':
				buffer.append("&amp;"); //$NON-NLS-1$
				break;
			case '<':
				buffer.append("&lt;"); //$NON-NLS-1$
				break;
			case '>':
				buffer.append("&gt;"); //$NON-NLS-1$
				break;
			case '"':
				buffer.append("&quot;"); //$NON-NLS-1$
				break;
			default:
				buffer.append(ch);
				break;
			}
		}
		return buffer.toString();
	}
}

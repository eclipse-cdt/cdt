/*******************************************************************************
 * Copyright (c) 2008, 2020 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Marco Stornelli <marco.stornelli@gmail.com> - Bug 333134
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 333134
 *******************************************************************************/
package org.eclipse.cdt.ui.text.doctools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * This class provides default behaviors for multi-line comment auto-editing.
 *
 * This class is intended to be sub-classed.
 *
 * @since 5.0
 */
public class DefaultMultilineCommentAutoEditStrategy implements IAutoEditStrategy {
	protected static final String MULTILINE_START = "/*"; //$NON-NLS-1$#
	protected static final String MULTILINE_MID = " * "; //$NON-NLS-1$
	protected static final String MULTILINE_END = "*/"; //$NON-NLS-1$
	private static String fgDefaultLineDelim = "\n"; //$NON-NLS-1$
	private ICProject fProject;

	/**
	 * @since 6.7
	 */
	public interface IDocCustomizer {
		StringBuilder customizeForDeclaration(IDocument doc, IASTNode dec, ITypedRegion region,
				CustomizeOptions options);
	}

	public DefaultMultilineCommentAutoEditStrategy() {
		this(null);
	}

	/**
	 * @since 6.6
	 */
	public DefaultMultilineCommentAutoEditStrategy(ICProject project) {
		fProject = project;
	}

	/**
	 * Return the project (if any) associated with this strategy
	 * @return A project or empty optional if no project is associated
	 * @since 6.7
	 */
	protected Optional<IProject> getProject() {
		if (fProject != null) {
			return Optional.of(fProject.getProject());
		}
		return Optional.empty();
	}

	/**
	 * Check if edit strategy is enabled
	 * @return True if enabled, false otherwise
	 * @since 6.6
	 */
	protected boolean isEnabled() {
		boolean formatBlocks = false;
		if (fProject == null) {
			formatBlocks = DefaultCodeFormatterConstants.TRUE
					.equals(CCorePlugin.getOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_BLOCK));
		} else {
			formatBlocks = DefaultCodeFormatterConstants.TRUE
					.equals(fProject.getOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_BLOCK, true));
		}
		return formatBlocks;
	}

	/**
	 * @see org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
	 */
	@Override
	public void customizeDocumentCommand(IDocument doc, DocumentCommand cmd) {
		if (!isEnabled())
			return;
		fgDefaultLineDelim = TextUtilities.getDefaultLineDelimiter(doc);
		if (doc instanceof IDocumentExtension4) {
			boolean forNewLine = cmd.length == 0 && cmd.text != null && endsWithDelimiter(doc, cmd.text);
			boolean forCommentEnd = "/".equals(cmd.text); //$NON-NLS-1$

			if (forNewLine || forCommentEnd) {
				IDocumentExtension4 ext4 = (IDocumentExtension4) doc;
				DocumentRewriteSession drs = ext4.startRewriteSession(DocumentRewriteSessionType.UNRESTRICTED_SMALL);
				try {
					if (forNewLine) {
						customizeDocumentAfterNewLine(doc, cmd);
					} else if (forCommentEnd) {
						customizeDocumentForMultilineCommentEnd(doc, cmd);
					}
				} finally {
					ext4.stopRewriteSession(drs);
				}
			}
		}
	}

	/**
	 * This implements a rule that when in a multi-line comment context typing a forward slash with
	 * one white space after the "*" will move eliminate the whitespace.
	 * @param doc
	 * @param command
	 */
	protected void customizeDocumentForMultilineCommentEnd(IDocument doc, DocumentCommand command) {
		if (command.offset < 2 || doc.getLength() == 0) {
			return;
		}
		try {
			if ("* ".equals(doc.get(command.offset - 2, 2))) { //$NON-NLS-1$
				// modify document command
				command.length++;
				command.offset--;
			}
		} catch (BadLocationException excp) {
			// stop work
		}
	}

	/**
	 * Copies the indentation of the previous line and adds a star.
	 * If the comment just started on this line adds also a blank.
	 *
	 * @param doc the document to work on
	 * @param c the command to deal with
	 */
	public void customizeDocumentAfterNewLine(IDocument doc, final DocumentCommand c) {
		if (!isEnabled())
			return;
		int offset = c.offset;
		if (offset == -1 || doc.getLength() == 0)
			return;

		String lineDelim = TextUtilities.getDefaultLineDelimiter(doc);
		final StringBuilder buf = new StringBuilder(c.text);
		try {
			// find start of line
			IRegion line = doc.getLineInformationOfOffset(c.offset);
			int lineStart = line.getOffset();
			int firstNonWS = findEndOfWhiteSpaceAt(doc, lineStart, c.offset);

			IRegion prefix = findPrefixRange(doc, line);
			String indentation = doc.get(prefix.getOffset(), prefix.getLength());
			int lengthToAdd = Math.min(offset - prefix.getOffset(), prefix.getLength());
			buf.append(indentation.substring(0, lengthToAdd));

			boolean commentAtStart = firstNonWS < c.offset && doc.getChar(firstNonWS) == '/';
			if (commentAtStart) {
				// comment started on this line
				buf.append(MULTILINE_MID);
			}

			c.shiftsCaret = false;
			c.caretOffset = c.offset + buf.length();

			if (commentAtStart && shouldCloseMultiline(doc, c.offset)) {
				try {
					doc.replace(c.offset, 0, indentation + " " + MULTILINE_END); // close the comment in order to parse //$NON-NLS-1$
					buf.append(lineDelim);

					// as we are auto-closing, the comment becomes eligible for auto-doc'ing
					IASTNode dec = null;
					IIndex index = null;
					ITranslationUnit unit = getTranslationUnitForActiveEditor();
					if (unit != null) {
						index = CCorePlugin.getIndexManager().getIndex(unit.getCProject());
						try {
							index.acquireReadLock();
						} catch (InterruptedException e) {
							index = null;
						}
					}
					try {
						IASTTranslationUnit ast = getAST(unit, index);

						if (this instanceof IDocCustomizer) {
							if (ast != null) {
								dec = findNextDocumentNode(ast, offset);
								if (dec == null) {
									IASTNodeSelector ans = ast.getNodeSelector(ast.getFilePath());
									IASTNode node = ans.findEnclosingNode(offset, 0);
									if (node instanceof IASTDeclaration) {
										dec = node;
									}
								}
							}

							if (dec != null) {
								ITypedRegion partition = TextUtilities.getPartition(doc,
										ICPartitions.C_PARTITIONING /* this! */, offset, false);
								StringBuilder content = null;
								IDocCustomizer customizer = (IDocCustomizer) this;
								CustomizeOptions options = new CustomizeOptions();
								content = customizer.customizeForDeclaration(doc, dec, partition, options);
								if (!options.addNewLine) {
									buf.setLength(buf.length() - MULTILINE_MID.length() - lineDelim.length());
								}
								buf.append(indent(content, indentation + MULTILINE_MID, lineDelim));
							}
						} else {
							if (ast != null) {
								dec = findFollowingDeclaration(ast, offset);
								if (dec == null) {
									IASTNodeSelector ans = ast.getNodeSelector(ast.getFilePath());
									IASTNode node = ans.findEnclosingNode(offset, 0);
									if (node instanceof IASTDeclaration) {
										dec = node;
									}
								}
							}

							if (dec != null) {
								ITypedRegion partition = TextUtilities.getPartition(doc,
										ICPartitions.C_PARTITIONING /* this! */, offset, false);
								StringBuilder content = null;
								content = customizeAfterNewLineForDeclaration(doc, (IASTDeclaration) dec, partition);
								buf.append(indent(content, indentation + MULTILINE_MID, lineDelim));
							}
						}
					} finally {
						if (index != null) {
							index.releaseReadLock();
						}
					}
				} catch (BadLocationException ble) {
					CUIPlugin.log(ble);
				} catch (CoreException e) {
					CUIPlugin.log(e);
				}
			}

			c.text = buf.toString();

		} catch (BadLocationException excp) {
			// stop work
		}
	}

	/**
	 * Options set by IDocCustomize
	 * @since 6.7
	 */
	public static class CustomizeOptions {
		/**
		 * Child of IDocCustomize can decide to add a new line
		 * before their comment body or not. By default a new
		 * line is added.
		 */
		public boolean addNewLine;

		public CustomizeOptions() {
			addNewLine = true;
		}
	}

	/**
	 * @deprecated This class is deprecated, clients should implement IDocCustomizer
	 * interface instead.
	 */
	@Deprecated
	protected StringBuilder customizeAfterNewLineForDeclaration(IDocument doc, IASTDeclaration dec,
			ITypedRegion region) {
		return new StringBuilder();
	}

	/*
	 * Utilities
	 */

	private static class SearchVisitor extends ASTVisitor {

		public SearchVisitor(int offset) {
			shouldVisitTranslationUnit = true;
			shouldVisitDeclarations = true;
			shouldVisitNamespaces = true;
			this.offset = offset;
		}

		private int nearestOffset = Integer.MAX_VALUE;
		private int offset;
		private IASTDeclaration target;

		public int getNearest() {
			return nearestOffset;
		}

		public IASTDeclaration getTarget() {
			return target;
		}

		@Override
		public int visit(ICPPASTNamespaceDefinition namespace) {
			IASTNodeLocation loc = namespace.getFileLocation();
			if (loc != null) {
				int candidateOffset = loc.getNodeOffset();
				if (offset <= candidateOffset && candidateOffset <= nearestOffset) {
					nearestOffset = candidateOffset;
					target = namespace;
					return PROCESS_ABORT;
				}
			}
			return PROCESS_CONTINUE;
		}

		/**
		 * Holds the
		 */
		IASTDeclaration stopWhenLeaving;

		@Override
		public int visit(IASTDeclaration declaration) {
			IASTNodeLocation loc = declaration.getFileLocation();
			if (loc != null) {
				int candidateOffset = loc.getNodeOffset();
				int candidateEndOffset = candidateOffset + loc.getNodeLength();

				if (offset <= candidateOffset && candidateOffset <= nearestOffset) {
					nearestOffset = candidateOffset;
					target = declaration;
					return PROCESS_ABORT;
				}

				boolean candidateEnclosesOffset = (offset >= candidateOffset) && (offset < candidateEndOffset);
				if (candidateEnclosesOffset) {
					stopWhenLeaving = declaration;
				}
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTDeclaration declaration) {
			if (declaration == stopWhenLeaving)
				return PROCESS_ABORT;
			return PROCESS_CONTINUE;
		}
	}

	/**
	 * Locates the {@link IASTNode} most immediately following the specified offset
	 * @param unit the translation unit, or null (in which case the result will also be null)
	 * @param offset the offset to begin the search from
	 * @return the {@link IASTNode} most immediately following the specified offset, or null if there
	 * is no node suitable for documentation, i.e. a declaration or a macro definition
	 * @since 6.7
	 */
	public static IASTNode findNextDocumentNode(IASTTranslationUnit unit, final int offset) {
		IASTNode bestNode = null;
		if (unit != null) {
			IASTPreprocessorMacroDefinition[] macros = unit.getMacroDefinitions();
			SearchVisitor av = new SearchVisitor(offset);
			unit.accept(av);
			int nearest = av.getNearest();
			bestNode = av.getTarget();
			for (IASTPreprocessorMacroDefinition m : macros) {
				if (m.getExpansionLocation() != null && m.getExpansionLocation().getNodeOffset() < nearest
						&& offset <= m.getExpansionLocation().getNodeOffset()) {
					bestNode = m;
					nearest = m.getExpansionLocation().getNodeOffset();
				}
			}
		}
		return bestNode;
	}

	/**
	 * Locates the {@link IASTDeclaration} most immediately following the specified offset
	 * @param unit the translation unit, or null (in which case the result will also be null)
	 * @param offset the offset to begin the search from
	 * @return the {@link IASTDeclaration} most immediately following the specified offset, or null if there
	 * is no {@link IASTDeclaration}
	 */
	public static IASTDeclaration findFollowingDeclaration(IASTTranslationUnit unit, final int offset) {
		IASTDeclaration[] dec = new IASTDeclaration[1];
		if (unit != null) {
			SearchVisitor av = new SearchVisitor(offset);
			unit.accept(av);
			dec[0] = av.getTarget();
		}
		return dec[0];
	}

	/**
	 * @return the AST unit for the active editor, not based on an index, or <code>null</code> if there
	 * is no active editor, or the AST could not be obtained.
	 */
	public IASTTranslationUnit getAST() {
		return getAST(getTranslationUnitForActiveEditor(), null);
	}

	/**
	 * @return the AST for the given translation unit, based on the given index, or <code>null</code> if
	 * the translation unit is <code>null</code>, or the AST could not be obtained.
	 * @since 5.10
	 */
	public IASTTranslationUnit getAST(ITranslationUnit unit, IIndex index) {
		try {
			if (unit != null) {
				IASTTranslationUnit ast = unit.getAST(index, ITranslationUnit.AST_SKIP_ALL_HEADERS);
				return ast;
			}
		} catch (CModelException e) {
			CUIPlugin.log(e);
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return null;
	}

	/**
	 * Assuming the offset is within a multi-line comment, returns a guess as to
	 * whether the enclosing multi-line comment is a new comment. The result is undefined if
	 * the offset does not occur within a multi-line comment.
	 *
	 * @param document the document
	 * @param offset the offset
	 * @return <code>true</code> if the comment should be closed, <code>false</code> if not
	 */
	/*
	 * Adapted from JDT
	 */
	public boolean shouldCloseMultiline(IDocument document, int offset) {
		try {
			IRegion line = document.getLineInformationOfOffset(offset);
			ITypedRegion partition = TextUtilities.getPartition(document, ICPartitions.C_PARTITIONING, offset, false);
			int partitionEnd = partition.getOffset() + partition.getLength();
			if (line.getOffset() >= partitionEnd)
				return false;

			String comment = document.get(partition.getOffset(), partition.getLength());
			if (comment.indexOf(MULTILINE_START, offset - partition.getOffset()) != -1)
				return true; // enclosed another comment -> probably a new comment

			if (document.getLength() == partitionEnd) {
				return !comment.endsWith(MULTILINE_END);
			}

			return false;

		} catch (BadLocationException e) {
			return false;
		}
	}

	/**
	 * @return the ITranslationUnit for the active editor, or null if no active
	 * editor could be found.
	 * @deprecated use getTranslationUnitForActiveEditor() instead
	 */
	@Deprecated
	protected static ITranslationUnit getTranslationUnit() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return null;

		IWorkbenchPage page = window.getActivePage();
		if (page == null)
			return null;

		IEditorPart editor = page.getActiveEditor();
		if (editor == null)
			return null;

		IWorkingCopyManager manager = CUIPlugin.getDefault().getWorkingCopyManager();
		ITranslationUnit unit = manager.getWorkingCopy(editor.getEditorInput());
		if (unit == null)
			return null;

		return unit;
	}

	/**
	 * Same as above, but nonstatic so derived classes can override it.
	 * @since 5.10
	 */
	protected ITranslationUnit getTranslationUnitForActiveEditor() {
		return getTranslationUnit();
	}

	/**
	 * Returns a new buffer with the specified indent string inserted at the beginning
	 * of each line in the specified input buffer
	 * @param buffer
	 * @param indent
	 * @param lineDelim
	 * @since 5.3
	 */
	protected static final StringBuilder indent(StringBuilder buffer, String indent, String lineDelim) {
		StringBuilder result = new StringBuilder();
		BufferedReader br = new BufferedReader(new StringReader(buffer.toString()));
		try {
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				result.append(indent).append(line).append(lineDelim);
			}
		} catch (IOException ioe) {
			throw new AssertionError(); // we can't get IO errors from a string backed reader
		}
		return result;
	}

	/**
	 * Returns a new buffer with the specified indent string inserted at the beginning
	 * of each line in the specified input buffer
	 * @param buffer
	 * @param indent
	 *
	 * @deprecated Use {{@link #indent(StringBuilder, String, String)} instead.
	 */
	@Deprecated
	protected static final StringBuilder indent(StringBuilder buffer, String indent) {
		return indent(buffer, indent, fgDefaultLineDelim);
	}

	/**
	 * Returns the offset of the first non-whitespace character in the specified document, searching
	 * right/downward from the specified start offset up to the specified end offset. If there is
	 * no non-whitespace then the end offset is returned.
	 * @param document
	 * @param offset
	 * @param end
	 * @throws BadLocationException
	 */
	protected static int findEndOfWhiteSpaceAt(IDocument document, int offset, int end) throws BadLocationException {
		while (offset < end) {
			char c = document.getChar(offset);
			if (c != ' ' && c != '\t') {
				return offset;
			}
			offset++;
		}
		return end;
	}

	/**
	 * Returns the range of the comment prefix on the given line in
	 * <code>document</code>. The prefix greedily matches the following regex
	 * pattern: <code>\s*\*\S*\s*</code>, that is, any number of whitespace
	 * characters, followed by an asterisk ('*'), followed by any number of
	 * non-whitespace characters, followed by any number of whitespace characters.
	 *
	 * @param document the document to which <code>line</code> refers
	 * @param line the line from which to extract the prefix range
	 * @return an <code>IRegion</code> describing the range of the prefix on
	 *         the given line
	 * @throws BadLocationException if accessing the document fails
	 */
	protected static IRegion findPrefixRange(IDocument document, IRegion line) throws BadLocationException {
		int lineOffset = line.getOffset();
		int lineEnd = lineOffset + line.getLength();
		int indentEnd = findEndOfWhiteSpaceAt(document, lineOffset, lineEnd);
		if (indentEnd < lineEnd && document.getChar(indentEnd) == '*') {
			indentEnd++;
			while (indentEnd < lineEnd && !isWhitespace(document.getChar(indentEnd)))
				indentEnd++;
			while (indentEnd < lineEnd && isWhitespace(document.getChar(indentEnd)))
				indentEnd++;
		}
		return new Region(lineOffset, indentEnd - lineOffset);
	}

	private static boolean isWhitespace(char ch) {
		return ch == ' ' || ch == '\t';
	}

	/**
	 * Returns whether the text ends with one of the specified IDocument object's
	 * legal line delimiters.
	 */
	protected static boolean endsWithDelimiter(IDocument d, String txt) {
		String[] delimiters = d.getLegalLineDelimiters();
		for (int i = 0; i < delimiters.length; i++) {
			if (txt.endsWith(delimiters[i]))
				return true;
		}
		return false;
	}
}

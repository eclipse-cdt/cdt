/*******************************************************************************
 * Copyright (c) 2013, 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.formatter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ToolFactory;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.rewrite.changegenerator.TextEditUtil;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Applies the C++ code formatter to the code affected by refactoring.
 */
public class ChangeFormatter {
	/**
	 * Applies the C++ code formatter to the code affected by refactoring.
	 *
	 * @param code The code being modified.
	 * @param tu The translation unit containing the code.
	 */
	public static MultiTextEdit formatChangedCode(String code, ITranslationUnit tu, MultiTextEdit rootEdit) {
		IDocument document = new Document(code);
		try {
			TextEdit edit = rootEdit.copy();
			// Apply refactoring changes to a temporary document.
			edit.apply(document, TextEdit.UPDATE_REGIONS);

			// Expand regions affected by the changes to cover complete lines. We calculate two
			// sets of regions, reflecting the state of the document before and after
			// the refactoring changes.
			TextEdit[] appliedEdits = edit.getChildren();
			TextEdit[] edits = rootEdit.copy().removeChildren();
			IRegion[] regions = new IRegion[appliedEdits.length];
			int numRegions = 0;
			int prevEnd = -1;
			for (int i = 0; i < appliedEdits.length; i++) {
				edit = appliedEdits[i];
				int offset = edit.getOffset();
				int end = offset + edit.getLength();
				int newOffset = document.getLineInformationOfOffset(offset).getOffset();
				edit = edits[i];
				int originalEnd = edit.getExclusiveEnd();
				// Expand to the end of the line unless the end of the edit region is at
				// the beginning of line both, before and after the change.
				IRegion lineInfo = document.getLineInformationOfOffset(end);
				int newEnd = lineInfo.getOffset();
				newEnd = (originalEnd == 0 || code.charAt(originalEnd - 1) == '\n') && end == newEnd ? end
						: endOffset(lineInfo);
				if (newOffset <= prevEnd && numRegions > 0) {
					numRegions--;
					newOffset = regions[numRegions].getOffset();
				}
				prevEnd = newEnd;
				if (newEnd != newOffset) { // Don't produce empty regions.
					regions[numRegions] = new Region(newOffset, newEnd - newOffset);
					numRegions++;
				}
			}

			if (numRegions == 0)
				return rootEdit;
			if (numRegions < regions.length)
				regions = Arrays.copyOf(regions, numRegions);

			// Calculate formatting changes for the regions after the refactoring changes.
			ICProject project = tu.getCProject();
			Map<String, Object> options = new HashMap<>(project.getOptions(true));
			options.put(DefaultCodeFormatterConstants.FORMATTER_TRANSLATION_UNIT, tu);
			// Allow all comments to be indented.
			options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN,
					DefaultCodeFormatterConstants.FALSE);
			CodeFormatter formatter = ToolFactory.createCodeFormatter(options);
			code = document.get();
			TextEdit[] formatEdits = formatter.format(CodeFormatter.K_TRANSLATION_UNIT, code, regions,
					TextUtilities.getDefaultLineDelimiter(document));

			TextEdit combinedFormatEdit = new MultiTextEdit();
			for (TextEdit formatEdit : formatEdits) {
				if (formatEdit != null)
					combinedFormatEdit = TextEditUtil.merge(combinedFormatEdit, formatEdit);
			}
			formatEdits = TextEditUtil.flatten(combinedFormatEdit).removeChildren();

			MultiTextEdit result = new MultiTextEdit();
			int delta = 0;
			TextEdit edit1 = null;
			TextEdit edit2 = null;
			int i = 0;
			int j = 0;
			while (true) {
				if (edit1 == null && i < edits.length)
					edit1 = edits[i++];
				if (edit2 == null && j < formatEdits.length)
					edit2 = formatEdits[j++];
				if (edit1 == null) {
					if (edit2 == null)
						break;
					edit2.moveTree(-delta);
					result.addChild(edit2);
					edit2 = null;
				} else if (edit2 == null) {
					delta += TextEditUtil.delta(edit1);
					result.addChild(edit1);
					edit1 = null;
				} else {
					if (edit2.getExclusiveEnd() - delta <= edit1.getOffset()) {
						edit2.moveTree(-delta);
						result.addChild(edit2);
						edit2 = null;
					} else {
						TextEdit piece = clippedEdit(edit2, new Region(-1, edit1.getOffset() + delta));
						if (piece != null) {
							piece.moveTree(-delta);
							result.addChild(piece);
						}
						int d = TextEditUtil.delta(edit1);
						Region region = new Region(edit1.getOffset() + delta, edit1.getLength() + d);
						int end = endOffset(region);
						MultiTextEdit format = new MultiTextEdit();
						while ((piece = clippedEdit(edit2, region)) != null) {
							format.addChild(piece);
							// The warning "The variable edit2 may be null at this location" is bogus.
							// Make the compiler happy:
							if (edit2 != null) {
								if (edit2.getExclusiveEnd() >= end || j >= formatEdits.length) {
									break;
								}
							}
							edit2 = formatEdits[j++];
						}
						if (format.hasChildren()) {
							format.moveTree(-delta);
							edit1 = applyEdit(format, edit1);
						}
						delta += d;
						result.addChild(edit1);
						edit1 = null;

						edit2 = clippedEdit(edit2, new Region(end, Integer.MAX_VALUE - end));
					}
				}
			}
			return result;
		} catch (MalformedTreeException e) {
			CCorePlugin.log(e);
		} catch (BadLocationException e) {
			CCorePlugin.log(e);
		}
		return rootEdit;
	}

	private static TextEdit clippedEdit(TextEdit edit, IRegion region) {
		if ((edit.getOffset() < region.getOffset() && edit.getExclusiveEnd() <= region.getOffset())
				|| edit.getOffset() >= endOffset(region)) {
			return null;
		}
		int offset = Math.max(edit.getOffset(), region.getOffset());
		int length = Math.min(endOffset(edit), endOffset(region)) - offset;
		if (offset == edit.getOffset() && length == edit.getLength()) {
			// InsertEdit always satisfies the above condition.
			return edit;
		}
		if (edit instanceof DeleteEdit) {
			return new DeleteEdit(offset, length);
		}
		if (edit instanceof ReplaceEdit) {
			String replacement = ((ReplaceEdit) edit).getText();
			int start = Math.max(offset - edit.getOffset(), 0);
			int end = Math.min(endOffset(region) - offset, replacement.length());
			if (end <= start) {
				return new DeleteEdit(offset, length);
			}
			return new ReplaceEdit(offset, length, replacement.substring(start, end));
		} else {
			throw new IllegalArgumentException("Unexpected edit type: " + edit.getClass().getSimpleName()); //$NON-NLS-1$
		}
	}

	/**
	 * Applies source edit to the target one and returns the combined edit.
	 */
	private static TextEdit applyEdit(TextEdit source, TextEdit target)
			throws MalformedTreeException, BadLocationException {
		source.moveTree(-target.getOffset());
		String text;
		if (target instanceof InsertEdit) {
			text = ((InsertEdit) target).getText();
		} else if (target instanceof ReplaceEdit) {
			text = ((ReplaceEdit) target).getText();
		} else {
			text = ""; //$NON-NLS-1$
		}

		IDocument document = new Document(text);
		source.apply(document, TextEdit.NONE);
		text = document.get();
		if (target.getLength() == 0) {
			return new InsertEdit(target.getOffset(), text);
		} else {
			return new ReplaceEdit(target.getOffset(), target.getLength(), text);
		}
	}

	private static int endOffset(TextEdit edit) {
		return edit.getOffset() + edit.getLength();
	}

	private static int endOffset(IRegion region) {
		return region.getOffset() + region.getLength();
	}
}

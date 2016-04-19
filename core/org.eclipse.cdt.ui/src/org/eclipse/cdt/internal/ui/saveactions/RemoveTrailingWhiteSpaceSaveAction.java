package org.eclipse.cdt.internal.ui.saveactions;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.ICPartitions;

public class RemoveTrailingWhiteSpaceSaveAction implements ISaveAction {
	
	private IRegion[] changedRegions;
	private final IDocument document;
	
	public RemoveTrailingWhiteSpaceSaveAction(IDocument document, IRegion[] changedRegions) {
		this.changedRegions = changedRegions;
		this.document = document;
	}
	

	@Override
	public TextEdit perform(TextEdit rootEdit) throws BadLocationException {
		if (!isLimitedRemoveTrailingWhitespace()) {
			// Pretend that the whole document changed.
			changedRegions = new IRegion[] { new Region(0, document.getLength()) };
		}
		TextEdit lastWhitespaceEdit = null;
		// Remove trailing whitespace from changed lines.
		for (IRegion region : changedRegions) {
			int firstLine = document.getLineOfOffset(region.getOffset());
			int lastLine = document.getLineOfOffset(region.getOffset() + region.getLength());
			for (int line = firstLine; line <= lastLine; line++) {
				IRegion lineRegion = document.getLineInformation(line);
				if (lineRegion.getLength() == 0) {
					continue;
				}
				int lineStart = lineRegion.getOffset();
				int lineEnd = lineStart + lineRegion.getLength();

				// Find the rightmost none-whitespace character
				int charPos = lineEnd - 1;
				while (charPos >= lineStart && Character.isWhitespace(document.getChar(charPos)))
					charPos--;

				charPos++;
				if (charPos < lineEnd) {
					// check partition - don't remove whitespace inside strings
					ITypedRegion partition = TextUtilities.getPartition(document, ICPartitions.C_PARTITIONING, charPos, false);
					if (!ICPartitions.C_STRING.equals(partition.getType())) {
						lastWhitespaceEdit = new DeleteEdit(charPos, lineEnd - charPos);
						if (rootEdit == null) {
							rootEdit = new MultiTextEdit();
						}
						rootEdit.addChild(lastWhitespaceEdit);
					}
				}
			}
		}
		return rootEdit;
	}
	
	private static boolean isLimitedRemoveTrailingWhitespace() {
		return PreferenceConstants.getPreferenceStore().getBoolean(
				PreferenceConstants.REMOVE_TRAILING_WHITESPACE_LIMIT_TO_EDITED_LINES);
	}

}

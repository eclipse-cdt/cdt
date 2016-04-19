package org.eclipse.cdt.internal.ui.saveactions;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.TextEdit;

public class AddNewLineSaveAction implements ISaveAction {
	
	private final IDocument document;
	
	public AddNewLineSaveAction(IDocument document) {
		this.document = document;
	}
	
	@Override
	public TextEdit perform(TextEdit rootEdit) throws BadLocationException {
		TextEdit lastEdit = null;
		if(rootEdit != null && rootEdit.getChildrenSize() != 0) {
			TextEdit[] children = rootEdit.getChildren();
			lastEdit = children[rootEdit.getChildrenSize()];
		}
		// Add newline at the end of the file.
		int endOffset = document.getLength();
		IRegion lastLineRegion = document.getLineInformationOfOffset(endOffset);
		// Insert newline at the end of the document if the last line is not
		// empty and
		// will not become empty after removal of trailing whitespace.
		if (lastLineRegion.getLength() != 0
				&& (lastEdit == null || lastEdit.getOffset() != lastLineRegion.getOffset()
						|| lastEdit.getLength() != lastLineRegion.getLength())) {
			TextEdit edit = new InsertEdit(endOffset, TextUtilities.getDefaultLineDelimiter(document));
			if (rootEdit == null) {
				lastEdit = edit;
			} else {
				rootEdit.addChild(edit);
			}
		}
		return rootEdit;
	}

}

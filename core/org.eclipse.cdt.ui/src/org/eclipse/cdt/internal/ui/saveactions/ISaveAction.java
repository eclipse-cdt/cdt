package org.eclipse.cdt.internal.ui.saveactions;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.TextEdit;

public interface ISaveAction {

	TextEdit perform(TextEdit rootEdit) throws BadLocationException;

}

package org.eclipse.cdt.internal.ui.saveactions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import org.eclipse.cdt.ui.PreferenceConstants;

public class CSaveActionsProvider {

	public static List<ISaveAction> getActiveSaveActions(IDocument document, IRegion[] changedRegions) {
		List<ISaveAction> activeActionsList = new ArrayList<ISaveAction>();
		if (shouldAddNewlineAtEof())
			activeActionsList.add(new AddNewLineSaveAction(document));
		if (shouldRemoveTrailingWhitespace())
			activeActionsList.add(new RemoveTrailingWhiteSpaceSaveAction(document, changedRegions));
		if (shouldAlignAllConst())
			activeActionsList.add(new AlignConstSaveAction());
		return activeActionsList;
	}

	public static boolean needsChangedRegions() {
		return shouldRemoveTrailingWhitespace() && isLimitedRemoveTrailingWhitespace();
	}

	private static boolean shouldAddNewlineAtEof() {
		return PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.ENSURE_NEWLINE_AT_EOF);
	}

	private static boolean shouldRemoveTrailingWhitespace() {
		return PreferenceConstants.getPreferenceStore()
				.getBoolean(PreferenceConstants.REMOVE_TRAILING_WHITESPACE);
	}

	private static boolean shouldAlignAllConst() {
		return PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.ALIGN_ALL_CONST);
	}

	private static boolean isLimitedRemoveTrailingWhitespace() {
		return PreferenceConstants.getPreferenceStore()
				.getBoolean(PreferenceConstants.REMOVE_TRAILING_WHITESPACE_LIMIT_TO_EDITED_LINES);
	}

}

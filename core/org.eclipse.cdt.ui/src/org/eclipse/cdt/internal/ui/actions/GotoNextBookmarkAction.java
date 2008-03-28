package org.eclipse.cdt.internal.ui.actions;

import java.util.Arrays;
import java.util.Comparator;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * Find and goto the next bookmark in the currently selected file.
 */
public class GotoNextBookmarkAction extends TextEditorAction {

    public static final String NEXT_BOOKMARK = "GotoNextBookmark"; //$NON-NLS-1$

    /**
	 * Private class to handle comparison of markers using their line numbers.
	 */
	private class CompareMarker implements Comparator {
		public int compare(Object o1, Object o2) {
			IMarker m1 = (IMarker) o1;
			IMarker m2 = (IMarker) o2;
			int l1 = MarkerUtilities.getLineNumber(m1);
			int l2 = MarkerUtilities.getLineNumber(m2);
			if (l1 > l2) return 1;
			if (l1 < l2) return -1;
			return 0;
		}
	}

	/**
	 * Creates new action.
	 */
	public GotoNextBookmarkAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		ITextEditor editor = getTextEditor();
		if (editor == null )
			return;

		ISelectionProvider provider = editor.getSelectionProvider();
		if (provider == null)
			return;

		ITextSelection selection = (ITextSelection) provider.getSelection();
		if (selection == null || selection.isEmpty())
			return;

		IEditorInput input= editor.getEditorInput();
		if (input == null)
			return;

		IResource resource = (IResource)(input).getAdapter(IResource.class);
		if (resource == null || !(resource instanceof IFile))
			return;

		try {
			IMarker[] bookmarks = resource.findMarkers(IMarker.BOOKMARK, true, IResource.DEPTH_ONE);
			if (bookmarks.length == 0)
				return;

			// sort bookmarks by line number
			CompareMarker comparator = new CompareMarker();
			Arrays.sort(bookmarks, comparator);

			// marker line numbers are 1-based
			int line = selection.getStartLine() + 1;
			IMarker lastBookmark = bookmarks[bookmarks.length - 1];

			// start from the beginning of file if reached or went beyond last bookmark
			if (line >= MarkerUtilities.getLineNumber(lastBookmark)) {
				line = 1;
			}

			// find the next bookmark and goto it
			for (int i = 0; i < bookmarks.length; i++) {
				IMarker bookmark = bookmarks[i];
				if (MarkerUtilities.getLineNumber(bookmark) > line) {
					IDE.openEditor(getTextEditor().getSite().getPage(), bookmark, OpenStrategy.activateOnOpen());
					break;
				}
			}
		}
		catch (CoreException e) {
			e.printStackTrace();
		}
	}

}

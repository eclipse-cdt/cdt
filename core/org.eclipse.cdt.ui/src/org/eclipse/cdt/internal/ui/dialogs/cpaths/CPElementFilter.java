package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Viewer filter for archive selection dialogs. Archives are files with file
 * extension '.so', '.dll' and '.a'. The filter is not case sensitive.
 */
public class CPElementFilter extends ViewerFilter {

	private List fExcludes;
	private int fKind;
	private boolean fExportedOnly;

	/**
	 * @param excludedFiles
	 *        Excluded paths will not pass the filter. <code>null</code> is
	 *        allowed if no files should be excluded.
	 * @param recusive
	 *        Folders are only shown if, searched recursivly, contain an archive
	 */
	public CPElementFilter(CPElement[] excludedElements, int kind, boolean exportedOnly) {
		if (excludedElements != null) {
			fExcludes = Arrays.asList(excludedElements);
		}
		fKind = kind;
		fExportedOnly = exportedOnly;
	}
	
	public CPElementFilter(int kind, boolean exportedOnly) {
		this(null, kind, exportedOnly);
	}
	

	/*
	 * @see ViewerFilter#select
	 */
	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof CPElement) {
			if (((CPElement) element).getEntryKind() == fKind) {
				if (fExcludes != null && !fExcludes.contains(element)) {
					if (fExportedOnly == true && !((CPElement) element).isExported()) {
						return false;
					}
					return true;
				}
			}
		} else if (element instanceof ICProject) {
			try {
				IPathEntry[] entries = ((ICProject) element).getRawPathEntries();
				for (int i = 0; i < entries.length; i++) {
					if (select(viewer, parent, CPElement.createFromExisting(entries[i], (ICProject) element))) {
						return true;
					}
				}
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e.getStatus());
			}
		} else if (element instanceof IPathEntry) {
			boolean bShow = ((IPathEntry) element).getEntryKind() == fKind;
			if (bShow && fExportedOnly == true) {
				return ((IPathEntry) element).isExported();
			}
			return bShow;
		}
		return false;
	}
}
package org.eclipse.cdt.internal.ui.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Creates a merge viewer for assembly code.
 */
public class AsmContentViewerCreator  implements IViewerCreator {

	/*
	 * @see org.eclipse.compare.IViewerCreator#createViewer(org.eclipse.swt.widgets.Composite, org.eclipse.compare.CompareConfiguration)
	 */
	public Viewer createViewer(Composite parent, CompareConfiguration config) {
		return new AsmMergeViewer(parent, SWT.NULL, config);
	}
}

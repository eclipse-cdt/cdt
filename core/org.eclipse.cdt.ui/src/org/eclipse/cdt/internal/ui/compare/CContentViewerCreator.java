package org.eclipse.cdt.internal.ui.compare;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;


import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;


import org.eclipse.jface.viewers.Viewer;


/**
 * Required when creating a CMergeViewer from the plugin.xml file.
 */
public class CContentViewerCreator implements IViewerCreator {
	
	public Viewer createViewer(Composite parent, CompareConfiguration mp) {
		return new CMergeViewer(parent, SWT.NULL, mp);
	}
}
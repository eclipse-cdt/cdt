/***********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class ErrorParserBlock extends AbstractCOptionPage {

	private static final String PREFIX = "ErrorParserBlock"; // $NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; // $NON-NLS-1$
	private static final String DESC = PREFIX + ".desc"; // $NON-NLS-1$

	private HashMap mapParsers = new HashMap();
	private CheckedListDialogField fErrorParserList;
	protected boolean listDirty = false;

	class FieldListenerAdapter implements  IDialogFieldListener {

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener#dialogFieldChanged(org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField)
		 */
		public void dialogFieldChanged(DialogField field) {
			listDirty = true;
		}

	}

	public ErrorParserBlock() {
		super(CUIPlugin.getResourceString(LABEL));
		setDescription(CUIPlugin.getResourceString(DESC));
	}

	public Image getImage() {
		return null;
	}

	/**
	 * Returns a label provider for the error parsers
	 *
	 * @return the content provider
	 */
	protected ILabelProvider getLabelProvider() {
		return new LabelProvider();
	}

	protected FieldListenerAdapter getFieldListenerAdapter() {
		return new FieldListenerAdapter();
	}

	protected void getPreferenceErrorParsers(List list) {
		String[] parserIDs = CCorePlugin.getDefault().getPreferenceErrorParserIDs();
		for (int i = 0; i < parserIDs.length; i++) {
			String parserName = (String)mapParsers.get(parserIDs[i]);
			if (parserName != null) {
				list.add(parserName);
			}
		}
	}

	/**
	 * To be implemented, abstract method.
	 * @param project
	 * @param list
	 */
	protected void getErrorParsers(IProject project, List list) {
	}

	/**
	 * To be implemented. abstract method.
	 * @param project
	 * @param parsers
	 */
	public void saveErrorParsers(IProject project, List parsers) {
	}

	protected void initMapParsers() {
		mapParsers.clear();
		IExtensionPoint point = CCorePlugin.getDefault().getDescriptor().getExtensionPoint(CCorePlugin.ERROR_PARSER_SIMPLE_ID);
		if (point != null) {
			IExtension[] exts = point.getExtensions();
			for (int i = 0; i < exts.length; i++) {
				mapParsers.put(exts[i].getUniqueIdentifier(), exts[i].getLabel());
			}
		}
	}

	protected void initializeValues() {
		initMapParsers();
		List list = new ArrayList(mapParsers.size());
		Iterator items =  mapParsers.values().iterator();
		while( items.hasNext()) {
			list.add((String)items.next());
		}
		fErrorParserList.setElements(list);

		list.clear();	
		IProject project = getContainer().getProject();
		if (project == null) {
			// Preference Page.
			getPreferenceErrorParsers(list);
		} else {
			// From the Project.
			getErrorParsers(project, list);
		}
		fErrorParserList.setCheckedElements(list);
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);

		String[] buttonLabels = new String[] {
			/* 0 */
			"up", //$NON-NLS-1$
			/* 1 */
			"down", //$NON-NLS-1$
			/* 2 */
			null,
			/* 3 */
			"checkall", //$NON-NLS-1$
			/* 4 */
			"uncheckall" //$NON-NLS-1$
		};

		fErrorParserList = new CheckedListDialogField(null, buttonLabels, getLabelProvider());
		fErrorParserList.setDialogFieldListener(getFieldListenerAdapter());
		fErrorParserList.setLabelText("Error Parsers"); //$NON-NLS-1$
		fErrorParserList.setUpButtonIndex(0);
		fErrorParserList.setDownButtonIndex(1);
		fErrorParserList.setCheckAllButtonIndex(3);
		fErrorParserList.setUncheckAllButtonIndex(4);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { fErrorParserList }, true);
		LayoutUtil.setHorizontalGrabbing(fErrorParserList.getListControl(null));

		initializeValues();
		setControl(composite);
	}

	public void performApply(IProgressMonitor monitor) throws CoreException {
		List list = fErrorParserList.getCheckedElements();
		if (listDirty) {
			IProject project = getContainer().getProject();
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			monitor.beginTask("Reference Projects", 1);
			saveErrorParsers(project, list);
		}
	}

	public void performDefaults() {
		initializeValues();
	}
}

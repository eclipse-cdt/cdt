/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;

public abstract class AbstractErrorParserBlock extends AbstractCOptionPage {

	private static final String PREFIX = "ErrorParserBlock"; // $NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; // $NON-NLS-1$
	private static final String DESC = PREFIX + ".desc"; // $NON-NLS-1$

	private static String[] EMPTY = new String[0];
	private Preferences fPrefs;
	protected HashMap mapParsers = new HashMap();
	private CheckedListDialogField fErrorParserList;
	protected boolean listDirty = false;
	private boolean usingDeprecatedContructor = false;

	class FieldListenerAdapter implements IDialogFieldListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener#dialogFieldChanged(org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField)
		 */
		public void dialogFieldChanged(DialogField field) {
			listDirty = true;
		}

	}

	/**
	 * @deprecated - use AbstractErrorParserBlock(), preferences setting should
	 *             now be handled by extending classes, use
	 *             setErrorParserIDs(boolean)/saveErrorParserIDs() to handle
	 *             getting/setting of values.
	 * 
	 * @param prefs
	 */
	public AbstractErrorParserBlock(Preferences prefs) {
		this();
		usingDeprecatedContructor = true;
		fPrefs = prefs;
	}

	public AbstractErrorParserBlock() {
		super(CUIPlugin.getResourceString(LABEL));
		setDescription(CUIPlugin.getResourceString(DESC));
	}

	public Image getImage() {
		return null;
	}

	public void updateValues() {
		fErrorParserList.removeAllElements();
		setValues();
	}

	/**
	 * Returns a label provider for the error parsers
	 * 
	 * @return the content provider
	 */
	protected ILabelProvider getLabelProvider() {
		return new LabelProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				String name = (String)mapParsers.get(element.toString());
				return name != null ? name : ""; //$NON-NLS-1$
			}
		};
	}

	protected FieldListenerAdapter getFieldListenerAdapter() {
		return new FieldListenerAdapter();
	}

	protected String[] getErrorParserIDs(boolean defaults) {
		String parserIDs = null;
		if (fPrefs != null) {
			if (defaults == true) {
				parserIDs = fPrefs.getDefaultString(ErrorParserManager.PREF_ERROR_PARSER);
			} else {
				parserIDs = fPrefs.getString(ErrorParserManager.PREF_ERROR_PARSER);
			}
		} else {
			return getErrorParserIDs();
		}
		String[] empty = new String[0];
		if (parserIDs != null && parserIDs.length() > 0) {
			StringTokenizer tok = new StringTokenizer(parserIDs, ";"); //$NON-NLS-1$
			List list = new ArrayList(tok.countTokens());
			while (tok.hasMoreElements()) {
				list.add(tok.nextToken());
			}
			return (String[])list.toArray(empty);
		}
		return empty;
	}

	/**
	 * To be implemented, abstract method.
	 * 
	 * @param project
	 * @return String[]
	 */
	protected abstract String[] getErrorParserIDs(IProject project);

	/**
	 * To be overloaded by subclasses with another method of getting the error
	 * parsers. For example, the managed builder new project wizard uses the
	 * selected Target.
	 *  @deprecated - use getErrorParserIDs(boolean defaults)
	 * @return String[]
	 */
	protected String[] getErrorParserIDs() {
		return new String[0];
	}

	/**
	 * To be implemented. abstract method.
	 * 
	 * @param project
	 * @param parsers
	 */
	protected abstract void saveErrorParsers(IProject project, String[] parserIDs) throws CoreException;

	/**
	 * @deprecated - use saveErrorParser(String[])
	 * @param prefs
	 * @param parserIDs
	 */

	protected void saveErrorParsers(Preferences prefs, String[] parserIDs) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < parserIDs.length; i++) {
			buf.append(parserIDs[i]).append(';');
		}
		prefs.setValue(ErrorParserManager.PREF_ERROR_PARSER, buf.toString());
	}

	protected void saveErrorParsers(String[] parserIDs) throws CoreException {
		saveErrorParsers(fPrefs, parserIDs);
	}

	protected void initMapParsers() {
		mapParsers.clear();
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID,
				CCorePlugin.ERROR_PARSER_SIMPLE_ID);
		if (point != null) {
			IExtension[] exts = point.getExtensions();
			for (int i = 0; i < exts.length; i++) {
				if (exts[i].getConfigurationElements().length > 0) {
					mapParsers.put(exts[i].getUniqueIdentifier(), exts[i].getLabel());
				}
			}
		}
	}

	protected void initializeValues() {
		initMapParsers();
		setValues();
	}

	protected void setDefaults() {
		String[] parserIDs;
		IProject project = getContainer().getProject();
		if (project == null) {
			// From a Preference.
			parserIDs = getErrorParserIDs(true);
		} else {
			parserIDs = getErrorParserIDs(false);
		}
		updateListControl(parserIDs);
	}

	protected void setValues() {
		String[] parserIDs;
		IProject project = getContainer().getProject();
		if (project == null) {
			parserIDs = getErrorParserIDs(false);
		} else {
			// From the Project.
			parserIDs = getErrorParserIDs(project);
		}
		updateListControl(parserIDs);
	}

	protected void updateListControl(String[] parserIDs) {
		List checkedList = Arrays.asList(parserIDs);
		fErrorParserList.setElements(checkedList);
		fErrorParserList.setCheckedElements(checkedList);
		if (checkedList.size() > 0) {
			fErrorParserList.getTableViewer().setSelection(new StructuredSelection(checkedList.get(0)), true);
		}
		Iterator items = mapParsers.keySet().iterator();
		while (items.hasNext()) {
			String item = (String)items.next();
			boolean found = false;
			for (int i = 0; i < parserIDs.length; i++) {
				if (item.equals(parserIDs[i])) {
					found = true;
					break;
				}
			}
			if (!found) {
				fErrorParserList.addElement(item);
			}
		}
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);

		WorkbenchHelp.setHelp(getControl(), ICHelpContextIds.ERROR_PARSERS_PAGE);

		String[] buttonLabels = new String[]{
		/* 0 */
		CUIMessages.getString("AbstractErrorParserBlock.label.up"), //$NON-NLS-1$
				/* 1 */
				CUIMessages.getString("AbstractErrorParserBlock.label.down"), //$NON-NLS-1$
				/* 2 */
				null,
				/* 3 */
				CUIMessages.getString("AbstractErrorParserBlock.label.selectAll"), //$NON-NLS-1$
				/* 4 */
				CUIMessages.getString("AbstractErrorParserBlock.label.unselectAll") //$NON-NLS-1$
		};

		fErrorParserList = new CheckedListDialogField(null, buttonLabels, getLabelProvider());
		fErrorParserList.setDialogFieldListener(getFieldListenerAdapter());
		fErrorParserList.setLabelText(CUIMessages.getString("AbstractErrorParserBlock.label.errorParsers")); //$NON-NLS-1$
		fErrorParserList.setUpButtonIndex(0);
		fErrorParserList.setDownButtonIndex(1);
		fErrorParserList.setCheckAllButtonIndex(3);
		fErrorParserList.setUncheckAllButtonIndex(4);

		LayoutUtil.doDefaultLayout(composite, new DialogField[]{fErrorParserList}, true);
		LayoutUtil.setHorizontalGrabbing(fErrorParserList.getListControl(null));

		initializeValues();
	}

	public void performApply(IProgressMonitor monitor) throws CoreException {
		if (listDirty) {
			IProject project = getContainer().getProject();
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			monitor.beginTask(CUIMessages.getString("AbstractErrorParserBlock.task.setErrorParser"), 1); //$NON-NLS-1$
			List elements = fErrorParserList.getElements();
			int count = elements.size();
			List list = new ArrayList(count);
			for (int i = 0; i < count; i++) {
				Object obj = elements.get(i);
				if (fErrorParserList.isChecked(obj)) {
					list.add(obj);
				}
			}

			String[] parserIDs = (String[])list.toArray(EMPTY);

			if (project == null) {
				//  Save to preferences
				saveErrorParsers(parserIDs);
			} else {
				saveErrorParsers(project, parserIDs);
			}
			monitor.worked(1);
			monitor.done();
		}
	}

	public void performDefaults() {
		setDefaults();
	}
}
/*******************************************************************************
 * Copyright (c) 2004, 2005 BitMethods Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * BitMethods Inc - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;


import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

/**
 * Field editor that uses FileListControl for user input.
 */

public class FileListControlFieldEditor extends FieldEditor {

	// file list control
	private FileListControl list;
	private int browseType;
	private GridLayout layout;
	private static final String DEFAULT_SEPERATOR = ";"; //$NON-NLS-1$

	//values
	private String[] values = null;

	/**
	 * Creates a file list control field editor.
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param parent the parent of the field editor's control
	 * @param type the browseType of the file list control
	 */
	public FileListControlFieldEditor(
		String name,
		String labelText,
		Composite parent,
		int type) {
		super(name, labelText, parent);
		browseType = type;
		// Set the browse strategy for the list editor
		list.setType(type);
	}

	/**
	 * Creates a file list control field editor.
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param parent the parent of the field editor's control
	 * @param value the field editor's value
	 * @param type the browseType of the file list control
	 */
	public FileListControlFieldEditor(
		String name,
		String labelText,
		Composite parent,
		String value,
		int type) {
		this(name, labelText, parent, type);
		browseType = type;
		this.values = parseString(value);
	}

	/**
	 * Fills this field editor's basic controls into the given parent.
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		Composite topLayout = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = numColumns;
		layout.marginWidth = 7;
		layout.marginHeight = 5;
		layout.makeColumnsEqualWidth = false;
		topLayout.setLayout(layout);
		GridData gddata = new GridData(GridData.FILL_HORIZONTAL);
		gddata.horizontalSpan = 2;
		topLayout.setLayoutData(gddata);
		// file list control
		list =
			new FileListControl(
				topLayout,
				getLabelText(),
				getType());
		list.addChangeListener(new IFileListChangeListener(){

			public void fileListChanged(FileListControl fileList, String oldValue[], String newValue[]) {
				handleFileListChange(fileList,oldValue,newValue);
			}
			
		});
		topLayout.setLayout(layout);
	}
	
	private void handleFileListChange(FileListControl fileList, String oldValue[], String newValue[]){
		values = fileList.getItems();
		fireValueChanged(
				FileListControlFieldEditor.this.getPreferenceName(),
				createList(oldValue),
				createList(newValue));
	}

	/**
	 * Returns the browseType of this field editor's file list control
	 * @return
	 */
	private int getType() {
		return browseType;
	}

	/**
	 * Returns the file list control 
	 * @return
	 */
	protected List getListControl() {
		return list.getListControl();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
	 */
	protected void doLoad() {
		if (list != null) {
			IPreferenceStore store = getPreferenceStore(); 
			if (store != null) {
				String s = store.getString(getPreferenceName());
				String[] array = parseString(s);
				list.setList(array);
				list.setSelection(0);
				// Set the resource the editor works for
				if (store instanceof BuildToolSettingsPreferenceStore) {
					IConfiguration config = ((BuildToolSettingsPreferenceStore)store).getSelectedConfig();
					if (config != null) {
						IResource project = config.getOwner();
						if (project != null) {
							list.setPath(project.getLocation());
						}
					}
				}
			}
		}
		list.selectionChanged();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	protected void doLoadDefault() {
		if (list != null) {
			list.removeAll();
			String s =
				getPreferenceStore().getDefaultString(getPreferenceName());
			String[] array = parseString(s);
			list.setList(array);
			list.selectionChanged();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	protected void doStore() {
		String s = createList(list.getItems());
		if (s != null)
			getPreferenceStore().setValue(getPreferenceName(), s);
	}
	
	public String[] getStringListValue(){
		return list.getItems();
	}

	/**
	* Returns the number of basic controls this field editor consists of.
	*
	* @return the number of controls
	*/
	public int getNumberOfControls() {
		return 1;
	}

	/**
	 * Answers a <code>String</code> containing the strings passed in the 
	 * argument separated by the DEFAULT_SEPERATOR
	 * 
	 * @param items An array of strings
	 * @return 
	 */
	private String createList(String[] items) {
		StringBuffer path = new StringBuffer(""); //$NON-NLS-1$

		for (int i = 0; i < items.length; i++) {
			path.append(items[i]);
			if (i < (items.length - 1)) {
				path.append(DEFAULT_SEPERATOR);
			}
		}
		return path.toString();
	}

	/**
	 * Parse the string with the separator and returns the string array. 
	 * @param stringList
	 * @return
	 */
	private String[] parseString(String stringList) {
		StringTokenizer tokenizer =
			new StringTokenizer(stringList, DEFAULT_SEPERATOR);
		ArrayList list = new ArrayList();
		while (tokenizer.hasMoreElements()) {
			list.add(tokenizer.nextElement());
		}
		return (String[]) list.toArray(new String[list.size()]);
	}

	/**
	 * Set style
	 */
	public void setStyle() {
		layout.marginWidth = 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	protected void adjustForNumColumns(int numColumns) {

	}
	
    public Label getLabelControl(Composite parent) {
    	return list.getLabelControl();
    }
    
    public void setEnabled(boolean enabled, Composite parent) {
    	list.setEnabled(enabled);
    }

}

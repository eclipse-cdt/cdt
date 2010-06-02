/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.pages;

import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Element;

import org.eclipse.cdt.core.templateengine.SharedDefaults;
import org.eclipse.cdt.core.templateengine.TemplateEngine;
import org.eclipse.cdt.core.templateengine.TemplateEngineHelper;
import org.eclipse.cdt.core.templateengine.TemplateEngineUtil;
import org.eclipse.cdt.ui.CUIPlugin;


/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By Provided GUI for SharedDefaults settings for the
 * Templates present in the Template Engine
 */

public class TemplatePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Preference Page buttons
	 */

	private static final String EditButton = Messages.getString("TemplatePreferencePage.8"); //$NON-NLS-1$
	private static final String DeleteButton = Messages.getString("TemplatePreferencePage.9"); //$NON-NLS-1$
	private static final String PageDescription = Messages.getString("TemplatePreferencePage.0");//$NON-NLS-1$
	public static final String Blank = "";//$NON-NLS-1$

	/**
	 * Validation Messages
	 */
	public static final String Message = Messages.getString("TemplatePreferencePage.1");//$NON-NLS-1$
	protected static final String DuplicateEntry = Messages.getString("TemplatePreferencePage.2");//$NON-NLS-1$
	private static final String DeleteValidator = Messages.getString("TemplatePreferencePage.3");//$NON-NLS-1$
	private static final String DeleteShellMessage = Messages.getString("TemplatePreferencePage.4");//$NON-NLS-1$

	/**
	 * Button ToolTips
	 */
	private static final String TableToolTip = Messages.getString("TemplatePreferencePage.5");//$NON-NLS-1$
	private static final String EditToolTip = Messages.getString("TemplatePreferencePage.6");//$NON-NLS-1$
	private static final String DeleteToolTip = Messages.getString("TemplatePreferencePage.7");//$NON-NLS-1$

	/**
	 * Class instances
	 */
	private static TemplateInputDialog inputDialog;
	private static SharedDefaults sharedDefaults = SharedDefaults.getInstance();

	/**
	 * Table Attributes
	 */
	private int columnWidth = 100;
	private int columnWeight = 50;
	private String columnNames[];
	private static List<Element> sharedElementList;
	private int attrListSize;

	private ColumnLayoutData columnLayouts[] = { new ColumnWeightData(columnWeight, columnWidth),
			new ColumnWeightData(columnWeight, columnWidth) };

	/**
	 * InfoHelp for SharedDefault
	 */
	private String pageID;
	private String SharedContextHelpID = "shared_defaults_help";//$NON-NLS-1$

	/**
	 * Button instance for ADD/EDIT/DELETE
	 */

	private Button editButton;
	private Button deleteButton;

	/**
	 * Checks for row(s) deletion
	 */
	private static boolean isDeleted;

	/**
	 * Checks for redundant data
	 */
	private boolean isRedundant;

	/**
	 * Takes isRedundant reference to get reflected in different class scope See
	 * TemplateInputDialog class
	 */
	public static boolean isDup;
	private static String delItemNames[] = null;
	private static Table table;

	/**
	 * Add/Edit option values
	 */
	protected static final int OPTION_ADD = 0;
	protected static final int OPTION_EDIT = 1;

	/**
	 * Dialog input values arriving from TemplateInputDialog class
	 */
	private String name;
	private String value;

	/**
	 * Constructor to initialize defaults
	 */

	public TemplatePreferencePage() {

		noDefaultAndApplyButton();
		initializeDefaults();
	}

	/**
	 * Sets the values of the Message Dialog
	 * 
	 * @param aName
	 * @param aValue
	 */
	public TemplatePreferencePage(String aName, String aValue) {
		this.name = aName;
		this.value = aValue;
	}

	/**
	 * Sets default settings and gathers attributes from the XML for Table
	 * properties
	 */

	private void initializeDefaults() {
		columnNames = new String[] { Messages.getString("TemplatePreferencePage.10"), Messages.getString("TemplatePreferencePage.11") };  //$NON-NLS-1$//$NON-NLS-2$
		// Setting InfoPop help (plugin-id+ContextID).
		pageID = CUIPlugin.getPluginId() + "." + //$NON-NLS-1$
				SharedContextHelpID;

		setTableAttributes();

	}

	/**
	 * Creates controls on the Preference Page Adds the created Table and Button
	 * composite to the parent composite.
	 * 
	 * @param parent
	 * @return subComposite
	 */

	@Override
	protected Control createContents(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		Label pageLabel = new Label(composite, SWT.NONE);
		pageLabel.setText(PageDescription);

		Composite subComposite = new Composite(parent, SWT.NONE);
		GridLayout subLayout = new GridLayout(2, false);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		subComposite.setLayout(subLayout);
		subComposite.setLayoutData(gridData);

		addFirstSection(subComposite);
		addSecondSection(subComposite);

		// Info help for SharedDefault is displayed when Functional Key (F1) is
		// triggered.
		PlatformUI.getWorkbench().getHelpSystem().setHelp(super.getControl(), pageID);
		return subComposite;

	}

	/**
	 * Adds table into the first composite present under parent Updates the
	 * Table with backend persistence data.
	 * 
	 * @param parent
	 */

	private void addFirstSection(Composite parent) {
		createTable(parent);
		setTableAttributes();
		addXMLDataIntoTable();
	}

	/**
	 * Creates second composite for buttons present under the parent
	 * 
	 * @param parent
	 */

	private void addSecondSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);
		addButtonControls(composite);
	}

	/**
	 * Creates default composite area for the Buttons
	 * 
	 * @param parent
	 * @return composite
	 */

	private Composite createDefaultComposite(Composite parent) {

		Composite composite = new Composite(parent, SWT.NULL);
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		layout.spacing = 5;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);

		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.BEGINNING;
		composite.setLayoutData(gridData);

		return composite;

	}

	/**
	 * Creates Table with XML properties as its settings
	 * 
	 * @param composite
	 */

	private void createTable(Composite composite) {

		table = new Table(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER
				| SWT.NO_REDRAW_RESIZE);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = convertHeightInCharsToPixels(10);
		gridData.widthHint = convertWidthInCharsToPixels(10);

		table.setLayoutData(gridData);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setToolTipText(TableToolTip);

		// The attribute size becomes zero when no data
		// remains in the table. To avoid fault creation of
		// the table the attribute size to required columns
		// in the table.
		if (attrListSize == 0) {
			attrListSize = 2;
		}

		for (int nCols = 0; nCols < attrListSize; nCols++) {
			layout.addColumnData(columnLayouts[nCols]);
			TableColumn tColumn = new TableColumn(table, SWT.LEFT, nCols);
			tColumn.setWidth(columnWidth);
			tColumn.setText(columnNames[nCols]);
		}

		addTableListener();
	}

	/**
	 * Table listener added to enable EDIT/DELETE functionality only when table
	 * listenes to an event.
	 */
	private void addTableListener() {

		SelectionListener sListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean isSelected = table.isSelected(table.getSelectionIndex());
				int selectionCount = table.getSelectionCount();

				// Enable EDIT/DELETE button when row(s) get selected
				if (isSelected) {
					editButton.setEnabled(true);
					deleteButton.setEnabled(true);
				}

				// disable EDIT button if more than one row is selected
				if (selectionCount > 1)
					editButton.setEnabled(false);
			}
		};
		table.addSelectionListener(sListener);
	}

	/**
	 * Adds XML backend data into the table Supports for pesistency.
	 */

	private void addXMLDataIntoTable() {
		for (int i = 0, l = sharedElementList.size(); i < l; i++) {
			Element xmlElement = sharedElementList.get(i);
			String name = xmlElement.getAttribute(TemplateEngineHelper.ID);
			String value = xmlElement.getAttribute(TemplateEngineHelper.VALUE);

			if (name.equals(Blank) && value.equals(Blank))
				return;

			String backEndData[] = new String[] { name, value };
			TableItem backEndItem = new TableItem(table, SWT.NONE);

			for (int data = 0; data < backEndData.length; data++) {
				if (backEndData[data] != null)
					backEndItem.setText(data, backEndData[data]);
			}
		}
	}

	/**
	 * Creates button controls on the first composite present under parent
	 * composite. Its aligned at rightmost end of Table and top of the second
	 * composite.
	 * 
	 * @param composite
	 */

	private void addButtonControls(Composite composite) {

		editButton = new Button(composite, SWT.PUSH);
		editButton.setText(EditButton);
		editButton.setEnabled(false);
		editButton.setToolTipText(EditToolTip);
		addButtonListener(editButton);

		deleteButton = new Button(composite, SWT.PUSH);
		deleteButton.setText(DeleteButton);
		deleteButton.setEnabled(false);
		deleteButton.setToolTipText(DeleteToolTip);
		addButtonListener(deleteButton);

	}

	/**
	 * Constructs button listeners to trigger specific functionality
	 * 
	 * @param button
	 */
	public void addButtonListener(final Button button) {

		inputDialog = new TemplateInputDialog(getShell());
		SelectionListener listener = new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(editButton)) {
					String editItemString = getSelectedItemNameFromTable();
					if (editItemString != null) {
						if (editItemString != Blank) {
							inputDialog.open(inputDialog, OPTION_EDIT);
						}
					}
				}

				if (e.getSource().equals(deleteButton)) {
					deleteRow();
					editButton.setEnabled(false);
					deleteButton.setEnabled(false);
				}
			}
		};
		button.addSelectionListener(listener);
	}

	/**
	 * Adding new data into the table and adds the same data to the backend XML
	 * Checks for duplicate entries and null values
	 */

	public void addNewDataIntoTable() {

		String addData[] = new String[] { name, value };
		if (!isDeleted) {
			TableItem duplicateItems[] = table.getItems();
			TableItem duplicateItem = null;

			for (TableItem duplicateItem2 : duplicateItems) {
				duplicateItem = duplicateItem2;
				String duplicateString = duplicateItem.getText();

				if (duplicateString.equals(name)) {
					int result = inputDialog.popDuplicate();
					if (result == SWT.OK) {
						isRedundant = true;
						isDup = isRedundant;
						break;
					}
				}
			}

			// Check if not redundant
			if (!isRedundant) {
				TableItem tableItem = new TableItem(table, SWT.NONE);

				for (int data = 0; data < addData.length; data++) {
					tableItem.setText(data, addData[data]);
				}

				isRedundant = false;
			}
		}

		sharedDefaults.addToBackEndStorage(name, value);
	}

	/**
	 * Gets the size of the Attributes of an Element of XML file
	 * 
	 * @param sharedElementList
	 * @return attrListSize
	 */
	private int getAttributeSize() {
		try {
			int listSize = sharedElementList.size();
			int i = 0;
			while (i < listSize) {
				Element xmlElement = sharedElementList.get(i++);
				attrListSize = xmlElement.getAttributes().getLength();
			}
		} catch (Exception exp) {
			TemplateEngineUtil.log(exp);
		}
		return attrListSize;
	}

	/**
	 * Setting the table attributes with the XML properties Sets XML-document
	 * Element List as the number of table rows and XML-document Attribute List
	 * as the number of table columns
	 */

	private void setTableAttributes() {

		try {
			SharedDefaults sharedTemp = new SharedDefaults();
			sharedElementList = TemplateEngine.getChildrenOfElement(sharedTemp.document.getDocumentElement());
			attrListSize = getAttributeSize();
			sharedDefaults.putAll(sharedTemp.getSharedDefaultsMap());

		} catch (Exception exp) {
			TemplateEngineUtil.log(exp);
		}
	}

	public void init(IWorkbench workbench) {

	}

	/**
	 * Updating data with the changed value for a given ID in the table.
	 */

	public void updateDataInTheTable() {

		try {
			int selectedItemIndex = table.getSelectionIndex();
			TableItem selectedItem = table.getItem(selectedItemIndex);
			String updateString[] = new String[] { name, value };
			selectedItem.setText(updateString);
			sharedDefaults.updateToBackEndStorage(name, value);
		} catch (Exception exp) {
			TemplateEngineUtil.log(exp);
		}
	}

	/**
	 * Gives the item for the selected row in the table
	 * 
	 * @return selectedItemName
	 */

	public static String getSelectedItemNameFromTable() {

		String selectedItemName = null;
		int selectedItemIndex = 0;

		try {
			selectedItemIndex = table.getSelectionIndex();
		}

		catch (Exception exp) {
			TemplateEngineUtil.log(exp);
		}

		TableItem selectedItem = table.getItem(selectedItemIndex);
		selectedItemName = selectedItem.getText();
		return selectedItemName;

	}

	/**
	 * Deletes the data for the specified row Data also gets deleted at the
	 * backend with Key-name as an identifier.
	 */
	private void deleteRow() {

		int result = 0;
		String nonEmptyItemString = getSelectedItemNameFromTable();

		if (nonEmptyItemString != null)
			result = confirmdeleteContents(nonEmptyItemString);

		if (result == SWT.OK) {
			int itemSelected[] = table.getSelectionIndices();
			table.remove(itemSelected);
			
			if (delItemNames != null) {
				sharedDefaults.deleteBackEndStorage(delItemNames);
			}
		}

		else if (result == SWT.CANCEL)
			isDeleted = false;
	}

	/**
	 * Sets confirmation for the data deletion at the fronend and backend
	 * 
	 * @param selectedItems
	 * @return result
	 */

	private int confirmdeleteContents(String selectedItems) {

		int result = 0;
		TableItem deleteItems[] = null;

		if (selectedItems != Blank) {

			deleteItems = table.getSelection();
			delItemNames = new String[deleteItems.length];

			for (int nDel = 0; nDel < deleteItems.length; nDel++) {

				TableItem item = deleteItems[nDel];
				delItemNames[nDel] = item.getText();

			}

			MessageBox mBox = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
			mBox.setText(DeleteShellMessage);
			mBox.setMessage(DeleteValidator);
			result = mBox.open();

		}

		return result;

	}

}


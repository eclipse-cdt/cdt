package org.eclipse.cdt.managedbuilder.ui.properties;

/**********************************************************************
 * Copyright (c) 2002,2004 IBM Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

public class BuildOptionListFieldEditor extends FieldEditor {
	/**
	 * Multi-purpose dialog to prompt the user for a value, path, or file.
	 * 
	 * @since 2.0
	 */
	class SelectPathInputDialog extends InputDialog {
		// Constants for externalized strings
		private static final String BROWSE = "BuildPropertyCommon.label.browse"; //$NON-NLS-1$
		private int type;
		
		/**
		 * @param parentShell
		 * @param dialogTitle
		 * @param dialogMessage
		 * @param initialValue
		 * @param validator
		 * @param type
		 */
		public SelectPathInputDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue, IInputValidator validator, int type) {
			super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
			this.type = type;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
		 */
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			if (type != IOption.BROWSE_NONE) {
				final Button browse = createButton(parent, 3, ManagedBuilderUIPlugin.getResourceString(BROWSE), true);
				browse.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent ev) {
						String currentName;
						String result;
						switch (type) {
							case IOption.BROWSE_DIR :
								DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
								currentName = getText().getText();
								if(currentName != null && currentName.trim().length() != 0) {
									dialog.setFilterPath(currentName);
								}
								result = dialog.open();
								if(result != null) {
									getText().setText(result);
								}
								break;
							case IOption.BROWSE_FILE:
								FileDialog browseDialog = new FileDialog(getShell());
								currentName = getText().getText();
								if (currentName != null && currentName.trim().length() != 0) {
									browseDialog.setFilterPath(currentName);
								}
								result = browseDialog.open();
								if (result != null) {
									getText().setText(result);
								}
								break;
						}
					}
				});
			}
		}

	}

	// Label constants
	private static final String LABEL = "BuildPropertyCommon.label";	//$NON-NLS-1$
	private static final String TITLE = LABEL + ".title";	//$NON-NLS-1$
	private static final String NEW = LABEL + ".new"; //$NON-NLS-1$
	private static final String REMOVE = LABEL + ".remove"; //$NON-NLS-1$
	private static final String UP = LABEL + ".up"; //$NON-NLS-1$
	private static final String DOWN = LABEL + ".down"; //$NON-NLS-1$
	private static final String EDIT = LABEL + ".editVar";	//$NON-NLS-1$
	private static final String FILE_TITLE = "BrowseEntryDialog.title.file";	//$NON-NLS-1$
	private static final String DIR_TITLE = "BrowseEntryDialog.title.directory";	//$NON-NLS-1$
	private static final String FILE_MSG = "BrowseEntryDialog.message.file";	//$NON-NLS-1$
	private static final String DIR_MSG = "BrowseEntryDialog.message.directory";	//$NON-NLS-1$
	
	// The top-level control for the field editor.
	private Composite top;
	// The list of tags.
	private List list;

	// The group control for the list and button composite
	private Group controlGroup;
	
	private String fieldName;
	private SelectionListener selectionListener;
	private int browseType;
	private IConfiguration configuration;
	private IResource owner;
	
	// The button for adding the contents of the text field to the list
	private Button addButton;
	// The button for swapping the currently-selected list item down
	private Button downButton;
	// The button to start the edit process
	private Button editButton;
	// The button for removing the currently-selected list item.
	private Button removeButton;
	// The button for swapping the currently selected item up
	private Button upButton;

	/**
	* @param name the name of the preference this field editor works on
	* @param labelText the label text of the field editor
	* @param parent the parent of the field editor's control
	*/
	public BuildOptionListFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
		this.fieldName = labelText;
		browseType = IOption.BROWSE_NONE;

	}

	/* (non-Javadoc)
	 * Event handler for the addButton widget
	 */
	protected void addPressed() {
		setPresentsDefaultValue(false);
		// Prompt user for a new item
		String input = getNewInputObject();
		
		// Add it to the list
		if (input != null && input.length() > 0) {
			int index = list.getSelectionIndex();
			if (index >= 0) {
				list.add(input, index + 1);
				list.setSelection(index + 1);
			}
			else {
				list.add(input, 0);
				list.setSelection(0);
			}
			selectionChanged();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	protected void adjustForNumColumns(int numColumns) {
		((GridData)top.getLayoutData()).horizontalSpan = numColumns;
	}

	/* (non-Javadoc)
	 * Creates the Add, Remove, Up, and Down button in the button composite.
	 *
	 * @param container the box for the buttons
	 */
	private void createButtons(Composite container) {
		addButton = createPushButton(container, ManagedBuilderUIPlugin.getResourceString(NEW));
		editButton = createPushButton(container, ManagedBuilderUIPlugin.getResourceString(EDIT));
		removeButton = createPushButton(container, ManagedBuilderUIPlugin.getResourceString(REMOVE));
		upButton = createPushButton(container, ManagedBuilderUIPlugin.getResourceString(UP));
		downButton = createPushButton(container, ManagedBuilderUIPlugin.getResourceString(DOWN));
	}

	/**
	 * @param items
	 * @return
	 */
	protected String createList(String[] items) {
		return BuildToolsSettingsStore.createList(items);
	}

	/* (non-Javadoc)
	 * Rather than using the ControlFactory helper methods, this field
	 * editor is using this helper method. Other field editors use a similar
	 * set of method calls, so this seems like the safest approach 
	 * 
	 * @param parent the button composite
	 * @param label the label to place in the button
	 * @return
	 */
	private Button createPushButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = convertVerticalDLUsToPixels(button, IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		button.addSelectionListener(getSelectionListener());
		return button;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		top = parent;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = numColumns;
		top.setLayoutData(gd);

		controlGroup = ControlFactory.createGroup(top, getLabelText(), 2);
		GridData groupData = new GridData(GridData.FILL_HORIZONTAL);
		groupData.horizontalSpan = numColumns;
		controlGroup.setLayoutData(groupData);

		// Make the list
		list = new List(controlGroup, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		list.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectionChanged();
			}
		});

		list.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				list = null;
			}
		});
		list.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				// Popup the editor on the selected item from the list
				editSelection();
			}
		});
		list.addKeyListener(new KeyAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.KeyAdapter#keyPressed(org.eclipse.swt.events.KeyEvent)
			 */
			public void keyPressed(KeyEvent e) {
				// Is this the delete key
				if (e.keyCode == SWT.DEL) {
					removePressed();
				} else {
					super.keyPressed(e);
				}
			}
		});

		// Create a composite for the buttons
		Composite buttonGroup = new Composite(controlGroup, SWT.NONE);
		GridData buttonData = new GridData();
		buttonData.horizontalSpan = 1;
		buttonData.verticalAlignment = GridData.BEGINNING;
		buttonGroup.setLayoutData(buttonData);
	
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.numColumns = 1;
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 0;
		buttonGroup.setLayout(buttonLayout);
		
		buttonGroup.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				addButton = null;
				editButton = null;
				removeButton = null;
				upButton = null;
				downButton = null;
			}
		});
	
		// Create the buttons
		createButtons(buttonGroup);

		// Create a grid data that takes up the extra space in the dialog and spans one column.
		GridData listData = new GridData(GridData.FILL_HORIZONTAL);
		Point buttonGroupSize = buttonGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT); 
		listData.heightHint = buttonGroupSize.y;
		listData.widthHint = buttonGroupSize.x * 2;
		list.setLayoutData(listData);
	}

	/* (non-Javadoc)
	 * Creates a selection listener that handles the selection events
	 * for the button controls and single-click events in the list to 
	 * trigger a selection change.
	 */
	public void createSelectionListener() {
		selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Widget widget = event.widget;
				if (widget == addButton) {
					addPressed();
				} else if (widget == editButton) {
					editPressed();
				} else if (widget == removeButton) {
					removePressed();
				} else if (widget == upButton) {
					upPressed();
				} else if (widget == downButton) {
					downPressed();
				} else if (widget == list) {
					selectionChanged();
				}
			}

		};
	}



	/* (non-Javadoc)
	 * Event handler for the down button
	 */
	protected void downPressed() {
		swap(false);		
	}

	/* (non-Javadoc)
	 * Event handler for the edit button pressed event. Delegates
	 * the work to a helper method.
	 */
	private void editPressed() {
		editSelection();
	}

	/* (non-Javadoc)
	 * Edit the value of the selected item.
	 */
	protected void editSelection() {
		// Edit the selection index
		int index = list.getSelectionIndex();
		if (index != -1) {
			String selItem = list.getItem(index);
			if (selItem != null) {
				InputDialog dialog = new InputDialog(getShell(), ManagedBuilderUIPlugin.getResourceString(TITLE), fieldName, selItem, null);
				String newItem = null;
				if (dialog.open() == InputDialog.OK) {
					newItem = dialog.getValue();
					if (newItem != null && !newItem.equals(selItem)) {
						list.setItem(index, newItem);
						selectionChanged();
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
	 */
	protected void doLoad() {
		if (list != null) {
			String s = getPreferenceStore().getString(getPreferenceName());
			String[] array = parseString(s);
			for (int i = 0; i < array.length; i++){
				list.add(array[i]);
			}
			list.setSelection(0);
			selectionChanged();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	protected void doLoadDefault() {
		if (list != null) {
			list.removeAll();
			String s = getPreferenceStore().getDefaultString(getPreferenceName());
			String[] array = parseString(s);
			for (int i = 0; i < array.length; i++){
				list.add(array[i]);
			}
			list.setSelection(0);
			selectionChanged();
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

	/* (non-Javadoc)
	 * @return Returns the configuration.
	 */
	private IConfiguration getConfiguration() {
		if (configuration == null) {
			BuildToolsSettingsStore store = (BuildToolsSettingsStore)getPreferenceStore();
			if (store != null) {
				configuration = store.getOwner();
			}
		}
		return configuration;
	}
	
	/*(non-Javadoc)
	 * @return Returns the owner.
	 */
	private IResource getOwner() {
		if (owner == null) {
			IConfiguration config = getConfiguration();
			if (config != null) {
				owner = config.getOwner();
			}
		}
		return owner;
	}
	
	/* (non-Javadoc)
	 * Answers a <code>String</code> containing the value the user entered, or 
	 * <code>null</code> if the user cancelled the interaction.
	 * 
	 * @return 
	 */
	protected String getNewInputObject() {
		// Create a dialog to prompt for a new list item
		String input = null;
		String title = new String();
		String message = new String();
		String initVal = new String();
		IPath path = null;
		
		if (browseType == IOption.BROWSE_DIR) {
			title = ManagedBuilderUIPlugin.getResourceString(DIR_TITLE);
			message = ManagedBuilderUIPlugin.getResourceString(DIR_MSG);
			path = getOwner().getLocation();
			initVal = path == null ? initVal : path.toString();
		} else if (browseType == IOption.BROWSE_FILE) {
			title = ManagedBuilderUIPlugin.getResourceString(FILE_TITLE);
			message = ManagedBuilderUIPlugin.getResourceString(FILE_MSG);
			path = getOwner().getLocation();
			initVal = path == null ? initVal : path.toString();
		} else {
			title = ManagedBuilderUIPlugin.getResourceString(TITLE);
			message = fieldName;
		}
		
		// Prompt for value
		SelectPathInputDialog dialog = new SelectPathInputDialog(getShell(), title, message, initVal, null, browseType);
		if (dialog.open() == SelectPathInputDialog.OK) {
			input = dialog.getValue().trim();
		}
		
		// Convert the value based on the type of input we expect
		switch (browseType) {
			case IOption.BROWSE_DIR:
			case IOption.BROWSE_FILE:
				String[] segments = input.split("\\s"); //$NON-NLS-1$
				if (segments.length > 1) {
					// Double-quote paths with whitespaces
					input = "\"" + input + "\"";
				}
				break;
			default:
				break;
		}
		
		return input;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
	 */
	public int getNumberOfControls() {
		// The group control has a list and buttons so we want it to get at
		// least 2 columns to display in.
		return 2;
	}

	/* (non-Javadoc)
	 * Returns this field editor's selection listener.
	 * The listener is created if nessessary.
	 *
	 * @return the selection listener
	 */
	private SelectionListener getSelectionListener() {
		if (selectionListener == null)
			createSelectionListener();
		return selectionListener;
	}

	/* (non-Javadoc)
	 * Returns this field editor's shell.
	 * 
	 * @return the shell
	 */
	protected Shell getShell() {
		if (addButton == null)
			return null;
		return addButton.getShell();
	}

	/* (non-Javadoc)
	 * @param stringList
	 * @return
	 */
	protected String[] parseString(String stringList) {
		return BuildToolsSettingsStore.parseString(stringList);
	}

	/* (non-Javadoc)
	 * Event handler for the removeButton selected event
	 */
	protected void removePressed() {
		// Remove the selected item from the list
		setPresentsDefaultValue(false);
		int index = list.getSelectionIndex();
		if (index >= 0) {
			list.remove(index);
			if (index - 1 < 0) {
				list.setSelection(0);
			} else {
				list.setSelection(index - 1);
			}
			selectionChanged();
		}
	}

	/* (non-Javadoc)
	 * Clean up the list and button control states after the event 
	 * handlers fire. 
	 */
	protected void selectionChanged() {
		int index = list.getSelectionIndex();
		int size = list.getItemCount();

		// Enable the edit button if there is at least one item in the list
		editButton.setEnabled(size > 0);
		// Enable the remove button if there is at least one item in the list
		removeButton.setEnabled(size > 0);
		// Enable the up button IFF there is more than 1 item and selection index is not first item
		upButton.setEnabled(size > 1 && index > 0);
		// Enable the down button IFF there is more than 1 item and selection index not last item
		downButton.setEnabled(size > 1 && index >= 0 && index < size - 1);
	}

	/**
	 * Set the behaviour of the field editor when the new button is pressed. 
	 * 
	 * @param browseType
	 */
	public void setBrowseStrategy(int browseType) {
		this.browseType = browseType;
	}

	/* (non-Javadoc)
	 * Swaps the location of two list elements. If the argument is <code>true</code> 
	 * the list item is swapped with the item preceeding it in the list. Otherwise 
	 *  it is swapped with the item following it.
	 * 
	 * @param moveUp
	 */
	private void swap(boolean moveUp) {
		setPresentsDefaultValue(false);
		int index = list.getSelectionIndex();
		int target = moveUp ? index - 1 : index + 1;

		if (index >= 0) {
			String[] selection = list.getSelection();
			Assert.isTrue(selection.length == 1);
			list.remove(index);
			list.add(selection[0], target);
			list.setSelection(target);
		}
		selectionChanged();
	}

	/* (non-Javadoc)
	 * Event handler for the up button. It simply swaps the selected 
	 * item with the list item above it.
	 */
	protected void upPressed() {
		swap(true);
	}
}

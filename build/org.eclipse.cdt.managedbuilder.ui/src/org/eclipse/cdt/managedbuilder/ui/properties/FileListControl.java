/*******************************************************************************
 * Copyright (c) 2004, 2005 BitMethods Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BitMethods Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIImages;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Instances of this class allow the user to add,remove, delete, moveup and movedown
 * the items in the list control.
 */

public class FileListControl {
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
		 * @param browseType
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
				final Button browse = createButton(parent, 3, ManagedBuilderUIMessages.getResourceString(BROWSE), false);
				browse.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent ev) {
						String currentName;
						String result;
						switch (type) {
							case IOption.BROWSE_DIR :
								DirectoryDialog dialog = new DirectoryDialog(getParentShell(), SWT.OPEN);
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
								FileDialog browseDialog = new FileDialog(getParentShell());
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

	//toolbar
	private ToolBar toolBar;
	// toolbar items
	private ToolItem titleItem, addItem, deleteItem, editItem, moveUpItem,
			moveDownItem;
	// title label
	private Label title;
	// images
	private Image addImage, deleteImage, editImage, moveUpImage, moveDownImage;
	private Composite composite;
	// list control
	private List list;
	private String compTitle;
	private SelectionListener selectionListener;
	private GridData tgdata, grid3, grid4, grid2;
	
	// The type of browse support that is required
	private int browseType;
	private IPath path;

	private java.util.List listeners = new ArrayList();
	private String oldValue[];
	
	private static final String ADD_STR = ManagedBuilderUIMessages.getResourceString("FileListControl.add"); //$NON-NLS-1$
	private static final String DEL_STR = ManagedBuilderUIMessages.getResourceString("FileListControl.delete"); //$NON-NLS-1$
	private static final String EDIT_STR = ManagedBuilderUIMessages.getResourceString("FileListControl.edit"); //$NON-NLS-1$
	private static final String MOVEUP_STR = ManagedBuilderUIMessages.getResourceString("FileListControl.moveup"); //$NON-NLS-1$
	private static final String MOVEDOWN_STR = ManagedBuilderUIMessages.getResourceString("FileListControl.movedown"); //$NON-NLS-1$
	private static final String FILE_TITLE = ManagedBuilderUIMessages.getResourceString("BrowseEntryDialog.title.file");	//$NON-NLS-1$
	private static final String DIR_TITLE = ManagedBuilderUIMessages.getResourceString("BrowseEntryDialog.title.directory");	//$NON-NLS-1$
	private static final String FILE_MSG = ManagedBuilderUIMessages.getResourceString("BrowseEntryDialog.message.file");	//$NON-NLS-1$
	private static final String DIR_MSG = ManagedBuilderUIMessages.getResourceString("BrowseEntryDialog.message.directory");	//$NON-NLS-1$
	private static final String TITLE = ManagedBuilderUIMessages.getResourceString("BuildPropertyCommon.label.title");	//$NON-NLS-1$
	//images
	private final Image IMG_ADD = ManagedBuilderUIImages
			.get(ManagedBuilderUIImages.IMG_FILELIST_ADD);
	private final Image IMG_DEL = ManagedBuilderUIImages
			.get(ManagedBuilderUIImages.IMG_FILELIST_DEL);
	private final Image IMG_EDIT = ManagedBuilderUIImages
			.get(ManagedBuilderUIImages.IMG_FILELIST_EDIT);
	private final Image IMG_MOVEUP = ManagedBuilderUIImages
			.get(ManagedBuilderUIImages.IMG_FILELIST_MOVEUP);
	private final Image IMG_MOVEDOWN = ManagedBuilderUIImages
			.get(ManagedBuilderUIImages.IMG_FILELIST_MOVEDOWN);
	
	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param compTitle
	 * @param browseType
	 */
	public FileListControl(Composite parent, String compTitle, int type) {
		// Default to no browsing
		browseType = type;

		//file panel
		Composite filePanel = new Composite(parent, SWT.NONE);
		GridLayout form1 = new GridLayout();
		form1.numColumns = 1;
		form1.horizontalSpacing = 0;
		form1.verticalSpacing = 0;
		form1.marginHeight = 0;
		form1.marginWidth = 0;
		filePanel.setLayout(form1);
		filePanel.setLayoutData(new GridData(GridData.FILL_BOTH));

		// title panel
		Composite titlePanel = new Composite(filePanel, SWT.BORDER);
		GridLayout titleform = new GridLayout(2, false);
		titleform.horizontalSpacing = 0;
		titleform.verticalSpacing = 0;
		titleform.marginHeight = 0;
		titleform.marginWidth = 0;
		titlePanel.setLayout(titleform);
		tgdata = new GridData(GridData.FILL_HORIZONTAL);
		tgdata.heightHint = IDialogConstants.BUTTON_BAR_HEIGHT;
		titlePanel.setLayoutData(tgdata);
		title = new Label(titlePanel, SWT.NONE | SWT.BOLD);
		this.compTitle = "  " + compTitle; //$NON-NLS-1$
		title.setText(this.compTitle);
		grid2 = new GridData(GridData.FILL_HORIZONTAL);
		title.setLayoutData(grid2);
		//button panel
		Composite buttonPanel = new Composite(titlePanel, SWT.NONE);
		GridLayout form2 = new GridLayout();
		form2.numColumns = 5;
		form2.horizontalSpacing = 0;
		form2.verticalSpacing = 0;
		form2.marginWidth = 0;
		form2.marginHeight = 0;
		buttonPanel.setLayout(form2);
		// toolbar
		toolBar = new ToolBar(buttonPanel, SWT.HORIZONTAL | SWT.RIGHT
				| SWT.FLAT);
		// add toolbar item
		addItem = new ToolItem(toolBar, SWT.PUSH);
		addItem.setImage(IMG_ADD);
		addItem.setToolTipText(ADD_STR);
		addItem.addSelectionListener(getSelectionListener());
		// delete toolbar item
		deleteItem = new ToolItem(toolBar, SWT.PUSH);
		deleteItem.setImage(IMG_DEL);
		deleteItem.setToolTipText(DEL_STR);
		deleteItem.addSelectionListener(getSelectionListener());
		// edit toolbar item
		editItem = new ToolItem(toolBar, SWT.PUSH);
		editItem.setImage(IMG_EDIT);
		editItem.setToolTipText(EDIT_STR);
		editItem.addSelectionListener(getSelectionListener());
		// moveup toolbar item
		moveUpItem = new ToolItem(toolBar, SWT.PUSH);
		moveUpItem.setImage(IMG_MOVEUP);
		moveUpItem.setToolTipText(MOVEUP_STR);
		moveUpItem.addSelectionListener(getSelectionListener());
		// movedown toolbar item
		moveDownItem = new ToolItem(toolBar, SWT.PUSH);
		moveDownItem.setImage(IMG_MOVEDOWN);
		moveDownItem.setToolTipText(MOVEDOWN_STR);
		moveDownItem.addSelectionListener(getSelectionListener());
		grid3 = new GridData(GridData.FILL_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_END);
		buttonPanel.setLayoutData(grid3);
		// list control
		list = new List(filePanel, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		grid4 = new GridData(GridData.FILL_BOTH);
		// force the list to be no wider than the title bar
		Point preferredSize = titlePanel.computeSize(SWT.DEFAULT, SWT.DEFAULT); 
		grid4.widthHint = preferredSize.x;
		grid4.heightHint = preferredSize.y * 3;
		grid4.horizontalSpan = 2;
		list.setLayoutData(grid4);
		list.addSelectionListener(getSelectionListener());
		//Add a double-click event handler
		list.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				// Popup the editor on the selected item from the list
				editSelection();
			}
		});
		// Add a delete event handler
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

		selectionChanged();
	}
	/**
	 * Set list values
	 * 
	 * @param listVal
	 */
	public void setList(String[] listVal) {
		if (list != null) {
			list.removeAll();
		}
		for (int i = 0; i < listVal.length; i++) {
			list.add(listVal[i]);
		}
		checkNotificationNeeded();
	}
	
	public void addChangeListener(IFileListChangeListener listener){
		listeners.add(listener);
	}
	
	public void removeChangeListener(IFileListChangeListener listener){
		listeners.remove(listener);
	}

	public void checkNotificationNeeded(){
		String items[] = getItems();
		if(oldValue != null){
			if(oldValue.length == items.length){
				int i;
				for(i = 0; i < oldValue.length; i++){
					if(!oldValue[i].equals(items[i]))
						break;
				}
				if(i == oldValue.length)
					return;
			}
			String old[] = oldValue;
			System.arraycopy(items,0,oldValue = new String[items.length],0,items.length);
			notifyListeners(old,oldValue);
		} else{
			System.arraycopy(items,0,oldValue = new String[items.length],0,items.length);
		}
	}
	
	public void notifyListeners(String oldVal[], String newVal[]){
		Iterator iter = listeners.iterator();
		while(iter.hasNext()){
			((IFileListChangeListener)iter.next()).fileListChanged(this,oldVal,newVal);
		}
	}
	
	/**
	 * Set selection
	 * 
	 * @param sel
	 */
	public void setSelection(int sel) {
		if (list.getItemCount() > 0)
			list.setSelection(sel);
		selectionChanged();
	}
	/**
	 * Set default selection
	 */
	public void setSelection() {
		if (list.getItemCount() > 0)
			list.setSelection(0);
	}
	/**
	 * removes all items from list control
	 */
	public void removeAll() {
		if (list != null){
			list.removeAll();
			checkNotificationNeeded();
		}
	}
	/**
	 * get list items
	 * 
	 * @return
	 */
	public String[] getItems() {
		return list.getItems();
	}
	/**
	 * Create selection listener for buttons
	 */
	private void createSelectionListener() {
		selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Widget widget = event.widget;
				if (widget == addItem) {
					addPressed();
				} else if (widget == deleteItem) {
					removePressed();
				} else if (widget == moveUpItem) {
					upPressed();
				} else if (widget == moveDownItem) {
					downPressed();
				} else if (widget == list) {
					selectionChanged();
				} else if (widget == editItem) {
					editSelection();
				}
			}
		};
	}
	/**
	 * Returns selection listener
	 * 
	 * @return
	 */
	private SelectionListener getSelectionListener() {
		if (selectionListener == null)
			createSelectionListener();
		return selectionListener;
	}
	/**
	 * This method will be called when the add button is pressed
	 */
	private void addPressed() {
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
			checkNotificationNeeded();
		}

		selectionChanged();
	}
	/**
	 * This method will be called when the remove button is pressed
	 */
	private void removePressed() {
		int index = list.getSelectionIndex();
		if (browseType == IOption.BROWSE_DIR || browseType == IOption.BROWSE_FILE) {
			String quest = ManagedBuilderUIMessages.getResourceString("FileListControl.deletedialog.message"); //$NON-NLS-1$
			String title = ManagedBuilderUIMessages.getResourceString("FileListControl.deletedialog.title"); //$NON-NLS-1$
			boolean delDir = MessageDialog.openQuestion(list.getShell(), title,
					quest);
			if (delDir && index != -1){
				list.remove(index);
				checkNotificationNeeded();
			}
		} else if (index != -1){
			list.remove(index);
			checkNotificationNeeded();
		}
		selectionChanged();
	}
	/**
	 * This method will be called when the move up button is pressed
	 */
	private void upPressed() {
		int index = list.getSelectionIndex();
		String curSelList = list.getItem(index);
		String preList = list.getItem(index - 1);
		list.setItem(index - 1, curSelList);
		list.setItem(index, preList);
		list.setSelection(index - 1);
		checkNotificationNeeded();
		selectionChanged();
	}
	/**
	 * This method will be called when the move down button is pressed
	 */
	private void downPressed() {
		int index = list.getSelectionIndex();
		String curSelList = list.getItem(index);
		String nextList = list.getItem(index + 1);
		list.setItem(index + 1, curSelList);
		list.setItem(index, nextList);
		list.setSelection(index + 1);
		checkNotificationNeeded();
		selectionChanged();
	}
	/**
	 * This method will be called when the edit button is pressed
	 */
	private void editSelection() {
		int index = list.getSelectionIndex();
		if (index != -1) {
			String selItem = list.getItem(index);
			String title = ManagedBuilderUIMessages.getResourceString("FileListControl.editdialog.title"); //$NON-NLS-1$
			if (selItem != null) {
				InputDialog dialog = new InputDialog(null, title, compTitle,
						selItem, null);
				String newItem = null;
				if (dialog.open() == InputDialog.OK) {
					newItem = dialog.getValue();
					if (newItem != null && !newItem.equals(selItem)) {
						list.setItem(index, newItem);
						checkNotificationNeeded();
						selectionChanged();
					}
				}
			}
		}
	}
	/**
	 * This method will be called when the list selection changed
	 */
	public void selectionChanged() {
		int index = list.getSelectionIndex();
		int size = list.getItemCount();
		deleteItem.setEnabled(size > 0);
		moveUpItem.setEnabled(size > 1 && index > 0);
		moveDownItem.setEnabled(size > 1 && index >= 0 && index < size - 1);
		editItem.setEnabled(size > 0);
	}
	/**
	 * Returns List control
	 * 
	 * @return
	 */
	public List getListControl() {
		return list;
	}

	/**
	 * Sets the IPath of the project the field editor was
	 * created for.
	 * 
	 * @param path The path to the 
	 */
	public void setPath(IPath path) {
		this.path = path;
	}
	
	/**
	 * Set browseType
	 */
	public void setType(int type) {
		browseType = type;
	}

	/**
	 * Returns the input dialog string
	 * 
	 * @return
	 */
	private String getNewInputObject() {
		// Create a dialog to prompt for a new list item
		String input = null;
		String title = new String();
		String message = new String();
		String initVal = new String();
		
		if (browseType == IOption.BROWSE_DIR) {
			title = DIR_TITLE;
			message = DIR_MSG;
			initVal = path == null ? initVal : path.toString();
		} else if (browseType == IOption.BROWSE_FILE) {
			title = FILE_TITLE;
			message = FILE_MSG;
			initVal = path == null ? initVal : path.toString();
		} else {
			title = TITLE;
			message = compTitle;
		}
		
		// Prompt for value
		SelectPathInputDialog dialog = new SelectPathInputDialog(getListControl().getShell(), title, message, initVal, null, browseType);
		if (dialog.open() == SelectPathInputDialog.OK) {
			input = dialog.getValue();
		}
		
		// Double-quote the spaces or backslashes in paths (if any)
		if (input != null && input.length() > 0) {
			if (browseType == IOption.BROWSE_DIR ||
					browseType == IOption.BROWSE_FILE) {
				// Check for spaces 
				int firstWhitespace = input.indexOf(" ");	//$NON-NLS-1$
				int firstBackslash = input.indexOf("\\");	//$NON-NLS-1$
				if (firstWhitespace != -1 || firstBackslash != -1) {
					// Double-quote paths with whitespaces
					input = "\"" + input + "\"";	//$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}

		return input;
	}
	
	public Label getLabelControl(){
		return title;
	}
	
	public void setEnabled(boolean enabled){
		title.setEnabled(enabled);
		toolBar.setEnabled(enabled);
		list.setEnabled(enabled);
	}
}

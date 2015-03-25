/*******************************************************************************
 * Copyright (c) 2010-2013 Nokia Siemens Networks Oyj, Finland.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Nokia Siemens Networks - initial implementation
 *      Petri Tuononen - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.llvm.ui.preferences;

import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.cdt.managedbuilder.llvm.util.Separators;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * An abstract list editor that manages a list of input values.
 * The editor displays a list containing the values, buttons for adding and removing
 * values, and Up and Down buttons to adjust the order of elements in the list.
 * 
 */
public abstract class LlvmListEditor extends ListEditor {

    /**
     * The list widget; <code>null</code> if none
     * (before creation or after disposal).
     */
    List list;

    /**
     * The button box containing the Add, Remove, Up, and Down buttons;
     * <code>null</code> if none (before creation or after disposal).
     */
    Composite buttonBox;

    /**
     * The Add button.
     */
    Button addButton;

    /**
     * The Remove button.
     */
    Button removeButton;

    /**
     * The Up button.
     */
    Button upButton;

    /**
     * The Down button.
     */
    Button downButton;

    /**
	 * The selection listener.
	 */
    private SelectionListener selectionListener;

    /**
     * Creates a list field editor.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     */
    protected LlvmListEditor(String name, String labelText, Composite parent) {
    	super(name, labelText, parent);
    }
    
	@Override
	/**
	 * Combines the given list of items into a single String.
	 * This method is the converse of parseString. 
	 */
	protected String createList(String[] items) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String item : items) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(Separators.getPathSeparator());
			}
			stringBuilder.append(item);
		}
		return stringBuilder.toString();
	}

	@Override
	/** Splits the given String into a list of Strings.
	 * This method is the converse of createList.
	 */
	protected String[] parseString(String stringList) {
		if (stringList != null && stringList.length() > 0) {
			return stringList.split(Pattern.quote(Separators.getPathSeparator()));
		}
		return new String[0];
	}

    /**
     * Creates the Add, Remove, Up, and Down button in the given button box.
     *
     * @param box the box for the buttons
     */
    private void createButtons(Composite box) {
        this.addButton = createPushButton(box, "ListEditor.add");//$NON-NLS-1$
        this.removeButton = createPushButton(box, "ListEditor.remove");//$NON-NLS-1$
        this.upButton = createPushButton(box, "ListEditor.up");//$NON-NLS-1$
        this.downButton = createPushButton(box, "ListEditor.down");//$NON-NLS-1$
    }

    /**
     * Helper method to create a push button.
     * 
     * @param parent the parent control
     * @param key the resource name used to supply the button's label text
     * @return Button
     */
    private Button createPushButton(Composite parent, String key) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(JFaceResources.getString(key));
        button.setFont(parent.getFont());
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        int widthHint = convertHorizontalDLUsToPixels(button,
                IDialogConstants.BUTTON_WIDTH);
        data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT,
                SWT.DEFAULT, true).x);
        button.setLayoutData(data);
        button.addSelectionListener(getSelectionListener());
        return button;
    }

    /**
     * Creates a selection listener.
     */
    @Override
	public void createSelectionListener() {
        this.selectionListener = new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent event) {
                Widget widget = event.widget;
                if (widget == LlvmListEditor.this.addButton) {
                    addPressed();
                } else if (widget == LlvmListEditor.this.removeButton) {
                    removePressed();
                } else if (widget == LlvmListEditor.this.upButton) {
                    upPressed();
                } else if (widget == LlvmListEditor.this.downButton) {
                    downPressed();
                } else if (widget == LlvmListEditor.this.list) {
                    selectionChanged();
                }
            }
        };
    }

    /**
     * Returns this field editor's button box containing the Add, Remove,
     * Up, and Down button.
     *
     * @param parent the parent control
     * @return the button box
     */
    @Override
	public Composite getButtonBoxControl(Composite parent) {
        if (this.buttonBox == null) {
            this.buttonBox = new Composite(parent, SWT.NULL);
            GridLayout layout = new GridLayout();
            layout.marginWidth = 0;
            this.buttonBox.setLayout(layout);
            createButtons(this.buttonBox);
            this.buttonBox.addDisposeListener(new DisposeListener() {
                @Override
		public void widgetDisposed(DisposeEvent event) {
                    LlvmListEditor.this.addButton = null;
                    LlvmListEditor.this.removeButton = null;
                    LlvmListEditor.this.upButton = null;
                    LlvmListEditor.this.downButton = null;
                    LlvmListEditor.this.buttonBox = null;
                }
            });

        } else {
            checkParent(this.buttonBox, parent);
        }

        selectionChanged();
        return this.buttonBox;
    }

    /**
     * Returns this field editor's list control.
     *
     * @param parent the parent control
     * @return the list control
     */
    @Override
	public List getListControl(Composite parent) {
        if (this.list == null) {
            this.list = new List(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL
                    | SWT.H_SCROLL);
            this.list.setFont(parent.getFont());
            this.list.addSelectionListener(getSelectionListener());
            this.list.addDisposeListener(new DisposeListener() {
                @Override
		public void widgetDisposed(DisposeEvent event) {
                    LlvmListEditor.this.list = null;
                }
            });
        } else {
            checkParent(this.list, parent);
        }
        return this.list;
    }

    /**
	 * Returns this field editor's selection listener. The listener is created if nessessary.
	 * @return  the selection listener
	 */
    private SelectionListener getSelectionListener() {
        if (this.selectionListener == null) {
			createSelectionListener();
		}
        return this.selectionListener;
    }

	/**
	 * Invoked when the selection in the list has changed.
	 * 
	 * <p>
	 * The default implementation of this method utilizes the selection index
	 * and the size of the list to toggle the enablement of the up, down and
	 * remove buttons.
	 * </p>
	 * 
	 */
    @Override
	protected void selectionChanged() {
        int index = this.list.getSelectionIndex();
        int size = this.list.getItemCount();

        this.removeButton.setEnabled(index >= 0);
        this.upButton.setEnabled(size > 1 && index > 0);
        this.downButton.setEnabled(size > 1 && index >= 0 && index < size - 1);
    }

    /**
     * Moves the currently selected item up or down.
     *
     * @param up <code>true</code> if the item should move up,
     *  and <code>false</code> if it should move down
     */
    private void swap(boolean up) {
        setPresentsDefaultValue(false);
        int index = this.list.getSelectionIndex();
        int target = up ? index - 1 : index + 1;

        if (index >= 0) {
            String[] selection = this.list.getSelection();
            Assert.isTrue(selection.length == 1);
            this.list.remove(index);
            this.list.add(selection[0], target);
            this.list.setSelection(target);
        }
        selectionChanged();
    }

    /**
     * Returns this field editor's shell.
     * <p>
     * This method is internal to the framework; subclasses should not call
     * this method.
     * </p>
     *
     * @return the shell
     */
    @Override
	protected Shell getShell() {
        if (this.addButton == null) {
			return null;
		}
        return this.addButton.getShell();
    }
    
    /**
     * Notifies that the Add button has been pressed.
     */
    void addPressed() {
        setPresentsDefaultValue(false);
        String input = getNewInputObject();

        if (input != null) {
            int index = this.list.getSelectionIndex();
            if (index >= 0) {
				this.list.add(input, index + 1);
			} else {
				this.list.add(input, 0);
			}
            selectionChanged();
        }
    }
    
    /**
     * Notifies that the Remove button has been pressed.
     */
    protected abstract void removePressed();
    
    /**
     * Notifies that the Up button has been pressed.
     */
    void upPressed() {
        swap(true);
    }

    /**
     * Notifies that the Down button has been pressed.
     */
    void downPressed() {
        swap(false);
    }
	
}

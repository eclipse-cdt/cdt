/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * This is an entry which allows the user to decide whether to
 * inherit a parent value or type in his own local value.
 * <p>
 * To accomplish this, we create a composite containing a toggle button
 * followed by an entry field.
 * <p>
 * The toggle button has left and right arrows. 
 * Typically, an arrow pointing to the left means to inherit from parent,
 * and pointing to the right means to override locally. 
 * However, the control can be used for any binary decision!
 * <p>
 * Although this control inherits from Composite it does not make sense to 
 * set a layout for it or to add children to it.
 */
/*
 * dwd: modified for defect 57974 (accessibility problems)
 * Formatted source and organized imports.
 * Removed all references to InheritControl.
 * Changed button from SWT.ARROW to SWT.PUSH.  SWT.ARROW buttons are not accessible.
 * Simplified internal call structure complicated by case handling for InheritControl.
 */
public class InheritableEntryField extends Composite implements KeyListener {
	private InheritButton toggleButton = null;
	private Text entryField = null;
	private String inheritValue = "";
	private String localValue = "";
	private boolean isLocal = true;
	private boolean allowEditOfInherited = false;

	/**
	 * Constructor
	 * @param parent The parent composite to hold this widget
	 * @param style the SWT style for this widget (eg, SWT.BORDER or SWT.NULL)
	 */
	public InheritableEntryField(Composite parent, int style) {
		this(parent, style, SWT.NULL, SWT.SINGLE | SWT.BORDER, true);
	}

	/**
	 * Constructor when you want to set the style of the toggle button and entry field too.
	 * @param parent The parent composite to hold this widget
	 * @param style the SWT style for this overall widget (eg, SWT.BORDER or SWT.NULL)
	 * @param style the SWT style for the toggle button widget
	 * @param style the SWT style for the entry field widget
	 */
	public InheritableEntryField(Composite parent, int style, int buttonStyle, int textStyle) {
		this(parent, style, buttonStyle, textStyle, true);
	}

	/**
	 * Constructor when you want to hide the toggle button
	 * @param parent The parent composite to hold this widget
	 * @param style the SWT style for this overall widget (eg, SWT.BORDER or SWT.NULL)
	 * @param style the SWT style for the toggle button widget
	 * @param style the SWT style for the entry field widget
	 * @param showToggleButton true to show the toggle button, false not to
	 */
	public InheritableEntryField(Composite parent, int style, int buttonStyle, int textStyle, boolean showToggleButton) {
		super(parent, style);
		prepareComposite(2);
		if (showToggleButton) {
			createToggleButton(this, buttonStyle);
		}
		createTextField(this, textStyle);
		setLocal(true); // default state
	}

	/**
	 * Toggle the inherit/local state.
	 * It is important that you have already called setLocalText and setInheritedText
	 */
	public void setLocal(boolean local) {
		boolean wasLocal = isLocal;
		isLocal = local;
		if (isLocal) { // from inherit to local
			if (allowEditOfInherited && !wasLocal) inheritValue = entryField.getText();
			entryField.setEnabled(true);
			entryField.setText(localValue);
		} else { // from local to inherit
			if (wasLocal) // if this is actually a toggle
				localValue = entryField.getText(); // remember what old local value was
			entryField.setText(inheritValue);
			entryField.setEnabled(allowEditOfInherited);
		}
		if (toggleButton != null) {
			toggleButton.setLocal(isLocal);
		}
	}

	/**
	 * Query the inherit/local state
	 */
	public boolean isLocal() {
		return isLocal;
	}

	/**
	 * Specify if user is allowed to edit the inherited text. Default is false.
	 */
	public void setAllowEditingOfInheritedText(boolean allow) {
		allowEditOfInherited = allow;
	}

	/**
	 * Set the entry field's inherited text value
	 */
	public void setInheritedText(String text) {
		if (text == null) text = "";
		this.inheritValue = text;
	}

	/**
	 * Query the entry field's inherited text value. 
	 * If widget is in inherit mode, returns entry field contents, else returns cached value
	 */
	public String getInheritedText() {
		if (!isLocal)
			return entryField.getText();
		else
			return inheritValue;
	}

	/**
	 * Set the entry field's local text value
	 */
	public void setLocalText(String text) {
		if (text == null) text = "";
		this.localValue = text;
	}

	/**
	 * Query the entry field's local text value.
	 * If widget is in local mode, returns entry field contents, else returns "".
	 */
	public String getLocalText() {
		if (isLocal)
			return entryField.getText();
		else
			return "";
	}

	/**
	 * Query the entry field's current contents, regardless of local/inherit state
	 */
	public String getText() {
		return entryField.getText();
	}

	/**
	 * Return a reference to the entry field
	 */
	public Text getTextField() {
		return entryField;
	}

	/**
	 * Return the toggle button
	 */
	public InheritButton getToggleButton() {
		return toggleButton;
	}

	/**
	 * Disable the toggle. Used when there is no inherited value
	 */
	public void setToggleEnabled(boolean enabled) {
		if (toggleButton == null) return;
		toggleButton.setEnabled(enabled);
	}

	/**
	 * Set the tooltip text for the toggle button
	 */
	public void setToggleToolTipText(String tip) {
		if (toggleButton == null) return;
		toggleButton.setToolTipText(tip);
	}

	/**
	 * Set the tooltip text for the entry field
	 */
	public void setTextFieldToolTipText(String tip) {
		entryField.setToolTipText(tip);
	}

	/**
	 * Set the entry field's text limit
	 */
	public void setTextLimit(int limit) {
		entryField.setTextLimit(limit);
	}

	/**
	 * Set the focus to the toggle button
	 */
	public void setToggleButtonFocus() {
		if (toggleButton == null) return;
		toggleButton.setFocus();
	}

	/**
	 * Set the focus to the entry field
	 */
	public void setTextFieldFocus() {
		entryField.setFocus();
	}

	/**
	 * Register a listener interested in when the button is toggled
	 * <p>
	 * Call {@link #isLocal()} to determine if left (false) or right (true) was pressed.
	 * @see #removeSelectionListener(SelectionListener)
	 */
	public void addSelectionListener(SelectionListener listener) {
		if (toggleButton == null) return;
		toggleButton.addSelectionListener(listener);
	}

	/** 
	 * Remove a previously set toggle button selection listener.
	 * @see #addSelectionListener(SelectionListener)
	 */
	public void removeSelectionListener(SelectionListener listener) {
		if (toggleButton == null) return;
		toggleButton.removeSelectionListener(listener);
	}

	/**
	 * Register a listener interested in entry field modify events
	 * <p>
	 * @see #removeModifyListener(ModifyListener)
	 */
	public void addModifyListener(ModifyListener listener) {
		entryField.addModifyListener(listener);
	}

	/** 
	 * Remove a previously set entry field listener.
	 * @see #addModifyListener(ModifyListener)
	 */
	public void removeModifyListener(ModifyListener listener) {
		entryField.removeModifyListener(listener);
	}

	// -----------------------
	// INTERNAL-USE METHODS...
	// -----------------------
	/**
	 * Prepares this composite control and sets the default layout data.
	 * @param Number of columns the new group will contain.     
	 */
	protected Composite prepareComposite(int numColumns) {
		Composite composite = this;
		//GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		//GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		return composite;
	}

	/**
	 * Create our text field and insert it into a GridLayout.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param GridLayout composite to put the field into.
	 */
	protected void createTextField(Composite parent, int textStyle) {
		entryField = new Text(parent, textStyle);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.widthHint = 150;
		data.verticalAlignment = GridData.CENTER;
		data.grabExcessVerticalSpace = false;
		entryField.setLayoutData(data);
		entryField.addKeyListener(this);
	}

	protected void createToggleButton(Composite parent, int buttonStyle) {
		toggleButton = new InheritButton(parent);
		toggleButton.addKeyListener(this);
		toggleButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setLocal(!isLocal());
			}
		});
	}

	public void setToggleButtonHeight(int height) {
		if (toggleButton == null) return;
		((GridData) toggleButton.getLayoutData()).heightHint = height;
		((GridData) toggleButton.getLayoutData()).grabExcessVerticalSpace = false;
		((GridData) toggleButton.getLayoutData()).verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		if ((e.stateMask == SWT.CTRL) && (e.keyCode == SWT.ARROW_LEFT) && isLocal()) {
			setLocal(false);
		} else if ((e.stateMask == SWT.CTRL) && (e.keyCode == SWT.ARROW_RIGHT) && !isLocal()) {
			setLocal(true);
		}
	}
}
/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.view;
import java.text.MessageFormat;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.rse.ui.widgets.InheritableEntryField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;


/**
 * A cell editor that manages an inheritable text entry field.
 * The cell editor's value is the text string itself.
 */
public class SystemInheritableTextCellEditor 
       //extends DialogCellEditor
       extends CellEditor
       implements SelectionListener
{
	protected InheritableEntryField textField;
	protected Text text;
	protected SystemInheritablePropertyData data;	
	private String toggleButtonToolTipText, entryFieldToolTipText;	

	private ModifyListener modifyListener;

	/**
	 * State information for updating action enablement
	 */
	private boolean isSelection = false;
	private boolean isDeleteable = false;
	private boolean isSelectable = false;

	/**
	 * Creates a new text string cell editor parented under the given control.
	 * The cell editor value is the string itself, which is initially the empty string. 
	 * Initially, the cell editor has no cell validator.
	 *
	 * @param parent the parent control
	 */
	public SystemInheritableTextCellEditor(Composite parent) 
	{
		super(parent);
	}
	/**
	 * Checks to see if the "deleteable" state (can delete/
	 * nothing to delete) has changed and if so fire an
	 * enablement changed notification.
	 */
	private void checkDeleteable() 
	{
		boolean oldIsDeleteable = isDeleteable;
		isDeleteable = isDeleteEnabled();
		if (oldIsDeleteable != isDeleteable) 
		{
			fireEnablementChanged(DELETE);
		}
	}
	
	
	/**
	 * Checks to see if the "selectable" state (can select)
	 * has changed and if so fire an enablement changed notification.
	 */
	private void checkSelectable() 
	{		
		boolean oldIsSelectable = isSelectable;
		isSelectable = isSelectAllEnabled();
		if (oldIsSelectable != isSelectable) 
		{
			fireEnablementChanged(SELECT_ALL);
		}
	}
	/**
	 * Checks to see if the selection state (selection /
	 * no selection) has changed and if so fire an
	 * enablement changed notification.
	 */
	private void checkSelection() 
	{
		boolean oldIsSelection = isSelection;
		isSelection = getTextField().getSelectionCount() > 0;
		if (oldIsSelection != isSelection) 
		{
			fireEnablementChanged(COPY);
			fireEnablementChanged(CUT);
		}
	}
	/**
	 * Return the entry field of the composite control
	 */
	private Text getTextField()
	{
		return textField.getTextField();
	}
	
	public InheritableEntryField getInheritableEntryField()
	{
		return textField;
	}
	
	/**
	 * Gets the toggleButtonToolTipText
	 * @return Returns a String
	 */
	public String getToggleButtonToolTipText() 
	{
		return toggleButtonToolTipText;
	}
	/**
	 * Sets the toggleButtonToolTipText
	 * @param toggleButtonToolTipText The toggleButtonToolTipText to set
	 */
	public void setToggleButtonToolTipText(String toggleButtonToolTipText)
	{
		this.toggleButtonToolTipText = toggleButtonToolTipText;
		if (textField != null)
		  textField.setToggleToolTipText(toggleButtonToolTipText);
	}

	/**
	 * Gets the entryFieldToolTipText
	 * @return Returns a String
	 */
	public String getEntryFieldToolTipText() 
	{
		return entryFieldToolTipText;
	}
	/**
	 * Sets the entryFieldToolTipText
	 * @param entryFieldToolTipText The entryFieldToolTipText to set
	 */
	public void setEntryFieldToolTipText(String entryFieldToolTipText)
	{
		this.entryFieldToolTipText = entryFieldToolTipText;
		if (textField != null)
		  textField.setTextFieldToolTipText(entryFieldToolTipText);
	}
	
	/* (non-Javadoc)
	 * Method declared on CellEditor.
	 */
	protected Control createControl(Composite parent) 
	{
		// specify no borders on text widget as cell outline in
		// table already provides the look of a border.
		textField = new InheritableEntryField(parent, SWT.NULL, SWT.BORDER, SWT.SINGLE);		
		textField.setToggleButtonHeight(14);
		textField.setBackground(parent.getBackground());	
		textField.addSelectionListener(this);
		if (toggleButtonToolTipText != null)
		  textField.setToggleToolTipText(toggleButtonToolTipText);			
		if (entryFieldToolTipText != null)
		  textField.setTextFieldToolTipText(entryFieldToolTipText);		  
		text = getTextField();
		text.addKeyListener(new KeyAdapter() 
		{
			public void keyPressed(KeyEvent e) 
			{
				// The call to inherited keyReleaseOccurred is what causes the apply
				//  event if the key pressed is Enter.
				keyReleaseOccured(e);
				// as a result of processing the above call, clients may have
				// disposed this cell editor
				if ((getControl() == null) || getControl().isDisposed())
					return;
				checkSelection(); // see explaination below
				checkDeleteable();
				checkSelectable();
			}
		});
		text.addTraverseListener(new TraverseListener() 
		{
			public void keyTraversed(TraverseEvent e) 
			{
				if (e.detail == SWT.TRAVERSE_ESCAPE || e.detail == SWT.TRAVERSE_RETURN) 
				{
					e.doit = false;
				}
			}
		});
		// We really want a selection listener but it is not supported so we
		// use a key listener and a mouse listener to know when selection changes
		// may have occured
		text.addMouseListener(new MouseAdapter() 
		{
			public void mouseUp(MouseEvent e) {
				checkSelection();
				checkDeleteable();
				checkSelectable();
			}
		});
		text.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
			}
			
			public void focusLost(FocusEvent e) {
					SystemInheritableTextCellEditor.this.focusLost();
			}
		});
		textField.getToggleButton().addFocusListener(new FocusAdapter() {
					public void focusLost(FocusEvent e) {
							SystemInheritableTextCellEditor.this.focusLost();
					}
					
					public void focusGained(FocusEvent e) {
					}
				});
		
		text.setFont(parent.getFont());
		//text.setBackground(parent.getBackground());		
		text.setText("");//$NON-NLS-1$
		text.addModifyListener(getModifyListener());
		setValueValid(true);
		return textField;
	}
	
	protected void focusLost() 
	{
		super.focusLost();	
	}
	
	/**
	 * Return current data.
	 * 
	 * @return the SystemInheritablePropertyData data object
	 */
	protected Object doGetValue() 
	{
        SystemInheritablePropertyData outputData = new SystemInheritablePropertyData();
		outputData.setIsLocal(textField.isLocal());
		outputData.setLocalValue(textField.getLocalText());
		outputData.setInheritedValue(textField.getInheritedText());
		return outputData;
	}


	/* (non-Javadoc)
	 * Method declared on CellEditor.
	 */
	protected void doSetFocus() 
	{
		if (text != null) 
		{
		  if (text.isEnabled())
		  {
			text.selectAll();
			text.setFocus();
		  }
		  else
		  {
		    textField.setToggleButtonFocus();
		  }  
		  
	      checkSelection();
	      checkDeleteable();
		  checkSelectable();		  
		}
	}
	/**
	 * The <code>TextCellEditor</code> implementation of
	 * this <code>CellEditor</code> framework method accepts
	 * a SystemInheritablePropertyData data object.
	 *
	 * @param value a SystemInheritablePropertyData object
	 */
	protected void doSetValue(Object value) 
	{
		Assert.isTrue(text != null && (value instanceof SystemInheritablePropertyData));
		textField.removeModifyListener(getModifyListener());
		data = (SystemInheritablePropertyData)value;
		textField.setLocalText(data.getLocalValue());
		textField.setInheritedText(data.getInheritedValue());
		textField.setLocal(data.getIsLocal());
		textField.addModifyListener(getModifyListener());
	}
	/**
	 * Processes a modify event that occurred in this text cell editor.
	 * This framework method performs validation and sets the error message
	 * accordingly, and then reports a change via <code>fireEditorValueChanged</code>.
	 * Subclasses should call this method at appropriate times. Subclasses
	 * may extend or reimplement.
	 *
	 * @param e the SWT modify event
	 */
	protected void editOccured(ModifyEvent e) 
	{
		String value = text.getText();
		if (value == null)
			value = "";
		Object typedValue = value;
		boolean oldValidState = isValueValid();
		boolean newValidState = isCorrect(typedValue);
		if (!newValidState) 
		{
			// try to insert the current value into the error message.
			setErrorMessage(MessageFormat.format(getErrorMessage(), new Object[] {value}));
		}
		valueChanged(oldValidState, newValidState);
	}
	/**
	 * Since a text editor field is scrollable we don't
	 * set a minimumSize.
	 */
	public LayoutData getLayoutData() 
	{
		return new LayoutData();
	}
	/**
	 * Return the modify listener.
	 */
	private ModifyListener getModifyListener() 
	{
		if (modifyListener == null) 
		{
			modifyListener = new ModifyListener() {
				public void modifyText(ModifyEvent e) 
				{
					editOccured(e);
				}
			};
		}
		return modifyListener;
	}
	/**
	 * The <code>TextCellEditor</code>  implementation of this 
	 * <code>CellEditor</code> method returns <code>true</code> if 
	 * the current selection is not empty.
	 */
	public boolean isCopyEnabled() 
	{
		if (text == null || text.isDisposed() || !text.isEnabled())
			return false;
		return text.getSelectionCount() > 0;
	}
	/**
	 * The <code>TextCellEditor</code>  implementation of this 
	 * <code>CellEditor</code> method returns <code>true</code> if 
	 * the current selection is not empty.
	 */
	public boolean isCutEnabled() 
	{
		if (text == null || text.isDisposed() || !text.isEnabled())
			return false;
		return text.getSelectionCount() > 0;
	}
	/**
	 * The <code>TextCellEditor</code>  implementation of this 
	 * <code>CellEditor</code> method returns <code>true</code>
	 * if there is a selection or if the caret is not positioned 
	 * at the end of the text.
	 */
	public boolean isDeleteEnabled() 
	{
		if (text == null || text.isDisposed() || !text.isEnabled())
			return false;
		return text.getSelectionCount() > 0 || text.getCaretPosition() < text.getCharCount();
	}
	/**
	 * The <code>TextCellEditor</code>  implementation of this 
	 * <code>CellEditor</code> method always returns <code>true</code>.
	 */
	public boolean isPasteEnabled() 
	{
		if (text == null || text.isDisposed() || !text.isEnabled())
			return false;
		return true;
	}
	/**
	 * The <code>TextCellEditor</code>  implementation of this 
	 * <code>CellEditor</code> method always returns <code>true</code>.
	 */
	public boolean isSaveAllEnabled() 
	{
		if (text == null || text.isDisposed() || !text.isEnabled())
			return false;
		return true;
	}
	/**
	 * Returns <code>true</code> if this cell editor is
	 * able to perform the select all action.
	 * <p>
	 * This default implementation always returns 
	 * <code>false</code>.
	 * </p>
	 * <p>
	 * Subclasses may override
	 * </p>
	 * @return <code>true</code> if select all is possible,
	 *  <code>false</code> otherwise
	 */
	public boolean isSelectAllEnabled() 
	{
		if (text == null || text.isDisposed() || !text.isEnabled())
			return false;
		return text.getText().length() > 0;
	}
	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method copies the
	 * current selection to the clipboard. 
	 */
	public void performCopy() 
	{
		text.copy();
	}
	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method cuts the
	 * current selection to the clipboard. 
	 */
	public void performCut() 
	{
		text.cut();
		checkSelection(); 
		checkDeleteable();
		checkSelectable();
	}
	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method deletes the
	 * current selection or, if there is no selection,
	 * the character next character from the current position. 
	 */
	public void performDelete() 
	{
		if (text.getSelectionCount() > 0)
			// remove the contents of the current selection
			text.insert("");
		else 
		{
			// remove the next character
			int pos = text.getCaretPosition();
			if (pos < text.getCharCount()) 
			{
				text.setSelection(pos, pos + 1);
				text.insert("");
			}
		}
		checkSelection(); 
		checkDeleteable();
		checkSelectable();
	}
	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method pastes the
	 * the clipboard contents over the current selection. 
	 */
	public void performPaste() 
	{
		text.paste();
		checkSelection(); 
		checkDeleteable();
		checkSelectable();
	}
	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method selects all of the
	 * current text. 
	 */
	public void performSelectAll() 
	{
		text.selectAll();
		checkSelection(); 
		checkDeleteable();
	}	
	
	// Selection Listener methods for InheritableTextCellEditor toggle switches
	public void widgetDefaultSelected(SelectionEvent e)
	{
	}
	public void widgetSelected(SelectionEvent e)
	{
		//System.out.println("Got widget selected event. isLocal() = " + textField.isLocal()+", text='"+textField.getText()+"'");
		boolean isLocal = textField.isLocal();
		String value = text.getText();
		data.setIsLocal(isLocal);
		boolean oldValidState = isValueValid();
		boolean newValidState = isLocal?isCorrect(value):true; //isCorrect(typedValue);
		if (!newValidState) 
		{
			// try to insert the current value into the error message.
			setErrorMessage(MessageFormat.format(getErrorMessage(), new Object[] {value}));
		}
		valueChanged(oldValidState, newValidState);
	}
}
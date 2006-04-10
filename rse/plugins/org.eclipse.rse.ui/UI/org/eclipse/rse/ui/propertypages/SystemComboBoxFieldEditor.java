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

package org.eclipse.rse.ui.propertypages;
import java.util.ResourceBundle;
import java.util.Vector;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.rse.ui.ISystemMassager;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;


/**
 * For string properties that have a discrete list of possibilities.
 */
public class SystemComboBoxFieldEditor extends FieldEditor
{
	
    private Combo textField;
    private String[] contentArray;
    private boolean contentInited = false;
    private boolean readOnly = true;
    private boolean isValid = true;
    private String tip;
    private SelectionListener selectionListener = null;    
    private ModifyListener modifyListener = null;    
    private boolean ignoreSelection = false;
    private ISystemValidator validator = null;
    private ISystemMassager  massager = null;
    private Composite parentComposite;
    private String oldValue;
    private int numColumnsInParentComposite;
    
    
	/**
	 * Constructor for SystemComboBoxFieldEditor
	 */
	private SystemComboBoxFieldEditor() 
	{
		super();
	}

	/**
	 * Constructor for SystemComboBoxFieldEditor, using a Vector for the contents
	 * @param name - the unique ID for this editor. Used as index in preference store
	 * @param labelText - the label to show as the prompt preceding the dropdown
	 * @param contents - the list of strings to show in the dropdown, as a vector
	 * @param readOnly - true if the user is to be prevented from entering text into the combo
	 * @param parent - the parent composite to host this editor
	 */
	public SystemComboBoxFieldEditor(String name, String labelText, Vector contents, boolean readOnly, Composite parent) 
	{
		super(name, labelText, parent);
		this.readOnly = readOnly;
		this.oldValue = "";
		contentArray = new String[contents.size()];
		for (int idx=0; idx<contentArray.length; idx++)
		   contentArray[idx] = contents.elementAt(idx).toString();	
	    doOurFillIntoGrid();
	    initContents();	
	}
	/**
	 * Constructor for SystemComboBoxFieldEditor, using an array for the contents
	 * @param name - the unique ID for this editor. Used as index in preference store
	 * @param labelText - the label to show as the prompt preceding the dropdown
	 * @param contents - the list of strings to show in the dropdown, as an array
	 * @param readOnly - true if the user is to be prevented from entering text into the combo
	 * @param parent - the parent composite to host this editor
	 */
	public SystemComboBoxFieldEditor(String name, String labelText, String[] contents, boolean readOnly, Composite parent) 
	{
		super(name, labelText, parent);
		this.readOnly = readOnly;
		this.oldValue = "";
		contentArray = contents;				
	    doOurFillIntoGrid();
	    initContents();
	}
	/**
	 * Constructor for SystemComboBoxFieldEditor, using an array for the contents,
	 *  and specifying a resource bundle and key, from which we will retrieve the label
	 *  and the tooltip text
	 * @param name - the unique ID for this editor. Used as index in preference store
	 * @param rb - the resource bundle from which to retrieve the mri
	 * @param rbKey - the key into the resource bundle, to get the label (+"label") and tooltip text (+"tooltip")
	 * @param contents - the list of strings to show in the dropdown, as an array
	 * @param readOnly - true if the user is to be prevented from entering text into the combo
	 * @param parent - the parent composite to host this editor
	 */
	public SystemComboBoxFieldEditor(String name, ResourceBundle rb, String rbKey, String[] contents, boolean readOnly, Composite parent) 
	{
		super(name, rb.getString(rbKey+"label"), parent);
		this.readOnly = readOnly;
		this.oldValue = "";
		contentArray = contents;				
	    doOurFillIntoGrid();
	    setToolTipText(rb.getString(rbKey+"tooltip"));
	    initContents();
	}
	
	/**
	 * If this combobox is editable, set the validator to use here per keystroke
	 */
	public void setValidator(ISystemValidator validator)
	{
		this.validator = validator;
		if (textField != null)
		  textField.setTextLimit(validator.getMaximumNameLength());
	}
	
	/**
	 * Set the massager that is used to affect the user-entered text before 
	 * saving it to the preference store
	 */
	public void setMassager(ISystemMassager massager)
	{
		this.massager = massager;
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
	 */
	public int getNumberOfControls() 
	{
		return 2;
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	protected void doStore() 
	{		
		String text = textField.getText();
		if (massager != null)
		{
		  text = massager.massage(text);		
		  ignoreSelection = true;
		  textField.setText(text);
		  ignoreSelection = false;
		}

	    getPreferenceStore().setValue(getPreferenceName(), text);		
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	protected void doLoadDefault() 
	{
	    if (textField != null) 
	    {
		    String value = getPreferenceStore().getDefaultString(getPreferenceName());
		    initSelection(value);
	    }		
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
	 */
	protected void doLoad() 
	{
	    if (textField != null) 
	    {
		    String value = getPreferenceStore().getString(getPreferenceName());
		    initSelection(value);
	    }		
	}
	
	private void initSelection(String value)
	{
		if (value != null)
		{
	      ignoreSelection = true;  
		  textField.setText(value);
		  oldValue = value;
		  ignoreSelection = false;		  
		}		
		else
		  oldValue = "";
	}
	
	private void initContents()
	{
		if ((contentArray!=null) && (textField!=null) && !contentInited)
		{
		  textField.setItems(contentArray);	
		  contentInited = true; 
		}
	}

	/**
	 * This is called by our parent's constructor, which is too soon for us!
	 * So, we do nothing here and then call doOurFillIntoGrid later within our own
	 *  constructor.
	 * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(Composite, int)
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) 
	{
		parentComposite = parent;
		numColumnsInParentComposite = numColumns;
	}
	/**
	 * Create controls
	 */
	protected void doOurFillIntoGrid() 
	{
	   getLabelControl(parentComposite);

	   textField = getTextControl(parentComposite);
	   GridData gd = (GridData)textField.getLayoutData();
	   gd.horizontalSpan = numColumnsInParentComposite - 1;
	   gd.horizontalAlignment = GridData.FILL;
	   gd.grabExcessHorizontalSpace = true;
	   textField.setLayoutData(gd);			   
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	protected void adjustForNumColumns(int numColumns) 
	{
	   GridData gd = (GridData)textField.getLayoutData();
	   gd.horizontalSpan = numColumns - 1;
	   // We only grab excess space if we have to
	   // If another field editor has more columns then
	   // we assume it is setting the width.
	   gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;		
	}

    /**
 	 * Returns this field editor's Combo control.
 	 */
	public Combo getTextControl() 
	{
		return textField;
    }    

    /**
     * Returns this field editor's text control.
     * <p>
     * The control is created if it does not yet exist
     * </p>
     *
     * @param parent the parent
     * @return the text control
     */
    public Combo getTextControl(Composite parent) 
    {
	    if (textField == null) 
	    {
	    	if (isReadOnly())
		      textField = SystemWidgetHelpers.createReadonlyCombo(parent, null);
		    else
		      textField = SystemWidgetHelpers.createCombo(parent, null);
		    if (tip != null)
		      textField.setToolTipText(tip);
 	        initContents();

		    textField.addSelectionListener(getSelectionListener());		     	        
		    if (!isReadOnly())
		    {
		      textField.addModifyListener(getModifyListener());
		      if (validator != null)
		        textField.setTextLimit(validator.getMaximumNameLength());

			  textField.addFocusListener(new FocusAdapter() 
			  {
					public void focusGained(FocusEvent e) {
						refreshValidState();
					}
					public void focusLost(FocusEvent e) {
						clearErrorMessage();
					}
			  });
		    }

		    textField.addDisposeListener(new DisposeListener() 
		    {
			  public void widgetDisposed(DisposeEvent event) 
			  {
				textField = null; // not sure why we have to do this
			  }
		    });
	    } else {
		    checkParent(textField, parent); // not sure what this is, came from some earlier eclipse example
	    }
	    return textField;
    } 
        
    /**
     * Allows setting of tooltip text
     */
    public void setToolTipText(String tip)
    {
        if (textField != null)
          textField.setToolTipText(tip);
        this.tip = tip;
    }
    
    /**
     * Is this a readonly combo?
     */
    protected boolean isReadOnly()
    {
    	 return readOnly;
    }   
    
	/**
 	 * Returns this field editor's selection listener.
 	 * The listener is created if nessessary.
 	 *
 	 * @return the selection listener
 	 */
	private SelectionListener getSelectionListener() 
	{
		if (selectionListener == null)
			createSelectionListener();
		return selectionListener;
	}

	/**
 	 * Creates a selection listener.
 	 */
	protected void createSelectionListener() 
	{
	    selectionListener = new SelectionAdapter() 
	    {
			public void widgetSelected(SelectionEvent event) 
			{
				Widget widget = event.widget;
				if (widget == textField)
				  selectionChanged();
			}
		};
	}
  
    /**
     * Combobox selection changed
     */
    protected void selectionChanged()
    {
    	if (!ignoreSelection)
		  setPresentsDefaultValue(false);
		ignoreSelection = false;
    }  

	/**
 	 * Returns this field editor's selection listener.
 	 * The listener is created if nessessary.
 	 *
 	 * @return the selection listener
 	 */
	private ModifyListener getModifyListener() 
	{
		if (modifyListener == null)
			createModifyListener();
		return modifyListener;
	}
	
	/**
 	 * Creates a modify text listener.
 	 * Ony used for non-readonly flavours
 	 */
	protected void createModifyListener() 
	{
	    modifyListener = new ModifyListener() 
	    {
			public void modifyText(ModifyEvent event) 
			{
				Widget widget = event.widget;
				if ((widget == textField) && !ignoreSelection)
				  valueChanged();
			}
		};
	}
  
    /**
     * Validate contents of combo field
     */
    protected void validate()
    {
    	if (!ignoreSelection)
    	{
		  setPresentsDefaultValue(false);
		  if (validator != null)
		  {
		  	 String errmsg = null;
		  	 String value = textField.getText();
		  	 if (!isSpecialValue(value))
		  	   errmsg = validator.isValid(value);
		  	 if (errmsg != null)
		  	   showErrorMessage(errmsg);
		  	 else
		  	   clearErrorMessage();
		  	 isValid = (errmsg == null);
		  }
    	}
		ignoreSelection = false;
    }  
    /**
     * Test if current value is one of the special values
     */
    private boolean isSpecialValue(String input)
    {
    	if (contentArray == null)
    	  return false;
    	else
    	{
    		boolean match = false;
    	    for (int idx=0; !match && (idx<contentArray.length); idx++)
    	      if (input.equalsIgnoreCase(contentArray[idx]))
    	        match = true;
    	    return match;
    	}
    }    
    
	/**
	 * Informs this field editor's listener, if it has one, about a change
	 * to the value (<code>VALUE</code> property) provided that the old and
	 * new values are different.
	 * <p>
	 * This hook is <em>not</em> called when the text is initialized 
	 * (or reset to the default value) from the preference store.
	 * </p>
	 */
	protected void valueChanged() 
	{
		setPresentsDefaultValue(false);
		boolean oldState = isValid;
		refreshValidState();

		if (isValid != oldState)
			fireStateChanged(IS_VALID, oldState, isValid);
			
	    String newValue = textField.getText();
	    fireValueChanged(VALUE, oldValue, newValue);
	    oldValue = newValue;
	}
    
    /**
     * Override of parent to return validity state.
     * If this readonly, we always return true, else we return result of last validation
     */
    public boolean isValid()
    {
        return isValid;
    }
    
    /**
     * Override of parent to refresh validity state by checking if the
     *  input is valid. Does nothing unless this is not a readonly combo.
     */
    protected void refreshValidState() 
    {    	
    	isValid = true;
    	if (!isReadOnly())
    	{
    	  validate();
    	}
    }
    
    /**
     * Set focus
     */
    public void setFocus() 
    {
	    if (textField!= null)
	      textField.setFocus();
    }
}
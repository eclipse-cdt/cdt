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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;


/**
 * A preference page field editor that prompts for a string.
 * Unlike the eclipse-supplied StringFieldEditor, this one allows
 * use of RSE validators and massagers for error checking and 
 * massaging of the user-entered input prior to persisting.
 */
public class SystemStringFieldEditor extends FieldEditor
{
	
    private Text textField;
    private String tip;
    private boolean isValid = true;
    private ModifyListener modifyListener = null;    
    private boolean ignoreSelection = false;
    private ISystemValidator validator = null;
    private ISystemMassager  massager = null;
    private Composite parentComposite;
    private String oldValue;
    private int numColumnsInParentComposite;
    
    
	/**
	 * Default constructor for SystemStringFieldEditor.
	 * Not permitted to be used.
	 */
	private SystemStringFieldEditor() 
	{
		super();
	}

	/**
	 * Constructor for SystemStringFieldEditor
	 * @param name - the unique ID for this editor. Used as index in preference store
	 * @param rb - the resource bundle from which to retrieve the mri
	 * @param rbKey - the key into the resource bundle, to get the label (_LABEL and tooltip text (_TOOLTIP
	 * @param parent - the parent composite to host this editor
	 */
	public SystemStringFieldEditor(String name, ResourceBundle rb, String rbKey, Composite parent) 
	{
		super(name, rb.getString(rbKey+"label"), parent);
		this.oldValue = "";
	    //createControl(parent);				
	    doOurFillIntoGrid();
	    setToolTipText(rb.getString(rbKey+"tooltip"));
	}
	
	/**
	 * Set the validator to use per keystroke. If not set, no validation is done
	 */
	public void setValidator(ISystemValidator validator)
	{
		this.validator = validator;
		if (textField != null)
		  textField.setTextLimit(validator.getMaximumNameLength());
	}
	
	/**
	 * Set the massager that is used to affect the user-entered text before 
	 * saving it to the preference store.
	 */
	public void setMassager(ISystemMassager massager)
	{
		this.massager = massager;
	}

	/**
	 * Return number of columns we need. We return 2.
	 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
	 */
	public int getNumberOfControls() 
	{
		return 2;
	}

	/**
	 * Save the user-entered value to the preference store.
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
	 * Load the entry field contents from the preference store default value
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
	 * Load the entry field contents from the preference store current value
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
	 * Adjust grid data to support the number of columns, after all field editors
	 * have been added to the page.
	 * 
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
 	 * Returns this field editor's Text control.
 	 */
	protected Text getTextControl() 
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
    public Text getTextControl(Composite parent) 
    {
	    if (textField == null) 
	    {
		    textField = SystemWidgetHelpers.createTextField(parent, null);
		    if (tip != null)
		      textField.setToolTipText(tip);

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
 	 * Creates a modify text listener used in per-keystroke validation
 	 */
	protected void createModifyListener() 
	{
	    modifyListener = new ModifyListener() 
	    {
			public void modifyText(ModifyEvent event) 
			{
				Widget widget = event.widget;
				if (widget == textField)
				  valueChanged();
			}
		};
	}
  
    /**
     * Validate contents of text field
     */
    protected void validate()
    {
    	if (!ignoreSelection)
    	{
		  setPresentsDefaultValue(false);
		  if (validator != null)
		  {
		  	 String value = textField.getText();
		  	 String errmsg = validator.isValid(value);
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
    	validate();
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
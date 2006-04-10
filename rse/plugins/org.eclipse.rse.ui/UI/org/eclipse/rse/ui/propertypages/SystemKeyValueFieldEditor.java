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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorSpecialChar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;


/**
 * For string key,value pair properties that have a discrete list of key values.
 * Used in preference pages.
 */
public class SystemKeyValueFieldEditor extends FieldEditor 
{
    private List  keysField;
    private Text  valueField;
    private Label keysLabel, valueLabel;
    private Composite keysComposite, valueComposite;
    private Group boxComposite;
    private Button setButton, clearButton;
    private String keysLabelString, valueLabelString;
    private char keyValueDelimiter='=';
    private char keyValuePairDelimiter=';';
    private SystemMessage errorMessage;
    private boolean contentInited;
	protected ISystemValidator valueValidator, defaultValueValidator;
    private String[] contentArray;
    private Hashtable keyValues;
    private SelectionListener selectionListener;
    
    private static boolean boxFlavor = true;
    
	/**
	 * Constructor 
	 */
	private SystemKeyValueFieldEditor() 
	{
		super();
	}

	/**
	 * Constructor 
	 */
	public SystemKeyValueFieldEditor(String name, String labelText, String[] keys, 
	                                    String keysLabelString, String valueLabelString, Composite parent) 
	{
		super(name, labelText, parent);
		keyValues = new Hashtable();
		contentArray = keys;
		this.keysLabelString = keysLabelString;
		this.valueLabelString = valueLabelString;
        // the ctor's call to super causes the controls to be created before this
        // instance data is accessible, so we do it now...
	    keysLabel.setText(keysLabelString);
  	    valueLabel.setText(valueLabelString);
  	    //if (boxFlavor)
  	    //  boxComposite.setText(labelText);
	    initContents();

		String specialChars = makeString(keyValueDelimiter,keyValuePairDelimiter);
		defaultValueValidator = new ValidatorSpecialChar(specialChars, ValidatorSpecialChar.EMPTY_ALLOWED_YES);	    
	}

	/**
	 * Call this to specify a validator for the value entry field. It will be called per keystroke.
	 * By default we just check against the special characters used to delimit the key and value (=)
	 *  and to delimiter each key/value pair (;)
	 */
	public void setValueValidator(ISystemValidator v)
	{
		valueValidator = v;
	}
	/**
	 * Call this to set the characters used to delimit the strings in the preferences store
	 * @param keyValueDelimiter The char to distinguish between key and value. Default is =
	 * @param keyValuePairDelimiter The char to distinguish between each key/value pair. Default is ;
	 */
	public void setDelimiterCharacters(char keyValueDelimiter, char keyValuePairDelimiter)
	{
		this.keyValueDelimiter = keyValueDelimiter;
		this.keyValuePairDelimiter = keyValuePairDelimiter;
		String specialChars = makeString(keyValueDelimiter,keyValuePairDelimiter);
		defaultValueValidator = new ValidatorSpecialChar(specialChars, ValidatorSpecialChar.EMPTY_ALLOWED_YES);
	}
	
	/**
	 * @see FieldEditor#getNumberOfControls()
	 */
	public int getNumberOfControls() 
	{
		return boxFlavor ? 1 : 2;
	}

	/**
	 * @see FieldEditor#doStore()
	 */
	protected void doStore() 
	{
	    String s = createString(keyValues);
	    if (s != null)
		  getPreferenceStore().setValue(getPreferenceName(), s);	    
	}

	/**
	 * @see FieldEditor#doLoadDefault()
	 */
	protected void doLoadDefault() 
	{
	    if (keysField != null) 
	    {
		    String value = getPreferenceStore().getDefaultString(getPreferenceName());
		    if (value != null)
		      keyValues = parseString(value);
		    else
		      keyValues = new Hashtable();
		    valueField.setText("");		    
		    keysField.select(0);		    
			clearButton.setEnabled(false);		    
			setButton.setEnabled(false);	
	        //selectionChanged();				    		    
	    }		
	}

	/**
	 * @see FieldEditor#doLoad()
	 */
	protected void doLoad() 
	{
	    //if (keysField != null) 
	    //{
		    String value = getPreferenceStore().getString(getPreferenceName());
		    if ((value == null) || (value.length()==0))
		      value = getPreferenceStore().getDefaultString(getPreferenceName());
		    if (value != null)
		      keyValues = parseString(value);
	        if (keysField != null) 
	        {
		      keysField.select(0);
	          selectionChanged();
	        }
	    //}		
	}
	
	/**
	 * Parse out list of key-value pairs into a hashtable
	 */
	public Hashtable parseString(String allvalues)
	{
		StringTokenizer tokens = new StringTokenizer(allvalues, makeString(keyValueDelimiter, keyValuePairDelimiter));
		Hashtable keyValues = new Hashtable(10);
		int count = 0;
		String token1=null;
		String token2=null;
		while (tokens.hasMoreTokens())
		{
			count++;
			if ((count % 2) == 0) // even number
			{
			  token2 = tokens.nextToken();
			  keyValues.put(token1, token2);
			}
			else
			  token1 = tokens.nextToken();
		}
		return keyValues;
	}
	/**
	 * Convert hashtable of key-value pairs into a single string
	 */
	protected String createString(Hashtable keyValues)
	{
		Enumeration keys = keyValues.keys();
		StringBuffer sb = new StringBuffer();
		while (keys.hasMoreElements())
		{
			String key = (String)keys.nextElement();
			String value = (String)keyValues.get(key);
			if ((value != null) && (value.length()>0))
			{
			  sb.append(key);
			  sb.append(keyValueDelimiter);
			  sb.append(value);
			  sb.append(keyValuePairDelimiter);
			}
		}
		//System.out.println("Pref String: " + sb);
		return sb.toString();
	}

	
	private String makeString(char charOne, char charTwo)
	{
		StringBuffer s = new StringBuffer(2);
		s.append(charOne);
		s.append(charTwo);
		return s.toString();
	}
	
	private void initContents()
	{
		if ((contentArray!=null) && (keysField!=null) && !contentInited)
		{
		  keysField.setItems(contentArray);	
		  contentInited = true; 
		}
	}

	/**
	 * @see FieldEditor#doFillIntoGrid(Composite, int)
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) 
	{
	   GridData gd = null;
	   // label goes all the way across top
	   if (!boxFlavor)
	   {
	     Control control = getLabelControl(parent);	   
	     if (control!=null)
	     {
	       gd = new GridData();
	       gd.horizontalSpan = numColumns;
 	       control.setLayoutData(gd);	 
	     }
	   }
 
boxComposite = SystemWidgetHelpers.createGroupComposite(parent, 2, getLabelText());
	   ((GridData)boxComposite.getLayoutData()).horizontalSpan = numColumns;
//	   parentComposite = boxComposite;
//	   numparentcols = 2;
	    
       // under the label we place a labeled combo box, and so on
	   keysComposite = getKeysControl(boxComposite);
//	   gd = new GridData();
//	   gd.horizontalAlignment = GridData.FILL;
//	   gd.grabExcessHorizontalSpace = true;
//	   gd = new GridData(GridData.FILL_BOTH); //dwd
//	   keysComposite.setLayoutData(gd);		
	   
	   valueComposite = getValueControl(boxComposite);
//	   gd = new GridData();
//	   gd.horizontalSpan = numparentcols - 1;
//	   gd.horizontalAlignment = GridData.FILL;
//	   gd.grabExcessHorizontalSpace = true;		   
//	   gd = new GridData(GridData.FILL_BOTH); //dwd
//	   keysComposite.setLayoutData(gd);			   

	}


	/**
	 * @see FieldEditor#adjustForNumColumns(int)
	 */
	protected void adjustForNumColumns(int numColumns) 
	{
	    Control control = getLabelControl();
	    if (control != null)
	      ((GridData)control.getLayoutData()).horizontalSpan = numColumns;
	    if (boxComposite != null)
	      ((GridData)boxComposite.getLayoutData()).horizontalSpan = numColumns;
	}



    /**
 	 * Returns this field editor's List control for the keys.
 	 */
	protected List getKeysControl() 
	{
		return keysField;
    }
    /**
 	 * Returns this field editor's Text control for the key value.
 	 */
	protected Text getValueControl() 
	{
		return valueField;
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
    public Composite getKeysControl(Composite parent) 
    {
	    if (keysComposite == null) 
	    {
	    	// create composite
	    	keysComposite = new Composite(parent, SWT.NULL);
	    	GridLayout layout = new GridLayout();
	    	keysComposite.setLayout(layout);
	        GridData data = new GridData(GridData.FILL_BOTH); // dwd was FILL_HORIZONTAL
	        data.heightHint = 150; // dwd was 100 
	        keysComposite.setLayoutData(data);   
	    	// populate with label
	    	keysLabel = new Label(keysComposite, SWT.NULL);
	    	if (keysLabelString != null)
	    	  keysLabel.setText(keysLabelString);	    	
	        data = new GridData(GridData.FILL_HORIZONTAL);
	        data.widthHint = 150;
	        keysLabel.setLayoutData(data);
	        // populate with list
	    	int options = SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL;
		    keysField = new List(keysComposite, options);
	        data = new GridData(GridData.FILL_BOTH); // dwd was FILL_HORIZONTAL
	        data.widthHint = 150;
	        keysField.setLayoutData(data);
		    keysField.addSelectionListener(getSelectionListener());		

		    keysComposite.addDisposeListener(new DisposeListener() 
		    {
			  public void widgetDisposed(DisposeEvent event) 
			  {
			  	keysComposite = null;
				keysField = null;
				keysLabel = null;
			  }
		    });
	    }
	   // else 
	   // {
		//    checkParent(keysComposite, parent);
	   // }
	    return keysComposite;
    } 

	/**
	 * Returns this field editor's value entry field control.
	 *
	 * @param parent the parent control
	 * @return the list control
	 */
	public Composite getValueControl(Composite parent) 
	{
		if (valueComposite == null) 
		{
	    	// create composite
	    	valueComposite = new Composite(parent, SWT.NULL);
	    	GridLayout layout = new GridLayout();
	    	valueComposite.setLayout(layout);
	        //GridData data = new GridData(GridData.FILL_HORIZONTAL); 
	        // DKM: not filling vertical causes trucations on linux	        
			GridData data = new GridData(GridData.FILL_BOTH);
	        data.heightHint = 150; // dwd was 100
	        valueComposite.setLayoutData(data);   
	    	// populate with label
	    	valueLabel = new Label(valueComposite, SWT.NULL);
	    	if (valueLabelString != null)
	    	  valueLabel.setText(valueLabelString);
	        data = new GridData(GridData.FILL_HORIZONTAL);
	        valueLabel.setLayoutData(data);
	        // populate with entry field
			valueField = new Text(valueComposite, SWT.BORDER | SWT.SINGLE);
	        data = new GridData(GridData.FILL_HORIZONTAL);
	        data.widthHint = 150;
	        valueField.setLayoutData(data);   			
	        // populate with button
			setButton = createPushButton(valueComposite, SystemResources.ACTION_SET_LABEL, SystemResources.ACTION_SET_TOOLTIP);
			clearButton = createPushButton(valueComposite, SystemResources.ACTION_CLEAR_LABEL, SystemResources.ACTION_CLEAR_TOOLTIP);			
			
		    // add keystroke listener...
		    valueField.addModifyListener(
			  new ModifyListener() 
			  {
				public void modifyText(ModifyEvent e) 
				{
					validateValueInput();
				}
			  }
		    );
            // add dispose listener
			valueComposite.addDisposeListener(new DisposeListener() 
			{
				public void widgetDisposed(DisposeEvent event) 
				{
					valueComposite = null;
					valueField = null;
					valueLabel = null;
					setButton = null;
				}
			});
		} 
	//	else 
	//	{
	//		checkParent(valueComposite, parent);
	//	}
		return valueComposite;
	}                   

    /**
 	 * Helper method to create a push button.
 	 * 
 	 * @param parent the parent control
 	 * @param label
 	 * @param tooltip
 	 */
	private Button createPushButton(Composite parent, String label, String tooltip) 
	{
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.setToolTipText(tooltip);		
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		int widthHint = convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		button.addSelectionListener(getSelectionListener());
		return button;
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
	public void createSelectionListener() 
	{
	    selectionListener = new SelectionAdapter() 
	    {
			public void widgetSelected(SelectionEvent event) 
			{
				Widget widget = event.widget;
				if (widget == setButton)
			      setPressed();
				else if (widget == clearButton) 
				  clearPressed();
				else if (widget == keysField)
				  selectionChanged();
			}
		};
	}
        
  	/**
	 * This hook method is called whenever the text changes in the input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the PreferencePage's message line.
	 * @see #setValueValidator(ISystemValidator)
	 */
	protected SystemMessage validateValueInput() 
	{			
	    errorMessage= null;
	    String valueInput = valueField.getText().trim();
		if (valueValidator != null)
	      errorMessage = valueValidator.validate(valueInput);
	    else if (defaultValueValidator != null)
	      errorMessage = defaultValueValidator.validate(valueInput);	    
	    if (errorMessage != null)
		  showErrorMessage(errorMessage.getLevelOneText());
		else
		  clearErrorMessage();
		setButton.setEnabled((errorMessage == null) && (valueInput.length()>0));
		//clearButton.setEnabled(true);		
		return errorMessage;		
	}
	
	/**
	 * Notifies that the Set button has been pressed.
	 */
	private void setPressed() 
	{
		setPresentsDefaultValue(false);
		int index = keysField.getSelectionIndex();		
		if (index >= 0) 
		{
		   String value = valueField.getText().trim();
		   valueField.setText(value);
		   if (value.length() == 0)
		     keyValues.remove(contentArray[index]);
		   else
		     keyValues.put(contentArray[index],value);
		   selectionChanged();
		}
		else
		  setButton.setEnabled(false);
	}

	/**
	 * Notifies that the Clear button has been pressed.
	 */
	private void clearPressed() 
	{
		setPresentsDefaultValue(false);
		int index = keysField.getSelectionIndex();		
		if (index >= 0) 
		{
		   //valueField.setText("");
		   keyValues.remove(contentArray[index]);
		   selectionChanged();
		}
		else 
		  clearButton.setEnabled(false);
	}
	/**
	 * Notifies that the list selection has changed.
	 */
	private void selectionChanged() 
	{
		int index = keysField.getSelectionIndex();
		if (index >= 0)
		{
          String key = contentArray[index];
          String value = (String)keyValues.get(key);
          if (value == null)
          {
            valueField.setText("");
            clearButton.setEnabled(false);            
          }
          else
          {
            valueField.setText(value);
            clearButton.setEnabled(true);
          }
		}
		else
		{
			clearButton.setEnabled(false);
		}
	    setButton.setEnabled(false);		
	}	

    /**
     * Change the height hint for this composite.
     * Default is 100 pixels.
     */
    public void setHeightHint(int hint)
    {
    	if (keysComposite != null)
 	      ((GridData)keysComposite.getLayoutData()).heightHint = hint;
    	if (valueComposite != null)
 	      ((GridData)valueComposite.getLayoutData()).heightHint = hint;

    }
    /**
     * Change the width hint for the keys list
     * Default is 150 pixels.
     */
    public void setKeysWidthHint(int hint)
    {
    	if (keysComposite != null)
 	      ((GridData)keysComposite.getLayoutData()).widthHint = hint;
    }
    /**
     * Change the width hint for the values fields on the right
     * Default is not set
     */
    public void setValuesWidthHint(int hint)
    {
    	if (valueComposite != null)
 	      ((GridData)valueComposite.getLayoutData()).widthHint = hint;
    }
    
    /**
     * Set the tooltip text
     */
    public void setToolTipText(String tip)
    {
    	if (boxFlavor)
    	  boxComposite.setToolTipText(tip);
    	else
    	{
    		keysComposite.setToolTipText(tip);
    		valueComposite.setToolTipText(tip);
    	}
    }
    /**
     * Get the tooltip text
     */
    public String getToolTipText()
    {
    	if (boxFlavor)
    	  return boxComposite.getToolTipText();
    	else
    	  return keysComposite.getToolTipText();    	   
    }
    
    /*
     * Override to return null!
     *
    public Label getLabelControl(Composite parent) 
    {
    	System.out.println("Inside getLabelControl");  
    	return super.getLabelControl(parent);  
    }*/
}
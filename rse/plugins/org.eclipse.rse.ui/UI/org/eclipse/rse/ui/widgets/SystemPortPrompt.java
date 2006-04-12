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

package org.eclipse.rse.ui.widgets;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemPropertyResources;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.SystemNumericVerifyListener;
import org.eclipse.rse.ui.validators.ValidatorPortInput;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;


/**
 * A composite encapsulating the GUI widgets for prompting for a port. Used in the core SubSystem property
 *  page but also be instantiated and used anywhere.
 */
public class SystemPortPrompt 
       //extends Composite 
       implements SelectionListener
{
	
	private   Composite              composite_prompts;
	private   Label                  labelPortPrompt, labelPort;
	private   InheritableEntryField  textPort;
	protected SystemMessage          errorMessage;

    protected boolean                portEditable=true;
    protected boolean                portApplicable=true;
	protected int                existingPortValue;
	// validators
	protected ISystemValidator       portValidator;	
	// Inputs from caller
	protected ISystemMessageLine     msgLine;    
	
    /**
     * Constructor when you want a new composite to hold the child controls
     */
	public SystemPortPrompt(Composite parent, int style, ISystemMessageLine msgLine, 
	                        boolean wantLabel, boolean isPortEditable, 
	                        int existingPortValue, ISystemValidator portValidator)
	{
		//super(parent, style);
		//composite_prompts = this;
		composite_prompts = new Composite(parent, style);		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = wantLabel ? 2 : 1;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite_prompts.setLayout(gridLayout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
	    gridData.grabExcessVerticalSpace = false;
		composite_prompts.setLayoutData(gridData);
		
		init(composite_prompts, msgLine, wantLabel, isPortEditable, existingPortValue, portValidator);
					    
		composite_prompts.pack();		
	}
    /**
     * Constructor when you have an existing composite to hold the child controls
     */
	public SystemPortPrompt(Composite composite_prompts, ISystemMessageLine msgLine, 
	                        boolean wantLabel, boolean isPortEditable, 
	                        int existingPortValue, ISystemValidator portValidator)
	{
		this.composite_prompts = composite_prompts;
		
		init(composite_prompts, msgLine, wantLabel, isPortEditable, existingPortValue, portValidator);
	}

    /**
     * Get user-entered Port number.
     */
    public int getPort()
    {
    	if (isEditable())
    	{
    	  String port = textPort.getLocalText(); // will be "" if !textPort.getIsLocal(), which results in wiping out local override
      	  Integer portInteger = null;
    	  if (textPort.isLocal() && (port.length()>0))
            portInteger = new Integer(port); 
    	  else
    	    portInteger = new Integer(0);
    	  return portInteger.intValue();
    	}
    	else
    	  return existingPortValue;
    }
    /**
     * Return user-enter Port number as a string
     */
    public String getPortString()
    {
    	return internalGetPort();
    }
    
    /**
     * Return true if port is user-editable
     */
    public boolean isEditable()
    {
    	return (portEditable && portApplicable);
    }
    
    /**
     * Return true if current port value is without error
     */
    public boolean isComplete()
    {
    	if (!isEditable())
    	  return true;
    	else
    	  return ((errorMessage==null) && (internalGetPort().length()>0));
    }

    /**
     * Set the initial port value
     */
    public void setPort(int port)
    {
	    // port
	    if (portEditable || portApplicable)
	    {
	      String localPort = null;

		  localPort = "" + port;		  
		  int iPort = port;
		  if (!portEditable) // applicable but not editable
		    labelPort.setText(localPort);
		  else // editable 
		  {
		    textPort.setLocalText(localPort);
		    textPort.setInheritedText("0 "+SystemPropertyResources.RESID_PORT_DYNAMICSELECT);
		    textPort.setLocal(iPort != 0);	    
		  }
	    }
    }
    
    /**
     * Set the focus
     */
    public boolean setFocus()
    {
    	if (textPort != null)
    	{
    	  textPort.getTextField().setFocus();
    	  return true;
    	}
    	else
    	  return composite_prompts.setFocus();
    }
    
    /**
     * Reset to original value
     */
    public void setDefault()
    {
    	setPort(existingPortValue);
    }
    
    /**
     * Return the entry field or label for the port prompt
     */
    public Control getPortField()
    {
    	if (textPort != null)
    	  return textPort.getTextField();
    	else
    	  return labelPort;
    }

  	/**
  	 * Validate port value per keystroke
	 */	
	public SystemMessage validatePortInput() 
	{			
		boolean wasInError = (errorMessage != null);
	    errorMessage= null;
	    if (textPort!=null)
	    {
	      if (!textPort.isLocal())
	      {
	      	if (wasInError)
	      	  clearErrorMessage();
	        return null;
	      }
		  if (portValidator != null)
	        errorMessage= portValidator.validate(textPort.getText().trim());
	      else if (internalGetPort().equals(""))
		    errorMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_USERID_EMPTY);
	    }
	    if (errorMessage == null)
	    {
	      if (wasInError)
	        clearErrorMessage();
	    }
	    else
		  setErrorMessage(errorMessage);
		//setPageComplete();
		return errorMessage;		
	}

    // -------------------
    // INTERNAL METHODS...
    // -------------------
    /**
     * Initialize vars, create and init prompts
     */
	protected void init(Composite composite_prompts, ISystemMessageLine msgLine, 
  	                    boolean wantLabel, boolean isPortEditable, 
	                    int existingPortValue, ISystemValidator portValidator)
	{
		this.msgLine = msgLine;
		this.portEditable = isPortEditable;
		this.existingPortValue = existingPortValue;
		if (portValidator == null)
		  portValidator = new ValidatorPortInput();
		this.portValidator = portValidator;
		
	    createPortPrompt(composite_prompts, wantLabel);
	    setPort(existingPortValue);
	    
		if (textPort != null)	
		{
		  textPort.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
		            //System.out.println("in modify text '"+internalGetPort()+"'");					
					validatePortInput();
				}
			});
		  
		  textPort.getTextField().addVerifyListener(new SystemNumericVerifyListener());
	      //textPort.addSelectionListener(this); Removed for defect 44132
		}
	}

	/**
	 * Return user-entered Port number.
	 */	    
	protected String internalGetPort()
	{
		if (textPort != null)
		  return textPort.getText().trim();
		else 
		  return labelPort.getText();
	}
    
    /**
     * Create GUI widgets
     */	
	protected void createPortPrompt(Composite composite_prompts, boolean wantLabel)
	{
		// Port prompt
		String portRange = " (1-" + ValidatorPortInput.MAXIMUM_PORT_NUMBER + ")";
		if (wantLabel) {
			String labelText = SystemWidgetHelpers.appendColon(SystemResources.RESID_SUBSYSTEM_PORT_LABEL + portRange);
			labelPortPrompt = SystemWidgetHelpers.createLabel(composite_prompts, labelText);
		}
	    portApplicable = isPortApplicable();
	    portEditable = isPortEditable();
	    if (isEditable())
	    {
          textPort = SystemWidgetHelpers.createInheritableTextField(
              composite_prompts,SystemResources.RESID_SUBSYSTEM_PORT_INHERITBUTTON_TIP,SystemResources.RESID_SUBSYSTEM_PORT_TIP);
          textPort.setFocus();
	    }
	    else
	    {
	      String labelValue = " ";
	      if (!portApplicable)
	        labelValue = getTranslatedNotApplicable();
	      labelPort = SystemWidgetHelpers.createLabel(composite_prompts, labelValue);
	    }
	}	

	/**
	 * Return true if the port is applicable.
	 * For this to be false, the caller must state the port is not editable,
	 *  and the port value must be null or Integer(-1).
	 */
	protected boolean isPortApplicable()
	{
		if (!isPortEditable() && (existingPortValue==-1))
		  return false;
		else
		  return true;
	}
	/**
	 * Return true if the port is editable for this subsystem
	 */
	protected boolean isPortEditable()
	{
		return portEditable;
	}

	/**
	 * Return "Not applicable" translated
	 */
	private String getTranslatedNotApplicable()
	{
		return SystemPropertyResources.RESID_TERM_NOTAPPLICABLE;
	} 
	
	protected void setErrorMessage(SystemMessage msg)
	{
		if (msgLine != null)
		  msgLine.setErrorMessage(msg);
	}
	protected void clearErrorMessage()
	{
		if (msgLine != null)
		  msgLine.clearErrorMessage();
	}
		
		
    // SELECTIONLISTENER...
    public void widgetDefaultSelected(SelectionEvent event)
    {
    }
    public void widgetSelected(SelectionEvent event)
    {
    	//System.out.println("Inside widgetSelected. textPort.isLocal(): " + textPort.isLocal());
    	if (textPort.isLocal()) // from local to non-local
    	{
    		/* I don't know why I did this! Phil. Removed for defect 44132
          if (errorMessage != null)
          {
          	errorMessage = null;
          	clearErrorMessage();
          }		*/
    	}
    	else
    	{
    		//validatePortInput(); doesn't work because it is called before the toggle
    	}
    }
    
}
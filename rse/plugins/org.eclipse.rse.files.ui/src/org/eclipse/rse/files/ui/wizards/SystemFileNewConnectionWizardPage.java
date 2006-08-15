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

package org.eclipse.rse.files.ui.wizards;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorServerPortInput;
import org.eclipse.rse.ui.wizards.AbstractSystemNewConnectionWizardPage;
import org.eclipse.rse.ui.wizards.AbstractSystemWizardPage;
import org.eclipse.rse.ui.wizards.ISystemWizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;



/**
 * A page that prompts for unique universal files information in the New Connection wizard.
 * This page appears for remote Unix, Linux and Windows connections but not for iSeries or Local connections.
 */
public class SystemFileNewConnectionWizardPage extends AbstractSystemNewConnectionWizardPage 
                                          
{	

	protected Label labelPortPrompt;
	protected Text  textPort;
	protected ISystemValidator portValidator;
	protected SystemMessage errorMessage;
	protected boolean enablePortSelection = true;//false; // todo: enable in next releaes

    // SEE DEFECTS 43194 AND 42780
    
	/**
	 * Constructor 
	 */
	public SystemFileNewConnectionWizardPage(IWizard wizard, ISubSystemConfiguration parentFactory)
	{
		//super(wizard, parentFactory); todo: use this when we enable port selection
		super(wizard, parentFactory, parentFactory.getId(), 
		      SystemResources.RESID_NEWCONN_SUBSYSTEMPAGE_FILES_TITLE,
		      SystemResources.RESID_NEWCONN_SUBSYSTEMPAGE_FILES_DESCRIPTION);		
		if (enablePortSelection)
		  getPortValidator();
	}
	
	/**
	 * Return true if we support port selection yet
	 */
	public boolean isInformationalOnly()
	{
		return !enablePortSelection;
	}

	/**
	 * @see AbstractSystemWizardPage#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl() 
	{
		if (textPort != null)
		  return textPort;
		else
		  return null;
	}
	
	public void setEnablePortSelection(boolean flag)
	{
		enablePortSelection = flag;
	}
	
	/**
	 * Get the port validator. By default returns new ValidatorPortInput
	 */
	protected ISystemValidator getPortValidator()
	{
		if (portValidator == null)
		  portValidator = new ValidatorServerPortInput();
		return portValidator;
	}

	/**
	 * @see AbstractSystemWizardPage#createContents(Composite)
	 */
	public Control createContents(Composite parent) 
	{
		int nbrColumns = 2;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);
		

        // Instructional verbage
		String text = null;
		text = SystemResources.RESID_NEWCONN_SUBSYSTEMPAGE_FILES_VERBAGE1;
			//,
		        // the following is default English text to use if the string is not found in the mri"
		      //  "To connect to your remote system, you must first copy and expand the supplied Java server code jar file on that system, and either manually start that server or the supplied daemon. You will find the instructions for this in the Help perspective. ");
		boolean border = false;
		int span = nbrColumns;
		int widthHint = 200;
		
		SystemWidgetHelpers.createVerbiage(composite_prompts, text, span, border, widthHint);		
		
		SystemWidgetHelpers.createSpacerLine(composite_prompts, span, false);
		
		text = SystemResources.RESID_NEWCONN_SUBSYSTEMPAGE_FILES_VERBAGE2;
		//,
		        // the following is default English text to use if the string is not found in the mri"
		  //      "If you manually start the communications server, you will need to set the port number property for this connection. To do this, expand your newly created connection in the Remote Systems Explorer perspective. Right click on the Files subsystem and select Properties. You can specify the port to match the port you specified or were assigned for the server.");
		
		SystemWidgetHelpers.createVerbiage(composite_prompts, text, span, border, widthHint);		
        
		
		// Port prompt
		if (enablePortSelection)
		{
		  String labelText = SystemWidgetHelpers.appendColon(SystemResources.RESID_SUBSYSTEM_PORT_LABEL);
		  labelPortPrompt = SystemWidgetHelpers.createLabel(composite_prompts, labelText);

          textPort = SystemWidgetHelpers.createTextField(
            composite_prompts,null,SystemResources.RESID_SUBSYSTEM_PORT_TIP);
          textPort.setText("0");

		  textPort.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validatePortInput();
				}
			}
		  );			
		}
		
			
		
		return composite_prompts;
	}


	/**
	 * Return true if the port is editable for this subsystem
	 */
	protected boolean isPortEditable()
	{
		return parentFactory.isPortEditable();
	}

	/**
	 * @see ISystemWizardPage#performFinish()
	 */
	public boolean performFinish() 
	{
		if (textPort == null)
		  return true;
		else  		
		  return (validatePortInput()==null);
	}
	
	/**
	 * Return the user-entered port number
	 */
	public int getPortNumber()
	{
		Integer iPort = null;
		if (textPort == null)
		{
		  iPort =  new Integer(0);
		}
		else
		{
			String sPort = textPort.getText().trim();
			try 
			{
				iPort = new Integer(sPort);
			} 
			catch (Exception exc) 
			{
				iPort = new Integer(0);
			}
		}
	    return iPort.intValue();
	}

  	/**
	 * Validate port value as it is typed
	 */	
	protected SystemMessage validatePortInput() 
	{	
		this.clearErrorMessage();
	    errorMessage = getPortValidator().validate(textPort.getText().trim());
	    if (errorMessage != null)
		  setErrorMessage(errorMessage);		
		setPageComplete(errorMessage==null);
		return errorMessage;		
	}
	
	/**
	 * Return true if the page is complete, so to enable Finish.
	 * Called by wizard framework.
	 */
	public boolean isPageComplete()
	{
		if (textPort == null)
		  return true;
		else
		  return (textPort.getText().trim().length()>0);
	}

}
/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType 
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty() 
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * David McKnight   (IBM)        - [226574] don't show encoding if no subsystem supports it
 * David McKnight   (IBM)        - [252708] Saving Profile Job happens when not changing Property Values on Connections
 * Noriaki Takatsu  (IBM)        - [332393] Default encoding is removed in System Connection property page
 ********************************************************************************/

package org.eclipse.rse.internal.ui.propertypages;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.ui.ISystemConnectionFormCaller;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemConnectionForm;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.propertypages.SystemBasePropertyPage;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


/**
 * The property page for connection properties
 * The plugin.xml file registers this for objects of class org.eclipse.rse.rse.model.IHost
 */
public class SystemConnectionPropertyPage extends SystemBasePropertyPage
       implements ISystemMessageLine, ISystemConnectionFormCaller
{
	
    protected SystemConnectionForm form;    
    protected String               parentHelpId;	
	
	/**
	 * Constructor for SystemConnectionPropertyPage
	 */
	public SystemConnectionPropertyPage()
	{
		super();
		parentHelpId = RSEUIPlugin.HELPPREFIX + "pcon0000"; //$NON-NLS-1$
		form = new SystemConnectionForm(this, this);
	}
	/**
	 * Create the page's GUI contents.
	 */
	protected Control createContentArea(Composite parent)
	{
		// prepare input data
    	IHost conn = (IHost)getElement();
		form.initializeInputFields(conn);
		
		// only add encoding fields if needed for this connection
		ISubSystem[] sses = conn.getSubSystems();
		boolean addEncodingFields = false;
		for (int i = 0; i < sses.length && !addEncodingFields; i++){
			ISubSystem ss = sses[i];
			addEncodingFields = ss.getSubSystemConfiguration().supportsEncoding(conn);
		}
		
		if (addEncodingFields){
			// add encoding fields
			form.addDefaultEncodingFields();
		}
		
		// create validators
    	ISystemValidator connectionNameValidators[] = new ISystemValidator[1];
    	connectionNameValidators[0] = SystemConnectionForm.getConnectionNameValidator(conn);    	
	    form.setConnectionNameValidators(connectionNameValidators);				
        // create content area
        Control c = form.createContents(parent,true, parentHelpId);
        // set focus
        form.getInitialFocusControl().setFocus();
		SystemWidgetHelpers.setCompositeHelp(parent,  parentHelpId);
        return c;
	}
		
	/**
	 * Get the input connection object
	 */
	protected IHost getConnection()
	{
		return (IHost)getElement();
	}

	private boolean hasConnectionChanged(IHost conn){
		if (!compareStrings(conn.getName(), form.getConnectionName()) ||
				!compareStrings(conn.getHostName(), form.getHostName()) ||
			    !compareStrings(conn.getDescription(), form.getConnectionDescription()) ||
			    !compareStrings(conn.getDefaultUserId(), form.getDefaultUserId())){
			return true;
		}
		return false;	    			  
	}
	
	private boolean compareStrings(String str1, String str2){
		if (str1 == null || str1.length() == 0)
			return (str2 == null || str2.length() == 0);
		else 
			return (str2 == null || str2.length() == 0) ? false: str1.equals(str2);
	}
	
	/**
	 * Called by parent when user presses OK
	 */
	public boolean performOk()
	{
		boolean okToClose = verifyPageContents();
		if (okToClose)
		{
		  IHost conn = (IHost)getElement();
		  ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		  
		  if (hasConnectionChanged(conn)){			  
			  sr.updateHost( conn, conn.getSystemType(), form.getConnectionName(),form.getHostName(),
			                       form.getConnectionDescription(), form.getDefaultUserId(),
			                       form.getUserIdLocation() );
		  }

		  // update encoding
		  String encoding = form.getDefaultEncoding();
		  boolean isRemoteEncoding = form.isEncodingRemoteDefault();
		  
		  String currentEncoding = conn.getDefaultEncoding(false);
		  		  		  
		  // user set encoding
		  if (!isRemoteEncoding && encoding != null && !encoding.equals(currentEncoding)) {
			  conn.setDefaultEncoding(encoding, false);
		  }
		  // remote default encoding
		  else if (currentEncoding != null && !encoding.equals(currentEncoding)){
			  // remove user encoding from host property first
			  conn.setDefaultEncoding(null, false);
			  // remove default remote encoding to indicate to get from remote system
			  conn.setDefaultEncoding(null, true);
		  }
		  
		  boolean offlineSelection = form.isWorkOffline();
		  if (offlineSelection != conn.isOffline())
		  {
		   	  // offline status has changed
			  if (!offlineSelection)
			  {
				  // offline going online
				  sr.setHostOffline(conn, false);
			  }
			  else
			  {
				  // these need to be set before calling disconnect so the iSeires subsystems know not
				  // to collapse 
				  sr.setHostOffline(conn, true);
							
				  // online going offline, disconnect all subsystems
				  ISubSystem[] subsystems = sr.getSubSystems(conn);
				  if (subsystems != null)
				  {
					  boolean cancelled = false;				
					  for (int i = 0; i < subsystems.length && !cancelled; i++)
					  {
						  try 
						  {
							  subsystems[i].disconnect(false);
						  } catch (InterruptedException e) {
							  // user cancelled disconnect
							  cancelled = true;
						  } catch (Exception e) {
							  SystemBasePlugin.logError("SystemConnectionPropertyPage.performOk", e); //$NON-NLS-1$
						  }
					  }
				  }
				
				  // check that everything was disconnedted okay and this is not the local connection
				  if(sr.isAnySubSystemConnected(conn) && !conn.getSystemType().isLocal())
				  {
					  // backout changes, likely because user cancelled the disconnect
					  sr.setHostOffline(conn, false);
					  okToClose = false;
				  }
			  }
		  }
		}
		return okToClose;
	}

    /**
     * Validate all the widgets on the page
	 * <p>
	 * Subclasses should override to do full error checking on all
	 *  the widgets on the page.
     */
    protected boolean verifyPageContents()
    {
    	return form.verify(true);
    }
	

	
    // ----------------------------------------
    // CALLBACKS FROM SYSTEM CONNECTION FORM...
    // ----------------------------------------
    /**
     * Event: the user has selected a system type.
     */
    public void systemTypeSelected(IRSESystemType systemType, boolean duringInitialization)
    {
    }
	
}
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
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.ui.ISystemConnectionFormCaller;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemConnectionForm;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


/**
 * The property page for connection properties
 * The plugin.xml file registers this for objects of class com.ibm.etools.systems.model.SystemConnection
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
		RSEUIPlugin sp = RSEUIPlugin.getDefault();

		parentHelpId = RSEUIPlugin.HELPPREFIX + "pcon0000";
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

	/**
	 * Called by parent when user presses OK
	 */
	public boolean performOk()
	{
		boolean okToClose = verifyPageContents();
		if (okToClose)
		{
		  IHost conn = (IHost)getElement();
		  ISystemRegistry sr = RSEUIPlugin.getDefault().getSystemRegistry();
		  sr.updateHost( getShell(),conn,conn.getSystemType(),form.getConnectionName(),
		                       form.getHostName(), form.getConnectionDescription(),
		                       form.getDefaultUserId(), form.getUserIdLocation() );

		  
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
							  subsystems[i].disconnect(getShell(), false);
						  } catch (InterruptedException e) {
							  // user cancelled disconnect
							  cancelled = true;
						  } catch (Exception e) {
							  SystemBasePlugin.logError("SystemConnectionPropertyPage.performOk", e);
						  }
					  }
				  }
				
				  // check that everything was disconnedted okay and this is not the local connection
				  if(sr.isAnySubSystemConnected(conn) && !IRSESystemType.SYSTEMTYPE_LOCAL.equals(conn.getSystemType()))
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
    public void systemTypeSelected(String systemType, boolean duringInitialization)
    {
    }
	
}
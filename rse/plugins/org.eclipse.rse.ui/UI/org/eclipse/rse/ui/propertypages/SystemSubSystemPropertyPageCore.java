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
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.util.ISubsystemConfigurationAdapter;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


/**
 * The property page for core subsystem properties.
 * The plugin.xml file registers this for objects of class com.ibm.etools.systems.subsystems.SubSystem
 */
public class SystemSubSystemPropertyPageCore extends SystemBasePropertyPage
       implements  ISystemMessages, ISystemMessageLine//, ISystemMessageLineTarget
{
	
	private ISystemSubSystemPropertyPageCoreForm form = null;
	
	/**
	 * Constructor
	 */
	public SystemSubSystemPropertyPageCore()
	{
		super();
		
	}
	/**
	 * Create the page's GUI contents.
	 */
	protected Control createContentArea(Composite parent)
	{
	    Object element = getElement();
	    if (element instanceof ISubSystem)
	    {
	    	ISubSystemConfiguration factory = ((ISubSystem)element).getSubSystemConfiguration();
	    	ISubsystemConfigurationAdapter adapter = (ISubsystemConfigurationAdapter)factory.getAdapter(ISubsystemConfigurationAdapter.class);
	        form = adapter.getSubSystemPropertyPageCoreFrom(factory, this, this);
	    }
	    else
	    {
	        form = new SystemSubSystemPropertyPageCoreForm(this, this);
	    }
		Control c = form.createContents(parent, getElement(), getShell());
		SystemWidgetHelpers.setCompositeHelp(parent, RSEUIPlugin.HELPPREFIX + "psubs0000");		
        return c;
	}
	
	/**
	 * Called by parent when user presses OK
	 */
	public boolean performOk()
	{
		if (super.performOk())
		  return form.performOk();
		else
		  return false;
	}
    /**
     * Validate all the widgets on the page
	 * <p>
	 * Subclasses should override to do full error checking on all
	 *  the widgets on the page.
     */
    public boolean verifyPageContents()
    {
    	return form.verifyFormContents();
    }

}
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

package org.eclipse.rse.ui.propertypages;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;



/**
 * This is a base class to simplify the creation of remote object property pages supplied via the
 * org.eclipse.rse.core.propertyPages extension point. 
 * <p>
 * This class extends {@link SystemBasePropertyPage} and so inherits the benefits of that class.<br>
 * <b>To get these benefits though, you must override {@link #createContentArea(Composite)} versus the
 * usual createContents(Composite) method.</b>.
 * <p>
 * The benefits of this class are:</p>
 * <ul>
 *   <li>From {@link SystemBasePropertyPage}: Adds a message line and {@link org.eclipse.rse.ui.messages.ISystemMessageLine} message methods. 
 *   <li>From {@link SystemBasePropertyPage}: Automatically assigns mnemonics to controls on this page, simplifying this common task. See {#wantMnemonics()}. 
 *   <li>From {@link SystemBasePropertyPage}: For pages with input controls, simplifies the page validation burden: only one method need be overridden: {@link #verifyPageContents()}
 *   <li>From {@link SystemBasePropertyPage}: If no Default and Apply buttons wanted, the area reserved for this is removed, removing extra white space.
 *   <li>Supplies helper method {@link #getRemoteObject()} for querying the selected remote object.
 *   <li>Supplies helper methods getRemoteObjectXXX() for querying the attributes of the selected remote object.
 *   <li>Supplies helper methods to query the {@link #getSubSystem() subsystem} and {@link #getSystemConnection() connection} containing the selected remote object.
 * </ul>
 * If your property page is for a file-system file or folder, use {@link SystemAbstractRemoteFilePropertyPageExtensionAction}.
 */
public abstract class SystemAbstractPropertyPageExtensionAction 
       //extends PropertyPage implements IWorkbenchPropertyPage 
       extends SystemBasePropertyPage implements IWorkbenchPropertyPage 
{
    protected static final Object[] EMPTY_ARRAY = new Object[0];
    
	/**
	 * Constructor 
	 */
	public SystemAbstractPropertyPageExtensionAction() 
	{
		super();
		// ensure the page has no special buttons
		noDefaultAndApplyButton();
	}
	
	// ------------------------
	// OVERRIDABLE METHODS...
	// ------------------------
	/**
	 * <i><b>Abstract</b>. You must override.</i><br>
	 * This is where child classes create their content area versus createContent,
	 *  in order to have the message line configured for them and mnemonics assigned.
	 */
	protected abstract Control createContentArea(Composite parent);
	
    /**
	 * <i>You may override if your page has input fields. By default returns true.</i><br>
     * Validate all the widgets on the page. Based on this, the Eclipse framework will know whether
     *  to veto any user attempt to select another property page from the list on the left in the 
     *  Properties dialog.
	 * <p>
	 * Subclasses should override to do full error checking on all the widgets on the page. Recommendation:<br>
	 * <ul>
	 * <li>If an error is detected, issue a {@link org.eclipse.rse.ui.messages.SystemMessage} via {@link #setErrorMessage(SystemMessage)} or text message via {@link #setErrorMessage(String)}.
	 * <li>If no errors detected, clear the message line via {@link #clearErrorMessage()}
	 * </ul>
	 * 
	 * @return true if there are no errors, false if any errors were found.
     */
    protected boolean verifyPageContents()
    {
    	return true;
    }
	
	// ---------------------------------------------
	// CONVENIENCE METHODS FOR SUBCLASSES TO USE...
	// ---------------------------------------------
	/**
	 * Retrieve the input remote object
	 * @see #getRemoteAdapter(Object)
	 */
	public Object getRemoteObject()
	{
		return getElement();
	}
	/**
	 * Retrieve the adapter of the input remote object as an ISystemRemoteElementAdapter object, for convenience.
	 * Will be null if there is nothing selected
	 */
	public ISystemRemoteElementAdapter getRemoteAdapter()
	{
		return getRemoteAdapter(getElement());
	}
    /**
     * Returns the implementation of ISystemRemoteElementAdapter for the given
     * object.  Returns null if this object does not adaptable to this.
     */
    protected ISystemRemoteElementAdapter getRemoteAdapter(Object o) 
    {
    	if (!(o instanceof IAdaptable)) 
          return (ISystemRemoteElementAdapter)Platform.getAdapterManager().getAdapter(o,ISystemRemoteElementAdapter.class);
    	return (ISystemRemoteElementAdapter)((IAdaptable)o).getAdapter(ISystemRemoteElementAdapter.class);
    }

    /**
     * Returns the name of the input remote object
     */
    public String getRemoteObjectName()
    {
    	return getRemoteAdapter().getName(getRemoteObject());
    }
    /**
     * Returns the id of the subsystem factory of the input remote object.
     */
    public String getRemoteObjectSubSystemFactoryId()
    {
    	return getRemoteAdapter().getSubSystemFactoryId(getRemoteObject());
    }
    /**
     * Returns the type category of the input remote object
     */
    public String getRemoteObjectTypeCategory()
    {
    	return getRemoteAdapter().getRemoteTypeCategory(getRemoteObject());
    }
    /**
     * Returns the type of the input remote object
     */
    public String getRemoteObjectType()
    {
    	return getRemoteAdapter().getRemoteType(getRemoteObject());
    }
    /**
     * Returns the subtype of the input remote object
     */
    public String getRemoteObjectSubType()
    {
    	return getRemoteAdapter().getRemoteSubType(getRemoteObject());
    }
    /**
     * Returns the sub-subtype of the input remote object
     */
    public String getRemoteObjectSubSubType()
    {
    	return getRemoteAdapter().getRemoteSubSubType(getRemoteObject());
    }
    /**
     * Returns the subsystem from which the input remote object was resolved
     */
    public ISubSystem getSubSystem()
    {
    	return getRemoteAdapter().getSubSystem(getRemoteObject());
    }
    /**
     * Returns the subsystem factory which owns the subsystem from which the input remote object was resolved
     */
    public ISubSystemConfiguration getSubSystemFactory()
    {
    	ISubSystem ss = getSubSystem();
    	if (ss != null)
    	  return ss.getSubSystemConfiguration();
    	else 
    	  return null;
    }    
    
    /**
     * Return the SystemConnection from which the selected remote objects were resolved
     */
    public IHost getSystemConnection()
    {
    	IHost conn = null;
    	ISubSystem ss = getRemoteAdapter().getSubSystem(getRemoteObject());
    	if (ss != null)
    	  conn = ss.getHost();
    	return conn;
    }
  

      
	/**
	 * Debug method to print out details of given selected object, in a composite GUI widget...
	 */
	protected Composite createTestComposite(Composite parent) 
	{
		// Inner composite
		int nbrColumns = 2;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);	
		
        //System.out.println("Remote object name................: " + getRemoteObjectName());
        //System.out.println("Remote object subsystem factory id: " + getRemoteObjectSubSystemFactoryId());   
        //System.out.println("Remote object type category.......: " + getRemoteObjectTypeCategory());
        //System.out.println("Remote object type ...............: " + getRemoteObjectType());
        //System.out.println("Remote object subtype ............: " + getRemoteObjectSubType());
        //System.out.println("Remote object subsubtype .........: " + getRemoteObjectSubSubType());
        
		SystemWidgetHelpers.createLabel(composite_prompts, "Remote object name: ");
		SystemWidgetHelpers.createLabel(composite_prompts, checkForNull(getRemoteObjectName()));

		SystemWidgetHelpers.createLabel(composite_prompts, "Remote object subsystem factory id: ");
		SystemWidgetHelpers.createLabel(composite_prompts, checkForNull(getRemoteObjectSubSystemFactoryId()));

		SystemWidgetHelpers.createLabel(composite_prompts, "Remote object type category: ");
		SystemWidgetHelpers.createLabel(composite_prompts, checkForNull(getRemoteObjectTypeCategory()));

		SystemWidgetHelpers.createLabel(composite_prompts, "Remote object type: ");
		SystemWidgetHelpers.createLabel(composite_prompts, checkForNull(getRemoteObjectType()));

		SystemWidgetHelpers.createLabel(composite_prompts, "Remote object subtype: ");
		SystemWidgetHelpers.createLabel(composite_prompts, checkForNull(getRemoteObjectSubType()));

		SystemWidgetHelpers.createLabel(composite_prompts, "Remote object subsubtype: ");
		SystemWidgetHelpers.createLabel(composite_prompts, checkForNull(getRemoteObjectSubSubType()));
		
		return composite_prompts;
	}
	
	/**
	 * Check for null, and if so, return ""
	 */
	private String checkForNull(String input)
	{
	   if (input == null)
	     return "";
	   else  
	     return input;
	}
	
}
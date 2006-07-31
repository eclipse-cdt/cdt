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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.ISystemUserIdConstants;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.util.ISubsystemConfigurationAdapter;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorPortInput;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;



/**
 * Adapter for displaying SubSystem objects in tree views.
 * These are children of SystemConnection objects
 */
public class SystemViewSubSystemAdapter extends AbstractSystemViewAdapter 
       implements ISystemViewElementAdapter, ISystemPropertyConstants, ISystemUserIdConstants
{
	protected String translatedType;
    // for reset property support
    private String  original_portData;
	private SystemInheritablePropertyData original_userIdData = new SystemInheritablePropertyData();        
	//private SystemInheritablePropertyData original_portData = new SystemInheritablePropertyData();        
	private TextPropertyDescriptor propertyPortDescriptor;
    private boolean changed_userId, changed_port;
    private boolean port_editable = true;
	// -------------------
	// property descriptors
	// -------------------
	private PropertyDescriptor[] propertyDescriptorArray = null;	
	//private SystemInheritablePropertyData portData = new SystemInheritablePropertyData();		
	//private SystemInheritableTextPropertyDescriptor portDescriptor;
	private SystemInheritablePropertyData userIdData = new SystemInheritablePropertyData();	
	private SystemInheritableTextPropertyDescriptor userIdDescriptor = null;
	
	/**
	 * Returns any actions that should be contributed to the popup menu
	 * for the given subsystem object.
	 * Calls the method getActions on the subsystem's factory, and places
	 * all action objects returned from the call, into the menu.
	 * @param menu The menu to contribute actions to
	 * @param selection The window's current selection.
	 * @param shell Shell of viewer
	 * @param menuGroup recommended menu group to add actions to. If added to another group, you must be sure to create that group first.
	 */
	public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup)
	{
		if (selection.size() != 1)
		  return; // does not make sense adding unique actions per multi-selection
		Object element = selection.getFirstElement();	
		ISubSystem ss = (ISubSystem)element;
		ISubSystemConfiguration ssFactory = RSEUIPlugin.getDefault().getSystemRegistry().getSubSystemConfiguration(ss);
		ISubsystemConfigurationAdapter adapter = (ISubsystemConfigurationAdapter)ssFactory.getAdapter(ISubsystemConfigurationAdapter.class);
			
		IAction[] actions = adapter.getSubSystemActions(ssFactory, ss,shell);
		if (actions != null)
		{
		  for (int idx=0; idx<actions.length; idx++)
		  {
		  	 IAction action = actions[idx];		
		  	 menu.add(menuGroup, action);
		  }
		}
	}
	
	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 * @param element The element for which an image is desired
	 */
	public ImageDescriptor getImageDescriptor(Object element)
	{
		//System.out.println("INSIDE GETIMAGEDESCRIPTOR FOR SUBSYSTEM VIEW ADAPTER: "+element);				
		ISubSystem ss = (ISubSystem)element;
		ISubSystemConfiguration ssFactory = RSEUIPlugin.getDefault().getSystemRegistry().getSubSystemConfiguration(ss);
		if (ssFactory != null)
		{
		  if (ss.isConnected())
		    return ssFactory.getLiveImage();
		  else
		    return ssFactory.getImage();
		}
		else
		{
	      SystemBasePlugin.logWarning("Unexpected error: ssFactory is null for ss " + ss.getName());
	      return null;
		}
	}
	
	/**
	 * Return the label for this object. Uses getName() on the subsystem object.
	 */
	public String getText(Object element)
	{
		return ((ISubSystem)element).getName();
	}
	/**
	 * Return the name of this object, which may be different than the display text ({#link #getText(Object)}.
	 * <p>
	 * Called by common rename and delete actions.
	 */
	public String getName(Object element)
	{
		return ((ISubSystem)element).getName();
	}
	/**
	 * Return the absolute name, versus just display name, of this object. 
	 * Returns profileName.connectionName.subsystemName;
	 */
	public String getAbsoluteName(Object element)
	{
		ISubSystem ss = (ISubSystem)element;
		return ss.getSystemProfileName() + "." + ss.getHostAliasName() + "." + ss.getName();
	}		
	/**
	 * Return the type label for this object
	 */
	public String getType(Object element)
	{
		if (translatedType == null)
          translatedType = SystemViewResources.RESID_PROPERTY_SUBSYSTEM_TYPE_VALUE;
		return translatedType;
	}	
	
	
	/**
	 * Return the parent of this object. This is a connection object.
	 */
	public Object getParent(Object element)
	{
		//System.out.println("INSIDE GETPARENT FOR SUBSYSTEM VIEW ADAPTER: "+element);		
		ISubSystem ss = (ISubSystem)element;		
		return RSEUIPlugin.getDefault().getSystemRegistry().getHost(ss.getSystemProfile(),ss.getHostAliasName());
	}
	
	/**
	 * Return the children of this object
	 */
	public Object[] getChildren(Object element)
	{
		//System.out.println("INSIDE GETCHILDREN FOR SUBSYSTEM VIEW ADAPTER: "+element);		
		ISubSystem ss = (ISubSystem)element;
		Object[] children = ss.getChildren();		
		return children;
	}
		
	/**
	 * Return true if this object has children
	 */
	public boolean hasChildren(Object element)
	{
		//System.out.println("INSIDE HASCHILDREN FOR SUBSYSTEM VIEW ADAPTER: "+element);		
		ISubSystem ss = (ISubSystem)element;
		return ss.hasChildren();
	}

    // ----------------------------------
    // Property sheet supplier methods...
    // ----------------------------------
    
	/**
	 * Returns the current collection of property descriptors for connection objects.
	 * @return an array containing all descriptors.  
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() 
	{
		if (propertyDescriptorArray == null)
		{
		  PropertyDescriptor[] defaultProperties = (PropertyDescriptor[])getDefaultDescriptors();
		  propertyDescriptorArray = new PropertyDescriptor[defaultProperties.length + 4];
		  int idx = 0;
		  for (idx = 0; idx < defaultProperties.length; idx++)
		    propertyDescriptorArray[idx] = defaultProperties[idx];
		    
		  // add our unique property descriptors...
		  //idx = defaultProperties.length; assertion
	      
	      // user id	      
	      //propertyDescriptorArray[idx] = new TextPropertyDescriptor(ISystemPropertyConstants.P_USERID, 
	      //                                                      SystemViewResources.RESID_PROPERTY_USERID_LABEL));
	      userIdDescriptor =
	           new SystemInheritableTextPropertyDescriptor(ISystemPropertyConstants.P_USERID, 
	                                                       SystemViewResources.RESID_PROPERTY_USERID_LABEL);

	      userIdDescriptor.setToggleButtonToolTipText(SystemResources.RESID_SUBSYSTEM_USERID_INHERITBUTTON_TIP);
	      userIdDescriptor.setEntryFieldToolTipText(SystemResources.RESID_SUBSYSTEM_USERID_TIP);
          propertyDescriptorArray[idx] = userIdDescriptor;
	      propertyDescriptorArray[idx].setDescription(SystemViewResources.RESID_PROPERTY_USERID_TOOLTIP);
	      
	      // port
	      propertyPortDescriptor = new TextPropertyDescriptor(ISystemPropertyConstants.P_PORT, 	      
	                                                            SystemViewResources.RESID_PROPERTY_PORT_LABEL);
	      propertyPortDescriptor.setValidator(new ValidatorPortInput());
          propertyDescriptorArray[++idx] = propertyPortDescriptor;	                                                                      
	      //propertyDescriptorArray[++idx] = getPortDescriptor();

	      // connected
	      propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_IS_CONNECTED, SystemViewResources.RESID_PROPERTY_CONNECTED_LABEL, SystemViewResources.RESID_PROPERTY_CONNECTED_TOOLTIP);

          // vrm
	      propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_VRM, SystemViewResources.RESID_PROPERTY_VRM_LABEL, SystemViewResources.RESID_PROPERTY_VRM_TOOLTIP);          
		}		
		return propertyDescriptorArray;
	}	
	/**
	 * Return our unique property descriptors
	 */
	protected org.eclipse.ui.views.properties.IPropertyDescriptor[] internalGetPropertyDescriptors()
	{
		return null;
	}	
	
	/**
	 * Create (first time), configure and return the property descriptor for the port number
	 *
	private SystemInheritableTextPropertyDescriptor getPortDescriptor()
	{
		if (portDescriptor == null)
		{
	      SystemViewPlugin plugin = SystemViewPlugin.getDefault();
	      RSEUIPlugin sp = RSEUIPlugin.getDefault();			
	      portDescriptor =
	           new SystemInheritableTextPropertyDescriptor(ISystemPropertyConstants.P_PORT, 
	                                                       SystemViewResources.RESID_PROPERTY_PORT_LABEL));	                                                      
	      portDescriptor.setToggleButtonToolTipText(SystemResources.RESID_SUBSYSTEM_PORT_INHERITBUTTON_TIP));
	      portDescriptor.setEntryFieldToolTipText(SystemResources.RESID_SUBSYSTEM_PORT_TIP));
	      portDescriptor.setDescription(SystemViewResources.RESID_PROPERTY_PORT_DESCRIPTION));	      
		}
		return portDescriptor;
	}*/

    /**
     * Set the values in the userIdPropertyData object that drives the userId property sheet widget
     */	
	private SystemInheritablePropertyData setUserIdPropertyData(SystemInheritablePropertyData data, ISubSystem subsys)
	{
		String localUserId = subsys.getLocalUserId();
		data.setLocalValue(localUserId);
		String parentUserId = subsys.getHost().getDefaultUserId();
		data.setInheritedValue(parentUserId);
		data.setIsLocal((localUserId!=null)&&(localUserId.length()>0));

	    // DY:  Defect 42735, check if user has temporarilly overrode the userid 
	    // for this connection via the signon dialog
	    if (subsys.isConnected()) 
	    {
	    	String connectedId = subsys.getConnectorService().getUserId();
	    	boolean connectedIdIsNull = (connectedId == null); // caused crash! Happens on Local subsystems. Phil.
	    	if (data.getIsLocal() && !connectedIdIsNull && !connectedId.equals(localUserId)) 
	    	{
    			data.setLocalValue(connectedId);
    			data.setIsLocal(true);
	    	}
	    	else if (!connectedIdIsNull && !connectedId.equals(parentUserId)) 
	    	{
				data.setLocalValue(connectedId);
				data.setIsLocal(true);
	    	}
	    }
	    
		return data;
	}

    /**
     * Set the values in the portPropertyData object that drives the port property sheet widget
     *
	private SystemInheritablePropertyData setPortPropertyData(SystemInheritablePropertyData data, SubSystem subsys)
	{
		Integer localPort = subsys.getPort();
		int iPort = 0;
		if (localPort != null)
		  iPort = localPort.intValue();
		if (iPort > 0)
		  data.setLocalValue(localPort.toString());
		else
		  data.setLocalValue(null); // clear history
	    SubSystemConfiguration ssFactory = subsys.getParentSubSystemConfiguration();
	    boolean notApplicable = (!ssFactory.isPortEditable() && (iPort <= 0));
		if (!notApplicable)
		{
		  data.setInheritedValue("0");			
	      data.setInheritedDisplayString(SystemResources.RESID_PORT_DYNAMICSELECT));		  		  
		  data.setIsLocal(iPort != 0);
		}
		data.setNotApplicable(notApplicable);
		getPortDescriptor().setEditable(!notApplicable);		
		//data.printDetails();
		return data;
	}
	*/

    private String getPortString(ISubSystem ss)
    {
          //return getPortPropertyData(portData, ss);			
          int iPort = ss.getConnectorService().getPort();
	      ISubSystemConfiguration ssFactory = ss.getSubSystemConfiguration();
	      boolean notApplicable = (!ssFactory.isPortEditable() && (iPort <= 0));          
	      if (notApplicable)
	        return getTranslatedNotApplicable();
	      else
	      {
	      	return Integer.toString(iPort);
	      }
    }
    
	/**
	 * Returns the current value for the named property.
	 * The parent handles P_TEXT and P_TYPE only, and we augment that here.
	 * @param	property the name of the property as named by its property descriptor
	 * @return  the current value of the property
	 */
	public Object getPropertyValue(Object property) 
	{
		String name = (String)property;		
	    ISubSystem ss = (ISubSystem)propertySourceInput;   					
		if (name.equals(P_USERID))
          return setUserIdPropertyData(userIdData, ss);
		else if (name.equals(P_PORT))
		  return getPortString(ss);
		else if (name.equals(P_VRM))
		{
			IConnectorService system = ss.getConnectorService();
			if (system != null)
			{
				String vrm = system.getVersionReleaseModification();
				if (vrm != null)
				  return vrm;
				else
				  return getTranslatedNotAvailable();
			}
			else
			  return getTranslatedNotAvailable();
		}
		else if (name.equals(P_IS_CONNECTED))
		{
			// yantzi: artemis 6.0, offline support
			if (ss.getHost().isOffline())
			{
				// connection is offline
				return SystemResources.RESID_OFFLINE_LABEL;
			}
			
			IConnectorService system = ss.getConnectorService();
			boolean connected = false;
			if (system == null)
			{
			  System.out.println("SystemViewSubSystemAdapter: Error! system is null for subsystem "+ss.getClass().getName());
			  SystemBasePlugin.logError("SystemViewSubSystemAdapter: Error! system is null for subsystem "+ss.getClass().getName(), null);
			}
			else  
			  connected = system.isConnected();
		    return connected ? getTranslatedYes() : getTranslatedNo();
		}
		else
 		  return super.getPropertyValue(name);
	}	
	
	/**
	 * Returns itself
	 */
	public ISubSystem getSubSystem(Object element)
	{
		if (element instanceof ISubSystem)
		  return (ISubSystem)element;
		else
		  return null;	
	}
	
	/**
	 * Return our unique property values
	 */
	public Object internalGetPropertyValue(Object key)
	{
		return null;
	}	
		
    // because this node has some editable properties, these overrides of our
    // parent class are needed as callbacks from the PropertySheet window.
	/**
	 * Set input object for property source queries. This is called by the
	 * SystemViewAdaptorFactory before returning this adapter object.
	 */
	public void setPropertySourceInput(Object propertySourceInput)
	{
		if (this.propertySourceInput == propertySourceInput)
		  return;
		super.setPropertySourceInput(propertySourceInput);
	    ISubSystem ss = (ISubSystem)propertySourceInput;   			
        ISubSystemConfiguration ssFactory = ss.getSubSystemConfiguration();	    
		original_userIdData = setUserIdPropertyData(original_userIdData,ss);		
		//original_portData = setPortPropertyData(original_portData,ss);		
		original_portData = getPortString(ss);
        changed_userId = changed_port = false;
        if (userIdDescriptor != null)
          userIdDescriptor.setValidator((ICellEditorValidator)ssFactory.getUserIdValidator());
        //getPortDescriptor().setValidator((ICellEditorValidator)ssFactory.getPortValidator());
        if (propertyPortDescriptor != null)
	    {
          propertyPortDescriptor.setValidator((ICellEditorValidator)ssFactory.getPortValidator());
	    }
        ss.getConnectorService().getPort();
	    port_editable = ssFactory.isPortEditable();
	}
	
	/**
	 * Returns whether the property value has changed from the default.
	 * Only applicable for editable properties.
	 * Called by PropertySheet viewer when user presses reset.
	 * @return	<code>true</code> if the value of the specified property has changed
	 *			from its original default value; <code>false</code> otherwise.
	 */
	public boolean isPropertySet(Object propertyObject) 
	{
		String property = (String)propertyObject;    			
		boolean changed = false;
	    if (property.equals(P_USERID))
	      changed = changed_userId;
	    else if (property.equals(P_PORT))
	      changed = changed_port && port_editable;	    
		return changed; 
	}	

    /**
     * Change the subsystem user Id value
     */
    private void updateUserId(ISubSystem subsys, SystemInheritablePropertyData data)
    {
    	//int whereToUpdate = USERID_LOCATION_SUBSYSTEM;
    	String userId = data.getLocalValue(); // will be "" if !data.getIsLocal(), which results in wiping out local override
        ISubSystemConfiguration ssFactory = subsys.getSubSystemConfiguration();	    
        // unlike with connection objects, we don't ever allow the user to change the parent's
        // userId value, even if it is empty, when working with subsystems. There is too much 
        // ambiquity as the parent could be the connnection or the user preferences setting for this
        // system type. Because of this decision, we don't need to tell updateSubSystem(...) where
        // to update, as it always the local subsystem.
	    ssFactory.updateSubSystem((Shell)null, subsys, true, userId, false, subsys.getConnectorService().getPort()); 		  		                      
    }
    /**
     * Change the subsystem port value
     *
    private void updatePort(SubSystem subsys, SystemInheritablePropertyData data)
    {
    	String port = data.getLocalValue(); // will be "" if !data.getIsLocal(), which results in wiping out local override
    	Integer portInteger = null;
    	if (data.getIsLocal() && (port.length()>0))
          portInteger = new Integer(port); 
    	else
    	  portInteger = new Integer(0);
        SubSystemConfiguration ssFactory = subsys.getParentSubSystemConfiguration();	    
	    ssFactory.updateSubSystem((Shell)null, subsys, false, subsys.getLocalUserId(), true, portInteger); 		  		                      
    }
    */
    /**
     * Change the subsystem port value
     */
    private void updatePort(ISubSystem subsys, String data)
    {
    	if (!port_editable)
    	  return;
        String port = (String)data;
    	Integer portInteger = null;
    	if (port.length()>0)
    	{
    	  try 
    	  {
            portInteger = new Integer(port); 
    	  } 
    	  catch (Exception exc) 
    	  {
    	  	return;
    	  }
    	}
    	else
    	{
    	  portInteger = new Integer(0);
    	}
    	int portInt = portInteger.intValue();
        ISubSystemConfiguration ssFactory = subsys.getSubSystemConfiguration();	    
	    ssFactory.updateSubSystem((Shell)null, subsys, false, subsys.getLocalUserId(), true, portInt); 		  		                      
    }

	
    /**
	 * Called when user selects the reset button in property sheet.
	 */
    public void resetPropertyValue(Object propertyObject)
    {
    	String property = (String)propertyObject;
	    ISubSystem ss = (ISubSystem)propertySourceInput;
        ss.getSubSystemConfiguration();	    
	    if (property.equals(P_USERID))
	    {
		  updateUserId(ss, original_userIdData);	      
	      changed_userId = false;
	    }
	    else if (property.equals(P_PORT))
	    {
	      //updatePort(ss, original_portData);
		  updatePort(ss, original_portData);
	      changed_port = false;
	    }
    }   
    /**
	 * Called when user changes property via property sheet.
	 */
    public void setPropertyValue(Object property, Object value)
    {
		String name = (String)property;    	
	    ISubSystem ss = (ISubSystem)propertySourceInput;
        ss.getSubSystemConfiguration();
	    //System.out.println("inside setPropVal: " + property + ", value: " + value);
	    if (name.equals(P_USERID))
	    {
		  updateUserId(ss, (SystemInheritablePropertyData)value);	      
	      changed_userId = true;
	    }
	    else if (name.equals(P_PORT))
	    {
	      //System.out.println("inside setPropVal: " + property + ", value: " + value);
		  //updatePort(ss, (SystemInheritablePropertyData)value);	      
		  updatePort(ss, (String)value);
	      changed_port = true;
	    }
    }  	

	/**
	 * Override of {@link AbstractSystemViewAdapter#testAttribute(Object, String, String)}. We add
	 *  one more attribute for subsystems:
	 * <ol>
	 *  <li>name="serverLaunchPP". Returns "true" if the given subsystem supports the Server Launch Settings
	 *      property page, which is determined by calling it's factory's {@link ISubSystemConfiguration#supportsServerLaunchProperties()} method.
	 * </ol>
	 *  
	 * This property is used to filter the existence of the Server Launch Settings property page.
	 * 
	 * @see org.eclipse.ui.IActionFilter#testAttribute(Object, String, String)
	 */
	public boolean testAttribute(Object target, String name, String value)
	{
		if (target instanceof ISubSystem)
		{
			if (name.equalsIgnoreCase("serverLaunchPP"))
			{
				ISubSystem ss = (ISubSystem)target;
				boolean supports = ss.getSubSystemConfiguration().supportsServerLaunchProperties(ss.getHost());
				return supports ? value.equals("true") : value.equals("false");
			}
			else if (name.equalsIgnoreCase("envVarPP"))
			{
				/** FIXME can't access specific subsystems from core anymore
				boolean supports = false;
				if (ss instanceof IRemoteFileSubSystem)
					supports = ((IRemoteFileSubSystemConfiguration)ss.getParentSubSystemConfiguration()).supportsEnvironmentVariablesPropertyPage();
				else
					supports = ((IRemoteCmdSubSystemConfiguration)ss.getParentSubSystemConfiguration()).supportsEnvironmentVariablesPropertyPage();
				*/
				boolean supports = false;
				return supports ? value.equals("true") : value.equals("false");
			}
			else if (name.equalsIgnoreCase("isConnectionError"))
			{
				ISubSystem ss = (ISubSystem) target;
				boolean error = ss.isConnectionError();
				return error ? value.equals("true") : value.equals("false");
			}
		}
		return super.testAttribute(target, name, value);
	}
	
	// FOR COMMON DELETE ACTIONS
	/**
	 * Return true if we should show the delete action in the popup for the given element.
	 * If true, then canDelete will be called to decide whether to enable delete or not.
	 */
	public boolean showDelete(Object element)
	{
		return canDelete(element);
	}	
	
	/**
	 * Return true if this object is deletable by the user. If so, when selected,
	 *  the Edit->Delete menu item will be enabled.
	 */
	public boolean canDelete(Object element)
	{
		//System.out.println("INSIDE ISDELETABLE FOR SUBSYSTEM VIEW ADAPTER: "+element);		
		ISubSystem ss = (ISubSystem)element;				
        ISubSystemConfiguration ssFactory = ss.getSubSystemConfiguration();		  	
		return ssFactory.isSubSystemsDeletable();
	}
	
	/**
	 * Perform the delete action.
	 */
	public boolean doDelete(Shell shell, Object element, IProgressMonitor monitor)
	{
		//System.out.println("INSIDE DODELETE FOR SUBSYSTEM VIEW ADAPTER: "+element);		
		ISubSystem ss = (ISubSystem)element;		
		ISystemRegistry sr = RSEUIPlugin.getDefault().getSystemRegistry();								
		sr.deleteSubSystem(ss);
		return true;
	}	
	
	// FOR COMMON RENAME ACTIONS
	/**
	 * Return true if we should show the rename action in the popup for the given element.
	 * If true, then canRename will be called to decide whether to enable delete or not.
	 */
	public boolean showRename(Object element)
	{
		return canRename(element);
	}		
	/**
	 * Return true if this object is renamable by the user. If so, when selected,
	 *  the Rename menu item will be enabled.
	 */
	public boolean canRename(Object element)
	{		
		return canDelete(element); // same rules for both delete and rename
	}
	
	/**
	 * Perform the rename action. Assumes uniqueness checking was done already.
	 */
	public boolean doRename(Shell shell, Object element, String name)
	{
		ISubSystem ss = (ISubSystem)element;
		ISubSystemConfiguration parentSSFactory = ss.getSubSystemConfiguration();
		parentSSFactory.renameSubSystem(ss,name); // renames, and saves to disk
		return true;
	}	
	
	/**
	 * Return a validator for verifying the new name is correct on a rename action.
	 * The default implementation is not to support rename hence this method returns
	 *  null. Override if appropriate.
	 */
    public ISystemValidator getNameValidator(Object element)
    {
    	return null;
    }
	
	// FOR COMMON DRAG AND DROP ACTIONS
	/**
	 * Indicates whether the subsystem can be dragged. 
	 * Can't be used for physical copies but rather 
	 * for views (like the Scratchpad)
	 */
	public boolean canDrag(Object element)
	{
		return true;
	}
	
	/**
	 * Returns the subsystem (no phyiscal operation required to drag and subsystem (because it's local)
	 */
	public Object doDrag(Object element, boolean sameSystemType, IProgressMonitor monitor)
	{
		return element;	
	}
	
			
    

	// ------------------------------------------------------------
	// METHODS FOR SAVING AND RESTORING EXPANSION STATE OF VIEWER...
	// ------------------------------------------------------------

	/**
	 * Return what to save to disk to identify this element in the persisted list of expanded elements.
	 * This just defaults to getName, but if that is not sufficient override it here.
	 */
	public String getMementoHandle(Object element)
	{
		ISubSystem ss = (ISubSystem)element;
		ISubSystemConfiguration ssf = ss.getSubSystemConfiguration();
		return ssf.getId()+"="+ss.getName();
	}
	/**
	 * Return what to save to disk to identify this element when it is the input object to a secondary
	 *  Remote Systems Explorer perspective.
	 */
	public String getInputMementoHandle(Object element)
	{
		Object parent = getParent(element);
		return getAdapter(parent).getInputMementoHandle(parent) + MEMENTO_DELIM + getMementoHandle(element);
	}
	/**
	 * Return a short string to uniquely identify the type of resource. Eg "conn" for connection.
	 * This just defaults to getType, but if that is not sufficient override it here, since that is
	 * a translated string.
	 */
	public String getMementoHandleKey(Object element)
	{
		return ISystemMementoConstants.MEMENTO_KEY_SUBSYSTEM;
	}
    
}
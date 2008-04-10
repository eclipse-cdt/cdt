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
 * David Dykstal (IBM) - 180562: remove implementation of IRSEUserIdConstants
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Xuan Chen        (IBM)        - [160775] [api] rename (at least within a zip) blocks UI thread
 * David Dykstal (IBM) - [217556] remove service subsystem types
 * Martin Oberhuber (Wind River) - [195399] Improve String display for default port 0
 * David McKnight   (IBM)        - [223103] [cleanup] fix broken externalized strings
 * Martin Oberhuber (Wind River) - [218304] Improve deferred adapter loading
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.subsystems.SubSystemConfigurationProxyAdapter;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorPortInput;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;



/**
 * Adapter for displaying SubSystem objects in tree views.
 * These are children of SystemConnection objects
 */
public class SystemViewSubSystemAdapter extends AbstractSystemViewAdapter
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
		// does not make sense adding unique actions per multi-selection
		if (selection.size() == 1) {
			Object element = selection.getFirstElement();
			ISubSystem ss = (ISubSystem)element;
			ISubSystemConfiguration ssFactory = ss.getSubSystemConfiguration();
			ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)ssFactory.getAdapter(ISubSystemConfigurationAdapter.class);
			if (adapter == null) {
				// FIXME Fallback to default SubSystemConfigurationAdapter, such
				// that we get at least the "connect" and other common actions
				// for subsystems before their adapter is loaded. Note that this
				// means that the popular "launch Shell" action will not be
				// available before the rse.shells.ui plugin is loaded; but that
				// should be fixed by contributing that action via plugin.xml,
				// rather than forcing full bundle activation here from the
				// menu. See
				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=226550
				// //adapter = new SubSystemConfigurationAdapter();
				Platform.getAdapterManager().loadAdapter(ssFactory, ISubSystemConfigurationAdapter.class.getName());
				adapter = (ISubSystemConfigurationAdapter) ssFactory.getAdapter(ISubSystemConfigurationAdapter.class);
				// if (adapter == null) {
				// //TODO is this right? It deprives clients from the ability
				// //to get rid of the standard actions contributed by the
				// //default adapter. We shouldn't do that.
				// adapter = new SubSystemConfigurationAdapter();
				// }
			}
			if (adapter != null) {
				// Lazy Loading: Dynamic contributed subsystem actions will be
				// provided only once the corresponding plugin is activated
				// (adapter factory loaded). This means, that by default,
				// dynamic actions are only shown after connecting a subsystem.
				// If a subsystem needs to show these earlier, it needs to
				// provision for eager bundle activation to ensure that its
				// ISubSystemConfigurationAdapter is loaded -- or, provide these
				// actions by static plugin.xml markup.
				IAction[] actions = adapter.getSubSystemActions(menu, selection, shell, menuGroup, ssFactory, ss);
				if (actions != null)
				{
					for (int idx = 0; idx < actions.length; idx++) {
						IAction action = actions[idx];
						menu.add(menuGroup, action);
					}
				}
			}
		}
		menu.appendToGroup(ISystemContextMenuConstants.GROUP_NEW, new GroupMarker(ISystemContextMenuConstants.GROUP_NEW_NONCASCADING));// user or BP/ISV additions
	}

	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 * @param element The element for which an image is desired
	 */
	public ImageDescriptor getImageDescriptor(Object element)
	{
		//System.out.println("INSIDE GETIMAGEDESCRIPTOR FOR SUBSYSTEM VIEW ADAPTER: "+element);
		ISubSystem ss = (ISubSystem)element;
		ISubSystemConfiguration ssFactory = ss.getSubSystemConfiguration();
		if (ssFactory != null)
		{
			ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)ssFactory.getAdapter(ISubSystemConfigurationAdapter.class);
			if (adapter != null) {
				if (ss.isConnected())
					return adapter.getLiveImage(ssFactory);
				else
					return adapter.getImage(ssFactory);
			} else {
				// get image from proxy
				ISubSystemConfigurationProxy proxy = ssFactory.getSubSystemConfigurationProxy();
				SubSystemConfigurationProxyAdapter proxyAdapter = (SubSystemConfigurationProxyAdapter) Platform.getAdapterManager().getAdapter(proxy,
						SubSystemConfigurationProxyAdapter.class);
				if (proxyAdapter != null) {
					if (ss.isConnected())
						return proxyAdapter.getLiveImageDescriptor();
					else
						return proxyAdapter.getImageDescriptor();
				} else {
					SystemBasePlugin.logWarning("Unexpected error: SubSystemConfiguration has no adapter and no proxyAdapter: " + ss.getName()); //$NON-NLS-1$
					return null;
				}
			}
		}
		else
		{
	      SystemBasePlugin.logWarning("Unexpected error: SubSystemConfiguration is null for ss " + ss.getName()); //$NON-NLS-1$
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier#getAbsoluteName(java.lang.Object)
	 */
	public String getAbsoluteName(Object element)
	{
		ISubSystem ss = (ISubSystem)element;
		String suffix = ss.getName();
		Class serviceType = ss.getServiceType();
		if (serviceType != null) {
			suffix = serviceType.toString();
		}
		return ss.getSystemProfileName() + "." + ss.getHostAliasName() + "." + suffix; //$NON-NLS-1$ //$NON-NLS-2$
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
		return RSECorePlugin.getTheSystemRegistry().getHost(ss.getSystemProfile(),ss.getHostAliasName());
	}

	/**
	 * Return the children of this object
	 */
	public Object[] getChildren(IAdaptable element, IProgressMonitor monitor)
	{
		//System.out.println("INSIDE GETCHILDREN FOR SUBSYSTEM VIEW ADAPTER: "+element);
		ISubSystem ss = (ISubSystem)element;
		Object[] children = ss.getChildren();
		return children;
	}

	/**
	 * Return true if this object has children
	 */
	public boolean hasChildren(IAdaptable element)
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
	      propertyPortDescriptor.setDescription(SystemViewResources.RESID_PROPERTY_PORT_TOOLTIP);
	      propertyPortDescriptor.setValidator(new ValidatorPortInput());
          propertyDescriptorArray[++idx] = propertyPortDescriptor;
	      //propertyDescriptorArray[++idx] = getPortDescriptor();

	      // connected
	      propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_IS_CONNECTED, SystemViewResources.RESID_PROPERTY_CONNECTED_LABEL, SystemViewResources.RESID_PROPERTY_CONNECTED_TOOLTIP);

          // vrm
	      propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_VRM, SystemViewResources.RESID_PROPERTY_VRM_LABEL, SystemViewResources.RESID_PROPERTY_VRM_TOOLTIP);
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
	    	//On Local subsystems, connectedId==null. Phil.
	    	if (data.getIsLocal() && connectedId!=null && !connectedId.equals(localUserId))
	    	{
    			data.setLocalValue(connectedId);
    			data.setIsLocal(true);
	    	}
	    	else if (connectedId!=null && !connectedId.equals(parentUserId))
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
	      data.setInheritedDisplayString(NLS.bind(SystemPropertyResources.RESID_PORT_DYNAMICSELECT, "0")); //$NON-NLS-1$
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
		if (name.equals(ISystemPropertyConstants.P_USERID))
          return setUserIdPropertyData(userIdData, ss);
		else if (name.equals(ISystemPropertyConstants.P_PORT))
		  return getPortString(ss);
		else if (name.equals(ISystemPropertyConstants.P_VRM))
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
		else if (name.equals(ISystemPropertyConstants.P_IS_CONNECTED))
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
			  System.out.println("SystemViewSubSystemAdapter: Error! system is null for subsystem "+ss.getClass().getName()); //$NON-NLS-1$
			  SystemBasePlugin.logError("SystemViewSubSystemAdapter: Error! system is null for subsystem "+ss.getClass().getName(), null); //$NON-NLS-1$
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

        ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)ssFactory.getAdapter(ISubSystemConfigurationAdapter.class);
        if (adapter != null) {
        	// Lazy Loading: Since this is called from opening a context
			// menu, Dynamic Validator will only be available once the
			// ISubSystemConfigurationAdapter is loaded, i.e. after
			// connecting.
			// If a subsystem wants to provision for having the validator
			// available earlier, it needs to eagerly load the bundle that
			// declares its ISubSystemConfigurationAdapter.
			if (userIdDescriptor != null) {
				userIdDescriptor.setValidator(adapter.getUserIdValidator(ssFactory));
			}
	        // getPortDescriptor().setValidator((ICellEditorValidator)ssFactory.getPortValidator());
			if (propertyPortDescriptor != null) {
				propertyPortDescriptor.setValidator(adapter.getPortValidator(ssFactory));
			}
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
	    if (property.equals(ISystemPropertyConstants.P_USERID))
	      changed = changed_userId;
	    else if (property.equals(ISystemPropertyConstants.P_PORT))
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
	    ssFactory.updateSubSystem(subsys, true, userId, false, subsys.getConnectorService().getPort());
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
        String port = data;
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
	    ssFactory.updateSubSystem(subsys, false, subsys.getLocalUserId(), true, portInt);
	    subsys.commit();
    }


    /**
	 * Called when user selects the reset button in property sheet.
	 */
    public void resetPropertyValue(Object propertyObject)
    {
    	String property = (String)propertyObject;
	    ISubSystem ss = (ISubSystem)propertySourceInput;
        ss.getSubSystemConfiguration();
	    if (property.equals(ISystemPropertyConstants.P_USERID))
	    {
		  updateUserId(ss, original_userIdData);
	      changed_userId = false;
	    }
	    else if (property.equals(ISystemPropertyConstants.P_PORT))
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
	    if (name.equals(ISystemPropertyConstants.P_USERID))
	    {
		  updateUserId(ss, (SystemInheritablePropertyData)value);
	      changed_userId = true;
	    }
	    else if (name.equals(ISystemPropertyConstants.P_PORT))
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
	 *      property page, which is determined by calling it's factory's {@link ISubSystemConfiguration#supportsServerLaunchProperties(org.eclipse.rse.core.model.IHost)} method.
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
			if (name.equalsIgnoreCase("serverLaunchPP")) //$NON-NLS-1$
			{
				ISubSystem ss = (ISubSystem)target;
				boolean supports = ss.getSubSystemConfiguration().supportsServerLaunchProperties(ss.getHost());
				return supports ? value.equals("true") : value.equals("false"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else if (name.equalsIgnoreCase("envVarPP")) //$NON-NLS-1$
			{
				/** FIXME can't access specific subsystems from core anymore
				boolean supports = false;
				if (ss instanceof IRemoteFileSubSystem)
					supports = ((IRemoteFileSubSystemConfiguration)ss.getParentSubSystemConfiguration()).supportsEnvironmentVariablesPropertyPage();
				else
					supports = ((IRemoteCmdSubSystemConfiguration)ss.getParentSubSystemConfiguration()).supportsEnvironmentVariablesPropertyPage();
				*/
				boolean supports = false;
				return supports ? value.equals("true") : value.equals("false"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else if (name.equalsIgnoreCase("isConnectionError")) //$NON-NLS-1$
			{
				ISubSystem ss = (ISubSystem) target;
				boolean error = ss.isConnectionError();
				return error ? value.equals("true") : value.equals("false"); //$NON-NLS-1$ //$NON-NLS-2$
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
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
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
	public boolean doRename(Shell shell, Object element, String name, IProgressMonitor monitor)
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
		return ssf.getId()+"="+ss.getName(); //$NON-NLS-1$
	}
	/**
	 * Return what to save to disk to identify this element when it is the input object to a secondary
	 *  Remote System Explorer perspective.
	 */
	public String getInputMementoHandle(Object element)
	{
		Object parent = getParent(element);
		return SystemAdapterHelpers.getViewAdapter(parent, getViewer()).getInputMementoHandle(parent) + MEMENTO_DELIM + getMementoHandle(element);
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

	/**
	 * This is a local RSE artifact so returning false
	 *
	 * @param element the object to check
	 * @return false since this is not remote
	 */
	public boolean isRemote(Object element) {
		return false;
	}
}
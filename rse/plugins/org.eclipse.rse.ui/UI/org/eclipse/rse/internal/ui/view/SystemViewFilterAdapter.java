/*******************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Tobias Schwarz   (Wind River) - [173267] "empty list" should not be displayed 
 * Martin Oberhuber (Wind River) - [190271] Move ISystemViewInputProvider to Core
 * Xuan Chen        (IBM)        - [160775] [api] rename (at least within a zip) blocks UI thread
 * David Dykstal (IBM) - [224671] [api] org.eclipse.rse.core API leaks non-API types
 * David Dykstal (IBM) - [226761] fix NPE in team view when expanding items
 * David McKnight   (IBM)        - [334295] SystemViewForm dialogs don't display cancellable progress in the dialog
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view;
import java.util.Arrays;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterString;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.ISystemModifiableContainer;
import org.eclipse.rse.core.model.ISystemViewInputProvider;
import org.eclipse.rse.core.model.SystemChildrenContentsType;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystemHelpers;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorFilterName;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Default Adapter for displaying filter objects in tree views.
 * For some subsystems, these are children of SubSystem objects.
 * This class offers default behaviour but can be subclassed to refine the 
 * behaviour. If this is done, you must register your subclass with the 
 * platform's adapter manager in your plugin class's startup method.
 */
public class SystemViewFilterAdapter extends AbstractSystemViewAdapter
{


	//private static String translatedFilterString = null;	
	// -------------------
	// property descriptors
	// -------------------
	private static PropertyDescriptor[] propertyDescriptorArray = null;		
	private SystemComboBoxPropertyDescriptor filterStringsDescriptor;
		
	/**
	 * Returns any actions that should be contributed to the popup menu
	 * for the given filter object.
	 * @param menu The menu to contribute actions to
	 * @param selection The window's current selection.
	 * @param shell Shell of viewer
	 * @param menuGroup recommended menu group to add actions to. If added to another group, you must be sure to create that group first.
	 */
	public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup)
	{
		//if (selection.size() != 1)
		//  return; // does not make sense adding unique actions per multi-selection
		ISystemFilter filter = (ISystemFilter)selection.getFirstElement();
		if (filter.isTransient())
		  return;
	    ISubSystemConfiguration ssFactory = SubSystemHelpers.getParentSubSystemConfiguration(filter);
	    ssFactory.setConnection(null);
	    ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)ssFactory.getAdapter(ISubSystemConfigurationAdapter.class);
		IAction[] actions = adapter.getFilterActions(menu, selection, shell, menuGroup, ssFactory, filter);
		if (actions != null)
		{
		  for (int idx=0; idx<actions.length; idx++)
		  {
		  	 IAction action = actions[idx];		
		  	 menu.add(menuGroup, action);
		  }    
		}    	  		
	}

	private ISystemFilter getFilter(Object element)
	{
		return (ISystemFilter)element;
	}
	
	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 * @param element The element for which an image is desired
	 * @return the desired image descriptor
	 */
	public ImageDescriptor getImageDescriptor(Object element) {
		ImageDescriptor filterImage = null;
		ISystemFilter filter = getFilter(element);
		if (filter.getProvider() != null) {
			ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter) filter.getProvider().getAdapter(ISubSystemConfigurationAdapter.class);
			if (adapter != null) {
				filterImage = adapter.getSystemFilterImage(filter);
			}
		}
		if (filterImage == null) {
			filterImage = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FILTER_ID);
		}
		return filterImage;
	}
	
	/**
	 * Return the label for this object. Calls getName on the filter
	 */
	public String getText(Object element)
	{
		return getFilter(element).getName();
	}
	/**
	 * Return the name of this object, which may be different than the display text ({#link #getText(Object)}.
	 * <p>
	 * Called by common rename and delete actions.
	 */
	public String getName(Object element)
	{
		return getFilter(element).getName();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier#getAbsoluteName(java.lang.Object)
	 */
	public String getAbsoluteName(Object element)
	{
		ISystemFilter filter = getFilter(element);
		if (filter.isTransient())
		{
			return filter.getName();
		}
		else
		{
			return filter.getSystemFilterPoolManager().getName() + "." + filter.getParentFilterPool().getName() + "." + filter.getName(); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}	
	/**
	 * Return the type label for this object
	 */
	public String getType(Object element)
	{
		ISystemFilter filter = getFilter(element);
		if (filter.isTransient())
		  return SystemResources.RESID_PP_FILTER_TYPE_VALUE;
		ISubSystemConfiguration ssParentFactory = SubSystemHelpers.getParentSubSystemConfiguration(filter);		
		return ssParentFactory.getTranslatedFilterTypeProperty(filter);
	}	
	
	/**
	 * Return the parent of this object. 
	 * This will be either a SubSystem object, or a filter object.
	 */
	public Object getParent(Object element)
	{
		ISystemFilter filter = getFilter(element);
		if (filter.isTransient())
		  return filter.getSubSystem(); 
		return filter.getParentFilterContainer();
	}
	
	/**
	 * Overriding because it's possible for a filter to be promptable while not being a ISystemPromptableObject
	 */
    public boolean isPromptable(Object element){
    	ISystemFilter filter = getFilter(element);
    	if (filter != null && filter.isPromptable()){
    		return true;
    	}
    	return super.isPromptable(element);
    }
    
	/**
	 * Return the children of this filter.
	 * This is a combination of nested filters and resolved filter objects.
	 */
	public Object[] getChildren(IAdaptable element, IProgressMonitor monitor)
	{
		ISystemFilter filter = getFilter(element);
		// transient filters...
		if (filter.isTransient())
		{
			 if (filter.isPromptable())
			   return checkForEmptyList(processPromptingFilter(filter), element, true);

			 Object[] children = null;
    	     String[] filterStrings = filter.getFilterStrings();
    	     // 50167pc: The following was a problem, as the parent in a SystemFilterSimpleImpl is not
    	     // to be trusted, since we tend to use the same instance for each connection in the list.     	   	 
             ISubSystem ss = (ISubSystem)filter.getSubSystem();
             String preSelectName = null;
		     try
		     {
//		     	Shell shell = getShell();
		     	// hack to propogate type filters down from connection in select dialogs...
		     	ISystemViewInputProvider inputProvider = getInput();
		     	if ((inputProvider instanceof SystemSelectRemoteObjectAPIProviderImpl) &&
		     	     (filterStrings != null) && (filterStrings.length > 0) )
		     	{
		     	  SystemSelectRemoteObjectAPIProviderImpl ip = (SystemSelectRemoteObjectAPIProviderImpl)inputProvider;
		     	  if (ip.filtersNeedDecoration(element))
		     	  {
		     	  	String[] adorned = new String[filterStrings.length];
		     	  	for (int idx = 0; idx < filterStrings.length; idx++)
		     	  	   adorned[idx] = ip.decorateFilterString(element, filterStrings[idx]);
		     	  	filterStrings = adorned;
		     	  }
		     	  preSelectName = ip.getPreSelectFilterChild();
		     	}
		     	if (filter instanceof ISystemModifiableContainer) {
		     		ISystemModifiableContainer containingFilter = (ISystemModifiableContainer) filter;
			     	// get children from cache if the children have been cached
			     	if (ss.getSubSystemConfiguration().supportsFilterCaching() && !containingFilter.isStale() &&
			     			containingFilter.hasContents(SystemChildrenContentsType.getInstance())) {
			     		children = containingFilter.getContents(SystemChildrenContentsType.getInstance());
			     	}
			     	// otherwise, get children and then cache
			     	else {
			     		children = checkForEmptyList(ss.resolveFilterStrings(filterStrings, monitor), element, true);
			     		
			     		if (ss.getSubSystemConfiguration().supportsFilterCaching()) {
			     			containingFilter.setContents(SystemChildrenContentsType.getInstance(), children);
			     		}
			     	}
		     	}
		     	
		     	
                if ((children !=null) && (preSelectName != null))
                {
                   Object match = null;
                   for (int idx=0; (match==null) && (idx<children.length); idx++)
                   {
                   	  Object child = children[idx];
                   	  String objName = SystemAdapterHelpers.getViewAdapter(child, getViewer()).getName(child);
                   	  if ((objName != null) && (objName.equals(preSelectName)))
                   	    match = child;
                   }
                   //if (match != null) always reset even if it is null
                   if (inputProvider instanceof SystemSelectRemoteObjectAPIProviderImpl)
                   {
					  ((SystemSelectRemoteObjectAPIProviderImpl) inputProvider).setPreSelectFilterChildObject(match);
                   }
                }
		     }
		     catch (InterruptedException exc)
		     {
		     	children = getCancelledMessageObject();
		     }
		     catch (Exception exc)
		     {
		     	children = getFailedMessageObject();
		     	SystemBasePlugin.logError("Exception resolving filters' strings ",exc); //$NON-NLS-1$
		     } // message already issued

    	   	 return children;                   
		}

		if (filter.isPromptable())
		  return checkForEmptyList(null, element, false);
		
		// normal filters...
    	//Vector strings = filter.getFilterStringsVector();
		ISystemFilterString[] filterStrings = filter.getSystemFilterStrings();
		ISystemFilter[] filters = filter.getSystemFilters();
    	Vector vChildren = new Vector();

        // start with nested filters...
    	for (int idx=0; idx < filters.length; idx++)
    	   vChildren.addElement(filters[idx]);  
    	// continue with resolved filter string objects...
    	for (int idx=0; idx < filterStrings.length; idx++)
    	{
    	   vChildren.addElement(filterStrings[idx]);
    	}
    	
    	// convert whole thing to an array...
    	Object[] children = new Object[vChildren.size()];
    	for (int idx=0; idx<vChildren.size(); idx++)
    	   children[idx] = vChildren.elementAt(idx);
    	   
    	return children;		
	}
	
    /**
     * Processing a prompting filter...
     */
    private Object[] processPromptingFilter(ISystemFilter filter)
    {
		Object[] children = new SystemMessageObject[1];
		ISystemViewInputProvider inputProvider = getInput();
		if ( (inputProvider != null) && (inputProvider instanceof SystemSelectRemoteObjectAPIProviderImpl) )
		{
    	  try 
    	  {
		     SystemSelectRemoteObjectAPIProviderImpl ip = (SystemSelectRemoteObjectAPIProviderImpl)inputProvider;    	  	 
             ISystemFilter newFilter = ip.createFilterByPrompting(filter, getShell());
             if (newFilter == null)
             {
		        children = getCancelledMessageObject();
             }
             else // filter string successfully created!
             {
                //SystemFilterSimpleImpl simpleFilter = (SystemFilterSimpleImpl)filter;
                //SubSystem ss = (SubSystem)simpleFilter.getParent();

             	// return "filter created successfully" message object for this node
		        children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_FILTERCREATED),
		                                              ISystemMessageObject.MSGTYPE_OBJECTCREATED,filter);
		                                              
		        // select the new filter and expand it
                Viewer v = (Viewer)inputProvider.getViewer();
		        if (v instanceof ISystemResourceChangeListener)
		        {
		          SystemResourceChangeEvent event = new SystemResourceChangeEvent(newFilter, ISystemResourceChangeEvents.EVENT_SELECT_EXPAND, null);
		          RSEUIPlugin.getTheSystemRegistryUI().postEvent((ISystemResourceChangeListener)v, event); // only expand in the current viewer, not all viewers!
		        }
             }
    	  } catch (Exception exc)     	
    	  {
		        children = getFailedMessageObject();
		     	SystemBasePlugin.logError("Exception prompting for filter ",exc);           //$NON-NLS-1$
     	  }
		}
    	//RSEUIPlugin.logDebugMessage(this.getClass().getName(),"returning children");
    	return children;
    }
	
	/**
	 * Return true if this object has children
	 */
	public boolean hasChildren(IAdaptable element)
	{
		ISystemFilter filter = getFilter(element);
    	if (filter.getFilterStringCount() > 0)
    	  return true;
    	else if (filter.getSystemFilterCount() > 0)
    	  return true;
    	else 
    	  return false;
	}

    // Property sheet descriptors defining all the properties we expose in the Property Sheet

	/**
	 * Return our unique property descriptors
	 */
	protected IPropertyDescriptor[] internalGetPropertyDescriptors()
	{
		if (propertyDescriptorArray == null)
		{
		 	propertyDescriptorArray = new PropertyDescriptor[3];
		  	int idx = 0;
		    
		  	// parent filter pool
		  	propertyDescriptorArray[idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_PARENT_FILTERPOOL,SystemViewResources.RESID_PROPERTY_FILTERPARENTPOOL_LABEL, SystemViewResources.RESID_PROPERTY_FILTERPARENTPOOL_TOOLTIP);
		  	// parent filter
		  	propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_PARENT_FILTER,SystemViewResources.RESID_PROPERTY_FILTERPARENTFILTER_LABEL, SystemViewResources.RESID_PROPERTY_FILTERPARENTFILTER_TOOLTIP);	      
		  	// number filter strings
		  	propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILTERSTRINGS_COUNT,SystemViewResources.RESID_PROPERTY_FILTERSTRINGS_COUNT_LABEL, SystemViewResources.RESID_PROPERTY_FILTERSTRINGS_COUNT_TOOLTIP);
		}		
		return propertyDescriptorArray;		
	}
	/**
	 * Return our unique property values
	 * The parent handles P_TEXT and P_TYPE only, and we augment that here.
	 * @param key the name of the property as named by its property descriptor
	 * @return the current value of the property
	 */
	protected Object internalGetPropertyValue(Object key)
	{
		String name = (String)key;		
		ISystemFilter filter = getFilter(propertySourceInput);
		// following not working yet...
		if (name.equals(ISystemPropertyConstants.P_FILTERSTRINGS))
		{
		   String[] filterStrings = filter.getFilterStrings();
		   filterStringsDescriptor.setValues(filterStrings);
		   if ((filterStrings != null) && (filterStrings.length>0))
			 return filterStrings[0];
		   else
			 return "null"; //$NON-NLS-1$
		}
		else if (name.equals(ISystemPropertyConstants.P_FILTERSTRINGS_COUNT))
		{
		   int nbrFilterStrings = filter.getFilterStringCount();
		   return Integer.toString(nbrFilterStrings);
		}
		else if (name.equals(ISystemPropertyConstants.P_PARENT_FILTER))
		{
		   if (filter.isTransient())
			 return getTranslatedNotApplicable();
		   ISystemFilter parent = filter.getParentFilter();
		   if (parent != null)
			 return parent.getName();
		   else
			 return getTranslatedNotApplicable();
		}
		else if (name.equals(ISystemPropertyConstants.P_PARENT_FILTERPOOL))
		{
		   if (filter.isTransient())
			 return getTranslatedNotApplicable();
		   ISystemFilterPool parent = filter.getParentFilterPool();
		   if (parent != null)
			 return parent.getName();
		   else
			 return null;
		}				
		else
		  return null;
	}	
	
	// FOR COMMON DELETE ACTIONS		
	public boolean showDelete(Object element)
	{
		return !getFilter(element).isTransient();
	}
	/**
	 * Return true if this object is deletable by the user. If so, when selected,
	 *  the Edit->Delete menu item will be enabled.
	 */
	public boolean canDelete(Object element)
	{
		return true;
	}
	
	/**
	 * Perform the delete action.
	 */
	public boolean doDelete(Shell shell, Object element, IProgressMonitor monitor)  throws Exception
	{
		ISystemFilter filter = getFilter(element);	
		ISystemFilterPoolManager fpMgr = filter.getSystemFilterPoolManager();
		fpMgr.deleteSystemFilter(filter);		
		return true;
	}
	
	// FOR COMMON RENAME ACTIONS	
	public boolean showRename(Object element)
	{
		return !getFilter(element).isTransient();
	}

	/**
	 * Return true if this object is renamable by the user. If so, when selected,
	 *  the Rename popup menu item will be enabled.
	 */
	public boolean canRename(Object element)
	{
		return true;
	}	
	/**
	 * Perform the rename action.
	 */
	public boolean doRename(Shell shell, Object element, String name, IProgressMonitor monitor) throws Exception
	{
		ISystemFilter filter = getFilter(element);
		ISystemFilterPoolManager fpMgr = filter.getSystemFilterPoolManager();
		fpMgr.renameSystemFilter(filter,name);		
		return true;
	}    	
	/**
	 * @param element either a filter for a rename action, or a filter pool for a "new" action.
	 * @return a validator for verifying the new name is correct.
	 */
    public ISystemValidator getNameValidator(Object element) {
		ISystemFilter filter = null;
		ISystemFilterPool pool = null;
		String[] filterNames = null;
		if (element instanceof ISystemFilter) {
			filter = (ISystemFilter) element;
			pool = filter.getParentFilterPool();
			if (pool != null)
				filterNames = pool.getSystemFilterNames();
			else {
				ISystemFilter parentFilter = filter.getParentFilter();
				filterNames = parentFilter.getSystemFilterNames();
			}
		} else {
			pool = (ISystemFilterPool) element;
			filterNames = pool.getSystemFilterNames();
		}
		Vector names = new Vector(filterNames.length);
		names.addAll(Arrays.asList(filterNames));
		ISystemValidator nameValidator = new ValidatorFilterName(filterNames);
		return nameValidator;
	}	

    /**
     * Parent override.
     * <p>
     * Form and return a new canonical (unique) name for this object, given a candidate for the new
     *  name. This is called by the generic multi-rename dialog to test that all new names are unique.
     *  To do this right, sometimes more than the raw name itself is required to do uniqueness checking.
     * <p>
     * Returns mgrName.poolName.filterName, upperCased
     */
    public String getCanonicalNewName(Object element, String newName)
    {
    	ISystemFilter filter = (ISystemFilter)element;
    	if (!filter.isTransient())
    	{
    	  String mgrName = filter.getSystemFilterPoolManager().getName();
    	  return (mgrName + "." + filter.getParentFilterPool().getName() + "." + newName).toUpperCase(); //$NON-NLS-1$ //$NON-NLS-2$
    	}
    	else
    	  return newName.toUpperCase();
    }

	// FOR COMMON REFRESH ACTIONS	
	public boolean showRefresh(Object element)
	{
		return !getFilter(element).isTransient();
	}

	/**
	 * Return true if we should show the refresh action in the popup for the given element.
	 */
	public boolean showOpenViewActions(Object element)
	{
		return !getFilter(element).isTransient();
	}

	/**
	 * Test an object to see if it has an attribute with a particular value. 
	 * From <code>IActionFilter</code> so the popupMenus extension point can use 
	 * &lt;filter&gt;, &lt;enablement&gt; or &lt;visibility&gt;.
	 * We add support for the following attributes:
	 * <ol>
	 * <li>"filterType"
	 * The value is tested against the non-translated filter type.
	 * Not all subsystems support different types of filters.
	 * <li>name="showChangeFilterStringsPropertyPage".
	 * The value is tested against the call to the
	 * subsystem configuration method showChangeFilterStringsPropertyPage(SystemFilter).
	 * values should be <code>true</code> or <code>false</code>.
	 * </ol>
	 * @param target the object whose attribute we are testing
	 * @param name the attribute to test.
	 * @param value the value to test.
	 * @return true if the attribute of the given name can be said to have the given value
	 * @see IActionFilter#testAttribute(Object, String, String)
	 */
	public boolean testAttribute(Object target, String name, String value) {
		boolean result = false;
		if (name.equalsIgnoreCase("filterType")) { //$NON-NLS-1$
			ISystemFilter filter = getFilter(target);
			String type = filter.getType();
			result = (type != null) && (type.length() > 0) && value.equals(type);
		} else if (name.equalsIgnoreCase("showChangeFilterStringPropertyPage")) { //$NON-NLS-1$
			ISystemFilter filter = getFilter(target);
			ISubSystemConfiguration ssf = SubSystemHelpers.getParentSubSystemConfiguration(filter);
			result = (ssf != null) && ssf.showChangeFilterStringsPropertyPage(filter) && value.equals("true"); //$NON-NLS-1$
		} else {
			result = super.testAttribute(target, name, value);
		}
		return result;
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

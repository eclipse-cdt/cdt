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
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystemHelpers;
import org.eclipse.rse.core.subsystems.util.ISubsystemConfigurationAdapter;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.filters.SystemFilterSimple;
import org.eclipse.rse.model.ISystemMessageObject;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeListener;
import org.eclipse.rse.model.SystemChildrenContentsType;
import org.eclipse.rse.model.SystemMessageObject;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorFilterName;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;


/**
 * Default Adapter for displaying filter objects in tree views.
 * For some subsystems, these are children of SubSystem objects.
 * This class offers default behaviour but can be subclassed to refine the 
 * behaviour. If this is done, you must register your subclass with the 
 * platform's adapter manager in your plugin class's startup method.
 */
public class SystemViewFilterAdapter extends AbstractSystemViewAdapter implements ISystemViewElementAdapter, ISystemMessages
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
	    ISubSystemConfiguration ssFactory = SubSystemHelpers.getParentSubSystemFactory(filter);
	    ssFactory.setConnection(null);
	    ISubsystemConfigurationAdapter adapter = (ISubsystemConfigurationAdapter)ssFactory.getAdapter(ISubsystemConfigurationAdapter.class);
		IAction[] actions = adapter.getFilterActions(ssFactory, filter, shell);
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
	 */
	public ImageDescriptor getImageDescriptor(Object element)
	{
		//return RSEUIPlugin.getDefault().getImageDescriptor(ISystemConstants.ICON_SYSTEM_FILTER_ID);
    	ImageDescriptor filterImage = null;
    	ISystemFilter filter = getFilter(element);
    	if (filter.getProvider() != null)
    	{
    		ISubsystemConfigurationAdapter adapter = (ISubsystemConfigurationAdapter)filter.getProvider().getAdapter(ISubsystemConfigurationAdapter.class);
    		filterImage = adapter.getSystemFilterImage(filter);
    	}
    	if (filterImage == null)
    	  filterImage = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FILTER_ID);
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
	/**
	 * Return the absolute name, versus just display name, of this object
	 */
	public String getAbsoluteName(Object element)
	{
		ISystemFilter filter = getFilter(element);
		return filter.getSystemFilterPoolManager().getName() + "." + filter.getParentFilterPool().getName() + "." + filter.getName();
	}	
	/**
	 * Return the type label for this object
	 */
	public String getType(Object element)
	{
		ISystemFilter filter = getFilter(element);
		if (filter.isTransient())
		  return SystemResources.RESID_PP_FILTER_TYPE_VALUE;
		ISubSystemConfiguration ssParentFactory = SubSystemHelpers.getParentSubSystemFactory(filter);		
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
		  return ((SystemFilterSimple)filter).getParent(); 
		return filter.getParentFilterContainer();
	}
	
	/**
	 * Return the children of this filter.
	 * This is a combination of nested filters and resolved filter objects.
	 */
	public Object[] getChildren(Object element)
	{
		ISystemFilter filter = getFilter(element);
		// transient filters...
		if (filter.isTransient())
		{
			 if (filter.isPromptable())
			   return checkForNull(processPromptingFilter(filter), true);

			 Object[] children = null;
             SystemFilterSimple simpleFilter = (SystemFilterSimple)filter;
    	     String[] filterStrings = simpleFilter.getFilterStrings();
    	     // 50167pc: The following was a problem, as the parent in a SimpleFilterSimpleImpl is not
    	     //  to be trusted, since we tend to use the same instance for each connection in the list.     	   	 
             ISubSystem ss = (ISubSystem)simpleFilter.getParent();
             String preSelectName = null;
		     try
		     {
		     	Shell shell = getShell();
		     	// hack to propogate type filters down from connection in select dialogs...
		     	ISystemViewInputProvider inputProvider = getInput();
		     	if ( (inputProvider != null) && (inputProvider instanceof SystemSelectRemoteObjectAPIProviderImpl) &&
		     	     (filterStrings != null) && (filterStrings.length>0) )
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
		     	
		     	// get children from cache if the children have been cached
		     	if (ss.getSubSystemConfiguration().supportsFilterCaching() && !simpleFilter.isStale() &&
		     			simpleFilter.hasContents(SystemChildrenContentsType.getInstance())) {
		     		children = simpleFilter.getContents(SystemChildrenContentsType.getInstance());
		     	}
		     	// otherwise, get children and then cache
		     	else {
		     		children = checkForNull(ss.resolveFilterStrings(filterStrings,shell), true);
		     		
		     		if (ss.getSubSystemConfiguration().supportsFilterCaching()) {
		     			simpleFilter.setContents(SystemChildrenContentsType.getInstance(), children);
		     		}
		     	}
		     	
                if ((children !=null) && (preSelectName != null))
                {
                   Object match = null;
                   for (int idx=0; (match==null) && (idx<children.length); idx++)
                   {
                   	  Object child = children[idx];
                   	  String objName = getAdapter(child).getName(child);
                   	  if ((objName != null) && (objName.equals(preSelectName)))
                   	    match = child;
                   }
                   //if (match != null) always reset even if it is null
                   ((SystemSelectRemoteObjectAPIProviderImpl)inputProvider).setPreSelectFilterChildObject(match);
                }
		     }
		     catch (InterruptedException exc)
		     {
		     	children = getCancelledMessageObject();
		     }
		     catch (Exception exc)
		     {
		     	children = getFailedMessageObject();
		     	SystemBasePlugin.logError("Exception resolving filters' strings ",exc);
		     } // message already issued

    	   	 return children;                   
		}

		if (filter.isPromptable())
		  return checkForNull(null, false);
		
		// normal filters...
    	//Vector strings = filter.getFilterStringsVector();
		Vector strings = filter.getFilterStringObjectsVector();
		Vector filters = filter.getSystemFiltersVector();
    	Vector vChildren = new Vector();

        // start with nested filters...
    	for (int idx=0; idx < filters.size(); idx++)
    	   vChildren.addElement(filters.elementAt(idx));  
    	// continue with resolved filter string objects...
    	for (int idx=0; idx < strings.size(); idx++)
    	{
    	   //String filterString = (String)strings.elementAt(idx);
		   ISystemFilterString filterString = (ISystemFilterString)strings.elementAt(idx);
    	   vChildren.addElement(filterString);
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
		        children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_FILTERCREATED),
		                                              ISystemMessageObject.MSGTYPE_OBJECTCREATED,filter);
		                                              
		        // select the new filter and expand it
                Viewer v = inputProvider.getViewer();
		        if ((v!=null) && (v instanceof ISystemResourceChangeListener))
		        {
		          ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		          SystemResourceChangeEvent event = new SystemResourceChangeEvent(newFilter, ISystemResourceChangeEvents.EVENT_SELECT_EXPAND, null);
                  sr.postEvent((ISystemResourceChangeListener)v, event); // only expand in the current viewer, not all viewers!
		        }
             }
    	  } catch (Exception exc)     	
    	  {
		        children = getFailedMessageObject();
		     	SystemBasePlugin.logError("Exception prompting for filter ",exc);          
     	  }
		}
    	//RSEUIPlugin.logDebugMessage(this.getClass().getName(),"returning children");
    	return children;
    }
	
	/**
	 * Return true if this object has children
	 */
	public boolean hasChildren(Object element)
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
		  	propertyDescriptorArray[idx] = createSimplePropertyDescriptor(P_PARENT_FILTERPOOL,SystemViewResources.RESID_PROPERTY_FILTERPARENTPOOL_LABEL, SystemViewResources.RESID_PROPERTY_FILTERPARENTPOOL_TOOLTIP);
		  	// parent filter
		  	propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_PARENT_FILTER,SystemViewResources.RESID_PROPERTY_FILTERPARENTFILTER_LABEL, SystemViewResources.RESID_PROPERTY_FILTERPARENTFILTER_TOOLTIP);	      
		  	// number filter strings
		  	propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILTERSTRINGS_COUNT,SystemViewResources.RESID_PROPERTY_FILTERSTRINGS_COUNT_LABEL, SystemViewResources.RESID_PROPERTY_FILTERSTRINGS_COUNT_TOOLTIP);
		}		
		return propertyDescriptorArray;		
	}
	/**
	 * Return our unique property values
	 * The parent handles P_TEXT and P_TYPE only, and we augment that here.
	 * @param	property the name of the property as named by its property descriptor
	 * @return	the current value of the property
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
			 return "null";
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
	public boolean doRename(Shell shell, Object element, String name) throws Exception
	{
		ISystemFilter filter = getFilter(element);
		ISystemFilterPoolManager fpMgr = filter.getSystemFilterPoolManager();
		fpMgr.renameSystemFilter(filter,name);		
		return true;
	}    	
	/**
	 * Return a validator for verifying the new name is correct.
	 * @param either a filter for a rename action, or a filter pool for a "new" action.
	 */
    public ISystemValidator getNameValidator(Object element)
    { 
		ISystemFilter filter = null;
		ISystemFilterPool pool = null;
		Vector filterNames = null;
		if (element instanceof ISystemFilter)
		{
		  filter = (ISystemFilter)element;
		  pool = filter.getParentFilterPool(); 
		  if (pool != null)
		    filterNames = pool.getSystemFilterNames();
		  else
		  {
		  	ISystemFilter parentFilter = filter.getParentFilter();
		  	filterNames = parentFilter.getSystemFilterNames();
		  }
		}
		else
		{
		  pool = (ISystemFilterPool)element;
  	      filterNames = pool.getSystemFilterNames();		  
		}
		/*
		if (filter != null)
		  filterNames.removeElement(filter.getName()); // remove current filter's name
		*/
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
    	  return (mgrName + "." + filter.getParentFilterPool().getName() + "." + newName).toUpperCase();
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
	 * <i>Overide of parent method.</i><br>
	 * From <samp>IActionFilter</samp> so the popupMenus extension point can use &lt;filter&gt;, &lt;enablement&gt;
	 * or &lt;visibility&gt;. We add support is for the following:
	 * <ol>
	 *   <li>name="filterType". The value is tested against the non-translated filter type. Note all subsystems
	 *       support different types of filters.
	 *   <li>name="showChangeFilterStringsPropertyPage". The value is tested against the call to the subsystem factory method showChangeFilterStringsPropertyPage(SystemFilter).
	 *       Compares against "true" (default) or "false". 
	 * </ol>
	 */
	public boolean testAttribute(Object target, String name, String value)
	{
		if (name.equalsIgnoreCase("filterType"))
		{
			ISystemFilter filter = getFilter(target);
			String type = filter.getType();
			if ((type == null) || (type.length() == 0))
				return false;
			else
				return value.equals(type);
		}
		else if (name.equalsIgnoreCase("showChangeFilterStringPropertyPage"))
		{			
			ISystemFilter filter = getFilter(target);
			ISubSystemConfiguration ssf = SubSystemHelpers.getParentSubSystemFactory(filter);
			if (value.equals("true"))
				return ssf.showChangeFilterStringsPropertyPage(filter);
			else
				return !ssf.showChangeFilterStringsPropertyPage(filter);			 	
		}
		else
			return super.testAttribute(target, name, value);
	}
}
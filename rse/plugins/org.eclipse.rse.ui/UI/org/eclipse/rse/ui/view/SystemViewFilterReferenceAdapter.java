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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystemHelpers;
import org.eclipse.rse.core.subsystems.util.ISubSystemConfigurationAdapter;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterContainerReference;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemMessageObject;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeListener;
import org.eclipse.rse.model.ISystemResourceSet;
import org.eclipse.rse.model.SystemChildrenContentsType;
import org.eclipse.rse.model.SystemMessageObject;
import org.eclipse.rse.model.SystemRemoteResourceSet;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorFilterName;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;


/**
 * Adapter for displaying SystemFilterReference objects in tree views.
 * These are children of SystemFilterPoolReference and SystemFilterReference objects
 */
public class SystemViewFilterReferenceAdapter 
	extends AbstractSystemViewAdapter 
	implements ISystemViewElementAdapter, ISystemMessages
{
	//private static String translatedFilterString = null;	
	// -------------------
	// property descriptors 
	// -------------------
	private static PropertyDescriptor[] propertyDescriptorArray = null;
	//private SystemComboBoxPropertyDescriptor filterStringsDescriptor, filtersDescriptor;	

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
		ISystemFilter filter = getFilter(selection.getFirstElement());
		ISubSystemConfiguration ssFactory = getSubSystemConfiguration(filter);
		ISubSystem currentSubSystem = (ISubSystem) getFilterReference(selection.getFirstElement()).getSubSystem();
		IHost currentConnection = currentSubSystem.getHost();
		ssFactory.setConnection(currentConnection);
		ssFactory.setCurrentSelection(selection.toArray());
		  ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)ssFactory.getAdapter(ISubSystemConfigurationAdapter.class);
			
		IAction[] actions = adapter.getFilterActions(ssFactory, filter, shell);
		if (actions != null)
		{
			for (int idx = 0; idx < actions.length; idx++)
			{
				IAction action = actions[idx];
				menu.add(menuGroup, action);
			}
		}
		actions = adapter.getFilterReferenceActions(ssFactory, getFilterReference(selection.getFirstElement()), shell);
		if (actions != null)
		{
			for (int idx = 0; idx < actions.length; idx++)
			{
				IAction action = actions[idx];
				menu.add(menuGroup, action);
			}
		}
	}

	private ISubSystemConfiguration getSubSystemConfiguration(ISystemFilter filter)
	{
		return SubSystemHelpers.getParentSubSystemConfiguration(filter);
	}
	/**
	 * <i>Overridden from parent.</i><br>
	 * Returns the subsystem that contains this object.
	 */
	public ISubSystem getSubSystem(Object element)
	{
		if (element instanceof ISystemFilterReference)
			return (ISubSystem) (((ISystemFilterReference) element).getProvider());
		else
			return null;
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
		if (filter.getProvider() != null) // getProvider() returns the subsystem factory
		{

			ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)filter.getProvider().getAdapter(ISubSystemConfigurationAdapter.class);
			filterImage = adapter.getSystemFilterImage(filter);
		}
		if (filterImage == null)
			filterImage = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FILTER_ID);
		return filterImage;
	}

	private ISystemFilterReference getFilterReference(Object element)
	{
		return (ISystemFilterReference) element; // get referenced object
	}
	private ISystemFilter getFilter(Object element)
	{
		return getFilterReference(element).getReferencedFilter(); // get master object
	}

	/**
	 * Return the label for this object. Uses getName() on the filter pool object.
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
		ISubSystemConfiguration ssParentFactory = getSubSystemConfiguration(filter);
		return ssParentFactory.getTranslatedFilterTypeProperty(filter);
	}

	/**
	 * Return the parent of this object
	 */
	public Object getParent(Object element)
	{
		ISystemFilterReference fr = getFilterReference(element);
		ISystemFilterContainerReference parentContainer = fr.getParent();
		// if parent is a filter (eg, we are nested) that is always the parent...
		if (parentContainer instanceof ISystemFilterReference)
			return parentContainer;
		// else parent is a filter pool. The parent will be the pool only if
		//  we are in "Show Filter Pools" mode, else it is the subsystem.
		boolean showFPs = SystemPreferencesManager.getPreferencesManager().getShowFilterPools();
		if (showFPs)
			return parentContainer;
		else
			return (ISubSystem) fr.getProvider();
		//return fr.getParent();
	}

	/**
	 * Return the children of this object.
	 * For filters, this is one or more of:
	 * <ul>
	 *  <li>filters if nested filters supported
	 *  <li>filter strings if user has elected to show filter strings in his preferences
	 *  <li>resolved objects for each filter string if user has elected NOT to show filter strings in his preferences
	 * </ul>
	 */
	public Object[] getChildren(IProgressMonitor monitor, Object element)
	{
		return internalGetChildren(monitor, element);
	}
	
	/**
	 * Return the children of this object.
	 * For filters, this is one or more of:
	 * <ul>
	 *  <li>filters if nested filters supported
	 *  <li>filter strings if user has elected to show filter strings in his preferences
	 *  <li>resolved objects for each filter string if user has elected NOT to show filter strings in his preferences
	 * </ul>
	 */
	public Object[] getChildren(Object element)
	{
	    return internalGetChildren(null, element);	    
	}
	
	/*
	 * Returns the children of the specified element.  If a monitor is passed in then 
	 * the context is assumed to be modal and, as such, the modal version of ss.resolveFilterStrings
	 * is called rather than the main thread version.
	 */
	protected synchronized Object[] internalGetChildren(IProgressMonitor monitor, Object element)
	{
		Object[] children = null;
		ISystemFilterReference fRef = getFilterReference(element);
		ISystemFilter referencedFilter = fRef.getReferencedFilter();
		boolean promptable = referencedFilter.isPromptable();

		ISubSystem ss = fRef.getSubSystem();
		ISubSystemConfiguration ssf = SubSystemHelpers.getParentSubSystemConfiguration(referencedFilter);

		// PROMPTING FILTER?...
		if (promptable)
		{
			children = new SystemMessageObject[1];
			try
			{
				ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)ssf.getAdapter(ISubSystemConfigurationAdapter.class);
					
				ISystemFilter newFilter = adapter.createFilterByPrompting(ssf, fRef, getShell());
				if (newFilter == null)
				{
					children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_CANCELLED), ISystemMessageObject.MSGTYPE_CANCEL, element);
				}
				else // filter successfully created!
					{
					// return "filter created successfully" message object for this node
					children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_FILTERCREATED), ISystemMessageObject.MSGTYPE_OBJECTCREATED, element);
					// select the new filter reference...
					ISystemFilterReference sfr = fRef.getParentSystemFilterReferencePool().getExistingSystemFilterReference(ss, newFilter);
					ISystemViewInputProvider inputProvider = getInput();
					if ((sfr != null) && (inputProvider != null) && (inputProvider.getViewer() != null))
					{
						ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
						SystemResourceChangeEvent event = new SystemResourceChangeEvent(sfr, ISystemResourceChangeEvents.EVENT_SELECT_EXPAND, null);
						Viewer v = inputProvider.getViewer();
						if (v instanceof ISystemResourceChangeListener)
						{
							//sr.fireEvent((ISystemResourceChangeListener)v, event); // only expand in the current viewer, not all viewers!
							sr.postEvent((ISystemResourceChangeListener) v, event); // only expand in the current viewer, not all viewers!
						}
					}
				}
			}
			catch (Exception exc)
			{
				children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_FAILED), ISystemMessageObject.MSGTYPE_ERROR, element);
				SystemBasePlugin.logError("Exception prompting for filter ", exc);
			}
			//RSEUIPlugin.logDebugMessage(this.getClass().getName(),"returning children");
			return children;
		}

		// NON-PROMPTING FILTER?...
		Object[] nestedFilterReferences = fRef.getSystemFilterReferences(ss);
		int nbrFilterStrings = referencedFilter.getFilterStringCount();
		if (nbrFilterStrings == 0)
			return nestedFilterReferences;
		else
		{
			/*
			// show filter strings    	
			if (ssf.showFilterStrings())
			{
				 SystemFilterStringReference[] refFilterStrings = fRef.getSystemFilterStringReferences();
				 if ((nestedFilterReferences == null) || (nestedFilterReferences.length == 0))
				   return refFilterStrings;
				 if ((refFilterStrings == null) || (refFilterStrings.length == 0))
				   return nestedFilterReferences;    	   	
				 int nbrChildren = nestedFilterReferences.length + refFilterStrings.length;
				 children = new Object[nbrChildren];
				 int idx=0;
				 for (idx=0; idx<nestedFilterReferences.length; idx++)
				    children[idx] = nestedFilterReferences[idx];
				 for (int jdx=0; jdx<refFilterStrings.length; jdx++)
				    children[idx++] = refFilterStrings[jdx];
				 return children;
			}
			// resolve filter strings
			else
			*/ {
				String[] filterStrings = referencedFilter.getFilterStrings();

				try
				{
				
					// hack to propogate type filters down from connection in select dialogs...
					ISystemViewInputProvider inputProvider = getInput();
					if ((inputProvider != null) && (inputProvider instanceof SystemSelectRemoteObjectAPIProviderImpl) && 
					        (filterStrings != null) && (filterStrings.length > 0))
					{
						SystemSelectRemoteObjectAPIProviderImpl ip = (SystemSelectRemoteObjectAPIProviderImpl) inputProvider;
						if (ip.filtersNeedDecoration(element))
						{
							String[] adorned = new String[filterStrings.length];
							for (int idx = 0; idx < filterStrings.length; idx++)
								adorned[idx] = ip.decorateFilterString(element, filterStrings[idx]);
							filterStrings = adorned;
						}
					}
					
					
					if (!referencedFilter.isTransient() && 
							ssf.supportsFilterCaching() &&
							!fRef.isStale() && 
							fRef.hasContents(SystemChildrenContentsType.getInstance()))
					{
					    children = fRef.getContents(SystemChildrenContentsType.getInstance());
					}
					else
					{
					    Object[] allChildren = null;
					    
						if (monitor == null)
						{
							Shell shell = getShell();
						    allChildren = ss.resolveFilterStrings(filterStrings, shell);
						}
						else
						{
						    allChildren = ss.resolveFilterStrings(monitor, filterStrings);
						}
						
						if (allChildren == null)
						{
						    System.out.println("filter children == null!");
						}
						else
						{
							int nbrNestedFilters = (nestedFilterReferences == null) ? 0 : nestedFilterReferences.length;
							children = new Object[nbrNestedFilters + allChildren.length];
							int idx = 0;
							for (idx = 0; idx < nbrNestedFilters; idx++)
								children[idx] = nestedFilterReferences[idx];
							for (int jdx = 0; jdx < allChildren.length; jdx++)
								children[idx++] = allChildren[jdx];
							
				
							if (!referencedFilter.isTransient() && ssf.supportsFilterCaching())
							{
								fRef.setContents(SystemChildrenContentsType.getInstance(), children);
							}
						}
					}
				
				}
				catch (InterruptedException exc)
				{
					children = new SystemMessageObject[1];
					children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_CANCELLED), ISystemMessageObject.MSGTYPE_CANCEL, element);
					SystemBasePlugin.logDebugMessage(this.getClass().getName(), "Filter resolving canceled by user.");
				}
				catch (Exception exc)
				{
					children = new SystemMessageObject[1];
					children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_FAILED), ISystemMessageObject.MSGTYPE_ERROR, element);
					SystemBasePlugin.logError("Exception resolving filters' strings ", exc);
				} // message already issued

				if ((children == null) || (children.length == 0))
				{
					children = new SystemMessageObject[1];
					children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_EMPTY), ISystemMessageObject.MSGTYPE_EMPTY, element);
				}
				return children;
			}
		}
	}

	/**
	 * Return true if this object has children.
	 * That is, if the referenced filter has nested filters or filter strings.
	 */
	public boolean hasChildren(Object element)
	{		
		ISystemFilterReference fRef = getFilterReference(element);
		ISystemFilter referencedFilter = fRef.getReferencedFilter();
		
		ISubSystemConfiguration factory = getSubSystemConfiguration(referencedFilter);
		if (factory.supportsFilterChildren())
		{		
			int nbrNestedFilters = referencedFilter.getSystemFilterCount();
			int nbrFilterStrings = referencedFilter.getFilterStringCount();
			return (nbrNestedFilters > 0) || (nbrFilterStrings > 0);
		}
		else
		{
			return false;
		}
	}

	/**
	 * Return true if this object is a "prompting" object that prompts the user when expanded.
	 * For such objects, we do not try to save and restore their expansion state on F5 or between
	 * sessions.
	 * <p>
	 * Default is false unless this is a prompting filter
	 */
	public boolean isPromptable(Object element)
	{
		boolean promptable = false;
		ISystemFilter filter = getFilter(element);
		promptable = filter.isPromptable();
		//if (!promptable && !SystemPreferencesManager.getPreferencesManager().getShowFilterStrings())
		if (!promptable)
		{
			//if (isCommandFilter(filter) || isJobFilter(filter))
			if (isCommandFilter(filter))
				promptable = true;
		}
		return promptable;
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
			ISystemFilterReference ref = getFilterReference(target);
			String type = ref.getReferencedFilter().getType();
			if ((type == null) || (type.length() == 0))
				return false;
			else
				return value.equals(type);
		}
		else if (name.equalsIgnoreCase("showChangeFilterStringPropertyPage"))
		{
			ISystemFilterReference ref = getFilterReference(target);
			ISubSystemConfiguration ssf = SubSystemHelpers.getParentSubSystemConfiguration(ref.getReferencedFilter());
			if (value.equals("true"))
				return ssf.showChangeFilterStringsPropertyPage(ref.getReferencedFilter());
			else
				return !ssf.showChangeFilterStringsPropertyPage(ref.getReferencedFilter());			 	
		}
		else
			return super.testAttribute(target, name, value);
	}

	// Property sheet descriptors defining all the properties we expose in the Property Sheet
	/**
	 * Return our unique property descriptors
	 */
	protected IPropertyDescriptor[] internalGetPropertyDescriptors()
	{
		if (propertyDescriptorArray == null)
		{
			int nbrOfProperties = 4;
			propertyDescriptorArray = new PropertyDescriptor[nbrOfProperties];
			int idx = 0;
			// parent filter pool
			propertyDescriptorArray[idx] = createSimplePropertyDescriptor(P_PARENT_FILTERPOOL, SystemViewResources.RESID_PROPERTY_FILTERPARENTPOOL_LABEL, SystemViewResources.RESID_PROPERTY_FILTERPARENTPOOL_TOOLTIP);
			// parent filter
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_PARENT_FILTER, SystemViewResources.RESID_PROPERTY_FILTERPARENTFILTER_LABEL, SystemViewResources.RESID_PROPERTY_FILTERPARENTFILTER_TOOLTIP);
			// number filter strings
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILTERSTRINGS_COUNT, SystemViewResources.RESID_PROPERTY_FILTERSTRINGS_COUNT_LABEL, SystemViewResources.RESID_PROPERTY_FILTERSTRINGS_COUNT_TOOLTIP);
			// Related connection
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_IS_CONNECTION_PRIVATE, SystemViewResources.RESID_PROPERTY_FILTERPOOLREFERENCE_IS_CONNECTIONPRIVATE_LABEL, SystemViewResources.RESID_PROPERTY_FILTERPOOLREFERENCE_IS_CONNECTIONPRIVATE_TOOLTIP); 
		}
		return propertyDescriptorArray;
	}
	/**
	 * Return our unique property values
	 */
	protected Object internalGetPropertyValue(Object key)
	{
		String name = (String) key;
		ISystemFilter filter = getFilter(propertySourceInput);
		if (name.equals(ISystemPropertyConstants.P_FILTERSTRINGS_COUNT))
		{
			int nbrFilterStrings = filter.getFilterStringCount();
			return Integer.toString(nbrFilterStrings);
		}
		else if (name.equals(ISystemPropertyConstants.P_PARENT_FILTER))
		{
			ISystemFilter parent = filter.getParentFilter();
			if (parent != null)
				return parent.getName();
			else
				return getTranslatedNotApplicable();
		}
		else if (name.equals(ISystemPropertyConstants.P_PARENT_FILTERPOOL))
		{
			ISystemFilterPool parent = filter.getParentFilterPool();
			if (parent != null)
				return parent.getName();
			else
				return getTranslatedNotApplicable();
		}
		else if (name.equals(ISystemPropertyConstants.P_IS_CONNECTION_PRIVATE))
		{
			ISystemFilterPool parent = filter.getParentFilterPool();
			return (parent.getOwningParentName()==null) ? getTranslatedNo() : getTranslatedYes();
		}
		else
			return null;
	}

	// FOR COMMON DELETE ACTIONS
	/**
	 * Return true if this object is deletable by the user. If so, when selected,
	 *  the Edit->Delete menu item will be enabled.
	 */
	public boolean canDelete(Object element)
	{
		ISystemFilter filter = getFilter(element);
		return !filter.isNonDeletable(); // defect 43190
		//return true;
	}

	/**
	 * Perform the delete action.
	 * This physically deletes the filter pool and all references.
	 */
	public boolean doDelete(Shell shell, Object element, IProgressMonitor monitor) throws Exception
	{
		ISystemFilter filter = getFilter(element);
		ISystemFilterPoolManager fpMgr = filter.getSystemFilterPoolManager();
		fpMgr.deleteSystemFilter(filter);
		return true;
	}

	// FOR COMMON RENAME ACTIONS
	/**
	 * Return true if this object is renamable by the user. If so, when selected,
	 *  the Rename menu item will be enabled.
	 */
	public boolean canRename(Object element)
	{
		ISystemFilter filter = getFilter(element);
		return !filter.isNonRenamable(); // defect 43190
		//return true;
	}

	/**
	 * Perform the rename action. Assumes uniqueness checking was done already.
	 */
	public boolean doRename(Shell shell, Object element, String name) throws Exception
	{
		ISystemFilter filter = getFilter(element);
		ISystemFilterPoolManager fpMgr = filter.getSystemFilterPoolManager();
		fpMgr.renameSystemFilter(filter, name);
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
		if (element instanceof ISystemFilterReference)
		{
			filter = getFilter(element);
			pool = filter.getParentFilterPool();
			if (pool != null)
				filterNames = pool.getSystemFilterNames();
			else
			{
				ISystemFilter parentFilter = filter.getParentFilter();
				filterNames = parentFilter.getSystemFilterNames();
			}
		}
		else if (element instanceof ISystemFilter)
		{
			filter = (ISystemFilter) element;
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
			pool = (ISystemFilterPool) element;
			filterNames = pool.getSystemFilterNames();
		}
		/*		
		if (filter != null)
		{
		  filterNames.removeElement(filter.getName()); // remove current filter's name
		  System.out.println("Existing names for " + filter.getName());
		  for (int idx=0; idx<filterNames.size(); idx++)
		     System.out.println("...: " + filterNames.elementAt(idx));		
		}
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
		ISystemFilterReference fRef = (ISystemFilterReference) element;
		ISystemFilter filter = fRef.getReferencedFilter();
		String mgrName = filter.getSystemFilterPoolManager().getName();
		return (mgrName + "." + filter.getParentFilterPool().getName() + "." + newName).toUpperCase();
	}

	/**
	 * Don't show "Open in new perspective" if this is promptable
	 */
	public boolean showOpenViewActions(Object element)
	{
		ISystemFilter filter = getFilter(element);
		return !filter.isPromptable();
	}
	
	
	/**
	  * Don't show generic "Show in Table" if the factory asks not to
	  */
	public boolean showGenericShowInTableAction(Object element)
	{	
		ISystemFilter filter = getFilter(element);
		ISubSystemConfiguration ssParentFactory = getSubSystemConfiguration(filter);
		return ssParentFactory.showGenericShowInTableOnFilter();
	}

	/**
	 * Return true if we should show the refresh action in the popup for the element.
	 */
	public boolean showRefresh(Object element)
	{
		ISystemFilter filter = getFilter(element);
		ISubSystemConfiguration ssParentFactory = getSubSystemConfiguration(filter);
		return ssParentFactory.showRefreshOnFilter();
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
		ISystemFilterReference fRef = getFilterReference(element);
		ISystemFilter referencedFilter = fRef.getReferencedFilter();
		ISystemFilterPool pool = referencedFilter.getParentFilterPool();
		String handle = pool.getReferenceName() + "=";
		ISystemFilter parentFilter = referencedFilter.getParentFilter();
		while (parentFilter != null)
		{
			handle += parentFilter.getName() + ";";
			parentFilter = parentFilter.getParentFilter();
		}
		handle += referencedFilter.getName();
		return handle;
	}
	/**
	 * Return what to save to disk to identify this element when it is the input object to a secondary
	 *  Remote Systems Explorer perspective.
	 */
	public String getInputMementoHandle(Object element)
	{
		Object parent = ((ISystemFilterReference) element).getParent(); //getParent(element); // will be filter (nested) or filter pool
		ISystemViewElementAdapter parentAdapter = getAdapter(parent);
		boolean showFPs = SystemPreferencesManager.getPreferencesManager().getShowFilterPools();
		if (parent instanceof ISystemFilterPoolReference) // not a nested filter
		{
			if (!showFPs) // not showing the real parent in GUI?
			{
				parent = parentAdapter.getParent(parent); // get the subsystem parent of the filter pool reference
				parentAdapter = getAdapter(parent); // get the adapter for the subsystem parent
			}
		}
		return parentAdapter.getInputMementoHandle(parent) + MEMENTO_DELIM + getMementoHandle(element);
	}

	/**
	 * Return a short string to uniquely identify the type of resource. Eg "conn" for connection.
	 * This just defaults to getType, but if that is not sufficient override it here, since that is
	 * a translated string.
	 */
	public String getMementoHandleKey(Object element)
	{
		return ISystemMementoConstants.MEMENTO_KEY_FILTERREFERENCE;
	}

	/**
	 * Somtimes we don't want to remember an element's expansion state, such as for temporarily inserted
	 *  messages. In these cases return false from this method. The default is true.
	 * <p>
	 * WE RETURN FALSE IF THIS IS A PROMPTABLE FILTER, COMMAND FILTER OR JOB FILTER.
	 */
	public boolean saveExpansionState(Object element)
	{
		boolean savable = true;
		ISystemFilterReference fRef = getFilterReference(element);
		ISystemFilter referencedFilter = fRef.getReferencedFilter();
		boolean promptable = referencedFilter.isPromptable();
		if (promptable)
			savable = false;
		else
		{
			// I thought the types would be set for these filters, but it isn't! Phil.
			/*
			String type = referencedFilter.getType();
			if ((type!=null) && (type.equals("Command") || type.equals("Job")))
			  savable = false;
			*/
			if (isCommandFilter(referencedFilter))
				savable = false;
		}
		return savable;
	}

	/**
	 * Return true if the given filter is from a command subsystem
	 */
	public static boolean isCommandFilter(ISystemFilter filter)
	{
		ISubSystemConfiguration ssf = (ISubSystemConfiguration) filter.getProvider();
		return ssf.supportsCommands();
	}

	// ------------------------------------------
	// METHODS TO SUPPORT COMMON DRAG AND DROP FUNCTION...
	// ------------------------------------------	
	/**
	 * drag support is handled directly for filter references, rather than delegated here.
	 */
	public boolean canDrag(Object element)
	{
		ISystemFilterReference fRef = getFilterReference(element);
		if (fRef != null)
		{
			if (getSubSystemConfiguration(fRef.getReferencedFilter()).supportsFilterStringExport())
			{	
				return true;
			}
		}
		return false;
	}

	/**
	 * Can this object be added as part of the filter?
	 */
	public boolean canDrop(Object element)
	{
	    ISystemFilterReference fRef = getFilterReference(element);
	    if (fRef != null)
	    {
	        ISubSystemConfiguration factory = getSubSystemConfiguration(fRef.getReferencedFilter());
	        if (factory.supportsDropInFilters())
	        {
	        	// if the drop is handled by the subsystem rather than this adapter, this will be true.
		        if (factory.providesCustomDropInFilters())
		        {
		        	return true;
		        }
		        
		        if (!fRef.getReferencedFilter().isNonChangable())
		        {		    
		            if (factory.supportsMultiStringFilters())
		            {	
		                return true;
		            }
		        }
	        }
	    }
		return false;
	}
	
	public ISystemResourceSet doDrag(SystemRemoteResourceSet set, IProgressMonitor monitor)
	{
		return set;
	}

	/**
	 * drag support is handled directory for filter references, rather than delegated here.
	 */
	public Object doDrag(Object element, boolean sameSystemType, IProgressMonitor monitor)
	{
		return element;
	}

	/**
	 * Add the absolute name of the from object to the list of filter strings for this filter
	 */
	public Object doDrop(Object from, Object to, boolean sameSystemType, boolean sameSystem, int srcType, IProgressMonitor monitor)
	{
		if (sameSystemType)
		{
			ISystemFilterReference fRef = getFilterReference(to);
			ISystemFilter filter = fRef.getReferencedFilter();

			if (from instanceof ISystemFilterReference)
			{
				ISystemFilter srcFilter = ((ISystemFilterReference) from).getReferencedFilter();
				String[] filterStrings = srcFilter.getFilterStrings();
				for (int i = 0; i < filterStrings.length; i++)
				{
					filter.addFilterString(filterStrings[i]);
				}
				return fRef;
			}
			else if (from instanceof IAdaptable)
			{
				ISystemRemoteElementAdapter radapter = (ISystemRemoteElementAdapter) ((IAdaptable) from).getAdapter(ISystemRemoteElementAdapter.class);
				
				{
				    
				    String newFilterStr = radapter.getFilterStringFor(from);
					if (newFilterStr != null)
					{
						filter.addFilterString(newFilterStr);					
						return fRef;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Validate that the source and target for the drag and drop operation are
	 * compatable.
	 */
	public boolean validateDrop(Object src, Object target, boolean sameSystem)
	{	    
		if (!sameSystem)
		{
		    if (src instanceof IResource) 
		    {
		        return true;
		    }
		    else
		    {
		        return false;
		    }
		}

		if (target instanceof ISystemFilterReference)
		{
			ISystemFilterReference filterRef = (ISystemFilterReference) target;
			if (getSubSystemConfiguration(filterRef.getReferencedFilter()).supportsMultiStringFilters())
			{
				if (src instanceof ISystemFilterReference)
				{
					// yantzi: wswb2.1.2 (defect 50994) add check for filter types
					String srcType = ((ISystemFilterReference)src).getReferencedFilter().getType();
					String targetType = filterRef.getReferencedFilter().getType();
					if (targetType != null && srcType != null)
					{
						if (targetType.equals(srcType))
						{
							return true;
						}
					}
					else
					{
						return true;
					}
				}
				// check if src has a filter string	
				else if (src instanceof IAdaptable)
				{
					ISystemRemoteElementAdapter adapter = (ISystemRemoteElementAdapter) ((IAdaptable) src).getAdapter(ISystemRemoteElementAdapter.class);
					if (adapter != null)
					{
						if (adapter.getFilterStringFor(src) != null)
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	
	/*
	 * Return whether deferred queries are supported. 
	 */
	public boolean supportsDeferredQueries()
	{
	    return true;
	}
}
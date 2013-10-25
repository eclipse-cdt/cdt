/*******************************************************************************
 * Copyright (c) 2002, 2013 IBM Corporation and others.
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
 * David Dykstal (IBM) - moved SystemsPreferencesManager to a new package
 * Tobias Schwarz (Wind River) - [181394] Include Context in getAbsoluteName() for filter and pool references
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Tobias Schwarz   (Wind River) - [173267] "empty list" should not be displayed
 * Martin Oberhuber (Wind River) - [190271] Move ISystemViewInputProvider to Core
 * Kevin Doyle (IBM) - [187707] Added separator between New Folder and New File in context menu
 * David McKnight   (IBM)        - [199566] Remove synchronzied from internalGetChildren
 * Xuan Chen        (IBM)        - [160775] [api] rename (at least within a zip) blocks UI thread
 * David McKnight   (IBM)        - [210563] error messages need to be shown if incurred during filter expansion
 * Martin Oberhuber (Wind River) - [218304] Improve deferred adapter loading
 * David McKnight   (IBM)        - [232148] Invalid thread access exception from SystemViewFilterReferenceAdapter.internalGetChildren()
 * David McKnight    (IBM)  - [233494] Show in Table Action should be removed from promptable filters
 * David McKnight   (IBM)        - [238507] Promptable Filters refreshed after modifying filter strings
 * David McKnight   (IBM)        - [244824] filter not refreshed if child is "empty list" or system message node
 * David McKnight   (IBM)        - [249245] not showing inappropriate popup actions for: Refresh, Show In Table, Go Into, etc. 
 * David McKnight   (IBM)        - [254614] Promptable filter's shouldn't require supportsCommands on the subsystem to be false
 * Noriaki Takatsu  (IBM)        - [288894] CANCEL has to be pressed 3 times in Userid/Password prompt window in Remote System Details view
 * David McKnight   (IBM)        - [416550] filter rename does case-insensitive check against original filter name
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterContainerReference;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemContainer;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.ISystemResourceSet;
import org.eclipse.rse.core.model.ISystemViewInputProvider;
import org.eclipse.rse.core.model.SystemChildrenContentsType;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.core.model.SystemRemoteResourceSet;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystemHelpers;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.SystemPreferencesManager;
import org.eclipse.rse.ui.filters.actions.SystemNewFilterAction;
import org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorFilterName;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.IContextObject;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;


/**
 * Adapter for displaying SystemFilterReference objects in tree views.
 * These are children of SystemFilterPoolReference and SystemFilterReference objects
 */
public class SystemViewFilterReferenceAdapter
	extends AbstractSystemViewAdapter
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
		ISubSystem currentSubSystem = getFilterReference(selection.getFirstElement()).getSubSystem();
		IHost currentConnection = currentSubSystem.getHost();
		ssFactory.setConnection(currentConnection);
		ssFactory.setCurrentSelection(selection.toArray());
		ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter) ssFactory.getAdapter(ISubSystemConfigurationAdapter.class);
		if (adapter != null) {
			// Lazy Loading: Specialized actions on filters are available only
			// after the bundle that declares the ISubSystemConfigurationAdapter
			// has been loaded, which typically is after connecting. We do not
			// load the bundle here because this code is executed as part of
			// showing a context menu. Subsystems who want their actions to be
			// available earlier need to provide them by static plugin.xml
			// markup or provision for more eager loading of their bundle, e.g.
			// through Platform.getAdapterManager().loadAdapter() at the right
			// time.
			IAction[] actions = adapter.getFilterActions(menu, selection, shell, menuGroup, ssFactory, filter);
			if (actions != null)
			{
				for (int idx = 0; idx < actions.length; idx++) {
					IAction action = actions[idx];
					if (action instanceof SystemNewFilterAction)
						menu.appendToGroup(ISystemContextMenuConstants.GROUP_NEW, new Separator());
					menu.add(menuGroup, action);
				}
			}
			actions = adapter.getFilterReferenceActions(menu, selection, shell, menuGroup, ssFactory, getFilterReference(selection.getFirstElement()));
			if (actions != null)
			{
				for (int idx = 0; idx < actions.length; idx++) {
					IAction action = actions[idx];
					menu.add(menuGroup, action);
				}
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
			if (adapter != null) {
				// Lazy Loading: Customized Filter Images will be available only
				// after the bundle that declares the
				// ISubSystemConfigurationAdapter has been loaded. Until that
				// time, a default filter image is used. Extenders who want to
				// see their filter images right away need to provision for
				// eager loading of their bundles at the right time (i.e. when
				// expanding the Subsystem node).
				filterImage = adapter.getSystemFilterImage(filter);
			}
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier#getAbsoluteName(java.lang.Object)
	 */
	public String getAbsoluteName(Object element)
	{
		//TODO consider caching the absolute name in the FilterReference to avoid unnecessary String operations - the name won't ever change
		ISystemFilterPoolReference filterPoolReference = getFilterReference(element).getParentSystemFilterReferencePool();
		ISystemViewElementAdapter adapter = SystemAdapterHelpers.getViewAdapter(filterPoolReference);
		String parentAbsoluteName = (adapter != null) ?	adapter.getAbsoluteName(filterPoolReference) : ""; //$NON-NLS-1$
		return parentAbsoluteName + "." + getName(element); //$NON-NLS-1$
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
		boolean showFPs = SystemPreferencesManager.getShowFilterPools();
		if (showFPs)
			return parentContainer;
		else
			return fr.getProvider();
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
	public Object[] getChildren(IAdaptable element, IProgressMonitor monitor)
	{
		return internalGetChildren(element, monitor);
	}

	/**
	 * Gets all the children and then passes the children to the subsystem configuration adapter for filtering.
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getChildren(org.eclipse.rse.ui.view.IContextObject, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Object[] getChildren(IContextObject element, IProgressMonitor monitor) {
		Object[] children = getChildren(element.getModelObject(), monitor);
		if (children == null) return null;
		ISubSystem subsystem = element.getSubSystem();
		ISubSystemConfiguration configuration = subsystem.getSubSystemConfiguration();
		Object adapter = Platform.getAdapterManager().getAdapter(configuration, ISubSystemConfigurationAdapter.class);

		if (adapter instanceof ISubSystemConfigurationAdapter)
		{
			children = ((ISubSystemConfigurationAdapter)adapter).applyViewFilters(element, children);
		}

		return children;
	}

	/*
	 * Returns the children of the specified element.  If a monitor is passed in then
	 * the context is assumed to be modal and, as such, the modal version of ss.resolveFilterStrings
	 * is called rather than the main thread version.
	 */
	protected Object[] internalGetChildren(Object element, IProgressMonitor monitor)
	{
		Object[] children = null;
		final ISystemFilterReference fRef = getFilterReference(element);
		ISystemFilter referencedFilter = fRef.getReferencedFilter();
		boolean promptable = referencedFilter.isPromptable();

		final ISubSystem ss = fRef.getSubSystem();
		final ISubSystemConfiguration ssf = SubSystemHelpers.getParentSubSystemConfiguration(referencedFilter);

		// PROMPTING FILTER?...
		if (promptable)
		{
			final Object[] pchildren = new SystemMessageObject[1];
			final Object pelement = element;
			// promptable's need to be run on the main thread since they display dialogs
			Display.getDefault().syncExec(new Runnable()
			{
				
				public void run()
				{
					try
					{
						ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)ssf.getAdapter(ISubSystemConfigurationAdapter.class);
		
						ISystemFilter newFilter = adapter.createFilterByPrompting(ssf, fRef, getShell());
						if (newFilter == null)
						{
							pchildren[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_CANCELLED), ISystemMessageObject.MSGTYPE_CANCEL, pelement);
						}
						else // filter successfully created!
							{
							// return "filter created successfully" message object for this node
							pchildren[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_FILTERCREATED), ISystemMessageObject.MSGTYPE_OBJECTCREATED, pelement);
							// select the new filter reference...
							ISystemFilterReference sfr = fRef.getParentSystemFilterReferencePool().getExistingSystemFilterReference(ss, newFilter);
							ISystemViewInputProvider inputProvider = getInput();
							if ((sfr != null) && (inputProvider != null) && (inputProvider.getViewer() != null))
							{
								SystemResourceChangeEvent event = new SystemResourceChangeEvent(sfr, ISystemResourceChangeEvents.EVENT_SELECT_EXPAND, null);
								Viewer v = (Viewer)inputProvider.getViewer();
								if (v instanceof ISystemResourceChangeListener)
								{
									//sr.fireEvent((ISystemResourceChangeListener)v, event); // only expand in the current viewer, not all viewers!
									RSEUIPlugin.getTheSystemRegistryUI().postEvent((ISystemResourceChangeListener) v, event); // only expand in the current viewer, not all viewers!
								}
							}
						}
					}
					catch (Exception exc)
					{
						pchildren[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_FAILED), ISystemMessageObject.MSGTYPE_ERROR, pelement);
						SystemBasePlugin.logError("Exception prompting for filter ", exc); //$NON-NLS-1$
					}
					//RSEUIPlugin.logDebugMessage(this.getClass().getName(),"returning children");
				}
			});
			return pchildren;
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

					boolean doQuery = true;
					if (!referencedFilter.isTransient() &&
							ssf.supportsFilterCaching() &&
							!fRef.isStale() &&
							fRef.hasContents(SystemChildrenContentsType.getInstance()))
					{
						doQuery = false;
					    children = fRef.getContents(SystemChildrenContentsType.getInstance());
					    if (children != null)
					    {
					    	if (children.length == 0){
					    		doQuery = true;
					    		fRef.markStale(true);
					    	}
					    	else {
						    	// check for stale children
						    	for (int i = 0; i < children.length && !doQuery; i++)
						    	{
						    		Object child = children[i];
						    		if (child instanceof ISystemContainer)
						    		{
						    			if (((ISystemContainer)child).isStale())
						    			{
						    				doQuery = true;
						    				fRef.markStale(true);
						    			}
						    		}
						    		else if (child instanceof ISystemMessageObject){
						    			if (((ISystemMessageObject)child).isTransient()){
						    				doQuery = true;
						    				fRef.markStale(true);
						    			}
						    		}
						    	}
					    	}
					    }
					}
					if (doQuery)
					{
					    Object[] allChildren = null;

						if (monitor == null)
						{
						    allChildren = ss.resolveFilterStrings(filterStrings, new NullProgressMonitor());
						}
						else
						{
						    allChildren = ss.resolveFilterStrings(filterStrings, monitor);
						}

						if (allChildren == null)
						{
						 //   System.out.println("filter children == null!"); //$NON-NLS-1$
						}
						else
						{
							if (allChildren.length == 1 && allChildren[0] instanceof ISystemMessageObject)
							{
								// error to display
								return allChildren; // nothing to sort or cache - just show the error
							}

							if (nestedFilterReferences != null)
							{
								int nbrNestedFilters = nestedFilterReferences.length;
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

				}
				catch (InterruptedException exc)
				{
					children = new SystemMessageObject[1];
					children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_CANCELLED), ISystemMessageObject.MSGTYPE_CANCEL, element);
					SystemBasePlugin.logDebugMessage(this.getClass().getName(), "Filter resolving cancelled by user."); //$NON-NLS-1$
				}
				catch (Exception exc)
				{
					children = new SystemMessageObject[1];
					children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_FAILED), ISystemMessageObject.MSGTYPE_ERROR, element);
					SystemBasePlugin.logError("Exception resolving filters' strings ", exc); //$NON-NLS-1$
					return null;
				} // message already issued

				return checkForEmptyList(children, element, true);
			}
		}
	}

	/**
	 * Return true if this object has children.
	 * That is, if the referenced filter has nested filters or filter strings.
	 */
	public boolean hasChildren(IAdaptable element)
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
		if (name.equalsIgnoreCase("filterType")) //$NON-NLS-1$
		{
			ISystemFilterReference ref = getFilterReference(target);
			String type = ref.getReferencedFilter().getType();
			if ((type == null) || (type.length() == 0))
				return false;
			else
				return value.equals(type);
		}
		else if (name.equalsIgnoreCase("showChangeFilterStringPropertyPage")) //$NON-NLS-1$
		{
			ISystemFilterReference ref = getFilterReference(target);
			ISubSystemConfiguration ssf = SubSystemHelpers.getParentSubSystemConfiguration(ref.getReferencedFilter());
			if (value.equals("true")) //$NON-NLS-1$
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
			propertyDescriptorArray[idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_PARENT_FILTERPOOL, SystemViewResources.RESID_PROPERTY_FILTERPARENTPOOL_LABEL, SystemViewResources.RESID_PROPERTY_FILTERPARENTPOOL_TOOLTIP);
			// parent filter
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_PARENT_FILTER, SystemViewResources.RESID_PROPERTY_FILTERPARENTFILTER_LABEL, SystemViewResources.RESID_PROPERTY_FILTERPARENTFILTER_TOOLTIP);
			// number filter strings
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILTERSTRINGS_COUNT, SystemViewResources.RESID_PROPERTY_FILTERSTRINGS_COUNT_LABEL, SystemViewResources.RESID_PROPERTY_FILTERSTRINGS_COUNT_TOOLTIP);
			// Related connection
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_IS_CONNECTION_PRIVATE, SystemViewResources.RESID_PROPERTY_FILTERPOOLREFERENCE_IS_CONNECTIONPRIVATE_LABEL, SystemViewResources.RESID_PROPERTY_FILTERPOOLREFERENCE_IS_CONNECTIONPRIVATE_TOOLTIP);
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
	public boolean doRename(Shell shell, Object element, String name, IProgressMonitor monitor) throws Exception
	{
		ISystemFilter filter = getFilter(element);
		ISystemFilterPoolManager fpMgr = filter.getSystemFilterPoolManager();
		fpMgr.renameSystemFilter(filter, name);
		return true;
	}

	/**
	 * Return a validator for verifying the new name is correct.
	 * @param element either a filter for a rename action, or a filter pool for a "new" action.
	 */
	public ISystemValidator getNameValidator(Object element) {
		ISystemFilter filter = null;
		ISystemFilterPool pool = null;
		String[] filterNames = null;
		if (element instanceof ISystemFilterReference) {
			filter = getFilter(element);
			pool = filter.getParentFilterPool();
			if (pool != null)
				filterNames = pool.getSystemFilterNames();
			else {
				ISystemFilter parentFilter = filter.getParentFilter();
				filterNames = parentFilter.getSystemFilterNames();
			}
		} else if (element instanceof ISystemFilter) {
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
		return (mgrName + "." + filter.getParentFilterPool().getName() + "." + newName).toUpperCase(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Don't show "Open in new perspective" if this is promptable
	 */
	public boolean showOpenViewActions(Object element)
	{
		ISystemFilter filter = getFilter(element);
		ISubSystemConfiguration ssParentFactory = getSubSystemConfiguration(filter);
		return !filter.isPromptable() && !ssParentFactory.supportsCommands();
	}


	/**
	  * Don't show generic "Show in Table" if the factory asks not to
	  */
	public boolean showGenericShowInTableAction(Object element)
	{
		ISystemFilter filter = getFilter(element);
		ISubSystemConfiguration ssParentFactory = getSubSystemConfiguration(filter);
		return ssParentFactory.showGenericShowInTableOnFilter() && !filter.isPromptable() && !ssParentFactory.supportsCommands();
	}

	/**
	 * Return true if we should show the refresh action in the popup for the element.
	 */
	public boolean showRefresh(Object element)
	{
		ISystemFilter filter = getFilter(element);
		ISubSystemConfiguration ssParentFactory = getSubSystemConfiguration(filter);
		return ssParentFactory.showRefreshOnFilter() && !filter.isPromptable() && !ssParentFactory.supportsCommands();
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
		String handle = pool.getReferenceName() + "="; //$NON-NLS-1$
		ISystemFilter parentFilter = referencedFilter.getParentFilter();
		while (parentFilter != null)
		{
			handle += parentFilter.getName() + ";"; //$NON-NLS-1$
			parentFilter = parentFilter.getParentFilter();
		}
		handle += referencedFilter.getName();
		return handle;
	}
	/**
	 * Return what to save to disk to identify this element when it is the input object to a secondary
	 *  Remote System Explorer perspective.
	 */
	public String getInputMementoHandle(Object element)
	{
		Object parent = ((ISystemFilterReference) element).getParent(); //getParent(element); // will be filter (nested) or filter pool
		ISystemViewElementAdapter parentAdapter = SystemAdapterHelpers.getViewAdapter(parent, getViewer());
		boolean showFPs = SystemPreferencesManager.getShowFilterPools();
		if (parent instanceof ISystemFilterPoolReference) // not a nested filter
		{
			if (!showFPs) // not showing the real parent in GUI?
			{
				parent = parentAdapter.getParent(parent); // get the subsystem parent of the filter pool reference
				parentAdapter = SystemAdapterHelpers.getViewAdapter(parent, getViewer()); // get the adapter for the subsystem parent
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
	        if (factory.supportsDropInFilters() && !fRef.getReferencedFilter().isNonChangable())
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
	 * Defer to the subsystem configuration.
	 */
	public boolean supportsDeferredQueries(ISubSystem subSys)
	{
	    return subSys.getSubSystemConfiguration().supportsDeferredQueries();
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
	
	/**
	 * Overriding since filters are case-sensitive and baseclass does it case-insensitive
	 */
   public boolean namesAreEqual(Object element, String newName)
    {
    	return getName(element).equals(newName);
    }
}

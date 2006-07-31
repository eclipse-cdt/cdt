/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.AbstractGroupMarker;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ActionDescriptor;
import org.eclipse.ui.internal.ActionExpression;
import org.eclipse.ui.internal.IObjectActionContributor;
import org.eclipse.ui.internal.ObjectFilterTest;
import org.eclipse.ui.internal.ObjectPluginAction;
import org.eclipse.ui.internal.PluginAction;
import org.eclipse.ui.internal.PluginActionBuilder;
import org.eclipse.ui.internal.PluginActionContributionItem;

/**
 * This class parses the <objectContributor> tag from our 
 * org.eclipse.rse.core.PopupMenus extension point.
 * <p>
 * We modelled our org.eclipse.rse.core.popupMenus extension point 
 * after the org.eclipse.ui.popupMenus extension point, so it makes sense to model 
 * the code to support it after the Eclipse code to support theirs.
 * To that end, we have to replace the class that parses the <objectContribution> tag, 
 * because that class does not support subclassing, and change it to:
 * <ul>
 * <li>Ignore all processing of the objectClass attribute, because we don't have one.
 * <li>Add processing for the filter attributes we added: subsystemconfigurationid, 
 * namefilter, typecategoryfilter, 
 * typefilter, subtypefilter, subsubtypefilter
 * </ul>
 * 
 * TODO use overrides list
 * 
 * @see SystemPopupMenuActionContributorManager
 */
public class SystemPopupMenuActionContributor extends PluginActionBuilder implements IObjectActionContributor {

	private static final String ATT_TYPE_CATEGORY_FILTER = "typecategoryfilter"; //$NON-NLS-1$
	private static final String ATT_NAME_FILTER = "namefilter"; //$NON-NLS-1$
	private static final String ATT_TYPE_FILTER = "typefilter"; //$NON-NLS-1$
	private static final String ATT_SUBTYPE_FILTER = "subtypefilter"; //$NON-NLS-1$
	private static final String ATT_SUBSUBTYPE_FILTER = "subsubtypefilter"; //$NON-NLS-1$
	private static final String ATT_SUBSYSTEM_FACTORY_ID = "subsystemconfigurationid"; //$NON-NLS-1$
	private static final String ATT_SUBSYSTEM_FACTORY_CATEGORY = "subsystemconfigurationCategory"; //$NON-NLS-1$
	private static final String ATT_SYSTEM_TYPES = "systemTypes"; //$NON-NLS-1$
	private static final String ATT_ID = "id"; //$NON-NLS-1$
	private static final String ATT_NAME = "name"; //$NON-NLS-1$
	private static final String ATT_LABEL = "label"; //$NON-NLS-1$
	private static final String ATT_PATH = "path"; //$NON-NLS-1$
	private static final String TAG_OBJECT_CONTRIBUTION = "objectContribution";//$NON-NLS-1$
	private static final String TAG_MENU = "menu"; //$NON-NLS-1$
	private static final String TAG_ACTION = "action"; //$NON-NLS-1$
	private static final String TAG_SEPARATOR = "separator"; //$NON-NLS-1$
	private static final String TAG_FILTER = "filter"; //$NON-NLS-1$
	private static final String TAG_VISIBILITY = "visibility"; //$NON-NLS-1$
	private static final String TAG_GROUP_MARKER = "groupMarker"; //$NON-NLS-1$
	private IConfigurationElement config;
	private List cachedMenus;
	private List cachedActions;
	private boolean configRead = false;
	private ObjectFilterTest filterTest;
	private ActionExpression visibilityTest;
	private SystemRemoteObjectMatcher matcher = null;

	/**
	 * Constructor for SystemPopupMenuActionContributor
	 */
	public SystemPopupMenuActionContributor(IConfigurationElement element) {
		super();
		config = element;
		cachedMenus = new ArrayList();
		cachedActions = new ArrayList();
		String categoryfilter = element.getAttribute(ATT_TYPE_CATEGORY_FILTER);
		String namefilter = element.getAttribute(ATT_NAME_FILTER);
		String typefilter = element.getAttribute(ATT_TYPE_FILTER);
		String subtypefilter = element.getAttribute(ATT_SUBTYPE_FILTER);
		String subsubtypefilter = element.getAttribute(ATT_SUBSUBTYPE_FILTER);
		String subsystemfilter = element.getAttribute(ATT_SUBSYSTEM_FACTORY_ID);
		String subsystemCategoryFilter = element.getAttribute(ATT_SUBSYSTEM_FACTORY_CATEGORY);
		String systypes = element.getAttribute(ATT_SYSTEM_TYPES);
		matcher = new SystemRemoteObjectMatcher(subsystemfilter, subsystemCategoryFilter, categoryfilter, systypes, namefilter, typefilter, subtypefilter, subsubtypefilter);
	}

	/**
	 * Return what was specified for the <samp>typecategoryfilter</samp> xml attribute.
	 */
	public String getCategoryFilter() {
		return matcher.getCategoryFilter();
	}

	/**
	 * Return what was specified for the <samp>namefilter</samp> xml attribute.
	 */
	public String getNameFilter() {
		return matcher.getNameFilter();
	}

	/**
	 * Return what was specified for the <samp>typefilter</samp> xml attribute.
	 */
	public String getTypeFilter() {
		return matcher.getTypeFilter();
	}

	/**
	 * Return what was specified for the <samp>subtypefilter</samp> xml attribute.
	 */
	public String getSubTypeFilter() {
		return matcher.getSubTypeFilter();
	}

	/**
	 * Return what was specified for the <samp>subsubtypefilter</samp> xml attribute.
	 */
	public String getSubSubTypeFilter() {
		return matcher.getSubSubTypeFilter();
	}

	/**
	 * Return what was specified for the <samp>subsystemconfigurationid</samp> xml attribute.
	 */
	public String getSubSystemConfigurationId() {
		return matcher.getSubSystemConfigurationId();
	}

	/**
	 * Return what was specified for the <samp>subsystemconfigurationCategory</samp> xml attribute.
	 */
	public String getSubSystemConfigurationCategoryFilter() {
		return matcher.getSubSystemConfigurationCategoryFilter();
	}

	/**
	 * Return what was specified for the <samp>systemTypes</samp> xml attribute.
	 */
	public String getSystemTypesFilter() {
		return matcher.getSystemTypesFilter();
	}

	/**
	 * Returns the implementation of ISystemRemoteElement for the given
	 * object.  Returns null if this object is not adaptable to this.
	 */
	private ISystemRemoteElementAdapter getRemoteAdapter(Object object) {
		Object adapter = null;
		if (object instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) object;
			adapter = adaptable.getAdapter(ISystemRemoteElementAdapter.class);
		} else {
			IAdapterManager adapterManager = Platform.getAdapterManager();
			adapter = adapterManager.getAdapter(object, ISystemRemoteElementAdapter.class);
		}
		return (ISystemRemoteElementAdapter) adapter;
	}

	/**
	 * Contributes actions applicable for the current selection.
	 */
	public boolean contributeObjectActions(IWorkbenchPart part, SystemMenuManager menu, ISelectionProvider selProv, List actionIdOverrides) {
		return contributeObjectActions(part, (IMenuManager) menu.getMenuManager(), selProv, actionIdOverrides);
	}

	/**
	 * Contributes actions applicable for the current selection.
	 */
	public boolean contributeObjectActions(IWorkbenchPart part, IMenuManager menu, ISelectionProvider selProv, List actionIdOverrides) {
		// Parse configuration
		readConfigElement();
		if (cache == null) {
			return false;
		}
		if (cachedActions.size() == 0) {
			return false;
		}
		// Get a structured selection.	
		ISelection sel = selProv.getSelection();
		if ((sel == null) || !(sel instanceof IStructuredSelection)) {
			return false;
		}
		IStructuredSelection selection = (IStructuredSelection) sel;
		// Generate actions.
		boolean actualContributions = false;
		for (int i = 0; i < cachedActions.size(); i++) {
			Object obj = cachedActions.get(i);
			if (obj instanceof ActionDescriptor) {
				ActionDescriptor ad = (ActionDescriptor) obj;
				contributeMenuAction(ad, menu, true);
				// Update action for the current selection and part.
				if (ad.getAction() instanceof ObjectPluginAction) {
					ObjectPluginAction action = (ObjectPluginAction) ad.getAction();
					//String actionText = action.getText(); // for debugging
					action.selectionChanged(selection);
					//System.out.println("action " + actionText + " enabled? " + action.isEnabled());
					action.setActivePart(part);
				}
				actualContributions = true;
			}
		}
		return actualContributions;
	}

	/**
	 * Contribute to the list the action identifiers from other contributions that 
	 * this contribution wants to override. Actions of these identifiers will
	 * not be contributed.
	 * @see IObjectActionContributor
	 */
	public void contributeObjectActionIdOverrides(List actionIdOverrides) {
		readConfigElement();
		// TODO: need to implement at some point
	}

	/**
	 * Contributes menus applicable for the current selection.
	 */
	public boolean contributeObjectMenus(SystemMenuManager menu, ISelectionProvider selProv) {
		return contributeObjectMenus((IMenuManager) menu.getMenuManager(), selProv);
	}

	/**
	 * Contributes menus applicable for the current selection.
	 * @see IObjectActionContributor
	 */
	public boolean contributeObjectMenus(IMenuManager menu, ISelectionProvider selProv) {
		// Parse config element.
		readConfigElement();
		if (cache == null) {
			return false;
		}
		if (cachedMenus.size() == 0) {
			return false;
		}
		// Get a structured selection.	
		ISelection sel = selProv.getSelection();
		if ((sel == null) || !(sel instanceof IStructuredSelection)) {
			return false;
		}
		// Generate menus.
		boolean actualContributions = false;
		for (int i = 0; i < cachedMenus.size(); i++) {
			Object obj = cachedMenus.get(i);
			if (obj instanceof IConfigurationElement) {
				IConfigurationElement menuElement = (IConfigurationElement) obj;
				contributeMenu(menuElement, menu, true);
				actualContributions = true;
			}
		}
		return actualContributions;
	}

	/**
	 * This factory method returns a new ActionDescriptor for the
	 * configuration element.  
	 */
	protected ActionDescriptor createActionDescriptor(IConfigurationElement element) {
		return new ActionDescriptor(element, ActionDescriptor.T_POPUP);
	}

	/**
	 * Returns true if the current selection matches all the given filtering criteria.
	 */
	public boolean isApplicableTo(Object object) {
		readConfigElement();
		ISystemRemoteElementAdapter adapter = getRemoteAdapter(object);
		boolean matches = (adapter != null);
		matches = matches && (visibilityTest == null || visibilityTest.isEnabledFor(object));
		matches = matches && (filterTest == null || filterTest.matches(object, true));
		matches = matches && matcher.appliesTo(adapter, object);
		return matches;
	}

	/**
	 * Reads the configuration element and all the children.
	 * This creates an action descriptor for every action in the extension.
	 */
	private void readConfigElement() {
		if (!configRead) {
			currentContribution = createContribution();
			readElementChildren(config);
			if (cache == null) cache = new ArrayList(4);
			cache.add(currentContribution);
			currentContribution = null;
			configRead = true;
		}
	}

	protected void readContributions(String id, String tag, String extensionPoint) {
		cachedMenus.clear();
		cachedActions.clear();
		super.readContributions(id, tag, extensionPoint);
	}

	/**
	 * Implements abstract method to handle the provided XML element
	 * in the registry.
	 */
	protected boolean readElement(IConfigurationElement element) {
		String tag = element.getName();
		if (tag.equals(TAG_VISIBILITY)) {
			visibilityTest = new ActionExpression(element);
			return true;
		} 
		if (tag.equals(TAG_FILTER)) {
			if (filterTest == null) filterTest = new ObjectFilterTest();
			filterTest.addFilterElement(element);
			return true;
		}
		// Ignore all object contributions element as these
		// are handled by the ObjectActionContributorReader.
		if (tag.equals(TAG_OBJECT_CONTRIBUTION)) {
			return true;
		}
		// Found top level contribution element		
		if (tag.equals(targetContributionTag)) {
			if (targetID != null) {
				// Ignore contributions not matching target id
				String id = getTargetID(element);
				if (id == null || !id.equals(targetID)) return true;
			}
			// Read it's sub-elements
			currentContribution = createContribution();
			readElementChildren(element);
			if (cache == null) cache = new ArrayList(4);
			cache.add(currentContribution);
			currentContribution = null;
			return true;
		}
		// Found menu contribution sub-element		
		if (tag.equals(TAG_MENU)) {
			currentContribution.addMenu(element);
			cachedMenus.add(element);
			return true;
		}
		// Found action contribution sub-element
		if (tag.equals(TAG_ACTION)) {
			ActionDescriptor ades = createActionDescriptor(element);
			currentContribution.addAction(ades);
			cachedActions.add(ades);
			return true;
		}
		return false;
	}

//	/**
//	 * get the root part of the path
//	 */
//	protected String getPathRoot(String path) {
//		int loc = path.indexOf('/');
//		if (loc != -1) {
//			if (loc > 0)
//				return path.substring(0, loc);
//			else
//				return ""; // should never happen!
//		} else {
//			return path;
//		}
//	}

	/*
	 * @see IObjectContributor#canAdapt()
	 */
	public boolean canAdapt() {
		return false;
	}

	/**** EVERYTHING BELOW IS HACK ***/
	/**
	 * Creates a menu from the information in the menu configuration element and
	 * adds it into the provided menu manager. If 'appendIfMissing' is true, and
	 * menu path slot is not found, it will be created and menu will be added
	 * into it. Otherwise, add operation will fail.
	 */
	protected void contributeMenu(IConfigurationElement menuElement, IMenuManager mng, boolean appendIfMissing) {
		// Get config data.
		String id = menuElement.getAttribute(ATT_ID);
		String label = menuElement.getAttribute(ATT_LABEL);
		String path = menuElement.getAttribute(ATT_PATH);
		if (label == null) {
			SystemBasePlugin.logInfo("Invalid Menu Extension (label == null): " + id); //$NON-NLS-1$
			return;
		}
		// Calculate menu path and group.
		String group = null;
		if (path != null) {
			int loc = path.lastIndexOf('/');
			if (loc != -1) {
				group = path.substring(loc + 1);
				path = path.substring(0, loc);
			} else {
				// assume that path represents a slot
				// so actual path portion should be null
				group = path;
				path = null;
			}
		}
		// Find parent menu.
		IMenuManager parent = mng;
		if (path != null) {
			parent = mng.findMenuUsingPath(path);
			if (parent == null) {
				SystemBasePlugin.logInfo("Invalid Menu Extension (Path is invalid): " + id); //$NON-NLS-1$
				return;
			}
		}
		// Find reference group.
		if (group == null) group = IWorkbenchActionConstants.MB_ADDITIONS;
		IContributionItem sep = parent.find(group);
		if (sep == null) {
			if (appendIfMissing)
				addGroup(parent, group);
			else {
				SystemBasePlugin.logInfo("Invalid Menu Extension (Group is invalid): " + id); //$NON-NLS-1$
				return;
			}
		}
		// If the menu does not exist create it.
		IMenuManager newMenu = parent.findMenuUsingPath(id);
		if (newMenu == null) newMenu = new MenuManager(label, id);
		// Add the menu
		try {
			insertAfter(parent, group, newMenu);
		} catch (IllegalArgumentException e) {
			SystemBasePlugin.logInfo("Invalid Menu Extension (Group is missing): " + id); //$NON-NLS-1$
		}
		// Get the menu again as it may be wrapped, otherwise adding
		// the separators and group markers below will not be wrapped
		// properly if the menu was just created.
		newMenu = parent.findMenuUsingPath(id);
		if (newMenu == null) SystemBasePlugin.logInfo("Could not find new menu: " + id); //$NON-NLS-1$
		// Create separators.
		IConfigurationElement[] children = menuElement.getChildren();
		for (int i = 0; i < children.length; i++) {
			String childName = children[i].getName();
			if (childName.equals(TAG_SEPARATOR)) {
				contributeSeparator(newMenu, children[i]);
			} else if (childName.equals(TAG_GROUP_MARKER)) {
				contributeGroupMarker(newMenu, children[i]);
			}
		}
	}

	/**
	 * Contributes action from action descriptor into the provided menu manager.
	 */
	protected void contributeMenuAction(ActionDescriptor ad, IMenuManager menu, boolean appendIfMissing) {
		// Get config data.
		String mpath = ad.getMenuPath();
		String mgroup = ad.getMenuGroup();
		if (mpath == null && mgroup == null) return;
		// Find parent menu.
		IMenuManager parent = menu;
		if (mpath != null) {
			parent = parent.findMenuUsingPath(mpath);
			if (parent == null) {
				SystemBasePlugin.logInfo("Invalid Menu Extension (Path is invalid): " + ad.getId()); //$NON-NLS-1$
				return;
			}
		}
		// Find reference group.
		if (mgroup == null) mgroup = IWorkbenchActionConstants.MB_ADDITIONS;
		IContributionItem sep = parent.find(mgroup);
		if (sep == null) {
			if (appendIfMissing)
				addGroup(parent, mgroup);
			else {
				SystemBasePlugin.logInfo("Invalid Menu Extension (Group is invalid): " + ad.getId()); //$NON-NLS-1$
				return;
			}
		}
		// Add action.
		try {
			insertAfter(parent, mgroup, ad.getAction());
		} catch (IllegalArgumentException e) {
			SystemBasePlugin.logInfo("Invalid Menu Extension (Group is missing): " + ad.getId()); //$NON-NLS-1$
		}
	}

	/**
	 * Creates a named menu separator from the information in the configuration element.
	 * If the separator already exists do not create a second.
	 */
	protected void contributeSeparator(IMenuManager menu, IConfigurationElement element) {
		String id = element.getAttribute(ATT_NAME);
		if (id == null || id.length() <= 0) return;
		IContributionItem sep = menu.find(id);
		if (sep != null) return;
		insertMenuGroup(menu, new Separator(id));
	}

	/**
	 * Creates a named menu group marker from the information in the configuration element.
	 * If the marker already exists do not create a second.
	 */
	protected void contributeGroupMarker(IMenuManager menu, IConfigurationElement element) {
		String id = element.getAttribute(ATT_NAME);
		if (id == null || id.length() <= 0) return;
		IContributionItem marker = menu.find(id);
		if (marker != null) return;
		insertMenuGroup(menu, new GroupMarker(id));
	}

	/**
	 * Contributes action from the action descriptor into the provided tool bar manager.
	 */
	protected void contributeToolbarAction(ActionDescriptor ad, IToolBarManager toolbar, boolean appendIfMissing) {
		// Get config data.
		String tId = ad.getToolbarId();
		String tgroup = ad.getToolbarGroupId();
		if (tId == null && tgroup == null) return;
		// Find reference group.
		if (tgroup == null) tgroup = IWorkbenchActionConstants.MB_ADDITIONS;
		IContributionItem sep = null;
		sep = toolbar.find(tgroup);
		if (sep == null) {
			if (appendIfMissing) {
				addGroup(toolbar, tgroup);
			} else {
				SystemBasePlugin.logInfo("Invalid Toolbar Extension (Group is invalid): " + ad.getId()); //$NON-NLS-1$
				return;
			}
		}
		// Add action to tool bar.
		try {
			insertAfter(toolbar, tgroup, ad.getAction());
		} catch (IllegalArgumentException e) {
			SystemBasePlugin.logInfo("Invalid Toolbar Extension (Group is missing): " + ad.getId()); //$NON-NLS-1$
		}
	}

	/**
	 * Inserts the separator or group marker into the menu. Subclasses may override.
	 */
	protected void insertMenuGroup(IMenuManager menu, AbstractGroupMarker marker) {
		menu.add(marker);
	}

	/**
	 * Inserts an action after another named contribution item.
	 * Subclasses may override.
	 */
	protected void insertAfter(IContributionManager mgr, String refId, PluginAction action) {
		insertAfter(mgr, refId, new PluginActionContributionItem(action));
	}

	/**
	 * Inserts a contribution item after another named contribution item.
	 * Subclasses may override.
	 */
	protected void insertAfter(IContributionManager mgr, String refId, IContributionItem item) {
		mgr.insertAfter(refId, item);
	}

	/**
	 * Adds a group to a contribution manager.
	 * Subclasses may override.
	 */
	protected void addGroup(IContributionManager mgr, String name) {
		mgr.add(new Separator(name));
	}
}
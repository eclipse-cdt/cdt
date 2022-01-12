/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.internal.ui.filters.CustomFiltersDialog;
import org.eclipse.cdt.internal.ui.filters.FilterDescriptor;
import org.eclipse.cdt.internal.ui.filters.FilterMessages;
import org.eclipse.cdt.internal.ui.filters.NamePatternFilter;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionGroup;

/**
 * Action group to add the filter action to a view part's tool bar
 * menu.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @since 2.0
 */
public class CustomFiltersActionGroup extends ActionGroup {

	class ShowFilterDialogAction extends Action {
		ShowFilterDialogAction() {
			setText(FilterMessages.OpenCustomFiltersDialogAction_text);
			//setImageDescriptor(CPluginImages.DESC_CLCL_FILTER);
		}

		@Override
		public void run() {
			openDialog();
		}
	}

	/**
	 * Menu contribution item which shows and lets check and uncheck filters.
	 *
	 * @since 3.0
	 */
	class FilterActionMenuContributionItem extends ContributionItem {

		private int fItemNumber;
		boolean fState;
		String fFilterId;
		private String fFilterName;
		CustomFiltersActionGroup fActionGroup;

		/**
		 * Constructor for FilterActionMenuContributionItem.
		 *
		 * @param actionGroup 	the action group
		 * @param filterId		the id of the filter
		 * @param filterName	the name of the filter
		 * @param state			the initial state of the filter
		 * @param itemNumber	the menu item index
		 */
		public FilterActionMenuContributionItem(CustomFiltersActionGroup actionGroup, String filterId,
				String filterName, boolean state, int itemNumber) {
			super(filterId);
			Assert.isNotNull(actionGroup);
			Assert.isNotNull(filterId);
			Assert.isNotNull(filterName);
			fActionGroup = actionGroup;
			fFilterId = filterId;
			fFilterName = filterName;
			fState = state;
			fItemNumber = itemNumber;
		}

		/*
		 * Overrides method from ContributionItem.
		 */
		@Override
		public void fill(Menu menu, int index) {
			MenuItem mi = new MenuItem(menu, SWT.CHECK, index);
			mi.setText("&" + fItemNumber + " " + fFilterName); //$NON-NLS-1$  //$NON-NLS-2$
			/*
			 * XXX: Don't set the image - would look bad because other menu items don't provide image
			 * XXX: Get working set specific image name from XML - would need to cache icons
			 */
			//			mi.setImage(JavaPluginImages.get(JavaPluginImages.IMG_OBJS_JAVA_WORKING_SET));
			mi.setSelection(fState);
			mi.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					fState = !fState;
					fActionGroup.setFilter(fFilterId, fState);
				}
			});
		}

		/*
		 * @see org.eclipse.jface.action.IContributionItem#isDynamic()
		 */
		@Override
		public boolean isDynamic() {
			return true;
		}
	}

	private static final String TAG_CUSTOM_FILTERS = "customFilters"; //$NON-NLS-1$
	private static final String TAG_USER_DEFINED_PATTERNS_ENABLED = "userDefinedPatternsEnabled"; //$NON-NLS-1$
	private static final String TAG_USER_DEFINED_PATTERNS = "userDefinedPatterns"; //$NON-NLS-1$
	private static final String TAG_XML_DEFINED_FILTERS = "xmlDefinedFilters"; //$NON-NLS-1$
	private static final String TAG_LRU_FILTERS = "lastRecentlyUsedFilters"; //$NON-NLS-1$

	private static final String TAG_CHILD = "child"; //$NON-NLS-1$
	private static final String TAG_PATTERN = "pattern"; //$NON-NLS-1$
	private static final String TAG_FILTER_ID = "filterId"; //$NON-NLS-1$
	private static final String TAG_IS_ENABLED = "isEnabled"; //$NON-NLS-1$

	private static final String SEPARATOR = ","; //$NON-NLS-1$

	private static final int MAX_FILTER_MENU_ENTRIES = 3;
	private static final String RECENT_FILTERS_GROUP_NAME = "recentFiltersGroup"; //$NON-NLS-1$

	private StructuredViewer fViewer;

	private NamePatternFilter fPatternFilter;
	private Map<String, ViewerFilter> fInstalledBuiltInFilters;

	private Map<String, Boolean> fEnabledFilterIds;
	private boolean fUserDefinedPatternsEnabled;
	private String[] fUserDefinedPatterns;
	/**
	 * Recently changed filter Ids stack with oldest on top (i.e. at the end).
	 *
	 * @since 3.0
	 */
	private Stack<String> fLRUFilterIdsStack;
	/**
	 * Handle to menu manager to dynamically update
	 * the last recently used filters.
	 *
	 * @since 3.0
	 */
	private IMenuManager fMenuManager;
	/**
	 * The menu listener which dynamically updates
	 * the last recently used filters.
	 *
	 * @since 3.0
	 */
	private IMenuListener fMenuListener;
	/**
	 * Filter Ids used in the last view menu invocation.
	 *
	 * @since 3.0
	 */
	private String[] fFilterIdsUsedInLastViewMenu;
	private HashMap<String, FilterDescriptor> fFilterDescriptorMap;
	private String fTargetId;

	/**
	 * Creates a new <code>CustomFiltersActionGroup</code>.
	 *
	 * @param part		the view part that owns this action group
	 * @param viewer	the viewer to be filtered
	 */
	public CustomFiltersActionGroup(IViewPart part, StructuredViewer viewer) {
		this(part.getViewSite().getId(), viewer);
	}

	/**
	 * Creates a new <code>CustomFiltersActionGroup</code>.
	 *
	 * @param ownerId	the id of this action group's owner
	 * @param viewer	the viewer to be filtered
	 */
	public CustomFiltersActionGroup(String ownerId, StructuredViewer viewer) {
		Assert.isNotNull(ownerId);
		Assert.isNotNull(viewer);
		fTargetId = ownerId;
		fViewer = viewer;

		fLRUFilterIdsStack = new Stack<>();

		initializeWithPluginContributions();
		initializeWithViewDefaults();

		installFilters();
	}

	/*
	 * Method declared on ActionGroup.
	 */
	@Override
	public void fillActionBars(IActionBars actionBars) {
		fillToolBar(actionBars.getToolBarManager());
		fillViewMenu(actionBars.getMenuManager());
	}

	public String[] removeFiltersFor(Object parent, Object element, IContentProvider contentProvider) {
		String[] enabledFilters = getEnabledFilterIds();
		Set<String> newFilters = new HashSet<>();
		for (String filterName : enabledFilters) {
			ViewerFilter filter = fInstalledBuiltInFilters.get(filterName);
			if (filter == null)
				newFilters.add(filterName);
			else if (isSelected(parent, element, contentProvider, filter))
				newFilters.add(filterName);
		}
		if (newFilters.size() == enabledFilters.length)
			return new String[0];
		return newFilters.toArray(new String[newFilters.size()]);
	}

	public void setFilters(String[] newFilters) {
		setEnabledFilterIds(newFilters);
		updateViewerFilters(true);
	}

	private boolean isSelected(Object parent, Object element, IContentProvider contentProvider, ViewerFilter filter) {
		if (contentProvider instanceof ITreeContentProvider) {
			// the element and all its parents have to be selected
			ITreeContentProvider provider = (ITreeContentProvider) contentProvider;
			while (element != null && !(element instanceof ICModel)) {
				if (!filter.select(fViewer, parent, element))
					return false;
				element = provider.getParent(element);
			}
			return true;
		}
		return filter.select(fViewer, parent, element);
	}

	/**
	 * Sets the enable state of the given filter.
	 *
	 * @param filterId the id of the filter
	 * @param state the filter state
	 */
	void setFilter(String filterId, boolean state) {
		// Renew filter id in LRU stack
		fLRUFilterIdsStack.remove(filterId);
		fLRUFilterIdsStack.add(0, filterId);

		fEnabledFilterIds.put(filterId, Boolean.valueOf(state));
		storeViewDefaults();

		updateViewerFilters(true);
	}

	private String[] getEnabledFilterIds() {
		Set<String> enabledFilterIds = new HashSet<>(fEnabledFilterIds.size());
		Iterator<Map.Entry<String, Boolean>> iter = fEnabledFilterIds.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Boolean> entry = iter.next();
			String id = entry.getKey();
			boolean isEnabled = (entry.getValue()).booleanValue();
			if (isEnabled)
				enabledFilterIds.add(id);
		}
		return enabledFilterIds.toArray(new String[enabledFilterIds.size()]);
	}

	private void setEnabledFilterIds(String[] enabledIds) {
		Iterator<String> iter = fEnabledFilterIds.keySet().iterator();
		while (iter.hasNext()) {
			String id = iter.next();
			fEnabledFilterIds.put(id, Boolean.FALSE);
		}
		for (String enabledId : enabledIds)
			fEnabledFilterIds.put(enabledId, Boolean.TRUE);
	}

	private void setUserDefinedPatterns(String[] patterns) {
		fUserDefinedPatterns = patterns;
		cleanUpPatternDuplicates();
	}

	/**
	 * Sets the recently changed filters.
	 *
	 * @param changeHistory the change history
	 * @since 3.0
	 */
	private void setRecentlyChangedFilters(Stack<FilterDescriptor> changeHistory) {
		Stack<String> oldestFirstStack = new Stack<>();

		int length = Math.min(changeHistory.size(), MAX_FILTER_MENU_ENTRIES);
		for (int i = 0; i < length; i++)
			oldestFirstStack.push((changeHistory.pop()).getId());

		length = Math.min(fLRUFilterIdsStack.size(), MAX_FILTER_MENU_ENTRIES - oldestFirstStack.size());
		int NEWEST = 0;
		for (int i = 0; i < length; i++) {
			String filter = fLRUFilterIdsStack.remove(NEWEST);
			if (!oldestFirstStack.contains(filter))
				oldestFirstStack.push(filter);
		}
		fLRUFilterIdsStack = oldestFirstStack;
	}

	private boolean areUserDefinedPatternsEnabled() {
		return fUserDefinedPatternsEnabled;
	}

	private void setUserDefinedPatternsEnabled(boolean state) {
		fUserDefinedPatternsEnabled = state;
	}

	private void fillToolBar(IToolBarManager tooBar) {
	}

	public void fillViewMenu(IMenuManager viewMenu) {
		/*
		 * Don't change the separator group name.
		 * Using this name ensures that other filters
		 * get contributed to the same group.
		 */
		viewMenu.add(new Separator("filters")); //$NON-NLS-1$
		viewMenu.add(new GroupMarker(RECENT_FILTERS_GROUP_NAME));
		viewMenu.add(new ShowFilterDialogAction());

		fMenuManager = viewMenu;
		fMenuListener = new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				removePreviousLRUFilterActions(manager);
				addLRUFilterActions(manager);
			}
		};
		fMenuManager.addMenuListener(fMenuListener);
	}

	void removePreviousLRUFilterActions(IMenuManager mm) {
		if (fFilterIdsUsedInLastViewMenu == null)
			return;

		for (String element : fFilterIdsUsedInLastViewMenu)
			mm.remove(element);
	}

	void addLRUFilterActions(IMenuManager mm) {
		if (fLRUFilterIdsStack.isEmpty()) {
			fFilterIdsUsedInLastViewMenu = null;
			return;
		}

		SortedSet<Object> sortedFilters = new TreeSet<>(fLRUFilterIdsStack);
		String[] recentlyChangedFilterIds = sortedFilters.toArray(new String[sortedFilters.size()]);

		fFilterIdsUsedInLastViewMenu = new String[recentlyChangedFilterIds.length];
		for (int i = 0; i < recentlyChangedFilterIds.length; i++) {
			String id = recentlyChangedFilterIds[i];
			fFilterIdsUsedInLastViewMenu[i] = id;
			boolean state = fEnabledFilterIds.containsKey(id) && fEnabledFilterIds.get(id).booleanValue();
			FilterDescriptor filterDesc = fFilterDescriptorMap.get(id);
			if (filterDesc != null) {
				IContributionItem item = new FilterActionMenuContributionItem(this, id, filterDesc.getName(), state,
						i + 1);
				mm.insertBefore(RECENT_FILTERS_GROUP_NAME, item);
			}
		}
	}

	/*
	 * Method declared on ActionGroup.
	 */
	@Override
	public void dispose() {
		if (fMenuManager != null)
			fMenuManager.removeMenuListener(fMenuListener);
		super.dispose();
	}

	private void initializeWithPluginContributions() {
		fUserDefinedPatterns = new String[0];
		fUserDefinedPatternsEnabled = false;

		FilterDescriptor[] filterDescs = FilterDescriptor.getFilterDescriptors(fTargetId);
		fFilterDescriptorMap = new HashMap<>(filterDescs.length);
		fEnabledFilterIds = new HashMap<>(filterDescs.length);
		for (FilterDescriptor filterDesc : filterDescs) {
			String id = filterDesc.getId();
			Boolean isEnabled = filterDesc.isEnabled();
			//if (fEnabledFilterIds.containsKey(id))
			//	CUIPlugin.log(new Status("WARNING: Duplicate id for extension-point \"org.eclipse.jdt.ui.CElementFilters\"")); //$NON-NLS-1$
			fEnabledFilterIds.put(id, isEnabled);
			fFilterDescriptorMap.put(id, filterDesc);
		}
	}

	// ---------- viewer filter handling ----------

	private void installFilters() {
		fInstalledBuiltInFilters = new HashMap<>(fEnabledFilterIds.size());
		fPatternFilter = new NamePatternFilter();
		fPatternFilter.setPatterns(getUserAndBuiltInPatterns());
		fViewer.addFilter(fPatternFilter);
		updateBuiltInFilters();
	}

	private void updateViewerFilters(boolean refresh) {
		String[] patterns = getUserAndBuiltInPatterns();
		fPatternFilter.setPatterns(patterns);
		fViewer.getControl().setRedraw(false);
		updateBuiltInFilters();
		if (refresh)
			fViewer.refresh();
		fViewer.getControl().setRedraw(true);
	}

	private void updateBuiltInFilters() {
		Set<String> installedFilters = fInstalledBuiltInFilters.keySet();
		Set<String> filtersToAdd = new HashSet<>(fEnabledFilterIds.size());
		Set<String> filtersToRemove = new HashSet<>(fEnabledFilterIds.size());
		Iterator<Map.Entry<String, Boolean>> iter = fEnabledFilterIds.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Boolean> entry = iter.next();
			String id = entry.getKey();
			boolean isEnabled = (entry.getValue()).booleanValue();
			if (isEnabled && !installedFilters.contains(id))
				filtersToAdd.add(id);
			else if (!isEnabled && installedFilters.contains(id))
				filtersToRemove.add(id);
		}

		// Install the filters
		FilterDescriptor[] filterDescs = FilterDescriptor.getFilterDescriptors(fTargetId);
		for (FilterDescriptor filterDesc : filterDescs) {
			String id = filterDesc.getId();
			// just to double check - id should denote a custom filter anyway
			boolean isCustomFilter = filterDesc.isCustomFilter();
			if (isCustomFilter) {
				if (filtersToAdd.contains(id)) {
					ViewerFilter filter = filterDesc.createViewerFilter();
					if (filter != null) {
						fViewer.addFilter(filter);
						fInstalledBuiltInFilters.put(id, filter);
					}
				}
				if (filtersToRemove.contains(id)) {
					fViewer.removeFilter(fInstalledBuiltInFilters.get(id));
					fInstalledBuiltInFilters.remove(id);
				}
			}
		}
	}

	private String[] getUserAndBuiltInPatterns() {
		List<String> patterns = new ArrayList<>(fUserDefinedPatterns.length);
		if (areUserDefinedPatternsEnabled())
			patterns.addAll(Arrays.asList(fUserDefinedPatterns));
		FilterDescriptor[] filterDescs = FilterDescriptor.getFilterDescriptors(fTargetId);
		for (FilterDescriptor filterDesc : filterDescs) {
			String id = filterDesc.getId();
			boolean isPatternFilter = filterDesc.isPatternFilter();
			Object isEnabled = fEnabledFilterIds.get(id);
			if (isEnabled != null && isPatternFilter && ((Boolean) isEnabled).booleanValue())
				patterns.add(filterDesc.getPattern());
		}
		return patterns.toArray(new String[patterns.size()]);
	}

	// ---------- view kind/defaults persistency ----------

	private void initializeWithViewDefaults() {
		// get default values for view
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();

		// XXX: can be removed once bug 22533 is fixed.
		if (!store.contains(getPreferenceKey("TAG_DUMMY_TO_TEST_EXISTENCE")))//$NON-NLS-1$
			return;

		// XXX: Uncomment once bug 22533 is fixed.
		//		if (!store.contains(getPreferenceKey(TAG_USER_DEFINED_PATTERNS_ENABLED)))
		//			return;

		fUserDefinedPatternsEnabled = store.getBoolean(getPreferenceKey(TAG_USER_DEFINED_PATTERNS_ENABLED));
		setUserDefinedPatterns(CustomFiltersDialog
				.convertFromString(store.getString(getPreferenceKey(TAG_USER_DEFINED_PATTERNS)), SEPARATOR));

		Iterator<String> iter = fEnabledFilterIds.keySet().iterator();
		while (iter.hasNext()) {
			String id = iter.next();
			Boolean isEnabled = store.getBoolean(id);
			fEnabledFilterIds.put(id, isEnabled);
		}

		fLRUFilterIdsStack.clear();
		String lruFilterIds = store.getString(TAG_LRU_FILTERS);
		StringTokenizer tokenizer = new StringTokenizer(lruFilterIds, SEPARATOR);
		while (tokenizer.hasMoreTokens()) {
			String id = tokenizer.nextToken();
			if (fFilterDescriptorMap.containsKey(id) && !fLRUFilterIdsStack.contains(id))
				fLRUFilterIdsStack.push(id);
		}
	}

	private void storeViewDefaults() {
		// get default values for view
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();

		// XXX: can be removed once bug 22533 is fixed.
		store.setValue(getPreferenceKey("TAG_DUMMY_TO_TEST_EXISTENCE"), "storedViewPreferences");//$NON-NLS-1$//$NON-NLS-2$

		store.setValue(getPreferenceKey(TAG_USER_DEFINED_PATTERNS_ENABLED), fUserDefinedPatternsEnabled);
		store.setValue(getPreferenceKey(TAG_USER_DEFINED_PATTERNS),
				CustomFiltersDialog.convertToString(fUserDefinedPatterns, SEPARATOR));

		Iterator<Entry<String, Boolean>> iter = fEnabledFilterIds.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Boolean> entry = iter.next();
			String id = entry.getKey();
			boolean isEnabled = (entry.getValue()).booleanValue();
			store.setValue(id, isEnabled);
		}

		StringBuilder buf = new StringBuilder(fLRUFilterIdsStack.size() * 20);
		Iterator<String> iterIds = fLRUFilterIdsStack.iterator();
		while (iterIds.hasNext()) {
			buf.append(iterIds.next());
			buf.append(SEPARATOR);
		}
		store.setValue(TAG_LRU_FILTERS, buf.toString());
	}

	private String getPreferenceKey(String tag) {
		return "CustomFiltersActionGroup." + fTargetId + '.' + tag; //$NON-NLS-1$
	}

	// ---------- view instance persistency ----------

	/**
	 * Saves the state of the custom filters in a memento.
	 *
	 * @param memento the memento into which the state is saved
	 */
	public void saveState(IMemento memento) {
		IMemento customFilters = memento.createChild(TAG_CUSTOM_FILTERS);
		customFilters.putString(TAG_USER_DEFINED_PATTERNS_ENABLED, String.valueOf(fUserDefinedPatternsEnabled));
		saveUserDefinedPatterns(customFilters);
		saveXmlDefinedFilters(customFilters);
		saveLRUFilters(customFilters);
	}

	private void saveXmlDefinedFilters(IMemento memento) {
		if (fEnabledFilterIds != null && !fEnabledFilterIds.isEmpty()) {
			IMemento xmlDefinedFilters = memento.createChild(TAG_XML_DEFINED_FILTERS);
			Iterator<Map.Entry<String, Boolean>> iter = fEnabledFilterIds.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, Boolean> entry = iter.next();
				String id = entry.getKey();
				Boolean isEnabled = entry.getValue();
				IMemento child = xmlDefinedFilters.createChild(TAG_CHILD);
				child.putString(TAG_FILTER_ID, id);
				child.putString(TAG_IS_ENABLED, isEnabled.toString());
			}
		}
	}

	/**
	 * Stores the last recently used filter Ids into
	 * the given memento
	 *
	 * @param memento the memento into which to store the LRU filter Ids
	 * @since 3.0
	 */
	private void saveLRUFilters(IMemento memento) {
		if (fLRUFilterIdsStack != null && !fLRUFilterIdsStack.isEmpty()) {
			IMemento lruFilters = memento.createChild(TAG_LRU_FILTERS);
			Iterator<String> iter = fLRUFilterIdsStack.iterator();
			while (iter.hasNext()) {
				String id = iter.next();
				IMemento child = lruFilters.createChild(TAG_CHILD);
				child.putString(TAG_FILTER_ID, id);
			}
		}
	}

	private void saveUserDefinedPatterns(IMemento memento) {
		if (fUserDefinedPatterns != null && fUserDefinedPatterns.length > 0) {
			IMemento userDefinedPatterns = memento.createChild(TAG_USER_DEFINED_PATTERNS);
			for (String userDefinedPattern : fUserDefinedPatterns) {
				IMemento child = userDefinedPatterns.createChild(TAG_CHILD);
				child.putString(TAG_PATTERN, userDefinedPattern);
			}
		}
	}

	/**
	 * Restores the state of the filter actions from a memento.
	 * <p>
	 * Note: This method does not refresh the viewer.
	 * </p>
	 *
	 * @param memento the memento from which the state is restored
	 */
	public void restoreState(IMemento memento) {
		if (memento == null)
			return;
		IMemento customFilters = memento.getChild(TAG_CUSTOM_FILTERS);
		if (customFilters == null)
			return;
		String userDefinedPatternsEnabled = customFilters.getString(TAG_USER_DEFINED_PATTERNS_ENABLED);
		if (userDefinedPatternsEnabled == null)
			return;

		fUserDefinedPatternsEnabled = Boolean.valueOf(userDefinedPatternsEnabled).booleanValue();
		restoreUserDefinedPatterns(customFilters);
		restoreXmlDefinedFilters(customFilters);
		restoreLRUFilters(customFilters);

		updateViewerFilters(false);
	}

	private void restoreUserDefinedPatterns(IMemento memento) {
		IMemento userDefinedPatterns = memento.getChild(TAG_USER_DEFINED_PATTERNS);
		if (userDefinedPatterns != null) {
			IMemento children[] = userDefinedPatterns.getChildren(TAG_CHILD);
			String[] patterns = new String[children.length];
			for (int i = 0; i < children.length; i++)
				patterns[i] = children[i].getString(TAG_PATTERN);

			setUserDefinedPatterns(patterns);
		} else
			setUserDefinedPatterns(new String[0]);
	}

	private void restoreXmlDefinedFilters(IMemento memento) {
		IMemento xmlDefinedFilters = memento.getChild(TAG_XML_DEFINED_FILTERS);
		if (xmlDefinedFilters != null) {
			IMemento[] children = xmlDefinedFilters.getChildren(TAG_CHILD);
			for (IMemento element : children) {
				String id = element.getString(TAG_FILTER_ID);
				Boolean isEnabled = Boolean.valueOf(element.getString(TAG_IS_ENABLED));
				fEnabledFilterIds.put(id, isEnabled);
			}
		}
	}

	private void restoreLRUFilters(IMemento memento) {
		IMemento lruFilters = memento.getChild(TAG_LRU_FILTERS);
		fLRUFilterIdsStack.clear();
		if (lruFilters != null) {
			IMemento[] children = lruFilters.getChildren(TAG_CHILD);
			for (IMemento element : children) {
				String id = element.getString(TAG_FILTER_ID);
				if (fFilterDescriptorMap.containsKey(id) && !fLRUFilterIdsStack.contains(id))
					fLRUFilterIdsStack.push(id);
			}
		}
	}

	private void cleanUpPatternDuplicates() {
		if (!areUserDefinedPatternsEnabled())
			return;
		List<String> userDefinedPatterns = new ArrayList<>(Arrays.asList(fUserDefinedPatterns));
		FilterDescriptor[] filters = FilterDescriptor.getFilterDescriptors(fTargetId);

		for (FilterDescriptor filter : filters) {
			if (filter.isPatternFilter()) {
				String pattern = filter.getPattern();
				if (userDefinedPatterns.contains(pattern)) {
					fEnabledFilterIds.put(filter.getId(), Boolean.TRUE);
					boolean hasMore = true;
					while (hasMore)
						hasMore = userDefinedPatterns.remove(pattern);
				}
			}
		}
		fUserDefinedPatterns = userDefinedPatterns.toArray(new String[userDefinedPatterns.size()]);
		setUserDefinedPatternsEnabled(fUserDefinedPatternsEnabled && fUserDefinedPatterns.length > 0);
	}

	// ---------- dialog related code ----------

	void openDialog() {
		CustomFiltersDialog dialog = new CustomFiltersDialog(fViewer.getControl().getShell(), fTargetId,
				areUserDefinedPatternsEnabled(), fUserDefinedPatterns, getEnabledFilterIds());

		if (dialog.open() == Window.OK) {
			setEnabledFilterIds(dialog.getEnabledFilterIds());
			setUserDefinedPatternsEnabled(dialog.areUserDefinedPatternsEnabled());
			setUserDefinedPatterns(dialog.getUserDefinedPatterns());
			setRecentlyChangedFilters(dialog.getFilterDescriptorChangeHistory());

			storeViewDefaults();

			updateViewerFilters(true);
		}
	}
}

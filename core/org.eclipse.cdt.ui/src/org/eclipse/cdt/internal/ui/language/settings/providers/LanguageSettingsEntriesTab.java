/*******************************************************************************
 * Copyright (c) 2010, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.language.settings.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsBroadcastingProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsBaseProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.ScannerDiscoveryLegacySupport;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;

import org.eclipse.cdt.internal.ui.newui.LanguageSettingsImages;
import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.internal.ui.newui.StatusMessageLine;

/**
 * This tab presents language settings entries categorized by language
 * settings providers.
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class LanguageSettingsEntriesTab extends AbstractCPropertyTab {
	private static final int[] DEFAULT_ENTRIES_SASH_WEIGHTS = new int[] { 10, 30 };

	private SashForm sashFormEntries;
	private Tree treeLanguages;
	private Tree treeEntries;
	private TreeViewer treeEntriesViewer;
	private String currentLanguageId = null;
	private static String currentLanguageIdGlobal = null;

	private Button builtInCheckBox;
	private Button enableProvidersCheckBox;
	private StatusMessageLine fStatusLine;

	private LanguageSettingsProvidersPage masterPropertyPage = null;

	private static final int BUTTON_ADD = 0;
	private static final int BUTTON_EDIT = 1;
	private static final int BUTTON_DELETE = 2;
	// there is a separator instead of button #3
	private static final int BUTTON_MOVE_UP = 4;
	private static final int BUTTON_MOVE_DOWN = 5;

	private static final String[] BUTTON_LABELS = new String[6];
	{
		BUTTON_LABELS[BUTTON_ADD] = ADD_STR;
		BUTTON_LABELS[BUTTON_EDIT] = EDIT_STR;
		BUTTON_LABELS[BUTTON_DELETE] = DEL_STR;
		BUTTON_LABELS[BUTTON_MOVE_UP] = MOVEUP_STR;
		BUTTON_LABELS[BUTTON_MOVE_DOWN] = MOVEDOWN_STR;
	}

	private static final String CLEAR_STR = Messages.LanguageSettingsProviderTab_Clear;

	private Map<String, List<ILanguageSettingsProvider>> initialProvidersMap = new HashMap<String, List<ILanguageSettingsProvider>>();

	/**
	 * Label provider for language settings providers displayed by this tab.
	 */
	private class EntriesTreeLabelProvider extends LanguageSettingsProvidersLabelProvider {
		@Override
		protected String[] getOverlayKeys(ILanguageSettingsProvider provider) {
			String[] overlayKeys = super.getOverlayKeys(provider);
			if (provider.getName() == null) {
				return overlayKeys;
			}

			if (currentLanguageId != null) {
				IResource rc = getResource();
				List<ICLanguageSettingEntry> entries = getSettingEntries(provider);
				if (entries == null && !(rc instanceof IProject)) {
					List<ICLanguageSettingEntry> entriesParent = getSettingEntriesUpResourceTree(provider);
					if (entriesParent != null) {
						overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_PARENT;
					}
				} else if (provider instanceof ILanguageSettingsBroadcastingProvider && (page.isForFile() || page.isForFolder())) {
					// Assuming that the default entries for a resource are always null.
					// Using that for performance reasons. See note in performDefaults().
					List<ICLanguageSettingEntry> entriesParent = provider.getSettingEntries(null, null, currentLanguageId);
					if (entries != null && !entries.equals(entriesParent)) {
						overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_SETTING;
					}
				}
			}

			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			List<ILanguageSettingsProvider> initialProviders = initialProvidersMap.get(cfgDescription.getId());
			if (initialProviders != null && !initialProviders.contains(provider)) {
				overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_EDITED;
			}
			return overlayKeys;
		}

		@Override
		public Image getImage(Object element) {
			if (element instanceof ICLanguageSettingEntry) {
				ICConfigurationDescription cfgDescription = getConfigurationDescription();
				return LanguageSettingsImages.getImage((ICLanguageSettingEntry) element, cfgDescription);
			}

			return super.getImage(element);
		}

		@Override
		public String getText(Object element) {
			if (element instanceof ICLanguageSettingEntry) {
				ICLanguageSettingEntry entry = (ICLanguageSettingEntry) element;
				String s = entry.getName();
				if ((entry.getKind() == ICSettingEntry.MACRO) && (entry.getFlags()&ICSettingEntry.UNDEFINED) == 0) {
					s = s + '=' + entry.getValue();
				}
				return s;
			}

			return super.getText(element);
		}
	}

	/**
	 * Content provider for setting entries tree.
	 */
	private class EntriesTreeContentProvider implements ITreeContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Object[])
				return (Object[]) parentElement;

			if (parentElement instanceof ILanguageSettingsProvider) {
				ILanguageSettingsProvider lsProvider = (ILanguageSettingsProvider)parentElement;
				List<ICLanguageSettingEntry> entriesList = getSettingEntriesUpResourceTree(lsProvider);
				if (entriesList == null) {
					return null;
				}

				// convert to modifiable list
				entriesList = new ArrayList<ICLanguageSettingEntry>(entriesList);

				if (builtInCheckBox.getSelection() == false) {
					for (Iterator<ICLanguageSettingEntry> iter = entriesList.iterator(); iter.hasNext();) {
						ICLanguageSettingEntry entry = iter.next();
						if (entry.isBuiltIn()) {
							iter.remove();
						}
					}
				}
				return entriesList.toArray();
			}

			return null;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			Object[] children = getChildren(element);
			return children!=null && children.length>0;
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}

	}

	/**
	 * Shortcut for getting the current resource for the property page.
	 */
	private IResource getResource() {
		return (IResource)page.getElement();
	}

	/**
	 * Shortcut for getting the current configuration description.
	 */
	private ICConfigurationDescription getConfigurationDescription() {
		return getResDesc().getConfiguration();
	}

	/**
	 * Shortcut for getting the currently selected provider.
	 */
	private ILanguageSettingsProvider getSelectedProvider() {
		ILanguageSettingsProvider provider = null;

		TreeItem[] items = treeEntries.getSelection();
		if (items.length == 1) {
			TreeItem item = items[0];
			Object itemData = item.getData();
			if (itemData instanceof ICLanguageSettingEntry) {
				item = item.getParentItem();
				if (item != null) {
					itemData = item.getData();
				}
			}
			if (itemData instanceof ILanguageSettingsProvider) {
				provider = (ILanguageSettingsProvider)itemData;
			}
		}

		return provider;
	}

	/**
	 * Shortcut for getting the currently selected setting entry.
	 */
	private ICLanguageSettingEntry getSelectedEntry() {
		ICLanguageSettingEntry entry = null;

		TreeItem[] items = treeEntries.getSelection();
		if (items.length == 1) {
			TreeItem item = items[0];
			Object itemData = item.getData();
			if (itemData instanceof ICLanguageSettingEntry) {
				entry = (ICLanguageSettingEntry)itemData;
			}
		}

		return entry;
	}

	/**
	 * Shortcut for getting setting entries for current context. {@link LanguageSettingsManager}
	 * will be checking parent resources if no settings defined for current resource.
	 *
	 * @return list of setting entries for the current context.
	 */
	private List<ICLanguageSettingEntry> getSettingEntriesUpResourceTree(ILanguageSettingsProvider provider) {
		if (currentLanguageId == null)
			return null;

		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		IResource rc = getResource();
		List<ICLanguageSettingEntry> entries = LanguageSettingsManager.getSettingEntriesUpResourceTree(provider, cfgDescription, rc, currentLanguageId);
		return entries;
	}

	/**
	 * Shortcut for getting setting entries for current context without checking the parent resource.
	 * @return list of setting entries for the current context.
	 */
	private List<ICLanguageSettingEntry> getSettingEntries(ILanguageSettingsProvider provider) {
		if (currentLanguageId==null)
			return null;

		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		IResource rc = getResource();
		return provider.getSettingEntries(cfgDescription, rc, currentLanguageId);
	}

	/**
	 * Store original providers to be able to tell whether they were changed by user.
	 */
	private void trackInitialSettings() {
		if (!page.isForPrefs()) {
			ICConfigurationDescription[] cfgDescriptions = page.getCfgsEditable();
			for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
				if (cfgDescription instanceof ILanguageSettingsProvidersKeeper) {
					String cfgId = cfgDescription.getId();
					List<ILanguageSettingsProvider> initialProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
					initialProvidersMap.put(cfgId, initialProviders);
				}
			}
		}
	}

	/**
	 * Create UI control for languages.
	 */
	private void createTreeForLanguages(Composite parent) {
		treeLanguages = new Tree(parent, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		treeLanguages.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		treeLanguages.setHeaderVisible(true);

		treeLanguages.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] items = treeLanguages.getSelection();
				if (items.length > 0) {
					currentLanguageId = (String) items[0].getData();
					currentLanguageIdGlobal = currentLanguageId;
					updateTreeForEntries();
				}
			}
		});

		final TreeColumn columnLanguages = new TreeColumn(treeLanguages, SWT.NONE);
		columnLanguages.setText(Messages.AbstractLangsListTab_Languages);
		columnLanguages.setWidth(200);
		columnLanguages.setResizable(false);
		columnLanguages.setToolTipText(Messages.AbstractLangsListTab_Languages);

		treeLanguages.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				int x = treeLanguages.getBounds().width - 5;
				if (columnLanguages.getWidth() != x) {
					columnLanguages.setWidth(x);
				}
			}
		});
	}

	/**
	 * Create tree for providers and their entries.
	 */
	private void createTreeForEntries(Composite parent) {
		treeEntries = new Tree(parent, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		treeEntries.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		treeEntries.setHeaderVisible(true);
		treeEntries.setLinesVisible(true);

		final TreeColumn treeCol = new TreeColumn(treeEntries, SWT.NONE);
		treeEntries.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				int x = treeEntries.getClientArea().width;
				if (treeCol.getWidth() != x)
					treeCol.setWidth(x);
			}
		});

		treeCol.setText(Messages.LanguageSettingsProviderTab_SettingEntries);
		treeCol.setWidth(200);
		treeCol.setResizable(false);
		treeCol.setToolTipText(Messages.LanguageSettingsProviderTab_SettingEntriesTooltip);

		treeEntriesViewer = new TreeViewer(treeEntries);
		treeEntriesViewer.setContentProvider(new EntriesTreeContentProvider());
		treeEntriesViewer.setLabelProvider(new EntriesTreeLabelProvider());

		treeEntriesViewer.setUseHashlookup(true);

		treeEntries.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (treeLanguages.getSelectionCount() == 0) {
					selectLanguage(currentLanguageId);
				}
				updateStatusLine();
				updateButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (buttonIsEnabled(BUTTON_EDIT) && treeEntries.getSelection().length>0)
					buttonPressed(BUTTON_EDIT);
			}
		});

	}

	/**
	 * Create sash form.
	 */
	private void createSashForm() {
		sashFormEntries = new SashForm(usercomp,SWT.HORIZONTAL);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.grabExcessVerticalSpace = true;
		sashFormEntries.setLayoutData(gd);

		GridLayout layout = new GridLayout();
		sashFormEntries.setLayout(layout);

		createTreeForLanguages(sashFormEntries);
		createTreeForEntries(sashFormEntries);

		sashFormEntries.setWeights(DEFAULT_ENTRIES_SASH_WEIGHTS);
	}

	/**
	 * Create check-box to control whether to show built-in entries or not.
	 */
	private void createBuiltInsCheckBox() {
		builtInCheckBox = setupCheck(usercomp, Messages.AbstractLangsListTab_ShowBuiltin, 1, GridData.FILL_HORIZONTAL);
		builtInCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTreeForEntries();
			}
		});
		builtInCheckBox.setSelection(true);
		builtInCheckBox.setEnabled(true);
	}

	/**
	 * Create check-box to allow disable/enable language settings providers functionality.
	 */
	private void createEnableProvidersCheckBox() {
		// take the flag from master page if available (normally for resource properties)
		if (masterPropertyPage != null) {
			enableProvidersCheckBox = setupCheck(usercomp, Messages.LanguageSettingsProviders_EnableForProject, 2, GridData.FILL_HORIZONTAL);
			enableProvidersCheckBox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean enabled = enableProvidersCheckBox.getSelection();
					masterPropertyPage.setLanguageSettingsProvidersEnabled(enabled);
					enableTabControls(enabled);
					updateStatusLine();
				}
			});

			enableProvidersCheckBox.setSelection(masterPropertyPage.isLanguageSettingsProvidersEnabled());

			// display but disable the checkbox for file/folder resource
			enableProvidersCheckBox.setEnabled(page.isForProject());
			enableTabControls(enableProvidersCheckBox.getSelection());
		}
	}

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		currentLanguageId = null;

		usercomp.setLayout(new GridLayout());
		GridData gd = (GridData) usercomp.getLayoutData();
		// Discourage settings entry table from trying to show all its items at once, see bug 264330
		gd.heightHint =1;

		if (page instanceof LanguageSettingsProvidersPage) {
			masterPropertyPage = (LanguageSettingsProvidersPage) page;
		}

		trackInitialSettings();

		createSashForm();
		fStatusLine = new StatusMessageLine(usercomp, SWT.LEFT, 2);
		createBuiltInsCheckBox();
		// "I want to try new scanner discovery" temporary checkbox
		createEnableProvidersCheckBox();

		initButtons(BUTTON_LABELS);
		updateData(getResDesc());
	}

	/**
	 * Gray out or restore all controls except enabling check-box.
	 */
	private void enableTabControls(boolean enable) {
		sashFormEntries.setEnabled(enable);
		treeLanguages.setEnabled(enable);
		treeEntries.setEnabled(enable);
		builtInCheckBox.setEnabled(enable);

		buttoncomp.setEnabled(enable);

		if (enable) {
			updateTreeForEntries();
		} else {
			buttonSetEnabled(BUTTON_ADD, false);
			buttonSetEnabled(BUTTON_EDIT, false);
			buttonSetEnabled(BUTTON_DELETE, false);
			buttonSetEnabled(BUTTON_MOVE_UP, false);
			buttonSetEnabled(BUTTON_MOVE_DOWN, false);
		}
	}

	/**
	 * Updates state for all buttons. Called when table selection changes.
	 */
	@Override
	protected void updateButtons() {
		ILanguageSettingsProvider provider = getSelectedProvider();
		ICLanguageSettingEntry entry = getSelectedEntry();
		List<ICLanguageSettingEntry> entries = getSettingEntriesUpResourceTree(provider);

		boolean isEntrySelected = (entry != null);
		boolean isProviderSelected = !isEntrySelected && (provider != null);

		boolean isAllowedToEdit = provider instanceof ILanguageSettingsEditableProvider
				&& LanguageSettingsProviderAssociationManager.isAllowedToEditEntries(provider);

		boolean isAllowedToClear = provider instanceof ILanguageSettingsEditableProvider
				&& LanguageSettingsProviderAssociationManager.isAllowedToClear(provider);

		boolean canAdd = isAllowedToEdit;
		boolean canEdit = isAllowedToEdit && isEntrySelected;
		boolean canDelete = isAllowedToEdit && isEntrySelected;
		boolean canClear = isAllowedToClear && isProviderSelected && entries != null && entries.size() > 0;

		boolean canMoveUp = false;
		boolean canMoveDown = false;
		if (isAllowedToEdit && isEntrySelected && entries != null) {
			int last = entries.size() - 1;
			int pos = getExactIndex(entries, entry);

			if (pos >= 0 && pos <= last) {
				canMoveUp = (pos != 0);
				canMoveDown = (pos != last);
			}
		}

		buttonSetText(BUTTON_DELETE, isProviderSelected ? CLEAR_STR : DEL_STR);

		buttonSetEnabled(BUTTON_ADD, canAdd);
		buttonSetEnabled(BUTTON_EDIT, canEdit);
		buttonSetEnabled(BUTTON_DELETE, canDelete || canClear);

		buttonSetEnabled(BUTTON_MOVE_UP, canMoveUp);
		buttonSetEnabled(BUTTON_MOVE_DOWN, canMoveDown);
	}

	/**
	 * Displays warning message - if any - for selected language settings entry.
	 */
	private void updateStatusLine() {
		IStatus status=null;
		if (enableProvidersCheckBox.getSelection() == true) {
			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			status = LanguageSettingsImages.getStatus(getSelectedEntry(), cfgDescription);
		}
		if (status == null || status == Status.OK_STATUS) {
			ILanguageSettingsProvider provider = getSelectedProvider();
			boolean isAllowedEditing = provider instanceof ILanguageSettingsEditableProvider
					&& LanguageSettingsProviderAssociationManager.isAllowedToEditEntries(provider);
			if (provider != null && !isAllowedEditing) {
				status = new Status(IStatus.INFO, CUIPlugin.PLUGIN_ID, Messages.LanguageSettingsEntriesTab_Entries_Not_Editable);
			}
		}
		if (status == null || status == Status.OK_STATUS) {
			if (treeLanguages.getItemCount() <= 0) {
				status = new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, Messages.LanguageSettingsEntriesTab_Cannot_Determine_Languages);
			}
		}
		fStatusLine.setErrorStatus(status);
	}

	/**
	 * Handle buttons
	 */
	@Override
	public void buttonPressed(int buttonIndex) {
		ILanguageSettingsProvider selectedProvider = getSelectedProvider();
		ICLanguageSettingEntry selectedEntry = getSelectedEntry();

		switch (buttonIndex) {
		case BUTTON_ADD:
			performAdd(selectedProvider);
			break;
		case BUTTON_EDIT:
			performEdit(selectedProvider, selectedEntry);
			break;
		case BUTTON_DELETE:
			performDelete(selectedProvider, selectedEntry);
			break;
		case BUTTON_MOVE_UP:
			performMoveUp(selectedProvider, selectedEntry);
			break;
		case BUTTON_MOVE_DOWN:
			performMoveDown(selectedProvider, selectedEntry);
			break;
		default:
		}
		treeEntries.setFocus();
	}

	/**
	 * That method returns exact position of an element in the list.
	 * Note that {@link List#indexOf(Object)} returns position of the first element
	 * equals to the given one, not exact element.
	 *
	 * @param entries
	 * @param entry
	 * @return exact position of the element or -1 of not found.
	 */
	private int getExactIndex(List<ICLanguageSettingEntry> entries, ICLanguageSettingEntry entry) {
		if (entries != null) {
			for (int i = 0; i < entries.size(); i++) {
				if (entries.get(i) == entry)
					return i;
			}
		}
		return -1;
	}

	/**
	 * Find TreeItem associated with a provider.
	 */
	private TreeItem findProviderItem(String id) {
		TreeItem[] providerItems = treeEntries.getItems();
		for (TreeItem providerItem : providerItems) {
			Object providerItemData = providerItem.getData();
			if (providerItemData instanceof ILanguageSettingsProvider) {
				ILanguageSettingsProvider provider = (ILanguageSettingsProvider)providerItemData;
				if (provider.getId().equals(id)) {
					return providerItem;
				}
			}
		}
		return null;
	}

	/**
	 * Find TreeItem associated with a provider's entry.
	 */
	private TreeItem findEntryItem(String providerId, ICLanguageSettingEntry entry) {
		TreeItem[] providerItems = treeEntries.getItems();
		for (TreeItem providerItem : providerItems) {
			Object providerItemData = providerItem.getData();
			if (providerItemData instanceof ILanguageSettingsProvider) {
				ILanguageSettingsProvider provider = (ILanguageSettingsProvider)providerItemData;
				if (provider.getId().equals(providerId)) {
					TreeItem[] entryItems = providerItem.getItems();
					for (TreeItem entryItem : entryItems) {
						Object entryItemData = entryItem.getData();
						if (entryItemData==entry)
							return entryItem;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Select language settings entry item in the tree.
	 */
	private void selectItem(String providerId, ICLanguageSettingEntry entry) {
		TreeItem providerItem = findProviderItem(providerId);
		if (providerItem != null) {
			treeEntries.select(providerItem);
			if (providerItem.getItems().length > 0) {
				treeEntries.showItem(providerItem.getItems()[0]);
			}
			TreeItem entryItem = findEntryItem(providerId, entry);
			if (entryItem != null) {
				treeEntries.showItem(entryItem);
				treeEntries.select(entryItem);
			}
			updateStatusLine();
		}
	}

	/**
	 * Add language settings entry.
	 */
	private void addEntry(ILanguageSettingsProvider provider, ICLanguageSettingEntry entry) {
		if (provider != null && entry != null) {
			String providerId = provider.getId();

			List<ICLanguageSettingEntry> entries = getEntriesShownToUser(provider);
			ICLanguageSettingEntry selectedEntry = getSelectedEntry();
			int pos = getExactIndex(entries, selectedEntry);
			entries.add(pos+1, entry);
			saveEntries(provider, entries);

			updateTreeForEntries();
			selectItem(providerId, entry);
			updateButtons();
		}
	}

	/**
	 * Save entries into provider considering resource parent.
	 */
	private void saveEntries(ILanguageSettingsProvider provider, List<ICLanguageSettingEntry> entries) {
		if (provider instanceof ILanguageSettingsEditableProvider) {
			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			IResource rc = getResource();
			if (entries != null && rc != null) {
				List<ICLanguageSettingEntry> parentEntries = null;
				if (rc instanceof IProject) {
					parentEntries = new ArrayList<ICLanguageSettingEntry>();
				} else {
					parentEntries = LanguageSettingsManager.getSettingEntriesUpResourceTree(provider, cfgDescription, rc.getParent(), currentLanguageId);
				}
				if (entries.equals(parentEntries)) {
					// to use parent entries instead
					entries = null;
				}
			}
			((ILanguageSettingsEditableProvider)provider).setSettingEntries(cfgDescription, rc, currentLanguageId, entries);
		}
	}

	/**
	 * Get list of setting entries shown to user. If current resource has no entries assigned the parent
	 * resource is inspected.
	 */
	private List<ICLanguageSettingEntry> getEntriesShownToUser(ILanguageSettingsProvider provider) {
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		IResource rc = getResource();
		List<ICLanguageSettingEntry> entries = provider.getSettingEntries(cfgDescription, rc, currentLanguageId);
		if (entries == null) {
			entries = getSettingEntriesUpResourceTree(provider);
		}
		entries = new ArrayList<ICLanguageSettingEntry>(entries);
		return entries;
	}

	/**
	 * Call dialog to add settings entry.
	 */
	private void performAdd(ILanguageSettingsProvider selectedProvider) {
		if (selectedProvider instanceof ILanguageSettingsEditableProvider) {
			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			ICLanguageSettingEntry selectedEntry = getSelectedEntry();
			LanguageSettingEntryDialog addDialog = new LanguageSettingEntryDialog(usercomp.getShell(), cfgDescription, selectedEntry, true);
			if (addDialog.open()) {
				ICLanguageSettingEntry settingEntry = addDialog.getEntries()[0];
				if (settingEntry != null) {
					selectedProvider = getWorkingCopy((ILanguageSettingsEditableProvider)selectedProvider);
					addEntry(selectedProvider, settingEntry);
				}
			}
		}
	}

	/**
	 * Return working copy of the provider to edit in current session. If the supplied provider is already
	 * the working copy return it. If not, create a copy to be edited.
	 */
	private ILanguageSettingsEditableProvider getWorkingCopy(ILanguageSettingsEditableProvider provider) {
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		List<ILanguageSettingsProvider> initialProviders = initialProvidersMap.get(cfgDescription.getId());
		if (initialProviders.contains(provider)) {
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders());
			int pos = providers.indexOf(provider);
			if (pos >= 0) {
				try {
					provider = provider.clone();
					providers.set(pos, provider);
					((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
				} catch (CloneNotSupportedException e) {
					CUIPlugin.log("Internal Error: cannot clone provider "+provider.getId(), e); //$NON-NLS-1$
				}
			} else {
				CUIPlugin.log("Internal Error: cannot find provider "+provider.getId(), new Exception()); //$NON-NLS-1$
			}
		}
		return provider;
	}

	/**
	 * Call dialog to edit settings entry.
	 */
	private void performEdit(ILanguageSettingsProvider selectedProvider, ICLanguageSettingEntry selectedEntry) {
		if (selectedProvider instanceof ILanguageSettingsEditableProvider && selectedEntry != null) {
			ICConfigurationDescription cfgDecsription = getConfigurationDescription();
			LanguageSettingEntryDialog editDialog = new LanguageSettingEntryDialog(usercomp.getShell(), cfgDecsription, selectedEntry);
			if (editDialog.open()) {
				ICLanguageSettingEntry newEntry = editDialog.getEntries()[0];
				if (newEntry != null) {
					selectedProvider = getWorkingCopy((ILanguageSettingsEditableProvider)selectedProvider);
					replaceEntry(selectedProvider, selectedEntry, newEntry);
				}
			}

		}
	}

	/**
	 * Delete provider's entry and update UI.
	 */
	private void deleteEntry(ILanguageSettingsProvider provider, ICLanguageSettingEntry entry) {
		if (provider != null && entry != null) {
			String providerId = provider.getId();

			List<ICLanguageSettingEntry> entries = getEntriesShownToUser(provider);
			int pos = getExactIndex(entries, entry);
			entries.remove(entry);
			saveEntries(provider, entries);

			if (pos >= entries.size()) {
				pos = entries.size() - 1;
			}
			ICLanguageSettingEntry entryToSelect = (pos >= 0) ? entries.get(pos) : null;

			updateTreeForEntries();
			selectItem(providerId, entryToSelect);
			updateButtons();
		}
	}

	/**
	 * Replace provider's entry and update UI.
	 */
	private void replaceEntry(ILanguageSettingsProvider provider, ICLanguageSettingEntry oldEntry, ICLanguageSettingEntry newEntry) {
		if (provider != null && oldEntry != null && newEntry != null) {
			String providerId = provider.getId();

			List<ICLanguageSettingEntry> entries = getEntriesShownToUser(provider);
			int pos = getExactIndex(entries, oldEntry);
			entries.set(pos, newEntry);
			saveEntries(provider, entries);

			updateTreeForEntries();
			selectItem(providerId, newEntry);
			updateButtons();
		}
	}

	/**
	 * Clear all provider's entries for the given resource and update UI.
	 */
	private void clearProvider(ILanguageSettingsProvider provider) {
		if (provider != null) {
			String providerId = provider.getId();
			List<ICLanguageSettingEntry> empty = new ArrayList<ICLanguageSettingEntry>();
			saveEntries(provider, empty);

			updateTreeForEntries();
			selectItem(providerId, null);
			updateButtons();
		}
	}

	/**
	 * Clear provider's entries for the given resource or remove one entry depending on selection.
	 */
	private void performDelete(ILanguageSettingsProvider selectedProvider, ICLanguageSettingEntry selectedEntry) {
		if (selectedProvider instanceof ILanguageSettingsEditableProvider) {
			selectedProvider = getWorkingCopy((ILanguageSettingsEditableProvider)selectedProvider);
			if (selectedEntry != null) {
				deleteEntry(selectedProvider, selectedEntry);
			} else {
				clearProvider(selectedProvider);
			}
		}
	}

	/**
	 * Move provider's entry up or down.
	 */
	private void moveEntry(ILanguageSettingsProvider provider, ICLanguageSettingEntry entry, boolean up) {
		if (provider != null && entry != null) {
			String providerId = provider.getId();

			List<ICLanguageSettingEntry> entries = getEntriesShownToUser(provider);
			int pos = getExactIndex(entries, entry);
			int newPos = up ? pos-1 : pos+1;
			Collections.swap(entries, pos, newPos);
			saveEntries(provider, entries);

			updateTreeForEntries();
			selectItem(providerId, entry);
			updateButtons();
		}
	}

	/**
	 * Move provider's entry up.
	 */
	private void performMoveUp(ILanguageSettingsProvider selectedProvider, ICLanguageSettingEntry selectedEntry) {
		if (selectedEntry != null && (selectedProvider instanceof ILanguageSettingsEditableProvider)) {
			selectedProvider = getWorkingCopy((ILanguageSettingsEditableProvider)selectedProvider);
			moveEntry(selectedProvider, selectedEntry, true);
		}
	}

	/**
	 * Move provider's entry down.
	 */
	private void performMoveDown(ILanguageSettingsProvider selectedProvider, ICLanguageSettingEntry selectedEntry) {
		if (selectedEntry != null && (selectedProvider instanceof ILanguageSettingsEditableProvider)) {
			selectedProvider = getWorkingCopy((ILanguageSettingsEditableProvider)selectedProvider);
			moveEntry(selectedProvider, selectedEntry, false);
		}
	}

	/**
	 * Get list of providers to display in the settings entry tree.
	 */
	private List<ILanguageSettingsProvider> getProviders(String languageSettingId) {
		List<ILanguageSettingsProvider> itemsList = new LinkedList<ILanguageSettingsProvider>();
		if (currentLanguageId != null) {
			IResource rc = getResource();
			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			if (rc != null && cfgDescription instanceof ILanguageSettingsProvidersKeeper) {
				List<ILanguageSettingsProvider> cfgProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
				for (ILanguageSettingsProvider cfgProvider : cfgProviders) {
					ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(cfgProvider);
					if (rawProvider instanceof LanguageSettingsBaseProvider) {
						// filter out providers incapable of providing entries for this language
						List<String> languageIds = ((LanguageSettingsBaseProvider)rawProvider).getLanguageScope();
						if (languageIds != null && !languageIds.contains(currentLanguageId)) {
							continue;
						}
					}
					itemsList.add(cfgProvider);
				}
			}
		}
		return itemsList;
	}

	/**
	 * Re-reads and refreshes the entries tree.
	 */
	private void updateTreeForEntries() {
		List<ILanguageSettingsProvider> tableItems = getProviders(currentLanguageId);
		treeEntriesViewer.setInput(tableItems.toArray(new Object[tableItems.size()]));
		updateStatusLine();
		updateButtons();
	}

	/**
	 * Re-reads and refreshes the languages tree.
	 */
	private void updateTreeForLanguages(ICResourceDescription rcDes) {
		treeLanguages.removeAll();
		currentLanguageId = null;

		List<String> languageIds = LanguageSettingsManager.getLanguages(rcDes);
		Collections.sort(languageIds);
		for (String langId : languageIds) {
			ILanguage language = LanguageManager.getInstance().getLanguage(langId);
			if (language == null)
				continue;

			String langName = language.getName();
			if (langName == null || langName.length() == 0)
				continue;

			TreeItem t = new TreeItem(treeLanguages, SWT.NONE);
			t.setText(0, langName);
			t.setData(langId);
			if (currentLanguageIdGlobal != null && currentLanguageIdGlobal.equals(langId)) {
				currentLanguageId = currentLanguageIdGlobal;
				treeLanguages.setSelection(t);
			} else if (currentLanguageId == null) {
				// this selects first language on first round
				// do not select the tree item and global language selection here, only on actual click
				currentLanguageId = langId;
			}
		}

	}

	/**
	 * Change selection of language.
	 */
	private void selectLanguage(String langId) {
		currentLanguageId = langId;
		currentLanguageIdGlobal = currentLanguageId;

		for (TreeItem t : treeLanguages.getItems()) {
			if (t.getData().equals(langId)) {
				treeLanguages.setSelection(t);
				break;
			}
		}
	}

	/**
	 * Update the tab. Called when configuration changes.
	 */
	@Override
	public void updateData(ICResourceDescription rcDes) {
		if (!canBeVisible())
			return;

		if (rcDes != null) {
			if (page.isMultiCfg()) {
				setAllVisible(false, null);
				return;
			} else {
				setAllVisible(true, null);
			}

			updateTreeForLanguages(rcDes);
			updateTreeForEntries();

			if (masterPropertyPage != null) {
				boolean enabled = masterPropertyPage.isLanguageSettingsProvidersEnabled();
				enableProvidersCheckBox.setSelection(enabled);
				enableTabControls(enabled);
			}
		}
		updateButtons();
	}

	@Override
	protected void performDefaults() {
		// This page restores defaults for file/folder only.
		// Project and Preferences page are restored by LanguageSettingsProviderTab.
		if (page.isForFile() || page.isForFolder()) {
			// The logic below is not exactly correct as the default for a resource could be different than null.
			// It is supposed to match the one taken from extension for the same resource which in theory can be non-null.
			// However for the performance reasons for resource decorators where the same logic is used
			// we use null for resetting file/folder resource which should be correct in most cases.
			// Count that as a feature.
			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			if (!(cfgDescription instanceof ILanguageSettingsProvidersKeeper)) {
				return;
			}

			boolean changed = false;
			IResource rc = getResource();
			List<ILanguageSettingsProvider> oldProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			List<ILanguageSettingsProvider> newProviders = new ArrayList<ILanguageSettingsProvider>(oldProviders.size());

			// clear entries for a given resource for all languages where applicable
			providers:	for (ILanguageSettingsProvider provider : oldProviders) {
				ILanguageSettingsEditableProvider providerCopy = null;
				if (provider instanceof ILanguageSettingsEditableProvider) {
					for (TreeItem langItems : treeLanguages.getItems()) {
						String langId = (String)langItems.getData();
						if (langId != null) {
							if (provider.getSettingEntries(cfgDescription, rc, langId) != null) {
								if (providerCopy == null) {
									// copy providers to be able to "Cancel" in UI
									providerCopy = LanguageSettingsManager.getProviderCopy((ILanguageSettingsEditableProvider) provider, true);
									if (providerCopy == null) {
										continue providers;
									}
								}
								providerCopy.setSettingEntries(cfgDescription, rc, langId, null);
								changed = true;
							}
						}
					}
				}
				if (providerCopy != null) {
					newProviders.add(providerCopy);
				} else {
					newProviders.add(provider);
				}
			}
			if (changed) {
				((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(newProviders);
				updateTreeForEntries();
			}
		}
	}

	@Override
	protected void performApply(ICResourceDescription srcRcDescription, ICResourceDescription destRcDescription) {
		if (!page.isForPrefs()) {
			ICConfigurationDescription sd = srcRcDescription.getConfiguration();
			ICConfigurationDescription dd = destRcDescription.getConfiguration();
			if (sd instanceof ILanguageSettingsProvidersKeeper && dd instanceof ILanguageSettingsProvidersKeeper) {
				List<ILanguageSettingsProvider> newProviders = ((ILanguageSettingsProvidersKeeper) sd).getLanguageSettingProviders();
				((ILanguageSettingsProvidersKeeper) dd).setLanguageSettingProviders(newProviders);
			}
		}

		performOK();

		trackInitialSettings();
		updateData(getResDesc());
	}

	@Override
	protected void performOK() {
		if (masterPropertyPage != null && enableProvidersCheckBox.getEnabled()) {
			masterPropertyPage.applyLanguageSettingsProvidersEnabled();
		}
	}

	@Override
	public boolean canBeVisible() {
		if (!ScannerDiscoveryLegacySupport.isLanguageSettingsProvidersFunctionalityEnabled(null)) {
			return false;
		}

		// filter out files not associated with any languages such as *.o
		if (page.isForFile()) {
			List<String> languageIds = LanguageSettingsManager.getLanguages(getResDesc());
			for (String langId : languageIds) {
				ILanguage language = LanguageManager.getInstance().getLanguage(langId);
				if (language != null)
					return true;
			}
			return false;
		}

		return true;
	}

}

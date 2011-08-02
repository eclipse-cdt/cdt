/*******************************************************************************
 * Copyright (c) 2010, 2011 Andrew Gvozdev and others.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsBaseProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager_TBD;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializable;
import org.eclipse.cdt.core.model.ILanguageDescriptor;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.model.util.CDTListComparator;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;

import org.eclipse.cdt.internal.ui.newui.LanguageSettingsImages;
import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.internal.ui.newui.StatusMessageLine;


/**
 * This tab presents language settings entries categorized by language
 * settings providers.
 *
 *@noinstantiate This class is not intended to be instantiated by clients.
 *@noextend This class is not intended to be subclassed by clients.
 */
public class LanguageSettingsEntriesTab extends AbstractCPropertyTab {
	private static final int[] DEFAULT_ENTRIES_SASH_WEIGHTS = new int[] { 10, 30 };

	private SashForm sashFormEntries;
	private Tree treeLanguages;
	private Tree treeEntries;
	private TreeViewer treeEntriesViewer;
	private ICLanguageSetting currentLanguageSetting = null;
	private ICLanguageSetting[] allLanguages;
	
	private Button builtInCheckBox;
	private Button enableProvidersCheckBox;
	private StatusMessageLine fStatusLine;
	
	private Page_LanguageSettingsProviders masterPropertyPage = null;

	private static final int BUTTON_ADD = 0;
	private static final int BUTTON_EDIT = 1;
	private static final int BUTTON_DELETE = 2;
	// there is a separator instead of button #3
	private static final int BUTTON_MOVE_UP = 4;
	private static final int BUTTON_MOVE_DOWN = 5;

	private final static String[] BUTTON_LABELS = {
		ADD_STR,
		EDIT_STR,
		DEL_STR,
		null,
		MOVEUP_STR,
		MOVEDOWN_STR,
	};
	private static final String CLEAR_STR = Messages.LanguageSettingsProviderTab_Clear;

	private Map<String, List<ILanguageSettingsProvider>> initialProvidersMap = new HashMap<String, List<ILanguageSettingsProvider>>();
	private boolean initialEnablement =false;
	
	private class EntriesTreeLabelProvider extends LanguageSettingsProvidersLabelProvider {
		@Override
		protected String[] getOverlayKeys(ILanguageSettingsProvider provider) {
			String[] overlayKeys = super.getOverlayKeys(provider);
			
//			if (LanguageSettingsManager.isWorkspaceProvider(provider))
//				provider = LanguageSettingsManager.getRawWorkspaceProvider(provider.getId());
//			
			if (currentLanguageSetting != null) {
				IResource rc = getResource();
				List<ICLanguageSettingEntry> entries = getSettingEntries(provider);
				if (entries == null && !(rc instanceof IProject)) {
					List<ICLanguageSettingEntry> entriesParent = getSettingEntriesUpResourceTree(provider);
					if (entriesParent != null /*&& entriesParent.size() > 0*/) {
						overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_PARENT;
					}
				} else if (provider instanceof ILanguageSettingsEditableProvider && (page.isForFile() || page.isForFolder())) {
					// Assuming that the default entries for a resource are always null.
					// Using that for performance reasons. See note in PerformDefaults().
					String languageId = currentLanguageSetting.getLanguageId();
					List<ICLanguageSettingEntry> entriesParent = provider.getSettingEntries(null, null, languageId);
					if (entries!=null && !entries.equals(entriesParent)) {
						overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_SETTING;
					}
				}
			}
			
			// TODO
			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			List<ILanguageSettingsProvider> initialProviders = initialProvidersMap.get(cfgDescription.getId());
			if (initialProviders!=null && !initialProviders.contains(provider)) {
				overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_EDITED;
			}
			return overlayKeys;
		}
		
		@Override
		public Image getImage(Object element) {
			if (element instanceof ICLanguageSettingEntry) {
				ICLanguageSettingEntry entry = (ICLanguageSettingEntry) element;
				return LanguageSettingsImages.getImage(entry);
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
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Object[])
				return (Object[]) parentElement;
			if (parentElement instanceof ILanguageSettingsProvider) {
				ILanguageSettingsProvider lsProvider = (ILanguageSettingsProvider)parentElement;
				List<ICLanguageSettingEntry> entriesList = getSettingEntriesUpResourceTree(lsProvider);

				if (builtInCheckBox.getSelection()==false) {
					for (Iterator<ICLanguageSettingEntry> iter = entriesList.iterator(); iter.hasNext();) {
						ICLanguageSettingEntry entry = iter.next();
						if (entry.isBuiltIn()) {
							iter.remove();
						}
					}
				}

				if (entriesList!=null) {
					return entriesList.toArray();
				}
			}
			return null;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			Object[] children = getChildren(element);
			return children!=null && children.length>0;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

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
		if (items.length>0) {
			TreeItem item = items[0];
			Object itemData = item.getData();
			if (itemData instanceof ICLanguageSettingEntry) {
				item = item.getParentItem();
				if (item!=null) {
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

		TreeItem[] selItems = treeEntries.getSelection();
		if (selItems.length==0) {
			return null;
		}

		TreeItem item = selItems[0];
		Object itemData = item.getData();
		if (itemData instanceof ICLanguageSettingEntry) {
			entry = (ICLanguageSettingEntry)itemData;
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
		if (currentLanguageSetting==null)
			return null;
		
		String languageId = currentLanguageSetting.getLanguageId();
		if (languageId==null)
			return null;

		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		IResource rc = getResource();
		List<ICLanguageSettingEntry> entries = LanguageSettingsManager.getSettingEntriesUpResourceTree(provider, cfgDescription, rc, languageId);
		return entries;
	}

	/**
	 * Shortcut for getting setting entries for current context without checking the parent resource.
	 * @return list of setting entries for the current context.
	 */
	private List<ICLanguageSettingEntry> getSettingEntries(ILanguageSettingsProvider provider) {
		String languageId = currentLanguageSetting.getLanguageId();
		if (languageId==null)
			return null;

		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		IResource rc = getResource();
		return provider.getSettingEntries(cfgDescription, rc, languageId);
	}

	private void addTreeForLanguages(Composite comp) {
		treeLanguages = new Tree(comp, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		treeLanguages.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		treeLanguages.setHeaderVisible(true);

		treeLanguages.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] items = treeLanguages.getSelection();
				if (items.length > 0) {
					ICLanguageSetting langSetting = (ICLanguageSetting) items[0].getData();
					if (langSetting != null) {
						currentLanguageSetting = langSetting;
						updateTreeEntries();
						updateButtons();
					}
				}
			}
		});

		final TreeColumn columnLanguages = new TreeColumn(treeLanguages, SWT.NONE);
		columnLanguages.setText(Messages.AbstractLangsListTab_Languages);
		columnLanguages.setWidth(200);
		columnLanguages.setResizable(false);
		columnLanguages.setToolTipText(Messages.AbstractLangsListTab_Languages);

		treeLanguages.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				int x = treeLanguages.getBounds().width - 5;
				if (columnLanguages.getWidth() != x)
					columnLanguages.setWidth(x);
			}
		});

	}

	private void addTreeForEntries(Composite comp) {
		treeEntries = new Tree(comp, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		treeEntries.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		treeEntries.setHeaderVisible(true);
		treeEntries.setLinesVisible(true);

		final TreeColumn treeCol = new TreeColumn(treeEntries, SWT.NONE);
		treeEntries.addPaintListener(new PaintListener() {
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

	private void trackInitialSettings() {
		if (!page.isForPrefs()) {
			ICConfigurationDescription[] cfgDescriptions = page.getCfgsEditable();
			for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
				if (cfgDescription!=null) {
					String cfgId = cfgDescription.getId();
					List<ILanguageSettingsProvider> initialProviders = cfgDescription.getLanguageSettingProviders();
					initialProvidersMap.put(cfgId, initialProviders);
				}
			}
			initialEnablement = LanguageSettingsManager.isLanguageSettingsProvidersEnabled(page.getProject());
		}
	}
	
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout());
		GridData gd = (GridData) usercomp.getLayoutData();
		// Discourage settings entry table from trying to show all its items at once, see bug 264330
		gd.heightHint =1;
		
		if (page instanceof Page_LanguageSettingsProviders) {
			masterPropertyPage = (Page_LanguageSettingsProviders) page;
		}

		trackInitialSettings();

		// SashForms for each mode
		createShowEntriesSashForm();
		
		// Status line
		fStatusLine = new StatusMessageLine(usercomp, SWT.LEFT, 2);

		// "Show built-ins" checkbox
		builtInCheckBox = setupCheck(usercomp, Messages.AbstractLangsListTab_ShowBuiltin, 1, GridData.FILL_HORIZONTAL);
		builtInCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTreeEntries();
			}
		});
		builtInCheckBox.setSelection(true);
		builtInCheckBox.setEnabled(true);

		// "I want to try new scanner discovery" temporary checkbox
		enableProvidersCheckBox = setupCheck(usercomp, Messages.CDTMainWizardPage_TrySD90, 2, GridData.FILL_HORIZONTAL);
		enableProvidersCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = enableProvidersCheckBox.getSelection();
				if (masterPropertyPage!=null)
					masterPropertyPage.setLanguageSettingsProvidersEnabled(enabled);
				enableControls(enabled);
				updateStatusLine();
			}
		});

		if (masterPropertyPage!=null)
			enableProvidersCheckBox.setSelection(masterPropertyPage.isLanguageSettingsProvidersEnabled());
		else
			enableProvidersCheckBox.setSelection(LanguageSettingsManager.isLanguageSettingsProvidersEnabled(page.getProject()));
		// display but disable the checkbox for file/folder resource
		enableProvidersCheckBox.setEnabled(page.isForProject()/* && !isConfigureMode*/);
		enableControls(enableProvidersCheckBox.getSelection());

		initButtons(BUTTON_LABELS);
		updateData(getResDesc());
	}

	private void createShowEntriesSashForm() {
		sashFormEntries = new SashForm(usercomp,SWT.HORIZONTAL);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.grabExcessVerticalSpace = true;
		sashFormEntries.setLayoutData(gd);
		
		GridLayout layout = new GridLayout();
		sashFormEntries.setLayout(layout);

		addTreeForLanguages(sashFormEntries);
		addTreeForEntries(sashFormEntries);

		sashFormEntries.setWeights(DEFAULT_ENTRIES_SASH_WEIGHTS);
	}

	private void enableControls(boolean enable) {
		sashFormEntries.setEnabled(enable);
		treeLanguages.setEnabled(enable);
		treeEntries.setEnabled(enable);
		builtInCheckBox.setEnabled(enable);
		
		buttoncomp.setEnabled(enable);

		if (enable) {
			updateTreeEntries();
		} else {
			disableButtons();
		}
	}
	
	private void disableButtons() {
		buttonSetEnabled(BUTTON_ADD, false);
		buttonSetEnabled(BUTTON_EDIT, false);
		buttonSetEnabled(BUTTON_DELETE, false);
		buttonSetEnabled(BUTTON_MOVE_UP, false);
		buttonSetEnabled(BUTTON_MOVE_DOWN, false);
//		buttonSetEnabled(BUTTON_CONFIGURE, false);
	}

	/**
	 * Updates state for all buttons. Called when table selection changes.
	 */
	@Override
	protected void updateButtons() {
		ILanguageSettingsProvider provider = getSelectedProvider();
		ICLanguageSettingEntry entry = getSelectedEntry();
		List<ICLanguageSettingEntry> entries = getSettingEntriesUpResourceTree(provider);

		boolean isEntrySelected = entry!=null;
		boolean isProviderSelected = !isEntrySelected && (provider!=null);

		boolean isProviderEditable = provider instanceof ILanguageSettingsEditableProvider;
//		boolean isUserProvider = provider instanceof UserLanguageSettingsProvider;
		
		boolean canAdd = isProviderEditable;
		boolean canEdit = isProviderEditable && isEntrySelected;
		boolean canDelete = isProviderEditable && isEntrySelected;
		boolean canClear = isProviderEditable && isProviderSelected && entries!=null && entries.size()>0;
		
		boolean canMoveUp = false;
		boolean canMoveDown = false;
		if (isProviderEditable && isEntrySelected && entries!=null) {
			int last = entries.size()-1;
			int pos = getExactIndex(entries, entry);
			
			if (pos>=0 && pos<=last) {
				canMoveUp = pos!=0;
				canMoveDown = pos!=last;
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
		if (enableProvidersCheckBox.getSelection()==true) {
			status = LanguageSettingsImages.getStatus(getSelectedEntry());
		}
		if (status==null || status==Status.OK_STATUS) {
			ILanguageSettingsProvider provider = getSelectedProvider();
			if (provider!=null && !(provider instanceof UserLanguageSettingsProvider)) {
				String msg = "Setting entries for this provider are supplied by system and are not editable.";
				status = new Status(IStatus.INFO, CUIPlugin.PLUGIN_ID, msg);
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
//		case BUTTON_CONFIGURE:
//			performConfigure(selectedProvider);
//			break;
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
		if (entries!=null) {
			for (int i=0;i<entries.size();i++) {
				if (entries.get(i)==entry)
					return i;
			}
		}
		return -1;
	}

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

	private TreeItem findEntryItem(String id, ICLanguageSettingEntry entry) {
		TreeItem[] providerItems = treeEntries.getItems();
		for (TreeItem providerItem : providerItems) {
			Object providerItemData = providerItem.getData();
			if (providerItemData instanceof ILanguageSettingsProvider) {
				ILanguageSettingsProvider provider = (ILanguageSettingsProvider)providerItemData;
				if (provider.getId().equals(id)) {
					TreeItem[] entryItems = providerItem.getItems();
					for (TreeItem entryItem : entryItems) {
						Object entryItemData = entryItem.getData();
						if (entryItemData==entry)
							return entryItem;
					}
//					return providerItem;
				}
			}
		}
		return null;
	}

	private void selectItem(String providerId, ICLanguageSettingEntry entry) {
		TreeItem providerItem = findProviderItem(providerId);
		if (providerItem!=null) {
			treeEntries.select(providerItem);
			if (providerItem.getItems().length>0) {
				treeEntries.showItem(providerItem.getItems()[0]);
			}
			TreeItem entryItem = findEntryItem(providerId, entry);
			if (entryItem!=null) {
				treeEntries.showItem(entryItem);
				treeEntries.select(entryItem);
			}
		}
	}

	private void addEntry(ILanguageSettingsProvider provider, ICLanguageSettingEntry entry) {
		if (provider!=null && entry != null) {
			String providerId = provider.getId();
			
			List<ICLanguageSettingEntry> entries = getWritableEntries(provider);
			ICLanguageSettingEntry selectedEntry = getSelectedEntry();
			int pos = getExactIndex(entries, selectedEntry);
			entries.add(pos+1, entry);
			saveEntries(provider, entries);
			
			updateTreeEntries();
			selectItem(providerId, entry);
			updateButtons();
		}
	}

	private void saveEntries(ILanguageSettingsProvider provider, List<ICLanguageSettingEntry> entries) {
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		IResource rc = getResource();
		String languageId = currentLanguageSetting.getLanguageId();
			
		if (provider instanceof LanguageSettingsSerializable) {
			if (entries!=null && rc!=null) {
				List<ICLanguageSettingEntry> parentEntries = null;
				if (rc instanceof IProject) {
					parentEntries = new ArrayList<ICLanguageSettingEntry>();
				} else {
					parentEntries = LanguageSettingsManager.getSettingEntriesUpResourceTree(provider, cfgDescription, rc.getParent(), languageId);
				}
				if (entries.equals(parentEntries)) {
					// to use parent entries instead
					entries = null;
				}
			}
			((LanguageSettingsSerializable)provider).setSettingEntries(cfgDescription, rc, languageId, entries);
		}
	}

	private List<ICLanguageSettingEntry> getWritableEntries(ILanguageSettingsProvider provider) {
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		IResource rc = getResource();
		String languageId = currentLanguageSetting.getLanguageId();
		
		List<ICLanguageSettingEntry> entries = provider.getSettingEntries(cfgDescription, rc, languageId);
		if (entries==null) {
			entries = getSettingEntriesUpResourceTree(provider);
		}
		entries = new ArrayList<ICLanguageSettingEntry>(entries);
		return entries;
	}

	private ICLanguageSettingEntry doAdd() {
		ICLanguageSettingEntry selectedEntry = getSelectedEntry();
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		LanguageSettingEntryDialog dlg = new LanguageSettingEntryDialog(usercomp.getShell(), cfgDescription, selectedEntry, true);
		if (dlg.open()) {
			return dlg.getEntries()[0];
		}
		return null;
	}

	private void performAdd(ILanguageSettingsProvider selectedProvider) {
		if (selectedProvider instanceof ILanguageSettingsEditableProvider) {
			ICLanguageSettingEntry settingEntry = doAdd();
			if (settingEntry!=null) {
				selectedProvider = arrangeEditedCopy((ILanguageSettingsEditableProvider)selectedProvider);
				addEntry(selectedProvider, settingEntry);
			}
		}
	}
	
	/**
	 * @param selectedProvider
	 * @return
	 */
	private ILanguageSettingsEditableProvider arrangeEditedCopy(ILanguageSettingsEditableProvider selectedProvider) {
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		List<ILanguageSettingsProvider> initialProviders = initialProvidersMap.get(cfgDescription.getId());
		if (initialProviders.contains(selectedProvider)) {
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(cfgDescription.getLanguageSettingProviders());
			int pos = providers.indexOf(selectedProvider);
			if (pos>=0) {
				try {
					selectedProvider = selectedProvider.clone();
					providers.set(pos, selectedProvider);
					cfgDescription.setLanguageSettingProviders(providers);
				} catch (CloneNotSupportedException e) {
					CUIPlugin.log("Internal Error: cannot clone provider "+selectedProvider.getId(), e);
				}
			} else {
				CUIPlugin.getDefault().logErrorMessage("Internal Error: cannot find provider "+selectedProvider.getId());
			}
		}
		return selectedProvider;
	}

	private ICLanguageSettingEntry doEdit(ICLanguageSettingEntry ent) {
		ICLanguageSettingEntry selectedEntry = getSelectedEntry();
		ICConfigurationDescription cfgDecsription = getConfigurationDescription();
		LanguageSettingEntryDialog dlg = new LanguageSettingEntryDialog(usercomp.getShell(), cfgDecsription, selectedEntry);
		if (dlg.open()) {
			return dlg.getEntries()[0];
		}
		return null;
	}

	private void performEdit(ILanguageSettingsProvider selectedProvider, ICLanguageSettingEntry selectedEntry) {
		if (selectedProvider instanceof ILanguageSettingsEditableProvider) {
			ICLanguageSettingEntry settingEntry = doEdit(selectedEntry);
			if (settingEntry!=null) {
				selectedProvider = arrangeEditedCopy((ILanguageSettingsEditableProvider)selectedProvider);
				deleteEntry(selectedProvider, selectedEntry);
				addEntry(selectedProvider, settingEntry);
			}
		}
	}

	private void deleteEntry(ILanguageSettingsProvider provider, ICLanguageSettingEntry entry) {
		if (provider!=null && entry != null) {
			String providerId = provider.getId();
			
			List<ICLanguageSettingEntry> entries = getWritableEntries(provider);
			int pos = getExactIndex(getSettingEntriesUpResourceTree(provider), entry);
			entries.remove(entry);
			saveEntries(provider, entries);
			
			if (pos>=entries.size())
				pos = entries.size()-1;
			ICLanguageSettingEntry nextEntry = pos>=0 ? entries.get(pos) : null;
			
			updateTreeEntries();
			selectItem(providerId, nextEntry);
			updateButtons();
		}
	}

	private void clearProvider(ILanguageSettingsProvider provider) {
		if (provider!=null) {
			String providerId = provider.getId();
			List<ICLanguageSettingEntry> empty = new ArrayList<ICLanguageSettingEntry>();
			saveEntries(provider, empty);
			
			updateTreeEntries();
			selectItem(providerId, null);
			updateButtons();
		}
	}

	private void performDelete(ILanguageSettingsProvider selectedProvider, ICLanguageSettingEntry selectedEntry) {
		if (selectedProvider instanceof ILanguageSettingsEditableProvider) {
			selectedProvider = arrangeEditedCopy((ILanguageSettingsEditableProvider)selectedProvider);
			if (selectedEntry!=null) {
				deleteEntry(selectedProvider, selectedEntry);
			} else {
				clearProvider(selectedProvider);
			}
		}
	}

	private void moveEntry(ILanguageSettingsProvider provider, ICLanguageSettingEntry entry, boolean up) {
		if (provider!=null && entry != null) {
			String providerId = provider.getId();
			
			List<ICLanguageSettingEntry> entries = getWritableEntries(provider);
			int pos = getExactIndex(entries, entry);
			int newPos = up ? pos-1 : pos+1;
			Collections.swap(entries, pos, newPos);
			saveEntries(provider, entries);
			
			updateTreeEntries();
			selectItem(providerId, entry);
			updateButtons();
		}
	}

	private void performMoveUp(ILanguageSettingsProvider selectedProvider, ICLanguageSettingEntry selectedEntry) {
		if (selectedEntry!=null && (selectedProvider instanceof ILanguageSettingsEditableProvider)) {
			selectedProvider = arrangeEditedCopy((ILanguageSettingsEditableProvider)selectedProvider);
			moveEntry(selectedProvider, selectedEntry, true);
		}
	}

	private void performMoveDown(ILanguageSettingsProvider selectedProvider, ICLanguageSettingEntry selectedEntry) {
		if (selectedEntry!=null && (selectedProvider instanceof ILanguageSettingsEditableProvider)) {
			selectedProvider = arrangeEditedCopy((ILanguageSettingsEditableProvider)selectedProvider);
			moveEntry(selectedProvider, selectedEntry, false);
		}
	}

	/**
	 * Get list of providers to display in the settings entry tree.
	 */
	private List<ILanguageSettingsProvider> getProviders(ICLanguageSetting languageSetting) {
		List<ILanguageSettingsProvider> itemsList = new LinkedList<ILanguageSettingsProvider>();
		if (languageSetting!=null) {
			String langId = languageSetting.getLanguageId();
			if (langId != null) {
				IResource rc = getResource();
				ICConfigurationDescription cfgDescription = getConfigurationDescription();
				if (rc!=null && cfgDescription!=null) {
					List<ILanguageSettingsProvider> cfgProviders = cfgDescription.getLanguageSettingProviders();
					for (ILanguageSettingsProvider cfgProvider : cfgProviders) {
						ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(cfgProvider);
						if (rawProvider instanceof LanguageSettingsBaseProvider) {
							// filter out providers incapable of providing entries for this language
							List<String> languageIds = ((LanguageSettingsBaseProvider)rawProvider).getLanguageScope();
							if (languageIds!=null && !languageIds.contains(langId)) {
								continue;
							}
						}
						itemsList.add(cfgProvider);
					}
				}
			}
		}
		return itemsList;
	}

	/**
	 * Refreshes the entries tree in "Show Entries" mode.
	 */
	public void updateTreeEntries() {
		List<ILanguageSettingsProvider> tableItems = getProviders(currentLanguageSetting);
		treeEntriesViewer.setInput(tableItems.toArray(new Object[tableItems.size()]));
		updateStatusLine();
		updateButtons();
	}

	private ICLanguageSetting[] getLangSettings(ICResourceDescription rcDes) {
		switch (rcDes.getType()) {
		case ICSettingBase.SETTING_PROJECT:
		case ICSettingBase.SETTING_CONFIGURATION:
		case ICSettingBase.SETTING_FOLDER:
			ICFolderDescription foDes = (ICFolderDescription) rcDes;
			return foDes.getLanguageSettings();
		case ICSettingBase.SETTING_FILE:
			ICFileDescription fiDes = (ICFileDescription) rcDes;
			ICLanguageSetting langSetting = fiDes.getLanguageSetting();
			return (langSetting != null) ? new ICLanguageSetting[] { langSetting } : null;
		}
		return null;
	}

	private void updateTreeLanguages(ICResourceDescription rcDes) {
		treeLanguages.removeAll();
		TreeItem selectedLanguageItem = null;
		allLanguages = getLangSettings(rcDes);
		if (allLanguages != null) {
			Arrays.sort(allLanguages, CDTListComparator.getInstance());
			for (ICLanguageSetting langSetting : allLanguages) {
				String langId = langSetting.getLanguageId();
				if (langId==null || langId.length()==0)
					continue;

				LanguageManager langManager = LanguageManager.getInstance();
				ILanguageDescriptor langDes = langManager.getLanguageDescriptor(langId);
				if (langDes == null)
					continue;

				langId = langDes.getName();
				if (langId == null || langId.length()==0)
					continue;

				TreeItem t = new TreeItem(treeLanguages, SWT.NONE);
				t.setText(0, langId);
				t.setData(langSetting);
				if (selectedLanguageItem == null) {
					if (currentLanguageSetting!=null) {
						if (currentLanguageSetting.getLanguageId().equals(langSetting.getLanguageId())) {
							selectedLanguageItem = t;
							currentLanguageSetting = langSetting;
						}
					} else {
						selectedLanguageItem = t;
						currentLanguageSetting = langSetting;
					}
				}
			}

			if (selectedLanguageItem != null) {
				treeLanguages.setSelection(selectedLanguageItem);
			}
		}
	}

	/**
	 * Called when configuration changed Refreshes languages list entries tree.
	 */
	@Override
	public void updateData(ICResourceDescription rcDes) {
		if (!canBeVisible())
			return;

		if (rcDes!=null) {
			if (page.isMultiCfg()) {
				setAllVisible(false, null);
				return;
			} else {
				setAllVisible(true, null);
			}
			
			updateTreeLanguages(rcDes);
			updateTreeEntries();
			if (masterPropertyPage!=null) {
				boolean enabled = masterPropertyPage.isLanguageSettingsProvidersEnabled();
				enableProvidersCheckBox.setSelection(enabled);
				enableControls(enabled);
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
			boolean changed = false;
			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			IResource rc = getResource();
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			List<ILanguageSettingsProvider> writableProviders = new ArrayList<ILanguageSettingsProvider>(providers.size());
			
providers:	for (ILanguageSettingsProvider provider : providers) {
				ILanguageSettingsEditableProvider writableProvider = null;
				if (provider instanceof ILanguageSettingsEditableProvider) {
					TreeItem[] tisLang = treeLanguages.getItems();
					for (TreeItem tiLang : tisLang) {
						Object item = tiLang.getData();
						if (item instanceof ICLanguageSetting) {
							String languageId = ((ICLanguageSetting)item).getLanguageId();
							if (languageId!=null) {
								if (provider.getSettingEntries(cfgDescription, rc, languageId)!=null) {
									try {
										// clone providers to be able to "Cancel" in UI
										if (writableProvider==null) {
											writableProvider = ((ILanguageSettingsEditableProvider) provider).clone();
										}
										writableProvider.setSettingEntries(cfgDescription, rc, languageId, null);
										changed = true;
									} catch (CloneNotSupportedException e) {
										CUIPlugin.log("Internal Error: cannot clone provider "+provider.getId(), e);
										continue providers;
									}
								}
							}
						}
					}
				}
				if (writableProvider!=null)
					writableProviders.add(writableProvider);
				else
					writableProviders.add(provider);
			}
			if (changed) {
				cfgDescription.setLanguageSettingProviders(writableProviders);
//				updateTreeEntries();
//				updateData(getResDesc());
				List<ILanguageSettingsProvider> tableItems = getProviders(currentLanguageSetting);
				treeEntriesViewer.setInput(tableItems.toArray(new Object[tableItems.size()]));
			}
		}
	}

	@Override
	protected void performApply(ICResourceDescription srcRcDescription, ICResourceDescription destRcDescription) {
		if (!page.isForPrefs()) {
			ICConfigurationDescription srcCfgDescription = srcRcDescription.getConfiguration();
			ICConfigurationDescription destCfgDescription = destRcDescription.getConfiguration();

			List<ILanguageSettingsProvider> providers = srcCfgDescription.getLanguageSettingProviders();
			destCfgDescription.setLanguageSettingProviders(providers);
		}
		
		if (!page.isForPrefs()) {
			ICConfigurationDescription sd = srcRcDescription.getConfiguration();
			ICConfigurationDescription dd = destRcDescription.getConfiguration();
			List<ILanguageSettingsProvider> newProviders = sd.getLanguageSettingProviders();
			dd.setLanguageSettingProviders(newProviders);
		}

		performOK();
	}

	@Override
	protected void performOK() {
		if (page.isForProject() && enableProvidersCheckBox!=null) {
			boolean enabled = enableProvidersCheckBox.getSelection();
			if (masterPropertyPage!=null)
				enabled = masterPropertyPage.isLanguageSettingsProvidersEnabled();
			LanguageSettingsManager.setLanguageSettingsProvidersEnabled(page.getProject(), enabled);
			enableProvidersCheckBox.setSelection(enabled);
		}
		
		try {
			LanguageSettingsManager_TBD.serializeWorkspaceProviders();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UnsupportedOperationException("Internal Error");
		}

		trackInitialSettings();
		updateData(getResDesc());
	}

	@Override
	public boolean canBeVisible() {
		if (CDTPrefUtil.getBool(CDTPrefUtil.KEY_NO_SHOW_PROVIDERS))
			return false;
		if (page.isForPrefs())
			return true;

		ICLanguageSetting [] langSettings = getLangSettings(getResDesc());
		if (langSettings == null)
			return false;

		for (ICLanguageSetting langSetting : langSettings) {
			String langId = langSetting.getLanguageId();
			if (langId!=null && langId.length()>0) {
				LanguageManager langManager = LanguageManager.getInstance();
				ILanguageDescriptor langDes = langManager.getLanguageDescriptor(langId);
				if (langDes != null)
					return true;
			}
		}

		return false;
	}

	/**
	 * Shortcut for setting setting entries for current context.
	 *
	 */
	private void setSettingEntries(ILanguageSettingsEditableProvider provider, List<ICLanguageSettingEntry> entries) {
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		IResource rc = getResource();
		String languageId = currentLanguageSetting.getLanguageId();
		if (languageId!=null)
			provider.setSettingEntries(cfgDescription, rc, languageId, entries);
	}

	@Override
	protected boolean isIndexerAffected() {
//		List<ILanguageSettingsProvider> newProvidersList = null;
//		ICConfigurationDescription cfgDescription = getConfigurationDescription();
//		if (cfgDescription!=null) {
//			newProvidersList = cfgDescription.getLanguageSettingProviders();
//		}
//		boolean newEnablement = LanguageSettingsManager.isLanguageSettingsProvidersEnabled(page.getProject());
//		
//		boolean isEqualList = (newProvidersList==initialProvidersList) || (newProvidersList!=null && newProvidersList.equals(initialProvidersList));
//		return newEnablement!=initialEnablement || (newEnablement==true && !isEqualList);
		// FIXME
		return true;
	}

}

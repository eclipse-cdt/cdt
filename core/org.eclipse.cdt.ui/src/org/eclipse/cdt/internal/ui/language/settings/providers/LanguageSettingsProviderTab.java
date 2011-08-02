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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager_TBD;
import org.eclipse.cdt.core.model.ILanguageDescriptor;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.DialogsMessages;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.internal.ui.newui.StatusMessageLine;

/**
 * This tab presents language settings entries categorized by language
 * settings providers.
 *
 *@noinstantiate This class is not intended to be instantiated by clients.
 *@noextend This class is not intended to be subclassed by clients.
 */
public class LanguageSettingsProviderTab extends AbstractCPropertyTab {
	private static final String WORKSPACE_PREFERENCE_PAGE = "org.eclipse.cdt.ui.preferences.BuildSettingProperties"; //$NON-NLS-1$
	// TODO: generalize
	private static final String TEST_PLUGIN_ID_PATTERN = "org.eclipse.cdt.*.tests.*"; //$NON-NLS-1$

//	private static final String RENAME_STR = "Rename...";
//	private static final String RUN_STR = Messages.LanguageSettingsProviderTab_Run;
	private static final String CLEAR_STR = Messages.LanguageSettingsProviderTab_Clear;
	private static final String RESET_STR = "Reset";

//	private static final int BUTTON_RENAME = 0;
//	private static final int BUTTON_RUN = 0;
	private static final int BUTTON_CLEAR = 0;
	private static final int BUTTON_RESET = 1;
	// there is a separator instead of button #2
	private static final int BUTTON_MOVE_UP = 3;
	private static final int BUTTON_MOVE_DOWN = 4;

	private final static String[] BUTTON_LABELS_PROJECT = {
//		RENAME_STR,
//		RUN_STR,
		CLEAR_STR,
		RESET_STR,
		null,
		MOVEUP_STR,
		MOVEDOWN_STR,
	};
	
	private final static String[] BUTTON_LABELS_PREF = {
//		RENAME_STR,
//		RUN_STR,
		CLEAR_STR,
		RESET_STR,
	};
	
	private static final int[] DEFAULT_CONFIGURE_SASH_WEIGHTS = new int[] { 50, 50 };
	private SashForm sashFormConfigure;

	private Table tableProviders;
	private CheckboxTableViewer tableProvidersViewer;
	private Group groupOptionsPage;
	private ICOptionPage currentOptionsPage = null;
	private Composite compositeOptionsPage;
	
	private Button enableProvidersCheckBox;
	private StatusMessageLine fStatusLine;

	private Button globalProviderCheckBox = null;
	private Link linkWorkspacePreferences = null;
	
	private Page_LanguageSettingsProviders masterPropertyPage = null;

	/**
	 * List of providers presented to the user.
	 * For global providers included in a configuration this contains references
	 * not raw providers.
	 */
	private List<ILanguageSettingsProvider> presentedProviders = null;
	private final Map<String, ICOptionPage> optionsPageMap = new HashMap<String, ICOptionPage>();
	private Map<String, List<ILanguageSettingsProvider>> initialProvidersByCfg = new HashMap<String, List<ILanguageSettingsProvider>>();
	
	private boolean initialEnablement = false;
	
	/**
	 * Returns current working copy of the provider. Creates one if it has not been created yet.
	 * Used by option pages when there is a need to modify the provider.
	 * Warning: Do not cache the result as the provider can be replaced at any time.
	 * @param providerId 
	 * 
	 * @return the provider
	 */
	public ILanguageSettingsProvider getWorkingCopy(String providerId) {
		ILanguageSettingsProvider provider = findProvider(providerId, presentedProviders);
		if (isWorkingCopy(provider))
			return provider;
		
		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
		Assert.isTrue(rawProvider instanceof ILanguageSettingsEditableProvider);
		
		ILanguageSettingsEditableProvider editableProvider = (ILanguageSettingsEditableProvider)rawProvider;
		try {
			ILanguageSettingsEditableProvider newProvider = editableProvider.clone();
			replaceSelectedProvider(newProvider);
			return newProvider;
			
		} catch (CloneNotSupportedException e) {
			CUIPlugin.log("Error cloning provider " + editableProvider.getId(), e);
			// TODO warning dialog for user?
		}
		
		return null;
	}

	private class ProvidersTableLabelProvider extends LanguageSettingsProvidersLabelProvider {
		@Override
		protected String[] getOverlayKeys(ILanguageSettingsProvider provider) {
			String[] overlayKeys = super.getOverlayKeys(provider);
			
			ILanguageSettingsProvider rawProvider = page.isForPrefs() ? LanguageSettingsManager.getRawProvider(provider) : provider;
			if (LanguageSettingsManager_TBD.isReconfigured(rawProvider)) {
				overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_SETTING;
			}
			
			if (isWorkingCopy(provider)) {
				overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_EDITED;
			}
			return overlayKeys;
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
		if (page.isForPrefs())
			return null;
		
		ICConfigurationDescription cfgDescription = getResDesc().getConfiguration();
		return cfgDescription;
	}

	/**
	 * Shortcut for getting the currently selected provider.
	 */
	private ILanguageSettingsProvider getSelectedProvider() {
		ILanguageSettingsProvider provider = null;

		int pos = tableProviders.getSelectionIndex();
		if (pos >= 0 && pos<tableProviders.getItemCount()) {
			provider = (ILanguageSettingsProvider)tableProvidersViewer.getElementAt(pos);
		}
		return provider;
	}

	private void trackInitialSettings() {
		if (page.isForProject()) {
			ICConfigurationDescription[] cfgDescriptions = page.getCfgsEditable();
			for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
				if (cfgDescription!=null) {
					String cfgId = cfgDescription.getId();
					List<ILanguageSettingsProvider> initialProviders = cfgDescription.getLanguageSettingProviders();
					initialProvidersByCfg.put(cfgId, initialProviders);
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
		createConfigureSashForm();
		
		// Status line
		fStatusLine = new StatusMessageLine(usercomp, SWT.LEFT, 2);

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
		enableProvidersCheckBox.setEnabled(page.isForProject() /*|| page.isForPrefs()*/);
		enableControls(enableProvidersCheckBox.getSelection());

		if (page.isForPrefs()) {
			initButtons(BUTTON_LABELS_PREF);
		} else {
			initButtons(BUTTON_LABELS_PROJECT);
		}
		updateData(getResDesc());
	}

	private void createConfigureSashForm() {
		// SashForm for Configure
		sashFormConfigure = new SashForm(usercomp, SWT.VERTICAL);
		GridLayout layout = new GridLayout();
		sashFormConfigure.setLayout(layout);

		// Providers table
		Composite compositeSashForm = new Composite(sashFormConfigure, SWT.BORDER | SWT.SINGLE);
		compositeSashForm.setLayout(new GridLayout());
		
		// items checkboxes  only for project properties page
		tableProviders = new Table(compositeSashForm, page.isForPrefs() ? SWT.NONE : SWT.CHECK);
		tableProviders.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableProviders.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				displaySelectedOptionPage();
				updateButtons();
			}
		});
		tableProvidersViewer = new CheckboxTableViewer(tableProviders);
		tableProvidersViewer.setContentProvider(new ArrayContentProvider());
		tableProvidersViewer.setLabelProvider(new ProvidersTableLabelProvider());

		tableProvidersViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				saveCheckedProviders(e.getElement());
				tableProvidersViewer.update(e.getElement(), null);
			}});

		createOptionsControl();

		sashFormConfigure.setWeights(DEFAULT_CONFIGURE_SASH_WEIGHTS);
		enableSashForm(sashFormConfigure, true);
	}

	private Link createLinkToPreferences(final Composite parent) {
		Link link = new Link(parent, SWT.NONE);
//		// FIXME
//		link.setText(DialogsMessages.RegexErrorParserOptionPage_LinkToPreferencesMessage + " Select Discovery Tab.");

		link.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				// Use event.text to tell which link was used
				PreferencesUtil.createPreferenceDialogOn(parent.getShell(), WORKSPACE_PREFERENCE_PAGE, null, null).open();
			}
		});

		return link;
	}

	// Called from globalProviderCheckBox listener
	private void toggleGlobalProvider(ILanguageSettingsProvider oldProvider, boolean toGlobal) {
		ILanguageSettingsProvider newProvider = null;

		String id = oldProvider.getId();
		if (toGlobal) {
			newProvider = LanguageSettingsManager.getWorkspaceProvider(id);
		} else {
			// Local provider instance chosen
			try {
				ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(oldProvider);
				if (rawProvider instanceof ILanguageSettingsEditableProvider) {
					newProvider = ((ILanguageSettingsEditableProvider) rawProvider).cloneShallow();
				}
			} catch (CloneNotSupportedException e) {
				CUIPlugin.log("Error cloning provider " + oldProvider.getId(), e);
			}
		}
		if (newProvider!=null) {
			replaceSelectedProvider(newProvider);
			
			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			initializeOptionsPage(newProvider, cfgDescription);
			displaySelectedOptionPage();
		}
	}

	private void replaceSelectedProvider(ILanguageSettingsProvider newProvider) {
		int pos = tableProviders.getSelectionIndex();
		presentedProviders.set(pos, newProvider);
		tableProvidersViewer.setInput(presentedProviders);
		tableProviders.setSelection(pos);

		ICConfigurationDescription cfgDescription = null;
		if (!page.isForPrefs()) {
			cfgDescription = getConfigurationDescription();
			
			List<ILanguageSettingsProvider> cfgProviders = new ArrayList<ILanguageSettingsProvider>(cfgDescription.getLanguageSettingProviders());
			pos = getProviderIndex(newProvider.getId(), cfgProviders);
			cfgProviders.set(pos, newProvider);
			cfgDescription.setLanguageSettingProviders(cfgProviders);
			tableProvidersViewer.setCheckedElements(cfgProviders.toArray(new ILanguageSettingsProvider[0]));
		}
		refreshItem(newProvider);
	}

	public void refreshItem(ILanguageSettingsProvider provider) {
		tableProvidersViewer.refresh(provider);
		updateButtons();
	}

	private void createOptionsControl() {
		groupOptionsPage = new Group(sashFormConfigure, SWT.SHADOW_ETCHED_IN);
		groupOptionsPage.setText("Language Settings Provider Options");
		groupOptionsPage.setLayout(new GridLayout(2, false));
		
		if (!page.isForPrefs()) {
			if (globalProviderCheckBox==null) {
				globalProviderCheckBox = new Button(groupOptionsPage, SWT.CHECK);
				globalProviderCheckBox.setText("Use global provider sharing settings among projects");
				globalProviderCheckBox.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						boolean isGlobal = globalProviderCheckBox.getSelection();
						ILanguageSettingsProvider provider = getSelectedProvider();
						if (isGlobal != LanguageSettingsManager.isWorkspaceProvider(provider)) {
							toggleGlobalProvider(provider, isGlobal);
						}
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						widgetSelected(e);
					}

				});
				
				linkWorkspacePreferences = createLinkToPreferences(groupOptionsPage);
			}
		}

		compositeOptionsPage = new Composite(groupOptionsPage, SWT.NONE);
		compositeOptionsPage.setLayout(new TabFolderLayout());
	}

	private void enableSashForm(SashForm sashForm, boolean enable) {
		sashForm.setVisible(enable);
		// Some of woodoo to fill properties page vertically and still keep right border visible in preferences 
		GridData gd = new GridData(enable || page.isForPrefs() ? GridData.FILL_BOTH : SWT.NONE);
		gd.horizontalSpan = 2;
		gd.heightHint = enable ? SWT.DEFAULT : 0;
		sashForm.setLayoutData(gd);
	}

	private void enableControls(boolean enable) {
		sashFormConfigure.setEnabled(enable);
		tableProviders.setEnabled(enable);
		compositeOptionsPage.setEnabled(enable);
		
		buttoncomp.setEnabled(enable);

		if (enable) {
			displaySelectedOptionPage();
		} else {
			if (currentOptionsPage != null) {
				currentOptionsPage.setVisible(false);
			}
			disableButtons();
		}
	}
	
	/**
	 * Populate provider tables and their option pages which are used in Configure mode
	 */
	private void updateProvidersTable() {
		ILanguageSettingsProvider selectedProvider = getSelectedProvider();
		String selectedId = selectedProvider!=null ? selectedProvider.getId() : null;

		// update viewer if the list of providers changed
		tableProvidersViewer.setInput(presentedProviders);

		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		if (cfgDescription!=null) {
			List<ILanguageSettingsProvider> cfgProviders = cfgDescription.getLanguageSettingProviders();
			tableProvidersViewer.setCheckedElements(cfgProviders.toArray(new ILanguageSettingsProvider[0]));
		}
	
		if (selectedId!=null) {
			for (int i=0; i<presentedProviders.size(); i++) {
				if (selectedId.equals(presentedProviders.get(i).getId())) {
					tableProviders.setSelection(i);
					break;
				}
			}
		}
		
		optionsPageMap.clear();
		for (ILanguageSettingsProvider provider : presentedProviders) {
			initializeOptionsPage(provider, cfgDescription);
		}

		displaySelectedOptionPage();
	}

	private void initializeProviders() {
		// The providers list is formed to consist of configuration providers (checked elements on top of the table)
		// and after that other providers which could be possible added (unchecked) sorted by name.

		List<String> idsList = new ArrayList<String>();

		List<ILanguageSettingsProvider> providers;
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		if (cfgDescription!=null) {
			providers = new ArrayList<ILanguageSettingsProvider>(cfgDescription.getLanguageSettingProviders());
			for (ILanguageSettingsProvider provider : providers) {
				idsList.add(provider.getId());
			}
		} else {
			providers =  new ArrayList<ILanguageSettingsProvider>();
		}
		
		List<ILanguageSettingsProvider> workspaceProviders = LanguageSettingsManager.getWorkspaceProviders();
		
		// ensure sorting by name all unchecked providers
		Set<ILanguageSettingsProvider> allAvailableProvidersSet = new TreeSet<ILanguageSettingsProvider>(new Comparator<ILanguageSettingsProvider>() {
			public int compare(ILanguageSettingsProvider prov1, ILanguageSettingsProvider prov2) {
				Boolean isTest1 = prov1.getId().matches(TEST_PLUGIN_ID_PATTERN);
				Boolean isTest2 = prov2.getId().matches(TEST_PLUGIN_ID_PATTERN);
				int result = isTest1.compareTo(isTest2);
				if (result==0)
					result = prov1.getName().compareTo(prov2.getName());
				return result;
			}
		});
		allAvailableProvidersSet.addAll(workspaceProviders);

		for (ILanguageSettingsProvider provider : allAvailableProvidersSet) {
			String id = provider.getId();
			if (!idsList.contains(id)) {
				providers.add(provider);
				idsList.add(id);
			}
		}
		
		// renders better when using temporary
		presentedProviders = providers;
		tableProvidersViewer.setInput(presentedProviders);
	}

	private ICOptionPage createOptionsPage(ILanguageSettingsProvider provider, ICConfigurationDescription cfgDescription) {
		ICOptionPage optionsPage = null;
		if (provider!=null) {
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			if (rawProvider!=null) {
				optionsPage = LanguageSettingsProviderAssociation.createOptionsPage(rawProvider);
			}

			if (optionsPage instanceof AbstractLanguageSettingProviderOptionPage) {
				((AbstractLanguageSettingProviderOptionPage)optionsPage).init(this, provider.getId());
			}
		}
		
		return optionsPage;
	}

	private void initializeOptionsPage(ILanguageSettingsProvider provider, ICConfigurationDescription cfgDescription) {
		ICOptionPage optionsPage = createOptionsPage(provider, cfgDescription);
		
		if (optionsPage!=null) {
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			boolean isEditableForProject = page.isForProject() && provider instanceof ILanguageSettingsEditableProvider;
			boolean isEditableForPrefs = page.isForPrefs() && rawProvider instanceof ILanguageSettingsEditableProvider;
			boolean isEditable = isEditableForProject || isEditableForPrefs;
			compositeOptionsPage.setEnabled(isEditable);
	
			String id = (provider!=null) ? provider.getId() : null;
			optionsPageMap.put(id, optionsPage);
			optionsPage.setContainer(page);
			optionsPage.createControl(compositeOptionsPage);
			optionsPage.setVisible(false);
			compositeOptionsPage.layout(true);
		}
	}

	private void displaySelectedOptionPage() {
		if (currentOptionsPage != null) {
			currentOptionsPage.setVisible(false);
		}

		ILanguageSettingsProvider provider = getSelectedProvider();
		String id = (provider!=null) ? provider.getId() : null;

		boolean isGlobal = LanguageSettingsManager.isWorkspaceProvider(provider);
		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);

		currentOptionsPage = optionsPageMap.get(id);

		boolean isChecked = tableProvidersViewer.getChecked(provider);
		if (!page.isForPrefs()) {
			boolean isRawProviderEditable = rawProvider instanceof ILanguageSettingsEditableProvider;
			globalProviderCheckBox.setSelection(isGlobal);
			globalProviderCheckBox.setEnabled(isChecked && isRawProviderEditable);
			globalProviderCheckBox.setVisible(provider!=null);
			
			boolean needPreferencesLink=isGlobal && currentOptionsPage!=null;
			// TODO: message
			linkWorkspacePreferences.setText(needPreferencesLink ? DialogsMessages.RegexErrorParserOptionPage_LinkToPreferencesMessage + " Select Discovery Tab." : "");
			linkWorkspacePreferences.pack();
		}
		
		if (currentOptionsPage != null) {
			boolean isEditableForProject = page.isForProject() && provider instanceof ILanguageSettingsEditableProvider;
			boolean isEditableForPrefs = page.isForPrefs() && rawProvider instanceof ILanguageSettingsEditableProvider;
			boolean isEditable = isEditableForProject || isEditableForPrefs;
			currentOptionsPage.getControl().setEnabled(isEditable);
			currentOptionsPage.setVisible(true);

			compositeOptionsPage.setEnabled(isEditable);
//			compositeOptionsPage.layout(true);
		}
	}


	private void saveCheckedProviders(Object selectedElement) {
		if (page.isForProject()) {
			Object[] checked = tableProvidersViewer.getCheckedElements();
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(checked.length);
			for (Object element : checked) {
				ILanguageSettingsProvider provider = (ILanguageSettingsProvider)element;
				providers.add(provider);
			}
			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			cfgDescription.setLanguageSettingProviders(providers);
			
			if (selectedElement!=null) {
				tableProvidersViewer.update(selectedElement, null);
				if (selectedElement instanceof ILanguageSettingsProvider) {
					ILanguageSettingsProvider selectedProvider = (ILanguageSettingsProvider) selectedElement;
					initializeOptionsPage(selectedProvider, cfgDescription);
					displaySelectedOptionPage();
				}
			}
		}
	}

	private void disableButtons() {
//		buttonSetEnabled(BUTTON_RENAME, false);
//		buttonSetEnabled(BUTTON_RUN, false);
		buttonSetEnabled(BUTTON_CLEAR, false);
		buttonSetEnabled(BUTTON_RESET, false);
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
		boolean isProviderSelected =provider!=null;
		boolean canForWorkspace = isProviderSelected && page.isForPrefs();
		boolean canForProject = isProviderSelected && page.isForProject() && !LanguageSettingsManager.isWorkspaceProvider(provider);

		int pos = tableProviders.getSelectionIndex();
		int count = tableProviders.getItemCount();
		int last = count - 1;
		boolean isRangeOk = pos >= 0 && pos <= last;

		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
		boolean canClear = false;
		if (rawProvider instanceof ILanguageSettingsEditableProvider) {
			if (!((ILanguageSettingsEditableProvider) rawProvider).isEmpty()) {
				canClear = canForWorkspace || canForProject;
			}
		}
		
		boolean canReset = false;
		if (rawProvider!=null && (canForWorkspace || canForProject)) {
			canReset = ! LanguageSettingsManager_TBD.isEqualExtensionProvider(rawProvider);
		}
		
		boolean canMoveUp = page.isForProject() && isProviderSelected && isRangeOk && pos!=0;
		boolean canMoveDown = page.isForProject() && isProviderSelected && isRangeOk && pos!=last;
		
//		buttonSetEnabled(BUTTON_RENAME, false);
//		buttonSetEnabled(BUTTON_RUN, false);
		buttonSetEnabled(BUTTON_CLEAR, canClear);
		buttonSetEnabled(BUTTON_RESET, canReset);
		buttonSetEnabled(BUTTON_MOVE_UP, canMoveUp);
		buttonSetEnabled(BUTTON_MOVE_DOWN, canMoveDown);
	}

	/**
	 * Displays warning message - if any - for selected language settings entry.
	 */
	private void updateStatusLine() {
//		IStatus status=null;
//		fStatusLine.setErrorStatus(status);
	}

	/**
	 * Handle buttons
	 */
	@Override
	public void buttonPressed(int buttonIndex) {
		ILanguageSettingsProvider selectedProvider = getSelectedProvider();

		switch (buttonIndex) {
//		case BUTTON_RENAME:
//			performRename(selectedProvider);
//			break;
//		case BUTTON_RUN:
//			performRun(selectedProvider);
//			break;
		case BUTTON_CLEAR:
			performClear(selectedProvider);
			break;
		case BUTTON_RESET:
			performReset(selectedProvider);
			break;
		case BUTTON_MOVE_UP:
			performMoveUp(selectedProvider);
			break;
		case BUTTON_MOVE_DOWN:
			performMoveDown(selectedProvider);
			break;
		default:
		}
	}

	private void performClear(ILanguageSettingsProvider selectedProvider) {
		if (isWorkingCopy(selectedProvider)) {
			if (selectedProvider instanceof ILanguageSettingsEditableProvider) {
				ILanguageSettingsEditableProvider editableProvider = (ILanguageSettingsEditableProvider) selectedProvider;
				editableProvider.clear();
				tableProvidersViewer.update(selectedProvider, null);
			}
		} else {
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(selectedProvider);
			if (rawProvider instanceof ILanguageSettingsEditableProvider) {
				ILanguageSettingsEditableProvider editableProvider = (ILanguageSettingsEditableProvider) rawProvider;
				
				try {
					ILanguageSettingsEditableProvider newProvider = editableProvider.cloneShallow();
					replaceSelectedProvider(newProvider);

					ICConfigurationDescription cfgDescription = getConfigurationDescription();
					initializeOptionsPage(newProvider, cfgDescription);
					displaySelectedOptionPage();
					
				} catch (CloneNotSupportedException e) {
					CUIPlugin.log("Error cloning provider " + editableProvider.getId(), e);
					return;
				}
			}
			
		}
		updateButtons();
	}

	private void performReset(ILanguageSettingsProvider selectedProvider) {
		ILanguageSettingsProvider newProvider = LanguageSettingsManager.getExtensionProviderCopy(selectedProvider.getId());
		replaceSelectedProvider(newProvider);

		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		initializeOptionsPage(newProvider, cfgDescription);
		displaySelectedOptionPage();
		updateButtons();
	}

	private boolean isWorkingCopy(ILanguageSettingsProvider provider) {
		boolean isWorkingCopy = false;
		if (page.isForPrefs()) {
			isWorkingCopy = ! LanguageSettingsManager.isWorkspaceProvider(provider);
		} else {
			if (!LanguageSettingsManager.isWorkspaceProvider(provider)) {
				ICConfigurationDescription cfgDescription = getConfigurationDescription();
				List<ILanguageSettingsProvider> initialProviders = initialProvidersByCfg.get(cfgDescription.getId());
				isWorkingCopy = ! initialProviders.contains(provider);
			}
			
		}
		return isWorkingCopy;
	}

	private void performMoveUp(ILanguageSettingsProvider selectedProvider) {
		int pos = presentedProviders.indexOf(selectedProvider);
		if (pos > 0) {
			moveProvider(pos, pos-1);
		}
	}

	private void performMoveDown(ILanguageSettingsProvider selectedProvider) {
		int pos = presentedProviders.indexOf(selectedProvider);
		int last = presentedProviders.size() - 1;
		if (pos >= 0 && pos < last) {
			moveProvider(pos, pos+1);
		}
	}

	private void moveProvider(int oldPos, int newPos) {
		Collections.swap(presentedProviders, oldPos, newPos);
		
		updateProvidersTable();
		tableProviders.setSelection(newPos);
		
		saveCheckedProviders(null);
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

	/**
	 * Called when configuration changed
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
			
			if (masterPropertyPage!=null) {
				boolean enabled = masterPropertyPage.isLanguageSettingsProvidersEnabled();
				enableProvidersCheckBox.setSelection(enabled);
				enableControls(enabled);
			}
		}

		// for Preference page initialize providers list just once as no configuration here to change
		// and re-initializing could ruins modified providers in case of switching tabs or pages
		if (!page.isForPrefs() || presentedProviders==null) {
			initializeProviders();
		}
		updateProvidersTable();
		updateButtons();
	}

	@Override
	protected void performDefaults() {
		if (enableProvidersCheckBox==null || enableProvidersCheckBox.getSelection()==false)
			return;
		
		if (page.isForPrefs() || page.isForProject()) {
			if (MessageDialog.openQuestion(usercomp.getShell(),
					Messages.LanguageSettingsProviderTab_TitleResetProviders,
					Messages.LanguageSettingsProviderTab_AreYouSureToResetProviders)) {
				
				if (page.isForProject()) {
					ICConfigurationDescription cfgDescription = getConfigurationDescription();
					List<ILanguageSettingsProvider> cfgProviders = new ArrayList<ILanguageSettingsProvider>(cfgDescription.getLanguageSettingProviders());
					boolean atLeastOneChanged = false;
					for (int i=0;i<cfgProviders.size();i++) {
						ILanguageSettingsProvider provider = cfgProviders.get(i);
						if (!LanguageSettingsManager.isWorkspaceProvider(provider) && !LanguageSettingsManager_TBD.isEqualExtensionProvider(provider)) {
							ILanguageSettingsProvider extProvider = LanguageSettingsManager.getExtensionProviderCopy(provider.getId());
							cfgProviders.set(i, extProvider);
							atLeastOneChanged = true;
						}
					}
					if (atLeastOneChanged) {
						cfgDescription.setLanguageSettingProviders(cfgProviders);
					}
					
				} else if (page.isForPrefs()) {
					int pos = tableProviders.getSelectionIndex();
					List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(presentedProviders);
					for (int i=0;i<providers.size();i++) {
						ILanguageSettingsProvider provider = providers.get(i);
						ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
						if (!LanguageSettingsManager_TBD.isEqualExtensionProvider(rawProvider)) {
							ILanguageSettingsProvider extProvider = LanguageSettingsManager.getExtensionProviderCopy(provider.getId());
							providers.set(i, extProvider);
						}
					}
					// seems to render better when temporary is used
					presentedProviders = providers;
					tableProvidersViewer.setInput(presentedProviders);
					tableProviders.setSelection(pos);
					
					updateButtons();
				}
			}
			
			updateData(getResDesc());
		}
	}

	@Override
	protected void performApply(ICResourceDescription srcRcDescription, ICResourceDescription destRcDescription) {
//		informOptionPages(true);

		if (!page.isForPrefs()) {
			IResource rc = getResource();

			ICConfigurationDescription srcCfgDescription = srcRcDescription.getConfiguration();
			ICConfigurationDescription destCfgDescription = destRcDescription.getConfiguration();

			List<ILanguageSettingsProvider> destProviders = new ArrayList<ILanguageSettingsProvider>();

			List<ILanguageSettingsProvider> srcProviders = srcCfgDescription.getLanguageSettingProviders();
			for (ILanguageSettingsProvider pro : srcProviders) {
				// TODO: clone
				destProviders.add(pro);
			}

			destCfgDescription.setLanguageSettingProviders(destProviders);
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
		if (!page.isForPrefs()) {
			// FIXME: for now only handles current configuration
			ICResourceDescription rcDesc = getResDesc();
			IResource rc = getResource();
			ICConfigurationDescription cfgDescription = rcDesc.getConfiguration();
			
			List<ILanguageSettingsProvider> destProviders = new ArrayList<ILanguageSettingsProvider>();
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			for (ILanguageSettingsProvider pro : providers) {
				// TODO: clone
				destProviders.add(pro);
			}
			cfgDescription.setLanguageSettingProviders(destProviders);
		}
		
		// Build Settings page
		if (page.isForPrefs()) {
			try {
				LanguageSettingsManager.setWorkspaceProviders(presentedProviders);
			} catch (CoreException e) {
				CUIPlugin.log("Error setting user defined providers", e);
			}
			initializeProviders();
		}
		
		if (page.isForProject() && enableProvidersCheckBox!=null) {
			boolean enabled = enableProvidersCheckBox.getSelection();
			if (masterPropertyPage!=null)
				enabled = masterPropertyPage.isLanguageSettingsProvidersEnabled();
			LanguageSettingsManager.setLanguageSettingsProvidersEnabled(page.getProject(), enabled);
			enableProvidersCheckBox.setSelection(enabled);
		}
		
		Collection<ICOptionPage> optionPages = optionsPageMap.values();
		for (ICOptionPage op : optionPages) {
			try {
				op.performApply(null);
			} catch (CoreException e) {
				CUIPlugin.log("Error applying options page", e);
			}
		}

		try {
			LanguageSettingsManager_TBD.serializeWorkspaceProviders();
		} catch (CoreException e) {
			CUIPlugin.log("Internal Error", e);
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
		
		if (!page.isForProject())
			return false;

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

	@Override
	protected boolean isIndexerAffected() {
		List<ILanguageSettingsProvider> newProvidersList = null;
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		if (cfgDescription!=null) {
			newProvidersList = cfgDescription.getLanguageSettingProviders();
		}
		boolean newEnablement = LanguageSettingsManager.isLanguageSettingsProvidersEnabled(page.getProject());
		
		// TODO
		boolean isEqualList = false;
//		boolean isEqualList = (newProvidersList==initialProvidersMap) || (newProvidersList!=null && newProvidersList.equals(initialProvidersMap));
		return newEnablement!=initialEnablement || (newEnablement==true && !isEqualList);
	}

	private ILanguageSettingsProvider findRawProvider(String id, List<ILanguageSettingsProvider> providers) {
		for (ILanguageSettingsProvider provider : providers) {
			if (provider.getId().equals(id)) {
				provider = LanguageSettingsManager.getRawProvider(provider);
				return provider;
			}
		}
		return null;
	}

	private ILanguageSettingsProvider findProvider(String id, List<ILanguageSettingsProvider> providers) {
		for (ILanguageSettingsProvider provider : providers) {
			if (provider.getId().equals(id)) {
				return provider;
			}
		}
		return null;
	}
	
	public ILanguageSettingsProvider getProvider(String id) {
		return findProvider(id, presentedProviders);
	}
	
	private int getProviderIndex(String id, List<ILanguageSettingsProvider> providers) {
		int pos = 0;
		for (ILanguageSettingsProvider p : providers) {
			if (p.getId().equals(id))
				return pos;
			pos++;
		}
		return -1;
	}

//	private void informOptionPages(boolean apply) {
//	Collection<ICOptionPage> pages = optionsPageMap.values();
//	for (ICOptionPage dynamicPage : pages) {
//		if (dynamicPage!=null && dynamicPage.isValid() && dynamicPage.getControl() != null) {
//			try {
//				if (apply)
//					dynamicPage.performApply(new NullProgressMonitor());
//				else
//					dynamicPage.performDefaults();
//			} catch (CoreException e) {
//				CUIPlugin.log("ErrorParsTab.error.OnApplyingSettings", e);
//			}
//		}
//	}
//}

}

/*******************************************************************************
 * Copyright (c) 2010, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.language.settings.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider;
import org.eclipse.cdt.core.language.settings.providers.ScannerDiscoveryLegacySupport;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.internal.ui.newui.StatusMessageLine;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.cdt.ui.language.settings.providers.AbstractLanguageSettingProviderOptionPage;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
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

/**
 * This tab presents language settings entries categorized by language
 * settings providers.
 *
 *@noinstantiate This class is not intended to be instantiated by clients.
 *@noextend This class is not intended to be subclassed by clients.
 */
public class LanguageSettingsProviderTab extends AbstractCPropertyTab {
	private static final String WORKSPACE_PREFERENCE_PAGE = "org.eclipse.cdt.ui.preferences.BuildSettingProperties"; //$NON-NLS-1$
	private static final String TEST_PLUGIN_ID_PATTERN = "org.eclipse.cdt.*.tests.*"; //$NON-NLS-1$

	private static final String CLEAR_STR = Messages.LanguageSettingsProviderTab_Clear;
	private static final String RESET_STR = Messages.LanguageSettingsProviderTab_Reset;

	private static final int BUTTON_CLEAR = 0;
	private static final int BUTTON_RESET = 1;
	// there is a separator instead of button #2
	private static final int BUTTON_MOVE_UP = 3;
	private static final int BUTTON_MOVE_DOWN = 4;

	private static final int[] DEFAULT_CONFIGURE_SASH_WEIGHTS = new int[] { 50, 50 };
	private SashForm sashFormProviders;

	private Table tableProviders;
	private CheckboxTableViewer tableProvidersViewer;
	private Group groupOptionsPage;
	private ICOptionPage currentOptionsPage = null;
	private Composite compositeOptionsPage;

	private StatusMessageLine fStatusLine;

	private Button sharedProviderCheckBox = null;
	private Link linkToWorkspacePreferences = null;
	private Button projectStorageCheckBox = null;

	private LanguageSettingsProvidersPage masterPropertyPage = null;

	/**
	 * List of providers presented to the user.
	 * For global providers included in a configuration this contains references
	 * not raw providers.
	 */
	private List<ILanguageSettingsProvider> presentedProviders = null;
	private final Map<String, ICOptionPage> optionsPageMap = new HashMap<>();
	private Map<String/*cfgId*/, List<ILanguageSettingsProvider>> initialProvidersByCfg = new HashMap<>();

	/**
	 * Label provider for language settings providers displayed by this tab.
	 */
	private class ProvidersTableLabelProvider extends LanguageSettingsProvidersLabelProvider {
		@Override
		protected String[] getOverlayKeys(ILanguageSettingsProvider provider) {
			String[] overlayKeys = super.getOverlayKeys(provider);
			if (provider.getName() == null) {
				return overlayKeys;
			}

			if (page.isForProject()) {
				if (isEditedForProject(provider)) {
					overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_EDITED;
				} else if (!LanguageSettingsManager.getExtensionProviderIds().contains(provider.getId())) {
					overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_USER;
				} else if (isReconfiguredForProject(provider)) {
					overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_SETTING;
				}
			} else if (page.isForPrefs()) {
				if (isWorkingCopy(provider) && !provider.equals(LanguageSettingsManager
						.getRawProvider(LanguageSettingsManager.getWorkspaceProvider(provider.getId())))) {
					overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_EDITED;
				} else if (!LanguageSettingsManager.getExtensionProviderIds().contains(provider.getId())) {
					overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_USER;
				} else {
					ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
					if (rawProvider instanceof ILanguageSettingsEditableProvider
							&& !LanguageSettingsManager.isEqualExtensionProvider(rawProvider, false)) {
						overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_SETTING;
					}
				}
			}

			return overlayKeys;
		}

		@Override
		public String getText(Object element) {
			if (element instanceof ILanguageSettingsProvider) {
				ILanguageSettingsProvider provider = (ILanguageSettingsProvider) element;
				String name = provider.getName();
				if (name != null && (page.isForPrefs() || isPresentedAsShared(provider))) {
					return name + Messages.LanguageSettingsProvidersLabelProvider_TextDecorator_Shared;
				}
			}
			return super.getText(element);
		}
	}

	/**
	 * Returns the provider which is being presented to the user in UI.
	 * Used by option pages when there is a need.
	 * Warning: Do not cache the result as the provider can be replaced at any time.
	 * @param id - id of the provider.
	 *
	 * @return the provider.
	 */
	public ILanguageSettingsProvider getProvider(String id) {
		return findProvider(id, presentedProviders);
	}

	/**
	 * Returns the provider equal to provider at the point from which editing started.
	 * Used by option pages when there is a need.
	 * @param id - id of the provider.
	 *
	 * @return the initial provider.
	 */
	public ILanguageSettingsProvider getInitialProvider(String id) {
		ILanguageSettingsProvider initialProvider = null;
		if (page.isForPrefs()) {
			initialProvider = LanguageSettingsManager.getWorkspaceProvider(id);
		} else {
			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			List<ILanguageSettingsProvider> initialProviders = initialProvidersByCfg.get(cfgDescription.getId());
			initialProvider = findProvider(id, initialProviders);
		}
		return initialProvider;
	}

	/**
	 * Check if the provider is a working copy and can be modified.
	 */
	private boolean isWorkingCopy(ILanguageSettingsProvider provider) {
		boolean isWorkingCopy = false;
		if (page.isForPrefs()) {
			isWorkingCopy = !LanguageSettingsManager.isWorkspaceProvider(provider);
		} else {
			if (!LanguageSettingsManager.isWorkspaceProvider(provider)) {
				ICConfigurationDescription cfgDescription = getConfigurationDescription();
				List<ILanguageSettingsProvider> initialProviders = initialProvidersByCfg.get(cfgDescription.getId());
				isWorkingCopy = initialProviders != null && !initialProviders.contains(provider);
			}

		}
		return isWorkingCopy;
	}

	/**
	 * Returns current working copy of the provider. Creates one if it has not been created yet.
	 * A working copy will be discarded if user pushes [Cancel] or it will replace original
	 * provider on [Apply] or [OK].
	 *
	 * This method is used also by option pages when there is a need to modify the provider.
	 * Warning: Do not cache the result as the provider can be replaced at any time.
	 *
	 * @param id - id of the provider.
	 * @return working copy of the provider.
	 */
	public ILanguageSettingsProvider getWorkingCopy(String id) {
		ILanguageSettingsProvider provider = findProvider(id, presentedProviders);
		if (isWorkingCopy(provider)) {
			return provider;
		}

		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
		ILanguageSettingsEditableProvider newProvider = LanguageSettingsManager
				.getProviderCopy((ILanguageSettingsEditableProvider) rawProvider, true);
		if (newProvider != null) {
			replaceSelectedProvider(newProvider);
			// Warning: Do not initializeOptionsPage() here as the method can be called from an existing page
		}

		return newProvider;
	}

	/**
	 * Refresh provider item in the table and update buttons.
	 * This method is intended for use by an Options Page of the provider.
	 *
	 * @param provider - provider item in the table to refresh.
	 */
	public void refreshItem(ILanguageSettingsProvider provider) {
		tableProvidersViewer.refresh(provider);
		updateButtons();
	}

	/**
	 * Check if provider should get "reconfigured" overlay in UI.
	 */
	private boolean isReconfiguredForProject(ILanguageSettingsProvider provider) {
		String id = provider.getId();

		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		String[] defaultIds = ((ILanguageSettingsProvidersKeeper) cfgDescription)
				.getDefaultLanguageSettingsProvidersIds();
		List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription)
				.getLanguageSettingProviders();

		// check for the provider mismatch in configuration list vs. default list from the tool-chain
		if (defaultIds != null && (Arrays.asList(defaultIds).contains(id) != providers.contains(provider))) {
			return true;
		}

		// check if provider belongs to configuration (i.e. checked in the table)
		if (!providers.contains(provider)) {
			return false;
		}

		// check if "shared" flag matches default shared preference from extension point definition
		if (LanguageSettingsManager.isPreferShared(id) != LanguageSettingsManager.isWorkspaceProvider(provider)) {
			return true;
		}

		// check if configuration provider equals to the default one from extension point
		if (!LanguageSettingsManager.isWorkspaceProvider(provider)
				&& !LanguageSettingsManager.isEqualExtensionProvider(provider, false)) {
			return true;
		}

		return false;
	}

	/**
	 * Check if provider should get "edited" overlay in UI.
	 */
	private boolean isEditedForProject(ILanguageSettingsProvider provider) {
		String id = provider.getId();
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		List<ILanguageSettingsProvider> initialProviders = initialProvidersByCfg.get(cfgDescription.getId());
		List<ILanguageSettingsProvider> providers = getCheckedProviders();

		// check for the provider mismatch in configuration list vs. initial list
		ILanguageSettingsProvider initialProvider = findProvider(id, initialProviders);
		if ((initialProvider != null) != providers.contains(provider)) {
			return true;
		}

		// check if "shared" flag matches that of initial provider
		if (providers.contains(provider) && LanguageSettingsManager
				.isWorkspaceProvider(initialProvider) != LanguageSettingsManager.isWorkspaceProvider(provider)) {
			return true;
		}

		// check if configuration provider equals to the initial one
		if (!LanguageSettingsManager.isWorkspaceProvider(provider) && !provider.equals(initialProvider)) {
			return true;
		}

		return false;
	}

	/**
	 * Checks if the provider should be presented as shared. Unchecked providers are shown as non-shared
	 * if they are defined as non-shared in extension point even if in fact shared instance is used to display
	 * the options page.
	 */
	private boolean isPresentedAsShared(ILanguageSettingsProvider provider) {
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription)
				.getLanguageSettingProviders();
		return LanguageSettingsManager.isWorkspaceProvider(provider)
				&& (providers.contains(provider) || LanguageSettingsManager.isPreferShared(provider.getId()));
	}

	/**
	 * Find provider with a given ID in the list or {@code null}.
	 */
	private ILanguageSettingsProvider findProvider(String id, List<ILanguageSettingsProvider> providers) {
		if (providers != null) {
			for (ILanguageSettingsProvider provider : providers) {
				if (provider.getId().equals(id)) {
					return provider;
				}
			}
		}
		return null;
	}

	/**
	 * Shortcut for getting the currently selected provider.
	 * Do not use if you need to change provider's settings or entries, use {@link #getWorkingCopy(String)}.
	 */
	private ILanguageSettingsProvider getSelectedProvider() {
		ILanguageSettingsProvider provider = null;

		int pos = tableProviders.getSelectionIndex();
		if (pos >= 0 && pos < tableProviders.getItemCount()) {
			provider = (ILanguageSettingsProvider) tableProvidersViewer.getElementAt(pos);
		}
		return provider;
	}

	/**
	 * Shortcut for getting the current configuration description.
	 */
	private ICConfigurationDescription getConfigurationDescription() {
		if (page.isForPrefs()) {
			return null;
		}

		return getResDesc().getConfiguration();
	}

	/**
	 * Get the list of providers checked in the table in UI.
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<ILanguageSettingsProvider> getCheckedProviders() {
		return (List) Arrays.asList(tableProvidersViewer.getCheckedElements());
	}

	/**
	 * Replace the selected provider in UI and in configuration.
	 */
	private void replaceSelectedProvider(ILanguageSettingsProvider newProvider) {
		int pos = tableProviders.getSelectionIndex();
		boolean isChecked = tableProvidersViewer.getChecked(tableProvidersViewer.getElementAt(pos));

		presentedProviders.set(pos, newProvider);
		tableProvidersViewer.refresh();
		tableProvidersViewer.setChecked(newProvider, isChecked);
		tableProviders.setSelection(pos);

		saveCheckedProviders();
		tableProvidersViewer.refresh(newProvider);
	}

	/**
	 * Save checked providers from UI table into configuration.
	 */
	private void saveCheckedProviders() {
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		if (cfgDescription instanceof ILanguageSettingsProvidersKeeper) {
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(getCheckedProviders());
		}
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
					List<ILanguageSettingsProvider> initialProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
							.getLanguageSettingProviders();
					initialProvidersByCfg.put(cfgId, initialProviders);
				}
			}
		}
	}

	/**
	 * Create table to display providers.
	 */
	private void createProvidersPane(Composite parent) {
		Composite composite = new Composite(parent, SWT.BORDER | SWT.SINGLE);
		composite.setLayout(new GridLayout());

		// items checkboxes  only for project properties page
		tableProviders = new Table(composite, page.isForPrefs() ? SWT.NONE : SWT.CHECK);
		tableProviders.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableProviders.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				displaySelectedOptionPage();
				updateButtons();
			}
		});
		tableProvidersViewer = new CheckboxTableViewer(tableProviders);
		tableProvidersViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableProvidersViewer.setLabelProvider(new ProvidersTableLabelProvider());

		tableProvidersViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				ILanguageSettingsProvider checkedProvider = (ILanguageSettingsProvider) event.getElement();
				String id = checkedProvider.getId();
				ILanguageSettingsProvider newProvider = null;

				if (event.getChecked()) {
					if (LanguageSettingsManager.isWorkspaceProvider(checkedProvider)
							&& !LanguageSettingsManager.isPreferShared(id)) {
						newProvider = getInitialProvider(id);
						if (newProvider == null) {
							ILanguageSettingsProvider rawProvider = LanguageSettingsManager
									.getRawProvider(checkedProvider);
							if (rawProvider instanceof ILanguageSettingsEditableProvider) {
								newProvider = LanguageSettingsManager
										.getProviderCopy((ILanguageSettingsEditableProvider) rawProvider, false);
							}
						}
					}
				} else {
					if (!LanguageSettingsManager.isWorkspaceProvider(checkedProvider)) {
						newProvider = LanguageSettingsManager.getWorkspaceProvider(id);
					}
				}

				int pos = presentedProviders.indexOf(checkedProvider);
				tableProviders.setSelection(pos);

				if (newProvider != null) {
					replaceSelectedProvider(newProvider); // will refresh and save checked providers
					createOptionsPage(newProvider);
				} else {
					saveCheckedProviders();
					tableProvidersViewer.refresh(checkedProvider);
					// option page is reused
				}

				displaySelectedOptionPage();
				updateButtons();
			}
		});
	}

	/**
	 * Change "globality" of a provider.
	 */
	private ILanguageSettingsProvider toggleGlobalProvider(ILanguageSettingsProvider provider, boolean toGlobal) {
		ILanguageSettingsProvider newProvider = null;

		String id = provider.getId();
		if (toGlobal) {
			newProvider = LanguageSettingsManager.getWorkspaceProvider(id);
		} else {
			// Toggle to configuration-owned provider
			newProvider = getInitialProvider(id);
			if (newProvider == null || LanguageSettingsManager.isWorkspaceProvider(newProvider)) {
				try {
					ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
					if (rawProvider instanceof ILanguageSettingsEditableProvider) {
						newProvider = ((ILanguageSettingsEditableProvider) rawProvider).cloneShallow();
					}
				} catch (CloneNotSupportedException e) {
					CUIPlugin.log("Error cloning provider " + id, e); //$NON-NLS-1$
				}
			}
		}
		if (newProvider != null) {
			replaceSelectedProvider(newProvider);
			createOptionsPage(newProvider);
			displaySelectedOptionPage();
			updateButtons();
		} else {
			newProvider = provider;
		}

		return newProvider;
	}

	/**
	 * Create a check-box for "shared" or "global" property of a provider.
	 */
	private void createSharedProviderCheckBox(Composite parent) {
		sharedProviderCheckBox = new Button(parent, SWT.CHECK);
		sharedProviderCheckBox.setText(Messages.LanguageSettingsProviderTab_ShareProviders);
		sharedProviderCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean isGlobal = sharedProviderCheckBox.getSelection();
				ILanguageSettingsProvider provider = getSelectedProvider();
				if (isGlobal != LanguageSettingsManager.isWorkspaceProvider(provider)) {
					// globality changed
					provider = toggleGlobalProvider(provider, isGlobal);
				}
				projectStorageCheckBox.setSelection(
						provider instanceof LanguageSettingsSerializableProvider && LanguageSettingsManager
								.isStoringEntriesInProjectArea((LanguageSettingsSerializableProvider) provider));
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
	}

	/**
	 * Create a check-box defining where to store entries of a provider.
	 */
	private void createProjectStorageCheckBox(Composite parent) {
		projectStorageCheckBox = new Button(parent, SWT.CHECK);
		projectStorageCheckBox.setLayoutData(new GridData(SWT.END, SWT.NONE, false, false));
		projectStorageCheckBox.setText(Messages.LanguageSettingsProviderTab_StoreEntriesInsideProject);
		projectStorageCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean inProjectArea = projectStorageCheckBox.getSelection();
				ILanguageSettingsProvider newProvider = getWorkingCopy(getSelectedProvider().getId());
				LanguageSettingsManager.setStoringEntriesInProjectArea(
						(LanguageSettingsSerializableProvider) newProvider, inProjectArea);
				replaceSelectedProvider(newProvider);
				createOptionsPage(newProvider);
				displaySelectedOptionPage();
				updateButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
	}

	/**
	 * Create a link to Preferences page.
	 */
	private void createLinkToPreferences(final Composite parent, int span) {
		linkToWorkspacePreferences = new Link(parent, SWT.NONE);
		String href = NLS.bind("<a href=\"workspace\">{0}</a>", Messages.LanguageSettingsProviderTab_WorkspaceSettings); //$NON-NLS-1$
		linkToWorkspacePreferences.setText(
				NLS.bind(Messages.LanguageSettingsProviderTab_OptionsCanBeChangedInPreferencesDiscoveryTab, href));
		GridData gd = new GridData();
		gd.horizontalSpan = span;
		linkToWorkspacePreferences.setLayoutData(gd);

		linkToWorkspacePreferences.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// Use event.text to tell which link was used
				PreferencesUtil.createPreferenceDialogOn(parent.getShell(), WORKSPACE_PREFERENCE_PAGE, null, null)
						.open();
			}
		});
	}

	/**
	 * Create Options pane.
	 */
	private void createOptionsPane(Composite parent) {
		groupOptionsPage = new Group(parent, SWT.SHADOW_ETCHED_IN);
		groupOptionsPage.setText(Messages.LanguageSettingsProviderTab_LanguageSettingsProvidersOptions);
		groupOptionsPage.setLayout(new GridLayout(2, false));

		if (!page.isForPrefs()) {
			createSharedProviderCheckBox(groupOptionsPage);
			createProjectStorageCheckBox(groupOptionsPage);
			createLinkToPreferences(groupOptionsPage, 2);
		}

		// composite to span over 2 columns
		Composite comp = new Composite(groupOptionsPage, SWT.NONE);
		comp.setLayout(new GridLayout());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 2;
		comp.setLayoutData(gd);

		compositeOptionsPage = new Composite(comp, SWT.NONE);
		compositeOptionsPage.setLayout(new TabFolderLayout());
		compositeOptionsPage.addControlListener(new ControlListener() {
			@Override
			public void controlResized(ControlEvent e) {
				compositeOptionsPage.setBounds(compositeOptionsPage.getParent().getClientArea());
			}

			@Override
			public void controlMoved(ControlEvent e) {
			}
		});
	}

	/**
	 * Create sash form.
	 */
	private void createSashForm() {
		sashFormProviders = new SashForm(usercomp, SWT.VERTICAL);
		GridLayout layout = new GridLayout();
		sashFormProviders.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		sashFormProviders.setLayoutData(gd);

		createProvidersPane(sashFormProviders);
		createOptionsPane(sashFormProviders);

		sashFormProviders.setWeights(DEFAULT_CONFIGURE_SASH_WEIGHTS);
	}

	/**
	 * Gray out or restore all controls except enabling check-box.
	 */
	private void enableTabControls(boolean enable) {
		sashFormProviders.setEnabled(enable);
		tableProviders.setEnabled(enable);
		compositeOptionsPage.setEnabled(enable);

		buttoncomp.setEnabled(enable);

		if (enable) {
			displaySelectedOptionPage();
		} else {
			if (currentOptionsPage != null) {
				currentOptionsPage.setVisible(false);
			}

			buttonSetEnabled(BUTTON_CLEAR, false);
			buttonSetEnabled(BUTTON_RESET, false);
			buttonSetEnabled(BUTTON_MOVE_UP, false);
			buttonSetEnabled(BUTTON_MOVE_DOWN, false);
		}
	}

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);

		usercomp.setLayout(new GridLayout());
		GridData gd = (GridData) usercomp.getLayoutData();
		// Discourage settings entry table from trying to show all its items at once, see bug 264330
		gd.heightHint = 1;

		if (page instanceof LanguageSettingsProvidersPage) {
			masterPropertyPage = (LanguageSettingsProvidersPage) page;
		}

		trackInitialSettings();

		createSashForm();

		fStatusLine = new StatusMessageLine(usercomp, SWT.LEFT, 2);
		if (!page.isForPrefs() && !page.isMultiCfg()) {
			enableTabControls(masterPropertyPage.isLanguageSettingsProvidersEnabled());
		}

		String[] buttonLabels;
		if (page.isForPrefs()) {
			buttonLabels = new String[2];
			buttonLabels[BUTTON_CLEAR] = CLEAR_STR;
			buttonLabels[BUTTON_RESET] = RESET_STR;
		} else {
			buttonLabels = new String[5];
			buttonLabels[BUTTON_CLEAR] = CLEAR_STR;
			buttonLabels[BUTTON_RESET] = RESET_STR;
			buttonLabels[BUTTON_MOVE_UP] = MOVEUP_STR;
			buttonLabels[BUTTON_MOVE_DOWN] = MOVEDOWN_STR;
		}
		initButtons(buttonLabels);

		updateData(getResDesc());
	}

	/**
	 * Clear entries of the selected provider.
	 */
	private void performClear(ILanguageSettingsProvider selectedProvider) {
		if (isWorkingCopy(selectedProvider)) {
			if (selectedProvider instanceof LanguageSettingsSerializableProvider) {
				LanguageSettingsSerializableProvider editableProvider = (LanguageSettingsSerializableProvider) selectedProvider;
				editableProvider.clear();
				tableProvidersViewer.update(selectedProvider, null);
			}
		} else {
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(selectedProvider);
			if (rawProvider instanceof ILanguageSettingsEditableProvider) {
				ILanguageSettingsEditableProvider newProvider = LanguageSettingsManager
						.getProviderCopy((ILanguageSettingsEditableProvider) rawProvider, false);
				if (newProvider != null) {
					replaceSelectedProvider(newProvider);
					createOptionsPage(newProvider);
					displaySelectedOptionPage();
				}
			}
		}
		updateButtons();
	}

	/**
	 * Reset settings of the selected provider.
	 */
	private void performReset(ILanguageSettingsProvider selectedProvider) {
		String id = selectedProvider.getId();

		ILanguageSettingsProvider newProvider = null;
		if (page.isForPrefs()) {
			newProvider = LanguageSettingsManager.getExtensionProviderCopy(id, true);
			if (newProvider == null) {
				Status status = new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, IStatus.ERROR,
						Messages.GeneralMessages_InternalError_ReportLogToCdtTeam,
						new Exception("Internal Error getting copy of provider id=" + id)); //$NON-NLS-1$
				fStatusLine.setErrorStatus(status);
				CUIPlugin.log(status);
			}
		} else {
			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			String[] defaultIds = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getDefaultLanguageSettingsProvidersIds();
			boolean isDefault = Arrays.asList(defaultIds).contains(id);
			if (isDefault && !LanguageSettingsManager.isPreferShared(id)) {
				newProvider = LanguageSettingsManager.getExtensionProviderCopy(id, true);
				if (newProvider == null) {
					Status status = new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, IStatus.ERROR,
							Messages.GeneralMessages_InternalError_ReportLogToCdtTeam,
							new Exception("Internal Error getting copy of provider id=" + id)); //$NON-NLS-1$
					fStatusLine.setErrorStatus(status);
					CUIPlugin.log(status);
				}
			} else {
				newProvider = LanguageSettingsManager.getWorkspaceProvider(id);
			}
			tableProvidersViewer.setChecked(selectedProvider, isDefault);
		}

		if (newProvider != null) {
			replaceSelectedProvider(newProvider);
			createOptionsPage(newProvider);
			displaySelectedOptionPage();
			updateButtons();
		}
	}

	/**
	 * Move selected provider in the table.
	 */
	private void moveProvider(int oldPos, int newPos) {
		Collections.swap(presentedProviders, oldPos, newPos);
		tableProvidersViewer.refresh();
		tableProviders.showSelection();

		saveCheckedProviders();
		updateButtons();
	}

	/**
	 * Move selected provider up.
	 */
	private void performMoveUp(ILanguageSettingsProvider selectedProvider) {
		int pos = presentedProviders.indexOf(selectedProvider);
		if (pos > 0) {
			moveProvider(pos, pos - 1);
		}
	}

	/**
	 * Move selected provider down.
	 */
	private void performMoveDown(ILanguageSettingsProvider selectedProvider) {
		int pos = presentedProviders.indexOf(selectedProvider);
		int last = presentedProviders.size() - 1;
		if (pos >= 0 && pos < last) {
			moveProvider(pos, pos + 1);
		}
	}

	/**
	 * Handle pressed buttons.
	 */
	@Override
	public void buttonPressed(int buttonIndex) {
		ILanguageSettingsProvider selectedProvider = getSelectedProvider();

		switch (buttonIndex) {
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

	/**
	 * Updates state for all buttons.
	 */
	@Override
	protected void updateButtons() {
		ILanguageSettingsProvider provider = getSelectedProvider();
		boolean isProviderSelected = provider != null;
		boolean canForWorkspace = isProviderSelected && page.isForPrefs();
		boolean canForProject = isProviderSelected && page.isForProject();

		int pos = tableProviders.getSelectionIndex();
		int count = tableProviders.getItemCount();
		int last = count - 1;
		boolean isRangeOk = (pos >= 0 && pos <= last);

		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
		boolean isAllowedClearing = rawProvider instanceof ILanguageSettingsEditableProvider
				&& rawProvider instanceof LanguageSettingsSerializableProvider
				&& LanguageSettingsProviderAssociationManager.isAllowedToClear(rawProvider);

		boolean canClear = isAllowedClearing
				&& (canForWorkspace || (canForProject && !LanguageSettingsManager.isWorkspaceProvider(provider)));
		if (rawProvider instanceof LanguageSettingsSerializableProvider) {
			canClear = canClear && !((LanguageSettingsSerializableProvider) rawProvider).isEmpty();
		}

		boolean canResetForProject = canForProject && isReconfiguredForProject(provider);
		boolean canResetForWorkspace = canForWorkspace
				&& (rawProvider instanceof ILanguageSettingsEditableProvider
						&& !LanguageSettingsManager.isEqualExtensionProvider(rawProvider, false))
				&& (LanguageSettingsManager.getExtensionProviderIds().contains(rawProvider.getId()));
		boolean canReset = canResetForProject || canResetForWorkspace;

		boolean canMoveUp = canForProject && isRangeOk && pos != 0;
		boolean canMoveDown = canForProject && isRangeOk && pos != last;

		buttonSetEnabled(BUTTON_CLEAR, canClear);
		buttonSetEnabled(BUTTON_RESET, canReset);
		buttonSetEnabled(BUTTON_MOVE_UP, canMoveUp);
		buttonSetEnabled(BUTTON_MOVE_DOWN, canMoveDown);
	}

	/**
	 * Sort providers displayed in UI. Sorting is by name except test providers are shown
	 * on bottom.
	 */
	private void sortByName(List<ILanguageSettingsProvider> providers) {
		// ensure sorting by name all unchecked providers
		Collections.sort(providers, new Comparator<ILanguageSettingsProvider>() {
			@Override
			public int compare(ILanguageSettingsProvider prov1, ILanguageSettingsProvider prov2) {
				Boolean isTest1 = prov1.getId().matches(TEST_PLUGIN_ID_PATTERN);
				Boolean isTest2 = prov2.getId().matches(TEST_PLUGIN_ID_PATTERN);
				int result = isTest1.compareTo(isTest2);
				if (result == 0) {
					String name1 = prov1.getName();
					String name2 = prov2.getName();
					if (name1 != null && name2 != null) {
						result = name1.compareTo(name2);
					}
				}
				return result;
			}
		});
	}

	/**
	 * Initialize providers list.
	 */
	private void initializeProviders() {
		// The providers list is formed to consist of configuration providers (checked elements on top of the table)
		// and after that other providers which could be possible added (unchecked) sorted by name.

		List<String> idsList = new ArrayList<>();

		List<ILanguageSettingsProvider> providers;
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		if (cfgDescription instanceof ILanguageSettingsProvidersKeeper) {
			providers = new ArrayList<>(
					((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders());
			for (ILanguageSettingsProvider provider : providers) {
				idsList.add(provider.getId());
			}
		} else {
			providers = new ArrayList<>();
		}

		List<ILanguageSettingsProvider> allAvailableProvidersSet = LanguageSettingsManager.getWorkspaceProviders();
		sortByName(allAvailableProvidersSet);

		for (ILanguageSettingsProvider provider : allAvailableProvidersSet) {
			String id = provider.getId();
			if (!idsList.contains(id) && ScannerDiscoveryLegacySupport.isProviderCompatible(id, cfgDescription)) {
				providers.add(provider);
				idsList.add(id);
			}
		}

		// renders better when using temporary
		presentedProviders = providers;

		ILanguageSettingsProvider selectedProvider = getSelectedProvider();
		String selectedId = selectedProvider != null ? selectedProvider.getId() : null;

		tableProvidersViewer.setInput(presentedProviders);
		if (selectedId != null) {
			for (int i = 0; i < presentedProviders.size(); i++) {
				if (selectedId.equals(presentedProviders.get(i).getId())) {
					tableProviders.setSelection(i);
					break;
				}
			}
		}
	}

	/**
	 * Get option page from {@link LanguageSettingsProviderAssociationManager}.
	 */
	private ICOptionPage getOptionsPage(ILanguageSettingsProvider provider) {
		ICOptionPage optionsPage = null;
		if (provider != null) {
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			if (rawProvider != null) {
				optionsPage = LanguageSettingsProviderAssociationManager.createOptionsPage(rawProvider);
			}

			if (optionsPage instanceof AbstractLanguageSettingProviderOptionPage) {
				((AbstractLanguageSettingProviderOptionPage) optionsPage).init(this, provider.getId());
			}
		}

		return optionsPage;
	}

	/**
	 * Create Options page for a provider.
	 */
	private void createOptionsPage(ILanguageSettingsProvider provider) {
		ICOptionPage optionsPage = getOptionsPage(provider);

		if (optionsPage != null) {
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			boolean isEditableForProject = page.isForProject() && provider instanceof ILanguageSettingsEditableProvider;
			boolean isEditableForPrefs = page.isForPrefs() && rawProvider instanceof ILanguageSettingsEditableProvider;
			boolean isEditable = isEditableForProject || isEditableForPrefs;
			compositeOptionsPage.setEnabled(isEditable);

			String id = (provider != null) ? provider.getId() : null;
			optionsPageMap.put(id, optionsPage);
			optionsPage.setContainer(page);
			optionsPage.createControl(compositeOptionsPage);
			optionsPage.setVisible(false);
			compositeOptionsPage.layout(true);
		}
	}

	/**
	 * Display selected option page.
	 */
	private void displaySelectedOptionPage() {
		if (currentOptionsPage != null) {
			currentOptionsPage.setVisible(false);
		}

		ILanguageSettingsProvider provider = getSelectedProvider();
		String id = (provider != null) ? provider.getId() : null;

		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);

		currentOptionsPage = optionsPageMap.get(id);

		if (!page.isForPrefs()) {
			boolean isChecked = tableProvidersViewer.getChecked(provider);
			boolean isShared = isPresentedAsShared(provider);
			boolean isRawProviderEditable = rawProvider instanceof ILanguageSettingsEditableProvider;

			sharedProviderCheckBox.setVisible(provider != null);
			sharedProviderCheckBox.setEnabled(isChecked && isRawProviderEditable);
			sharedProviderCheckBox.setSelection(isShared);

			projectStorageCheckBox.setVisible(rawProvider instanceof LanguageSettingsSerializableProvider);
			projectStorageCheckBox.setEnabled(isChecked && !isShared);
			projectStorageCheckBox
					.setSelection(provider instanceof LanguageSettingsSerializableProvider && LanguageSettingsManager
							.isStoringEntriesInProjectArea((LanguageSettingsSerializableProvider) provider));

			linkToWorkspacePreferences.setVisible(isShared && currentOptionsPage != null);
			linkToWorkspacePreferences.setEnabled(isChecked);
		}

		if (currentOptionsPage != null) {
			currentOptionsPage.setVisible(true);

			boolean isEditableForProject = page.isForProject() && provider instanceof ILanguageSettingsEditableProvider;
			boolean isEditableForPrefs = page.isForPrefs() && rawProvider instanceof ILanguageSettingsEditableProvider;
			boolean isEditable = isEditableForProject || isEditableForPrefs;
			currentOptionsPage.getControl().setEnabled(isEditable);
			compositeOptionsPage.setEnabled(isEditable);
			compositeOptionsPage.layout(true);
		}
	}

	/**
	 * Populate provider tables and their option pages
	 */
	private void updateProvidersTable() {
		ILanguageSettingsProvider selectedProvider = getSelectedProvider();
		String selectedId = selectedProvider != null ? selectedProvider.getId() : null;
		boolean wasChecked = selectedProvider != null && tableProvidersViewer.getChecked(selectedProvider);

		// update viewer if the list of providers changed
		int pos = tableProviders.getSelectionIndex();
		tableProvidersViewer.setInput(presentedProviders);
		tableProviders.setSelection(pos);

		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		if (cfgDescription instanceof ILanguageSettingsProvidersKeeper) {
			List<ILanguageSettingsProvider> cfgProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			tableProvidersViewer.setCheckedElements(cfgProviders.toArray(new ILanguageSettingsProvider[0]));
		}

		if (selectedId != null) {
			for (int i = 0; i < presentedProviders.size(); i++) {
				ILanguageSettingsProvider provider = presentedProviders.get(i);
				if (selectedId.equals(provider.getId())) {
					boolean isChecked = tableProvidersViewer.getChecked(provider);
					if (isChecked || isChecked == wasChecked) {
						tableProviders.setSelection(i);
					} else {
						tableProviders.setSelection(0);
					}
					break;
				}
			}
		}
		tableProvidersViewer.refresh();

		optionsPageMap.clear();
		for (ILanguageSettingsProvider provider : presentedProviders) {
			SafeRunner.run(() -> {
				createOptionsPage(provider);
			});
		}

		displaySelectedOptionPage();
	}

	/**
	 * Update the tab. Called when configuration changes.
	 */
	@Override
	public void updateData(ICResourceDescription rcDes) {
		if (!canBeVisible())
			return;

		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		if (cfgDescription != null) {
			String cfgId = cfgDescription.getId();
			if (!initialProvidersByCfg.containsKey(cfgId)) {
				if (cfgDescription instanceof ILanguageSettingsProvidersKeeper) {
					List<ILanguageSettingsProvider> initialProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
							.getLanguageSettingProviders();
					initialProvidersByCfg.put(cfgId, initialProviders);
				}
			}
		}

		if (rcDes != null) {
			if (page.isMultiCfg()) {
				setAllVisible(false, null);
				return;
			} else {
				setAllVisible(true, null);
			}

			if (masterPropertyPage != null) {
				boolean enabled = masterPropertyPage.isLanguageSettingsProvidersEnabled();
				enableTabControls(enabled);
			}
		}

		// for Preference page initialize providers list just once as no configuration here to change
		// and re-initializing could overwrite modified providers in case of switching tabs or pages
		if (!page.isForPrefs() || presentedProviders == null) {
			initializeProviders();
		}
		updateProvidersTable();
		updateButtons();
	}

	@Override
	protected void performDefaults() {
		if (page.isForPrefs() || page.isForProject()) {
			if (MessageDialog.openQuestion(usercomp.getShell(),
					Messages.LanguageSettingsProviderTab_TitleResetProviders,
					Messages.LanguageSettingsProviderTab_AreYouSureToResetProviders)) {

				if (page.isForProject()) {
					// set project LSP enablement to that of workspace
					masterPropertyPage.setLanguageSettingsProvidersEnabled(
							ScannerDiscoveryLegacySupport.isLanguageSettingsProvidersFunctionalityEnabled(null));
					ICConfigurationDescription cfgDescription = getConfigurationDescription();
					if (cfgDescription instanceof ILanguageSettingsProvidersKeeper) {
						List<ILanguageSettingsProvider> cfgProviders = new ArrayList<>(
								((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders());
						String[] defaultIds = ((ILanguageSettingsProvidersKeeper) cfgDescription)
								.getDefaultLanguageSettingsProvidersIds();
						List<ILanguageSettingsProvider> newProviders = LanguageSettingsManager
								.createLanguageSettingsProviders(defaultIds);
						if (!cfgProviders.equals(newProviders)) {
							((ILanguageSettingsProvidersKeeper) cfgDescription)
									.setLanguageSettingProviders(newProviders);
						}
					}

				} else if (page.isForPrefs()) {
					presentedProviders = new ArrayList<>();
					for (String id : LanguageSettingsManager.getExtensionProviderIds()) {
						ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(id);
						ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
						if (!LanguageSettingsManager.isEqualExtensionProvider(rawProvider, true)) {
							ILanguageSettingsProvider extProvider = LanguageSettingsManager.getExtensionProviderCopy(id,
									true);
							if (extProvider != null) {
								provider = extProvider;
							}
						}
						presentedProviders.add(provider);
					}
					sortByName(presentedProviders);
				}
			}

			ICResourceDescription rcDescription = getResDesc();

			updateData(rcDescription);
			// update other tabs
			if (masterPropertyPage != null) {
				masterPropertyPage.informAll(UPDATE, rcDescription);
			}
		}
	}

	@Override
	protected void performApply(ICResourceDescription srcRcDescription, ICResourceDescription destRcDescription) {
		if (!page.isForPrefs()) {
			ICConfigurationDescription sd = srcRcDescription.getConfiguration();
			ICConfigurationDescription dd = destRcDescription.getConfiguration();
			if (sd instanceof ILanguageSettingsProvidersKeeper && dd instanceof ILanguageSettingsProvidersKeeper) {
				List<ILanguageSettingsProvider> newProviders = ((ILanguageSettingsProvidersKeeper) sd)
						.getLanguageSettingProviders();
				((ILanguageSettingsProvidersKeeper) dd).setLanguageSettingProviders(newProviders);
			}
		}

		performOK();

		trackInitialSettings();
		updateData(getResDesc());
	}

	@Override
	protected void performOK() {
		// give option pages a chance for provider-specific pre-apply actions
		Collection<ICOptionPage> optionPages = optionsPageMap.values();
		for (ICOptionPage op : optionPages) {
			try {
				op.performApply(null);
			} catch (CoreException e) {
				CUIPlugin.log("Error applying options page", e); //$NON-NLS-1$
			}
		}

		if (page.isForPrefs()) {
			try {
				LanguageSettingsManager.setWorkspaceProviders(presentedProviders);
			} catch (CoreException e) {
				CUIPlugin.log("Error setting user defined providers", e); //$NON-NLS-1$
			}
			initializeProviders();
		}

		if (masterPropertyPage != null && masterPropertyPage.isLanguageSettingsProvidersEnabled()) {
			masterPropertyPage.applyLanguageSettingsProvidersEnabled();
		}
	}

	@Override
	public boolean canBeVisible() {
		if (!ScannerDiscoveryLegacySupport.isLanguageSettingsProvidersFunctionalityEnabled(null)) {
			return false;
		}

		return page.isForPrefs() || page.isForProject();
	}

}

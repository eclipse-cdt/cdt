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
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.cdt.internal.ui.util.Messages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * A registry for all extensions to the
 * <code>org.eclipse.cdt.ui.completionProposalComputer</code>
 * extension point.
 *
 * @since 4.0
 */
public final class CompletionProposalComputerRegistry {

	private static final String EXTENSION_POINT = "completionProposalComputer"; //$NON-NLS-1$

	/** The singleton instance. */
	private static CompletionProposalComputerRegistry fgSingleton = null;

	/**
	 * Returns the default computer registry.
	 * <p>
	 * TODO keep this or add some other singleton, e.g. CUIPlugin?
	 * </p>
	 *
	 * @return the singleton instance
	 */
	public static synchronized CompletionProposalComputerRegistry getDefault() {
		if (fgSingleton == null) {
			fgSingleton = new CompletionProposalComputerRegistry();
		}

		return fgSingleton;
	}

	/**
	 * The sets of descriptors, grouped by partition type (key type:
	 * {@link String}, value type:
	 * {@linkplain List List&lt;CompletionProposalComputerDescriptor&gt;}).
	 */
	private final Map<String, List<CompletionProposalComputerDescriptor>> fDescriptorsByPartition = new HashMap<>();
	/**
	 * Unmodifiable versions of the sets stored in
	 * <code>fDescriptorsByPartition</code> (key type: {@link String},
	 * value type:
	 * {@linkplain List List&lt;CompletionProposalComputerDescriptor&gt;}).
	 */
	private final Map<String, List<CompletionProposalComputerDescriptor>> fPublicDescriptorsByPartition = new HashMap<>();
	/**
	 * All descriptors (element type:
	 * {@link CompletionProposalComputerDescriptor}).
	 */
	private final List<CompletionProposalComputerDescriptor> fDescriptors = new ArrayList<>();
	/**
	 * Unmodifiable view of <code>fDescriptors</code>
	 */
	private final List<CompletionProposalComputerDescriptor> fPublicDescriptors = Collections
			.unmodifiableList(fDescriptors);

	private final List<CompletionProposalCategory> fCategories = new ArrayList<>();
	private final List<CompletionProposalCategory> fPublicCategories = Collections.unmodifiableList(fCategories);
	/**
	 * <code>true</code> if this registry has been loaded.
	 */
	private boolean fLoaded = false;

	/**
	 * Creates a new instance.
	 */
	public CompletionProposalComputerRegistry() {
	}

	/**
	 * Returns the list of {@link CompletionProposalComputerDescriptor}s describing all extensions
	 * to the <code>completionProposalComputer</code> extension point for the given partition
	 * type.
	 * <p>
	 * A valid partition is either one of the constants defined in
	 * {@link org.eclipse.cdt.ui.text.ICPartitions} or
	 * {@link org.eclipse.jface.text.IDocument#DEFAULT_CONTENT_TYPE}. An empty list is returned if
	 * there are no extensions for the given partition.
	 * </p>
	 * <p>
	 * The returned list is read-only and is sorted in the order that the extensions were read in.
	 * There are no duplicate elements in the returned list. The returned list may change if plug-ins
	 * are loaded or unloaded while the application is running or if an extension violates the API
	 * contract of {@link org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer}. When
	 * computing proposals, it is therefore imperative to copy the returned list before iterating
	 * over it.
	 * </p>
	 *
	 * @param partition
	 *        the partition type for which to retrieve the computer descriptors
	 * @return the list of extensions to the <code>completionProposalComputer</code> extension
	 *         point (element type: {@link CompletionProposalComputerDescriptor})
	 */
	List<CompletionProposalComputerDescriptor> getProposalComputerDescriptors(String partition) {
		ensureExtensionPointRead();
		List<CompletionProposalComputerDescriptor> result = fPublicDescriptorsByPartition.get(partition);
		if (result == null)
			return Collections.emptyList();
		return result;
	}

	/**
	 * Returns the list of {@link CompletionProposalComputerDescriptor}s describing all extensions
	 * to the <code>completionProposalComputer</code> extension point.
	 * <p>
	 * The returned list is read-only and is sorted in the order that the extensions were read in.
	 * There are no duplicate elements in the returned list. The returned list may change if plug-ins
	 * are loaded or unloaded while the application is running or if an extension violates the API
	 * contract of {@link org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer}. When
	 * computing proposals, it is therefore imperative to copy the returned list before iterating
	 * over it.
	 * </p>
	 *
	 * @return the list of extensions to the <code>completionProposalComputer</code> extension
	 *         point (element type: {@link CompletionProposalComputerDescriptor})
	 */
	List<CompletionProposalComputerDescriptor> getProposalComputerDescriptors() {
		ensureExtensionPointRead();
		return fPublicDescriptors;
	}

	/**
	 * Returns the list of proposal categories contributed to the
	 * <code>completionProposalComputer</code> extension point.
	 * <p>
	 * <p>
	 * The returned list is read-only and is sorted in the order that the extensions were read in.
	 * There are no duplicate elements in the returned list. The returned list may change if
	 * plug-ins are loaded or unloaded while the application is running.
	 * </p>
	 *
	 * @return list of proposal categories contributed to the
	 *         <code>completionProposalComputer</code> extension point (element type:
	 *         {@link CompletionProposalCategory})
	 */
	public List<CompletionProposalCategory> getProposalCategories() {
		ensureExtensionPointRead();
		return fPublicCategories;
	}

	/**
	 * Ensures that the extensions are read and stored in
	 * <code>fDescriptorsByPartition</code>.
	 */
	private void ensureExtensionPointRead() {
		boolean reload;
		synchronized (this) {
			reload = !fLoaded;
			fLoaded = true;
		}
		if (reload)
			reload();
	}

	/**
	 * Reloads the extensions to the extension point.
	 * <p>
	 * This method can be called more than once in order to reload from
	 * a changed extension registry.
	 * </p>
	 */
	public void reload() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		List<IConfigurationElement> elements = new ArrayList<>(
				Arrays.asList(registry.getConfigurationElementsFor(CUIPlugin.getPluginId(), EXTENSION_POINT)));

		Map<String, List<CompletionProposalComputerDescriptor>> map = new HashMap<>();
		List<CompletionProposalComputerDescriptor> all = new ArrayList<>();

		List<CompletionProposalCategory> categories = getCategories(elements);
		for (IConfigurationElement element : elements) {
			try {
				CompletionProposalComputerDescriptor desc = new CompletionProposalComputerDescriptor(element, this,
						categories);
				Set<String> partitions = desc.getPartitions();
				for (Object element2 : partitions) {
					String partition = (String) element2;
					List<CompletionProposalComputerDescriptor> list = map.get(partition);
					if (list == null) {
						list = new ArrayList<>();
						map.put(partition, list);
					}
					list.add(desc);
				}
				all.add(desc);

			} catch (CoreException x) {
				/*
				 * Element is not valid any longer as the contributing plug-in was unloaded or for
				 * some other reason. Do not include the extension in the list and inform the user
				 * about it.
				 */
				Object[] args = { element.toString() };
				String message = Messages
						.format(ContentAssistMessages.CompletionProposalComputerRegistry_invalid_message, args);
				IStatus status = new Status(IStatus.WARNING, CUIPlugin.getPluginId(), IStatus.OK, message, x);
				informUser(status);
			}
		}

		synchronized (this) {
			fCategories.clear();
			fCategories.addAll(categories);

			Set<String> partitions = map.keySet();
			fDescriptorsByPartition.keySet().retainAll(partitions);
			fPublicDescriptorsByPartition.keySet().retainAll(partitions);
			for (String partition : partitions) {
				List<CompletionProposalComputerDescriptor> old = fDescriptorsByPartition.get(partition);
				List<CompletionProposalComputerDescriptor> current = map.get(partition);
				if (old != null) {
					old.clear();
					old.addAll(current);
				} else {
					fDescriptorsByPartition.put(partition, current);
					fPublicDescriptorsByPartition.put(partition, Collections.unmodifiableList(current));
				}
			}

			fDescriptors.clear();
			fDescriptors.addAll(all);
		}
	}

	private List<CompletionProposalCategory> getCategories(List<IConfigurationElement> elements) {
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		String preference = store.getString(PreferenceConstants.CODEASSIST_EXCLUDED_CATEGORIES);
		Set<String> disabled = new HashSet<>();
		StringTokenizer tok = new StringTokenizer(preference, "\0"); //$NON-NLS-1$
		while (tok.hasMoreTokens())
			disabled.add(tok.nextToken());
		Map<String, Integer> ordered = new HashMap<>();
		preference = store.getString(PreferenceConstants.CODEASSIST_CATEGORY_ORDER);
		tok = new StringTokenizer(preference, "\0"); //$NON-NLS-1$
		while (tok.hasMoreTokens()) {
			StringTokenizer inner = new StringTokenizer(tok.nextToken(), ":"); //$NON-NLS-1$
			String id = inner.nextToken();
			int rank = Integer.parseInt(inner.nextToken());
			ordered.put(id, Integer.valueOf(rank));
		}

		List<CompletionProposalCategory> categories = new ArrayList<>();
		for (Iterator<IConfigurationElement> iter = elements.iterator(); iter.hasNext();) {
			IConfigurationElement element = iter.next();
			try {
				if (element.getName().equals("proposalCategory")) { //$NON-NLS-1$
					iter.remove(); // remove from list to leave only computers

					CompletionProposalCategory category = new CompletionProposalCategory(element, this);
					categories.add(category);
					category.setIncluded(!disabled.contains(category.getId()));
					Integer rank = ordered.get(category.getId());
					if (rank != null) {
						int r = rank.intValue();
						boolean separate = r < 0xffff;
						category.setSeparateCommand(separate);
						category.setSortOrder(r);
					}
				}
			} catch (CoreException x) {
				/*
				 * Element is not valid any longer as the contributing plug-in was unloaded or for
				 * some other reason. Do not include the extension in the list and inform the user
				 * about it.
				 */
				Object[] args = { element.toString() };
				String message = Messages
						.format(ContentAssistMessages.CompletionProposalComputerRegistry_invalid_message, args);
				IStatus status = new Status(IStatus.WARNING, CUIPlugin.getPluginId(), IStatus.OK, message, x);
				informUser(status);
			}
		}
		return categories;
	}

	/**
	 * Log the status and inform the user about a misbehaving extension.
	 *
	 * @param descriptor the descriptor of the misbehaving extension
	 * @param status a status object that will be logged
	 */
	void informUser(CompletionProposalComputerDescriptor descriptor, IStatus status) {
		CUIPlugin.log(status);
		String title = ContentAssistMessages.CompletionProposalComputerRegistry_error_dialog_title;
		CompletionProposalCategory category = descriptor.getCategory();
		IContributor culprit = descriptor.getContributor();
		Set<String> affectedPlugins = getAffectedContributors(category, culprit);

		final String avoidHint;
		final String culpritName = culprit == null ? null : culprit.getName();
		if (affectedPlugins.isEmpty()) {
			if (CUIPlugin.PLUGIN_ID.equals(culpritName)) {
				// don't warn about internal computers
				return;
			}
			avoidHint = Messages.format(ContentAssistMessages.CompletionProposalComputerRegistry_messageAvoidanceHint,
					new Object[] { culpritName, category.getDisplayName() });
		} else {
			avoidHint = Messages.format(
					ContentAssistMessages.CompletionProposalComputerRegistry_messageAvoidanceHintWithWarning,
					new Object[] { culpritName, category.getDisplayName(), toString(affectedPlugins) });
		}
		String message = status.getMessage();
		// inlined from MessageDialog.openError
		MessageDialog dialog = new MessageDialog(CUIPlugin.getActiveWorkbenchShell(), title, null /* default image */,
				message, MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL }, 0) {
			@Override
			protected Control createCustomArea(Composite parent) {
				Link link = new Link(parent, SWT.NONE);
				link.setText(avoidHint);
				link.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						PreferencesUtil
								.createPreferenceDialogOn(getShell(),
										"org.eclipse.cdt.ui.preferences.CodeAssistPreferenceAdvanced", null, null) //$NON-NLS-1$
								.open();
					}
				});
				GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
				gridData.widthHint = this.getMinimumMessageWidth();
				link.setLayoutData(gridData);
				return link;
			}
		};
		dialog.open();
	}

	/**
	 * Returns the names of contributors affected by disabling a category.
	 *
	 * @param category the category that would be disabled
	 * @param culprit the culprit plug-in, which is not included in the returned list
	 * @return the names of the contributors other than <code>culprit</code> that contribute to <code>category</code> (element type: {@link String})
	 */
	private Set<String> getAffectedContributors(CompletionProposalCategory category, IContributor culprit) {
		Set<String> affectedPlugins = new HashSet<>();
		for (CompletionProposalComputerDescriptor desc : getProposalComputerDescriptors()) {
			CompletionProposalCategory cat = desc.getCategory();
			if (cat.equals(category)) {
				IContributor contributor = desc.getContributor();
				if (contributor != null && !culprit.equals(contributor))
					affectedPlugins.add(contributor.getName());
			}
		}
		return affectedPlugins;
	}

	private Object toString(Collection<String> collection) {
		// strip brackets off AbstractCollection.toString()
		String string = collection.toString();
		return string.substring(1, string.length() - 1);
	}

	private void informUser(IStatus status) {
		CUIPlugin.log(status);
		String title = ContentAssistMessages.CompletionProposalComputerRegistry_error_dialog_title;
		String message = status.getMessage();
		MessageDialog.openError(CUIPlugin.getActiveWorkbenchShell(), title, message);
	}
}

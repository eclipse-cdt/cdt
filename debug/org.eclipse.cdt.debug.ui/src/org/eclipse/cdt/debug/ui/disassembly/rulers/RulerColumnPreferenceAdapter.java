/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *     Wind River Systems, Inc. - adapted for for disassembly parts
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.disassembly.rulers;

import java.util.Set;

import org.eclipse.cdt.debug.internal.ui.preferences.StringSetSerializer;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Manages the preferences for ruler contributions stored in a preference store.
 *
 * @since 7.2
 */
public final class RulerColumnPreferenceAdapter {
	private final IPreferenceStore fStore;
	private final String fKey;

	/**
	 * Creates a new preference adapter that will read and write under the specified key in the
	 * given preference store.
	 *
	 * @param store the preference store
	 * @param key the key
	 */
	public RulerColumnPreferenceAdapter(IPreferenceStore store, String key) {
		Assert.isLegal(store != null);
		Assert.isLegal(key != null);
		fStore = store;
		fKey = key;
	}

	/**
	 * Returns the enablement state of the given ruler contribution.
	 *
	 * @param descriptor a ruler contribution descriptor
	 * @return <code>true</code> if the ruler is enabled, <code>false</code> otherwise
	 */
	public boolean isEnabled(RulerColumnDescriptor descriptor) {
		Assert.isLegal(descriptor != null);
		String preference = fStore.getString(fKey);
		return StringSetSerializer.deserialize(preference).contains(descriptor.getId())
				^ descriptor.getDefaultEnablement();
	}

	/**
	 * Sets the enablement state of the given ruler contribution.
	 *
	 * @param descriptor a ruler contribution descriptor
	 * @param enabled <code>true</code> to enable the contribution, <code>false</code> to
	 *        disable it
	 */
	public void setEnabled(RulerColumnDescriptor descriptor, boolean enabled) {
		Assert.isLegal(descriptor != null);
		String id = descriptor.getId();
		String preference = fStore.getString(fKey);
		Set<String> marked = StringSetSerializer.deserialize(preference);
		boolean shouldMark = enabled ^ descriptor.getDefaultEnablement();
		boolean isMarked = marked.contains(id);
		if (isMarked != shouldMark) {
			if (shouldMark)
				marked.add(id);
			else
				marked.remove(id);
			fStore.setValue(fKey, StringSetSerializer.serialize(marked));
		}
	}

	/**
	 * Toggles the enablement state of given the ruler contribution.
	 *
	 * @param descriptor a ruler contribution descriptor
	 */
	public void toggle(RulerColumnDescriptor descriptor) {
		Assert.isLegal(descriptor != null);
		setEnabled(descriptor, !isEnabled(descriptor));
	}
}

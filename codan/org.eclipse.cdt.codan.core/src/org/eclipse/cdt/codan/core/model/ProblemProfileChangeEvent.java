/*******************************************************************************
 * Copyright (c) 2009, 2016 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import java.util.EventObject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;

/**
 * An event object describing the details of a change to a preference
 * in the preference store.
 *
 * @since 2.0
 * @deprecated use {@link IEclipsePreferences} change listener event instead.
 */
@Deprecated
public final class ProblemProfileChangeEvent extends EventObject {
	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	private String key;
	private Object newValue;
	private Object oldValue;
	private Object resource;
	private IProblemProfile profile;
	public static final String PROBLEM_KEY = "problem"; //$NON-NLS-1$
	public static final String PROBLEM_PREF_KEY = "problem_params"; //$NON-NLS-1$

	/**
	 * Constructor for a new profile change event. The node and the
	 * key must not be <code>null</code>.
	 *
	 * @param profile the profile on which the change occurred
	 * @param resource the resource for which profile changes occurred
	 * @param key the preference key
	 * @param oldValue the old preference value
	 * @param newValue the new preference value
	 */
	public ProblemProfileChangeEvent(IProblemProfile profile, Object resource, String key, Object oldValue,
			Object newValue) {
		super(resource);
		this.key = key;
		this.newValue = newValue;
		this.oldValue = oldValue;
		this.profile = profile;
		this.resource = resource;
	}

	/**
	 * Return the resource on which the change occurred.
	 * Must not be <code>null</code>.
	 *
	 * @return the node
	 */
	public Object getResource() {
		return resource;
	}

	/**
	 * @return profile
	 */
	public IProblemProfile getProfile() {
		return profile;
	}

	/**
	 * Return the key of the preference which was changed.
	 * Must not be <code>null</code>.
	 *
	 * @return the preference key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Return the new value for the preference encoded as a
	 * <code>String</code>, or <code>null</code> if the
	 * preference was removed.
	 *
	 * @return the new value or <code>null</code>
	 */
	public Object getNewValue() {
		return newValue;
	}

	/**
	 * Return the old value for the preference encoded as a
	 * <code>String</code>, or <code>null</code> if the
	 * preference was removed or if it cannot be determined.
	 *
	 * @return the old value or <code>null</code>
	 */
	public Object getOldValue() {
		return oldValue;
	}
}
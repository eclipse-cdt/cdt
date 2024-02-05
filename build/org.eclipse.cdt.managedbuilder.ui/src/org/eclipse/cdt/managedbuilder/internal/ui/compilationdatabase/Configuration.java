/*******************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.compilationdatabase;

import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.IPreferenceMetadataStore;

public interface Configuration {

	/**
	 * Returns the defaults
	 *
	 * @return  defaults
	 */
	Object defaults();

	/**
	 * Returns the options for the given context like {@link IResource} or {@link URI}, must not return <code>null</code>
	 * @param context to be adapter to the proper scope
	 *
	 * @return options
	 */
	Object options(Object context);

	/**
	 * Returns the editor preference store for the given context like {@link IResource} or {@link URI}, must not return <code>null</code>
	 * @param context to be adapter to the proper scope
	 *
	 * @return preference store
	 */
	IPreferenceMetadataStore storage(Object context);

	/**
	 * Return the metadata for options, must not return <code>null</code>
	 *
	 * @return the option metadata
	 */
	Object metadata();

	/**
	 * Default qualifier to use for preference storage
	 * @return preference qualifier
	 */
	String qualifier();
}

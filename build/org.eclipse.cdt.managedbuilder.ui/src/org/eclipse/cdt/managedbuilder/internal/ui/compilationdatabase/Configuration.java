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

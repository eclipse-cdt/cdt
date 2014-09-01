package org.eclipse.cdt.launchbar.core.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * A wrapper class that delays instantiation of classes until they're needed
 * to prevent early plug-in loading.
 *
 * @param <T> the type of the object created
 */
public class ExecutableExtension<T> {

	private IConfigurationElement element;
	private String propertyName;
	private T object;

	public ExecutableExtension(IConfigurationElement element, String propertyName) {
		this.element = element;
		this.propertyName = propertyName;
	}

	// For testing, pre-populate the object
	public ExecutableExtension(T object) {
		this.object = object;
	}

	/**
	 * Get the object instantiating it if necessary.
	 * @return object
	 * @throws CoreException
	 */
	@SuppressWarnings("unchecked")
	public T get() throws CoreException {
		if (element != null) {
			object = (T) element.createExecutableExtension(propertyName);
			element = null;
			propertyName = null;
		}
		return object;
	}

}

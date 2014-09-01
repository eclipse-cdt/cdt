package org.eclipse.cdt.launchbar.core.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

public class ExecutableExtension<T> {

	private IConfigurationElement element;
	private String propertyName;
	private T object;
	
	public ExecutableExtension(IConfigurationElement element, String propertyName) {
		this.element = element;
		this.propertyName = propertyName;
	}

	// For testing, prepopulate the object
	public ExecutableExtension(T object) {
		this.object = object;
	}

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

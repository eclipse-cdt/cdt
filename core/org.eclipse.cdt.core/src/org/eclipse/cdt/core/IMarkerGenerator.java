package org.eclipse.cdt.core;

import org.eclipse.core.resources.IResource;

/**
 * @author sam.robb
 */
public interface IMarkerGenerator {
	int SEVERITY_INFO = 0;
	int SEVERITY_WARNING = 1;
	int SEVERITY_ERROR_RESOURCE = 2;
	int SEVERITY_ERROR_BUILD = 3;

	void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar);
}

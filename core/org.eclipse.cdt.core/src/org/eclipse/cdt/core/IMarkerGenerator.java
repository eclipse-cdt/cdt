package org.eclipse.cdt.core;

import org.eclipse.core.resources.IResource;

/**
 * @author sam.robb
 */
public interface IMarkerGenerator {
	public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar);
}

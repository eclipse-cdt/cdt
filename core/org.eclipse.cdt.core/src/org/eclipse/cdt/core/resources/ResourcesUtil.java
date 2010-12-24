package org.eclipse.cdt.core.resources;

import java.net.URI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class ResourcesUtil {
	/**
	 * Refresh file when it happens to belong to Workspace. There could
	 * be multiple workspace {@link IFile} associated with one URI.
	 * Hint: use {@link org.eclipse.core.filesystem.URIUtil#toURI(String)}
	 * to convert filesystem path to URI.
	 * 
	 * @param uri - URI of the file.
	 */
	public static void refreshWorkspaceFiles(URI uri) {
		if (uri!=null) {
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
			for (IFile file : files) {
				try {
					file.refreshLocal(IResource.DEPTH_ZERO, null);
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
		}
	}

}

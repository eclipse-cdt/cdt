package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class ConfigBasedPathEntryContainer implements IPathEntryContainer {
	public static final IPath CONTAINER_PATH = new Path("org.eclipse.cdt.core.CFG_BASED_CONTAINER");	//$NON-NLS-1$
	private IPathEntry[] fEntries;

	public ConfigBasedPathEntryContainer(IPathEntry entries[]){
		this.fEntries = (IPathEntry[])entries.clone();
	}
	
	public String getDescription() {
		return "Configuration Description info container";	//$NON-NLS-1$
	}

	public IPath getPath() {
		return CONTAINER_PATH;
	}

	public IPathEntry[] getPathEntries() {
		return (IPathEntry[])fEntries.clone();
	}

}

/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.index;
import org.eclipse.cdt.internal.core.index.IndexFileLocation;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * A location converter for converting project resource locations to be relative to a specified
 * container. Resources outside of the associated project will not be converted (ignored).
 * <br>
 * This location converter is internal-representation-compatible with URIRelativeLocationConverter
 */
public class ResourceContainerRelativeLocationConverter implements IIndexLocationConverter {
	protected IWorkspaceRoot root;
	protected IPath fullPath;
	
	/**
	 * @param container the resource container to convert relative to
	 */
	public ResourceContainerRelativeLocationConverter(IContainer container) {
		this.fullPath = container.getFullPath();
		this.root = ResourcesPlugin.getWorkspace().getRoot();
	}

	@Override
	public IIndexFileLocation fromInternalFormat(String raw) {
		IResource member= root.getFile(fullPath.append(raw)); 
		return new IndexFileLocation(member.getLocationURI(), member.getFullPath().toString());
	}
	@Override
	public String toInternalFormat(IIndexFileLocation location) {
		String sFullPath= location.getFullPath();
		if(sFullPath!=null) {
			IPath path= new Path(sFullPath);
			if(fullPath.isPrefixOf(path)) {
				return path.removeFirstSegments(fullPath.segmentCount()).toString();
			}
		}
		return null;
	}
}

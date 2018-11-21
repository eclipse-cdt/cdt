/*******************************************************************************
 * Copyright (c) 2004, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup;

import java.io.File;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.sourcelookup.CProjectSourceContainer;
import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.cdt.debug.internal.core.sourcelookup.MapEntrySourceContainer;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Workbench adapter for CDT source containers.
 */
public class SourceContainerWorkbenchAdapter implements IWorkbenchAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object o) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	@Override
	public ImageDescriptor getImageDescriptor(Object o) {
		if (o instanceof MappingSourceContainer) {
			return CDebugImages.DESC_OBJS_PATH_MAPPING;
		}
		if (o instanceof MapEntrySourceContainer) {
			return CDebugImages.DESC_OBJS_PATH_MAP_ENTRY;
		}
		if (o instanceof CProjectSourceContainer) {
			IProject project = ((CProjectSourceContainer) o).getProject();
			if (project != null) {
				ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
				if (cProject != null)
					return getImageDescriptor(cProject);
			}
		} else if (o instanceof ProjectSourceContainer) {
			IProject project = ((ProjectSourceContainer) o).getProject();
			ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
			if (cProject != null)
				return getImageDescriptor(cProject);
		}
		return null;
	}

	protected ImageDescriptor getImageDescriptor(ICElement element) {
		IWorkbenchAdapter adapter = element.getAdapter(IWorkbenchAdapter.class);
		if (adapter != null) {
			return adapter.getImageDescriptor(element);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object o) {
		if (o instanceof MappingSourceContainer) {
			return SourceLookupUIMessages.SourceContainerWorkbenchAdapter_0 + ((MappingSourceContainer) o).getName();
		}
		if (o instanceof MapEntrySourceContainer) {
			return ((MapEntrySourceContainer) o).getName();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object o) {
		return null;
	}

	public String getQualifiedName(IPath path) {
		StringBuilder buffer = new StringBuilder();
		String[] segments = path.segments();
		if (segments.length > 0) {
			buffer.append(path.lastSegment());
			if (segments.length > 1) {
				buffer.append(" - "); //$NON-NLS-1$
				if (path.getDevice() != null) {
					buffer.append(path.getDevice());
				}
				for (int i = 0; i < segments.length - 1; i++) {
					buffer.append(File.separatorChar);
					buffer.append(segments[i]);
				}
			}
			return buffer.toString();
		}
		return ""; //$NON-NLS-1$
	}
}

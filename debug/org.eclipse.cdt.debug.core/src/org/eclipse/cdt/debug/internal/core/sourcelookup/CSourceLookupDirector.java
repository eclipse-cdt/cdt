/*******************************************************************************
 * Copyright (c) 2004, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Nokia - Added support for AbsoluteSourceContainer(159833)
 *     Texas Instruments - added extension point for source container type (279473) 
 *     Sergey Prigogin (Google)
*******************************************************************************/
package org.eclipse.cdt.debug.internal.core.sourcelookup; 

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.sourcelookup.AbsolutePathSourceContainer;
import org.eclipse.cdt.debug.core.sourcelookup.CProjectSourceContainer;
import org.eclipse.cdt.debug.core.sourcelookup.IMappingSourceContainer;
import org.eclipse.cdt.debug.core.sourcelookup.ProgramRelativePathSourceContainer;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;

/**
 * C/C++ source lookup director.
 * 
 * Most instantiations of this class are transient, created through
 * {@link ILaunchManager#newSourceLocator(String)}. A singleton is also created
 * to represent the global source locators.
 * 
 * An instance is either associated with a particular launch configuration or it
 * has no association (global).
 * 
 * This class is created by the {@link ILaunchManager#newSourceLocator(String)}
 * (e.g. DebugPlugin.getDefault().getLaunchManager().newSourceLocator(type)) and
 * must have a no-arguments constructor.
 */
public class CSourceLookupDirector extends AbstractSourceLookupDirector {
	private static Set<String> fSupportedTypes;
	private static Object fSupportedTypesLock = new Object();

	@Override
	public void initializeParticipants() {
		addParticipants(new ISourceLookupParticipant[] { new CSourceLookupParticipant() });
	}

	@Override
	public boolean supportsSourceContainerType(ISourceContainerType type) {
		readSupportedContainerTypes();
		return fSupportedTypes.contains(type.getId());
	}

	/**
	 * @deprecated Only used by CDI, scheduled for removal as part of Bug 484900
	 */
	@Deprecated
	public boolean contains(String source) {
		for (ISourceContainer cont : getSourceContainers()) {
			if (contains(cont, source))
				return true;
		}
		return false;
	}

	/**
	 * @deprecated Only used by CDI, scheduled for removal as part of Bug 484900
	 */
	@Deprecated
	public boolean contains(ICBreakpoint breakpoint) {
		try {
			String handle = breakpoint.getSourceHandle();
			for (ISourceContainer cont : getSourceContainers()) {
				if (contains(cont, handle))
					return true;
			}
		} catch (CoreException e) {
		}
		return false;
	}

	/**
	 * @deprecated Only used by CDI, scheduled for removal as part of Bug 484900
	 */
	@Deprecated
	public boolean contains(IProject project) {
		for (ISourceContainer cont : getSourceContainers()) {
			if (contains(cont, project))
				return true;
		}
		return false;
	}

	/**
	 * @deprecated Only used by CDI, scheduled for removal as part of Bug 484900
	 */
	@Deprecated
	private boolean contains(ISourceContainer container, IProject project) {
		if (container instanceof CProjectSourceContainer && project.equals(((CProjectSourceContainer) container).getProject())) {
			return true;
		}
		if (container instanceof ProjectSourceContainer && project.equals(((ProjectSourceContainer) container).getProject())) {
			return true;
		}
		try {
			for (ISourceContainer cont : container.getSourceContainers()) {
				if (contains(cont, project))
					return true;
			}
		} catch (CoreException e) {
		}
		return false;
	}

	/**
	 * @deprecated Only used by CDI, scheduled for removal as part of Bug 484900
	 */
	@Deprecated
	private boolean contains(ISourceContainer container, String sourceName) {
		IPath path = new Path(sourceName);
		if (!path.isValidPath(sourceName))
			return false;
		if (container instanceof IMappingSourceContainer) {
			return ((IMappingSourceContainer) container).getCompilationPath(sourceName) != null; 
		}
		if (container instanceof CProjectSourceContainer) {
			IProject project = ((CProjectSourceContainer) container).getProject();
			if (project != null) {
				IPath projPath = project.getLocation();
				if (projPath != null && projPath.isPrefixOf(path)) {
					IFile file = ((CProjectSourceContainer) container).getProject().getFile(path.removeFirstSegments(projPath.segmentCount()));
					return file != null && file.exists();
				}
			}
		} else if (container instanceof ProjectSourceContainer) {
			IProject project = ((ProjectSourceContainer) container).getProject();
			IPath projPath = project.getLocation();
			if (projPath != null && projPath.isPrefixOf(path)) {
				IFile file = ((ProjectSourceContainer) container).getProject().getFile(path.removeFirstSegments(projPath.segmentCount()));
				return file != null && file.exists();
			}
		} else if (container instanceof FolderSourceContainer) {
			IContainer folder = ((FolderSourceContainer) container).getContainer();
			IPath folderPath = folder.getLocation();
			if (folderPath != null && folderPath.isPrefixOf(path)) {
				IFile file = ((FolderSourceContainer) container).getContainer().getFile(path.removeFirstSegments(folderPath.segmentCount()));
				return file != null && file.exists();
			}
		} else if (container instanceof DirectorySourceContainer) {
			File dir = ((DirectorySourceContainer) container).getDirectory();
			boolean searchSubfolders = ((DirectorySourceContainer) container).isComposite();
			IPath dirPath = new Path(dir.getAbsolutePath());
			if (searchSubfolders || dirPath.segmentCount() + 1 == path.segmentCount())
				return dirPath.isPrefixOf(path);
		} else if (container instanceof AbsolutePathSourceContainer) {
			return ((AbsolutePathSourceContainer) container).isValidAbsoluteFilePath(sourceName); 
		} else if (container instanceof ProgramRelativePathSourceContainer) {
			try {
				Object[] elements = ((ProgramRelativePathSourceContainer) container).findSourceElements(sourceName);
				return elements.length > 0;	
			} catch (CoreException e) {
				return false;
			}
		}
		try {
			for (ISourceContainer cont : container.getSourceContainers()) {
				if (contains(cont, sourceName))
					return true;
			}
		} catch (CoreException e) {
		}
		return false;
	}

	/**
	 * Translate a local file name to a name understood by the backend.
	 *
	 * This method is used when CDT needs to send a command to the backend
	 * containing a file name. For example, inserting a breakpoint. The platform
	 * breakpoint's file name is a path of a file on the user's machine, but GDB
	 * needs the path that corresponds to the debug information.
	 *
	 * @param sourceName
	 *            file name of a local file
	 * @return file name as understood by the debugger backend
	 */
	public IPath getCompilationPath(String sourceName) {
		for (ISourceContainer container : getSourceContainers()) {
			IPath path = SourceUtils.getCompilationPath(container, sourceName);
			if (path != null) {
				return path;
			}
		}
		return null;
	}

	/**
	 * Load and cache the source container types which are supported for CDT
	 * debugging.
	 *
	 * See Bug 279473 for more information.
	 */
	private void readSupportedContainerTypes() {
		synchronized (fSupportedTypesLock) {
			if (fSupportedTypes == null) {
				fSupportedTypes = new HashSet<String>();
				String name = CDebugCorePlugin.PLUGIN_ID + ".supportedSourceContainerTypes"; //$NON-NLS-1$; 
				IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(name);
				if (extensionPoint != null) {  
					for (IExtension extension : extensionPoint.getExtensions()) { 
						for (IConfigurationElement configurationElements : extension.getConfigurationElements()) {
							String id = configurationElements.getAttribute("id"); //$NON-NLS-1$;
							if (id != null)
								fSupportedTypes.add(id);
						}
					}
				}
			}
		}
	}
}

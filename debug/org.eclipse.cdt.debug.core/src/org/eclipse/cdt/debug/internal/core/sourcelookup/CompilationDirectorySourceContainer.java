/*******************************************************************************
 * Copyright (c) 2010, 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	  Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.sourcelookup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.sourcelookup.IMappingSourceContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.CompositeSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;

/**
 * A directory in the local file system that is used for running the C/C++ compiler. This container
 * is intended to be used when debugging information produced by the C/C++ compiler contains paths
 * to the source and header files relative to the directory where the compiler is run from.
 * The assumption is that all files under a compilation directory are compiled relative to
 * that directory, unless they belong to another compilation directory container that is higher on
 * the source container list.
 *
 * Source elements returned from <code>findSourceElements(...)</code> are instances of
 * <code>IFile</code> or <code>LocalFileStorage</code>.
 * <p>
 * Clients may instantiate this class.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CompilationDirectorySourceContainer extends CompositeSourceContainer implements IMappingSourceContainer {
	/**
	 * Unique identifier for the compilation directory source container type
	 * (value <code>org.eclipse.debug.core.containerType.compilationDirectory</code>).
	 */
	public static final String TYPE_ID = CDebugCorePlugin.getUniqueIdentifier() + ".containerType.compilationDirectory"; //$NON-NLS-1$

	// Root directory.
	private File fDirectory;
	// Whether to each subdirectory of the compilation directory is also the compilation directory
	// for the files it contains.
	private boolean fSubfolders;

	/**
	 * Constructs an external folder container for the
	 * directory identified by the given path.
	 *
	 * @param dirPath path to a directory in the local file system
	 * @param subfolders whether folders within the root directory
	 *  should be searched for source elements
	 */
	public CompilationDirectorySourceContainer(IPath dirPath, boolean subfolders) {
		this(dirPath.toFile(), subfolders);
	}

	/**
	 * Constructs an external folder container for the
	 * directory identified by the given file.
	 *
	 * @param dir a directory in the local file system
	 * @param subfolders whether folders within the root directory
	 * 		should be searched for source elements
	 */
	public CompilationDirectorySourceContainer(File dir, boolean subfolders) {
		fDirectory = dir;
		fSubfolders = subfolders;
	}

	@Override
	public String getName() {
		return fDirectory.getAbsolutePath();
	}

	/**
	 * Returns the root directory in the local file system associated
	 * with this source container.
	 *
	 * @return the root directory in the local file system associated
	 * with this source container
	 */
	public File getDirectory() {
		return fDirectory;
	}

	@Override
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}

	/**
	 * Source elements returned from this method are instances of {@link IFile} or {@link LocalFileStorage}.
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#findSourceElements(String)
	 */
	@Override
	public Object[] findSourceElements(String name) throws CoreException {
		File file = new File(name);
		if (!file.isAbsolute()) {
			file = new File(fDirectory, name);
		}
		List<Object> sources = new ArrayList<>();
		if (file.exists() && file.isFile()) {
			Collections.addAll(sources, SourceUtils.findSourceElements(file, getDirector()));
		}

		// Check sub-folders
		if (fSubfolders && (isFindDuplicates() || sources.isEmpty())) {
			for (ISourceContainer container : getSourceContainers()) {
				Object[] elements = container.findSourceElements(name);
				if (elements == null || elements.length == 0) {
					continue;
				}
				if (isFindDuplicates()) {
					for (Object element : elements) {
						sources.add(element);
					}
				} else {
					sources.add(elements[0]);
					break;
				}
			}
		}

		if (sources.isEmpty())
			return EMPTY;
		return sources.toArray();
	}

	@Override
	public boolean isComposite() {
		return fSubfolders;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CompilationDirectorySourceContainer) {
			CompilationDirectorySourceContainer container = (CompilationDirectorySourceContainer) obj;
			return container.getDirectory().equals(getDirectory());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getDirectory().hashCode();
	}

	@Override
	protected ISourceContainer[] createSourceContainers() throws CoreException {
		if (fSubfolders) {
			String[] files = fDirectory.list();
			if (files != null) {
				List<ISourceContainer> dirs = new ArrayList<>();
				for (String name : files) {
					File file = new File(getDirectory(), name);
					if (file.exists() && file.isDirectory()) {
						dirs.add(new CompilationDirectorySourceContainer(file, true));
					}
				}
				ISourceContainer[] containers = dirs.toArray(new ISourceContainer[dirs.size()]);
				for (ISourceContainer container : containers) {
					container.init(getDirector());
				}
				return containers;
			}
		}
		return new ISourceContainer[0];
	}

	@Override
	public IPath getCompilationPath(String sourceName) {
		IPath path = new Path(sourceName);
		IPath base = new Path(fDirectory.getPath());
		if (base.isPrefixOf(path)) {
			if (fSubfolders) {
				base = path.removeLastSegments(1);
			}
			return path.makeRelativeTo(base);
		}
		return null;
	}
}

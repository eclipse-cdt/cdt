/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.sourcelookup;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.sourcelookup.CProjectSourceContainer;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.IDirectorySourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.IMappingSourceContainer;
import org.eclipse.cdt.debug.core.sourcelookup.IProjectSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ibm.icu.text.MessageFormat;

public class SourceUtils {
	private static final String NAME_COMMON_SOURCE_LOCATIONS = "commonSourceLocations"; //$NON-NLS-1$
	private static final String NAME_SOURCE_LOCATION = "sourceLocation"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_MEMENTO = "memento"; //$NON-NLS-1$

	public static String getCommonSourceLocationsMemento(ICSourceLocation[] locations) {
		Document document = null;
		Throwable ex = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element element = document.createElement(NAME_COMMON_SOURCE_LOCATIONS);
			document.appendChild(element);
			saveSourceLocations(document, element, locations);
			return CDebugUtils.serializeDocument(document);
		} catch (ParserConfigurationException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		} catch (TransformerException e) {
			ex = e;
		}
		CDebugCorePlugin.log(new Status(IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), 0,
				"Error saving common source settings.", ex)); //$NON-NLS-1$
		return null;
	}

	private static void saveSourceLocations(Document doc, Element node, ICSourceLocation[] locations) {
		for (int i = 0; i < locations.length; i++) {
			Element child = doc.createElement(NAME_SOURCE_LOCATION);
			child.setAttribute(ATTR_CLASS, locations[i].getClass().getName());
			try {
				child.setAttribute(ATTR_MEMENTO, locations[i].getMemento());
			} catch (CoreException e) {
				CDebugCorePlugin.log(e);
				continue;
			}
			node.appendChild(child);
		}
	}

	public static ICSourceLocation[] getCommonSourceLocationsFromMemento(String memento) {
		ICSourceLocation[] result = new ICSourceLocation[0];
		if (!isEmpty(memento)) {
			try {
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				StringReader reader = new StringReader(memento);
				InputSource source = new InputSource(reader);
				Element root = parser.parse(source).getDocumentElement();
				if (root.getNodeName().equalsIgnoreCase(NAME_COMMON_SOURCE_LOCATIONS))
					result = initializeSourceLocations(root);
			} catch (ParserConfigurationException e) {
				CDebugCorePlugin.log(new Status(IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), 0,
						"Error initializing common source settings.", e)); //$NON-NLS-1$
			} catch (SAXException e) {
				CDebugCorePlugin.log(new Status(IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), 0,
						"Error initializing common source settings.", e)); //$NON-NLS-1$
			} catch (IOException e) {
				CDebugCorePlugin.log(new Status(IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), 0,
						"Error initializing common source settings.", e)); //$NON-NLS-1$
			}
		}
		return result;
	}

	public static ICSourceLocation[] initializeSourceLocations(Element root) {
		List<ICSourceLocation> sourceLocations = new LinkedList<>();
		NodeList list = root.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element entry = (Element) node;
				if (entry.getNodeName().equalsIgnoreCase(NAME_SOURCE_LOCATION)) {
					String className = entry.getAttribute(ATTR_CLASS);
					String data = entry.getAttribute(ATTR_MEMENTO);
					if (className == null || className.trim().length() == 0) {
						CDebugCorePlugin.log("Unable to restore common source locations - invalid format."); //$NON-NLS-1$
						continue;
					}
					Class<?> clazz = null;
					try {
						clazz = CDebugCorePlugin.getDefault().getBundle().loadClass(className);
					} catch (ClassNotFoundException e) {
						CDebugCorePlugin
								.log(MessageFormat.format("Unable to restore source location - class not found {0}", //$NON-NLS-1$
										(Object[]) new String[] { className }));
						continue;
					}
					ICSourceLocation location = null;
					try {
						location = (ICSourceLocation) clazz.newInstance();
					} catch (IllegalAccessException e) {
						CDebugCorePlugin.log("Unable to restore source location: " + e.getMessage()); //$NON-NLS-1$
						continue;
					} catch (InstantiationException e) {
						CDebugCorePlugin.log("Unable to restore source location: " + e.getMessage()); //$NON-NLS-1$
						continue;
					}
					try {
						location.initializeFrom(data);
						sourceLocations.add(location);
					} catch (CoreException e) {
						CDebugCorePlugin.log("Unable to restore source location: " + e.getMessage()); //$NON-NLS-1$
					}
				}
			}
		}
		return sourceLocations.toArray(new ICSourceLocation[sourceLocations.size()]);
	}

	private static boolean isEmpty(String string) {
		return string == null || string.trim().length() == 0;
	}

	static public ISourceContainer[] convertSourceLocations(ICSourceLocation[] locations) {
		ArrayList<ISourceContainer> containers = new ArrayList<>(locations.length);
		int mappingCount = 0;
		for (ICSourceLocation location : locations) {
			if (location instanceof IProjectSourceLocation) {
				containers.add(new CProjectSourceContainer(((IProjectSourceLocation) location).getProject(), false));
			} else if (location instanceof IDirectorySourceLocation) {
				IDirectorySourceLocation d = (IDirectorySourceLocation) location;
				IPath a = d.getAssociation();
				if (a != null) {
					MappingSourceContainer mapping = new MappingSourceContainer(
							InternalSourceLookupMessages.SourceUtils_0 + (++mappingCount));
					mapping.addMapEntries(new MapEntrySourceContainer[] {
							new MapEntrySourceContainer(a.toOSString(), d.getDirectory()) });
					containers.add(mapping);

				}
				containers.add(new DirectorySourceContainer(d.getDirectory(), d.searchSubfolders()));
			}
		}
		return containers.toArray(new ISourceContainer[containers.size()]);
	}

	static IPath getCompilationPath(ISourceContainer container, String sourceName) {
		if (container instanceof IMappingSourceContainer) {
			return ((IMappingSourceContainer) container).getCompilationPath(sourceName);
		}

		try {
			for (ISourceContainer cont : container.getSourceContainers()) {
				IPath path = getCompilationPath(cont, sourceName);
				if (path != null)
					return path;
			}
		} catch (CoreException e) {
		}
		return null;
	}

	public static IProject[] getAllReferencedProjects(IProject project) throws CoreException {
		Set<IProject> all = new HashSet<>();
		getAllReferencedProjects(all, project);
		return all.toArray(new IProject[all.size()]);
	}

	private static void getAllReferencedProjects(Set<IProject> all, IProject project) throws CoreException {
		for (IProject ref : project.getReferencedProjects()) {
			if (!all.contains(ref) && ref.exists() && ref.isOpen()) {
				all.add(ref);
				getAllReferencedProjects(all, ref);
			}
		}
	}

	/**
	 * Returns the project from the launch configuration, or {@code null} if it's not available.
	 */
	public static IProject getLaunchConfigurationProject(ISourceLookupDirector director) {
		String name = getLaunchConfigurationProjectName(director);
		return name != null ? ResourcesPlugin.getWorkspace().getRoot().getProject(name) : null;
	}

	/**
	 * Returns the project name from the launch configuration, or {@code null} if it's not available.
	 */
	public static String getLaunchConfigurationProjectName(ISourceLookupDirector director) {
		ILaunchConfiguration config = director.getLaunchConfiguration();
		if (config != null) {
			try {
				String name = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
				if (name.length() > 0)
					return name;
			} catch (CoreException e) {
				CDebugCorePlugin.log(e);
			}
		}
		return null;
	}

	/**
	 * Returns source elements corresponding to a file.
	 * @param file A source or header file.
	 * @param director A source lookup director.
	 * @return An array of source elements sorted in relevance order. The elements of the array can
	 * 		be either instances of IFile, ITranslationUnit or LocalFileStorage. The returned array can be empty if
	 * 		no source elements match the given file.
	 */
	public static Object[] findSourceElements(File file, ISourceLookupDirector director) {
		IFile[] wfiles = ResourceLookup.findFilesForLocation(new Path(file.getAbsolutePath()));
		IProject lcProject = null;
		if (director != null) {
			lcProject = getLaunchConfigurationProject(director);
		}

		if (wfiles.length > 0) {
			ResourceLookup.sortFilesByRelevance(wfiles, lcProject);
			return updateUnavailableResources(wfiles, lcProject);
		}

		try {
			// Check the canonical path as well to support case insensitive file
			// systems like Windows.
			wfiles = ResourceLookup.findFilesForLocation(new Path(file.getCanonicalPath()));
			if (wfiles.length > 0) {
				ResourceLookup.sortFilesByRelevance(wfiles, lcProject);
				return updateUnavailableResources(wfiles, lcProject);
			}

			// The file is not already in the workspace so try to create an external translation unit for it.
			if (lcProject != null) {
				ICProject project = CoreModel.getDefault().create(lcProject);
				if (project != null) {
					ITranslationUnit translationUnit = CoreModel.getDefault().createTranslationUnitFrom(project,
							URIUtil.toURI(file.getCanonicalPath(), true));
					if (translationUnit != null) // if we failed do not return array with null in it
						return new ITranslationUnit[] { translationUnit };
				}
			}
		} catch (IOException e) { // ignore if getCanonicalPath throws
		}

		// If we can't create an ETU then fall back on LocalFileStorage.
		return new LocalFileStorage[] { new LocalFileStorage(file) };
	}

	/**
	 * Check for IFile to be available in workspace ( see {@link IFile#isAccessible()} ).
	 * Unavailable resources are replaced with  {@link ITranslationUnit}
	 * @param wfiles
	 * @param project
	 * @return
	 */
	private static Object[] updateUnavailableResources(IFile[] wfiles, IProject project) {
		// with no projects context we will not be able to create ITranslationUnits
		if (project == null) {
			return wfiles;
		}

		ICProject cProject = CoreModel.getDefault().create(project);
		Object[] result = new Object[wfiles.length];
		for (int i = 0; i < wfiles.length; ++i) {
			IFile wkspFile = wfiles[i];
			if (wkspFile.isAccessible()) {
				result[i] = wkspFile;
			} else {
				result[i] = CoreModel.getDefault().createTranslationUnitFrom(cProject, wkspFile.getLocationURI());
			}
		}
		return result;
	}
}

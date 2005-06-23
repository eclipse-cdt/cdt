/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IIncludeFileEntry;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IMacroFileEntry;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.core.model.IProjectEntry;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.core.resources.IPathEntryVariableManager;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class PathEntryUtil {

	static PathEntryManager manager = PathEntryManager.getDefault();
	static final IMarker[] NO_MARKERS = new IMarker[0];

	private PathEntryUtil() {
		super();
	}

	public static IPathEntry getExpandedPathEntry(IPathEntry entry, ICProject cproject) throws CModelException {
		switch (entry.getEntryKind()) {
			case IPathEntry.CDT_INCLUDE : {
				IIncludeEntry includeEntry = (IIncludeEntry)entry;
				IPath refPath = includeEntry.getBaseReference();
				if (refPath != null && !refPath.isEmpty()) {
					IPath includePath = includeEntry.getIncludePath();
					if (refPath.isAbsolute()) {
						IResource res = cproject.getCModel().getWorkspace().getRoot().findMember(refPath);
						if (res != null && res.getType() == IResource.PROJECT) {
							IProject project = (IProject)res;
							if (CoreModel.hasCNature(project) || CoreModel.hasCCNature(project)) {
								ICProject refCProject = CoreModel.getDefault().create(project);
								if (refCProject != null) {
									IPathEntry[] entries = manager.getResolvedPathEntries(refCProject);
									for (int i = 0; i < entries.length; i++) {
										if (entries[i].getEntryKind() == IPathEntry.CDT_INCLUDE) {
											IIncludeEntry refEntry = (IIncludeEntry)entries[i];
											if (refEntry.getIncludePath().equals(includePath)) {
												IPath newBasePath = refEntry.getBasePath();
												// If the includePath is
												// relative give a new basepath
												// if none
												if (!newBasePath.isAbsolute() && !includePath.isAbsolute()) {
													IResource refRes;
													if (!newBasePath.isEmpty()) {
														refRes = cproject.getCModel().getWorkspace().getRoot().findMember(
																newBasePath);
													} else {
														IPath refResPath = refEntry.getPath();
														refRes = cproject.getCModel().getWorkspace().getRoot().findMember(
																refResPath);
													}
													if (refRes != null) {
														if (refRes.getType() == IResource.FILE) {
															refRes = refRes.getParent();
														}
														newBasePath = refRes.getLocation().append(newBasePath);
													}
												}
												return CoreModel.newIncludeEntry(includeEntry.getPath(), newBasePath, includePath);
											}
										}
									}
								}
							}
						}
					} else { // Container ref
						IPathEntryContainer container = manager.getPathEntryContainer(refPath, cproject);
						if (container != null) {
							IPathEntry[] entries = container.getPathEntries();
							for (int i = 0; i < entries.length; i++) {
								if (entries[i].getEntryKind() == IPathEntry.CDT_INCLUDE) {
									IIncludeEntry refEntry = (IIncludeEntry)entries[i];
									if (refEntry.getIncludePath().equals(includePath)) {
										IPath newBasePath = refEntry.getBasePath();
										return CoreModel.newIncludeEntry(includeEntry.getPath(), newBasePath, includePath);
									}
								}
							}
						}
					}
				}
				break;
			}

			case IPathEntry.CDT_MACRO : {
				IMacroEntry macroEntry = (IMacroEntry)entry;
				IPath refPath = macroEntry.getBaseReference();
				if (refPath != null && !refPath.isEmpty()) {
					String name = macroEntry.getMacroName();
					if (refPath.isAbsolute()) {
						IResource res = cproject.getCModel().getWorkspace().getRoot().findMember(refPath);
						if (res != null && res.getType() == IResource.PROJECT) {
							IProject project = (IProject)res;
							if (CoreModel.hasCNature(project) || CoreModel.hasCCNature(project)) {
								ICProject refCProject = CoreModel.getDefault().create(project);
								if (refCProject != null) {
									IPathEntry[] entries = manager.getResolvedPathEntries(refCProject);
									for (int i = 0; i < entries.length; i++) {
										if (entries[i].getEntryKind() == IPathEntry.CDT_MACRO) {
											IMacroEntry refEntry = (IMacroEntry)entries[i];
											if (refEntry.getMacroName().equals(name)) {
												String value = refEntry.getMacroValue();
												return CoreModel.newMacroEntry(macroEntry.getPath(), name, value);
											}
										}
									}
								}
							}
						}
					} else { // Container ref
						IPathEntryContainer container = manager.getPathEntryContainer(refPath, cproject);
						if (container != null) {
							IPathEntry[] entries = container.getPathEntries();
							for (int i = 0; i < entries.length; i++) {
								if (entries[i].getEntryKind() == IPathEntry.CDT_MACRO) {
									IMacroEntry refEntry = (IMacroEntry)entries[i];
									if (refEntry.getMacroName().equals(name)) {
										String value = refEntry.getMacroValue();
										return CoreModel.newMacroEntry(macroEntry.getPath(), name, value);
									}
								}
							}
						}
					}
				}
				break;
			}

			case IPathEntry.CDT_LIBRARY : {
				ILibraryEntry libEntry = (ILibraryEntry)entry;
				IPath refPath = libEntry.getBaseReference();
				if (refPath != null && !refPath.isEmpty()) {
					IPath libraryPath = libEntry.getLibraryPath();
					if (refPath.isAbsolute()) {
						IResource res = cproject.getCModel().getWorkspace().getRoot().findMember(refPath);
						if (res != null && res.getType() == IResource.PROJECT) {
							IProject project = (IProject)res;
							if (CoreModel.hasCNature(project) || CoreModel.hasCCNature(project)) {
								ICProject refCProject = CoreModel.getDefault().create(project);
								if (refCProject != null) {
									IPathEntry[] entries = manager.getResolvedPathEntries(refCProject);
									for (int i = 0; i < entries.length; i++) {
										if (entries[i].getEntryKind() == IPathEntry.CDT_LIBRARY) {
											ILibraryEntry refEntry = (ILibraryEntry)entries[i];
											if (refEntry.getLibraryPath().equals(libraryPath)) {
												IPath newBasePath = refEntry.getBasePath();
												// If the libraryPath is
												// relative give a new basepath
												// if none
												if (!newBasePath.isAbsolute() && !libraryPath.isAbsolute()) {
													IResource refRes;
													if (!newBasePath.isEmpty()) {
														refRes = cproject.getCModel().getWorkspace().getRoot().findMember(
																newBasePath);
													} else {
														IPath refResPath = refEntry.getPath();
														refRes = cproject.getCModel().getWorkspace().getRoot().findMember(
																refResPath);
													}
													if (refRes != null) {
														if (refRes.getType() == IResource.FILE) {
															refRes = refRes.getParent();
														}
														newBasePath = refRes.getLocation().append(newBasePath);
													}
												}

												return CoreModel.newLibraryEntry(entry.getPath(), newBasePath,
														refEntry.getLibraryPath(), refEntry.getSourceAttachmentPath(),
														refEntry.getSourceAttachmentRootPath(),
														refEntry.getSourceAttachmentPrefixMapping(), false);
											}
										}
									}
								}
							}
						}
					} else { // Container ref
						IPathEntryContainer container = manager.getPathEntryContainer(refPath, cproject);
						if (container != null) {
							IPathEntry[] entries = container.getPathEntries();
							for (int i = 0; i < entries.length; i++) {
								if (entries[i].getEntryKind() == IPathEntry.CDT_LIBRARY) {
									ILibraryEntry refEntry = (ILibraryEntry)entries[i];
									if (refEntry.getPath().equals(libraryPath)) {
										return CoreModel.newLibraryEntry(entry.getPath(), refEntry.getBasePath(),
												refEntry.getLibraryPath(), refEntry.getSourceAttachmentPath(),
												refEntry.getSourceAttachmentRootPath(),
												refEntry.getSourceAttachmentPrefixMapping(), false);
									}
								}
							}
						}
					}
				}
				break;
			}

		}
		return entry;
	}

	public static IPathEntry cloneEntryAndExpand(IPath rpath, IPathEntry entry) {

		// get the path
		IPath entryPath = entry.getPath();
		if (entryPath == null) {
			entryPath = Path.EMPTY;
		}
		IPath resourcePath = (entryPath.isAbsolute()) ? entryPath : rpath.append(entryPath);
	
		IPathEntryVariableManager varManager = CCorePlugin.getDefault().getPathEntryVariableManager();
		switch (entry.getEntryKind()) {
			case IPathEntry.CDT_INCLUDE : {
				IIncludeEntry include = (IIncludeEntry)entry;

				IPath basePath = include.getBasePath();
				basePath = varManager.resolvePath(basePath);

				IPath includePath = include.getIncludePath();
				includePath = varManager.resolvePath(includePath);
				
				return CoreModel.newIncludeEntry(resourcePath, basePath, includePath,
						include.isSystemInclude(), include.getExclusionPatterns(), include.isExported());
			}
			case IPathEntry.CDT_INCLUDE_FILE : {
				IIncludeFileEntry includeFile = (IIncludeFileEntry)entry;

				IPath basePath = includeFile.getBasePath();
				basePath = varManager.resolvePath(basePath);

				IPath includeFilePath = includeFile.getIncludeFilePath();
				includeFilePath = varManager.resolvePath(includeFilePath);
				
				return CoreModel.newIncludeFileEntry(resourcePath, basePath, Path.EMPTY, includeFilePath,
						includeFile.getExclusionPatterns(), includeFile.isExported());
			}
			case IPathEntry.CDT_LIBRARY : {
				ILibraryEntry library = (ILibraryEntry)entry;

				IPath basePath = library.getBasePath();
				basePath = varManager.resolvePath(basePath);

				IPath libraryPath = library.getLibraryPath();
				libraryPath = varManager.resolvePath(libraryPath);

				IPath sourceAttachmentPath = library.getSourceAttachmentPath();
				sourceAttachmentPath = varManager.resolvePath(sourceAttachmentPath);

				IPath sourceAttachmentRootPath = library.getSourceAttachmentRootPath();
				sourceAttachmentRootPath = varManager.resolvePath(sourceAttachmentRootPath);

				IPath sourceAttachmentPrefixMapping = library.getSourceAttachmentPrefixMapping();
				sourceAttachmentPrefixMapping = varManager.resolvePath(sourceAttachmentPrefixMapping);

				return CoreModel.newLibraryEntry(resourcePath, basePath, libraryPath,
						sourceAttachmentPath, sourceAttachmentRootPath,
						sourceAttachmentPrefixMapping, library.isExported());
			}
			case IPathEntry.CDT_MACRO : {
				IMacroEntry macro = (IMacroEntry)entry;
				return CoreModel.newMacroEntry(resourcePath, macro.getMacroName(), macro.getMacroValue(),
						macro.getExclusionPatterns(), macro.isExported());
			}
			case IPathEntry.CDT_MACRO_FILE : {
				IMacroFileEntry macroFile = (IMacroFileEntry)entry;

				IPath basePath = macroFile.getBasePath();
				basePath = varManager.resolvePath(basePath);

				IPath macroFilePath = macroFile.getMacroFilePath();
				macroFilePath = varManager.resolvePath(macroFilePath);
				
				return CoreModel.newMacroFileEntry(resourcePath, basePath, Path.EMPTY, macroFilePath,
						macroFile.getExclusionPatterns(), macroFile.isExported());
			}
			case IPathEntry.CDT_OUTPUT : {
				IOutputEntry out = (IOutputEntry)entry;
				return CoreModel.newOutputEntry(resourcePath, out.getExclusionPatterns());
			}
			case IPathEntry.CDT_PROJECT : {
				IProjectEntry projEntry = (IProjectEntry)entry;
				return CoreModel.newProjectEntry(projEntry.getPath(), projEntry.isExported());
			}
			case IPathEntry.CDT_SOURCE : {
				ISourceEntry source = (ISourceEntry)entry;
				return CoreModel.newSourceEntry(resourcePath, source.getExclusionPatterns());
			}
			case IPathEntry.CDT_CONTAINER : {
				return CoreModel.newContainerEntry(entry.getPath(), entry.isExported());
			}
		}
		return entry;
	}

	public static ICModelStatus validatePathEntry(ICProject cProject, IPathEntry[] entries) {

		// Check duplication.
		for (int i = 0; i < entries.length; i++) {
			IPathEntry entry = entries[i];
			if (entry == null) {
				continue;
			}
			for (int j = 0; j < entries.length; j++) {
				IPathEntry otherEntry = entries[j];
				if (otherEntry == null) {
					continue;
				}
				if (entry != otherEntry && otherEntry.equals(entry)) {
					StringBuffer errMesg = new StringBuffer(CCorePlugin.getResourceString("CoreModel.PathEntry.DuplicateEntry")); //$NON-NLS-1$
					return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, errMesg.toString());
				}
			}
		}

		// check duplication of sources
		List dups = checkForDuplication(Arrays.asList(entries), IPathEntry.CDT_SOURCE);
		if (dups.size() > 0) {
			ICModelStatus[] cmodelStatus = new ICModelStatus[dups.size()];
			for (int i = 0; i < dups.size(); ++i) {
				StringBuffer errMesg = new StringBuffer(CCorePlugin.getResourceString("CoreModel.PathEntry.DuplicateEntry")); //$NON-NLS-1$
				cmodelStatus[i] = new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, errMesg.toString());
			}
			return CModelStatus.newMultiStatus(ICModelStatusConstants.INVALID_PATHENTRY, cmodelStatus);
		}

		// check duplication of Outputs
		dups = checkForDuplication(Arrays.asList(entries), IPathEntry.CDT_OUTPUT);
		if (dups.size() > 0) {
			ICModelStatus[] cmodelStatus = new ICModelStatus[dups.size()];
			for (int i = 0; i < dups.size(); ++i) {
				StringBuffer errMesg = new StringBuffer(CCorePlugin.getResourceString("CoreModel.PathEntry.DuplicateEntry")); //$NON-NLS-1$
				cmodelStatus[i] = new CModelStatus(ICModelStatusConstants.NAME_COLLISION, errMesg.toString());
			}
			return CModelStatus.newMultiStatus(ICModelStatusConstants.INVALID_PATHENTRY, cmodelStatus);
		}

		// allow nesting source entries in each other as long as the outer entry
		// excludes the inner one
		for (int i = 0; i < entries.length; i++) {
			IPathEntry entry = entries[i];
			if (entry == null) {
				continue;
			}
			IPath entryPath = entry.getPath();
			int kind = entry.getEntryKind();
			if (kind == IPathEntry.CDT_SOURCE) {
				for (int j = 0; j < entries.length; j++) {
					IPathEntry otherEntry = entries[j];
					if (otherEntry == null) {
						continue;
					}
					int otherKind = otherEntry.getEntryKind();
					IPath otherPath = otherEntry.getPath();
					if (entry != otherEntry && (otherKind == IPathEntry.CDT_SOURCE)) {
						char[][] exclusionPatterns = ((ISourceEntry)otherEntry).fullExclusionPatternChars();
						if (otherPath.isPrefixOf(entryPath) && !otherPath.equals(entryPath)
								&& !CoreModelUtil.isExcluded(entryPath.append("*"), exclusionPatterns)) { //$NON-NLS-1$

							String exclusionPattern = entryPath.removeFirstSegments(otherPath.segmentCount()).segment(0);
							if (CoreModelUtil.isExcluded(entryPath, exclusionPatterns)) {
								StringBuffer errMesg = new StringBuffer(
										CCorePlugin.getResourceString("CoreModel.PathEntry.NestedEntry")); //$NON-NLS-1$
								return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, errMesg.toString());
							} else if (otherKind == IPathEntry.CDT_SOURCE) {
								exclusionPattern += '/';
								StringBuffer errMesg = new StringBuffer(
										CCorePlugin.getResourceString("CoreModel.PathEntry.NestedEntry")); //$NON-NLS-1$
								return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, errMesg.toString());
							} else {
								StringBuffer errMesg = new StringBuffer(
										CCorePlugin.getResourceString("CoreModel.PathEntry.NestedEntry")); //$NON-NLS-1$
								return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, errMesg.toString()); //$NON-NLS-1$
							}
						}
					}
				}
			}
		}

		return CModelStatus.VERIFIED_OK;
	}

	public static ICModelStatus validatePathEntry(ICProject cProject, IPathEntry entry, boolean checkSourceAttachment,
			boolean recurseInContainers) {
		IProject project = cProject.getProject();
		IPath path = entry.getPath();
		if (entry.getEntryKind() != IPathEntry.CDT_PROJECT &&
				entry.getEntryKind() != IPathEntry.CDT_CONTAINER) {
			if (!isValidWorkspacePath(project, path)) {
				return new CModelStatus(
						ICModelStatusConstants.INVALID_PATHENTRY,
						CoreModelMessages.getString("PathEntryManager.0") + path.toOSString() + " for " + ((PathEntry)entry).getKindString()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		switch (entry.getEntryKind()) {
			case IPathEntry.CDT_INCLUDE : {
				IIncludeEntry include = (IIncludeEntry)entry;
				IPath includePath = include.getFullIncludePath();
				if (!isValidExternalPath(includePath)) {
					return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY,
							CoreModelMessages.getString("PathEntryManager.2") + " (" + includePath.toOSString() + ")"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				}
				if (!isValidBasePath(include.getBasePath())) {
					return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY,
							CoreModelMessages.getString("PathEntryManager.1") + " (" + includePath.toOSString() + ")");  //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				}
				break;
			}
			case IPathEntry.CDT_LIBRARY : {
				ILibraryEntry library = (ILibraryEntry)entry;
				if (checkSourceAttachment) {
					IPath sourceAttach = library.getSourceAttachmentPath();
					if (sourceAttach != null) {
						if (!sourceAttach.isAbsolute()) {
							if (!isValidWorkspacePath(project, sourceAttach) || !isValidExternalPath(sourceAttach)) {
								return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY,
										CoreModelMessages.getString("PathEntryManager.3") + " (" + sourceAttach.toOSString() + ")"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
							}
						}
					}
				}
				IPath libraryPath = library.getFullLibraryPath();
				if (!isValidExternalPath(libraryPath)) {
					return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY,
							CoreModelMessages.getString("PathEntryManager.4") + " (" + libraryPath.toOSString() + ")"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				}
				if (!isValidBasePath(library.getBasePath())) {
					return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY,
							CoreModelMessages.getString("PathEntryManager.7") + " (" + libraryPath.toOSString() + ")");  //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				}
				break;
			}
			case IPathEntry.CDT_PROJECT : {
				IProjectEntry projEntry = (IProjectEntry)entry;
				path = projEntry.getPath();
				IProject reqProject = project.getWorkspace().getRoot().getProject(path.segment(0));
				if (!reqProject.isAccessible()) {
					return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY,
							CoreModelMessages.getString("PathEntryManager.5")); //$NON-NLS-1$
				}
				if (! (CoreModel.hasCNature(reqProject) || CoreModel.hasCCNature(reqProject))) {
					return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY,
							CoreModelMessages.getString("PathEntryManager.6")); //$NON-NLS-1$
				}
				break;
			}
			case IPathEntry.CDT_CONTAINER :
				if (recurseInContainers) {
					try {
						IPathEntryContainer cont = manager.getPathEntryContainer((IContainerEntry)entry, cProject);
						IPathEntry[] contEntries = cont.getPathEntries();
						for (int i = 0; i < contEntries.length; i++) {
							ICModelStatus status = validatePathEntry(cProject, contEntries[i], checkSourceAttachment, false);
							if (!status.isOK()) {
								return status;
							}
						}
					} catch (CModelException e) {
						return new CModelStatus(e);
					}
				}
				break;
		}
		return CModelStatus.VERIFIED_OK;
	}

	private static boolean isValidWorkspacePath(IProject project, IPath path) {
		if (path == null) {
			return false;
		}
		IWorkspaceRoot workspaceRoot = project.getWorkspace().getRoot();
		// We accept empy path as the project
		IResource res = null;
		if (path.isAbsolute()) {
			res = workspaceRoot.findMember(path);
		} else {
			res = project.findMember(path);
		}
		return (res != null && res.isAccessible());
	}

	private static boolean isValidExternalPath(IPath path) {
		if (path != null) {
			File file = path.toFile();
			if (file != null) {
				return file.exists();
			}
		}
		return false;
	}

	private static boolean isValidBasePath(IPath path) {
		if (!path.isEmpty() && !path.isAbsolute()) {
			IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			if (res == null || !res.isAccessible()) {
				return false;
			}
		}
		return true;
	}

	public static List checkForDuplication(List pathEntries, int type) {
		List duplicate = new ArrayList(pathEntries.size());
		for (int i = 0; i < pathEntries.size(); ++i) {
			IPathEntry pathEntry = (IPathEntry)pathEntries.get(i);
			if (pathEntry.getEntryKind() == type) {
				for (int j = 0; j < pathEntries.size(); ++j) {
					IPathEntry otherEntry = (IPathEntry)pathEntries.get(j);
					if (otherEntry.getEntryKind() == type) {
						if (!pathEntry.equals(otherEntry)) {
							if (!duplicate.contains(pathEntry)) {
								if (pathEntry.getPath().equals(otherEntry.getPath())) {
									// duplication of sources
									duplicate.add(otherEntry);
								}
							}
						}
					}
				}
			}
		}
		return duplicate;
	}

	/**
	 * Record a new marker denoting a pathentry problem
	 */
	public static void createPathEntryProblemMarker(IProject project, ICModelStatus status) {
		int severity = code2Severity(status);
		try {
			IMarker marker = project.createMarker(ICModelMarker.PATHENTRY_PROBLEM_MARKER);
			marker.setAttributes(new String[]{IMarker.MESSAGE, IMarker.SEVERITY, IMarker.LOCATION,
					ICModelMarker.PATHENTRY_FILE_FORMAT,}, new Object[]{status.getMessage(), new Integer(severity), "pathentry",//$NON-NLS-1$
					"false",//$NON-NLS-1$
			});
		} catch (CoreException e) {
			// could not create marker: cannot do much
			//e.printStackTrace();
		}
	}

	/**
	 * Remove all markers denoting pathentry problems
	 */
	public static void flushPathEntryProblemMarkers(IProject project) {
		IWorkspace workspace = project.getWorkspace();

		try {
			IMarker[] markers = getPathEntryProblemMarkers(project);
			workspace.deleteMarkers(markers);
		} catch (CoreException e) {
			// could not flush markers: not much we can do
			//e.printStackTrace();
		}
	}

	/**
	 * get all markers denoting pathentry problems
	 */
	public static IMarker[] getPathEntryProblemMarkers(IProject project) {

		try {
			IMarker[] markers = project.findMarkers(ICModelMarker.PATHENTRY_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
			if (markers != null) {
				return markers;
			}
		} catch (CoreException e) {
			//e.printStackTrace();
		}
		return NO_MARKERS;
	}

	public static boolean hasPathEntryProblemMarkersChange(IProject project, ICModelStatus[] status) {
		IMarker[] markers = getPathEntryProblemMarkers(project);
		if (markers.length != status.length) {
			return true;
		}
		for (int i = 0; i < markers.length; ++i) {
			boolean found = false;
			String message = markers[i].getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
			int severity = markers[i].getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			for (int j = 0; j < status.length; ++j) {
				String msg = status[j].getMessage();
				int cseverity = code2Severity(status[j]);
				if (msg.equals(message) && severity == cseverity) {
					found = true;
				}
			}
			if (!found) {
				return true;
			}
		}
		return false;
	}

	public static int code2Severity(ICModelStatus status) {
		int severity;
		switch (status.getCode()) {
			case ICModelStatusConstants.INVALID_PATHENTRY :
				severity = IMarker.SEVERITY_WARNING;
				break;

			case ICModelStatusConstants.INVALID_PATH :
				severity = IMarker.SEVERITY_WARNING;
				break;

			default :
				severity = IMarker.SEVERITY_ERROR;
				break;
		}

		return severity;
	}

}

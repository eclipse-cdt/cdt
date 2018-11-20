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
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.cdt.internal.core.util.MementoTokenizer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

public class CProject extends Openable implements ICProject {
	private static final String CUSTOM_DEFAULT_OPTION_VALUE = "#\r\n\r#custom-non-empty-default-value#\r\n\r#"; //$NON-NLS-1$

	public CProject(ICElement parent, IProject project) {
		super(parent, project, ICElement.C_PROJECT);
	}

	@Override
	public IBinaryContainer getBinaryContainer() throws CModelException {
		return ((CProjectInfo) getElementInfo()).getBinaryContainer();
	}

	@Override
	public IArchiveContainer getArchiveContainer() throws CModelException {
		return ((CProjectInfo) getElementInfo()).getArchiveContainer();
	}

	@Override
	public IProject getProject() {
		return getUnderlyingResource().getProject();
	}

	@Override
	public ICElement findElement(IPath path) throws CModelException {
		ICElement celem = null;
		if (path.isAbsolute()) {
			celem = CModelManager.getDefault().create(path);
		} else {
			IProject project = getProject();
			if (project != null) {
				IPath p = project.getFullPath().append(path);
				celem = CModelManager.getDefault().create(p);
			}
		}
		if (celem == null) {
			CModelStatus status = new CModelStatus(ICModelStatusConstants.INVALID_PATH, path);
			throw new CModelException(status);
		}
		return celem;
	}

	public static boolean hasCNature(IProject p) {
		try {
			return p.hasNature(CProjectNature.C_NATURE_ID);
		} catch (CoreException e) {
			//throws exception if the project is not open.
		}
		return false;
	}

	public static boolean hasCCNature(IProject p) {
		try {
			return p.hasNature(CCProjectNature.CC_NATURE_ID);
		} catch (CoreException e) {
			//throws exception if the project is not open.
		}
		return false;
	}

	private boolean isCProject() {
		return hasCNature(getProject()) || hasCCNature(getProject());
	}

	/**
	 * Returns true if this handle represents the same C project
	 * as the given handle. Two handles represent the same
	 * project if they are identical or if they represent a project with
	 * the same underlying resource and occurrence counts.
	 *
	 * @see CElement#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (!(o instanceof CProject))
			return false;

		CProject other = (CProject) o;
		return getProject().equals(other.getProject());
	}

	@Override
	protected CElementInfo createElementInfo() {
		return new CProjectInfo(this);
	}

	// CHECKPOINT: CProjects will return the hash code of their underlying IProject
	@Override
	public int hashCode() {
		return getProject().hashCode();
	}

	@Override
	public IIncludeReference[] getIncludeReferences() throws CModelException {
		CProjectInfo pinfo = (CProjectInfo) CModelManager.getDefault().peekAtInfo(this);
		IIncludeReference[] incRefs = null;
		if (pinfo != null) {
			incRefs = pinfo.incReferences;
		}
		if (incRefs == null) {
			IPathEntry[] entries = getResolvedPathEntries();
			ArrayList<IncludeReference> list = new ArrayList<>(entries.length);
			for (IPathEntry entrie : entries) {
				if (entrie.getEntryKind() == IPathEntry.CDT_INCLUDE) {
					IIncludeEntry entry = (IIncludeEntry) entrie;
					list.add(new IncludeReference(this, entry));
				}
			}
			incRefs = list.toArray(new IIncludeReference[list.size()]);
			if (pinfo != null) {
				pinfo.incReferences = incRefs;
			}
		}
		return incRefs;
	}

	@Override
	public ILibraryReference[] getLibraryReferences() throws CModelException {
		CProjectInfo pinfo = (CProjectInfo) CModelManager.getDefault().peekAtInfo(this);
		ILibraryReference[] libRefs = null;
		if (pinfo != null) {
			libRefs = pinfo.libReferences;
		}

		if (libRefs == null) {
			BinaryParserConfig[] binConfigs = CModelManager.getDefault().getBinaryParser(getProject());
			IPathEntry[] entries = getResolvedPathEntries();
			ArrayList<ILibraryReference> list = new ArrayList<>(entries.length);
			for (IPathEntry entrie : entries) {
				if (entrie.getEntryKind() == IPathEntry.CDT_LIBRARY) {
					ILibraryEntry entry = (ILibraryEntry) entrie;
					ILibraryReference lib = getLibraryReference(this, binConfigs, entry);
					if (lib != null) {
						list.add(lib);
					}
				}
			}
			libRefs = list.toArray(new ILibraryReference[list.size()]);
			if (pinfo != null) {
				pinfo.libReferences = libRefs;
			}
		}
		return libRefs;
	}

	private static ILibraryReference getLibraryReference(ICProject cproject, BinaryParserConfig[] binConfigs,
			ILibraryEntry entry) {
		if (binConfigs == null) {
			binConfigs = CModelManager.getDefault().getBinaryParser(cproject.getProject());
		}
		ILibraryReference lib = null;
		if (binConfigs != null) {
			for (BinaryParserConfig binConfig : binConfigs) {
				IBinaryFile bin;
				try {
					IBinaryParser parser = binConfig.getBinaryParser();
					bin = parser.getBinary(entry.getFullLibraryPath());
					if (bin != null) {
						if (bin.getType() == IBinaryFile.ARCHIVE) {
							lib = new LibraryReferenceArchive(cproject, entry, (IBinaryArchive) bin);
						} else if (bin instanceof IBinaryObject) {
							lib = new LibraryReferenceShared(cproject, entry, (IBinaryObject) bin);
						}
						break;
					}
				} catch (IOException | CoreException e) {
				}
			}
		}
		if (lib == null) {
			lib = new LibraryReference(cproject, entry);
		}
		return lib;
	}

	/**
	 * @see ICProject#getRequiredProjectNames()
	 */
	@Override
	public String[] getRequiredProjectNames() throws CModelException {
		return projectPrerequisites(getResolvedPathEntries());
	}

	public String[] projectPrerequisites(IPathEntry[] entries) throws CModelException {
		return PathEntryManager.getDefault().projectPrerequisites(entries);
	}

	/**
	 * @see org.eclipse.cdt.core.model.ICProject#getOption(String, boolean)
	 */
	@Override
	public String getOption(String optionName, boolean inheritCCoreOptions) {
		if (CModelManager.OptionNames.contains(optionName)) {
			IEclipsePreferences preferences = getPreferences();
			final String cCoreDefault = inheritCCoreOptions ? CCorePlugin.getOption(optionName) : null;
			if (preferences == null) {
				return cCoreDefault;
			}
			String value = preferences.get(optionName, cCoreDefault).trim();
			return value == null ? null : value.trim();
		}

		return null;
	}

	/**
	 * @see org.eclipse.cdt.core.model.ICProject#getOptions(boolean)
	 */
	@Override
	public Map<String, String> getOptions(boolean inheritCCoreOptions) {
		// initialize to the defaults from CCorePlugin options pool
		Map<String, String> options = inheritCCoreOptions ? CCorePlugin.getOptions() : new HashMap<>(5);

		IEclipsePreferences preferences = getPreferences();
		if (preferences == null)
			return options;
		HashSet<String> optionNames = CModelManager.OptionNames;

		// create project options
		try {
			String[] propertyNames = preferences.keys();
			for (String propertyName : propertyNames) {
				String value = preferences.get(propertyName, null);
				if (value != null && optionNames.contains(propertyName)) {
					options.put(propertyName, value.trim());
				}
			}
		} catch (BackingStoreException e) {
			// ignore silently
		}
		return options;
	}

	@Override
	public void setOption(String optionName, String optionValue) {
		if (!CModelManager.OptionNames.contains(optionName))
			return; // unrecognized option

		IEclipsePreferences projectPreferences = getPreferences();
		if (optionValue == null) {
			// remove preference
			projectPreferences.remove(optionName);
		} else {
			projectPreferences.put(optionName, optionValue);
		}

		// Dump changes
		try {
			projectPreferences.flush();
		} catch (BackingStoreException e) {
			// problem with pref store - quietly ignore
		}
	}

	@Override
	public void setOptions(Map<String, String> newOptions) {
		Preferences preferences = new Preferences();
		setPreferences(preferences); // always reset (26255)

		if (newOptions != null) {
			for (Map.Entry<String, String> e : newOptions.entrySet()) {
				String key = e.getKey();
				if (!CModelManager.OptionNames.contains(key))
					continue; // unrecognized option

				// no filtering for encoding (custom encoding for project is allowed)
				String value = e.getValue();
				preferences.setDefault(key, CUSTOM_DEFAULT_OPTION_VALUE); // empty string isn't the default (26251)
				preferences.setValue(key, value);
			}
		}

		// persist options
		savePreferences(preferences);
	}

	/**
	 * Returns the project custom preference pool.
	 * Project preferences may include custom encoding.
	 * @return IEclipsePreferences or <code>null</code> if the project
	 * 	does not have a C nature.
	 */
	private IEclipsePreferences getPreferences() {
		if (!(isCProject())) {
			return null;
		}
		IScopeContext context = new ProjectScope(getProject());
		final IEclipsePreferences preferences = context.getNode(CCorePlugin.PLUGIN_ID);
		return preferences;
	}

	/**
	 * Save project custom preferences to persistent properties
	 */
	private void savePreferences(Preferences preferences) {
		if (preferences == null)
			return;
		if (!isCProject()) {
			return; // ignore
		}
		Iterator<String> iter = CModelManager.OptionNames.iterator();

		while (iter.hasNext()) {
			String qualifiedName = iter.next();
			String dequalifiedName = qualifiedName.substring(CCorePlugin.PLUGIN_ID.length() + 1);
			String value = null;

			try {
				value = preferences.getString(qualifiedName);

				if (value != null && !value.equals(preferences.getDefaultString(qualifiedName))) {
					resource.setPersistentProperty(new QualifiedName(CCorePlugin.PLUGIN_ID, dequalifiedName), value);
				} else {
					resource.setPersistentProperty(new QualifiedName(CCorePlugin.PLUGIN_ID, dequalifiedName), null);
				}
			} catch (CoreException e) {
			}
		}
	}

	/**
	 * Sets cached preferences, no preferences are saved, only info is updated
	 */
	private void setPreferences(Preferences preferences) {
		if (!isCProject()) {
			return; // ignore
		}
		// Do nothing
	}

	@Override
	public IPathEntry[] getResolvedPathEntries() throws CModelException {
		return CoreModel.getResolvedPathEntries(this);
	}

	@Override
	public IPathEntry[] getRawPathEntries() throws CModelException {
		return CoreModel.getRawPathEntries(this);
	}

	@Override
	public void setRawPathEntries(IPathEntry[] newEntries, IProgressMonitor monitor) throws CModelException {
		CoreModel.setRawPathEntries(this, newEntries, monitor);
	}

	@Override
	public ISourceRoot getSourceRoot(ISourceEntry entry) throws CModelException {
		return getSourceRoot(new CSourceEntry(entry.getPath(), entry.getExclusionPatterns(), 0));
	}

	public ISourceRoot getSourceRoot(ICSourceEntry entry) throws CModelException {
		IPath p = getPath();
		IPath sp = entry.getFullPath();
		if (p.isPrefixOf(sp)) {
			int count = sp.matchingFirstSegments(p);
			sp = sp.removeFirstSegments(count);
			IResource res = null;
			if (sp.isEmpty()) {
				res = getProject();
			} else {
				res = getProject().findMember(sp);
			}
			if (res != null) {
				return new SourceRoot(this, res, entry);
			}
		}
		return null;
	}

	@Override
	public ISourceRoot findSourceRoot(IResource res) {
		try {
			ISourceRoot[] roots = getAllSourceRoots();
			for (ISourceRoot root : roots) {
				if (root.isOnSourceEntry(res)) {
					return root;
				}
			}
		} catch (CModelException e) {
		}
		return null;
	}

	@Override
	public ISourceRoot findSourceRoot(IPath path) {
		try {
			ISourceRoot[] roots = getAllSourceRoots();
			for (ISourceRoot root : roots) {
				if (root.getPath().equals(path)) {
					return root;
				}
			}
		} catch (CModelException e) {
		}
		return null;
	}

	@Override
	public ISourceRoot[] getSourceRoots() throws CModelException {
		Object[] children = getChildren();
		ArrayList<ISourceRoot> result = new ArrayList<>(children.length);
		for (Object element : children) {
			if (element instanceof ISourceRoot) {
				result.add((ISourceRoot) element);
			}
		}
		return result.toArray(new ISourceRoot[result.size()]);
	}

	/**
	 * Returns all source roots.
	 *
	 * @return all source roots
	 * @throws CModelException
	 */
	@Override
	public ISourceRoot[] getAllSourceRoots() throws CModelException {
		CProjectInfo pinfo = (CProjectInfo) CModelManager.getDefault().peekAtInfo(this);
		ISourceRoot[] roots = null;
		if (pinfo != null) {
			if (pinfo.sourceRoots != null) {
				roots = pinfo.sourceRoots;
			} else {
				List<ISourceRoot> list = computeSourceRoots();
				roots = pinfo.sourceRoots = list.toArray(new ISourceRoot[list.size()]);
			}
		} else {
			List<ISourceRoot> list = computeSourceRoots();
			roots = list.toArray(new ISourceRoot[list.size()]);
		}
		return roots;
	}

	@Override
	public IOutputEntry[] getOutputEntries() throws CModelException {
		CProjectInfo pinfo = (CProjectInfo) CModelManager.getDefault().peekAtInfo(this);
		IOutputEntry[] outs = null;
		if (pinfo != null) {
			if (pinfo.outputEntries != null) {
				outs = pinfo.outputEntries;
			} else {
				IPathEntry[] entries = getResolvedPathEntries();
				outs = pinfo.outputEntries = getOutputEntries(entries);
			}
		} else {
			IPathEntry[] entries = getResolvedPathEntries();
			outs = getOutputEntries(entries);
		}
		return outs;
	}

	public IOutputEntry[] getOutputEntries(IPathEntry[] entries) throws CModelException {
		ArrayList<IPathEntry> list = new ArrayList<>(entries.length);
		for (IPathEntry entrie : entries) {
			if (entrie.getEntryKind() == IPathEntry.CDT_OUTPUT) {
				list.add(entrie);
			}
		}
		IOutputEntry[] outputs = new IOutputEntry[list.size()];
		list.toArray(outputs);
		return outputs;
	}

	private boolean isParentOfOutputEntry(IResource resource) {
		IPath path = resource.getFullPath();

		// ensure that folders are only excluded if all of their children are excluded
		if (resource.getType() == IResource.FOLDER || resource.getType() == IResource.PROJECT) {
			try {
				IOutputEntry[] entries = getOutputEntries();
				for (IOutputEntry entry : entries) {

					if (path.isPrefixOf(entry.getPath())) {
						return true;
					}
				}
			} catch (CModelException e) {
				//
			}
			return false;
		}

		return false;

	}

	@Override
	public boolean isOnOutputEntry(IResource resource) {
		IPath path = resource.getFullPath();

		// ensure that folders are only excluded if all of their children are excluded
		if (resource.getType() == IResource.FOLDER || resource.getType() == IResource.PROJECT) {
			path = path.append("*"); //$NON-NLS-1$
		}

		try {
			IOutputEntry[] entries = getOutputEntries();
			for (IOutputEntry entrie : entries) {
				boolean on = isOnOutputEntry(entrie, path);
				if (on) {
					return on;
				}
			}
		} catch (CModelException e) {
			//
		}
		return false;
	}

	private boolean isOnOutputEntry(IOutputEntry entry, IPath path) {
		if (entry.getPath().isPrefixOf(path) && !CoreModelUtil.isExcluded(path, entry.fullExclusionPatternChars())) {
			return true;
		}
		return false;
	}

	@Override
	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm, Map<ICElement, CElementInfo> newElements,
			IResource underlyingResource) throws CModelException {
		boolean validInfo = false;
		try {
			IResource res = getResource();
			if (res != null && res.isAccessible()) {
				validInfo = computeChildren(info, res);
			} else {
				throw newNotPresentException();
			}
		} finally {
			if (!validInfo) {
				CModelManager.getDefault().removeInfo(this);
			}
		}
		return validInfo;
	}

	protected List<ISourceRoot> computeSourceRoots() throws CModelException {
		//IPathEntry[] entries = getResolvedPathEntries();
		ICSourceEntry[] entries = null;
		ICProjectDescription des = CProjectDescriptionManager.getInstance().getProjectDescription(getProject(), false);
		if (des != null) {
			ICConfigurationDescription cfg = des.getDefaultSettingConfiguration();
			if (cfg != null)
				entries = cfg.getResolvedSourceEntries();
		}

		if (entries != null) {
			ArrayList<ISourceRoot> list = new ArrayList<>(entries.length);
			for (ICSourceEntry sourceEntry : entries) {
				ISourceRoot root = getSourceRoot(sourceEntry);
				if (root != null) {
					list.add(root);
				}
			}
			return list;
		}
		return Collections.emptyList();
	}

	protected boolean computeChildren(OpenableInfo info, IResource res) throws CModelException {
		List<ISourceRoot> sourceRoots = computeSourceRoots();
		List<ICContainer> children = new ArrayList<>(sourceRoots.size());
		children.addAll(sourceRoots);

		boolean projectIsSourceRoot = false;
		for (ISourceRoot sourceRoot : sourceRoots) {
			if (sourceRoot.getResource().equals(getProject())) {
				projectIsSourceRoot = true;
				break;
			}
		}

		// Now look for output folders
		try {
			IResource[] resources = getProject().members();
			for (IResource child : resources) {
				if (child.getType() == IResource.FOLDER) {
					boolean found = false;
					for (ISourceRoot sourceRoot : sourceRoots) {
						if (sourceRoot.isOnSourceEntry(child)) {
							found = true;
							break;
						}
					}

					// Not in source folder, check if it's a container on output entry
					// Also make sure I'm not a source root since my SourceRoot object would
					// have already added this.
					if (!found && !projectIsSourceRoot && (isParentOfOutputEntry(child) || isOnOutputEntry(child)))
						children.add(new CContainer(this, child));
				}
			}
		} catch (CoreException e) {
			// ignore
		}

		info.setChildren(children);
		if (info instanceof CProjectInfo) {
			CProjectInfo pinfo = (CProjectInfo) info;
			pinfo.sourceRoots = sourceRoots.toArray(new ISourceRoot[sourceRoots.size()]);
			pinfo.setNonCResources(null);
		}
		return true;
	}

	@Override
	public boolean isOnSourceRoot(ICElement element) {
		try {
			ISourceRoot[] roots = getSourceRoots();
			for (ISourceRoot root : roots) {
				if (root.isOnSourceEntry(element)) {
					return true;
				}
			}
		} catch (CModelException e) {
			// ..
		}
		return false;
	}

	@Override
	public boolean isOnSourceRoot(IResource resource) {
		try {
			ISourceRoot[] roots = getSourceRoots();
			for (ISourceRoot root : roots) {
				if (root.isOnSourceEntry(resource)) {
					return true;
				}
			}
		} catch (CModelException e) {
			//
		}
		return false;
	}

	@Override
	public boolean exists() {
		if (!isCProject()) {
			return false;
		}
		return true;
	}

	@Override
	public Object[] getNonCResources() throws CModelException {
		return ((CProjectInfo) getElementInfo()).getNonCResources(getResource());
	}

	@Override
	protected void closing(Object info) throws CModelException {
		if (info instanceof CProjectInfo) {
			CModelManager.getDefault().removeBinaryRunner(this);
			CProjectInfo pinfo = (CProjectInfo) info;
			if (pinfo.vBin != null) {
				pinfo.vBin.close();
			}
			if (pinfo.vLib != null) {
				pinfo.vLib.close();
			}
			pinfo.resetCaches();
		}
		super.closing(info);
	}

	/**
	 * Resets this project's caches
	 */
	public void resetCaches() {
		CProjectInfo pinfo = (CProjectInfo) CModelManager.getDefault().peekAtInfo(this);
		if (pinfo != null) {
			pinfo.resetCaches();
		}
	}

	@Override
	public ICElement getHandleFromMemento(String token, MementoTokenizer memento) {
		switch (token.charAt(0)) {
		case CEM_SOURCEROOT:
			IPath rootPath = Path.EMPTY;
			token = null;
			while (memento.hasMoreTokens()) {
				token = memento.nextToken();
				char firstChar = token.charAt(0);
				if (firstChar != CEM_SOURCEFOLDER && firstChar != CEM_TRANSLATIONUNIT) {
					rootPath = rootPath.append(token);
					token = null;
				} else {
					break;
				}
			}
			if (!rootPath.isAbsolute()) {
				rootPath = getProject().getFullPath().append(rootPath);
			}
			CElement root = (CElement) findSourceRoot(rootPath);
			if (root != null) {
				if (token != null) {
					return root.getHandleFromMemento(token, memento);
				} else {
					return root.getHandleFromMemento(memento);
				}
			}
			break;
		case CEM_TRANSLATIONUNIT:
			if (!memento.hasMoreTokens())
				return this;
			String tuName = memento.nextToken();
			final IPath path = Path.fromPortableString(tuName);
			CElement tu = null;
			if (!path.isAbsolute()) {
				final IProject project = getProject();
				if (project != null) {
					IResource resource = project.findMember(path);
					if (resource != null && resource.getType() == IResource.FILE) {
						final IFile file = (IFile) resource;
						tu = (CElement) CModelManager.getDefault().create(file, this);
						if (tu == null) {
							String contentTypeId = CoreModel.getRegistedContentTypeId(project, file.getName());
							if (contentTypeId != null) {
								tu = new TranslationUnit(this, file, contentTypeId);
							}
						}
					}
				}
			} else {
				tu = (CElement) CoreModel.getDefault().createTranslationUnitFrom(this, path);
			}
			if (tu != null) {
				return tu.getHandleFromMemento(memento);
			}
			break;
		}
		return null;
	}

	@Override
	protected char getHandleMementoDelimiter() {
		return CEM_CPROJECT;
	}
}

package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.BinaryParserConfig;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinaryContainer;
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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.QualifiedName;

public class CProject extends Openable implements ICProject {

	private static final String CUSTOM_DEFAULT_OPTION_VALUE = "#\r\n\r#custom-non-empty-default-value#\r\n\r#"; //$NON-NLS-1$

	public CProject(ICElement parent, IProject project) {
		super(parent, project, CElement.C_PROJECT);
	}

	public IBinaryContainer getBinaryContainer() throws CModelException {
		return ((CProjectInfo) getElementInfo()).getBinaryContainer();
	}

	public IArchiveContainer getArchiveContainer() throws CModelException {
		return ((CProjectInfo) getElementInfo()).getArchiveContainer();
	}

	public IProject getProject() {
		return getUnderlyingResource().getProject();
	}

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
	public boolean equals(Object o) {
	
		if (this == o)
			return true;
	
		if (!(o instanceof CProject))
			return false;
	
		CProject other = (CProject) o;
		return getProject().equals(other.getProject());
	}

	protected CElementInfo createElementInfo() {
		return new CProjectInfo(this);
	}

	// CHECKPOINT: CProjects will return the hash code of their underlying IProject
	public int hashCode() {
		return getProject().hashCode();
	}

	public IIncludeReference[] getIncludeReferences() throws CModelException {
		CProjectInfo pinfo = (CProjectInfo)CModelManager.getDefault().peekAtInfo(this);
		IIncludeReference[] incRefs = null;
		if (pinfo != null) {
			incRefs = pinfo.incReferences;
		}
		if (incRefs == null) {
			IPathEntry[] entries = getResolvedPathEntries();
			ArrayList list = new ArrayList(entries.length);
			for (int i = 0; i < entries.length; i++) {
				if (entries[i].getEntryKind() == IPathEntry.CDT_INCLUDE) {
					IIncludeEntry entry = (IIncludeEntry) entries[i];
					IIncludeReference inc = new IncludeReference(this, entry);
					if (inc != null) {
						list.add(inc);
					}
				}
			}
			incRefs = (IIncludeReference[]) list.toArray(new IIncludeReference[0]);
			if (pinfo != null) {
				pinfo.incReferences = incRefs;
			}
		}
		return incRefs;
	}

	public ILibraryReference[] getLibraryReferences() throws CModelException {
		CProjectInfo pinfo = (CProjectInfo)CModelManager.getDefault().peekAtInfo(this);
		ILibraryReference[] libRefs = null;
		if (pinfo != null) {
			libRefs = pinfo.libReferences;
		}

		if (libRefs == null) {
			BinaryParserConfig[] binConfigs = CModelManager.getDefault().getBinaryParser(getProject());
			IPathEntry[] entries = getResolvedPathEntries();
			ArrayList list = new ArrayList(entries.length);
			for (int i = 0; i < entries.length; i++) {
				if (entries[i].getEntryKind() == IPathEntry.CDT_LIBRARY) {
					ILibraryEntry entry = (ILibraryEntry) entries[i];
					ILibraryReference lib = getLibraryReference(this, binConfigs, entry);
					if (lib != null) {
						list.add(lib);
					}
				}
			}
			libRefs = (ILibraryReference[]) list.toArray(new ILibraryReference[0]);
			if (pinfo != null) {
				pinfo.libReferences = libRefs;
			}
		}
		return libRefs;
	}

	private static ILibraryReference getLibraryReference(ICProject cproject, BinaryParserConfig[] binConfigs, ILibraryEntry entry) {
		if (binConfigs == null) {
			binConfigs = CModelManager.getDefault().getBinaryParser(cproject.getProject());
		}
		ILibraryReference lib = null;
		if (binConfigs != null) {
			for (int i = 0; i < binConfigs.length; i++) {
				IBinaryFile bin;
				try {
					bin = binConfigs[i].getBinaryParser().getBinary(entry.getFullLibraryPath());
					if (bin != null) {
						if (bin.getType() == IBinaryFile.ARCHIVE) {
							lib = new LibraryReferenceArchive(cproject, entry, (IBinaryArchive)bin);
						} else if (bin instanceof IBinaryObject){
							lib = new LibraryReferenceShared(cproject, entry, (IBinaryObject)bin);
						}
						break;
					}
				} catch (IOException e1) {
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
	public String[] getRequiredProjectNames() throws CModelException {
		return projectPrerequisites(getResolvedPathEntries());
	}

	public String[] projectPrerequisites(IPathEntry[] entries) throws CModelException {
		return PathEntryManager.getDefault().projectPrerequisites(entries);
	}


	/**
	 * @see org.eclipse.cdt.core.model.ICProject#getOption(String, boolean)
	 */
	public String getOption(String optionName, boolean inheritCCoreOptions) {

		if (CModelManager.OptionNames.contains(optionName)) {
			Preferences preferences = getPreferences();

			if (preferences == null || preferences.isDefault(optionName)) {
				return inheritCCoreOptions ? CCorePlugin.getOption(optionName) : null;
			}

			return preferences.getString(optionName).trim();
		}

		return null;
	}

	/**
	 * @see org.eclipse.cdt.core.model.ICProject#getOptions(boolean)
	 */
	public Map getOptions(boolean inheritCCoreOptions) {
		// initialize to the defaults from CCorePlugin options pool
		Map options = inheritCCoreOptions ? CCorePlugin.getOptions() : new HashMap(5);

		Preferences preferences = getPreferences();
		if (preferences == null)
			return options;
		HashSet optionNames = CModelManager.OptionNames;

		// get preferences set to their default
		if (inheritCCoreOptions) {
			String[] defaultPropertyNames = preferences.defaultPropertyNames();
			for (int i = 0; i < defaultPropertyNames.length; i++) {
				String propertyName = defaultPropertyNames[i];
				if (optionNames.contains(propertyName)) {
					options.put(propertyName, preferences.getDefaultString(propertyName).trim());
				}
			}
		}
		// get custom preferences not set to their default
		String[] propertyNames = preferences.propertyNames();
		for (int i = 0; i < propertyNames.length; i++) {
			String propertyName = propertyNames[i];
			if (optionNames.contains(propertyName)) {
				options.put(propertyName, preferences.getString(propertyName).trim());
			}
		}
		return options;
	}

	/**
	 * @see org.eclipse.cdt.core.model.ICProject#setOption(java.lang.String, java.lang.String)
	 */
	public void setOption(String optionName, String optionValue) {
		if (!CModelManager.OptionNames.contains(optionName))
			return; // unrecognized option

		Preferences preferences = getPreferences();
		preferences.setDefault(optionName, CUSTOM_DEFAULT_OPTION_VALUE); // empty string isn't the default (26251)
		preferences.setValue(optionName, optionValue);

		savePreferences(preferences);
	}

	/**
	 * @see org.eclipse.cdt.core.model.ICProject#setOptions(Map)
	 */
	public void setOptions(Map newOptions) {
		Preferences preferences = new Preferences();
		setPreferences(preferences); // always reset (26255)

		if (newOptions != null) {
			Iterator keys = newOptions.keySet().iterator();

			while (keys.hasNext()) {
				String key = (String) keys.next();
				if (!CModelManager.OptionNames.contains(key))
					continue; // unrecognized option

				// no filtering for encoding (custom encoding for project is allowed)
				String value = (String) newOptions.get(key);
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
	 */
	private Preferences getPreferences() {
		if (!(isCProject())) {
			return null;
		}
		Preferences preferences = new Preferences();
		Iterator iter = CModelManager.OptionNames.iterator();

		while (iter.hasNext()) {
			String qualifiedName = (String) iter.next();
			String dequalifiedName = qualifiedName.substring(CCorePlugin.PLUGIN_ID.length() + 1);
			String value = null;

			try {
				value = resource.getPersistentProperty(new QualifiedName(CCorePlugin.PLUGIN_ID, dequalifiedName));
			} catch (CoreException e) {
			}

			if (value != null)
				preferences.setValue(qualifiedName, value);
		}

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
		Iterator iter = CModelManager.OptionNames.iterator();

		while (iter.hasNext()) {
			String qualifiedName = (String) iter.next();
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

	/*
	 * Set cached preferences, no preferences are saved, only info is updated
	 */
	private void setPreferences(Preferences preferences) {
		if (!isCProject()) {
			return; // ignore
		}
		// Do nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICProject#getResolvedCPathEntries()
	 */
	public IPathEntry[] getResolvedPathEntries() throws CModelException {
		return CoreModel.getResolvedPathEntries(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICProject#getRawCPathEntries()
	 */
	public IPathEntry[] getRawPathEntries() throws CModelException {
		return CoreModel.getRawPathEntries(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICProject#setRawCPathEntries(org.eclipse.cdt.core.model.IPathEntry[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setRawPathEntries(IPathEntry[] newEntries, IProgressMonitor monitor) throws CModelException {
		CoreModel.setRawPathEntries(this, newEntries, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICProject#getSourceRoot(org.eclipse.cdt.core.model.ISourceEntry)
	 */
	public ISourceRoot getSourceRoot(ISourceEntry entry) throws CModelException {
		IPath p = getPath();
		IPath sp = entry.getPath();
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICProject#getSourceRoots()
	 */
	public ISourceRoot[] getSourceRoots() throws CModelException {
		Object[] children;
		int length;

		children = getChildren();
		length = children.length; 
		ISourceRoot[] roots = new ISourceRoot[length]; 
		System.arraycopy(children, 0, roots, 0, length);
			
		return roots;
	}

	/**
	 * 
	 * @return
	 * @throws CModelException
	 */
	public ISourceRoot[] getAllSourceRoots() throws CModelException {
		CProjectInfo pinfo = (CProjectInfo)CModelManager.getDefault().peekAtInfo(this);
		ISourceRoot[] roots = null;
		if (pinfo != null) {
			if (pinfo.sourceRoots != null) {
				roots = pinfo.sourceRoots;
			} else {
				roots = pinfo.sourceRoots = (ISourceRoot[])computeSourceRoots().toArray(new ISourceRoot[computeSourceRoots().size()]);				
			}
		} else {
			roots = (ISourceRoot[])computeSourceRoots().toArray(new ISourceRoot[computeSourceRoots().size()]);
		}
		return roots;
	}

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

	/**
	 * 
	 */
	public IOutputEntry[] getOutputEntries(IPathEntry[] entries) throws CModelException {
		ArrayList list = new ArrayList(entries.length);
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getEntryKind() == IPathEntry .CDT_OUTPUT) {
				list.add(entries[i]);
			}
		}
		IOutputEntry[] outputs = new IOutputEntry[list.size()];
		list.toArray(outputs);
		return outputs;
	}

	/**
	 * 
	 */
	public boolean isOnOutputEntry(IResource resource) {
		IPath path = resource.getFullPath();
		
		// ensure that folders are only excluded if all of their children are excluded
		if (resource.getType() == IResource.FOLDER || resource.getType() == IResource.PROJECT) {
			path = path.append("*"); //$NON-NLS-1$
		}

		try {
			IOutputEntry[] entries = getOutputEntries();
			for (int i = 0; i < entries.length; i++) {
				boolean on = isOnOutputEntry(entries[i], path);
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#buildStructure(org.eclipse.cdt.internal.core.model.OpenableInfo, org.eclipse.core.runtime.IProgressMonitor, java.util.Map, org.eclipse.core.resources.IResource)
	 */
	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm,
			Map newElements, IResource underlyingResource)
			throws CModelException {
		boolean validInfo = false;
		try {
			IResource res = getResource();
			if (res != null && res.isAccessible()) {
				validInfo = computeSourceRoots(info, res);
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

	protected List computeSourceRoots() throws CModelException {
		IPathEntry[] entries = getResolvedPathEntries();
		ArrayList list = new ArrayList(entries.length);
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getEntryKind() == IPathEntry.CDT_SOURCE) {
				ISourceEntry sourceEntry = (ISourceEntry)entries[i];
				ISourceRoot root = getSourceRoot(sourceEntry);
				if (root != null) {
					list.add(root);
				}
			}
		}
		return list;
	}

	protected boolean computeSourceRoots(OpenableInfo info, IResource res) throws CModelException {
		info.setChildren(computeSourceRoots());
		if (info instanceof CProjectInfo) {
			CProjectInfo pinfo = (CProjectInfo)info;
			pinfo.setNonCResources(null);
		}

		return true;
	}

	/*
	 * @see ICProject
	 */
	public boolean isOnSourceRoot(ICElement element) {
		try {
			ISourceRoot[] roots = getSourceRoots();
			for (int i = 0; i < roots.length; i++) {
				if (roots[i].isOnSourceEntry(element)) {
					return true;
				}
			}
		} catch (CModelException e) {
			// ..
		}
		return false;
	}

	/*
	 * @see ICProject
	 */
	public boolean isOnSourceRoot(IResource resource) {
		try {
			ISourceRoot[] roots = getSourceRoots();
			for (int i = 0; i < roots.length; i++) {
				if (roots[i].isOnSourceEntry(resource)) {
					return true;
				}
			}
		} catch (CModelException e) {
			//
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#exists()
	 */
	public boolean exists() {
		if (!isCProject()) {
			return false;
		}
		return super.exists();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICProject#getNonCResources()
	 */
	public Object[] getNonCResources() throws CModelException {
		return ((CProjectInfo) getElementInfo()).getNonCResources(getResource());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CElement#closing(java.lang.Object)
	 */
	protected void closing(Object info) throws CModelException {
		if (info instanceof CProjectInfo) {
			CProjectInfo pinfo = (CProjectInfo)info;
			if (pinfo.vBin != null) {
				pinfo.vBin.close();
			}
			if (pinfo.vLib != null) {
				pinfo.vLib.close();
			}
			pinfo.resetCaches();
			CModelManager.getDefault().removeBinaryRunner(this);
		}
		super.closing(info);
	}

	/*
	 * Resets this project's caches
	 */
	public void resetCaches() {
		CProjectInfo pinfo = (CProjectInfo) CModelManager.getDefault().peekAtInfo(this);
		if (pinfo != null){
			pinfo.resetCaches();
		}
	}

}

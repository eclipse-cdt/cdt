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
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.QualifiedName;

public class CProject extends CContainer implements ICProject {

	public CProject(ICElement parent, IProject project) {
		super(parent, project, CElement.C_PROJECT);
	}

	public IBinaryContainer getBinaryContainer() {
		return ((CProjectInfo) getElementInfo()).getBinaryContainer();
	}

	public IArchiveContainer getArchiveContainer() {
		return ((CProjectInfo) getElementInfo()).getArchiveContainer();
	}

	public IProject getProject() {
		return getUnderlyingResource().getProject();
	}

	private static final String CUSTOM_DEFAULT_OPTION_VALUE = "#\r\n\r#custom-non-empty-default-value#\r\n\r#"; //$NON-NLS-1$

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

	protected CElementInfo createElementInfo() {
		return new CProjectInfo(this);
	}

	// CHECKPOINT: CProjects will return the hash code of their underlying IProject
	public int hashCode() {
		return getProject().hashCode();
	}

	public ILibraryReference[] getLibraryReferences() throws CModelException {
		ArrayList list = new ArrayList(5);
		IBinaryParser binParser = null;
		try {
			binParser = CCorePlugin.getDefault().getBinaryParser(getProject());
		} catch (CoreException e) {
		}
		IPathEntry[] entries = getResolvedPathEntries();
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getEntryKind() == IPathEntry.CDT_LIBRARY) {
				ILibraryEntry entry = (ILibraryEntry) entries[i];
				ILibraryReference lib = getLibraryReference(this, binParser, entry);
				if (lib != null) {
					list.add(lib);
				}
			}
		}
		return (ILibraryReference[]) list.toArray(new ILibraryReference[0]);
	}

	public static ILibraryReference getLibraryReference(ICProject cproject, IBinaryParser binParser, ILibraryEntry entry) {
		if (binParser == null) {
			try {
				binParser = CCorePlugin.getDefault().getBinaryParser(cproject.getProject());
			} catch (CoreException e) {
			}
		}
		ILibraryReference lib = null;
		if (binParser != null) {
			IBinaryFile bin;
			try {
				bin = binParser.getBinary(entry.getPath());
				if (bin.getType() == IBinaryFile.ARCHIVE) {
					lib = new LibraryReferenceArchive(cproject, entry, (IBinaryArchive)bin);
				} else {
					lib = new LibraryReferenceShared(cproject, entry, bin);
				}
			} catch (IOException e1) {
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
		// Do nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICProject#getResolvedCPathEntries()
	 */
	public IPathEntry[] getResolvedPathEntries() throws CModelException {
		return CoreModel.getDefault().getResolvedClasspathEntries(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICProject#getRawCPathEntries()
	 */
	public IPathEntry[] getRawPathEntries() throws CModelException {
		return CoreModel.getDefault().getRawPathEntries(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICProject#setRawCPathEntries(org.eclipse.cdt.core.model.IPathEntry[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setRawPathEntries(IPathEntry[] newEntries, IProgressMonitor monitor) throws CModelException {
		CoreModel.getDefault().setRawPathEntries(this, newEntries, monitor);
	}

}

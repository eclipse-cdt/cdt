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
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ICPathEntry;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IProjectEntry;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.QualifiedName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CProject extends CContainer implements ICProject {

	/**
	 * An empty array of strings indicating that a project doesn't have any prerequesite projects.
	 */
	protected static final String[] NO_PREREQUISITES = new String[0];

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
		ICPathEntry[] entries = getResolvedCPathEntries();
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getEntryKind() == ICPathEntry.CDT_LIBRARY) {
				ILibraryEntry entry = (ILibraryEntry) entries[i];
				ILibraryReference lib = null;
				if (binParser != null) {
					IBinaryFile bin;
					try {
						bin = binParser.getBinary(entry.getLibraryPath());
						if (bin.getType() == IBinaryFile.ARCHIVE) {
							lib = new LibraryReferenceArchive(this, entry, (IBinaryArchive)bin);
						} else {
							lib = new LibraryReferenceShared(this, entry, bin);
						}
					} catch (IOException e1) {
						lib = new LibraryReference(this, entry);
					}
				}
				if (lib != null) {
					list.add(lib);
				}
			}
		}
		return (ILibraryReference[]) list.toArray(new ILibraryReference[0]);
	}

	/**
	 * @see ICProject#getRequiredProjectNames()
	 */
	public String[] getRequiredProjectNames() throws CModelException {
		return projectPrerequisites(getResolvedCPathEntries());
	}

	public String[] projectPrerequisites(ICPathEntry[] entries) throws CModelException {
		ArrayList prerequisites = new ArrayList();
		for (int i = 0, length = entries.length; i < length; i++) {
			if (entries[i].getEntryKind() == ICPathEntry.CDT_PROJECT) {
				IProjectEntry entry = (IProjectEntry)entries[i];
				prerequisites.add(entry.getProjectPath().lastSegment());
			}
		}
		int size = prerequisites.size();
		if (size != 0) {
			String[] result = new String[size];
			prerequisites.toArray(result);
			return result;
		}
		return NO_PREREQUISITES;
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


	static String PATH_ENTRY = "cpathentry"; //$NON-NLS-1$
	static String PATH_ENTRY_ID = "org.eclipse.cdt.core.cpathentry"; //$NON-NLS-1$
	static String ATTRIBUTE_KIND = "kind"; //$NON-NLS-1$
	static String ATTRIBUTE_PATH = "path"; //$NON-NLS-1$
	static String ATTRIBUTE_EXPORTED = "exported"; //$NON-NLS-1$
	static String ATTRIBUTE_SOURCEPATH = "sourcepath"; //$NON-NLS-1$
	static String ATTRIBUTE_ROOTPATH = "roopath"; //$NON-NLS-1$
	static String ATTRIBUTE_PREFIXMAPPING = "prefixmapping"; //$NON-NLS-1$
	static String ATTRIBUTE_EXCLUDING = "excluding"; //$NON-NLS-1$
	static String ATTRIBUTE_RECUSIVE = "recusive"; //$NON-NLS-1$
	static String ATTRIBUTE_OUTPUT = "output"; //$NON-NLS-1$
	static String ATTRIBUTE_INCLUDE = "include"; //$NON-NLS-1$
	static String ATTRIBUTE_SYSTEM = "system"; //$NON-NLS-1$
	static String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	static String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$
	static String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	static String VALUE_TRUE = "true"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICProject#getResolvedCPathEntries()
	 */
	public ICPathEntry[] getResolvedCPathEntries() throws CModelException {
		// Not implemented
		return getRawCPathEntries();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICProject#getRawCPathEntries()
	 */
	public ICPathEntry[] getRawCPathEntries() throws CModelException {
		ArrayList pathEntries = new ArrayList();
		try {
			ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(getProject());
			Element element = cdesc.getProjectData(PATH_ENTRY_ID);
			NodeList list = element.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node childNode = list.item(i);
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					if (childNode.getNodeName().equals(PATH_ENTRY)) {
						pathEntries.add(decodeCPathEntry((Element) childNode));
					}
				}
			}
		} catch (CoreException e) {
			throw new CModelException(e);
		}
		return (ICPathEntry[]) pathEntries.toArray(new ICPathEntry[0]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICProject#setRawCPathEntries(org.eclipse.cdt.core.model.ICPathEntry[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setRawCPathEntries(ICPathEntry[] newEntries, IProgressMonitor monitor) throws CModelException {
		try {
			SetCPathEntriesOperation op = new SetCPathEntriesOperation(this, getRawCPathEntries(), newEntries);
			runOperation(op, monitor);
		} catch (CoreException e) {
			throw new CModelException(e);
		}

	}

	ICPathEntry decodeCPathEntry(Element element) throws CModelException {
		IPath projectPath = getProject().getFullPath();

		// kind
		String kindAttr = element.getAttribute(ATTRIBUTE_KIND);
		int kind = CPathEntry.kindFromString(kindAttr);

		// exported flag
		boolean isExported = false;
		if (element.hasAttribute(ATTRIBUTE_EXPORTED)) {
			isExported = element.getAttribute(ATTRIBUTE_EXPORTED).equals(VALUE_TRUE);
		}

		// ensure path is absolute
		String pathAttr = element.getAttribute(ATTRIBUTE_PATH);
		IPath path = new Path(pathAttr);
		if (kind != ICPathEntry.CDT_VARIABLE && !path.isAbsolute()) {
			path = projectPath.append(path);
		}

		// source attachment info (optional)
		IPath sourceAttachmentPath =
			element.hasAttribute(ATTRIBUTE_SOURCEPATH) ? new Path(element.getAttribute(ATTRIBUTE_SOURCEPATH)) : null;
		IPath sourceAttachmentRootPath =
			element.hasAttribute(ATTRIBUTE_ROOTPATH) ? new Path(element.getAttribute(ATTRIBUTE_ROOTPATH)) : null;
		IPath sourceAttachmentPrefixMapping =
			element.hasAttribute(ATTRIBUTE_PREFIXMAPPING) ? new Path(element.getAttribute(ATTRIBUTE_PREFIXMAPPING)) : null;

		// exclusion patterns (optional)
		String exclusion = element.getAttribute(ATTRIBUTE_EXCLUDING);
		IPath[] exclusionPatterns = ACPathEntry.NO_EXCLUSION_PATTERNS;
		if (!exclusion.equals("")) { //$NON-NLS-1$
			char[][] patterns = CharOperation.splitOn('|', exclusion.toCharArray());
			int patternCount;
			if ((patternCount = patterns.length) > 0) {
				exclusionPatterns = new IPath[patternCount];
				for (int j = 0; j < patterns.length; j++) {
					exclusionPatterns[j] = new Path(new String(patterns[j]));
				}
			}
		}

		boolean isRecursive = false;
		if (element.hasAttribute(ATTRIBUTE_RECUSIVE)) {
			isRecursive = element.getAttribute(ATTRIBUTE_RECUSIVE).equals(VALUE_TRUE);
		}

		// recreate the CP entry

		switch (kind) {

			case ICPathEntry.CDT_PROJECT :
				return CoreModel.newProjectEntry(path, isExported);

			case ICPathEntry.CDT_LIBRARY :
				return CoreModel.newLibraryEntry(
					path,
					sourceAttachmentPath,
					sourceAttachmentRootPath,
					sourceAttachmentPrefixMapping,
					isExported);

			case ICPathEntry.CDT_SOURCE :
				{
					// custom output location
					IPath outputLocation = element.hasAttribute(ATTRIBUTE_OUTPUT) ? projectPath.append(element.getAttribute(ATTRIBUTE_OUTPUT)) : null; //$NON-NLS-1$ //$NON-NLS-2$
					// must be an entry in this project or specify another project
					String projSegment = path.segment(0);
					if (projSegment != null && projSegment.equals(getElementName())) { // this project
						return CoreModel.newSourceEntry(path, outputLocation, isRecursive, exclusionPatterns);
					} else { // another project
						return CoreModel.newProjectEntry(path, isExported);
					}
				}

				//			case ICPathEntry.CDT_VARIABLE :
				//				return CoreModel.newVariableEntry(path, sourceAttachmentPath, sourceAttachmentRootPath);

			case ICPathEntry.CDT_INCLUDE :
				{
					// include path info (optional
					IPath includePath =
						element.hasAttribute(ATTRIBUTE_INCLUDE) ? new Path(element.getAttribute(ATTRIBUTE_INCLUDE)) : null;
					// isSysteminclude
					boolean isSystemInclude = false;
					if (element.hasAttribute(ATTRIBUTE_SYSTEM)) {
						isSystemInclude = element.getAttribute(ATTRIBUTE_SYSTEM).equals(VALUE_TRUE);
					}
					return CoreModel.newIncludeEntry(
						path,
						includePath,
						isSystemInclude,
						isRecursive,
						exclusionPatterns,
						isExported);
				}

			case ICPathEntry.CDT_MACRO :
				{
					String macroName = element.getAttribute(ATTRIBUTE_NAME); //$NON-NLS-1$
					String macroValue = element.getAttribute(ATTRIBUTE_VALUE); //$NON-NLS-1$
					return CoreModel.newMacroEntry(path, macroName, macroValue, isRecursive, exclusionPatterns, isExported);
				}

			case ICPathEntry.CDT_CONTAINER :
				{
					String id = element.getAttribute(ATTRIBUTE_ID); //$NON-NLS-1$
					return CoreModel.newContainerEntry(id, isExported);
				}

			default :
				{
					ICModelStatus status = new CModelStatus(ICModelStatus.ERROR, "CPathEntry: unknown kind (" + kindAttr + ")"); //$NON-NLS-1$ //$NON-NLS-2$
					throw new CModelException(status);
				}
		}
	}

	void encodeCPathEntries(Document doc, Element configRootElement, ICPathEntry[] entries) {
		Element element;
		IPath projectPath = getProject().getFullPath();
		for (int i = 0; i < entries.length; i++) {
			element = doc.createElement(PATH_ENTRY);
			configRootElement.appendChild(element);
			int kind = entries[i].getEntryKind();

			// Set the kind
			element.setAttribute(ATTRIBUTE_KIND, CPathEntry.kindToString(kind));

			// Save the exclusions attributes
			if (entries[i] instanceof ACPathEntry) {
				ACPathEntry entry = (ACPathEntry) entries[i];
				IPath[] exclusionPatterns = entry.getExclusionPatterns();
				if (exclusionPatterns.length > 0) {
					StringBuffer excludeRule = new StringBuffer(10);
					for (int j = 0, max = exclusionPatterns.length; j < max; j++) {
						if (j > 0) {
							excludeRule.append('|');
						}
						excludeRule.append(exclusionPatterns[j]);
					}
					element.setAttribute(ATTRIBUTE_EXCLUDING, excludeRule.toString());
				}
				if (entry.isRecursive()) {
					element.setAttribute(ATTRIBUTE_RECUSIVE, VALUE_TRUE);
				}
			}

			if (kind == ICPathEntry.CDT_SOURCE) {
				ISourceEntry source = (ISourceEntry) entries[i];
				IPath path = source.getSourcePath();
				element.setAttribute(ATTRIBUTE_PATH, path.toString());
				IPath output = source.getOutputLocation();
				if (output != null && output.isEmpty()) {
					element.setAttribute(ATTRIBUTE_OUTPUT, output.toString());
				}
			} else if (kind == ICPathEntry.CDT_LIBRARY) {
				ILibraryEntry lib = (ILibraryEntry) entries[i];
				IPath path = lib.getLibraryPath();
				element.setAttribute(ATTRIBUTE_PATH, path.toString());
				if (lib.getSourceAttachmentPath() != null) {
					element.setAttribute(ATTRIBUTE_SOURCEPATH, lib.getSourceAttachmentPath().toString());
				}
				if (lib.getSourceAttachmentRootPath() != null) {
					element.setAttribute(ATTRIBUTE_ROOTPATH, lib.getSourceAttachmentRootPath().toString());
				}
				if (lib.getSourceAttachmentPrefixMapping() != null) {
					element.setAttribute(ATTRIBUTE_PREFIXMAPPING, lib.getSourceAttachmentPrefixMapping().toString());
				}
			} else if (kind == ICPathEntry.CDT_PROJECT) {
				IProjectEntry pentry = (IProjectEntry) entries[i];
				IPath path = pentry.getProjectPath();
				element.setAttribute(ATTRIBUTE_PATH, path.toString());
			} else if (kind == ICPathEntry.CDT_INCLUDE) {
				IIncludeEntry include = (IIncludeEntry) entries[i];
				IPath path = include.getResourcePath();
				element.setAttribute(ATTRIBUTE_PATH, path.toString());
				IPath includePath = include.getIncludePath();
				element.setAttribute(ATTRIBUTE_INCLUDE, includePath.toString());
				if (include.isSystemInclude()) {
					element.setAttribute(ATTRIBUTE_SYSTEM, VALUE_TRUE);
				}
			} else if (kind == ICPathEntry.CDT_MACRO) {
				IMacroEntry macro = (IMacroEntry) entries[i];
				IPath path = macro.getResourcePath();
				element.setAttribute(ATTRIBUTE_PATH, path.toString());
				element.setAttribute(ATTRIBUTE_NAME, macro.getMacroName());
				element.setAttribute(ATTRIBUTE_VALUE, macro.getMacroValue());
			} else if (kind == ICPathEntry.CDT_CONTAINER) {
				IContainerEntry container = (IContainerEntry) entries[i];
				element.setAttribute(ATTRIBUTE_ID, container.getId());
			}
			if (entries[i].isExported()) {
				element.setAttribute(ATTRIBUTE_EXPORTED, VALUE_TRUE);
			}
		}
	}
}

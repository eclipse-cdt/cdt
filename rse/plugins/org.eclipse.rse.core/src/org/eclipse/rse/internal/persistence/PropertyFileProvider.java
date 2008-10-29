/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed the initial implementation:
 * David McKnight, David Dykstal.
 * 
 * Contributors:
 * David Dykstal (IBM) - removed printlns, printStackTrace and added logging.
 * David Dykstal (IBM) - [177882] fixed escapeValue for garbling of CJK characters
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * David Dykstal (IBM) - [188863] fix job conflict problems for save jobs, ignore bad profiles on restore
 * David Dykstal (IBM) - [189274] provide import and export operations for profiles
 * David Dykstal (IBM) - [225988] need API to mark persisted profiles as migrated
 * David Dykstal (IBM) - [252357] made nested property sets and properties embedded nodes in the persistent form
 ********************************************************************************/
package org.eclipse.rse.internal.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.core.IRSECoreStatusCodes;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.internal.core.RSECoreMessages;
import org.eclipse.rse.logging.Logger;
import org.eclipse.rse.persistence.IRSEPersistenceProvider;
import org.eclipse.rse.persistence.dom.IRSEDOMConstants;
import org.eclipse.rse.persistence.dom.RSEDOM;
import org.eclipse.rse.persistence.dom.RSEDOMNode;
import org.eclipse.rse.persistence.dom.RSEDOMNodeAttribute;

/**
 * This is class is used to restore an RSE DOM from disk and import it into RSE.
 * It stores the DOM as a tree of folders and .properties files.
 */
public class PropertyFileProvider implements IRSEPersistenceProvider, IRSEImportExportProvider {

	private static final String NULL_VALUE_STRING = "null"; //$NON-NLS-1$
	/* interesting character sets */
	private static final String VALID = "abcdefghijklmnopqrstuvwxyz0123456789-._"; //$NON-NLS-1$ 
	private static final String UPPER = "ABCDEFGHIJKLMNOPQRTSUVWXYZ"; //$NON-NLS-1$
	
	/* properties */
	
	/**
	 * anchor location - can be workspace or metadata
	 */
	private static final String P_LOCATION = "location"; //$NON-NLS-1$
	
	/* property values */
	private static final String PV_LOCATION_WORKSPACE = "workspace"; //$NON-NLS-1$
	private static final String PV_LOCATION_METADATA = "metadata"; //$NON-NLS-1$
	
	/* location names */
	private static final String LOC_PROFILES = "profiles"; //$NON-NLS-1$
	
	private Pattern period = Pattern.compile("\\."); //$NON-NLS-1$
	private Pattern suffixPattern = Pattern.compile("_(\\d+)$"); //$NON-NLS-1$
	private Pattern unicodePattern = Pattern.compile("#(\\p{XDigit}+)#"); //$NON-NLS-1$
	private Map typeQualifiers = getTypeQualifiers();
	private Map saveJobs = new HashMap();
//	private PFPersistenceAnchor anchor = null;
	private Properties properties = null;
	private String providerId = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceProvider#getSavedProfileNames()
	 */
	public String[] getSavedProfileNames() {
		String[] locationNames = getAnchor().getProfileLocationNames();
		List names = new ArrayList(locationNames.length);
		for (int i = 0; i < locationNames.length; i++) {
			String locationName = locationNames[i];
			String profileName = getNodeName(locationName);
			if (isValidName(profileName)) {
				names.add(profileName);
			}
		}
		String[] result = new String[names.size()];
		names.toArray(result);
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceProvider#deleteProfile(java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus deleteProfile(String profileName, IProgressMonitor monitor) {
		String profileLocationName = getLocationName(PFConstants.AB_PROFILE, profileName);
		IStatus result = getAnchor().deleteProfileLocation(profileLocationName, monitor);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceProvider#saveRSEDOM(org.eclipse.rse.persistence.dom.RSEDOM, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean saveRSEDOM(RSEDOM dom, IProgressMonitor monitor) {
		boolean saved = false;
		synchronized (dom) {
			String profileLocationName = getLocationName(dom);
			PFPersistenceAnchor anchor = getAnchor();
			saved = save(dom, anchor, profileLocationName, monitor);
			if (saved) {
				dom.markUpdated();
			}
		}
		return saved;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceProvider#loadRSEDOM(org.eclipse.rse.model.ISystemProfileManager, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RSEDOM loadRSEDOM(String profileName, IProgressMonitor monitor) {
		String profileLocationName = getLocationName(PFConstants.AB_PROFILE, profileName);
		PFPersistenceAnchor anchor = getAnchor();
		RSEDOM dom = load(anchor, profileLocationName, monitor);
		return dom;
	}

	/**
	 * Load a DOM from a named location within an anchor
	 * @param anchor the anchor that contained named locations for DOMs
	 * @param name the named location of that DOM
	 * @param monitor for progress reporting and cancelation
	 * @return the DOM what was loaded or null if the load failed
	 */
	private RSEDOM load(PFPersistenceAnchor anchor, String name, IProgressMonitor monitor) {
		RSEDOM dom = null;
		PFPersistenceLocation location = anchor.getProfileLocation(name);
		if (location.exists()) {
			int n = countLocations(location);
			monitor.beginTask(RSECoreMessages.PropertyFileProvider_LoadingTaskName, n);
			dom = (RSEDOM)loadNode(null, location, monitor);
			monitor.done();
		} else {
			String message = location.getLocator().toString() + " does not exist."; //$NON-NLS-1$
			getLogger().logError(message, null);
		}
		return dom;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceProvider#getSaveJob(org.eclipse.rse.persistence.dom.RSEDOM)
	 */
	public Job getSaveJob(RSEDOM dom) {
		Job saveJob = (Job) saveJobs.get(dom);
		if (saveJob == null) {
			PFPersistenceAnchor anchor = getAnchor();
			saveJob = anchor.makeSaveJob(dom, this);
			saveJobs.put(dom, saveJob);
		}
		return saveJob;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceProvider#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties properties) {
		Properties defaults = new Properties();
		defaults.setProperty(P_LOCATION, PV_LOCATION_WORKSPACE);
		this.properties = new Properties(defaults);
		Set keys = properties.keySet();
		for (Iterator z = keys.iterator(); z.hasNext();) {
			String key = (String) z.next();
			String value = properties.getProperty(key);
			if (value != null) {
				this.properties.put(key, value);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.internal.persistence.IRSEImportExportProvider#exportRSEDOM(java.io.File, org.eclipse.rse.persistence.dom.RSEDOM, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean exportRSEDOM(File folder, RSEDOM dom, IProgressMonitor monitor) {
		PFPersistenceAnchor anchor = new PFFileSystemAnchor(folder);
		String name = "DOM"; //$NON-NLS-1$
		boolean saved = save(dom, anchor, name, monitor);
		return saved;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.internal.persistence.IRSEImportExportProvider#importRSEDOM(java.io.File, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RSEDOM importRSEDOM(File folder, IProgressMonitor monitor) {
		PFPersistenceAnchor anchor = new PFFileSystemAnchor(folder);
		String name = "DOM"; //$NON-NLS-1$
		RSEDOM dom = load(anchor, name, monitor);
		return dom;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.internal.persistence.IRSEImportExportProvider#setId(java.lang.String)
	 */
	public void setId(String providerId) {
		this.providerId = providerId;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.internal.persistence.IRSEImportExportProvider#getId()
	 */
	public String getId() {
		return providerId;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceProvider#setMigratedMark(java.lang.String, boolean)
	 */
	public IStatus setMigrationMark(String profileName, boolean migrated) {
		String message = "PropertyFileProvider does not support profile migration"; //$NON-NLS-1$
		return new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, IRSECoreStatusCodes.MIGRATION_NOT_SUPPORTED, message, null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceProvider#supportsMigration()
	 */
	public boolean supportsMigration() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceProvider#getMigratedProfileNames()
	 */
	public String[] getMigratedProfileNames() {
		return new String[0];
	}
	
	/**
	 * Checks a profile name for validity. Currently all names are valid except for completely blank names.
	 * @param profileName the name to check
	 * @return true if the name is valid.
	 */
	private boolean isValidName(String profileName) {
		return (profileName.trim().length() > 0);
	}
	
	private PFPersistenceAnchor getAnchor() {
		PFPersistenceAnchor anchor = null;
		String location = properties.getProperty(P_LOCATION);
		if (location.equals(PV_LOCATION_WORKSPACE)) {
			anchor = new PFWorkspaceAnchor();
		} else if (location.equals(PV_LOCATION_METADATA)) {
			File profilesFolder = getProfilesFolder();
			anchor = new PFFileSystemAnchor(profilesFolder);
		} else {
			// if no explicit location is specified we assume the metadata location
			File profilesFolder = getProfilesFolder();
			anchor = new PFFileSystemAnchor(profilesFolder);
		}
		return anchor;
	}
	
	/**
	 * @return the folder that acts as the parent for profile folders.
	 */
	private File getProfilesFolder() {
		IPath statePath = RSECorePlugin.getDefault().getStateLocation();
		File stateFolder = new File(statePath.toOSString());
		File profilesFolder = new File(stateFolder, LOC_PROFILES);
		if (!profilesFolder.exists()) {
			profilesFolder.mkdir();
		}
		return profilesFolder;
	}
	
	/**
	 * Save a DOM
	 * @param dom the DOM to save
	 * @param anchor the anchor in which DOMs may be saved
	 * @param name the name of the saved DOM within the anchor
	 * @param monitor for progress reporting and cancelation
	 * @return true if the DOM was saved to the named location within the anchor.
	 */
	private boolean save(RSEDOM dom, PFPersistenceAnchor anchor, String name, IProgressMonitor monitor) {
		boolean saved = false;
		PFPersistenceLocation profileLocation = anchor.getProfileLocation(name);
		try {
			int n = countNodes(dom);
			monitor.beginTask(RSECoreMessages.PropertyFileProvider_SavingTaskName, n);
			saveNode(dom, profileLocation, monitor);
			monitor.done();
			saved = true;
		} catch (Exception e) {
			logException(e);
		}
		return saved;
	}

	/**
	 * Saves a node from the DOM to the file system.
	 * @param node The node to save.
	 * @param location The location in which to save this node.
	 * @param monitor The progress monitor. If the monitor has been cancel then
	 * this method will do nothing and return null.
	 */
	private void saveNode(RSEDOMNode node, PFPersistenceLocation location, IProgressMonitor monitor) {
		if (monitor.isCanceled()) return;
		location.ensure();
		Properties properties = getProperties(node, false, monitor);
		RSEDOMNode[] childNodes = node.getChildren();
		Set childNames = new HashSet();
		for (int i = 0; i < childNodes.length; i++) {
			RSEDOMNode childNode = childNodes[i];
			String index = getIndexString(i);
			if (!isNodeEmbedded(childNode)) {
				String key = combine(PFConstants.MT_REFERENCE[0], index);
				String childName = getLocationName(childNode);
				PFPersistenceLocation childLocation = location.getChild(childName);
				saveNode(childNode, childLocation, monitor);
				properties.put(key, childName);
				childNames.add(childName);
			}
		}
		location.keepChildren(childNames);
		writeProperties(properties, "RSE DOM Node", location); //$NON-NLS-1$
	}

	/**
	 * Returns the node name derived from the location name.
	 * Location names are constructed to be valid file system resource names and 
	 * node names may be arbitrary. There is a mapping between the two.
	 * @param locationName The location name from which to derive the node name.
	 * @return the node name.
	 */
	private String getNodeName(String locationName) {
		String[] parts = split(locationName, 2);
		String frozenName = parts[1];
		String result = thaw(frozenName);
		return result;
	}

	/**
	 * Returns the name of a location that can be used to store a node of a particular 
	 * type. The name is contructed to conform to the rules for file systems.
	 * The names are derived from the name and type of the node. Note that the 
	 * actual name of the node is also stored as a property so we need not attempt
	 * to recover the node name from the location name.
	 * @param node The node that will eventually be stored in this location.
	 * @return The name of the location to store this node.
	 */
	private String getLocationName(RSEDOMNode node) {
		String type = node.getType();
		type = (String) typeQualifiers.get(type);
		String name = node.getName();
		String result = getLocationName(type, name);
		return result;
	}

	/**
	 * Returns the name of a location that can be used to store a node of a particular 
	 * type. The name is contructed to conform to the rules for file systems.
	 * The names are derived from the name and type of the node. Note that the 
	 * actual name of the node is also stored as a property so we need not attempt
	 * to recover the node name from the location name.
	 * @param nodeName The node that will eventually be stored in this location.
	 * @param typeName The type abbreviation for the node at this location.
	 * @return The name of the location to store this node.
	 */
	private String getLocationName(String typeName, String nodeName) {
		String name = freeze(nodeName);
		String result = combine(typeName, name);
		return result;
	}

	/**
	 * Transforms an arbitrary name into one that can be used in any file system
	 * that supports long names. The transformation appends a number to the name
	 * that captures the case of the letters in the name. If a character falls
	 * outside the range of understood characters, it is converted to its hexadecimal unicode
	 * equivalent. Spaces are converted to underscores.
	 * @param name The name to be transformed
	 * @return The transformed name
	 * @see #thaw(String)
	 */
	private String freeze(String name) {
		int p = name.indexOf(':');
		if (p >= 0) {
			name = name.substring(p + 1);
		}
		StringBuffer buf = new StringBuffer(name.length());
		char[] chars = name.toCharArray();
		long suffix = 0;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			suffix *= 2;
			if (VALID.indexOf(c) >= 0) {
				buf.append(c);
			} else if (UPPER.indexOf(c) >= 0) { // if uppercase
				buf.append(Character.toLowerCase(c));
				suffix += 1;
			} else if (c == ' ') { // if space
				buf.append('_');
				suffix += 1;
			} else { // if out of range
				buf.append('#');
				buf.append(Integer.toHexString(c));
				buf.append('#');
			}
		}
		name = buf.toString() + "_" + Long.toString(suffix); //$NON-NLS-1$
		return name;
	}

	/**
	 * Recovers an arbitrary name from its frozen counterpart.
	 * @param name The name to be transformed
	 * @return The transformed name
	 * @see #freeze(String)
	 */
	private String thaw(String name) {
		String result = name;
		Matcher m = suffixPattern.matcher(name);
		if (m.find()) {
			String root = name.substring(0, m.start());
			String suffix = m.group(1);
			long caseCode = Long.parseLong(suffix);
			root = thawUnicode(root);
			root = thawCase(root, caseCode);
			result = root;
		}
		return result;
	}

	private String thawUnicode(String name) {
		Matcher m = unicodePattern.matcher(name);
		StringBuffer b = new StringBuffer();
		int p0 = 0;
		while (m.find()) {
			int p1 = m.start();
			String chunk0 = name.substring(p0, p1);
			String digits = m.group(1);
			int codePoint = Integer.valueOf(digits, 16).intValue();
			char ch = (char) codePoint;
			String chunk1 = Character.toString(ch);
			b.append(chunk0);
			b.append(chunk1);
			p0 = m.end();
		}
		b.append(name.substring(p0));
		String result = b.toString();
		return result;
	}

	private String thawCase(String name, long caseCode) {
		StringBuffer b = new StringBuffer();
		char[] chars = name.toCharArray();
		for (int i = chars.length - 1; i >= 0; i--) {
			char ch = chars[i];
			boolean shift = (caseCode & 1L) == 1;
			if (shift) {
				ch = (ch == '_') ? ' ' : Character.toUpperCase(ch);
			}
			b.append(ch);
			caseCode = caseCode >> 1;
		}
		String result = b.reverse().toString();
		return result;
	}

	private Map getTypeQualifiers() {
		Map typeQualifiers = new HashMap();
		typeQualifiers.put(IRSEDOMConstants.TYPE_CONNECTOR_SERVICE, PFConstants.AB_CONNECTOR_SERVICE);
		typeQualifiers.put(IRSEDOMConstants.TYPE_FILTER, PFConstants.AB_FILTER);
		typeQualifiers.put(IRSEDOMConstants.TYPE_FILTER_POOL, PFConstants.AB_FILTER_POOL);
		typeQualifiers.put(IRSEDOMConstants.TYPE_FILTER_POOL_REFERENCE, PFConstants.AB_FILTER_POOL_REFERENCE);
		typeQualifiers.put(IRSEDOMConstants.TYPE_FILTER_STRING, PFConstants.AB_FILTER_STRING);
		typeQualifiers.put(IRSEDOMConstants.TYPE_HOST, PFConstants.AB_HOST);
		typeQualifiers.put(IRSEDOMConstants.TYPE_PROFILE, PFConstants.AB_PROFILE);
		typeQualifiers.put(IRSEDOMConstants.TYPE_PROPERTY, PFConstants.AB_PROPERTY);
		typeQualifiers.put(IRSEDOMConstants.TYPE_PROPERTY_SET, PFConstants.AB_PROPERTY_SET);
		typeQualifiers.put(IRSEDOMConstants.TYPE_SERVER_LAUNCHER, PFConstants.AB_SERVICE_LAUNCHER);
		typeQualifiers.put(IRSEDOMConstants.TYPE_SUBSYSTEM, PFConstants.AB_SUBSYSTEM);
		return typeQualifiers;
	}

	/**
	 * Write a set of properties to a location.
	 * @param properties The Properties object to write.
	 * @param header The header to include in the location contents.
	 * @param location The PersistenceLocation which will contain the properties.
	 * @param monitor The progress monitor.
	 */
	private void writeProperties(Properties properties, String header, PFPersistenceLocation location) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(500);
		PrintWriter out = new PrintWriter(outStream);
		out.println("# " + header); //$NON-NLS-1$
		Map map = new TreeMap(properties);
		Set keys = map.keySet();
		for (Iterator z = keys.iterator(); z.hasNext();) {
			String key = (String) z.next();
			String value = (String) map.get(key);
			String keyvalue = key + "=" + escapeValue(value); //$NON-NLS-1$
			out.println(keyvalue);
		}
		out.close();
		ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
		location.setContents(inStream);
	}

	/**
	 * Tests if a node's definition should be embedded in its parent's definition or
	 * if it should be a separate definition. The test is usually based on node type.
	 * @param node The node to be tested.
	 * @return true if the node is to be embedded.
	 */
	private boolean isNodeEmbedded(RSEDOMNode node) {
		String nodeType = node.getType();
		if (nodeType.equals(IRSEDOMConstants.TYPE_FILTER)) return true;
		if (nodeType.equals(IRSEDOMConstants.TYPE_CONNECTOR_SERVICE)) return true;
		if (nodeType.equals(IRSEDOMConstants.TYPE_PROPERTY)) return true;
		if (nodeType.equals(IRSEDOMConstants.TYPE_PROPERTY_SET)) {
			RSEDOMNode parent = node.getParent();
			String parentType = parent.getType();
			if (parentType.equals(IRSEDOMConstants.TYPE_PROPERTY_SET)) return true;
		}
		return false;
	}

	/**
	 * Transforms an integer into its five digit counterpart complete with leading 
	 * zeroes.
	 * @param i an integer from 0 to 99999.
	 * @return a string equivalent from "00000" to "99999"
	 */
	private String getIndexString(int i) {
		assert (i >= 0 && i <= 99999);
		String index = "00000" + Integer.toString(i); //$NON-NLS-1$
		index = index.substring(index.length() - 5);
		return index;
	}

	/**
	 * "Fixes" a value. Values in Properties objects may not be null. Changes all
	 * null values to the string "null" and escapes the characters in other values.
	 * @param value The value to check.
	 * @return The fixed value
	 */
	private String fixValue(String value) {
		if (value == null) return NULL_VALUE_STRING;
		return value;
	}

	/**
	 * Escapes the characters in the supplied string according to the rules
	 * for properties files.
	 * @param value The value to examine.
	 * @return The equivalent value with escapes.
	 */
	private String escapeValue(String value) {
		StringBuffer buffer = new StringBuffer(value.length() + 20);
		char[] characters = value.toCharArray();
		for (int i = 0; i < characters.length; i++) {
			char c = characters[i];
			if (c == '\\') {
				buffer.append("\\\\"); //$NON-NLS-1$
			} else if (c == '\t') {
				buffer.append("\\t"); //$NON-NLS-1$
			} else if (c == '\f') {
				buffer.append("\\f"); //$NON-NLS-1$
			} else if (c == '\n') {
				buffer.append("\\n"); //$NON-NLS-1$
			} else if (c == '\r') {
				buffer.append("\\r"); //$NON-NLS-1$
			} else if ((c < '\u0020' || c > '\u007E')) {
				String cString = "0000" + Integer.toHexString(c); //$NON-NLS-1$
				cString = cString.substring(cString.length() - 4);
				cString = "\\u" + cString; //$NON-NLS-1$
				buffer.append(cString);
			} else if ("=!#:".indexOf(c) >= 0) { //$NON-NLS-1$
				buffer.append('\\');
				buffer.append(c);
			} else {
				buffer.append(c);
			}
		}
		return buffer.toString();
	}

	/**
	 * Constructs a properties object containing all the properties for this node.
	 * The following properties exist:
	 * name property, type property, embedded child properties, and referenced 
	 * child properties. Each property has its own key format in the Properties
	 * object.  
	 * @param node The node to extract the properties from.
	 * @param force Force children to be embedded rather than reference.
	 * @param monitor The progress monitor. The work count is increased by one each time
	 * this method is invoked.
	 * @return The Properties object containing that node definition.
	 */
	private Properties getProperties(RSEDOMNode node, boolean force, IProgressMonitor monitor) {
		Properties properties = new Properties();
		properties.put(PFConstants.MT_NODE_NAME[0], node.getName());
		properties.put(PFConstants.MT_NODE_TYPE[0], node.getType());
		properties.putAll(getAttributes(node));
		RSEDOMNode[] children = node.getChildren();
		for (int i = 0; i < children.length; i++) {
			RSEDOMNode child = children[i];
			String index = getIndexString(i);
			if (force || isNodeEmbedded(child)) {
				String prefix = combine(PFConstants.MT_CHILD[0], index);
				Properties childProperties = getProperties(child, true, monitor);
				Enumeration e = childProperties.keys();
				while (e.hasMoreElements()) {
					String key = (String) e.nextElement();
					String value = childProperties.getProperty(key);
					String newKey = combine(prefix, key);
					properties.put(newKey, value);
				}
			}
		}
		monitor.worked(1);
		return properties;
	}

	/**
	 * Constructs a Properties object from the attributes present in a DOM node.
	 * @param node The node containing the attributes. Keys for attributes are of the
	 * form "attribute.[attribute-name]". If the attribute has a type then there
	 * will also be an "attribute-type.[attribute-name]" property.
	 * [attribute-name] may contain periods but must not be null.
	 * @return The newly constructed Properties object.
	 */
	private Properties getAttributes(RSEDOMNode node) {
		Properties properties = new Properties();
		RSEDOMNodeAttribute[] attributes = node.getAttributes();
		for (int i = 0; i < attributes.length; i++) {
			RSEDOMNodeAttribute attribute = attributes[i];
			String attributeName = attribute.getKey();
			String propertyKey = combine(PFConstants.MT_ATTRIBUTE[0], attributeName);
			properties.put(propertyKey, fixValue(attribute.getValue()));
			String attributeType = attribute.getType();
			if (attributeType != null) {
				propertyKey = combine(PFConstants.MT_ATTRIBUTE_TYPE[0], attributeName);
				properties.put(propertyKey, attributeType);
			}
		}
		return properties;
	}

	/**
	 * Count the number of nodes in a tree rooted in the supplied node. The
	 * supplied node is counted so the minimum result is one.
	 * @param node The root of the tree.
	 * @return The node count.
	 */
	private int countNodes(RSEDOMNode node) {
		RSEDOMNode[] children = node.getChildren();
		int result = 1;
		for (int i = 0; i < children.length; i++) {
			RSEDOMNode child = children[i];
			result += countNodes(child);
		}
		return result;
	}

	/**
	 * Counts the number of locations that have contents in this location and below.
	 * This provides a lower bound to the number of nodes that have to be created from this 
	 * persistent form of a DOM.
	 * @param location
	 * @return the number of locations that have contents.
	 */
	private int countLocations(PFPersistenceLocation location) {
		int result = 0;
		if (location.hasContents()) {
			result += 1;
		}
		PFPersistenceLocation[] children = location.getChildren();
		for (int i = 0; i < children.length; i++) {
			PFPersistenceLocation child = children[i];
			result += countLocations(child);
		}
		return result;
	}
	
	/**
	 * Loads a node from a location.
	 * @param parent The parent of the node to be created. If null then this node is assumed to
	 * be a DOM root node (RSEDOM).
	 * @param nodeLocation The location in which the contents of this node is found.
	 * @param monitor The monitor used to report progress and cancelation. If the monitor is
	 * in cancelled state then it this does method does nothing and returns null. If the monitor
	 * is not cancelled then its work count is incremented by one.
	 * @return The newly loaded node.
	 */
	private RSEDOMNode loadNode(RSEDOMNode parent, PFPersistenceLocation nodeLocation, IProgressMonitor monitor) {
		RSEDOMNode node = null;
		if (!monitor.isCanceled()) {
			Properties properties = loadProperties(nodeLocation);
			if (properties != null) {
				node = makeNode(parent, nodeLocation, properties, monitor);
			}
			monitor.worked(1);
		}
		return node;
	}

	/**
	 * Loads the properties found in the location. Returns null if no properties
	 * were found.
	 * @param location The location in which to look for properties.
	 * @return The Properties object.
	 */
	private Properties loadProperties(PFPersistenceLocation location) {
		Properties properties = null;
		if (location.hasContents()) {
			properties = new Properties();
			InputStream inStream = location.getContents();
			try {
				properties.load(inStream);
				inStream.close();
			} catch (IOException e) {
				logException(e);
			}
		}
		return properties;
	}

	/**
	 * Makes a new RSEDOMNode from a set of properties. The properties must (at least) include
	 * a "name" property and a "type" property. Any child nodes are created and attached as well.
	 * @param parent The parent node of the node to be created.
	 * @param location The location in which referenced child folders can be found.
	 * @param properties The properties from which to create the node.
	 * @param monitor a monitor to support cancellation and progress reporting.
	 * @return the newly created DOM node and its children.
	 */
	private RSEDOMNode makeNode(RSEDOMNode parent, PFPersistenceLocation location, Properties properties, IProgressMonitor monitor) {
		String nodeType = getProperty(properties, PFConstants.MT_NODE_TYPE);
		String nodeName = getProperty(properties, PFConstants.MT_NODE_NAME);
		RSEDOMNode node = (parent == null) ? new RSEDOM(nodeName) : new RSEDOMNode(parent, nodeType, nodeName);
		node.setRestoring(true);
		Set keys = properties.keySet();
		Map attributes = new HashMap();
		Map attributeTypes = new HashMap();
		Map childPropertiesMap = new HashMap();
		Set childNames = new TreeSet(); // child names are 5 digit strings, a tree set is used to maintain ordering
		Set referenceKeys = new TreeSet(); // ditto for reference keys, "<reference-metatype>.<index>"
		// since the properties are in no particular  order, we make a first pass to gather info on what's there
		for (Iterator z = keys.iterator(); z.hasNext();) {
			String key = (String) z.next();
			String[] words = split(key, 2);
			String metatype = words[0];
			if (find(metatype, PFConstants.MT_ATTRIBUTE)) {
				String value = properties.getProperty(key);
				attributes.put(words[1], value);
			} else if (find(metatype, PFConstants.MT_ATTRIBUTE_TYPE)) {
				String type = properties.getProperty(key);
				attributeTypes.put(words[1], type);
			} else if (find(metatype, PFConstants.MT_REFERENCE)) {
				referenceKeys.add(key);
			} else if (find(metatype, PFConstants.MT_CHILD)) {
				String value = properties.getProperty(key);
				words = split(words[1], 2);
				String childName = words[0];
				childNames.add(childName);
				String newKey = words[1];
				Properties p = getProperties(childPropertiesMap, childName);
				p.put(newKey, value);
			}
		}
		Set attributeNames = attributes.keySet();
		for (Iterator z = attributeNames.iterator(); z.hasNext();) {
			String attributeName = (String) z.next();
			String attributeValue = (String) attributes.get(attributeName);
			if (attributeValue.equals(NULL_VALUE_STRING)) attributeValue = null;
			String attributeType = (String) attributeTypes.get(attributeName);
			node.addAttribute(attributeName, attributeValue, attributeType);
		}
		for (Iterator z = childNames.iterator(); z.hasNext();) {
			String childName = (String) z.next();
			Properties p = getProperties(childPropertiesMap, childName);
			makeNode(node, location, p, monitor);
		}
		for (Iterator z = referenceKeys.iterator(); z.hasNext();) {
			String key = (String) z.next();
			String childLocationName = properties.getProperty(key);
			PFPersistenceLocation childLocation = location.getChild(childLocationName);
			loadNode(node, childLocation, monitor);
		}
		node.setRestoring(false);
		return node;
	}

	/**
	 * Gets a property given a "multi-key"
	 * @param properties The properties object to search
	 * @param keys the "multi-key" for that property
	 * @return The first property found using one of the elements of the multi-key.
	 */
	private String getProperty(Properties properties, String[] keys) {
		String result = null;
		for (int i = 0; i < keys.length && result == null; i++) {
			String key = keys[i];
			result = properties.getProperty(key);
		}
		return result;
	}

	/**
	 * Finds a key (needle) in an array of values (the haystack)
	 * @param needle The value to look for
	 * @param haystack the values to search
	 * @return true if the value was found
	 */
	private boolean find(String needle, String[] haystack) {
		for (int i = 0; i < haystack.length; i++) {
			String value = haystack[i];
			if (value.equals(needle)) return true;
		}
		return false;
	}

	/**
	 * Returns a Properties object from the given Map that holds them using the 
	 * selector String. Creates a new Properties object and places it in the map
	 * if one does not exist for the selector.
	 * @param propertiesMap The map in which to look for Properties objects.
	 * @param selector The name of the Properties object
	 * @return a Properties object.
	 */
	private Properties getProperties(Map propertiesMap, String selector) {
		Properties p = (Properties) propertiesMap.get(selector);
		if (p == null) {
			p = new Properties();
			propertiesMap.put(selector, p);
		}
		return p;
	}

	/**
	 * Convenience method to combine two names into one. The individual names in the
	 * combined name are separated by periods.
	 * @param typeName The first name.
	 * @param nodeName The second name
	 * @return the combined name.
	 */
	private String combine(String typeName, String nodeName) {
		return combine(new String[] { typeName, nodeName });
	}

	/**
	 * The generic method for creating a qualified name from a string of segments.
	 * The individual names are separated by periods.
	 * @param names The names to combine
	 * @return The combined name.
	 */
	private String combine(String[] names) {
		StringBuffer buf = new StringBuffer(100);
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			if (i > 0) buf.append('.');
			buf.append(name);
		}
		return buf.toString();
	}

	/**
	 * Splits a combined name into its component parts. The period is used as the name 
	 * separator. If a limit > 0 is specified the return value will contain at most that
	 * number of segments. The last segment may, in fact, be split some more.
	 * @param longName The name to be split
	 * @param limit The number of parts to split the name into.
	 * @return The parts of the name.
	 */
	private String[] split(String longName, int limit) {
		return period.split(longName, limit);
	}

	private Logger getLogger() {
		Logger logger = RSECorePlugin.getDefault().getLogger();
		return logger;
	}

	private void logException(Exception e) {
		getLogger().logError("unexpected exception", e); //$NON-NLS-1$
	}

}

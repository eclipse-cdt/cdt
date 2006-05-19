/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed the initial implementation:
 * David McKnight, David Dykstal.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Pattern;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.rse.core.SystemResourceManager;
import org.eclipse.rse.persistence.dom.IRSEDOMConstants;
import org.eclipse.rse.persistence.dom.RSEDOM;
import org.eclipse.rse.persistence.dom.RSEDOMNode;
import org.eclipse.rse.persistence.dom.RSEDOMNodeAttribute;

/**
 * This is class is used to restore an RSE DOM from disk and import it into RSE.
 * It stores the DOM as a tree of folders and .properties files.
 */
public class PropertyFileProvider implements IRSEPersistenceProvider {
	
	private static final String NULL_VALUE_STRING = "null";
	private static final String PROPERTIES_FILE_NAME = "node.properties";

	/* Metatype names */
	private static final String MT_ATTRIBUTE_TYPE = "attribute-type";
	private static final String MT_ATTRIBUTE = "attribute";
	private static final String MT_CHILD = "child";
	private static final String MT_NODE_TYPE = "node-type";
	private static final String MT_NODE_NAME = "node-name";
	private static final String MT_REFERENCE = "reference";

	/* Type abbreviations */
	private static final String AB_SUBSYSTEM = "SS";
	private static final String AB_SERVICE_LAUNCHER = "SL";
	private static final String AB_PROPERTY_SET = "PS";
	private static final String AB_PROPERTY = "P";
	private static final String AB_HOST = "H";
	private static final String AB_FILTER_STRING = "FS";
	private static final String AB_FILTER_POOL_REFERENCE = "FPR";
	private static final String AB_FILTER_POOL = "FP";
	private static final String AB_FILTER = "F";
	private static final String AB_CONNECTOR_SERVICE = "CS";
	private static final String AB_PROFILE = "PRF";
	
	private Pattern period = Pattern.compile("\\.");
	private Map typeQualifiers = getTypeQualifiers();

	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceProvider#getSavedProfileNames()
	 */
	public String[] getSavedProfileNames() {
		List names = new Vector(10);
		IFolder providerFolder = getProviderFolder();
		try {
			IResource[] profileCandidates = providerFolder.members();
			for (int i = 0; i < profileCandidates.length; i++) {
				IResource profileCandidate = profileCandidates[i];
				if (profileCandidate.getType() == IResource.FOLDER) {
					String candidateName = profileCandidate.getName();
					String[] parts = split(candidateName, 2);
					if (parts[0].equals(AB_PROFILE)) {
						String name = parts[1];
						names.add(name);
					}
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] result = new String[names.size()];
		names.toArray(result);
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceProvider#saveRSEDOM(org.eclipse.rse.persistence.dom.RSEDOM, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean saveRSEDOM(RSEDOM dom, IProgressMonitor monitor) {
		String profileName = dom.getName();
		IFolder providerFolder = getProviderFolder();
		System.out.println("saving profile " + profileName + " to " + providerFolder.getFullPath().toString() + "..."); // TODO: dwd debugging
		try {
			int n = countNodes(dom);
			if (monitor != null) monitor.beginTask("Saving DOM", n);
			saveNode(dom, providerFolder, monitor);
			if (monitor != null) monitor.done();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Saves a node from the DOM to the file system.
	 * @param node The node to save.
	 * @param parentFolder The folder in which to save this node. The node will be a
	 * subfolder of this folder.
	 * @param monitor The progress monitor. If the monitor has been cancel then
	 * this method will do nothing and return null.
	 * @return The name of the folder saving this node. Can be used for constructing 
	 * references to children. May return null if the monitor has been canceled.
	 */
	private String saveNode(RSEDOMNode node, IFolder parentFolder, IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled()) return null;
		String nodeFolderName = getSaveFolderName(node);
		IFolder nodeFolder = getFolder(parentFolder, nodeFolderName);
		Properties properties = getProperties(node, false, monitor);
		RSEDOMNode[] children = node.getChildren();
		Set childFolderNames = new HashSet();
		for (int i = 0; i < children.length; i++) {
			RSEDOMNode child = children[i];
			String index = getIndexString(i);
			if (!isNodeEmbedded(child)) {
				String key = combine(MT_REFERENCE, index);
				String childFolderName = saveNode(child, nodeFolder, monitor);
				if (childFolderName != null) properties.put(key, childFolderName);
				childFolderNames.add(childFolderName);
			}
		}
		removeFolders(nodeFolder, childFolderNames);
		String propertiesFileName = PROPERTIES_FILE_NAME;
		IFile propertiesFile = nodeFolder.getFile(propertiesFileName);
		writeProperties(properties, "RSE DOM Node", propertiesFile);
		return nodeFolderName;
	}
	
	/**
	 * Removes childFolders from the parent folder that are not in the keep set.
	 * Typically used to clean renamed nodes from the tree on a save operation.
	 * @param parentFolder The folder whose subfolders are to be examined.
	 * @param keepSet The names of the folders that should be kept. Others are discarded.
	 */
	private void removeFolders(IFolder parentFolder, Set keepSet) {
		try {
			IResource[] children = parentFolder.members();
			for (int i = 0; i < children.length; i++) {
				IResource child = children[i];
				if (child.getType() == IResource.FOLDER) {
					String childFolderName = child.getName();
					if (!keepSet.contains(childFolderName)) {
						child.delete(true, null);
					}
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Returns the name of a folder that can be used to store a node of a particular 
	 * type. Since this is a folder, its name must conform to the rules of the file
	 * system. The names are derived from the name and type of the node. Note that the 
	 * actual name of the node is also stored as a property so we need not attempt
	 * to recover the node name from this name.
	 * @param node The node that will eventually be stored in this folder.
	 * @return The name of the folder to store this node.
	 */
	private String getSaveFolderName(RSEDOMNode node) {
		String type = node.getType();
		type = (String) typeQualifiers.get(type);
		String name = node.getName();
		int i = name.indexOf(':');
		if (i >= 0) {
			name = name.substring(i + 1);
		}
		String result = combine(type, name);
		return result;
	}
	
	private Map getTypeQualifiers() {
		Map typeQualifiers = new HashMap();
		typeQualifiers.put(IRSEDOMConstants.TYPE_CONNECTOR_SERVICE, AB_CONNECTOR_SERVICE);
		typeQualifiers.put(IRSEDOMConstants.TYPE_FILTER, AB_FILTER);
		typeQualifiers.put(IRSEDOMConstants.TYPE_FILTER_POOL, AB_FILTER_POOL);
		typeQualifiers.put(IRSEDOMConstants.TYPE_FILTER_POOL_REFERENCE, AB_FILTER_POOL_REFERENCE);
		typeQualifiers.put(IRSEDOMConstants.TYPE_FILTER_STRING, AB_FILTER_STRING);
		typeQualifiers.put(IRSEDOMConstants.TYPE_HOST, AB_HOST);
		typeQualifiers.put(IRSEDOMConstants.TYPE_PROFILE, AB_PROFILE);
		typeQualifiers.put(IRSEDOMConstants.TYPE_PROPERTY, AB_PROPERTY);
		typeQualifiers.put(IRSEDOMConstants.TYPE_PROPERTY_SET, AB_PROPERTY_SET);
		typeQualifiers.put(IRSEDOMConstants.TYPE_SERVER_LAUNCHER, AB_SERVICE_LAUNCHER);
		typeQualifiers.put(IRSEDOMConstants.TYPE_SUBSYSTEM, AB_SUBSYSTEM);
		return typeQualifiers;
	}

	/**
	 * Write a set of properties to a file.
	 * @param properties The Properties object to write.
	 * @param header The header to include in the properties file.
	 * @param file The IFile which will contain the properties.
	 * @param monitor The progress monitor.
	 */
	private void writeProperties(Properties properties, String header, IFile file) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(500);
		PrintWriter out = new PrintWriter(outStream);
		out.println("# " + header);
		Map map = new TreeMap(properties);
		Set keys = map.keySet();
		for (Iterator z = keys.iterator(); z.hasNext();) {
			String key = (String) z.next();
			String value = (String)map.get(key);
			out.println(key + "=" + value);
		}
		out.close();
		ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
		try {
			if (!file.exists()) {
				file.create(inStream, true, null);
			} else {
				file.setContents(inStream, true, true, null);
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Tests if a node's definition should be embedded in its parent's definition or
	 * if it should be a separate definition. The test is usually based on node type.
	 * Currently only filter strings are embedded in their parent filter definition.
	 * @param node The node to be tested.
	 * @return true if the node is to be embedded.
	 */
	private boolean isNodeEmbedded(RSEDOMNode node) {
		boolean result = false;
		if (node.getType().equals(IRSEDOMConstants.TYPE_FILTER_STRING)) {
			result = true;
		}
		return result;
	}

	/**
	 * Transforms an integer into its five digit counterpart complete with leading 
	 * zeroes.
	 * @param i an integer from 0 to 99999.
	 * @return a string equivalent from "00000" to "99999"
	 */
	private String getIndexString(int i) {
		if (i < 0 || i > 99999) throw new IllegalArgumentException("Argument must be between 0 and 99999");
		String index = "00000" + Integer.toString(i);
		index = index.substring(index.length() - 5);
		return index;
	}

	/**
	 * "Fixes" a value. Values in Properties objects may not be null. Changes all
	 * null values to the string "null" and returns other values unaltered.
	 * @param value the value to check.
	 * @return The fixed value
	 */
	private String fixValue(String value) {
		if (value == null) return NULL_VALUE_STRING;
		return value;
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
		properties.put(MT_NODE_NAME, fixValue(node.getName()));
		properties.put(MT_NODE_TYPE, fixValue(node.getType()));
		properties.putAll(getAttributes(node));
		RSEDOMNode[] children = node.getChildren();
		for (int i = 0; i < children.length; i++) {
			RSEDOMNode child = children[i];
			String index = getIndexString(i);
			if (force || isNodeEmbedded(child)) {
				String prefix = combine(MT_CHILD, index);
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
		if (monitor != null) monitor.worked(1);
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
			String propertyKey = combine(MT_ATTRIBUTE, attributeName);
			properties.put(propertyKey, fixValue(attribute.getValue()));
			String attributeType = attribute.getType();
			if (attributeType != null) {
				propertyKey = combine(MT_ATTRIBUTE_TYPE, attributeName);
				properties.put(propertyKey, attributeType);
			} 
		}
		return properties;
	}
	
	/**
	 * Count the number of nodes in a tree rooted in the supplied node. The
	 * supplied node is counted so the mininum result is one.
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceProvider#loadRSEDOM(org.eclipse.rse.model.ISystemProfileManager, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RSEDOM loadRSEDOM(String profileName, IProgressMonitor monitor) {
		RSEDOM dom = null;
		IFolder profileFolder = getProfileFolder(profileName);
		if (profileFolder.exists()) {
			System.out.println("loading from " + profileFolder.getFullPath().toString() + "..."); // TODO: dwd debugging
			int n = countPropertiesFiles(profileFolder);
			if (monitor != null) monitor.beginTask("Loading DOM", n);
			dom = (RSEDOM) loadNode(null, profileFolder, monitor);
			if (monitor != null) monitor.done();
		} else {
			System.out.println(profileFolder.getFullPath().toString() + " does not exist.");
		}
		return dom;
	}

	/**
	 * Counts the number of properties files in this folder and below. This provides
	 * a lower bound to the number of nodes that have to be created from this 
	 * persistent form of a DOM.
	 * @param folder
	 * @return the number of properties files found.
	 */
	private int countPropertiesFiles(IFolder folder) {
		int result = 0;
		IFile propertiesFile = folder.getFile(PROPERTIES_FILE_NAME);
		if (propertiesFile.exists()) {
			result += 1;
			try {
				IResource[] members = folder.members();
				for (int i = 0; i < members.length; i++) {
					IResource member = members[i];
					if (member.getType() == IResource.FOLDER) {
						IFolder childFolder = (IFolder) member;
						result += countPropertiesFiles(childFolder);
					}
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Loads a node from a folder.
	 * @param parent The parent of the node to be created. If null then this node is assumed to
	 * be a DOM root node (RSEDOM).
	 * @param nodeFolder The folder in which the node.properties file of this node is found.
	 * @param monitor The monitor used to report progress and cancelation. If the monitor is
	 * in canceled state then it this does method does nothing and returns null. If the monitor
	 * is not canceled then its work count is incremented by one.
	 * @return The newly loaded node.
	 */
	private RSEDOMNode loadNode(RSEDOMNode parent, IFolder nodeFolder, IProgressMonitor monitor) {
		RSEDOMNode node = null;
		if (monitor == null || !monitor.isCanceled()) {
			Properties properties = loadProperties(nodeFolder);
			if (properties != null) {
				node = makeNode(parent, nodeFolder, properties, monitor);
			}
			if (monitor != null) monitor.worked(1);
		}
		return node;
	}
	
	/**
	 * Loads the properties found in the folder. Returns null if no properties
	 * file was found.
	 * @param folder The folder in which to look for properties.
	 * @return The Properties object.
	 */
	private Properties loadProperties(IFolder folder) {
		Properties properties = null;
		IFile attributeFile = folder.getFile(PROPERTIES_FILE_NAME);
		if (attributeFile.exists()) {
			properties = new Properties();
			try {
				InputStream inStream = attributeFile.getContents();
				try {
					properties.load(inStream);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return properties;
	}
	
	/**
	 * Makes a new RSEDOMNode from a set of properties. The properties must (at least) include
	 * a "name" property and a "type" property. Any child nodes are created and attached as well.
	 * @param parent The parent node of the node to be created.
	 * @param nodeFolder The folder in which referenced child folders can be found. This will
	 * almost always be the folder in which the properties for the node to be created were found.
	 * @param properties The properties from which to create the node.
	 * @param monitor a monitor to support cancelation and progress reporting.
	 * @return the newly created DOM node and its children.
	 */
	private RSEDOMNode makeNode(RSEDOMNode parent, IFolder nodeFolder, Properties properties, IProgressMonitor monitor) {
		String nodeType = properties.getProperty(MT_NODE_TYPE);
		String nodeName = properties.getProperty(MT_NODE_NAME);
		RSEDOMNode node = (parent == null) ? new RSEDOM(nodeName) : new RSEDOMNode(parent, nodeType, nodeName);
		node.setRestoring(true);
		Set keys = properties.keySet();
		int nReferences = 0;
		int nChildren = 0;
		Map attributes = new HashMap();
		Map attributeTypes = new HashMap();
		Map childPropertiesMap = new HashMap();
		for (Iterator z = keys.iterator(); z.hasNext();) {
			String key = (String) z.next();
			String[] words = split(key, 3);
			String metatype = words[0];
			if (metatype.equals(MT_ATTRIBUTE)) {
				String value = properties.getProperty(key);
				String name = words[1];
				attributes.put(name, value);
			} else if (metatype.equals(MT_ATTRIBUTE_TYPE)) {
				String type = properties.getProperty(key);
				String name = words[1];
				attributeTypes.put(name, type);
			} else if (metatype.equals(MT_REFERENCE)) {
				int n = Integer.parseInt(words[1]) + 1;
				if (nReferences < n) nReferences = n;
			} else if (metatype.equals(MT_CHILD)) {
				String value = properties.getProperty(key);
				String indexString = words[1];
				int n = Integer.parseInt(indexString) + 1;
				if (nChildren < n) nChildren = n;
				Properties p = getProperties(childPropertiesMap, indexString);
				String newKey = words[2]; 
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
		for (int i = 0; i < nChildren; i++) {
			String selector = getIndexString(i);
			Properties p = getProperties(childPropertiesMap, selector);
			makeNode(node, nodeFolder, p, monitor);
		}
		for (int i = 0; i < nReferences; i++) {
			String selector = getIndexString(i);
			String key = combine(MT_REFERENCE, selector);
			String childFolderName = properties.getProperty(key);
			IFolder childFolder = getFolder(nodeFolder, childFolderName);
			loadNode(node, childFolder, monitor);
		}
		node.setRestoring(false);
		return node;
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
		Properties p = (Properties)propertiesMap.get(selector);
		if (p == null) {
			p = new Properties();
			propertiesMap.put(selector, p);
		}
		return p;
	}

	/**
	 * Returns the IFolder in which this persistence provider stores its profiles.
	 * This will create the folder if the folder was not found.
	 * @return The folder that was created or found.
	 */
	private IFolder getProviderFolder() {
		IProject project = SystemResourceManager.getRemoteSystemsProject();
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null); 
		} catch (Exception e) {
		}
		IFolder providerFolder = getFolder(project, "org.eclipse.rse.dom.properties");
		return providerFolder;
	}
	
	/**
	 * Returns the IFolder in which a profile is stored. 
	 * @return The folder that was created or found.
	 */
	private IFolder getProfileFolder(String profileName) {
		String profileFolderName = combine(AB_PROFILE, profileName);
		IFolder providerFolder = getProviderFolder();
		IFolder profileFolder = getFolder(providerFolder, profileFolderName);
		return profileFolder;
	}
	
	/**
	 * Returns the specified folder of the parent container. If the folder does
	 * not exist it creates it.
	 * @param parent the parent container - typically a project or folder
	 * @param name the name of the folder to find or create
	 * @return the found or created folder
	 */
	private IFolder getFolder(IContainer parent, String name) {
		IPath path = new Path(name);
		IFolder folder = parent.getFolder(path);
		if (!folder.exists()) {
			try {
				folder.create(IResource.NONE, true, null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return folder;
	}
	
	/**
	 * Convenience method to combine two names into one. The individual names in the
	 * combined name are separated by periods.
	 * @param typeName The first name.
	 * @param nodeName The second name
	 * @return the combined name.
	 */
	private String combine(String typeName, String nodeName) {
		return combine(new String[] {typeName, nodeName});
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
}
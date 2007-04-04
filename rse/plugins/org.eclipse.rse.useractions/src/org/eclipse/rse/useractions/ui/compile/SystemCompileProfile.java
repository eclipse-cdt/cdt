/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [180562][api] dont implement ISystemCompileXMLConstants
 *******************************************************************************/

package org.eclipse.rse.useractions.ui.compile;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemResourceHelpers;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * A SystemCompileProfile has a one-to-one correspondence with a SystemProfile. There is one
 *  for each profile, for each subsystem factory that supports compiles.
 * <p>
 * The compile profile manages all aspects of the compile framework for this subsystem factory,
 *  for this system profile. Underneath, this basically means managing the xml file where the
 *  compile information is stored.
 * <p>
 * At a high level, a SystemCompileProfile manages a list of {@link SystemCompileType} objects, 
 *  of which there is one per compilable source type. Given a raw source type like ".cpp" there 
 *  is a method {@link #getCompileType(String)} to return the SystemCompileType object for it. 
 *  From that, one can get a list of compile commands registered for that type, and the 
 *  last-used compile command for that type.
 */
public abstract class SystemCompileProfile {
	private SystemCompileManager parentManager;
	//private SystemProfile systemProfile;	
	private String profileName;
	private Vector compileTypes;
	private String[] srcTypes;
	private boolean isRead;
	private Object associatedData;

	/**
	 * Constructor for SystemCompileProfile
	 * Will automatically read from disk.
	 * @param manager - the SystemCompileManager which instantiated this
	 * @param profileName - the name for this profile. 
	 */
	public SystemCompileProfile(SystemCompileManager manager, String profileName) {
		super();
		this.parentManager = manager;
		this.profileName = profileName;
		//this.systemProfile = profile;
		doPreRead();
		readFromDisk();
	}

	// -----------------
	// PUBLIC METHODS...
	// -----------------
	/**
	 * Reset the profile name, on a profile rename operation, say.
	 */
	public void setProfileName(String name) {
		//System.out.println("Inside SystemCompileProfile#setProfileName. Old = " + profileName + ", New = " + name);
		this.profileName = name;
	}

	/**
	 * Set any data you want associated with this profile, while it is in memory
	 */
	public void setAssociatedData(Object data) {
		this.associatedData = data;
	}

	/**
	 * Get the associated data set via setAssociatedData
	 */
	public Object getAssociatedData() {
		return associatedData;
	}

	/**
	 * Return the name of this profile as given in the constructor
	 */
	public String getProfileName() {
		return profileName;
	}

	/**
	 * Return the system profile this is associated with
	 */
	public ISystemProfile getProfile() {
		return RSEUIPlugin.getTheSystemRegistry().getSystemProfile(profileName);
	}

	/**
	 * Return the SystemCompileManager responsible for this profile
	 */
	public SystemCompileManager getParentManager() {
		return parentManager;
	}

	/**
	 * Add a compile type
	 */
	public void addCompileType(SystemCompileType type) {
		compileTypes.add(type);
		flushCache();
	}

	/**
	 * Remote a compile type.
	 * Should only be called if the type is empty of compile commands
	 */
	public void removeCompileType(SystemCompileType type) {
		compileTypes.remove(type);
		flushCache();
	}

	/**
	 * Get compile types. 
	 * @return a Vector of SystemCompileType objects.
	 */
	public Vector getCompileTypes() {
		return compileTypes;
	}

	/**
	 * Get compile types as an array of strings.
	 */
	public String[] getCompileTypesArray() {
		if ((srcTypes == null) || (srcTypes.length != compileTypes.size())) {
			srcTypes = new String[compileTypes.size()];
			for (int idx = 0; idx < srcTypes.length; idx++)
				srcTypes[idx] = ((SystemCompileType) compileTypes.elementAt(idx)).getType();
		}
		return srcTypes;
	}

	/**
	 * Get the compile type, given a type
	 */
	public SystemCompileType getCompileType(String typeString) {
		SystemCompileType compileType;
		for (int i = 0; i < compileTypes.size(); i++) {
			compileType = (SystemCompileType) (compileTypes.get(i));
			if (compileType.getType().equalsIgnoreCase(typeString)) return compileType;
		}
		return null;
	}

	/**
	 * Save this profile to disk. It is saved to an xml file that is scoped
	 *  to a folder named after the subsystem factory, and that in turn is 
	 *  scoped to a folder named "CompileCommands" within this system profile's 
	 *  folder.
	 */
	public void writeToDisk() {
		write(compileTypes);
		isRead = false;
	}

	/**
	 * Should you require access to the IFolder containing the persisted xml
	 *  file, call this method. Note, this folder is only created on first touch,
	 *  so this call may have the side-effect of creating the folder.
	 * <p>
	 * This defers back to the owning SystemCompileManager.
	 */
	public IFolder getCompileFolder() {
		return parentManager.getCompileProfileFolder(this);
	}

	/**
	 * Should you require access to the IFile handle to the persisted xml file,
	 *  call this method. Note, this file is only created on first touch,
	 *  so file may be a handle to something that doesn't exist yet.
	 */
	public IFile getCompileProfileFile() {
		IFolder folder = getCompileFolder();
		IFile file = folder.getFile(getSaveFileName());
		return file;
	}

	/**
	 * Should you require access to the java.io.File handle to the persisted xml file,
	 *  call this method. Note, this file is only created on first touch,
	 *  so file may be a handle to something that doesn't exist yet.
	 */
	public File getCompileProfileJavaFile() {
		IFolder folder = getCompileFolder();
		IPath path = folder.getLocation().makeAbsolute();
		path = path.append(IPath.SEPARATOR + getSaveFileName());
		File file = new File(path.makeAbsolute().toOSString());
		return file;
	}

	// -------------------
	// ABSTRACT METHODS...
	// -------------------
	/**
	 * When the time comes to actually run a compile command against a selected source object,
	 *  this method is called to return the instance of SystemCompilableSource to do that. 
	 * <p>
	 * This method must be implemented to return an instance of your subclass of SystemCompilableSource.
	 */
	public abstract SystemCompilableSource getCompilableSourceObject(Shell shell, Object selectedObject, SystemCompileCommand compileCmd, boolean isPrompt, Viewer viewer);

	// -----------------------------------
	// POTENTIALLY OVERRIDABLE METHODS...
	// -----------------------------------
	/**
	 * Return the name of the xml file we will persist this profile's compile command
	 *  information to. 
	 * The default is "compile.xml" and need not be overridden unless you don't like that name :-).
	 */
	protected String getSaveFileName() {
		return ISystemCompileXMLConstants.FILE_NAME;
	}

	/**
	 * This method is called by the constructor, prior to reading the xml contents from disk.
	 * It is an exit point in case subclasses need to do anything before the read, such as rename
	 *  or migrate legacy information.
	 */
	protected void doPreRead() {
	}

	// -------------------
	// PRIVATE METHODS...
	// -------------------
	/**
	 * Read all compile types associated with the profile from the disk
	 */
	private void readFromDisk() {
		if (!isRead) {
			compileTypes = read();
			isRead = true;
		}
	}

	/**
	 * Clear any cached info, as something has changed.
	 */
	private void flushCache() {
		srcTypes = null;
	}

	/**
	 * Read the XML file that holds all information about the
	 * types and associated compile names in this profile.
	 */
	private Vector read() {
		Vector types = null;
		File file = getCompileProfileJavaFile();
		// If the file does not exist, then write all IBM supplied default
		// types and compile names first before reading
		if (file == null || !file.exists()) {
			if (parentManager.wantToPrimeWithDefaults(this)) // we only prime the user's private profile with default compile commands
			{
				if (!writeDefaults()) return new Vector();
			} else {
				return new Vector();
			}
		}
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);
			types = getTypes(doc);
		} catch (Exception e) {
			SystemBasePlugin.logError("Error reading compile names XML file for profile " + getProfileName(), e); //$NON-NLS-1$
		}
		return types;
	}

	/**
	 * Get all the compile types.
	 * @return a vector of SystemCompileType objects.
	 */
	private Vector getTypes(Document doc) {
		Vector types = new Vector();
		Element root = doc.getDocumentElement();
		String oldvrm = root.getAttribute(ISystemCompileXMLConstants.VERSION_ATTRIBUTE);
		boolean oldversion = (oldvrm != null) && !oldvrm.equals(ISystemCompileXMLConstants.VERSION_VALUE);
		NodeList list = doc.getElementsByTagName(ISystemCompileXMLConstants.TYPE_ELEMENT);
		if (list == null) return types;
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			NamedNodeMap map = node.getAttributes();
			// get the type
			Node typeAttr = map.getNamedItem(ISystemCompileXMLConstants.TYPE_ATTRIBUTE);
			String type = typeAttr.getNodeValue();
			// get the label of the last compile name
			Node lastUsedAttr = map.getNamedItem(ISystemCompileXMLConstants.LASTUSED_ATTRIBUTE);
			String lastUsed = lastUsedAttr.getNodeValue();
			SystemCompileType newType = new SystemCompileType(this, type);
			NodeList childList = node.getChildNodes();
			for (int j = 0; j < childList.getLength(); j++) {
				Node child = childList.item(j);
				NamedNodeMap childAttrMap = child.getAttributes();
				// get the name of the compile name
				Node nameAttr = childAttrMap.getNamedItem(ISystemCompileXMLConstants.LABEL_ATTRIBUTE);
				String name = nameAttr.getNodeValue();
				// get the nature of the compile name
				Node natureAttr = childAttrMap.getNamedItem(ISystemCompileXMLConstants.NATURE_ATTRIBUTE);
				String nature = natureAttr.getNodeValue();
				// get the default command string
				Node defaultAttr = childAttrMap.getNamedItem(ISystemCompileXMLConstants.DEFAULT_ATTRIBUTE);
				String defaultString = (defaultAttr != null) ? defaultAttr.getNodeValue() : ""; //$NON-NLS-1$
				// get the current string
				Node currentAttr = childAttrMap.getNamedItem(ISystemCompileXMLConstants.CURRENT_ATTRIBUTE);
				String currentString = currentAttr.getNodeValue();
				// get the menu option
				Node menuAttr = childAttrMap.getNamedItem(ISystemCompileXMLConstants.MENU_ATTRIBUTE);
				String menuOption = menuAttr.getNodeValue();
				// get the jobenv option
				Node jobenvAttr = childAttrMap.getNamedItem(ISystemCompileXMLConstants.JOBENV_ATTRIBUTE);
				String jobEnv = null;
				if (jobenvAttr != null) jobEnv = jobenvAttr.getNodeValue();
				// get the ordering
				int order = j;
				Node orderAttr = childAttrMap.getNamedItem(ISystemCompileXMLConstants.ORDER_ATTRIBUTE);
				// to ensure previous beta customers do not have problems
				if (orderAttr != null) {
					order = Integer.valueOf(orderAttr.getNodeValue()).intValue();
				}
				// get the id option
				Node idAttr = childAttrMap.getNamedItem(ISystemCompileXMLConstants.ID_ATTRIBUTE);
				String id = null;
				if (idAttr != null) {
					id = idAttr.getNodeValue();
				}
				// get the label editable option
				Node labelEditableAttr = childAttrMap.getNamedItem(ISystemCompileXMLConstants.LABEL_EDITABLE_ATTRIBUTE);
				String labelEditable = null;
				if (labelEditableAttr != null) {
					labelEditable = labelEditableAttr.getNodeValue();
				}
				// get the label editable option
				Node stringEditableAttr = childAttrMap.getNamedItem(ISystemCompileXMLConstants.STRING_EDITABLE_ATTRIBUTE);
				String stringEditable = null;
				if (stringEditableAttr != null) {
					stringEditable = stringEditableAttr.getNodeValue();
				}
				// id can be null, in which case the contructor will try to configure the id automatically
				// so no need to check for id == null here
				SystemCompileCommand newCmd = new SystemCompileCommand(newType, id, name, nature, defaultString, currentString, menuOption, order);
				// if label editable is null, i.e. attribute did not exist, then don't do anything
				// because the command is either IBM supplied or user supplied, and the constructor will take
				// care of it. We only care if the attribute is not null.
				if (labelEditable != null) {
					boolean isLabelEditable = Boolean.valueOf(labelEditable).booleanValue();
					newCmd.setLabelEditable(isLabelEditable);
				}
				// if string editable is null, i.e. attribute did not exist, then don't do anything
				// because the command is either IBM supplied or user supplied, and the constructor will take
				// care of it. We only care if the attribute is not null.
				if (stringEditable != null) {
					boolean isStringEditable = Boolean.valueOf(stringEditable).booleanValue();
					newCmd.setCommandStringEditable(isStringEditable);
				}
				if (jobEnv != null) newCmd.setJobEnvironment(jobEnv);
				if (name.equalsIgnoreCase(lastUsed)) newType.setLastUsedCompileCommand(newCmd);
				if (oldversion) newCmd = migrateCompileCommand(newCmd, oldvrm);
				newType.addCompileCommandInOrder(newCmd);
			}
			// add compile type and all its contents to the types list
			types.add(newType);
		}
		// if we currently have an older version on disk, we may have added new default types and associated
		// default compile commands for this release. so find out what new default source types we have
		// in this release, and add them to list of types in memory.
		// We only want to add these new default types to the default private profile.
		// Warning:: this will not handle the case where we want to change a default compile command for
		// an existing type with a new release. Need to modify the code below for that.
		if (parentManager.wantToPrimeWithDefaults(this) && oldversion) {
			SystemDefaultCompileCommands allCmds = parentManager.getDefaultCompileCommands();
			if (allCmds == null) {
				return types;
			}
			// get all default types
			String[] defaultTypes = allCmds.getAllDefaultSuppliedSourceTypes();
			// for each default type, find out if we already have it in memory, i.e.
			// it exists from a previous release and so is not new.
			for (int i = 0; i < defaultTypes.length; i++) {
				boolean typeFound = false;
				Iterator iter = types.iterator();
				// iterate over all types in memory
				while (iter.hasNext()) {
					SystemCompileType tempType = (SystemCompileType) iter.next();
					// if the types match, we know this type is not new
					if (tempType.getType().equalsIgnoreCase(defaultTypes[i])) {
						typeFound = true;
						break;
					}
				}
				// type wasn't found in memory, so this is a new default type which we need to add
				// also need to add the default commands for that source type
				if (!typeFound) {
					SystemCompileType type = new SystemCompileType(this, defaultTypes[i]);
					SystemDefaultCompileCommand[] defaultCmds = allCmds.getCommandsForSrcType(defaultTypes[i]);
					if ((defaultCmds == null) || (defaultCmds.length == 0)) {
						types.add(type);
						continue;
					}
					SystemCompileCommand[] cmds = new SystemCompileCommand[defaultCmds.length];
					for (int j = 0; j < defaultCmds.length; j++) {
						String cmdName = defaultCmds[j].getLabel();
						String commandString = defaultCmds[j].getCommandWithParameters();
						// we can pass in null for the id, because the constructor checks if id is null
						// and if so tries to configure id automatically. This will work for IBM supplied commands.
						cmds[j] = new SystemCompileCommand(type, null, cmdName, ISystemCompileXMLConstants.NATURE_IBM_VALUE, commandString, commandString, ISystemCompileXMLConstants.MENU_BOTH_VALUE, j);
						type.addCompileCommandInOrder(cmds[j]);
						String jobEnv = defaultCmds[j].getJobEnvironment();
						if (jobEnv != null) {
							cmds[j].setJobEnvironment(jobEnv);
						}
						if (j == 0) {
							type.setLastUsedCompileCommand(cmds[j]);
						}
					}
					types.add(type);
				}
			}
		}
		return types;
	}

	/**
	 * 
	 * Add compile contributions made through extension points for the given resource,
	 * and save them to disk.
	 */
	public void addContributions(Object element) {
		// we only add contributions to the default private profile
		// Should we update existing labels in other profiles, e.g. a user may have copied a contributed
		// label to another profile? This won't change those labels, only the label in the default private profile.
		if (parentManager.wantToPrimeWithDefaults(this)) {
			SystemCompileContributorManager.getInstance().contributeCompileCommands(this, element);
		}
		// write the contributions to disk
		// writeToDisk();
	}

	/**
	 * Opportunity for subclasses to do migration of compile commands read from disk,
	 *  from a document that has an older vrm than the current vrm.
	 */
	protected SystemCompileCommand migrateCompileCommand(SystemCompileCommand oldCmd, String oldVrm) {
		return oldCmd;
	}

	/**
	 * Do substring substitution. Using you are replacing &1 (say) with
	 *  another string.
	 * @param string - string containing substring to be substituted. 
	 * @param subOld - substitution variable. Eg "%1"
	 * @param subNew - substitution data. Eg "001"
	 * @return string with all occurrences of subOld substituted with subNew.
	 */
	protected String sub(String string, String subOld, String subNew) {
		if (string == null) return string;
		StringBuffer temp = new StringBuffer();
		int lastHit = 0;
		int newHit = 0;
		for (newHit = string.indexOf(subOld, lastHit); newHit != -1; lastHit = newHit, newHit = string.indexOf(subOld, lastHit)) {
			if (newHit >= 0) temp.append(string.substring(lastHit, newHit));
			temp.append(subNew);
			newHit += subOld.length();
		}
		if (lastHit >= 0) temp.append(string.substring(lastHit));
		return temp.toString();
	}

	/**
	 * Write the contents of the file, given the contents as a Vector of SystemCompileType objects.
	 */
	private void write(Vector types) {
		File file = getCompileProfileJavaFile();
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document doc = builder.getDOMImplementation().createDocument(null, ISystemCompileXMLConstants.ROOT_ELEMENT, null);
			// get root element and set attributes
			Element root = doc.getDocumentElement();
			root.setAttribute(ISystemCompileXMLConstants.VERSION_ATTRIBUTE, ISystemCompileXMLConstants.VERSION_VALUE);
			// write the copyright info
			Element copyright = doc.createElement(ISystemCompileXMLConstants.COPYRIGHT_ELEMENT);
			Text copyrightText = doc.createTextNode(ISystemCompileXMLConstants.COPYRIGHT_TEXT);
			copyright.appendChild(copyrightText);
			root.appendChild(copyright);
			// write type and compile commands for each
			for (int i = 0; i < types.size(); i++) {
				SystemCompileType type = (SystemCompileType) (types.get(i));
				Element typeElement = doc.createElement(ISystemCompileXMLConstants.TYPE_ELEMENT);
				typeElement.setAttribute(ISystemCompileXMLConstants.TYPE_ATTRIBUTE, type.getType());
				SystemCompileCommand lastUsedCompileName = type.getLastUsedCompileCommand();
				String lastUsedName = null;
				if (lastUsedCompileName == null) {
					lastUsedName = ""; //$NON-NLS-1$
				} else {
					lastUsedName = lastUsedCompileName.getLabel();
				}
				typeElement.setAttribute(ISystemCompileXMLConstants.LASTUSED_ATTRIBUTE, lastUsedName);
				Vector cmds = type.getCompileCommands();
				for (int j = 0; j < cmds.size(); j++) {
					SystemCompileCommand cmd = (SystemCompileCommand) (cmds.get(j));
					Element cmdElement = doc.createElement(ISystemCompileXMLConstants.COMPILECOMMAND_ELEMENT);
					if (cmd.getId() != null) {
						cmdElement.setAttribute(ISystemCompileXMLConstants.ID_ATTRIBUTE, cmd.getId());
					}
					cmdElement.setAttribute(ISystemCompileXMLConstants.LABEL_ATTRIBUTE, cmd.getLabel());
					cmdElement.setAttribute(ISystemCompileXMLConstants.NATURE_ATTRIBUTE, cmd.getNature());
					cmdElement.setAttribute(ISystemCompileXMLConstants.DEFAULT_ATTRIBUTE, cmd.getDefaultString());
					cmdElement.setAttribute(ISystemCompileXMLConstants.CURRENT_ATTRIBUTE, cmd.getCurrentString());
					cmdElement.setAttribute(ISystemCompileXMLConstants.MENU_ATTRIBUTE, cmd.getMenuOption());
					cmdElement.setAttribute(ISystemCompileXMLConstants.ORDER_ATTRIBUTE, String.valueOf(j));
					cmdElement.setAttribute(ISystemCompileXMLConstants.LABEL_EDITABLE_ATTRIBUTE, String.valueOf(cmd.isLabelEditable()));
					cmdElement.setAttribute(ISystemCompileXMLConstants.STRING_EDITABLE_ATTRIBUTE, String.valueOf(cmd.isCommandStringEditable()));
					if (cmd.getJobEnvironment() != null) {
						cmdElement.setAttribute(ISystemCompileXMLConstants.JOBENV_ATTRIBUTE, cmd.getJobEnvironment());
					}
					typeElement.appendChild(cmdElement);
				}
				root.appendChild(typeElement);
			}
			// write out document to XML file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource input = new DOMSource(doc);
			Result output = new StreamResult(file);
			transformer.transform(input, output);
			// now refresh the eclipse workspace model for the parent folder, to recognize changes we made
			SystemResourceHelpers.getResourceHelpers().refreshResource(getCompileFolder());
		} catch (Exception e) {
			SystemBasePlugin.logError("Error writing compile names xml file for profile " + getProfileName(), e); //$NON-NLS-1$
		}
	}

	/**
	 * Prime document with default (supplied) types and names.
	 * Return true if any written, false if none to write.
	 */
	private boolean writeDefaults() {
		SystemDefaultCompileCommands allCmds = parentManager.getDefaultCompileCommands();
		if (allCmds == null) return false;
		String[] defaultTypes = allCmds.getAllDefaultSuppliedSourceTypes();
		Vector types = new Vector();
		for (int i = 0; i < defaultTypes.length; i++) {
			SystemCompileType type = new SystemCompileType(this, defaultTypes[i]);
			SystemDefaultCompileCommand[] defaultCmds = allCmds.getCommandsForSrcType(defaultTypes[i]);
			if ((defaultCmds == null) || (defaultCmds.length == 0)) {
				types.add(type);
				continue;
			}
			SystemCompileCommand[] cmds = new SystemCompileCommand[defaultCmds.length];
			for (int j = 0; j < defaultCmds.length; j++) {
				String cmdName = defaultCmds[j].getLabel();
				String commandString = defaultCmds[j].getCommandWithParameters();
				// we can pass in null for the id, because the constructor checks if id is null
				// and if so tries to configure id automatically. This will work for IBM supplied commands.
				cmds[j] = new SystemCompileCommand(type, null, cmdName, ISystemCompileXMLConstants.NATURE_IBM_VALUE, commandString, commandString, ISystemCompileXMLConstants.MENU_BOTH_VALUE, j);
				type.addCompileCommandInOrder(cmds[j]);
				String jobEnv = defaultCmds[j].getJobEnvironment();
				if (jobEnv != null) cmds[j].setJobEnvironment(jobEnv);
				if (j == 0) {
					type.setLastUsedCompileCommand(cmds[j]);
				}
			}
			types.add(type);
		}
		write(types);
		//printCommandsByType(types); // temporary, for debugging
		return (defaultTypes.length > 0);
	}

	/**
	 * Print the commands to standard out, sorted by source type, for debugging purposes
	 */
	public void printCommandsByType(Vector compileTypes) {
		System.out.println();
		System.out.println("Compile commands"); //$NON-NLS-1$
		System.out.println("-----------------"); //$NON-NLS-1$
		for (int idx = 0; idx < compileTypes.size(); idx++) {
			SystemCompileType type = (SystemCompileType) compileTypes.elementAt(idx);
			System.out.println("Type: " + type.getType()); //$NON-NLS-1$
			for (int jdx = 0; jdx < type.getNumOfCommands(); jdx++) {
				SystemCompileCommand cmd = type.getCompileCommand(jdx);
				cmd.printCommand("  "); //$NON-NLS-1$
			}
		}
		System.out.println();
	}
}

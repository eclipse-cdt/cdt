/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.content.*;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IInputOrder;
import org.eclipse.cdt.managedbuilder.core.IAdditionalInput;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class InputType extends BuildObject implements IInputType {

	private static final String DEFAULT_SEPARATOR = ","; //$NON-NLS-1$
	private static final String EMPTY_STRING = new String();

	//  Superclass
	private IInputType superClass;
	private String superClassId;
	//  Parent and children
	private ITool parent;
	private Vector inputOrderList;
	private Vector additionalInputList;
	//  Managed Build model attributes
	private String sourceContentTypeId;
	private IContentType sourceContentType;
	private List inputExtensions;
	private String dependencyContentTypeId;
	private IContentType dependencyContentType;
	private List dependencyExtensions;
	private String optionId;
	private String buildVariable;
	private Boolean multipleOfType;
	private Boolean primaryInput;
	private IConfigurationElement dependencyGeneratorElement = null;
	private IManagedDependencyGenerator dependencyGenerator = null;
	//  Miscellaneous
	private boolean isExtensionInputType = false;
	private boolean isDirty = false;
	private boolean resolved = true;

	/*
	 *  C O N S T R U C T O R S
	 */
	
	/**
	 * This constructor is called to create an InputType defined by an extension point in 
	 * a plugin manifest file, or returned by a dynamic element provider
	 * 
	 * @param parent  The ITool parent of this InputType
	 * @param element The InputType definition from the manifest file or a dynamic element
	 *                provider
	 */
	public InputType(ITool parent, IManagedConfigElement element) {
		this.parent = parent;
		isExtensionInputType = true;
		
		// setup for resolving
		resolved = false;

		loadFromManifest(element);
		
		// Hook me up to the Managed Build Manager
		ManagedBuildManager.addExtensionInputType(this);
		
		// Load Children
		IManagedConfigElement[] iElements = element.getChildren();
		for (int l = 0; l < iElements.length; ++l) {
			IManagedConfigElement iElement = iElements[l];
			if (iElement.getName().equals(IInputOrder.INPUT_ORDER_ELEMENT_NAME)) {
				InputOrder inputOrder = new InputOrder(this, iElement);
				getInputOrderList().add(inputOrder);
			} else if (iElement.getName().equals(IAdditionalInput.ADDITIONAL_INPUT_ELEMENT_NAME)) {
				AdditionalInput addlInput = new AdditionalInput(this, iElement);
				getAdditionalInputList().add(addlInput);
			}
		}
	}

	/**
	 * This constructor is called to create an InputType whose attributes and children will be 
	 * added by separate calls.
	 * 
	 * @param Tool The parent of the an InputType
	 * @param InputType The superClass, if any
	 * @param String The id for the new InputType
	 * @param String The name for the new InputType
	 * @param boolean Indicates whether this is an extension element or a managed project element
	 */
	public InputType(Tool parent, IInputType superClass, String Id, String name, boolean isExtensionElement) {
		this.parent = parent;
		this.superClass = superClass;
		if (this.superClass != null) {
			superClassId = this.superClass.getId();
		}
		setId(Id);
		setName(name);
			
		isExtensionInputType = isExtensionElement;
		if (isExtensionElement) {
			// Hook me up to the Managed Build Manager
			ManagedBuildManager.addExtensionInputType(this);
		} else {
			setDirty(true);
		}
	}

	/**
	 * Create an <code>InputType</code> based on the specification stored in the 
	 * project file (.cdtbuild).
	 * 
	 * @param parent The <code>ITool</code> the InputType will be added to. 
	 * @param element The XML element that contains the InputType settings.
	 * 
	 */
	public InputType(ITool parent, Element element) {
		this.parent = parent;
		isExtensionInputType = false;
		
		// Initialize from the XML attributes
		loadFromProject(element);
		
		// Load children
		NodeList configElements = element.getChildNodes();
		for (int i = 0; i < configElements.getLength(); ++i) {
			Node configElement = configElements.item(i);
			if (configElement.getNodeName().equals(IInputOrder.INPUT_ORDER_ELEMENT_NAME)) {
				InputOrder inputOrder = new InputOrder(this, (Element)configElement);
				getInputOrderList().add(inputOrder);
			} else if (configElement.getNodeName().equals(IAdditionalInput.ADDITIONAL_INPUT_ELEMENT_NAME)) {
				AdditionalInput addlInput = new AdditionalInput(this, (Element)configElement);
				getAdditionalInputList().add(addlInput);
			}
		}
	}

	/**
	 * Create an <code>InputType</code> based upon an existing InputType.
	 * 
	 * @param parent The <code>ITool</code> the InputType will be added to.
	 * @param Id The identifier of the new InputType
	 * @param name The name of the new InputType
	 * @param inputType The existing InputType to clone.
	 */
	public InputType(ITool parent, String Id, String name, InputType inputType) {
		this.parent = parent;
		superClass = inputType.superClass;
		if (superClass != null) {
			if (inputType.superClassId != null) {
				superClassId = new String(inputType.superClassId);
			}
		}
		setId(Id);
		setName(name);
			
		isExtensionInputType = false;
		
		//  Copy the remaining attributes

		if (inputType.sourceContentTypeId != null) {
			sourceContentTypeId = new String(inputType.sourceContentTypeId);
		}
		sourceContentType = inputType.sourceContentType;
		if (inputType.inputExtensions != null) {
			inputExtensions = new ArrayList(inputType.inputExtensions);
		}
		if (inputType.dependencyContentTypeId != null) {
			dependencyContentTypeId = new String(inputType.dependencyContentTypeId);
		}
		dependencyContentType = inputType.dependencyContentType;
		if (inputType.dependencyExtensions != null) {
			dependencyExtensions = new ArrayList(inputType.dependencyExtensions);
		}
		if (inputType.optionId != null) {
			optionId = new String(inputType.optionId);
		}
		if (inputType.buildVariable != null) {
			buildVariable = new String(inputType.buildVariable);
		}
		if (inputType.multipleOfType != null) {
			multipleOfType = new Boolean(inputType.multipleOfType.booleanValue());
		}
		if (inputType.primaryInput != null) {
			primaryInput = new Boolean(inputType.primaryInput.booleanValue());
		}
		dependencyGeneratorElement = inputType.dependencyGeneratorElement; 
		dependencyGenerator = inputType.dependencyGenerator; 

		//  Clone the children
		if (inputType.inputOrderList != null) {
			Iterator iter = inputType.getInputOrderList().listIterator();
			while (iter.hasNext()) {
				InputOrder inputOrder = (InputOrder) iter.next();
				InputOrder newInputOrder = new InputOrder(this, inputOrder);
				getInputOrderList().add(newInputOrder);
			}
		}
		if (inputType.additionalInputList != null) {
			Iterator iter = inputType.getAdditionalInputList().listIterator();
			while (iter.hasNext()) {
				AdditionalInput additionalInput = (AdditionalInput) iter.next();
				AdditionalInput newAdditionalInput = new AdditionalInput(this, additionalInput);
				getAdditionalInputList().add(newAdditionalInput);
			}
		}
		
		setDirty(true);
	}

	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */
	
	/* (non-Javadoc)
	 * Loads the InputType information from the ManagedConfigElement specified in the 
	 * argument.
	 * 
	 * @param element Contains the InputType information 
	 */
	protected void loadFromManifest(IManagedConfigElement element) {
		ManagedBuildManager.putConfigElement(this, element);
		
		// id
		setId(element.getAttribute(IBuildObject.ID));
		
		// Get the name
		setName(element.getAttribute(IBuildObject.NAME));
		
		// superClass
		superClassId = element.getAttribute(IProjectType.SUPERCLASS);
		
		// sourceContentType
		sourceContentTypeId = element.getAttribute(IInputType.SOURCE_CONTENT_TYPE); 
		
		// Get the supported input file extensions
		String inputs = element.getAttribute(ITool.SOURCES);
		if (inputs != null) {
			StringTokenizer tokenizer = new StringTokenizer(inputs, DEFAULT_SEPARATOR);
			while (tokenizer.hasMoreElements()) {
				getInputExtensionsList().add(tokenizer.nextElement());
			}
		}
		
		// dependencyContentType
		dependencyContentTypeId = element.getAttribute(IInputType.DEPENDENCY_CONTENT_TYPE); 
		
		// Get the dependency (header file) extensions
		String headers = element.getAttribute(IInputType.DEPENDENCY_EXTENSIONS);
		if (headers != null) {
			StringTokenizer tokenizer = new StringTokenizer(headers, DEFAULT_SEPARATOR);
			while (tokenizer.hasMoreElements()) {
				getDependencyExtensionsList().add(tokenizer.nextElement());
			}
		}
		
		// option
		optionId = element.getAttribute(IInputType.OPTION); 
		
		// multipleOfType
        String isMOT = element.getAttribute(IInputType.MULTIPLE_OF_TYPE);
        if (isMOT != null){
    		multipleOfType = new Boolean("true".equals(isMOT)); //$NON-NLS-1$
        }
		
		// primaryInput
        String isPI = element.getAttribute(IInputType.PRIMARY_INPUT);
        if (isPI != null){
			primaryInput = new Boolean("true".equals(isPI)); //$NON-NLS-1$
        }
		
		// buildVariable
		buildVariable = element.getAttribute(IInputType.BUILD_VARIABLE); 

		// Store the configuration element IFF there is a dependency generator defined 
		String depGenerator = element.getAttribute(ITool.DEP_CALC_ID); 
		if (depGenerator != null && element instanceof DefaultManagedConfigElement) {
			dependencyGeneratorElement = ((DefaultManagedConfigElement)element).getConfigurationElement();			
		}
	}
	
	/* (non-Javadoc)
	 * Initialize the InputType information from the XML element 
	 * specified in the argument
	 * 
	 * @param element An XML element containing the InputType information 
	 */
	protected boolean loadFromProject(Element element) {
		
		// id
		setId(element.getAttribute(IBuildObject.ID));

		// name
		if (element.hasAttribute(IBuildObject.NAME)) {
			setName(element.getAttribute(IBuildObject.NAME));
		}
		
		// superClass
		superClassId = element.getAttribute(IProjectType.SUPERCLASS);
		if (superClassId != null && superClassId.length() > 0) {
			superClass = ManagedBuildManager.getExtensionInputType(superClassId);
			if (superClass == null) {
				// TODO:  Report error
			}
		}
		
		// sourceContentType
		IContentTypeManager manager = Platform.getContentTypeManager();
		if (element.hasAttribute(IInputType.SOURCE_CONTENT_TYPE)) {
			sourceContentTypeId = element.getAttribute(IInputType.SOURCE_CONTENT_TYPE);
			if (sourceContentTypeId != null && sourceContentTypeId.length() > 0) {
				sourceContentType = manager.getContentType(sourceContentTypeId);
			}
		}

        // sources
		if (element.hasAttribute(IInputType.SOURCES)) {
			String inputs = element.getAttribute(ITool.SOURCES);
			if (inputs != null) {
				StringTokenizer tokenizer = new StringTokenizer(inputs, DEFAULT_SEPARATOR);
				while (tokenizer.hasMoreElements()) {
					getInputExtensionsList().add(tokenizer.nextElement());
				}
			}
		}
		
		// dependencyContentType
		if (element.hasAttribute(IInputType.DEPENDENCY_CONTENT_TYPE)) {
			dependencyContentTypeId = element.getAttribute(IInputType.DEPENDENCY_CONTENT_TYPE);
			if (dependencyContentTypeId != null && dependencyContentTypeId.length() > 0) {
				dependencyContentType = manager.getContentType(dependencyContentTypeId);
			}
		}
		
		// dependencyExtensions
		// Get the dependency (header file) extensions
		if (element.hasAttribute(IInputType.DEPENDENCY_EXTENSIONS)) {
			String headers = element.getAttribute(IInputType.DEPENDENCY_EXTENSIONS);
			if (headers != null) {
				StringTokenizer tokenizer = new StringTokenizer(headers, DEFAULT_SEPARATOR);
				while (tokenizer.hasMoreElements()) {
					getDependencyExtensionsList().add(tokenizer.nextElement());
				}
			}
		}
		
		// option
		if (element.hasAttribute(IInputType.OPTION)) { 
			optionId = element.getAttribute(IInputType.OPTION);
		}
		
		// multipleOfType
		if (element.hasAttribute(IInputType.MULTIPLE_OF_TYPE)) {
			String isMOT = element.getAttribute(IInputType.MULTIPLE_OF_TYPE);
			if (isMOT != null){
				multipleOfType = new Boolean("true".equals(isMOT)); //$NON-NLS-1$
			}
		}
		
		// primaryInput
		if (element.hasAttribute(IInputType.PRIMARY_INPUT)) {
	        String isPI = element.getAttribute(IInputType.PRIMARY_INPUT);
	        if (isPI != null){
				primaryInput = new Boolean("true".equals(isPI)); //$NON-NLS-1$
	        }
		}
		
		// buildVariable
		if (element.hasAttribute(IInputType.BUILD_VARIABLE)) {
			buildVariable = element.getAttribute(IInputType.BUILD_VARIABLE);
		}
		
		// Note: dependency generator cannot be specified in a project file because
		//       an IConfigurationElement is needed to load it!
		if (element.hasAttribute(ITool.DEP_CALC_ID)) {
			// TODO:  Issue warning?
		}
		
		return true;
	}

	/**
	 * Persist the InputType to the project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(Document doc, Element element) {
		if (superClass != null)
			element.setAttribute(IProjectType.SUPERCLASS, superClass.getId());
		
		element.setAttribute(IBuildObject.ID, id);
		
		if (name != null) {
			element.setAttribute(IBuildObject.NAME, name);
		}
		
		// sourceContentType
		if (sourceContentTypeId != null) {
			element.setAttribute(IInputType.SOURCE_CONTENT_TYPE, sourceContentTypeId);
		}
		
		// input file extensions
		if (getInputExtensionsList().size() > 0) {
			String inputs;
			List list = getInputExtensionsList();
			Iterator iter = list.listIterator();
			inputs = (String)iter.next();
			while (iter.hasNext()) {
				inputs += DEFAULT_SEPARATOR;
				inputs += iter.next();
			}
			element.setAttribute(IInputType.SOURCES, inputs);
		}
		
		// dependencyContentType
		if (dependencyContentTypeId != null) {
			element.setAttribute(IInputType.DEPENDENCY_CONTENT_TYPE, dependencyContentTypeId);
		}
		
		// dependency (header file) extensions
		if (getDependencyExtensionsList().size() > 0) {
			String headers;
			List list = getDependencyExtensionsList();
			Iterator iter = list.listIterator();
			headers = (String)iter.next();
			while (iter.hasNext()) {
				headers += DEFAULT_SEPARATOR;
				headers += iter.next();
			}
			element.setAttribute(IInputType.DEPENDENCY_EXTENSIONS, headers);
		}
		
		if (optionId != null) {
			element.setAttribute(IInputType.OPTION, optionId);
		}
		
		if (multipleOfType != null) {
			element.setAttribute(IInputType.MULTIPLE_OF_TYPE, multipleOfType.toString());
		}
		
		if (primaryInput != null) {
			element.setAttribute(IInputType.PRIMARY_INPUT, primaryInput.toString());
		}

		if (buildVariable != null) {
			element.setAttribute(IInputType.BUILD_VARIABLE, buildVariable);
		}

		// Note: dependency generator cannot be specified in a project file because
		//       an IConfigurationElement is needed to load it!
		if (dependencyGeneratorElement != null) {
			//  TODO:  issue warning?
		}

		// Serialize my children
		List childElements = getInputOrderList();
		Iterator iter = childElements.listIterator();
		while (iter.hasNext()) {
			InputOrder io = (InputOrder) iter.next();
			Element ioElement = doc.createElement(InputOrder.INPUT_ORDER_ELEMENT_NAME);
			element.appendChild(ioElement);
			io.serialize(doc, ioElement);
		}
		childElements = getAdditionalInputList();
		iter = childElements.listIterator();
		while (iter.hasNext()) {
			AdditionalInput ai = (AdditionalInput) iter.next();
			Element aiElement = doc.createElement(AdditionalInput.ADDITIONAL_INPUT_ELEMENT_NAME);
			element.appendChild(aiElement);
			ai.serialize(doc, aiElement);
		}
		
		// I am clean now
		isDirty = false;
	}

	/*
	 *  P A R E N T   A N D   C H I L D   H A N D L I N G
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getParent()
	 */
	public ITool getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#createInputOrder()
	 */
	public IInputOrder createInputOrder(String path) {
		InputOrder inputOrder = new InputOrder(this, false);
		inputOrder.setPath(path);
		getInputOrderList().add(inputOrder);
		setDirty(true);
		return inputOrder;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getInputOrders()
	 */
	public IInputOrder[] getInputOrders() {
		IInputOrder[] orders; 
		Vector ours = getInputOrderList();
		orders = (IInputOrder[])ours.toArray(new IInputOrder[ours.size()]); 
		return orders;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getInputOrder()
	 */
	public IInputOrder getInputOrder(String path) {
		// TODO Convert both paths to absolute? 
		List orders = getInputOrderList();
		Iterator iter = orders.listIterator();
		while (iter.hasNext()) {
			InputOrder io = (InputOrder) iter.next();
			if (path.compareToIgnoreCase(io.getPath()) != 0) {
				return io;
			}
		}
		return null;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#removeInputOrder()
	 */
	public void removeInputOrder(String path) {
		IInputOrder order = getInputOrder(path);
		if (order != null) removeInputOrder(order);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#removeInputOrder()
	 */
	public void removeInputOrder(IInputOrder element) {
		getInputOrderList().remove(element);
		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#createAdditionalInput()
	 */
	public IAdditionalInput createAdditionalInput(String paths) {
		AdditionalInput addlInput = new AdditionalInput(this, false);
		addlInput.setPaths(paths);
		getAdditionalInputList().add(addlInput);
		setDirty(true);
		return addlInput;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getAdditionalInputs()
	 */
	public IAdditionalInput[] getAdditionalInputs() {
		IAdditionalInput[] inputs; 
		Vector ours = getAdditionalInputList();
		inputs = (IAdditionalInput[])ours.toArray(new IAdditionalInput[ours.size()]); 
		return inputs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getAdditionalInput()
	 */
	public IAdditionalInput getAdditionalInput(String paths) {
		// TODO Convert both paths to absolute?
		// Must match all strings
		String[] inputTokens = paths.split(";"); //$NON-NLS-1$
		List inputs = getInputOrderList();
		Iterator iter = inputs.listIterator();
		while (iter.hasNext()) {
			AdditionalInput ai = (AdditionalInput) iter.next();
			boolean match = false;
			String[] tokens = ai.getPaths();
			if (tokens.length == inputTokens.length) {
				match = true;
				for (int i = 0; i < tokens.length; i++) {
					if (tokens[i].compareToIgnoreCase(inputTokens[i]) != 0) {
						match = false;
						break;
					}
				}
			}
			if (match) return ai;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#removeAdditionalInput()
	 */
	public void removeAdditionalInput(String path) {
		IAdditionalInput input = getAdditionalInput(path);
		if (input != null) removeAdditionalInput(input);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#removeAdditionalInput()
	 */
	public void removeAdditionalInput(IAdditionalInput element) {
		getAdditionalInputList().remove(element);
		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getAdditionalDependencies()
	 */
	public IPath[] getAdditionalDependencies() {
		List deps = new ArrayList();
		Iterator typeIter = getAdditionalInputList().iterator();
		while (typeIter.hasNext()) {
			AdditionalInput current = (AdditionalInput)typeIter.next();
			int kind = current.getKind();
			if (kind == IAdditionalInput.KIND_ADDITIONAL_DEPENDENCY ||
				kind == IAdditionalInput.KIND_ADDITIONAL_INPUT_DEPENDENCY) {
				String[] paths = current.getPaths();
				if (paths != null) {
					for (int i = 0; i < paths.length; i++) {
						if (paths[i].length() > 0) {
							deps.add(Path.fromOSString(paths[i]));
						}
					}
				}
			}
		}
		return (IPath[])deps.toArray(new IPath[deps.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getAdditionalResources()
	 */
	public IPath[] getAdditionalResources() {
		List ins = new ArrayList();
		Iterator typeIter = getAdditionalInputList().iterator();
		while (typeIter.hasNext()) {
			AdditionalInput current = (AdditionalInput)typeIter.next();
			int kind = current.getKind();
			if (kind == IAdditionalInput.KIND_ADDITIONAL_INPUT ||
				kind == IAdditionalInput.KIND_ADDITIONAL_INPUT_DEPENDENCY) {
				String[] paths = current.getPaths();
				if (paths != null) {
					for (int i = 0; i < paths.length; i++) {
						if (paths[i].length() > 0) {
							ins.add(Path.fromOSString(paths[i]));
						}
					}
				}
			}
		}
		return (IPath[])ins.toArray(new IPath[ins.size()]);
	}

	/* (non-Javadoc)
	 * Returns the project that uses this IInputType 
	 */
	public IProject getProject(ITool tool) {
		IProject project = null;
		IBuildObject toolParent = tool.getParent();
		if (toolParent != null) {
			if (toolParent instanceof IToolChain) {
				return (IProject)((IToolChain)toolParent).getParent().getOwner();
			} else if (toolParent instanceof IResourceConfiguration) {
				return (IProject)((IResourceConfiguration)toolParent).getOwner();
			}
		}
		return project;
	}
	
	/* (non-Javadoc)
	 * Memory-safe way to access the list of input orders
	 */
	private Vector getInputOrderList() {
		if (inputOrderList == null) {
			inputOrderList = new Vector();
		}
		return inputOrderList;
	}
	
	/* (non-Javadoc)
	 * Memory-safe way to access the list of input orders
	 */
	private Vector getAdditionalInputList() {
		if (additionalInputList == null) {
			additionalInputList = new Vector();
		}
		return additionalInputList;
	}

	
	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IInputType#getSuperClass()
	 */
	public IInputType getSuperClass() {
		return superClass;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getName()
	 */
	public String getName() {
		return (name == null && superClass != null) ? superClass.getName() : name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getBuildVariable()
	 */
	public String getBuildVariable() {
		if (buildVariable == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.getBuildVariable();
			} else {
				return EMPTY_STRING;
			}
		}
		return buildVariable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#setBuildVariable()
	 */
	public void setBuildVariable(String variableName) {
		if (variableName == null && buildVariable == null) return;
		if (buildVariable == null || variableName == null || !(variableName.equals(buildVariable))) {
			buildVariable = variableName;
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getDependencyContentType()
	 */
	public IContentType getDependencyContentType() {
		if (dependencyContentType == null) {
			if (superClass != null) {
				return superClass.getDependencyContentType();
			} else {
				return null;
			}			
		}
		return dependencyContentType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#setDependencyContentType()
	 */
	public void setDependencyContentType(IContentType type) {
		if (dependencyContentType != type) {
			dependencyContentType = type;
			if (dependencyContentType != null) {
				dependencyContentTypeId = dependencyContentType.getId();				
			} else {
				dependencyContentTypeId = null;
			}
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getDependencyExtensionsAttribute()
	 */
	public String[] getDependencyExtensionsAttribute() {
		if (dependencyExtensions == null || dependencyExtensions.size() == 0) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.getDependencyExtensionsAttribute();
			} else {
			    if (dependencyExtensions == null) {
					dependencyExtensions = new ArrayList();
			    }
			}
		}
		return (String[])dependencyExtensions.toArray(new String[dependencyExtensions.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#setDependencyExtensionsAttribute()
	 */
	public void setDependencyExtensionsAttribute(String extensions) {
		getDependencyExtensionsList().clear();
		if (extensions != null) {
			StringTokenizer tokenizer = new StringTokenizer(extensions, DEFAULT_SEPARATOR);
			while (tokenizer.hasMoreElements()) {
				getDependencyExtensionsList().add(tokenizer.nextElement());
			}
		}
		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getDependencyExtensions()
	 */
	public String[] getDependencyExtensions(ITool tool) {
		//  Use content type if specified and registered with Eclipse
		IContentType type = getDependencyContentType();
		if (type != null) {
			IContentTypeSettings settings = null;
			IProject project = getProject(tool);
			if (project != null) {
				IScopeContext projectScope = new ProjectScope(project);
				try {
					settings = type.getSettings(projectScope);
				} catch (Exception e) {}
				if (settings != null) {
					String[] specs = settings.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
					//  TODO: There doesn't seem to be any way to distinguish between these 2 cases:
					//      1. No project specific entries have been set so getFileSpecs returns an empty list
					//      2. There are project specific entries and all of the "default" entries have been removed
					//    For now, we have to assume the first case.
					if (specs.length > 0) return specs;
				}
			}
			return type.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
		}
		return getDependencyExtensionsAttribute();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#isDependencyExtension()
	 */
	public boolean isDependencyExtension(ITool tool, String ext) {
		String[] exts = getDependencyExtensions(tool);
		for (int i=0; i<exts.length; i++) {
			if (ext.equals(exts[i])) return true;
		}
		return false;
	}

	private List getDependencyExtensionsList() {
		if (dependencyExtensions == null) {
			dependencyExtensions = new ArrayList();
		}
		return dependencyExtensions;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getDependencyGenerator()
	 */
	public IManagedDependencyGenerator getDependencyGenerator() {
		if (dependencyGenerator != null) {
			return dependencyGenerator;
		}
		IConfigurationElement element = getDependencyGeneratorElement();
		if (element != null) {
			try {
				if (element.getAttribute(ITool.DEP_CALC_ID) != null) {
					dependencyGenerator = (IManagedDependencyGenerator) element.createExecutableExtension(ITool.DEP_CALC_ID);
					return dependencyGenerator;
				}
			} catch (CoreException e) {}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getDependencyGeneratorElement()
	 */
	public IConfigurationElement getDependencyGeneratorElement() {
		if (dependencyGeneratorElement == null) {
			if (superClass != null) {
				return ((InputType)superClass).getDependencyGeneratorElement();
			}
		}
		return dependencyGeneratorElement;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#setDependencyGeneratorElement()
	 */
	public void setDependencyGeneratorElement(IConfigurationElement element) {
		dependencyGeneratorElement = element;
		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getMultipleOfType()
	 */
	public boolean getMultipleOfType() {
		if (multipleOfType == null) {
			if (superClass != null) {
				return superClass.getMultipleOfType();
			} else {
				return false;	// default is false
			}
		}
		return multipleOfType.booleanValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#setMultipleOfType()
	 */
	public void setMultipleOfType(boolean b) {
		if (multipleOfType == null || !(b == multipleOfType.booleanValue())) {
			multipleOfType = new Boolean(b);
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getPrimaryInput()
	 */
	public boolean getPrimaryInput() {
		if (primaryInput == null) {
			if (superClass != null) {
				return superClass.getPrimaryInput();
			} else {
				return false;	// default is false
			}
		}
		return primaryInput.booleanValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#setMultipleOfType()
	 */
	public void setPrimaryInput(boolean b) {
		if (primaryInput == null || !(b == primaryInput.booleanValue())) {
			primaryInput = new Boolean(b);
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getOptionId()
	 */
	public String getOptionId() {
		if (optionId == null) {
			if (superClass != null) {
				return superClass.getOptionId();
			} else {
				return null;
			}			
		}
		return optionId; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#setOptionId()
	 */
	public void setOptionId(String id) {
		if (id == null && optionId == null) return;
		if (id == null || optionId == null || !(optionId.equals(id))) {
			optionId = id;
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getSourceContentType()
	 */
	public IContentType getSourceContentType() {
		if (sourceContentType == null) {
			if (superClass != null) {
				return superClass.getSourceContentType();
			} else {
				return null;
			}			
		}
		return sourceContentType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#setSourceContentType()
	 */
	public void setSourceContentType(IContentType type) {
		if (sourceContentType != type) {
			sourceContentType = type;
			if (sourceContentType != null) {
				sourceContentTypeId = sourceContentType.getId();				
			} else {
				sourceContentTypeId = null;
			}
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getSourceExtensionsAttribute()
	 */
	public String[] getSourceExtensionsAttribute() {
		if( (inputExtensions == null) || ( inputExtensions.size() == 0) ) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.getSourceExtensionsAttribute();
			} else {
				inputExtensions = new ArrayList();
			}
		}
		return (String[])inputExtensions.toArray(new String[inputExtensions.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#setSourceExtensionsAttribute()
	 */
	public void setSourceExtensionsAttribute(String extensions) {
		getInputExtensionsList().clear();
		if (extensions != null) {
			StringTokenizer tokenizer = new StringTokenizer(extensions, DEFAULT_SEPARATOR);
			while (tokenizer.hasMoreElements()) {
				getInputExtensionsList().add(tokenizer.nextElement());
			}
		}
		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#getSourceExtensions()
	 */
	public String[] getSourceExtensions(ITool tool) {
		//  Use content type if specified and registered with Eclipse
		IContentType type = getSourceContentType();
		if (type != null) {
			IContentTypeSettings settings = null;
			IProject project = getProject(tool);
			if (project != null) {
				IScopeContext projectScope = new ProjectScope(project);
				try {
					settings = type.getSettings(projectScope);
				} catch (Exception e) {}
				if (settings != null) {
					String[] specs = settings.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
					//  TODO: There doesn't seem to be any way to distinguish between these 2 cases:
					//      1. No project specific entries have been set so getFileSpecs returns an empty list
					//      2. There are project specific entries and all of the "default" entries have been removed
					//    For now, we have to assume the first case.
					if (specs.length > 0) return specs;
				}
			}
			return type.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
		}
		return getSourceExtensionsAttribute();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputType#isSourceExtension()
	 */
	public boolean isSourceExtension(ITool tool, String ext) {
		String[] exts = getSourceExtensions(tool);
		for (int i=0; i<exts.length; i++) {
			if (ext.equals(exts[i])) return true;
		}
		return false;
	}

	private List getInputExtensionsList() {
		if (inputExtensions == null) {
				inputExtensions = new ArrayList();
		}
		return inputExtensions;
	}

	/*
	 *  O B J E C T   S T A T E   M A I N T E N A N C E
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IInputType#isExtensionElement()
	 */
	public boolean isExtensionElement() {
		return isExtensionInputType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IInputType#isDirty()
	 */
	public boolean isDirty() {
		// This shouldn't be called for an extension InputType
 		if (isExtensionInputType) return false;
		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IInputType#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}
	
	/* (non-Javadoc)
	 *  Resolve the element IDs to interface references
	 */
	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
			// Resolve superClass
			if (superClassId != null && superClassId.length() > 0) {
				superClass = ManagedBuildManager.getExtensionInputType(superClassId);
				if (superClass == null) {
					// Report error
					ManagedBuildManager.OutputResolveError(
							"superClass",	//$NON-NLS-1$
							superClassId,
							"inputType",	//$NON-NLS-1$
							getId());
				}
			}
			
			// Resolve content types
			IContentTypeManager manager = Platform.getContentTypeManager();
			if (sourceContentTypeId != null && sourceContentTypeId.length() > 0) {
				sourceContentType = manager.getContentType(sourceContentTypeId);
			}
			if (dependencyContentTypeId != null && dependencyContentTypeId.length() > 0) {
				dependencyContentType = manager.getContentType(dependencyContentTypeId);
			}
			
			//  Call resolveReferences on our children
			Iterator typeIter = getInputOrderList().iterator();
			while (typeIter.hasNext()) {
				InputOrder current = (InputOrder)typeIter.next();
				current.resolveReferences();
			}
			typeIter = getAdditionalInputList().iterator();
			while (typeIter.hasNext()) {
				AdditionalInput current = (AdditionalInput)typeIter.next();
				current.resolveReferences();
			}
		}
	}

	/**
	 * @return Returns the managedBuildRevision.
	 */
	public String getManagedBuildRevision() {
		if ( managedBuildRevision == null) {
			if ( getParent() != null) {
				return getParent().getManagedBuildRevision();
			}
		}
		return managedBuildRevision;
	}

	/**
	 * @return Returns the version.
	 */
	public PluginVersionIdentifier getVersion() {
		if ( version == null) {
			if ( getParent() != null) {
				return getParent().getVersion();
			}
		}
		return version;
	}
	
	public void setVersion(PluginVersionIdentifier version) {
		// Do nothing
	}
}

/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Builder extends BuildObject implements IBuilder {

	private static final String EMPTY_STRING = new String();

	//  Superclass
	private IBuilder superClass;
	private String superClassId;
	//  Parent and children
	private IToolChain parent;
	//  Managed Build model attributes
	private String unusedChildren;
	private String errorParserIds;
	private Boolean isAbstract;
	private String command;
	private String args;
	private IConfigurationElement buildFileGeneratorElement;
	//  Miscellaneous
	private boolean isExtensionBuilder = false;
	private boolean isDirty = false;
	private boolean resolved = true;

	/*
	 *  C O N S T R U C T O R S
	 */
	
	/**
	 * This constructor is called to create a builder defined by an extension point in 
	 * a plugin manifest file, or returned by a dynamic element provider
	 * 
	 * @param parent  The IToolChain parent of this builder, or <code>null</code> if
	 *                defined at the top level
	 * @param element The builder definition from the manifest file or a dynamic element
	 *                provider
	 */
	public Builder(IToolChain parent, IManagedConfigElement element) {
		this.parent = parent;
		isExtensionBuilder = true;
		
		// setup for resolving
		resolved = false;

		loadFromManifest(element);
		
		// Hook me up to the Managed Build Manager
		ManagedBuildManager.addExtensionBuilder(this);
	}

	/**
	 * This constructor is called to create a Builder whose attributes and children will be 
	 * added by separate calls.
	 * 
	 * @param ToolChain The parent of the builder, if any
	 * @param Builder The superClass, if any
	 * @param String The id for the new tool chain
	 * @param String The name for the new tool chain
	 * @param boolean Indicates whether this is an extension element or a managed project element
	 */
	public Builder(ToolChain parent, IBuilder superClass, String Id, String name, boolean isExtensionElement) {
		this.parent = parent;
		this.superClass = superClass;
		if (this.superClass != null) {
			superClassId = this.superClass.getId();
		}
		setId(Id);
		setName(name);
		isExtensionBuilder = isExtensionElement;
		if (isExtensionElement) {
			// Hook me up to the Managed Build Manager
			ManagedBuildManager.addExtensionBuilder(this);
		} else {
			setDirty(true);
		}
	}

	/**
	 * Create a <code>Builder</code> based on the specification stored in the 
	 * project file (.cdtbuild).
	 * 
	 * @param parent The <code>IToolChain</code> the Builder will be added to. 
	 * @param element The XML element that contains the Builder settings.
	 */
	public Builder(IToolChain parent, Element element) {
		this.parent = parent;
		isExtensionBuilder = false;
		
		// Initialize from the XML attributes
		loadFromProject(element);
	}

	/**
	 * Create a <code>Builder</code> based upon an existing builder.
	 * 
	 * @param parent The <code>IToolChain</code> the builder will be added to. 
	 * @param builder The existing builder to clone.
	 */
	public Builder(IToolChain parent, String Id, String name, Builder builder) {
		this.parent = parent;
		superClass = builder.superClass;
		if (superClass != null) {
			if (builder.superClassId != null) {
				superClassId = new String(builder.superClassId);
			}
		}
		setId(Id);
		setName(name);
		isExtensionBuilder = false;
		
		//  Copy the remaining attributes
		if (builder.unusedChildren != null) {
			unusedChildren = new String(builder.unusedChildren);
		}
		if (builder.errorParserIds != null) {
			errorParserIds = new String(builder.errorParserIds);
		}
		if (builder.isAbstract != null) {
			isAbstract = new Boolean(builder.isAbstract.booleanValue());
		}
		if (builder.command != null) {
			command = new String(builder.command);
		}
		if (builder.args != null) {
			args = new String(builder.args);
		}
		buildFileGeneratorElement = builder.buildFileGeneratorElement; 
		
		setDirty(true);
	}

	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */
	
	/* (non-Javadoc)
	 * Loads the project-type information from the ManagedConfigElement specified in the 
	 * argument.
	 * 
	 * @param element Contains the tool-chain information 
	 */
	protected void loadFromManifest(IManagedConfigElement element) {
		ManagedBuildManager.putConfigElement(this, element);
		
		// id
		setId(element.getAttribute(IBuildObject.ID));
		
		// Get the name
		setName(element.getAttribute(IBuildObject.NAME));
		
		// superClass
		superClassId = element.getAttribute(IProjectType.SUPERCLASS);

		// Get the unused children, if any
		unusedChildren = element.getAttribute(IProjectType.UNUSED_CHILDREN); 
		
		// isAbstract
        String isAbs = element.getAttribute(IProjectType.IS_ABSTRACT);
        if (isAbs != null){
    		isAbstract = new Boolean("true".equals(isAbs)); //$NON-NLS-1$
        }

        // command
		command = element.getAttribute(IBuilder.COMMAND); 
        
        // arguments
		args = element.getAttribute(IBuilder.ARGUMENTS);
        
		// Get the semicolon separated list of IDs of the error parsers
		errorParserIds = element.getAttribute(IToolChain.ERROR_PARSERS);
		
		// Store the configuration element IFF there is a build file generator defined 
		String buildfileGenerator = element.getAttribute(BUILDFILEGEN_ID); 
		if (buildfileGenerator != null && element instanceof DefaultManagedConfigElement) {
			buildFileGeneratorElement = ((DefaultManagedConfigElement)element).getConfigurationElement();			
		}
	}
	
	/* (non-Javadoc)
	 * Initialize the builder information from the XML element 
	 * specified in the argument
	 * 
	 * @param element An XML element containing the builder information 
	 */
	protected void loadFromProject(Element element) {
		
		// id
		setId(element.getAttribute(IBuildObject.ID));

		// name
		if (element.hasAttribute(IBuildObject.NAME)) {
			setName(element.getAttribute(IBuildObject.NAME));
		}
		
		// superClass
		superClassId = element.getAttribute(IProjectType.SUPERCLASS);
		if (superClassId != null && superClassId.length() > 0) {
			superClass = ManagedBuildManager.getExtensionBuilder(superClassId);
			if (superClass == null) {
				// TODO:  Report error
			}
		}

		// Get the unused children, if any
		if (element.hasAttribute(IProjectType.UNUSED_CHILDREN)) {
				unusedChildren = element.getAttribute(IProjectType.UNUSED_CHILDREN); 
		}
		
		// isAbstract
		if (element.hasAttribute(IProjectType.IS_ABSTRACT)) {
			String isAbs = element.getAttribute(IProjectType.IS_ABSTRACT);
			if (isAbs != null){
				isAbstract = new Boolean("true".equals(isAbs)); //$NON-NLS-1$
			}
		}

        // command
		if (element.hasAttribute(IBuilder.COMMAND)) {
			command = element.getAttribute(IBuilder.COMMAND); 
		}
        
        // arguments
		if (element.hasAttribute(IBuilder.ARGUMENTS)) {
			args = element.getAttribute(IBuilder.ARGUMENTS);
		}
		
		// Get the semicolon separated list of IDs of the error parsers
		if (element.hasAttribute(IToolChain.ERROR_PARSERS)) {
			errorParserIds = element.getAttribute(IToolChain.ERROR_PARSERS);
		}
		
		// Note: build file generator cannot be specified in a project file because
		//       an IConfigurationElement is needed to load it!
		if (element.hasAttribute(IBuilder.BUILDFILEGEN_ID)) {
			// TODO:  Issue warning?
		}
	}

	/**
	 * Persist the builder to the project file.
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

		if (unusedChildren != null) {
			element.setAttribute(IProjectType.UNUSED_CHILDREN, unusedChildren);
		}
		
		if (isAbstract != null) {
			element.setAttribute(IProjectType.IS_ABSTRACT, isAbstract.toString());
		}

		if (errorParserIds != null) {
			element.setAttribute(IToolChain.ERROR_PARSERS, errorParserIds);
		}
		
		if (command != null) {
			element.setAttribute(IBuilder.COMMAND, command);
		}
		
		if (args != null) {
			element.setAttribute(IBuilder.ARGUMENTS, args);
		}

		// Note: build file generator cannot be specified in a project file because
		//       an IConfigurationElement is needed to load it!
		if (buildFileGeneratorElement != null) {
			//  TODO:  issue warning?
		}
		
		// I am clean now
		isDirty = false;
	}

	/*
	 *  P A R E N T   A N D   C H I L D   H A N D L I N G
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuilder#getParent()
	 */
	public IToolChain getParent() {
		return parent;
	}

	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#getSuperClass()
	 */
	public IBuilder getSuperClass() {
		return superClass;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuilder#getName()
	 */
	public String getName() {
		return (name == null && superClass != null) ? superClass.getName() : name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuilder#isAbstract()
	 */
	public boolean isAbstract() {
		if (isAbstract != null) {
			return isAbstract.booleanValue();
		} else {
			return false;	// Note: no inheritance from superClass
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuilder#getUnusedChildren()
	 */
	public String getUnusedChildren() {
		if (unusedChildren != null) {
			return unusedChildren;
		} else
			return EMPTY_STRING;	// Note: no inheritance from superClass
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuilder#getCommand()
	 */
	public String getCommand() {
		if (command == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.getCommand();
			} else {
				return new String("make"); //$NON-NLS-1$
			}
		}
		return command;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuilder#getArguments()
	 */
	public String getArguments() {
		if (args == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.getArguments();
			} else {
				return new String("-k"); //$NON-NLS-1$
			}
		}
		return args;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#getErrorParserIds()
	 */
	public String getErrorParserIds() {
		String ids = errorParserIds;
		if (ids == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				ids = superClass.getErrorParserIds();
			}
		}
		return ids;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#getErrorParserList()
	 */
	public String[] getErrorParserList() {
		String parserIDs = getErrorParserIds();
		String[] errorParsers = null;
		if (parserIDs != null) {
			// Check for an empty string
			if (parserIDs.length() == 0) {
				errorParsers = new String[0];
			} else {
				StringTokenizer tok = new StringTokenizer(parserIDs, ";"); //$NON-NLS-1$
				List list = new ArrayList(tok.countTokens());
				while (tok.hasMoreElements()) {
					list.add(tok.nextToken());
				}
				String[] strArr = {""};	//$NON-NLS-1$
				errorParsers = (String[]) list.toArray(strArr);
			}
		} else {
			errorParsers = new String[0];
		}
		return errorParsers;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuilder.setCommand(String)
	 */
	public void setCommand(String cmd) {
		if (cmd == null && command == null) return;
		if (command == null || cmd == null || !cmd.equals(command)) {
			command = cmd;
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuilder#setArguments(String)
	 */
	public void setArguments(String newArgs) {
		if (newArgs == null && args == null) return;
		if (args == null || newArgs == null || !newArgs.equals(args)) {
			args = newArgs;
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#setErrorParserIds()
	 */
	public void setErrorParserIds(String ids) {
		String currentIds = getErrorParserIds();
		if (ids == null && currentIds == null) return;
		if (currentIds == null || ids == null || !(currentIds.equals(ids))) {
			errorParserIds = ids;
			isDirty = true;
		}
	}

	/* (non-Javadoc)
	 * Sets the isAbstract attribute
	 */
	public void setIsAbstract(boolean b) {
		isAbstract = new Boolean(b);
		setDirty(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#getBuildFileGeneratorElement()
	 */
	public IConfigurationElement getBuildFileGeneratorElement() {
		if (buildFileGeneratorElement == null) {
			if (superClass != null) {
				return superClass.getBuildFileGeneratorElement();
			}
		}
		return buildFileGeneratorElement;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#setBuildFileGeneratorElement(String)
	 */
	public void setBuildFileGeneratorElement(IConfigurationElement element) {
		buildFileGeneratorElement = element;
		setDirty(true);
	}

	/*
	 *  O B J E C T   S T A T E   M A I N T E N A N C E
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#isExtensionElement()
	 */
	public boolean isExtensionElement() {
		return isExtensionBuilder;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#isDirty()
	 */
	public boolean isDirty() {
		// This shouldn't be called for an extension Builder
 		if (isExtensionBuilder) return false;
		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#setDirty(boolean)
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
				superClass = ManagedBuildManager.getExtensionBuilder(superClassId);
				if (superClass == null) {
					// TODO:  Report error
				}
			}
		}
	}
	
}

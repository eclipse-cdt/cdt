/*******************************************************************************
 * Copyright (c) 2005, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IAdditionalInput;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AdditionalInput implements IAdditionalInput {

	private static final String EMPTY_STRING = new String();

	//  Superclass
	//  Parent and children
	private IInputType parent;
	//  Managed Build model attributes
	private String paths;
	private Integer kind;
	//  Miscellaneous
	private boolean isExtensionAdditionalInput = false;
	private boolean isDirty = false;
	private boolean resolved = true;
	private boolean rebuildState;

	/*
	 *  C O N S T R U C T O R S
	 */
	
	/**
	 * This constructor is called to create an AdditionalInput defined by an extension point in 
	 * a plugin manifest file, or returned by a dynamic element provider
	 * 
	 * @param parent  The IInputType parent of this AdditionalInput
	 * @param element The AdditionalInput definition from the manifest file or a dynamic element
	 *                provider
	 */
	public AdditionalInput(IInputType parent, IManagedConfigElement element) {
		this.parent = parent;
		isExtensionAdditionalInput = true;
		
		// setup for resolving
		resolved = false;

		loadFromManifest(element);
	}

	/**
	 * This constructor is called to create an AdditionalInput whose attributes and children will be 
	 * added by separate calls.
	 * 
	 * @param InputType The parent of the an AdditionalInput
	 * @param boolean Indicates whether this is an extension element or a managed project element
	 */
	public AdditionalInput(InputType parent, boolean isExtensionElement) {
		this.parent = parent;
		isExtensionAdditionalInput = isExtensionElement;
		if (!isExtensionElement) {
			setDirty(true);
			setRebuildState(true);
		}
	}

	/**
	 * Create an <code>AdditionalInput</code> based on the specification stored in the 
	 * project file (.cdtbuild).
	 * 
	 * @param parent The <code>ITool</code> the AdditionalInput will be added to. 
	 * @param element The XML element that contains the AdditionalInput settings.
	 */
	public AdditionalInput(IInputType parent, Element element) {
		this.parent = parent;
		isExtensionAdditionalInput = false;
		
		// Initialize from the XML attributes
		loadFromProject(element);
	}

	/**
	 * Create an <code>AdditionalInput</code> based upon an existing AdditionalInput.
	 * 
	 * @param parent The <code>IInputType</code> the AdditionalInput will be added to.
	 * @param additionalInput The existing AdditionalInput to clone.
	 */
	public AdditionalInput(IInputType parent, AdditionalInput additionalInput) {
		this.parent = parent;
		isExtensionAdditionalInput = false;
		
		//  Copy the remaining attributes
		if (additionalInput.paths != null) {
			paths = new String(additionalInput.paths);
		}

		if (additionalInput.kind != null) {
			kind = new Integer(additionalInput.kind.intValue());
		}
		
		setDirty(true);
		setRebuildState(true);
	}

	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */
	
	/* (non-Javadoc)
	 * Loads the AdditionalInput information from the ManagedConfigElement specified in the 
	 * argument.
	 * 
	 * @param element Contains the AdditionalInput information 
	 */
	protected void loadFromManifest(IManagedConfigElement element) {

		// path
		paths = element.getAttribute(IAdditionalInput.PATHS); 

		// kind
		String kindStr = element.getAttribute(IAdditionalInput.KIND);
		if (kindStr == null || kindStr.equals(ADDITIONAL_INPUT_DEPENDENCY)) {
			kind = new Integer(KIND_ADDITIONAL_INPUT_DEPENDENCY);
		} else if (kindStr.equals(ADDITIONAL_INPUT)) {
			kind = new Integer(KIND_ADDITIONAL_INPUT);
		} else if (kindStr.equals(ADDITIONAL_DEPENDENCY)) {
			kind = new Integer(KIND_ADDITIONAL_DEPENDENCY);
		}
	}
	
	/* (non-Javadoc)
	 * Initialize the AdditionalInput information from the XML element 
	 * specified in the argument
	 * 
	 * @param element An XML element containing the AdditionalInput information 
	 */
	protected void loadFromProject(Element element) {
		
		// path
		if (element.hasAttribute(IAdditionalInput.PATHS)) {
			paths = element.getAttribute(IAdditionalInput.PATHS);
		}
		
		// kind
		if (element.hasAttribute(IAdditionalInput.KIND)) {
			String kindStr = element.getAttribute(IAdditionalInput.KIND);
			if (kindStr == null || kindStr.equals(ADDITIONAL_INPUT_DEPENDENCY)) {
				kind = new Integer(KIND_ADDITIONAL_INPUT_DEPENDENCY);
			} else if (kindStr.equals(ADDITIONAL_INPUT)) {
				kind = new Integer(KIND_ADDITIONAL_INPUT);
			} else if (kindStr.equals(ADDITIONAL_DEPENDENCY)) {
				kind = new Integer(KIND_ADDITIONAL_DEPENDENCY);
			}
		}
	}

	/**
	 * Persist the AdditionalInput to the project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(Document doc, Element element) {

		if (paths != null) {
			element.setAttribute(IAdditionalInput.PATHS, paths);
		}

		if (kind != null) {
			String str;
			switch (getKind()) {
				case KIND_ADDITIONAL_INPUT:
					str = ADDITIONAL_INPUT;
					break;
				case KIND_ADDITIONAL_DEPENDENCY:
					str = ADDITIONAL_DEPENDENCY;
					break;
				case KIND_ADDITIONAL_INPUT_DEPENDENCY:
					str = ADDITIONAL_INPUT_DEPENDENCY;
					break;
				default:
					str = EMPTY_STRING; 
					break;
			}
			element.setAttribute(IAdditionalInput.KIND, str);
		}
		
		// I am clean now
		isDirty = false;
	}

	/*
	 *  P A R E N T   A N D   C H I L D   H A N D L I N G
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IAdditionalInput#getParent()
	 */
	public IInputType getParent() {
		return parent;
	}

	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IAdditionalInput#getPaths()
	 */
	public String[] getPaths() {
		if (paths == null) {
			return null;
		}
		String[] nameTokens = paths.split(";"); //$NON-NLS-1$
		return nameTokens;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IAdditionalInput#setPaths()
	 */
	public void setPaths(String newPaths) {
		if (paths == null && newPaths == null) return;
		if (paths == null || newPaths == null || !(paths.equals(newPaths))) {
			paths = newPaths;
			isDirty = true;
			setRebuildState(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IAdditionalInput#getKind()
	 */
	public int getKind() {
		if (kind == null) {
			return KIND_ADDITIONAL_INPUT_DEPENDENCY;
		}
		return kind.intValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IAdditionalInput#setKind()
	 */
	public void setKind(int newKind) {
		if (kind == null || !(kind.intValue() == newKind)) {
			kind = new Integer(newKind);
			isDirty = true;
			setRebuildState(true);
		}
	}

	/*
	 *  O B J E C T   S T A T E   M A I N T E N A N C E
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IAdditionalInput#isExtensionElement()
	 */
	public boolean isExtensionElement() {
		return isExtensionAdditionalInput;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IAdditionalInput#isDirty()
	 */
	public boolean isDirty() {
		// This shouldn't be called for an extension AdditionalInput
 		if (isExtensionAdditionalInput) return false;
		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IAdditionalInput#setDirty(boolean)
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
		}
	}
	
	public boolean needsRebuild(){
		return rebuildState;
	}
	
	public void setRebuildState(boolean rebuild){
		if(isExtensionElement() && rebuild)
			return;

		rebuildState = rebuild;
	}

}

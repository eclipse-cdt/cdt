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

import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IInputOrder;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class InputOrder implements IInputOrder {

	private static final String EMPTY_STRING = new String();

	//  Superclass
	//  Parent and children
	private IInputType parent;
	//  Managed Build model attributes
	private String path;
	private String order;
	private Boolean excluded;
	//  Miscellaneous
	private boolean isExtensionInputOrder = false;
	private boolean isDirty = false;
	private boolean resolved = true;

	/*
	 *  C O N S T R U C T O R S
	 */
	
	/**
	 * This constructor is called to create an InputOrder defined by an extension point in 
	 * a plugin manifest file, or returned by a dynamic element provider
	 * 
	 * @param parent  The IInputType parent of this InputOrder
	 * @param element The InputOrder definition from the manifest file or a dynamic element
	 *                provider
	 */
	public InputOrder(IInputType parent, IManagedConfigElement element) {
		this.parent = parent;
		isExtensionInputOrder = true;
		
		// setup for resolving
		resolved = false;

		loadFromManifest(element);
	}

	/**
	 * This constructor is called to create an InputOrder whose attributes and children will be 
	 * added by separate calls.
	 * 
	 * @param InputType The parent of the an InputOrder
	 * @param boolean Indicates whether this is an extension element or a managed project element
	 */
	public InputOrder(InputType parent, boolean isExtensionElement) {
		this.parent = parent;
		isExtensionInputOrder = isExtensionElement;
		if (!isExtensionElement) {
			setDirty(true);
		}
	}

	/**
	 * Create an <code>InputOrder</code> based on the specification stored in the 
	 * project file (.cdtbuild).
	 * 
	 * @param parent The <code>ITool</code> the InputOrder will be added to. 
	 * @param element The XML element that contains the InputOrder settings.
	 */
	public InputOrder(IInputType parent, Element element) {
		this.parent = parent;
		isExtensionInputOrder = false;
		
		// Initialize from the XML attributes
		loadFromProject(element);
	}

	/**
	 * Create an <code>InputOrder</code> based upon an existing InputOrder.
	 * 
	 * @param parent The <code>IInputType</code> the InputOrder will be added to.
	 * @param inputOrder The existing InputOrder to clone.
	 */
	public InputOrder(IInputType parent, InputOrder inputOrder) {
		this.parent = parent;
		isExtensionInputOrder = false;
		
		//  Copy the remaining attributes
		if (inputOrder.path != null) {
			path = new String(inputOrder.path);
		}

		if (inputOrder.order != null) {
			order = new String(inputOrder.order);
		}

		if (inputOrder.excluded != null) {
			excluded = new Boolean(inputOrder.excluded.booleanValue());
		}
		
		setDirty(true);
	}

	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */
	
	/* (non-Javadoc)
	 * Loads the InputOrder information from the ManagedConfigElement specified in the 
	 * argument.
	 * 
	 * @param element Contains the InputOrder information 
	 */
	protected void loadFromManifest(IManagedConfigElement element) {

		// path
		path = element.getAttribute(IInputOrder.PATH); 

		// order
		order = element.getAttribute(IInputOrder.ORDER); 
		
		// excluded
        String isEx = element.getAttribute(IInputOrder.EXCLUDED);
        if (isEx != null){
    		excluded = new Boolean("true".equals(isEx)); //$NON-NLS-1$
        }
	}
	
	/* (non-Javadoc)
	 * Initialize the InputOrder information from the XML element 
	 * specified in the argument
	 * 
	 * @param element An XML element containing the InputOrder information 
	 */
	protected void loadFromProject(Element element) {
		
		// path
		if (element.hasAttribute(IInputOrder.PATH)) {
			path = element.getAttribute(IInputOrder.PATH);
		}
		
		// order
		if (element.hasAttribute(IInputOrder.ORDER)) {
			order = element.getAttribute(IInputOrder.ORDER);
		}
		
		// excluded
		if (element.hasAttribute(IInputOrder.EXCLUDED)) {
			String isEx = element.getAttribute(IInputOrder.EXCLUDED);
			if (isEx != null){
				excluded = new Boolean("true".equals(isEx)); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Persist the InputOrder to the project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(Document doc, Element element) {

		if (path != null) {
			element.setAttribute(IInputOrder.PATH, path);
		}

		if (order != null) {
			element.setAttribute(IInputOrder.ORDER, order);
		}
		
		if (excluded != null) {
			element.setAttribute(IInputOrder.EXCLUDED, excluded.toString());
		}
		
		// I am clean now
		isDirty = false;
	}

	/*
	 *  P A R E N T   A N D   C H I L D   H A N D L I N G
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputOrder#getParent()
	 */
	public IInputType getParent() {
		return parent;
	}

	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputOrder#getPsth()
	 */
	public String getPath() {
		return path;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputOrder#setPath()
	 */
	public void setPath(String newPath) {
		if (path == null && newPath == null) return;
		if (path == null || newPath == null || !(path.equals(newPath))) {
			path = newPath;
			isDirty = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputOrder#getOrder()
	 */
	public String getOrder() {
		return order;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputOrder#setOrder()
	 */
	public void setOrder(String newOrder) {
		if (order == null && newOrder == null) return;
		if (order == null || newOrder == null || !(order.equals(newOrder))) {
			order = newOrder;
			isDirty = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputOrder#getExcluded()
	 */
	public boolean getExcluded() {
		return excluded.booleanValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IInputOrder#setExcluded()
	 */
	public void setExcluded(boolean b) {
		if (excluded == null || !(b == excluded.booleanValue())) {
			excluded = new Boolean(b);
			setDirty(true);
		}
	}


	/*
	 *  O B J E C T   S T A T E   M A I N T E N A N C E
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IInputOrder#isExtensionElement()
	 */
	public boolean isExtensionElement() {
		return isExtensionInputOrder;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IInputOrder#isDirty()
	 */
	public boolean isDirty() {
		// This shouldn't be called for an extension InputOrder
 		if (isExtensionInputOrder) return false;
		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IInputOrder#setDirty(boolean)
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
	
}

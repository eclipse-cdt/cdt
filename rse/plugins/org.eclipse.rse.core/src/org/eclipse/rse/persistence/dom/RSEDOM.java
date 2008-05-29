/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David Dykstal (IBM) - [226561] Add API markup to RSE Javadocs where extend / implement is allowed
 *******************************************************************************/

package org.eclipse.rse.persistence.dom;

import org.eclipse.rse.core.model.ISystemProfile;

/**
 * This class is the root node of an RSE DOM.  Each
 * RSEDOM represents the properties of a profile to persist.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RSEDOM extends RSEDOMNode {

	/*
	 * Recommended for serializable objects. This should be updated if there is a schema change. 
	 */
	private static final long serialVersionUID = 1L;
	private transient ISystemProfile _profile;

	public RSEDOM(ISystemProfile profile) {
		super(null, IRSEDOMConstants.TYPE_PROFILE, profile.getName());
		_profile = profile;
	}

	public RSEDOM(String profileName) {
		super(null, IRSEDOMConstants.TYPE_PROFILE, profileName);
		_profile = null;
	}

	public ISystemProfile getProfile() {
		return _profile;
	}

	/**
	 * Indicate that this DOM needs to be saved
	 */
	public void markForSave() {
		if (!restoring && !_needsSave) {
			_needsSave = true;
		}
	}

	/**
	 * @return true if this DOM has the DOM changed since last saved or restored.
	 */
	public boolean needsSave() {
		return _needsSave;
	}

	public void print(RSEDOMNode node, String indent) {
		String type = node.getType();
		String name = node.getName();
		RSEDOMNodeAttribute[] attributes = node.getAttributes();
		RSEDOMNode[] children = node.getChildren();

		System.out.println(indent + "RSEDOMNode " + type); //$NON-NLS-1$
		System.out.println(indent + "{"); //$NON-NLS-1$
		String sindent = indent + "  "; //$NON-NLS-1$

		System.out.println(sindent + "name=" + name); //$NON-NLS-1$
		for (int i = 0; i < attributes.length; i++) {
			RSEDOMNodeAttribute attribute = attributes[i];
			String key = attribute.getKey();
			String value = attribute.getValue();
			System.out.println(sindent + key + "=" + value); //$NON-NLS-1$
		}

		String cindent = sindent + "    "; //$NON-NLS-1$
		for (int c = 0; c < children.length; c++) {
			RSEDOMNode child = children[c];
			print(child, cindent);
		}
		System.out.println(indent + "}"); //$NON-NLS-1$
	}

}

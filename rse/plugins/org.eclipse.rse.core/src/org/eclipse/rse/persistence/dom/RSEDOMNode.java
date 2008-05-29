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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RSEDOMNode implements Serializable {

	private static final long serialVersionUID = 1L; // This should be updated if there is a schema change.
	protected String _type;
	protected String _name;
	protected RSEDOMNode _parent;
	protected List _children;
	protected List _attributes;

	protected boolean _needsSave = false;
	protected boolean _isDirty = true;
	protected boolean restoring = false;

	public RSEDOMNode(RSEDOMNode parent, String type, String name) {
		_type = type;
		_name = name;
		_parent = parent;
		_children = new ArrayList();
		_attributes = new ArrayList();
		if (parent != null) {
			parent.addChild(this);
		}
	}

	public void markUpdated() {
		if (_needsSave) {
			_needsSave = false;

			for (int i = 0; i < _children.size(); i++) {
				RSEDOMNode child = (RSEDOMNode) _children.get(i);
				child.markUpdated();
			}
		}
	}

	/**
	 * Propagates the needs save indicator up to the root
	 */
	public void markForSave() {
		if (!restoring && !_needsSave) {
			_needsSave = true;
			_parent.markForSave();
		}
	}

	/**
	 * Recursively removes all the children from this node on down
	 *
	 */
	public void clearChildren() {
		RSEDOMNode[] children = getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].clearAttributes();
			children[i].clearChildren();
		}
		_children.clear();
	}

	/**
	 * Clears all attributes
	 * 
	 */
	public void clearAttributes() {
		_attributes.clear();
	}

	/**
	 * @return the name of this node
	 */
	public String getName() {
		return _name;
	}

	/**
	 * @return the type of this node
	 */
	public String getType() {
		return _type;
	}

	/**
	 * @return the parent of this node
	 */
	public RSEDOMNode getParent() {
		return _parent;
	}

	/**
	 * @return all the children of this node
	 */
	public RSEDOMNode[] getChildren() {
		return (RSEDOMNode[]) _children.toArray(new RSEDOMNode[_children.size()]);
	}

	/**
	 * @param key the name of this attribute
	 * @return the first attribute found that has the specified name
	 */
	public RSEDOMNodeAttribute getAttribute(String key) {
		for (int i = 0; i < _attributes.size(); i++) {
			RSEDOMNodeAttribute attribute = (RSEDOMNodeAttribute) _attributes.get(i);
			if (key.equals(attribute.getKey())) {
				return attribute;
			}
		}
		return null;
	}

	/**
	 * @param type
	 * @return the immediate children of this node that are of the specified type
	 */
	public RSEDOMNode[] getChildren(String type) {
		List results = new ArrayList();
		for (int i = 0; i < _children.size(); i++) {
			RSEDOMNode child = (RSEDOMNode) _children.get(i);
			if (type.equals(child.getType())) {
				results.add(child);
			}
		}
		return (RSEDOMNode[]) results.toArray(new RSEDOMNode[results.size()]);
	}

	/**
	 * @param type
	 * @param name
	 * @return the first immediate child of this node that is of the specified type and name
	 */
	public RSEDOMNode getChild(String type, String name) {
		for (int i = 0; i < _children.size(); i++) {
			RSEDOMNode child = (RSEDOMNode) _children.get(i);
			if (type.equals(child.getType()) && name.equals(child.getName())) {
				return child;
			}
		}
		return null;
	}

	/**
	 * @return all the attributes for this node
	 */
	public RSEDOMNodeAttribute[] getAttributes() {
		return (RSEDOMNodeAttribute[]) _attributes.toArray(new RSEDOMNodeAttribute[_attributes.size()]);
	}

	/**
	 * Adds a child to this node
	 * @param child
	 */
	public void addChild(RSEDOMNode child) {
		_children.add(child);
		markForSave();
	}

	/**
	 * Removes a child from this node
	 * @param child
	 */
	public void removeChild(RSEDOMNode child) {
		_children.remove(child);
		markForSave();
	}

	/**
	 * Adds an attribute to the node
	 * @param name
	 * @param value
	 * @param type
	 */
	public void addAttribute(String name, String value, String type) {
		RSEDOMNodeAttribute attr = new RSEDOMNodeAttribute(name, value, type);
		_attributes.add(attr);
		markForSave();
	}

	/**
	 * Adds an attribute to the node
	 * @param name
	 * @param value
	 */
	public void addAttribute(String name, String value) {
		RSEDOMNodeAttribute attr = new RSEDOMNodeAttribute(name, value);
		_attributes.add(attr);
		markForSave();
	}

	public boolean isDirty() {
		return _isDirty;
	}

	public void setDirty(boolean isDirty) {
		_isDirty = isDirty;
	}

	public void setRestoring(boolean restoring) {
		this.restoring = restoring;
	}
	
	public void setName(String name) {
		_name = name;
	}
	
	public void setType(String type) {
		_type = type;
	}

}

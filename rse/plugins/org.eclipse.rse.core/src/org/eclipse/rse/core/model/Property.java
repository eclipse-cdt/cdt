/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David Dykstal (IBM) - added javadoc
 * David Dykstal (IBM) - [150939] added read-only attribute
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 ********************************************************************************/

package org.eclipse.rse.core.model;

import java.util.Observable;

/**
 * A Property is used in PropertySets and may be persisted as a result of persisting the 
 * containing set. Each property has a type, a label, a key and a value.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class Property extends Observable implements IProperty {
	
	private String _name;
	private String _label;
	private String _value;
	private IPropertyType _type;
	private boolean _isEnabled = true;
	private boolean _isReadOnly = false;

	public Property(IProperty property) {
		_name = property.getKey();
		_label = property.getLabel();
		_value = property.getValue();
		_type = property.getType();
		_isEnabled = property.isEnabled();
		touch();
	}

	public Property(String name, String value, IPropertyType type, boolean isEnabled) {
		_name = name;
		_value = value;
		_type = type;
		_isEnabled = isEnabled;
		touch();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IProperty#getKey()
	 */
	public String getKey() {
		return _name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IProperty#setLabel(java.lang.String)
	 */
	public void setLabel(String label) {
		if (!stringsAreEqual(_label, label)) {
			_label = label;
			touch();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IProperty#getLabel()
	 */
	public String getLabel() {
		if (_label == null) {
			return _name;
		}
		return _label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IProperty#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		if (!stringsAreEqual(_value, value)) {
			_value = value;
			touch();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IProperty#getValue()
	 */
	public String getValue() {
		return _value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IProperty#setType(org.eclipse.rse.core.model.IPropertyType)
	 */
	public void setType(IPropertyType type) {
		if (_type != type) {
			_type = type;
			touch();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IProperty#getType()
	 */
	public IPropertyType getType() {
		return _type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IProperty#setEnabled(boolean)
	 */
	public void setEnabled(boolean flag) {
		if (_isEnabled != flag) {
			_isEnabled = flag;
			touch();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IProperty#isEnabled()
	 */
	public boolean isEnabled() {
		return _isEnabled;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IProperty#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean flag) {
		if (_isReadOnly != flag) {
			_isReadOnly = flag;
			touch();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IProperty#isReadOnly()
	 */
	public boolean isReadOnly() {
		return _isReadOnly;
	}
	
	private boolean stringsAreEqual(String s1, String s2) {
		if (s1 == s2) return true;
		if (s1 == null) return false;
		return s1.equals(s2);
	}
	
	private void touch() {
		setChanged();
		notifyObservers();
	}

}
/********************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 ********************************************************************************/

package org.eclipse.rse.core.model;

/**
 * Properties are contained in property sets ({@link IPropertySet}) and may be
 * associated with objects that implement {@link IPropertySetContainer}. These
 * would typically be model objects. Properties also have a type (see
 * {@link IPropertyType}).
 *
 * @see IRSEModelObject
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients. Use
 *           {@link Property} directly.
 */
public interface IProperty {

	/**
	 * @return the name of the property.
	 */
	public String getKey();

	/**
	 * Sets the displayable label of the property.
	 * @param label the label for this property.
	 */
	public void setLabel(String label);

	/**
	 * @return the displayable label of this property
	 */
	public String getLabel();

	/**
	 * Sets the value of this property.
	 * May raise a runtime exception if the new value of the property is
	 * not compatible with its type.
	 * @param value the new value for this property.
	 */
	public void setValue(String value);

	/**
	 * @return the value of this property
	 */
	public String getValue();

	/**
	 * Sets the type of this property. May raise an runtime exception if the
	 * value of the property is not compatible with the new type.
	 *
	 * @param type the property type
	 */
	public void setType(IPropertyType type);

	/**
	 * @return the type of this property
	 */
	public IPropertyType getType();

	/**
	 * Sets the "enabled" presentation attribute of this property.
	 * This is an attribute that can be used to drive the presentation of this
	 * property and does not otherwise affect how this property can be used.
	 * Properties are enabled by default.
	 * @param flag true if the property is to be enabled.
	 */
	public void setEnabled(boolean flag);

	/**
	 * Retrieves the "enabled" presentation attribute of this property.
	 * This is an attribute that can be used to drive the presentation of this
	 * property and does not otherwise affect how this property can be used.
	 * @return true if the property is enabled.
	 */
	public boolean isEnabled();

	/**
	 * Sets the "read-only" presentation attribute of this property.
	 * This is an attribute that can be used to drive the presentation of this
	 * property and does not otherwise affect how this property can be used.
	 * Properties are read-write by default.
	 * @param flag true if the property is to be read-only.
	 */
	public void setReadOnly(boolean flag);

	/**
	 * Retrieves the "read-only" presentation attribute of this property.
	 * This is an attribute that can be used to drive the presentation of this
	 * property and does not otherwise affect how this property can be used.
	 * @return true if the property is read-only.
	 */
	public boolean isReadOnly();

}
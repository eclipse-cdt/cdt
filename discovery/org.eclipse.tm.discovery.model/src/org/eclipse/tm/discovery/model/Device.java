/********************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 ********************************************************************************/

package org.eclipse.tm.discovery.model;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * 
 * A representation of the model object '<em><b>Device</b></em>'.
 * 
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.tm.discovery.model.Device#getServiceType <em>Service Type</em>}</li>
 *   <li>{@link org.eclipse.tm.discovery.model.Device#getAddress <em>Address</em>}</li>
 *   <li>{@link org.eclipse.tm.discovery.model.Device#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.tm.discovery.model.ModelPackage#getDevice()
 * @model extendedMetaData="name='Device' kind='elementOnly'"
 * @generated
 */
public interface Device extends EObject {
	/**
	 * Returns the value of the '<em><b>Service Type</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.tm.discovery.model.ServiceType}.
	 * 
	 * @return the value of the '<em>Service Type</em>' containment reference list.
	 * @see org.eclipse.tm.discovery.model.ModelPackage#getDevice_ServiceType()
	 * @model type="org.eclipse.tm.discovery.model.ServiceType" containment="true" required="true"
	 *        extendedMetaData="kind='element' name='ServiceType'"
	 * @generated
	 */
	EList getServiceType();

	/**
	 * Returns the value of the '<em><b>Address</b></em>' attribute.
	 *
	 * @return the value of the '<em>Address</em>' attribute.
	 * @see #setAddress(String)
	 * @see org.eclipse.tm.discovery.model.ModelPackage#getDevice_Address()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='attribute' name='address'"
	 * @generated
	 */
	String getAddress();

	/**
	 * Sets the value of the '{@link org.eclipse.tm.discovery.model.Device#getAddress <em>Address</em>}' attribute.
	 * 
	 * @param value the new value of the '<em>Address</em>' attribute.
	 * @see #getAddress()
	 * @generated
	 */
	void setAddress(String value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * 
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.tm.discovery.model.ModelPackage#getDevice_Name()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='attribute' name='name'"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.tm.discovery.model.Device#getName <em>Name</em>}' attribute.
	 * 
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

} // Device
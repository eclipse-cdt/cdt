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
 * A representation of the model object '<em><b>Service Type</b></em>'.
 * 
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.tm.discovery.model.ServiceType#getService <em>Service</em>}</li>
 *   <li>{@link org.eclipse.tm.discovery.model.ServiceType#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.tm.discovery.model.ModelPackage#getServiceType()
 * @model extendedMetaData="name='ServiceType' kind='elementOnly'"
 * @generated
 */
public interface ServiceType extends EObject {
	/**
	 * Returns the value of the '<em><b>Service</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.tm.discovery.model.Service}.
	 * 
	 * @return the value of the '<em>Service</em>' containment reference list.
	 * @see org.eclipse.tm.discovery.model.ModelPackage#getServiceType_Service()
	 * @model type="org.eclipse.tm.discovery.model.Service" containment="true" required="true"
	 *        extendedMetaData="kind='element' name='Service'"
	 * @generated
	 */
	EList getService();

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * 
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.tm.discovery.model.ModelPackage#getServiceType_Name()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='attribute' name='name'"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.tm.discovery.model.ServiceType#getName <em>Name</em>}' attribute.
	 * 
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

} // ServiceType
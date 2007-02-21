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
 * A representation of the model object '<em><b>Service</b></em>'.
 * 
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.tm.discovery.model.Service#getPair <em>Pair</em>}</li>
 *   <li>{@link org.eclipse.tm.discovery.model.Service#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.tm.discovery.model.ModelPackage#getService()
 * @model extendedMetaData="name='Service' kind='elementOnly'"
 * @generated
 */
public interface Service extends EObject {
	/**
	 * Returns the value of the '<em><b>Pair</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.tm.discovery.model.Pair}.
	 * 
	 * @return the value of the '<em>Pair</em>' containment reference list.
	 * @see org.eclipse.tm.discovery.model.ModelPackage#getService_Pair()
	 * @model type="org.eclipse.tm.discovery.model.Pair" containment="true"
	 *        extendedMetaData="kind='element' name='Pair'"
	 * @generated
	 */
	EList getPair();

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * 
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.tm.discovery.model.ModelPackage#getService_Name()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='attribute' name='name'"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.tm.discovery.model.Service#getName <em>Name</em>}' attribute.
	 * 
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

} // Service
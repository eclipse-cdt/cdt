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

import org.eclipse.emf.ecore.EObject;

/**
 * 
 * A representation of the model object '<em><b>Pair</b></em>'.
 * 
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.tm.discovery.model.Pair#getKey <em>Key</em>}</li>
 *   <li>{@link org.eclipse.tm.discovery.model.Pair#getValue <em>Value</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.tm.discovery.model.ModelPackage#getPair()
 * @model extendedMetaData="name='Pair' kind='empty'"
 * @generated
 */
public interface Pair extends EObject {
	/**
	 * Returns the value of the '<em><b>Key</b></em>' attribute.
	 * 
	 * @return the value of the '<em>Key</em>' attribute.
	 * @see #setKey(String)
	 * @see org.eclipse.tm.discovery.model.ModelPackage#getPair_Key()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='attribute' name='key'"
	 * @generated
	 */
	String getKey();

	/**
	 * Sets the value of the '{@link org.eclipse.tm.discovery.model.Pair#getKey <em>Key</em>}' attribute.
	 * 	 
	 * @param value the new value of the '<em>Key</em>' attribute.
	 * @see #getKey()
	 * @generated
	 */
	void setKey(String value);

	/**
	 * Returns the value of the '<em><b>Value</b></em>' attribute.
	 * 
	 * @return the value of the '<em>Value</em>' attribute.
	 * @see #setValue(String)
	 * @see org.eclipse.tm.discovery.model.ModelPackage#getPair_Value()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='attribute' name='value'"
	 * @generated
	 */
	String getValue();

	/**
	 * Sets the value of the '{@link org.eclipse.tm.discovery.model.Pair#getValue <em>Value</em>}' attribute.
	 * 
	 * @param value the new value of the '<em>Value</em>' attribute.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(String value);

} // Pair
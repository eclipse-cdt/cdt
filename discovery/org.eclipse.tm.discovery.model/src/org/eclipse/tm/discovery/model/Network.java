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
 * A representation of the model object '<em><b>Network</b></em>'.
 * 
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.tm.discovery.model.Network#getDevice <em>Device</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.tm.discovery.model.ModelPackage#getNetwork()
 * @model extendedMetaData="name='Network' kind='elementOnly'"
 * @generated
 */
public interface Network extends EObject {
	/**
	 * Returns the value of the '<em><b>Device</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.tm.discovery.model.Device}.
	 * 
	 * @return the value of the '<em>Device</em>' containment reference list.
	 * @see org.eclipse.tm.discovery.model.ModelPackage#getNetwork_Device()
	 * @model type="org.eclipse.tm.discovery.model.Device" containment="true" required="true"
	 *        extendedMetaData="kind='element' name='Device'"
	 * @generated
	 */
	EList getDevice();

} // Network
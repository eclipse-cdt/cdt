/********************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 ********************************************************************************/

package org.eclipse.tm.internal.discovery.model.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.tm.discovery.model.Device;
import org.eclipse.tm.discovery.model.ModelPackage;
import org.eclipse.tm.discovery.model.Network;

/**
 * 
 * An implementation of the model object '<em><b>Network</b></em>'.
 * 
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.tm.internal.discovery.model.impl.NetworkImpl#getDevice <em>Device</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class NetworkImpl extends EObjectImpl implements Network {
	/**
	 * The cached value of the '{@link #getDevice() <em>Device</em>}' containment reference list.
	 * 
	 * @see #getDevice()
	 * @generated
	 * @ordered
	 */
	protected EList device = null;

	/**
	 * 
	 * @generated
	 */
	protected NetworkImpl() {
		super();
	}

	/**
	 * 
	 * @generated
	 */
	protected EClass eStaticClass() {
		return ModelPackage.Literals.NETWORK;
	}

	/**
	 * 
	 * @generated
	 */
	public EList getDevice() {
		if (device == null) {
			device = new EObjectContainmentEList(Device.class, this, ModelPackage.NETWORK__DEVICE);
		}
		return device;
	}

	/**
	 * 
	 * @generated
	 */
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ModelPackage.NETWORK__DEVICE:
				return ((InternalEList)getDevice()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 *
	 * @generated
	 */
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelPackage.NETWORK__DEVICE:
				return getDevice();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * 
	 * @generated
	 */
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ModelPackage.NETWORK__DEVICE:
				getDevice().clear();
				getDevice().addAll((Collection)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 *
	 * @generated
	 */
	public void eUnset(int featureID) {
		switch (featureID) {
			case ModelPackage.NETWORK__DEVICE:
				getDevice().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * 
	 * @generated
	 */
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ModelPackage.NETWORK__DEVICE:
				return device != null && !device.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //NetworkImpl
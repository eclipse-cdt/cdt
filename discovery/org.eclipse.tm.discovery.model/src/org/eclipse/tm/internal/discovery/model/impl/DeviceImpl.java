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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.tm.discovery.model.Device;
import org.eclipse.tm.discovery.model.ModelPackage;
import org.eclipse.tm.discovery.model.ServiceType;

/**
 * 
 * An implementation of the model object '<em><b>Device</b></em>'.
 * 
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.tm.internal.discovery.model.impl.DeviceImpl#getServiceType <em>Service Type</em>}</li>
 *   <li>{@link org.eclipse.tm.internal.discovery.model.impl.DeviceImpl#getAddress <em>Address</em>}</li>
 *   <li>{@link org.eclipse.tm.internal.discovery.model.impl.DeviceImpl#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DeviceImpl extends EObjectImpl implements Device {
	/**
	 * The cached value of the '{@link #getServiceType() <em>Service Type</em>}' containment reference list.
	 * 
	 * @see #getServiceType()
	 * @generated
	 * @ordered
	 */
	protected EList serviceType = null;

	/**
	 * The default value of the '{@link #getAddress() <em>Address</em>}' attribute.
	 * 
	 * @see #getAddress()
	 * @generated
	 * @ordered
	 */
	protected static final String ADDRESS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getAddress() <em>Address</em>}' attribute.
	 * 
	 * @see #getAddress()
	 * @generated
	 * @ordered
	 */
	protected String address = ADDRESS_EDEFAULT;

	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * 
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * 
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * 
	 * @generated
	 */
	protected DeviceImpl() {
		super();
	}

	/**
	 * 
	 * @generated
	 */
	protected EClass eStaticClass() {
		return ModelPackage.Literals.DEVICE;
	}

	/**
	 * 
	 * @generated
	 */
	public EList getServiceType() {
		if (serviceType == null) {
			serviceType = new EObjectContainmentEList(ServiceType.class, this, ModelPackage.DEVICE__SERVICE_TYPE);
		}
		return serviceType;
	}

	/**
	 * 
	 * @generated
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * 
	 * @generated
	 */
	public void setAddress(String newAddress) {
		String oldAddress = address;
		address = newAddress;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.DEVICE__ADDRESS, oldAddress, address));
	}

	/**
	 * 
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @generated
	 */
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.DEVICE__NAME, oldName, name));
	}

	/**
	 * 
	 * @generated
	 */
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ModelPackage.DEVICE__SERVICE_TYPE:
				return ((InternalEList)getServiceType()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * 
	 * @generated
	 */
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelPackage.DEVICE__SERVICE_TYPE:
				return getServiceType();
			case ModelPackage.DEVICE__ADDRESS:
				return getAddress();
			case ModelPackage.DEVICE__NAME:
				return getName();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * 
	 * @generated
	 */
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ModelPackage.DEVICE__SERVICE_TYPE:
				getServiceType().clear();
				getServiceType().addAll((Collection)newValue);
				return;
			case ModelPackage.DEVICE__ADDRESS:
				setAddress((String)newValue);
				return;
			case ModelPackage.DEVICE__NAME:
				setName((String)newValue);
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
			case ModelPackage.DEVICE__SERVICE_TYPE:
				getServiceType().clear();
				return;
			case ModelPackage.DEVICE__ADDRESS:
				setAddress(ADDRESS_EDEFAULT);
				return;
			case ModelPackage.DEVICE__NAME:
				setName(NAME_EDEFAULT);
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
			case ModelPackage.DEVICE__SERVICE_TYPE:
				return serviceType != null && !serviceType.isEmpty();
			case ModelPackage.DEVICE__ADDRESS:
				return ADDRESS_EDEFAULT == null ? address != null : !ADDRESS_EDEFAULT.equals(address);
			case ModelPackage.DEVICE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * 
	 * @generated
	 */
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (address: "); //$NON-NLS-1$
		result.append(address);
		result.append(", name: "); //$NON-NLS-1$
		result.append(name);
		result.append(')');
		return result.toString();
	}

} //DeviceImpl
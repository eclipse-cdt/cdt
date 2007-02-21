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

import org.eclipse.tm.discovery.model.ModelPackage;
import org.eclipse.tm.discovery.model.Service;
import org.eclipse.tm.discovery.model.ServiceType;

/**
 * 
 * An implementation of the model object '<em><b>Service Type</b></em>'.
 * 
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.tm.internal.discovery.model.impl.ServiceTypeImpl#getService <em>Service</em>}</li>
 *   <li>{@link org.eclipse.tm.internal.discovery.model.impl.ServiceTypeImpl#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ServiceTypeImpl extends EObjectImpl implements ServiceType {
	/**
	 * The cached value of the '{@link #getService() <em>Service</em>}' containment reference list.
	 * 
	 * @see #getService()
	 * @generated
	 * @ordered
	 */
	protected EList service = null;

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
	protected ServiceTypeImpl() {
		super();
	}

	/**
	 * 
	 * @generated
	 */
	protected EClass eStaticClass() {
		return ModelPackage.Literals.SERVICE_TYPE;
	}

	/**
	 * 
	 * @generated
	 */
	public EList getService() {
		if (service == null) {
			service = new EObjectContainmentEList(Service.class, this, ModelPackage.SERVICE_TYPE__SERVICE);
		}
		return service;
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
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.SERVICE_TYPE__NAME, oldName, name));
	}

	/**
	 * 
	 * @generated
	 */
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ModelPackage.SERVICE_TYPE__SERVICE:
				return ((InternalEList)getService()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * 
	 * @generated
	 */
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelPackage.SERVICE_TYPE__SERVICE:
				return getService();
			case ModelPackage.SERVICE_TYPE__NAME:
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
			case ModelPackage.SERVICE_TYPE__SERVICE:
				getService().clear();
				getService().addAll((Collection)newValue);
				return;
			case ModelPackage.SERVICE_TYPE__NAME:
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
			case ModelPackage.SERVICE_TYPE__SERVICE:
				getService().clear();
				return;
			case ModelPackage.SERVICE_TYPE__NAME:
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
			case ModelPackage.SERVICE_TYPE__SERVICE:
				return service != null && !service.isEmpty();
			case ModelPackage.SERVICE_TYPE__NAME:
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
		result.append(" (name: "); //$NON-NLS-1$
		result.append(name);
		result.append(')');
		return result.toString();
	}

} //ServiceTypeImpl
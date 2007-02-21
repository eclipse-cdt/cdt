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
import org.eclipse.tm.discovery.model.Pair;
import org.eclipse.tm.discovery.model.Service;

/**
 *
 * An implementation of the model object '<em><b>Service</b></em>'.
 * 
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.tm.internal.discovery.model.impl.ServiceImpl#getPair <em>Pair</em>}</li>
 *   <li>{@link org.eclipse.tm.internal.discovery.model.impl.ServiceImpl#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ServiceImpl extends EObjectImpl implements Service {
	/**
	 * The cached value of the '{@link #getPair() <em>Pair</em>}' containment reference list.
	 * 
	 * @see #getPair()
	 * @generated
	 * @ordered
	 */
	protected EList pair = null;

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
	protected ServiceImpl() {
		super();
	}

	/**
	 * 
	 * @generated
	 */
	protected EClass eStaticClass() {
		return ModelPackage.Literals.SERVICE;
	}

	/**
	 * 
	 * @generated
	 */
	public EList getPair() {
		if (pair == null) {
			pair = new EObjectContainmentEList(Pair.class, this, ModelPackage.SERVICE__PAIR);
		}
		return pair;
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
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.SERVICE__NAME, oldName, name));
	}

	/**
	 *
	 * @generated
	 */
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ModelPackage.SERVICE__PAIR:
				return ((InternalEList)getPair()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 *
	 * @generated
	 */
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelPackage.SERVICE__PAIR:
				return getPair();
			case ModelPackage.SERVICE__NAME:
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
			case ModelPackage.SERVICE__PAIR:
				getPair().clear();
				getPair().addAll((Collection)newValue);
				return;
			case ModelPackage.SERVICE__NAME:
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
			case ModelPackage.SERVICE__PAIR:
				getPair().clear();
				return;
			case ModelPackage.SERVICE__NAME:
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
			case ModelPackage.SERVICE__PAIR:
				return pair != null && !pair.isEmpty();
			case ModelPackage.SERVICE__NAME:
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

} //ServiceImpl
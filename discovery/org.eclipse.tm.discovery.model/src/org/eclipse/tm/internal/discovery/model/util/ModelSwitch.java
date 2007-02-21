/********************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 ********************************************************************************/

package org.eclipse.tm.internal.discovery.model.util;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.tm.discovery.model.*;

/**
 * 
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * 
 * @see org.eclipse.tm.discovery.model.ModelPackage
 * @generated
 */
public class ModelSwitch {
	/**
	 * The cached model package
	 * 
	 * @generated
	 */
	protected static ModelPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * 
	 * @generated
	 */
	public ModelSwitch() {
		if (modelPackage == null) {
			modelPackage = ModelPackage.eINSTANCE;
		}
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * @param theEObject 
	 * 
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	public Object doSwitch(EObject theEObject) {
		return doSwitch(theEObject.eClass(), theEObject);
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * 
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	protected Object doSwitch(EClass theEClass, EObject theEObject) {
		if (theEClass.eContainer() == modelPackage) {
			return doSwitch(theEClass.getClassifierID(), theEObject);
		}
		else {
			List eSuperTypes = theEClass.getESuperTypes();
			return
				eSuperTypes.isEmpty() ?
					defaultCase(theEObject) :
					doSwitch((EClass)eSuperTypes.get(0), theEObject);
		}
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * 
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	protected Object doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case ModelPackage.DEVICE: {
				Device device = (Device)theEObject;
				Object result = caseDevice(device);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.NETWORK: {
				Network network = (Network)theEObject;
				Object result = caseNetwork(network);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.PAIR: {
				Pair pair = (Pair)theEObject;
				Object result = casePair(pair);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.SERVICE: {
				Service service = (Service)theEObject;
				Object result = caseService(service);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.SERVICE_TYPE: {
				ServiceType serviceType = (ServiceType)theEObject;
				Object result = caseServiceType(serviceType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpretting the object as an instance of '<em>Device</em>'.
	 *
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * 
	 * @param object the target of the switch.
	 * @return the result of interpretting the object as an instance of '<em>Device</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public Object caseDevice(Device object) {
		return null;
	}

	/**
	 * Returns the result of interpretting the object as an instance of '<em>Network</em>'.
	 * 
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * 
	 * @param object the target of the switch.
	 * @return the result of interpretting the object as an instance of '<em>Network</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public Object caseNetwork(Network object) {
		return null;
	}

	/**
	 * Returns the result of interpretting the object as an instance of '<em>Pair</em>'.
	 * 
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * 
	 * @param object the target of the switch.
	 * @return the result of interpretting the object as an instance of '<em>Pair</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public Object casePair(Pair object) {
		return null;
	}

	/**
	 * Returns the result of interpretting the object as an instance of '<em>Service</em>'.
	 * 
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * 
	 * @param object the target of the switch.
	 * @return the result of interpretting the object as an instance of '<em>Service</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public Object caseService(Service object) {
		return null;
	}

	/**
	 * Returns the result of interpretting the object as an instance of '<em>Service Type</em>'.
	 * 
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * 
	 * @param object the target of the switch.
	 * @return the result of interpretting the object as an instance of '<em>Service Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public Object caseServiceType(ServiceType object) {
		return null;
	}

	/**
	 * Returns the result of interpretting the object as an instance of '<em>EObject</em>'.
	 * 
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * 
	 * @param object the target of the switch.
	 * @return the result of interpretting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	public Object defaultCase(EObject object) {
		return null;
	}

} //ModelSwitch

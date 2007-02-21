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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * 
 * @see org.eclipse.tm.discovery.model.ModelFactory
 * @model kind="package"
 * @generated
 */
public interface ModelPackage extends EPackage {
	/**
	 * The package name.
	 * 
	 * @generated
	 */
	String eNAME = "model"; //$NON-NLS-1$

	/**
	 * The package namespace URI.
	 * 
	 * @generated
	 */
	String eNS_URI = "http://www.eclipse.org/tm/discovery/model"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * 
	 * @generated
	 */
	String eNS_PREFIX = "model"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * 
	 * @generated
	 */
	ModelPackage eINSTANCE = org.eclipse.tm.internal.discovery.model.impl.ModelPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.tm.internal.discovery.model.impl.DeviceImpl <em>Device</em>}' class.
	 * 
	 * @see org.eclipse.tm.internal.discovery.model.impl.DeviceImpl
	 * @see org.eclipse.tm.internal.discovery.model.impl.ModelPackageImpl#getDevice()
	 * @generated
	 */
	int DEVICE = 0;

	/**
	 * The feature id for the '<em><b>Service Type</b></em>' containment reference list.
	 * 
	 * @generated
	 * @ordered
	 */
	int DEVICE__SERVICE_TYPE = 0;

	/**
	 * The feature id for the '<em><b>Address</b></em>' attribute.
	 * 
	 * @generated
	 * @ordered
	 */
	int DEVICE__ADDRESS = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * 
	 * @generated
	 * @ordered
	 */
	int DEVICE__NAME = 2;

	/**
	 * The number of structural features of the '<em>Device</em>' class.
	 * 
	 * @generated
	 * @ordered
	 */
	int DEVICE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link org.eclipse.tm.internal.discovery.model.impl.NetworkImpl <em>Network</em>}' class.
	 * 
	 * @see org.eclipse.tm.internal.discovery.model.impl.NetworkImpl
	 * @see org.eclipse.tm.internal.discovery.model.impl.ModelPackageImpl#getNetwork()
	 * @generated
	 */
	int NETWORK = 1;

	/**
	 * The feature id for the '<em><b>Device</b></em>' containment reference list.
	 * 
	 * @generated
	 * @ordered
	 */
	int NETWORK__DEVICE = 0;

	/**
	 * The number of structural features of the '<em>Network</em>' class.
	 * 
	 * @generated
	 * @ordered
	 */
	int NETWORK_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.eclipse.tm.internal.discovery.model.impl.PairImpl <em>Pair</em>}' class.
	 * 
	 * @see org.eclipse.tm.internal.discovery.model.impl.PairImpl
	 * @see org.eclipse.tm.internal.discovery.model.impl.ModelPackageImpl#getPair()
	 * @generated
	 */
	int PAIR = 2;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * 
	 * @generated
	 * @ordered
	 */
	int PAIR__KEY = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * 
	 * @generated
	 * @ordered
	 */
	int PAIR__VALUE = 1;

	/**
	 * The number of structural features of the '<em>Pair</em>' class.
	 * 
	 * @generated
	 * @ordered
	 */
	int PAIR_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.tm.internal.discovery.model.impl.ServiceImpl <em>Service</em>}' class.
	 * 
	 * @see org.eclipse.tm.internal.discovery.model.impl.ServiceImpl
	 * @see org.eclipse.tm.internal.discovery.model.impl.ModelPackageImpl#getService()
	 * @generated
	 */
	int SERVICE = 3;

	/**
	 * The feature id for the '<em><b>Pair</b></em>' containment reference list.
	 * 
	 * @generated
	 * @ordered
	 */
	int SERVICE__PAIR = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * 
	 * @generated
	 * @ordered
	 */
	int SERVICE__NAME = 1;

	/**
	 * The number of structural features of the '<em>Service</em>' class.
	 * 
	 * @generated
	 * @ordered
	 */
	int SERVICE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.tm.internal.discovery.model.impl.ServiceTypeImpl <em>Service Type</em>}' class.
	 * 
	 * @see org.eclipse.tm.internal.discovery.model.impl.ServiceTypeImpl
	 * @see org.eclipse.tm.internal.discovery.model.impl.ModelPackageImpl#getServiceType()
	 * @generated
	 */
	int SERVICE_TYPE = 4;

	/**
	 * The feature id for the '<em><b>Service</b></em>' containment reference list.
	 * 
	 * @generated
	 * @ordered
	 */
	int SERVICE_TYPE__SERVICE = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * 
	 * @generated
	 * @ordered
	 */
	int SERVICE_TYPE__NAME = 1;

	/**
	 * The number of structural features of the '<em>Service Type</em>' class.
	 * 
	 * @generated
	 * @ordered
	 */
	int SERVICE_TYPE_FEATURE_COUNT = 2;


	/**
	 * Returns the meta object for class '{@link org.eclipse.tm.discovery.model.Device <em>Device</em>}'.
	 * 
	 * @return the meta object for class '<em>Device</em>'.
	 * @see org.eclipse.tm.discovery.model.Device
	 * @generated
	 */
	EClass getDevice();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.tm.discovery.model.Device#getServiceType <em>Service Type</em>}'.
	 * 
	 * @return the meta object for the containment reference list '<em>Service Type</em>'.
	 * @see org.eclipse.tm.discovery.model.Device#getServiceType()
	 * @see #getDevice()
	 * @generated
	 */
	EReference getDevice_ServiceType();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.tm.discovery.model.Device#getAddress <em>Address</em>}'.
	 * 
	 * @return the meta object for the attribute '<em>Address</em>'.
	 * @see org.eclipse.tm.discovery.model.Device#getAddress()
	 * @see #getDevice()
	 * @generated
	 */
	EAttribute getDevice_Address();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.tm.discovery.model.Device#getName <em>Name</em>}'.
	 * 
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.tm.discovery.model.Device#getName()
	 * @see #getDevice()
	 * @generated
	 */
	EAttribute getDevice_Name();

	/**
	 * Returns the meta object for class '{@link org.eclipse.tm.discovery.model.Network <em>Network</em>}'.
	 * 
	 * @return the meta object for class '<em>Network</em>'.
	 * @see org.eclipse.tm.discovery.model.Network
	 * @generated
	 */
	EClass getNetwork();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.tm.discovery.model.Network#getDevice <em>Device</em>}'.
	 * 
	 * @return the meta object for the containment reference list '<em>Device</em>'.
	 * @see org.eclipse.tm.discovery.model.Network#getDevice()
	 * @see #getNetwork()
	 * @generated
	 */
	EReference getNetwork_Device();

	/**
	 * Returns the meta object for class '{@link org.eclipse.tm.discovery.model.Pair <em>Pair</em>}'.
	 * 
	 * @return the meta object for class '<em>Pair</em>'.
	 * @see org.eclipse.tm.discovery.model.Pair
	 * @generated
	 */
	EClass getPair();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.tm.discovery.model.Pair#getKey <em>Key</em>}'.
	 * 
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see org.eclipse.tm.discovery.model.Pair#getKey()
	 * @see #getPair()
	 * @generated
	 */
	EAttribute getPair_Key();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.tm.discovery.model.Pair#getValue <em>Value</em>}'.
	 * 
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see org.eclipse.tm.discovery.model.Pair#getValue()
	 * @see #getPair()
	 * @generated
	 */
	EAttribute getPair_Value();

	/**
	 * Returns the meta object for class '{@link org.eclipse.tm.discovery.model.Service <em>Service</em>}'.
	 * 
	 * @return the meta object for class '<em>Service</em>'.
	 * @see org.eclipse.tm.discovery.model.Service
	 * @generated
	 */
	EClass getService();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.tm.discovery.model.Service#getPair <em>Pair</em>}'.
	 * 
	 * @return the meta object for the containment reference list '<em>Pair</em>'.
	 * @see org.eclipse.tm.discovery.model.Service#getPair()
	 * @see #getService()
	 * @generated
	 */
	EReference getService_Pair();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.tm.discovery.model.Service#getName <em>Name</em>}'.
	 * 
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.tm.discovery.model.Service#getName()
	 * @see #getService()
	 * @generated
	 */
	EAttribute getService_Name();

	/**
	 * Returns the meta object for class '{@link org.eclipse.tm.discovery.model.ServiceType <em>Service Type</em>}'.
	 * 
	 * @return the meta object for class '<em>Service Type</em>'.
	 * @see org.eclipse.tm.discovery.model.ServiceType
	 * @generated
	 */
	EClass getServiceType();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.tm.discovery.model.ServiceType#getService <em>Service</em>}'.
	 * 
	 * @return the meta object for the containment reference list '<em>Service</em>'.
	 * @see org.eclipse.tm.discovery.model.ServiceType#getService()
	 * @see #getServiceType()
	 * @generated
	 */
	EReference getServiceType_Service();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.tm.discovery.model.ServiceType#getName <em>Name</em>}'.
	 * 
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.tm.discovery.model.ServiceType#getName()
	 * @see #getServiceType()
	 * @generated
	 */
	EAttribute getServiceType_Name();

	/**
	 * Returns the factory that creates the instances of the model.
	 * 
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ModelFactory getModelFactory();

	/**
	 * 
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * 
	 * @generated
	 */
	
	
	
	
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.tm.internal.discovery.model.impl.DeviceImpl <em>Device</em>}' class.
		 * 
		 * @see org.eclipse.tm.internal.discovery.model.impl.DeviceImpl
		 * @see org.eclipse.tm.internal.discovery.model.impl.ModelPackageImpl#getDevice()
		 * @generated
		 */
		
		EClass DEVICE = eINSTANCE.getDevice();

		/**
		 * The meta object literal for the '<em><b>Service Type</b></em>' containment reference list feature.
		 * 
		 * @generated
		 */
		EReference DEVICE__SERVICE_TYPE = eINSTANCE.getDevice_ServiceType();

		/**
		 * The meta object literal for the '<em><b>Address</b></em>' attribute feature.
		 * 
		 * @generated
		 */
		EAttribute DEVICE__ADDRESS = eINSTANCE.getDevice_Address();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * 
		 * @generated
		 */
		EAttribute DEVICE__NAME = eINSTANCE.getDevice_Name();

		/**
		 * The meta object literal for the '{@link org.eclipse.tm.internal.discovery.model.impl.NetworkImpl <em>Network</em>}' class.
		 * 
		 * @see org.eclipse.tm.internal.discovery.model.impl.NetworkImpl
		 * @see org.eclipse.tm.internal.discovery.model.impl.ModelPackageImpl#getNetwork()
		 * @generated
		 */
		EClass NETWORK = eINSTANCE.getNetwork();

		/**
		 * The meta object literal for the '<em><b>Device</b></em>' containment reference list feature.
		 * 
		 * @generated
		 */
		EReference NETWORK__DEVICE = eINSTANCE.getNetwork_Device();

		/**
		 * The meta object literal for the '{@link org.eclipse.tm.internal.discovery.model.impl.PairImpl <em>Pair</em>}' class.
		 * 
		 * @see org.eclipse.tm.internal.discovery.model.impl.PairImpl
		 * @see org.eclipse.tm.internal.discovery.model.impl.ModelPackageImpl#getPair()
		 * @generated
		 */
		EClass PAIR = eINSTANCE.getPair();

		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * 
		 * @generated
		 */
		EAttribute PAIR__KEY = eINSTANCE.getPair_Key();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * 
		 * @generated
		 */
		EAttribute PAIR__VALUE = eINSTANCE.getPair_Value();

		/**
		 * The meta object literal for the '{@link org.eclipse.tm.internal.discovery.model.impl.ServiceImpl <em>Service</em>}' class.
		 * 
		 * @see org.eclipse.tm.internal.discovery.model.impl.ServiceImpl
		 * @see org.eclipse.tm.internal.discovery.model.impl.ModelPackageImpl#getService()
		 * @generated
		 */
		EClass SERVICE = eINSTANCE.getService();

		/**
		 * The meta object literal for the '<em><b>Pair</b></em>' containment reference list feature.
		 * 
		 * @generated
		 */
		EReference SERVICE__PAIR = eINSTANCE.getService_Pair();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * 
		 * @generated
		 */
		EAttribute SERVICE__NAME = eINSTANCE.getService_Name();

		/**
		 * The meta object literal for the '{@link org.eclipse.tm.internal.discovery.model.impl.ServiceTypeImpl <em>Service Type</em>}' class.
		 * 
		 * @see org.eclipse.tm.internal.discovery.model.impl.ServiceTypeImpl
		 * @see org.eclipse.tm.internal.discovery.model.impl.ModelPackageImpl#getServiceType()
		 * @generated
		 */
		EClass SERVICE_TYPE = eINSTANCE.getServiceType();

		/**
		 * The meta object literal for the '<em><b>Service</b></em>' containment reference list feature.
		 * 
		 * @generated
		 */
		EReference SERVICE_TYPE__SERVICE = eINSTANCE.getServiceType_Service();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * 
		 * @generated
		 */
		EAttribute SERVICE_TYPE__NAME = eINSTANCE.getServiceType_Name();

	}

} //ModelPackage

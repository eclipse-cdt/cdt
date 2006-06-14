/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.internal.references;

import org.eclipse.rse.internal.filters.SystemFilterPoolReferenceManager;
import org.eclipse.rse.references.ISystemBasePersistableReferenceManager;
import org.eclipse.rse.references.ISystemBasePersistableReferencedObject;
import org.eclipse.rse.references.ISystemBaseReferencedObject;
import org.eclipse.rse.references.ISystemPersistableReferencingObject;


/**
 * This class represents a object that references another object, where this reference
 *  is persistable to disk.
 * @see org.eclipse.rse.references.ISystemBasePersistableReferenceManager
 * @lastgen class SystemPersistableReferencingObjectImpl extends SystemReferencingObjectImpl implements SystemPersistableReferencingObject, SystemReferencingObject {}
 */
public abstract class SystemPersistableReferencingObject extends SystemReferencingObject implements ISystemPersistableReferencingObject 
{
	/**
	 * The default value of the '{@link #getReferencedObjectName() <em>Referenced Object Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReferencedObjectName()
	 * @generated
	 * @ordered
	 */
	protected static final String REFERENCED_OBJECT_NAME_EDEFAULT = null;

//    private SystemReferencingObjectHelper helper = null; DWD dangerous, overriding and covering superclass

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected String referencedObjectName = REFERENCED_OBJECT_NAME_EDEFAULT;
	
	// FIXME
	protected ISystemBasePersistableReferenceManager _referenceManager; 

    /**
	 * Constructor. Typically called by MOF framework via factory create method.
	 */
	protected SystemPersistableReferencingObject() 
	{
		super();
		helper = new SystemReferencingObjectHelper(this);
	}
	/**
     * Set the persistable referenced object name
	 * @generated This field/method will be replaced during code generation.
     */
	public void setReferencedObjectName(String newReferencedObjectName)
	{
		referencedObjectName = newReferencedObjectName;
	}

	// ----------------------------------------------
	// ISystemPersistableReferencingObject methods...
	// ----------------------------------------------
	/**
     * Set the in-memory reference to the master object.
     * This implementation also extracts that master object's name and calls
     *  setReferencedObjectName as part of this method call.
	 * @see org.eclipse.rse.references.ISystemBasePersistableReferencingObject#setReferencedObject(ISystemBasePersistableReferencedObject)
	 */
	public void setReferencedObject(ISystemBasePersistableReferencedObject obj)
	{
        helper.setReferencedObject((ISystemBaseReferencedObject)obj);     		
        setReferencedObjectName(obj.getReferenceName());
	}

	/**
	 * Get the persistable referenced object name. Handled by MOF generated code.
	 * @generated This field/method will be replaced during code generation
	 */
	public String getReferencedObjectName()
	{
		return referencedObjectName;
	}

	// ----------------------------------------------
	// ISystemReferencingObject methods...
	// ----------------------------------------------
	
	/**
	 * @see org.eclipse.rse.references.ISystemBaseReferencingObject#setReferencedObject(ISystemBaseReferencedObject)
	 */
	public void setReferencedObject(ISystemBaseReferencedObject obj)
	{
        helper.setReferencedObject(obj);
	}
	
	/**
	 * @see org.eclipse.rse.references.ISystemBaseReferencingObject#getReferencedObject()
	 */
	public ISystemBaseReferencedObject getReferencedObject()
	{
        return helper.getReferencedObject();
 	}
	
	/**
	 * @see org.eclipse.rse.references.ISystemBaseReferencingObject#removeReference()
	 */
	public int removeReference()
	{
        return helper.removeReference();
	}	    
	
	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public ISystemBasePersistableReferenceManager getParentReferenceManager()
	{
		/*FIXME
		if (eContainerFeatureID != ReferencesPackage.SYSTEM_PERSISTABLE_REFERENCING_OBJECT__PARENT_REFERENCE_MANAGER) return null;
		return (SystemPersistableReferenceManager)eContainer;
		*/
		if (_referenceManager == null)
		{
			//SystemFilterPoolReferenceManagerImpl.createSystemFilterPoolReferenceManager(caller, relatedPoolManagerProvider, mgrFolder, name, savePolicy, namingPolicy)
			_referenceManager = new SystemFilterPoolReferenceManager();
		}
		return _referenceManager;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setParentReferenceManager(ISystemBasePersistableReferenceManager newParentReferenceManager)
	{
		/*FIXME
		if (newParentReferenceManager != eContainer || (eContainerFeatureID != ReferencesPackage.SYSTEM_PERSISTABLE_REFERENCING_OBJECT__PARENT_REFERENCE_MANAGER && newParentReferenceManager != null))
		{
			if (EcoreUtil.isAncestor(this, newParentReferenceManager))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eContainer != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newParentReferenceManager != null)
				msgs = ((InternalEObject)newParentReferenceManager).eInverseAdd(this, ReferencesPackage.SYSTEM_PERSISTABLE_REFERENCE_MANAGER__REFERENCING_OBJECT_LIST, SystemPersistableReferenceManager.class, msgs);
			msgs = eBasicSetContainer((InternalEObject)newParentReferenceManager, ReferencesPackage.SYSTEM_PERSISTABLE_REFERENCING_OBJECT__PARENT_REFERENCE_MANAGER, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ReferencesPackage.SYSTEM_PERSISTABLE_REFERENCING_OBJECT__PARENT_REFERENCE_MANAGER, newParentReferenceManager, newParentReferenceManager));
			*/
		_referenceManager = newParentReferenceManager;
		return;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public String toString()
	{
		

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (referencedObjectName: ");
		result.append(referencedObjectName);
		result.append(')');
		return result.toString();
	}

}
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

package org.eclipse.rse.internal.subsystems.shells.subsystems;







import org.eclipse.rse.core.subsystems.IRemoteSystemEnvVar;

/**
 * Encapsulation of environment variable properties set uniquely per connection.
 */
/**
 * @lastgen class RemoteSystemEnvVarImpl Impl implements RemoteSystemEnvVar {}
 */
public class RemoteSystemEnvVar implements IRemoteSystemEnvVar
{
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */

	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected String name = NAME_EDEFAULT;
	/**
	 * The default value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected static final String VALUE_EDEFAULT = null;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected String value = VALUE_EDEFAULT;
	/**
	 * The default value of the '{@link #getAdditionalAttributes() <em>Additional Attributes</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAdditionalAttributes()
	 * @generated
	 * @ordered
	 */
	protected static final String ADDITIONAL_ATTRIBUTES_EDEFAULT = null;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected String additionalAttributes = ADDITIONAL_ATTRIBUTES_EDEFAULT;
		/**
		 * @generated This field/method will be replaced during code generation.
		 */
	public RemoteSystemEnvVar()
	{
		super();
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 * The name of the environment variable to set at connect time
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setName(String newName)
	{
		name = newName;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 * The value to set this environment variable to at connect time
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setValue(String newValue)
	{
		value = newValue;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 * Additional attributes that may need to be persisted per environment variable.
	 */
	public String getAdditionalAttributes()
	{
		return additionalAttributes;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setAdditionalAttributes(String newAdditionalAttributes)
	{
		additionalAttributes = newAdditionalAttributes;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public String toString()
	{
		

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (name: ");
		result.append(name);
		result.append(", value: ");
		result.append(value);
		result.append(", additionalAttributes: ");
		result.append(additionalAttributes);
		result.append(')');
		return result.toString();
	}

}
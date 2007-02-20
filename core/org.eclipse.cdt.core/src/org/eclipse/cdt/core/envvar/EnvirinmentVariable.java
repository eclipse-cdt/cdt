/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.envvar;



/**
 * a trivial implementation of the IBuildEnvironmentVariable
 * 
 * @since 3.0
 */
public class EnvirinmentVariable implements IEnvironmentVariable, Cloneable {
	protected String fName;
	protected String fValue;
	protected String fDelimiter;
	protected int fOperation;
	
	public EnvirinmentVariable(String name, String value, int op, String delimiter){
		fName = name;
		fOperation = op;
		fValue = value;
		fDelimiter = delimiter;
	}
	
	protected EnvirinmentVariable(){
		
	}
	
	public EnvirinmentVariable(String name){
		this(name,null,ENVVAR_REPLACE,null);
	}
	
	public EnvirinmentVariable(String name, String value){
		this(name,value,ENVVAR_REPLACE,null);	
	}

	public EnvirinmentVariable(String name, String value, String delimiter){
		this(name,value,ENVVAR_REPLACE,delimiter);	
	}
	
	public EnvirinmentVariable(IEnvironmentVariable var){
		this(var.getName(),var.getValue(),var.getOperation(),var.getDelimiter());	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable#getName()
	 */
	public String getName(){
		return fName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable#getValue()
	 */
	public String getValue(){
		return fValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable#getOperation()
	 */
	public int getOperation(){
		return fOperation;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable#getDelimiter()
	 */
	public String getDelimiter(){
		return fDelimiter;
	}
	
	public Object clone(){
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}
}

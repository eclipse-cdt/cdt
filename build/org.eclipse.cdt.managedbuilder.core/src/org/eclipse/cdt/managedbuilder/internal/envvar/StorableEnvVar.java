/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.envvar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents the Environment variable that could be loaded
 * and stored in XML
 * 
 * @since 3.0
 *
 */
public class StorableEnvVar extends BuildEnvVar {
	public static final String VARIABLE_ELEMENT_NAME = "variable"; //$NON-NLS-1$
	public static final String NAME = "name"; //$NON-NLS-1$
	public static final String VALUE = "value"; //$NON-NLS-1$
	public static final String OPERATION = "operation"; //$NON-NLS-1$
	public static final String DELIMITER = "delimiter"; //$NON-NLS-1$
	
	public static final String REPLACE = "replace"; //$NON-NLS-1$
	public static final String REMOVE = "remove"; //$NON-NLS-1$
	public static final String APPEND = "append"; //$NON-NLS-1$
	public static final String PREPEND = "prepend"; //$NON-NLS-1$
	
	public StorableEnvVar(String name, String value, int op, String delimiter){
		super(name,value,op,delimiter);
	}
	
	public StorableEnvVar(String name){
		this(name,null,ENVVAR_REPLACE,null);
	}
	
	public StorableEnvVar(String name, String value){
		this(name,value,ENVVAR_REPLACE,null);	
	}
	
	public StorableEnvVar(String name, String value, String delimiter){
		this(name,value,ENVVAR_REPLACE,delimiter);	
	}
	
	public StorableEnvVar(Element element){
		load(element);
	}
	
	private void load(Element element){
		fName = element.getAttribute(NAME);

		fValue = element.getAttribute(VALUE);

		fOperation = opStringToInt(element.getAttribute(OPERATION));
			
		fDelimiter = element.getAttribute(DELIMITER);
		if("".equals(fDelimiter)) //$NON-NLS-1$
			fDelimiter = null;
	}
	
	private int opStringToInt(String op){
		int operation;
		
		if(REMOVE.equals(op))
			operation = ENVVAR_REMOVE;
		else if(APPEND.equals(op))
			operation = ENVVAR_APPEND;
		else if(PREPEND.equals(op))
			operation = ENVVAR_PREPEND;
		else
			operation = ENVVAR_REPLACE;
		
		return operation;
	}
	
	private String opIntToString(int op){
		String operation;
		
		if(ENVVAR_REMOVE == op)
			operation = REMOVE;
		else if(ENVVAR_APPEND == op)
			operation = APPEND;
		else if(ENVVAR_PREPEND == op)
			operation = PREPEND;
		else
			operation = REPLACE;
		
		return operation;
	}

	public void serialize(Document doc, Element element){
		if(fName != null)
			element.setAttribute(NAME,fName);
		
		if(fValue != null)
			element.setAttribute(VALUE,fValue);
		
		element.setAttribute(OPERATION,opIntToString(fOperation));
		
		if(fDelimiter != null)
			element.setAttribute(DELIMITER,fDelimiter);
	}
}

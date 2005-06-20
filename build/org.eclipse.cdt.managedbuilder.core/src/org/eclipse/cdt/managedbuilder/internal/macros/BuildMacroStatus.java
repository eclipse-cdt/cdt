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
package org.eclipse.cdt.managedbuilder.internal.macros;

import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This class implements the IBuildMacroStatus interface 
 * 
 * @since 3.0
 */
public class BuildMacroStatus extends Status implements IBuildMacroStatus {
	/*
	 * String constants
	 */
	private static final String PREFIX = "BuildMacroStatus";	//$NON-NLS-1$
	private static final String STATUS = PREFIX + ".status";	//$NON-NLS-1$
	private static final String STATUS_MACRO_UNDEFINED = STATUS + ".macro.undefined";	//$NON-NLS-1$
	private static final String STATUS_MACROS_REFERENCE_EACHOTHER = STATUS + ".reference.eachother";	//$NON-NLS-1$
	private static final String STATUS_MACRO_REFERENCE_INCORRECT = STATUS + ".reference.incorrect";	//$NON-NLS-1$
	private static final String STATUS_MACRO_NOT_STRING = STATUS + ".macro.not.string";	//$NON-NLS-1$
	private static final String STATUS_MACRO_NOT_STRINGLIST = STATUS + ".macro.not.stringlist";	//$NON-NLS-1$
	private static final String STATUS_ERROR = STATUS + ".error";	//$NON-NLS-1$
	private static final String VALUE_UNDEFINED = PREFIX + ".value.undefined";	//$NON-NLS-1$
	
	private String fMacroName;
	private String fExpression;
	private String fReferencedName;
	private int fContextType;
	private Object fContextData;

	/**
	 * 
	 * @param severity one of the IStatus.xxx severity statuses
	 * @param code one of the IBuildMacroStatus.TYPE_xxx statusses
	 * @param message message, can be null. In this case the default message will
	 *  be generated base upon the other status info
	 * @param exception a low-level exception, or <code>null</code> if not
	 *    applicable 
	 * @param macroName the name of the build macro whose resolution caused this status creation or null if none
	 * @param expression the string whose resolutinon caused caused this status creation or null if none
	 * @param referencedName the macro name referenced in the resolution string that caused this this status creation or null if none
	 * @param contextType the context type used in the operation
	 * @param contextData the context data used in the operation
	 */
	public BuildMacroStatus(int severity, 
			int code, 
			String message, 
			Throwable exception, 
			String macroName,
			String expression, 
			String referencedName,
			int contextType,
			Object contextData) {
		super(severity,ManagedBuilderCorePlugin.getUniqueIdentifier(),code,message != null ? message : "",exception);	//$NON-NLS-1$
		fExpression = expression;
		fReferencedName = referencedName;
		fContextType = contextType;
		fContextData = contextData;
		fMacroName = macroName;
		if(message == null)
			setMessage(generateMessage());
	}

	/**
	 * Creates status with the IStatus.ERROR severity
	 * 
	 * @param code one of the IBuildMacroStatus.TYPE_xxx statusses
	 * @param message message, can be null. In this case the default message will
	 *  be generated base upon the other status info
	 * @param exception a low-level exception, or <code>null</code> if not
	 *    applicable 
	 * @param macroName the name of the build macro whose resolution caused this status creation or null if none
	 * @param expression the string whose resolutinon caused caused this status creation or null if none
	 * @param referencedName the macro name referenced in the resolution string that caused this this status creation or null if none
	 * @param contextType the context type used in the operation
	 * @param contextData the context data used in the operation
	 */
	public BuildMacroStatus( 
			int code, 
			String message, 
			Throwable exception, 
			String macroName, 
			String expression, 
			String referencedName,
			int contextType,
			Object contextData) {
		this(IStatus.ERROR,code,message,exception,macroName,expression,referencedName,contextType,contextData);
	}

	/**
	 * Creates status with the IStatus.ERROR severity and with the default message
	 * 
	 * @param code one of the IBuildMacroStatus.TYPE_xxx statusses
	 * @param exception a low-level exception, or <code>null</code> if not
	 *    applicable 
	 * @param macroName the name of the build macro whose resolution caused this status creation or null if none
	 * @param expression the string whose resolutinon caused caused this status creation or null if none
	 * @param referencedName the macro name referenced in the resolution string that caused this this status creation or null if none
	 * @param contextType the context type used in the operation
	 * @param contextData the context data used in the operation
	 */
	public BuildMacroStatus( 
			int code, 
			String macroName, 
			String expression, 
			String referencedName,
			int contextType,
			Object contextData) {
		this(IStatus.ERROR,code,null,null,macroName,expression,referencedName,contextType,contextData);
	}

	/**
	 * generates and returns the default status message based upon then status data
	 * 
	 * @return String
	 */
	protected String generateMessage(){
		String message = null;
		switch(getCode()){
		case TYPE_MACRO_UNDEFINED:{
			String refName = fReferencedName;
			if(refName == null)
				refName = ManagedMakeMessages.getResourceString(VALUE_UNDEFINED);
			message = ManagedMakeMessages.getFormattedString(STATUS_MACRO_UNDEFINED,refName);
		}
		break;
		case TYPE_MACROS_REFERENCE_EACHOTHER:{
			String name = fMacroName;
			String refName = fReferencedName;
			if(name == null)
				name = ManagedMakeMessages.getResourceString(VALUE_UNDEFINED);
			if(refName == null)
				refName = ManagedMakeMessages.getResourceString(VALUE_UNDEFINED);
			message = ManagedMakeMessages.getFormattedString(STATUS_MACROS_REFERENCE_EACHOTHER,new String[]{name,refName});
		}
		break;
		case TYPE_MACRO_REFERENCE_INCORRECT:{
			String refName = fReferencedName;
			if(refName == null)
				refName = ManagedMakeMessages.getResourceString(VALUE_UNDEFINED);
			message = ManagedMakeMessages.getFormattedString(STATUS_MACRO_REFERENCE_INCORRECT,refName);
		}
		break;
		case TYPE_MACRO_NOT_STRING:{
			String refName = fReferencedName;
			if(refName == null)
				refName = ManagedMakeMessages.getResourceString(VALUE_UNDEFINED);
			message = ManagedMakeMessages.getFormattedString(STATUS_MACRO_NOT_STRING,refName);
		}
		break;
		case TYPE_MACRO_NOT_STRINGLIST:{
			String refName = fReferencedName;
			if(refName == null)
				refName = ManagedMakeMessages.getResourceString(VALUE_UNDEFINED);
			message = ManagedMakeMessages.getFormattedString(STATUS_MACRO_NOT_STRINGLIST,refName);
		}
		break;
		case TYPE_ERROR:
		default:
			message = ManagedMakeMessages.getResourceString(STATUS_ERROR);
		}
		return message;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroStatus#getMacroName()
	 */
	public String getMacroName() {
		return fMacroName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroStatus#getExpression()
	 */
	public String getExpression() {
		return fExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroStatus#getReferencedName()
	 */
	public String getReferencedMacroName() {
		return fReferencedName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroStatus#getContextType()
	 */
	public int getContextType() {
		return fContextType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroStatus#getContextData()
	 */
	public Object getContextData() {
		return fContextData;
	}

}

/**********************************************************************
 * Copyright (c) 2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.ui;

/**
 * This class is a helper class which takes care of implementing some of the 
 * function prototype parsing and stripping.
 */
public class FunctionPrototypeSummary implements IFunctionSummary.IFunctionPrototypeSummary {
	String fname;
	String freturn;
	String farguments;
		
	/**
	 * Create a function prototype summary based on a prototype string.
	 * @param The string describing the prototype which is properly 
	 * formed with following format -- returntype function(arguments)
	 * The following formats will be converted as follows:
	 * function(arguments) --> function(arguments) //constructors!
	 * returntype function --> returntype function()
	 * function            --> void function() 
	 */
	public FunctionPrototypeSummary(String proto) {
		int leftbracket = proto.indexOf('(');
		int rightbracket = proto.lastIndexOf(')');
		
		//If there are brackets missing, then assume void parameters
		if(leftbracket == -1 || rightbracket == -1) {
			if(leftbracket != -1) {
				proto = proto.substring(leftbracket) + ")"; //$NON-NLS-1$
			} else if(rightbracket != -1) {
				proto = proto.substring(rightbracket - 1) + "()";				 //$NON-NLS-1$
			} else {
				proto = proto + "()"; //$NON-NLS-1$
			}
		
			leftbracket = proto.indexOf('(');
			rightbracket = proto.lastIndexOf(')');
		} 
		
		farguments = proto.substring(leftbracket + 1, rightbracket);
			
		// fix for bug #44359
		if(farguments.equals("void")) //$NON-NLS-1$
			farguments = ""; //$NON-NLS-1$
		
		int nameend = leftbracket - 1;
		while(proto.charAt(nameend) == ' ') {
			nameend--;
		}

		int namestart = nameend;
		while(namestart > 0 && proto.charAt(namestart) != ' ') {
			namestart--;
		}

		fname = proto.substring(namestart, nameend + 1).trim();
			
		if(namestart == 0) {
			//Constructors are like this, don't stick a type on them.
			freturn = ""; //$NON-NLS-1$
		} else {
			freturn = proto.substring(0, namestart).trim();
		}
	}

	public String getName() {
		return fname;
	}

	public String getReturnType() {
		return freturn;
	}
		
	public String getArguments() {
		return farguments;
	}
		
	public String getPrototypeString(boolean namefirst) {
		return getPrototypeString(namefirst, true);
	}
	
	public String getPrototypeString(boolean namefirst, boolean appendReturnType) {
		StringBuffer buffer = new StringBuffer();
		if((!namefirst) && (appendReturnType)) {
			buffer.append(getReturnType());
			buffer.append(" "); //$NON-NLS-1$
		}
		buffer.append(getName());
		buffer.append("("); //$NON-NLS-1$
		if(getArguments() != null) {
			buffer.append(getArguments());
		}
		buffer.append(")"); //$NON-NLS-1$
		if((namefirst) && (appendReturnType) && getReturnType().length() > 0 ) {
			buffer.append(" "); //$NON-NLS-1$
			buffer.append(getReturnType());
		}
		return buffer.toString();
	}
}

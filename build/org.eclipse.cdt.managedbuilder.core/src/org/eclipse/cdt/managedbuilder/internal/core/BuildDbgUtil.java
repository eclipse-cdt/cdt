/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

public class BuildDbgUtil extends DbgUtilBase {
	public static final int BUILD_INFO_LOAD = 1;
	public static boolean DEBUG = false;
	private static BuildDbgUtil fInstance;
	
	private BuildDbgUtil(){
		fDbgOn = DEBUG;
	}
	
	public static BuildDbgUtil getInstance(){
		if(fInstance == null)
			fInstance = new BuildDbgUtil();
		return fInstance;
	}
	
	public int getFlags(){
		return fFlags;
	}
	
	public void setFlags(int flags){
		fFlags = flags;
	}

	public void enable(boolean enable){
		DEBUG = enable;
		fDbgOn = enable;
	}
	
	public boolean isEnabled(){
		return DEBUG && fDbgOn;
	}

}

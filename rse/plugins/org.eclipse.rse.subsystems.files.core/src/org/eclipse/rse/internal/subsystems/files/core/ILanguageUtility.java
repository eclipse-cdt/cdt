/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.internal.subsystems.files.core;

import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;

/**
 * Interface for a language utility. Each programming language should have its own utility,
 * for example Java, C/C++, etc.
 */
public interface ILanguageUtility {
	
	/**
	 * Constant for Java language.
	 */
	public static final String LANGUAGE_JAVA = "Java";
	
	/**
	 * Constant for C language.
	 */
	public static final String LANGUAGE_C = "C";
	
	/**
	 * Constant for C++ language.
	 */
	public static final String LANGUAGE_CPP = "C++";
	
	/**
	 * Returns the subsystem with which the utility is associated.
	 * @return the subsystem.
	 */
	public IRemoteFileSubSystem getSubSystem();
	
	/**
	 * Returns the language to which this utility applies. It could be one of
	 * <code>LANGUAGE_JAVA</code>, <code>LANGUAGE_C</code>, and <code>LANGUAGE_CPP</code>, or
	 * another language.
	 * @return the language. 
	 */
	public String getLanguage();
}
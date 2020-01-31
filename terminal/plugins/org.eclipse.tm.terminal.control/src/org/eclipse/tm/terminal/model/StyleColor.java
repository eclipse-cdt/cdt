/*******************************************************************************
 * Copyright (c) 2007, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License 2.0 
 * which accompanies this distribution, and is available at 
 * https://www.eclipse.org/legal/epl-2.0/ 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.model;

import java.util.HashMap;
import java.util.Map;

/** 
 * 
 * Flyweight
 * Threadsafe.
 */
public class StyleColor {
	private final static Map<String, StyleColor> fgStyleColors=new HashMap<String, StyleColor>();
	final String fName;
	
	/**
	 * @param name the name of the color. It is up to the UI to associate a
	 * named color with a visual representation
	 * @return a StyleColor
	 */
	public static StyleColor getStyleColor(String name) {
		StyleColor result;
		synchronized (fgStyleColors) {
			result=fgStyleColors.get(name);
			if(result==null) {
				result=new StyleColor(name);
				fgStyleColors.put(name, result);
			}
		}
		return result;
	}
	// nobody except the factory method is allowed to instantiate this class!
	private StyleColor(String name) {
		fName = name;
	}

	public String getName() {
		return fName;
	}

	public String toString() {
		return fName;
	}
	// no need to override equals and hashCode, because Object uses object identity
}
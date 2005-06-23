/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.testplugin.util;


import java.util.Stack;
/**
 * @author Peter Graves
 *
 * This utility class maintains a list of strings, and as a tests finds strings
 * in a structure/list, it will maintain a list of unfound/extra strings.
 */
public class ExpectedStrings {

	public String [] expStrings;
	private boolean[] foundStrings; 
	private Stack extraStrings; 	/* A stack of the unecpected strings we 
									 * recieved
									 */
	private boolean extra;

	/**
	 * Constructor for ExpectedStrings.
	 */
	public ExpectedStrings() {
	}
	/**
	 * Constructor for ExpectedStrings that accepts a list of strings that
	 * we expect to get.
	 */
	public ExpectedStrings(String[] values) {
		int x;
		expStrings=new String[values.length];
		for (x=0;x<values.length;x++) {
			expStrings[x]=new String(values[x]);
		}
		foundStrings=new boolean[values.length];
		for (x=0;x<values.length;x++) {
			foundStrings[x]=false;
		}
		extraStrings=new Stack();
		extra=false;
	}
	
	public int foundString(String current) {
		int x;
		for (x=0;x<expStrings.length;x++) {
			if (current.equals(expStrings[x])) {
				foundStrings[x]=true;
				return(0);
			}
		}
		/* If we arrive here, the strings was not found, so this is
		 * and extra string
		 */

		 extraStrings.push(new String(current));
		 extra=true;
		 return(1);
	}
	public int getNum(String name) {
		int x;
		for (x=0;x<expStrings.length;x++) {
			if (name.equals(expStrings[x]))
				return(x);
		}
		return(-1);
	}
	public boolean gotAll() {
		int x;
		for (x=0;x<expStrings.length;x++) {
			if (foundStrings[x]==false)
				return(false);
		}
		return(true);
	}
	public boolean gotExtra() {
		return(extra);
	}
	public String getMissingString() {
		int x;
		String missing = new String("Missing elements: ");
		for (x=0;x<expStrings.length;x++) {
			if (foundStrings[x]==false)
				missing+=expStrings[x];
				missing+=" ";
		}
		return(missing);
	}
	public String getExtraString() {
		String extra= new String("Extra elements: ");
		while (!extraStrings.empty()) {
			extra+=extraStrings.pop();
			extra+=" ";
		}
		return(extra);
	}
}

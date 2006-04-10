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

package org.eclipse.rse.internal.logging;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides a set of utility routines for processing mnemonics for labels that are
 * to be used in dialogs and menus.
 */
public class LabelUtil {
	
	private final static String upperLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private final static String lowerLetters = "abcdefghijklmnopqrstuvwxyz";
	private final static char lpar = '(';
	private final static char rpar = ')';
	private final static char trigger = '&';
	
	/**
	 * The Assignment class contains the result of a mnemonic assignment.
	 */
	private static class Assignment {
		private String label;
		private Character mnemonic;
		Assignment(String label, Character mnemonic) {
			this.label = label;
			this.mnemonic = mnemonic;
		}
		String getLabel() {
			return label;
		}
		Character getMnemonic() {
			return mnemonic;
		}
		boolean hasMnemonic() {
			return mnemonic != null;
		}
	}

	/**
	 * Assigns a mnemonic to a label and adds it to the set of used mnemonic characters.
	 * Used mnemonics are ignored in the search for a valid mnemonic.
	 * If there are no characters in the label that can be used as mnemonics then a
	 * valid mnemonic is appended to the label in parentheses.  
	 * If the label already has a mnemonic, its mnemonic is added to the set of used
	 * mnemonics, but the label itself is unchanged.
	 * @param label the label in which the mnemonic is to be discovered
	 * @param used the set of Characters that cannot be used as mnemonics. This set is modified
	 * by the operation if a mnemonic could be found.
	 * @return the label with the mnemonic identified or the original label if a mnemonic could
	 * not be found.
	 */
	public static String assignMnemonic(String label, Set used) {
		Assignment result = null;
		Character c = findMnemonic(label);
		if (c != null) {
			used.add(c);
			return label;
		}
		result = assignMnemonic(label, upperLetters, used);
		if (!result.hasMnemonic()) {
			result = assignMnemonic(label, lowerLetters, used);
			if (!result.hasMnemonic()) {
				Assignment temp = assignMnemonic(upperLetters, upperLetters, used);
				if (temp.hasMnemonic()) {
					c = temp.getMnemonic();
					StringBuffer b = new StringBuffer(label.length() + 4);
					b.append(label);
					b.append(lpar);
					b.append(trigger);
					b.append(c);
					b.append(rpar);
					result = new Assignment(b.toString(), c);
				}
			}
		}
		if (result.hasMnemonic()) {
			Character mnemonic = result.getMnemonic();
			Character lower = new Character(Character.toLowerCase(mnemonic.charValue()));
			Character upper = new Character(Character.toUpperCase(mnemonic.charValue()));
			used.add(lower);
			used.add(upper);
			
		}
		return result.getLabel();
	}
	
	/**
	 * This is a convenience method for constucting a "used set" by taking 
	 * the individual characters of a string
	 * in both lower and upper cases and adding them to the set.  Mnemonics are
	 * not case sensitive.
	 * @param s the String from which to construct the set.
	 * @return the set.
	 */
	public static Set usedFromString(String s) {
		Set result = new HashSet();
		char[] characters = s.toLowerCase().toCharArray();
		for (int i = 0; i < characters.length; i++) {
			result.add(new Character(characters[i]));
		}
		characters = s.toUpperCase().toCharArray();
		for (int i = 0; i < characters.length; i++) {
			result.add(new Character(characters[i]));
		}
		return result;
	}
	
	/**
	 * Assigns a mnemonic to a label from a List of candidate mnemonics.
	 * @param label the label in which the mnemonic is to be discovered.
	 * @param candidates the String of valid candidates to use as mnemonics.
	 * @param used The Set of Characters that have already been used.
	 * @return the Assignment containing the new string with the mnemonic identified.  If no 
	 * mnemonic could be assigned then the Assignment will have no mnemonic specified.
	 */
	private static Assignment assignMnemonic(String label, String candidates, Set used) {
		char[] characters = label.toCharArray();
		Assignment result = new Assignment(label, null);
		for (int i = 0; i < characters.length && !result.hasMnemonic(); i++) {
			Character c = new Character(characters[i]);
			if (!used.contains(c) && candidates.indexOf(c.charValue()) >= 0) {
				StringBuffer b = new StringBuffer(label.length() + 1);
				b.append(label.substring(0, i));
				b.append(trigger);
				b.append(label.substring(i));
				result = new Assignment(b.toString(), c);
			}
		}
		return result;
	}

	/**
	 * Finds the Character used as a mnemonic in a label
	 * @param label the label to search
	 * @return the Character used as the mnemonic, null if none exists.
	 */
	private static Character findMnemonic(String label) {
		Character result = null;
		int i = label.indexOf(trigger);
		if (i >=0 && i < label.length() - 1) {
			result = new Character(label.charAt(i + 1));
		}
		return result;
	}

}
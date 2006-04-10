/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.services.clientserver;


/**
 * This class represents search string.
 */
public class SystemSearchString {
	
	/**
	 * Constant indicating that the depth is infinite, -1.
	 */
	public static final int DEPTH_INFINITE = -1;
	
	protected String textString;
	protected boolean isCaseSensitive;
	protected boolean isTextStringRegex;
	protected String fileNamesString;
	protected boolean isFileNamesRegex;
	protected boolean includeArchives;
	protected boolean includeSubfolders;
	protected String classificationString;
	
	/**
	 * Creates a new search string.
	 * @param textString the text string.
	 * @param isCaseSensitive <code>true</code> if the search should be case sensitive, <code>false</code> otherwise.
	 * @param isTextStringRegex <code>true</code> if the text string is a regular expression, <code>false</code> otherwise.
	 * @param fileNamesString the file names pattern.
	 * @param isFileNamesRegex <code>true</code> if the file names string is a regular expression, <code>false</code> otherwise.
	 * @param includeArchives <code>true</code> to search inside archives, <code>false</code> otherwise.
	 * @param includeSubfolders <code>true</code> to search subfolders, <code>false</code> otherwise.
	 */
	public SystemSearchString(String textString, boolean isCaseSensitive, boolean isTextStringRegex,
							  String fileNamesString, boolean isFileNamesRegex, boolean includeArchives,
							  boolean includeSubfolders) {
		this(textString, isCaseSensitive, isTextStringRegex, fileNamesString, isFileNamesRegex, includeArchives,
				includeSubfolders, "");
	}
	
	/**
	 * Creates a new search string that allows search to be restricted to files with a certain classification.
	 * @param textString the text string.
	 * @param isCaseSensitive <code>true</code> if the search should be case sensitive, <code>false</code> otherwise.
	 * @param isTextStringRegex <code>true</code> if the text string is a regular expression, <code>false</code> otherwise.
	 * @param fileNamesString the file names pattern.
	 * @param isFileNamesRegex <code>true</code> if the file names string is a regular expression, <code>false</code> otherwise.
	 * @param includeArchives <code>true</code> to search inside archives, <code>false</code> otherwise.
	 * @param includeSubfolders <code>true</code> to search subfolders, <code>false</code> otherwise.
	 * @param classificationString the classification string that file classifications should match with.
	 */
	public SystemSearchString(String textString, boolean isCaseSensitive, boolean isTextStringRegex,
							  String fileNamesString, boolean isFileNamesRegex, boolean includeArchives,
							  boolean includeSubfolders, String classificationString) {
		this.textString = textString;
		this.isCaseSensitive = isCaseSensitive;
		this.isTextStringRegex = isTextStringRegex;
		this.fileNamesString = fileNamesString;
		this.isFileNamesRegex = isFileNamesRegex;
		this.includeArchives = includeArchives;
		this.includeSubfolders = includeSubfolders;
		this.classificationString = classificationString;
	}
	
	/**
	 * Returns the text string.
	 * @return the text string.
	 */
	public String getTextString() {
		return textString;
	}
	
	/**
	 * Returns whether the search is case sensitive.
	 * @return <code>true</code> if the search is case sensitive, <code>false</code> otherwise.
	 */
	public boolean isCaseSensitive() {
		return isCaseSensitive;
	}
	
	/**
	 * Returns whether the text string is a regular expression.
	 * @return <code>true</code> if the text string is a regular expression, <code>false</code> otherwise.
	 */
	public boolean isTextStringRegex() {
		return isTextStringRegex;
	}
	
	/**
	 * Returns the file names string.
	 * @return the file names string.
	 */
	public String getFileNamesString() {
		return fileNamesString;
	}
	
	/**
	 * Returns whether the file names string is a regular expression.
	 * @return <code>true</code> if the file names string is a regular expression, <code>false</code> otherwise.
	 */
	public boolean isFileNamesRegex() {
		return isFileNamesRegex;
	}
	
	/**
	 * Returns whether archives should be searched.
	 * @return <code>true</code> to search archives, <code>false</code> otherwise.
	 */
	public boolean isIncludeArchives() {
		return includeArchives;
	}

	/**
	 * Returns whether subfolders should be searched.
	 * @return <code>true</code> to search subfolders, <code>false</code> otherwise.
	 */
	public boolean isIncludeSubfolders() {
		return includeSubfolders;
	}
	
	/**
	 * Returns the classification string that file classifications should match with.
	 * @return the classification.
	 */
	public String getClassificationString() {
		return classificationString;
	}
	
	/**
	 * Writes the contents of the search string
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return textString + " - " + isCaseSensitive + " - " + isTextStringRegex + " - " +
				fileNamesString + " - " + isFileNamesRegex + " - " + includeArchives + " - " +
				includeSubfolders + " - " + classificationString;
	}
}
package org.eclipse.cdt.core.index;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IFile;


public interface ITagEntry {

	/**
	 * Name of tag.
	 */
	public String getTagName();

	/**
	 * Path of source file containing definition of tag.
	 */
	public String getFileName();

	/**
	 * IFile of source file containing definition of tag.
	 */
	public IFile getIFile();
	
	/**
	 * Address for locating tag in source file
	 * (may be null if not present).
	 */
	public String getPattern();

	/**
	 * line number in source file of tag definition
	 * (may be zero if not known).
	 */
	public int getLineNumber();

	/**
	 * Kind of tag.
	 */
	public int getKind();

	/**
	 * Language of the file.
	 */
	public String getLanguage();

	/**
	 * Is tag of file-limited scope?
	 */
	public boolean hasFileScope();

	/**
	 * Return base classes.
	 */
	public String[] getInherits();

	/**
	 * Access Control of structure/class/union members.
	 */
	public int getAccessControl();

	// implementation:
	public int getImplementation();

	/**
	 * Class name of the tag if a member,
	 * null if not a member of a struct/class/union.
	 */
	public String getClassName();

	/**
	 * Return the original tag line.
	 */
	public String getLine();
}

package org.eclipse.cdt.ui;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

public interface IRequiredInclude {

	/**
	 * Returns the name that has been imported. 
	 * For an on-demand import, this includes the trailing <code>".*"</code>.
	 * For example, for the statement <code>"import java.util.*"</code>,
	 * this returns <code>"java.util.*"</code>.
	 * For the statement <code>"import java.util.Hashtable"</code>,
	 * this returns <code>"java.util.Hashtable"</code>.
	 */
	String getIncludeName();

	/**
	 * Returns whether the include is to search on "standard places" like /usr/include first .
	 * An include is standard if it starts with <code>"\<"</code>.
	 */
	boolean isStandard();
}


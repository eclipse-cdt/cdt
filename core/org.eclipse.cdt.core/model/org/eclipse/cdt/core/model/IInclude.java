package org.eclipse.cdt.core.model;
/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 * Represents an include declaration in a C translation unit.
 */
public interface IInclude extends ICElement, ISourceReference, ISourceManipulation {
	/**
	 * Returns the name that of the included file. 
	 * For example, for the statement <code>"#include <stdio.h></code>,
	 * this returns <code>"stdio.h"</code>.
	 */
	String getIncludeName();

	/**
	 * Returns whether the included was search on "standard places" like /usr/include first .
	 * An include is standard if it starts with <code>"\<"</code>.
	 * For example, <code>"#include \<stdio.h\>"</code> returns true and
	 * <code>"#include "foobar.h"</code> returns false.
	 */
	boolean isStandard();
}

package org.eclipse.cdt.internal.corext.template;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.util.Assert;



/**
 * A typical text based document template context.
 */
public abstract class DocumentTemplateContext extends TemplateContext {

	/** The text of the document. */
	private final String fString;
	/** The completion position. */
	private final int fCompletionPosition;

	/**
	 * Creates a document template context.
	 */
	protected DocumentTemplateContext(ContextType type, String string, int completionPosition) {
		super(type);
		
		Assert.isNotNull(string);
		Assert.isTrue(completionPosition >= 0 && completionPosition <= string.length());
		
		fString= string;
		fCompletionPosition= completionPosition;
	}
	
	/**
	 * Returns the string of the context.
	 */
	public String getString() {
		return fString;
	}
	
	/**
	 * Returns the completion position within the string of the context.
	 */
	public int getCompletionPosition() {
		return fCompletionPosition;	
	}
	
	/**
	 * Returns the keyword which triggered template insertion.
	 */
	public String getKey() {
		return fString.substring(getStart(), getEnd());
	}

	/**
	 * Returns the beginning offset of the keyword.
	 */
	public int getStart() {
		return fCompletionPosition;		
	}
	
	/**
	 * Returns the end offset of the keyword.
	 */
	public int getEnd() {
		return fCompletionPosition;
	}
		
}

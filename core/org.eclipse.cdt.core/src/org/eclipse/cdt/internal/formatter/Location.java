/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.formatter;

/**
 * A location maintains positional information both in original source 
 * and in the output source.
 * It remembers source offsets, line/column and indentation level.
 * 
 * @since 4.0
 */
public class Location {
	public int inputOffset;
	public int outputLine;
	public int outputColumn;
	public int outputIndentationLevel;
	public boolean needSpace;
	public boolean pendingSpace;
	public int numberOfIndentations;

	// chunk management
	public int lastNumberOfNewLines;
	
	// edits management
	int editsIndex;
	OptimizedReplaceEdit textEdit;
	
	public Location(Scribe scribe, int sourceRestart){
		update(scribe, sourceRestart);
	}
	
	public void update(Scribe scribe, int sourceRestart){
		this.outputColumn = scribe.column;
		this.outputLine = scribe.line;
		this.inputOffset = sourceRestart;
		this.outputIndentationLevel = scribe.indentationLevel;
		this.lastNumberOfNewLines = scribe.lastNumberOfNewLines;
		this.needSpace = scribe.needSpace;
		this.pendingSpace = scribe.pendingSpace;
		this.editsIndex = scribe.editsIndex;
		this.numberOfIndentations = scribe.numberOfIndentations;
		textEdit = scribe.getLastEdit();
	}
}

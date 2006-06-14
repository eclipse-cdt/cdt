/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.textmanipulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;

public class GroupDescription {

	private String fDescription;
	private List fEdits;

	public GroupDescription() {
		this( "NO_DESCRIPTION"); //$NON-NLS-1$
	}

	public GroupDescription(String description) {
		super();
		Assert.isNotNull(description);
		fDescription= description;
		fEdits= new ArrayList(3);
	}

	public GroupDescription(String description, TextEdit[] edits) {
		super();
		Assert.isNotNull(description);
		Assert.isNotNull(edits);
		fDescription= description;
		fEdits= new ArrayList(Arrays.asList(edits));
	}

	public void addTextEdit(TextEdit edit) {
		fEdits.add(edit);
	}
	
	public boolean hasTextEdits() {
		return fEdits.isEmpty();
	}
	
	public TextEdit[] getTextEdits() {
		return (TextEdit[]) fEdits.toArray(new TextEdit[fEdits.size()]);
	}
	
	/**
	 * Returns the text range covered by the edits managed via this
	 * group description. The method requires that the group description
	 * manages at least one text edit.
	 */
	public IRegion getTextRange() {
		int size= fEdits.size();
		if (size == 1) {
			return ((TextEdit)fEdits.get(0)).getRegion();
		}
		return TextEdit.getCoverage((TextEdit[])fEdits.toArray(new TextEdit[fEdits.size()]));
	}
	
	public String getName() {
		return fDescription;
	}
}

/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors.parser;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class AutoconfElement {
	
	protected String name;
	protected String var;
	protected int startOffset;
	protected int endOffset;
	protected ArrayList<AutoconfElement> children;
	protected AutoconfElement parent;
	private IDocument document;
	
	public AutoconfElement(String name) {
		this(name, null);
	}
	
	public AutoconfElement(String name, String var) {
		this.name = name;
		this.var = var;
		this.startOffset = 0;
		this.children = new ArrayList<AutoconfElement>();
	}
	

	public String toString() {
		String source = getSource();
		if (source == null) {
			StringBuffer kids = new StringBuffer();
			for (Iterator<AutoconfElement> iterator = children.iterator(); iterator.hasNext();) {
				AutoconfElement kid = (AutoconfElement) iterator.next();
				kids.append(kid.toString());
				kids.append(","); //$NON-NLS-1$
			}
			source = kids.toString();
		}
		return getClass().getSimpleName() + ": '" + source + "'"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void addChild(AutoconfElement element) {
		children.add(element);
		if (element.getParent() == null)
			element.setParent(this);
	}
	
	public void addSibling(AutoconfElement element) {
		parent.addChild(element);
	}
	
	public AutoconfElement getLastChild() {
		if (hasChildren())
			return (AutoconfElement)children.get(children.size() - 1);
		return null;
	}
	
	public AutoconfElement getParent() {
		return parent;
	}
	
	public void setParent(AutoconfElement parent) {
		this.parent = parent;
	}
	
	public AutoconfElement[] getChildren() {
		return (AutoconfElement[]) children.toArray(new AutoconfElement[children.size()]);
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public String getName() {
		return name;
	}
	 
	public void setName(String string) {
		this.name = string;
	}

	
	public String getVar() {
		return var;
	}
	
	public void setVar(String value) {
		var = value;
	}
	
	public void setDocument(IDocument document) {
		this.document = document;
	}
	
	public IDocument getDocument() {
		return document;
	}
	
	public void setStartOffset(int offset) {
		this.startOffset = offset;
	}
	
	public int getStartOffset() {
		return startOffset;
	}
	
	public void setEndOffset(int offset) {
		this.endOffset = offset;
	}
	
	public int getEndOffset() {
		return endOffset;
	}

	public String getSource() {
		if (document != null && startOffset >= 0 && endOffset >= startOffset) {
			try {
				return document.get(startOffset, endOffset - startOffset);
			} catch (BadLocationException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Return the length of this element.
	 * @return the length of this element.
	 */
	public int getLength() {
		return endOffset - startOffset;
	}
}

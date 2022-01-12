/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.pro.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

/**
 * Very basic parser for Qt Project Files that uses regular expressions. For now, this class only supports finding variables within
 * a Document that follow the syntax:
 *
 * <pre>
 * <code>VARIABLE_NAME += value1 \ # comment
 *     value2 \ # comment
 *     value3</code>
 * </pre>
 *
 * The assignment operator may be one of =, +=, -=, or *= in accordance with qmake syntax. Variable names are not checked for
 * semantic validity. That is, this class does not make sure the variable name is a registered qmake variable, nor that there are
 * multiple instances of a variable in the document.
 */
public class QtProjectFileParser implements IDocumentListener {

	IDocument document;
	List<QtProjectVariable> variables;

	public QtProjectFileParser(IDocument doc) {
		if (doc == null) {
			throw new IllegalArgumentException("document cannot be null"); //$NON-NLS-1$
		}

		document = doc;
		variables = parse();
		document.addDocumentListener(this);
	}

	public IDocument getDocument() {
		return document;
	}

	private List<QtProjectVariable> parse() {
		// Just build the list from scratch
		List<QtProjectVariable> variables = new CopyOnWriteArrayList<>();
		try (Scanner scanner = new Scanner(document.get())) {
			QtProjectVariable next;
			while ((next = QtProjectVariable.findNextVariable(scanner)) != null) {
				variables.add(next);
			}
		}
		return variables;
	}

	/**
	 * Retrieves a specific Qt Project Variable from the provided <code>IDocument</code>. If the variable cannot be found,
	 * <code>null</code> is returned instead.
	 * <p>
	 * <b>Note:</b> This method is greedy in the sense that it returns the first match it finds. If multiple variables exist with
	 * the same name in the <code>IDocument</code>, this method will only return the first match.
	 * </p>
	 *
	 * @param name
	 *            the name of the variable
	 * @return the <code>QtProjectVariable</code> or <code>null</code> if it couldn't be found
	 */
	public QtProjectVariable getVariable(String name) {
		for (QtProjectVariable v : variables) {
			if (v.getName().equals(name)) {
				return v;
			}
		}
		return null;
	}

	/**
	 * Returns a list of all Qt Project Variables found within the provided <code>IDocument</code>. A fresh list is always returned
	 * with the internal list copied into it. As such, modifying this list does not modify the internal list of the parser.
	 *
	 * @return the list of all Qt Project Variables
	 */
	public List<QtProjectVariable> getAllVariables() {
		return new ArrayList<>(variables);
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		// Nothing to do
	}

	@Override
	public void documentChanged(DocumentEvent event) {
		// Re-parse the document every time it changes
		variables = parse();
	}

	@Override
	protected void finalize() throws Throwable {
		// Make sure that we are removed from the document's listeners
		if (document != null) {
			document.removeDocumentListener(this);
		}
	}
}

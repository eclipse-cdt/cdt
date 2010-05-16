/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jesper Kamstrup Linnet (eclipse@kamstrup-linnet.dk) - initial API and implementation
 *   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.eclipse.osgi.util.TextProcessor;

import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.cdt.internal.ui.util.SelectionUtil;

/**
 * Copies contents of a TreeViewer to the clipboard.
 */
public class CopyTreeAction extends Action {
	private static final char INDENTATION= '\t';

	private ViewPart fView;
	private TreeViewer fViewer;
	private final Clipboard fClipboard;

	public CopyTreeAction(String label, ViewPart view, Clipboard clipboard, TreeViewer viewer) {
		super(label);
		Assert.isNotNull(clipboard);
		fView= view;
		fClipboard= clipboard;
		fViewer= viewer;
	}

	public boolean canActionBeAdded() {
		Object element= SelectionUtil.getSingleElement(getSelection());
		return element != null;
	}

	private ISelection getSelection() {
		ISelectionProvider provider= fView.getSite().getSelectionProvider();

		if (provider != null) {
			return provider.getSelection();
		}

		return null;
	}

	/*
	 * @see IAction#run()
	 */
	@Override
	public void run() {
		StringBuilder buf= new StringBuilder();
		addChildren(fViewer.getTree().getSelection()[0], 0, buf);

		TextTransfer plainTextTransfer= TextTransfer.getInstance();
		try {
			fClipboard.setContents(
					new String[] { convertLineTerminators(buf.toString()) },
					new Transfer[] { plainTextTransfer });
		} catch (SWTError e) {
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
				throw e;
			if (MessageDialog.openQuestion(fView.getViewSite().getShell(),
					ActionMessages.CopyTreeAction_problem, ActionMessages.CopyTreeAction_clipboard_busy)) {
				run();
			}
		}
	}

	/**
	 * Adds the specified {@link TreeItem}'s text to the StringBuilder.
	 * 
	 * @param item the tree item
	 * @param indent the indent size
	 * @param buf the string buffer
	 */
	private void addChildren(TreeItem item, int indent, StringBuilder buf) {
		for (int i= 0; i < indent; i++) {
			buf.append(INDENTATION);
		}

		buf.append(TextProcessor.deprocess(item.getText()));
		buf.append('\n');

		if (item.getExpanded()) {
			TreeItem[] items= item.getItems();
			for (int i= 0; i < items.length; i++) {
				addChildren(items[i], indent + 1, buf);
			}
		}
	}

	static String convertLineTerminators(String in) {
		StringWriter stringWriter= new StringWriter();
		PrintWriter printWriter= new PrintWriter(stringWriter);
		StringReader stringReader= new StringReader(in);
		BufferedReader bufferedReader= new BufferedReader(stringReader);
		try {
			String line= bufferedReader.readLine();
			while (line != null) {
				printWriter.print(line);
				line= bufferedReader.readLine();
				if (line != null && line.length() != 0)
					printWriter.println();

			}
		} catch (IOException e) {
			return in; // return the call hierarchy unfiltered
		}
		return stringWriter.toString();
	}
}

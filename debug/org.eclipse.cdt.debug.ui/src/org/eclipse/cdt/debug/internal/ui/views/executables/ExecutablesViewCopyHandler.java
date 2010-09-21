/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.executables;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

public class ExecutablesViewCopyHandler extends AbstractHandler {

	private Clipboard clipboard;

	private Clipboard getClipboard() {
		if (clipboard == null)
			clipboard = new Clipboard(Display.getDefault());
		return clipboard;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (clipboard != null)
			clipboard.dispose();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection == null) {
			return null;
		}

		if (selection instanceof IStructuredSelection) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);

            Iterator<?> iter = ((IStructuredSelection) selection).iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof Executable) {
					Executable exe = (Executable) obj;
					ps.println(exe.getName());
				} else if (obj instanceof ITranslationUnit) {
					ITranslationUnit tu = (ITranslationUnit) obj;
					ps.println(tu.getLocation().toFile().getName());
				} else
					ps.println(obj.toString());

			}
            ps.flush();
            try {
				baos.flush();
			} catch (IOException e) {
				throw new ExecutionException("", e); //$NON-NLS-1$
			}
			Clipboard cp = getClipboard();
			cp.setContents(new Object[] { baos.toString().trim() },
					new Transfer[] { TextTransfer.getInstance() });
		}

		return null;
	}

}

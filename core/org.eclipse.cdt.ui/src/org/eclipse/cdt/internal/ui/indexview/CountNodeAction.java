/*******************************************************************************
 * Copyright (c) 2005, 2009 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.IPDOM;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMacro;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;

/**
 * @author dschaefer
 *
 */
public class CountNodeAction extends IndexAction {

	public CountNodeAction(IndexView view, TreeViewer viewer) {
		super(view, viewer, CUIPlugin.getResourceString("IndexView.CountSymbols.name")); //$NON-NLS-1$
	}

	@Override
	public boolean valid() {
		ISelection selection = viewer.getSelection();
		if (!(selection instanceof IStructuredSelection))
			return false;
		Object[] objs = ((IStructuredSelection)selection).toArray();
		for (int i = 0; i < objs.length; ++i)
			if (objs[i] instanceof ICProject)
				return true;
		return false;
	}

	static final int FILES = 0;
	static final int MACROS = 1;
	static final int SYMBOLS = 2;
	static final int REFS = 3;
	static final int DECLS = 4;
	static final int DEFS = 5;
	
	@Override
	public void run() {
		final int[] count = new int[6];
		
		try {
			ISelection selection = viewer.getSelection();
			if (!(selection instanceof IStructuredSelection))
				return;

			Object[] objs = ((IStructuredSelection)selection).toArray();
			for (int i = 0; i < objs.length; ++i) {
				if (!(objs[i] instanceof ICProject))
					continue;

				ICProject project = (ICProject)objs[i];
				IPDOM ipdom= CCoreInternals.getPDOMManager().getPDOM(project);
				if (!(ipdom instanceof PDOM)) {
					continue;
				}
				final PDOM pdom = (PDOM) ipdom;
				//pdom.getDB().reportFreeBlocks();

				pdom.acquireReadLock();
				try {
					pdom.getFileIndex().accept(new IBTreeVisitor() {
						@Override
						public int compare(long record) throws CoreException {
							return 0;
						}

						@Override
						public boolean visit(long record) throws CoreException {
							if (record != 0) {
								PDOMFile file = PDOMFile.recreateFile(pdom, record);
								++count[FILES];
								PDOMMacro macro = file.getFirstMacro();
								while (macro != null) {
									++count[MACROS];
									macro = macro.getNextMacro();
								}
							}
							return true;
						}
					});
					pdom.accept(new IPDOMVisitor() {
						@Override
						public boolean visit(IPDOMNode node)
								throws CoreException {
							++count[SYMBOLS];
							if (node instanceof PDOMBinding) {
								PDOMBinding binding = (PDOMBinding) node;
								for (PDOMName name = binding
										.getFirstReference(); name != null; name = name
										.getNextInBinding())
									++count[REFS];
								for (PDOMName name = binding
										.getFirstDeclaration(); name != null; name = name
										.getNextInBinding())
									++count[DECLS];
								for (PDOMName name = binding
										.getFirstDefinition(); name != null; name = name
										.getNextInBinding())
									++count[DEFS];
							}
							return true;
						}

						@Override
						public void leave(IPDOMNode node) throws CoreException {
						}
					});
				} finally {
					pdom.releaseReadLock();
				}
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
		
		MessageDialog.openInformation(null,
				CUIPlugin.getResourceString("IndexView.CountSymbols.title"), //$NON-NLS-1$
				CUIPlugin.getFormattedString("IndexView.CountSymbols.message", //$NON-NLS-1$
						new String[] {
							String.valueOf(count[0]),
							String.valueOf(count[1]),
							String.valueOf(count[2]),
							String.valueOf(count[3]),
							String.valueOf(count[4]),
							String.valueOf(count[5])
						}));
	}

}

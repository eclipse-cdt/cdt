/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.includebrowser;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.ui.part.ResourceTransfer;

public class IBDragSourceListener implements DragSourceListener {

	private TreeViewer fTreeViewer;
	private ArrayList<IBNode> fSelectedNodes = new ArrayList<>();
	private IBDropTargetListener fDropTargetListener;

	public IBDragSourceListener(TreeViewer viewer) {
		fTreeViewer = viewer;
	}

	@Override
	public void dragStart(DragSourceEvent event) {
		if (fDropTargetListener != null) {
			fDropTargetListener.setEnabled(false);
		}
		fSelectedNodes.clear();
		if (event.doit) {
			IStructuredSelection sel = (IStructuredSelection) fTreeViewer.getSelection();
			for (Iterator<?> iter = sel.iterator(); iter.hasNext();) {
				Object element = iter.next();
				if (element instanceof IBNode) {
					fSelectedNodes.add((IBNode) element);
				}
			}
			event.doit = !fSelectedNodes.isEmpty();
		}
	}

	public void setDependentDropTargetListener(IBDropTargetListener dl) {
		fDropTargetListener = dl;
	}

	@Override
	public void dragSetData(DragSourceEvent event) {
		if (ResourceTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = getResources();
		} else if (FileTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = getFiles();
		}
	}

	private String[] getFiles() {
		ArrayList<String> files = new ArrayList<>(fSelectedNodes.size());
		for (Iterator<IBNode> iter = fSelectedNodes.iterator(); iter.hasNext();) {
			IBNode node = iter.next();
			IIndexFileLocation ifl = (IIndexFileLocation) node.getAdapter(IIndexFileLocation.class);
			if (ifl != null) {
				IPath location = IndexLocationFactory.getAbsolutePath(ifl);
				if (location != null) {
					files.add(location.toOSString());
				}
			}
		}
		return files.toArray(new String[files.size()]);
	}

	private IFile[] getResources() {
		ArrayList<IFile> files = new ArrayList<>(fSelectedNodes.size());
		for (Iterator<IBNode> iter = fSelectedNodes.iterator(); iter.hasNext();) {
			IBNode node = iter.next();
			IFile file = (IFile) node.getAdapter(IFile.class);
			if (file != null) {
				files.add(file);
			}
		}
		return files.toArray(new IFile[files.size()]);
	}

	@Override
	public void dragFinished(DragSourceEvent event) {
		if (fDropTargetListener != null) {
			fDropTargetListener.setEnabled(true);
		}
		fSelectedNodes.clear();
	}
}

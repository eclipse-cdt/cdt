/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.includebrowser;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.*;
import org.eclipse.ui.part.ResourceTransfer;

public class IBDragSourceListener implements DragSourceListener {

    private TreeViewer fTreeViewer;
    private ArrayList fSelectedNodes= new ArrayList();
	private IBDropTargetListener fDropTargetListener;

    public IBDragSourceListener(TreeViewer viewer) {
        fTreeViewer= viewer;
    }

    public void dragStart(DragSourceEvent event) {
    	if (fDropTargetListener != null) {
    		fDropTargetListener.setEnabled(false);
    	}
        fSelectedNodes.clear();
        if (event.doit) {
            IStructuredSelection sel= (IStructuredSelection) fTreeViewer.getSelection();
            for (Iterator iter = sel.iterator(); iter.hasNext();) {
                Object element = iter.next();
                if (element instanceof IBNode) {
                    fSelectedNodes.add(element);
                }
            }
            event.doit= !fSelectedNodes.isEmpty();
        }
    }

    public void setDependentDropTargetListener(IBDropTargetListener dl) {
    	fDropTargetListener= dl;
    }
    
    public void dragSetData(DragSourceEvent event) {
        if (ResourceTransfer.getInstance().isSupportedType(event.dataType)) {
            event.data= getResources();
        }
        else if (FileTransfer.getInstance().isSupportedType(event.dataType)) {
            event.data= getFiles();
        }
    }

    private String[] getFiles() {
        ArrayList files= new ArrayList(fSelectedNodes.size());
        for (Iterator iter = fSelectedNodes.iterator(); iter.hasNext();) {
            IBNode node = (IBNode) iter.next();
            IFile file= (IFile) node.getAdapter(IFile.class);
            if (file != null) {
                IPath location= file.getLocation();
                if (location != null) {
                    files.add(location.toOSString());
                }
            }
        }
        return (String[]) files.toArray(new String[files.size()]);
    }

    private IFile[] getResources() {
        ArrayList files= new ArrayList(fSelectedNodes.size());
        for (Iterator iter = fSelectedNodes.iterator(); iter.hasNext();) {
            IBNode node = (IBNode) iter.next();
            IFile file= (IFile) node.getAdapter(IFile.class);
            if (file != null) {
                files.add(file);
            }
        }
        return (IFile[]) files.toArray(new IFile[files.size()]);
    }

    public void dragFinished(DragSourceEvent event) {
    	if (fDropTargetListener != null) {
    		fDropTargetListener.setEnabled(true);
    	}
        fSelectedNodes.clear();
    }
}

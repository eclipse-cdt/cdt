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

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ResourceTransfer;

import org.eclipse.cdt.core.model.ITranslationUnit;

public class IBDropTargetListener implements DropTargetListener {
    
    private IBViewPart fIncludeBrowser;
	private ITranslationUnit fTranslationUnit;
	private boolean fEnabled= true;

    public IBDropTargetListener(IBViewPart view) {
        fIncludeBrowser= view;
    }
    
    public void setEnabled(boolean val) {
    	fEnabled= val;
    }
    
    public void dragEnter(DropTargetEvent event) {
        fTranslationUnit= null;
        checkOperation(event);
        if (event.detail != DND.DROP_NONE) {
			if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
				fTranslationUnit= checkLocalSelection();
				if (fTranslationUnit == null) {
					TransferData alternateDataType = checkForAlternateDataType(event.dataTypes, 
							new Transfer[] {ResourceTransfer.getInstance(), FileTransfer.getInstance()});
					if (alternateDataType == null) {
						event.detail= DND.DROP_NONE;
					}
					else {
						event.currentDataType= alternateDataType;
					}
				}
        	}
        }
    }

	private TransferData checkForAlternateDataType(TransferData[] dataTypes, Transfer[] transfers) {
		for (int i = 0; i < dataTypes.length; i++) {
			TransferData dataType = dataTypes[i];
			for (int j = 0; j < transfers.length; j++) {
				Transfer transfer = transfers[j];
				if (transfer.isSupportedType(dataType)) {
					return dataType;
				}
			}
		}
		return null;
	}

	private ITranslationUnit checkLocalSelection() {
		ISelection sel= LocalSelectionTransfer.getTransfer().getSelection();
		if (sel instanceof IStructuredSelection) {
			for (Iterator iter = ((IStructuredSelection)sel).iterator(); iter.hasNext();) {
				Object element = iter.next();
				if (element instanceof ITranslationUnit) {
					return (ITranslationUnit) element;
				}
			}
		}
		return null;
	}

    public void dragLeave(DropTargetEvent event) {
    }

    public void dragOperationChanged(DropTargetEvent event) {
        checkOperation(event);
    }

    public void dragOver(DropTargetEvent event) {
    }

    public void drop(DropTargetEvent event) {
    	if (fTranslationUnit == null) {
    		fTranslationUnit= findFirstTranslationUnit(event.data);
    	}            
        if (fTranslationUnit == null) {
            fIncludeBrowser.setMessage(IBMessages.IBViewPart_falseInputMessage);
            Display.getCurrent().beep();
        }
        else {
            fIncludeBrowser.setInput(fTranslationUnit);
        }
    }

    private ITranslationUnit findFirstTranslationUnit(Object o) {
        if (o instanceof String[]) {
            String[] filePaths= (String[]) o;
            IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
            for (int i = 0; i < filePaths.length; i++) {
                String filePath = filePaths[i];
                ITranslationUnit tu= findTranslationUnit(root.findFilesForLocation(Path.fromOSString(filePath)));
                if (tu != null) {
                    return tu;
                }
            }
            return null;
        }
        if (o instanceof IResource[]) {
            return findTranslationUnit((IResource[]) o);
        }
        return null;
    }

    private ITranslationUnit findTranslationUnit(IResource[] files) {
        for (int i = 0; i < files.length; i++) {
            IResource resource = files[i];
            if (resource.getType() == IResource.FILE) {
                ITranslationUnit tu= IBConversions.fileToTU((IFile) resource);
                if (tu != null) {
                    return tu;
                }
            }
        }
        return null;
    }

    public void dropAccept(DropTargetEvent event) {
    }
    
    private void checkOperation(DropTargetEvent event) {
        if (fEnabled && (event.operations & DND.DROP_COPY) != 0) {
            event.detail= DND.DROP_COPY;
        }
        else {
            event.detail= DND.DROP_NONE;
        }
    }
}

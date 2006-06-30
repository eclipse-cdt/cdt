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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.cdt.core.model.*;
import org.eclipse.cdt.ui.CUIPlugin;

public class IBConversions {
    public static ITranslationUnit fileToTU(IFile file) {
		if (CoreModel.isTranslationUnit(file)) {
			ICProject cp= CoreModel.getDefault().getCModel().getCProject(file.getProject().getName());
			if (cp != null) {
				ICElement tu;
				try {
					tu = cp.findElement(file.getProjectRelativePath());
					if (tu instanceof ITranslationUnit) {
						return (ITranslationUnit) tu;
					}
				} catch (CModelException e) {
					CUIPlugin.getDefault().log(e);
				}
			}
		}
        return null;
    }

    public static IBNode selectionToNode(ISelection sel) {
        if (sel instanceof IStructuredSelection) {
            IStructuredSelection ssel= (IStructuredSelection) sel;
            for (Iterator iter = ssel.iterator(); iter.hasNext();) {
                Object o= iter.next();
                if (o instanceof IBNode) {
                    IBNode node = (IBNode) o;
                    return node;
                }
            }
        }
        return null;
    }

    public static ITranslationUnit selectionToTU(ISelection sel) {
        if (sel instanceof IStructuredSelection) {
            IStructuredSelection ssel= (IStructuredSelection) sel;
            for (Iterator iter = ssel.iterator(); iter.hasNext();) {
                ITranslationUnit tu= objectToTU(iter.next());
                if (tu != null) {
                    return tu;
                }
            }
        }
        return null;
    }

    public static ITranslationUnit objectToTU(Object object) {
        if (object instanceof ITranslationUnit) {
            return (ITranslationUnit) object;
        }
        if (object instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) object;
            ITranslationUnit result= (ITranslationUnit) adaptable.getAdapter(ITranslationUnit.class);
            if (result != null) {
                return result;
            }
            IFile file= (IFile) adaptable.getAdapter(IFile.class);
            if (file != null) {
                result= fileToTU(file);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}

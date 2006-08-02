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

package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CElementImageDescriptor;
import org.eclipse.cdt.ui.CElementLabelProvider;

import org.eclipse.cdt.internal.ui.viewsupport.ImageImageDescriptor;

public class CHLabelProvider extends LabelProvider implements IColorProvider {
    private CElementLabelProvider fCLabelProvider= new CElementLabelProvider(CElementLabelProvider.SHOW_PARAMETERS);
//    private Color fColorInactive;
    private CHContentProvider fContentProvider;
    private HashMap fCachedImages= new HashMap();
    private boolean fShowFiles;
    
    public CHLabelProvider(Display display, CHContentProvider cp) {
//        fColorInactive= display.getSystemColor(SWT.COLOR_DARK_GRAY);
        fContentProvider= cp;
    }
    
    public Image getImage(Object element) {
        if (element instanceof CHNode) {
            CHNode node= (CHNode) element;
            ICElement decl= node.getRepresentedDeclaration();
            if (decl != null) {
            	Image image= fCLabelProvider.getImage(decl);
            	return decorateImage(image, node);
            }
        }
        return super.getImage(element);
    }

    public String getText(Object element) {
        if (element instanceof CHNode) {
            CHNode node= (CHNode) element;
            ICElement decl= node.getRepresentedDeclaration();
            if (decl != null) {
            	String text= fCLabelProvider.getText(decl);
                if (fShowFiles) {
                	// mstodo append filenames
//                	ICElement tu= null;
//                	while (tu == null && decl != null) {
//                		if (decl instanceof ITranslationUnit) {
//                			tu= decl;
//                		}
//                		else {
//                			decl= decl.getParent();
//                		}
//                	}
//                	if (tu != null) {
//                		
                }
            	return text;
            }
        }
        return super.getText(element);
    }
    
    public void dispose() {
        fCLabelProvider.dispose();
        for (Iterator iter = fCachedImages.values().iterator(); iter.hasNext();) {
            Image image = (Image) iter.next();
            image.dispose();
        }
        fCachedImages.clear();
        super.dispose();
    }

    private Image decorateImage(Image image, CHNode node) {
        int flags= 0;        
        if (node.isRecursive()) {
            flags |= CElementImageDescriptor.RECURSIVE_RELATION;
        }
        else if (fContentProvider.hasChildren(node)) {
            if (fContentProvider.getComputeReferencedBy()) {
                flags |= CElementImageDescriptor.REFERENCED_BY;
            }
            else {
                flags |= CElementImageDescriptor.RELATES_TO;
            }
        }

//        if (node.getRepresentedTranslationUnit() == null) {
//        	flags |= CElementImageDescriptor.WARNING;
//        }

        if (flags == 0) {
            return image;
        }
        String key= image.toString()+String.valueOf(flags);
        Image result= (Image) fCachedImages.get(key);
        if (result == null) {
            ImageDescriptor desc= new CElementImageDescriptor(
                    new ImageImageDescriptor(image), flags, new Point(20,16));
            result= desc.createImage();
            fCachedImages.put(key, result);
        }
        return result;
    }

    public Color getBackground(Object element) {
        return null;
    }

    public Color getForeground(Object element) {
//        if (element instanceof CHNode) {
//            CHNode node= (CHNode) element;
//            if (!node.isActiveCode()) {
//                return fColorInactive;
//            }
//        }
        return null;
    }

    public void setShowFiles(boolean show) {
        fShowFiles= show;
    }
}

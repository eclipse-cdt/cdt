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

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CElementImageDescriptor;
import org.eclipse.cdt.ui.CElementLabelProvider;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.viewsupport.ImageImageDescriptor;

public class IBLabelProvider extends LabelProvider implements IColorProvider {
    private CElementLabelProvider fCLabelProvider= new CElementLabelProvider();
    private Color fColorInactive;
    private IBContentProvider fContentProvider;
    private HashMap fCachedImages= new HashMap();
    private boolean fShowFolders;
    
    public IBLabelProvider(Display display, IBContentProvider cp) {
        fColorInactive= display.getSystemColor(SWT.COLOR_DARK_GRAY);
        fContentProvider= cp;
    }
    
    public Image getImage(Object element) {
        if (element instanceof IBNode) {
            IBNode node= (IBNode) element;
            ITranslationUnit tu= node.getRepresentedTranslationUnit();
            Image image= tu != null ? fCLabelProvider.getImage(tu) : CPluginImages.get(CPluginImages.IMG_OBJS_TUNIT_HEADER);
            return decorateImage(image, node);
        }
        return super.getImage(element);
    }

    public String getText(Object element) {
        if (element instanceof IBNode) {
            IBNode node= (IBNode) element;
            IPath path= node.getRepresentedPath();
            if (path != null) {
            	if (fShowFolders) {
            		return path.lastSegment() + " (" + path.removeLastSegments(1) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            	}
            	return path.lastSegment();
            }
            return node.getDirectiveName();
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

    private Image decorateImage(Image image, IBNode node) {
        int flags= 0;
        if (node.isSystemInclude()) {
            flags |= CElementImageDescriptor.SYSTEM_INCLUDE;
        }
        
        if (node.isRecursive()) {
            flags |= CElementImageDescriptor.RECURSIVE_RELATION;
        }
        else if (fContentProvider.hasChildren(node)) {
            if (fContentProvider.getComputeIncludedBy()) {
                flags |= CElementImageDescriptor.REFERENCED_BY;
            }
            else {
                flags |= CElementImageDescriptor.RELATES_TO;
            }
        }

        if (node.getRepresentedTranslationUnit() == null) {
        	flags |= CElementImageDescriptor.WARNING;
        }

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
        if (element instanceof IBNode) {
            IBNode node= (IBNode) element;
            if (!node.isActiveCode()) {
                return fColorInactive;
            }
        }
        return null;
    }

    public void setShowFolders(boolean show) {
        fShowFolders= show;
    }
}

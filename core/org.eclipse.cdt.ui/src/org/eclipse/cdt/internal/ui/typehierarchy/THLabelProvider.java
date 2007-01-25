/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.typehierarchy;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CElementImageDescriptor;

import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.cdt.internal.ui.viewsupport.CUILabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.ImageImageDescriptor;

public class THLabelProvider extends LabelProvider implements IColorProvider {
	private final static int LABEL_OPTIONS_SIMPLE= CElementLabels.ALL_FULLY_QUALIFIED | CElementLabels.M_PARAMETER_TYPES;
	private final static int LABEL_OPTIONS_SHOW_FILES= LABEL_OPTIONS_SIMPLE | CElementLabels.MF_POST_FILE_QUALIFIED;
	
    private CUILabelProvider fCLabelProvider= new CUILabelProvider(LABEL_OPTIONS_SIMPLE, 0);
    private THHierarchyModel fModel;
    private HashMap fCachedImages= new HashMap();
	private Color fColorInactive;
    
    public THLabelProvider(Display display, THHierarchyModel model) {
        fColorInactive= display.getSystemColor(SWT.COLOR_DARK_GRAY);
        fModel= model;
    }
    
    public Image getImage(Object element) {
        if (element instanceof THNode) {
            THNode node= (THNode) element;
            ICElement decl= node.getElement();
            if (decl != null) {
            	Image image= fCLabelProvider.getImage(decl);
                if (image != null) {
                	return decorateImage(image, node);
                }
            }
        }
        else if (element instanceof ICElement) {
        	return fCLabelProvider.getImage(element);
        }
        return super.getImage(element);
    }

    public String getText(Object element) {
        if (element instanceof THNode) {
            THNode node= (THNode) element;
            ICElement decl= node.getElement();
            if (decl != null) {
            	String label= fCLabelProvider.getText(decl);
            	return label;
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

    private Image decorateImage(Image image, THNode node) {
        int flags= 0;        
        if (node.hasChildren()) {
            if (fModel.getHierarchyKind() == THHierarchyModel.SUPER_TYPE_HIERARCHY) {
            	flags |= CElementImageDescriptor.RELATES_TO;
            }
            else {
                flags |= CElementImageDescriptor.REFERENCED_BY;
            }
        }
        if (node.isImplementor()) {
        	flags |= CElementImageDescriptor.DEFINES;
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
    	if (element instanceof THNode) {
    		THNode node= (THNode) element;
    		if (node.isFiltered()) {
    			return fColorInactive;
    		}
    	}
    	return null;
    }

    public void setShowFiles(boolean show) {
		fCLabelProvider.setTextFlags(show ? LABEL_OPTIONS_SHOW_FILES : LABEL_OPTIONS_SIMPLE);
    }
}

/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.presentation;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.DisassemblyMessages;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationPresentation;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

/**
 * DisassemblyIPAnnotation
 */
public class DisassemblyIPAnnotation extends Annotation implements IAnnotationPresentation {

    public static final String ID_TOP = IDebugUIConstants.ANNOTATION_TYPE_INSTRUCTION_POINTER_CURRENT;
    public static final String ID_SECONDARY = IDebugUIConstants.ANNOTATION_TYPE_INSTRUCTION_POINTER_SECONDARY;

	private Image fImage;
	private int fContext = Integer.MIN_VALUE;

    /**
	 * Annotation denoting the current instruction pointer.
	 */
	public DisassemblyIPAnnotation(boolean isTopFrame, int context) {
		super(
			isTopFrame ? ID_TOP : ID_SECONDARY,
			false,
			isTopFrame ? DisassemblyMessages.DisassemblyIPAnnotation_primary
					   : DisassemblyMessages.DisassemblyIPAnnotation_secondary
		);
		setContext(context);
	}

	public boolean isTopFrame() {
		return ID_TOP.equals(getType());
	}
	
	public void setContext(int context) {
		if (context == fContext) {
			return;
		}
		fContext = context;
		// TLETODO [disassembly] context dependent IP icon
		if (isTopFrame()) {
			fImage = DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER_TOP);
		} else {
			fImage = DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER);
		}
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationPresentation#getLayer()
	 */
	@Override
	public int getLayer() {
		return 5;
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationPresentation#paint(org.eclipse.swt.graphics.GC, org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
	 */
	@Override
	public void paint(GC gc, Canvas canvas, Rectangle bounds) {
		Rectangle imageBounds = fImage.getBounds();
		gc.drawImage(fImage, bounds.x + (bounds.width - imageBounds.width) / 2 , bounds.y + (bounds.height - imageBounds.height) / 2);
	}
	
}

/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.form;

import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.cdt.linkerscript.linkerScript.Memory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class MemoryLabelProvider extends ColumnLabelProvider {
	private final Supplier<ILinkerScriptModel> modelSupplier;
	private final Function<Memory, String> op;
	private boolean image;

	public MemoryLabelProvider(Supplier<ILinkerScriptModel> modelSupplier, Function<Memory, String> op, boolean image) {
		this.modelSupplier = modelSupplier;
		this.op = op;
		this.image = image;
	}

	@Override
	public String getText(Object element) {
		return modelSupplier.get().readModel(element, Memory.class, "", op);
	}

	@Override
	public Image getImage(Object element) {
		if (image) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
		return null;
	}
}
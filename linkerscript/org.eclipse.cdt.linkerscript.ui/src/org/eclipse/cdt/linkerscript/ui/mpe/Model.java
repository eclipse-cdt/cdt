/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.mpe;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.cdt.linkerscript.ui.form.ILinkerScriptModel;
import org.eclipse.cdt.linkerscript.ui.form.ILinkerScriptModelListener;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.IXtextModelListener;
import org.eclipse.xtext.util.ITextRegion;

public class Model implements ILinkerScriptModel {
	private Supplier<IXtextDocument> documentSupplier;
	private ListenerList listeners = new ListenerList();
	private IXtextModelListener xtextListener = event -> {
		Object[] listenersObjs = listeners.getListeners();
		for (Object listener : listenersObjs) {
			((ILinkerScriptModelListener) listener).modelChanged();
		}
	};

	public Model(Supplier<IXtextDocument> documentSupplier) {
		this.documentSupplier = documentSupplier;
	}

	@Override
	public <T, P extends EObject> T readModel(Object uri, Class<P> clazz, T defaultValue, Function<P, T> op) {
		IXtextDocument document = documentSupplier.get();
		if (document == null) {
			return defaultValue;
		}

		if (!(uri instanceof String)) {
			return defaultValue;
		}

		return document.readOnly(resource -> {
			EObject element = resource.getEObject((String) uri);
			if (clazz.isInstance(element)) {
				@SuppressWarnings("unchecked")
				P typedElement = (P) element;
				return op.apply(typedElement);
			}
			return defaultValue;
		});
	}

	@Override
	public <P extends EObject> void writeModel(Object uri, Class<P> clazz, Consumer<P> op) {
		IXtextDocument document = documentSupplier.get();
		if (document == null) {
			return;
		}

		if (!(uri instanceof String)) {
			return;
		}

		document.modify(resource -> {
			EObject element = (EObject) resource.getEObject((String) uri);
			if (clazz.isInstance(element)) {
				@SuppressWarnings("unchecked")
				P elementTyped = (P) element;
				op.accept(elementTyped);
			}
			return null;
		});
	}

	@Override
	public void writeResource(Consumer<XtextResource> op) {
		IXtextDocument document = documentSupplier.get();
		document.modify(resource -> {
			op.accept(resource);
			return null;
		});
	}

	@Override
	public void writeText(Object uri, String text) {
		IXtextDocument document = documentSupplier.get();
		if (document == null) {
			return;
		}

		if (!(uri instanceof String)) {
			return;
		}

		ITextRegion region = readModel(uri, EObject.class, null, obj -> {
			ICompositeNode node = NodeModelUtils.getNode(obj);
			if (node == null) {
				return null;
			}
			return node.getTextRegion();
		});
		if (region != null) {
			try {
				document.replace(region.getOffset(), region.getLength(), text);
			} catch (BadLocationException e) {
				throw new RuntimeException("Failed to perform replacement", e);
			}
		}
	}

	@Override
	public void addModelListener(ILinkerScriptModelListener listener) {
		if (listeners.isEmpty()) {
			documentSupplier.get().addModelListener(xtextListener);
		}
		listeners.add(listener);
	}

	@Override
	public void removeModelListener(ILinkerScriptModelListener listener) {
		listeners.remove(listener);
		if (listeners.isEmpty()) {
			documentSupplier.get().removeModelListener(xtextListener);
		}
	}
}
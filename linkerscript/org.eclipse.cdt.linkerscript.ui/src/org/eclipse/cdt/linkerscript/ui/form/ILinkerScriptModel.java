/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.form;

import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

/**
 * Abstract the Linker Script model to allow different types of real model.
 * <p>
 * Key purpose of this method is it reads "safely" from the model when it is an
 * IXtextDocument using the {@link IXtextDocument#readOnly(IUnitOfWork)} safe
 * read if needed.
 */
public interface ILinkerScriptModel {

	/**
	 * Read the element in the resource specified by uri, performing the unit of
	 * work to obtain details.
	 * <p>
	 * <b>Do not return references to model from the op function.</b>
	 *
	 * @param <T>
	 *            return type
	 * @param <P>
	 *            element type uri refers to, determined by clazz
	 * @param uri
	 *            in the model
	 * @param clazz
	 *            expected object type in the model
	 * @param defaultValue
	 *            see return annotation for details
	 * @param op
	 *            operation to extract relevant info from the model element.
	 * @return defaultValue if uri does not refer to object of type clazz in the
	 *         document
	 */
	<T, P extends EObject> T readModel(Object uri, Class<P> clazz, T defaultValue, Function<P, T> op);

	/**
	 * Return the object type for the given URI.
	 *
	 * @param uri
	 * @return the class of the object at the given uri, or null if the uri is
	 *         somehow invalid.
	 */
	default Class<? extends EObject> getClass(Object uri) {
		return readModel(uri, EObject.class, null, EObject::getClass);
	}

	/**
	 * Write to the model at the given uri, the actual modification is performed
	 * by op.
	 * <p>
	 * <b>Do not return references to model from this method.</b>
	 *
	 * @param <P>
	 *            element type uri refers to, determined by clazz
	 * @param uri
	 *            in the model
	 * @param clazz
	 *            expected object type in the model
	 * @param op
	 *            the operation that does the modification to the model
	 */
	<P extends EObject> void writeModel(Object uri, Class<P> clazz, Consumer<P> op);

	void writeResource(Consumer<XtextResource> op);

	/**
	 * Write to the model's underlying file at the given uri.
	 *
	 * @param uri
	 *            in the model
	 * @param text
	 *            new text to replace file at the location given by the uri
	 */
	void writeText(Object uri, String text);

	/**
	 * Adds the give listener to the list of listeners notified on any model
	 * changes.
	 *
	 * @param listener
	 */
	void addModelListener(ILinkerScriptModelListener listener);

	/**
	 * Removes the give listener to the list of listeners notified on any model
	 * changes.
	 *
	 * @param listener
	 */
	void removeModelListener(ILinkerScriptModelListener listener);

}

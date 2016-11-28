/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.form.tests

import com.google.inject.Inject
import java.io.ByteArrayOutputStream
import java.util.function.Consumer
import java.util.function.Function
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript
import org.eclipse.cdt.linkerscript.ui.form.ILinkerScriptModel
import org.eclipse.cdt.linkerscript.ui.form.ILinkerScriptModelListener
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.junit4.util.ResourceHelper
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.resource.SaveOptions
import org.eclipse.xtext.resource.XtextResource

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

class LinkerScriptModel implements ILinkerScriptModel {

	public StringBuilder contents
	public LinkerScript model
	public XtextResource resource

	@Inject
	ResourceHelper resourceHelper


	def setContents(String string) {
		resource = resourceHelper.resource(string) as XtextResource
		this.contents = new StringBuilder(string)
		parse()
	}

	override <T, P extends EObject> readModel(Object uri, Class<P> clazz, T defaultValue, Function<P, T> op) {
		val object = resource.getEObject(uri as String)
		if (!clazz.isInstance(object)) {
			return defaultValue
		}
		return op.apply(object as P)
	}

	override <P extends EObject> writeModel(Object uri, Class<P> clazz, Consumer<P> op) {
		op.accept(resource.getEObject(uri as String) as P)
		serialize()
	}

	override writeResource(Consumer<XtextResource> op) {
		op.accept(resource)
		serialize()
	}

	override writeText(Object uri, String text) {
		val region = readModel(uri as String, EObject, null, [ obj |
			val node = NodeModelUtils.getNode(obj)
			if (node == null) {
				return null
			}
			return node.getTextRegion()
		])
		if (region == null) {
			return
		}
		contents.replace(region.offset, region.offset + region.length, text)
		parse()
	}

	def void parse() {
		resource.reparse(contents.toString)
		if (resource.getContents().isEmpty()) {
			model = null
		} else {
			model = resource.getContents().get(0) as LinkerScript
		}
		assertThat(resource.errors, is(empty()))
		assertThat(resource.warnings, is(empty()))
	}

	def void serialize() {
		val bos = new ByteArrayOutputStream();
		// need format, See SerializerTest.serializerMergesMemoryLines()
		val options = SaveOptions.newBuilder().format.options
		resource.save(bos, options.toOptionsMap());
		contents.replace(0, contents.length, bos.toString)
		parse()
		println(contents)
	}

	override void addModelListener(ILinkerScriptModelListener listener) {
		// Not part of tests, tests have to manage refresh manually if needed
	}

	override void removeModelListener(ILinkerScriptModelListener listener) {
		// Not part of tests, tests have to manage refresh manually if needed
	}

}

package org.eclipse.cdt.linkerscript.ui.form.tests

import com.google.inject.Inject
import java.io.ByteArrayOutputStream
import java.util.function.Consumer
import java.util.function.Function
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript
import org.eclipse.cdt.linkerscript.ui.form.ILinkerScriptModel
import org.eclipse.cdt.linkerscript.ui.form.ILinkerScriptModelListener
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.resource.SaveOptions

import static org.junit.Assert.*

class LinkerScriptModel implements ILinkerScriptModel {

	public StringBuilder contents
	public LinkerScript model

	@Inject
	ParseHelper<LinkerScript> helper

	def setContents(String string) {
		this.contents = new StringBuilder(string)
		parse()
	}

	override <T, P extends EObject> readModel(Object uri, Class<P> clazz, T defaultValue, Function<P, T> op) {
		val object = model.eResource.getEObject(uri as String)
		if (!clazz.isInstance(object)) {
			return defaultValue
		}
		return op.apply(object as P)
	}

	override <P extends EObject> writeModel(Object uri, Class<P> clazz, Consumer<P> op) {
		op.accept(model.eResource.getEObject(uri as String) as P)
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
		model = helper.parse(contents)
		// TODO verify no errors
		assertNotNull(model)
	}

	def void serialize() {
		val bos = new ByteArrayOutputStream();
		val options = SaveOptions.newBuilder().options
		model.eResource().save(bos, options.toOptionsMap());
		contents.replace(0, contents.length, bos.toString)
		parse()
		println(contents)
	}

	override void addModelListener(ILinkerScriptModelListener listener) {
		// XXX: Not part of tests
	}

	override void removeModelListener(ILinkerScriptModelListener listener) {
		// XXX: Not part of tests
	}

}

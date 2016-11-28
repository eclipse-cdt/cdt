/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.serializer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.SaveOptions;
import org.eclipse.xtext.serializer.impl.Serializer;
import org.eclipse.xtext.util.ReplaceRegion;

public class LinkerScriptSerializer extends Serializer {
	/**
	 * There is a bug in the formatter/serializer that I have not successfully
	 * resolved/understood. This code is a "last chance" to fixup lost
	 * whitespace that would corrupt the document.
	 *
	 * See ignored tests SerializerTest.testSerializeReplacement and
	 * testSerializeReplacementWithFormat for tests that are affected
	 */
	@Override
	public ReplaceRegion serializeReplacement(EObject obj, SaveOptions options) {
		ICompositeNode node = NodeModelUtils.findActualNodeFor(obj);
		if (node == null) {
			throw new IllegalStateException("Cannot replace an obj that has no associated node");
		}
		String text = serialize(obj, options);
		int totalOffset = node.getTotalOffset();
		if (text != null && !text.isEmpty()) {
			boolean startsWithWhitespace = Character.isWhitespace(text.charAt(0)) || totalOffset == 0;
			boolean endsWithWhitspace = Character.isWhitespace(text.charAt(text.length() - 1));
			if (!startsWithWhitespace && !endsWithWhitspace) {
				text = " " + text + " ";
			} else if (!startsWithWhitespace) {
				text = " " + text;
			} else if (!endsWithWhitspace) {
				text = text + " ";
			} else {
				// already has whitespace on both sides
			}
		}
		return new ReplaceRegion(totalOffset, node.getTotalLength(), text);
	}
}

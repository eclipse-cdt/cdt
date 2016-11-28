/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.tests

import java.io.ByteArrayOutputStream
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.resource.SaveOptions

import static org.junit.Assert.*

class TestUtils {
	def static String serialize(EObject obj) {
		val bos = new ByteArrayOutputStream();
		val options = SaveOptions.newBuilder().options
		obj.eResource().save(bos, options.toOptionsMap());
		return bos.toString;
	}

	def static void assertEObjectEquals(EObject expected, EObject actual) {
		if (EcoreUtil.equals(expected, actual)) {
			return;
		}

		// if the objects are different, then the string
		// representation is probably different, assert
		// on that because it gives better error messages
		val actualSerial = actual.serialize
		assertEquals(expected.serialize, actualSerial)

		// hmm, should be unreachable
		fail("Objects compared unequal, but serialized were the same:\n" + actualSerial)
	}

}

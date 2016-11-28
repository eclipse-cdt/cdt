/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.validation

import com.google.inject.Inject
import com.google.inject.Provider
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript
import org.eclipse.cdt.linkerscript.util.LinkerScriptModelUtils
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.resource.IResourceServiceProvider
import org.eclipse.xtext.util.CancelIndicator
import org.eclipse.xtext.validation.CancelableDiagnostician
import org.eclipse.xtext.validation.Check
import org.eclipse.xtext.validation.INamesAreUniqueValidationHelper

import static org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptPackage.Literals.*

/**
 * This class contains custom validation rules.
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
class LinkerScriptValidator extends AbstractLinkerScriptValidator {
	@Inject
	private IResourceServiceProvider.Registry resourceServiceProviderRegistry = IResourceServiceProvider.Registry.
		INSTANCE

	@Inject
	private Provider<LExpressionReducer> reducerProvider;

	@Inject
	private INamesAreUniqueValidationHelper helper;

	@Check
	def void checkUniqueNamesInResourceOf(EObject eObject) {
		val context = getContext();
		val resource = eObject.eResource();
		if (resource == null)
			return;
		var CancelIndicator cancelIndicator = null;
		if (context != null) {
			if (context.containsKey(resource))
				return; // resource was already validated
			context.put(resource, this);
			cancelIndicator = context.get(CancelableDiagnostician.CANCEL_INDICATOR) as CancelIndicator
		}
		doCheckUniqueNames(resource, cancelIndicator);
	}

	def doCheckUniqueNames(Resource resource, CancelIndicator cancelIndicator) {
		val resourceServiceProvider = resourceServiceProviderRegistry.getResourceServiceProvider(resource.getURI());
		if (resourceServiceProvider == null)
			return;
		val manager = resourceServiceProvider.getResourceDescriptionManager();
		if (manager != null) {
			val description = manager.getResourceDescription(resource);
			if (description != null) {
				val descriptions = description.getExportedObjects();
				val filtered = descriptions.filter[desc|desc.EClass == MEMORY || desc.EClass == OUTPUT_SECTION]
				helper.checkUniqueNames(filtered, cancelIndicator, this);
			}
		}
	}

	@Check
	def void checkMemoryExpressionsConstant(LinkerScript ld) {
		val memories = LinkerScriptModelUtils.getAllMemories(ld)
		val reducer = reducerProvider.get()
		for (memory : memories) {
			val origin = reducer.reduceToLong(memory.origin)
			if (origin.isPresent) {
				reducer.memoryOriginMap.put(memory.name, origin.get())
			} else {
				warning("Memory's origin is not constant", memory, MEMORY__ORIGIN, INSIGNIFICANT_INDEX, null)
			}

			val length = reducer.reduceToLong(memory.length)
			if (length.isPresent) {
				reducer.memoryLengthMap.put(memory.name, length.get())
			} else {
				warning("Memory's length is not constant", memory, MEMORY__LENGTH, INSIGNIFICANT_INDEX, null)
			}
		}
	}

}

/********************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 ********************************************************************************/

package org.eclipse.tm.internal.discovery.model.util;

import java.util.Map;

import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.xmi.util.XMLProcessor;

import org.eclipse.tm.discovery.model.ModelPackage;

/**
 * This class contains helper methods to serialize and deserialize XML documents
 * 
 * @generated
 */
public class ModelXMLProcessor extends XMLProcessor {
	/**
	 * Public constructor to instantiate the helper.
	 * 
	 * @generated
	 */
	public ModelXMLProcessor() {
		super((EPackage.Registry.INSTANCE));
		ModelPackage.eINSTANCE.eClass();
	}
	
	/**
	 * Register for "*" and "xml" file extensions the ModelResourceFactoryImpl factory.
	 * 
	 * @generated
	 */
	protected Map getRegistrations() {
		if (registrations == null) {
			super.getRegistrations();
			registrations.put(XML_EXTENSION, new ModelResourceFactoryImpl());
			registrations.put(STAR_EXTENSION, new ModelResourceFactoryImpl());
		}
		return registrations;
	}

} //ModelXMLProcessor

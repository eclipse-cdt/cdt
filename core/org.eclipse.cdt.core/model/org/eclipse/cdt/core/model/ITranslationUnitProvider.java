/*
 * Copyright (c) 2013 Zeligsoft (2009) Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.core.model;

/**
 * Represents a workbench object that is able to provide instances of ITranslationUnit.  For
 * example, the CEditor (in the CDT UI plugin) implements this interface in order to provide
 * a copy of the editor's active translation unit.
 */
public interface ITranslationUnitProvider {

	/**
	 * Return the translation unit that is provided by the receiver or null if there is no
	 * such translation unit.
	 */
	public ITranslationUnit getTranslationUnit();
}

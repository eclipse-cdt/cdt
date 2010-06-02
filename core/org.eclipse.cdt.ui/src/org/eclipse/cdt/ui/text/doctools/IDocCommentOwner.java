/*******************************************************************************
 * Copyright (c) 2008, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.text.doctools;

/**
 * A comment owner provides {@link IDocCommentViewerConfiguration} to
 * the CDT c/c++ editor.<p>
 * 
 * In future it may also provide a point for providing
 * <ul>
 * <li>access an implementation of a documentation comment validation mechanism
 * <li>owner specific preference/property pages
 * <li>information for code generation e.g. default single and multi-line comment
 * delimiting prefix/post-fixes
 * </ul>
 *  
 * @since 5.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IDocCommentOwner {
	/**
	 * @return the non-null unique identifier for this comment owner. If contributed via the
	 * extension point, the ID corresponds to that in plugin.xml. 
	 */
	String getID();
	
	/**
	 * @return a non-null human-readable name for this comment owner. If contributed via plugin.xml
	 * this name can be localized using the plug-in externalization mechanism.
	 */
	String getName();

	/**
	 * @return a non-null {@link IDocCommentViewerConfiguration} suitable for a multi-line comment context
	 */
	IDocCommentViewerConfiguration getMultilineConfiguration();
	
	/**
	 * @return a non-null {@link IDocCommentViewerConfiguration} suitable for a single-line comment context
	 */
	IDocCommentViewerConfiguration getSinglelineConfiguration();
}

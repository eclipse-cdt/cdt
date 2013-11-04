/*******************************************************************************
 * Copyright (c) 2009, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.language.settings.providers;


/**
 * This class has been moved to the cdt.core plugin.
 * @noextend This class is not intended to be subclassed by clients.
 *
 * @since 8.1
 * @deprecated Since CDT 8.3.0. Use org.eclipse.cdt.core.language.settings.providers.AbstractBuildCommandParser instead.
 */
@Deprecated
public abstract class AbstractBuildCommandParser extends org.eclipse.cdt.core.language.settings.providers.AbstractBuildCommandParser {
	/**
	 * @deprecated Use {@link org.eclipse.cdt.core.language.settings.providers.AbstractBuildCommandParser.ResourceScope} instead.
	 */
	@Deprecated
	public enum ResourceScope { FILE, FOLDER, PROJECT }
	/**
	 * @deprecated Use {@link org.eclipse.cdt.core.language.settings.providers.AbstractBuildCommandParser#setRcScope(org.eclipse.cdt.core.language.settings.providers.AbstractBuildCommandParser.ResourceScope)} instead.
	 */
	@Deprecated
	public void setResourceScope(ResourceScope rs) {
		switch(rs) {
		case FILE:
			super.setRcScope(org.eclipse.cdt.core.language.settings.providers.AbstractBuildCommandParser.ResourceScope.FILE);
			break;
		case FOLDER:
			super.setRcScope(org.eclipse.cdt.core.language.settings.providers.AbstractBuildCommandParser.ResourceScope.FOLDER);
			break;
		case PROJECT:
			super.setRcScope(org.eclipse.cdt.core.language.settings.providers.AbstractBuildCommandParser.ResourceScope.PROJECT);
			break;
		}
	}

	/**
	 * @deprecated Use {@link org.eclipse.cdt.core.language.settings.providers.AbstractBuildCommandParser#getRcScope()} instead.
	 */
	@Deprecated
	public ResourceScope getResourceScope() {
		org.eclipse.cdt.core.language.settings.providers.AbstractBuildCommandParser.ResourceScope rs = getRcScope();
		switch(rs) {
		case FILE:
			return ResourceScope.FILE;
		case FOLDER:
			return ResourceScope.FOLDER;
		case PROJECT:
			return ResourceScope.PROJECT;
		}
		return null;
	}

	protected static abstract class AbstractBuildCommandPatternHighlighter extends org.eclipse.cdt.core.language.settings.providers.AbstractBuildCommandParser.AbstractBuildCommandPatternHighlighter {
		public AbstractBuildCommandPatternHighlighter(String parserId) {
			super(parserId);
		}
	}
}

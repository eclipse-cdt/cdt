/*******************************************************************************
 * Copyright (c) 2006, 2011 Siemens AG and others.
 * All rights reserved. This content and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Norbert Ploett - Initial implementation
 * Sami Wagiaalla (Red Hat) - Bug 352166: Added attributes and type API
 *                            and improved documentation.
 *******************************************************************************/

package org.eclipse.cdt.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * {@code ProblemMarkerInfo} is an object used to pass error properties to {@link ErrorParserManager}.
 * The information stored in this object will later be used to create an {@link IMarker} by {@link ACBuilder}
 * @see ErrorParserManager#addProblemMarker(ProblemMarkerInfo)
 * @see ErrorParserManager#generateMarker(IResource, int, String, int, String)
 * @see ErrorParserManager#generateExternalMarker(IResource, int, String, int, String, IPath)
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public  class ProblemMarkerInfo {
		
		public IResource file;
		public int lineNumber;
		/**
		 * @since 5.4
		 */
		public int startChar;
		/**
		 * @since 5.4
		 */
		public int endChar;
		public String description;
		public int severity;
		public String variableName;
		public IPath externalPath ;
		private Map<String, String> attributes;
		private String type;

		/**
		 * Create a new {@link ProblemMarkerInfo} object.
		 * 
		 * @param file - the file where the problem has occurred.
		 * @param lineNumber - the line number of the problem.
		 * @param description - a description of the problem.
		 * @param severity - the severity of the problem, see {@link IMarkerGenerator}
		 *        for acceptable severity values.
		 * @param variableName - the name of the variable involved in the error if any.
		 */
		public ProblemMarkerInfo(IResource file, int lineNumber, String description, int severity, String variableName) {
			this(file, lineNumber, -1,-1, description, severity, variableName);
		}

		/**
		 * Create a new {@link ProblemMarkerInfo} object.
		 * 
		 * @param file - the file where the problem has occurred.
		 * @param lineNumber - the line number of the problem.
		 * @param startChar - start char of the problem.
		 * @param endChar - end char of the problem.
		 * @param description - a description of the problem.
		 * @param severity - the severity of the problem, see {@link IMarkerGenerator}
		 *        for acceptable severity values.
		 * @param variableName - the name of the variable involved in the error if any.
		 * @since 5.4
		 */
		public ProblemMarkerInfo(IResource file, int lineNumber, int startChar, int endChar, 
				String description, int severity, String variableName) {
			this.file = (file != null) ? file : ResourcesPlugin.getWorkspace().getRoot();
			this.lineNumber = lineNumber;
			this.description = description;
			this.severity = severity;
			this.variableName = variableName;
			this.externalPath = null ;
			this.type = null;
			this.attributes = new HashMap<String, String>();
			this.startChar = -1;
			this.endChar = -1;
		}

		/**
		 * Create a new {@link ProblemMarkerInfo} object.
		 * 
		 * @param file - the file where the problem has occurred.
		 * @param lineNumber - the line number of the problem.
		 * @param description - a description of the problem.
		 * @param severity - the severity of the problem, see {@link IMarkerGenerator}
		 *        for acceptable severity values
		 * @param variableName - the name of the variable involved in the error if any.
		 * @param externalPath - if this error involves a file outside the workspace this parameter should
		 *                       contain the path to that file.
		 */
		public ProblemMarkerInfo(IResource file, int lineNumber, String description, int severity, String variableName, IPath externalPath) {
			this(file, lineNumber, description, severity, variableName);
			this.externalPath = externalPath;
		}
		
		/**
		 * Get the attribute map.
		 * @return Map of attributes and their values.
		 * @since 5.4
		 */
		public Map<String, String> getAttributes(){
			return this.attributes;
		}

		/**
		 * Return the value of the attribute with the given key,
		 * or null if no such attribute exists.
		 * 
		 * @param key - attribute key.
		 * @return attribute value
		 * @since 5.4
		 */
		public String getAttribute(String key){
			return this.attributes.get(key);
		}

		/**
		 * Set the value of the attribute with the given key
		 * to the given value, or add one if one does not already
		 * exist.
		 * 
		 * @param key - attribute key.
		 * @param value - new attribute value.
		 * @since 5.4
		 */
		public void setAttribute(String key, String value){
			this.attributes.put(key, value);
		}

		/**
		 * Return the type of this problem marker or null
		 * if type was not set.
		 * @return the type.
		 * @since 5.4
		 */
		public String getType() {
			return this.type;
		}

		/**
		 * Set the type of this problem marker.
		 * 
		 * @param type - the new type.
		 * @since 5.4
		 */
		public void setType(String type){
			this.type = type;
		}
}
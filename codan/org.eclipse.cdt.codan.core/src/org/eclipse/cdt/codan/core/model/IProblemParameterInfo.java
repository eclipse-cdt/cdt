/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

/**
 * Problem parameter usually key=value settings that allows to alter checker
 * behaviour for given problem. For example if checker finds violation of naming
 * conventions for function, parameter would be the pattern of allowed names.
 * ProblemParameterInfo represent parameter meta-info for the ui.
 * If more that one parameter required ParameterInfo should describe hash or array of parameters.
 * This is only needed for auto-generated ui for parameter editing. For complex case custom ui control should be used
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IProblemParameterInfo {
	String getKey();

	/**
	 * type of the parameter, supports boolean, integer, string, file and list.
	 * If list is the value - it is an array - subparameter can be accessed by number, if
	 * hash is the value - it is a hash - subparameter can be accesses by name
	 * 
	 * @return string value of the type
	 */
	String getType();

	/**
	 * Additional info on how it is represented in the ui, for example boolean
	 * can be represented as checkbox, drop-down and so on, Values TBD
	 * 
	 * @return ui info or null if not set
	 */
	String getUiInfo();

	/**
	 * User visible label for the parameter control in UI
	 * @return the label
	 */
	String getLabel();

	/**
	 * Available if type is list or hash. Returns value of subparamer with the
	 * name of key. For the "list" type key is the number (index).
	 * 
	 * @param key
	 *            - name of the subparameter.
	 * @return
	 */
	IProblemParameterInfo getElement(String key);
}

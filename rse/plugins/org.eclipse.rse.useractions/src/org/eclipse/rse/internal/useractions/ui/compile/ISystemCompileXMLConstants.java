/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Xuan Chen        (IBM)    - [222263] Need to provide a PropertySet Adapter for System Team View (cleanup some use action stuff)
 *******************************************************************************/

package org.eclipse.rse.internal.useractions.ui.compile;

import org.eclipse.rse.core.RSECorePlugin;

/**
 * Constants used in the persistence of the compile commands, into an xml file
 */
public interface ISystemCompileXMLConstants {
	
	public static final String COMPILE_COMMAND_PROPRERTY_SET_PREFIX = "CompileCommand."; //$NON-NLS-1$
	public static final String COMPILE_COMMAND_NAME = "Compile Commands"; //$NON-NLS-1$
	
	// root tag
	/**
	 * The name of the root element (tag) for the compile types xml file. That element is named "types".
	 */
	public static final String ROOT_ELEMENT = "types"; //$NON-NLS-1$
	/**
	 * The name of the attribute holding the version number when this document was last written.
	 * The xml attribute is named "version".
	 */
	public static final String VERSION_ATTRIBUTE = "version"; //$NON-NLS-1$
	/**
	 * Current version number for the compile framework
	 */
	public static final String VERSION_VALUE = RSECorePlugin.CURRENT_RELEASE_NAME; // changed from "5.1.0" by Phil
	/**
	 * The name of the copyright element (tag) holding the copyright value. That element is named "copyright".
	 */
	public static final String COPYRIGHT_ELEMENT = "copyright"; //$NON-NLS-1$
	/**
	 * The data of the copyright element (tag).
	 */
	public static final String COPYRIGHT_TEXT = "Copyright (c) IBM Corporation and others 2002, 2007"; //$NON-NLS-1$
	// type tag
	/**
	 * The name of the element (tag) containing all the compile command sub-elements (tags) for
	 *  a source type. 
	 * <p> 
	 * The xml element is named "compiletype".
	 */
	public static final String TYPE_ELEMENT = "compiletype"; //$NON-NLS-1$
	public static final String SOURCETYPE_ATTRIBUTE = "sourcetype"; //$NON-NLS-1$
	/**
	 * The name of the attribute holding the last-used compile command. This value identifies that 
	 *   command via its label value. 
	 * <p>
	 * The xml attribute is named "lastcompilename",  for historical reasons (when compile commands
	 *  were called compile names).
	 */
	public static final String LASTUSED_ATTRIBUTE = "lastcompilename"; //$NON-NLS-1$
	// compile name tag
	/**
	 * The name of the element (tag) containing all the compile command attributes. 
	 * The xml element is named "compilename", for historical reasons (when compile commands
	 *  were called compile names).
	 */
	public static final String COMPILECOMMAND_ELEMENT = "compilename"; //$NON-NLS-1$
	/**
	 * The name of the attribute holding the compile command label. This is the name the user sees for this
	 *   compile command. The xml attribute is named "name" for historical reasons (when compile commands
	 *   were called compile names).
	 */
	public static final String LABEL_ATTRIBUTE = "name"; //$NON-NLS-1$
	public static final String TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$
	/**
	 * The name of the attribute holding the nature value. This tells the framework if this is
	 *   an IBM-supplied or user-supplied compile command. The xml attribute is named "nature".
	 */
	public static final String NATURE_ATTRIBUTE = "nature"; //$NON-NLS-1$
	/**
	 * Value for the compile command nature attribute for IBM-supplied commands: "IBM defined"
	 */
	public static final String NATURE_IBM_VALUE = "IBM defined"; //$NON-NLS-1$
	/**
	 * Value for the compile command nature attribute for user-supplied commands: "User defined"
	 */
	public static final String NATURE_USER_VALUE = "User defined"; //$NON-NLS-1$
	/**
	 * Value for the compile command nature attribute for vendor-supplied commands: "ISV defined"
	 */
	public static final String NATURE_ISV_VALUE = "ISV defined"; //$NON-NLS-1$
	/**
	 * The name of the attribute holding the default string value. This is the IBM-supplied value for 
	 *   support of "Restore Defaults". The xml attribute is named "default".
	 */
	public static final String DEFAULT_ATTRIBUTE = "default"; //$NON-NLS-1$
	/**
	 * The name of the attribute holding the current string value. This is the potentially user-edited
	 *   compile command including parameters. The xml attribute is named "current".
	 */
	public static final String CURRENT_ATTRIBUTE = "current"; //$NON-NLS-1$
	/**
	 * The name of the attribute holding the menu option value. This tells the compile framework if this
	 *   user action is to displayed in the non-promptable cascading menu, the promptable cascading menu,
	 *   or both cascading menus. These menus shown in the popup menu for a compilable remote source
	 *   object.
	 * The xml attribute is named "menu".
	 */
	public static final String MENU_ATTRIBUTE = "menu"; //$NON-NLS-1$
	/**
	 * Value for the compile command menu attribute for prompt-only commands: "Prompt"
	 */
	public static final String MENU_PROMPTABLE_VALUE = "Prompt"; //$NON-NLS-1$
	/**
	 * Value for the compile command menu attribute for no-prompt-only commands: "NoPrompt"
	 */
	public static final String MENU_NON_PROMPTABLE_VALUE = "NoPrompt"; //$NON-NLS-1$
	/**
	 * Value for the compile command menu attribute for both prompt and no-prompt commands: "Both"
	 */
	public static final String MENU_BOTH_VALUE = "Both"; //$NON-NLS-1$
	/**
	 * Value for the compile command menu attribute for neither promptable nor non-promptable commands: "None".
	 * These compile commands do not appear in the menu.
	 */
	public static final String MENU_NONE_VALUE = "None"; //$NON-NLS-1$
	/**
	 * The name of the attribute holding the relative order the compile command is to appear in any 
	 *  list of compile commands: "order"
	 */
	public static final String ORDER_ATTRIBUTE = "order"; //$NON-NLS-1$
	/**
	 * The name of the attribute holding the job environment value. This is not used in all cases, but those
	 *   that need it (such as for iSeries IFS which needs to prompt for QSYS vs QSHELL cmd), this is where to
	 *   store it. The attribute name is "jobenv".
	 */
	public static final String JOBENV_ATTRIBUTE = "jobenv"; //$NON-NLS-1$
	/**
	 * The name of the attribute holding the id: "id"
	 */
	public static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	/**
	 * The name of the attribute holding whether the label is editable: "labeleditable"
	 */
	public static final String LABEL_EDITABLE_ATTRIBUTE = "labeleditable"; //$NON-NLS-1$
	/**
	 * The name of the attribute holding whether the command string is editable: "stringeditable"
	 */
	public static final String STRING_EDITABLE_ATTRIBUTE = "stringeditable"; //$NON-NLS-1$
}
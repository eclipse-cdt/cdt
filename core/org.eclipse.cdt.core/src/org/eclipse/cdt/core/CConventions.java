/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.core;

import java.util.StringTokenizer;

import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.model.CModelStatus;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
/**
 * @author hamer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

public class CConventions {
	private final static String scopeResolutionOperator= "::";
	private final static char fgDot= '.';
	private final static char fgColon= ':';
	
	/**
	 * Validate the given CPP class name, either simple or qualified.
	 * For example, <code>"A::B::C"</code>, or <code>"C"</code>.
	 * <p>
	 *
	 * @param name the name of a class
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a CPP class name, 
	 *      a status with code <code>IStatus.WARNING</code>
	 *		indicating why the given name is discouraged, 
	 *      otherwise a status object indicating what is wrong with 
	 *      the name
	 */
	public static IStatus validateClassName(String name) {
		if (name == null) {
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Util.bind("convention.class.nullName"), null); //$NON-NLS-1$
		}
		String trimmed = name.trim();
		if ((!name.equals(trimmed)) || (name.indexOf(" ") != -1) ){
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Util.bind("convention.class.nameWithBlanks"), null); //$NON-NLS-1$
		}
		int index = name.lastIndexOf(scopeResolutionOperator);
		char[] scannedID;
		if (index == -1) {
			// simple name
			scannedID = name.toCharArray();
		} else {
			// qualified name
			String pkg = name.substring(0, index).trim();
			IStatus status = validateScopeName(pkg);
			if (!status.isOK()) {
				return status;
			}
			String type = name.substring(index + 1).trim();
			scannedID = type.toCharArray();
		}
	
		if (scannedID != null) {
			IStatus status = ResourcesPlugin.getWorkspace().validateName(new String(scannedID), IResource.FILE);
			if (!status.isOK()) {
				return status;
			}
			if (CharOperation.contains('$', scannedID)) {
				return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, -1, Util.bind("convention.class.dollarName"), null); //$NON-NLS-1$
			}
			if ((scannedID.length > 0 && Character.isLowerCase(scannedID[0]))) {
				return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, -1, Util.bind("convention.class.lowercaseName"), null); //$NON-NLS-1$
			}
			return CModelStatus.VERIFIED_OK;
		} else {
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Util.bind("convention.class.invalidName", name), null); //$NON-NLS-1$
		}
	}
	/**
	 * Validate the given scope name.
	 * <p>
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a class name, otherwise a status 
	 *		object indicating what is wrong with the name
	 */
	public static IStatus validateScopeName(String name) {
	
		if (name == null) {
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Util.bind("convention.scope.nullName"), null); //$NON-NLS-1$
		}
		int length;
		if ((length = name.length()) == 0) {
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Util.bind("convention.scope.emptyName"), null); //$NON-NLS-1$
		}
		if (name.charAt(0) == fgDot || name.charAt(length-1) == fgDot) {
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Util.bind("convention.scope.dotName"), null); //$NON-NLS-1$
		}
		if (CharOperation.isWhitespace(name.charAt(0)) || CharOperation.isWhitespace(name.charAt(name.length() - 1))) {
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Util.bind("convention.scope.nameWithBlanks"), null); //$NON-NLS-1$
		}
//		int dot = 0;
//		while (dot != -1 && dot < length-1) {
//			if ((dot = name.indexOf(fgDot, dot+1)) != -1 && dot < length-1 && name.charAt(dot+1) == fgDot) {
//				return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Util.bind("convention.package.consecutiveDotsName"), null); //$NON-NLS-1$
//				}
//		}
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		StringTokenizer st = new StringTokenizer(name, scopeResolutionOperator);
		boolean firstToken = true;
		while (st.hasMoreTokens()) {
			String typeName = st.nextToken();
			typeName = typeName.trim(); // grammar allows spaces
			char[] scannedID = typeName.toCharArray(); 
			if (scannedID == null) {
				return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Util.bind("convention.illegalIdentifier", typeName), null); //$NON-NLS-1$
			}
			IStatus status = workspace.validateName(new String(scannedID), IResource.FOLDER);
			if (!status.isOK()) {
				return status;
			}
			if (firstToken && scannedID.length > 0 && Character.isLowerCase(scannedID[0])) {
				return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, -1, Util.bind("convention.scope.lowercaseName"), null); //$NON-NLS-1$
			}
			firstToken = false;
		}
		return CModelStatus.VERIFIED_OK;
	}
}
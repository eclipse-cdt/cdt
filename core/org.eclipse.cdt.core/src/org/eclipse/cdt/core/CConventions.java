/*******************************************************************************
 *  Copyright (c) 2001, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Rational Software - initial implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.util.StringTokenizer;

import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.dom.parser.AbstractCLikeLanguage;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.Messages;
import org.eclipse.cdt.internal.core.model.CModelStatus;
import org.eclipse.cdt.internal.core.parser.scanner.ILexerLog;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CConventions {
	private final static String scopeResolutionOperator= "::"; //$NON-NLS-1$
	private final static char fgDot= '.';

	private final static String ILLEGAL_FILE_CHARS = "/\\:<>?*|\""; //$NON-NLS-1$
	
	public static boolean isLegalIdentifier(String name) {
		if (name == null) {
			return false;
		}

		if (name.indexOf(' ') != -1) {
			return false;
		}

		int length = name.length();
		char c;

		if (length == 0) {
			return false;
		}

		c = name.charAt(0);
		if ((!Character.isLetter(c)) && (c != '_')) {
			return false;
		}

		for (int i = 1; i < length; ++i) {
			c = name.charAt(i);
			if ((!Character.isLetterOrDigit(c)) && (c != '_')) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Validate the given CPP class name, either simple or qualified. For
	 * example, <code>"A::B::C"</code>, or <code>"C"</code>.
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
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Messages.convention_class_nullName, null); 
		}
		String trimmed = name.trim();
		if ((!name.equals(trimmed)) || (name.indexOf(" ") != -1) ){ //$NON-NLS-1$
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Messages.convention_class_nameWithBlanks, null); 
		}
		int index = name.lastIndexOf(scopeResolutionOperator);
		char[] scannedID;
		if (index == -1) {
			// simple name
			IStatus status = validateIdentifier(name, GPPLanguage.getDefault());
			if (!status.isOK()){
				return status;
			}

			scannedID = name.toCharArray();
		} else {
			// qualified name
			String pkg = name.substring(0, index).trim();
			IStatus status = validateScopeName(pkg);
			if (!status.isOK()) {
				return status;
			}
			String type = name.substring(index + scopeResolutionOperator.length()).trim();
			status = validateIdentifier(type, GPPLanguage.getDefault());
			if (!status.isOK()){
				return status;
			}
			scannedID = type.toCharArray();
		}

		if (scannedID != null) {
			if (CharOperation.contains('$', scannedID)) {
				return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, -1, Messages.convention_class_dollarName, null); 
			}
			if (scannedID.length > 0 && scannedID[0] == '_') {
				return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, -1, Messages.convention_class_leadingUnderscore, null); 
			}
			if (scannedID.length > 0 && Character.isLowerCase(scannedID[0])) {
				return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, -1, Messages.convention_class_lowercaseName, null); 
			}
			return CModelStatus.VERIFIED_OK;
		}
		return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, NLS.bind(Messages.convention_class_invalidName, name), null); 
	}

	/**
	 * Validate the given CPP namespace name, either simple or qualified. For
	 * example, <code>"A::B::C"</code>, or <code>"C"</code>.
	 * <p>
	 *
	 * @param name the name of a namespace
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a CPP class name,
	 *      a status with code <code>IStatus.WARNING</code>
	 *		indicating why the given name is discouraged,
	 *      otherwise a status object indicating what is wrong with
	 *      the name
	 */
	public static IStatus validateNamespaceName(String name) {
		if (name == null) {
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Messages.convention_namespace_nullName, null); 
		}
		String trimmed = name.trim();
		if ((!name.equals(trimmed)) || (name.indexOf(" ") != -1) ){ //$NON-NLS-1$
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Messages.convention_namespace_nameWithBlanks, null); 
		}
		int index = name.lastIndexOf(scopeResolutionOperator);
		char[] scannedID;
		if (index == -1) {
			// simple name
			IStatus status = validateIdentifier(name, GPPLanguage.getDefault());
			if (!status.isOK()){
				return status;
			}

			scannedID = name.toCharArray();
		} else {
			// qualified name
			String pkg = name.substring(0, index).trim();
			IStatus status = validateScopeName(pkg);
			if (!status.isOK()) {
				return status;
			}
			String type = name.substring(index + scopeResolutionOperator.length()).trim();
			status = validateIdentifier(type, GPPLanguage.getDefault());
			if (!status.isOK()){
				return status;
			}
			scannedID = type.toCharArray();
		}

		if (scannedID != null) {
			if (CharOperation.contains('$', scannedID)) {
				return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, -1, Messages.convention_namespace_dollarName, null); 
			}
			if (scannedID.length > 0 && scannedID[0] == '_') {
				return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, -1, Messages.convention_namespace_leadingUnderscore, null); 
			}
//			if (scannedID.length > 0 && Character.isLowerCase(scannedID[0])) {
//				return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, -1, Messages.convention.namespace.lowercaseName"), null); //$NON-NLS-1$
//			}
			return CModelStatus.VERIFIED_OK;
		}
		return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, NLS.bind(Messages.convention_class_invalidName, name), null); 
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
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Messages.convention_scope_nullName, null); 
		}
		int length;
		if ((length = name.length()) == 0) {
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Messages.convention_scope_emptyName, null); 
		}
		if (name.charAt(0) == fgDot || name.charAt(length-1) == fgDot) {
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Messages.convention_scope_dotName, null); 
		}
		if (CharOperation.isWhitespace(name.charAt(0)) || CharOperation.isWhitespace(name.charAt(name.length() - 1))) {
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Messages.convention_scope_nameWithBlanks, null); 
		}

		StringTokenizer st = new StringTokenizer(name, scopeResolutionOperator);
		boolean firstToken = true;
		while (st.hasMoreTokens()) {
			String typeName = st.nextToken();
			typeName = typeName.trim(); // grammar allows spaces
			char[] scannedID = typeName.toCharArray();
			if (scannedID == null) {
				return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, NLS.bind(Messages.convention_illegalIdentifier, typeName), null); 
			}
			if (firstToken && scannedID.length > 0 && scannedID[0] == '_') {
				return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, -1, Messages.convention_scope_leadingUnderscore, null); 
			}
			if (firstToken && scannedID.length > 0 && Character.isLowerCase(scannedID[0])) {
				return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, -1, Messages.convention_scope_lowercaseName, null); 
			}
			firstToken = false;
		}
		return CModelStatus.VERIFIED_OK;
	}

	/**
	 * Validate the given field name.
	 * <p>
	 * Syntax of a field name corresponds to VariableDeclaratorId (JLS2 8.3).
	 * For example, <code>"x"</code>.
	 *
	 * @param name the name of a field
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a field name, otherwise a status
	 *		object indicating what is wrong with the name
	 */
	public static IStatus validateFieldName(String name) {
		return validateIdentifier(name, GPPLanguage.getDefault());
	}

	/**
	 * Validate the given identifier.
	 * A valid identifier can act as a simple type name, method name or field name.
	 *
	 * @param id the C identifier
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given identifier is a valid C identifier, otherwise a status
	 *		object indicating what is wrong with the identifier
	 * @deprecated Notice that the identifier is not being checked against language keywords.
	 *      Use validateIdentifier(String id, AbstractCLikeLanguage language) instead.
	 */
	@Deprecated
	public static IStatus validateIdentifier(String id) {
		if (!isLegalIdentifier(id)) {
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, NLS.bind(Messages.convention_illegalIdentifier, id), null); 
		}

		if (!isValidIdentifier(id)) {
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, NLS.bind(Messages.convention_invalid, id), null); 
		}

		return CModelStatus.VERIFIED_OK;
	}

	/**
	 * Validate the given C or C++ identifier.
	 * The identifier must not have the same spelling as a C or C++ keyword.
	 * A valid identifier can act as a simple type name, method name or field name.
	 *
	 * @param id the C identifier
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given identifier is a valid C identifier, otherwise a status
	 *		object indicating what is wrong with the identifier
	 * @since 5.3
	 */
	public static IStatus validateIdentifier(String id, AbstractCLikeLanguage language) {
		if (!isLegalIdentifier(id)) {
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, NLS.bind(Messages.convention_illegalIdentifier, id), null); 
		}

		if (!isValidIdentifier(id)) {
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, NLS.bind(Messages.convention_invalid, id), null); 
		}

		if (isReservedKeyword(id, language) || isBuiltinType(id, language)) {
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, NLS.bind(Messages.convention_reservedKeyword, id), null);
		}

		return CModelStatus.VERIFIED_OK;
	}

	/**
	 * Validate the given method name.
	 * The special names "&lt;init&gt;" and "&lt;clinit&gt;" are not valid.
	 * <p>
	 * The syntax for a method  name is defined by Identifier
	 * of MethodDeclarator (JLS2 8.4). For example "println".
	 *
	 * @param name the name of a method
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a method name, otherwise a status
	 *		object indicating what is wrong with the name
	 */
	public static IStatus validateMethodName(String name) {
		if (name.startsWith("~")) { //$NON-NLS-1$
			return validateIdentifier(name.substring(1), GPPLanguage.getDefault());
		}
		return validateIdentifier(name, GPPLanguage.getDefault());
	}

	/**
	 * Validate the given include name.
	 * <p>
	 * The name of an include without the surrounding double quotes or brackets
	 * For example, <code>stdio.h</code> or <code>iostream</code>.
	 *
	 * @param name the include declaration
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as an include name, otherwise a status 
	 *		object indicating what is wrong with the name
	 */

	public static IStatus validateIncludeName(IProject project, String name) {
		String[] segments = new Path(name).segments();
		for (int i = 0; i < segments.length; ++i) {
			IStatus status;
			if (i == (segments.length - 1)) {
				status = validateHeaderFileName(project, segments[i]);
			} else {
				status = validateFileName(segments[i]);
			}
			if (!status.isOK()) {
				return status;
			}
		}
		return CModelStatus.VERIFIED_OK;
	}

	public static boolean isValidIdentifier(String name){
		// Create a scanner and get the type of the token
		// assuming that you are given a valid identifier
		IToken token = null;
		Lexer lexer= new Lexer(name.toCharArray(), new Lexer.LexerOptions(), ILexerLog.NULL, null);
		try {
			token = lexer.nextToken();
			if (token.getType() == IToken.tIDENTIFIER && lexer.nextToken().getType() == IToken.tEND_OF_INPUT) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}
	
	private static boolean isReservedKeyword(String name, AbstractCLikeLanguage language) {
		String[] keywords = language.getKeywords();
		for (String kw : keywords) {
			if (kw.equals(name))
				return true;
		}
		return false;
	}

	private static boolean isBuiltinType(String name, AbstractCLikeLanguage language) {
		String[] types = language.getBuiltinTypes();
		for (String type : types) {
			if (type.equals(name))
				return true;
		}
		return false;
	}

	private static boolean isLegalFilename(String name) {
		if (name == null || name.isEmpty()) {
			return false;
		}

		//TODO we need platform-independent validation, see bug#24152
		
		int len = name.length();
//		if (Character.isWhitespace(name.charAt(0)) || Character.isWhitespace(name.charAt(len - 1))) {
//			return false;
//		}
		for (int i = 0; i < len; i++) {
			char c = name.charAt(i);
			if (ILLEGAL_FILE_CHARS.indexOf(c) != -1) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Validate the given file name.
	 * The name must be the short file name (including the extension).
	 * It should not contain any prefix or path delimiters.
	 *
	 * @param name the file name
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a C/C++ file name,
	 *      a status with code <code>IStatus.WARNING</code>
	 *		indicating why the given name is discouraged,
	 *      otherwise a status object indicating what is wrong with
	 *      the name
	 */
	public static IStatus validateFileName(String name) {
		//TODO could use a preferences option for file naming conventions
		if (name == null || name.length() == 0) {
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Messages.convention_filename_nullName, null); 
		}
		if (!isLegalFilename(name)) {
			//TODO we need platform-independent validation, see bug#24152
			//return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Messages.convention.filename.invalid"), null); //$NON-NLS-1$
			return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, -1, Messages.convention_filename_possiblyInvalid, null); 
		}

		String trimmed = name.trim();
		if ((!name.equals(trimmed)) || (name.indexOf(" ") != -1)) { //$NON-NLS-1$
			return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, -1, Messages.convention_filename_nameWithBlanks, null); 
		}
		
		return CModelStatus.VERIFIED_OK;
	}
	
	/**
	 * Validate the given header file name.
	 * The name must be the short file name (including the extension).
	 * It should not contain any prefix or path delimiters.
	 *
	 * @param name the header file name
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a C/C++ header file name,
	 *      a status with code <code>IStatus.WARNING</code>
	 *		indicating why the given name is discouraged,
	 *      otherwise a status object indicating what is wrong with
	 *      the name
	 */
	public static IStatus validateHeaderFileName(IProject project, String name) {
		//TODO could use a preferences option for header file naming conventions
	    IStatus val = validateFileName(name);
	    if (val.getSeverity() == IStatus.ERROR) {
	        return val;
	    }

	    if (!CoreModel.isValidHeaderUnitName(project, name)) {
			return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, -1, Messages.convention_headerFilename_filetype, null); 
	    }

		return val;
	}
	
	/**
	 * Validate the given source file name.
	 * The name must be the short file name (including the extension).
	 * It should not contain any prefix or path delimiters.
	 *
	 * @param name the source file name
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a C/C++ source file name,
	 *      a status with code <code>IStatus.WARNING</code>
	 *		indicating why the given name is discouraged,
	 *      otherwise a status object indicating what is wrong with
	 *      the name
	 */
	public static IStatus validateSourceFileName(IProject project, String name) {
		//TODO could use a preferences option for source file naming conventions
	    IStatus val = validateFileName(name);
	    if (val.getSeverity() == IStatus.ERROR) {
	        return val;
	    }

	    if (!CoreModel.isValidSourceUnitName(project, name)) {
			return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, -1, Messages.convention_sourceFilename_filetype, null); 
	    }

		return val;
	}

	/**
	 * Validate the given C++ enum name, either simple or qualified. For
	 * example, <code>"A::B::C"</code>, or <code>"C"</code>.
	 * <p>
	 *
	 * @param name the name of a enum
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a CPP enum name,
	 *      a status with code <code>IStatus.WARNING</code>
	 *		indicating why the given name is discouraged,
	 *      otherwise a status object indicating what is wrong with
	 *      the name
	 * @since 4.0
	 */
	public static IStatus validateEnumName(String name) {
		if (name == null) {
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Messages.convention_enum_nullName, null); 
		}
		String trimmed = name.trim();
		if ((!name.equals(trimmed)) || (name.indexOf(" ") != -1) ){ //$NON-NLS-1$
			return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Messages.convention_enum_nameWithBlanks, null); 
		}
		int index = name.lastIndexOf(scopeResolutionOperator);
		char[] scannedID;
		if (index == -1) {
			// simple name
			IStatus status = validateIdentifier(name, GPPLanguage.getDefault());
			if (!status.isOK()){
				return status;
			}
	
			scannedID = name.toCharArray();
		} else {
			// qualified name
			String pkg = name.substring(0, index).trim();
			IStatus status = validateScopeName(pkg);
			if (!status.isOK()) {
				return status;
			}
			String type = name.substring(index + scopeResolutionOperator.length()).trim();
			status = validateIdentifier(type, GPPLanguage.getDefault());
			if (!status.isOK()){
				return status;
			}
			scannedID = type.toCharArray();
		}
	
		if (scannedID != null) {
			if (CharOperation.contains('$', scannedID)) {
				return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, -1, Messages.convention_enum_dollarName, null); 
			}
			if (scannedID.length > 0 && scannedID[0] == '_') {
				return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, -1, Messages.convention_enum_leadingUnderscore, null); 
			}
			if (scannedID.length > 0 && Character.isLowerCase(scannedID[0])) {
				return new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, -1, Messages.convention_enum_lowercaseName, null); 
			}
			return CModelStatus.VERIFIED_OK;
		}
		return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Messages.convention_enum_invalidName, null);  
	}
}

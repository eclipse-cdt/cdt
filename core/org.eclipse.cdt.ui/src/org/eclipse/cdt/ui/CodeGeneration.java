/*******************************************************************************
 *  Copyright (c) 2001, 2011 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     Rational Software - initial implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.templates.Template;

/**
 * Class that offers access to the templates contained in the 'Code Templates' preference page.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 2.1
 */
public class CodeGeneration {

	private CodeGeneration() {
	}

	/**
	 * Returns the content for a new header file using the default 'header file' code template.
	 * @param tu The translation unit to create the source for. The translation unit does not need
	 * 		to exist.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the new content or <code>null</code> if the template is undefined or empty.
	 * @throws CoreException
	 * @since 5.3
	 */
	public static String getHeaderFileContent(Template template, ITranslationUnit tu, String lineDelimiter)
			throws CoreException {
		return getHeaderFileContent(template, tu, null, null, null, null, null, null, null, lineDelimiter);
	}

	/**
	 * Returns the content for a new header file using the default 'header file' code template.
	 * @param tu The translation unit to create the source for. The translation unit does not need
	 * 		to exist.
	 * @param typeComment The comment for the type to created. Used when the code template contains
	 * 		a ${typecomment} variable. Can be <code>null</code> if no comment should be added.
	 * @param typeContent The code of the type, including type declaration and body.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the new content or <code>null</code> if the template is undefined or empty.
	 * @throws CoreException
	 */
	public static String getHeaderFileContent(ITranslationUnit tu, String typeComment, String typeContent,
			String lineDelimiter) throws CoreException {
		return getHeaderFileContent(tu, null, null, null, null, typeComment, typeContent, null, lineDelimiter);
	}

	/**
	 * Returns the content for a new header file using the default 'header file' code template.
	 * @param template  The file template to use or <code>null</code> for the default template
	 * @param tu The translation unit to create the source for. The translation unit does not need
	 * 		to exist.
	 * @param typeComment The comment for the type to created. Used when the code template contains
	 * 		a ${typecomment} variable. Can be <code>null</code> if no comment should be added.
	 * @param typeContent The code of the type, including type declaration and body.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the new content or <code>null</code> if the template is undefined or empty.
	 * @throws CoreException
	 */
	public static String getHeaderFileContent(Template template, ITranslationUnit tu, String typeComment,
			String typeContent, String lineDelimiter) throws CoreException {
		return getHeaderFileContent(template, tu, null, null, null, null, typeComment, typeContent, null,
				lineDelimiter);
	}

	/**
	 * Returns the content for a new header file using the default 'header file' code template.
	 * @param tu The translation unit to create the source for. The translation unit does not need
	 * 		to exist.
	 * @param includes Include statements, or {@code null} if there are no includes.
	 * @param namespaceBegin Beginning of namespace declarations, or {@code null} if there is no
	 * 		namespace.
	 * @param namespaceEnd End of namespace declarations, or {@code null} if there is no
	 * 		namespace.
	 * @param namespaceName Fully qualified namespace name, or {@code null} if there is no
	 * 		namespace.
	 * @param typeComment The comment for the type to created. Used when the code template contains
	 * 		a ${typecomment} variable. Can be <code>null</code> if no comment should be added.
	 * @param typeContent The code of the type, including type declaration and body.
	 * @param typeName The name of the type.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the new content or <code>null</code> if the template is undefined or empty.
	 * @throws CoreException
	 * @since 5.3
	 */
	public static String getHeaderFileContent(ITranslationUnit tu, String includes, String namespaceBegin,
			String namespaceEnd, String namespaceName, String typeComment, String typeContent, String typeName,
			String lineDelimiter) throws CoreException {
		return StubUtility.getHeaderFileContent(tu, typeContent, getFileComment(tu, lineDelimiter), includes,
				namespaceBegin, namespaceEnd, namespaceName, typeComment, typeName, lineDelimiter);
	}

	/**
	 * Returns the content for a new header file using the default 'header file' code template.
	 * @param template  The file template to use or <code>null</code> for the default template
	 * @param tu The translation unit to create the source for. The translation unit does not need
	 * 		to exist.
	 * @param includes Include statements, or {@code null} if there are no includes.
	 * @param namespaceBegin Beginning of namespace declarations, or {@code null} if there is no
	 * 		namespace.
	 * @param namespaceEnd End of namespace declarations, or {@code null} if there is no
	 * 		namespace.
	 * @param namespaceName Fully qualified namespace name, or {@code null} if there is no
	 * 		namespace.
	 * @param typeComment The comment for the type to created. Used when the code template contains
	 * 		a ${typecomment} variable. Can be <code>null</code> if no comment should be added.
	 * @param typeContent The code of the type, including type declaration and body.
	 * @param typeName The name of the type.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the new content or <code>null</code> if the template is undefined or empty.
	 * @throws CoreException
	 * @since 5.3
	 */
	public static String getHeaderFileContent(Template template, ITranslationUnit tu, String includes,
			String namespaceBegin, String namespaceEnd, String namespaceName, String typeComment, String typeContent,
			String typeName, String lineDelimiter) throws CoreException {
		return StubUtility.getHeaderFileContent(template, tu, typeContent, getFileComment(tu, lineDelimiter), includes,
				namespaceBegin, namespaceEnd, namespaceName, typeComment, typeName, lineDelimiter);
	}

	/**
	 * Returns the content for a new translation unit using the 'source file' code template.
	 * @param tu The translation unit to create the source for. The translation unit does not need
	 * 		to exist.
	 * @param typeComment The comment for the type to created. Used when the code template contains
	 * 		a ${typecomment} variable. Can be <code>null</code> if no comment should be added.
	 * @param typeContent The code of the type, including type declaration and body.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the new content or <code>null</code> if the template is undefined or empty.
	 * @throws CoreException
	 */
	public static String getBodyFileContent(ITranslationUnit tu, String typeComment, String typeContent,
			String lineDelimiter) throws CoreException {
		return getBodyFileContent(tu, typeContent, null, null, null, null, typeComment, null, lineDelimiter);
	}

	/**
	 * Returns the content for a new translation unit using the 'source file' code template.
	 * @param template  The file template to use or <code>null</code> for the default template
	 * @param tu The translation unit to create the source for. The translation unit does not need
	 * 		to exist.
	 * @param typeComment The comment for the type to created. Used when the code template contains
	 * 		a ${typecomment} variable. Can be <code>null</code> if no comment should be added.
	 * @param typeContent The code of the type, including type declaration and body.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the new content or <code>null</code> if the template is undefined or empty.
	 * @throws CoreException
	 */
	public static String getBodyFileContent(Template template, ITranslationUnit tu, String typeComment,
			String typeContent, String lineDelimiter) throws CoreException {
		return getBodyFileContent(template, tu, typeContent, null, null, null, null, typeComment, null, lineDelimiter);
	}

	/**
	 * Returns the content for a new source file using the default 'source file' code template.
	 * @param tu The translation unit to create the source for. The translation unit does not need
	 * 		to exist.
	 * @param includes Include statements, or {@code null} if there are no includes.
	 * @param namespaceBegin Beginning of namespace declarations, or {@code null} if there is no
	 * 		namespace.
	 * @param namespaceEnd End of namespace declarations, or {@code null} if there is no
	 * 		namespace.
	 * @param namespaceName Fully qualified namespace name, or {@code null} if there is no
	 * 		namespace.
	 * @param typeComment The comment for the type to created. Used when the code template contains
	 * 		a ${typecomment} variable. Can be <code>null</code> if no comment should be added.
	 * @param typeContent The code of the type, including type declaration and body.
	 * @param typeName The name of the type.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the new content or <code>null</code> if the template is undefined or empty.
	 * @throws CoreException
	 * @since 5.3
	 */
	public static String getBodyFileContent(ITranslationUnit tu, String includes, String namespaceBegin,
			String namespaceEnd, String namespaceName, String typeComment, String typeContent, String typeName,
			String lineDelimiter) throws CoreException {
		return StubUtility.getBodyFileContent(tu, typeContent, getFileComment(tu, lineDelimiter), includes,
				namespaceBegin, namespaceEnd, namespaceName, typeComment, typeName, lineDelimiter);
	}

	/**
	 * Returns the content for a new source file using the default 'source file' code template.
	 * @param template  The file template to use or <code>null</code> for the default template
	 * @param tu The translation unit to create the source for. The translation unit does not need
	 * 		to exist.
	 * @param includes Include statements, or {@code null} if there are no includes.
	 * @param namespaceBegin Beginning of namespace declarations, or {@code null} if there is no
	 * 		namespace.
	 * @param namespaceEnd End of namespace declarations, or {@code null} if there is no
	 * 		namespace.
	 * @param namespaceName Fully qualified namespace name, or {@code null} if there is no
	 * 		namespace.
	 * @param typeComment The comment for the type to created. Used when the code template contains
	 * 		a ${typecomment} variable. Can be <code>null</code> if no comment should be added.
	 * @param typeContent The code of the type, including type declaration and body.
	 * @param typeName The name of the type.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the new content or <code>null</code> if the template is undefined or empty.
	 * @throws CoreException
	 * @since 5.3
	 */
	public static String getBodyFileContent(Template template, ITranslationUnit tu, String includes,
			String namespaceBegin, String namespaceEnd, String namespaceName, String typeComment, String typeContent,
			String typeName, String lineDelimiter) throws CoreException {
		return StubUtility.getBodyFileContent(template, tu, typeContent, getFileComment(tu, lineDelimiter), includes,
				namespaceBegin, namespaceEnd, namespaceName, typeComment, typeName, lineDelimiter);
	}

	/**
	 * Returns the content for a new translation unit using the 'test file' code template.
	 * @param tu The translation unit to create the source for. The translation unit does not need
	 * 		to exist.
	 * @param includes Include statements, or {@code null} if there are no includes.
	 * @param namespaceBegin Beginning of namespace declarations, or {@code null} if there is no
	 * 		namespace.
	 * @param namespaceEnd End of namespace declarations, or {@code null} if there is no
	 * 		namespace.
	 * @param namespaceName Fully qualified namespace name, or {@code null} if there is no
	 * 		namespace.
	 * @param testCases The test cases code.
	 * @param typeName The name of the type.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the new content or <code>null</code> if the template is undefined or empty.
	 * @throws CoreException
	 * @since 5.3
	 */
	public static String getTestFileContent(ITranslationUnit tu, String includes, String namespaceBegin,
			String namespaceEnd, String namespaceName, String testCases, String typeName, String lineDelimiter)
			throws CoreException {
		return StubUtility.getTestFileContent(tu, testCases, getFileComment(tu, lineDelimiter), includes,
				namespaceBegin, namespaceEnd, namespaceName, typeName, lineDelimiter);
	}

	/**
	 * Returns the content of the body for a method using the method body template.
	 * <code>null</code> is returned if the template is empty.
	 * <p>The returned string is unformatted and not indented.
	 *
	 * @param tu The translation unit to which the method belongs. The translation unit does not need to exist.
	 * @param typeName Name of the type to which the method belongs.
	 * @param methodName Name of the method.
	 * @param bodyStatement The code to be entered at the place of the variable ${body_statement}.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the constructed body content or <code>null</code> if
	 * the comment code template is empty. The returned string is unformatted and and has no indent (formatting required).
	 * @throws CoreException Thrown when the evaluation of the code template fails.
	 */
	public static String getMethodBodyContent(ITranslationUnit tu, String typeName, String methodName,
			String bodyStatement, String lineDelimiter) throws CoreException {
		return StubUtility.getMethodBodyContent(tu.getCProject(), typeName, methodName, bodyStatement, lineDelimiter);
	}

	/**
	 * Returns the content of the body for a constructor using the constructor body template.
	 * <code>null</code> is returned if the template is empty.
	 * <p>The returned string is unformatted and not indented.
	 *
	 * @param tu The translation unit to which the method belongs. The translation unit
	 * 		does not need to exist.
	 * @param typeName Name of the type to which the constructor belongs.
	 * @param bodyStatement The code to be entered at the place of the variable ${body_statement}.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the constructed body content or <code>null</code> if the comment code
	 * 		template is empty. The returned string is unformatted and and has no indent
	 * 		(formatting required).
	 * @throws CoreException Thrown when the evaluation of the code template fails.
	 */
	public static String getConstructorBodyContent(ITranslationUnit tu, String typeName, String bodyStatement,
			String lineDelimiter) throws CoreException {
		return StubUtility.getConstructorBodyContent(tu.getCProject(), typeName, bodyStatement, lineDelimiter);
	}

	/**
	 * Returns the content of the body for a destructor using the destructor body template.
	 * <code>null</code> is returned if the template is empty.
	 * <p>The returned string is unformatted and not indented.
	 *
	 * @param tu The translation unit to which the method belongs. The translation unit
	 * 		does not need to exist.
	 * @param typeName Name of the type to which the constructor belongs.
	 * @param bodyStatement The code to be entered at the place of the variable ${body_statement}.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the constructed body content or <code>null</code> if the comment code
	 * 		template is empty. The returned string is unformatted and and has no indent
	 * 		(formatting required).
	 * @throws CoreException Thrown when the evaluation of the code template fails.
	 */
	public static String getDestructorBodyContent(ITranslationUnit tu, String typeName, String bodyStatement,
			String lineDelimiter) throws CoreException {
		return StubUtility.getDestructorBodyContent(tu.getCProject(), typeName, bodyStatement, lineDelimiter);
	}

	/**
	 * Returns the content of the class definition body using the class body template.
	 * <code>null</code> is returned if the template is empty.
	 * <p>The returned string is unformatted and not indented.
	 *
	 * @param tu The translation unit to which the method belongs. The translation unit
	 * 		does not need to exist.
	 * @param className The name of the class.
	 * @param classMemberDeclarations The code to be entered at the place of the variable
	 * 		${declarations}.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the constructed class body content or <code>null</code> if
	 * 		the class body code template is empty. The returned string is unformatted and
	 * 		has no indent (formatting required).
	 * @throws CoreException Thrown when the evaluation of the code template fails.
	 * @since 5.3
	 */
	public static String getClassBodyContent(ITranslationUnit tu, String className, String classMemberDeclarations,
			String lineDelimiter) throws CoreException {
		return StubUtility.getClassBodyContent(tu.getCProject(), className, classMemberDeclarations, lineDelimiter);
	}

	/**
	 * Returns the content of the beginning of a namespace declaration using the corresponding
	 * template. <code>null</code> is returned if the template is empty.
	 * <p>The returned string is unformatted and not indented.
	 *
	 * @param tu The translation unit to which the method belongs. The translation unit
	 * 		does not need to exist.
	 * @param namespaceName The name of the namespace.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the constructed beginning of a namespace declaration,or <code>null</code> if
	 * 		the namespace code template is empty. The returned string is unformatted and has no
	 * 		indent (formatting required).
	 * @throws CoreException Thrown when the evaluation of the code template fails.
	 * @since 5.3
	 */
	public static String getNamespaceBeginContent(ITranslationUnit tu, String namespaceName, String lineDelimiter)
			throws CoreException {
		return StubUtility.getNamespaceBeginContent(tu.getCProject(), namespaceName, lineDelimiter);
	}

	/**
	 * Returns the content of the end of a namespace declaration using the corresponding
	 * template. <code>null</code> is returned if the template is empty.
	 * <p>The returned string is unformatted and not indented.
	 *
	 * @param tu The translation unit to which the method belongs. The translation unit
	 * 		does not need to exist.
	 * @param namespaceName The name of the namespace.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the constructed end of a namespace declaration,or <code>null</code> if
	 * 		the namespace code template is empty. The returned string is unformatted and has no
	 * 		indent (formatting required).
	 * @throws CoreException Thrown when the evaluation of the code template fails.
	 * @since 5.3
	 */
	public static String getNamespaceEndContent(ITranslationUnit tu, String namespaceName, String lineDelimiter)
			throws CoreException {
		return StubUtility.getNamespaceEndContent(tu.getCProject(), namespaceName, lineDelimiter);
	}

	/**
	 * Returns the content for a new file comment using the 'file comment' code template.
	 * The returned content is unformatted and is not indented.
	 * @param tu The translation unit to add the comment to. The translation unit does not need
	 * 		to exist.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the new content or <code>null</code> if the code template is undefined
	 * 		or empty. The returned content is unformatted and is not indented.
	 * @throws CoreException Thrown when the evaluation of the code template fails.
	 * @since 5.0
	 */
	public static String getFileComment(ITranslationUnit tu, String lineDelimiter) throws CoreException {
		return StubUtility.getFileComment(tu, lineDelimiter);
	}

	/**
	 * Returns the content for a new type comment using the 'typecomment' code template.
	 * The returned content is unformatted and is not indented.
	 * @param tu The translation unit where the type is contained. The translation unit
	 * 		does not need to exist.
	 * @param typeQualifiedName The name of the type to which the comment is added. For inner types
	 * 		the name must be qualified and include the outer types names (dot separated).
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the new content or <code>null</code> if the code template is undefined
	 * 		or empty. The returned content is unformatted and is not indented.
	 * @throws CoreException
	 */
	public static String getClassComment(ITranslationUnit tu, String typeQualifiedName, String lineDelimiter)
			throws CoreException {
		return StubUtility.getClassComment(tu, typeQualifiedName, lineDelimiter);
	}

	/**
	 * Returns the comment for a method using the method comment code template.
	 * <code>null</code> is returned if the template is empty.
	 * <p>The returned string is unformatted and not indented.
	 *
	 * @param tu The translation unit to which the method belongs. The translation unit
	 * 		does not need to exist.
	 * @param declaringTypeName Name of the type to which the method belongs.
	 * @param methodName Name of the method.
	 * @param paramNames Names of the parameters for the method.
	 * @param excTypeSig Thrown exceptions.
	 * @param retTypeSig Return type.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the constructed comment or <code>null</code> if the comment code template
	 * 		is empty. The returned content is unformatted and not indented (formatting required).
	 * @throws CoreException Thrown when the evaluation of the code template fails.
	 */
	public static String getMethodComment(ITranslationUnit tu, String declaringTypeName, String methodName,
			String[] paramNames, String[] excTypeSig, String retTypeSig, String lineDelimiter) throws CoreException {
		return StubUtility.getMethodComment(tu, declaringTypeName, methodName, paramNames, excTypeSig, retTypeSig,
				lineDelimiter);
	}

	/**
	 * Returns the comment for a constructor using the constructor comment code template.
	 * <code>null</code> is returned if the template is empty.
	 * <p>The returned string is unformatted and not indented.
	 *
	 * @param tu The translation unit to which the method belongs. The translation unit
	 * 		does not need to exist.
	 * @param declaringTypeName Name of the type to which the method belongs.
	 * @param paramNames Names of the parameters for the method.
	 * @param excTypeSig Thrown exceptions.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the constructed comment or <code>null</code> if the comment code template
	 * 		is empty. The returned content is unformatted and not indented (formatting required).
	 * @throws CoreException Thrown when the evaluation of the code template fails.
	 */
	public static String getConstructorComment(ITranslationUnit tu, String declaringTypeName, String[] paramNames,
			String[] excTypeSig, String lineDelimiter) throws CoreException {
		return StubUtility.getConstructorComment(tu, declaringTypeName, paramNames, excTypeSig, lineDelimiter);
	}

	/**
	 * Returns the comment for a destructor using the destructor comment code template.
	 * <code>null</code> is returned if the template is empty.
	 * <p>The returned string is unformatted and not indented.
	 *
	 * @param tu The translation unit to which the method belongs. The translation unit
	 * 		does not need to exist.
	 * @param declaringTypeName Name of the type to which the method belongs.
	 * @param excTypeSig Thrown exceptions.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the constructed comment or <code>null</code> if the comment code template
	 * 		is empty. The returned content is unformatted and not indented (formatting required).
	 * @throws CoreException Thrown when the evaluation of the code template fails.
	 */
	public static String getDestructorComment(ITranslationUnit tu, String declaringTypeName, String[] excTypeSig,
			String lineDelimiter) throws CoreException {
		return StubUtility.getDestructorComment(tu, declaringTypeName, excTypeSig, lineDelimiter);
	}
}

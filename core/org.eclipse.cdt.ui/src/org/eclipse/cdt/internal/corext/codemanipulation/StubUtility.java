/*******************************************************************************
 * Copyright (c) 2001, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - initial implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Jens Elmenthaler (Verigy) - http://bugs.eclipse.org/235586
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.codemanipulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.swt.SWT;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;

import com.ibm.icu.text.BreakIterator;

import org.eclipse.cdt.core.CConventions;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.dom.parser.AbstractCLikeLanguage;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.utils.PathUtil;

import org.eclipse.cdt.internal.corext.template.c.CodeTemplateContext;
import org.eclipse.cdt.internal.corext.template.c.CodeTemplateContextType;
import org.eclipse.cdt.internal.corext.template.c.FileTemplateContext;
import org.eclipse.cdt.internal.corext.template.c.FileTemplateContextType;
import org.eclipse.cdt.internal.corext.util.Strings;

import org.eclipse.cdt.internal.ui.text.CBreakIterator;
import org.eclipse.cdt.internal.ui.util.NameComposer;
import org.eclipse.cdt.internal.ui.viewsupport.ProjectTemplateStore;

public class StubUtility {
	private static final String[] EMPTY= {};
	
	private StubUtility() {
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.eclipse.cdt.ui.CodeGeneration#getHeaderFileContent(ITranslationUnit, String, String, String)
	 */	
	public static String getHeaderFileContent(ITranslationUnit tu, String declarations,
			String fileComment, String includes, String namespaceBegin,	String namespaceEnd,
			String namespaceName, String typeComment, String typeName,
			String lineDelimiter) throws CoreException {
		return getHeaderFileContent(getDefaultFileTemplate(tu), tu, declarations, fileComment,
				includes, namespaceBegin, namespaceEnd, namespaceName, typeComment, typeName,
				lineDelimiter);
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.eclipse.cdt.ui.CodeGeneration#getHeaderFileContent(Template, ITranslationUnit, String, String, String)
	 */	
	public static String getHeaderFileContent(Template template, ITranslationUnit tu,
			String declarations, String fileComment, String includes, String namespaceBegin,
			String namespaceEnd, String namespaceName, String typeComment, String typeName,
			String lineDelimiter) throws CoreException {
		if (template == null) {
			return null;
		}
		ICProject project= tu.getCProject();
		CodeTemplateContext context= new CodeTemplateContext(template.getContextTypeId(), project, lineDelimiter);
		context.setTranslationUnitVariables(tu);
		String includeGuardSymbol= generateIncludeGuardSymbol(tu.getResource(), project);
		context.setVariable(CodeTemplateContextType.DECLARATIONS, declarations != null ? declarations : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.FILE_COMMENT, fileComment != null ? fileComment : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.INCLUDE_GUARD_SYMBOL, includeGuardSymbol != null ? includeGuardSymbol : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.INCLUDES, includes != null ? includes : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.NAMESPACE_BEGIN, namespaceBegin != null ? namespaceBegin : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.NAMESPACE_END, namespaceEnd != null ? namespaceEnd : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.NAMESPACE_NAME, namespaceName != null ? namespaceName : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.TYPE_COMMENT, typeComment != null ? typeComment : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.TYPENAME, typeName != null ? typeName : ""); //$NON-NLS-1$
		String[] fullLine= {
				CodeTemplateContextType.DECLARATIONS, CodeTemplateContextType.FILE_COMMENT,
				CodeTemplateContextType.INCLUDES,
				CodeTemplateContextType.NAMESPACE_BEGIN, CodeTemplateContextType.NAMESPACE_END,
				CodeTemplateContextType.TYPE_COMMENT
			};

		String text = evaluateTemplate(context, template, fullLine);
		if (text != null && !text.endsWith(lineDelimiter))
			text += lineDelimiter;
		return text;
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.eclipse.cdt.ui.CodeGeneration#getBodyFileContent(ITranslationUnit, String, String, String, String, String, String, String, String, String)
	 */	
	public static String getBodyFileContent(ITranslationUnit tu,
			String declarations, String fileComment, String includes, String namespaceBegin,
			String namespaceEnd, String namespaceName, String typeComment, String typeName,
			String lineDelimiter) throws CoreException {
		return getBodyFileContent(getDefaultFileTemplate(tu), tu, declarations, fileComment,
				includes, namespaceBegin, namespaceEnd, namespaceName, typeComment, typeName,
				lineDelimiter);
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.eclipse.cdt.ui.CodeGeneration#getBodyFileContent(Template, ITranslationUnit, String, String, String, String, String, String, String, String, String)
	 */	
	public static String getBodyFileContent(Template template, ITranslationUnit tu,
			String declarations, String fileComment, String includes, String namespaceBegin,
			String namespaceEnd, String namespaceName, String typeComment, String typeName,
			String lineDelimiter) throws CoreException {
		if (template == null) {
			return null;
		}
		ICProject project= tu.getCProject();
		CodeTemplateContext context= new CodeTemplateContext(template.getContextTypeId(), project, lineDelimiter);
		context.setTranslationUnitVariables(tu);
		context.setVariable(CodeTemplateContextType.DECLARATIONS, declarations != null ? declarations : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.FILE_COMMENT, fileComment != null ? fileComment : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.INCLUDES, includes != null ? includes : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.NAMESPACE_BEGIN, namespaceBegin != null ? namespaceBegin : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.NAMESPACE_END, namespaceEnd != null ? namespaceEnd : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.NAMESPACE_NAME, namespaceName != null ? namespaceName : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.TYPE_COMMENT, typeComment != null ? typeComment : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.TYPENAME, typeName != null ? typeName : ""); //$NON-NLS-1$
		String[] fullLine= {
				CodeTemplateContextType.DECLARATIONS, CodeTemplateContextType.FILE_COMMENT,
				CodeTemplateContextType.INCLUDES,
				CodeTemplateContextType.NAMESPACE_BEGIN, CodeTemplateContextType.NAMESPACE_END,
				CodeTemplateContextType.TYPE_COMMENT
			};
		String text = evaluateTemplate(context, template, fullLine);
		if (text != null && !text.endsWith(lineDelimiter))
			text += lineDelimiter;
		return text;
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.eclipse.cdt.ui.CodeGeneration#getTestFileContent(ITranslationUnit, String, String, String)
	 */	
	public static String getTestFileContent(ITranslationUnit tu, String declarations,
			String fileComment, String includes, String namespaceBegin, String namespaceEnd,
			String namespaceName, String typeName, String lineDelimiter)
					throws CoreException {
		return getBodyFileContent(getTestFileTemplate(tu), tu, declarations, fileComment,
				includes, namespaceBegin, namespaceEnd, namespaceName, null, typeName,
				lineDelimiter);
	}

	public static String getFileContent(Template template, IFile file, String lineDelimiter) throws CoreException {
		ICProject cproject = null;
		final IProject project = file.getProject();
		if (CoreModel.hasCNature(project)) {
			cproject = CoreModel.getDefault().create(project);
		}
		FileTemplateContext context;
		if (cproject != null) {
			context= new CodeTemplateContext(template.getContextTypeId(), cproject, lineDelimiter);
		} else {
			context= new FileTemplateContext(template.getContextTypeId(), lineDelimiter);
		}
		String fileComment= getFileComment(file, lineDelimiter);
		context.setVariable(CodeTemplateContextType.FILE_COMMENT, fileComment != null ? fileComment : ""); //$NON-NLS-1$
		String includeGuardSymbol= generateIncludeGuardSymbol(file, cproject);
		context.setVariable(CodeTemplateContextType.INCLUDE_GUARD_SYMBOL, includeGuardSymbol != null ? includeGuardSymbol : ""); //$NON-NLS-1$
		context.setResourceVariables(file);
		String[] fullLine= { CodeTemplateContextType.FILE_COMMENT };
		
		String text = evaluateTemplate(context, template, fullLine);
		if (!text.endsWith(lineDelimiter))
			text += lineDelimiter;
		return text;
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 */
	public static String getClassBodyContent(ICProject project, String className,
			String classMemberDeclarations, String lineDelimiter) throws CoreException {
		Template template= getCodeTemplate(CodeTemplateContextType.CLASS_BODY_ID, project);
		if (template == null) {
			return classMemberDeclarations;
		}
		CodeTemplateContext context= new CodeTemplateContext(template.getContextTypeId(), project, lineDelimiter);
		context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE, className);
		context.setVariable(CodeTemplateContextType.DECLARATIONS, classMemberDeclarations != null ? classMemberDeclarations : ""); //$NON-NLS-1$
		String str= evaluateTemplate(context, template,
				new String[] { CodeTemplateContextType.DECLARATIONS });
		if (str == null && classMemberDeclarations != null && !Strings.containsOnlyWhitespaces(classMemberDeclarations)) {
			return classMemberDeclarations;
		}
		return str;
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 */
	public static String getMethodBodyContent(ICProject project, String typeName, String methodName, String bodyStatement, String lineDelimiter) throws CoreException {
		String templateId= CodeTemplateContextType.METHODSTUB_ID;
		return getMethodBodyContent(templateId, project, typeName, methodName, bodyStatement, lineDelimiter);
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 */
	public static String getMethodBodyContent(String templateId, ICProject project, String typeName,
			String methodName, String bodyStatement, String lineDelimiter) throws CoreException {
		Template template= getCodeTemplate(templateId, project);
		if (template == null) {
			return bodyStatement;
		}
		CodeTemplateContext context= new CodeTemplateContext(template.getContextTypeId(), project, lineDelimiter);
		context.setVariable(CodeTemplateContextType.ENCLOSING_METHOD, methodName);
		context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE, typeName);
		context.setVariable(CodeTemplateContextType.BODY_STATEMENT, bodyStatement != null ? bodyStatement : ""); //$NON-NLS-1$
		String str= evaluateTemplate(context, template, new String[] { CodeTemplateContextType.BODY_STATEMENT });
		if (str == null && bodyStatement != null && !Strings.containsOnlyWhitespaces(bodyStatement)) {
			return bodyStatement;
		}
		return str;
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 */
	public static String getConstructorBodyContent(ICProject project, String typeName, String bodyStatement, String lineDelimiter) throws CoreException {
		String templateId= CodeTemplateContextType.CONSTRUCTORSTUB_ID;
		return getMethodBodyContent(templateId, project, typeName, typeName, bodyStatement, lineDelimiter);
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 */
	public static String getDestructorBodyContent(ICProject project, String typeName, String bodyStatement, String lineDelimiter) throws CoreException {
		String templateId= CodeTemplateContextType.DESTRUCTORSTUB_ID;
		return getMethodBodyContent(templateId, project, typeName, "~"+typeName, bodyStatement, lineDelimiter); //$NON-NLS-1$
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 */
	public static String getNamespaceBeginContent(ICProject project, String namespaceName,
			String lineDelimiter) throws CoreException {
		Template template= getCodeTemplate(CodeTemplateContextType.NAMESPACE_BEGIN_ID, project);
		if (template == null) {
			return null;
		}
		CodeTemplateContext context= new CodeTemplateContext(template.getContextTypeId(), project, lineDelimiter);
		context.setVariable(CodeTemplateContextType.NAMESPACE_NAME, namespaceName);
		return evaluateTemplate(context, template, EMPTY);
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 */
	public static String getNamespaceEndContent(ICProject project, String namespaceName,
			String lineDelimiter) throws CoreException {
		Template template= getCodeTemplate(CodeTemplateContextType.NAMESPACE_END_ID, project);
		if (template == null) {
			return null;
		}
		CodeTemplateContext context= new CodeTemplateContext(template.getContextTypeId(), project, lineDelimiter);
		context.setVariable(CodeTemplateContextType.NAMESPACE_NAME, namespaceName);
		return evaluateTemplate(context, template, EMPTY);
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.eclipse.cdt.ui.CodeGeneration#getFileComment(ITranslationUnit, String)
	 */	
	public static String getFileComment(ITranslationUnit tu, String lineDelimiter) throws CoreException {
		Template template= getCodeTemplate(CodeTemplateContextType.FILECOMMENT_ID, tu.getCProject());
		if (template == null) {
			return null;
		}
		
		ICProject project= tu.getCProject();
		CodeTemplateContext context= new CodeTemplateContext(template.getContextTypeId(), project, lineDelimiter);
		context.setTranslationUnitVariables(tu);
		return evaluateTemplate(context, template);
	}	

	private static String getFileComment(IFile file, String lineDelimiter) throws CoreException {
		Template template= getCodeTemplate(CodeTemplateContextType.FILECOMMENT_ID, file.getProject());
		if (template == null) {
			return null;
		}
		
		FileTemplateContext context= new FileTemplateContext(template.getContextTypeId(), lineDelimiter);
		context.setResourceVariables(file);
		return evaluateTemplate(context, template);
	}	

	/*
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.eclipse.cdt.ui.CodeGeneration#getClassComment(ITranslationUnit, String, String)
	 */	
	public static String getClassComment(ITranslationUnit tu, String typeQualifiedName, String lineDelimiter) throws CoreException {
		Template template= getCodeTemplate(CodeTemplateContextType.TYPECOMMENT_ID, tu.getCProject());
		if (template == null) {
			return null;
		}
		
		ICProject project= tu.getCProject();
		CodeTemplateContext context= new CodeTemplateContext(template.getContextTypeId(), project, lineDelimiter);
		context.setTranslationUnitVariables(tu);
		context.setVariable(CodeTemplateContextType.TYPENAME, typeQualifiedName);
		return evaluateTemplate(context, template);
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.eclipse.cdt.ui.CodeGeneration#getMethodComment(ITranslationUnit, String, String, String[], String[], String, String)
	 */
	public static String getMethodComment(ITranslationUnit tu, String typeName, String methodName, String[] paramNames, String[] excTypeSig, String retTypeSig, String lineDelimiter) throws CoreException {
		String templateId= CodeTemplateContextType.METHODCOMMENT_ID;
		return getMethodComment(templateId, tu, typeName, methodName, paramNames, excTypeSig, retTypeSig, lineDelimiter);
	}
	
	/*
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.eclipse.cdt.ui.CodeGeneration#getConstructorComment(ITranslationUnit, String, String[], String[], String)
	 */
	public static String getConstructorComment(ITranslationUnit tu, String typeName, String[] paramNames, String[] excTypeSig, String lineDelimiter) throws CoreException {
		String templateId= CodeTemplateContextType.CONSTRUCTORCOMMENT_ID;
		return getMethodComment(templateId, tu, typeName, typeName, paramNames, excTypeSig, null, lineDelimiter);
	}
	
	/*
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.eclipse.cdt.ui.CodeGeneration#getDestructorComment(ITranslationUnit, String, String[], String)
	 */
	public static String getDestructorComment(ITranslationUnit tu, String typeName, String[] excTypeSig, String lineDelimiter) throws CoreException {
		String templateId= CodeTemplateContextType.DESTRUCTORCOMMENT_ID;
		return getMethodComment(templateId, tu, typeName, "~" + typeName, EMPTY, excTypeSig, null, lineDelimiter); //$NON-NLS-1$
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.eclipse.cdt.ui.CodeGeneration#getMethodComment(ITranslationUnit, String, String, String[], String[], String, String)
	 */
	public static String getMethodComment(String templateId, ITranslationUnit tu, String typeName, String methodName, String[] paramNames, String[] excTypeSig, String retTypeSig, String lineDelimiter) throws CoreException {
		Template template= getCodeTemplate(templateId, tu.getCProject());
		if (template == null) {
			return null;
		}
		CodeTemplateContext context= new CodeTemplateContext(template.getContextTypeId(), tu.getCProject(), lineDelimiter);
		context.setTranslationUnitVariables(tu);
		context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE, typeName);
		context.setVariable(CodeTemplateContextType.ENCLOSING_METHOD, methodName);
		
		if (retTypeSig != null) {
			context.setVariable(CodeTemplateContextType.RETURN_TYPE, retTypeSig);
		}
		context.setTranslationUnitVariables(tu);
		TemplateBuffer buffer;
		try {
			buffer= context.evaluate(template);
		} catch (BadLocationException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		} catch (TemplateException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		if (buffer == null) {
			return null;
		}
		
		// TODO doc comment tags
		
		String str= buffer.getString();
		if (Strings.containsOnlyWhitespaces(str)) {
			return null;
		}

		return str;
	}
	
	// remove lines for empty variables, prefix multi-line variables
	private static String fixFullLineVariables(TemplateBuffer buffer, String[] variables) throws MalformedTreeException, BadLocationException {
		IDocument doc= new Document(buffer.getString());
		int nLines= doc.getNumberOfLines();
		MultiTextEdit edit= new MultiTextEdit();
		HashSet<Integer> removedLines= new HashSet<Integer>();
		for (int i= 0; i < variables.length; i++) {
			TemplateVariable position= findVariable(buffer, variables[i]);
			if (position == null) {
				continue;
			}
			if (position.getLength() > 0) {
				int[] offsets= position.getOffsets();
				for (int j= 0; j < offsets.length; j++) {
					final int offset = offsets[j];
					try {
						int startLine= doc.getLineOfOffset(offset);
						int startOffset= doc.getLineOffset(startLine);
						int endLine= doc.getLineOfOffset(offset + position.getLength());
						String prefix= doc.get(startOffset, offset - startOffset);
						if (prefix.length() > 0 && startLine < endLine) {
							for (int line= startLine + 1; line <= endLine; ++line) {
								int lineOffset= doc.getLineOffset(line);
								edit.addChild(new InsertEdit(lineOffset, prefix));
							}
						}
					} catch (BadLocationException e) {
						break;
					}
				}
			} else {
				int[] offsets= position.getOffsets();
				for (int k= 0; k < offsets.length; k++) {
					int line= doc.getLineOfOffset(offsets[k]);
					IRegion lineInfo= doc.getLineInformation(line);
					int offset= lineInfo.getOffset();
					String str= doc.get(offset, lineInfo.getLength());
					if (Strings.containsOnlyWhitespaces(str) && nLines > line + 1 && removedLines.add(new Integer(line))) {
						int nextStart= doc.getLineOffset(line + 1);
						int length= nextStart - offset;
						edit.addChild(new DeleteEdit(offset, length));
					}
				}
			}
		}
		edit.apply(doc, 0);
		return doc.get();
	}

	private static TemplateVariable findVariable(TemplateBuffer buffer, String variable) {
		TemplateVariable[] positions= buffer.getVariables();
		for (int i= 0; i < positions.length; i++) {
			TemplateVariable curr= positions[i];
			if (variable.equals(curr.getType())) {
				return curr;
			}
		}
		return null;
	}
	
	private static String evaluateTemplate(TemplateContext context, Template template) throws CoreException {
		TemplateBuffer buffer;
		try {
			buffer= context.evaluate(template);
		} catch (BadLocationException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		} catch (TemplateException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		if (buffer == null)
			return null;
		String str= buffer.getString();
		if (Strings.containsOnlyWhitespaces(str)) {
			return null;
		}
		return str;
	}
	
	private static String evaluateTemplate(TemplateContext context, Template template, String[] fullLineVariables) throws CoreException {
		TemplateBuffer buffer;
		try {
			buffer= context.evaluate(template);
			if (buffer == null)
				return null;
			String str= fixFullLineVariables(buffer, fullLineVariables);
			if (Strings.containsOnlyWhitespaces(str)) {
				return null;
			}
			return str;
		} catch (BadLocationException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		} catch (TemplateException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
	}

	/**
	 * Returns the line delimiter which is used in the specified project.
	 * 
	 * @param project the C project, or <code>null</code>
	 * @return the used line delimiter
	 */
	public static String getLineDelimiterUsed(ICProject project) {
		return getProjectLineDelimiter(project);
	}

	private static String getProjectLineDelimiter(ICProject cProject) {
		IProject project= null;
		if (cProject != null)
			project= cProject.getProject();
		
		String lineDelimiter= getLineDelimiterPreference(project);
		if (lineDelimiter != null)
			return lineDelimiter;
		
		return System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static String getLineDelimiterPreference(IProject project) {
		IScopeContext[] scopeContext;
		if (project != null) {
			// project preference
			scopeContext= new IScopeContext[] { new ProjectScope(project) };
			String lineDelimiter= Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, null, scopeContext);
			if (lineDelimiter != null)
				return lineDelimiter;
		}
		// workspace preference
		scopeContext= new IScopeContext[] { InstanceScope.INSTANCE };
		String platformDefault= System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, platformDefault, scopeContext);
	}
	
	/**
	 * Examines a string and returns the first line delimiter found.
	 */
	public static String getLineDelimiterUsed(ICElement elem) throws CModelException {
        if (elem == null) return ""; //$NON-NLS-1$
        
		ITranslationUnit cu= (ITranslationUnit) elem.getAncestor(ICElement.C_UNIT);
		if (cu != null && cu.exists()) {
			IBuffer buf= cu.getBuffer();
			int length= buf.getLength();
			for (int i= 0; i < length; i++) {
				char ch= buf.getChar(i);
				if (ch == SWT.CR) {
					if (i + 1 < length) {
						if (buf.getChar(i + 1) == SWT.LF) {
							return "\r\n"; //$NON-NLS-1$
						}
					}
					return "\r"; //$NON-NLS-1$
				} else if (ch == SWT.LF) {
					return "\n"; //$NON-NLS-1$
				}
			}
		}
		return getProjectLineDelimiter(elem.getCProject());
	}

	/**
	 * Get the default task tag for the given project.
	 * 
	 * @param project
	 * @return the default task tag
	 */
	public static String getTodoTaskTag(ICProject project) {
		String markers= null;
		if (project == null) {
			markers= CCorePlugin.getOption(CCorePreferenceConstants.TODO_TASK_TAGS);
		} else {
			markers= project.getOption(CCorePreferenceConstants.TODO_TASK_TAGS, true);
		}
		
		if (markers != null && markers.length() > 0) {
			int idx= markers.indexOf(',');
			if (idx == -1) {
				return markers;
			}
			return markers.substring(0, idx);
		}
		return CCorePreferenceConstants.DEFAULT_TASK_TAG;
	}
	
	public static boolean doAddComments(ICProject project) {
		return PreferenceConstants.getPreference(PreferenceConstants.CODEGEN_ADD_COMMENTS, project, false); 
	}
	
	private static Template getDefaultFileTemplate(ITranslationUnit tu) {
		String templateId= null;
		if (tu.isASMLanguage()) {
			templateId= CodeTemplateContextType.ASM_SOURCEFILE_ID;
		} else if (tu.isCXXLanguage()) {
			if (tu.isHeaderUnit()) {
				templateId= CodeTemplateContextType.CPP_HEADERFILE_ID;
			} else {
				templateId= CodeTemplateContextType.CPP_SOURCEFILE_ID;
			}
		} else if (tu.isCLanguage()) {
			if (tu.isHeaderUnit()) {
				templateId= CodeTemplateContextType.C_HEADERFILE_ID;
			} else {
				templateId= CodeTemplateContextType.C_SOURCEFILE_ID;
			}
		}
		return getCodeTemplate(templateId, tu.getCProject());
	}

	private static Template getTestFileTemplate(ITranslationUnit tu) {
		String templateId= null;
		if (tu.isCXXLanguage() && !tu.isHeaderUnit()) {
			templateId= CodeTemplateContextType.CPP_TESTFILE_ID;
		}
		return getCodeTemplate(templateId, tu.getCProject());
	}

	private static Template getCodeTemplate(String id, ICProject cProject) {
		return getCodeTemplate(id, cProject != null ? cProject.getProject() : null);
	}

	private static Template getCodeTemplate(String id, IProject project) {
		if (project == null)
			return CUIPlugin.getDefault().getCodeTemplateStore().findTemplateById(id);
		ProjectTemplateStore projectStore= new ProjectTemplateStore(project);
		try {
			projectStore.load();
		} catch (IOException e) {
			CUIPlugin.log(e);
		}
		return projectStore.findTemplateById(id);
	}

	private static String generateIncludeGuardSymbol(IResource file, ICProject cproject) {
		int scheme = PreferenceConstants.getPreference(
				PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME, cproject,
				PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_FILE_NAME);
		
		switch (scheme) {
		case PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_FILE_PATH:
			if (file == null)
				return null;
			IPath path = file.getFullPath();
			ISourceRoot root = cproject.findSourceRoot(file);
			if (root != null) {
				path = PathUtil.makeRelativePath(path, root.getPath());
			}
			return generateIncludeGuardSymbolFromFilePath(path.toString());
			
		default:
			CUIPlugin.log("Unknown preference value " + scheme + " for include guard scheme.", null); //$NON-NLS-1$ //$NON-NLS-2$
			//$FALL-THROUGH$
		case PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_FILE_NAME:
			if (file == null)
				return null;
			return generateIncludeGuardSymbolFromFilePath(file.getName());

		case PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_UUID:
    		return generateIncludeGuardSymbolFromUUID();
		}
    }

	private static String generateIncludeGuardSymbolFromFilePath(String filename) {
		// Convert to upper case and replace invalid characters with underscores,
		// e.g. convert some/directory/foo-bar.h to SOME_DIRECTORY_FOO_BAR_H_
		StringBuilder buf = new StringBuilder(filename.length() + 1);
		for (int i = 0; i < filename.length(); ++i) {
			char ch = filename.charAt(i);
			if (Character.isLetterOrDigit(ch)) {
				buf.append(Character.toUpperCase(ch));
			} else if (buf.length() > 0){
				buf.append('_');
			}
		}
		buf.append('_');
		return buf.toString();
	}

	private static String generateIncludeGuardSymbolFromUUID() {
		String uuid = UUID.randomUUID().toString();
		
		// 1) Make sure the guard always starts with a letter.
		// 2) Convert to upper case and remove invalid characters
		// 
		// e.g. convert
		//         067e6162-3b6f-4ae2-a171-2470b63dff00 to
		//        H067E6162-3b6F-4AE2-A171-2470B63DFF00
		StringBuilder buf = new StringBuilder();
		
		buf.append('H');
		
		for (int i = 0; i < uuid.length(); ++i) {
			char ch = uuid.charAt(i);
			if (Character.isLetterOrDigit(ch)) {
				buf.append(Character.toUpperCase(ch));
			} else {
				buf.append('_');
			}
		}

		return buf.toString();
	}

	/**
	 * Get a set of file templates for the given content types.
	 * 
	 * @param contentTypes  the list of content types
	 * @param project  the project or <code>null</code>
	 * @return an array of templates
	 */
	public static Template[] getFileTemplatesForContentTypes(String[] contentTypes, IProject project) {
		if (contentTypes == null || contentTypes.length == 0) {
			return new Template[0];
		}
		TemplatePersistenceData[] templateDatas;
		if (project == null) {
			templateDatas= CUIPlugin.getDefault().getCodeTemplateStore().getTemplateData(true);
		} else {
			ProjectTemplateStore projectStore= new ProjectTemplateStore(project.getProject());
			try {
				projectStore.load();
			} catch (IOException e) {
				CUIPlugin.log(e);
			}
			templateDatas= projectStore.getTemplateData();
		}
		List<Template> result= new ArrayList<Template>();
		for (int j = 0; j < contentTypes.length; j++) {
			for (int i = 0; i < templateDatas.length; i++) {
				Template template = templateDatas[i].getTemplate();
				final String contextTypeId = template.getContextTypeId();
				if (FileTemplateContextType.isContextTypeForContentType(contextTypeId, contentTypes[j])) {
					result.add(template);
				}
			}
		}
		return result.toArray(new Template[result.size()]);
	}

	/**
	 * Returns a suggested name for a getter that is guaranteed to be a valid identifier
	 * and not collide with a set of given names.
	 *  
	 * @param baseName the name used as an inspiration
	 * @param bool <code>true</code> if the getter is for a boolean field
	 * @param excluded the set of excluded names, can be {@code null}
	 * @param context the translation unit for which the code is intended, can be {@code null}
	 * @return the suggested name, or {@code null} if all possible names are taken
	 */
	public static String suggestGetterName(String baseName, boolean bool, Set<String> excluded, ITranslationUnit context) {
		IPreferencesService preferences = Platform.getPreferencesService();
    	int capitalization = preferences.getInt(CUIPlugin.PLUGIN_ID,
    			PreferenceConstants.NAME_STYLE_GETTER_CAPITALIZATION,
    			PreferenceConstants.NAME_STYLE_CAPITALIZATION_CAMEL_CASE, null);
    	String wordDelimiter = preferences.getString(CUIPlugin.PLUGIN_ID,
    			PreferenceConstants.NAME_STYLE_GETTER_WORD_DELIMITER, "", null); //$NON-NLS-1$
    	String prefix = bool ?
    			preferences.getString(CUIPlugin.PLUGIN_ID,
    					PreferenceConstants.NAME_STYLE_GETTER_PREFIX_FOR_BOOLEAN, "is", null) : //$NON-NLS-1$
				preferences.getString(CUIPlugin.PLUGIN_ID,
						PreferenceConstants.NAME_STYLE_GETTER_PREFIX, "get", null); //$NON-NLS-1$
    	String suffix = preferences.getString(CUIPlugin.PLUGIN_ID,
    			PreferenceConstants.NAME_STYLE_GETTER_SUFFIX, "", null); //$NON-NLS-1$
		NameComposer composer = new NameComposer(capitalization, wordDelimiter, prefix, suffix);
		return adjustName(composer.compose(baseName), excluded, context);
	}

	/**
	 * Returns a suggested name for a setter that is guaranteed to be a valid identifier
	 * and not collide with a set of given names.
	 *  
	 * @param baseName the name used as an inspiration
	 * @param excluded the set of excluded names, can be {@code null}
	 * @param context the translation unit for which the code is intended, can be {@code null}
	 * @return the suggested name, or {@code null} if all possible names are taken
	 */
	public static String suggestSetterName(String baseName, Set<String> excluded, ITranslationUnit context) {
		IPreferencesService preferences = Platform.getPreferencesService();
		int capitalization = preferences.getInt(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_SETTER_CAPITALIZATION,
				PreferenceConstants.NAME_STYLE_CAPITALIZATION_CAMEL_CASE, null);
		String wordDelimiter = preferences.getString(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_SETTER_WORD_DELIMITER, "", null); //$NON-NLS-1$
		String prefix = preferences.getString(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_SETTER_PREFIX, "set", null); //$NON-NLS-1$
		String suffix = preferences.getString(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_SETTER_SUFFIX, "", null); //$NON-NLS-1$
		NameComposer composer = new NameComposer(capitalization, wordDelimiter, prefix, suffix);
		return adjustName(composer.compose(baseName), excluded, context);
	}

	/**
	 * Returns a suggested name for a function parameter that is guaranteed to be a valid identifier
	 * and not collide with a set of given names.
	 *  
	 * @param baseName the name used as an inspiration
	 * @param excluded the set of excluded names, can be {@code null}
	 * @param context the translation unit for which the code is intended, can be {@code null}
	 * @return the suggested name, or {@code null} if all possible names are taken
	 */
	public static String suggestParameterName(String baseName, Set<String> excluded, ITranslationUnit context) {
		IPreferencesService preferences = Platform.getPreferencesService();
		int capitalization = preferences.getInt(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_VARIABLE_CAPITALIZATION,
				PreferenceConstants.NAME_STYLE_CAPITALIZATION_ORIGINAL, null);
		String wordDelimiter = preferences.getString(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_VARIABLE_WORD_DELIMITER, "", null); //$NON-NLS-1$
		String prefix = preferences.getString(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_VARIABLE_PREFIX, "", null); //$NON-NLS-1$
		String suffix = preferences.getString(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_VARIABLE_SUFFIX, "", null); //$NON-NLS-1$
		NameComposer composer = new NameComposer(capitalization, wordDelimiter, prefix, suffix);
		return adjustName(composer.compose(baseName), excluded, context);
	}

	/**
	 * Returns a suggested name for a method that is guaranteed to be a valid identifier
	 * and not collide with a set of given names.
	 *  
	 * @param baseName the name used as an inspiration
	 * @param excluded the set of excluded names, can be {@code null}
	 * @param context the translation unit for which the code is intended, can be {@code null}
	 * @return the suggested name, or {@code null} if all possible names are taken
	 */
	public static String suggestMethodName(String baseName, Set<String> excluded, ITranslationUnit context) {
		IPreferencesService preferences = Platform.getPreferencesService();
		int capitalization = preferences.getInt(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_METHOD_CAPITALIZATION,
				PreferenceConstants.NAME_STYLE_CAPITALIZATION_ORIGINAL, null);
		String wordDelimiter = preferences.getString(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_METHOD_WORD_DELIMITER, "", null); //$NON-NLS-1$
		String prefix = preferences.getString(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_METHOD_PREFIX, "", null); //$NON-NLS-1$
		String suffix = preferences.getString(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_METHOD_SUFFIX, "", null); //$NON-NLS-1$
		NameComposer composer = new NameComposer(capitalization, wordDelimiter, prefix, suffix);
		return adjustName(composer.compose(baseName), excluded, context);
	}

	/**
	 * Checks is the given name is valid and, if not, tries to adjust it by adding a numeric suffix
	 * to it.
	 * 
	 * @param name the name to check and, possibly, adjust
	 * @param namesToAvoid the set of names to avoid
	 * @param context the translation unit, can be {@code null}
	 * @return the adjusted name, or <code>null</code> if a valid name could not be generated. 
	 */
	private static String adjustName(String name, Set<String> namesToAvoid, ITranslationUnit context) {
		ILanguage language = null;
		try {
			if (context != null)
				language = context.getLanguage();
		} catch (CoreException e) {
			// Ignore
		}
		return adjustName(name, namesToAvoid, language);
	}

	/**
	 * Checks is the given name is valid and, if not, tries to adjust it by adding a numeric suffix
	 * to it.
	 * 
	 * @param name the name to check and, possibly, adjust
	 * @param namesToAvoid the set of names to avoid
	 * @param language the language of the translation unit, can be {@code null}
	 * @return the adjusted name, or <code>null</code> if a valid name could not be generated. 
	 */
	private static String adjustName(String name, Set<String> namesToAvoid, ILanguage language) {
		if (language == null) {
			language = GPPLanguage.getDefault();
		}
		String originalName = name;
		if (!isValidIdentifier(name, language)) {
			if ("class".equals(name)) { //$NON-NLS-1$
				name = "clazz"; //$NON-NLS-1$
			} else {
				name = '_' + name;
			}
		}
		int numTries = namesToAvoid != null ? namesToAvoid.size() + 1 : 1;
		for (int i = 1; i <= numTries; i++) {
			if ((namesToAvoid == null || !namesToAvoid.contains(name)) &&
					isValidIdentifier(name, language)) {
				return name;
			}
			name = originalName + i;
		}
		return null;
	}

	private static boolean isValidIdentifier(String name, ILanguage language) {
		if (language instanceof AbstractCLikeLanguage) {
			return CConventions.validateIdentifier(name, (AbstractCLikeLanguage) language).isOK();
		}
		return true;
	}

	/**
	 * Returns the trimmed field name. Leading and trailing non-alphanumeric characters are trimmed.
	 * If the first word of the name consists of a single letter and the name contains more than
	 * one word, the first word is removed.
	 * 
	 * @param fieldName a field name to trim
	 * @return the trimmed field name
	 */
	public static String trimFieldName(String fieldName) {
		CBreakIterator iterator = new CBreakIterator();
		iterator.setText(fieldName);
		int firstWordStart = -1;
		int firstWordEnd = -1;
		int secondWordStart = -1;
		int lastWordEnd = -1;
		int end;
		for (int start = iterator.first(); (end = iterator.next()) != BreakIterator.DONE; start = end) {
			if (Character.isLetterOrDigit(fieldName.charAt(start))) {
				int pos = end;
				while (--pos >= start && !Character.isLetterOrDigit(fieldName.charAt(pos))) {
				}
				lastWordEnd = pos + 1;
				if (firstWordStart < 0) {
					firstWordStart = start;
					firstWordEnd = lastWordEnd;
				} else if (secondWordStart < 0) {
					secondWordStart = start;
				}
			}
		}
		// Skip the first word if it consists of a single letter and the name contains more than
		// one word.
		if (firstWordStart >= 0 && firstWordStart + 1 == firstWordEnd && secondWordStart >= 0) {
			firstWordStart = secondWordStart;
		}
		if (firstWordStart < 0) {
			return fieldName;
		} else {
			return fieldName.substring(firstWordStart, lastWordEnd);
		}
	}
}

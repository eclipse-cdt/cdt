/*******************************************************************************
 * Copyright (c) 2001, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - initial implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Jens Elmenthaler (Verigy) - http://bugs.eclipse.org/235586
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.codemanipulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
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

import org.eclipse.cdt.internal.ui.viewsupport.ProjectTemplateStore;

public class StubUtility {
	private static final String[] EMPTY= new String[0];
	
	private StubUtility() {
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.eclipse.cdt.ui.CodeGeneration#getHeaderFileContent(ITranslationUnit, String, String, String)
	 */	
	public static String getHeaderFileContent(ITranslationUnit tu, String fileComment, String typeComment, String declarations, String lineDelimiter) throws CoreException {
		return getHeaderFileContent(getDefaultFileTemplate(tu), tu, fileComment, typeComment, declarations, lineDelimiter);
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.eclipse.cdt.ui.CodeGeneration#getHeaderFileContent(Template, ITranslationUnit, String, String, String)
	 */	
	public static String getHeaderFileContent(Template template, ITranslationUnit tu, String fileComment, String typeComment, String declarations, String lineDelimiter) throws CoreException {
		if (template == null) {
			return null;
		}
		ICProject project= tu.getCProject();
		CodeTemplateContext context= new CodeTemplateContext(template.getContextTypeId(), project, lineDelimiter);
		context.setTranslationUnitVariables(tu);
		context.setVariable(CodeTemplateContextType.TYPE_COMMENT, typeComment != null ? typeComment : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.FILE_COMMENT, fileComment != null ? fileComment : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.DECLARATIONS, declarations != null ? declarations : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.TYPENAME, new Path(tu.getElementName()).removeFileExtension().toString());
		String includeGuardSymbol= generateIncludeGuardSymbol(tu.getResource(), project);
		context.setVariable(CodeTemplateContextType.INCLUDE_GUARD_SYMBOL, includeGuardSymbol != null ? includeGuardSymbol : ""); //$NON-NLS-1$
		
		String[] fullLine= { CodeTemplateContextType.FILE_COMMENT, CodeTemplateContextType.TYPE_COMMENT, CodeTemplateContextType.DECLARATIONS };
		return evaluateTemplate(context, template, fullLine);
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.eclipse.cdt.ui.CodeGeneration#getBodyFileContent(ITranslationUnit, String, String, String)
	 */	
	public static String getBodyFileContent(ITranslationUnit tu, String fileComment, String typeComment, String declarations, String lineDelimiter) throws CoreException {
		return getBodyFileContent(getDefaultFileTemplate(tu), tu, fileComment, typeComment, declarations, lineDelimiter);
	}

	/*
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.eclipse.cdt.ui.CodeGeneration#getBodyFileContent(Template, ITranslationUnit, String, String, String)
	 */	
	public static String getBodyFileContent(Template template, ITranslationUnit tu, String fileComment, String typeComment, String declarations, String lineDelimiter) throws CoreException {
		if (template == null) {
			return null;
		}
		ICProject project= tu.getCProject();
		CodeTemplateContext context= new CodeTemplateContext(template.getContextTypeId(), project, lineDelimiter);
		context.setTranslationUnitVariables(tu);
		context.setVariable(CodeTemplateContextType.TYPE_COMMENT, typeComment != null ? typeComment : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.FILE_COMMENT, fileComment != null ? fileComment : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.DECLARATIONS, declarations != null ? declarations : ""); //$NON-NLS-1$
		
		String[] fullLine= { CodeTemplateContextType.FILE_COMMENT, CodeTemplateContextType.TYPE_COMMENT, CodeTemplateContextType.DECLARATIONS };
		return evaluateTemplate(context, template, fullLine);
	}

	public static String getFileContent(Template template, IFile file, String lineDelimiter) throws CoreException {
		FileTemplateContext context= new FileTemplateContext(template.getContextTypeId(), lineDelimiter);
		String fileComment= getFileComment(file, lineDelimiter);
		context.setVariable(CodeTemplateContextType.FILE_COMMENT, fileComment != null ? fileComment : ""); //$NON-NLS-1$
		ICProject cproject = CoreModel.getDefault().create(file.getProject());
		String includeGuardSymbol= generateIncludeGuardSymbol(file, cproject);
		context.setVariable(CodeTemplateContextType.INCLUDE_GUARD_SYMBOL, includeGuardSymbol != null ? includeGuardSymbol : ""); //$NON-NLS-1$
		context.setResourceVariables(file);
		String[] fullLine= { CodeTemplateContextType.FILE_COMMENT };
		return evaluateTemplate(context, template, fullLine);
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
	public static String getMethodBodyContent(String templateId, ICProject project, String typeName, String methodName, String bodyStatement, String lineDelimiter) throws CoreException {
		Template template= getCodeTemplate(templateId, project);
		if (template == null) {
			return bodyStatement;
		}
		CodeTemplateContext context= new CodeTemplateContext(template.getContextTypeId(), project, lineDelimiter);
		context.setVariable(CodeTemplateContextType.ENCLOSING_METHOD, methodName);
		context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE, typeName);
		context.setVariable(CodeTemplateContextType.BODY_STATEMENT, bodyStatement != null ? bodyStatement : ""); //$NON-NLS-1$
		String str= evaluateTemplate(context, template, new String[] { CodeTemplateContextType.BODY_STATEMENT });
		if (str == null && !Strings.containsOnlyWhitespaces(bodyStatement)) {
			return bodyStatement;
		}
		return str;
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
					final int offset = offsets[j] ;
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
		scopeContext= new IScopeContext[] { new InstanceScope() };
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
}

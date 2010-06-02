/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.template.c;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.SimpleTemplateVariableResolver;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.jface.text.templates.TemplateVariableType;

import org.eclipse.cdt.internal.corext.util.Messages;

/**
 * A generic template context type for file resources based on content-type.
 *
 * @since 5.0
 */
public class FileTemplateContextType extends TemplateContextType {

	public static final String CONTEXTTYPE_SUFFIX= ".contenttype_context"; //$NON-NLS-1$

	public static final String CONTENTTYPE_TEXT= "org.eclipse.core.runtime.text"; //$NON-NLS-1$
	public static final String TEXTFILE_CONTEXTTYPE= CONTENTTYPE_TEXT + CONTEXTTYPE_SUFFIX;

	/* resolver types */
	public static final String FILENAME= "file_name"; //$NON-NLS-1$
	public static final String FILEBASE= "file_base"; //$NON-NLS-1$
	public static final String FILELOCATION= "file_loc"; //$NON-NLS-1$
	public static final String FILEPATH= "file_path"; //$NON-NLS-1$
	public static final String PROJECTNAME= "project_name"; //$NON-NLS-1$

	/**
	 * Resolver that resolves to the variable defined in the context.
	 */
	static class FileTemplateVariableResolver extends SimpleTemplateVariableResolver {
		public FileTemplateVariableResolver(String type, String description) {
			super(type, description);
		}
		
		@Override
		protected String resolve(TemplateContext context) {
			String value= context.getVariable(getType());
			return value != null ? value : ""; //$NON-NLS-1$
		}
	}

	/**
	 * This date variable evaluates to the current date in a specific format.
	 */
	static class DateVariableResolver extends SimpleTemplateVariableResolver {
		private String fFormat;

		public DateVariableResolver() {
			super("date", TemplateMessages.FileTemplateContextType_variable_description_date); //$NON-NLS-1$
		}
		
		/*
		 * @see org.eclipse.jface.text.templates.TemplateVariableResolver#resolve(org.eclipse.jface.text.templates.TemplateVariable, org.eclipse.jface.text.templates.TemplateContext)
		 */
		@Override
		public void resolve(TemplateVariable variable, TemplateContext context) {
			fFormat= null;
			TemplateVariableType type= variable.getVariableType();
			List<?> params= type.getParams();
			if (params.size() == 1) {
				fFormat= params.get(0).toString();
			}
			super.resolve(variable, context);
		}
		
		/*
		 * @see org.eclipse.jface.text.templates.SimpleTemplateVariableResolver#resolve(org.eclipse.jface.text.templates.TemplateContext)
		 */
		@Override
		protected String resolve(TemplateContext context) {
			DateFormat f;
			if (fFormat == null) {
				f= DateFormat.getDateInstance();
			} else {
				f= new SimpleDateFormat(fFormat);
			}
			return f.format(new java.util.Date());
		}
	}

	/**
	 * Resolver that resolves to the value of a core variable.
	 */
	static class CoreVariableResolver extends SimpleTemplateVariableResolver {
		private String fVariableName;
		private String[] fArguments;

		public CoreVariableResolver(String type) {
			super(type, TemplateMessages.FileTemplateContextType__variable_description_eclipse);
		}

		/*
		 * @see org.eclipse.jface.text.templates.TemplateVariableResolver#resolve(org.eclipse.jface.text.templates.TemplateVariable, org.eclipse.jface.text.templates.TemplateContext)
		 */
		@Override
		public void resolve(TemplateVariable variable, TemplateContext context) {
			fVariableName= variable.getName();
			TemplateVariableType type= variable.getVariableType();
			List<?> params= type.getParams();
			fArguments= params.toArray(new String[params.size()]);
			super.resolve(variable, context);
		}
		
		/*
		 * @see org.eclipse.jface.text.templates.SimpleTemplateVariableResolver#resolve(org.eclipse.jface.text.templates.TemplateContext)
		 */
		@Override
		protected String resolve(TemplateContext context) {
			StringBuffer expr= new StringBuffer("${"); //$NON-NLS-1$
			expr.append(fVariableName);
			for (int i = 0; i < fArguments.length; i++) {
				expr.append(':').append(fArguments[i]);
			}
			expr.append('}');
			IStringVariableManager mgr= VariablesPlugin.getDefault().getStringVariableManager();
			try {
				return mgr.performStringSubstitution(expr.toString(), false);
			} catch (CoreException exc) {
				return expr.toString();
			}
		}
		
	}

	public FileTemplateContextType(String contextTypeId) {
		this(contextTypeId, contextTypeId);
	}

	public FileTemplateContextType(String contextTypeId, String contextName) {
		super(contextTypeId, contextName);

		// global
		addResolver(new GlobalTemplateVariables.Dollar());
		addResolver(new DateVariableResolver());
		addResolver(new GlobalTemplateVariables.Year());
		addResolver(new GlobalTemplateVariables.Time());
		addResolver(new GlobalTemplateVariables.User());

//		addResolver(new CoreVariableResolver("eclipse")); //$NON-NLS-1$
		
		addResourceVariables();
	}
	
	protected void addResourceVariables() {
		addResolver(new FileTemplateVariableResolver(FILENAME, TemplateMessages.FileTemplateContextType_variable_description_filename));
		addResolver(new FileTemplateVariableResolver(FILEBASE, TemplateMessages.FileTemplateContextType_variable_description_filebase));
		addResolver(new FileTemplateVariableResolver(FILELOCATION, TemplateMessages.FileTemplateContextType_variable_description_fileloc));
		addResolver(new FileTemplateVariableResolver(FILEPATH, TemplateMessages.FileTemplateContextType_variable_description_filepath));
		addResolver(new FileTemplateVariableResolver(PROJECTNAME, TemplateMessages.FileTemplateContextType_variable_description_projectname));
	}

	@Override
	protected TemplateVariableResolver getResolver(String type) {
		// compatibility with editor template variables
		if ("file".equals(type)) { //$NON-NLS-1$
			type= FILENAME;
		} else if ("project".equals(type) || "enclosing_project".equals(type)) { //$NON-NLS-1$ //$NON-NLS-2$
			type= PROJECTNAME;
		}
		return super.getResolver(type);
	}

	@Override
	protected void validateVariables(TemplateVariable[] variables) throws TemplateException {
		ArrayList<String> required=  new ArrayList<String>(5);
		for (int i= 0; i < variables.length; i++) {
			String type= variables[i].getType();
			if (getResolver(type) == null) {
				throw new TemplateException(Messages.format(TemplateMessages.FileTemplateContextType_validate_unknownvariable, type));
			}
			required.remove(type);
		}
		if (!required.isEmpty()) {
			String missing= required.get(0);
			throw new TemplateException(Messages.format(TemplateMessages.FileTemplateContextType_validate_missingvariable, missing));
		}
		super.validateVariables(variables);
	}

	/*
	 * @see org.eclipse.jface.text.templates.TemplateContextType#resolve(org.eclipse.jface.text.templates.TemplateVariable, org.eclipse.jface.text.templates.TemplateContext)
	 */
	@Override
	public void resolve(TemplateVariable variable, TemplateContext context) {
		String type= variable.getType();
		TemplateVariableResolver resolver= getResolver(type);
		if (resolver == null) {
			resolver= new FileTemplateVariableResolver(type, ""); //$NON-NLS-1$
		}
		resolver.resolve(variable, context);
	}

	public static void registerContextTypes(ContextTypeRegistry registry) {
		IContentTypeManager contentTypeMgr= Platform.getContentTypeManager();
		IContentType[] contentTypes= contentTypeMgr.getAllContentTypes();
		for (int i = 0; i < contentTypes.length; i++) {
			IContentType contentType = contentTypes[i];
			if (isTextContentType(contentType) && contentType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC).length > 0) {
				final String contextTypeId= contextTypeIdForContentType(contentType);
				if (registry.getContextType(contextTypeId) == null) {
					registry.addContextType(new FileTemplateContextType(contextTypeId, contentType.getName()));
				}
			}
		}
	}

	public static String contextTypeIdForContentType(IContentType contentType) {
		return contentType.getId() + CONTEXTTYPE_SUFFIX;
	}
	public static boolean isFileTemplateContextType(String contextTypeId) {
		return contextTypeId.endsWith(CONTEXTTYPE_SUFFIX);
	}
	public static boolean isContextTypeForContentType(String contextTypeId, String contentTypeId) {
		return contextTypeId.endsWith(CONTEXTTYPE_SUFFIX) && contextTypeId.startsWith(contentTypeId);
	}
	public static String contentTypeIdForContextType(String contextTypeId) {
		return contextTypeId.substring(0, contextTypeId.length() - CONTEXTTYPE_SUFFIX.length());
	}

	private static boolean isTextContentType(IContentType contentType) {
		if (contentType == null) {
			return false;
		}
		String id= contentType.getId();
		if (id.equals(CONTENTTYPE_TEXT)) {
			return true;
		}
		if (id.indexOf(".pde.") != -1 || id.indexOf(".jdt.") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		return isTextContentType(contentType.getBaseType());
	}

}

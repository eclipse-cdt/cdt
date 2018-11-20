/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QnX Software System
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.template.c;

import org.eclipse.osgi.util.NLS;

public final class TemplateMessages extends NLS {
	public static String CContextType_variable_description_file;
	public static String CContextType_variable_description_file_base;
	public static String CContextType_variable_description_enclosing_method;
	public static String CContextType_variable_description_enclosing_project;
	public static String CContextType_variable_description_enclosing_method_arguments;
	public static String CContextType_variable_description_return_type;
	public static String CContextType_variable_description_todo;

	public static String CodeTemplateContextType_variable_description_todo;
	public static String CodeTemplateContextType_variable_description_typedeclaration;
	public static String CodeTemplateContextType_variable_description_class_members;
	public static String CodeTemplateContextType_variable_description_fieldname;
	public static String CodeTemplateContextType_variable_description_fieldtype;
	public static String CodeTemplateContextType_variable_description_typecomment;
	public static String CodeTemplateContextType_variable_description_enclosingtype;
	public static String CodeTemplateContextType_variable_description_includes;
	public static String CodeTemplateContextType_variable_description_namespace_begin;
	public static String CodeTemplateContextType_variable_description_namespace_end;
	public static String CodeTemplateContextType_variable_description_namespace_name;
	public static String CodeTemplateContextType_variable_description_typename;
	public static String CodeTemplateContextType_variable_description_class_name;
	public static String CodeTemplateContextType_variable_description_base_classes;
	public static String CodeTemplateContextType_variable_description_include_guard_symbol;
	public static String CodeTemplateContextType_variable_description_enclosingmethod;
	public static String CodeTemplateContextType_variable_description_bodystatement;
	public static String CodeTemplateContextType_variable_description_returntype;
	public static String CodeTemplateContextType_variable_description_filecomment;
	public static String CodeTemplateContextType_validate_invalidcomment;
	public static String CodeTemplateContextType_csource_name;
	public static String CodeTemplateContextType_cheader_name;
	public static String CodeTemplateContextType_cppsource_name;
	public static String CodeTemplateContextType_cppheader_name;
	public static String CodeTemplateContextType_asmsource_name;

	public static String FileTemplateContextType__variable_description_eclipse;
	public static String FileTemplateContextType_validate_unknownvariable;
	public static String FileTemplateContextType_validate_missingvariable;
	public static String FileTemplateContextType_variable_description_date;
	public static String FileTemplateContextType_variable_description_filename;
	public static String FileTemplateContextType_variable_description_filebase;
	public static String FileTemplateContextType_variable_description_fileloc;
	public static String FileTemplateContextType_variable_description_filepath;
	public static String FileTemplateContextType_variable_description_projectname;

	static {
		NLS.initializeMessages(TemplateMessages.class.getName(), TemplateMessages.class);
	}

	// Do not instantiate.
	private TemplateMessages() {
	}
}
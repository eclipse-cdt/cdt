/**********************************************************************
 * Copyright (c) 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * @author dsteffle
 */
public class DOMASTPluginImages {
	private static ImageRegistry imageRegistry = new ImageRegistry(CUIPlugin.getStandardDisplay());
	
	/**
	 * Returns the standard display to be used. The method first checks, if
	 * the thread calling this method has an associated display. If so, this
	 * display is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display= Display.getCurrent();
		if (display == null) {
			display= Display.getDefault();
		}
		return display;		
	}	
	
	// Subdirectory (under the package containing this class) where 16 color images are
	private static URL fgIconBaseURL;

	static {
		try {
			fgIconBaseURL= new URL(CTestPlugin.getDefault().getBundle().getEntry("/"), "icons/" ); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (MalformedURLException e) {}
	}	
	public static final String PLUGIN_ID = "org.eclipse.cdt.testplugin.CTestPlugin"; //$NON-NLS-1$
	private static final String NAME_PREFIX= PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH= NAME_PREFIX.length();
	public static final String ICON_PREFIX= "used/"; //$NON-NLS-1$

	public static final String IMG_IASTArrayModifier= NAME_PREFIX + "showasarray_co.gif"; //$NON-NLS-1$
	public static final String IMG_IASTDeclaration= NAME_PREFIX + "cdeclaration_obj.gif"; //$NON-NLS-1$
	public static final String IMG_IASTDeclarator= NAME_PREFIX + "variable_obj.gif"; //$NON-NLS-1$
	public static final String IMG_IASTDeclSpecifier= NAME_PREFIX + "var_simple.gif"; //$NON-NLS-1$
	public static final String IMG_IASTEnumerator= NAME_PREFIX + "enumerator_obj.gif"; //$NON-NLS-1$
	public static final String IMG_IASTExpression= NAME_PREFIX + "expression_obj.gif"; //$NON-NLS-1$
	public static final String IMG_IASTInitializer= NAME_PREFIX + "variable_local_obj.gif"; //$NON-NLS-1$
	public static final String IMG_IASTName= NAME_PREFIX + "tnames_co.gif"; //$NON-NLS-1$
	public static final String IMG_IASTParameterDeclaration= NAME_PREFIX + "var_declaration_obj.gif"; //$NON-NLS-1$
	public static final String IMG_IASTPointerOperator= NAME_PREFIX + "var_pointer.gif"; //$NON-NLS-1$
	public static final String IMG_IASTPreprocessorStatement= NAME_PREFIX + "define_obj.gif"; //$NON-NLS-1$
	public static final String IMG_IASTProblem= NAME_PREFIX + "warning_obj.gif"; //$NON-NLS-1$
	public static final String IMG_IASTSimpleDeclaration= NAME_PREFIX + "method_public_obj.gif"; //$NON-NLS-1$
	public static final String IMG_IASTStatement= NAME_PREFIX + "statement_obj.gif"; //$NON-NLS-1$
	public static final String IMG_IASTTranslationUnit= NAME_PREFIX + "source_attach_attrib.gif"; //$NON-NLS-1$
	public static final String IMG_IASTTypeId= NAME_PREFIX + "types.gif"; //$NON-NLS-1$
	public static final String IMG_ICASTDesignator= NAME_PREFIX + "watch_globals.gif"; //$NON-NLS-1$
	public static final String IMG_ICPPASTConstructorChainInitializer = NAME_PREFIX + "jump_co.gif"; //$NON-NLS-1$
	public static final String IMG_ICPPASTTemplateParameter = NAME_PREFIX + "disassembly.gif"; //$NON-NLS-1$
	public static final String IMG_DEFAULT = NAME_PREFIX + "brkpd_obj.gif"; //$NON-NLS-1$
	public static final String IMG_EXPAND_ALL = NAME_PREFIX + "expandall.gif"; //$NON-NLS-1$
	public static final String IMG_COLLAPSE_ALL = NAME_PREFIX + "collapseall.gif"; //$NON-NLS-1$
	public static final String IMG_CLEAR = NAME_PREFIX + "clear.gif"; //$NON-NLS-1$
	
	public static final ImageDescriptor DESC_IASTArrayModifier= createManaged(ICON_PREFIX, IMG_IASTArrayModifier);
	public static final ImageDescriptor DESC_IASTDeclaration= createManaged(ICON_PREFIX, IMG_IASTDeclaration);
	public static final ImageDescriptor DESC_IASTDeclarator= createManaged(ICON_PREFIX, IMG_IASTDeclarator);
	public static final ImageDescriptor DESC_IASTDeclSpecifier= createManaged(ICON_PREFIX, IMG_IASTDeclSpecifier);
	public static final ImageDescriptor DESC_IASTEnumerator= createManaged(ICON_PREFIX, IMG_IASTEnumerator);
	public static final ImageDescriptor DESC_IASTExpression= createManaged(ICON_PREFIX, IMG_IASTExpression);
	public static final ImageDescriptor DESC_IASTInitializer= createManaged(ICON_PREFIX, IMG_IASTInitializer);
	public static final ImageDescriptor DESC_IASTName= createManaged(ICON_PREFIX, IMG_IASTName);
	public static final ImageDescriptor DESC_IASTParameterDeclaration= createManaged(ICON_PREFIX, IMG_IASTParameterDeclaration);
	public static final ImageDescriptor DESC_IASTPointerOperator= createManaged(ICON_PREFIX, IMG_IASTPointerOperator);
	public static final ImageDescriptor DESC_IASTPreprocessorStatement= createManaged(ICON_PREFIX, IMG_IASTPreprocessorStatement);
	public static final ImageDescriptor DESC_IASTProblem= createManaged(ICON_PREFIX, IMG_IASTProblem);
	public static final ImageDescriptor DESC_IASTSimpleDeclaration= createManaged(ICON_PREFIX, IMG_IASTSimpleDeclaration);
	public static final ImageDescriptor DESC_IASTStatement= createManaged(ICON_PREFIX, IMG_IASTStatement);
	public static final ImageDescriptor DESC_IASTTranslationUnit= createManaged(ICON_PREFIX, IMG_IASTTranslationUnit);
	public static final ImageDescriptor DESC_IASTTypeId= createManaged(ICON_PREFIX, IMG_IASTTypeId);
	public static final ImageDescriptor DESC_ICASTDesignator= createManaged(ICON_PREFIX, IMG_ICASTDesignator);
	public static final ImageDescriptor DESC_ICPPASTConstructorChainInitializer= createManaged(ICON_PREFIX, IMG_ICPPASTConstructorChainInitializer);
	public static final ImageDescriptor DESC_ICPPASTTemplateParameter= createManaged(ICON_PREFIX, IMG_ICPPASTTemplateParameter);
	public static final ImageDescriptor DESC_DEFAULT= createManaged(ICON_PREFIX, IMG_DEFAULT);
	public static final ImageDescriptor DESC_EXPAND_ALL= createManaged(ICON_PREFIX, IMG_EXPAND_ALL);
	public static final ImageDescriptor DESC_COLLAPSE_ALL= createManaged(ICON_PREFIX, IMG_COLLAPSE_ALL);
	public static final ImageDescriptor DESC_CLEAR= createManaged(ICON_PREFIX, IMG_CLEAR);
	
	private static ImageDescriptor createManaged(String prefix, String name) {
		return createManaged(imageRegistry, prefix, name);
	}
	
	private static ImageDescriptor createManaged(ImageRegistry registry, String prefix, String name) {
		ImageDescriptor result= ImageDescriptor.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
		registry.put(name, result);
		return result;
	}
	
	private static URL makeIconFileURL(String prefix, String name) {
		StringBuffer buffer= new StringBuffer(prefix);
		buffer.append(name);
		try {
			return new URL(fgIconBaseURL, buffer.toString());
		} catch (MalformedURLException e) {
			CUIPlugin.getDefault().log(e);
			return null;
		}
	}
	
	public static Image get(String key) {
		return imageRegistry.get(key);
	}
	
}

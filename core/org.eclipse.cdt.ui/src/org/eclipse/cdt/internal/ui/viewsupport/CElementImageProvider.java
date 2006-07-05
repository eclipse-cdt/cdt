/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryModule;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContributedCElement;
import org.eclipse.cdt.core.model.IDeclaration;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITemplate;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.ui.CElementImageDescriptor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.IWorkbenchAdapter;


/**
 * Default strategy of the C plugin for the construction of C element icons.
 */
public class CElementImageProvider {
	
	/**
	 * Flags for the CImageLabelProvider:
	 * Generate images with overlays.
	 */
	public final static int OVERLAY_ICONS= 0x1;

	/**
	 * Generate small sized images.
	 */
	public final static int SMALL_ICONS= 0x2;

	/**
	 * Use the 'light' style for rendering types.
	 */	
	public final static int LIGHT_TYPE_ICONS= 0x4;
	
	/**
	 * Show error overrlay. 
	 */	
	public final static int OVERLAY_ERROR= 0x8;

	/**
	 * Show warning overrlay
	 */	
	public final static int OVERLAY_WARNING= 0x10;
	
	/**
	 * Show override overrlay. 
	 */	
	public final static int OVERLAY_OVERRIDE= 0x20;

	/**
	 * Show implements overrlay. 
	 */	
	public final static int OVERLAY_IMPLEMENTS= 0x40;
	
	public static final Point SMALL_SIZE= new Point(16, 16);
	public static final Point BIG_SIZE= new Point(22, 16);

	private static ImageDescriptor DESC_OBJ_PROJECT_CLOSED;	
	private static ImageDescriptor DESC_OBJ_PROJECT;	
	//private static ImageDescriptor DESC_OBJ_FOLDER;
	{
		ISharedImages images= CUIPlugin.getDefault().getWorkbench().getSharedImages(); 
		DESC_OBJ_PROJECT_CLOSED= images.getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED);
		DESC_OBJ_PROJECT= 		 images.getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT);
		//DESC_OBJ_FOLDER= 		 images.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
	}
	
	public CElementImageProvider() {
	}	
		
	/**
	 * Returns the icon for a given element. The icon depends on the element type
	 * and element properties. If configured, overlay icons are constructed for
	 * <code>ISourceReference</code>s.
	 * @param flags Flags as defined by the JavaImageLabelProvider
	 */
	public Image getImageLabel(Object element, int flags) {
		ImageDescriptor descriptor= null;
		if (element instanceof ICElement) {
			descriptor= getCImageDescriptor((ICElement) element, flags);
		} else if (element instanceof IFile) {
			// Check for Non Translation Unit.
			IFile file = (IFile)element;
			if (CoreModel.isValidTranslationUnitName(file.getProject(), file.getName()) ||
					CoreModel.isValidTranslationUnitName(null, file.getName())) {
				descriptor = CPluginImages.DESC_OBJS_TUNIT_RESOURCE;
				Point size= useSmallSize(flags) ? SMALL_SIZE : BIG_SIZE;
				descriptor = new CElementImageDescriptor(descriptor, 0, size);
			}
		}
		if (descriptor == null && element instanceof IAdaptable) {
			descriptor= getWorkbenchImageDescriptor((IAdaptable) element, flags);
		}
		if (descriptor != null) {
			return CUIPlugin.getImageDescriptorRegistry().get(descriptor);
		}
		return null;
	}

	public static ImageDescriptor getImageDescriptor(int type) {
		switch (type) {
			case ICElement.C_VCONTAINER:
				return CPluginImages.DESC_OBJS_CONTAINER;

			case ICElement.C_BINARY:
				return CPluginImages.DESC_OBJS_BINARY;
	
			case ICElement.C_ARCHIVE:
				return CPluginImages.DESC_OBJS_ARCHIVE;

			case ICElement.C_UNIT:
				return CPluginImages.DESC_OBJS_TUNIT;
				
			case ICElement.C_CCONTAINER:
				//return DESC_OBJ_FOLDER;
				return CPluginImages.DESC_OBJS_CFOLDER;
			
			case ICElement.C_PROJECT:
				return DESC_OBJ_PROJECT;
					
			case ICElement.C_STRUCT:
			case ICElement.C_TEMPLATE_STRUCT:
				return CPluginImages.DESC_OBJS_STRUCT;
				
			case ICElement.C_CLASS:
			case ICElement.C_TEMPLATE_CLASS:
				return CPluginImages.DESC_OBJS_CLASS;

			case ICElement.C_UNION:
			case ICElement.C_TEMPLATE_UNION:
				return CPluginImages.DESC_OBJS_UNION;

			case ICElement.C_TYPEDEF:
				return CPluginImages.DESC_OBJS_TYPEDEF;

			case ICElement.C_ENUMERATION:
				return CPluginImages.DESC_OBJS_ENUMERATION;

			case ICElement.C_ENUMERATOR:
				return CPluginImages.DESC_OBJS_ENUMERATOR;

			case ICElement.C_FIELD:
				return CPluginImages.DESC_OBJS_PUBLIC_FIELD;
			
			case ICElement.C_VARIABLE:
			case ICElement.C_TEMPLATE_VARIABLE:
				return CPluginImages.DESC_OBJS_VARIABLE;

			case ICElement.C_METHOD:  
			case ICElement.C_METHOD_DECLARATION:
			case ICElement.C_TEMPLATE_METHOD:
			case ICElement.C_TEMPLATE_METHOD_DECLARATION:
				return CPluginImages.DESC_OBJS_PUBLIC_METHOD;
				
			case ICElement.C_FUNCTION:
			case ICElement.C_TEMPLATE_FUNCTION:
				return CPluginImages.DESC_OBJS_FUNCTION;

			case ICElement.C_STRUCT_DECLARATION:
			case ICElement.C_CLASS_DECLARATION:
			case ICElement.C_UNION_DECLARATION:
			case ICElement.C_VARIABLE_DECLARATION:
			case ICElement.C_TEMPLATE_CLASS_DECLARATION:
			case ICElement.C_TEMPLATE_STRUCT_DECLARATION:
			case ICElement.C_TEMPLATE_UNION_DECLARATION:
				return CPluginImages.DESC_OBJS_VAR_DECLARARION;
			
			case ICElement.C_FUNCTION_DECLARATION:
			case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
				return CPluginImages.DESC_OBJS_DECLARARION;

			case ICElement.C_INCLUDE:
				return CPluginImages.DESC_OBJS_INCLUDE;

			case ICElement.C_MACRO:
				return CPluginImages.DESC_OBJS_MACRO;
				
			case ICElement.C_NAMESPACE:
				return CPluginImages.DESC_OBJS_NAMESPACE;

			case ICElement.C_USING:
				return CPluginImages.DESC_OBJS_USING;
		}
		return null;
	}

	private boolean showOverlayIcons(int flags) {
		return (flags & OVERLAY_ICONS) != 0;
	}
	
//	private boolean useLightIcons(int flags) {
//		return (flags & LIGHT_TYPE_ICONS) != 0;
//	}
	
	private boolean useSmallSize(int flags) {
		return (flags & SMALL_ICONS) != 0;
	}
	
	/**
	 * Returns an image descriptor for a C element. The descriptor includes overlays, if specified.
	 */
	public ImageDescriptor getCImageDescriptor(ICElement element, int flags) {
		int adornmentFlags= computeCAdornmentFlags(element, flags);
		Point size= useSmallSize(flags) ? SMALL_SIZE : BIG_SIZE;
		ImageDescriptor desc = getBaseImageDescriptor(element, flags);
		if(desc != null) {
			return new CElementImageDescriptor(desc, adornmentFlags, size);
		}
		return null;
	}

	/**
	 * Returns an image descriptor for a IAdaptable. The descriptor includes overlays, if specified (only error ticks apply).
	 * Returns <code>null</code> if no image could be found.
	 */	
	public ImageDescriptor getWorkbenchImageDescriptor(IAdaptable adaptable, int flags) {
		IWorkbenchAdapter wbAdapter= (IWorkbenchAdapter) adaptable.getAdapter(IWorkbenchAdapter.class);
		if (wbAdapter == null) {
			return null;
		}
		ImageDescriptor descriptor= wbAdapter.getImageDescriptor(adaptable);
		if (descriptor == null) {
			return null;
		}
		int adornmentFlags= computeBasicAdornmentFlags(adaptable, flags);
		Point size= useSmallSize(flags) ? SMALL_SIZE : BIG_SIZE;
		return new CElementImageDescriptor(descriptor, adornmentFlags, size);
	}
	
	// ---- Computation of base image key -------------------------------------------------
	
	/**
	 * Returns an image descriptor for a C element. This is the base image, no overlays.
	 */
	public ImageDescriptor getBaseImageDescriptor(ICElement celement, int renderFlags) {
		// Allow contributed languages to provide icons for their extensions to the ICElement hierarchy
		if (celement instanceof IContributedCElement)
		    return (ImageDescriptor)((IContributedCElement)celement).getAdapter(ImageDescriptor.class);
		
		int type = celement.getElementType();
		switch (type) {
			case ICElement.C_VCONTAINER:
				if (celement instanceof IBinaryModule) {
					return CPluginImages.DESC_OBJS_BINARY;
				} else if (celement instanceof ILibraryReference) {
					return CPluginImages.DESC_OBJS_UNKNOWN;
				} else if (celement instanceof IIncludeReference) {
					return CPluginImages.DESC_OBJS_INCLUDES_FOLDER;
				}
				return CPluginImages.DESC_OBJS_CONTAINER;

			case ICElement.C_BINARY: {
				IBinary bin = (IBinary)celement;
				if (bin.isExecutable()) {
					if (bin.hasDebug())
						return CPluginImages.DESC_OBJS_CEXEC_DEBUG;
					return CPluginImages.DESC_OBJS_CEXEC;
				} else if (bin.isSharedLib()) {
					return CPluginImages.DESC_OBJS_SHLIB;
				} else if (bin.isCore()) {
					return CPluginImages.DESC_OBJS_CORE;
				}
				return CPluginImages.DESC_OBJS_BINARY;
			}
	
			case ICElement.C_ARCHIVE:
				return CPluginImages.DESC_OBJS_ARCHIVE;

			case ICElement.C_UNIT: {
				ITranslationUnit unit = (ITranslationUnit)celement;
				if (unit.isHeaderUnit()) {
					return CPluginImages.DESC_OBJS_TUNIT_HEADER;
				} else if (unit.isSourceUnit()) {
					if (unit.isASMLanguage()) {
						return CPluginImages.DESC_OBJS_TUNIT_ASM;
					}
				}
				return CPluginImages.DESC_OBJS_TUNIT;
			}
				
			case ICElement.C_CCONTAINER:
				if (celement instanceof ISourceRoot) {
					return CPluginImages.DESC_OBJS_SOURCE_ROOT;
				}
				//return DESC_OBJ_FOLDER;
				return CPluginImages.DESC_OBJS_CFOLDER;
			
			case ICElement.C_PROJECT:
				ICProject cp= (ICProject)celement;
				if (cp.getProject().isOpen()) {
					IProject project= cp.getProject();
					IWorkbenchAdapter adapter= (IWorkbenchAdapter)project.getAdapter(IWorkbenchAdapter.class);
					if (adapter != null) {
						ImageDescriptor result= adapter.getImageDescriptor(project);
						if (result != null)
							return result;
					}
					return DESC_OBJ_PROJECT;
				}
				return DESC_OBJ_PROJECT_CLOSED;

			case ICElement.C_STRUCT:
			case ICElement.C_TEMPLATE_STRUCT:
				return getStructImageDescriptor();
				
			case ICElement.C_CLASS:
			case ICElement.C_TEMPLATE_CLASS:
				return getClassImageDescriptor();
				
			case ICElement.C_UNION:
			case ICElement.C_TEMPLATE_UNION:
				return getUnionImageDescriptor();

			case ICElement.C_TYPEDEF:
				return getTypedefImageDescriptor();

			case ICElement.C_ENUMERATION:
				return getEnumerationImageDescriptor();

			case ICElement.C_ENUMERATOR:
				return getEnumeratorImageDescriptor();

			case ICElement.C_FIELD:
				try {
					IField  field = (IField)celement;
					ASTAccessVisibility visibility = field.getVisibility();
					return getFieldImageDescriptor(visibility);
				} catch (CModelException e) {
					return null;
				}
			
			case ICElement.C_METHOD:  
			case ICElement.C_METHOD_DECLARATION:
			case ICElement.C_TEMPLATE_METHOD:
			case ICElement.C_TEMPLATE_METHOD_DECLARATION:
				try {
					
					IMethodDeclaration  md= (IMethodDeclaration)celement;
					ASTAccessVisibility visibility =md.getVisibility();
					return getMethodImageDescriptor(visibility); 
				} catch (CModelException e) {
					return null;
				}
			case ICElement.C_VARIABLE:
			case ICElement.C_TEMPLATE_VARIABLE:
				return getVariableImageDescriptor();
				
			case ICElement.C_FUNCTION:
			case ICElement.C_TEMPLATE_FUNCTION:
				return getFunctionImageDescriptor();

			case ICElement.C_STRUCT_DECLARATION:
			case ICElement.C_CLASS_DECLARATION:
			case ICElement.C_UNION_DECLARATION:
			case ICElement.C_VARIABLE_DECLARATION:
			case ICElement.C_TEMPLATE_CLASS_DECLARATION:
			case ICElement.C_TEMPLATE_UNION_DECLARATION:
			case ICElement.C_TEMPLATE_STRUCT_DECLARATION:
				return getVariableDeclarationImageDescriptor();
			
			case ICElement.C_FUNCTION_DECLARATION:
			case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
				return getFunctionDeclarationImageDescriptor();

			case ICElement.C_INCLUDE:
				return getIncludeImageDescriptor();

			case ICElement.C_MACRO:
				return getMacroImageDescriptor();
				
			case ICElement.C_NAMESPACE:
				return getNamespaceImageDescriptor();

			case ICElement.C_USING:
				return getUsingImageDescriptor();

		}
		return null;
	}	


	// ---- Methods to compute the adornments flags ---------------------------------
	
	private int computeCAdornmentFlags(ICElement element, int renderFlags) {
		
		int flags= computeBasicAdornmentFlags(element, renderFlags);

		try {
			if (showOverlayIcons(renderFlags) && element instanceof IDeclaration) {
				IDeclaration decl = (IDeclaration) element;
				if(decl.isStatic()){
					flags |= CElementImageDescriptor.STATIC;
				}
				if(decl.isConst()){
					flags |= CElementImageDescriptor.CONSTANT;
				}
				if(decl.isVolatile()){
					flags |= CElementImageDescriptor.VOLATILE;
				}
				if(element instanceof ITemplate){
					flags |= CElementImageDescriptor.TEMPLATE;
				}
			}
		} catch (CModelException e) {
		}
		return flags;
	}
	
	private int computeBasicAdornmentFlags(Object element, int renderFlags) {
		int flags= 0;
		if ((renderFlags & OVERLAY_ERROR) !=0) {
			flags |= CElementImageDescriptor.ERROR;
		}
		if ((renderFlags & OVERLAY_WARNING) !=0) {
			flags |= CElementImageDescriptor.WARNING;
		}		
		if ((renderFlags & OVERLAY_OVERRIDE) !=0) {
			flags |= CElementImageDescriptor.OVERRIDES;
		}
		if ((renderFlags & OVERLAY_IMPLEMENTS) !=0) {
			flags |= CElementImageDescriptor.IMPLEMENTS;
		}
		return flags;			
	}	
	
	public void dispose() {
	}
	
	public static ImageDescriptor getStructImageDescriptor(){
		return CPluginImages.DESC_OBJS_STRUCT;	
	}
	
	public static ImageDescriptor getClassImageDescriptor(){
		return CPluginImages.DESC_OBJS_CLASS;	
	}
	
	public static ImageDescriptor getUnionImageDescriptor(){
		return CPluginImages.DESC_OBJS_UNION;	
	}
	
	public static ImageDescriptor getTypedefImageDescriptor(){
		return CPluginImages.DESC_OBJS_TYPEDEF;	
	}
	
	public static ImageDescriptor getEnumerationImageDescriptor(){
		return CPluginImages.DESC_OBJS_ENUMERATION;	
	}
	
	public static ImageDescriptor getEnumeratorImageDescriptor(){
		return CPluginImages.DESC_OBJS_ENUMERATOR;	
	}

	public static ImageDescriptor getFieldImageDescriptor(ASTAccessVisibility visibility) {
		if (visibility == ASTAccessVisibility.PUBLIC)
			return CPluginImages.DESC_OBJS_PUBLIC_FIELD;
		if( visibility == ASTAccessVisibility.PROTECTED)
			return CPluginImages.DESC_OBJS_PROTECTED_FIELD;
		
		return CPluginImages.DESC_OBJS_PRIVATE_FIELD;			
	}

	public static ImageDescriptor getMethodImageDescriptor(ASTAccessVisibility visibility) {
		if( visibility == ASTAccessVisibility.PUBLIC)
			return CPluginImages.DESC_OBJS_PUBLIC_METHOD;
		if( visibility == ASTAccessVisibility.PROTECTED)
			return CPluginImages.DESC_OBJS_PROTECTED_METHOD;
		
		return CPluginImages.DESC_OBJS_PRIVATE_METHOD;				
	}

	public static ImageDescriptor getVariableImageDescriptor(){
		return getImageDescriptor(ICElement.C_VARIABLE);
	}

	public static ImageDescriptor getLocalVariableImageDescriptor(){
		return CPluginImages.DESC_OBJS_LOCAL_VARIABLE;	
	}
	
	public static ImageDescriptor getFunctionImageDescriptor(){
		return getImageDescriptor(ICElement.C_FUNCTION);
	}

	public static ImageDescriptor getVariableDeclarationImageDescriptor(){
		return getImageDescriptor(ICElement.C_VARIABLE_DECLARATION);
	}

	public static ImageDescriptor getFunctionDeclarationImageDescriptor(){
		return getImageDescriptor(ICElement.C_FUNCTION_DECLARATION);
	}

	public static ImageDescriptor getIncludeImageDescriptor(){
		return getImageDescriptor(ICElement.C_INCLUDE);
	}

	public static ImageDescriptor getMacroImageDescriptor(){
		return getImageDescriptor(ICElement.C_MACRO);
	}

	public static ImageDescriptor getNamespaceImageDescriptor(){
		return getImageDescriptor(ICElement.C_NAMESPACE);
	}

	public static ImageDescriptor getUsingImageDescriptor(){
		return getImageDescriptor(ICElement.C_USING);
	}

	public static ImageDescriptor getKeywordImageDescriptor(){
		return CPluginImages.DESC_OBJS_KEYWORD;
	}

}

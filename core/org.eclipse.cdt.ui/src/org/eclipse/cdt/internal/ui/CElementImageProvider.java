package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.internal.ui.util.ImageDescriptorRegistry;
import org.eclipse.cdt.ui.CElementImageDescriptor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.ISharedImages;
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
	
	private static final Point SMALL_SIZE= new Point(16, 16);
	private static final Point BIG_SIZE= new Point(22, 16);

	private static ImageDescriptor DESC_OBJ_PROJECT_CLOSED;	
	private static ImageDescriptor DESC_OBJ_PROJECT;	
	private static ImageDescriptor DESC_OBJ_FOLDER;
	{
		ISharedImages images= CUIPlugin.getDefault().getWorkbench().getSharedImages(); 
		DESC_OBJ_PROJECT_CLOSED= images.getImageDescriptor(ISharedImages.IMG_OBJ_PROJECT_CLOSED);
		DESC_OBJ_PROJECT= 		 images.getImageDescriptor(ISharedImages.IMG_OBJ_PROJECT);
		DESC_OBJ_FOLDER= 		 images.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
	}
	
	private ImageDescriptorRegistry fRegistry;
		
	public CElementImageProvider() {
		fRegistry= CUIPlugin.getImageDescriptorRegistry();
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
		}
//		if (descriptor == null && element instanceof ICFile) {
//			element = ((ICFile)element).getFile();
//		}
		if (descriptor == null && element instanceof IAdaptable) {
			descriptor= getWorkbenchImageDescriptor((IAdaptable) element, flags);
		}
		if (descriptor != null) {
			return fRegistry.get(descriptor);
		}
		return null;
	}
	
	private boolean showOverlayIcons(int flags) {
		return (flags & OVERLAY_ICONS) != 0;
	}
	
	private boolean useLightIcons(int flags) {
		return (flags & LIGHT_TYPE_ICONS) != 0;
	}
	
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
		int type = celement.getElementType();
		switch (type) {
			case ICElement.C_VCONTAINER:
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

			case ICElement.C_UNIT:
				return CPluginImages.DESC_OBJS_TUNIT;
				
			case ICElement.C_CCONTAINER:
				return DESC_OBJ_FOLDER;
			
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
				return CPluginImages.DESC_OBJS_STRUCT;
				
			case ICElement.C_CLASS:
				return CPluginImages.DESC_OBJS_CLASS;
				
			case ICElement.C_UNION:
				return CPluginImages.DESC_OBJS_UNION;

			case ICElement.C_FIELD:
			case ICElement.C_VARIABLE:
				return CPluginImages.DESC_OBJS_FIELD;

			case ICElement.C_METHOD:  
			case ICElement.C_METHOD_DECLARATION:
				IMethodDeclaration  md= (IMethodDeclaration)celement;
				switch(md.getVisibility()){
					case IMember.V_PUBLIC:
						return CPluginImages.DESC_OBJS_PUBLIC_METHOD;
					case IMember.V_PROTECTED:
						return CPluginImages.DESC_OBJS_PROTECTED_METHOD;
					case IMember.V_PRIVATE:
						return CPluginImages.DESC_OBJS_PRIVATE_METHOD;
				}
			case ICElement.C_FUNCTION:
				return CPluginImages.DESC_OBJS_FUNCTION;
		
			case ICElement.C_FUNCTION_DECLARATION:
				return CPluginImages.DESC_OBJS_DECLARARION;

			case ICElement.C_INCLUDE:
				return CPluginImages.DESC_OBJS_INCLUDE;

			case ICElement.C_MACRO:
				return CPluginImages.DESC_OBJS_MACRO;
		}
		return null;
	}
	
	public ImageDescriptor getCElementImageDescriptor(int type) {
		switch (type) {
			case ICElement.C_VCONTAINER:
				return CPluginImages.DESC_OBJS_CONTAINER;

			case ICElement.C_UNIT:
				return CPluginImages.DESC_OBJS_TUNIT;

			case ICElement.C_STRUCT:
				return CPluginImages.DESC_OBJS_STRUCT;
				
			case ICElement.C_CLASS:
				return CPluginImages.DESC_OBJS_CLASS;
				
			case ICElement.C_UNION:
				return CPluginImages.DESC_OBJS_UNION;

			case ICElement.C_FIELD:
			case ICElement.C_VARIABLE:
				return CPluginImages.DESC_OBJS_FIELD;

			case ICElement.C_METHOD: // assumed public
				return CPluginImages.DESC_OBJS_PUBLIC_METHOD;
				
			case ICElement.C_FUNCTION:
			case ICElement.C_FUNCTION_DECLARATION:
				return CPluginImages.DESC_OBJS_FUNCTION;

			case ICElement.C_INCLUDE:
				return CPluginImages.DESC_OBJS_INCLUDE;

			case ICElement.C_MACRO:
				return CPluginImages.DESC_OBJS_MACRO;
		}
		System.out.println("Unknown base object ype " + type);
		return CPluginImages.DESC_OBJS_MACRO;
		//return null;
	}


	// ---- Methods to compute the adornments flags ---------------------------------
	
	private int computeCAdornmentFlags(ICElement element, int renderFlags) {
		
		int flags= computeBasicAdornmentFlags(element, renderFlags);
		
		/* if (showOverlayIcons(renderFlags) && element instanceof ISourceReference) { 
			ISourceReference sourceReference= (ISourceReference)element;
			int modifiers= getModifiers(sourceReference);
		
			if (Flags.isAbstract(modifiers) && confirmAbstract((IMember) sourceReference))
				flags |= JavaElementImageDescriptor.ABSTRACT;
			if (Flags.isFinal(modifiers))
				flags |= JavaElementImageDescriptor.FINAL;
			if (Flags.isSynchronized(modifiers) && confirmSynchronized((IMember) sourceReference))
				flags |= JavaElementImageDescriptor.SYNCHRONIZED;
			if (Flags.isStatic(modifiers))
				flags |= JavaElementImageDescriptor.STATIC;
				
			if (sourceReference instanceof IType) {
				try {
					if (JavaModelUtil.hasMainMethod((IType)sourceReference))
						flags |= JavaElementImageDescriptor.RUNNABLE;
				} catch (JavaModelException e) {
					// do nothing. Can't compute runnable adornment.
				}
			}
		} */
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
	
}


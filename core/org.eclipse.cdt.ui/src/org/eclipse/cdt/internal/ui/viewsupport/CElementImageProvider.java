/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *     QNX Software System
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.IBinaryModule;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContributedCElement;
import org.eclipse.cdt.core.model.IDeclaration;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.IPragma;
import org.eclipse.cdt.core.model.IPragma.PragmaMarkInfo;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITemplate;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CElementImageDescriptor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Default strategy of the C plugin for the construction of C element icons.
 */
public class CElementImageProvider {

	/**
	 * Flags for the CElementImageProvider:
	 * Generate images with overlays.
	 */
	public final static int OVERLAY_ICONS = 0x1;

	/**
	 * Generate small sized images.
	 */
	public final static int SMALL_ICONS = 0x2;

	/**
	 * Use the 'light' style for rendering types.
	 */
	public final static int LIGHT_TYPE_ICONS = 0x4;

	/**
	 * Show error overlay.
	 */
	public final static int OVERLAY_ERROR = 0x8;

	/**
	 * Show warning overlay
	 */
	public final static int OVERLAY_WARNING = 0x10;

	/**
	 * Show override overlay.
	 */
	public final static int OVERLAY_OVERRIDE = 0x20;

	/**
	 * Show implements overlay.
	 */
	public final static int OVERLAY_IMPLEMENTS = 0x40;

	/**
	 * Show external file overlay.
	 */
	public final static int OVERLAY_EXTERNAL = 0x80;

	public static final Point SMALL_SIZE = new Point(16, 16);
	public static final Point BIG_SIZE = new Point(22, 16);

	private static ImageDescriptor DESC_OBJ_PROJECT_CLOSED;
	private static ImageDescriptor DESC_OBJ_PROJECT;
	//private static ImageDescriptor DESC_OBJ_FOLDER;
	{
		ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
		DESC_OBJ_PROJECT_CLOSED = images.getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED);
		DESC_OBJ_PROJECT = images.getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT);
		//DESC_OBJ_FOLDER= 		 images.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
	}

	/**
	 * Map of a descriptor back to its canonical one. This is needed to work around a bug in
	 * the Eclipse platform, see Bug 563454
	 */
	private final Map<CElementImageDescriptor, CElementImageDescriptor> allDescriptors = new HashMap<>();

	public CElementImageProvider() {
	}

	/**
	 * Returns the icon for a given element. The icon depends on the element type
	 * and element properties. If configured, overlay icons are constructed for
	 * <code>ISourceReference</code>s.
	 * @param flags Flags as defined by the CElementImageProvider
	 */
	public Image getImageLabel(Object element, int flags) {
		ImageDescriptor descriptor = null;
		if (element instanceof ICElement) {
			if (!CCorePlugin.showSourceRootsAtTopOfProject() && element instanceof ICContainer
					&& isParentOfSourceRoot(element)) {

				descriptor = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_SOURCE2_ROOT);
			} else {
				descriptor = getCImageDescriptor((ICElement) element, flags);
			}
		} else if (element instanceof IFile) {
			// Check for Non Translation Unit.
			IFile file = (IFile) element;
			String name = file.getName();
			if (CoreModel.isValidTranslationUnitName(file.getProject(), name)
					|| CoreModel.isValidTranslationUnitName(null, name)) {
				if (CoreModel.isValidCHeaderUnitName(null, name) || CoreModel.isValidCXXHeaderUnitName(null, name))
					descriptor = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_TUNIT_RESOURCE_H);
				else if (CoreModel.isValidASMSourceUnitName(null, name))
					descriptor = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_TUNIT_RESOURCE_A);
				else
					descriptor = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_TUNIT_RESOURCE);

				Point size = useSmallSize(flags) ? SMALL_SIZE : BIG_SIZE;
				descriptor = new CElementImageDescriptor(descriptor, 0, size);
			}
		} else if (!CCorePlugin.showSourceRootsAtTopOfProject() && element instanceof IFolder
				&& isParentOfSourceRoot(element)) {
			descriptor = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_SOURCE2_ROOT);
		}
		if (descriptor == null && element instanceof IAdaptable) {
			descriptor = getWorkbenchImageDescriptor((IAdaptable) element, flags);
		}
		if (descriptor != null) {
			return CUIPlugin.getImageDescriptorRegistry().get(descriptor);
		}
		return null;
	}

	private boolean isParentOfSourceRoot(Object element) {
		// we want to return true for parents of source roots which are not themselves source roots
		// so we can distinguish the two and return the source root icon or the parent of source root icon
		IFolder folder = null;
		if (element instanceof ICContainer && !(element instanceof ISourceRoot))
			folder = (IFolder) ((ICContainer) element).getResource();
		else if (element instanceof IFolder)
			folder = (IFolder) element;
		if (folder == null)
			return false;

		ICProject cproject = CModelManager.getDefault().getCModel().findCProject(folder.getProject());
		if (cproject != null) {
			try {
				IPath folderPath = folder.getFullPath();
				for (ICElement sourceRoot : cproject.getSourceRoots()) {
					IPath sourceRootPath = sourceRoot.getPath();
					if (folderPath.isPrefixOf(sourceRootPath)) {
						return true;
					}
				}
			} catch (CModelException e) {
			}
		}

		return false;
	}

	public static ImageDescriptor getImageDescriptor(int type) {
		switch (type) {
		case ICElement.C_VCONTAINER:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_CONTAINER);

		case ICElement.C_BINARY:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_BINARY);

		case ICElement.C_ARCHIVE:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_ARCHIVE);

		case ICElement.C_UNIT:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_TUNIT);

		case ICElement.C_CCONTAINER:
			//return DESC_OBJ_FOLDER;
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_CFOLDER);

		case ICElement.C_PROJECT:
			return DESC_OBJ_PROJECT;

		case ICElement.C_STRUCT:
		case ICElement.C_TEMPLATE_STRUCT:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_STRUCT);

		case ICElement.C_CLASS:
		case ICElement.C_TEMPLATE_CLASS:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_CLASS);

		case ICElement.C_UNION:
		case ICElement.C_TEMPLATE_UNION:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_UNION);

		case ICElement.C_TYPEDEF:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_TYPEDEF);

		case ICElement.C_ENUMERATION:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_ENUMERATION);

		case ICElement.C_ENUMERATOR:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_ENUMERATOR);

		case ICElement.C_FIELD:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_PUBLIC_FIELD);

		case ICElement.C_VARIABLE:
		case ICElement.C_TEMPLATE_VARIABLE:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_VARIABLE);

		case ICElement.C_METHOD:
		case ICElement.C_METHOD_DECLARATION:
		case ICElement.C_TEMPLATE_METHOD:
		case ICElement.C_TEMPLATE_METHOD_DECLARATION:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_PUBLIC_METHOD);

		case ICElement.C_FUNCTION:
		case ICElement.C_TEMPLATE_FUNCTION:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_FUNCTION);

		case ICElement.C_STRUCT_DECLARATION:
		case ICElement.C_CLASS_DECLARATION:
		case ICElement.C_UNION_DECLARATION:
		case ICElement.C_VARIABLE_DECLARATION:
		case ICElement.C_TEMPLATE_CLASS_DECLARATION:
		case ICElement.C_TEMPLATE_STRUCT_DECLARATION:
		case ICElement.C_TEMPLATE_UNION_DECLARATION:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_VAR_DECLARATION);

		case ICElement.C_FUNCTION_DECLARATION:
		case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_DECLARATION);

		case ICElement.C_INCLUDE:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_INCLUDE);

		case ICElement.C_MACRO:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_MACRO);

		case ICElement.C_NAMESPACE:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_NAMESPACE);

		case ICElement.C_USING:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_USING);

		case ICElement.ASM_LABEL:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_LABEL);
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
		int adornmentFlags = computeCAdornmentFlags(element, flags);
		Point size = useSmallSize(flags) ? SMALL_SIZE : BIG_SIZE;
		ImageDescriptor desc = getBaseImageDescriptor(element, flags);
		if (desc != null) {
			CElementImageDescriptor descriptor = new CElementImageDescriptor(desc, adornmentFlags, size);
			descriptor = allDescriptors.computeIfAbsent(descriptor, (k) -> k);
			return descriptor;
		}
		return null;
	}

	/**
	 * Returns an image descriptor for a IAdaptable. The descriptor includes overlays, if specified (only error ticks apply).
	 * Returns <code>null</code> if no image could be found.
	 */
	public ImageDescriptor getWorkbenchImageDescriptor(IAdaptable adaptable, int flags) {
		IWorkbenchAdapter wbAdapter = adaptable.getAdapter(IWorkbenchAdapter.class);
		if (wbAdapter == null) {
			return null;
		}
		ImageDescriptor descriptor = wbAdapter.getImageDescriptor(adaptable);
		if (descriptor == null) {
			return null;
		}
		int adornmentFlags = computeBasicAdornmentFlags(adaptable, flags);
		Point size = useSmallSize(flags) ? SMALL_SIZE : BIG_SIZE;
		return new CElementImageDescriptor(descriptor, adornmentFlags, size);
	}

	// ---- Computation of base image key -------------------------------------------------

	/**
	 * Returns an image descriptor for a C element. This is the base image, no overlays.
	 */
	public ImageDescriptor getBaseImageDescriptor(ICElement celement, int renderFlags) {
		// Allow contributed languages to provide icons for their extensions to the ICElement hierarchy
		if (celement instanceof IContributedCElement)
			return ((IContributedCElement) celement).getAdapter(ImageDescriptor.class);

		int type = celement.getElementType();
		switch (type) {
		case ICElement.C_VCONTAINER:
			if (celement instanceof IBinaryModule) {
				return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_BINARY);
			} else if (celement instanceof ILibraryReference) {
				return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_UNKNOWN);
			} else if (celement instanceof IIncludeReference) {
				return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_INCLUDES_FOLDER);
			} else if (celement instanceof IArchiveContainer) {
				return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_ARCHIVES_CONTAINER);
			} else if (celement instanceof IBinaryContainer) {
				return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_BINARIES_CONTAINER);
			}
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_CONTAINER);

		case ICElement.C_BINARY: {
			IBinary bin = (IBinary) celement;
			if (bin.isExecutable()) {
				if (bin.hasDebug())
					return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_CEXEC_DEBUG);
				return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_CEXEC);
			} else if (bin.isSharedLib()) {
				return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_SHLIB);
			} else if (bin.isCore()) {
				return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_CORE);
			}
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_BINARY);
		}

		case ICElement.C_ARCHIVE:
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_ARCHIVE);

		case ICElement.C_UNIT: {
			ITranslationUnit unit = (ITranslationUnit) celement;
			if (unit.isHeaderUnit()) {
				return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_TUNIT_HEADER);
			} else if (unit.isSourceUnit()) {
				if (unit.isASMLanguage()) {
					return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_TUNIT_ASM);
				}
			}
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_TUNIT);
		}

		case ICElement.C_CCONTAINER:
			if (celement instanceof ISourceRoot) {
				return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_SOURCE_ROOT);
			}
			//return DESC_OBJ_FOLDER;
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_CFOLDER);

		case ICElement.C_PROJECT:
			ICProject cp = (ICProject) celement;
			if (cp.getProject().isOpen()) {
				IProject project = cp.getProject();
				IWorkbenchAdapter adapter = project.getAdapter(IWorkbenchAdapter.class);
				if (adapter != null) {
					ImageDescriptor result = adapter.getImageDescriptor(project);
					if (result != null)
						return result;
				}
				return DESC_OBJ_PROJECT;
			}
			return DESC_OBJ_PROJECT_CLOSED;

		case ICElement.C_STRUCT:
		case ICElement.C_TEMPLATE_STRUCT:
			return getStructImageDescriptor((renderFlags & LIGHT_TYPE_ICONS) != 0);

		case ICElement.C_CLASS:
		case ICElement.C_TEMPLATE_CLASS:
			return getClassImageDescriptor((renderFlags & LIGHT_TYPE_ICONS) != 0);

		case ICElement.C_UNION:
		case ICElement.C_TEMPLATE_UNION:
			return getUnionImageDescriptor((renderFlags & LIGHT_TYPE_ICONS) != 0);

		case ICElement.C_TYPEDEF:
			return getTypedefImageDescriptor((renderFlags & LIGHT_TYPE_ICONS) != 0);

		case ICElement.C_ENUMERATION:
			return getEnumerationImageDescriptor((renderFlags & LIGHT_TYPE_ICONS) != 0);

		case ICElement.C_ENUMERATOR:
			return getEnumeratorImageDescriptor();

		case ICElement.C_FIELD:
			try {
				IField field = (IField) celement;
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
				IMethodDeclaration md = (IMethodDeclaration) celement;
				ASTAccessVisibility visibility = md.getVisibility();
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

		case ICElement.C_PRAGMA:
			IPragma pragma = (IPragma) celement;
			Optional<PragmaMarkInfo> pragmaMarkInfo = pragma.getPragmaMarkInfo();
			if (pragmaMarkInfo.isPresent()) {
				return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OUTLINE_MARK);
			} else {
				return null;
			}

		default:
			return getImageDescriptor(type);
		}
	}

	// ---- Methods to compute the adornments flags ---------------------------------

	private int computeCAdornmentFlags(ICElement element, int renderFlags) {

		int flags = computeBasicAdornmentFlags(element, renderFlags);
		if (showOverlayIcons(renderFlags)) {
			try {
				if (element instanceof IDeclaration) {
					IDeclaration decl = (IDeclaration) element;
					if (decl.isStatic()) {
						flags |= CElementImageDescriptor.STATIC;
					}
					if (decl.isConst()) {
						flags |= CElementImageDescriptor.CONSTANT;
					}
					if (decl.isVolatile()) {
						flags |= CElementImageDescriptor.VOLATILE;
					}
					if (element instanceof ITemplate) {
						flags |= CElementImageDescriptor.TEMPLATE;
					}
				}
				if (element instanceof ISourceReference) {
					ISourceReference sref = (ISourceReference) element;
					if (!sref.isActive()) {
						flags |= CElementImageDescriptor.INACTIVE;
					} else {
						if (element instanceof IInclude) {
							IInclude include = (IInclude) element;
							if (!include.isResolved()) {
								flags |= CElementImageDescriptor.WARNING;
							}
						}
					}
				}
			} catch (CModelException e) {
			}
		}
		return flags;
	}

	private int computeBasicAdornmentFlags(Object element, int renderFlags) {
		int flags = 0;
		if ((renderFlags & OVERLAY_ERROR) != 0) {
			flags |= CElementImageDescriptor.ERROR;
		}
		if ((renderFlags & OVERLAY_WARNING) != 0) {
			flags |= CElementImageDescriptor.WARNING;
		}
		//		if ((renderFlags & OVERLAY_OVERRIDE) !=0) {
		//			flags |= CElementImageDescriptor.OVERRIDES;
		//		}
		//		if ((renderFlags & OVERLAY_IMPLEMENTS) !=0) {
		//			flags |= CElementImageDescriptor.IMPLEMENTS;
		//		}
		if ((renderFlags & OVERLAY_EXTERNAL) != 0) {
			flags |= CElementImageDescriptor.EXTERNAL_FILE;
		}
		return flags;
	}

	public void dispose() {
	}

	public static ImageDescriptor getStructImageDescriptor() {
		return getStructImageDescriptor(false);
	}

	public static ImageDescriptor getStructImageDescriptor(boolean alt) {
		return alt ? CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_STRUCT_ALT)
				: CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_STRUCT);
	}

	public static ImageDescriptor getClassImageDescriptor() {
		return getClassImageDescriptor(false);
	}

	public static ImageDescriptor getClassImageDescriptor(boolean alt) {
		return alt ? CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_CLASS_ALT)
				: CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_CLASS);
	}

	public static ImageDescriptor getUnionImageDescriptor() {
		return getUnionImageDescriptor(false);
	}

	public static ImageDescriptor getUnionImageDescriptor(boolean alt) {
		return alt ? CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_UNION_ALT)
				: CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_UNION);
	}

	public static ImageDescriptor getTypedefImageDescriptor() {
		return getTypedefImageDescriptor(false);
	}

	public static ImageDescriptor getTypedefImageDescriptor(boolean alt) {
		return alt ? CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_TYPEDEF_ALT)
				: CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_TYPEDEF);
	}

	public static ImageDescriptor getEnumerationImageDescriptor() {
		return getEnumerationImageDescriptor(false);
	}

	public static ImageDescriptor getEnumerationImageDescriptor(boolean alt) {
		return alt ? CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_ENUMERATION_ALT)
				: CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_ENUMERATION);
	}

	public static ImageDescriptor getEnumeratorImageDescriptor() {
		return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_ENUMERATOR);
	}

	public static ImageDescriptor getFieldImageDescriptor(ASTAccessVisibility visibility) {
		if (visibility == ASTAccessVisibility.PUBLIC)
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_PUBLIC_FIELD);
		if (visibility == ASTAccessVisibility.PROTECTED)
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_PROTECTED_FIELD);

		return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_PRIVATE_FIELD);
	}

	public static ImageDescriptor getMethodImageDescriptor(ASTAccessVisibility visibility) {
		if (visibility == ASTAccessVisibility.PUBLIC)
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_PUBLIC_METHOD);
		if (visibility == ASTAccessVisibility.PROTECTED)
			return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_PROTECTED_METHOD);

		return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_PRIVATE_METHOD);
	}

	public static ImageDescriptor getVariableImageDescriptor() {
		return getImageDescriptor(ICElement.C_VARIABLE);
	}

	public static ImageDescriptor getLocalVariableImageDescriptor() {
		return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_LOCAL_VARIABLE);
	}

	public static ImageDescriptor getFunctionImageDescriptor() {
		return getImageDescriptor(ICElement.C_FUNCTION);
	}

	public static ImageDescriptor getVariableDeclarationImageDescriptor() {
		return getImageDescriptor(ICElement.C_VARIABLE_DECLARATION);
	}

	public static ImageDescriptor getFunctionDeclarationImageDescriptor() {
		return getImageDescriptor(ICElement.C_FUNCTION_DECLARATION);
	}

	public static ImageDescriptor getIncludeImageDescriptor() {
		return getImageDescriptor(ICElement.C_INCLUDE);
	}

	public static ImageDescriptor getMacroImageDescriptor() {
		return getImageDescriptor(ICElement.C_MACRO);
	}

	public static ImageDescriptor getNamespaceImageDescriptor() {
		return getImageDescriptor(ICElement.C_NAMESPACE);
	}

	public static ImageDescriptor getUsingImageDescriptor() {
		return getImageDescriptor(ICElement.C_USING);
	}

	public static ImageDescriptor getKeywordImageDescriptor() {
		return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_KEYWORD);
	}

}

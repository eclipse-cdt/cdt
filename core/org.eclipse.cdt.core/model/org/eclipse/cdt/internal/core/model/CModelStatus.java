package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @see ICModelStatus
 */

public class CModelStatus extends Status implements ICModelStatus, ICModelStatusConstants, IResourceStatus {

	/**
	 * The elements related to the failure, or <code>null</code>
	 * if no elements are involved.
	 */
	protected ICElement[] fElements;
	protected final static ICElement[] EmptyElement = new ICElement[] {};
	/**
	 * The path related to the failure, or <code>null</code>
	 * if no path is involved.
	 */
	protected IPath fPath;
	/**
	 * The <code>String</code> related to the failure, or <code>null</code>
	 * if no <code>String</code> is involved.
	 */
	protected String fString;
	/**
	 * Empty children
	 */
	protected final static IStatus[] fgEmptyChildren = new IStatus[] {};
	protected IStatus[] fChildren= fgEmptyChildren;

	/**
	 * Singleton OK object
	 */
	public static final ICModelStatus VERIFIED_OK = new CModelStatus(OK, OK, org.eclipse.cdt.internal.core.Util.bind("status.OK")); //$NON-NLS-1$

	/**
	 * Constructs an C model status with no corresponding elements.
	 */
	public CModelStatus() {
		// no code for an multi-status
		super(ERROR, CCorePlugin.PLUGIN_ID, 0, "CModelStatus", null); //$NON-NLS-1$
	}

	/**
	 * Constructs an C model status with no corresponding elements.
	 */
	public CModelStatus(int code) {
		super(ERROR, CCorePlugin.PLUGIN_ID, code, "CModelStatus", null); //$NON-NLS-1$
		fElements = CElement.NO_ELEMENTS;
	}

	/**
	 * Constructs an C model status with the given corresponding
	 * elements.
	 */
	public CModelStatus(int code, ICElement[] elements) {
		super(ERROR, CCorePlugin.PLUGIN_ID, code, "CModelStatus", null); //$NON-NLS-1$
		fElements = elements;
		fPath= null;
	}

	/**
	 * Constructs an C model status with no corresponding elements.
	 */
	public CModelStatus(int code, String string) {
		this(ERROR, code, string);
	}

	public CModelStatus(int severity, int code, String string) {
		super(severity, CCorePlugin.PLUGIN_ID, code, "CModelStatus", null); //$NON-NLS-1$
		fElements = CElement.NO_ELEMENTS;
		fPath= null;
		fString = string;
	}	

	/**
	 * Constructs an C model status with no corresponding elements.
	 */
	public CModelStatus(int code, Throwable throwable) {
		super(ERROR, CCorePlugin.PLUGIN_ID, code, "CModelStatus", throwable); //$NON-NLS-1$
		fElements = CElement.NO_ELEMENTS;
	}

	/**
	 * Constructs an C model status with no corresponding elements.
	 */
	public CModelStatus(int code, IPath path) {
		super(ERROR, CCorePlugin.PLUGIN_ID, code, "CModelStatus", null); //$NON-NLS-1$
		fElements = CElement.NO_ELEMENTS;
		fPath= path;
	}

	/**
	 * Constructs an C model status with the given corresponding
	 * element.
	 */
	public CModelStatus(int code, ICElement element) {
		this(code, new ICElement[]{element});
	}

	/**
	 * Constructs an C model status with the given corresponding
	 * element and string
	 */
	public CModelStatus(int code, ICElement element, String string) {
		this(code, new ICElement[]{element});
		fString= string;
	}

	public CModelStatus(int code, ICElement element, IPath path) {
		this(code, new ICElement[]{element});
		fPath = path;
	}	

	/**
	 * Constructs an C model status with no corresponding elements.
	 */
	public CModelStatus(CoreException coreException) {
		super(ERROR, CCorePlugin.PLUGIN_ID, CORE_EXCEPTION, "CModelStatus", coreException); //$NON-NLS-1$
		fElements= CElement.NO_ELEMENTS;
	}

	protected int getBits() {
		int severity = 1 << (getCode() % 100 / 33);
		int category = 1 << ((getCode() / 100) + 3);
		return severity | category;
	}

	/**
	 * @see IStatus
	 */
	public IStatus[] getChildren() {
		return fChildren;
	}

	/**
	 * @see ICModelStatus
	 */
	public ICElement[] getElements() {
		return fElements;
	}

	/**
	 * Returns the message that is relevant to the code of this status.
	 */
	public String getMessage() {
		Throwable exception = getException();
		if (exception == null) {
			switch (getCode()) {
				case CORE_EXCEPTION :
					return CoreModelMessages.getFormattedString("status.coreException"); //$NON-NLS-1$

				//case BUILDER_INITIALIZATION_ERROR:
				//	return Util.bind("build.initializationError"); //$NON-NLS-1$

				//case BUILDER_SERIALIZATION_ERROR:
				//	return Util.bind("build.serializationError"); //$NON-NLS-1$

				case DEVICE_PATH:
					return CoreModelMessages.getFormattedString("status.cannotUseDeviceOnPath", getPath().toString()); //$NON-NLS-1$

				//case DOM_EXCEPTION:
				//	return Util.bind("status.JDOMError"); //$NON-NLS-1$

				case ELEMENT_DOES_NOT_EXIST:
					return CoreModelMessages.getFormattedString("element.doesNotExist",((ICElement)fElements[0]).toString()); //$NON-NLS-1$

				case EVALUATION_ERROR:
					return CoreModelMessages.getFormattedString("status.evaluationError", fString); //$NON-NLS-1$

				case INDEX_OUT_OF_BOUNDS:
					return CoreModelMessages.getFormattedString("status.indexOutOfBounds"); //$NON-NLS-1$

				case INVALID_CONTENTS:
					return CoreModelMessages.getFormattedString("status.invalidContents"); //$NON-NLS-1$

				case INVALID_DESTINATION:
					return CoreModelMessages.getFormattedString("status.invalidDestination", ((ICElement)fElements[0]).toString()); //$NON-NLS-1$

				case INVALID_ELEMENT_TYPES:
					StringBuffer buff= new StringBuffer(CoreModelMessages.getFormattedString("operation.notSupported")); //$NON-NLS-1$
					for (int i= 0; i < fElements.length; i++) {
						if (i > 0) {
							buff.append(", "); //$NON-NLS-1$
						}
						buff.append(((ICElement)fElements[i]).toString());
					}
					return buff.toString();

				case INVALID_NAME:
					return CoreModelMessages.getFormattedString("status.invalidName", fString); //$NON-NLS-1$

				//case INVALID_PACKAGE:
				//	return Util.bind("status.invalidPackage", string); //$NON-NLS-1$

				case INVALID_PATH:
					if (fString != null) {
						return fString;
					} else {
						return CoreModelMessages.getFormattedString("status.invalidPath", getPath() == null ? "null" : getPath().toString()); //$NON-NLS-1$ //$NON-NLS-2$
					}

				case INVALID_PROJECT:
					return CoreModelMessages.getFormattedString("status.invalidProject", fString); //$NON-NLS-1$

				case INVALID_RESOURCE:
					return CoreModelMessages.getFormattedString("status.invalidResource", fString); //$NON-NLS-1$

				case INVALID_RESOURCE_TYPE:
					return CoreModelMessages.getFormattedString("status.invalidResourceType", fString); //$NON-NLS-1$

				case INVALID_SIBLING:
					if (fString != null) {
						return CoreModelMessages.getFormattedString("status.invalidSibling", fString); //$NON-NLS-1$
					} else {
						return CoreModelMessages.getFormattedString("status.invalidSibling", ((ICElement)fElements[0]).toString()); //$NON-NLS-1$
					}

				case IO_EXCEPTION:
					return CoreModelMessages.getFormattedString("status.IOException"); //$NON-NLS-1$

				case NAME_COLLISION:
					StringBuffer sb = new StringBuffer();
					if (fElements != null && fElements.length > 0) {
						ICElement element = fElements[0];
						sb.append(element.getElementName()).append(' ');
					}
					if (fString != null) {
						return fString;
					} else {
						return CoreModelMessages.getFormattedString("status.nameCollision", sb.toString()); //$NON-NLS-1$ //$NON-NLS-2$
					}

				case NO_ELEMENTS_TO_PROCESS:
					return CoreModelMessages.getFormattedString("operation.needElements"); //$NON-NLS-1$

				case NULL_NAME:
					return CoreModelMessages.getFormattedString("operation.needName"); //$NON-NLS-1$

				case NULL_PATH:
					return CoreModelMessages.getFormattedString("operation.needPath"); //$NON-NLS-1$

				case NULL_STRING:
					return CoreModelMessages.getFormattedString("operation.needString"); //$NON-NLS-1$

				case PATH_OUTSIDE_PROJECT:
					return CoreModelMessages.getFormattedString("operation.pathOutsideProject", new String[]{fString, ((ICElement)fElements[0]).toString()}); //$NON-NLS-1$

				case READ_ONLY:
					ICElement element = fElements[0];
					String name = element.getElementName();
					return CoreModelMessages.getFormattedString("status.readOnly", name); //$NON-NLS-1$

				case RELATIVE_PATH:
					return CoreModelMessages.getFormattedString("operation.needAbsolutePath", getPath().toString()); //$NON-NLS-1$

				case TARGET_EXCEPTION:
					return CoreModelMessages.getFormattedString("status.targetException"); //$NON-NLS-1$

				case UPDATE_CONFLICT:
					return CoreModelMessages.getFormattedString("status.updateConflict"); //$NON-NLS-1$

				case NO_LOCAL_CONTENTS :
					return CoreModelMessages.getFormattedString("status.noLocalContents", getPath().toString()); //$NON-NLS-1$

				//case CP_CONTAINER_PATH_UNBOUND:
				//	element = (ICElement)fElements[0];
					//PathEContainerInitializer initializer = CoreModel.getPathEntryContainerInitializer(this.path.segment(0));
					//String description = null;
					//if (initializer != null) description = initializer.getDescription(this.path, javaProject);
					//if (description == null) description = path.makeRelative().toString();
				//	return CoreModelMessages.getFormattedString("pathentry.unboundContainerPath", element.getElementName()); //$NON-NLS-1$

				//case INVALID_CP_CONTAINER_ENTRY:
				//	element = (ICElement)fElements[0];
					//IPathEntryContainer container = null;
					//description = null;
					//try {
					//	container = CoreModel.getPathEntryContainer(path, javaProject);
					//} catch(CModelException e){
						// project doesn't exist: ignore
					//}
					//if (container == null) {
					//	 initializer = CoreModel.getPathEntryContainerInitializer(path.segment(0));
					//	if (initializer != null) description = initializer.getDescription(path, javaProject);
					//} else {
					//	description = container.getDescription();
					//}
					//if (description == null) description = path.makeRelative().toString();
				//	return CoreModelMessages.getFormattedString("pathentry.invalidContainer", element.getElementName()); //$NON-NLS-1$

			case CP_VARIABLE_PATH_UNBOUND:
				element = (ICElement)fElements[0];
				return CoreModelMessages.getFormattedString("pathentry.unboundVariablePath",
						new String[] {getPath().makeRelative().toString(), element.getElementName()}); //$NON-NLS-1$
					
			//case CLASSPATH_CYCLE: 
			//	element = (ICElement)fElements[0];
			//	return CoreModelMessages.getFormattedString("pathentry.cycle", element.getElementName()); //$NON-NLS-1$
												 
			//case DISABLED_CP_EXCLUSION_PATTERNS:

			//case DISABLED_CP_MULTIPLE_OUTPUT_LOCATIONS:

			//case INCOMPATIBLE_JDK_LEVEL:
			}
			if (fString != null) {
				return fString;
			} else {
				return ""; // //$NON-NLS-1$
			}
		} else {
			String message = exception.getMessage();
			if (message != null) {
				return message;
			} else {
				return exception.toString();
			}
		}
	}

	/**
	 * @see IOperationStatus
	 */
	public IPath getPath() {
		return fPath;
	}

	/**
	 * @see IStatus
	 */
	public int getSeverity() {
		if (fChildren == fgEmptyChildren) return super.getSeverity();
		int severity = -1;
		for (int i = 0, max = fChildren.length; i < max; i++) {
			int childrenSeverity = fChildren[i].getSeverity();
			if (childrenSeverity > severity) {
				severity = childrenSeverity;
			}
		}
		return severity;
	}

	/**
	 * @see ICModelStatus
	 */
	public String getString() {
		return fString;
	}

	/**
	 * @see ICModelStatus
	 */
	public boolean doesNotExist() {
		return getCode() == ELEMENT_DOES_NOT_EXIST;
	}

	/**
	 * @see IStatus
	 */
	public boolean isMultiStatus() {
		return fChildren != fgEmptyChildren;
	}

	/**
	 * @see ICModelStatus
 	*/
	public boolean isOK() {
		return getCode() == OK;
	}

	/**
	 * @see IStatus#matches
	 */
	public boolean matches(int mask) {
		if (! isMultiStatus()) {
			return matches(this, mask);
		} else {
			for (int i = 0, max = fChildren.length; i < max; i++) {
				if (matches((CModelStatus) fChildren[i], mask))
					return true;
			}
			return false;
		}
	}

	/**
	 * Helper for matches(int).
	 */
	protected boolean matches(CModelStatus status, int mask) {
		int severityMask = mask & 0x7;
		int categoryMask = mask & ~0x7;
		int bits = status.getBits();
		return ((severityMask == 0) || (bits & severityMask) != 0) && ((categoryMask == 0) || (bits & categoryMask) != 0);
	}

	/**
	 * Creates and returns a new <code>ICModelStatus</code> that is a
	 * a multi-status status.
	 *
	 * @see IStatus#.isMultiStatus()
	 */
	public static ICModelStatus newMultiStatus(ICModelStatus[] children) {
		CModelStatus jms = new CModelStatus();
		jms.fChildren = children;
		return jms;
	}

	/**
	 * Returns a printable representation of this exception for debugging
	 * purposes.
	 */
	public String toString() {
		if (this == VERIFIED_OK){
			return "CModelStatus[OK]"; //$NON-NLS-1$
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("C Model Status ["); //$NON-NLS-1$
		buffer.append(getMessage());
		buffer.append("]"); //$NON-NLS-1$
		return buffer.toString();
	}
}

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
	protected ICElement[] fElements = new ICElement[0];
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
	public static final ICModelStatus VERIFIED_OK = new CModelStatus(OK, OK, org.eclipse.cdt.internal.core.Util.bind("status.OK"));;

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
		fElements= CElementInfo.fgEmptyChildren;
	}

	/**
	 * Constructs an C model status with the given corresponding
	 * elements.
	 */
	public CModelStatus(int code, ICElement[] elements) {
		super(ERROR, CCorePlugin.PLUGIN_ID, code, "CModelStatus", null); //$NON-NLS-1$
		fElements= elements;
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
		fElements= CElementInfo.fgEmptyChildren;
		fPath= null;
		fString = string;
	}	

	/**
	 * Constructs an C model status with no corresponding elements.
	 */
	public CModelStatus(int code, Throwable throwable) {
		super(ERROR, CCorePlugin.PLUGIN_ID, code, "CModelStatus", throwable); //$NON-NLS-1$
		fElements= CElementInfo.fgEmptyChildren;
	}

	/**
	 * Constructs an C model status with no corresponding elements.
	 */
	public CModelStatus(int code, IPath path) {
		super(ERROR, CCorePlugin.PLUGIN_ID, code, "CModelStatus", null); //$NON-NLS-1$
		fElements= CElementInfo.fgEmptyChildren;
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
		//fElements= CElementInfo.fgEmptyChildren;
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
			if (fString == null) {
				return "Error in C Plugin(" + getCode() + ")";
			}
			return "Error in C Plugin(" + getCode() + "):" + fString ;
		} else {
			String mesg = exception.getMessage();
			if (mesg != null && mesg.length() > 0) {
				return mesg;
			}
			return exception.toString();
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

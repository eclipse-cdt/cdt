package org.eclipse.launchbar.core.target;

import org.eclipse.launchbar.core.internal.Messages;

/**
 * The status for a launch target.
 */
public class TargetStatus {

	public enum Code {
		OK, WARNING, ERROR
	}

	private final TargetStatus.Code code;
	private final String message;

	public static final TargetStatus OK_STATUS = new TargetStatus(Code.OK, Messages.OK);

	public TargetStatus(TargetStatus.Code code, String message) {
		this.code = code;
		this.message = message;
	}

	public TargetStatus.Code getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

}
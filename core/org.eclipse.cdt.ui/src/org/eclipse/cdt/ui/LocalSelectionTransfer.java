package org.eclipse.cdt.ui;

import java.util.Arrays;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * @author kcampbell
 */
public class LocalSelectionTransfer extends ByteArrayTransfer {
	private static final LocalSelectionTransfer INSTANCE = new LocalSelectionTransfer();

	private final String typeName;
	private final int typeId;
	private ISelection selection;

	private LocalSelectionTransfer() {
		super();
		// Try to ensure that different Eclipse applications use different "types" of <code>LocalSelectionTransfer</code>
		typeName = "cdt-local-selection-transfer-format" + System.currentTimeMillis(); //$NON-NLS-1$;
		typeId = registerType(typeName);
		selection = null;
	}

	/**
	 * Returns the singleton.
	 */
	public static LocalSelectionTransfer getInstance() {
		return INSTANCE;
	}

	/**
	 * Sets the transfer data for local use.
	 */
	public void setSelection(ISelection selection) {
		this.selection = selection;
	}

	/**
	 * Returns the local transfer data.
	 */
	public ISelection getSelection() {
		return selection;
	}

	public void javaToNative(Object object, TransferData transferData) {
		// No encoding needed since this is a hardcoded string read and written in the same process.
		// See nativeToJava below
		super.javaToNative(typeName.getBytes(), transferData);
	}

	public Object nativeToJava(TransferData transferData) {
		Object result = super.nativeToJava(transferData);

		// No decoding needed: see javaToNative above.
		Assert.isTrue(result instanceof byte[] && Arrays.equals(typeName.getBytes(), (byte[]) result));

		return selection;
	}

	/**
	 * The type id used to identify this transfer.
	 */
	protected int[] getTypeIds() {
		return new int[] { typeId };
	}

	protected String[] getTypeNames() {
		return new String[] { typeName };
	}
}
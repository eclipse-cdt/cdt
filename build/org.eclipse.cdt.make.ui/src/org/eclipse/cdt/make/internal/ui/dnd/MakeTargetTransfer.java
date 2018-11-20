/*******************************************************************************
 * Copyright (c) 2008, 2009 Andrew Gvozdev.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Gvozdev (Quoin Inc.) - Initial implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.dnd.MakeTargetTransferData.MakeTargetData;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * This class can be used to transfer an instance of
 * {@link MakeTargetTransferData} between two parts in a workbench in a drag and
 * drop or clipboard copy/paste operation.
 *
 * @see ByteArrayTransfer
 * @see Transfer
 * @see org.eclipse.swt.dnd.DropTarget
 * @see org.eclipse.swt.dnd.DragSource
 */
public class MakeTargetTransfer extends ByteArrayTransfer {

	private static final String TYPE_NAME = "make-target-transfer-format";//$NON-NLS-1$

	private static final int TYPEID = registerType(TYPE_NAME);

	/**
	 * Singleton instance.
	 */
	private static MakeTargetTransfer instance = new MakeTargetTransfer();

	/**
	 * Creates a new transfer object.
	 */
	private MakeTargetTransfer() {
		super();
	}

	/**
	 * @return the singleton instance.
	 */
	public static MakeTargetTransfer getInstance() {
		return instance;
	}

	/**
	 * @return ids of the data types that can be converted using this transfer
	 *         agent.
	 *
	 * @see Transfer#getTypeIds()
	 */
	@Override
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	/**
	 * @return the list of type names that can be converted using this transfer
	 *         agent.
	 *
	 * @see Transfer#getTypeNames()
	 */
	@Override
	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}

	/**
	 * Converts a java representation of data to a platform specific
	 * representation of the data.
	 *
	 * @param object - a java representation of the data to be converted.
	 * @param transferData - an empty TransferData object; this object will be
	 *        filled in on return with the platform specific representation of
	 *        the data.
	 *
	 * @exception org.eclipse.swt.SWTException {@code ERROR_INVALID_DATA} - if
	 *            object doesn't contain data in a valid format or {@code null}.
	 *
	 * @see Transfer#javaToNative(Object object, TransferData transferData)
	 */
	@Override
	protected void javaToNative(Object object, TransferData transferData) {
		if (object instanceof MakeTargetTransferData) {
			MakeTargetTransferData realData = (MakeTargetTransferData) object;
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				DataOutputStream dataOut = new DataOutputStream(out);
				dataOut.writeInt(realData.getMakeTargetDataList().size());

				for (MakeTargetData mtd : realData.getMakeTargetDataList()) {
					dataOut.writeUTF(mtd.getName());
					dataOut.writeBoolean(mtd.runAllBuilders());
					dataOut.writeBoolean(mtd.appendProjectEnvironment());
					dataOut.writeUTF(mtd.getBuildTarget());
					dataOut.writeUTF(mtd.getBuildCommand());
					dataOut.writeUTF(mtd.getBuildArguments());
					dataOut.writeBoolean(mtd.isStopOnError());
					dataOut.writeBoolean(mtd.isDefaultBuildCmd());
					dataOut.writeBoolean(mtd.appendEnvironment());
				}
				dataOut.close();
				super.javaToNative(out.toByteArray(), transferData);
			} catch (IOException e) {
				MakeUIPlugin.log(e);
			}
		}
	}

	/**
	 * Converts a platform specific representation of data to a java
	 * representation.
	 *
	 * @param transferData - the platform specific representation of the data to
	 *        be converted.
	 *
	 * @return a java representation of the converted data if the conversion was
	 *         successful; otherwise {@code null}. If transferData is {@code
	 *         null} then {@code null} is returned. The type of Object that is
	 *         returned is dependent on the {@link Transfer} subclass.
	 *
	 * @see Transfer#nativeToJava(TransferData transferData)
	 */
	@Override
	protected Object nativeToJava(TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		DataInputStream dataIn = new DataInputStream(in);
		try {
			MakeTargetTransferData mttd = new MakeTargetTransferData();
			int size = dataIn.readInt();
			for (int i = 0; i < size; i++) {

				String name = dataIn.readUTF();
				boolean runAllBuilders = dataIn.readBoolean();
				boolean appendProjectEnvironment = dataIn.readBoolean();
				String buildTarget = dataIn.readUTF();
				String buildCommand = dataIn.readUTF();
				String buildArguments = dataIn.readUTF();
				boolean stopOnError = dataIn.readBoolean();
				boolean useDefaultBuildCmd = dataIn.readBoolean();
				boolean appendEnvironment = dataIn.readBoolean();

				mttd.addMakeTarget(name, runAllBuilders, appendProjectEnvironment, buildTarget, buildCommand,
						buildArguments, stopOnError, useDefaultBuildCmd, appendEnvironment);
			}
			return mttd;
		} catch (IOException e) {
			MakeUIPlugin.log(e);
		}
		return null;
	}

}

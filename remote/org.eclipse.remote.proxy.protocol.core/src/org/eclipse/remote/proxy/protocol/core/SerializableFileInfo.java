/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.remote.proxy.protocol.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.provider.FileInfo;

public class SerializableFileInfo implements Serializable {
	private static final long serialVersionUID = -1986643088683154145L;

	private IFileInfo info;

	public SerializableFileInfo() {
	}

	public SerializableFileInfo(IFileInfo info) {
		setIFileInfo(info);
	}

	public void setIFileInfo(IFileInfo info) {
		this.info = info;
	}

	public IFileInfo getIFileInfo() {
		return info;
	}

	public void writeObject(DataOutputStream out) throws IOException {
		out.writeUTF(info.getName());
		boolean symlink = info.getAttribute(EFS.ATTRIBUTE_SYMLINK);
		out.writeBoolean(symlink);
		if (symlink) {
			out.writeUTF(info.getStringAttribute(EFS.ATTRIBUTE_LINK_TARGET));
		}
		out.writeBoolean(info.exists());
		out.writeLong(info.getLastModified());
		out.writeLong(info.getLength());
		out.writeBoolean(info.isDirectory());
		out.writeBoolean(info.getAttribute(EFS.ATTRIBUTE_GROUP_READ));
		out.writeBoolean(info.getAttribute(EFS.ATTRIBUTE_GROUP_WRITE));
		out.writeBoolean(info.getAttribute(EFS.ATTRIBUTE_GROUP_EXECUTE));
		out.writeBoolean(info.getAttribute(EFS.ATTRIBUTE_OTHER_READ));
		out.writeBoolean(info.getAttribute(EFS.ATTRIBUTE_OTHER_WRITE));
		out.writeBoolean(info.getAttribute(EFS.ATTRIBUTE_OTHER_EXECUTE));
		out.writeBoolean(info.getAttribute(EFS.ATTRIBUTE_OWNER_READ));
		out.writeBoolean(info.getAttribute(EFS.ATTRIBUTE_OWNER_WRITE));
		out.writeBoolean(info.getAttribute(EFS.ATTRIBUTE_OWNER_EXECUTE));
	}

	public void readObject(DataInputStream in) throws IOException {
		FileInfo newInfo = new FileInfo();

		try {
			newInfo.setName(in.readUTF());
			boolean symlink = in.readBoolean();
			newInfo.setAttribute(EFS.ATTRIBUTE_SYMLINK, symlink);
			if (symlink) {
				newInfo.setStringAttribute(EFS.ATTRIBUTE_LINK_TARGET, in.readUTF());
			}
			newInfo.setExists(in.readBoolean());
			newInfo.setLastModified(in.readLong());
			newInfo.setLength(in.readLong());
			newInfo.setDirectory(in.readBoolean());
			newInfo.setAttribute(EFS.ATTRIBUTE_GROUP_READ, in.readBoolean());
			newInfo.setAttribute(EFS.ATTRIBUTE_GROUP_WRITE, in.readBoolean());
			newInfo.setAttribute(EFS.ATTRIBUTE_GROUP_EXECUTE, in.readBoolean());
			newInfo.setAttribute(EFS.ATTRIBUTE_OTHER_READ, in.readBoolean());
			newInfo.setAttribute(EFS.ATTRIBUTE_OTHER_WRITE, in.readBoolean());
			newInfo.setAttribute(EFS.ATTRIBUTE_OTHER_EXECUTE, in.readBoolean());
			newInfo.setAttribute(EFS.ATTRIBUTE_OWNER_READ, in.readBoolean());
			newInfo.setAttribute(EFS.ATTRIBUTE_OWNER_WRITE, in.readBoolean());
			newInfo.setAttribute(EFS.ATTRIBUTE_OWNER_EXECUTE, in.readBoolean());
		} catch (IOException e) {
			newInfo.setError(IFileInfo.IO_ERROR);
		}

		setIFileInfo(newInfo);
	}
}

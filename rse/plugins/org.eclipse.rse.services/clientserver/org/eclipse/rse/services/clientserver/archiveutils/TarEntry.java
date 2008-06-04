/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [219975] Fix implementations of clone()
 * Xuan Chen (IBM) - [api] SystemTarHandler has inconsistent API
 *******************************************************************************/

package org.eclipse.rse.services.clientserver.archiveutils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.rse.internal.services.clientserver.archiveutils.ITarConstants;

/**
 * This class represents a tar file entry.
 * @since 3.0
 */
public class TarEntry implements Cloneable {

	// NOTE: Read the GNU tar specification to understand what each of the fields mean.
	// http://www.gnu.org/software/tar/manual/html_mono/tar.html#SEC118

	// TODO (KM): Do we need to worry about ASCII? I think we do. We are constantly
	// switching between bytes and String assuming local encoding. However, the tar specification states
	// local ASCII variant must always be used. I think our code will probably fail on non-ASCII machines
	// (e.g. Japanese, iSeries IFS, etc.). Need to test such a scenario to be sure though.
	// Also, what does local variant of ASCII mean anyway? In a Japanese locale, what would be the
	// local variant of ASCII? Can we just use US-ASCII everywhere and get away with it. I think
	// that should work. Local variant of ASCII possibly means slightly different versions of ASCII used
	// on different machines, but not between locales.

	// block header fields
	public byte[] name = new byte[ITarConstants.NAME_LENGTH];
	public byte[] mode = new byte[ITarConstants.MODE_LENGTH];
	public byte[] uid = new byte[ITarConstants.UID_LENGTH];
	public byte[] gid = new byte[ITarConstants.GID_LENGTH];
	public byte[] size = new byte[ITarConstants.SIZE_LENGTH];
	public byte[] mtime = new byte[ITarConstants.MTIME_LENGTH];
	public byte[] chksum = new byte[ITarConstants.CHKSUM_LENGTH];
	public byte typeflag;
	public byte[] linkname = new byte[ITarConstants.LINKNAME_LENGTH];
	public byte[] magic = new byte[ITarConstants.MAGIC_LENGTH];
	public byte[] version = new byte[ITarConstants.VERSION_LENGTH];
	public byte[] uname = new byte[ITarConstants.UNAME_LENGTH];
	public byte[] gname = new byte[ITarConstants.GNAME_LENGTH];
	public byte[] devmajor = new byte[ITarConstants.DEVMAJOR_LENGTH];
	public byte[] devminor = new byte[ITarConstants.DEVMINOR_LENGTH];
	public byte[] prefix = new byte[ITarConstants.PREFIX_LENGTH];

	/**
	 * Creates a new tar entry with the specified name. Use the setter methods to
	 * populate the other fields of the entry.
	 * @param name the name of the tar entry.
	 * @throws NullPointerException if the name is <code>null</code>.
	 * @throws IllegalArgumentException if the length of the name is greater that
	 */
	public TarEntry(String name) {
		setName(name);
	}

	/**
	 * Creates a new tar entry from the given block data. Fills in all the fields from the
	 * block data.
	 * @param blockData the block data.
	 * @throws NullPointerException if block data is null.
	 * @throws IllegalArgumentException if the block data is less than the length of a block.
	 * @throws IOException if an I/O error occurs.
	 */
	TarEntry(byte[] blockData) throws IOException {
		checkNull(blockData);

		if (blockData.length != ITarConstants.BLOCK_SIZE) {
			throw new IllegalArgumentException();
		}

		populateFields(blockData);
	}

	/**
	 * Fills in the fields of the entry from block data.
	 * @param blockData data in a header block.
	 * @throws IOException if an I/O error occurs.
	 */
	private void populateFields(byte[] blockData) throws IOException {

		InputStream byteStream = new ByteArrayInputStream(blockData);

		// read the name
		byteStream.read(name);

		// if the name is an empty string, then don't fill in other fields,
		// since this indicates that we have reached end of file
		if (getName().equals("")) { //$NON-NLS-1$
			return;
		}

		byteStream.read(mode);
		byteStream.read(uid);
		byteStream.read(gid);
		byteStream.read(size);
		byteStream.read(mtime);
		byteStream.read(chksum);
		typeflag = (byte)byteStream.read();
		byteStream.read(linkname);
		byteStream.read(magic);
		byteStream.read(version);
		byteStream.read(uname);
		byteStream.read(gname);
		byteStream.read(devmajor);
		byteStream.read(devminor);
		byteStream.read(prefix);
	}

	/**
	 * Checks whether the given object is null, and throws a <code>NullPointerException</code> if the
	 * obect is <code>null</code>.
	 * @param o an object
	 * @throws NullPointerException if the given object is <code>null</code>.
	 */
	private void checkNull(Object o) {

		if (o == null) {
			throw new NullPointerException();
		}
	}

	/**
	 * Sets the name of the tar entry.
	 * @param fileName the name for the tar entry.
	 * @throws NullPointerException if the name is <code>null</code>.
	 */
	public void setName(String fileName) {
		checkNull(fileName);

		int length = ITarConstants.NAME_LENGTH - fileName.length();

		// append null characters to the name
		for (int i = 0; i < length; i++) {
			fileName = fileName + "\0"; //$NON-NLS-1$
		}

		name = fileName.getBytes();
	}

	/**
	 * Gets the name.
	 * @return the name.
	 */
	public String getName() {
		return (new String(name)).trim();
	}

	/**
	 * Sets the user mod.
	 * @param canRead <code>true</code> if the user has read permission, <code>false</code> otherwise.
	 * @param canWrite <code>true</code> if the user has write permission, <code>false</code> otherwise.
	 * @param canExecute <code>true</code> if the user has execute permission, <code>false</code> otherwise.
	 */
	public void setUserMode(boolean canRead, boolean canWrite, boolean canExecute) {

		int mod = 00;

		if (canRead) {
			mod += 04;
		}

		if (canWrite) {
			mod += 02;
		}

		if (canExecute) {
			mod += 01;
		}

		String modString = "0100" + Integer.toString(mod, 8) + "44"; //$NON-NLS-1$ //$NON-NLS-2$
		modString = modString + "\0"; //$NON-NLS-1$

		mode = modString.getBytes();
	}

	/**
	 * Gets the mode in octal.
	 * @return the mode.
	 */
	public String getMode() {
		return (new String(mode)).trim();
	}

	/**
	 * Gets the uid in octal.
	 * @return the uid.
	 */
	public String getUID() {
		return (new String(uid)).trim();
	}

	/**
	 * Gets the gid in octal.
	 * @return the gid.
	 */
	public String getGID() {
		return (new String(gid)).trim();
	}

	/**
	 * Sets the file size in bytes.
	 * @param fileSize the file size.
	 */
	public void setSize(long fileSize) {

		// get the octal representation of the file size as a string
		String sizeString = Long.toString(fileSize, 8).trim();

		// get the length of the string
		int length = sizeString.length();

		int diff = ITarConstants.SIZE_LENGTH - length - 1;

		// prepend the string with 0s
		for (int i = 0; i < diff; i++) {
			sizeString = "0" + sizeString; //$NON-NLS-1$
		}

		// append a space at the end
		sizeString = sizeString + " "; //$NON-NLS-1$

		size = sizeString.getBytes();
	}

	/**
	 * Gets the size in bytes.
	 * @return the size.
	 */
	public long getSize() {
		return Long.parseLong((new String(size)).trim(), 8);
	}

	/**
	 * Sets the modification time.
	 * @param modTime the modification time, in milliseconds since 00:00:00 GMT, January 1, 1970.
	 */
	public void setModificationTime(long modTime) {

		// get the octal representation of the modification time as a string
		String mtimeString = Long.toString(modTime/1000, 8).trim();

		// get the length of the string
		int length = mtimeString.length();

		int diff = ITarConstants.MTIME_LENGTH - length - 1;

		// prepend the string with 0s
		for (int i = 0; i < diff; i++) {
			mtimeString = "0" + mtimeString; //$NON-NLS-1$
		}

		// append a space at the end
		mtimeString = mtimeString + " "; //$NON-NLS-1$

		mtime = mtimeString.getBytes();
	}

	/**
	 * Gets the modification time, in milliseconds since 00:00:00 GMT, January 1, 1970.
	 * @return the modification time.
	 */
	public long getModificationTime() {
		return Long.parseLong((new String(mtime)).trim(), 8) * 1000;
	}

	/**
	 * Gets the checksum.
	 * @return the checksum.
	 */
	public long getChecksum() {
		return Long.parseLong((new String(chksum)).trim(), 8);
	}

	/**
	 * Gets the type of file archived.
	 * @return the type flag.
	 */
	public char getTypeFlag() {
		return (char)typeflag;
	}

	/**
	 * Gets the link name.
	 * @return the link name.
	 */
	public String getLinkName() {
		return (new String(linkname)).trim();
	}

	/**
	 * Returns whether the archive was output in the P1003 archive format.
	 * This is not used.
	 * @return the magic field.
	 */
	public String getMagic() {
		return (new String(magic)).trim();
	}

	/**
	 * Gets the version in octal.
	 * @return the version.
	 */
	public String getVersion() {
		return (new String(version)).trim();
	}

	/**
	 * Sets the user name of the tar entry.
	 * @param userName the user name for the tar entry.
	 * @throws NullPointerException if the user name is <code>null</code>.
	 */
	public void setUserName(String userName) {
		checkNull(userName);

		int length = ITarConstants.UNAME_LENGTH - userName.length();

		// append null characters to the user name
		for (int i = 0; i < length; i++) {
			userName = userName + "\0"; //$NON-NLS-1$
		}

		uname = userName.getBytes();
	}

	/**
	 * Gets the user name.
	 * @return the user name.
	 */
	public String getUserName() {
		return (new String(uname)).trim();
	}

	/**
	 * Gets the group name.
	 * @return the group name.
	 */
	public String getGroupName() {
		return (new String(gname)).trim();
	}

	/**
	 * Gets the major device number in octal.
	 * @return the major device number.
	 */
	public String getDevMajor() {
		return (new String(devmajor)).trim();
	}

	/**
	 * Gets the minor device number in octal.
	 * @return the minor device number.
	 */
	public String getDevMinor() {
		return (new String(devminor)).trim();
	}

	/**
	 * Gets the prefix in octal.
	 * @return the prefix.
	 */
	public String getPrefix() {
		return (new String(prefix)).trim();
	}

	/**
	 * Returns whether the entry represents a directory.
	 * @return <code>true</code> if the entry represents a directory, <code>false</code> otherwise.
	 */
	public boolean isDirectory() {

		String entryName = getName();

		if (entryName.endsWith("/")) { //$NON-NLS-1$
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Write the fields to the given output stream.
	 * @param outStream the output stream to write to.
	 */
	public void writeFields(OutputStream outStream) throws IOException {
		outStream.write(name);
		outStream.write(mode);
		outStream.write(uid);
		outStream.write(gid);
		outStream.write(size);
		outStream.write(mtime);
		outStream.write(chksum);
		outStream.write(typeflag);
		outStream.write(linkname);
		outStream.write(magic);
		outStream.write(version);
		outStream.write(uname);
		outStream.write(gname);
		outStream.write(devmajor);
		outStream.write(devminor);
		outStream.write(prefix);
	}

	/**
	 * Calculates the checksum of the entry.
	 */
	public void calculateChecksum() {
		int sum = 0;

		// add name bytes
		for (int i = 0; i < name.length; i++) {
			sum += name[i];
		}

		// add mode bytes
		for (int i = 0; i < mode.length; i++) {
			sum += mode[i];
		}

		// add uid bytes
		for (int i = 0; i < uid.length; i++) {
			sum += uid[i];
		}

		// add gid bytes
		for (int i = 0; i < gid.length; i++) {
			sum += gid[i];
		}

		// add size bytes
		for (int i = 0; i < size.length; i++) {
			sum += size[i];
		}

		// add mtime bytes
		for (int i = 0; i < mtime.length; i++) {
			sum += mtime[i];
		}

		// add checksum bytes assuming check sum is blank spaces
		char space = ' ';
		byte spaceByte = (byte)space;

		for (int i = 0; i < chksum.length; i++) {
			sum += spaceByte;
		}

		// add typeflag byte
		sum += typeflag;

		// add linkname bytes
		for (int i = 0; i < linkname.length; i++) {
			sum += linkname[i];
		}

		// add magic bytes
		for (int i = 0; i < magic.length; i++) {
			sum += magic[i];
		}

		// add version bytes
		for (int i = 0; i < version.length; i++) {
			sum += version[i];
		}

		// add uname bytes
		for (int i = 0; i < uname.length; i++) {
			sum += uname[i];
		}

		// add gname bytes
		for (int i = 0; i < gname.length; i++) {
			sum += gname[i];
		}

		// add devmajor bytes
		for (int i = 0; i < devmajor.length; i++) {
			sum += devmajor[i];
		}

		// add devminor bytes
		for (int i = 0; i < devminor.length; i++) {
			sum += devminor[i];
		}

		// add prefix bytes
		for (int i = 0; i < prefix.length; i++) {
			sum += prefix[i];
		}

		// get the octal representation of the sum as a string
		String sumString = Long.toString(sum, 8).trim();

		// get the length of the string
		int length = sumString.length();

		int diff = ITarConstants.CHKSUM_LENGTH - length - 2;

		// prepend the string with 0s
		for (int i = 0; i < diff; i++) {
			sumString = "0" + sumString; //$NON-NLS-1$
		}

		// append a null character
		sumString = sumString + "\0"; //$NON-NLS-1$

		// append a space
		sumString = sumString + " "; //$NON-NLS-1$

		// set the checksum
		chksum = sumString.getBytes();
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		TarEntry newEntry = (TarEntry)super.clone();
		newEntry.mode = (byte[])this.mode.clone();
		newEntry.uid = (byte[])this.uid.clone();
		newEntry.gid = (byte[])this.gid.clone();
		newEntry.size = (byte[])this.size.clone();
		newEntry.mtime = (byte[])this.mtime.clone();
		newEntry.chksum = (byte[])this.chksum.clone();
		//newEntry.typeflag = this.typeflag;
		newEntry.linkname = (byte[])this.linkname.clone();
		newEntry.magic = (byte[])this.magic.clone();
		newEntry.version = (byte[])this.version.clone();
		newEntry.uname = (byte[])this.uname.clone();
		newEntry.gname = (byte[])this.gname.clone();
		newEntry.devmajor = (byte[])this.devmajor.clone();
		newEntry.devminor = (byte[])this.devminor.clone();
		newEntry.prefix = (byte[])this.prefix.clone();
		return newEntry;
	}
}

package org.eclipse.cdt.debug.dap;

import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

public class CDTDebugProtocol {

	/**
	 * https://github.com/eclipse-cdt/cdt-gdb-adapter/blob/5d788cbbc6ace142b0930375fcd931b4241eddbb/src/GDBDebugSession.ts#L73
	 * export interface MemoryResponse extends Response {
	 *    body: MemoryContents;
	 * }
	 */
	public static class MemoryRequestResponse {
		@NonNull
		private MemoryContents body;

		@Pure
		@NonNull
		public MemoryContents getBody() {
			return this.body;
		}

		public void setBody(@NonNull final MemoryContents address) {
			if (address == null) {
				throw new IllegalArgumentException("Property must not be null: body"); //$NON-NLS-1$
			}
			this.body = address;
		}

		@Override
		@Pure
		public String toString() {
			ToStringBuilder b = new ToStringBuilder(this);
			b.add("body", this.body); //$NON-NLS-1$
			return b.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((body == null) ? 0 : body.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MemoryRequestResponse other = (MemoryRequestResponse) obj;
			if (body == null) {
				if (other.body != null)
					return false;
			} else if (!body.equals(other.body))
				return false;
			return true;
		}
	}

	/**
	 * https://github.com/eclipse-cdt/cdt-gdb-adapter/blob/5d788cbbc6ace142b0930375fcd931b4241eddbb/src/GDBDebugSession.ts#L67
	 * export interface MemoryContents {
	 *     /\* Hex-encoded string of bytes.  *\/
	 *     data: string;
	 *     address: string;
	 * }
	 */
	public static class MemoryContents {
		@NonNull
		private String data;
		@NonNull
		private String address;

		@Pure
		@NonNull
		public String getData() {
			return this.data;
		}

		public void setData(@NonNull final String address) {
			if (address == null) {
				throw new IllegalArgumentException("Property must not be null: data"); //$NON-NLS-1$
			}
			this.data = address;
		}

		@Pure
		@NonNull
		public String getAddress() {
			return this.address;
		}

		public void setAddress(@NonNull final String address) {
			if (address == null) {
				throw new IllegalArgumentException("Property must not be null: address"); //$NON-NLS-1$
			}
			this.address = address;
		}

		@Override
		@Pure
		public String toString() {
			ToStringBuilder b = new ToStringBuilder(this);
			b.add("data", this.data); //$NON-NLS-1$
			b.add("address", this.address); //$NON-NLS-1$
			return b.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((address == null) ? 0 : address.hashCode());
			result = prime * result + ((data == null) ? 0 : data.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MemoryContents other = (MemoryContents) obj;
			if (address == null) {
				if (other.address != null)
					return false;
			} else if (!address.equals(other.address))
				return false;
			if (data == null) {
				if (other.data != null)
					return false;
			} else if (!data.equals(other.data))
				return false;
			return true;
		}
	}

	/**
	 * https://github.com/eclipse-cdt/cdt-gdb-adapter/blob/5d788cbbc6ace142b0930375fcd931b4241eddbb/src/GDBDebugSession.ts#L58
	 * export interface MemoryRequestArguments {
	 *     address: string;
	 *     length: number;
	 *     offset?: number;
	 * }
	 *
	 */
	public static class MemoryRequestArguments {
		@NonNull
		private String address;
		@NonNull
		private Long length;
		private Long offset;

		@Pure
		@NonNull
		public String getAddress() {
			return this.address;
		}

		public void setAddress(@NonNull final String address) {
			if (address == null) {
				throw new IllegalArgumentException("Property must not be null: address"); //$NON-NLS-1$
			}
			this.address = address;
		}

		@Pure
		@NonNull
		public Long getLength() {
			return this.length;
		}

		public void setLength(@NonNull final Long length) {
			if (length == null) {
				throw new IllegalArgumentException("Property must not be null: length"); //$NON-NLS-1$
			}
			this.length = length;
		}

		@Pure
		public Long getOffset() {
			return this.offset;
		}

		public void setOffset(@NonNull final Long length) {
			if (length == null) {
				throw new IllegalArgumentException("Property must not be null: offset"); //$NON-NLS-1$
			}
			this.offset = length;
		}

		@Override
		@Pure
		public String toString() {
			ToStringBuilder b = new ToStringBuilder(this);
			b.add("address", this.address); //$NON-NLS-1$
			b.add("length", this.length); //$NON-NLS-1$
			b.add("offset", this.offset); //$NON-NLS-1$
			return b.toString();
		}

		@Override
		@Pure
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((address == null) ? 0 : address.hashCode());
			result = prime * result + ((length == null) ? 0 : length.hashCode());
			result = prime * result + ((offset == null) ? 0 : offset.hashCode());
			return result;
		}

		@Override
		@Pure
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MemoryRequestArguments other = (MemoryRequestArguments) obj;
			if (address == null) {
				if (other.address != null)
					return false;
			} else if (!address.equals(other.address))
				return false;
			if (length == null) {
				if (other.length != null)
					return false;
			} else if (!length.equals(other.length))
				return false;
			if (offset == null) {
				if (other.offset != null)
					return false;
			} else if (!offset.equals(other.offset))
				return false;
			return true;
		}

	}
}
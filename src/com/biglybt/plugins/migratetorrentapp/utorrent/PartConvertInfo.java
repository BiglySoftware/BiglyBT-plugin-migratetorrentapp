/*
 * Copyright (C) Bigly Software.  All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.biglybt.plugins.migratetorrentapp.utorrent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PartConvertInfo
{
	public class PartConvertRange
	{
		public final long torrentDataStartPos;

		public final long len;

		public PartConvertRange(long torrentDataStartPos, long len) {
			this.torrentDataStartPos = torrentDataStartPos;
			this.len = len;
		}
	}

	public final PartFile partFile;

	public final File destFile;

	public final int fileIndex;

	public PartConvertRange startRange;

	public PartConvertRange endRange;

	public PartConvertInfo(PartFile partFile, File destFile, int fileIndex) {
		this.partFile = partFile;
		this.destFile = destFile;
		this.fileIndex = fileIndex;
	}

	public PartConvertInfo setStartRange(long torrentDataStartPos, long len) {
		if (endRange != null && endRange.torrentDataStartPos == torrentDataStartPos
				&& endRange.len == len) {
			return this;
		}
		startRange = new PartConvertRange(torrentDataStartPos, len);
		return this;
	}

	public PartConvertInfo setEndRange(long torrentDataStartPos, long len) {
		if (startRange != null
				&& startRange.torrentDataStartPos == torrentDataStartPos
				&& startRange.len == len) {
			return this;
		}
		endRange = new PartConvertRange(torrentDataStartPos, len);
		return this;
	}

	public String toDebugString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		if (startRange != null) {
			sb.append("start: ");
			sb.append(startRange.len).append(" bytes @ ").append(
					startRange.torrentDataStartPos);
			PartFile.PartInfo partInfo = partFile.getPartInfoByTorrentDataPos(
					startRange.torrentDataStartPos);
			if (partInfo != null) {
				sb.append(" with start part ").append(partInfo.toDebugString());
			}
		}
		if (endRange != null) {
			if (startRange != null) {
				sb.append(", ");
			}
			sb.append("end: ");
			sb.append(endRange.len).append(" bytes @ ").append(
					endRange.torrentDataStartPos);
			PartFile.PartInfo partInfo = partFile.getPartInfoByTorrentDataPos(
					endRange.torrentDataStartPos);
			if (partInfo != null) {
				sb.append(" with start part ").append(partInfo.toDebugString());
			}
		}
		sb.append(" ]");
		return sb.toString();
	}
}

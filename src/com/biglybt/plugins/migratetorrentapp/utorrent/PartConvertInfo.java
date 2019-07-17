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

	public final List<PartConvertRange> ranges = new ArrayList<>();

	public PartConvertInfo(PartFile partFile, File destFile) {
		this.partFile = partFile;
		this.destFile = destFile;
	}

	public PartConvertInfo add(long torrentDataStartPos, long len) {
		ranges.add(new PartConvertRange(torrentDataStartPos, len));
		return this;
	}
}

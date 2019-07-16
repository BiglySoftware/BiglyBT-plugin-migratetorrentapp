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
import java.io.FileInputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.biglybt.core.torrent.TOTorrent;
import com.biglybt.core.torrent.TOTorrentException;
import com.biglybt.core.torrent.TOTorrentFactory;
import com.biglybt.core.util.protocol.AzURLStreamHandlerFactory;
import com.biglybt.plugins.migratetorrentapp.Utils;

/**
 * uTorrent partfile<br/>
 * Basically, torrent data is split into 64k parts.  Header has 4 byte index for
 * each part, pointing to data if index is > 0.  
 * After the header is the 64k data chunks, first data chunk is 1, second is 2, etc. 
 * Last data chunk may be smaller then 64k.
 * 
 * <pre>
 * ~uTorrentPartFile_<i>&lt;hexsize></i>.dat
 *   &lt;Header>, &lt;data>
 *   
 * hexsize
 *   torrent data length in bytes in hex with no leading 0
 *
 * Header
 *   &lt;DataIndex>[&lt;Num64kParts>]
 *   Raw header length = &lt;Num64kParts> * 4
 *
 * Num64kParts
 *   How many parts is required if you split torrent data length into 64k sections. 
 *   ie. Math.ciel(torrent data length in bytes / 64k)
 *
 * DataIndex
 *   4 byte little endian integer.  Values:
 *      0 
 *        No data for this 64k part
 *      1..&lt;num64Parts>
 *        1-based positional index in &lt;data>
 *        Location in part file can be calculated with
 *          (Header Size) + ((value - 1) * 64k)
 *   
 * data
 *   &lt;DataPart>[up to &lt;num64kParts>]
 *   
 * DataPart
 *   64k byte array containing torrent data.  
 *   Bytes in &lt;DataPart> that are stored elsewhere in real files will be 0x00. ie. non-skipped files sharing the 64k part will be 0x00. 
 *   Last &lt;DataPart> may be less than 64k, which means the rest of the 64k would be 0x00 (and part of a non-skipped file)
 *     
 * </pre>
 */
public class PartFile
{

	public static final int SIZE_64_K = 1024 * 64;

	private long torrentDataSize;

	private TOTorrent torrent;

	private File partsFile;

	private int numParts;

	private Map<Integer, Integer> mapHeaderIndexToPartIndex;

	private Map<Integer, PartInfo> mapParts;

	private int lastPartSize;

	private long pieceLength;

	public static class PartInfo
	{
		final int index;

		final long startPos;

		final byte[] data;

		final int len;

		public PartInfo(int index, long startPos, byte[] data, int len) {
			this.index = index;
			this.startPos = startPos;
			this.data = data;
			this.len = len;
		}

		public String toDebugString() {
			return "Data for range " + startPos + " to " + (startPos + len) + " ("
					+ len + " bytes) stored in index " + index + Utils.NL;
		}
	}

	public static PartFile getFromSaveLocation(TOTorrent torrent, File saveLocation) {
		File file = new File(saveLocation,
				"~uTorrentPartFile_" + Long.toHexString(torrent.getSize()) + ".dat");
		if (file.exists()) {
			return new PartFile(torrent, file);
		}
		return null;
	}

	public boolean hasPartFile() {
		return mapHeaderIndexToPartIndex != null;
	}

	public PartFile(TOTorrent torrent, File partsFile) {
		this.torrent = torrent;
		this.partsFile = partsFile;
		try {
			Pattern pat = Pattern.compile("_([A-Z0-9]+)\\.", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pat.matcher(partsFile.getName());
			if (!matcher.find()) {
				return;
			}
			torrentDataSize = Long.parseLong(matcher.group(1), 16);

			numParts = (int) ((torrentDataSize + (SIZE_64_K - 1)) / SIZE_64_K);

			FileInputStream is = new FileInputStream(partsFile);

			Map<Integer, Integer> mapPartIndexToHeaderIndex = new HashMap<>();
			mapHeaderIndexToPartIndex = new HashMap<>();

			byte[] partInfoBytes = new byte[numParts * 4];
			is.read(partInfoBytes);

			ByteBuffer bb = ByteBuffer.wrap(partInfoBytes);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			for (int i = 0; i < numParts; i++) {
				int partIndex = bb.getInt();
				if (partIndex == 0) {
					continue;
				}
				mapPartIndexToHeaderIndex.put(partIndex, i);
				mapHeaderIndexToPartIndex.put(i, partIndex);
			}

			partInfoBytes = null;

			mapParts = new HashMap<>();
			int partIndex = 1;

			while (true) {
				byte[] partData = new byte[SIZE_64_K];
				int read = is.read(partData);
				if (read == -1) {
					lastPartSize = SIZE_64_K;
					break;
				}
				Integer headerIndex = mapPartIndexToHeaderIndex.get(partIndex);
				long startPos = headerIndex * SIZE_64_K;
				mapParts.put(partIndex,
						new PartInfo(partIndex, startPos, partData, read));

				partIndex++;
				if (read < partData.length) {
					lastPartSize = read;
					break;
				}
			}

			pieceLength = torrent.getPieceLength();

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.setProperty("transitory.startup", "1");
		System.setProperty("no_diag_logger", "1");
		System.setProperty("stringinterner.disable", "1");
		URL.setURLStreamHandlerFactory(new AzURLStreamHandlerFactory());

		for (String torrentLoc : args) {
			File fileTorrent = new File(torrentLoc);
			try {
				TOTorrent torrent = TOTorrentFactory.deserialiseFromBEncodedFile(
						fileTorrent);
				File dir = fileTorrent.getParentFile();
				File[] files = dir.listFiles(
						pathname -> pathname.getName().contains("~uTorrentPartFile"));
				for (File file : files) {
					PartFile partFile = new PartFile(torrent, file);
					System.out.println(partFile.toDebugString());
				}
			} catch (TOTorrentException e) {
				e.printStackTrace();
			}
		}
	}

	public String toDebugString() {

		StringBuilder sb = new StringBuilder();

		sb.append(partsFile).append(Utils.NL);
		sb.append("Data Size: " + torrentDataSize).append(Utils.NL);
		if (torrentDataSize != torrent.getSize()) {
			sb.append("DATA SIZE DOES NOT MATCH .torrent's size field of "
					+ torrent.getSize()).append(Utils.NL);
		}

		sb.append("# 64k parts: " + numParts).append(Utils.NL);

		sb.append(mapHeaderIndexToPartIndex).append(Utils.NL);

		sb.append("# 64k Parts Read: " + mapParts.size()).append(Utils.NL);
		sb.append("last 64k Part Size: " + lastPartSize).append(Utils.NL);

		sb.append("Torrent Piece Length: " + pieceLength).append(Utils.NL);

		for (Integer partIndex : mapParts.keySet()) {
			PartInfo partInfo = mapParts.get(partIndex);
			sb.append(partInfo.toDebugString());
		}

		return sb.toString();
	}

	public boolean hasByteRange(long globalStartPos, long len) {
		int headerIndexNoStart = (int) (globalStartPos / SIZE_64_K);
		Integer partIndex = mapHeaderIndexToPartIndex.get(headerIndexNoStart);
		if (partIndex == null) {
			return false;
		}
		PartInfo partInfo = mapParts.get(partIndex);
		if (partInfo == null) {
			return false;
		}
		if (len < SIZE_64_K
				&& partInfo.startPos + partInfo.len < globalStartPos + len) {
			return false;
		}
		int headerIndexNoEnd = (int) ((globalStartPos + len - 1) / SIZE_64_K);
		if (headerIndexNoStart != headerIndexNoEnd) {
			partIndex = mapHeaderIndexToPartIndex.get(headerIndexNoEnd);
			if (partIndex == null) {
				return false;
			}
			partInfo = mapParts.get(partIndex);
			if (partInfo == null || globalStartPos + len > partInfo.startPos + partInfo.len) {
				return false;
			}
		}

		return true;
	}
}

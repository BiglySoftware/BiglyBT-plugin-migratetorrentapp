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

import java.io.*;
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

	private final File partsFile;

	private int numPartsInTorrent;

	private final Map<Integer, PartInfo> map64kPositionToPart = new HashMap<>();

	private int lastPartSize;

	public static class PartInfo
	{
		final long startPos;

		final int len;

		public PartInfo(long startPos, int len) {
			this.startPos = startPos;
			this.len = len;
		}

		public String toDebugString() {
			return "Data for range " + startPos + " to " + (startPos + len) + " ("
					+ len + " bytes)" + Utils.NL;
		}
	}

	public static PartFile getFromSaveLocation(TOTorrent torrent,
			File saveLocation) {
		File file = new File(saveLocation, "~uTorrentPartFile_"
				+ Long.toHexString(torrent.getSize()).toUpperCase() + ".dat");
		if (file.exists()) {
			return new PartFile(torrent, file);
		}
		return null;
	}

	public boolean hasPartFile() {
		return map64kPositionToPart.size() > 0;
	}

	public PartFile(TOTorrent torrent, File partsFile) {
		this.torrent = torrent;
		this.partsFile = partsFile;
		try {
			Pattern pat = Pattern.compile("_([A-Z0-9]+)\\.",
					Pattern.CASE_INSENSITIVE);
			Matcher matcher = pat.matcher(partsFile.getName());
			if (!matcher.find()) {
				return;
			}
			torrentDataSize = Long.parseLong(matcher.group(1), 16);

			numPartsInTorrent = (int) ((torrentDataSize + (SIZE_64_K - 1))
					/ SIZE_64_K);

			byte[] partInfoBytes = new byte[numPartsInTorrent * 4];

			FileInputStream is = new FileInputStream(partsFile);
			try {
				is.read(partInfoBytes);
			} finally {
				is.close();
			}

			Map<Integer, Integer> mapPartIndexToHeaderIndex = new HashMap<>();

			ByteBuffer bb = ByteBuffer.wrap(partInfoBytes);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			for (int i = 0; i < numPartsInTorrent; i++) {
				int partIndex = bb.getInt();
				if (partIndex == 0) {
					continue;
				}
				mapPartIndexToHeaderIndex.put(partIndex, i);
			}

			long dataLen = partsFile.length() - (numPartsInTorrent * 4);
			lastPartSize = (int) (dataLen % SIZE_64_K);
			if (lastPartSize == 0) {
				lastPartSize = SIZE_64_K;
			}

			int numParts = mapPartIndexToHeaderIndex.size();
			for (int partIndex = 1; partIndex <= numParts; partIndex++) {
				Integer headerIndex = mapPartIndexToHeaderIndex.get(partIndex);
				if (headerIndex == null) {
					System.err.println(
							"Part " + partIndex + " doesn't have a header entry");
					continue;
				}
				long startPos = headerIndex * SIZE_64_K;
				int len = partIndex == numParts ? lastPartSize : SIZE_64_K;
				PartInfo partInfo = new PartInfo(startPos, len);
				map64kPositionToPart.put(headerIndex, partInfo);
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("All")
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
		sb.append("Data Size: ").append(torrentDataSize).append(Utils.NL);
		if (torrentDataSize != torrent.getSize()) {
			sb.append("DATA SIZE DOES NOT MATCH .torrent's size field of ").append(
					torrent.getSize()).append(Utils.NL);
		}

		sb.append("# 64k parts in torrent: ").append(numPartsInTorrent).append(
				Utils.NL);

		sb.append("# 64k parts in partfile: ").append(
				map64kPositionToPart.size()).append(Utils.NL);
		sb.append("Last 64k part Size: ").append(lastPartSize).append(Utils.NL);

		for (PartInfo partInfo : map64kPositionToPart.values()) {
			sb.append(partInfo.toDebugString());
		}

		return sb.toString();
	}

	public boolean hasByteRange(long globalStartPos, long len) {
		int headerIndexNoStart = (int) (globalStartPos / SIZE_64_K);
		PartInfo partInfo = map64kPositionToPart.get(headerIndexNoStart);
		if (partInfo == null) {
			return false;
		}
		if (len < SIZE_64_K
				&& partInfo.startPos + partInfo.len < globalStartPos + len) {
			return false;
		}
		int headerIndexNoEnd = (int) ((globalStartPos + len - 1) / SIZE_64_K);
		if (headerIndexNoStart != headerIndexNoEnd) {
			partInfo = map64kPositionToPart.get(headerIndexNoEnd);
			if (partInfo == null
					|| globalStartPos + len > partInfo.startPos + partInfo.len) {
				return false;
			}
		}

		return true;
	}

	public byte[] readPart(PartInfo partInfo)
			throws IOException {
		byte[] partInfoBytes = new byte[partInfo.len];
		FileInputStream is = new FileInputStream(partsFile);
		try {
			long skip = is.skip(partInfo.startPos);
			if (skip != partInfo.startPos) {
				throw new EOFException(
						"Skip " + partInfo.startPos + " skipped " + skip);
			}
			is.read(partInfoBytes);
		} finally {
			is.close();
		}

		return partInfoBytes;
	}

	public void writeTorrentData(OutputStream os, long torrentDataStartPos,
			long len)
			throws IOException {
		byte[] data = new byte[SIZE_64_K];

		FileInputStream is = new FileInputStream(partsFile);
		try {
			while (len > 0) {
				int partIndex = (int) (torrentDataStartPos / SIZE_64_K);
				PartInfo partInfo = map64kPositionToPart.get(partIndex);
				int partStartPos = (int) (torrentDataStartPos % SIZE_64_K);
				int partRemaining = SIZE_64_K - partStartPos;
				long partfileStartPos = partInfo.startPos + partStartPos;

				long skip = is.skip(partfileStartPos);
				if (skip != partfileStartPos) {
					throw new EOFException(
							"Skip " + partfileStartPos + " skipped " + skip);
				}
				int bytesRead = is.read(data, 0, (int) Math.min(partRemaining, len));

				if (bytesRead == -1) {
					throw new EOFException();
				}

				os.write(data, 0, bytesRead);

				torrentDataStartPos += bytesRead;
				len -= bytesRead;

			}
		} finally {
			is.close();
		}
	}
}

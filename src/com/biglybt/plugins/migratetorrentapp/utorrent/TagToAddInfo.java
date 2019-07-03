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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.biglybt.core.tag.Tag;
import com.biglybt.plugins.migratetorrentapp.Utils;

import static com.biglybt.plugins.migratetorrentapp.Utils.NL;

public class TagToAddInfo
{
	public String initialSaveFolder;

	public String constraint;

	public int maxUp;

	public int maxDown;

	public Tag tag;

	String name;

	String group;

	public boolean showInSidebar;

	final List<WeakReference<TorrentImportInfo>> items = new ArrayList<>();

	public TagToAddInfo(String name, String group) {
		this.name = name;
		this.group = group;
	}

	public String toDebugString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Utils.wrapString(name));
		if (group != null) {
			sb.append(" [").append(group).append("]");
		}
		if (items.size() > 0) {
			sb.append(", ").append(items.size()).append(" torrents assigned");
		}
		if (initialSaveFolder != null) {
			sb.append(NL).append("\tInitial Save Folder: ").append(
					Utils.wrapString(initialSaveFolder));
		}
		if (maxUp != 0 || maxDown != 0) {
			sb.append(NL).append("\tMax Up: ").append(maxUp).append("; Max Down: ").append(
					maxDown);
		}
		if (constraint != null) {
			sb.append(NL).append("\t").append(constraint.replaceAll(NL, NL + "\t"));
		}

		return sb.toString();
	}
}

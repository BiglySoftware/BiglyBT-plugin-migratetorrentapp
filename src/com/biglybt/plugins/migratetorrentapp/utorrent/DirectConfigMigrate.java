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

import java.util.ArrayList;
import java.util.List;

import com.biglybt.core.config.COConfigurationManager;
import com.biglybt.plugins.migratetorrentapp.Utils;

public class DirectConfigMigrate
	implements ConfigMigrateItem
{
	// Count for debug purposes
	public static int count = 0;

	public static class ConfigChange
	{
		final String utKey;

		final Object utValue;

		final String biglyKey;

		final Object biglyValue;

		final boolean privateValues;

		public ConfigChange(String utKey, Object utValue, String biglyKey,
				Object biglyValue, boolean privateValues) {
			this.utKey = utKey;
			this.utValue = utValue;
			this.biglyKey = biglyKey;
			this.biglyValue = biglyValue;
			this.privateValues = privateValues;
		}

		public StringBuilder toDebugString() {
			StringBuilder sb = new StringBuilder();

			sb.append(isAlreadyValue() ? "[Same] " : "[Change] ");
			if (utKey == null) {
				sb.append("-> ").append(biglyKey).append("(").append(biglyValue).append(
						")\n");
			} else {
				sb.append(utKey).append("(");
				if (privateValues) {
					sb.append(Utils.wrapString("" + utValue));
				} else {
					sb.append(utValue);
				}
				sb.append(") -> ");
				sb.append(biglyKey);
				sb.append("(");
				if (privateValues) {
					sb.append(Utils.wrapString("" + biglyValue));
				} else {
					sb.append(biglyValue);
				}
				sb.append(")\n");
			}
			return sb;
		}

		public boolean isAlreadyValue() {
			Object val;
			if (utValue instanceof Boolean && biglyValue instanceof Boolean) {
				val = COConfigurationManager.getBooleanParameter(biglyKey);
			} else if (utValue instanceof String && biglyValue instanceof String) {
				val = COConfigurationManager.getStringParameter(biglyKey);
			} else {
				val = COConfigurationManager.getParameter(biglyKey);
			}
			return biglyValue.equals(val);
		}
	}

	final List<ConfigChange> configChanges = new ArrayList<>();

	public DirectConfigMigrate(String utKey, Object utValue, String biglyKey,
			Object biglyValue) {
		this();
		add(utKey, utValue, biglyKey, biglyValue);
	}

	public DirectConfigMigrate(String utKey, String biglyKey, Object sameValue) {
		this();
		add(utKey, biglyKey, sameValue);
	}

	public DirectConfigMigrate() {
		count++;
	}

	public DirectConfigMigrate add(String utKey, Object utValue, String biglyKey,
			Object biglyValue) {
		configChanges.add(
				new ConfigChange(utKey, utValue, biglyKey, biglyValue, false));
		return this;
	}

	public DirectConfigMigrate addPrivate(String utKey, Object utValue,
			String biglyKey, Object biglyValue) {
		configChanges.add(
				new ConfigChange(utKey, utValue, biglyKey, biglyValue, true));
		return this;
	}

	public DirectConfigMigrate add(String utKey, String biglyKey,
			Object sameValue) {
		configChanges.add(
				new ConfigChange(utKey, sameValue, biglyKey, sameValue, false));
		return this;
	}

	public DirectConfigMigrate addPrivate(String utKey, String biglyKey,
			Object sameValue) {
		configChanges.add(
				new ConfigChange(utKey, sameValue, biglyKey, sameValue, true));
		return this;
	}

	@Override
	public StringBuilder toDebugString() {
		StringBuilder sb = new StringBuilder();
		boolean indent = configChanges.size() > 1;
		if (indent) {
			sb.append("Group of ").append(configChanges.size()).append('\n');
		}
		for (ConfigChange change : configChanges) {
			if (indent) {
				sb.append("\t");
			}
			sb.append(change.toDebugString());
		}
		return sb;
	}

	@Override
	public void migrate() {
		for (ConfigChange configChange : configChanges) {
			setParameter(configChange.biglyKey, configChange.biglyValue);
		}
	}

	private boolean setParameter(String key, Object value) {
		boolean changed = false;
		if (value == null) {
			return COConfigurationManager.removeParameter(key);
		}
		Class valueType = value.getClass();
		if (String.class.equals(valueType)) {
			changed = COConfigurationManager.setParameter(key, (String) value);
		} else if (Integer.class.equals(valueType)) {
			changed = COConfigurationManager.setParameter(key, (Integer) value);
		} else if (Float.class.equals(valueType)) {
			changed = COConfigurationManager.setParameter(key, (Float) value);
		} else if (Boolean.class.equals(valueType)) {
			changed = COConfigurationManager.setParameter(key, (Boolean) value);
		} else if (Long.class.equals(valueType)) {
			changed = COConfigurationManager.setParameter(key, (Long) value);
		} else if (byte[].class.equals(valueType)) {
			changed = COConfigurationManager.setParameter(key, (byte[]) value);
		}

		return changed;
	}

}

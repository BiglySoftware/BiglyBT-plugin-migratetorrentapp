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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.*;

import com.biglybt.core.config.COConfigurationManager;
import com.biglybt.core.config.ConfigKeys;
import com.biglybt.core.config.ConfigKeys.*;
import com.biglybt.core.config.impl.ConfigurationDefaults;
import com.biglybt.core.util.BDecoder;
import com.biglybt.core.util.Constants;
import com.biglybt.plugins.migratetorrentapp.Utils;
import com.biglybt.plugins.migratetorrentapp.utorrent.SettingsConstants.Connection;
import com.biglybt.plugins.migratetorrentapp.utorrent.SettingsConstants.*;
import com.biglybt.util.MapUtils;

import static com.biglybt.core.config.ConfigKeys.Connection.*;
import static com.biglybt.core.config.ConfigKeys.File.*;
import static com.biglybt.core.config.ConfigKeys.Tracker.*;
import static com.biglybt.core.config.ConfigKeys.Transfer.*;

public class SettingsImportInfo
{
	private static final String TG_SCHEDULER = "uTorrent Scheduler";

	private static final String TG_UTORRENT_DIRMAPPING = "uTorrent Directory Mapping";

	private static final String TG_UTORRENT_CONTAINS = "uTorrent Auto-Label";

	private final Importer_uTorrent importer;

	final List<ConfigMigrateItem> listConfigMigrations = new ArrayList<>();

	public final StringBuilder logWarnings = new StringBuilder();

	public final StringBuilder logInfo = new StringBuilder();

	public boolean granularPriorities;

	private Map<String, Object> utSettings;

	boolean hasRunProgram;

	public boolean preAllocSpace;

	public SettingsImportInfo(Importer_uTorrent importer) {
		this.importer = importer;
	}

	public void processSettings(File configDir) {
		DirectConfigMigrate.count = 0;
		RememberedDecisionConfig.count = 0;

		File fileSettings = new File(configDir, "settings.dat");
		try {
			BufferedInputStream is;
			is = new BufferedInputStream(new FileInputStream(fileSettings));
			BDecoder decoder = new BDecoder(Constants.UTF_8);
			utSettings = decoder.decodeStream(is, false);
			is.close();
			DirectConfigMigrate item;

			///

			List utListDirHist = MapUtils.getMapList(utSettings,
					SettingsConstants.ADD_DIALOG_HIST, Collections.emptyList());
			if (utListDirHist.size() > 0) {
				List<String> listDirHist = new ArrayList<>();

				String lastDir = "";
				for (Object o : utListDirHist) {
					String dir = Utils.objectToString(o);
					if (dir == null) {
						continue;
					}
					if (dir.endsWith(File.separator)) {
						dir = dir.substring(0, dir.length() - 1);
					}
					if (dir.equals(lastDir)) {
						continue;
					}
					lastDir = dir;
					listDirHist.add(dir);
				}

				List<String> utListDirHistStrings = new ArrayList<>(listDirHist);

				if (listDirHist.size() > 0) {
					List<String> existing = COConfigurationManager.getStringListParameter(
							ConfigKeys.File.SCFG_SAVE_TO_LIST);
					listDirHist.addAll(existing);

					item = new DirectConfigMigrate().addPrivate(
							SettingsConstants.ADD_DIALOG_HIST, utListDirHistStrings,
							ConfigKeys.File.SCFG_SAVE_TO_LIST, listDirHist);
					listConfigMigrations.add(item);
				}
			}

			///

			processPreferencesGeneral();

			processPreferencesUISettings();

			processPreferencesDirectories();

			processPreferencesConnection();

			processPreferencesBitTorrent();

			processPreferencesTransferCap();

			processPreferencesQueueing();

			processPreferencesBandwidth();

			processPreferencesScheduler();

			processPreferencesLabel();

			///

			String lastDir = MapUtils.getMapString(utSettings,
					SettingsConstants.DIR_LAST, "");
			if (!lastDir.isEmpty() && new File(lastDir).isDirectory()) {
				item = new DirectConfigMigrate(SettingsConstants.DIR_LAST,
						"previous.filter.dir.data", lastDir);
				listConfigMigrations.add(item);
			}

			///

			hasRunProgram = !MapUtils.getMapString(utSettings,
					advRunProgram.FINISH_CMD, "").isEmpty()
					|| !MapUtils.getMapString(utSettings, advRunProgram.STATE_CMD,
							"").isEmpty();

			/// 

			String persistLabels = MapUtils.getMapString(utSettings,
					SettingsConstants.GUI_PERSISTENT_LABELS, "");
			String[] persistLabelsArray = persistLabels.split("\\|");
			for (String label : persistLabelsArray) {
				if (label.isEmpty()) {
					continue;
				}
				TagToAddInfo tagToAddInfo = importer.addTagIgnoreGroup(null, label,
						"uTorrent Persistent Label");
				tagToAddInfo.showInSidebar = true;
			}

			processPreferencesAdvanced();

		} catch (Throwable t) {
			t.printStackTrace();
			importer.loggerChannel.log(t);
		}

		System.out.println(
				"DirectConfigMigrate.count = " + DirectConfigMigrate.count);
		System.out.println(
				"RememberedDecisionConfig  = " + RememberedDecisionConfig.count);
		System.out.println(
				"listConfigMigrations.size()=" + listConfigMigrations.size());
	}

	private void processPreferencesAdvanced() {
		Field[] fields = adv.class.getFields();
		boolean first = true;
		for (Field field : fields) {
			try {
				Object o = field.get(null);
				if (o instanceof String) {
					if (utSettings.containsKey((o))) {
						if (first) {
							logWarnings.append(
									"\nThe following advanced settings were not migrated:\n");
							first = false;
						}
						logWarnings.append("\t").append(o).append(" : ").append(
								utSettings.get(o)).append("\n");
					}
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	private void processPreferencesQueueing() {
		DirectConfigMigrate item;

		int maxActiveTorrents = MapUtils.getMapInt(utSettings,
				Queueing.MAX_ACTIVE_TORRENT, Queueing.MAX_ACTIVE_TORRENT_DEF);
		item = new DirectConfigMigrate(Queueing.MAX_ACTIVE_TORRENT,
				"max active torrents", maxActiveTorrents);
		listConfigMigrations.add(item);

		int maxDownloads = MapUtils.getMapInt(utSettings,
				Queueing.MAX_ACTIVE_DOWNLOADS, Queueing.MAX_ACTIVE_DOWNLOADS_DEF);
		item = new DirectConfigMigrate(Queueing.MAX_ACTIVE_DOWNLOADS,
				"max downloads", maxDownloads);
		listConfigMigrations.add(item);

		int minRatioX10 = MapUtils.getMapInt(utSettings, Queueing.SEED_RATIO_X10,
				Queueing.SEED_RATIO_DEF);
		if (minRatioX10 != Queueing.SEED_RATIO_DEF) {
			item = new DirectConfigMigrate(Queueing.SEED_RATIO_X10,
					"StartStopManager_iFirstPriority_ShareRatio", minRatioX10);
			listConfigMigrations.add(item);
		}

		///

		boolean limitUlRateOnSeedingGoal = getFlag(Queueing.SEED_PRIO_LIMITUL_FLAG,
				Queueing.SEED_PRIO_LIMITUL_FLAG_DEF);

		int minSeedingTime = MapUtils.getMapInt(utSettings, Queueing.SEED_TIME_SECS,
				Queueing.SEED_TIME_SECS_DEF);
		int minSeedingTimeMins = minSeedingTime / 60;
		if (minSeedingTime > 0) {
			// BiglyBT supports any minutes, but the UI config page only displays
			// 90 min, 2 hours, 3 hours, up to 13 hours
			if (minSeedingTimeMins < 90) {
				minSeedingTimeMins = 90;
			} else {
				// round up to nearest hour
				minSeedingTimeMins = (int) Math.ceil(minSeedingTimeMins / 60.0) * 60;
				if (minSeedingTimeMins > 13 * 60) {
					minSeedingTimeMins = 13 * 60;
				}
			}
		}
		item = new DirectConfigMigrate(Queueing.SEED_TIME_SECS, minSeedingTime,
				"StartStopManager_iFirstPriority_SeedingMinutes", minSeedingTimeMins);
		listConfigMigrations.add(item);

		///

		if (limitUlRateOnSeedingGoal) {
			int minSeeds = MapUtils.getMapInt(utSettings, Queueing.SEED_NUM,
					Queueing.SEED_NUM_DEF);
			if (minSeeds != Queueing.SEED_NUM_DEF) {
				logWarnings.append(
						"BiglyBT doesn't have a direct equivalent for Queueing->Seeding Goals->Min number of available seeds. ").append(
								"BiglyBT does have Ignore Rules that are limited based on min seeds.  ").append(
										"There's also the AutoPilot plugin which adds more auto-stop options.\n");
			}

			int limitUlRateTo = MapUtils.getMapInt(utSettings,
					Queueing.SEED_PRIO_LIMITUL, Queueing.SEED_PRIO_LIMITUL_DEF);
			if (limitUlRateTo == 0) {
				logWarnings.append(
						"BiglyBT doesn't have a direct equivalent for auto-stopping torrents based on seeding goals. ").append(
								"BiglyBT does have Ignore Rules that fullfill your needs. ").append(
										"There's also the AutoPilot plugin which adds more auto-stop options.\n");
			} else {
				logWarnings.append(
						"BiglyBT doesn't have a direct equivalent for altering a torrent's upload rate based on seeding goals. ").append(
								"You might be able to achieve your goals via Tagging logic, but that is beyond the scope of this migration tool.\n");
			}
		}
	}

	private void processPreferencesTransferCap() {
		boolean enableTransferCap = getFlag(TransferCap.MULTI_DAY_TRANSFER_LIMIT_EN,
				TransferCap.MULTI_DAY_TRANSFER_LIMIT_EN_DEF);
		if (enableTransferCap) {
			logWarnings.append(
					"Migration of Transfer Cap settings not currently supported.\n");
			logWarnings.append(
					"To manually create Transfer Caps in BiglyBT, see https://github.com/BiglySoftware/BiglyBT/wiki/Speed-Limit-Scheduler#Network_Limits\n");
			logWarnings.append(
					"For example, if you want a monthly cap of 200G, in Tools->Speed Limits->Schedule and Settings, you'd need the following line:\n");
			logWarnings.append("\tnet_limit monthly total=200G\n");
			logWarnings.append(
					"Make sure your stats start day is set to the right day in Tools->Options->Statistics->Long Term\n\n");
		}
	}

	private void processPreferencesGeneral() {
		DirectConfigMigrate item;
		boolean autoStart = getFlag(General.AUTOSTART, General.AUTOSTART_DEF);
		item = new DirectConfigMigrate(General.AUTOSTART,
				StartupShutdown.BCFG_START_ON_LOGIN, autoStart);
		listConfigMigrations.add(item);

		preAllocSpace = getFlag(General.PREALLOC_SPACE, General.PREALLOC_SPACE_DEF);
		item = new DirectConfigMigrate(General.PREALLOC_SPACE, BCFG_ZERO_NEW,
				preAllocSpace);
		if (!preAllocSpace) {
			// Even when BCFG_ZERO_NEW is off, BiglyBT allocates the whole size
			// Disabling BCFG_ENABLE_INCREMENTAL_FILE_CREATION allocates a 0 byte file, which is as close to the uT setting as we can get
			item.add(General.PREALLOC_SPACE, preAllocSpace,
					BCFG_ENABLE_INCREMENTAL_FILE_CREATION, true);
		}
		listConfigMigrations.add(item);
	}

	private void processPreferencesLabel() {
		boolean useAutoLabel = getFlag(label.USEAUTOLABEL, false);

		if (useAutoLabel) {
			Map<String, TagToAddInfo> mapNewLabel2Directory = new HashMap<>();

			Map mapLabelDirectory = MapUtils.getMapMap(utSettings,
					label.LABELDIRECTORYMAP, Collections.emptyMap());
			for (Object key : mapLabelDirectory.keySet()) {
				String label = (String) key;
				String dir = Utils.objectToString(mapLabelDirectory.get(key));
				TagToAddInfo tagToAddInfo = importer.addTag(null, label,
						TG_UTORRENT_DIRMAPPING);
				if (tagToAddInfo != null) {
					tagToAddInfo.initialSaveFolder = dir;
					mapNewLabel2Directory.put(label, tagToAddInfo);
				}
			}
			Map mapLabelRule = MapUtils.getMapMap(utSettings, label.LABELRULEMAP,
					Collections.emptyMap());
			for (Object key : mapLabelRule.keySet()) {
				String label = (String) key;
				String rule = Utils.objectToString(mapLabelRule.get(key));
				if (rule.startsWith("contains:")) {
					String containRule = rule.substring(9);
					TagToAddInfo tagToAddInfo = mapNewLabel2Directory.get(label);
					if (tagToAddInfo == null) {
						tagToAddInfo = importer.addTag(null, label, TG_UTORRENT_CONTAINS);
					}
					if (tagToAddInfo != null) {
						String constraint = "contains(name, \""
								+ containRule.replaceAll("\"", "\\\"") + "\", 1)"; // 1 = case insensitive
						if (tagToAddInfo.constraint != null) {
							tagToAddInfo.constraint = tagToAddInfo.constraint + " && "
									+ constraint;
						} else {
							tagToAddInfo.constraint = constraint;
						}
					}
				}
			}
		}
	}

	private void processPreferencesBandwidth() {
		DirectConfigMigrate item;
		///
		// Preferences -> Bandwidth
		////

		int maxUlRate = MapUtils.getMapInt(utSettings, Bandwidth.MAX_UL_RATE,
				Bandwidth.MAX_UL_RATE_DEF);
		item = new DirectConfigMigrate(Bandwidth.MAX_UL_RATE,
				ICFG_MAX_UPLOAD_SPEED_KBS, maxUlRate);
		listConfigMigrations.add(item);

		///

		boolean altUlRate = getFlag(Bandwidth.MAX_UL_RATE_SEED_FLAG,
				Bandwidth.MAX_UL_RATE_SEED_FLAG_DEF);
		item = new DirectConfigMigrate(Bandwidth.MAX_UL_RATE_SEED_FLAG,
				BCFG_ENABLE_SEEDINGONLY_MAXUPLOADS, altUlRate);

		int maxUlRateAlt = MapUtils.getMapInt(utSettings,
				Bandwidth.MAX_UL_RATE_SEED, Bandwidth.MAX_UL_RATE_SEED_DEF);
		item.add(Bandwidth.MAX_UL_RATE_SEED, ICFG_MAX_UPLOAD_SPEED_SEEDING_KBS,
				maxUlRateAlt);
		listConfigMigrations.add(item);

		///

		int maxDlRate = MapUtils.getMapInt(utSettings, Bandwidth.MAX_DL_RATE,
				Bandwidth.MAX_DL_RATE_DEF);
		item = new DirectConfigMigrate(Bandwidth.MAX_DL_RATE,
				ICFG_MAX_DOWNLOAD_SPEED_KBS, maxDlRate);
		listConfigMigrations.add(item);

		///

		boolean rateLimitIncludesTransport = getFlag(Bandwidth.NET_CALC_OVERHEAD,
				Bandwidth.NET_CALC_OVERHEAD_DEF);
		item = new DirectConfigMigrate(Bandwidth.NET_CALC_OVERHEAD,
				BCFG_DOWN_RATE_LIMITS_INCLUDE_PROTOCOL, rateLimitIncludesTransport);
		item.add(Bandwidth.NET_CALC_OVERHEAD, BCFG_UP_RATE_LIMITS_INCLUDE_PROTOCOL,
				rateLimitIncludesTransport);
		listConfigMigrations.add(item);

		///

		int maxConnections = MapUtils.getMapInt(utSettings,
				Bandwidth.CONNS_GLOBALLY, Bandwidth.CONNS_GLOBALLY_DEF);
		int maxConnsPerTorrent = MapUtils.getMapInt(utSettings,
				Bandwidth.CONNS_PER_TORRENT, Bandwidth.CONNS_PER_TORRENT_DEF);
		int uploadSlots = MapUtils.getMapInt(utSettings,
				Bandwidth.UL_SLOTS_PER_TORRENT, Bandwidth.UL_SLOTS_PER_TORRENT_DEF);
		if (maxConnections == Bandwidth.CONNS_GLOBALLY_DEF
				&& maxConnsPerTorrent == Bandwidth.CONNS_PER_TORRENT_DEF
				&& uploadSlots == Bandwidth.UL_SLOTS_PER_TORRENT_DEF) {
			// User didn't modify uT bandwidth settings, use "Auto" in BiglyBT
			item = new DirectConfigMigrate(null, BCFG_AUTO_ADJUST_TRANSFER_DEFAULTS,
					true);
			listConfigMigrations.add(item);
		} else {
			item = new DirectConfigMigrate(null, BCFG_AUTO_ADJUST_TRANSFER_DEFAULTS,
					false);
			item.add(Bandwidth.CONNS_GLOBALLY, ICFG_MAX_PEER_CONNECTIONS_TOTAL,
					maxConnections);
			item.add(Bandwidth.CONNS_PER_TORRENT,
					ICFG_MAX_PEER_CONNECTIONS_PER_TORRENT, maxConnsPerTorrent);
			item.add(Bandwidth.UL_SLOTS_PER_TORRENT, ICFG_MAX_UPLOADS, uploadSlots);
			listConfigMigrations.add(item);
		}
	}

	private void processPreferencesBitTorrent() {
		DirectConfigMigrate item;
		///
		// Preferences -> BitTorrent
		////

		boolean enableDHT = getFlag(BitTorrent.DHT, BitTorrent.DHT_DEF);
		item = new DirectConfigMigrate(BitTorrent.DHT, "Plugin.mlDHT.enable",
				enableDHT);
		item.add(BitTorrent.DHT, "Plugin.DHT.dht.enabled", enableDHT);
		listConfigMigrations.add(item);

		if (enableDHT) {
			importer.addRequiredPlugin(Importer_uTorrent.PLUGINID_MLDHT);
		}

		///

		boolean enableDHTnewTorrents = getFlag(BitTorrent.DHT_PER_TORRENT,
				BitTorrent.DHT_PER_TORRENT_DEF);
		item = new DirectConfigMigrate(BitTorrent.DHT_PER_TORRENT,
				BCFG_PREFIX_PEER_SRC_SELECTION_DEF + "DHT", enableDHTnewTorrents);
		// mlDHT is classified as  BCFG_PREFIX_PEER_SRC_SELECTION_DEF + "Plugin"
		// enable/disabling it also affects other plugins such as I2P
		item.add(BitTorrent.DHT_PER_TORRENT,
				BCFG_PREFIX_PEER_SRC_SELECTION_DEF + "Plugin", enableDHTnewTorrents);
		listConfigMigrations.add(item);

		///

		boolean enableLPD = getFlag(BitTorrent.LPD, BitTorrent.LPD_DEF);
		item = new DirectConfigMigrate(BitTorrent.LPD,
				"Plugin.azlocaltracker.Plugin.localtracker.enable", enableLPD);
		listConfigMigrations.add(item);

		///

		boolean useUDPTrackers = getFlag(BitTorrent.USE_UDP_TRACKERS,
				BitTorrent.USE_UDP_TRACKERS_DEF);
		item = new DirectConfigMigrate(BitTorrent.USE_UDP_TRACKERS,
				BCFG_SERVER_ENABLE_UDP, useUDPTrackers);
		listConfigMigrations.add(item);

		///

		String trackerIP = MapUtils.getMapString(utSettings, BitTorrent.TRACKER_IP,
				"");
		item = new DirectConfigMigrate(BitTorrent.TRACKER_IP, SCFG_OVERRIDE_IP,
				trackerIP);
		listConfigMigrations.add(item);

		///

		boolean enableScrape = getFlag(BitTorrent.ENABLE_SCRAPE,
				BitTorrent.ENABLE_SCRAPE_DEF);
		item = new DirectConfigMigrate(BitTorrent.ENABLE_SCRAPE,
				BCFG_TRACKER_CLIENT_SCRAPE_ENABLE, enableScrape);
		listConfigMigrations.add(item);

		///

		boolean enablePEX = getFlag(BitTorrent.PEX, BitTorrent.PEX_DEF);
		item = new DirectConfigMigrate(BitTorrent.PEX,
				BCFG_PREFIX_PEER_SRC_SELECTION_DEF + "PeerExchange", enablePEX);
		listConfigMigrations.add(item);

		///

		boolean limitLocalPeerBW = getFlag(BitTorrent.RATE_LIMIT_LOCAL_PEERS,
				BitTorrent.RATE_LIMIT_LOCAL_PEERS_DEF);
		item = new DirectConfigMigrate(BitTorrent.RATE_LIMIT_LOCAL_PEERS,
				limitLocalPeerBW, BCFG_LAN_SPEED_ENABLED, !limitLocalPeerBW);
		if (!limitLocalPeerBW) {
			item.add(BitTorrent.RATE_LIMIT_LOCAL_PEERS,
					ICFG_MAX_LAN_DOWNLOAD_SPEED_K_BS, 0);
			item.add(BitTorrent.RATE_LIMIT_LOCAL_PEERS,
					ICFG_MAX_LAN_UPLOAD_SPEED_K_BS, 0);
		}

		boolean altruistic = getFlag(BitTorrent.ENABLE_ALTRUISTIC,
				BitTorrent.ENABLE_ALTRUISTIC_DEF);
		if (altruistic) {
			logWarnings.append(
					"BiglyBT doesn't support altruistic mode, however you may want to look at the Share Ratio Maximizer plugin\n");
		}

		///

		int protEncryption = MapUtils.getMapInt(utSettings,
				BitTorrent.ENCRYPTION_MODE, BitTorrent.ENCRYPTION_MODE_DEF);
		if (protEncryption == 0) {
			item = new DirectConfigMigrate(BitTorrent.ENCRYPTION_MODE, protEncryption,
					BCFG_NETWORK_TRANSPORT_ENCRYPTED_REQUIRE, false);
			listConfigMigrations.add(item);
		} else if (protEncryption == 1) { // enable
			item = new DirectConfigMigrate(BitTorrent.ENCRYPTION_MODE, protEncryption,
					BCFG_NETWORK_TRANSPORT_ENCRYPTED_REQUIRE, true);
			item.add(BitTorrent.ENCRYPTION_MODE, protEncryption,
					SCFG_NETWORK_TRANSPORT_ENCRYPTED_MIN_LEVEL, "RC4");
			item.add(BitTorrent.ENCRYPTION_MODE, protEncryption,
					BCFG_NETWORK_TRANSPORT_ENCRYPTED_FALLBACK_OUTGOING, true);
			listConfigMigrations.add(item);
		} else if (protEncryption == 2) { // Forced
			item = new DirectConfigMigrate(BitTorrent.ENCRYPTION_MODE, protEncryption,
					BCFG_NETWORK_TRANSPORT_ENCRYPTED_REQUIRE, true);
			item.add(BitTorrent.ENCRYPTION_MODE, protEncryption,
					SCFG_NETWORK_TRANSPORT_ENCRYPTED_MIN_LEVEL, "RC4");
			item.add(BitTorrent.ENCRYPTION_MODE, protEncryption,
					BCFG_NETWORK_TRANSPORT_ENCRYPTED_FALLBACK_OUTGOING, false);
			listConfigMigrations.add(item);
		}

		///

		boolean protEncryptionLegacy = getFlag(BitTorrent.ENCRYPTION_ALLOW_LEGACY,
				BitTorrent.ENCRYPTION_ALLOW_LEGACY_DEF);
		item = new DirectConfigMigrate(BitTorrent.ENCRYPTION_ALLOW_LEGACY,
				BCFG_NETWORK_TRANSPORT_ENCRYPTED_FALLBACK_INCOMING,
				protEncryptionLegacy);
		listConfigMigrations.add(item);
	}

	private void processPreferencesConnection() {
		DirectConfigMigrate item;///
		// Preferences -> Connection
		////

		long bindPort = MapUtils.getMapLong(utSettings, Connection.BIND_PORT, 0);
		if (bindPort > 0) {
			item = new DirectConfigMigrate(Connection.BIND_PORT,
					ConfigKeys.Connection.ICFG_TCP_LISTEN_PORT, bindPort).add(
							Connection.BIND_PORT, ConfigKeys.Connection.ICFG_UDP_LISTEN_PORT,
							bindPort);
			listConfigMigrations.add(item);
		}

		///

		boolean enableUPNP = getFlag(Connection.UPNP, Connection.UPNP_DEF);
		item = new DirectConfigMigrate(Connection.UPNP, "Plugin.UPnP.upnp.enable",
				enableUPNP);
		listConfigMigrations.add(item);

		///

		boolean randomizePort = getFlag(Connection.RAND_PORT_ON_START,
				Connection.RAND_PORT_ON_START_DEF);
		item = new DirectConfigMigrate(Connection.RAND_PORT_ON_START,
				BCFG_LISTEN_PORT_RANDOMIZE_ENABLE, randomizePort);
		listConfigMigrations.add(item);

		///

		boolean enableNATPMP = getFlag(Connection.NATPMP, Connection.NATPMP_DEF);
		item = new DirectConfigMigrate(Connection.NATPMP,
				"Plugin.UPnP.natpmp.enable", enableNATPMP);
		listConfigMigrations.add(item);

		///
		// Preferences -> Connection -> Proxy Server
		////

		int proxyType = MapUtils.getMapInt(utSettings, Connection.PROXY_TYPE,
				Connection.PROXY_TYPE_DEF);
		if (proxyType == 0) {
			item = new DirectConfigMigrate(Connection.PROXY_TYPE, proxyType,
					BCFG_ENABLE_PROXY, false);
			listConfigMigrations.add(item);
		} else {
			item = new DirectConfigMigrate(Connection.PROXY_TYPE, proxyType,
					BCFG_ENABLE_PROXY, true);

			item.add(Connection.PROXY_TYPE, proxyType, BCFG_ENABLE_SOCKS,
					proxyType == 1 || proxyType == 2);

			String addr = MapUtils.getMapString(utSettings, Connection.PROXY_PROXY,
					"");
			item.add(Connection.PROXY_PROXY, SCFG_PROXY_HOST, addr);

			int port = MapUtils.getMapInt(utSettings, Connection.PROXY_PORT,
					Connection.PROXY_PORT_DEF);
			item.add(Connection.PROXY_PORT, port, SCFG_PROXY_PORT, "" + port);

			boolean doAuth = getFlag(Connection.PROXY_AUTH,
					Connection.PROXY_AUTH_DEF);
			if (doAuth) {
				String username = MapUtils.getMapString(utSettings,
						Connection.PROXY_USERNAME, "<none>");
				item.add(Connection.PROXY_USERNAME, SCFG_PROXY_USERNAME, username);

				String pw = MapUtils.getMapString(utSettings, Connection.PROXY_PASSWORD,
						"");
				item.add(Connection.PROXY_PASSWORD, SCFG_PROXY_PASSWORD, pw);
			} else {
				item.add(Connection.PROXY_USERNAME, SCFG_PROXY_USERNAME, "<none>");
				item.add(Connection.PROXY_PASSWORD, SCFG_PROXY_PASSWORD, "");
			}

			boolean proxyP2P = getFlag(Connection.PROXY_P2P,
					Connection.PROXY_P2P_DEF);
			item.add(Connection.PROXY_P2P, BCFG_PROXY_DATA_ENABLE, proxyP2P);
			if (proxyP2P) {
				item.add(Connection.PROXY_P2P, proxyP2P, BCFG_PROXY_DATA_SAME, true);
				item.add(Connection.PROXY_P2P, proxyP2P, SCFG_PROXY_DATA_SOCKS_VERSION,
						proxyType == 1 ? "V4" : "V5");
			}

			boolean disableLocalDNS = getFlag(Connection.NO_LOCAL_DNS,
					Connection.NO_LOCAL_DNS_DEF);
			item.add(Connection.NO_LOCAL_DNS, BCFG_PROXY_SOCKS_TRACKER_DNS_DISABLE,
					disableLocalDNS);

			listConfigMigrations.add(item);
		}
	}

	private void processPreferencesDirectories() {
		DirectConfigMigrate item;///
		// Preferences -> Directories
		////

		boolean customSavePath = getFlag(Directories.DIR_ACTIVE_DOWNLOAD_FLAG,
				false);
		if (customSavePath) {
			String savePath = MapUtils.getMapString(utSettings,
					Directories.DIR_ACTIVE_DOWNLOAD, "");
			if (savePath.length() > 0 && new File(savePath).isDirectory()) {
				item = new DirectConfigMigrate().addPrivate(
						Directories.DIR_ACTIVE_DOWNLOAD,
						ConfigKeys.File.SCFG_DEFAULT_SAVE_PATH, savePath);
				listConfigMigrations.add(item);
			} else {
				logWarnings.append("Invalid default save folder of ").append(
						Utils.wrapString(savePath)).append("\n");
			}
		}

		///

		boolean moveOnComplete = getFlag(Directories.DIR_COMPLETED_DOWNLOAD_FLAG,
				false);
		item = new DirectConfigMigrate(Directories.DIR_COMPLETED_DOWNLOAD_FLAG,
				ConfigKeys.File.BCFG_MOVE_COMPLETED_WHEN_DONE, moveOnComplete);
		String moveOnCompletePath = MapUtils.getMapString(utSettings,
				Directories.DIR_COMPLETED_DOWNLOAD, "");
		if (moveOnCompletePath.length() > 0
				&& new File(moveOnCompletePath).isDirectory()) {
			item.addPrivate(Directories.DIR_COMPLETED_DOWNLOAD,
					ConfigKeys.File.SCFG_COMPLETED_FILES_DIRECTORY, moveOnCompletePath);
			listConfigMigrations.add(item);
		} else if (moveOnComplete) {
			logWarnings.append("Invalid default save folder of ").append(
					Utils.wrapString(moveOnCompletePath)).append("\n");
		}
		if (moveOnComplete) {
			boolean addLabel = getFlag(Directories.DIR_COMPLETED_ADD_LABEL, false);
			if (addLabel) {
				logWarnings.append(
						"'Append the torrent's label' option on 'Move completed downloads' is not currently supported by BiglyBT.\n");
			}

			boolean moveOnlyIfDefault = getFlag(
					Directories.DIR_COMPLETED_MOVE_IF_DEFDIR, true);
			item.add(Directories.DIR_COMPLETED_MOVE_IF_DEFDIR,
					ConfigKeys.File.BCFG_MOVE_ONLY_WHEN_IN_DEFAULT_SAVE_DIR,
					moveOnlyIfDefault);
		}
		listConfigMigrations.add(item);

		///

		boolean saveTorrentFile = getFlag(Directories.DIR_TORRENT_FILES_FLAG,
				false);
		// uTorrent flag is different than BiglyBT in that is still makes a copy
		// of the .torrent file.  So we will keep BiglyBT flag as is
		//item = new DirectConfigMigrate(Directories.DIR_TORRENT_FILES_FLAG,
		//		ConfigKeys.File.BCFG_SAVE_TORRENT_FILES, saveTorrentFile);
		item = new DirectConfigMigrate();
		if (saveTorrentFile) {
			String saveTorrentFileDir = MapUtils.getMapString(utSettings,
					Directories.DIR_TORRENT_FILES, "");
			if (!saveTorrentFileDir.isEmpty()
					&& new File(saveTorrentFileDir).isDirectory()) {
				item.addPrivate(Directories.DIR_TORRENT_FILES,
						ConfigKeys.File.SCFG_GENERAL_DEFAULT_TORRENT_DIRECTORY,
						saveTorrentFileDir);
			} else {
				logWarnings.append("Invalid default .torrent save folder of").append(
						Utils.wrapString(saveTorrentFileDir)).append('\n');
			}
		}
		listConfigMigrations.add(item);

		///

		boolean moveTorrentFileOnComplete = getFlag(
				Directories.DIR_COMPLETED_TORRENTS_FLAG, false);
		item = new DirectConfigMigrate(Directories.DIR_COMPLETED_DOWNLOAD_FLAG,
				ConfigKeys.File.BCFG_MOVE_TORRENT_WHEN_DONE, moveTorrentFileOnComplete);
		String moveTorrentFileOnCompleteDir = MapUtils.getMapString(utSettings,
				Directories.DIR_COMPLETED_TORRENTS, "");
		if (!moveTorrentFileOnCompleteDir.isEmpty()
				&& new File(moveTorrentFileOnCompleteDir).isDirectory()) {
			item.addPrivate(Directories.DIR_COMPLETED_TORRENTS,
					ConfigKeys.File.SCFG_MOVE_TORRENT_WHEN_DONE_DIRECTORY,
					moveTorrentFileOnCompleteDir);
		}
		listConfigMigrations.add(item);
		if (moveTorrentFileOnComplete && !moveOnComplete) {
			logWarnings.append(
					"BiglyBT doesn't support moving .torrent files on completion without also moving data files on completion :(\n");
			// TODO: We could support this with tags
		}

		///

		boolean autoImportTorrents = getFlag(Directories.DIR_AUTOLOAD_FLAG, false);
		if (autoImportTorrents) {
			item = new DirectConfigMigrate(Directories.DIR_AUTOLOAD_FLAG,
					ConfigKeys.File.BCFG_WATCH_TORRENT_FOLDER, autoImportTorrents);

			String autoImportTorrentsDir = MapUtils.getMapString(utSettings,
					Directories.DIR_AUTOLOAD, "");
			if (!autoImportTorrentsDir.isEmpty()
					&& new File(autoImportTorrentsDir).exists()) {
				String key;
				int i = 0;
				while (true) {
					key = SCFG_PREFIX_WATCH_TORRENT_FOLDER_PATH
							+ (i == 0 ? "" : (" " + i));
					String existingWatchPath = COConfigurationManager.getStringParameter(key);
					if (existingWatchPath.isEmpty()) {
						break;
					}
					if (existingWatchPath.equalsIgnoreCase(autoImportTorrentsDir)) {
						key = null;
						break;
					}
					i++;
				}
				if (key != null) {
					item.addPrivate(Directories.DIR_AUTOLOAD, key, autoImportTorrentsDir);
				}

				logInfo.append(
						"You can also assign a tag to automatically imported torrents in Options->Files->Torrents\n");
				listConfigMigrations.add(item);
			}

			boolean deleteLoaded = getFlag(Directories.DIR_AUTOLOAD_DELETE, false);
			if (deleteLoaded) {
				logWarnings.append(
						"BiglyBT doesn't support deleting automatically imported .torrent files. Torrents auto-added from ").append(
								Utils.wrapString(autoImportTorrentsDir)).append(
										" will not be deleted.\n");
			}
		}
	}

	private void processPreferencesUISettings() {
		DirectConfigMigrate item;

		///
		// UI Settings -> Display Options
		////

		boolean confirmDeleteTorrents = getFlag(UI_Settings.CONFIRM_WHEN_DELETING,
				UI_Settings.CONFIRM_WHEN_DELETING_DEF);
		// When deleting "torrents", BiglyBT has separate options for deleting torrent content and torrent
		// Don't turn off delete prompt. Use can do it on first delete
		if (confirmDeleteTorrents) {
			item = new DirectConfigMigrate(UI_Settings.CONFIRM_WHEN_DELETING,
					confirmDeleteTorrents, "tb.confirm.delete.content", 0);
			listConfigMigrations.add(item);
		}

		///

		boolean confirmExit = getFlag(UI_Settings.CONFIRM_EXIT,
				UI_Settings.CONFIRM_EXIT_DEF);
		item = new DirectConfigMigrate(UI_Settings.CONFIRM_EXIT, confirmExit,
				"confirmationOnExit", confirmExit);
		listConfigMigrations.add(item);

		///

		boolean confirmTrackerDelete = getFlag(UI_Settings.CONFIRM_REMOVE_TRACKER,
				UI_Settings.CONFIRM_REMOVE_TRACKER_DEF);
		if (confirmTrackerDelete) {
			listConfigMigrations.add(
					new RememberedDecisionConfig(UI_Settings.CONFIRM_REMOVE_TRACKER,
							confirmExit, "removeTracker", -1));
		}

		///

		/** {@link UI_Settings#GUI_ALTERNATE_COLOR} explicitly not imported */

		///

		boolean speedInTitle = getFlag(UI_Settings.GUI_SPEED_IN_TITLE,
				UI_Settings.GUI_SPEED_IN_TITLE_DEF);
		item = new DirectConfigMigrate(UI_Settings.GUI_SPEED_IN_TITLE,
				"Show Status In Window Title", speedInTitle);
		listConfigMigrations.add(item);

		///

		// BiglyBT always has granular.  Torrent import will use this variable
		granularPriorities = getFlag(UI_Settings.GUI_GRANULAR_PRIORITY,
				UI_Settings.GUI_GRANULAR_PRIORITY_DEF);

		boolean confimExitCriticalSeeder = getFlag(
				UI_Settings.CONFIRM_EXIT_CRITICAL_SEEDER,
				UI_Settings.CONFIRM_EXIT_CRITICAL_SEEDER_DEF);
		if (confimExitCriticalSeeder) {
			logWarnings.append(
					"BiglyBT does not support a warning dialog on exit when you are a critical seeder.\n");
		}

		// UI Settings -> System Tray
		////

		boolean minimizeMinimizedToTray = getFlag(UI_Settings.MINIMIZE_TO_TRAY,
				UI_Settings.MINIMIZE_TO_TRAY_DEF);
		item = new DirectConfigMigrate(UI_Settings.MINIMIZE_TO_TRAY,
				"Minimize To Tray", minimizeMinimizedToTray);
		listConfigMigrations.add(item);

		///

		/** {@link UI_Settings#TRAY_SHOW} explicitely not imported.
		 * uT shows tray even when setting is off (and user closes to tray).
		 * BiglyBT's related config really removes system tray
		 */

		///

		boolean closeToTray = getFlag(UI_Settings.CLOSE_TO_TRAY,
				UI_Settings.CLOSE_TO_TRAY_DEF);
		item = new DirectConfigMigrate(UI_Settings.CLOSE_TO_TRAY, "Close To Tray",
				closeToTray);
		listConfigMigrations.add(item);

		///

		boolean addStopped = getFlag(UI_Settings.TORRENTS_START_STOPPED,
				UI_Settings.TORRENTS_START_STOPPED_DEF);
		item = new DirectConfigMigrate(UI_Settings.TORRENTS_START_STOPPED,
				BCFG_DEFAULT_START_TORRENTS_STOPPED, addStopped);
		listConfigMigrations.add(item);

		///

		boolean activateOnTorrentAdd = getFlag(UI_Settings.ACTIVATE_ON_FILE,
				UI_Settings.ACTIVATE_ON_FILE_DEF);
		item = new DirectConfigMigrate(UI_Settings.ACTIVATE_ON_FILE,
				"Activate Window On External Download", activateOnTorrentAdd);
		listConfigMigrations.add(item);

		///

		boolean showOpenOptionsDialog = getFlag(UI_Settings.SHOW_ADD_DIALOG,
				UI_Settings.SHOW_ADD_DIALOG_DEF);
		item = new DirectConfigMigrate(UI_Settings.SHOW_ADD_DIALOG,
				showOpenOptionsDialog, ConfigurationDefaults.CFG_TORRENTADD_OPENOPTIONS,
				ConfigurationDefaults.CFG_TORRENTADD_OPENOPTIONS_ALWAYS);
		listConfigMigrations.add(item);
	}

	private boolean getFlag(String key, boolean def) {
		return MapUtils.getMapBoolean(utSettings, key, def);
	}

	private void processPreferencesScheduler() {
		// Scheduler
		// uTorrent has 4 states.

		// Option 1:
		// We can mimic these with Speed Scheduler and Tags
		// - 1 tag to manage Speed, named "Speed Schedule", constraint "true"
		// - 1 tag to manage non-force start torrents, named "NonForceStart", constraint "!isForceStart()"
		//
		// Full Speed
		//   "Speed Schedule" tag set with max up/down set to global up/down speeds
		//   "NonForceStart" tag with max up/down set to 0
		// Limited
		//   "Speed Schedule" tag set with max up/down set to uTorrent's limited up/down
		//   "NonForceStart" tag with max up/down set to 0
		// Turn Off
		//   "Speed Schedule" tag set with max up/down set to global up/down speeds
		//   "NonForceStart" tag with max up/down set to -1
		// Seeding Only
		//   "Speed Schedule" tag set with max up/down set to global up/down speeds
		//   "NonForceStart" tag with max down set to -1, max up set to global up speed

		// Option 2:
		// Use tags only
		// * Easier for user to see/edit the speeds
		// * Harder for user to change time range if schedule is complex
		// 
		// Tags:
		// "LimitedSpeed" tag 
		//   * constraint using hour_of_day, day_of_week
		//   * max up/down set to uTorrent's limited up/down
		// "Turn Off" tag 
		//   * constraint using hour_of_day, day_of_week, and "!isForceStart()"
		//   * max up/down set to -1 (disabled)
		// "Seeding Only" tag
		//   * constraint using hour_of_day, day_of_week, and "!isForceStart()"
		//   * max down set to -1, max up set to global up speed
		//
		// The "Full Speed" option in uTorrent is BiglyBT's normal settings, so
		// no tag for it is required.  The three tags above will not be active
		// during "Full Speed" time, so speeds will default back to global

		boolean enableScheduler = getFlag(Scheduler.SCHED_ENABLE, false);
		if (!enableScheduler) {
			return;
		}

		/* Part of Option 1
		TagToAddInfo tagSpeedSchedule = importer.addTag(null, "Speed Schedule", "Imported uTorrent Scheduler");
		tagSpeedSchedule.constraint = "true";
		TagToAddInfo tagNonForceStart = importer.addTag(null, "NonForceStart", "Imported uTorrent Scheduler");
		tagNonForceStart.constraint = "!isForceStart()";
		*/

		byte[] schedule = MapUtils.getMapByteArray(utSettings,
				Scheduler.SCHED_TABLE, new byte[0]);
		if (schedule.length != 168) {
			logWarnings.append("Schedule incomplete.  Ignoring\n");
			return;
		}

		int numFullSpeed = 0;
		int numTurnOff = 0;
		for (byte hourState : schedule) {
			if (hourState == '0') {
				numFullSpeed++;
			} else if (hourState == '2') {
				numTurnOff++;
			}
		}

		if (numFullSpeed == 168) {
			logInfo.append("Every entry in Scheduler is Full Speed.  Skipping!");
			return;
		}

		if (numTurnOff > 0) {
			boolean disableDHTonTurnOff = getFlag(Scheduler.SCHED_DIS_DHT, true);
			if (disableDHTonTurnOff) {
				logWarnings.append(
						"BiglyBT does not support the uTorrent scheduler option to disable DHT when in 'Turn off' state.  DHT will remain on during those times.\n");
			}
		}

		int altDlRate = MapUtils.getMapInt(utSettings, Scheduler.SCHED_DL_RATE, 0);
		int altUlRate = MapUtils.getMapInt(utSettings, Scheduler.SCHED_UL_RATE, 0);

		int pos = 0;
		StringBuilder sbLimited = new StringBuilder();
		StringBuilder sbTurnOff = new StringBuilder();
		StringBuilder sbSeedingOnly = new StringBuilder();
		for (int weekday = 0; weekday < 7; weekday++) {
			int consecutive = 0;

			StringBuilder sbLimitedToday = new StringBuilder();
			StringBuilder sbTurnOffToday = new StringBuilder();
			StringBuilder sbSeedingOnlyToday = new StringBuilder();
			boolean hasEntries = false;
			for (int hour = 0; hour < 24; hour++, pos++) {

				byte hourState = schedule[pos];
				byte nextState = pos == 167 ? 0 : schedule[pos + 1];
				if (hourState == nextState) {
					consecutive++;
					continue;
				}

				StringBuilder sb;
				switch (hourState) {
					case '1': // Limited
						sb = sbLimitedToday;
						break;
					case '2': // Turn Off
						sb = sbTurnOffToday;
						break;
					case '3': // Seeding Only
						sb = sbSeedingOnlyToday;
						break;
					case '0': // Full Speed
					default:
						sb = null;
						break;
				}

				if (sb != null) {
					if (sb.length() > 0) {
						sb.append(" || ");
					}
					if (consecutive == 0) {
						sb.append("hour_of_day == ").append(hour);
					} else if (consecutive == 23) {
						sb.append(" "); // add blank because we rely on length > 0 to append day_of_week line
					} else {
						int start = hour - consecutive;
						if (start == 0) {
							sb.append("hour_of_day <= ").append(hour);
						} else if (hour == 23) {
							sb.append("hour_of_day >= ").append(start);

						} else {
							sb.append("(hour_of_day >= ").append(start).append(
									" && hour_of_day <= ").append(hour).append(")");
						}
					}
				}
				consecutive = 0;
			}

			// (day_of_week == 0 && ( (hour_of_day == 5) || (hour_of_day == 6) ))
			StringBuilder[] sbTodays = {
				sbLimitedToday,
				sbSeedingOnlyToday,
				sbTurnOffToday
			};
			StringBuilder[] sbs = {
				sbLimited,
				sbSeedingOnly,
				sbTurnOff
			};
			for (int i = 0, sbsLength = sbs.length; i < sbsLength; i++) {
				StringBuilder sb = sbs[i];
				StringBuilder sbToday = sbTodays[i];
				if (sbToday.length() > 0) {
					if (sb.length() > 0) {
						sb.append("\n || \n");
					}
					sb.append("(day_of_week == ").append(weekday);
					String today = sbToday.toString().trim();
					if (today.length() > 0) {
						sb.append(" && ( ").append(today).append(" )");
					}
					sb.append(")");
				}
			}
		}

		if (sbLimited.length() > 0) {
			TagToAddInfo tagLimited = importer.addTag(null, "SS.Limited",
					TG_SCHEDULER);
			tagLimited.maxUp = altUlRate;
			tagLimited.maxDown = altDlRate;

			tagLimited.constraint = sbLimited.toString();
		}
		if (sbSeedingOnly.length() > 0) {
			TagToAddInfo tagSeedingOnly = importer.addTag(null, "SS.SeedingOnly",
					TG_SCHEDULER);
			tagSeedingOnly.maxDown = -1;
			tagSeedingOnly.maxUp = 0;

			tagSeedingOnly.constraint = "!isForceStart() && (\n"
					+ sbSeedingOnly.toString() + "\n)";
		}
		if (sbTurnOff.length() > 0) {
			TagToAddInfo tagTurnOff = importer.addTag(null, "SS.TurnOff",
					TG_SCHEDULER);
			tagTurnOff.maxDown = -1;
			tagTurnOff.maxUp = -1;

			tagTurnOff.constraint = "!isForceStart() && (\n" + sbTurnOff.toString()
					+ "\n)";
		}
	}

	public String toDebugString(boolean showPrivate) {
		String s = toDebugString();
		if (showPrivate) {
			return s;
		}

		return Utils.hidePrivate(s);
	}

	public String toDebugString() {
		StringBuilder sb = new StringBuilder();
		if (logWarnings.length() > 0) {
			sb.append("\nConfig Warnings\n");
			sb.append("--------\n");
			sb.append(logWarnings).append("\n");
		}

		sb.append("Config Migrations\n");
		sb.append("-----------------\n");
		for (ConfigMigrateItem item : listConfigMigrations) {
			sb.append(item.toDebugString());
		}

		if (logInfo.length() > 0) {
			sb.append("\nInfo\n").append("----\n").append(logInfo).append("\n");
		}

		return sb.toString();
	}

	public void migrate() {
		for (ConfigMigrateItem item : listConfigMigrations) {
			item.migrate();
		}
	}
}

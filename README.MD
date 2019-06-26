# BiglyBT Plugin - Migrate Torrent App

```
This plugin is not yet functional
```

This BiglyBT plugin migrates uTorrent and BitTorrent client configurations and torrents to BiglyBT.

You will be able to review the changes before applying them.

## Features

### uTorrent/BitTorrent Client Migration

#### Torrents
1. Partial and Full Block completion status
1. File renames and relocations
1. File priorities
1. Statistics (bytes downloaded, uploaded, etc)
1. Speed Limits
1. Labels
   
#### Configuration
1. Directories (new downloads, move on complete, .torrent location, etc)
1. Port settings
1. Proxy settings
1. Bandwidth Limits
1. Protocol settings (DHT, uTP, PEX, etc)
1. Queue settings
1. Scheduler
1. Auto-Labels and Label-Directory Mappings

#### Skipped, Not Supported and Migration Notes

* "Enable DHT for new torrents" flag in uT will also change accepting peer sources from any plugin (including P2P)
* No "Altruistic Mode". Depending on your needs the Share Ratio Maximizer plugin might be a good replacement.
* Moving .torrent files on completion also required moving data files on completion (Plugin limitation, not a BiglyBT one. In BiglyBT, this can be done with Tags)
* "Confirm exit if critical seeder" is not supported
* 'Append the torrent's label' option on 'Move completed downloads' is not migrated.  BiglyBT can handle this feature by setting the "Move On Complete" directory on a specific Tag.
* BiglyBT doesn't support deleting automatically imported .torrent files
* BiglyBT does not support the uTorrent scheduler option to disable DHT when in 'Turn off' state
* Transfer Cap and daily bandwidth usage stats are not currently migrated.
  To manually create Transfer Caps in BiglyBT, see [Speed Limit Scheduler: Network Limits](https://github.com/BiglySoftware/BiglyBT/wiki/Speed-Limit-Scheduler#Network_Limits) wiki page.
  For example, if you want a monthly cap of 200G, in Tools->Speed Limits->Schedule and Settings, you'd need the following lines:
    ```
    net_limit monthly total=200G
    ```
  Make sure your stats start day is set to the right day in Tools->Options->Statistics->Long Term
* Adjusting or stopping torrents based on Seeding Goals are not compatible with BiglyBT's.  You will most likely need to rebuild your goals using First Priority Rules, Ignore Rules, Tagging logic, and/or the AutoPilot plugin.
* Anything related to Paired Devices is not imported from uT
* WebUI and uT Remote settings are not migrated.  BiglyBT has a Transmission compatible remote rpc that can be accessed via Tools->Remote Pairing, or by installing the xmwebui plugin.
* uT's Advanced->Run Program (global and per-torrent) migration not supported.  Equivilant functionality can be obtained using the Command Runner plugin.
* Disk Cache setting are not migrated
* Column setup is not migrated
* RSS Feeds are not migrated
* Most uT settings listed in Preferences->Advanced are not migrated yet
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

package com.biglybt.plugins.migratetorrentapp.swt;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.biglybt.core.internat.MessageText;
import com.biglybt.core.util.FileUtil;
import com.biglybt.pifimpl.local.ui.config.ParameterImpl;
import com.biglybt.pifimpl.local.ui.config.ParameterImplListener;
import com.biglybt.plugins.migratetorrentapp.utorrent.ConfigModel_uTorrent;
import com.biglybt.plugins.migratetorrentapp.utorrent.ConfigModel_uTorrent.MigrateListener;
import com.biglybt.plugins.migratetorrentapp.utorrent.Importer_uTorrent;
import com.biglybt.plugins.migratetorrentapp.utorrent.TagToAddInfo;
import com.biglybt.plugins.migratetorrentapp.utorrent.TorrentImportInfo;
import com.biglybt.ui.UIFunctions;
import com.biglybt.ui.UIFunctionsManager;
import com.biglybt.ui.swt.Messages;
import com.biglybt.ui.swt.Utils;
import com.biglybt.ui.swt.config.BaseSwtParameter;
import com.biglybt.ui.swt.pif.UISWTView;
import com.biglybt.ui.swt.pif.UISWTViewEvent;
import com.biglybt.ui.swt.pif.UISWTViewEventListener;
import com.biglybt.ui.swt.pifimpl.MultiParameterImplListenerSWT;
import com.biglybt.ui.swt.shells.MessageBoxShell;
import com.biglybt.ui.swt.skin.SWTSkinObjectText;
import com.biglybt.ui.swt.views.ConfigView;
import com.biglybt.ui.swt.views.skin.SkinnedDialog;

import com.biglybt.pif.PluginInterface;
import com.biglybt.pif.ui.config.Parameter;
import com.biglybt.pif.ui.config.ParameterListener;
import com.biglybt.pif.ui.model.BasicPluginConfigModel;
import com.biglybt.pif.utils.LocaleUtilities;

import static com.biglybt.plugins.migratetorrentapp.Utils.NL;
import static com.biglybt.plugins.migratetorrentapp.Utils.hidePrivate;

public class MigrateVEL_uTorrent
	implements UISWTViewEventListener, MigrateListener
{
	private UISWTView swtView;

	private Composite parent;

	private PluginInterface pi;

	LocaleUtilities localeUtilities;

	private ConfigModel_uTorrent configModelInfo;

	private StyledText resultTextArea;

	private ScrolledComposite sc;

	private Composite cResultsArea;

	private boolean showOnlyWarningTorrents = true;

	private boolean goingToRecalcSC;

	private Label lblStatus;

	private ParameterListener paramSAOListener;

	private SkinnedDialog dlgProgress;

	// Because migration or analysis may be so fast that dlgProcess might not be created until after completion event
	private boolean showPogressDialog = false;

	@Override
	public boolean eventOccurred(UISWTViewEvent event) {
		switch (event.getType()) {
			case UISWTViewEvent.TYPE_CREATE:
				swtView = (UISWTView) event.getData();
				pi = swtView.getPluginInterface();
				localeUtilities = pi.getUtilities().getLocaleUtilities();
				swtView.setTitle(localeUtilities.getLocalisedMessageText(
						"migrateapp.sidebar.title"));
				break;

			case UISWTViewEvent.TYPE_DESTROY:
				delete();
				break;

			case UISWTViewEvent.TYPE_INITIALIZE:
				initialize((Composite) event.getData());
				break;

			case UISWTViewEvent.TYPE_DATASOURCE_CHANGED:
				configModelInfo = (ConfigModel_uTorrent) event.getData();
				break;

			case UISWTViewEvent.TYPE_LANGUAGEUPDATE:
				Messages.updateLanguageForControl(getComposite());
				if (swtView != null) {
					swtView.setTitle(localeUtilities.getLocalisedMessageText(
							"migrateapp.sidebar.title"));
				}
				break;
		}

		return true;
	}

	private void delete() {
		if (configModelInfo != null) {
			configModelInfo.paramShowAdditionalOptions.removeListener(
					paramSAOListener);
			configModelInfo.removeListener(this);
		}
	}

	private void initialize(Composite parent) {
		this.parent = parent;
		if (configModelInfo == null) {
			// Bug in BBT <= 2.0.0.0 where view doesn't get the datasource if 
			// view was first loaded from previous launch.
			// We use datasource to get configModelInfo. Since this class is for uT,
			// we'll just snatch the stored instance.
			configModelInfo = ConfigModel_uTorrent.getInstance(pi);
		}

		sc = new ScrolledComposite(parent, SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.addListener(SWT.Resize, event -> {
			if (goingToRecalcSC) {
				return;
			}
			goingToRecalcSC = true;
			Utils.execSWTThreadLater(10, this::recalcScrolledComposite);
		});

		GridData gridData = new GridData(GridData.FILL_BOTH);
		sc.setLayoutData(gridData);

		Composite composite = new Composite(sc, SWT.NULL);

		sc.setContent(composite);

		gridData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gridData);

		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.verticalSpacing = 10;
		composite.setLayout(gridLayout);

		// Hack the config model into our composite. muah ha ha!

		BasicPluginConfigModel ourModel = configModelInfo.getConfigModel(
				pi.getUIManager(), MigrateTorrentAppUISWT.getSingleton());

		Parameter[] parameters = ourModel.getParameters();

		try {

			Map<ParameterImpl, BaseSwtParameter> map = new HashMap<>();

			Method mBuildScreen = ConfigView.class.getDeclaredMethod("buildScreen",
					Composite.class, Parameter[].class, Map.class,
					ParameterImplListener.class);
			mBuildScreen.setAccessible(true);
			mBuildScreen.invoke(null, composite, parameters, map,
					new MultiParameterImplListenerSWT(map));
		} catch (Exception e) {
			e.printStackTrace();
		}

		////

		Link lblReport = new Link(composite, SWT.WRAP);
		lblReport.setText(localeUtilities.getLocalisedMessageText(
				"migrateapp.reportissues", new String[] {
					"https://github.com/BiglySoftware/BiglyBT-plugin-migratetorrentapp/issues"
				}));
		lblReport.addListener(SWT.Selection, event -> Utils.launch(event.text));
		lblReport.setLayoutData(
				Utils.getWrappableLabelGridData(2, GridData.FILL_HORIZONTAL));

		lblStatus = new Label(composite, SWT.WRAP);
		lblStatus.setLayoutData(
				Utils.getWrappableLabelGridData(2, GridData.FILL_HORIZONTAL));

		cResultsArea = new Composite(composite, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		cResultsArea.setLayoutData(gridData);
		cResultsArea.setLayout(new GridLayout());

		paramSAOListener = param -> recalcScrolledComposite();
		configModelInfo.paramShowAdditionalOptions.addListener(paramSAOListener);

		configModelInfo.addListener(this);
	}

	private void buildResultsArea(Importer_uTorrent importer) {
		GridData gridData;

		Utils.disposeComposite(cResultsArea, false);

		Composite cButtonArea = new Composite(cResultsArea, SWT.NONE);
		gridData = new GridData();
		cButtonArea.setLayoutData(gridData);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		cButtonArea.setLayout(gridLayout);

		Button btnSaveAnalysis = new Button(cButtonArea, SWT.PUSH);
		Messages.setLanguageText(btnSaveAnalysis, "migrateapp.button.saveAnalysis");
		Label lblCopy = new Label(cButtonArea, SWT.WRAP);
		Messages.setLanguageText(lblCopy, "migrateapp.button.saveAnalysis.info");

		btnSaveAnalysis.addListener(SWT.Selection, event -> {
			FileDialog dialog = new FileDialog(Utils.findAnyShell(), SWT.SAVE);
			dialog.setText(MessageText.getString("migrateapp.button.saveAnalysis"));
			dialog.setFilterNames(new String[] {
				"Text Files",
				"All Files (*.*)"
			});
			dialog.setFilterExtensions(new String[] {
				"*.txt",
				"*.*"
			});
			dialog.setFileName("uT_Migrate_Analysis.txt");
			String name = dialog.open();
			if (name != null) {
				StringBuilder sb = buildAnalysisResults(importer, false, false);
				File file = new File(name);
				FileUtil.writeStringAsFile(file, hidePrivate(sb.toString()));
				UIFunctionsManager.getUIFunctions().showInExplorer(file);
			}
		});

		Button btnShowOnlyWarnings = new Button(cButtonArea, SWT.CHECK);
		btnShowOnlyWarnings.setSelection(showOnlyWarningTorrents);
		Messages.setLanguageText(btnShowOnlyWarnings,
				"migrateapp.checkbox.onlyWwarnings");
		btnShowOnlyWarnings.addListener(SWT.Selection, event -> {
			showOnlyWarningTorrents = btnShowOnlyWarnings.getSelection();
			StringBuilder sb = buildAnalysisResults(importer, showOnlyWarningTorrents,
					true);
			resultTextArea.setText(sb.toString());
			recalcScrolledComposite();
		});
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		btnShowOnlyWarnings.setLayoutData(gridData);

		Button btnMigrateConfig = new Button(cButtonArea, SWT.CHECK);
		btnMigrateConfig.setSelection(
				importer.settingsImportInfo.getMigrateConfig());
		Messages.setLanguageText(btnMigrateConfig,
				"migrateapp.options.migrateConfig");
		btnMigrateConfig.addListener(SWT.Selection,
				event -> importer.settingsImportInfo.setMigrateConfig(
						btnMigrateConfig.getSelection()));
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		btnMigrateConfig.setLayoutData(gridData);

		// Using StyledText instead of Text, because Text with focus messed up
		// scrolling with mouse wheel.
		resultTextArea = new StyledText(cResultsArea,
				SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		resultTextArea.setFont(
				com.biglybt.plugins.migratetorrentapp.swt.FontUtils.getMonospaceFont(
						resultTextArea.getDisplay(), 10));
		gridData = new GridData(GridData.FILL_BOTH);
		resultTextArea.setLayoutData(gridData);
		resultTextArea.addListener(SWT.KeyDown, event -> {
			int key = event.character;

			if (key <= 26 && key > 0) {
				key += 'a' - 1;
			}

			if (key == 'a' && event.stateMask == SWT.MOD1) {
				event.doit = false;
				resultTextArea.selectAll();
			}
		});
	}

	private void recalcScrolledComposite() {
		int width = sc.getClientArea().width;
		Point size = parent.computeSize(width, SWT.DEFAULT);
		sc.setMinSize(size);
		sc.layout(true, true);
		goingToRecalcSC = false;
	}

	@Override
	public void initMigrateListener() {
		// not needed after 2001
		Utils.execSWTThread(() -> {
			cResultsArea.forceFocus();
			Utils.disposeComposite(cResultsArea, false);
		}, false);
	}

	@Override
	public void analysisStart(Importer_uTorrent importer_uTorrent) {
		LocaleUtilities localeUtilities = pi.getUtilities().getLocaleUtilities();
		openProgressDialog(localeUtilities.getLocalisedMessageText(
				"migrateapp.analyzing", new String[] {
					configModelInfo.paramConfigDir.getValue()
				}));
	}

	public void openProgressDialog(String title) {
		synchronized (this) {
			showPogressDialog = true;
		}
		Utils.execSWTThread(() -> {
			synchronized (this) {
				if (!showPogressDialog) {
					return;
				}
				if (dlgProgress == null) {
					dlgProgress = new SkinnedDialog(this.getClass().getClassLoader(),
							"com/biglybt/plugins/migratetorrentapp/swt/",
							"skin3_dlg_progress", "shell", SWT.DIALOG_TRIM);
					dlgProgress.setTitle(title);
					dlgProgress.open();
				}
			}
		});
	}

	@Override
	public void analysisStatus(String status) {
		changeStatus(status);
	}

	private void changeStatus(String status) {
		Utils.execSWTThread(() -> {
			synchronized (this) {
				if (dlgProgress != null) {
					SWTSkinObjectText soStatusText = (SWTSkinObjectText) dlgProgress.getSkin().getSkinObject(
							"status-text");
					if (soStatusText != null) {
						soStatusText.setText(status);
						Shell shell = dlgProgress.getShell();
						Point computeSize = shell.computeSize(shell.getClientArea().width,
								SWT.DEFAULT);
						shell.setSize(computeSize);
					}
				}
			}
			if (lblStatus == null || lblStatus.isDisposed()) {
				return;
			}
			lblStatus.setText(status);
			lblStatus.requestLayout();
		});
	}

	@Override
	public void migrationStatus(String status) {
		changeStatus(status);
	}

	@Override
	public void analysisComplete(Importer_uTorrent importer) {
		StringBuilder sb = buildAnalysisResults(importer, showOnlyWarningTorrents,
				true);
		closeProgressDialog();

		Utils.execSWTThread(() -> {
			buildResultsArea(importer);
			resultTextArea.setText(sb.toString());
			recalcScrolledComposite();
		});
	}

	@Override
	public void migrationStart(Importer_uTorrent importer_uTorrent) {
		LocaleUtilities localeUtilities = pi.getUtilities().getLocaleUtilities();
		openProgressDialog(localeUtilities.getLocalisedMessageText(
				"migrateapp.migrating", new String[] {
					configModelInfo.paramConfigDir.getValue()
				}));
	}

	@Override
	public void migrationComplete(String migrateLog) {
		closeProgressDialog();

		MessageBoxShell mb = new MessageBoxShell(
				MessageText.getString("migrateapp.results.title"),
				MessageText.getString("migrateapp.migrated.restart.recommended"),
				new String[] {
					MessageText.getString("UpdateWindow.restart"),
					MessageText.getString("UpdateWindow.restartLater"),
				}, 0);
		mb.setHtml("<pre style=\"font-size:70%\">" + migrateLog + "</pre>");
		mb.open(result -> {
			if (result != 0) {
				return;
			}
			UIFunctions uif = UIFunctionsManager.getUIFunctions();
			if (uif != null) {
				uif.dispose(true, false);
			}
		});

	}

	private void closeProgressDialog() {
		synchronized (this) {
			showPogressDialog = false;
			if (dlgProgress != null) {
				dlgProgress.close();
				dlgProgress = null;
			}
		}
	}

	private StringBuilder buildAnalysisResults(Importer_uTorrent importer,
			boolean onlyWarningTorrents, boolean autoTrim) {
		StringBuilder sb = new StringBuilder();

		if (!importer.canMigrate()) {
			for (String s : importer.needsPathReplacement.keySet()) {
				sb.append("Require folder replacement for paths starting with ").append(
						s).append(NL);
				String[] strings = importer.needsPathReplacement.get(s).toArray(
						new String[0]);
				Arrays.sort(strings);
				int maxToShow = 20;
				for (int i = 0, stringsLength = strings.length; i < stringsLength
						&& i < maxToShow; i++) {
					String string = strings[i];
					sb.append("\t").append(string).append(NL);
				}
				if (strings.length > maxToShow) {
					sb.append("\t.. and ").append(strings.length - maxToShow).append(
							" others");
				}
			}
			return sb;
		}

		String nl = NL + "│ ";
		String s;

		sb.append("┌─────────────────────────────────────────────────").append(nl);
		s = importer.settingsImportInfo.toDebugString(
				showOnlyWarningTorrents).replaceAll(NL, nl);
		sb.append(s);

		sb.append(NL);
		sb.append("└──────────────────────────────────────────────────");
		sb.append(NL).append(NL);

		sb.append("┌─────────────────────────────────────────────────").append(nl);
		sb.append(importer.listTorrentsToImport.size()).append(
				" torrents found in uT. Any torrents already in BiglyBT will be skipped.").append(
						nl);
		for (TorrentImportInfo importInfo : importer.listTorrentsToImport) {
			if (onlyWarningTorrents && !importInfo.hasWarnings()) {
				continue;
			}

			boolean showFullDetails = !autoTrim
					|| sb.length() < Integer.MAX_VALUE / 2;

			sb.append(NL);
			sb.append("├─────────────────────────────────────────────────");
			sb.append(nl).append(nl);
			s = importInfo.toDebugString(showFullDetails).replaceAll(NL, nl);
			sb.append(s);
		}
		sb.append(NL);
		sb.append("└─────────────────────────────────────────────────");
		sb.append(NL).append(NL);

		sb.append("┌─────────────────────────────────────────────────").append(nl);
		sb.append("Tags").append(nl);
		sb.append("----");
		if (importer.mapTagsToAdd.size() == 0) {
			sb.append(nl).append("No labels found to import as tags");
		}
		for (TagToAddInfo value : importer.mapTagsToAdd.values()) {
			sb.append(nl);
			sb.append(value.toDebugString());
		}
		sb.append(NL);
		sb.append("└──────────────────────────────────────────────────");
		return sb;
	}

	public Composite getComposite() {
		return parent;
	}
}

/**
 * Copyright 2016 By_syk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.by_syk.netupdown.service;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.by_syk.netupdown.util.C;

/**
 * Created by By_syk on 2016-11-12.
 */

@TargetApi(24)
public class QSTileService extends TileService {
//    @Override
//    public void onTileAdded() {
//        super.onTileAdded();
//    }

//    @Override
//    public void onTileRemoved() {
//        super.onTileRemoved();
//    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        if (NetTrafficService.isRunning) {
            switchState(true);
        } else {
            switchState(false);
        }
    }

//    @Override
//    public void onStopListening() {
//        super.onStopListening();
//    }

    @Override
    public void onClick() {
        super.onClick();

        if (C.SDK >= 23 && !Settings.canDrawOverlays(this)) { // No permission
            return;
        }

        if (!NetTrafficService.isRunning) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            sp.edit().putBoolean("run", true).commit();
            startService(new Intent(getApplicationContext(), NetTrafficService.class));

            switchState(true);
        } else {
            stopService(new Intent(getApplicationContext(), NetTrafficService.class));

            switchState(false);
        }
    }

    private void switchState(boolean isActive) {
        Tile tile = getQsTile();
        if (tile != null) {
            if (isActive) {
                tile.setState(Tile.STATE_ACTIVE);
            } else {
                tile.setState(Tile.STATE_INACTIVE);
            }

            tile.updateTile();
        }
    }
}

/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.policy;

import static android.view.KeyEvent.KEYCODE_ALT_LEFT;
import static android.view.KeyEvent.KEYCODE_B;
import static android.view.KeyEvent.KEYCODE_C;
import static android.view.KeyEvent.KEYCODE_CTRL_LEFT;
import static android.view.KeyEvent.KEYCODE_E;
import static android.view.KeyEvent.KEYCODE_K;
import static android.view.KeyEvent.KEYCODE_M;
import static android.view.KeyEvent.KEYCODE_META_LEFT;
import static android.view.KeyEvent.KEYCODE_N;
import static android.view.KeyEvent.KEYCODE_P;
import static android.view.KeyEvent.KEYCODE_S;
import static android.view.KeyEvent.KEYCODE_SLASH;
import static android.view.KeyEvent.KEYCODE_SPACE;
import static android.view.KeyEvent.KEYCODE_TAB;
import static android.view.KeyEvent.KEYCODE_U;
import static android.view.KeyEvent.KEYCODE_Z;

import android.content.Intent;
import android.os.RemoteException;
import android.util.SparseArray;

import org.junit.Test;

public class ModifierShortcutTests extends ShortcutKeyTestBase {
    private static final SparseArray<String> META_SHORTCUTS =  new SparseArray<>();
    static {
        META_SHORTCUTS.append(KEYCODE_U, Intent.CATEGORY_APP_CALCULATOR);
        META_SHORTCUTS.append(KEYCODE_B, Intent.CATEGORY_APP_BROWSER);
        META_SHORTCUTS.append(KEYCODE_C, Intent.CATEGORY_APP_CONTACTS);
        META_SHORTCUTS.append(KEYCODE_E, Intent.CATEGORY_APP_EMAIL);
        META_SHORTCUTS.append(KEYCODE_K, Intent.CATEGORY_APP_CALENDAR);
        META_SHORTCUTS.append(KEYCODE_M, Intent.CATEGORY_APP_MAPS);
        META_SHORTCUTS.append(KEYCODE_P, Intent.CATEGORY_APP_MUSIC);
        META_SHORTCUTS.append(KEYCODE_S, Intent.CATEGORY_APP_MESSAGING);
    }

    /**
     * Test meta+ shortcuts defined in bookmarks.xml.
     */
    @Test
    public void testMetaShortcuts() {
        for (int i = 0; i < META_SHORTCUTS.size(); i++) {
            final int keyCode = META_SHORTCUTS.keyAt(i);
            final String category = META_SHORTCUTS.valueAt(i);

            sendKeyCombination(new int[]{KEYCODE_META_LEFT, keyCode}, 0);
            mPhoneWindowManager.assertLaunchCategory(category);
        }
    }

    /**
     * ALT + TAB to show recent apps.
     */
    @Test
    public void testAltTab() {
        mPhoneWindowManager.overrideStatusBarManagerInternal();
        sendKeyCombination(new int[]{KEYCODE_ALT_LEFT, KEYCODE_TAB}, 0);
        mPhoneWindowManager.assertShowRecentApps();
    }

    /**
     * CTRL + SPACE to switch keyboard layout.
     */
    @Test
    public void testCtrlSpace() {
        sendKeyCombination(new int[]{KEYCODE_CTRL_LEFT, KEYCODE_SPACE}, 0);
        mPhoneWindowManager.assertSwitchKeyboardLayout();
    }

    /**
     * META + SPACE to switch keyboard layout.
     */
    @Test
    public void testMetaSpace() {
        sendKeyCombination(new int[]{KEYCODE_META_LEFT, KEYCODE_SPACE}, 0);
        mPhoneWindowManager.assertSwitchKeyboardLayout();
    }

    /**
     * CTRL + ALT + Z to enable accessibility service.
     */
    @Test
    public void testCtrlAltZ() {
        sendKeyCombination(new int[]{KEYCODE_CTRL_LEFT, KEYCODE_ALT_LEFT, KEYCODE_Z}, 0);
        mPhoneWindowManager.assertAccessibilityKeychordCalled();
    }

    /**
     * META + CTRL+ S to take screenshot.
     */
    @Test
    public void testMetaCtrlS() {
        sendKeyCombination(new int[]{KEYCODE_META_LEFT, KEYCODE_CTRL_LEFT, KEYCODE_S}, 0);
        mPhoneWindowManager.assertTakeScreenshotCalled();
    }

    /**
     * META + N to expand notification panel.
     */
    @Test
    public void testMetaN() throws RemoteException {
        mPhoneWindowManager.overrideExpandNotificationsPanel();
        sendKeyCombination(new int[]{KEYCODE_META_LEFT, KEYCODE_N}, 0);
        mPhoneWindowManager.assertExpandNotification();
    }

    /**
     * META + SLASH to toggle shortcuts menu.
     */
    @Test
    public void testMetaSlash() {
        mPhoneWindowManager.overrideStatusBarManagerInternal();
        sendKeyCombination(new int[]{KEYCODE_META_LEFT, KEYCODE_SLASH}, 0);
        mPhoneWindowManager.assertToggleShortcutsMenu();
    }

    /**
     * META  + ALT to toggle Cap Lock.
     */
    @Test
    public void testMetaAlt() {
        sendKeyCombination(new int[]{KEYCODE_META_LEFT, KEYCODE_ALT_LEFT}, 0);
        mPhoneWindowManager.assertToggleCapsLock();
    }
}

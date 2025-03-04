/*
 * Copyright (C) 2022 Paranoid Android
 * Copyright (C) 2023 The XPerience Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.util;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.R;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PropImitationHooks {

    private static final String TAG = "PropImitationHooks";
    private static final boolean DEBUG = false;

    private static final String sCertifiedFp =
            Resources.getSystem().getString(R.string.config_certifiedFingerprint);

    private static final String sStockFp =
            Resources.getSystem().getString(R.string.config_stockFingerprint);

    private static final String PACKAGE_ARCORE = "com.google.ar.core";
    private static final String PACKAGE_FINSKY = "com.android.vending";
    private static final String PACKAGE_GMS = "com.google.android.gms";
    private static final String PROCESS_GMS_UNSTABLE = PACKAGE_GMS + ".unstable";

    private static final String PACKAGE_GPHOTOS = "com.google.android.apps.photos";
    private static final Map<String, Object> sP1Props = new HashMap<>();
    static {
        sP1Props.put("BRAND", "google");
        sP1Props.put("MANUFACTURER", "Google");
        sP1Props.put("DEVICE", "marlin");
        sP1Props.put("PRODUCT", "marlin");
        sP1Props.put("MODEL", "Pixel XL");
        sP1Props.put("FINGERPRINT", "google/marlin/marlin:10/QP1A.191005.007.A3/5972272:user/release-keys");
    }
    private static final String[] sFeaturesBlacklist = {
        "PIXEL_2017_PRELOAD",
        "PIXEL_2018_PRELOAD",
        "PIXEL_2019_MIDYEAR_PRELOAD",
        "PIXEL_2019_PRELOAD",
        "PIXEL_2020_EXPERIENCE",
        "PIXEL_2020_MIDYEAR_EXPERIENCE",
        "PIXEL_2021_EXPERIENCE",
        "PIXEL_2021_MIDYEAR_EXPERIENCE"
    };

    private static final String PACKAGE_VELVET = "com.google.android.quicksearchbox";
    private static final String PACKAGE_WALLPAPERS = "com.google.android.apps.wallpaper";
    private static final Map<String, Object> sP6Props = new HashMap<>();
    static {
        sP6Props.put("BRAND", "google");
        sP6Props.put("MANUFACTURER", "Google");
        sP6Props.put("DEVICE", "raven");
        sP6Props.put("PRODUCT", "raven");
        sP6Props.put("MODEL", "Pixel 6 Pro");
        sP6Props.put("FINGERPRINT", "google/raven/raven:13/TQ2A.230405.003.E1/9802792:user/release-keys");
    }

    //cheetah
    private static final String PACKAGE_TURBO = "com.google.android.apps.turbo";
    private static final String PACKAGE_SETUPWIZARD = "com.google.android.setupwizard";
    private static final String PACKAGE_GBOARD = "com.google.android.inputmethod.latin";
    private static final Map<String, Object> sP7Props = new HashMap<>();
    static {
        sP7Props.put("BRAND", "google");
        sP7Props.put("MANUFACTURER", "Google");
        sP7Props.put("DEVICE", "cheetah");
        sP7Props.put("PRODUCT", "cheetah");
        sP7Props.put("MODEL", "Pixel 7 Pro");
        sP7Props.put("FINGERPRINT", "google/cheetah/cheetah:13/TQ1A.230205.001.D2/9471403:user/release-keys");
    }

    private static final Map<String, Object> xUserProps = new HashMap<>();
    static {
        xUserProps.put("TYPE", "user");
        xUserProps.put("TAGS", "release-keys");
    }

    private static final boolean sSpoofGapps =
            Resources.getSystem().getBoolean(R.bool.config_spoofGoogleApps);

    private static final String PACKAGE_NETFLIX = "com.netflix.mediaclient";
    private static final String sNetflixModel =
            Resources.getSystem().getString(R.string.config_netflixSpoofModel);

    //dolby Atmos
    private static final String PACKAGE_DAX_UI = "com.dolby.daxappui";
    private static final String PACKAGE_DAX_SERVICE = "com.dolby.daxservice";
    private static final boolean sDolbyAtmos =
            Resources.getSystem().getBoolean(R.bool.config_dolbyAtmosSpoof);
    private static final Map<String, Object> sDolbyAtmosProps = new HashMap<>();
    static {
        sDolbyAtmosProps.put("ro.vendor.product.device.db", "OP_DEVICE");
        sDolbyAtmosProps.put("ro.vendor.product.manufacturer.db", "OP_PHONE");
        sDolbyAtmosProps.put("vendor.product.device", "OP_PHONE");
        sDolbyAtmosProps.put("vendor.product.manufacturer", "OPD");
    }

    private static volatile boolean sIsGms = false;
    private static volatile boolean sIsFinsky = false;
    private static volatile boolean sIsPhotos = false;

    public static void setProps(Context context) {
        final String packageName = context.getPackageName();
        final String processName = Application.getProcessName();

        if (TextUtils.isEmpty(packageName) || processName == null) {
            return;
        }

        sIsGms = packageName.equals(PACKAGE_GMS) && processName.equals(PROCESS_GMS_UNSTABLE);
        sIsFinsky = packageName.equals(PACKAGE_FINSKY);
        sIsPhotos = sSpoofGapps && packageName.equals(PACKAGE_GPHOTOS);
        dlog("Spoofing compile type to 'user' even if it is 'USER' build ");
        xUserProps.forEach((k, v) -> setPropValue(k, v));

        if (!sCertifiedFp.isEmpty() && sIsGms) {
            dlog("Spoofing build for GMS");
            setPropValue("FINGERPRINT", sCertifiedFp);
            setPropValue("MODEL", Build.MODEL + "\u200b");
        } else if (!sStockFp.isEmpty() && packageName.equals(PACKAGE_ARCORE)) {
            dlog("Setting stock fingerprint for: " + packageName);
            setPropValue("FINGERPRINT", sStockFp);
        } else if (sIsPhotos) {
            dlog("Spoofing Pixel XL for Google Photos");
            sP1Props.forEach((k, v) -> setPropValue(k, v));
        } else if (sSpoofGapps && (packageName.equals(PACKAGE_VELVET)
                || packageName.equals(PACKAGE_WALLPAPERS))) {
            dlog("Spoofing Pixel 6 Pro for: " + packageName);
            sP6Props.forEach((k, v) -> setPropValue(k, v));
        } else if (!sNetflixModel.isEmpty() && packageName.equals(PACKAGE_NETFLIX)) {
            dlog("Setting model to " + sNetflixModel + " for Netflix");
            setPropValue("MODEL", sNetflixModel);
        } else if (sDolbyAtmos && packageName.equals(PACKAGE_DAX_UI) && packageName.equals(PACKAGE_DAX_SERVICE)) {
            dlog("Spoofing OnePlus device for: " + packageName);
            sDolbyAtmosProps.forEach((k, v) -> setPropValue(k, v));
        } else if (sSpoofGapps && packageName.equals(PACKAGE_TURBO) || packageName.equals(PACKAGE_GBOARD) || packageName.equals(PACKAGE_SETUPWIZARD)) {
            dlog("Spoofing Pixel 7 Pro for: " + packageName);
            sP7Props.forEach((k, v) -> setPropValue(k, v));
        }
    }

    private static void setPropValue(String key, Object value){
        try {
            dlog("Setting prop " + key + " to " + value.toString());
            Field field = Build.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set prop " + key, e);
        }
    }

    private static boolean isCallerSafetyNet() {
        return sIsGms && Arrays.stream(Thread.currentThread().getStackTrace())
                .anyMatch(elem -> elem.getClassName().contains("DroidGuard"));
    }

    public static void onEngineGetCertificateChain() {
        // Check stack for SafetyNet or Play Integrity
        if (isCallerSafetyNet() || sIsFinsky) {
            dlog("Blocked key attestation sIsGms=" + sIsGms + " sIsFinsky=" + sIsFinsky);
            throw new UnsupportedOperationException();
        }
    }

    public static boolean hasSystemFeature(String name, boolean def) {
        if (sIsPhotos && def &&
                Arrays.stream(sFeaturesBlacklist).anyMatch(name::contains)) {
            dlog("Blocked system feature " + name + " for Google Photos");
            return false;
        }
        return def;
    }

    public static void dlog(String msg) {
      if (DEBUG) Log.d(TAG, msg);
    }
}

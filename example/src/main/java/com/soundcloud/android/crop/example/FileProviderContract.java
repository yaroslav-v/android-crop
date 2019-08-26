package com.soundcloud.android.crop.example;

import android.net.Uri;

public class FileProviderContract {

    /**
     * The authority for the contacts provider
     */
    public static final String AUTHORITY = "com.soundcloud.android.crop.example.fileprovider";

    /**
     * A content:// style uri to the authority for the contacts provider
     */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
}

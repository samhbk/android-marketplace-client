package com.marketplace.app.util

import com.marketplace.app.BuildConfig

object ImageUrl {

    fun resolve(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        val base = BuildConfig.API_BASE_URL.trimEnd('/')
        val p = if (path.startsWith("/")) path else "/$path"
        return base + p
    }
}

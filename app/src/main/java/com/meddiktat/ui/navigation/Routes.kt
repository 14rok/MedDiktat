package com.meddiktat.ui.navigation

/** Zentrale Routen-Definitionen der App. */
object Routes {
    const val LIST = "list"
    const val RECORD = "record"

    const val ARG_DICTATION_ID = "dictationId"
    const val DETAIL = "detail/{$ARG_DICTATION_ID}"

    fun detail(id: String): String = "detail/$id"
}

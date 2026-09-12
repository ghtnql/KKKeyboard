package com.ghtnql.kkkeyboard

import android.view.inputmethod.EditorInfo

/**
 * Resolves the editor action shown by the host field without doing any I/O.
 * Keeping this decision pure makes Enter behavior cheap in the IME hot path
 * and easy to regression-test.
 */
object EnterActionResolver {
    fun actionId(imeOptions: Int): Int? {
        if ((imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) return null

        return when (val action = imeOptions and EditorInfo.IME_MASK_ACTION) {
            EditorInfo.IME_ACTION_GO,
            EditorInfo.IME_ACTION_SEARCH,
            EditorInfo.IME_ACTION_SEND,
            EditorInfo.IME_ACTION_NEXT,
            EditorInfo.IME_ACTION_DONE,
            EditorInfo.IME_ACTION_PREVIOUS,
            -> action

            else -> null
        }
    }
}

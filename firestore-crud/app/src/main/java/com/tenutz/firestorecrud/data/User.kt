package com.tenutz.firestorecrud.data

import com.chibatching.kotpref.KotprefModel

object User: KotprefModel() {

    var provider: String by stringPref()
}
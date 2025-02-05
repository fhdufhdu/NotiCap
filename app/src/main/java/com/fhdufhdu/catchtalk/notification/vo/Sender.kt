package com.fhdufhdu.catchtalk.notification.vo

import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat

data class Sender(
    val name: String,
    val icon: IconCompat?,
) {
    companion object {
        fun from(person: Person) =
            Sender(
                name = person.name.toString(),
                icon = person.icon,
            )
    }
}

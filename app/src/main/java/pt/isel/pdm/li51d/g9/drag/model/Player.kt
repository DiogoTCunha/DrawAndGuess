package pt.isel.pdm.li51d.g9.drag.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Player (var points: Int = 0) : Parcelable {

    fun addPoint() {
        this.points++
    }

}
package pt.isel.pdm.li51d.g9.drag.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import pt.isel.pdm.li51d.g9.drag.model.figures.Drawing

@Parcelize
data class Round(val drawings : MutableList<Drawing> = ArrayList(), val words : MutableList<String> = ArrayList()) : Parcelable
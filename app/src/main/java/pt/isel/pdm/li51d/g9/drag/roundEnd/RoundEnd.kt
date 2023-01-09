package pt.isel.pdm.li51d.g9.drag.roundEnd

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import pt.isel.pdm.li51d.g9.drag.DragApplication
import pt.isel.pdm.li51d.g9.drag.model.Player
import pt.isel.pdm.li51d.g9.drag.model.Round

class RoundEndModel(application: Application,
                    private val savedState: SavedStateHandle
) : AndroidViewModel(application) {

    private val application : DragApplication by lazy {
        getApplication<DragApplication>()
    }

    fun getRound(roundNumber : Int) : Round? {
        return application.getRound(roundNumber)
    }

    fun getPlayers () : List<Player>? {
        return application.getPlayers()
    }


}

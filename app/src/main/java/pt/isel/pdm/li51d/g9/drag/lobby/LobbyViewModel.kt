package pt.isel.pdm.li51d.g9.drag.lobby

import pt.isel.pdm.li51d.g9.drag.initialScreen.InitialScreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import pt.isel.pdm.li51d.g9.drag.*

/**
 * The View Model to be used in the [InitialScreen].
 *
 * Challenges are created by participants and are posted on the server, awaiting acceptance.
 */
class LobbyViewModel(app: Application) : AndroidViewModel(app) {
    private val app: DragApplication by lazy {
        getApplication<DragApplication>()
    }
    private var lobbylistener : ListenerRegistration? = null
    val lobby : MutableLiveData<LobbyInfo> = MutableLiveData()

    fun subscribeToLobby(id : String) {
        lobbylistener = app.repo.subscribeToLobby(id,{

        },{

            lobby.postValue(it)
        })
    }

    fun leaveLobby(lobbyId: String) {
        app.repo.leaveLobby(lobbyId)
    }

    fun updateLobbyOnStart(lobbyId: String) {
        app.repo.updateLobbyOnStart(lobbyId)
    }

    fun joinGame(lobbyId :String, onSuccess: (String) -> Unit,OnError : (Exception) -> Unit) {
        app.repo.joinGame(lobbyId,onSuccess,OnError)
    }

    fun unsubcribeFromLobby() {
        lobbylistener?.remove()
    }

}

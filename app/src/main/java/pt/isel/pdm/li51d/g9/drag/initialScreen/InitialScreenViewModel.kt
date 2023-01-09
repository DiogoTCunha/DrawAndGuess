package pt.isel.pdm.li51d.g9.drag.initialScreen

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import pt.isel.pdm.li51d.g9.drag.*
import java.lang.Thread.currentThread
import java.util.concurrent.Executors


/**
 * The View Model to be used in the [InitialScreen].
 *
 * Challenges are created by participants and are posted on the server, awaiting acceptance.
 */
class InitialScreenViewModel(app: Application) : AndroidViewModel(app) {

    val result: LiveData<RequestResult<LobbyInfo, Exception>> = MutableLiveData()

    /**
     * Creates a lobby with the given arguments. The result is placed in [result]
     */
    fun createLobby(name: String, players: Long, rounds: Long, onSuccess: (LobbyInfo) -> Unit,
                    onError: (Exception) -> Unit) {
        val app = getApplication<DragApplication>()
        app.repo.publishLobby(name, players, rounds,
                onSuccess ,
                onError
        )
    }

    val state: LiveData<RequestState> = MutableLiveData(RequestState.IDLE)
}
package pt.isel.pdm.li51d.g9.drag.lobbylist

import pt.isel.pdm.li51d.g9.drag.RequestResult
import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import pt.isel.pdm.li51d.g9.drag.DragApplication
import pt.isel.pdm.li51d.g9.drag.LobbyInfo
import pt.isel.pdm.li51d.g9.drag.RequestState

class LobbyListViewModel(application: Application,
                    private val savedState: SavedStateHandle
) : AndroidViewModel(application) {

    private val application: DragApplication by lazy {
        getApplication<DragApplication>()
    }

    val joinResult: LiveData<RequestResult<LobbyInfo, Exception>> = MutableLiveData()
    val lobbies: MutableLiveData<List<LobbyInfo>> = MutableLiveData()

    /**
     * Gets the challenges list by fetching them from the server. The operation's result is exposed
     * through [lobbies]
     */
    fun fetchChallenges() {
        application.repo.fetchLobbies(
            onSuccess = {
                lobbies.value = it
            },
            onError = {
                Toast.makeText(application, "error", Toast.LENGTH_LONG).show()
            }
        )
    }

    fun tryJoinLobby(lobbyInfo: LobbyInfo) {

        val state = joinResult as MutableLiveData<RequestResult<LobbyInfo, Exception>>

        state.value = RequestResult(RequestState.ONGOING, lobbyInfo)
        application.repo.joinLobby(lobbyInfo.id,{
            state.postValue(RequestResult(RequestState.COMPLETE,it))
        },{
            state.postValue(RequestResult(RequestState.COMPLETE,null,it))
        })

    }

}
package pt.isel.pdm.li51d.g9.drag.game


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.firestore.ListenerRegistration
import pt.isel.pdm.li51d.g9.drag.*
import pt.isel.pdm.li51d.g9.drag.model.figures.Drawing
import pt.isel.pdm.li51d.g9.drag.model.figures.Shape
import java.lang.IllegalStateException

private const val SAVED_STATE_KEY = "GameViewModel.SavedState"



class OnlineGameViewModel(
        application: Application,
        private val savedState: SavedStateHandle
) : AndroidViewModel(application),IGameModel{

    override val game: MutableLiveData<GameState> by lazy {
        MutableLiveData<GameState>(savedState.get<GameState>(SAVED_STATE_KEY) ?: GameState())
    }

    var turn =0;
    var lobbyId : String? = null
    var playerId : String? = null
    private var lobbylistener : ListenerRegistration? = null
    private var playerlistener : ListenerRegistration? = null
    val lobby : MutableLiveData<LobbyInfo> = MutableLiveData()
    val awaitingPlayer : MutableLiveData<PlayerInfo> = MutableLiveData()

    fun subscribeToLobby(id : String) {
        lobbylistener?.remove()
        lobbylistener = app.repo.subscribeToLobby(id,{
        },{
            lobby.postValue(it)
        })
    }

    fun lookForPlayer(playerNumber : Int ) {
        val id = lobby.value?.players?.get(playerNumber-1)?: throw IllegalStateException()
        playerlistener?.remove()
        playerlistener = app.repo.subscribeToPlayer(id,{

        },{
            awaitingPlayer.postValue(it)
        })
    }

    private val app: DragApplication by lazy {
        getApplication<DragApplication>()
    }

    override fun startGame(numberOfPlayers: Int, numberOfRounds: Int) {
        val id = lobbyId?:throw IllegalStateException()
        app.repo.getLobby(id,{
            lobby.value =it
            subscribeToLobby(id)
            startNewRound()
            savedState[SAVED_STATE_KEY] = game.value
        },{})

    }

    override fun startNewRound() {
        turn = 1
        if(lobbyId == null || playerId == null) throw IllegalStateException("lobby and player id need to be set")
        game.value = GameState(getCurrentRoundNumber()+1, getCurrentPlayerNumber(), state = State.WAITING_FOR_WORD)
        app.repo.getInitialWord(getCurrentPlayerNumber(),getCurrentRoundNumber(),lobbyId!!,playerId!!){
            game.value!!.currentWord = it
            game.value!!.state = State.DRAWING
            game.postValue(game.value)
        }
    }

    fun getPlayerInfo(onSuccess: (PlayerInfo) -> Unit, onError: (Exception) -> Unit) {
        app.repo.getPlayerInfo(playerId!!,
                onSuccess,
                onError
        )
    }

    fun getCurrentTurn() : Int{
        return turn
    }

    fun getLookingForPlayerId() : String{
        val x =game.value?.currentPlayer!!-1
        return lobby.value?.players!![x]
    }

    override fun addWord(word: String) {
        app.repo.addWord(getLookingForPlayerId(), word)
        game.value?.state=State.WAITING_FOR_WORD
        //IF THE NUMBER OF PLAYERS IS ODD WE PLAY ONE LESS TURN
        if(turn == (getNumberOfPlayers() - getNumberOfPlayers()%2)){
            lookForPlayer(getCurrentPlayerNumber())
            game.value?.state=State.GAME_ENDED
            game.postValue(game.value)
        }else{
            turn++
            nextPlayerToLookFor()
            app.repo.getPlayerInfo(getLookingForPlayerId(),{
                val game = game.value?: throw IllegalStateException()
                //Calculate the turn that we are in counting from the first
                if(it.words.size>turn/2){
                    Log.v(TAG, "FOUND DRAWING")
                    setWord(it.words[turn/2])
                    this.game.value?.state = State.DRAWING
                    this.game.postValue(this.game.value)
                }else{
                    lookForPlayer(game.currentPlayer)
                }
            },{ throw(it)})
        }
    }

    fun setWord(word: String) {
        game.value?.currentWord = word
    }

    private fun nextPlayerToLookFor() : Int{
        val numberOfPlayers = lobby.value?.currentPlayers?.toInt()?:throw IllegalStateException()
        if(game.value?.currentPlayer == numberOfPlayers) game.value?.currentPlayer = 1
        else game.value?.currentPlayer = game.value?.currentPlayer!!+1

        return game.value?.currentPlayer!!
    }

    override fun addDrawingToRepo() {
        app.repo.addDrawing(game.value?.currentDrawing!!, getCurrentRoundNumber(),getLookingForPlayerId())
        turn ++
        nextPlayerToLookFor()

        game.value?.state = State.WAITING_FOR_DRAWING
        game.postValue(game.value)

        Log.v(TAG, "WAITING FOR PLAYER ${game.value?.currentPlayer}")

        val id = lobby.value?.players?.get(game.value?.currentPlayer!!-1)?: throw IllegalStateException()
        app.repo.getPlayerInfo(id, {
            val game = game.value?: throw IllegalStateException()

            lobby.value?.currentPlayers?:throw IllegalStateException()

            //Calculate the turn that we are in counting from the first
            val turn =getCurrentTurn()

            if(it.drawings.size>turn/2-1){
                Log.v(TAG, "FOUND DRAWING")
                setDrawing(it.drawings[turn/2-1])
                this.game.value?.state = State.WRITING
                this.game.postValue(this.game.value)
            }else{
                Log.v(TAG, "LOOKING FOR PLAYER DRAWING $game.currentPlayer")
                lookForPlayer(game.currentPlayer)
            }
        },{throw(it)})

        savedState[SAVED_STATE_KEY] = game.value
    }


    override fun getCurrentPlayerNumber(): Int {
        val playerNumber = lobby.value?.players?.indexOf(playerId)
        return(playerNumber?:0)+1
    }

    override fun getCurrentRoundNumber(): Int {
        return game.value?.currentRound ?: 0
    }



    fun getNumberOfPlayers(): Int {
        return lobby.value?.currentPlayers!!.toInt()
    }


    override fun addShape(shape: Shape) {
        game.value?.currentDrawing?.addShape(shape)
        savedState[SAVED_STATE_KEY] = game.value
    }

    override fun undoShape() {
        game.value?.currentDrawing?.removeLastShape()
        game.postValue(game.value)
        savedState[SAVED_STATE_KEY] = game.value
    }

    override fun setDrawing(drawing: Drawing) {
        game.value?.currentDrawing = drawing

    }

    fun waitForEndGame() {
        lookForPlayer(getCurrentPlayerNumber())

    }


}


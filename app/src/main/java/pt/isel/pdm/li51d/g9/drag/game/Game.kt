package pt.isel.pdm.li51d.g9.drag.game


import android.app.Application
import android.os.Parcelable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import kotlinx.android.parcel.Parcelize
import pt.isel.pdm.li51d.g9.drag.DragApplication
import pt.isel.pdm.li51d.g9.drag.model.figures.Drawing
import pt.isel.pdm.li51d.g9.drag.model.figures.Shape
import java.lang.IllegalStateException

private const val SAVED_STATE_KEY = "GameViewModel.SavedState"

@Parcelize
data class GameState(
    var currentRound: Int = 0,
    var currentPlayer: Int = 0,
    var currentWord: String = "",
    var currentDrawing: Drawing = Drawing(),
    var state: State = State.NOT_STARTED
) : Parcelable

enum class State {
    NOT_STARTED, DRAWING, WRITING, ROUND_ENDED, GAME_ENDED, WAITING_FOR_WORD,WAITING_FOR_DRAWING

}
class GameViewModel(
    application: Application,
    private val savedState: SavedStateHandle
) : AndroidViewModel(application),IGameModel {
    
    override val game: MutableLiveData<GameState> by lazy {
        MutableLiveData<GameState>(savedState.get<GameState>(SAVED_STATE_KEY) ?: GameState())
    }

    private val app: DragApplication by lazy {
        getApplication<DragApplication>()
    }

    override fun startGame(numberOfPlayers: Int, numberOfRounds: Int) {
        if(numberOfPlayers % 2 != 0){
            game.value = GameState(1, 1,"", state = State.WRITING)
            savedState[SAVED_STATE_KEY] = game.value
            getApplication<DragApplication>().initGame(numberOfRounds, numberOfPlayers)
        }else {
            app.repo.fetchWord {
                game.value = GameState(1, 1, it, state = State.DRAWING)
                savedState[SAVED_STATE_KEY] = game.value
                getApplication<DragApplication>().initGame(numberOfRounds, numberOfPlayers)
                app.addWord(it, 1)
            }
        }
    }

    override fun startNewRound() {

        if( getNumberOfPlayers() % 2 != 0){
            game.value = GameState(getCurrentRoundNumber() + 1, 1,"", state = State.WRITING)
            savedState[SAVED_STATE_KEY] = game.value
        }else {
            app.repo.fetchWord {
                game.value = GameState(getCurrentRoundNumber() + 1, 1, it, state = State.DRAWING)
                savedState[SAVED_STATE_KEY] = game.value
                app.addWord(it, getCurrentRoundNumber())
            }
        }
    }


    override fun addWord(word: String) {

        app.addWord(word, getCurrentRoundNumber())
        game.value?.currentWord = word
        if (getCurrentPlayerNumber() + 1 > getNumberOfPlayers()) {

            val round = app.getRound(getCurrentRoundNumber())

            //Currently gives a point to player 1, will change in the future
            if (round?.words?.get(0) ?: "" == word)
                app.addPoint(1)

            //Ready For the nextRound
            game.value?.currentPlayer = 1
            if (getCurrentRoundNumber() + 1 > app.getNumberOfRounds())
                game.value?.state = State.GAME_ENDED
            else
                game.value?.state = State.ROUND_ENDED
        } else {
            nextPlayer()
            game.value?.state = State.DRAWING
        }
        //When a drawing is described we discard it
        game.value?.currentDrawing = Drawing()
        game.postValue(game.value)
        savedState[SAVED_STATE_KEY] = game.value
    }

    override fun addDrawingToRepo() {
        app.addDrawing(game.value?.currentDrawing!!, getCurrentRoundNumber())
        game.value?.state = State.WRITING
        game.postValue(game.value)
        nextPlayer()
        savedState[SAVED_STATE_KEY] = game.value
    }

    override fun setDrawing(drawing: Drawing) {
        game.value?.currentDrawing=drawing
    }


    override fun getCurrentPlayerNumber(): Int {
        return game.value?.currentPlayer ?: 0
    }

    override fun getCurrentRoundNumber(): Int {
        return game.value?.currentRound ?: 0
    }

    private fun nextPlayer() {
        if (game.value?.state == State.NOT_STARTED) throw IllegalStateException()
        game.value?.currentPlayer = game.value?.currentPlayer!! + 1
    }


    private fun getNumberOfPlayers(): Int {
        return app.getNumberOfPlayers()
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


}


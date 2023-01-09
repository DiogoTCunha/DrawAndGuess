package pt.isel.pdm.li51d.g9.drag.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import pt.isel.pdm.li51d.g9.drag.DragApplication
import pt.isel.pdm.li51d.g9.drag.model.figures.Drawing
import pt.isel.pdm.li51d.g9.drag.model.figures.Shape
import java.lang.IllegalStateException

interface IGameModel{

    val game: MutableLiveData<GameState>

    fun startGame(numberOfPlayers: Int, numberOfRounds: Int)

    fun startNewRound()

    fun addDrawingToRepo()

    fun setDrawing(drawing : Drawing)

    fun addWord(word: String)

    fun getCurrentPlayerNumber() : Int

    fun getCurrentRoundNumber() : Int

    fun addShape(shape: Shape)

    fun undoShape()
}
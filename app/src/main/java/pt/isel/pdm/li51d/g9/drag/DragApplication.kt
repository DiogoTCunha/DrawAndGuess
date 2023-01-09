package pt.isel.pdm.li51d.g9.drag

import android.app.Application
import com.android.volley.toolbox.Volley
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.firebase.FirebaseApp
import pt.isel.pdm.li51d.g9.drag.model.Player
import pt.isel.pdm.li51d.g9.drag.model.Round
import pt.isel.pdm.li51d.g9.drag.model.figures.Drawing
import java.lang.IllegalStateException


const val TAG = "DRAG"

class DragApplication : Application() {
    val repo by lazy {
        GameRepository( this,
                Volley.newRequestQueue(this),
            jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        )
    }



    override fun onCreate() {

        FirebaseApp.initializeApp(this)
        super.onCreate()
    }

    fun initGame(numberOfRounds: Int, numberOfPlayers: Int){
        val playerList = List(numberOfPlayers) { _ -> Player() }
        val roundList = List(numberOfRounds) { _ -> Round() }
        repo.numberOfPlayers = numberOfPlayers
        repo.numberOfRounds = numberOfRounds
        repo.players = playerList
        repo.roundHistory= roundList
    }

    fun addWord(word : String, roundNumber : Int){
        repo.roundHistory?.get(roundNumber-1)?.words?.add(word) ?: throw IllegalStateException()
    }

    fun addDrawing(drawing : Drawing, roundNumber : Int){
        //repo.addDrawing(drawing,roundNumber,id)
       repo.roundHistory?.get(roundNumber-1)?.drawings?.add(drawing) ?: throw IllegalStateException()
    }

    fun getNumberOfPlayers() : Int{
        return repo.numberOfPlayers
    }

    fun getNumberOfRounds() : Int{
        return repo.numberOfRounds
    }

    fun getPlayers(): List<Player>? {
        return repo.players
    }

    fun getRound(roundNumber : Int) : Round? {
        return repo.roundHistory?.get(roundNumber - 1)
    }

    fun addPoint(playerNumber: Int) {
        repo.players?.get(playerNumber)?.addPoint()
    }


}
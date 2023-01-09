package pt.isel.pdm.li51d.g9.drag.initialScreen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.activity.viewModels
import pt.isel.pdm.li51d.g9.drag.R
import pt.isel.pdm.li51d.g9.drag.databinding.ActivityInitialScreenBinding
import pt.isel.pdm.li51d.g9.drag.game.GameActivity
import pt.isel.pdm.li51d.g9.drag.game.OFFLINE_MODE
import pt.isel.pdm.li51d.g9.drag.game.ONLINE_MODE
import pt.isel.pdm.li51d.g9.drag.lobby.LobbyActivity
import pt.isel.pdm.li51d.g9.drag.lobbylist.LobbyListActivity

const val RESULT_EXTRA = "CL.Result"

class InitialScreen : AppCompatActivity() {

    private val viewModel: InitialScreenViewModel by viewModels()
    private val binding: ActivityInitialScreenBinding by lazy { ActivityInitialScreenBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableButtons()
        setContentView(binding.root)

        binding.startButton.isEnabled = true
        binding.startButton.setOnClickListener{startGame()}

        binding.startonlineButton.isEnabled = true
        binding.startonlineButton.setOnClickListener { startOnlineGame() }

        binding.findgameButton.isEnabled = true
        binding.findgameButton.setOnClickListener{findGame()}

    }

    private fun validateInputNumbers(playerCount : Long, roundCount : Long) : Boolean {

        var validInput = true

        if(playerCount < 5) {
            validInput = false
            val errorMessage = R.string.not_enough_players_error
            Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
        }

        if (roundCount <= 0) {
            validInput = false
            val errorMessage = R.string.not_enough_rounds_error
            Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
        }

        return validInput
    }

    private fun validateInputExists(binding: ActivityInitialScreenBinding, online: Boolean) : Boolean {

        var validInput = true

        if(binding.numberOfPlayers.text.isEmpty()) {
            validInput = false
            val errorMessage = R.string.no_players_input_error
            Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
        }

        if(binding.numberOfRounds.text.isEmpty()) {
            validInput = false
            val errorMessage = R.string.no_rounds_input_error
            Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
        }

        if(online) {
            if(binding.lobbyName.text.isEmpty()) {
                validInput = false
                val errorMessage = R.string.no_lobby_name_input_error
                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        return validInput
    }

    private fun findGame(){
        disableButtons()
        val intent = Intent(this, LobbyListActivity::class.java)
        this.startActivity(intent)
        enableButtons()
    }

    private fun startGame(){

        if(!validateInputExists(binding, false))
            return

        val playerCount : Long = binding.numberOfPlayers.text.toString().toLong()
        val roundCount : Long = binding.numberOfRounds.text.toString().toLong()

        if (validateInputNumbers(playerCount, roundCount)) {
            disableButtons()
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("MODE", OFFLINE_MODE)
            intent.putExtra("playerCount", playerCount)
            intent.putExtra("roundCount", roundCount)
            this.startActivity(intent)
            enableButtons()
        }
    }

    private fun disableButtons(){
        binding.startButton.isEnabled=false
        binding.startonlineButton.isEnabled=false
        binding.findgameButton.isEnabled=false
    }

    private fun enableButtons(){
        binding.startButton.isEnabled=true
        binding.startonlineButton.isEnabled=true
        binding.findgameButton.isEnabled=true
    }

    private fun startOnlineGame() {
        if(!validateInputExists(binding, true))
            return

        val playerCount : Long = binding.numberOfPlayers.text.toString().toLong()
        val roundCount : Long = binding.numberOfRounds.text.toString().toLong()
        val lobbyName : String = binding.lobbyName.text.toString()

        if (validateInputNumbers(playerCount, roundCount)) {
            disableButtons()
            binding.startonlineButton.setOnClickListener(null)
            viewModel.createLobby(lobbyName, playerCount, roundCount, {//OnSuccess
                val intent = Intent(this, LobbyActivity::class.java)
                intent.putExtra("lobbyId",it.id)
                intent.putExtra("playerCount", playerCount)
                intent.putExtra("roundCount", roundCount)
                intent.putExtra("lobbyName", lobbyName)
                this.startActivity(intent)
                enableButtons()
            }, {//OnError
                Toast.makeText(this, R.string.error_creating_lobby, Toast.LENGTH_LONG)
                    .show()
                enableButtons()
            })
        }
    }
}
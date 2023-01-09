package pt.isel.pdm.li51d.g9.drag.lobby

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import pt.isel.pdm.li51d.g9.drag.LobbyInfo
import pt.isel.pdm.li51d.g9.drag.databinding.ActivityLobbyBinding
import pt.isel.pdm.li51d.g9.drag.game.GameActivity
import pt.isel.pdm.li51d.g9.drag.game.ONLINE_MODE

class LobbyActivity : AppCompatActivity() {

    private val viewModel: LobbyViewModel by viewModels()

    private val binding: ActivityLobbyBinding by lazy {
        ActivityLobbyBinding.inflate(layoutInflater)
    }

    private val id: String by lazy {
        intent.getStringExtra("lobbyId")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lobbyName = intent.getStringExtra("lobbyName")


        binding.nameText.text = lobbyName
        binding.startGameButton.isEnabled = false
        binding.startGameButton.setOnClickListener { startGame() }
        setContentView(binding.root)

        viewModel.subscribeToLobby(id)
        viewModel.lobby.observe(this, Observer {
            if (it.started) {
                Log.v("DRAG","STARTED")
                viewModel.unsubcribeFromLobby()
                joinGame(it)
                return@Observer
            }
            val roundsText = "Rounds: ${it.rounds}"
            val playersText = "Players : ${it.currentPlayers}/${it.maxPlayers}"
            if (it.currentPlayers >= 2) {
                enableButtons()
            }
            binding.playersText.text = playersText
            binding.roundsText.text = roundsText
        })
    }

    private fun joinGame(lobby: LobbyInfo?) {
        Log.v("DRAG","STARTINGGAME")
        viewModel.joinGame(lobby?.id!!,{
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("lobbyId", id)
            intent.putExtra("playerId", it)
            intent.putExtra("MODE", ONLINE_MODE)
            intent.putExtra("playerCount", lobby.currentPlayers)
            intent.putExtra("roundCount", lobby.rounds)
            this.startActivity(intent)
        },{
            Toast.makeText(this, "Couldn't add you to the game",Toast.LENGTH_SHORT).show()
            finish()
        })
    }

    private fun startGame() {
        viewModel.updateLobbyOnStart(id)
    }

    private fun enableButtons() {
        binding.startGameButton.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            if(viewModel.lobby.value?.started == false) {
                viewModel.leaveLobby(id)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}
package pt.isel.pdm.li51d.g9.drag.lobbylist

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import pt.isel.pdm.li51d.g9.drag.LobbyInfo
import pt.isel.pdm.li51d.g9.drag.R
import pt.isel.pdm.li51d.g9.drag.RequestState
import pt.isel.pdm.li51d.g9.drag.databinding.ActivityLobbyListBinding
import pt.isel.pdm.li51d.g9.drag.lobby.LobbyActivity


/**
 * The associated view binding
 */

class LobbyListActivity : AppCompatActivity() {
    private val viewModel: LobbyListViewModel by viewModels()

    private val binding: ActivityLobbyListBinding by lazy {
        ActivityLobbyListBinding.inflate(layoutInflater)
    }

    private fun updateLobbiesList() {
        binding.refreshLayout.isRefreshing = true
        viewModel.fetchChallenges()
    }

    private fun lobbySelected(lobby: LobbyInfo) {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.join_lobby_dialog_title, lobby.name))
                .setPositiveButton(R.string.join_lobby_dialog_ok) { _, _ -> viewModel.tryJoinLobby(lobby) }
                .setNegativeButton(R.string.join_lobby_cancel, null)
                .create()
                .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        viewModel.joinResult.observe(this, {
            if(it.state == RequestState.COMPLETE){
                if(it.error != null){
                    Toast.makeText(this,"Couldn't join lobby",Toast.LENGTH_SHORT).show()
                }
                else{
                    if(it.result!=null) {
                        val intent = Intent(this, LobbyActivity::class.java)
                        intent.putExtra("lobbyId", it.result.id)
                        this.startActivity(intent)
                    }
                }
            }
        })

        binding.lobbylist.setHasFixedSize(true)
        binding.lobbylist.layoutManager = LinearLayoutManager(this)

        viewModel.fetchChallenges()

        // Get view model instance and add its contents to the recycler view
        viewModel.lobbies.observe(this) {
            binding.lobbylist.adapter = LobbyListAdapter(it, ::lobbySelected)
            binding.refreshLayout.isRefreshing = false
        }

        // Setup ui event handlers
        binding.refreshLayout.setOnRefreshListener {
            updateLobbiesList()
        }


    }
}
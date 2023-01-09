package pt.isel.pdm.li51d.g9.drag.lobbylist

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.isel.pdm.li51d.g9.drag.LobbyInfo
import pt.isel.pdm.li51d.g9.drag.R

class LobbyViewHolder(private val view: ViewGroup) : RecyclerView.ViewHolder(view){

    private val nameView: TextView = view.findViewById(R.id.gamename)
    private val playersView: TextView = view.findViewById(R.id.players)
    private val roundsView: TextView = view.findViewById(R.id.rounds)

    fun bindTo(lobby: LobbyInfo, itemSelectedListener: (LobbyInfo) -> Unit) {
        val lobbyName = "Lobby name: ${lobby.name}"
        nameView.text = lobbyName
        val playersString = "${lobby.currentPlayers}/${lobby.maxPlayers}"
        playersView.text = playersString
        roundsView.text = lobby.rounds.toString()

        view.setOnClickListener { itemSelectedListener(lobby) }

    }
}

class LobbyListAdapter(
    private val contents: List<LobbyInfo>,private val itemSelectedListener: (LobbyInfo) -> Unit) :
    RecyclerView.Adapter<LobbyViewHolder>() {

    override fun onBindViewHolder(holder: LobbyViewHolder, position: Int) {
        holder.bindTo(contents[position], itemSelectedListener)
    }

    override fun getItemCount(): Int = contents.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LobbyViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.lobby_list_item, parent, false) as ViewGroup

        return LobbyViewHolder(view)
    }
}


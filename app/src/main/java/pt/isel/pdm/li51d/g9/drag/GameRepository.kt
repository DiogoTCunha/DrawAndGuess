package pt.isel.pdm.li51d.g9.drag

import android.app.Application
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.RequestQueue
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.firebase.firestore.*
import kotlinx.android.parcel.Parcelize
import pt.isel.pdm.li51d.g9.drag.game.DrawingMode
import pt.isel.pdm.li51d.g9.drag.model.Player
import pt.isel.pdm.li51d.g9.drag.model.Round
import pt.isel.pdm.li51d.g9.drag.model.dto.WordsRequest
import pt.isel.pdm.li51d.g9.drag.model.figures.Drawing
import pt.isel.pdm.li51d.g9.drag.model.figures.Position
import pt.isel.pdm.li51d.g9.drag.model.figures.ShapeData
import java.io.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.hashMapOf
import kotlin.collections.map
import kotlin.collections.set
import kotlin.collections.toList

private const val GAME_LOBBY_COLLECTION = "GAME_LOBBY"
private const val GAME_COLLECTION = "GAME"
private const val PLAYER_DRAWINGS = "DRAWINGS"
private const val GAME_NAME = "name"
private const val GAME_MAXPLAYERS = "maxplayers"
private const val GAME_CURRENTPLAYERS = "currentplayers"
private const val GAME_ROUND = "rounds"
private const val GAME_WORDS = "words"
private const val PLAYER_WORDS = "WORDS"
private const val GAME_STARTED = "started"
private const val GAME_PLAYERS = "players"

class ShapeDTO(
    @JsonProperty("mode") val mode: String,
    @JsonProperty("strokeWidth") val strokeWidth: Float,
    @JsonProperty("positions") val positions: List<Position>
)

data class LobbyInfo(
    val id: String,
    val name: String,
    val maxPlayers: Long,
    val currentPlayers: Long,
    val rounds: Long,
    val words: List<String>,
    val started: Boolean,
    val players: List<String>
)

@Parcelize
class PlayerInfo(
        val id: String,
        val drawings: List<Drawing>,
        val words: List<String>
) : Parcelable

private fun QueryDocumentSnapshot.toLobbyInfo(): LobbyInfo {

    return LobbyInfo(
        id,
        data[GAME_NAME] as String,
        (data[GAME_MAXPLAYERS] ?: -1L) as Long,
        (data[GAME_CURRENTPLAYERS] ?: -1L) as Long,
        (data[GAME_ROUND] ?: -1L) as Long,
        data[GAME_WORDS] as List<String>,
        data[GAME_STARTED] as Boolean,
        data[GAME_PLAYERS] as List<String>
    )
}

private fun DocumentSnapshot.toLobbyInfo() = LobbyInfo(
    id,
    data?.get(GAME_NAME) as String,
    (data?.get(GAME_MAXPLAYERS) ?: -1L) as Long,
    (data?.get(GAME_CURRENTPLAYERS) ?: -1L) as Long,
    (data?.get(GAME_ROUND) ?: -1L) as Long,
    (data?.get(GAME_WORDS)) as List<String>,
    data?.get(GAME_STARTED) as Boolean,
    data?.get(GAME_PLAYERS) as List<String>
)


@Suppress("UNCHECKED_CAST")
class GameRepository(val app: Application, val queue: RequestQueue, val mapper: ObjectMapper) {
    var numberOfRounds: Int = 0
    var numberOfPlayers: Int = 0
    var players: List<Player>? = null
    var roundHistory: List<Round>? = null
    val db = FirebaseFirestore.getInstance()

    fun subscribeToLobby(
        lobbyID: String,
        onSubscriptionError: (Exception) -> Unit,
        onStateChanged: (LobbyInfo) -> Unit
    ): ListenerRegistration {

        return db
            .collection(GAME_LOBBY_COLLECTION)
            .document(lobbyID)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onSubscriptionError(error)
                    return@addSnapshotListener
                }

                if (snapshot?.exists() == true) {
                    onStateChanged(snapshot.toLobbyInfo())
                }
            }
    }

    fun subscribeToPlayer(
        playerID: String,
        onSubscriptionError: (Exception) -> Unit,
        onStateChanged: (PlayerInfo) -> Unit
    ): ListenerRegistration? {
        return db
            .collection(GAME_COLLECTION)
            .document(playerID)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onSubscriptionError(error)
                    return@addSnapshotListener
                }

                if (snapshot?.exists() == true) {


                    onStateChanged(snapshot.toPlayerInfo())
                }
            }
    }

    fun fetchWords(
        numberOfWords: Int,
        onResult: (Result<List<String>>) -> Unit
    ): LiveData<Result<List<String>>?> {
        val result = MutableLiveData<Result<List<String>>?>()
        Log.v(
            "REQUEST",
            "${RANDOM_WORDS_URL}&limit=${numberOfWords}&api_key=${BuildConfig.appId}"
        )
        val request = WordsRequest(
            "${RANDOM_WORDS_URL}&limit=${numberOfWords}&api_key=${BuildConfig.appId}",
            mapper,
            {
                onResult(Result.success((it.map { it.word }).toList()))
            },
            {
                Log.v("REQUEST", "ERROR ${it.message}")
                onResult(Result.failure(it))
            }
        )

        queue.add(request)
        return result
    }

    fun fetchWord(onStringReceive: (String) -> Unit) {
        fetchWords(1, onResult = {
            onStringReceive(it.getOrDefault(readWordsFromFile(1))[0])
        })
    }

    fun getLobby(lobbyId: String, onSuccess: (LobbyInfo) -> Unit, onError: (Exception) -> Unit) {
        db.collection(GAME_LOBBY_COLLECTION)
            .document(lobbyId)
            .get()
            .addOnSuccessListener { result ->
                Log.v(TAG, "Repo got list from Firestore")
                onSuccess(result.toLobbyInfo())
            }
            .addOnFailureListener {
                Log.e(TAG, "Repo: An error occurred while fetching list from Firestore")
                Log.e(TAG, "Error was $it")
                onError(it)
            }
    }

    fun fetchLobbies(onSuccess: (List<LobbyInfo>) -> Unit, onError: (Exception) -> Unit) {
        db.collection(GAME_LOBBY_COLLECTION)
            .whereEqualTo(GAME_STARTED, false)
            .get()
            .addOnSuccessListener { result ->
                Log.v(TAG, "Repo got list from Firestore")
                onSuccess(result.map { it.toLobbyInfo() }.toList())
            }
            .addOnFailureListener {
                Log.e(TAG, "Repo: An error occurred while fetching list from Firestore")
                Log.e(TAG, "Error was $it")
                onError(it)
            }
    }

    fun joinLobby(lobbyId: String, onSuccess: (LobbyInfo) -> Unit, onError: (Exception) -> Unit) {
        val doc = db.collection(GAME_LOBBY_COLLECTION).document(lobbyId)
        doc.update(GAME_CURRENTPLAYERS, FieldValue.increment(1))
        doc.get().addOnSuccessListener { result ->
            Log.v(TAG, "Repo got list from Firestore")

            if (result.data?.get(GAME_STARTED) == true)
                onError(IllegalStateException("Game already started"))

            val lobby = result.toLobbyInfo()
            if (lobby.currentPlayers > lobby.maxPlayers) {
                doc.update(GAME_CURRENTPLAYERS, FieldValue.increment(-1))
                onError(IllegalStateException("The party is full"))
            } else {
                onSuccess(lobby)
            }
        }.addOnFailureListener {
            Log.e(TAG, "Repo: An error occurred while fetching list from Firestore")
            Log.e(TAG, "Error was $it")
            onError(it)
        }
    }

    fun publishLobby(
        name: String, players: Long, rounds: Long,
        onSuccess: (LobbyInfo) -> Unit,
        onError: (Exception) -> Unit
    ) {
        fetchWords((players).toInt(), onResult = {
            val words: List<String> = it.getOrDefault(readWordsFromFile((players).toInt()))

            db.collection(GAME_LOBBY_COLLECTION)
                .add(
                    hashMapOf(
                        GAME_NAME to name, GAME_MAXPLAYERS to players,
                        GAME_CURRENTPLAYERS to 1, GAME_ROUND to rounds,
                        GAME_WORDS to words, GAME_STARTED to false,
                        GAME_PLAYERS to ArrayList<String>()
                    )
                )
                .addOnSuccessListener {
                    onSuccess(
                        LobbyInfo(
                            it.id,
                            name,
                            players,
                            1,
                            rounds,
                            words,
                            false,
                            ArrayList()
                        )
                    )
                }
                .addOnFailureListener { onError(it) }
        })
    }

    fun leaveLobby(lobbyId: String) {
        val doc = db.collection(GAME_LOBBY_COLLECTION).document(lobbyId)
        doc.update(GAME_CURRENTPLAYERS, FieldValue.increment(-1))
        doc.get().addOnSuccessListener { result ->
            Log.v(TAG, "Repo got list from Firestore")
            val lobby = result.toLobbyInfo()
            if (lobby.currentPlayers <= 0) {
                doc.update(GAME_CURRENTPLAYERS, FieldValue.increment(-1))
                doc.delete()
            }
        }
    }


    fun updateLobbyOnStart(lobbyId: String) {

        val doc = db.collection(GAME_LOBBY_COLLECTION).document(lobbyId)
        doc.update(GAME_STARTED, true)
    }

    fun addDrawing(drawing: Drawing, roundNumber: Int, lobbyId: String) {
        val gamedoc = db.collection(GAME_COLLECTION).document(lobbyId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(gamedoc)
            val drawings: HashMap<String, List<ShapeData>> =
                snapshot.get("DRAWINGS") as HashMap<String, List<ShapeData>>
            val idx = drawings.keys.size
            drawings["DRAWING_${idx}"] = drawing.shapes
            transaction.update(gamedoc, "DRAWINGS", drawings)
            null
        }.addOnSuccessListener { Log.d(TAG, "Transaction success!") }
            .addOnFailureListener { e -> Log.w(TAG, "Transaction failure.", e) }
    }

    fun getInitialWord(
        playerNumber: Int, roundNumber: Int, lobbyId: String, playerID: String,
        onSuccess: (String) -> Unit
    ) {

        db.collection(GAME_LOBBY_COLLECTION).document(lobbyId).get().addOnSuccessListener {
            val words = it.get(GAME_WORDS) as List<String>
            val word = words[playerNumber - 1]
            db.collection(GAME_COLLECTION).document(playerID).update(
                "WORDS", FieldValue.arrayUnion(
                    word
                )
            )
            onSuccess(word)
        }
    }



    fun addWord(playerId: String, word: String) {

        db.collection(GAME_COLLECTION).document(playerId).update(
            "WORDS",
            FieldValue.arrayUnion(word)
        )
    }

    /*This will make the player join the game by adding himself to the list of players on the lobby
        doc and adding a document in the game doc, this way all the players can know his id to access it
    */
    fun joinGame(lobbyId: String, onSuccess: (String) -> Unit, onFail: (Exception) -> Unit) {
        val gamedoc = db.collection(GAME_COLLECTION)
        val lobbydoc = db.collection(GAME_LOBBY_COLLECTION)
        gamedoc.add(
            hashMapOf(
                "WORDS" to ArrayList<String>(),
                "DRAWINGS" to hashMapOf<Any, Any>()
            )
        ).addOnSuccessListener { player ->
                lobbydoc.document(lobbyId).update(GAME_PLAYERS, FieldValue.arrayUnion(player.id))
                    .addOnSuccessListener { onSuccess(player.id) }
                    .addOnFailureListener {
                        onFail(Exception("Couldn't be added to the game"))
                    }
            }.addOnFailureListener {
                onFail(Exception("Couldn't be added to the game"))
            }
    }

    fun getPlayerInfo(
        playerId: String,
        onSuccess: (PlayerInfo) -> Unit,
        onFail: (Exception) -> Unit
    ) {
        db.collection(GAME_COLLECTION)
            .document(playerId).get().addOnSuccessListener {
                onSuccess(it.toPlayerInfo())
            }.addOnFailureListener {
                onFail(Exception("Couldn't be added to the game"))
            }
    }

    private fun readWordsFromFile(numberOfWords: Int): ArrayList<String> {

        Log.v(TAG, "Got words from file")
        val buffReader = BufferedReader(InputStreamReader(app.applicationContext.resources.openRawResource(R.raw.words)))
        val fullWordList = ArrayList<String>()
        val finalWordList = ArrayList<String>()

        buffReader.forEachLine { line -> fullWordList.add(line) }
        buffReader.close()

        val rand = Random()
        for(i in 0 until numberOfWords) {
            var index = rand.nextInt(fullWordList.size)
            finalWordList.add(fullWordList[index])
        }
        return finalWordList
    }
}

private fun DocumentSnapshot.toPlayerInfo(): PlayerInfo {
    //TRANSFORM SERVER DATA IN DRAWINGS LIST
    val drawingsToReturn: MutableList<Drawing> = ArrayList()
    val drawings = data?.get(PLAYER_DRAWINGS) as HashMap<String, Any>
    val size = drawings.keys.size
    Log.v(TAG, drawings.toString())
    for (i in 0 until size) {
        Log.v(TAG, i.toString())
        val shapeList = drawings["DRAWING_${i}"] as List<HashMap<String, Any>>
        val shapes = ArrayList<ShapeData>()
        for (Item: HashMap<String, Any> in shapeList) {
            val positions = ArrayList<Position>()
            for (positionData in Item["positions"] as List<HashMap<String, Double>>) {
                positions.add(
                    Position(
                        positionData["x"]!!.toFloat(),
                        positionData["y"]!!.toFloat()
                    )
                )
            }

            val mode = DrawingMode.valueOf(Item["mode"] as String)
            val strokeWidth = Item["strokeWidth"] as Double
            shapes.add(ShapeData(positions, strokeWidth.toFloat(), mode))
        }
        drawingsToReturn.add(Drawing(shapes))
    }

    val words = data?.get(PLAYER_WORDS) as List<String>

    return PlayerInfo(id, drawingsToReturn, words)
}









package pt.isel.pdm.li51d.g9.drag.game

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import pt.isel.pdm.li51d.g9.drag.*
import pt.isel.pdm.li51d.g9.drag.initialScreen.InitialScreen
import pt.isel.pdm.li51d.g9.drag.roundEnd.RoundEndActivity


import pt.isel.pdm.li51d.g9.drag.databinding.ActivityGameBinding
import pt.isel.pdm.li51d.g9.drag.model.DrawingListener
import pt.isel.pdm.li51d.g9.drag.model.figures.Drawing
import pt.isel.pdm.li51d.g9.drag.model.figures.Shape

enum class DrawingMode {
    LINE, RECT, CIRCLE, FREE
}

public const val ONLINE_MODE = "ONLINE"
public const val OFFLINE_MODE = "OFFLINE"

class GameActivity : AppCompatActivity() {
    private val onlineViewModel: OnlineGameViewModel by viewModels()
    private val gameViewModel: GameViewModel by viewModels()

    private val viewModel: IGameModel by lazy { getModel() }

    private val binding: ActivityGameBinding by lazy { ActivityGameBinding.inflate(layoutInflater) }

    private val mode: String by lazy {
        intent.getStringExtra("MODE")
    }
    private val lobbyId: String by lazy {
        intent.getStringExtra("lobbyId")
    }
    private val playerId: String by lazy {
        intent.getStringExtra("playerId")
    }

    fun getModel(): IGameModel {
        return if (intent.getStringExtra("MODE") == ONLINE_MODE) onlineViewModel
        else {
            gameViewModel
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val playerCount = intent.getLongExtra("playerCount", 5).toInt()
        val roundCount = intent.getLongExtra("roundCount", 5).toInt()

        if (mode == ONLINE_MODE) {
            //OBSERVER FOR ONLINE VIEW MODEL
            val viewModel = viewModel as OnlineGameViewModel
            viewModel.awaitingPlayer.observe(this) {
                val game = viewModel.game.value ?: throw IllegalStateException()
                if (game.state == State.WAITING_FOR_DRAWING) {
                    //Calculate the turn that we are in counting from the first
                    val turn = viewModel.getCurrentTurn()

                    if (it.drawings.size > (turn / 2 - 1)) {
                        Log.v(TAG, "FOUND DRAWING ON TURN ${turn}")
                        viewModel.setDrawing(it.drawings[turn / 2 - 1])
                        viewModel.game.value?.state = State.WRITING
                        viewModel.game.postValue(viewModel.game.value)
                    }
                } else if (game.state == State.WAITING_FOR_WORD) {
                    val turn = viewModel.getCurrentTurn()
                    if (it.words.size > turn / 2) {
                        Log.v(TAG, "FOUND WORD")
                        viewModel.setWord(it.words[turn / 2])
                        viewModel.game.value?.state = State.DRAWING
                        viewModel.game.postValue(viewModel.game.value)
                    }
                }
                if (game.state == State.GAME_ENDED) {
                    val playerNumber = viewModel.getNumberOfPlayers()

                    val numberOfInputs = playerNumber + (if (playerNumber % 2 == 0) 1 else 0)
                    Log.v(TAG, "$numberOfInputs")
                    if (it.drawings.size + it.words.size >= numberOfInputs) {
                        showRoundEndInfo()
                    }
                }
            }

        }


        viewModel.game.observe(this, {
            Log.v(TAG, it.state.toString())

            val roundString = getString(R.string.round) + " " + viewModel.getCurrentRoundNumber()
            val playerString = getString(R.string.player) + " " + viewModel.getCurrentPlayerNumber()

            binding.playerTextView.text = playerString
            binding.roundTextView.text = roundString
            binding.DrawingDescription.setText(viewModel.game.value?.currentWord)
            binding.drawingBoard.setDrawing(viewModel.game.value?.currentDrawing)
            when (it.state) {
                State.DRAWING -> drawingState()
                State.WRITING -> writingWordState()
                State.NOT_STARTED -> startGameState(playerCount, roundCount)
                State.WAITING_FOR_WORD -> waitingState()
                State.WAITING_FOR_DRAWING -> waitingState()
                else -> {
                    if (mode == ONLINE_MODE) {
                        gameEndedState();
                        (viewModel as OnlineGameViewModel).waitForEndGame();
                        return@observe
                    }
                    val showRoundIntent = Intent(this, RoundEndActivity::class.java)
                    showRoundIntent.putExtra("roundNumber", viewModel.game.value?.currentRound)
                    startActivityForResult(showRoundIntent, 0)
                    if (it.state == State.ROUND_ENDED)
                        startNewRoundState()
                    else {
                        // Scoreboard to be added on next phase, when other players manage to get points
                    }
                }
            }
        })

        initUI(binding)
    }

    private fun gameEndedState() {
        binding.drawingBoard.isEnabled = false
        binding.RectButton.isEnabled = false
        binding.CircleButton.isEnabled = false
        binding.LineButton.isEnabled = false
        binding.FreeButton.isEnabled = false
        binding.undoButton.isEnabled = false
        binding.seekBar.isEnabled = false
        binding.DrawingDescription.isEnabled = false
        binding.nextButton.isEnabled = false
        binding.DrawingDescription.setText("Waiting for other players to finish")
    }

    private fun waitingState() {
        binding.drawingBoard.isEnabled = false
        binding.RectButton.isEnabled = false
        binding.CircleButton.isEnabled = false
        binding.LineButton.isEnabled = false
        binding.FreeButton.isEnabled = false
        binding.undoButton.isEnabled = false
        binding.seekBar.isEnabled = false
        binding.DrawingDescription.isEnabled = false
        binding.nextButton.isEnabled = false
    }


    override fun onResume() {
        super.onResume()
        if (viewModel.game.value?.state == State.GAME_ENDED) {
            val startIntent = Intent(this, InitialScreen::class.java)
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(startIntent)
        }
    }

    private fun startNewRoundState() {
        binding.nextButton.isEnabled = false
        binding.DrawingDescription.text.clear()
        binding.drawingBoard.isEnabled = false
        binding.RectButton.isEnabled = false
        binding.CircleButton.isEnabled = false
        binding.LineButton.isEnabled = false
        binding.FreeButton.isEnabled = false
        binding.undoButton.isEnabled = false
        binding.seekBar.isEnabled = false
        binding.DrawingDescription.isEnabled = true
        viewModel.startNewRound()
        binding.nextButton.setOnClickListener(null)
    }

    private fun drawingState() {
        binding.nextButton.isEnabled = true
        binding.drawingBoard.isEnabled = true
        binding.RectButton.isEnabled = true
        binding.CircleButton.isEnabled = true
        binding.LineButton.isEnabled = true
        binding.FreeButton.isEnabled = true
        binding.undoButton.isEnabled = true
        binding.seekBar.isEnabled = true
        binding.DrawingDescription.isEnabled = false
        val drawing = Drawing()
        binding.drawingBoard.setDrawing(drawing)
        viewModel.setDrawing(drawing)
        binding.nextButton.setOnClickListener {
            viewModel.addDrawingToRepo()
        }
    }

    private fun showRoundEndInfo() {
        (viewModel as OnlineGameViewModel).getPlayerInfo({
            val intent = Intent(this, RoundEndActivity::class.java)
            intent.putExtra("playerInfo", it)
            intent.putExtra("MODE", ONLINE_MODE)
            this.startActivity(intent)
            finish()
        }, {
        })
    }

    private fun writingWordState() {
        binding.drawingBoard.isEnabled = false
        binding.RectButton.isEnabled = false
        binding.CircleButton.isEnabled = false
        binding.LineButton.isEnabled = false
        binding.FreeButton.isEnabled = false
        binding.undoButton.isEnabled = false
        binding.seekBar.isEnabled = false
        binding.DrawingDescription.text.clear()
        binding.DrawingDescription.isEnabled = true
        binding.nextButton.isEnabled = true
        binding.nextButton.setOnClickListener {
            if (binding.DrawingDescription.text.isNotEmpty()) {
                val word: String = binding.DrawingDescription.text.toString()
                viewModel.addWord(word)
            } else {
                val errorMessage = R.string.no_word_input_error
                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startGameState(playerCount: Int, roundCount: Int) {
        binding.drawingBoard.isEnabled = false
        binding.RectButton.isEnabled = false
        binding.CircleButton.isEnabled = false
        binding.LineButton.isEnabled = false
        binding.FreeButton.isEnabled = false
        binding.undoButton.isEnabled = false
        binding.seekBar.isEnabled = false
        binding.DrawingDescription.isEnabled = false
        binding.nextButton.isEnabled = false
        if (mode == ONLINE_MODE) {
            (viewModel as OnlineGameViewModel).lobbyId = lobbyId
            (viewModel as OnlineGameViewModel).playerId = playerId
        }
        viewModel.startGame(playerCount, roundCount)
    }


    private fun initUI(binding: ActivityGameBinding) {
        binding.drawingBoard.listener = object : DrawingListener {
            override fun onDrawnNewShape(shape: Shape) {
                viewModel.addShape(shape)
            }

            override fun onUndo() {
                viewModel.undoShape()
            }
        }

        binding.CircleButton.setOnClickListener { _ ->
            binding.drawingBoard.drawingMode = DrawingMode.CIRCLE
        }

        binding.LineButton.setOnClickListener { _ ->
            binding.drawingBoard.drawingMode = DrawingMode.LINE
        }

        binding.FreeButton.setOnClickListener { _ ->
            binding.drawingBoard.drawingMode = DrawingMode.FREE
        }

        binding.RectButton.setOnClickListener { _ ->
            binding.drawingBoard.drawingMode = DrawingMode.RECT
        }

        binding.undoButton.setOnClickListener { _ ->
            binding.drawingBoard.undo()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {

                binding.drawingBoard.strokeWidth = seekBar.progress.toFloat()
            }
        })
    }

}

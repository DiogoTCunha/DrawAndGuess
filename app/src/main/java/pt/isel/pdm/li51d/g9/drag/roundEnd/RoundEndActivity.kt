package pt.isel.pdm.li51d.g9.drag.roundEnd

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_round_end.view.*
import pt.isel.pdm.li51d.g9.drag.PlayerInfo
import pt.isel.pdm.li51d.g9.drag.model.DrawingBoard
import pt.isel.pdm.li51d.g9.drag.databinding.ActivityRoundEndBinding
import pt.isel.pdm.li51d.g9.drag.game.ONLINE_MODE
import pt.isel.pdm.li51d.g9.drag.model.figures.Drawing


class RoundEndActivity : AppCompatActivity() {

    private val binding: ActivityRoundEndBinding by lazy { ActivityRoundEndBinding.inflate(layoutInflater) }
    private val viewModel: RoundEndModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mode = intent.getStringExtra("MODE")
        val words: MutableList<String>
        val drawings: MutableList<Drawing>

        if (mode == ONLINE_MODE) {
            val playerInfo = intent.getParcelableExtra<PlayerInfo>("playerInfo")
            drawings = playerInfo.drawings.toMutableList()
            words = playerInfo.words.toMutableList()
        } else {
            val roundNumber = intent.getIntExtra("roundNumber", 1)
            val round = viewModel.getRound(roundNumber)
            words = round?.words!!
            drawings = round.drawings
        }


        for (i in 0 until (drawings?.size ?: 0)) {
            val textView = TextView(this)
            textView.gravity = Gravity.CENTER_HORIZONTAL
            textView.text = words?.get(i) ?: ""
            textView.textSize = 30f
            textView.setTypeface(textView.typeface, Typeface.BOLD)

            val drawingView = DrawingBoard(this)
            val paramsDrawing = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsDrawing.setMargins(0, 0,0, 30)

            drawingView.layoutParams = paramsDrawing
            drawingView.setDrawing(drawings?.get(i))
            drawingView.isEnabled = false

            binding.ScrollView.LinearLayout.addView(textView)
            binding.ScrollView.LinearLayout.addView(drawingView)
        }

        val textView = TextView(this)
        textView.gravity = Gravity.CENTER_HORIZONTAL
        textView.text = words?.get(words.size - 1) ?: ""
        textView.textSize = 30f
        textView.setTypeface(textView.typeface, Typeface.BOLD)

        binding.ScrollView.LinearLayout.addView(textView)
        setContentView(binding.root)
    }
}
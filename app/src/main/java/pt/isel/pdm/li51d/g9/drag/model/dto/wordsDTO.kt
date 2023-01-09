package pt.isel.pdm.li51d.g9.drag.model.dto

import android.util.Log
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonRequest
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import pt.isel.pdm.li51d.g9.drag.model.figures.Position

class WordDTO( @JsonProperty("word") val word: String)



class WordsRequest(
    url: String,
    private val mapper: ObjectMapper,
    success: Response.Listener<Array<WordDTO>>,
    error: Response.ErrorListener
) : JsonRequest<Array<WordDTO>>(Method.GET, url, "", success, error) {

    override fun parseNetworkResponse(response: NetworkResponse): Response<Array<WordDTO>> {
        Log.v("REQUEST", String(response.data))
        val currenciesDto = mapper.readValue(String(response.data), Array<WordDTO>::class.java)
        return Response.success(currenciesDto, null)
    }
}
package pt.isel.pdm.li51d.g9.drag



enum class RequestState { IDLE, ONGOING, COMPLETE }

data class RequestResult<R, E>(
        val state: RequestState = RequestState.IDLE,
        val result: R? = null,
        val error: E? = null
)

package otus.homework.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CatsPresenter(
    private val catsService: CatsService
) {

    private var _catsView: ICatsView? = null

    private val presenterScope = CoroutineScope(
        context = Dispatchers.Main + CoroutineName("CatsCoroutine")
    )

    fun onInitComplete() {
        presenterScope.launch {
            val response = runCatching { catsService.getCatFact() }
            withContext(Dispatchers.Main) {
                val fact = response.getOrNull()
                if (response.isSuccess && fact != null) {
                    _catsView?.populate(fact)
                } else {
                    response.exceptionOrNull()?.let { ex -> CrashMonitor.trackWarning(ex.message) }
                }
            }
        }
    }

    fun attachView(catsView: ICatsView) {
        _catsView = catsView
    }

    fun detachView() {
        _catsView = null
        presenterScope.cancel()
    }
}
package otus.homework.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException

class CatsPresenter(
    private val catsService: CatsService
) {

    private var _catsView: ICatsView? = null

    private val presenterScope = CoroutineScope(
        context = Dispatchers.Main + CoroutineName("CatsCoroutine")
    )

    fun onInitComplete() {
        presenterScope.launch {
            try {
                val fact = withContext(Dispatchers.IO) {
                    catsService.getCatFact()
                }
                _catsView?.populate(fact)
            } catch (e: Throwable) {
                handleError(e)
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

    private fun handleError(throwable: Throwable) =
        if (throwable is SocketTimeoutException) {
            _catsView?.showToast(R.string.error_message)
        } else {
            CrashMonitor.trackWarning(throwable.message)
            _catsView?.showToast(throwable.message)
        }
}
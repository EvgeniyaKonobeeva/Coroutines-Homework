package otus.homework.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
                coroutineScope {
                    val catFact = async(Dispatchers.IO) { catsService.getCatFact() }
                    val catImage = async(Dispatchers.IO) { catsService.getCatImage() }
                    _catsView?.populate(catInfo = CatInfo(catFact.await(), catImage.await().first()))
                }
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
import app.cash.turbine.test
import com.fuzz.kedux.Effects
import com.fuzz.kedux.KeduxLoader
import com.fuzz.kedux.LoadingAction
import com.fuzz.kedux.LoadingModel
import com.fuzz.kedux.Store
import com.fuzz.kedux.createSelector
import com.fuzz.kedux.createStore
import com.fuzz.kedux.error
import com.fuzz.kedux.optionalSuccess
import com.fuzz.kedux.success
import com.fuzz.kedux.typedReducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNull
import logAssertEquals as assertEquals

data class State(val product: LoadingModel<Product> = LoadingModel.empty())

val initialLoadingState = State()

val loadingProduct = KeduxLoader<Int, Product>("product") { id -> flowOf(Product(id = id, name = "Product Demo")) }

val reducer = typedReducer { state: State, action: LoadingAction<*, *> ->
    state.copy(product = loadingProduct.reducer.reduce(state.product, action))
}

fun CoroutineScope.getProductSelector() = createSelector(this) { state: State -> state.product }
fun CoroutineScope.getProductSuccessSelector() = getProductSelector().success()
fun CoroutineScope.getProductOptionalSuccessSelector() = getProductSelector().optionalSuccess()
fun CoroutineScope.getProductErrorSelector() = getProductSelector().error()


/**
 * Description:
 */
class LoadingTest : BaseTest() {

    private lateinit var store: Store<State>
    private lateinit var effects: Effects

    @BeforeTest
    fun beforeTest() {
        effects = Effects(loadingProduct.effect)
        store = createStore(reducer, initialLoadingState).also { effects.bindTo(it) }
    }

    @AfterTest
    fun afterTest() {
        effects.clearBindings()
    }

    @Test
    fun testRequestState() = runBlocking {
        store.select(getProductSuccessSelector())
                .test {
                    store.dispatch(loadingProduct.request(5))
                    assertEquals(Product(5, "Product Demo"), expectItem())
                }
    }

    @Test
    fun testRequestClear() = runBlocking {
        store.select(getProductOptionalSuccessSelector())
                .test {
                    assertNull(expectItem())

                    store.dispatch(loadingProduct.request(5))
                    assertNull(expectItem())

                    store.dispatch(loadingProduct.clear)
                    assertNull(expectItem())
                }
    }

    @Test
    fun testErrorState() = runBlocking {
        store.select(getProductErrorSelector())
                .test {
                    val error1 = Error("This is an error")
                    store.dispatch(loadingProduct.error(error1))
                    assertEquals(error1, expectItem())
                }
    }

}


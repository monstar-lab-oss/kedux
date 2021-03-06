package sample

import com.fuzz.kedux.js_react.useSelector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.css.Display.flex
import kotlinx.css.FlexDirection.column
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.functionalComponent
import react.setState
import react.useState
import styled.css
import styled.styledButton
import styled.styledDiv
import styled.styledSpan

external interface MovieState : RState {
    var movies: List<Movie>
    var isMovieAdded: Boolean
}

val SelectorComponent = functionalComponent<RProps> {
    val (movies, setMovies) = useState(listOf<Movie>())
    useSelector(setMovies, moviesSelector)
    div {
        +"Found ${movies.size}"
    }
}

@JsExport
class MoviesComponent : RComponent<RProps, MovieState>() {

    private val componentScope = CoroutineScope(Job() + Dispatchers.Main)

    override fun MovieState.init() {
        movies = listOf()
        isMovieAdded = false
    }

    override fun componentDidMount() {
        store.select(moviesSelector)
                .onEach { setState { movies = it } }
                .launchIn(componentScope)
        store.select(isMovieAddedSelector)
                .onEach { setState { isMovieAdded = it } }
                .launchIn(componentScope)
    }

    override fun componentWillUnmount() {
        componentScope.cancel()
    }

    private fun addMovie() {
        store.dispatch(MovieActions.AddMovie(Movie(0, "Random Movie", "This is a movie")))
    }

    private fun removeMovie() {
        state.movies.lastOrNull()?.let { last ->
            store.dispatch(MovieActions.RemoveMovie(last))
        }
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                display = flex
                flexDirection = column
            }
            styledSpan {
                +"Is Movie Added? ${state.isMovieAdded}"
            }
            state.movies.forEach {
                styledSpan {
                    +it.name
                }
            }
            styledButton {
                +"Add Movie"
                attrs {
                    onClickFunction = { addMovie() }
                }
            }
            styledButton {
                +"Remove Movie"
                attrs {
                    onClickFunction = { removeMovie() }
                }
            }
        }
    }
}

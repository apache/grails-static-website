package example

//tag::rxjavaImports[]
import rx.Observer
import rx.Observable
import grails.rx.web.RxController
import java.util.concurrent.TimeUnit
import groovy.transform.CompileStatic

/**
 * Demo Server Sent Events Controller
 */
@CompileStatic
class TickTockController implements RxController {
//end::rxjavaImports[]

    //tag::rxStream[]
    def index() {
        rx.stream { Observer observer ->

            //tag::rxOnNext[]
            for (i in (0 .. 5)) {
                if (i % 2 == 0) {
                    observer.onNext(
                        rx.render('Tick')
                    )
                }
                else {
                    observer.onNext(
                        rx.render('Tock')
                    )
                }
                sleep 1000
            }
            //end::rxOnNext[]
            //tag::rxOnCompleted[]
            observer.onCompleted()
            //end::rxOnCompleted[]
        }
    }
    //end::rxStream[]

    //tag::interval[]
    @SuppressWarnings('DuplicateNumberLiteral')
    def example() {
        int i = 0
        rx.stream(Observable
                    .interval(1, TimeUnit.SECONDS)
                    .map {
            i++
            if (i % 2 == 0) {
                rx.render('Tick')
            }
            else {
                rx.render('Tock')
            }
        })
    }
    //end::interval[]
}

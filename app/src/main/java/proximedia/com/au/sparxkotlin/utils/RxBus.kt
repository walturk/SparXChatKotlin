
package proximedia.com.au.sparxkotlin.utils

/**
 * Created by pc on 25/10/2017.
 */

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

// Use object so we have a singleton instance
object RxBus {

    private val publisher = PublishSubject.create<Any>()

    fun publish(event: Any) {
        publisher.onNext(event)
    }

    // Listen should return an Observable and not the publisher
    // Using ofType we filter only events that match that class type
    fun <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)

}
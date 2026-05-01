package demo

import com.budjb.rabbitmq.publisher.RabbitMessagePublisher
import groovy.transform.CompileStatic

@CompileStatic
class BookShowInterceptor {
    RabbitMessagePublisher rabbitMessagePublisher  //<1>

    BookShowInterceptor() { //<2>
        match(controller:"book", action:"show")
    }

    boolean after() { //<3>
        final Book book = (Book) model.bookInstance //<4>

        rabbitMessagePublisher.send { //<5>
            routingKey = "bookQueue"
            body = [id: book.id, title: book.title]
        }
        true
    }
}
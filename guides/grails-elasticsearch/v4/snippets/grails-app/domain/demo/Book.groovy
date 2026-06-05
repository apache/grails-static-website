//tag::package[]
package demo
//end::package[]
//tag::importAndClass[]

class Book {
//end::importAndClass[]

    //tag::properties[]
    String title
    String author
    String about
    String href
    //end::properties[]

    //tag::mapping[]
    static mapping = {
        about type: 'text'
    }
    //end::mapping[]

    //tag::searchable[]
    static searchable = {
        title boost: 2.0  // <1>
        about boost: 1.0  // <2>
        except = ['href'] // <3>
    }
    //end::searchable[]
//tag::closeClass[]
}
//end::closeClass[]

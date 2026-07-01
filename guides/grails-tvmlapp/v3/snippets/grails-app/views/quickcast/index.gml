import com.ociweb.quickcasts.Quickcast

model {
    Iterable<Quickcast> quickcasts
}

document {
    stackTemplate {
        banner {
            title this.g.message(code: 'quickcasts.title')
        }
        collectionList {
            shelf {
                section {
                    quickcasts.each { quickcast ->
                        lockup(onselect: "getDocument('quickcast/${quickcast.id}')") {
                            img(src: quickcast.heroImg, width: 150, height: 226)
                            title quickcast.title
                        }
                    }
                }
            }
        }
    }
}
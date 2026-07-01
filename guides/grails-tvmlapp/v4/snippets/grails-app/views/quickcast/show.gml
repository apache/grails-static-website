import com.ociweb.quickcasts.Quickcast

model {
    Quickcast quickcast
    Iterable<Quickcast> relatedQuickcasts
}

document {
    productTemplate {
        banner {
            infoList {
                info {
                    header {
                        title this.g.message(code: 'quickcast.authors.header')
                    }
                    quickcast.authors.each { author ->
                        text "$author"
                    }
                }
            }
            stack {
                title quickcast.title
                subtitle quickcast.subtitle
                row {
                    text "${quickcast.durationMinutes}min ${quickcast.durationSeconds}sec"
                    text quickcast.releaseYear
                }
                description(allowsZooming: "true", moreLabel: "more", "${quickcast.description}")
                row {
                    buttonLockup(onselect: "playMedia('${quickcast.videoUrl}', 'video')") {
                        badge(src: "resource://button-play")
                        title "Play"
                    }
                }
            }
            heroImg(src: quickcast.heroImg)
        }

        shelf {
            header {
                title this.g.message(code: 'quickcast.cross.sell.header')
            }
            section {
                relatedQuickcasts.each { relatedQuickcast ->
                    lockup(onselect: "getDocument('quickcast/${quickcast.id}')") {
                        img(src: relatedQuickcast.heroImg, width: 150, height: 226)
                        title relatedQuickcast.title
                    }
                }
            }
        }
    }
}
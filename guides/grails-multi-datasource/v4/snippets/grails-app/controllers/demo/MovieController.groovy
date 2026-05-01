package demo

import groovy.transform.CompileStatic

@CompileStatic
class MovieController {

    static allowedMethods = [save: 'POST', index: 'GET']

    static responseFormats = ['json']

    MovieService movieService

    MovieDataService movieDataService

    KeywordService keywordService

    def save(SaveMovieCommand cmd) {
        movieService.addMovie(cmd.title, cmd.keywords)
        render status: 201
    }

    def index() {
        [movieList: movieDataService.findAll()]
    }

    def delete(String title) {
        movieDataService.deleteByTitle(title)
        render status: 204
    }

    def keywords() {
        render view: '/keyword/index',
               model: [keywordList: keywordService.findAllDefaultDataSourceKeywords()]
    }
}
package demo

class ConferenceUrlMappings {

    static mappings = {

        group "/conf", {
            get "/talks/$id?"(controller: 'conference', action: 'talks')
            get "/speakers/$id?"(controller: 'conference', action: 'speakers')
            get "/agenda"(controller: 'conference', action: 'agenda')
        }
    }
}

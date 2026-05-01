class Announcement {
    let primaryKey: Int
    let title: String
    var body: String?
    
    init(primaryKey: Int, title: String) {
        self.primaryKey = primaryKey
        self.title = title
    }
}

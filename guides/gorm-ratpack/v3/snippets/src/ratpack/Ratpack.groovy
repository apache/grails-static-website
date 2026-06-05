import demo.BootStrapService
import demo.GormModule
import demo.ManufacturerHandler

import static ratpack.groovy.Groovy.ratpack

ratpack {
  bindings {
    module GormModule
    bindInstance new BootStrapService()
    add(new ManufacturerHandler())
  }
  handlers {
    get(":id/vehicles", ManufacturerHandler)
    get(ManufacturerHandler)
  }
}

package example

import org.grails.datastore.mapping.multitenancy.exceptions.TenantNotFoundException

class UrlMappings {

    static mappings = {
        // tag::home[]
        '/'(controller: 'manufacturer')
        // end::home[]

        // tag::noTenant[]
        '500' (controller: 'manufacturer', exception: TenantNotFoundException)
        // end::noTenant[]

        // tag::manufacturerSelect[]
        "/manufacturer/$id"(controller: 'manufacturer', action: 'select')
        // end::manufacturerSelect[]

        // tag::vehicles[]
        '/vehicles'(resources: 'vehicle')
        // end::vehicles[]

        '500'(view: '/error')
        '404'(view: '/notFound')
    }
}
